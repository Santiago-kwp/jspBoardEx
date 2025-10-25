package com.ssg.jspboard.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostVO {
    private Long postId;
    private String title;
    private String content;
    private String writer;
    private String passphrase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
