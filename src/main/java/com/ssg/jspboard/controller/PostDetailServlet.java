package com.ssg.jspboard.controller;

import com.ssg.jspboard.dto.PostDTO;
import com.ssg.jspboard.service.PostService;
import lombok.extern.log4j.Log4j2;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.NoSuchElementException;

@WebServlet(name="postDetailServlet", value="/posts/detail")
@Log4j2
public class PostDetailServlet extends HttpServlet {

    PostService postService = PostService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            PostDTO dto = postService.getDetail(Integer.parseInt(req.getParameter("id")));
            req.setAttribute("post", dto);
            req.getRequestDispatcher("/WEB-INF/views/detail.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            log.warn("잘못된 ID 형식: {}", req.getParameter("id"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");

        } catch (NoSuchElementException e) {
            log.warn("게시글 없음: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "해당 게시글을 찾을 수 없습니다.");

        } catch (Exception e) {
            log.error("게시글 조회 중 오류", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}
