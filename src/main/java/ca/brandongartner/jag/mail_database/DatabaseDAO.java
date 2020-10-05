/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database;

import ca.brandongartner.jag.beans.AttachmentBean;
import ca.brandongartner.jag.beans.EmailBean;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import jodd.mail.EmailAddress;
import javax.activation.DataSource;
import jodd.mail.EmailAttachment;


//GENERAL TODO:
//FINISH CREATE SETTING UP BRIDGING TABLE FOR ADDRESSES
//UPDATE/DELETE
//COMMENTS
//LOG4J
//TESTING
//TODO: EMAILBEAN GENERATOR FROM FINDEMAIL RESULTSET

/**
 *
 * @author Brandon Gartner
 */
public class DatabaseDAO {
    
    private final static Logger LOG = LoggerFactory.getLogger(DatabaseDAO.class);
    
    private final String url = "jdbc:mysql://localhost:3306/mysql?zeroDateTimeBehavior=CONVERT_TO_NULL";
    private final String user = "root";
    private final String password = "dawson";
    
    private Connection generateConnection() throws SQLException{
        try{
            Connection connection = DriverManager.getConnection(this.url, this.user, this.password);
            return connection;
        }
        catch (SQLException sqlex){
            throw new SQLException("Connection to database failed!");
        }
    }
    
    //TODO: CREATE SENT EMAIL
    public int insertSentEmail(EmailBean emailBean, String folderName) throws SQLException{
        Connection connection = generateConnection();
        int counter = addEmailRecipientsToAddressesTable(connection, emailBean);
        counter += addAttachmentsToAttachmentTable(connection, emailBean);
        String sql = "INSERT INTO emails (subject, message, htmlMessage, sendDate, receiveDate, folderId, from)" + 
                      " VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, emailBean.getSubject());
        ps.setString(2, emailBean.getMessage());
        ps.setString(3, emailBean.getHtmlMessage());
        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        
        int folderId = getFolderIdFromName(connection, folderName);
        
        ps.setInt(6, folderId);
        
        int fromId = getAddressIdFromEmailAddress(connection, emailBean.getFrom());
        
        ps.setInt(7, fromId);
        
        counter += ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()){
            int emailId = rs.getInt(1);
            emailBean.setEmailId(emailId);
        }
        
