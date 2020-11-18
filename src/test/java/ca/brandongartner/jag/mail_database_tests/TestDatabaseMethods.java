/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database_tests;

import ca.brandongartner.jag.beans.EmailBean;
import ca.brandongartner.jag.beans.MailConfigBean;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import jodd.mail.Email;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
    private EmailBean basicEmail;

    
    
    
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

        final String seedDataScript = loadAsString("CreateTableStructure.sql");
        try ( Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb?zeroDateTimeBehavior=CONVERT_TO_NULL", "brandon", "dawson!123");) {
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
        initialize();
        basicEmail = createBasicEmailBean();
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

    private EmailBean createBasicEmailBean(){
        EmailBean theBean = new EmailBean();
        Email email = new Email();
        email.to("bg02test@gmail.com");
        email.subject("subject1");
        email.textMessage("aaaaaaaaaaaaaaaa");
        theBean.setEmail(email);
        theBean.setFolderId(3);
        theBean.setEmailId(1);
        return theBean;
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
    
    public void initialize(){
        configBean.setUserName("doesTheUserNameDoAnything");
        configBean.setUserEmailAddress("bg01test@gmail.com");
        configBean.setPassword("dawson123");
        configBean.setImapURL("imap.gmail.com");
        configBean.setSmtpUrl("smtp.gmail.com");
        configBean.setImapPort("993");
        configBean.setSmtpPort("465");
        configBean.setMySqlURL("jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=CONVERT_TO_NULL");
        configBean.setDatabaseName("emailDB");
        configBean.setMySqlPort("3306");
        configBean.setMySqlUsername("brandon");
        configBean.setMySqlPassword("dawson!123");
        instance = new DatabaseDAO(configBean);
        
    }
    
    //in class comment: 2-3 per public method, 1 fail at least
    //list of methods:
    //insertEmail
    //createFolder
    //findEmailsbyEmailAddress
    //updateEmailFolder
    //updateDraftEmail
    //deleteemail
    //need to do roughly 16-24
    
    
    //findStringInEmail
    //1.1 finding a string inside of an email
    @Test
    public void testFindStringInEmail() throws SQLException{
        LOG.warn(">>>>>>>>>>FIND EMAIL TEST>>>>>>>>>>>>>");
        ResultSet rs = instance.findStringInEmail("aaaaa");
        if (rs.next()){
            assertEquals(getRowCount(rs), 1);
        }
        else{
            LOG.warn("Failed test 1.1");
            fail();
        }
    }
    
    //1.2 test email that isn't there, undone
    @Test
    public void testFindFakeStringInEmail() throws SQLException{
        LOG.warn(">>>>>>>>>>FIND EMAIL TEST>>>>>>>>>>>>>");
        ResultSet rs = instance.findStringInEmail("ewrijiuhweriwiergiouwsiouergiowhrg");
        if (rs.next()){
            assertEquals(getRowCount(rs), 0);
        }
        else{
            LOG.warn("Failed test 1.2");
            fail();
        }
    }
    
    //1.3 test search for too long string/null
    @Test(expected = NullPointerException.class)
    public void testFindNullStringInEmail() throws SQLException{
        LOG.warn(">>>>>>>>>>FIND EMAIL TEST>>>>>>>>>>>>>");
        ResultSet rs = instance.findStringInEmail(null);
    }
    
    
    //findEmailInFolder
    //2.1 finding the emails in a folder that exists
    @Test
    public void findInboxFolder() throws SQLException{
        ResultSet rs = instance.findEmailInFolder("Inbox");
        if (rs.next()){
            assertEquals(getRowCount(rs), 14);
        }
        else{
            LOG.warn("Failed test 2.1");
            fail();
        }
    }
    
    //2.2 trying to find emails in a folder that doesn't exist.
    @Test
    public void findInbox2Folder() throws SQLException{
        ResultSet rs = instance.findEmailInFolder("Inbox2");
        if (rs.next()){
            fail();
        }
        else {
            assertTrue(true);
        }
    }
    
    
    //insertEmail
    //3.1
    @Test
    public void testInsertFineEmail() throws SQLException {
        basicEmail.setEmailId(25);
        int rowsAffected = instance.insertEmail(basicEmail, "Inbox");
        assertEquals(rowsAffected, 2);
    }
    //3.2
    @Test(expected = SQLException.class)
    public void testInsertBadEmail() throws SQLException {
        int rowsAffected = instance.insertEmail(basicEmail, "Inbox");
    }
    
    
    //createFolder
    //4.1
    @Test
    public void testCreateFolder() throws SQLException{
        int rowsAffected = instance.createFolder("NewFolder");
        assertEquals(rowsAffected, 1);
    }
    //4.2
    @Test(expected = SQLException.class)
    public void testCreateFolderTooLongName() throws SQLException {
        //my SQL database accepts folder names up to 50 characters
        int rowsAffected = instance.createFolder("thistextisactuallyonecharactertoolongthisshouldfail");
    }
    
    
    //findEmailsFromEmailAddress
    //5.1
    //5.2
    
    
    //updateEmailFolder
    //6.1
    @Test
    public void testUpdateEmailFolder() throws SQLException {
        int rowsAffected = instance.updateEmailFolder(1, "Inbox");
        assertEquals(rowsAffected, 1);
    }
    //6.2
    @Test(expected = SQLException.class)
    public void testUpdateEmailFolderFakeFolder() throws SQLException {
        int rowsAffected = instance.updateEmailFolder(1, "jxhsdvbfjhgsbuj");
    }
    
    
    //updateDraftEmail
    //7.1
    @Test
    public void testUpdateDraft() throws SQLException{
        int rowsAffected = instance.updateDraftEmail(basicEmail);
        assertEquals(rowsAffected, 4);
    }
    //7.2
    @Test(expected = SQLException.class)
    public void testUpdateDraftThatIsntDraft() throws SQLException {
        basicEmail.setEmailId(2147483647);
        int rowsAffected = instance.updateDraftEmail(basicEmail);
    }
    
    
    //deleteEmail
    //8.1
    @Test
    public void testDeleteEmail() throws SQLException {
        int rowsAffected = instance.deleteEmail(1);
        assertEquals(rowsAffected, 2);
    }
    //8.2
    @Test(expected = SQLException.class)
    public void testDeleteNonexistentEmail() throws SQLException {
        int rowsAffected = instance.deleteEmail(2147483647);
    }
    
    
    
    //method courtesy of here:
    //https://stackoverflow.com/questions/8292256/get-number-of-rows-returned-by-resultset-in-java
    private int getRowCount(ResultSet rs){
        if (rs == null){
            return 0;
        }
        
        try{
            rs.last();
            return rs.getRow();
        } catch (SQLException e){
            LOG.error("SQLException while counting rows of ResultSet.", e);
        } finally{
            try{
                rs.beforeFirst();
            } catch (SQLException e){
                LOG.error("SQLException while going to first row of ResultSet.", e);
            }
        }
        
        return 0;
    }
}