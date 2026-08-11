package com.alphabetz.webalphabetz.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlite.SQLiteDataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Bean
    public DataSource dataSource() {
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl(datasourceUrl);
        migrateFundoTopoSchema(ds);
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private void migrateFundoTopoSchema(SQLiteDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            if (!tableExists(connection, "fundo_topo") || columnExists(connection, "fundo_topo", "nome_pagina")) {
                return;
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "ALTER TABLE fundo_topo ADD COLUMN nome_pagina TEXT NOT NULL DEFAULT 'Página inicial'");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível atualizar a estrutura da tabela fundo_topo.", exception);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var statement = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

}