        addToEmailsToAddresses(connection, emailBean);
        //get from field
        //create bridging tables
        //insert email
    }
    
    private void addToEmailsToAddresses(Connection connection, EmailBean emailBean) throws SQLException{
        addTosToBridging(connection, emailBean);
        addCCsToBridging(connection, emailBean);
        addBCCsToBridging(connection, emailBean);
    }
    
    private void addTosToBridging(Connection connection, EmailBean emailBean) throws SQLException{
        List<EmailAddress> listOfTos = emailBean.getTos();
        String sql = "INSERT INTO emailToAddresses (emailId, type, addressId) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (EmailAddress address : listOfTos){
            int addressId = getAddressIdFromEmailAddress(connection, address.getEmail());
            ps.setInt(1, emailBean.getEmailId());
            ps.setString(2, "To");
            ps.setInt(3, addressId);
            ps.executeUpdate();
        }
    }
    
    private void addCCsToBridging(Connection connection, EmailBean emailBean) throws SQLException{
        List<EmailAddress> listOfCCs = emailBean.getCCs();
        String sql = "INSERT INTO emailToAddresses (emailId, type, addressId) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (EmailAddress address : listOfCCs){
            int addressId = getAddressIdFromEmailAddress(connection, address.getEmail());
            ps.setInt(1, emailBean.getEmailId());
            ps.setString(2, "CC");
            ps.setInt(3, addressId);
            ps.executeUpdate();
        }
    }
    
    private void addBCCsToBridging(Connection connection, EmailBean emailBean) throws SQLException{
        List<EmailAddress> listOfBCCs = emailBean.getBCCs();
        String sql = "INSERT INTO emailToAddresses (emailId, type, addressId) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (EmailAddress address : listOfBCCs){
            int addressId = getAddressIdFromEmailAddress(connection, address.getEmail());
            ps.setInt(1, emailBean.getEmailId());
            ps.setString(2, "BCC");
            ps.setInt(3, addressId);
            ps.executeUpdate();
        }
    }
    
    private int getFolderIdFromName(Connection connection, String folderName) throws SQLException{
        String sql = "SELECT * FROM folders WHERE name = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, folderName);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int folderId = rs.getInt("folderId");
        return folderId;
    }
    
    
    /**
     * 
     * @param emailBean
     * @return 
     */
    private HashSet<EmailAddress> getCombinedRecipientSet(EmailBean emailBean){
        HashSet<EmailAddress> recipients = new HashSet<EmailAddress>();
        for (EmailAddress address : emailBean.getTos()){
            recipients.add(address);
        }
        for (EmailAddress address : emailBean.getCCs()){
            recipients.add(address);
        }
        for (EmailAddress address : emailBean.getBCCs()){
            recipients.add(address);
        }
        return recipients;
    }
    
    /**
     * 
     * @param connection
     * @param recipients
     * @return
     * @throws SQLException 
     */
    private ArrayList<EmailAddress> findRecipientsMissingFromAddresses(Connection connection, HashSet<EmailAddress> recipients) throws SQLException{
        ArrayList<EmailAddress> recipientsNotInDB = new ArrayList<EmailAddress>(recipients);
        String sql = "SELECT emailAddress FROM addresses WHERE emailAddress = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (EmailAddress address : recipientsNotInDB){
            ps.setString(1, address.getEmail());
            ResultSet result = ps.executeQuery();
            if (checkIfEmailInsideOfResultSet(result, address.getEmail())){
                continue;
            }
            else {
                recipientsNotInDB.remove(address);
            }
        }
        return recipientsNotInDB;
    }
    
    /**
     * 
     * @param rs
     * @param address
     * @return
     * @throws SQLException 
     */
    private boolean checkIfEmailInsideOfResultSet(ResultSet rs, String address) throws SQLException{
        String receivedEmail = rs.getString("emailAddress");
        if (receivedEmail != null && receivedEmail.equals(address)){
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * 
     * @param connection
     * @param emailBean
     * @return
     * @throws SQLException 
     */
    private int addEmailRecipientsToAddressesTable(Connection connection, EmailBean emailBean) throws SQLException{
        HashSet<EmailAddress> addressesSet = getCombinedRecipientSet(emailBean);
        ArrayList<EmailAddress> addressesNotInDB = findRecipientsMissingFromAddresses(connection, addressesSet);
        String sql = "INSERT INTO addresses (emailAddress, name) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        int addedCounter = 0;
        for (EmailAddress address : addressesNotInDB){
            ps.setString(1, address.getEmail());
            ps.setString(2, address.getPersonalName());
            addedCounter++;
        }
        return addedCounter;
    }
    
    /**
     * modified to not check for duplicates since apparently we don't need a bridging table for this
     * @param connection
     * @param emailBean
     * @return
     * @throws SQLException 
     */
    private int addAttachmentsToAttachmentTable(Connection connection, EmailBean emailBean) throws SQLException{
        HashSet<AttachmentBean> attachmentSet = getAttachmentSet(emailBean);
        //ArrayList<AttachmentBean> beansToAdd = findMissingAttachments(connection, attachmentSet);
        String sql = "INSERT INTO attachments (file, isEmbedded) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        int counter = 0;
        //modified to use attachmentSet instead of beansToAdd
        for (AttachmentBean bean : attachmentSet){
            ps.setBytes(1, bean.getAttachment());
            ps.setBoolean(2, bean.getIsEmbedded());
            ps.executeUpdate();
            ResultSet resultSet = ps.getGeneratedKeys();
            if (resultSet.next()){
                bean.setId(resultSet.getInt(1));
            }
            counter += 1;
        }
        return counter;
    }
    
    private HashSet<AttachmentBean> getAttachmentSet(EmailBean emailBean){
        HashSet<AttachmentBean> attachmentSet = new HashSet<AttachmentBean>();
        List<EmailAttachment<? extends DataSource>> attachments = emailBean.getAttachments();
        for (EmailAttachment attachment : emailBean.getAttachments()){
            AttachmentBean newAttachmentBean = generateAttachmentBean(attachment);
            attachmentSet.add(newAttachmentBean);
        }
        return attachmentSet;
    }
    
    private AttachmentBean generateAttachmentBean(EmailAttachment emailAttachment){
        AttachmentBean newBean = new AttachmentBean();
        newBean.setIsEmbedded(emailAttachment.getContentId() != null);
        newBean.setFileName(emailAttachment.getName());
        newBean.setAttachment(emailAttachment.toByteArray());
        return newBean;
    }
    
    private ArrayList<AttachmentBean> findMissingAttachments(Connection connection, HashSet<AttachmentBean> attachments) throws SQLException{
        ArrayList<AttachmentBean> attachmentsNotInDB = new ArrayList<AttachmentBean>(attachments);
        //String sql = "SELECT emailAddress FROM addresses WHERE emailAddress = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        for (AttachmentBean attachment : attachmentsNotInDB){
            ps.setBytes(1, attachment.getAttachment());
            ResultSet result = ps.executeQuery();
            if (checkIfAttachmentInsideOfResultSet(result, attachment.getAttachment())){
                continue;
            }
            else {
                attachmentsNotInDB.remove(attachment);
            }
        }
        return attachmentsNotInDB;
    }
    
    private boolean checkIfAttachmentInsideOfResultSet(ResultSet rs, byte[] bytes) throws SQLException{
        byte[] receivedAttachment = rs.getBytes("file");
        if (receivedAttachment != null && receivedAttachment.equals(bytes)){
            return true;
        }
        else {
            return false;
        }
    }
    
    
    
    
    //tests for each:
    //exceeding name length
    
    //CREATE RECEIVED EMAIL
    /**
     * 
     * @param emailBean
     * @return 
     */
    public int insertReceivedEmail(EmailBean emailBean){
        
    }
    
    public int insertDraftEmail(EmailBean emailBean){
        
    }
    
    /**
     * Takes a string, and creates a new folder with that name.
     * @param connection the connection through which all database interactions will operate
     * @param name the name of the new folder
     * @return 1, if the folder is successfully created
     * @throws SQLException if there is an error when setting the string, or generating the preparedStatement
     */
    public int createFolder(Connection connection, String name) throws SQLException{
        if (name.length() > 50){
            throw new IllegalArgumentException("Name of the new folder was too long, should be 50 characters maximum.");
        }
        String sql = "INSERT INTO folders (name) VALUES (?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, name);
        ps.executeUpdate();
        return 1;
    }
    
    //UPDATE EMAILS (IF IN DRAFTS)
    public int updateDraftEmail(EmailBean emailBean){
        
    }
    
    
    //DELETE EMAILS FROM INBOX/DRAFTS
    public int deleteEmail(int emailId){
        deleteRelatedEmailStuff(emailId);
        //deleteemails
    }
    
    //DELETE ALL EMAILS FROM EMAILTOADDRESSES WITH A CERTAIN EMAIL ADDRESS, DELETE ATTACHMENTS ONLY RELATED TO THE ONE EMAIL
    private int deleteRelatedEmailStuff(int emailId){
        
    }
    
    /**
     * 
     * @param connection
     * @param emailId
     * @param folderId
     * @return
     * @throws SQLException 
     */
    //UPDATE EMAIL FOLDER (NOT FROM SENT/DRAFTS, NOT INTO SENT/DRAFTS
    public int updateEmailFolder(Connection connection, int emailId, int folderId) throws SQLException{
        if (checkIfFolderExists(connection, folderId)){
            String sql = "UPDATE emails SET folderId = ? WHERE emailId = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, folderId);
            ps.setInt(2, emailId);
            int rowsModified = ps.executeUpdate();
            return rowsModified;
        }
        else {
            throw new SQLException("Cannot add to a nonexistent folder.");
        }
    }
    
    /**
     * 
     * @param connection
     * @param folderId
     * @return 
     */
    private boolean checkIfFolderExists(Connection connection, int folderId) throws SQLException{
        String sql = "SELECT * FROM folders WHERE folderId = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, folderId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        if (rs.getInt(folderId) == folderId){
            return true;
        }
        else {
            return false;
        }
        
    }
    
    
    
    /**
     * 
     * @param connection
     * @param folderName
     * @return
     * @throws SQLException 
     */
    public ResultSet findEmailInFolder(Connection connection, String folderName) throws SQLException{
        String sql = "SELECT * FROM emails " +
                      "INNER JOIN folders ON emails.folderId = folders.folderId " +
                      "WHERE folders.name = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, folderName);
        ResultSet rs = ps.executeQuery();
        return rs;
    }
    
    /**
     * 
     * @param connection
     * @param messageToSearch
     * @return
     * @throws SQLException 
     */
    public ResultSet findStringInEmail(Connection connection, String messageToSearch) throws SQLException{
        String sql = "SELECT * FROM emails WHERE message LIKE %?% OR htmlMessage LIKE %?% OR subject LIKE %?%";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, messageToSearch);
        ps.setString(2, messageToSearch);
        ps.setString(3, messageToSearch);
        ResultSet rs = ps.executeQuery();
        return rs;
    }
    
    public ResultSet findEmailsFromEmailAddress(Connection connection, String emailAddress) throws SQLException{
        String sql = "SELECT * FROM emails WHERE addresses.emailAddress = ?" + 
                      "INNER JOIN emailToAddresses ON emails.emailId = emailToAddresses.emailId " + 
                      "INNER JOIN addresses ON emailToAddresses.addressId = addresses.addressId";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, emailAddress);
        ResultSet rs = ps.executeQuery();
        return rs;
    }
    
    private int getAddressIdFromEmailAddress(Connection connection, String emailAddress) throws SQLException{
        String sql = "SELECT * FROM addresses WHERE emailAddress = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, emailAddress);
        ResultSet rs = ps.executeQuery();
        rs.next();
        int addressId = rs.getInt("addressId");
        return addressId;
    }
    
    
}
