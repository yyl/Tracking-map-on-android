package com.yyl.myrmex.tracking

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import com.google.android.maps.ItemizedOverlay
import com.google.android.maps.OverlayItem

class MyMapOverlay(defaultMarker: Drawable) : ItemizedOverlay<OverlayItem>(boundCenterBottom(defaultMarker)) {

    private val mOverlays = ArrayList<OverlayItem>()
    private var mContext: Context? = null

    constructor(defaultMarker: Drawable, context: Context) : this(defaultMarker) {
        mContext = context
    }

    override fun createItem(i: Int): OverlayItem {
        return mOverlays[i]
    }

    override fun size(): Int {
        return mOverlays.size
    }

    fun addOverlay(overlay: OverlayItem) {
        mOverlays.add(overlay)
        populate()
    }

    override fun onTap(index: Int): Boolean {
        val item = mOverlays[index]
        val dialog = AlertDialog.Builder(mContext)
        dialog.setTitle(item.title)
        dialog.setMessage(item.snippet)
        dialog.show()
        return true
    }
}
