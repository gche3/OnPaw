package com.example.onpaw

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var chipQuickSafe: Chip
    private lateinit var chipQuickEta: Chip

    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        Log.d("ChatDebug", "Activity created")

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "John Smith"    // ensure title shows correctly
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        recyclerView = findViewById(R.id.recyclerViewMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        chipQuickSafe = findViewById(R.id.chipQuickSafe)
        chipQuickEta = findViewById(R.id.chipQuickEta)

        messageAdapter = MessageAdapter(messages)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        chipQuickSafe.setOnClickListener {
            handleUserMessage(
                text = "Is my pet safe?",
                route = ReplyRoute.SAFE
            )
        }

        chipQuickEta.setOnClickListener {
            handleUserMessage(
                text = "What's your ETA?",
                route = ReplyRoute.ETA
            )
        }

        btnSend.setOnClickListener {
            val messageText = etMessage.text?.toString()?.trim().orEmpty()
            if (messageText.isNotEmpty()) {
                handleUserMessage(
                    text = messageText,
                    route = ReplyRoute.GENERIC
                )
                etMessage.text?.clear()
            }
        }
    }

    // Different scripted replies
    private enum class ReplyRoute { SAFE, ETA, GENERIC }

    private fun handleUserMessage(text: String, route: ReplyRoute) {
        // User bubble
        addMessage(Message(text = text, isUser = true))

        // Sitter bubble(s)
        when (route) {
            ReplyRoute.SAFE -> {
                // Text then photo
                addMessage(
                    Message(
                        text = "Yes, Rover is safe and sound!",
                        isUser = false
                    )
                )
                addMessage(
                    Message(
                        text = null,
                        isUser = false,
                        hasImage = true
                    )
                )
            }
            ReplyRoute.ETA -> {
                addMessage(
                    Message(
                        text = "I'll be there in about 10 minutes.",
                        isUser = false
                    )
                )
            }
            ReplyRoute.GENERIC -> {
                addMessage(
                    Message(
                        text = "Understood! I'll keep you updated.",
                        isUser = false
                    )
                )
            }
        }
    }

    private fun addMessage(message: Message) {
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    data class Message(
        val text: String?,
        val isUser: Boolean,
        val hasImage: Boolean = false
    )

    class MessageAdapter(private val messages: List<Message>) :
        RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

        companion object {
            private const val VIEW_TYPE_USER = 1
            private const val VIEW_TYPE_SITTER = 2
        }

        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_SITTER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val layoutRes = when (viewType) {
                VIEW_TYPE_USER -> R.layout.item_message_user
                VIEW_TYPE_SITTER -> R.layout.item_message_sitter
                else -> error("Invalid viewType")
            }
            val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }

        override fun getItemCount(): Int = messages.size

        class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessageText: TextView = itemView.findViewById(R.id.tvMessageText)
            private val ivMessageImage: ImageView = itemView.findViewById(R.id.ivMessageImage)

            fun bind(message: Message) {
                // Image bubble
                if (message.hasImage) {
                    ivMessageImage.visibility = View.VISIBLE
                    ivMessageImage.setImageResource(R.drawable.happy_pet)
                } else {
                    ivMessageImage.visibility = View.GONE
                }

                // Text bubble
                if (!message.text.isNullOrBlank()) {
                    tvMessageText.visibility = View.VISIBLE
                    tvMessageText.text = message.text
                } else {
                    tvMessageText.visibility = View.GONE
                }
            }
        }
    }
}
