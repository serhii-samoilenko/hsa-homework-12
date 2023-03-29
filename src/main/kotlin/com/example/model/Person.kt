package com.example.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.RandomStringUtils

data class Person(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("first_name")
    val firstName: String,
    @JsonProperty("last_name")
    val lastName: String,
    @JsonProperty("data")
    val data: String,
) {
    companion object {

        fun generateRandom(id: String) = Person(
            id = id,
            firstName = randomName(19),
            lastName = randomName(20),
            data = randomText(
                paragraphs = 5,
                sentences = 5,
                words = 10,
            ),
        )

        private fun randomName(length: Int) = RandomStringUtils.randomAlphabetic(length)
        private fun randomWord() = RandomStringUtils.randomNumeric(8, 17)
        private fun randomSentence(words: Int) = (1..words).joinToString(" ") { randomWord() }.plus(".\n")
        private fun randomParagraph(sentences: Int, words: Int) = (1..sentences).joinToString("") { randomSentence(words) }
        private fun randomText(paragraphs: Int, sentences: Int, words: Int) =
            (1..paragraphs).joinToString("") { randomParagraph(sentences, words) }
    }
}
