/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database_tests;

import ca.brandongartner.jag.beans.AttachmentBean;
import ca.brandongartner.jag.beans.EmailBean;
import ca.brandongartner.jag.beans.MailConfigBean;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import jodd.mail.Email;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//things that are probably broken
//deleting messages
//checking if a folder exists when inserting
//dates may or may not work
//literally everything with from, including adding it, and getting it from the db
//ccs bccs, tos don't work when retrieving
//sending, deleting, etc, from different perspectives/accounts


/**
 *
 * @author Brandon Gartner
 */
public class TestDatabaseMethods {
    MailConfigBean configBean = new MailConfigBean();
    private final static Logger LOG = LoggerFactory.getLogger(TestDatabaseMethods.class);
    private DatabaseDAO instance;

    
    
    
    /**
     * The database is recreated before each test. If the last test is destructive then the database is in an unstable state. @AfterClass is called just once
     * when the test class is finished with by the JUnit framework. It is instantiating the test class anonymously so that it can execute its non-static
     * seedDatabase routine.
     */
    @AfterClass
    public static void seedAfterTestCompleted() {
        new TestDatabaseMethods().seedDatabase();
    }

    /**
     * This routine recreates the database before every test. This makes sure that a destructive test will not interfere with any other test. Does not support
     * stored procedures.
     *
     * This routine is courtesy of Bartosz Majsak, an Arquillian developer at JBoss
     */
    @Before
    public void seedDatabase() {
        LOG.info("@Before seeding");

        final String seedDataScript = loadAsString("./CreateTableStructure.sql");
        try ( Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb?zeroDateTimeBehavior=CONVERT_TO_NULL", "root", "dawson");) {
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
        MailConfigBean configBean = new MailConfigBean();
        configBean.setMySqlURL("jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=CONVERT_TO_NULL");
        configBean.setMySqlUsername("root");
        configBean.setMySqlPassword("dawson");
        instance = new DatabaseDAO(configBean);
    }

    /**
     * The following methods support the seedDatabse method
     */
    private String loadAsString(final String path) {
        try ( InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);  Scanner scanner = new Scanner(inputStream)) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close input stream.", e);
        }
    }

    private List<String> splitStatements(Reader reader, String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }

    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//") || line.startsWith("/*");
    }
    
    //1.1 no time to implement
    @Test
    public void testInsertEmail() throws Exception{
        throw new Exception("");
    }
}