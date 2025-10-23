package com.ssg.jspboard.dao;

import com.ssg.jspboard.dto.PostDTO;
import com.ssg.jspboard.util.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;


import java.util.Optional;

public class PostDAOImpl implements PostDAO {
    @Override
    public List<PostDTO> findAll(int page, int size) {

        // 1. OFFSET 계산 (페이지 번호는 1부터 시작한다고 가정)
        // 예를 들어 page=1, size=10 이면 offset=0
        // 예를 들어 page=2, size=10 이면 offset=10
        int offset = (page - 1) * size;

        // 2. SQL 쿼리문: 최신 글을 먼저 보여주기 위해 post_id를 기준으로 내림차순 정렬 후,
        // OFFSET 만큼 건너뛰고 LIMIT 만큼 데이터를 가져옵니다.
        String sql = "SELECT post_id, title, content, writer, passphrase, created_at, updated_at " +
                "FROM board_post " +
                "ORDER BY post_id DESC " + // 최신순 정렬
                "LIMIT ? OFFSET ?";

        List<PostDTO> postList = new ArrayList<>();

        try(Connection conn = DBConnection.INSTANCE.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, offset);
            // 3. ResultSet을 별도의 try-with-resources에 선언하여 자동 닫기 보장
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PostDTO post = new PostDTO();
                    post.setPostId(rs.getLong("post_id"));
                    post.setTitle(rs.getString("title"));
                    post.setContent(rs.getString("content"));
                    post.setWriter(rs.getString("writer"));
                    // passphrase는 보안상 목록에서는 제외하거나 null로 설정하는 것이 일반적이지만,
                    // DTO에 필드가 있으므로 일단 가져오는 구문은 포함합니다.
                    post.setPassphrase(rs.getString("passphrase"));

                    // TIMESTAMP/DATETIME -> LocalDateTime 변환
                    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

                    postList.add(post);
                }
                }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return postList;
    }

    @Override
    public boolean countAll() {
        return false;
    }

    @Override
    public Optional<PostDTO> findById(long id) {
        return Optional.empty();
    }

    @Override
    public long save(PostDTO post) {
        return 0;
    }

    @Override
    public boolean update(PostDTO post) {
        return false;
    }

    @Override
    public boolean delete(long id) {
        return false;
    }

    @Override
    public boolean checkPassphrase(long id, String passphrase) {
        return false;
    }
}
