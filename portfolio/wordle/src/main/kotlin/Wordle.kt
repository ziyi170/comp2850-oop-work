import java.io.File

private const val WORD_LENGTH = 5

/**
 * Checks if a word is valid for Wordle (exactly 5 letters).
 *
 * @param word The word to validate
 * @return true if the word consists of exactly 5 letters, false otherwise
 */
fun isValid(word: String): Boolean = word.length == WORD_LENGTH && word.all { it.isLetter() }

/**
 * Reads a list of valid Wordle words from the specified file.
 *
 * @param filename The path to the file containing words
 * @return A mutable list of words read from the file
 */
fun readWordList(filename: String): MutableList<String> = File(filename)
    .readLines()
    .map { it.trim().uppercase() }
    .filter { it.isNotEmpty() }
    .toMutableList()

/**
 * Picks a random word from the given list and removes it.
 *
 * @param words The list of words to choose from
 * @return A randomly selected word from the list
 */
fun pickRandomWord(words: MutableList<String>): String {
    require(words.isNotEmpty()) { "Word list cannot be empty" }
    val randomIndex = words.indices.random()
    return words.removeAt(randomIndex)
}

/**
 * Prompts the user to enter a guess and validates it.
 *
 * @param attempt The current attempt number
 * @return A valid 5-letter word entered by the user
 */
fun obtainGuess(attempt: Int): String {
    while (true) {
        print("Attempt $attempt: ")
        val input = readln().trim().uppercase()
        if (isValid(input)) {
            return input
        }
        println("Invalid input. Please enter exactly 5 letters.")
    }
}

/**
 * Evaluates a guess against the target word using full Wordle rules.
 *
 * Returns a list of integers indicating the match status for each letter:
 * - 0: Letter is not in the target word
 * - 1: Letter is in the target word but in the wrong position
 * - 2: Letter is in the correct position
 *
 * @param guess The guessed word
 * @param target The target word to match against
 * @return A list of 5 integers representing the match status
 */
fun evaluateGuess(guess: String, target: String): List<Int> {
    val result = MutableList(WORD_LENGTH) { 0 }
    val targetChars = target.toCharArray()
    val used = BooleanArray(WORD_LENGTH) { false }

    // First pass: mark exact matches (green)
    guess.indices.forEach { i ->
        if (guess[i] == targetChars[i]) {
            result[i] = 2
            used[i] = true
        }
    }

    // Second pass: mark wrong position matches (yellow)
    guess.indices.forEach { i ->
        if (result[i] == 0) {
            targetChars.indices.find { j -> !used[j] && guess[i] == targetChars[j] }?.let { j ->
                result[i] = 1
                used[j] = true
            }
        }
    }

    return result
}

/**
 * Displays the guess with visual feedback based on match results.
 *
 * @param guess The guessed word
 * @param matches The list of match statuses from evaluateGuess
 */
fun displayGuess(guess: String, matches: List<Int>) {
    val greenBackground = "\u001B[42m"
    val yellowBackground = "\u001B[43m"
    val grayBackground = "\u001B[100m"
    val reset = "\u001B[0m"
    val bold = "\u001B[1m"

    for (i in guess.indices) {
        val color = when (matches[i]) {
            2 -> greenBackground
            1 -> yellowBackground
            else -> grayBackground
        }
        print("$color$bold ${guess[i]} $reset")
    }
    println()
}
