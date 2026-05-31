package com.zahran.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.json.*
@PublishedApi internal val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * Utility to convert raw JVM/Firebase types to Kotlinx Serialization JsonElements.
 */
fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Map<*, *> -> this.toJsonObject()
    is List<*> -> JsonArray(this.map { it.toJsonElement() })
    else -> JsonPrimitive(this.toString())
}

/**
 * Converts a Map representation of a document into a JsonObject.
 */
fun Map<*, *>.toJsonObject(): JsonObject {
    val map = mutableMapOf<String, JsonElement>()
    for ((key, value) in this) {
        if (key is String) {
            map[key] = value.toJsonElement()
        }
    }
    return JsonObject(map)
}

/**
 * Maps a Firestore document data map to a target Serializable class using Kotlinx Serialization.
 */
inline fun <reified T> Map<String, Any?>.toSerializableObject(): T {
    val jsonObject = this.toJsonObject()
    return json.decodeFromJsonElement(jsonObject)
}

/**
 * Maps a Serializable object back to a Firestore-compatible Map.
 */
inline fun <reified T> T.toFirestoreMap(): Map<String, Any?> {
    val jsonElement = Json.encodeToJsonElement(this)
    return (jsonElement as JsonObject).toMap()
}

/**
 * Maps a Firestore JsonObject back to a standard Map representation.
 */
fun JsonObject.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    for ((key, value) in this) {
        map[key] = when (value) {
            is JsonNull -> null
            is JsonPrimitive -> {
                if (value.isString) value.content
                else value.booleanOrNull ?: value.longOrNull ?: value.doubleOrNull
            }
            is JsonObject -> value.toMap()
            is JsonArray -> value.map { it.toString() } // simplifications for lists of strings
        }
    }
    return map
}

/**
 * Extension to convert a DocumentSnapshot directly to a Serializable object.
 */
inline fun <reified T> DocumentSnapshot.toSerializableObject(): T? {
    val dataMap = this.data ?: return null
    // Add the document ID to the object mapping if we want to ensure it has the correct ID
    val mutableData = dataMap.toMutableMap()
    if ("id" !in mutableData || mutableData["id"] == "") {
        mutableData["id"] = this.id
    }
    return mutableData.toSerializableObject()
}
