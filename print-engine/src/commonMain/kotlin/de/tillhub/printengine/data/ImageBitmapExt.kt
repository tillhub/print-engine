package de.tillhub.printengine.data

import androidx.compose.ui.graphics.ImageBitmap

expect fun ImageBitmap.encodeToBase64(): String

/**
 * Builds an [ImageBitmap] from opaque ARGB [pixels]. The only platform specific step of
 * [normalizeForPrintHead]; everything above it is shared Kotlin.
 */
internal expect fun argbToImageBitmap(
    pixels: IntArray,
    width: Int,
    height: Int,
): ImageBitmap

/**
 * Returns this image prepared for a monochrome thermal print head that is [headWidthPx]
 * dots wide: downscaled to that width when wider, and reduced to 1 bit black and white by
 * Floyd-Steinberg error diffusion.
 *
 * Both steps are lossless as far as the device is concerned - the head prints a dot or no
 * dot, at its own resolution - while typically shrinking the PNG behind a data URI by an
 * order of magnitude. On the HTML printers that is the difference between a receipt that
 * fits into a single IPC transaction and one that is silently dropped.
 *
 * Images at or below [headWidthPx] keep their size: upscaling adds no printable detail and
 * would only grow the payload.
 */
fun ImageBitmap.normalizeForPrintHead(headWidthPx: Int): ImageBitmap {
    if (width <= 0 || height <= 0 || headWidthPx <= 0) return this

    val pixels = IntArray(width * height)
    readPixels(pixels)

    val normalized = MonochromeImage.normalize(
        pixels = pixels,
        sourceWidth = width,
        sourceHeight = height,
        headWidthPx = headWidthPx,
    )
    return argbToImageBitmap(normalized.pixels, normalized.width, normalized.height)
}
