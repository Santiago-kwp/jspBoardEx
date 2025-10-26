package com.ssg.jspboard.controller;

import com.ssg.jspboard.dto.PostDTO;
import com.ssg.jspboard.service.PostService;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "postListServlet", urlPatterns = "/posts")
@Log4j2
public class PostListServlet extends HttpServlet {

  private PostService postService = PostService.INSTANCE;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    log.info("doGet");

    try {
      List<PostDTO> dtoList = postService.getList(1,100); // 임의로 일단 지정
      req.setAttribute("dtoList",dtoList);
      req.getRequestDispatcher("/WEB-INF/views/list.jsp").forward(req,resp);

    } catch (IllegalArgumentException e) {
      log.warn("잘못된 요청: {}", e.getMessage());
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청입니다.");

    } catch (NoSuchElementException e) {
      log.warn("데이터 없음: {}", e.getMessage());
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "게시글을 찾을 수 없습니다.");

    } catch (Exception e) {
      log.error("게시글 목록 조회 실패", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }
  }


}
