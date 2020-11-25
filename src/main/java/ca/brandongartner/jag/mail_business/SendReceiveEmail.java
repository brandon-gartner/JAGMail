/**
 * Here is your documentation on Jodd
 * https://jodd.org/email/index.html
 */
package ca.brandongartner.jag.mail_business;

import ca.brandongartner.jag.beans.MailConfigFXMLBean;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javax.activation.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Flags;

import jodd.mail.EmailFilter;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ImapServer;
import jodd.mail.MailServer;
import jodd.mail.RFC2822AddressParser;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.mail.MailException;

/**
 * This is demo code for phase 1.  It all seems to be working, and I'm happy with it.
 * Hopefully you are, too!
 * Originally written by Ken Fogel.  Refactored by Brandon Gartner
 *
 * (not sure whether to replace this stuff)
 * @author Ken
 * @version 2.1
 *
 */
public class SendReceiveEmail {

    // Real programmers use logging
    private final static Logger LOG = LoggerFactory.getLogger(SendReceiveEmail.class);

    private final MailConfigFXMLBean UserBean;
    
    private final int secondsToSleep = 3;

    public SendReceiveEmail(MailConfigFXMLBean bean){
        UserBean = bean;
    }
    
    /**
     * takes a lot of inputs, uses them to generate an email, sends the email, and then returns an appropriate email object
     * @param emailToList list of strings which the email will be directly sent to
     * @param emailCCList list of strings which are the email addresses to be CCed
     * @param emailBCCList list of strings which are the email addresses to be BCCed
     * @param subject the subject line of your email
     * @param textMessage the actual text body of your email
     * @param HTMLMessage HTML text which the program attempts to display as part of your email
     * @param attachments an arraylist of files which we wish to attach as attachments
     * @param embeddedAttachments an arraylist of embedded attachments.  for now, probably only works with images.
     * @return returns an object of type email, with fields completely matching the email we sent
     * @throws MailException if any of the email addresses fail to be validated
     */
    public Email sendEmail(ArrayList<String> emailToList, ArrayList<String> emailCCList, ArrayList<String> emailBCCList, String subject, String textMessage, String HTMLMessage, ArrayList<File> attachments, ArrayList<File> embeddedAttachments) throws MailException{
        //verifies the email address of the MailConfigBean, as well as all that we were supposed to send to as a normal email, cc, or bcc
        try {
            checkEmail(UserBean.getUserEmailAddress());
            validateEmailList(emailToList);
            validateEmailList(emailCCList);
            validateEmailList(emailBCCList);
        } catch (MailException e){
            errorAlert("InvalidEmail");
        }
        
        //generate an smtp server
        SmtpServer smtpServer = generateSmtpServer();
        
        //generating the email using all given fields, then opening the session and sending the email
        Email email = generateEmail(emailToList, emailCCList, emailBCCList, subject, textMessage, HTMLMessage, attachments, embeddedAttachments);
        openSessionSendEmail(email, smtpServer);
        
        return email;
    }
    
    /**
     * takes an email address, and validates it, returning if it is validated, throwing an exception if not
     * @param address to verify.  if the address is null or invalid, throws an exception
     * @throws MailException if any of the email addresses are invalid, we'd rather prevent the email from being sent and notifying the user then sending to invalid 
     */
    private void checkEmail(String address) throws MailException {
        if (address == null){
            throw new MailException("An email address we attempted to verify was null.");
        }
        else if (RFC2822AddressParser.STRICT.parseToEmailAddress(address) != null) {
            LOG.info("Email " + address + " verified!");
            return;
        }
        else {
            throw new MailException("An email address we attempted to verify was not valid.");
        }
    }
    
    
     /**
      * loops through an arrayList of email address strings, throws an exception using
      * checkEmail if any of them are definitely false
      * @param a list of entered email addresses
      * @return the potentially real email addresses on the parameter arraylist
      */
    private void validateEmailList(ArrayList<String> listOfAddresses) throws MailException{
        for (String address : listOfAddresses){
            checkEmail(address);
            }
        }
    
    /**
     * uses various data from our mailapplication and mailconfigbean
     * to generate an smtpserver, which it then returns, for sending emails
     * @return an smtp server
     */
    private SmtpServer generateSmtpServer(){
        
        SmtpServer smtpServer = MailServer.create()
                    .ssl(true)
                    .host(UserBean.getSmtpURL())
                    .auth(UserBean.getUserEmailAddress(), UserBean.getPassword())
                    //.debugMode(true)
                    .buildSmtpMailServer();
        LOG.info("SMTP server generated!");
        return smtpServer;
    }
    
