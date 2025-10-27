## 1. 목표 & 요구사항

- 목표: MVC 흐름 이해(요청 → Controller(서블릿) → Service → DAO/JDBC → DB → JSP 응답)
- 게시글 **목록/상세/작성/수정/삭제(CRUD)**, 입력검증, 리다이렉트-PRG 패턴

## 2. DB 테이블  
```sql
   CREATE TABLE board_post (
   post_id     BIGINT PRIMARY KEY AUTO_INCREMENT,   //글번호
   title       VARCHAR(200) NOT NULL,               //글제목
   content     TEXT NOT NULL,                       //글 내용
   writer      VARCHAR(50) NOT NULL,                //글쓴이
   passphrase  VARCHAR(100) NOT NULL,               //수정/삭제용 비밀번호
   created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  //글생성 날짜
   updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP   //글수정 날짜
   ON UPDATE CURRENT_TIMESTAMP
   );
```

## 3. 프로젝트 구조

```xml

src/
 └─ main/
     ├─ java/
     │   └─ com.ssg.board/
     │        ├─ controller/   (서블릿)
     │        ├─ service/      (비즈니스 로직)
     │        ├─ dao/          (JDBC)
     │        ├─ dto/          (PostDTO)
     │        ├─ domain/       (PostVO)
     │        └─ util/         (DBConnection, MapperUtil, UTF8Filter)
     └─ webapp/
         ├─ WEB-INF/
         │    ├─ views/        (JSP: list, detail, formCommon, form, edit.jsp)
         │    └─ web.xml
         └─ resources/        

```

## 4. 구현 결과
### 4.1. 목록(List) 화면 만들기 (GET `/posts`)

- DAO에서 최신순 목록 조회.
- 목록에 **번호, 제목, 작성자, 작성일** 표시. 제목 클릭 시 상세로 이동.
- JSP는 **JSTL/EL** 사용

![image.png](/assets/images/getPosts.png)

### 4.2. 작성(Create) (GET `/posts/new` + POST `/posts/save`)

- **GET**: 작성 폼(JSP) : 제목/작성자/내용
- **POST**: 서버 검증(제목 2 ~ 200, 작성자 1 ~ 50, 내용 5자 이상, 비밀번호 4 ~ 20)
- insert 성공 → **PRG 패턴**으로 `/posts` 리다이렉트

![image.png](/assets/images/getPostForm.png)

- POST : 서버 검증
```java
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

```


   
### 4.3. 상세(Read) (GET `/posts/view?id=...`)

- id로 단건 조회
- 상세에서 **수정/삭제 버튼** 제공.

![image.png](/assets/images/getPostDetail.png)


### 4.4. 수정(Update) (GET `/posts/edit?id=...` + POST `/posts/update`)

- **GET**: 기존 값 바인딩된 폼 출력(제목/내용만 수정하고  작성자 수정은 금지)
- **POST**: **비밀번호(passphrase) 확인** 후 업데이트. 불일치 시 오류 메시지.
- 성공 시 상세 페이지로 리다이렉트.

![image.png](/assets/images/getPostEditForm.png)

- 비밀번호 틀릴 시
![image.png](/assets/images/redirectEditErrorForm.png)

- 성공 시 상세 페이지 리다이렉트
![image.png](/assets/images/getEditSuccess.png)


### 4.5. 삭제(Delete) (POST `/posts/delete`)

- 비밀번호 확인 삭제
- 성공 시 목록으로 리다이렉트. 실패 시 상세로 돌아가 오류 표시.

![image.png](/assets/images/postPostDelete.png)

- 실패 시 비밀번호 오류 표시
  ![image.png](/assets/images/postPostDeleteFail.png)


## 5. 구현 코드
### 5.1 `PostDAOImpl.java`
```java
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

```

