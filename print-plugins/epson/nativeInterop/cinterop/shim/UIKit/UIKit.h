/**
 * UIKit shim for cinterop commonization.
 *
 * ePOS2.h imports <UIKit/UIKit.h> but only uses UIImage.
 * Importing the full UIKit framework causes the Kotlin/Native commonizer
 * to crash on types that differ across iOS targets (e.g. UIMenuSystemElementGroupPreference).
 *
 * This shim intercepts the import and pulls in only UIImage,
 * so cinterop resolves it as the real platform.UIKit.UIImage type.
 */
#ifndef EPOS2_UIKIT_SHIM_H
#define EPOS2_UIKIT_SHIM_H

#import <UIKit/UIImage.h>

#endif /* EPOS2_UIKIT_SHIM_H */
