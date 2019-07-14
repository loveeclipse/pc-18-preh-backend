object ServiceData {
    val HOST = System.getenv("MISSIONS_HOST")?.toString() ?: "localhost"
    val PORT = System.getenv("PORT")?.toInt() ?: 10001
}