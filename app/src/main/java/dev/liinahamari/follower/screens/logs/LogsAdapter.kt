package dev.liinahamari.follower.screens.logs

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding4.view.clicks
import dev.liinahamari.follower.R
import dev.liinahamari.follower.databinding.ItemErrorLogBinding
import dev.liinahamari.follower.databinding.ItemInfoLogBinding
import dev.liinahamari.follower.ext.throttleFirst
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
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
            is ErrorLogViewHolder -> holder.bind(position)
            is InfoLogViewHolder -> holder.binding?.infoLog = logs[position] as LogUi.InfoLog
            else -> throw IllegalStateException()
        }
    }

    private inner class ErrorLogViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private val binding: ItemErrorLogBinding? = DataBindingUtil.bind(containerView)

        fun bind(position: Int) {
            binding?.errorLog = logs[position] as LogUi.ErrorLog

            itemView.doOnLayout {
                if (errorItemHeight == -1) {
                    errorItemHeight = (itemView.height - itemView.context.resources.getDimensionPixelSize(R.dimen.arrow_button_height)) / 2
                    arrowBtn.layoutParams = (arrowBtn.layoutParams as ConstraintLayout.LayoutParams).apply {
                        setMargins(0, errorItemHeight, 0, 0)
                    }
                }
            }

            if (errorItemHeight != -1) {
                /** expedient duplicated code */
                arrowBtn.layoutParams = (arrowBtn.layoutParams as ConstraintLayout.LayoutParams).apply {
                    setMargins(0, errorItemHeight, 0, 0)
                }
            }

            with(expandedMarkers[position]) {
                arrowBtn.rotation = if (this) 180f else 0f
                expandableLayout.setExpanded(this, false)
            }

            clicks += itemView.clicks()
                .throttleFirst()
                .map { expandableLayout.isExpanded.not() }
                .subscribe {
                    expandedMarkers.put(position, it)
                    notifyItemChanged(position)
                }
        }
    }

    private inner class InfoLogViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        val binding: ItemInfoLogBinding? = DataBindingUtil.bind(containerView)
    }
}