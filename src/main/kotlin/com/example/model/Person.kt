package com.example.model

import org.apache.commons.lang3.RandomStringUtils
import kotlin.random.Random

data class Person(
    val id: String? = null,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val phoneNumber: String,
    val cv: String,
) {
    companion object {
        fun generateRandom() = generateRandom(
            firstName = randomName(19),
            lastName = randomName(20),
        )

        fun generateRandom(firstName: String, lastName: String) = Person(
            firstName = firstName,
            lastName = lastName,
            age = randomAge(),
            phoneNumber = randomPhoneNumber(),
            cv = randomText(
                paragraphs = Random.nextInt(3, 4),
                sentences = Random.nextInt(5, 10),
                words = Random.nextInt(11, 17),
            ),
        )

        private fun randomName(length: Int) = RandomStringUtils.randomAlphabetic(length)
        private fun randomAge() = Random.nextInt(0, 100)
        private fun randomPhoneNumber() = RandomStringUtils.randomNumeric(10)
        private fun randomWord() = RandomStringUtils.randomNumeric(8, 17)
        private fun randomSentence(words: Int) = (1..words).joinToString(" ") { randomWord() }.plus(".\n")
        private fun randomParagraph(sentences: Int, words: Int) = (1..sentences).joinToString("") { randomSentence(words) }
        private fun randomText(paragraphs: Int, sentences: Int, words: Int) =
            (1..paragraphs).joinToString("") { randomParagraph(sentences, words) }
    }
}
