package com.youngsbook.common

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youngsbook.common.recyclerview.ItemClickSupport
import kotlin.math.max
import kotlin.math.min

open class RecyclerViewAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    //    var viewHolder : RecyclerView.ViewHolder
    val currentItem : T?
        get() = if (0 > _position || _arrayList.size <= _position) {
            null
        } else _arrayList[_position]

    // 아이템 위치의 차이가 10칸 미만이면 부드럽게 이동하고 차이가 크면 바로 이동한다.
    var selection: Int
        get() = _position
        set(position) {
            if (0 > position || _arrayList.size <= position) {
                return
            }

            val prevPosition = _position
            _position = position
            listView!!.post {
                this.notifyItemChanged(prevPosition)
                this.notifyItemChanged(_position)
            }

            if (null != listView) {
//                listView!!.scrollToPosition(_position)
                if (10 > Math.abs(_position - prevPosition) && _position != 0)
                    listView!!.smoothScrollToPosition(_position)
                else
                    listView!!.scrollToPosition(_position)
            }
        }

    val isFirstElement: Boolean
        get() = _arrayList.isEmpty() || _position <= 0

    val isLastElement: Boolean
        get() = _arrayList.isEmpty() || _position >= _arrayList.size - 1

    private var _onItemTapListener: OnItemTapListener? = null
    private var _position = -1

    private var _onGetViewListener: OnGetViewListener<T>? = null


    var _arrayList = ArrayList<T>()
    var listView: RecyclerView? = null
        // row click listener
        @SuppressLint("ClickableViewAccessibility")
        set(listView) {
            listView!!.adapter = this
            field = listView
            field?.let {

                // row click listener
                if (_onItemTapListener != null) {

                    ItemClickSupport(it)
                        .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                            override fun onItemClicked(
                                recyclerView: RecyclerView?,
                                position: Int,
                                v: View?
                            ) {
                                // do it
                                notifyItemChanged(_position)
                                _position = position
                                notifyItemChanged(_position)
                                _onItemTapListener?.onSingleTap(position)

                            }


                            override fun onItemDoubleClicked(
                                recyclerView: RecyclerView?,
                                position: Int,
                                v: View?
                            ) {
                                notifyItemChanged(_position)
                                _position = position
                                notifyItemChanged(_position)
                                _onItemTapListener?.onDoubleTap(position)

                            }

                            override fun onItemLongClicked(
                                recyclerView: RecyclerView?,
                                position: Int,
                                v: View?
                            ) {
                                notifyItemChanged(_position)
                                _position = position
                                notifyItemChanged(_position)
                                _onItemTapListener?.onLongTap(position)
                            }
                        })

                }
            }
        }

    interface OnItemTapListener {
        fun onDoubleTap(position: Int)
        fun onSingleTap(position: Int)
        fun onLongTap(position: Int)
    }

    interface OnGetViewListener<T> {
        fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
            item: T,
            focused: Boolean
        ): View?
    }

    fun setOnItemTapListener(listener: OnItemTapListener?) {
        _onItemTapListener = listener
    }

    fun isExist(key: T): Boolean {
        //int i = _arrayList.indexOf(key);
        return _arrayList.contains(key)//i>0? true:false;
    }
    fun onClickEvent(position: Int){
        _onItemTapListener?.onSingleTap(position)
    }

    fun getItem(position: Int): T {
        return _arrayList[position]
    }

    fun clear() {
        _position = -1
        _arrayList.clear()
        this.notifyDataSetChanged()
    }

    fun addItem(jobject: T) {
        _arrayList.add(jobject)
        this.notifyDataSetChanged()
    }

    fun addItem(position: Int, jobject: T) {
        _arrayList.add(position, jobject)
        this.notifyDataSetChanged()
    }

    fun removeItem(position: Int): T? {
        val item = _arrayList.removeAt(position)
        if (null != item) {
            this.notifyDataSetChanged()
        }
        _position = -1
        return item
    }

    fun replaceItem(position: Int, item: T) {
        if (null != _arrayList.removeAt(position)) {
            _arrayList.add(position, item)
            this.notifyDataSetChanged()
        }
    }

    fun refreshItem() {
        this.notifyDataSetChanged()
    }

    fun front(): T? {
        if (_arrayList.isNotEmpty()) {
            _position = 0
            this.notifyDataSetChanged()

            listView?.scrollToPosition(_position)

            return _arrayList[_position]
        }
        return null
    }

    //
    fun back(): T? {
        if (_arrayList.isNotEmpty()) {
            _position = _arrayList.size - 1
            this.notifyDataSetChanged()

            listView?.scrollToPosition(_position)
        }

        return null
    }

    fun prev(): Boolean {
        if (_arrayList.isNotEmpty() && _position > 0) {
            _position--
            listView?.scrollToPosition(max(_position - 1, 0))
            this.notifyDataSetChanged()
        }
        return _position > 0
    }

    operator fun next(): Boolean {
        if (_arrayList.isNotEmpty() && _position < _arrayList.size - 1) {
            _position++
            listView?.scrollToPosition(min(_position + 1, _arrayList.size - 1))
            this.notifyDataSetChanged()
        }
        return _position < _arrayList.size - 1
    }

    override fun getItemCount(): Int {
        return _arrayList.size
    }

    fun getCount(): Int {
        return _arrayList.size
    }

    override fun onBindViewHolder(p0: VH, p1: Int) {

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VH {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
