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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.HTMLEditor;
import jodd.mail.Email;
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
        
        Bindings.bindBidirectional(toField.textProperty(), formBean.getToFieldProperty());
        Bindings.bindBidirectional(ccField.textProperty(), formBean.getCCFieldProperty());
        Bindings.bindBidirectional(bccField.textProperty(), formBean.getBCCFieldProperty());
        Bindings.bindBidirectional(subjectField.textProperty(), formBean.getSubjectFieldProperty());
        //Bindings.bindBidirectional(emailHTMLEditor., htmlBean.getHTMLProperty());

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
    
    @FXML
    private void draggedOnto(DragEvent event){
        LOG.trace("DraggedOnto triggered.");
        
        //only accept it if the source is something aside from this, or 
    }
    
    @FXML
    private void draggedDropped(DragEvent event){
        LOG.trace("DraggedDropped triggered.");
        Dragboard dragboard = event.getDragboard();
        boolean succeeded = false;
        if (dragboard.hasString()){
            //TODO: apply email text
            emailHTMLEditor.setHtmlText(dragboard.getString());
            succeeded = true;
        }
        //let the source know whether it worked or not
        
        event.setDropCompleted(succeeded);
        
        event.consume();
        
    }
    
    public void setConfigBean(MailConfigFXMLBean configBean){
        this.configBean = configBean;
    }
    
    
    
    @FXML
    private void handleSendReceive() throws SQLException {
        LOG.trace("User attempted to send an email.");
        String toList = formBean.getToField();
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
        ArrayList<File> attachments = new ArrayList<File>();
        ArrayList<File> embeddedAttachments = new ArrayList<File>();
        LOG.trace("Created and removed irrelevant entries from the various lists for sending.");
        
        SendReceiveEmail sendReceiver = new SendReceiveEmail(configBean);
        Email sentEmail = sendReceiver.sendEmail(tos, ccs, bccs, subject, message, htmlMessage, attachments, embeddedAttachments);
        EmailBean sentEmailBean = new EmailBean();
        sentEmailBean.setEmail(sentEmail);
        LOG.trace("Sent the email.");
        //save email into database in sent
        DAO.insertEmail(sentEmailBean, "Sent");
        
        //receive email
        //ReceivedEmail[] received = sendReceiver.receiveEmail();
        
        //save received email into database
        /*for (ReceivedEmail email : received){
            EmailBean emailBean = new EmailBean();
            emailBean.setEmail(email);
        }*/
    }
    
    private ArrayList<String> createNonEmptyEmailListFromUnsplitString(String unsplitList){
        if (!unsplitList.equals("")){
            return new ArrayList<String>(Arrays.asList(unsplitList.split(",")));
        } else{
            return new ArrayList<String>();
        }
    }
    
    @FXML
    public void setDAO(DatabaseDAO DAO){
        this.DAO = DAO;
    }
            
    @FXML //prints out the html text to the log
    public void displayEmailAsHTML(KeyEvent event){
        LOG.trace("Text in HTMLEditor: " + emailHTMLEditor.getHtmlText());
    }
    
    @FXML
    public void modifyFields(EmailFXBean bean){
        formBean.setToField(bean.getFrom());
        formBean.setSubjectField(bean.getSubject());
        //this.emailHTMLEditor.setHtmlText(bean.htmlText());
    }
            
}
