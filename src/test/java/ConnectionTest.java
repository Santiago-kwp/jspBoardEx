import com.ssg.jspboard.util.DBConnection;
import java.sql.Connection;
import org.junit.jupiter.api.Test;

public class ConnectionTest {

  @Test
  public void testHikariCP() throws Exception {

    Connection connection = DBConnection.INSTANCE.getConnection();

    System.out.println(connection);
    connection.close();
  }


}
