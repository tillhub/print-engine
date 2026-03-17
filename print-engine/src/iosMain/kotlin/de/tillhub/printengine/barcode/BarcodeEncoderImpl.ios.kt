package de.tillhub.printengine.barcode

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.CoreImage.CIFilter
import platform.Foundation.NSClassFromString
import platform.Foundation.NSData
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.setValue
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.darwin.NSObject
import platform.posix.memcpy
import org.jetbrains.skia.Image as SkiaImage

internal actual class BarcodeEncoderImpl : BarcodeEncoder {
    @OptIn(ExperimentalForeignApi::class)
    actual override fun encodeAsBitmap(
        content: String,
        type: BarcodeType,
        imgWidth: Int,
        imgHeight: Int,
    ): ImageBitmap? {
        val filterName = when (type) {
            BarcodeType.QR_CODE -> "CIQRCodeGenerator"
            BarcodeType.CODE_128 -> "CICode128BarcodeGenerator"
        }

        val filter = createCIFilter(filterName) ?: run {
            Logger.e("Failed to create CIFilter: $filterName")
            return null
        }

        val messageData = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: return null
        filter.setValue(messageData, forKey = "inputMessage")

        if (type == BarcodeType.QR_CODE) {
            filter.setValue("Q", forKey = "inputCorrectionLevel")
        }

        val outputImage = filter.outputImage ?: run {
            Logger.e("CIFilter produced no output image")
            return null
        }

        // Scale to desired dimensions
        val extent = outputImage.extent
        val extentWidth = CGRectGetWidth(extent)
        val extentHeight = CGRectGetHeight(extent)
        if (extentWidth <= 0.0 || extentHeight <= 0.0) return null

        val scaleX = imgWidth.toDouble() / extentWidth
        val scaleY = imgHeight.toDouble() / extentHeight
        val scaledImage = outputImage.imageByApplyingTransform(
            CGAffineTransformMakeScale(scaleX, scaleY),
        )

        // Render CIImage to a bitmap-backed UIImage
        val ciBasedImage = UIImage(cIImage = scaledImage)
        val targetSize = CGSizeMake(imgWidth.toDouble(), imgHeight.toDouble())

        UIGraphicsBeginImageContextWithOptions(targetSize, true, 1.0)
        ciBasedImage.drawInRect(CGRectMake(0.0, 0.0, imgWidth.toDouble(), imgHeight.toDouble()))
        val renderedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        val pngData = renderedImage?.let { UIImagePNGRepresentation(it) } ?: run {
            Logger.e("Failed to create PNG representation")
            return null
        }

        // Convert NSData → ByteArray → Skia Image → Compose ImageBitmap
        val bytes = pngData.toByteArray()
        return try {
            SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        } catch (e: Exception) {
            Logger.e("Failed to decode barcode image", e)
            null
        }
    }

    /**
     * Creates a CIFilter via ObjC runtime +[CIFilter filterWithName:].
     * K/N bindings don't expose this factory method, so we call it
     * via performSelector on the CIFilter class object.
     */
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    @Suppress("UNCHECKED_CAST")
    private fun createCIFilter(name: String): CIFilter? {
        val cls = NSClassFromString("CIFilter") as? NSObject ?: return null
        val sel = NSSelectorFromString("filterWithName:")
        return cls.performSelector(sel, withObject = name) as? CIFilter
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
    }
}
