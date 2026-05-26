package com.brewmaster.db;

import java.sql.*;
import java.util.Properties;

/**
 * Quản lý kết nối JDBC đến MySQL
 * Singleton pattern để dùng chung connection
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;

    // === CẤU HÌNH KẾT NỐI ===
    // Docker: xem docker-compose.yml tại thư mục gốc project
    // Chạy: docker-compose up -d  để khởi động MySQL container
    private static String HOST     = "localhost";
    private static int    PORT     = 3306;
    private static String DATABASE = "cfe_di_rom";
    private static String USERNAME = "root";
    private static String PASSWORD = "root";  // khớp MYSQL_ROOT_PASSWORD trong docker-compose.yml

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /** Kết nối đến MySQL */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh",
                HOST, PORT, DATABASE
            );
            Properties props = new Properties();
            props.setProperty("user", USERNAME);
            props.setProperty("password", PASSWORD);
            connection = DriverManager.getConnection(url, props);
        }
        return connection;
    }

    /** Cập nhật thông tin kết nối */
    public static void configure(String host, int port, String db, String user, String pass) {
        HOST = host;
        PORT = port;
        DATABASE = db;
        USERNAME = user;
        PASSWORD = pass;
    }

    /** Kiểm tra kết nối */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /** Đóng kết nối */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Helper: đóng resources */
    public static void close(ResultSet rs, Statement st) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (st != null) st.close(); } catch (SQLException ignored) {}
    }
}