### `5.2 PostService.java`
```java
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


  public void edit(PostDTO post, String passphrase) {
    Optional<PostVO> original = dao.findById(post.getPostId());
    if (!original.isPresent()) {
      throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
    }

    PostVO existing = original.get();

    if (post.getPassphrase() == null || !Objects.equals(post.getPassphrase(), existing.getPassphrase())) {
      throw new IllegalArgumentException("비밀번호가 같지 않습니다.");
    }

    // DTO의 값만 기존 VO에 덮어쓰기 => 빌더 패턴 필요 없음. 업데이트 시간은 어차피 DB에서 최신화됨.
    modelMapper.map(post, existing);

    boolean success = dao.update(existing);
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

```


### 5.3 서블릿 매핑
```text
GET /posts → PostListServlet

GET /posts/view → PostDetailServlet

GET /posts/new → PostNewFormServlet

POST /posts/save → PostSaveServlet

GET /posts/edit → PostEditFormServlet

POST /posts/update → PostUpdateServlet

POST /posts/delete → PostDeleteServlet
```

#### 5.3.1 `PostListServlet`
```java
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
```

#### 5.3.2 `PostDetailServlet`
```java
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

```
#### 5.3.3 `PostNewFormServlet`
```java
@WebServlet(name = "postNewFormServlet", urlPatterns = "/posts/new")
@Log4j2
public class PostNewFormServlet extends HttpServlet {

    PostService postService = PostService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/form.jsp").forward(req, resp);
    }
}

```
#### 5.3.4 `PostSaveServlet`
```java
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
            request.getRequestDispatcher("/WEB-INF/views/form.jsp").forward(request, response);
        }


    }

}
```
#### 5.3.5 `PostEditFormServlet`
```java
@WebServlet(name="postEditFormServlet", value="/posts/edit")
@Log4j2
public class PostEditFormServlet extends HttpServlet {
    PostService postService = PostService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            long id = Long.parseLong(req.getParameter("id"));
            PostDTO dto = postService.getDetail(id); // 기존 글 조회
            req.setAttribute("post", dto);
            req.getRequestDispatcher("/WEB-INF/views/edit.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            log.warn("잘못된 ID 형식: {}", req.getParameter("id"));
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 ID입니다.");
        } catch (NoSuchElementException e) {
            log.warn("게시글 없음: {}", e.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "해당 게시글을 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("수정 폼 로딩 실패", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}

```
#### 5.3.6 `PostUpdateServlet`
```java
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
            req.getRequestDispatcher("/WEB-INF/views/edit.jsp").forward(req, resp);

        } catch (Exception e) {
            log.error("게시글 수정 중 오류", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }
}

```
#### 5.3.7 `PostDeleteServlet`
```java
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

```

### 5.4 JSP 뷰 파일
#### 5.4.1 `list.jsp`
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>게시글 목록</title>
	<style>
		table {
			width: 80%;
			border-collapse: collapse;
			margin: 20px auto;
		}
		th, td {
			border: 1px solid #ccc;
			padding: 8px;
			text-align: center;
		}
		th {
			background-color: #f2f2f2;
		}
		a {
			text-decoration: none;
			color: #333;
		}
	</style>
</head>
<body>
<h1 style="text-align:center;">📋 게시글 목록</h1>

<table>
	<thead>
	<tr>
		<th>번호</th>
		<th>제목</th>
		<th>작성자</th>
		<th>작성일</th>
	</tr>
	</thead>
	<tbody>
	<c:forEach var="post" items="${dtoList}" varStatus="status">
		<tr>
			<td>${status.index + 1}</td>
			<td>
				<a href="${pageContext.request.contextPath}/posts/detail?id=${post.postId}">
						${post.title}
				</a>
			</td>
			<td>${post.writer}</td>
			<td>${post.createdAt}</td>
		</tr>
	</c:forEach>
	</tbody>
</table>

<div style="text-align:center;">
	<a href="${pageContext.request.contextPath}/posts/new">✏️ 새 글 작성</a>
</div>
</body>
</html>

