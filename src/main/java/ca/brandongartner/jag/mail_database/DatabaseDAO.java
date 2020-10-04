/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database;

import ca.brandongartner.jag.beans.EmailBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import jodd.mail.EmailAddress;

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
    public int insertSentEmail(EmailBean emailBean) throws SQLException{
        Connection connection = generateConnection();
        int counter = addEmailRecipientsToAddressesTable(connection, emailBean);
        //TODO: same stuff but using attachments
        //create bridging tables
        //insert email
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
    }
    
    //DELETE ALL EMAILS FROM EMAILTOADDRESSES WITH A CERTAIN EMAIL ADDRESS
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
    private boolean checkIfFolderExists(Connection connection, int folderId){
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
    
    //READ EMAIL AND PRODUCE EMAILBEAN FROM IT
    //READ EMAIL FROM DATABASE AND CREATE EMAILBEAN FROM IT
    public int findEmail(EmailBean emailBean){
        
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
    
    
    //COMMENTS
    //LOG4J
    //TESTING
}
