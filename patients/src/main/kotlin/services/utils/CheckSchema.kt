package services.utils

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

object CheckSchema {
    fun checkSchema(json: JsonObject, required: List<String>?, parameters: List<String>): Boolean {
        required?.forEach { key -> if (!json.containsKey(key)) return false }
        return parameters.containsAll(json.fieldNames())
    }

    inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
        return enumValues<T>().any { it.toString() == name }
    }

    inline fun <reified T : Enum<T>> getAndCheckName(routingContext: RoutingContext, string: String): Boolean {
        val name = routingContext.request().getParam(string)
        return enumContains<T>(name)
    }
}