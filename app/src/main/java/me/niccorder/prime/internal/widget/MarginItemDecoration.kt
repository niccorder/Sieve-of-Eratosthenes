package me.niccorder.prime.internal.widget

import android.content.Context
import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.annotation.NonNull
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Used to draw a margin around items inside of the recycler. This is mainly used inside of
 * recycler's which are based off the GridLayoutManager
 */
class MarginItemDecoration(
        context: Context,
        @DimenRes val dimenId: Int
) : RecyclerView.ItemDecoration() {

    private val offsets = Rect()

    init {
        val margin = pixelSize(context, dimenId)
        offsets.set(margin, margin, margin, margin)
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(offsets)
    }

    private fun pixelSize(@NonNull context: Context, @DimenRes id: Int): Int {
        return context.resources.getDimensionPixelSize(id)
    }
}