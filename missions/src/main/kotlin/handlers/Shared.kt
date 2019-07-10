package handlers

import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

object Shared {
    const val MISSIONS_COLLECTION = "missions"
    const val FAILED_VALIDATION_MESSAGE = "Document failed validation"
    val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to System.getenv("MONGO_CONNECTION_STRING")?.toString()
    ) }
}
