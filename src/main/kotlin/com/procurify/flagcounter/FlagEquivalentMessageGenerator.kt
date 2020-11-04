package com.procurify.flagcounter

import kotlin.math.log10

object FlagEquivalentMessageGenerator {

    /**
     * Returns the message string for the number of configurations for the input [flagCount]
     * TODO Separate out formatting of message from calculation for easier testing
     */
    fun getNearestNumberMessage(flagCount: Int): String {

        // Convert the number of binary flags into the corresponding base ten power
        val baseTenExponent = (flagCount * log10(2.0)).toInt()

        return numberMap[baseTenExponent]?.let {
            "That's as many configurations as $it!"
        } ?: findTwoSum(baseTenExponent).let {
            if (it.size == 2) {
                val firstString = numberMap[it[0]]
                val secondString = numberMap[it[1]]
                if (firstString != null && secondString != null) {
                    "That's as many configurations as $firstString multiplied by $secondString!"
                } else {
                    null
                }
            } else {
                null
            } ?: "That's 10^$baseTenExponent different configurations!"
        }
    }

    /**
     * Finds the two sum solution for the [target] integer using the [numberMap]. Returns an empty array if no match
     * is found
     */
    private fun findTwoSum(target: Int): IntArray {
        val keyArray = numberMap.keys.toIntArray()
        val map = mutableMapOf<Int, Int>()
        keyArray.forEachIndexed { index, number ->
            map[number]?.let { return intArrayOf(keyArray[it], keyArray[index]) }
            map[target - number] = index
        }
        return intArrayOf()
    }

    //Mapping of powers of 10 to visualization aiding strings
    //Taken from: https://en.wikipedia.org/wiki/Orders_of_magnitude_(numbers)
    private val numberMap = hashMapOf(
            1 to "the number of fingers on a typical human",
            2 to "the number of chemical elements on the periodic table",
            3 to "the number of known extant works by Johann Sebastian Bach",
            4 to "the average number of neurons that each neuron is connected to in the human brain",
            38 to "the total number of different possible keys in the AES 128-bit key space",
            40 to "the least Common Multiple of every integer from 1-100",
            44 to "the Second Cullen Prime",
            45 to "the possible permutations for the Rubik's Revenge",
            46 to "the proven upper bound for the number of legal chess positions",
            50 to "the estimated number of atoms in Earth",
            53 to "the order of the monster group",
            57 to "the total number of different possible keys in the AES 192-bit key space",
            60 to "roughly the number of Planck time intervals since the universe began",
            63 to "Archimedes' estimate of the total number of grains of sand that could fit into the entire cosmos",
            67 to "the number of ways to order the cards in a 52-card deck",
            68 to "the possible combinations for the Megaminx",
            72 to "the largest known prime factor found by ECM factorization"
    )
}