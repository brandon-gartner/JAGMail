package ca.brandongartner.jag.controllers;

/**
 * Sample Skeleton for 'HTMLEditorFXML.fxml' Controller Class
 */

import ca.brandongartner.jag.beans.EmailBean;
import ca.brandongartner.jag.beans.EmailFXBean;
import ca.brandongartner.jag.beans.FormBean;
import ca.brandongartner.jag.beans.HTMLEditorFXBean;
import ca.brandongartner.jag.beans.MailConfigFXMLBean;
import ca.brandongartner.jag.mail_business.SendReceiveEmail;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.ReceivedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * controller for the html component of the email program
 * @author Brandon Gartner
 */

public class HTMLEditorFXMLController {
    
    private final static Logger LOG = LoggerFactory.getLogger(HTMLEditorFXMLController.class);

    private FormBean formBean;
    
    private HTMLEditorFXBean htmlBean;
    
    private MailConfigFXMLBean configBean;
    
    private DatabaseDAO DAO;
    
    private TreeFXMLController referenceToTree;
    
    private RootFXMLController root;
    
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    
    @FXML //fx:id="htmlPane"
    private BorderPane htmlPane; // Value injected by FXMLLoader

    @FXML // fx:id="emailHTMLEditor"
    private HTMLEditor emailHTMLEditor; // Value injected by FXMLLoader

    @FXML // fx:id="toField"
    private TextField toField; // Value injected by FXMLLoader

    @FXML // fx:id="ccField"
    private TextField ccField; // Value injected by FXMLLoader

    @FXML // fx:id="bccField"
    private TextField bccField; // Value injected by FXMLLoader

    @FXML // fx:id="subjectField"
    private TextField subjectField; // Value injected by FXMLLoader

    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert emailHTMLEditor != null : "fx:id=\"emailHTMLEditor\" was not injected: check your FXML file 'HTMLEditorFXML.fxml'.";
        assert toField != null : "fx:id=\"toField\" was not injected: check your FXML file 'HTMLEditorFXML.fxml'.";
        assert ccField != null : "fx:id=\"ccField\" was not injected: check your FXML file 'HTMLEditorFXML.fxml'.";
        assert bccField != null : "fx:id=\"bccField\" was not injected: check your FXML file 'HTMLEditorFXML.fxml'.";
        assert subjectField != null : "fx:id=\"subjectField\" was not injected: check your FXML file 'HTMLEditorFXML.fxml'.";
        formBean = new FormBean();
        htmlBean = new HTMLEditorFXBean();
        
        adjustWidths();
        
