package com.youngsbook.common.recyclerview

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.youngsbook.R

class ItemClickSupport(recyclerView: RecyclerView) {
    private var mRecyclerView: RecyclerView? = recyclerView
    private var mOnItemClickListener: OnItemClickListener? = null
    private val mOnDoubleClickListener: OnDoubleClickListener = object : OnDoubleClickListener() {
        override fun onDoubleClick(v: View?) {
            if (mOnItemClickListener != null) {
                val holder: RecyclerView.ViewHolder = mRecyclerView!!.getChildViewHolder(v!!)
                mOnItemClickListener!!.onItemDoubleClicked(mRecyclerView,
                    holder.absoluteAdapterPosition,
                    v)
            }
        }

        override fun onSingleClick(v: View?) {
            if (mOnItemClickListener != null) {
                val holder: RecyclerView.ViewHolder = mRecyclerView!!.getChildViewHolder(v!!)
                mOnItemClickListener!!.onItemClicked(mRecyclerView, holder.adapterPosition, v)
            }
        }

    }

    private val mAttachListener: RecyclerView.OnChildAttachStateChangeListener =
        object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (mOnItemClickListener != null) {
                    view.setOnClickListener(mOnDoubleClickListener)
                    view.setOnLongClickListener{
                        val holder: RecyclerView.ViewHolder = mRecyclerView!!.getChildViewHolder(it!!)
                        mOnItemClickListener!!.onItemLongClicked(mRecyclerView, holder.adapterPosition, it)
                        return@setOnLongClickListener true
                    }
                }
            }
            override fun onChildViewDetachedFromWindow(view: View) {
            }
        }

    fun setOnItemClickListener(listener: OnItemClickListener?): ItemClickSupport {
        mOnItemClickListener = listener
        return this
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(mAttachListener)
        view.setTag(R.id.tag_accessibility_actions, null)
    }

    interface OnItemClickListener {
        fun onItemClicked(recyclerView: RecyclerView?, position: Int, v: View?)
        fun onItemDoubleClicked(recyclerView: RecyclerView?, position: Int, v: View?)
        fun onItemLongClicked(recyclerView: RecyclerView?, position: Int, v: View?)
    }

    init {
        mRecyclerView = recyclerView
//        mRecyclerView!!.setTag(R.id.item_click_support, this)
        mRecyclerView!!.addOnChildAttachStateChangeListener(mAttachListener)
    }
}