package com.portfolio.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EntityScanTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseTablesAreCreated() throws Exception {
        // This test verifies that the database tables are created
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Get all table names
            List<String> tableNames = new ArrayList<>();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName.toUpperCase());
            }
            
            // Verify expected tables exist
            assertTrue(tableNames.contains("CASH_ACCOUNT"), 
                "CASH_ACCOUNT table not found. Available tables: " + tableNames);
            assertTrue(tableNames.contains("PORTFOLIO_ITEM"), 
                "PORTFOLIO_ITEM table not found. Available tables: " + tableNames);
            assertTrue(tableNames.contains("TRADE_HISTORY"), 
                "TRADE_HISTORY table not found. Available tables: " + tableNames);
            
            System.out.println("Found tables: " + tableNames);
        }
    }
} 