    /**
     * uses various data from our mailapplication and mailconfigbean
     * to generate an imapserver, which it then returns, for receiving emails
     * @return an imap server
     */
    private ImapServer generateImapServer(){
        LOG.debug("Email Address: " + UserBean.getUserEmailAddress());
        LOG.debug("Password: " + UserBean.getPassword());
         ImapServer imapServer = MailServer.create()
                    .host(UserBean.getImapURL())
                    .ssl(true)
                    .auth(UserBean.getUserEmailAddress(), UserBean.getPassword())
                    //.debugMode(true)
                    .buildImapMailServer();
         LOG.info("IMAP server generated!");
         return imapServer;
    }
    
    /**
     * uses an smtpserver and an email to send an email
     * @param email which it sends
     * @param smtpServer which it uses to send the email
     * @return returns a boolean which is true if it succeeded
     * @throws MailException which occurs if it fails to send the email
     */
    private boolean openSessionSendEmail(Email email, SmtpServer smtpServer) throws MailException{
        // Like a file we open the session, send the message and close the
            // session
            try ( // A session is the object responsible for communicating with the server
                SendMailSession session = smtpServer.createSession()) {
                // Like a file we open the session, send the message and close the
                // session
                session.open();
                session.sendMail(email);
                LOG.info("Email sent.");
                return true;
            }
    }
    
    /**
     * takes various parameters of your email and applies them all to an email object.  the email object is then returned.
     * @param emailTos an arraylist of string email addresses which will be added to the to field of your email
     * @param emailCCs an arraylist of string email addresses which become ccs on your email
     * @param emailBCCs an arraylist of string email addresses which become bccs on your email
     * @param subject the subject line of your email
     * @param messageText the message to be sent in the email.  is overwritten if you send an html message
     * @param HTMLMessage the html message that should be sent on the email.  adding one of these will overwrite the messageText
     * @param attachments an arraylist of attachments to attach.
     * @param embeddedAttachments an arraylist of attachments to be embedded.  currently only likely supports images.
     * @return returns an email which was generated from its parameters
     */
    private Email generateEmail(ArrayList<String> emailTos, ArrayList<String> emailCCs, ArrayList<String> emailBCCs, String subject, String messageText, String HTMLMessage, ArrayList<File> attachments, ArrayList<File> embeddedAttachments){
        Email email = new Email();
        email.from(UserBean.getUserEmailAddress());
        email = addToField(emailTos, email);
        email = addCCField(emailCCs, email);
        email = addBCCField(emailBCCs, email);
        email.subject(subject);
        email.textMessage(messageText);
        //if you have both an htmlmessage and a messagetext, the messagetext is overwritten when they open the email.  this simply ensures that messagetext isn't overwritten by an empty
        //htmlmessage
        if (verifyHTML(HTMLMessage)){
            email.htmlMessage(HTMLMessage);
        }
        email = addAttachments(attachments, email);
        email = embedAttachments(embeddedAttachments, email);
        //stuff
        return email;
    }
    
    /**
     * takes an arraylist of email addresses and adds them to the 'to' field of the email
     * @param recipients an arraylist of string email addresses which we would like to send this email to
     * @param email the email which we would like to append this 'to' field onto
     * @return returns the email once this method has filled the 'to' field
     */
    private Email addToField(ArrayList<String> recipients, Email email){
        for (String recipient : recipients){
            email.to(recipient);
            LOG.info("Recipient: " + recipient + " added!");
        }
        return email;
    }
    
    /**
     * takes an arraylist of email addresses and adds them to the 'cc' field of the email
     * @param recipients an arraylist of string email addresses who we would like to 'cc' this email
     * @param email the email which we would like to append this 'cc' field onto
     * @return returns the email once this method has filled the 'cc' field
     */
    private Email addCCField(ArrayList<String> CCRecipients, Email email){
        for (String CCRecipient : CCRecipients){
            email.cc(CCRecipient);
            LOG.info("CC Recipient: " + CCRecipient + " added!");
        }
        return email;
    }
    
    /**
     * takes an arraylist of email addresses and adds them to the 'bcc' field of the email
     * @param recipients an arraylist of string email addresses who we would like to 'bcc' this email
     * @param email the email which we would like to append this 'bcc' field onto
     * @return returns the email once this method has filled the 'bcc' field
     */
    private Email addBCCField(ArrayList<String> BCCRecipients, Email email){
        for (String BCCRecipient : BCCRecipients){
            email.bcc(BCCRecipient);
            LOG.info("BCC Recipient: " + BCCRecipient + " added!");
        }
        return email;
    }
    
