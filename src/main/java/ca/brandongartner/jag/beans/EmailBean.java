/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;

import jodd.mail.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Brandon Gartner
 */
public class EmailBean {
    private final static Logger LOG = LoggerFactory.getLogger(EmailBean.class);
    
    private Email containedEmail;
    private int folderId;
    private java.sql.Timestamp receivedDate;
    public EmailBean(Email containedEmail, int folderId, java.sql.Timestamp receivedDate){
        this.containedEmail = containedEmail;
        this.folderId = folderId;
        this.receivedDate = receivedDate;
    }
    
    //WARNING, CHANGE THIS TO THE INDIVIDUAL EMAIL THINGS LATER
    public Email getEmail(){
        return this.containedEmail;
    }
    
    public int getFolderId(){
        return this.folderId;
    }
    
    public java.sql.Timestamp getReceivedDate(){
        return this.receivedDate;
    }
}
