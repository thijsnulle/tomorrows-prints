package utility.http

abstract class HttpHandler {

    abstract fun post(url: String, body: String)
}