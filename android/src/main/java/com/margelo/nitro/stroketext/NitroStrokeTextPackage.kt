package com.margelo.nitro.stroketext

import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.BaseReactPackage
import com.facebook.react.ViewManagerOnDemandReactPackage
import com.facebook.react.bridge.ModuleSpec
import com.facebook.react.uimanager.ViewManager
import com.margelo.nitro.stroketext.views.HybridStrokeTextViewManager

class NitroStrokeTextPackage : BaseReactPackage(), ViewManagerOnDemandReactPackage {
    private val viewManagers: Map<String, ModuleSpec> by lazy {
        mapOf(
            "StrokeTextView" to ModuleSpec.viewManagerSpec { HybridStrokeTextViewManager() }
        )
    }

    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? = null

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider = ReactModuleInfoProvider { HashMap() }

    override fun getViewManagers(reactContext: ReactApplicationContext): List<ModuleSpec> {
        return viewManagers.values.toList()
    }

    override fun getViewManagerNames(reactContext: ReactApplicationContext): Collection<String> {
        return viewManagers.keys
    }

    override fun createViewManager(reactContext: ReactApplicationContext, viewManagerName: String) =
        viewManagers[viewManagerName]?.provider?.get() as? ViewManager<*, *>

    companion object {
        init {
            NitroStrokeTextOnLoad.initializeNative()
        }
    }
}
