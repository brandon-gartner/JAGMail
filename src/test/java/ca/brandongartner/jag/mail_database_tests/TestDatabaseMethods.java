/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database_tests;

import ca.brandongartner.jag.beans.EmailBean;
import ca.brandongartner.jag.beans.MailConfigFXMLBean;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import jodd.mail.Email;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
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
    MailConfigFXMLBean configBean = new MailConfigFXMLBean();
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
        try ( Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/EMAILDB?zeroDateTimeBehavior=CONVERT_TO_NULL", "brandon", "dawson!123");) {
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
        email.htmlMessage("bbbbbbbbbb");
        email.sentDate(Timestamp.valueOf(LocalDateTime.now()));
        email.from("bg02test@gmail.com");
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
        configBean.setSmtpURL("smtp.gmail.com");
        configBean.setImapPort("993");
        configBean.setSmtpPort("465");
        configBean.setMySqlURL("jdbc:mysql://localhost");
        configBean.setDatabaseName("EMAILDB");
        configBean.setMySqlPort("3306");
        configBean.setMySqlUserName("brandon");
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
    /*@Test
    public void testFindStringInEmail() throws SQLException{
        LOG.warn(">>>>>>>>>> FIND EMAIL TEST >>>>>>>>>>>>>");
        ResultSet rs = instance.findStringInEmail("aaaaa");
        if (rs.next()){
            assertEquals(1, getRowCount(rs));
        }
        else{
            LOG.warn("Failed test 1.1");
            fail();
        }
    }
    
    //1.2 test email that isn't there, undone
    @Test
    public void testFindFakeStringInEmail() throws SQLException{
        LOG.warn(">>>>>>>>>> FIND EMAIL TES T>>>>>>>>>>>>>");
        ResultSet rs = instance.findStringInEmail("ewrijiuhweriwiergiouwsiouergiowhrg");
        
        assertEquals(0, getRowCount(rs));
    }
    
    //1.3 test search for too long string/null
    @Test(expected = NullPointerException.class)
    public void testFindNullStringInEmail() throws SQLException{
        LOG.warn(">>>>>>>>>> FIND NULL EMAIL TEST >>>>>>>>>>>>>");
        ResultSet rs = instance.findStringInEmail(null);
    }*/
    
    
    //findEmailInFolder
    //2.1 finding the emails in a folder that exists
    @Test
    public void findInboxFolder() throws SQLException{
        LOG.warn(">>>>>>>>>> FIND EMAIL IN FOLDER TEST >>>>>>>>>>>>>");
        ArrayList<EmailBean> beans = instance.findEmailInFolder("Inbox");
        assertEquals(beans.size(), 14);
    }
    
    //2.2 trying to find emails in a folder that doesn't exist.
    @Test(expected=SQLException.class)
    public void findInbox2Folder() throws SQLException{
        LOG.warn(">>>>>>>>>> FIND EMAIL IN FAKE FOLDER TEST >>>>>>>>>>>>>");
        ArrayList<EmailBean> beans = instance.findEmailInFolder("Inbox2");
        assertEquals(beans.size(), 0);
    }
    
    
    //insertEmail
    //3.1
    @Test
    public void testInsertFineEmail() throws SQLException {
        LOG.warn(">>>>>>>>>> INSERT NORMAL EMAIL TEST >>>>>>>>>>>>>");
        basicEmail.setEmailId(25);
        int rowsAffected = instance.insertEmail(basicEmail, "Inbox");
        assertEquals(1, rowsAffected);
    }
    //3.2
    @Test
    public void testInsertBadEmail() throws SQLException {
    LOG.warn(">>>>>>>>>> INSERT BAD EMAIL TEST >>>>>>>>>>>>>");
        basicEmail.getEmail().textMessage(null);
        int rowsAffected = instance.insertEmail(basicEmail, "Inbox");
        
    }
    
    
    //createFolder
    //4.1
    @Test
    public void testCreateFolder() throws SQLException{
        LOG.warn(">>>>>>>>>> CREATE NEW FOLDER TEST >>>>>>>>>>>>>");
        int rowsAffected = instance.createFolder("NewFolder");
        assertEquals(1, rowsAffected);
    }
    //4.2
    @Test(expected = IllegalArgumentException.class)
    public void testCreateFolderTooLongName() throws SQLException {
        
        LOG.warn(">>>>>>>>>> CREATE TOO LONG FOLDER TEST >>>>>>>>>>>>>");
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
    LOG.warn(">>>>>>>>>> CHANGE FOLDER TEST>>>>>>>>>>>>>");
        int rowsAffected = instance.updateEmailFolder(1, "Inbox");
        assertEquals(1, rowsAffected);
    }
    //6.2
    @Test(expected = SQLException.class)
    public void testUpdateEmailFolderFakeFolder() throws SQLException {
    LOG.warn(">>>>>>>>>> MOVE TO FAKE FOLDER TEST >>>>>>>>>>>>>");
        int rowsAffected = instance.updateEmailFolder(1, "jxhsdvbfjhgsbuj");
    }
    
    
    //updateDraftEmail
    //7.1
    @Test
    public void testUpdateDraft() throws SQLException{
    LOG.warn(">>>>>>>>>> UPDATE DRAFT TEST >>>>>>>>>>>>>");
        int rowsAffected = instance.updateDraftEmail(basicEmail);
        assertEquals(7, rowsAffected);
    }
    //7.2
    @Test
    public void testUpdateDraftThatIsntDraft() throws SQLException {
        
    LOG.warn(">>>>>>>>>> UPDATE FAKE DRAFT TEST >>>>>>>>>>>>>");
        basicEmail.setEmailId(2147483647);
        int rowsAffected = instance.updateDraftEmail(basicEmail);
        assertEquals(0, rowsAffected);
    }
    
    
    //deleteEmail
    //8.1
    @Test
    public void testDeleteEmail() throws SQLException {
    LOG.warn(">>>>>>>>>> DELETE EMAIL TEST >>>>>>>>>>>>>");
        int rowsAffected = instance.deleteEmail(1);
        assertEquals(6, rowsAffected);
    }
    //8.2
    @Test
    public void testDeleteNonexistentEmail() throws SQLException {
    LOG.warn(">>>>>>>>>> DELETE FAKE EMAIL TEST >>>>>>>>>>>>>");
        int rowsAffected = instance.deleteEmail(2147483647);
        assertEquals(0, rowsAffected);
    }
}