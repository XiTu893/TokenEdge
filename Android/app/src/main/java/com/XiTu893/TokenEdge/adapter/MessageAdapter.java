package com.XiTu893.TokenEdge.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.XiTu893.TokenEdge.R;
import com.XiTu893.TokenEdge.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View userMessageLayout;
        View assistantMessageLayout;
        TextView tvUserMessage;
        TextView tvAssistantMessage;

        ViewHolder(View itemView) {
            super(itemView);
            userMessageLayout = itemView.findViewById(R.id.userMessageLayout);
            assistantMessageLayout = itemView.findViewById(R.id.assistantMessageLayout);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvAssistantMessage = itemView.findViewById(R.id.tvAssistantMessage);
        }

        void bind(ChatMessage message) {
            if (message.role == ChatMessage.Role.USER) {
                userMessageLayout.setVisibility(View.VISIBLE);
                assistantMessageLayout.setVisibility(View.GONE);
                tvUserMessage.setText(message.content);
            } else {
                userMessageLayout.setVisibility(View.GONE);
                assistantMessageLayout.setVisibility(View.VISIBLE);
                tvAssistantMessage.setText(message.content);
            }
        }
    }
}