    /**
     * takes an arraylist of files and adds them as attachments to this email
     * @param recipients an arraylist of files which we would like to become attachments
     * @param email the email which we would like to attach the file to
     * @return returns the email once this file has been attached
     */
    private Email addAttachments(ArrayList<File> attachments, Email email){
        for (File attachment : attachments){
            email.attachment(EmailAttachment.with().content(attachment));
            LOG.info("Attachment: " + attachment.getName() + " added!");
        }
        return email;
    }
    
    /**
     * takes an arraylist of files and adds them as embedded attachments to this email
     * @param recipients an arraylist of files which we would like to become embedded attachments
     * @param email the email which we would like to embed the file on
     * @return returns the email once this file has been embedded
     */
    private Email embedAttachments(ArrayList<File> embeddedAttachments, Email email){
        for (File embeddedAttachment : embeddedAttachments){
            email.embeddedAttachment(EmailAttachment.with().content(embeddedAttachment));
            
            //appends the appropriate html code to append the file to the email
            String htmlMessage = "<img width=100 height=100 id=\"1\" src=\"cid:"+ embeddedAttachment.getPath() + "\"/>";
            email.htmlMessage(htmlMessage);
            LOG.info("Embedded attachment: " + embeddedAttachment.getName() + " added!");
        }
        return email;
    }
    
    /**
     * simply verifies that a string (htmlmessage) isn't empty, so it doesn't overwrite a real message in the email
     * @param html to check to see if it is empty
     * @return true if the html isn't empty, returns false if it is empty
     */
    private boolean verifyHTML(String html){
        if (html.equals("")){
            return false;
        }
        else {
            return true;
        }
    }
    
    /**
     * gets recent emails, sets them as seen, and returns their data to us
     * @return returns an array of all of the receivedEmail that it received
     * @throws MailException if it can't properly connect to 
     */
    public ReceivedEmail[] receiveEmail() throws MailException{
        checkEmail(UserBean.getUserEmailAddress());
        //generate the server
        ImapServer imapServer = generateImapServer();
        ReceiveMailSession session = imapServer.createSession();
        session.open();
        ReceivedEmail[] emails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
            if (emails != null) {
                LOG.info("\n >>>> ReceivedEmail count = " + emails.length);
                for (ReceivedEmail email : emails) {
                        logReceivedEmail(email);
                }
            }
       return emails;            
    }
        
    
    /**
     * takes a received email, and logs the important stuff about it
     * @param email takes a received email, of which it logs all important info, messages, and attachment details
     */
    private void logReceivedEmail(ReceivedEmail email){
        LOG.info("\n\n===[" + email.messageNumber() + "]===");

        // common info
        LOG.info("FROM:" + email.from());
        // Handling array in email object
        
        LOG.info("TO:" + Arrays.toString(email.to()));
        LOG.info("CC:" + Arrays.toString(email.cc()));
        LOG.info("SUBJECT:" + email.subject());
        LOG.info("PRIORITY:" + email.priority());
        LOG.info("SENT DATE:" + email.sentDate());
        LOG.info("RECEIVED DATE: " + email.receivedDate());

        // process messages, logs important message details
        List<EmailMessage> messages = email.messages();
        messages.stream().map((msg) -> {
            LOG.info("------");
            return msg;
        }).map((msg) -> {
            LOG.info(msg.getEncoding());
            return msg;
        }).map((msg) -> {
            LOG.info(msg.getMimeType());
            return msg;
        }).forEachOrdered((msg) -> {
            LOG.info(msg.getContent());
        });

        // process attachments, log important info about the attachments
        List<EmailAttachment<? extends DataSource>> attachments = email.attachments();
        if (attachments != null) {
            LOG.info("+++++");
            attachments.stream().map((attachment) -> {
                LOG.info("name: " + attachment.getName());
            return attachment;
            }).map((attachment) -> {
                LOG.info("cid: " + attachment.getContentId());
            return attachment;
            }).map((attachment) -> {
                LOG.info("size: " + attachment.getSize());
            return attachment;
            });
        }
    }
    
     /**
     * Error message popup dialog
     *
     * @param the message we want to appear in the box
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        LOG.trace("Creating alert dialog.");
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle").getString("Error"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle").getString("Error"));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle").getString(msg));
        dialog.show();
    }
    
}
