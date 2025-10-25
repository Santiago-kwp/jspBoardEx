package com.ssg.jspboard.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
  private Long postId;
  private String title;
  private String content;
  private String writer;
  private String passphrase;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  // getters/setters
}
