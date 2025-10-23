use sqldb;

CREATE TABLE board_post (
                            post_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
                            title       VARCHAR(200) NOT NULL,
  content     TEXT NOT NULL,
  writer      VARCHAR(50) NOT NULL,
  passphrase  VARCHAR(100) NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
              ON UPDATE CURRENT_TIMESTAMP
);


INSERT INTO board_post(title, content, writer, passphrase)
VALUES ('첫 글입니다', '서블릿/JSP 게시판', '홍길동','1234'),
       ('질문 있어요', 'JDBC 커넥션', '김자바','1111');


select * from board_post;

