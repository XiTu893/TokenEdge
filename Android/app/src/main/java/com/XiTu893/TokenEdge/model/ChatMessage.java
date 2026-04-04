package com.XiTu893.TokenEdge.model;

public class ChatMessage {
    public enum Role {
        USER,
        ASSISTANT
    }

    public Role role;
    public String content;

    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }
}
