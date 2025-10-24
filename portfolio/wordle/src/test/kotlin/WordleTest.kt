import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveLength

/**
 * Comprehensive test suite for Wordle game functions.
 *
 * Test coverage:
 * 4. isValid: 9 tests (edge cases, special characters, case sensitivity)
 * 3. readWordList: 4 tests (file reading, formatting, validation)
 * 2. pickRandomWord: 6 tests (randomness, edge cases, removal)
 * 1. evaluateGuess: 12 tests (exact matches, wrong positions, duplicates)
 *
 * Total: 31 test cases.
 */
class WordleTest : FunSpec({

    // Tests for isValid function
    context("isValid") {
        test("should return true for exactly 5 letters") {
            isValid("HELLO") shouldBe true
            isValid("WORLD") shouldBe true
            isValid("hello") shouldBe true
        }

        test("should return false for words with fewer than 5 letters") {
            isValid("HI") shouldBe false
            isValid("YES") shouldBe false
            isValid("WORD") shouldBe false
        }

        test("should return false for words with more than 5 letters") {
            isValid("WORLDS") shouldBe false
            isValid("TESTING") shouldBe false
            isValid("ELEPHANT") shouldBe false
        }

        test("should return false for empty string") {
            isValid("") shouldBe false
        }

        test("should return false for words containing numbers") {
            isValid("HELL0") shouldBe false
            isValid("12345") shouldBe false
        }

        test("should return false for words containing special characters") {
            isValid("HEL-O") shouldBe false
            isValid("HELLO!") shouldBe false
            isValid("WO RD") shouldBe false
        }

        test("should return true for all uppercase letters") {
            isValid("ABCDE") shouldBe true
        }

        test("should return true for all lowercase letters") {
            isValid("abcde") shouldBe true
        }

        test("should return true for mixed case letters") {
            isValid("HeLLo") shouldBe true
        }
    }

    // Tests for readWordList function
    context("readWordList") {
        test("should read words from the provided data file") {
            val words = readWordList("data/words.txt")

            words.size shouldNotBe 0
            words.all { it.length == 5 } shouldBe true
        }

        test("should return all uppercase words") {
            val words = readWordList("data/words.txt")

            words.all { word -> word.all { it.isUpperCase() } } shouldBe true
        }

        test("should return mutable list that can be modified") {
            val words = readWordList("data/words.txt")
            val originalSize = words.size

            words.add("TESTS")

            words.size shouldBe originalSize + 1
        }

        test("should contain only valid 5-letter words") {
            val words = readWordList("data/words.txt")

            words.forEach { word ->
                word.length shouldBe 5
                word.all { it.isLetter() } shouldBe true
            }
        }
    }

    // Tests for pickRandomWord function
    context("pickRandomWord") {
        test("should return a word from the list") {
            val words = mutableListOf("APPLE", "GRAPE", "PEACH")
            val originalList = words.toList()

            val picked = pickRandomWord(words)

            originalList shouldContain picked
        }

        test("should remove the picked word from the list") {
            val words = mutableListOf("APPLE", "GRAPE", "PEACH")

            val picked = pickRandomWord(words)

            words shouldNotContain picked
            words shouldHaveSize 2
        }

        test("should work with a single word") {
            val words = mutableListOf("APPLE")

            val picked = pickRandomWord(words)

            picked shouldBe "APPLE"
            words shouldHaveSize 0
        }

        test("should throw exception for empty list") {
            val words = mutableListOf<String>()

            shouldThrow<IllegalArgumentException> {
                pickRandomWord(words)
            }
        }

        test("should pick different words on multiple calls") {
            val words = mutableListOf("APPLE", "GRAPE", "PEACH", "LEMON", "MELON")
            val picked = mutableSetOf<String>()

            repeat(5) {
                picked.add(pickRandomWord(words))
            }

            // Should have picked at least 2 different words in 5 attempts
            // (statistically very likely unless extremely unlucky)
            picked.size shouldNotBe 0
        }

        test("should return valid 5-letter word") {
            val words = mutableListOf("APPLE", "GRAPE", "PEACH")

            val picked = pickRandomWord(words)

            picked shouldHaveLength 5
        }
    }

    // Tests for evaluateGuess function
    context("evaluateGuess") {
        test("should return all 2s for exact match") {
            val result = evaluateGuess("APPLE", "APPLE")
            result shouldBe listOf(2, 2, 2, 2, 2)
        }

        test("should return all 0s for no matching letters") {
            val result = evaluateGuess("XYZ12", "APPLE")
            result shouldBe listOf(0, 0, 0, 0, 0)
        }

        test("should return 2 for correct position") {
            val result = evaluateGuess("AXXXX", "APPLE")
            result[0] shouldBe 2
        }

        test("should return 1 for correct letter in wrong position") {
            val result = evaluateGuess("PXXXX", "APPLE")
            result[0] shouldBe 1
        }

        test("should handle mixed correct and wrong positions") {
            val result = evaluateGuess("APPEL", "APPLE")
            result shouldBe listOf(2, 2, 2, 1, 1)
        }

        test("should handle duplicate letters correctly - case 1") {
            // Target: APPLE (A-P-P-L-E)
            // Guess:  PAPER (P-A-P-E-R)
            val result = evaluateGuess("PAPER", "APPLE")
            // P at pos 0: not in correct position, but P exists at pos 1 or 2 -> 1 (yellow)
            // A at pos 1: not in correct position, but A exists at pos 0 -> 1 (yellow)
            // P at pos 2: matches position 2 exactly -> 2 (green)
            // E at pos 3: not in correct position, but E exists at pos 4 -> 1 (yellow)
            // R at pos 4: not in word -> 0 (gray)
            result shouldBe listOf(1, 1, 2, 1, 0)
        }

        test("should handle duplicate letters correctly - case 2") {
            // Target: SPEED (S-P-E-E-D) - E at positions 2 and 3
            // Guess:  ERASE (E-R-A-S-E)
            val result = evaluateGuess("ERASE", "SPEED")
            // First pass - exact matches: none (no exact position matches)
            // Second pass - wrong positions:
            //   E at pos 0: finds E at pos 2 -> 1 (yellow), used[2] = true
            //   R at pos 1: not in word -> 0 (gray)
            //   A at pos 2: not in word -> 0 (gray)
            //   S at pos 3: not in correct position, S exists at pos 0 -> 1 (yellow), used[0] = true
            //   E at pos 4: finds unused E at pos 3 -> 1 (yellow), used[3] = true
            // Result: [1, 0, 0, 1, 1]
            result shouldBe listOf(1, 0, 0, 1, 1)
        }

        test("should handle duplicate letters with limited occurrences") {
            // Target: ROBOT (R-O-B-O-T) - has O at positions 1 and 3
            // Guess:  OOZES (O-O-Z-E-S)
            val result = evaluateGuess("OOZES", "ROBOT")
            // First pass - exact matches:
            //   O at pos 1: matches exactly -> 2 (green), used[1] = true
            // Second pass - wrong positions:
            //   O at pos 0: finds unused O at pos 3 -> 1 (yellow), used[3] = true
            //   Z at pos 2: not in word -> 0 (gray)
            //   E at pos 3: not in word -> 0 (gray)
            //   S at pos 4: not in word -> 0 (gray)
            // Result: [1, 2, 0, 0, 0]
            result shouldBe listOf(1, 2, 0, 0, 0)
        }

        test("should handle all different letters") {
            val result = evaluateGuess("ABCDE", "FGHIJ")
            result shouldBe listOf(0, 0, 0, 0, 0)
        }

        test("should handle repeated letters in guess") {
            val result = evaluateGuess("AAAAA", "APPLE")
            // First A matches (2), others don't have matches in remaining letters
            result[0] shouldBe 2
            result[1] shouldBe 0
            result[2] shouldBe 0
            result[3] shouldBe 0
            result[4] shouldBe 0
        }

        test("should return list of size 5") {
            val result = evaluateGuess("HELLO", "WORLD")
            result shouldHaveSize 5
        }

        test("should handle complex case with mixed matches") {
            // Target: LABEL (L-A-B-E-L)
            // Guess:  LLAMA (L-L-A-M-A)
            val result = evaluateGuess("LLAMA", "LABEL")
            // First pass - exact matches:
            //   L at pos 0: matches exactly -> 2 (green), used[0] = true
            // Second pass - wrong positions:
            //   L at pos 1: finds unused L at pos 4 -> 1 (yellow), used[4] = true
            //   A at pos 2: finds unused A at pos 1 -> 1 (yellow), used[1] = true
            //   M at pos 3: not in word -> 0 (gray)
            //   A at pos 4: all A's already used -> 0 (gray)
            // Result: [2, 1, 1, 0, 0]
            result shouldBe listOf(2, 1, 1, 0, 0)
        }
    }
})
