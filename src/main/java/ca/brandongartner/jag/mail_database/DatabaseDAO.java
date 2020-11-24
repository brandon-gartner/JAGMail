/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database;

import ca.brandongartner.jag.beans.AttachmentBean;
import ca.brandongartner.jag.beans.EmailBean;
import ca.brandongartner.jag.beans.EmailFXBean;
import ca.brandongartner.jag.beans.FolderBean;
import ca.brandongartner.jag.beans.MailConfigFXMLBean;
import ca.brandongartner.jag.mail_business.SendReceiveEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jodd.mail.EmailAddress;
import javax.activation.DataSource;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;

/**
 * CRUD methods for a basic single-user email database
 * @author Brandon Gartner
 */
public class DatabaseDAO {
    
    private final static Logger LOG = LoggerFactory.getLogger(DatabaseDAO.class);
    
    public MailConfigFXMLBean instanceConfig;
    
    public DatabaseDAO(MailConfigFXMLBean configBean){
        this.instanceConfig = configBean;
    }
    
    /**
     * creates a connection for the current instance to use to connect to the database
     * @return
     * @throws SQLException 
     */
    private Connection generateConnection() throws SQLException{
        try{
            LOG.trace("Attempting to generate a connection.");
            String connectionString = instanceConfig.getMySqlURL() + ":" + instanceConfig.getMySqlPort() + "/" + instanceConfig.getDatabaseName() + "?zeroDateTimeBehavior=CONVERT_TO_NULL";
            LOG.debug("Connection string: " + connectionString);
            Connection connection = DriverManager.getConnection(connectionString, instanceConfig.getMySqlUserName(), instanceConfig.getMySqlPassword());
            LOG.trace("Connection created.");
            return connection;
        }
        catch (SQLException sqlex){
            LOG.warn("Connection failed.");
            throw new SQLException("Connection to database failed!");
        }
    }
    
    
    /**
     * transforms an emailAttachment into an attachmentBean
     * @param emailAttachment takes an attachment object and returns an AttachmentBean based off of it
     * @return the attachmentbean object based on the emailAttachment sent
     */
    private AttachmentBean generateAttachmentBean(EmailAttachment emailAttachment){
        AttachmentBean newBean = new AttachmentBean();
        LOG.trace("Created attachment bean.");
        newBean.setIsEmbedded(emailAttachment.getContentId() != null);
        newBean.setFileName(emailAttachment.getName());
        newBean.setAttachment(emailAttachment.toByteArray());
        LOG.trace("Added fields to attachment bean.");
        return newBean;
    }
    
    
    //CREATE/PRIVATE METHODS FOR CREATE
    //TODO: PRIVATE METHOD CENTRAL PIECE OF THIS METHOD
    /**
     * overall, does all of the things involved with adding an email to the database
     * @param emailBean the emailBean that the email we're adding is based off of
     * @param folderName the name of the folder we want to add the email to
     * @returns the amount of rows modified
     * @throws SQLException 
     */
    public int insertEmail(EmailBean emailBean, String folderName) throws SQLException{
        //creates a connection, adds relevant email addresses and attachments to their respective tables (TODO: move addattachments to later, so it can contain emailid)
        Connection connection = generateConnection();
        int counter = addEmailRecipientsToAddressesTable(connection, emailBean);
        LOG.trace("Added all forms of recipients to the addresses table.");
        
        //create sql statement, prepare it, insert data into it
        String sql = "INSERT INTO emails (subject, message, htmlMessage, sendDate, receiveDate, folderId, from_who)" + 
                      " VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                                
                                
        ps.setString(1, emailBean.getSubject());
        ps.setString(2, emailBean.getMessage());
        ps.setString(3, emailBean.getHtmlMessage());
        ps.setTimestamp(4, emailBean.getSentDate());
        LOG.debug("Added subject, message, htmlmessage to the preparedStatement");
        ps.setTimestamp(5, getReceivedDate(folderName));
        int folderId = getFolderIdFromName(connection, folderName);
        ps.setInt(6, folderId);
        int fromId = getAddressIdFromEmailAddress(connection, emailBean.getFrom());
        ps.setInt(7, fromId);
        LOG.info("Finished setting parameters to insert an email.");
        counter += ps.executeUpdate();
        LOG.trace("Executed query.");
        LOG.debug("Attempting to get the generated keys.");
        ResultSet rs = ps.getGeneratedKeys();
        LOG.debug("Got generated keys.");
        if (rs.next()){
            LOG.debug("Going through a result in email insertion.");
            int emailId = rs.getInt(1);
            emailBean.setEmailId(emailId);
        }
        
        counter += addAttachmentsToAttachmentTable(connection, emailBean);
        LOG.trace("Added all attachments to the attachment table.");
        
        //adding the addresses to the right places
        addToEmailsToAddresses(connection, emailBean);
        LOG.trace("Added emails to bridging table.");
        LOG.trace("Finished adding email to database.");
        return counter;
    }
    
