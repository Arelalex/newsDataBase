package db.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {

    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static String PASSWORD_KEY = "db.password";
    private static String USERNAME_KEY = "db.username";
    private static String URL_KEY = "db.url";
    private static String POOL_SIZE_KEY = "db.pool.size";
    private static BlockingQueue<Connection> pool;
   // private static List<Connection> sourceConnections;

   private static final List<Connection> sourceConnections = new ArrayList<>();

    static {
        loadDriver();
        initConnectionPool();
    }

    private ConnectionManager() {
    }

    private static void initConnectionPool() {
        var poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        // sourceConnections = new ArrayList<>(size);
        synchronized (sourceConnections) {
            for (int i = 0; i < size; i++) {
                var connection = open();
                var proxyConnection = (Connection)
                        Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                                (proxy, method, args) -> method.getName().equals("close")
                                        ? pool.add((Connection) proxy)
                                        : method.invoke(connection, args));
                pool.add(proxyConnection);
                sourceConnections.add(connection);
            }
        }
    }

    private static void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection get() {
        try {
            return pool.take(); //возвращает соединение если оно есть, если пул пустой то ждет
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Connection open() {
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closePool() {
        try {
            for (Connection sourceConnection : sourceConnections) {
                sourceConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}

