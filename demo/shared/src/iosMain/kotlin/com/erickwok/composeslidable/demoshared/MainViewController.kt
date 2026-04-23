package com.erickwok.composeslidable.demoshared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("unused")
fun MainViewController(): UIViewController =
    ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
        }
    ) { DemoApp() }
