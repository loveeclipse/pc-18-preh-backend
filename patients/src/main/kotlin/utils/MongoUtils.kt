package utils

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

object MongoUtils {

    private const val DUPLICATED_KEY_CODE = "E11000"
    val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to System.getenv("MONGO_CONNECTION_STRING")?.toString()
    ) }

    fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false

    fun checkSchema(json: JsonObject, required: List<String>?, parameters: List<String>): Boolean {
        required?.forEach { key -> if (!json.containsKey(key)) return false }
        return parameters.containsAll(json.fieldNames())
    }
}