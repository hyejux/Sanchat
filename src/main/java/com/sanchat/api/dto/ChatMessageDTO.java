package com.sanchat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDTO {

    public enum MessageType {
        ENTER, CHAT, EXIT
    }

    private MessageType messageType;
    private Long chatRoomNo;
    private Long userNo;
    private String messageContent;
    private LocalDateTime sendAt;
}