        LOG.trace("Created bindings for the htmleditor.");
        Bindings.bindBidirectional(toField.textProperty(), formBean.getToFieldProperty());
        Bindings.bindBidirectional(ccField.textProperty(), formBean.getCCFieldProperty());
        Bindings.bindBidirectional(bccField.textProperty(), formBean.getBCCFieldProperty());
        Bindings.bindBidirectional(subjectField.textProperty(), formBean.getSubjectFieldProperty());

    }
    
    /**
     * adjusts the widths of the different elements in the htmleditor
     */
    private void adjustWidths(){
        LOG.trace("Adjusting the widths inside of the HTML Editor.");
        double width = htmlPane.getWidth();
        emailHTMLEditor.prefWidth(width);
        toField.prefWidth(width * .85);
        ccField.prefWidth(width * .85);
        bccField.prefWidth(width * .85);
        subjectField.prefWidth(width * .85);
        LOG.trace("Adjusted the widths inside of the HTML Editor.");
    }
    
    /**
     * set the mailconfigbean we wish to use for this controller
     * @param configBean the bean we wish to set
     */
    public void setConfigBean(MailConfigFXMLBean configBean){
        this.configBean = configBean;
    }
    
    /**
     * set the treefxmlcontroller we want to use to reference the tree
     * @param tree the treefxmlcontroller we wish to reference
     */
    public void setReferenceToTree(TreeFXMLController tree){
        this.referenceToTree = tree;
    }
    
    /**
     * set the rootcontroller we wish to have a reference to (for attachments)
     * @param root the rootcontroller we'll reference
     */
    public void setRootController(RootFXMLController root){
        this.root = root;
    }
    
    /**
     * generates and sends an email.  then, waits 5 seconds, and then receives emails from the server, and updates the table
     * @param event the event of clicking on the button
     * @throws SQLException 
     */
    @FXML
    private void handleSendReceive(MouseEvent event) throws SQLException {
        SendReceiveEmail sendReceiver = new SendReceiveEmail(configBean);
        LOG.trace("Detected an attempt to send an email.");
        if (formBean.getToField() != null && !formBean.getToField().equals("")){
            LOG.debug("The client is sending an email.");
            Email sentEmail = sendEmailFromFields(sendReceiver);
            EmailBean sentEmailBean = new EmailBean();
            sentEmailBean.setEmail(sentEmail);
            LOG.trace("Sent the email.");
            //save email into database in sent
            DAO.insertEmail(sentEmailBean, "Sent");
            clearFields();
        
            try{
                LOG.trace("Pausing to wait for new email, in case we sent to ourselves.");
                Thread.sleep(5000);
            } catch (InterruptedException e){
                //do nothing
            }
        } else {
            LOG.trace("To field was empty, so the user only wanted to receive emails.  The sending process has been skipped.");
        }
        
        //receive email
        ReceivedEmail[] received = sendReceiver.receiveEmail();
        LOG.trace("Received new emails from the database.");
        //save received email into database
        ArrayList<EmailBean> receivedEmailBeans = new ArrayList<EmailBean>();
        for (ReceivedEmail email : received){
            EmailBean newBean = new EmailBean(email);
            receivedEmailBeans.add(newBean);
        }
        
        LOG.trace("Finished converting all received emails to email beans.");
        
        for (EmailBean emailBean : receivedEmailBeans){
            DAO.insertEmail(emailBean, "Inbox");
        }
        LOG.trace("Finished inserting received emails into the database.");
        
        referenceToTree.displayTree();
        LOG.trace("Updated tree, and emails stored in memory.");
    }
    
    /**
     * takes a string.  if it is empty, returns an empty arraylist.  if it is full, returns a string arraylist, splitting the original string over commas
     * @param unsplitList the string we want to split into a list
     * @return the arraylist of strings
     */
    private ArrayList<String> createNonEmptyEmailListFromUnsplitString(String unsplitList){
        if (!unsplitList.equals("")){
            return new ArrayList<String>(Arrays.asList(unsplitList.split(",")));
        } else{
            return new ArrayList<String>();
        }
    }
    
    /**
     * sets this controller's DAO to the given one
     * @param DAO the DAO to set to
     */
    @FXML
    public void setDAO(DatabaseDAO DAO){
        this.DAO = DAO;
    }
    
    /**
     * updates the fields on the htmlController to reflect an emailfxbean.  also sets it up to appropriately reply to an email
     * @param bean the bean which we should update the fields to reflect.
     */
    @FXML
    public void modifyFields(EmailFXBean bean){
        clearFields();
        
        //necessary so that multiple file arrays don't get mixed together.
        root.clearFiles();
        formBean.setToField(bean.getFrom());
        formBean.setSubjectField(bean.getSubject());
        this.emailHTMLEditor.setHtmlText(bean.getHtmlField().toString());
    }
    
    /**
     * empties all of the htmleditor's fields
     */
    private void clearFields(){
        formBean.setToField("");
        formBean.setSubjectField("");
        formBean.setCCField("");
        formBean.setBCCField("");
        this.emailHTMLEditor.setHtmlText("");
        root.clearFiles();
    }
    
    /**
     * saves a draft email into the drafts
     * @param event the event of clicking on the associated button
     * @throws SQLException 
     */
    @FXML
    private void handleSave(MouseEvent event) throws SQLException {
        LOG.trace("User attempted to save.");
        Email draft = generateDraftFromFields();
        
        EmailBean bean = new EmailBean();
        bean.setEmail(draft);
        LOG.trace("Attempting to save draft to database.");
        DAO.insertEmail(bean, "Drafts");
        
        LOG.trace("Draft saved!");
        
        referenceToTree.displayTree();
        LOG.trace("Re-displayed the tree.");
    }
    
    /**
     * gets email data from each field on the htmleditor form, and compiles them into an email object, which is then returned
     * @return the constructed email
     */
    private Email generateDraftFromFields(){
        String from = configBean.getUserEmailAddress();
        String toList = formBean.getToField();
        String ccList = formBean.getCCField();
        String bccList = formBean.getBCCField();
        String subject = formBean.getSubjectField();
        String message = "";
        
        //we can seemingly only get htmlmessages
        String containedHtmlMessage = emailHTMLEditor.getHtmlText();
        LOG.trace("Got all fields from the relevant beans.");
        
        ArrayList<String> tos = createNonEmptyEmailListFromUnsplitString(toList);
        ArrayList<String> ccs = createNonEmptyEmailListFromUnsplitString(ccList);
        ArrayList<String> bccs = createNonEmptyEmailListFromUnsplitString(bccList);
        ArrayList<File> attachments = new ArrayList<File>();
        ArrayList<File> embeddedAttachments = new ArrayList<File>();
        LOG.trace("Created and removed irrelevant entries from the various lists for sending.");
        
        Email email = new Email();
        
        email.from(from);
        email.subject(subject);
        email.textMessage(message);
        email.htmlMessage(containedHtmlMessage);
        
        LOG.trace("Applying tos to the draft.");
        for (String address : tos){
            email.to(address);
        }
        LOG.trace("Applying ccs to the draft.");
        for (String address : ccs){
            email.cc(address);
        }
        LOG.trace("Applying bccs to the draft.");
        for (String address : bccs){
            email.bcc(address);
        }
        LOG.trace("Applying attachments to the draft.");
        for (File file : attachments){
            email.attachment(EmailAttachment.with().content(file));
        }
        LOG.trace("Applying embedded attachments to the draft.");
        for (File file : embeddedAttachments){
            email.embeddedAttachment(EmailAttachment.with().content(file));
            
            //appends the appropriate html code to append the file to the email
            String htmlMessage = "<img width=100 height=100 id=\"1\" src=\"cid:"+ file.getPath() + "\"/>";
            email.htmlMessage(htmlMessage);
        }
        
        return email;
    }
    
    /**
     * takes data from all of the htmleditor's fields, and compiles them into an email, which is then sent
     * @param sendReceiver the sendReceiver tha we will send the email using
     * @return the email which we have sent
     */
    private Email sendEmailFromFields(SendReceiveEmail sendReceiver){
        String toList = formBean.getToField();
        if (toList == null){
            
        }
        String ccList = formBean.getCCField();
        String bccList = formBean.getBCCField();
        String subject = formBean.getSubjectField();
        String message = "";
        
        //we can seemingly only get htmlmessages
        String htmlMessage = emailHTMLEditor.getHtmlText();
        LOG.trace("Got all fields from the relevant beans.");
        
        ArrayList<String> tos = createNonEmptyEmailListFromUnsplitString(toList);
        ArrayList<String> ccs = createNonEmptyEmailListFromUnsplitString(ccList);
        ArrayList<String> bccs = createNonEmptyEmailListFromUnsplitString(bccList);
        ArrayList<File> attachments = root.getCurrentFiles();
        ArrayList<File> embeddedAttachments = new ArrayList<File>();
        LOG.trace("Created and removed irrelevant entries from the various lists for sending.");
        
        //successfully combines the fields together, and then sends them
        Email sentEmail = sendReceiver.sendEmail(tos, ccs, bccs, subject, message, htmlMessage, attachments, embeddedAttachments);
        LOG.trace("Successfully sent the email!");
        return sentEmail;
    }
    
    /**
     * sets up the htmleditor to be replying to an email
     * @param emailBean the bean of the email you want to reply to
     */
    public void replyEmail(EmailFXBean emailBean){
        modifyFields(emailBean);
        
    }
    
    /**
     * sets up the htmleditor to be forwarding the given emailbean
     * @param emailBean the emailbean to be forwarded
     */
    public void forwardEmail(EmailFXBean emailBean){
        formBean.setToField("");
        formBean.setSubjectField("FW: " + emailBean.getSubject());
        this.emailHTMLEditor.setHtmlText("<hr> \n" + emailBean.getDate() + "<br>" + emailBean.getHtmlField() + "\n");
    }
            
}
