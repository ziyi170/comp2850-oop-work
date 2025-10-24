import java.io.FileNotFoundException
import java.io.IOException

const val MAX_ATTEMPTS = 6
const val WORD_FILE = "data/words.txt"
private const val SEPARATOR_LENGTH = 40
private const val SINGLE_ATTEMPT = 1

fun main() {
    println("=".repeat(SEPARATOR_LENGTH))
    println("         WELCOME TO WORDLE")
    println("=".repeat(SEPARATOR_LENGTH))
    println()
    println("Guess the 5-letter word in $MAX_ATTEMPTS attempts!")
    println("Green = correct letter in correct position")
    println("Yellow = correct letter in wrong position")
    println("Gray = letter not in the word")
    println()

    try {
        val words = readWordList(WORD_FILE)
        if (words.isEmpty()) {
            println("Error: No words found in $WORD_FILE")
            return
        }
        val targetWord = pickRandomWord(words)
        var attempts = 0
        var hasWon = false
        while (attempts < MAX_ATTEMPTS && !hasWon) {
            attempts++
            val guess = obtainGuess(attempts)
            val matches = evaluateGuess(guess, targetWord)
            displayGuess(guess, matches)
            if (matches.all { it == 2 }) {
                hasWon = true
            }
            println()
        }
        println("=".repeat(SEPARATOR_LENGTH))
        if (hasWon) {
            println("Congratulations! You guessed the word!")
            println("You solved it in $attempts attempt${if (attempts == SINGLE_ATTEMPT) "" else "s"}!")
        } else {
            println("Game Over!")
            println("The word was: $targetWord")
        }
        println("=".repeat(SEPARATOR_LENGTH))
    } catch (e: FileNotFoundException) {
        println("Error: File not found - ${e.message}")
        println("Please ensure the file '$WORD_FILE' exists.")
    } catch (e: IOException) {
        println("Error: Unable to read file - ${e.message}")
        println("Please ensure the file '$WORD_FILE' is readable.")
    }
}
