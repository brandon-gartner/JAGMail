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
    
    private boolean checkIfEmailInsideOfResultSet(ResultSet rs, String address) throws SQLException{
        String receivedEmail = rs.getString("emailAddress");
        if (receivedEmail != null && receivedEmail.equals(address)){
            return true;
        }
        else {
            return false;
        }
    }
    
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
    
    //CREATE RECEIVED EMAIL
    public int insertReceivedEmail(EmailBean emailBean){
        
    }
    
    public int insertDraftEmail(EmailBean emailBean){
        
    }
    
    //UPDATE EMAILS (IF IN DRAFTS)
    public int updateDraftEmail(EmailBean emailBean){
        
    }
    
    
    //DELETE EMAILS FROM INBOX/DRAFTS
    public int deleteEmail(EmailBean emailBean){
        
    }
    
    //DELETE ALL EMAILS FROM EMAILTOADDRESSES WITH A CERTAIN EMAIL ADDRESS
    private int deleteRelatedEmailStuff(EmailBean emailBean){
        
    }
    
    //UPDATE EMAIL FOLDER (NOT FROM SENT/DRAFTS, NOT INTO SENT/DRAFTS
    public int updateEmailFolder(EmailBean emailBean){
        
    }
    
    //READ EMAIL AND PRODUCE EMAILBEAN FROM IT
    //READ EMAIL FROM DATABASE AND CREATE EMAILBEAN FROM IT
    public int findEmail(EmailBean emailBean){
        
    }
    
    public int findEmailInFolder(String folderName){
        sql = "SELECT * FROM "
    }
    
    public int findEmailMessage(EmailBean emailBean){
        
    }
    
    public int findEmailSubject(EmailBean emailBean){
        
    }
    
    
    //COMMENTS
    //LOG4J
    //TESTING
}
