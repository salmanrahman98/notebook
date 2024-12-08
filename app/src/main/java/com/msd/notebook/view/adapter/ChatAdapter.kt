package com.msd.notebook.view.adapter

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msd.notebook.R
import com.msd.notebook.databinding.ReceiverChatLayoutBinding
import com.msd.notebook.databinding.SenderChatLayoutBinding
import com.msd.notebook.models.ChatMessage
import com.msd.notebook.view.activity.DocumentBotAI_Activity

class ChatAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var chatMessageList: ArrayList<ChatMessage> = ArrayList()

    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
    }

    fun updateMessages(messages: ArrayList<ChatMessage>) {
        chatMessageList = messages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessageList[position].sentByMe) VIEW_TYPE_SENDER else VIEW_TYPE_RECEIVER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_chat_layout, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_chat_layout, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessageList[position]
        if (holder is SenderViewHolder) {
            holder.bind(message)
            holder.binding.senderMessageText.text = message.message
        } else if (holder is ReceiverViewHolder) {
            holder.bind(message)

            val styledText = styleHeadings(message.message)
            holder.binding.receiverMessageText.text = styledText
        }
    }

    override fun getItemCount(): Int = chatMessageList.size

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var binding: SenderChatLayoutBinding
//        private val senderMessageTextView: TextView = itemView.findViewById(R.id.sender_message_text)

        fun bind(message: ChatMessage) {
            binding = SenderChatLayoutBinding.bind(itemView)
//            senderMessageTextView.text = message.message
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var binding: ReceiverChatLayoutBinding
//        private val receiverMessageTextView: TextView = itemView.findViewById(R.id.receiver_message_text)

        fun bind(message: ChatMessage) {
            binding = ReceiverChatLayoutBinding.bind(itemView)
//            receiverMessageTextView.text = message.message
        }
    }

    private fun styleHeadings(text: String): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder()
        val lines = text.lines()

        for (line in lines) {
            val spannableString = SpannableString(line)

            // Check for headings (### or ##)
            if (line.startsWith("### ")) {
                styleHeading(spannableString, 3, 1.2f) // Size increase for ###
            } else if (line.startsWith("## ")) {
                styleHeading(spannableString, 2, 1.3f) // Size increase for ##
            } else if (line.startsWith("# ")) { // Added support for single # heading
                styleHeading(spannableString, 1, 1.4f) // Size increase for #
            }
            // Check for bold text (**)
            val boldRegex = "\\*\\*(.*?)\\*\\*".toRegex()
            val boldMatches = boldRegex.findAll(line)
            for (match in boldMatches) {
                val startIndex = match.range.first
                val endIndex = match.range.last + 1
                spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, 0)
            }


            spannableStringBuilder.append(spannableString)
            spannableStringBuilder.append("\n")
        }

        return spannableStringBuilder
    }

    private fun styleHeading(spannableString: SpannableString, hashCount: Int, sizeMultiplier: Float) {
        val headingText = spannableString.substring(hashCount + 1) // Remove hashes and space
        spannableString.setSpan(StyleSpan(Typeface.BOLD), hashCount + 1, spannableString.length, 0)
        spannableString.setSpan(RelativeSizeSpan(sizeMultiplier), hashCount + 1, spannableString.length, 0)
    }
}