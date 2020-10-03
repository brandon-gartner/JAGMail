/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.mail_business_tests;
import ca.brandongartner.jag.mail_business.MainApplication;
import ca.brandongartner.jag.beans.MailConfigBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.io.File;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ReceivedEmail;
import jodd.mail.MailException;
import jodd.mail.EmailAddress;
import javax.activation.DataSource;
import java.util.List;
/**
 *
 * @author Brandon Gartner
 * Just a note to my teacher, for some reason (probably my internet), on my laptop, where I did most of this, most of the methods took a really long time to do, 
 * so I had to give them decent timeouts (10s)
 * just so that they never fail based on that, instead of how it is right now (6s), where maybe every 4 test runs one of them will fail based on that.
 * Also, the methods using receive basically all got a longer timeout since they took much longer to run than the sending methods.
 * Also, fun fact, on bg01test@gmail.com, the account I used to send emails, I hit the daily email limit, completely unintentionally, by only sending emails via testing.
 */


public class TestMainApplication {
    private MailConfigBean sendingConfigBean;
    private MailConfigBean receivingConfigBean;
    private MainApplication sending;
    private MainApplication receiving;
    private ArrayList<String> to;
    private ArrayList<String> cc;
    private ArrayList<String> bcc;
    private ArrayList<File> attachments;
    private ArrayList<File> embeddedAttachments;
    
    
    @Before
    /**
     * generates new config beans, new mainApplications, and empties all arraylist email fields,
     * so that it's ready for the next test
     */
    public void createBeans(){
        sendingConfigBean = new MailConfigBean("smtp.gmail.com", "bg01test@gmail.com", "Dawson123");
        receivingConfigBean = new MailConfigBean("imap.gmail.com", "bg02test@gmail.com", "Dawson123");
        sending = new MainApplication(sendingConfigBean);
        receiving = new MainApplication(receivingConfigBean);
        to = new ArrayList<String>();
        cc = new ArrayList<String>();
        bcc = new ArrayList<String>();
        attachments = new ArrayList<File>();
        embeddedAttachments = new ArrayList<File>();
    }
    
