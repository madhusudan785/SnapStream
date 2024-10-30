package com.videoplayback.videoStream.Payload;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomMessage {

    private boolean success = false;
    private String message;
}