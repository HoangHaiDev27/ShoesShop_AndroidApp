// MessageAdapter.java
package com.example.basketballshoesandroidshop.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshoesandroidshop.Domain.ChatMessage;
import com.example.basketballshoesandroidshop.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private Context context;

    public MessageAdapter(List<ChatMessage> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo layout programmatically thay vì dùng XML
        LinearLayout itemView = new LinearLayout(context);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        itemView.setPadding(16, 8, 16, 8);
        itemView.setOrientation(LinearLayout.HORIZONTAL);

        TextView messageText = new TextView(context);
        messageText.setId(View.generateViewId());
        messageText.setPadding(20, 15, 20, 15);
        messageText.setTextSize(16);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        itemView.addView(messageText, textParams);

        return new MessageViewHolder(itemView, messageText);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        holder.messageText.setText(message.getMessage());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageText.getLayoutParams();

        if (message.isFromUser()) {
            // User message - right side, blue
            params.gravity = Gravity.END;
            holder.messageText.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
            holder.messageText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            params.setMargins(100, 5, 10, 5);
        } else {
            // Bot message - left side, gray
            params.gravity = Gravity.START;
            holder.messageText.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            holder.messageText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            params.setMargins(10, 5, 100, 5);
        }

        holder.messageText.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView, TextView messageText) {
            super(itemView);
            this.messageText = messageText;
        }
    }
}