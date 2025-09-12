package de.tillhub.printengine.data

/**
 * Defines printing intensity (darkness of the print).
 * For PAX devices it is defined as:
 *  DEFAULT: 100%
 *  LIGHT: 50%
 *  DARK: 150%
 *  DARKER: 250%
 *  DARKEST: 500%
 */
enum class PrintingIntensity {
    DEFAULT,
    LIGHT,
    DARK,
    DARKER,
    DARKEST
}
