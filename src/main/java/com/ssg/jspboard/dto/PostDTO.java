package com.ssg.jspboard.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
