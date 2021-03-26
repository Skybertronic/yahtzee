import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Database {

    public static Connection connection;
    private static boolean hasData = false;

    public void will_work() throws SQLException, ClassNotFoundException, MalformedURLException, IllegalAccessException, InstantiationException {
        URL u = new URL("jar:file:/path/to/pgjdbc2.jar!/");
        String classname = "org.postgresql.Driver";
        URLClassLoader ucl = new URLClassLoader(new URL[] { u });
        Driver d = (Driver)Class.forName(classname, true, ucl).newInstance();
        DriverManager.registerDriver(new DriverShim(d));
        DriverManager.getConnection("jdbc:postgresql://localhost/Yahtzee", "user", "");
        // Success!

        this.initialise();
    }
    
    public Database() {

        try {
            while (displayUsers().next()) {
                System.out.println(displayUsers().getString("fname" + " " + displayUsers().getString("lname")));
            }
        } catch (SQLException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
    }

    public ResultSet displayUsers() throws ClassNotFoundException, SQLException {

        if (connection == null) {
            try {
                will_work();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        this.initialise();

        Statement statement = connection.createStatement();
        return statement.executeQuery("SELECT fname, lname FROM user");
    }

    

    private void getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:Yahtzee.db");
        this.initialise();
    }

    private void initialise() throws SQLException {
        if (!hasData) {
            hasData = true;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite master WHERE type = 'table' AND name = 'user'");
            if (!resultSet.next()) {
                System.out.println("Building the User table with prepopulated values.");
                Statement statement1 = connection.createStatement();
                statement1.execute("CREATE TABLE user(id integer auto_increment, fname varchar(60), lname varchar(60), primary key(id)");

                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO user values(?, ?, ?);");
                preparedStatement.setString(2, "John");
                preparedStatement.setString(3, "McNeil");
                preparedStatement.execute();

                PreparedStatement preparedStatement1 = connection.prepareStatement("INSERT INTO user values(?, ?, ?);");
                preparedStatement1.setString(2, "Paul");
                preparedStatement1.setString(3, "Smith");
                preparedStatement1.execute();
            }
        }
    }

    private class DriverShim implements Driver {
        private Driver driver;
        DriverShim(Driver d) {
            this.driver = d;
        }
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return this.driver.getParentLogger();
        }
    }
}

