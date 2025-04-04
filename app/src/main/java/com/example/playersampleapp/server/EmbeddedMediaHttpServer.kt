package com.example.playersampleapp.server

import com.example.playersampleapp.extension.getIP
import com.example.playersampleapp.server.model.ConnectDTO
import com.example.playersampleapp.server.model.DeviceStatusDTO
import com.example.playersampleapp.server.model.ErrorResponceDTO
import com.example.playersampleapp.server.model.MediaCommands
import com.example.playersampleapp.server.model.MuteDTO
import com.example.playersampleapp.server.model.PlayDTO
import com.example.playersampleapp.server.model.PlayerAPIEndpoint
import com.example.playersampleapp.server.model.PlaylistItemDTO
import com.example.playersampleapp.server.model.SeekDTO
import com.example.playersampleapp.server.model.StatusType
import com.example.playersampleapp.server.model.VolumeDTO
import com.example.playersampleapp.shared.MainThreadDispatcher
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

typealias HttpCallbackResponse = (HttpMediaServerEvent<*>?) -> Unit
data class HttpMediaServerEvent<T>(val payload: Result<T>)

data class AddressInfo(val host: String, val port: Int)

interface RequestCallbackChannel {
    fun status() : DeviceStatusDTO?
}

class EmbeddedMediaHttpServer(val endpoints: PlayerAPIEndpoint) {

    var callback : HttpCallbackResponse = {

    }

    var requestCallbackChannel : RequestCallbackChannel = object : RequestCallbackChannel {
        override fun status(): DeviceStatusDTO? {
            return null
        }
    }


    private fun success(mediaCommands: MediaCommands) {
        MainThreadDispatcher.post {
            callback.invoke(HttpMediaServerEvent(Result.success(mediaCommands)))
        }
    }

    private fun error(throwable: Throwable) {
        callback.invoke(HttpMediaServerEvent(Result.failure<Throwable>(throwable)))
    }

    private var engine: ApplicationEngine? = null

    @OptIn(ExperimentalSerializationApi::class)
    fun start() {

        Thread {
            engine = embeddedServer(Netty, port = 0) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    })
                }
                routing {
                    post(endpoints.load) {
                        println("[ACC] server - request load")
                        try {

                            val list = call.receive<List<PlaylistItemDTO>>()
                            success(MediaCommands.Load(list))

                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.play) {
                        println("[ACC] server - request play")
                        try {

                            val payload = call.receive<PlayDTO>()
                            success(MediaCommands.Play(payload))

                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.pause) {
                        println("[ACC] server - request pause")
                        try {

                            success(MediaCommands.Pause)
                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.previous) {
                        println("[ACC] server - request previuos")
                        try {

                            success(MediaCommands.Previous)
                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.next) {
                        println("[ACC] server - request next")
                        try {

                            success(MediaCommands.Next)
                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.seek) {
                        try {
                            val payload = call.receive<SeekDTO>()
                            success(MediaCommands.SeekTo(payload))
                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.stop) {
                        try {
                            success(MediaCommands.Stop)
                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.mute) {
                        try {
                            val payload = call.receive<MuteDTO>()
                            success(MediaCommands.Mute(payload))
                            call.respond(HttpStatusCode.NoContent)
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                    post(endpoints.volume) {
                        try {
                            println("[ACC] server - volume")
                            val volumeDTO = call.receive<VolumeDTO>()

                            success(MediaCommands.SetVolume(volumeDTO))
                            call.respond(HttpStatusCode.NoContent)
                        }catch (e: Exception) {
                            println("[ACC] failure - volume")
                            e.printStackTrace()
                            call.respond(HttpStatusCode.BadRequest, ErrorResponceDTO("Device doesn't support volume level change.", code = null))
                        }
                    }
                    post(endpoints.connect) {
                        try {
                            println("[ACC] server - connect")
                            val connectDTO = call.receive<ConnectDTO>()
                            success(MediaCommands.Connect(connectDTO))
                            connected = true
                            call.respond(HttpStatusCode.OK)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    post(endpoints.disconnect){
                        println("[ACC] server - disconnect")
                        connected = false
                        //todo implement disconnect logic. Should we stop playback?
                        success(MediaCommands.Disconnect())
                        call.respond(HttpStatusCode.OK)
                    }
                    get(endpoints.status) {
                        println("[ACC] server call - status")
                        try {
                            val status = requestCallbackChannel.status()
                            status?.let {
                                call.respond(HttpStatusCode.OK, status)
                            } ?: call.respond(HttpStatusCode.OK, DeviceStatusDTO("1", StatusType.ENDED, 0.0f))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            call.respond(HttpStatusCode.BadRequest, ErrorResponceDTO("Error, unable to get current status.", code = null))
                        }
                    }
                }
            }
            engine?.start(wait = false)

            println("server init - engine created")
            (engine?.application?.environment as ApplicationEngineEnvironment?)?.connectors?.forEach {
                println("server ini, address - ${it.host}:${it.port}")
            }
            val port = runBlocking { engine?.resolvedConnectors()?.firstOrNull()?.port } ?: return@Thread
            println("server init - read - port $port")

            getIP()?.let { ipAdress ->
                callback.invoke(HttpMediaServerEvent(
                    Result.success(AddressInfo(ipAdress, port))
                ))
            }

        }.start()
    }

    private var connected = false

    fun stop() {
        println("server init - stop req")
        engine?.stop()
        println("server init - stop done")
    }
}