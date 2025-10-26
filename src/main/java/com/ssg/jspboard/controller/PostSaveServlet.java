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

@WebServlet(name = "postSaveServlet", urlPatterns = "/posts/save")
@Log4j2
public class PostSaveServlet extends HttpServlet {
    PostService postService = PostService.INSTANCE;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            PostDTO postDTO = PostDTO.builder()
                    .title(request.getParameter("title"))
                    .content(request.getParameter("content"))
                    .writer(request.getParameter("writer"))
                    .passphrase(request.getParameter("passphrase"))
                    .build();

            log.info("create Post DTO: " + postDTO);

            long postId = postService.write(postDTO);

            // 성공 시 PRG 패턴 적용
            response.sendRedirect("/posts");

        } catch (IllegalArgumentException e) {
            // ❗ 여기서 오류 메시지를 담고 다시 폼으로 forward
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/formCommon.jsp").forward(request, response);
        }


    }

}
