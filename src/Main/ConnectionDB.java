package Main;

import javax.print.MultiDocPrintService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface ConnectionDB {
    String url = "jdbc:mysql://localhost:3306/ats";
    String username = "root";
    String password = "abcd1234";

    void Entry(String url,String username,String password) throws SQLException;
}
