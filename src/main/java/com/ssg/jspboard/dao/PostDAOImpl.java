package com.ssg.jspboard.dao;

import com.ssg.jspboard.domain.PostVO;
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
    public List<PostVO> findAll(int page, int size) {

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

        List<PostVO> postList = new ArrayList<>();

        try(Connection conn = DBConnection.INSTANCE.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ) {

            pstmt.setInt(1, size);
            pstmt.setInt(2, offset);
            // 3. ResultSet을 별도의 try-with-resources에 선언하여 자동 닫기 보장
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PostVO post = PostVO.builder()
                            .postId(rs.getLong("post_id"))
                            .title(rs.getString("title"))
                            .content(rs.getString("content"))
                            .writer(rs.getString("writer"))
                            .passphrase(rs.getString("passphrase"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                            .build();

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
        String sql = "SELECT COUNT(*) FROM board_post";

        try (Connection conn = DBConnection.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<PostVO> findById(long id) {
        String sql = "SELECT * FROM board_post WHERE post_id = ?";

        PostVO post = null;
        try (Connection conn = DBConnection.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    post = PostVO.builder()
                            .postId(rs.getLong("post_id"))
                            .title(rs.getString("title"))
                            .content(rs.getString("content"))
                            .writer(rs.getString("writer"))
                            .passphrase(rs.getString("passphrase"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                            .build();
                    return Optional.of(post);
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 한 개의 글 저장
    public long save(PostVO post) {
        String sql = "INSERT INTO board_post (title, content, writer, passphrase) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setString(3, post.getWriter());
            pstmt.setString(4, post.getPassphrase());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("글 저장 실패: 영향 받은 행 없음");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("글 저장 실패: ID 생성 안됨");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 한 개의 글 업데이트
    @Override
    public boolean update(PostVO post) {
        String sql = "UPDATE board_post SET title = ?, content = ?, updated_at = CURRENT_TIMESTAMP WHERE post_id = ?";

        try (Connection conn = DBConnection.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.setLong(3, post.getPostId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 한개의 글 삭제
    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM board_post WHERE post_id = ?";

        try (Connection conn = DBConnection.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 한개의 글에 비밀번호 확인
    @Override
    public boolean checkPassphrase(long id, String passphrase) {
        String sql = "SELECT COUNT(*) FROM board_post WHERE post_id = ? AND passphrase = ?";

        try (Connection conn = DBConnection.INSTANCE.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.setString(2, passphrase);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
