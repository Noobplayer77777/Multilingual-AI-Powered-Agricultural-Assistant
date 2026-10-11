package com.agri.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;

@SpringBootApplication
public class QueryServiceApplication {

    public static void main(String[] args) {
        configureDatabaseEnvironment();
        SpringApplication.run(QueryServiceApplication.class, args);
    }

    private static void configureDatabaseEnvironment() {
        // 1. Try reading .env file if properties are not set in environment
        File[] candidates = new File[] {
            new File(".env"),
            new File("../.env"),
            new File("../../.env")
        };
        for (File envFile : candidates) {
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                            int eqIdx = line.indexOf('=');
                            String key = line.substring(0, eqIdx).trim();
                            String val = line.substring(eqIdx + 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            }
        }

        // 2. Normalize DATABASE_URL (e.g. from Neon / Heroku / Railway) for Spring Boot JDBC
        String dbUrl = System.getProperty("DATABASE_URL");
        if (dbUrl == null) {
            dbUrl = System.getenv("DATABASE_URL");
        }
        if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
            try {
                // Ensure URI parsing handles postgres:// or postgresql://
                String standardUriStr = dbUrl.startsWith("postgres://") ? "postgresql://" + dbUrl.substring(11) : dbUrl;
                URI uri = new URI(standardUriStr.replace("postgresql://", "http://"));
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String query = uri.getQuery();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + (query != null ? "?" + query : "");
                System.setProperty("spring.datasource.url", jdbcUrl);

                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    System.setProperty("spring.datasource.username", parts[0]);
                    if (parts.length > 1) {
                        System.setProperty("spring.datasource.password", parts[1]);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

}