```
#### 5.4.2 `detail.jsp`
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>게시글 상세</title>
	<style>
		.container {
			width: 600px;
			margin: 30px auto;
			padding: 20px;
			border: 1px solid #ccc;
			background-color: #f9f9f9;
		}
		.field {
			margin-bottom: 15px;
		}
		.label {
			font-weight: bold;
		}
		.buttons {
			text-align: center;
			margin-top: 20px;
		}
		.buttons button {
			margin: 0 10px;
			padding: 8px 16px;
			background-color: #eee;
			border: 1px solid #ccc;
			color: #333;
			cursor: pointer;
			font-size: 14px;
		}
		.buttons button:hover {
			background-color: #ddd;
		}
		.error {
			color: red;
			margin-bottom: 10px;
			text-align: center;
		}
	</style>
	<script>
		function confirmDelete() {
			const pass = prompt("게시글을 삭제하려면 비밀번호를 입력하세요:");
			if (pass !== null && pass.trim().length > 0) {
				document.getElementById("deletePassphrase").value = pass;
				document.getElementById("deleteForm").submit();
			}
		}
	</script>
</head>
<body>

<div class="container">
	<h2>📄 게시글 상세</h2>
	<c:if test="${not empty error}">
		<div class="error">${error}</div>
	</c:if>

	<div class="field">
		<span class="label">제목:</span> ${post.title}
	</div>

	<div class="field">
		<span class="label">작성자:</span> ${post.writer}
	</div>

	<div class="field">
		<span class="label">작성일:</span> ${post.createdAt}
	</div>

	<div class="field">
		<span class="label">내용:</span><br/>
		<pre>${post.content}</pre>
	</div>

	<div class="buttons">
		<form method="get" action="${pageContext.request.contextPath}/posts/edit" style="display:inline;">
			<input type="hidden" name="id" value="${post.postId}" />
			<button type="submit">✏️ 수정</button>
		</form>

		<form id="deleteForm" method="post" action="${pageContext.request.contextPath}/posts/delete" style="display:inline;">
			<input type="hidden" name="id" value="${post.postId}" />
			<input type="hidden" name="passphrase" id="deletePassphrase" />
			<button type="button" onclick="confirmDelete()">🗑️ 삭제</button>
		</form>

		<form method="get" action="${pageContext.request.contextPath}/posts" style="display:inline;">
			<button type="submit">📋 목록</button>
		</form>
	</div>
</div>


</body>
</html>

```
#### 5.4.3 `form.jsp`
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
	<title>글 작성</title>
	<style>
		form {
			width: 500px;
			margin: 30px auto;
			padding: 20px;
			border: 1px solid #ccc;
			background-color: #f9f9f9;
		}
		label {
			display: block;
			margin-top: 10px;
		}
		input[type="text"], input[type="password"], textarea {
			width: 100%;
			padding: 8px;
			margin-top: 5px;
		}
		button {
			margin-top: 15px;
			padding: 10px 20px;
		}
		.error {
			color: red;
			margin-bottom: 10px;
		}
	</style>
</head>
<body>

<h2 style="text-align:center;">✏️ 새 글 작성</h2>

<form method="post" action="${pageContext.request.contextPath}/posts/save">
	<c:if test="${not empty error}">
		<div class="error">${error}</div>
	</c:if>

	<label for="title">제목</label>
	<input type="text" id="title" name="title" required minlength="2" maxlength="200" />

	<label for="writer">작성자</label>
	<input type="text" id="writer" name="writer" required minlength="1" maxlength="50" />

	<label for="content">내용</label>
	<textarea id="content" name="content" rows="6" required minlength="5"></textarea>

	<label for="passphrase">비밀번호</label>
	<input type="password" id="passphrase" name="passphrase" required minlength="4" maxlength="20" />

	<button type="submit">등록</button>
</form>

<div style="text-align:center;">
	<a href="${pageContext.request.contextPath}/posts">📋 목록으로 돌아가기</a>
</div>

</body>
</html>

