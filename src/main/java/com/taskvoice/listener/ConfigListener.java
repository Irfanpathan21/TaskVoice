package com.taskvoice.listener;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads environment variables from .env at application startup.
 * Values are stored as servlet context attributes so they are accessible
 * from any component via AppConfig.get(key).
 */
@WebListener
public class ConfigListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(ConfigListener.class);
    private static volatile ServletContext ctx;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ctx = sce.getServletContext();

        try {
            String realPath = ctx.getRealPath("/");
            java.io.File envFileInWebapp = new java.io.File(realPath, ".env");
            java.io.File envFileInUserDir = new java.io.File(System.getProperty("user.dir"), ".env");
            java.io.File envFileInParent = new java.io.File(System.getProperty("user.dir"), "../.env");

            Dotenv dotenv = null;
            if (envFileInWebapp.exists()) {
                dotenv = Dotenv.configure().directory(realPath).ignoreIfMissing().load();
            } else if (envFileInUserDir.exists()) {
                dotenv = Dotenv.configure().directory(System.getProperty("user.dir")).ignoreIfMissing().load();
            } else {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            }

            store(dotenv, "GEMINI_API_KEY");
            store(dotenv, "DB_URL");
            store(dotenv, "DB_USERNAME");
            store(dotenv, "DB_PASSWORD");
            store(dotenv, "SESSION_SECRET");

            log.info("TaskVoice configuration loaded successfully.");
        } catch (Exception e) {
            log.error("Failed to load configuration from .env: {}", e.getMessage());
        }
    }

    private void store(Dotenv dotenv, String key) {
        String value = (dotenv != null) ? dotenv.get(key, System.getenv(key)) : System.getenv(key);
        if (value != null && !value.isBlank()) {
            ctx.setAttribute(key, value);
            log.debug("Config key '{}' loaded.", key);
        } else {
            log.warn("Config key '{}' is missing or empty.", key);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ctx = null;
    }

    /** Static accessor for any class that needs a config value. */
    public static String get(String key) {
        return ctx == null ? null : (String) ctx.getAttribute(key);
    }
}
