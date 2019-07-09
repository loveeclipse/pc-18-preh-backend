package handlers

import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

object Shared {
    const val MISSIONS_COLLECTION = "missions"
    const val FAILED_VALIDATION_MESSAGE = "Document failed validation"
    val MONGODB_CONFIGURATION = json { obj(
            "connection_string" to "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr"
    ) }
}