    /**
     * 1. sending empty email to 1 email address, essentially is testing if the to() method works
     * @throws MailException if an email address fails to be verified when attempting to send an email
     */
    @Test(timeout = 10000)
    public void testSendEmptyMessage() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "", "", "", attachments, embeddedAttachments);
        assertSame((email.to()[0].getEmail()), receivingConfigBean.getUserEmailAddress());
    }
    
    /**
     * 2.  sending email with nothing but text message (and a person it's being sent to)
     * @throws MailException if the email it's being sent to is invalid
     */
    @Test(timeout = 10000)
    public void testSendTextOnlyMessage() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        String emailText = "this is the email with nothing but text and a recipient";
        Email email = sending.sendEmail(to, cc, bcc, "", emailText, "", attachments, embeddedAttachments);
        List<EmailMessage> messages = email.messages();
        messages.stream().map((msg) -> {
             assertSame(emailText, msg.getContent());
             return msg;
        });
    }
    
    /**
     * 3. sending email with subject (and a person it's being sent to)
     * @throws MailException if one of the entered email addresses is invalid
     */
    @Test(timeout = 10000)
    public void testSendTextSubjectHTMLEmail() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "test email 3", "", "", attachments, embeddedAttachments);
        assertSame(email.subject(), "test email 3");
    }
    
    /**
     * 4. sending email with HTML (and a person it's being sent to).  checks if the html message of the email object is the same as the one we chose to send
     * @throws MailException if the email address is invalid, or if sending the email fails
     */
    @Test(timeout = 10000)
    public void testSendHTMLEmail() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        String htmlMessage = "<html><META http-equiv=Content-Type "
                            + "content=\"text/html; charset=utf-8\">"
                            + "<body><h1>HTML Message</h1>"
                            + "<h2>Here is some text in the HTML message (3)</h2></body></html>";
        Email email = sending.sendEmail(to, cc, bcc, "", "", htmlMessage, attachments, embeddedAttachments);
        List<EmailMessage> messages = email.messages();
        messages.stream().map((msg) -> {
             assertSame(htmlMessage, msg.getContent());
             return msg;
        });
    }
    
    
    /**
     * 5. send email without recipient
     * @throws MailException if the email entered is invalid (it is)
     */
    @Test(expected = MailException.class, timeout = 10000)
    public void testSendWithoutRecipient() throws MailException{
        Email email = sending.sendEmail(to, cc, bcc, "", "this email has no recipient", "", attachments, embeddedAttachments);
    }
    
    /**
     * 6. send email with multiple recipients, check if the recipients we send to match those in the email object
     * @throws MailException if any of the entered emails are invalid
     */
    @Test(timeout = 10000)
    public void testSendMultipleRecipients() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        to.add("bg03test@gmail.com");
        Email email = sending.sendEmail(to, cc, bcc, "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", attachments, embeddedAttachments);
        boolean same = false;
        if ((to.get(0).equals(email.to()[0].toString())) && (to.get(1).equals(email.to()[1].toString()))){
            same = true;
        }
        assertTrue(same);
    }
    
    
    /**
     * 7. send email with one recipient, one cc, checks to see if our CC is the same as the one that is stored in the email object
     * @throws MailException if the email address of the CC person is invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientOneCC() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        cc.add("bg03test@gmail.com");
        Email email = sending.sendEmail(to, cc, bcc, "", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "", attachments, embeddedAttachments);
        assertEquals(email.cc()[0].toString(), "bg03test@gmail.com");
    }
    
    //
    /**
     * 8. send email with one recipient, one bcc, checks if our stored bcc is the same as the one in the email object
     * @throws MailException if the email address of the bcc person is invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientOneBCC() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        bcc.add("bg04test@gmail.com");
        Email email = sending.sendEmail(to, cc, bcc, "", "cccccccccccccccccccccccccccccccccccccccc", "", attachments, embeddedAttachments);
        assertEquals(email.bcc()[0].toString(), "bg04test@gmail.com");
    }
    
    
    /**
     * 9. send email with one recipient, two cc, checks if both people stored for CC match those stored in the email object
     * @throws MailException if either of the CC email addresses are invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientTwoCC() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        String[] ccsArray = {"bg03test@gmail.com", "bg04test@gmail.com"};
        cc.add(ccsArray[0]);
        cc.add(ccsArray[1]);
        Email email = sending.sendEmail(to, cc, bcc, "", "ddddddddddddddddddddddddddddddddddd", "", attachments, embeddedAttachments);
        EmailAddress[] ccAddresses = email.cc();
        boolean same = true;
        for (int iterator = 0; iterator < ccsArray.length; iterator++){
            if (ccAddresses[iterator].toString() != ccsArray[iterator]){
                same = false;
            }
        }
        assertTrue(same);
    }
    
    /**
     * 10. send email with one recipient, two bcc, checks if our stored bccs match those in the new email object
     * @throws MailException if either of the bccs are invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientTwoBCC() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        String[] bccsArray = {"bg03test@gmail.com", "bg04test@gmail.com"};
        bcc.add(bccsArray[0]);
        bcc.add(bccsArray[1]);
        Email email = sending.sendEmail(to, cc, bcc, "", "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", "", attachments, embeddedAttachments);
        EmailAddress[] bccAddresses = email.bcc();
        boolean same = true;
        for (int iterator = 0; iterator < bccsArray.length; iterator++){
            if (bccAddresses[iterator].toString() != bccsArray[iterator]){
                same = false;
            }
        }
        assertTrue(same);
    }
    
    /**
     * 11.  send email with one recipient, one attachment, checks if the attached attachment name is the same as that of the file we attached
     * @throws MailException if the recipient email address is invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientOneAttachment() throws MailException{
        File attachment = new File("bliss.png");
        attachments.add(attachment);
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "", "fffffffffffffffffffffffffffffffffffffffff", "", attachments, embeddedAttachments);
        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
        if (attachments != null){
            attachments.stream().map((singleAttachment) -> {
            assertSame(singleAttachment.getName(), "bliss.png");
            return attachment;
            });
        }
    }
    
    /**
     * 12.  send email with one recipient, multiple attachments, checks if the attachment names are the same as those of the files we attached
     * @throws MailException if the recipient's email address was invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientMultipleAttachments() throws MailException{
        String[] fileNames = {"bliss.png", "blue.png"};
        File attachment1 = new File(fileNames[0]);
        File attachment2 = new File(fileNames[1]);
        attachments.add(attachment1);
        attachments.add(attachment2);
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "", "gggggggggggggggggggggggggggggggggggggggg", "", attachments, embeddedAttachments);
        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
        if (attachments != null){
            boolean same = true;
            for (int iterator = 0; iterator < fileNames.length; iterator++){
                if (attachments.get(iterator).getName() != fileNames[iterator]){
                    same = false;
                }
            }
            assertTrue(same);
        }
    }
    
    /**
     * 13. send email with one recipient, one embedded attachment, checks if the embedded attachment's name is the same as the one which we attached
     * @throws MailException if the recipient's email address is invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientOneEmbeddedAttachment() throws MailException{
        File embeddedAttachment = new File("blue.png");
        embeddedAttachments.add(embeddedAttachment);
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "", "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh", "", attachments, embeddedAttachments);
        assertEquals(embeddedAttachments.get(0).getName(),email.attachments().get(0).getName());
    }

    /**
     * 14. send email with one recipient, multiple embedded attachments, checks if the names of the embedded attachments are the same as the ones that we attached
     * @throws MailException if the recipient's email address is invalid
     */
    @Test(timeout = 10000)
    public void testOneRecipientMultipleEmbeddedAttachment() throws MailException{
        File embeddedAttachment1 = new File("blue.png");
        File embeddedAttachment2 = new File("bliss.png");
        embeddedAttachments.add(embeddedAttachment1);
        embeddedAttachments.add(embeddedAttachment2);
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "", "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii", "", attachments, embeddedAttachments);
        boolean same = false;
        if (embeddedAttachments.get(0).getName().equals(email.attachments().get(0).getName()) && embeddedAttachments.get(1).getName().equals(email.attachments().get(1).getName())){
            same = true;
        }
        assertTrue(same);
    }
    
    
    /**
     * 15. broken mailconfigbean, incorrect email address, it should throw a mailexception
     * @throws MailException when it attempts to open the sending session with the incorrect email
     */
    @Test(expected = MailException.class, timeout = 10000)
    public void testBadMailConfigBadEmailOnSend() throws MailException{
        MailConfigBean brokenBean = new MailConfigBean("smtp.gmail.com", "bg0test@gmail.com", "Dawson123");
        MainApplication testApplication = new MainApplication(brokenBean);
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = testApplication.sendEmail(to, cc, bcc, "", "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk", "", attachments, embeddedAttachments);
    }
    
    /**
     * 16. broken mailconfigbean, incorrect password, it should throw a mailexception
     * @throws MailException when it attempts to open the sending session with the incorrect password
     */
    @Test(expected = MailException.class, timeout = 10000)
    public void testBadMailConfigWrongPassOnSend() throws MailException{
        MailConfigBean brokenBean = new MailConfigBean("smtp.gmail.com", "bg01test@gmail.com", "Dawson1234");
        to.add(receivingConfigBean.getUserEmailAddress());
        MainApplication testApplication = new MainApplication(brokenBean);
        Email email = testApplication.sendEmail(to, cc, bcc, "", "llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll", "", attachments, embeddedAttachments);
    }
    
    
    /**
     * 17. testing if someone entered null as an email address, should throw a nullexception in the email-verifying function
     * @throws MailException if the entered email address is invalid/null
     */
    @Test(expected = MailException.class, timeout = 10000)
    public void testNullEmailAddressSend() throws MailException{
        to.add(null);
        Email email = sending.sendEmail(to, cc, bcc, "", "mmmmmmmmmmmmmmmmmmmmmmmmmmmmm", "", attachments, embeddedAttachments);
    }
    
    /**
     * 18. attempts to send to an invalid email address.  email verifying function should throw a mailexception here
     * @throws MailException if it has an invalid email address
     */
    @Test(expected = MailException.class, timeout = 10000)
    public void testInvalidEmailAddressSend() throws MailException{
        to.add("this is not a real email address");
        Email email = sending.sendEmail(to, cc, bcc, "", "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", "", attachments, embeddedAttachments);
    }
    
    /**
     * 19. check if receiving emails properly returns them through the receive function
     * just checks that the receivedemail array isn't null
     * @throws MailException if any of the entered email addresses are invalid
     */
    @Test(timeout = 30000)
    public void testRetrievingReturningArrayofReceivedEmails() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        sending.sendEmail(to, cc, bcc, "words", "more words", "", attachments, embeddedAttachments);
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
        }
        ReceivedEmail[] received = receiving.receiveEmail();
        assertNotNull(received);
    }
    
    /**
     * 20. checks whether it can receive 5 emails and properly report that number
     * checks the size of the receivedemail array after sending 5 emails
     * @throws MailException if any of the entered email addresses are invalid
     */
    @Test(timeout = 60000)
    public void testReceivingEmails() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        
        //receive emails here to prevent this test from accidentally reading emails that were already in the inbox
        receiving.receiveEmail();
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
            
        }
        //simple loop to send email 5 times
        for (int iterator = 0; iterator < 5; iterator++){
            Email email = sending.sendEmail(to, cc, bcc, "sagsdf", "text in text email", "", attachments, embeddedAttachments);
        }
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
            
        }
        
        //to make sure that 5 emails was the number of emails received
        ReceivedEmail[] received = receiving.receiveEmail();
        assertEquals(5, received.length);
    }
    
    /**
     * 21. test if can retrieve more emails after retrieving emails the first time (without sending more)
     * sends emails, retrieves them, then tries to retrieve again
     * @throws MailException if any of the entered email addresses are invalid
     */
    @Test(timeout = 30000)
    public void testDoubleReceive() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        sending.sendEmail(to, cc, bcc, "ahsgdfahd", "askjdhfbgkajshdbfgjshbd", "", attachments, embeddedAttachments);
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
        }
        receiving.receiveEmail();
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
        }
        //tries to receive again, without having any new emails sent since
        ReceivedEmail[] received = receiving.receiveEmail();
        assertEquals(0, received.length);
    }
    
    /**
     * 22. check fields from received emails are the same, checking if theyre .equals to each other
     * @throws MailException if any of the email addresses entered were invalid
     */
    @Test(timeout = 30000)
    public void testEmailFieldsSame() throws MailException{
        to.add(receivingConfigBean.getUserEmailAddress());
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
        }
        Email email = sending.sendEmail(to, cc, bcc, "thisisthesubjecta", "andtexthere", "", attachments, embeddedAttachments);
        
        ReceivedEmail[] receivedEmails = receiving.receiveEmail();
        List<EmailMessage> messagesReceived = receivedEmails[receivedEmails.length - 1].messages();
        boolean same = false;
        
        String receivedMessageWithNewline = messagesReceived.get(messagesReceived.size() - 1).getContent();
        
        //this method kept giving me the same exact string, but with a newline added to the end
        //this is to strip away that newline so that we are only comparing the content of the strings
        String receivedMessageWithoutNewline = receivedMessageWithNewline.substring(0, receivedMessageWithNewline.length() - 2);
        
        //simply checking that both are equal to the other
        if (receivedMessageWithoutNewline.equals(email.messages().get(0).getContent()) && receivedEmails[receivedEmails.length - 1].subject().equals(email.subject())){
            same = true;
        }
        assertTrue(same);
    }
    
    /**
     * 23. attempts to receive emails using an invalid email address in the mailconfigbean
     * @throws MailException if any of the entered email addresses were invalid
     */
    @Test(expected = MailException.class, timeout = 30000)
    public void testBadMailConfigBadEmailOnReceive() throws MailException{
        MailConfigBean brokenBean = new MailConfigBean("imap.gmail.com", "bgtest@gmail.com", "Dawson123");
        MainApplication testApplication = new MainApplication(brokenBean);
        to.add(receivingConfigBean.getUserEmailAddress());
        Email email = sending.sendEmail(to, cc, bcc, "", "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk", "", attachments, embeddedAttachments);
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
        }
        ReceivedEmail[] receivedEmails = testApplication.receiveEmail();
    }
    
   /**
     * 24. attempts to receive emails using an invalid password in the mailconfigbean
     * @throws MailException if any of the entered email addresses were invalid
     */
    @Test(expected = MailException.class, timeout = 30000)
    public void testBadMailConfigWrongPassOnReceive() throws MailException{
        MailConfigBean brokenBean = new MailConfigBean("smtp.gmail.com", "bg01test@gmail.com", "Dawson1234");
        to.add(receivingConfigBean.getUserEmailAddress());
        MainApplication testApplication = new MainApplication(brokenBean);
        Email email = sending.sendEmail(to, cc, bcc, "", "llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll", "", attachments, embeddedAttachments);
        try{
            Thread.sleep(3000);
        }
        catch (InterruptedException e){
        }
        ReceivedEmail[] receivedEmails = testApplication.receiveEmail();
    }
    
    
    @After
    /**
     * causes a temporary sleep between methods, which reduces the chance of having multiple sessions open at the same time, and to reduce chance of tests
     * reading emails created for other tests
     */
    public void runsAfter(){
       try{
           Thread.sleep(3000);
       }
       catch (InterruptedException e){
           
       }
    }
    
}
