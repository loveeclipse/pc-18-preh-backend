package services.utils

object DuplicatedKey {

    private const val DUPLICATED_KEY_CODE = "E11000"

    fun isDuplicateKey(errorMessage: String?) = errorMessage?.startsWith(DUPLICATED_KEY_CODE) ?: false
}