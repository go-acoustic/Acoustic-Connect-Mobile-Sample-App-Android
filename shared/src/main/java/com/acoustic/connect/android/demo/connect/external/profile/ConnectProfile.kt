/*
 * Copyright (C) 2026 Acoustic, L.P. All rights reserved.
 *
 * Licensed under the Acoustic License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy
 * at https://www.acoustic.com/licenses/acoustic-license
 *
 * Sample app provided "as is", without warranty of any kind.
 */
package com.acoustic.connect.android.demo.connect.external.profile

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Where a profile reads and writes the SDK's configuration: [SdkConfigStore] in the apps. */
interface ConnectConfigStore {

    /** The value of [key] in [module]'s configuration, or null when there is none. */
    fun read(module: String, key: String): String?

    /** Sets [key] in [module]'s configuration; false when the SDK does not take the write. */
    fun write(module: String, key: String, value: String): Boolean
}

/**
 * A named set of SDK configuration overrides, read from `assets/connect-profiles/<name>.json`.
 *
 * ```
 * {
 *   "description": "…",                                  optional, ignored
 *   "set":   { "<module>": { "<key>": <value> } },       replaces the key's value
 *   "merge": { "<module>": { "<key>": { …object… } } }   merged into the key's JSON object
 * }
 * ```
 *
 * A `set` value that is not a string is written as its JSON text, which is how the SDK keeps
 * `AutoLayout`. A `merge` replaces only the leaves it names: the objects it shares with the current
 * value are merged, every other value, a list included, is replaced.
 */
class ConnectProfile private constructor(
    val name: String,
    private val overrides: List<ConfigOverride>,
) {

    /** The `module.key` of each override. */
    val keys: List<String> get() = overrides.map { "${it.module}.${it.key}" }

    /**
     * Writes every override to [store], in the file's order, `set` before `merge`.
     *
     * @return the `module.key` of each override the store did not take.
     */
    fun applyTo(store: ConnectConfigStore): List<String> = overrides
        .filterNot { it.applyTo(store) }
        .map { "${it.module}.${it.key}" }

    private sealed class ConfigOverride(val module: String, val key: String) {

        abstract fun applyTo(store: ConnectConfigStore): Boolean

        class Replace(module: String, key: String, private val value: String) :
            ConfigOverride(module, key) {

            override fun applyTo(store: ConnectConfigStore) = store.write(module, key, value)
        }

        class Merge(module: String, key: String, private val patch: JSONObject) :
            ConfigOverride(module, key) {

            override fun applyTo(store: ConnectConfigStore): Boolean {
                val current = store.read(module, key)?.let { parseObject(it) } ?: return false
                return store.write(module, key, mergeObjects(current, patch).toString())
            }

            private fun parseObject(text: String): JSONObject? =
                try {
                    JSONObject(text)
                } catch (e: JSONException) {
                    null
                }
        }
    }

    companion object {

        private const val SET = "set"
        private const val MERGE = "merge"
        private val TOP_LEVEL_KEYS = setOf("description", SET, MERGE)

        /** @throws JSONException when [json] is not a profile. */
        fun parse(name: String, json: String): ConnectProfile {
            val root = JSONObject(json)
            val unknown = entries(root).map { it.first }.filterNot { it in TOP_LEVEL_KEYS }
            if (unknown.isNotEmpty()) throw JSONException("unknown top-level keys $unknown")

            val replaces = overridesOf(root, SET) { module, key, value ->
                ConfigOverride.Replace(module, key, textOf("$SET.$module.$key", value))
            }
            val merges = overridesOf(root, MERGE) { module, key, value ->
                val patch = value as? JSONObject
                    ?: throw JSONException("$MERGE.$module.$key must be an object")
                ConfigOverride.Merge(module, key, patch)
            }
            return ConnectProfile(name, replaces + merges)
        }

        private fun overridesOf(
            root: JSONObject,
            section: String,
            build: (module: String, key: String, value: Any) -> ConfigOverride,
        ): List<ConfigOverride> {
            if (!root.has(section)) return emptyList()
            val modules = root.opt(section) as? JSONObject
                ?: throw JSONException("\"$section\" must be an object")
            return entries(modules).flatMap { (module, keys) ->
                val moduleKeys = keys as? JSONObject
                    ?: throw JSONException("$section.$module must be an object")
                entries(moduleKeys).map { (key, value) -> build(module, key, value) }
            }
        }

        private fun textOf(path: String, value: Any): String = when (value) {
            is String -> value
            is Boolean, is Number, is JSONObject, is JSONArray -> value.toString()
            else -> throw JSONException("$path must be a string, number, boolean, object or array")
        }

        private fun entries(json: JSONObject): List<Pair<String, Any>> =
            json.keys().asSequence().map { it to json.get(it) }.toList()
    }
}

/** [patch] merged into a copy of [base]: shared objects merge, any other value is replaced. */
internal fun mergeObjects(base: JSONObject, patch: JSONObject): JSONObject {
    val merged = JSONObject(base.toString())
    patch.keys().forEach { key ->
        val patched = patch.get(key)
        val current = merged.opt(key)
        merged.put(
            key,
            if (patched is JSONObject && current is JSONObject) mergeObjects(current, patched) else patched,
        )
    }
    return merged
}
