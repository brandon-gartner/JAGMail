/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_database_tests;

import ca.brandongartner.jag.beans.AttachmentBean;
import ca.brandongartner.jag.beans.EmailBean;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import jodd.mail.Email;
import org.junit.Test;

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
    Email email1 = new Email();
    Email email2 = new Email();
    String message1 = "hi";
    String message2 = "hi2.0";
    String subject1 = "hello there";
    String subject2 = "general kenobi";
    String htmlMessage1 = "<!DOCTYPE html><html><body><h1>yes</h1><p>no</p></body></html>";
    String htmlMessage2 = "<!DOCTYPE html><html><body><h1>no</h1><p>yes</p></body></html>";
    String emailAddress1 = "bg01test@gmail.com";
    String emailAddress2 = "bg02test@gmail.com";
    String emailAddress3 = "bg03test@gmail.com";
    String emailAddress4 = "bg04test@gmail.com";
    EmailBean emailBean1 = new EmailBean();
    EmailBean emailBean2 = new EmailBean();
    AttachmentBean attachmentBean1 = new AttachmentBean();
    AttachmentBean attachmentBean2 = new AttachmentBean();
    File bliss = new File("bliss.png");
    File blue = new File("blue.png");
    ArrayList<File> singleFile = new ArrayList<File>();
    ArrayList<File> doubleFile = new ArrayList<File>();
    //insert email
    //create folder
    //findstringinemail
    //findemailinfolder
    //updateEmailFolder
    //deleteEmail
    //findEmailsfromEmailAddress
    
    @Before
    public void ranBeforeTests(){
        singleFile.add(bliss);
        doubleFile.add(bliss);
        doubleFile.add(blue);
        email1.to(emailAddress2);
        email1.cc(emailAddress3);
        email1.bcc(emailAddress4);
        email1.subject(subject1);
        email1.textMessage(message1);
        emailBean1.setEmail(email1);
        
    }
    
    //1.1
    @Test
    public void testInsertEmail(){
        
    }
    
    //1.2
    @Test
    
    //1.3
    @Test
    
    //1.4
    @Test
    
    //1.5
    @Test
    
    //2.1
    @Test
    
    //2.2
    @Test
    
    //2.3
    @Test
    
    //2.4
    @Test
    
    //2.5
    @Test
    
    //3.1
    @Test
    
    //3.2
    @Test
    
    //3.3
    @Test
    
    //3.4
    @Test
    
    //3.5
    @Test
    
    //4.1
    @Test
    
    //4.2
    @Test
    
    //4.3
    @Test
    
    //4.4
    @Test
    
    //4.5
    @Test
    
    //5.1
    @Test
    
    //5.2
    @Test
    
    //5.3
    @Test
    
    //5.4
    @Test
    
    //5.5
    @Test
    
    //6.1
    @Test
    
    //6.2
    @Test
    
    //6.3
    @Test
    
    //6.4
    @Test
    
    //6.5
    @Test
    
    //7.1
    @Test
    
    //7.2
    @Test
    
    //7.3
    @Test
    
    //7.4
    @Test
    
    //7.5
    @Test
    
    private ArrayList<EmailBean> constructEmailBeans(ResultSet rs) throws SQLException{
        ArrayList<EmailBean> beanList = new ArrayList<EmailBean>();
        while (rs.next()){
            EmailBean toReturn = constructIndividualEmailBean(rs);
            beanList.add(toReturn);
        }
        return beanList;
    }
    
    //add attachment functionality to this, add cc/bcc/to if have time
    private EmailBean constructIndividualEmailBean(ResultSet rs) throws SQLException{
        EmailBean newBean = new EmailBean();
        Email email = new Email();
        email.subject(rs.getString("subject"));
        email.textMessage(rs.getString("message"));
        email.htmlMessage(rs.getString("htmlMessage"));
        
    }
    
}
