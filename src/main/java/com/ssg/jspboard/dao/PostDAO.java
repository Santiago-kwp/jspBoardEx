package com.ssg.jspboard.dao;

import com.ssg.jspboard.domain.PostVO;

import java.util.List;
import java.util.Optional;

public interface PostDAO {
  List<PostVO> findAll(int page, int size);
  boolean countAll();
  Optional<PostVO> findById(long id);
  long save(PostVO post);
  boolean  update(PostVO post);
  boolean  delete(long id);
  boolean checkPassphrase(long id, String passphrase);
}