```
#### 5.4.4 `edit.jsp`
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>글 수정</title>
  <style>
    form {
      width: 500px;
      margin: 30px auto;
      padding: 20px;
      border: 1px solid #ccc;
      background-color: #f9f9f9;
    }
    label {
      display: block;
      margin-top: 10px;
    }
    input[type="text"], input[type="password"], textarea {
      width: 100%;
      padding: 8px;
      margin-top: 5px;
    }
    button {
      margin-top: 15px;
      padding: 10px 20px;
    }
    .error {
      color: red;
      margin-bottom: 10px;
    }
  </style>
</head>
<body>

<h2 style="text-align:center;">🛠️ 글 수정</h2>

<form method="post" action="${pageContext.request.contextPath}/posts/update">
  <c:if test="${not empty error}">
    <div class="error">${error}</div>
  </c:if>

  <input type="hidden" name="postId" value="${post.postId}" />

  <label for="title">제목</label>
  <input type="text" id="title" name="title" value="${post.title}" required minlength="2" maxlength="200" />

  <label for="writer">작성자</label>
  <input type="text" id="writer" name="writer" value="${post.writer}" readonly />

  <label for="content">내용</label>
  <textarea id="content" name="content" rows="6" required minlength="5">${post.content}</textarea>

  <label for="passphrase">비밀번호(기존 비밀번호)</label>
  <input type="password" id="passphrase" name="passphrase" required minlength="4" maxlength="20" />

  <button type="submit">수정</button>
</form>

<div style="text-align:center;">
  <a href="${pageContext.request.contextPath}/posts/detail?id=${post.postId}">🔙 상세로 돌아가기</a>
</div>

</body>
</html>
```

#### 5.4.5 `formCommon.jsp`
- 하나의 jsp 파일로 글 생성/수정 가능하게 구현
- jstl 조건 분기에 따라 동적 변동되도록 함
- 핵심은 post 객체가 존재하면 수정, 없으면 신규 작성으로 판단
```html
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title><c:choose>
    <c:when test="${not empty post}">글 수정</c:when>
    <c:otherwise>글 작성</c:otherwise>
  </c:choose></title>
  head>
<body>

<h2 style="text-align:center;">
  <c:choose>
    <c:when test="${not empty post}">🛠️ 글 수정</c:when>
    <c:otherwise>✏️ 새 글 작성</c:otherwise>
  </c:choose>
</h2>

<form method="post" action="${pageContext.request.contextPath}/posts/<c:out value='${not empty post ? "update" : "save"}'/>">
  <c:if test="${not empty error}">
    <div class="error">${error}</div>
  </c:if>

  <c:if test="${not empty post}">
    <input type="hidden" name="postId" value="${post.postId}" />
  </c:if>

  <label for="title">제목</label>
  <input type="text" id="title" name="title"
         value="<c:out value='${post.title}'/>"
         required minlength="2" maxlength="200" />

  <label for="writer">작성자</label>
  <input type="text" id="writer" name="writer"
         value="<c:out value='${post.writer}'/>"
         <c:if test="${not empty post}">readonly</c:if>
         required minlength="1" maxlength="50" />

  <label for="content">내용</label>
  <textarea id="content" name="content" rows="6" required minlength="5"><c:out value='${post.content}'/></textarea>

  <label for="passphrase">
    <c:choose>
      <c:when test="${not empty post}">비밀번호(기존 비밀번호)</c:when>
      <c:otherwise>비밀번호</c:otherwise>
    </c:choose>
  </label>
  <input type="password" id="passphrase" name="passphrase" required minlength="4" maxlength="20" />

  <button type="submit">
    <c:choose>
      <c:when test="${not empty post}">수정</c:when>
      <c:otherwise>등록</c:otherwise>
    </c:choose>
  </button>
</form>

<div style="text-align:center;">
  <c:choose>
    <c:when test="${not empty post}">
      <a href="${pageContext.request.contextPath}/posts/detail?id=${post.postId}">🔙 상세로 돌아가기</a>
    </c:when>
    <c:otherwise>
      <a href="${pageContext.request.contextPath}/posts">📋 목록으로 돌아가기</a>
    </c:otherwise>
  </c:choose>
</div>

</body>
</html>

```

