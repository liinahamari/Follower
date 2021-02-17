package com.example.follower.screens.logs

import android.os.Handler
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.follower.R
import com.example.follower.databinding.ItemErrorLogBinding
import com.example.follower.databinding.ItemInfoLogBinding
import com.example.follower.ext.dpToPx
import com.example.follower.ext.pxToDp
import com.example.follower.ext.throttleFirst
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_error_log.*

private const val LOG_TYPE_INFO = 1
private const val LOG_TYPE_ERROR = 2

class LogsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val clicks = CompositeDisposable()
    private lateinit var expandedMarkers: SparseBooleanArray
    private var errorItemHeight = -1

    var logs: List<LogUi> = emptyList()
        set(value) {
            field = value
            expandedMarkers = SparseBooleanArray(value.size)
            notifyDataSetChanged()
        }

    override fun getItemCount() = logs.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) = clicks.clear()

    override fun getItemViewType(position: Int): Int = when (logs[position]) {
        is LogUi.InfoLog -> LOG_TYPE_INFO
        is LogUi.ErrorLog -> LOG_TYPE_ERROR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        LOG_TYPE_INFO -> InfoLogViewHolder(ItemInfoLogBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
        LOG_TYPE_ERROR -> ErrorLogViewHolder(ItemErrorLogBinding.inflate(LayoutInflater.from(parent.context), parent, false).root)
        else -> throw IllegalStateException()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ErrorLogViewHolder -> {
                holder.itemView.doOnLayout {
                    if (errorItemHeight == -1) {
                        errorItemHeight =(holder.itemView.height - holder.itemView.context.resources.getDimensionPixelSize(R.dimen.arrow_button_height)) / 2
                        holder.arrowBtn.layoutParams = (holder.arrowBtn.layoutParams as ConstraintLayout.LayoutParams).apply {
                            setMargins(0, errorItemHeight, 0, 0)
                        }
                    }
                }

                if (errorItemHeight != -1) {
                    holder.arrowBtn.layoutParams = (holder.arrowBtn.layoutParams as ConstraintLayout.LayoutParams).apply {
                        setMargins(0, errorItemHeight, 0, 0)
                    }
                }
                holder.binding?.errorLog = logs[position] as LogUi.ErrorLog
                holder.expandableLayout.setExpanded(expandedMarkers[position], false)

                clicks += holder.itemView.clicks()
                    .throttleFirst()
                    .map { holder.expandableLayout.isExpanded.not() }
                    .subscribe {
                        expandedMarkers.put(position, it)
                        notifyItemChanged(position)
                    }
            }

            is InfoLogViewHolder -> holder.binding?.infoLog = logs[position] as LogUi.InfoLog

            else -> throw IllegalStateException()
        }
    }

    class ErrorLogViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemErrorLogBinding? = DataBindingUtil.bind(containerView)
    }

    class InfoLogViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemInfoLogBinding? = DataBindingUtil.bind(containerView)
    }
}