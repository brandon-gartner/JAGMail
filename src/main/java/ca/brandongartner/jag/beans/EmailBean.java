/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.activation.DataSource;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
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
    private int emailId;
    
    public Email getEmail(){
        return this.containedEmail;
    }
    
    public int getFolderId(){
        return this.folderId;
    }
    
    public java.sql.Timestamp getSentDate(){
        if (containedEmail.sentDate() == null){
            return new java.sql.Timestamp(System.currentTimeMillis());
        }
        return new java.sql.Timestamp(containedEmail.sentDate().getTime());
    }
    
    public int getEmailId(){
        return this.emailId;
    }
    
    public ArrayList<EmailAddress> getTos(){
        EmailAddress[] arrayOfTos = containedEmail.to();
        ArrayList<EmailAddress> tos = new ArrayList<EmailAddress>();
        Collections.addAll(tos, arrayOfTos);
        return tos;
    }
    
    public ArrayList<EmailAddress> getCCs(){
        EmailAddress[] arrayOfCCs = containedEmail.cc();
        ArrayList<EmailAddress> ccs = new ArrayList<EmailAddress>();
        Collections.addAll(ccs, arrayOfCCs);
        return ccs;
    }
    
    public ArrayList<EmailAddress> getBCCs(){
        EmailAddress[] arrayOfTos = containedEmail.bcc();
        ArrayList<EmailAddress> bccs = new ArrayList<EmailAddress>();
        Collections.addAll(bccs, arrayOfTos);
        return bccs;
    }
    
    public String getFrom(){
        return containedEmail.from().getEmail();
    }
    
    public String getSubject(){
        return containedEmail.subject();
    }
    
    public String getMessage(){
        List<EmailMessage> message = containedEmail.messages();
        return message.get(0).getContent();
    }
    
    public String getHtmlMessage(){
        return containedEmail.messages().get(1).getContent();
    }
    
    public List<EmailAttachment<? extends DataSource>> getAttachments(){
        return containedEmail.attachments();
    }
    
    public void setEmail(Email email){
        this.containedEmail = email;
    }
    
    public void setFolderId(int folderId){
        this.folderId = folderId;
    }
    
    
    public void setEmailId(int newId){
        this.emailId = newId;
    }
    
}
