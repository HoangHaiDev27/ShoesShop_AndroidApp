package com.example.basketballshoesandroidshop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.basketballshoesandroidshop.Domain.ChatMessage;
import com.example.basketballshoesandroidshop.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private Context context;
    private LayoutInflater inflater;

    public MessageAdapter(List<ChatMessage> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        // Reset visibility cho tất cả layouts
        holder.userMessageLayout.setVisibility(View.GONE);
        holder.botMessageLayout.setVisibility(View.GONE);
        holder.systemMessageLayout.setVisibility(View.GONE);

        // Hiển thị layout phù hợp dựa trên loại tin nhắn
        switch (message.getMessageType()) {
            case USER:
                holder.userMessageLayout.setVisibility(View.VISIBLE);
                holder.userMessageText.setText(message.getMessage());
                holder.userMessageTime.setText(message.getFormattedTime());
                break;

            case BOT:
                holder.botMessageLayout.setVisibility(View.VISIBLE);
                holder.botMessageText.setText(message.getMessage());
                holder.botMessageTime.setText(message.getFormattedTime());
                break;

            case SYSTEM:
                holder.systemMessageLayout.setVisibility(View.VISIBLE);
                holder.systemMessageText.setText(message.getMessage());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        // User message views
        LinearLayout userMessageLayout;
        TextView userMessageText;
        TextView userMessageTime;

        // Bot message views
        LinearLayout botMessageLayout;
        TextView botMessageText;
        TextView botMessageTime;

        // System message views
        LinearLayout systemMessageLayout;
        TextView systemMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // User message views
            userMessageLayout = itemView.findViewById(R.id.userMessageLayout);
            userMessageText = itemView.findViewById(R.id.userMessageText);
            userMessageTime = itemView.findViewById(R.id.userMessageTime);

            // Bot message views
            botMessageLayout = itemView.findViewById(R.id.botMessageLayout);
            botMessageText = itemView.findViewById(R.id.botMessageText);
            botMessageTime = itemView.findViewById(R.id.botMessageTime);

            // System message views
            systemMessageLayout = itemView.findViewById(R.id.systemMessageLayout);
            systemMessageText = itemView.findViewById(R.id.systemMessageText);
        }
    }
}