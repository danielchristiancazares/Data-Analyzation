package helpers;

//For git
import java.sql.Connection;
import java.sql.DriverManager;

public class HelperUtils {

    public static String printSuccess(String data) {
        return "<div class=\"alert alert-success\">" + data + "</div>";
    }

    public static String printError(String data) {
        return "<div class=\"alert alert-danger\">" + data + "</div>";
    }

    public static String printInfo(String data) {
        return "<div class=\"alert alert-info\">" + data + "</div>";
    }

    public static String printModal(String id, String title, String data) {
        return "<div class=\"modal fade\" id=\""
                + id
                + "\" aria-hidden=\"true\">"
                + "<div class=\"modal-dialog\">"
                + "<div class=\"modal-content\">"
                + "<div class=\"modal-header\">"
                + "<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>"
                + "<h4 class=\"modal-title\" id=\"myModalLabel\">" + title + "</h4>" + "</div>"
                + "<div class=\"modal-body\">" + data + "</div></div></div></div>";
    }

    public static Connection connect() throws Exception {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://127.7.218.2:5432/jbossews";
        String user = "adminvhdnx7t";
        String password = "C9hwuInPLcxD";
        return DriverManager.getConnection(url, user, password);
    }
}