    /**
     * decides on what the receive date should be, based on the folder it's being inserted into
     * @param folderName the folder which the email is being inserted into
     * @return the date
     */
    private Timestamp getReceivedDate(String folderName){
        switch (folderName){
            case "Sent":
            case "Drafts":
                return null;
            default:
                return Timestamp.valueOf(LocalDateTime.now());
        }
    }
    
    /**
     * checks if there is a folder with the given name
     * @param connection the connection this is all done through
     * @param folderName folder name to check if it already exists
     * @return a boolean indicating whether or not the folder exists
     */
    private boolean checkIfFolderExists(Connection connection, String folderName) throws SQLException{
        String sql = "SELECT * FROM folders WHERE name = ?";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, folderName);
        LOG.info("Added parameters to folder-checking query.");
        ResultSet rs = ps.executeQuery();
        LOG.trace("Executed query.");
        rs.next();
        if (rs.getString("name").equals(folderName)){
            LOG.trace("Match found.");
            return true;
        }
        else {
            LOG.trace("Match not found.");
            return false;
        }
        
    }
    
    /**
     * Takes a string, and creates a new folder with that name.
     * @param connection the connection through which all database interactions will operate
     * @param name the name of the new folder
     * @return 1, if the folder is successfully created
     * @throws SQLException if there is an error when setting the string, or generating the preparedStatement
     */
    public int createFolder(String name) throws SQLException{
        if (name.length() > 50){
            LOG.error("New folder name too long.");
            throw new IllegalArgumentException("Name of the new folder was too long, should be 50 characters maximum.");
        }
        Connection connection = generateConnection();
        String sql = "INSERT INTO folders (name) VALUES (?)";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, name);
        LOG.info("Parameters set for folder-creation query.");
        ps.executeUpdate();
        LOG.trace("Query executed.");
        return 1;
    }
    
    /**
     * adds the entries for all of the tos, bccs, and ccs
     * @param connection the connection through which all of these methods are ran
     * @param emailBean the emailBean that the tos and ccs and bccs are all based on
     * @return the amount of rows modified
     * @throws SQLException if any of the methods inside cause an SQLException
     */
    private int addToEmailsToAddresses(Connection connection, EmailBean emailBean) throws SQLException{
        int counter = addTosToBridging(connection, emailBean);
        counter += addCCsToBridging(connection, emailBean);
        counter += addBCCsToBridging(connection, emailBean);
        return counter;
    }
    
    /**
     * adds all emails in the to() field to the bridging table
     * @param connection the connection this is all done through
     * @param emailBean the email whose recipients we want to add
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int addTosToBridging(Connection connection, EmailBean emailBean) throws SQLException{
        List<EmailAddress> listOfTos = emailBean.getTos();
        String sql = "INSERT INTO emailToAddresses (emailId, type, addressId) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        int counter = 0;
        for (EmailAddress address : listOfTos){
            int addressId = getAddressIdFromEmailAddress(connection, address.getEmail());
            ps.setInt(1, emailBean.getEmailId());
            ps.setString(2, "To");
            ps.setInt(3, addressId);
            LOG.info("Added parameters to insert-to-address-into-bridging-table query.");
            ps.executeUpdate();
            LOG.trace("Added a 'To' address to the bridging table.");
            counter += 1;
        }
        return counter;
    }
    
    /**
     * add all of an email's ccs to the bridging table
     * @param connection the connection this is all done through
     * @param emailBean the email whose CCs we want to add to the bridging table
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int addCCsToBridging(Connection connection, EmailBean emailBean) throws SQLException{
        List<EmailAddress> listOfCCs = emailBean.getCCs();
        String sql = "INSERT INTO emailToAddresses (emailId, type, addressId) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        int counter = 0;
        for (EmailAddress address : listOfCCs){
            int addressId = getAddressIdFromEmailAddress(connection, address.getEmail());
            ps.setInt(1, emailBean.getEmailId());
            ps.setString(2, "CC");
            ps.setInt(3, addressId);
            LOG.info("Added parameters to insert-cc-address-into-bridging-table query.");
            ps.executeUpdate();
            LOG.trace("Added a 'CC' to the bridging table.");
            counter += 1;
        }
        return counter;
    }
    
    /**
     * adds an email's bccs to the bridging table
     * @param connection the connection this is all done through
     * @param emailBean the email whose BCCs we want to add to the bridging table
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int addBCCsToBridging(Connection connection, EmailBean emailBean) throws SQLException{
        List<EmailAddress> listOfBCCs = emailBean.getBCCs();
        String sql = "INSERT INTO emailToAddresses (emailId, type, addressId) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        int counter = 0;
        for (EmailAddress address : listOfBCCs){
            int addressId = getAddressIdFromEmailAddress(connection, address.getEmail());
            ps.setInt(1, emailBean.getEmailId());
            ps.setString(2, "BCC");
            ps.setInt(3, addressId);
            LOG.info("Added parameters to insert-bcc-address-into-bridging-table query.");
            ps.executeUpdate();
            LOG.trace("Added a 'BCC' to the bridging table.");
            counter += 1;
        }
        return counter;
    }
    
    /**
     * creates a hashset of all of an email's recipients, ccs, and bccs.
     * @param emailBean the email whos recipient types you want to compile into a single hashset
     * @return hashset of all of the tos, ccs, and bccs combined from the given emailBean
     */
    private HashSet<EmailAddress> getCombinedRecipientSet(EmailBean emailBean){
        HashSet<EmailAddress> recipients = new HashSet<EmailAddress>();
        for (EmailAddress address : emailBean.getTos()){
            recipients.add(address);
            LOG.info("Added a 'to' address to the HashSet.");
        }
        LOG.trace("Added all 'to' addresses to the HashSet.");
        for (EmailAddress address : emailBean.getCCs()){
            recipients.add(address);
            LOG.info("Added a 'cc' address to the HashSet.");
        }
        LOG.trace("Added all 'cc' addresses to the HashSet.");
        for (EmailAddress address : emailBean.getBCCs()){
            recipients.add(address);
            LOG.info("Added a 'bcc' address to the HashSet.");
        }
        LOG.trace("Added all 'cc' addresses to the HashSet.");
        return recipients;
    }
    
    /**
     * takes a hashset of email addresses, returns an arraylist of those which aren't in the database yet
     * @param connection the connection this is all done through
     * @param recipients a hashset of all of the recipients, ccs, and bccs
     * @return an arraylist of emailAddresses from the recipients hashset, which are not inside of the database
     * @throws SQLException 
     */
    private ArrayList<EmailAddress> findRecipientsMissingFromAddresses(Connection connection, HashSet<EmailAddress> recipients) throws SQLException{
        ArrayList<EmailAddress> recipientsNotInDB = new ArrayList<EmailAddress>(recipients);
        String sql = "SELECT emailAddress FROM addresses WHERE emailAddress = ?";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        Iterator<EmailAddress> emailIterator = recipientsNotInDB.iterator();
        while (emailIterator.hasNext()){
            EmailAddress address = emailIterator.next();
            ps.setString(1, address.getEmail());
            LOG.info("Set parameters to find if a certain email address is inside of the database or not.");
            ResultSet result = ps.executeQuery();
            LOG.trace("Executed query.");
            if (checkIfEmailInsideOfResultSet(result, address.getEmail())){
                LOG.debug("Began looping through the email list.");
                emailIterator.remove();
                LOG.info(address + " was found in database, thus removed from the HashSet.");
            }
            else {
                LOG.info(address + " was not found in the database, thus it was left in the HashSet.");
                continue;
            }
        }
        LOG.trace("Done finding emails that aren't in the database.");
        return recipientsNotInDB;
    }
    
    /**
     * checks if a particular email address exists within the given resultset
     * @param rs resultset to check for the email address
     * @param address the address we're checking for
     * @return returns a boolean which indicates whether the address is there or not
     * @throws SQLException 
     */
    private boolean checkIfEmailInsideOfResultSet(ResultSet rs, String address) throws SQLException{
        LOG.debug("Applying rs.next() to begin checking inside of the resultSet");
        rs.next();
        LOG.debug("Checking if an email was inside of the resultset.");
        String receivedEmail = rs.getString("emailAddress");
        if (receivedEmail != null && receivedEmail.equals(address)){
            LOG.info("Email was found.");
            return true;
        }
        else {
            LOG.info("Email was not found.");
            return false;
        }
    }
    
    /**
     * adds all of an email's tos,ccs,bccs to the addresses table
     * @param connection the connection this is all done through
     * @param emailBean the email whose recipients/ccs/bccs we want to add to the addresses table
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int addEmailRecipientsToAddressesTable(Connection connection, EmailBean emailBean) throws SQLException{
        HashSet<EmailAddress> addressesSet = getCombinedRecipientSet(emailBean);
        LOG.trace("Combining recipient sets together.");
        ArrayList<EmailAddress> addressesNotInDB = findRecipientsMissingFromAddresses(connection, addressesSet);
        LOG.trace("Found all recipients not yet in the database.");
        String sql = "INSERT INTO addresses (emailAddress, name) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        int addedCounter = 0;
        for (EmailAddress address : addressesNotInDB){
            ps.setString(1, address.getEmail());
            ps.setString(2, address.getPersonalName());
            LOG.info("Set parameters.");
            addedCounter++;
            ps.executeUpdate();
            LOG.trace("Added addresses to database.");
        }
        LOG.trace("Done adding new addresses to the database.");
        return addedCounter;
    }
    
    /**
     * modified to not check for duplicates since apparently we don't need a bridging table for this
     * @param connection the connection this is all done through
     * @param emailBean the email whose attachments we want to insert to the table
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int addAttachmentsToAttachmentTable(Connection connection, EmailBean emailBean) throws SQLException{
        HashSet<AttachmentBean> attachmentSet = getAttachmentSet(emailBean);
        String sql = "INSERT INTO attachments (file, isEmbedded, emailId, fileName) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                Statement.RETURN_GENERATED_KEYS);
        int counter = 0;
        for (AttachmentBean bean : attachmentSet){
            ps.setBytes(1, bean.getAttachment());
            ps.setBoolean(2, bean.getIsEmbedded());
            ps.setInt(3, emailBean.getEmailId());
            ps.setString(4, bean.getFileName());
            LOG.info("Finished setting parameters for adding attachments to their table.");
            ps.executeUpdate();
            LOG.info("Added attachment to table.");
            ResultSet resultSet = ps.getGeneratedKeys();
            /*if (resultSet.next()){
                bean.setId(resultSet.getInt(1));
                LOG.trace("Added attachmentId to the attachment bean.");
            }*/
            bean.setId(emailBean.getEmailId());
            counter += 1;
        }
        LOG.trace("Finished adding attachments to the attachment table.");
        return counter;
    }
    
    /**
     * takes an emailbean and returns a hashset of attachmentbeans base off of that email's attachments
     * @param emailBean an emailBean to get the attachments from
     * @return a hashset of attachments from the email
     */
    private HashSet<AttachmentBean> getAttachmentSet(EmailBean emailBean){
        HashSet<AttachmentBean> attachmentSet = new HashSet<AttachmentBean>();
        List<EmailAttachment<? extends DataSource>> attachments = emailBean.getAttachments();
        for (EmailAttachment attachment : attachments){
            AttachmentBean newAttachmentBean = generateAttachmentBean(attachment);
            LOG.info("Created a new attachment bean.");
            attachmentSet.add(newAttachmentBean);
        }
        LOG.trace("Finished adding creating the HashSet of attachment beans.");
        return attachmentSet;
    }
    
    //READ/PRIVATE METHODS TO READ
    
    /**
     * takes a string, and returns all emails that contain that string
     * @param connection the connection this is all done through
     * @param messageToSearch a string of text we're searching inside of emails for
     * @return a resultset that can be processed into a set of emails
     * @throws SQLException 
     */
    public ResultSet findStringInEmail(String messageToSearch) throws SQLException{
        String sql = "SELECT * FROM emails WHERE message LIKE ? OR htmlMessage LIKE ? OR subject LIKE ?";
        Connection connection = generateConnection();
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, "%" + messageToSearch + "%");
        ps.setString(2, "%" + messageToSearch + "%");
        ps.setString(3, "%" + messageToSearch + "%");
        LOG.info("Finished setting parameters to search emails for a specified string.");
        ResultSet rs = ps.executeQuery();
        LOG.trace("Executed query.");
        return rs;
    }
    
    /**
     * finds all emails within a specified folder
     * @param connection the connection this is all done through
     * @param folderName the folder you want to find all emails that are inside of
     * @return a resultset that can be processed into a set of emails
     * @throws SQLException 
     */
    public ArrayList<EmailBean> findEmailInFolder(String folderName) throws SQLException{
        String sql = "SELECT * FROM emails " +
                      "INNER JOIN folders ON emails.folderId = folders.folderId " +
                      "INNER JOIN addresses ON emails.from_who = addresses.addressId " +
                      "WHERE folders.name = ?";
        Connection connection = generateConnection();
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, folderName);
        LOG.info("Finished setting parameters to search for all emails in a specified folder.");
        ResultSet rs = ps.executeQuery();
        LOG.trace("Executed query.");
        ArrayList<EmailBean> emailBeans = new ArrayList<EmailBean>();
        emailBeans = generateReceivedEmailBeanListFromFolder(connection, rs, folderName);
        return emailBeans;
    }
    
    /**
     * searches for all emails by a certain emailAddress
     * @param connection the connection this is all done through
     * @param emailAddress an emailAddress you want to search for all emails from
     * @return a resultSet that can be processed into a set of emails
     * @throws SQLException 
     */
    public ResultSet findEmailsFromEmailAddress(String emailAddress) throws SQLException{
        String sql = "SELECT * FROM emails WHERE addresses.emailAddress = ?" + 
                      "INNER JOIN emailToAddresses ON emails.emailId = emailToAddresses.emailId " + 
                      "INNER JOIN addresses ON emailToAddresses.addressId = addresses.addressId";
        Connection connection = generateConnection();
        PreparedStatement ps = connection.prepareStatement(sql, 
                            ResultSet.TYPE_SCROLL_INSENSITIVE, 
                            ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, emailAddress);
        LOG.info("Finished setting parameters to search for emails by their senders/receivers.");
        ResultSet rs = ps.executeQuery();
        LOG.info("Executed query.");
        return rs;
    }
    
    //UPDATE/PRIVATE METHODS TO UPDATE
    
    /**
     * moves an email from one folder to another
     * @param connection the connection this is all done through
     * @param emailId the emailId of the email you'd like to move
     * @param folderName the name of the folder you want to move the email to
     * @return the amount of rows modified
     * @throws SQLException 
     */
    //UPDATE EMAIL FOLDER (NOT FROM SENT/DRAFTS, NOT INTO SENT/DRAFTS
    public int updateEmailFolder(int emailId, String folderName) throws SQLException{
        Connection connection = generateConnection();
        if (checkIfFolderExists(connection, folderName)){
            LOG.debug("<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>" + checkIfFolderExists(connection, folderName));
            String sql = "UPDATE emails SET folderId = ? WHERE emailId = ?";
            PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
            int folderId = getFolderIdFromName(connection, folderName);
            ps.setInt(1, folderId);
            ps.setInt(2, emailId);
            LOG.info("Finished setting parameters to move an email to a new folder.");
            int rowsModified = ps.executeUpdate();
            LOG.trace("Executed query.");
            return rowsModified;
        }
        else {
            LOG.error("Folder does not exist.");
            throw new SQLException("Cannot add to a nonexistent folder.");
        }
    }
    
    //UPDATE EMAILS (IF IN DRAFTS)
    /**
     * takes an emailBean, and replaces the old version of it in the database with what's in the emailBean now.  we assume they are in drafts, since we'll be able to verify that more easily once we're doing GUI
     * if nothing is deleted, nothing will be added, either
     * @param emailBean the emailBean of the email you'd like to update
     * @return the amount of rows modified
     * @throws SQLException 
     */
    public int updateDraftEmail(EmailBean emailBean) throws SQLException{
        LOG.trace("Began updating draft email.");
        int counter = deleteEmail(emailBean.getEmailId());
        LOG.trace("Deleted email.");
        if (counter > 0){
            counter += insertEmail(emailBean, "Drafts");
            LOG.trace("Inserted updated email.");
            return counter;
        } else {
            LOG.warn("That was not a real draft email");
            return counter;
        }
    }
    
    /**
     * takes an emailId and returns an arraylist of all emailaddresses related to that email, based on data in the db
     * @param connection the connection this is all done through
     * @param emailId the emailId you want to find all related addresses of from the database
     * @return an arraylist of the related email addresses
     * @throws SQLException 
     */
    private ArrayList<EmailAddress> findAllRelatedAddresses(Connection connection, int emailId) throws SQLException {
        String sql = "SELECT * FROM addresses INNER JOIN emailToAddresses ON addresses.addressId = emailToAddresses.addressId " +
                      "INNER JOIN emails ON emailToAddresses.addressId = emails.addressId WHERE emails.emailId = ?";
        ArrayList<EmailAddress> listOfAddresses = new ArrayList<EmailAddress>();
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setInt(1, emailId);
        LOG.info("Finished setting parameters to find all related addresses.");
        ResultSet rs = ps.executeQuery();
        LOG.trace("Executed query.");
        while (rs.next()) {
            EmailAddress newAddress = new EmailAddress(rs.getString("emailAddress"), rs.getString("name"));
            listOfAddresses.add(newAddress);
            LOG.info("Added " + newAddress.getEmail() + " to the EmailAddress arrayList.");
        }
        LOG.trace("Finished getting array list of related email addresses.");
        return listOfAddresses;
    }
    
    
    
    //DELETE/PRIVATE METHODS TO DELETE
    
    //DELETE ALL EMAILS FROM EMAILTOADDRESSES WITH A CERTAIN EMAIL ADDRESS, DELETE ATTACHMENTS ONLY RELATED TO THE ONE EMAIL
    /**
     * takes an emailId and deletes all of its emailToAddresses entries
     * @param connection the connection this is all done through
     * @param emailId the emailId whose emailtoAddresses entries you want to delete
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int deleteRelatedEmailToAddresses(Connection connection, int emailId) throws SQLException{
        String sql = "DELETE FROM emailToAddresses WHERE emailId = ?";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setInt(1, emailId);
        LOG.info("Finished setting parameters to delete all email to addresses entries with specified emailId");
        int counter = ps.executeUpdate();
        LOG.trace("Executed query.");
        return counter;
    }
    
    /**
     * takes an emailId, and deletes all attachments in the DB associated with that email
     * @param connection the connection through which we're accessing the database
     * @param emailId the emailId of the email whose attachments we want to delete
     * @return the amount of rows modified
     * @throws SQLException if it fails to set the int, prepaer the statement, or execute the update
     */
    private int deleteRelatedAttachments(Connection connection, int emailId) throws SQLException{
        String sql = "DELETE FROM attachments WHERE emailId = ?";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setInt(1,emailId);
        LOG.info("Finished setting parameters to delete all email to addresses entries with specified emailId");
        int counter = ps.executeUpdate();
        LOG.trace("Executed query.");
        return counter;
    }
    
    /**
     * deletes the email with the specified emailId from the email table
     * @param connection the connection this is all done through
     * @param emailId the emailId of the email
     * @return the amount of rows modified
     * @throws SQLException 
     */
    private int deleteEmailsFromTable(Connection connection, int emailId) throws SQLException{
        String sql = "DELETE FROM emails WHERE emailId = ?";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setInt(1, emailId);
        LOG.info("Finished setting parameters to delete emails that match a certain emailId");
        int counter = ps.executeUpdate();
        LOG.info("Executed query.");
        return counter;
    }
    
    /**
     * deletes an email from the email table, clearing out the bridging table and the attachment table's related attachments
     * @param emailId the emailId of the email you want to delete
     * @return the amount of rows modified
     * @throws SQLException 
     */
    public int deleteEmail(int emailId) throws SQLException {
        LOG.info("Beginning email deletion");
        Connection connection = generateConnection();
        int counter = deleteRelatedAttachments(connection, emailId);
        LOG.trace("Deleted attachments.");
        counter += deleteRelatedEmailToAddresses(connection, emailId);
        LOG.trace("Deleted emailToAddress entries.");
        counter += deleteEmailsFromTable(connection, emailId);
        LOG.trace(("Deleted email."));
        return counter;
    }
    
    
    //GENERAL UTILITY METHODS
    /**
     * takes an emailaddress and returns the addressid of that emailaddress
     * @param connection the connection this is all done through
     * @param emailAddress the emailAddress you want to find the addressid of
     * @return the addressId of the emailAddress
     * @throws SQLException 
     */
    private int getAddressIdFromEmailAddress(Connection connection, String emailAddress) throws SQLException{
        LOG.debug("Beginning to find the addressId based on the email address");
        String sql = "SELECT * FROM addresses WHERE emailAddress = ?";
        PreparedStatement ps = connection.prepareStatement(sql, 
                                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                ResultSet.CONCUR_READ_ONLY);
        ps.setString(1, emailAddress);
        LOG.info("Finished setting parameters to get the addressId of an email address");
        ResultSet rs = ps.executeQuery();
        LOG.trace("Executed query.");
        rs.next();
        int addressId = rs.getInt("addressId");
        return addressId;
    }
    
    
    /**
     * takes an emailBean and returns an arraylist of all email addresses in any of the to, cc, or bcc fields
     * @param emailBean the emailBean to get all addresses from
     * @return arrayList of all emailAddresses contained inside of to, cc, or bcc
     */
    private ArrayList<EmailAddress> findAllSendingAddressesInEmailBean(EmailBean emailBean){
        HashSet<EmailAddress> setOfAddresses = new HashSet<EmailAddress>();
        for (EmailAddress address : emailBean.getTos()){
            setOfAddresses.add(address);
            LOG.info("Got all 'to' addresses from the email bean.");
        }
        for (EmailAddress address : emailBean.getCCs()){
            setOfAddresses.add(address);
            LOG.info("Got all 'cc' addresses from the email bean.");
        }
        for (EmailAddress address : emailBean.getBCCs()){
            setOfAddresses.add(address);
            LOG.info("Got all 'bcc' addresses from the email bean.");
        }
        ArrayList<EmailAddress> addressList = new ArrayList<EmailAddress>(setOfAddresses);
        LOG.trace("Finished getting all recipient addresses from the email bean.");
        return addressList;
    }
    
    /**
     * finds the folderId of a folder, based on the name you entered
     * @param connection the connection this is all done through
     * @param folderName the folder name we're looking for
     * @return the folderId of the folder you're looking for
     * @throws SQLException 
     */
    private int getFolderIdFromName(Connection connection, String folderName) throws SQLException{
        String sql = "SELECT * FROM folders WHERE name = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, folderName);
        LOG.info("Finished setting parameters to get a folderId based on its folder name.");
        ResultSet rs = ps.executeQuery();
        LOG.trace("Executed query.");
        rs.next();
        int folderId = rs.getInt("folderId");
        LOG.trace("Found folderId");
        return folderId;
    }
    
    /**
     * creates an arraylist of emailbeans from the resultset of emails that was received
     * @param conn the connection to work using
     * @param rs the resultset to constrict the emailBeans from
     * @param folderName the folder that these emails were stored inside of
     * @return an arraylist of emailbeans stored in the resultset
     * @throws SQLException 
     */
    private ArrayList<EmailBean> generateReceivedEmailBeanListFromFolder(Connection conn, ResultSet rs, String folderName) throws SQLException {
        int folderId = getFolderIdFromName(conn, folderName);
        LOG.trace("Got the folderId of the folder.");
        ArrayList<EmailBean> emailBeans = new ArrayList<EmailBean>();
        while (rs.next()){
            EmailBean newBean = new EmailBean();
            newBean.setEmailId(rs.getInt("emailId"));
            newBean.setFolderId(folderId);
            Email email = generateReceivedEmail(rs.getString("emailAddress"), rs.getString("subject"), rs.getString("message"), rs.getString("htmlMessage"), rs.getDate("sendDate"), rs.getDate("receiveDate"));
            newBean.setEmail(email);
            emailBeans.add(newBean);
            
        }
        LOG.trace("Finished creating the arraylist of email beans.");
        return emailBeans;
    }
    
    /**
     * takes multiple properties, and constructs it into a basic email, to be saved into the database as a received email.
     * @param from who the email is from
     * @param subject the email's subject
     * @param textMessage the text message of the email
     * @param htmlMessage the html message of the email (will overwrite text message, if not empty)
     * @param sentDate the date the email was sent
     * @param receivedDate the date the email was received
     * @return the constructed email from these properties
     */
    private Email generateReceivedEmail(String from, String subject, String textMessage, String htmlMessage, Date sentDate, Date receivedDate){
        Email email = new Email();
        email.from(from);
        email.subject(subject);
        email.textMessage(textMessage);
        if (!htmlMessage.equals("")){
            email.htmlMessage(htmlMessage);
        }
        email.sentDate(sentDate);
        LOG.trace("Finished copying properties over.");
        return email;
    }
    
    /**
     * gets the names of all folders that exist on the database
     * @return an arraylist of all foldernames
     * @throws SQLException 
     */
    public ArrayList<String> getAllFolderNames() throws SQLException{
        LOG.trace("Attempting to get the names of all folders in the database.");
        String sql = "SELECT * FROM folders";
        ArrayList<String> folderNames = new ArrayList<String>();
        try (Connection conn = generateConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                folderNames.add(rs.getString("name"));
            }
        }
        
        LOG.trace("Finished building arraylist of all folder names.");
        return folderNames;
    }
    
    /**
     * gets all emails, inside of all folders, saves them all into the appropriate format for a folderbean,
     * and then returns an observable list of those folderbeans
     * @return the folderbeans we've produced inside of this method.
     * @throws SQLException 
     */
    public ObservableList<FolderBean> getAllFolders() throws SQLException {
        LOG.debug("Beginning to look for all folders.");
        ArrayList<String> folderNames = getAllFolderNames();
        LOG.debug("Found all folder names.");
        ObservableList<FolderBean> folderBeans = FXCollections.observableArrayList();
        for (String folder : folderNames){
            LOG.debug("Attempting to create a folder bean based on the name of the folder: " + folder);
            FolderBean folderBean = new FolderBean();
            ArrayList<EmailBean> emailBeans = findEmailInFolder(folder);
            for (EmailBean eBean : emailBeans){
                EmailFXBean newFxBean = convertEmailBean(eBean);
                folderBean.addEmail(newFxBean);
            }
            folderBean.setFolderName(folder);
            LOG.debug("Finished constructing the folder bean.");
            folderBeans.add(folderBean);
            LOG.debug("Added the folder bean to the list.");
        }
        LOG.debug("Finished constructing the folder bean observablelist.");
        return folderBeans;
    }
    
    /**
     * converts an emailBean into an EmailFXBean
     * @param bean the emailBean we wish to convert to an emailFXBean
     * @return the appropriate emailfxbean
     */
    public EmailFXBean convertEmailBean(EmailBean bean){
        EmailFXBean newFxBean = new EmailFXBean();
        Email email = bean.getEmail();
        newFxBean.setEmailId((Integer.toString(bean.getEmailId())));
        newFxBean.setFromField(email.from().getEmail());
        newFxBean.setSubjectField(email.subject());
        String message;
        //sets a message to be empty if it happens to be null
        if (email.messages().get(1).getContent() == null){
            message = "";
        }
        else {
            message = email.messages().get(1).getContent();
        }
        newFxBean.setHtmlField(message);
        String date;
        //adds the current time if the sent date was null
        if (email.sentDate() == null){
            date = LocalDateTime.now().toString();
        }else {
            date = email.sentDate().toString();
        }
        LOG.trace("Finished copying fields over.");
        newFxBean.setDate(date);
        return newFxBean;
    }
    
    /**
     * deletes a folder that has a certain name, and deletes all emails inside
     * @param folderName the folder which we wish to delete
     * @return the amount of rows affected
     * @throws SQLException 
     */
    public int deleteFolder(String folderName) throws SQLException {
        Connection conn = generateConnection();
        int folderId = getFolderIdFromName(conn, folderName);
        String sql = "SELECT * FROM emails WHERE folderId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, folderId);
        LOG.trace("Set parameters to get all emails within a folder.");
        ResultSet rs = ps.executeQuery();
        ArrayList<Integer> emailIds = new ArrayList<Integer>();
        while (rs.next()){
            emailIds.add(rs.getInt("emailId"));
        }
        int counter = 0;
        for (Integer num : emailIds){
            counter += deleteEmail(num);
        }
        LOG.trace("Finished deleting all emails within the folder.");
        
        sql = "DELETE FROM folders WHERE name = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, folderName);
        counter += ps.executeUpdate();
        LOG.trace("Deleted the folder.");
        
        return counter;
    }
    
    /**
     * returns an arraylist of attachment beans that are the attachments related to the emailid
     * @param emailId the emailid whose attachments you want
     * @return the arraylist of attachment beans
     * @throws SQLException 
     */
    public ArrayList<AttachmentBean> getRelatedAttachmentBeans(int emailId) throws SQLException {
        Connection conn = generateConnection();
        String sql = "SELECT * FROM attachments WHERE emailId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, emailId);
        LOG.trace("Set parameters to get all attachments of an emailId.");
        ResultSet rs = ps.executeQuery();
        ArrayList<AttachmentBean> beanList = new ArrayList<AttachmentBean>();
        while (rs.next()){
            AttachmentBean newBean = new AttachmentBean();
            newBean.setIsEmbedded(rs.getBoolean("isEmbedded"));
            newBean.setAttachment(rs.getBytes("file"));
            newBean.setFileName(rs.getString("fileName"));
            beanList.add(newBean);
        }
        LOG.trace("Created the arrayList of attachment beans.");
        return beanList;
    }
}
