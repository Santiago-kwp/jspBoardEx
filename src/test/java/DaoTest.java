import com.ssg.jspboard.dao.PostDAO;
import com.ssg.jspboard.dao.PostDAOImpl;
import org.junit.jupiter.api.Test;

public class DaoTest {


    @Test
    public void findAllPostsTest() {
        PostDAO postDAO = new PostDAOImpl();
        postDAO.findAll(1, 10);

    }
}
