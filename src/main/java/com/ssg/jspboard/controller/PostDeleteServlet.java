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

@WebServlet(name="postDeleteServlet", value = "/posts/delete")
@Log4j2
public class PostDeleteServlet extends HttpServlet {

    PostService postService = PostService.INSTANCE;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            PostDTO dto = postService.getDetail(Long.parseLong(req.getParameter("id")));
            postService.remove(dto.getPostId(),req.getParameter("passphrase"));
            // 삭제 성공 → 목록으로 리다이렉트
            resp.sendRedirect(req.getContextPath() + "/posts");
        } catch (IllegalArgumentException e) {
            log.warn("삭제 실패: {}", e.getMessage());
            // 다시 상세 페이지로 forward + 오류 메시지 전달
            try {
                long id = Long.parseLong(req.getParameter("id"));
                PostDTO dto = postService.getDetail(id); // 다시 조회해서 상세 출력

                req.setAttribute("post", dto);
                req.setAttribute("error", e.getMessage());
                req.getRequestDispatcher("/WEB-INF/views/detail.jsp").forward(req, resp);
            } catch (Exception ex) {
                log.error("상세 페이지 복귀 실패", ex);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            }

        } catch (Exception e) {
                log.error("게시글 삭제 중 오류", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}
