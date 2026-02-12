package util

fun isRightPassword(password: String): Boolean {
    if (password.length < 8) return false
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    return hasLetter && hasDigit
}