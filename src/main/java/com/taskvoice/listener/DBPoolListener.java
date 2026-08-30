package com.taskvoice.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Initialises the Apache DBCP2 connection pool at startup.
 * The DataSource is stored as a servlet context attribute under the key "dataSource".
 */
@WebListener
public class DBPoolListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(DBPoolListener.class);
    private static volatile BasicDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String url      = ConfigListener.get("DB_URL");
        String username = ConfigListener.get("DB_USERNAME");
        String password = ConfigListener.get("DB_PASSWORD");

        if (url == null || url.isBlank()) {
            url = System.getenv("DB_URL");
            username = System.getenv("DB_USERNAME");
            password = System.getenv("DB_PASSWORD");
        }

        if (url == null || url.isBlank()) {
            url = "jdbc:mysql://localhost:3306/taskvoice";
            username = "root";
            password = "";
        }

        // Support mysql:// scheme if provided by cloud hosting platform
        if (url.startsWith("mysql://")) {
            url = "jdbc:" + url;
        }

        // Automatically append SSL & Public Key parameters for cloud databases (e.g. Aiven, AWS RDS, Render)
        if (url.startsWith("jdbc:mysql://")) {
            if (!url.contains("trustServerCertificate=")) {
                url += (url.contains("?") ? "&" : "?") + "trustServerCertificate=true";
            }
            if (!url.contains("allowPublicKeyRetrieval=")) {
                url += "&allowPublicKeyRetrieval=true";
            }
            if (!url.contains("useSSL=") && !url.contains("sslMode=")) {
                url += "&useSSL=true";
            }
            if (!url.contains("serverTimezone=")) {
                url += "&serverTimezone=UTC";
            }
        }

        log.info("Connecting to database URL: {}", url.replaceAll("password=[^&]*", "password=***"));

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);

        // Optimized Pool configuration for cloud databases
        ds.setInitialSize(2);
        ds.setMaxTotal(20);
        ds.setMaxIdle(10);
        ds.setMinIdle(2);
        ds.setMaxWaitMillis(10_000);
        ds.setValidationQuery("SELECT 1");
        ds.setTestOnBorrow(true);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(30_000);
        ds.setMinEvictableIdleTimeMillis(60_000);

        dataSource = ds;
        sce.getServletContext().setAttribute("dataSource", ds);
        log.info("Database connection pool initialized successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) {
            try {
                dataSource.close();
                log.info("Database connection pool closed.");
            } catch (Exception e) {
                log.error("Error closing connection pool", e);
            }
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
