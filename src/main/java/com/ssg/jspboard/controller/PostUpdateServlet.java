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

@WebServlet(name = "postUpdateServlet", urlPatterns = "/posts/update")
@Log4j2
public class PostUpdateServlet extends HttpServlet {

    PostService postService = PostService.INSTANCE;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try{
            PostDTO dto = PostDTO.builder()
                    .postId(Long.parseLong(req.getParameter("postId")))
                    .title(req.getParameter("title"))
                    .content(req.getParameter("content"))
                    .passphrase(req.getParameter("passphrase"))
                    .build();

            log.info("postDTO: {} updated", dto);

            postService.edit(dto, dto.getPassphrase()); // 비밀번호 검증 + 수정

            // 성공 시 상세 페이지로 리다이렉트
            resp.sendRedirect(req.getContextPath() + "/posts/detail?id=" + dto.getPostId());

        } catch (IllegalArgumentException e) {
            log.warn("수정 실패: {}", e.getMessage());
            req.setAttribute("error", e.getMessage());

            // 값 유지
            PostDTO dto = PostDTO.builder()
                    .postId(Long.parseLong(req.getParameter("postId")))
                    .title(req.getParameter("title"))
                    .content(req.getParameter("content"))
                    .writer(req.getParameter("writer"))
                    .build();

            req.setAttribute("post", dto);
            // 실패 시 데이터를 가지고 다시 수정 페이지로 이동
            req.getRequestDispatcher("/WEB-INF/views/formCommon.jsp").forward(req, resp);

        } catch (Exception e) {
            log.error("게시글 수정 중 오류", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}
