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
        val singleStringOrNull by lazy { numberMap[baseTenExponent] }
        val twoSumOrEmpty by lazy { findTwoSum(baseTenExponent) }
        val firstTwoSumString by lazy { numberMap[twoSumOrEmpty[0]] }
        val secondTwoSumString by lazy { numberMap[twoSumOrEmpty[1]] }

        return when {
            singleStringOrNull != null -> "That's as many configurations as $singleStringOrNull!"
            twoSumOrEmpty.size == 2 && firstTwoSumString != null && secondTwoSumString != null ->
                "That's as many configurations as $firstTwoSumString multiplied by $secondTwoSumString!"
            else -> "That's 10^$baseTenExponent different configurations!"
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
            5 to "the number of strands of hair on a human head",
            6 to "the number of 5-card hands that can be dealt from a standard 52-card deck",
            7 to "the number of articles on Wikipedia across all languages",
            9 to "the number of base pairs in the human genome",
            10 to "the number of pennies manufactured by the U.S. Mint each year",
            11 to "the number of planets in the Milky Way",
            12 to "the estimated number of galaxies in the observable universe",
            15 to "the estimated number of ants alive on Earth at any time",
            19 to "the number of configurations for a 3x3x3 Rubik's Cube",
            21 to "the estimated number of grains of sand on all of the world's beaches",
            23 to "the approximate number of stars in the observable universe",
            25 to "the estimated number of water droplets in Earth's seas",
            27 to "the approximate number of atoms in the human body",
            30 to "the estimated number of bacterial cells on Earth",
            31 to "the estimated number of individual viruses on Earth",
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