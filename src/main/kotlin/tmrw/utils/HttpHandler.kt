package tmrw.utils

abstract class HttpHandler {

    abstract fun post(url: String, body: String)
}