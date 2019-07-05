package services.utils

object UriParser {
    fun getLastItemFromUrl(url: String): String {
        return url.replaceFirst(".*/([^/?]+).*".toRegex(), "$1")
    }
}