package com.example.follower.screens.logs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.follower.databinding.ItemErrorLogBinding
import com.example.follower.databinding.ItemInfoLogBinding
import kotlinx.android.extensions.LayoutContainer

private const val LOG_TYPE_INFO = 1
private const val LOG_TYPE_ERROR = 2

class LogsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var logs: List<LogUi> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = logs.size

    override fun getItemViewType(position: Int): Int = when(logs[position]) {
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
            is ErrorLogViewHolder -> holder.binding?.errorLog = logs[position] as LogUi.ErrorLog
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