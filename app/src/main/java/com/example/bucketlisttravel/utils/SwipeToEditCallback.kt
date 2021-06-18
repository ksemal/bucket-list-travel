package com.example.bucketlisttravel.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bucketlisttravel.R

abstract class SwipeToEditCallback(private val context: Context, private val directions: Int) :
    ItemTouchHelper.SimpleCallback(0, directions) {

    private val editIcon = getDrawable(directions)
    private val intrinsicWidth: Int = (editIcon?.intrinsicWidth) ?: 0
    private val intrinsicHeight: Int = (editIcon?.intrinsicHeight) ?: 0
    private val background = ColorDrawable()
    private val backgroundColor = getBackgroundColor(directions)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        /**
         * To disable "swipe" for specific item return 0 here.
         * For example:
         * if (viewHolder?.itemViewType == YourAdapter.SOME_TYPE) return 0
         * if (viewHolder?.adapterPosition == 0) return 0
         */
        if (viewHolder.adapterPosition == 10) return 0
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(
                c,
                getSide(directions, itemView) + dX,
                itemView.top.toFloat(),
                getSide(directions, itemView).toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the edit/delete background
        background.color = backgroundColor
        background.setBounds(
            getSide(directions, itemView) + dX.toInt(),
            itemView.top,
            getSide(directions, itemView),
            itemView.bottom
        )
        background.draw(c)

        // Calculate position of edit/delete icon
        val editIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val editIconMargin = (itemHeight - intrinsicHeight) / 2
        val editIconLeft = getSide(
            directions,
            itemView
        ) + (editIconMargin - intrinsicWidth * getModulo(directions)) * getModulo(directions)
        val editIconRight = getSide(directions, itemView) + (editIconMargin) * getModulo(directions)
        val editIconBottom = editIconTop + intrinsicHeight

        // Draw the edit/delete icon
        editIcon?.setBounds(editIconLeft, editIconTop, editIconRight, editIconBottom)
        editIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }

    private fun getDrawable(direction: Int): Drawable? {
        return when (direction) {
            ItemTouchHelper.RIGHT -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_edit_white_24dp
            )
            ItemTouchHelper.LEFT -> ContextCompat.getDrawable(context, android.R.drawable.ic_delete)
                .also { it?.setTint(context.getColor(R.color.white)) }
            else -> null
        }
    }

    private fun getBackgroundColor(direction: Int): Int {
        return when (direction) {
            ItemTouchHelper.RIGHT -> Color.parseColor("#8570A1")
            ItemTouchHelper.LEFT -> Color.parseColor("#A5064C")
            else -> 0
        }
    }

    private fun getSide(direction: Int, itemView: View): Int {
        return when (direction) {
            ItemTouchHelper.RIGHT -> itemView.left
            ItemTouchHelper.LEFT -> itemView.right
            else -> 0
        }
    }

    private fun getModulo(direction: Int): Int {
        return when (direction) {
            ItemTouchHelper.RIGHT -> 1
            ItemTouchHelper.LEFT -> -1
            else -> 1
        }
    }

}