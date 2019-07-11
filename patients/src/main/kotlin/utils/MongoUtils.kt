package utils

import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

object MongoUtils {

    private const val DUPLICATED_KEY_CODE = "E11000"
    const val FAILED_VALIDATION_MESSAGE = "Document failed validation"
    val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to System.getenv("MONGO_CONNECTION_STRING")?.toString()
    ) }

    fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false
}