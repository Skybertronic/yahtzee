
public class Database {

/*
    public static Connection connection;
    private static boolean hasData = false;
    public ResultSet displayUsers() throws ClassNotFoundException, SQLException {
        if (connection == null) {
            getConnection();
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
    */
}

