package com.margelo.nitro.stroketext.views

import android.view.View
import com.margelo.nitro.R.id.associated_hybrid_view_tag
import com.margelo.nitro.stroketext.HybridStrokeTextView

class StrokeTextViewManager : HybridStrokeTextViewManager() {
  override fun onDropViewInstance(view: View) {
    val hybridView = view.getTag(associated_hybrid_view_tag) as? HybridStrokeTextView
    view.setTag(associated_hybrid_view_tag, null)
    hybridView?.dispose()
    super.onDropViewInstance(view)
  }
}

