package service

import io.netty.handler.codec.http.HttpResponseStatus

import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext

import java.util.UUID

object RequestManager {
    private var vertx: Vertx? = null
    private val CONFIG = JsonObject().put("connection_string", "mongodb://loveeclipse:PC-preh2019@ds149676.mlab.com:49676/heroku_jw7pjmcr")

    fun initializeRequestManager(vertx: Vertx) {
        RequestManager.vertx = vertx
    }

    fun handleNewPatient(routingContext: RoutingContext) {

    }

    fun handleGetPatientData(routingContext: RoutingContext){

    }

    fun handleUpdatePatientData(routingContext: RoutingContext){

    }


}