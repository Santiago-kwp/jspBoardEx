package com.ssg.jspboard.service;

import com.ssg.jspboard.dao.PostDAO;
import com.ssg.jspboard.dao.PostDAOImpl;
import com.ssg.jspboard.domain.PostVO;
import com.ssg.jspboard.dto.PostDTO;
import com.ssg.jspboard.util.MapperUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

@Log4j2
public enum PostService {
  INSTANCE;
  private final PostDAO dao;
  private ModelMapper modelMapper;


  PostService() {
    this.dao = new PostDAOImpl();
    this.modelMapper = MapperUtil.INSTANCE.get();
  }

  public List<PostDTO> getList(int page, int size) throws Exception {
    List<PostVO> postVOs = dao.findAll(page, size);

    List<PostDTO> dtoList = postVOs.stream().map(
            vo -> modelMapper.map(vo,PostDTO.class)).collect(Collectors.toList());

    return dtoList; }

  public PostDTO getDetail(long id) { // 조회수 증가 포함 아직
    PostVO vo = dao.findById(id)
            .orElseThrow(() -> new NoSuchElementException("해당 게시글이 존재하지 않습니다: id=" + id));

    PostDTO dto = modelMapper.map(vo,PostDTO.class);

    return dto;
  }


  public long write(PostDTO post) { // 검증 + 저장
    // 1. 서버측 유효성 검사
    if (post.getTitle() == null || post.getTitle().length() < 2 || post.getTitle().length() > 200) {
      throw new IllegalArgumentException("제목은 2~200자 사이여야 합니다.");
    }

    if (post.getWriter() == null || post.getWriter().length() < 1 || post.getWriter().length() > 50) {
      throw new IllegalArgumentException("작성자는 1~50자 사이여야 합니다.");
    }

    if (post.getContent() == null || post.getContent().length() < 5) {
      throw new IllegalArgumentException("내용은 5자 이상이어야 합니다.");
    }

    if (post.getPassphrase() == null || post.getPassphrase().length() < 4 || post.getPassphrase().length() > 20) {
      throw new IllegalArgumentException("비밀번호는 4~20자 사이여야 합니다.");
    }

    // 2. DTO → VO 변환
    PostVO vo = modelMapper.map(post, PostVO.class);

    // 3. DB 저장
    long result = dao.save(vo);

    // 4. 저장 성공 시 post_id 반환
    return result;
  }


  public void edit(PostDTO post, String passphrase) {// 비번검증 + 수정

    // 서버측 비밀번호 검사
    Optional<PostVO> original = dao.findById(post.getPostId());

    if (!original.isPresent()) {
      throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
    }

    PostVO existing = original.get();

    if (post.getPassphrase() == null || !Objects.equals(post.getPassphrase(), existing.getPassphrase())) {
      throw new IllegalArgumentException("비밀번호가 같지 않습니다.");
    }

    // 글 수정
    PostVO voUpdated = PostVO.builder()
            .postId(post.getPostId())
            .title(post.getTitle())
            .content(post.getContent())
            .writer(existing.getWriter()) // 기존 작성자 유지
            .passphrase(existing.getPassphrase()) // 기존 비밀번호 유지
            .createdAt(existing.getCreatedAt())
            .updatedAt(LocalDateTime.now()) // 수정 LocalDateTime 기준
            .build();

    boolean success = dao.update(voUpdated);
    if (!success) {
      throw new RuntimeException("게시글 업데이트 중 오류가 발생했습니다.");
    }
  }


  public void remove(long id, String passphrase) {
    // 서버측 비밀번호 검사
    Optional<PostVO> original = dao.findById(id);

    if (!original.isPresent()) {
      throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
    }

    PostVO existing = original.get();

    if (passphrase == null || !Objects.equals(passphrase, existing.getPassphrase())) {
      throw new IllegalArgumentException("비밀번호가 같지 않습니다.");
    }

    boolean success = dao.delete(id);
    if (!success) {
      throw new RuntimeException("게시글 삭제 중 오류가 발생했습니다.");
    }
  }     // 비번검증 + 삭제
}
