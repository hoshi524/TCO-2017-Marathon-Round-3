import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQL {

    private static final String url = "jdbc:mysql://localhost/test";
    private static final String username = "root";
    private static final String password = "";
    private Connection connection;
    private Statement statement;

    MySQL() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void insert(int bottles, int poison, int strips, int rounds, int size, double expect, int time) {
        try {
            statement.executeUpdate(String.format(
                    "INSERT INTO `tco-2017-marathon-round-3-data`(`bottles`, `poison`, `strips`, `rounds`, `size`, `expect`, `time`) VALUES (%d,%d,%d,%d,%d,%f,%d)",
                    bottles,
                    poison,
                    strips,
                    rounds,
                    size,
                    expect,
                    time
            ));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class SizeData {
        int bottles;
        int poison;
        int strips;
        int rounds;
        int size;
        double expect;
        int time;

        SizeData(ResultSet result) throws SQLException {
            bottles = result.getInt("bottles");
            poison = result.getInt("poison");
            strips = result.getInt("strips");
            rounds = result.getInt("rounds");
            size = result.getInt("size");
            expect = result.getDouble("expect");
            time = result.getInt("time");
        }
    }

    List<SizeData> select() {
        try {
            List<SizeData> result = new ArrayList<>();
            ResultSet set = statement.executeQuery("SELECT * FROM `tco-2017-marathon-round-3-data`");
            while (set.next()) {
                result.add(new SizeData(set));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}