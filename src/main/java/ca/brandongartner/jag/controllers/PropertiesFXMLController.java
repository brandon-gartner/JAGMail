package ca.brandongartner.jag.controllers;

/**
 * Sample Skeleton for 'PropertiesFXML.fxml' Controller Class
 */
import ca.brandongartner.jag.beans.MailConfigFXMLBean;
import ca.brandongartner.jag.gui_business.PropertyManagement;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * controller for the properties menu that appears when the program is loaded while there is no properties file to be loaded.
 * @author Brandon Gartner
 */
public class PropertiesFXMLController {

    private final static Logger LOG = LoggerFactory.getLogger(PropertiesFXMLController.class);
    
    private MailConfigFXMLBean configBean;
    
    private PropertyManagement pm;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="emailPassword"
    private PasswordField emailPassword; // Value injected by FXMLLoader

    @FXML // fx:id="mySQLPassword"
    private PasswordField mySQLPassword; // Value injected by FXMLLoader

    @FXML // fx:id="username"
    private TextField username; // Value injected by FXMLLoader

    @FXML // fx:id="emailAddress"
    private TextField emailAddress; // Value injected by FXMLLoader

    @FXML // fx:id="imapUrl"
    private TextField imapUrl; // Value injected by FXMLLoader

    @FXML // fx:id="smtpUrl"
    private TextField smtpUrl; // Value injected by FXMLLoader

    @FXML // fx:id="imapPort"
    private TextField imapPort; // Value injected by FXMLLoader

    @FXML // fx:id="smtpPort"
    private TextField smtpPort; // Value injected by FXMLLoader

    @FXML // fx:id="mySQLUrl"
    private TextField mySQLUrl; // Value injected by FXMLLoader

    @FXML // fx:id="mySQLPort"
    private TextField mySQLPort; // Value injected by FXMLLoader

    @FXML // fx:id="mySQLDatabase"
    private TextField mySQLDatabase; // Value injected by FXMLLoader

    @FXML // fx:id="mySQLUser"
    private TextField mySQLUser; // Value injected by FXMLLoader

    @FXML // fx:id="saveButton"
    private Button saveButton; // Value injected by FXMLLoader

    @FXML // fx:id="cancelButton"
    private Button cancelButton; // Value injected by FXMLLoader

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() throws IOException {
        assert emailPassword != null : "fx:id=\"emailPassword\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert mySQLPassword != null : "fx:id=\"mySQLPassword\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert username != null : "fx:id=\"username\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert emailAddress != null : "fx:id=\"emailAddress\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert imapUrl != null : "fx:id=\"imapUrl\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert smtpUrl != null : "fx:id=\"smtpUrl\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert imapPort != null : "fx:id=\"imapPort\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert smtpPort != null : "fx:id=\"smtpPort\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert mySQLUrl != null : "fx:id=\"mySQLUrl\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert mySQLPort != null : "fx:id=\"mySQLPort\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert mySQLDatabase != null : "fx:id=\"mySQLDatabase\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert mySQLUser != null : "fx:id=\"mySQLUser\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert saveButton != null : "fx:id=\"saveButton\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";
        assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'PropertiesFXML.fxml'.";

        
        configBean = new MailConfigFXMLBean();
        pm = new PropertyManagement();
        pm.loadTextProperties(configBean, "", "config");

        Bindings.bindBidirectional(username.textProperty(), configBean.getUserNameProperty());
        Bindings.bindBidirectional(emailAddress.textProperty(), configBean.getEmailAddressProperty());
        Bindings.bindBidirectional(emailPassword.textProperty(), configBean.getPasswordProperty());
        Bindings.bindBidirectional(imapUrl.textProperty(), configBean.getImapURLProperty());
        Bindings.bindBidirectional(smtpUrl.textProperty(), configBean.getSmtpURLProperty());
        Bindings.bindBidirectional(imapPort.textProperty(), configBean.getImapPortProperty());
        Bindings.bindBidirectional(smtpPort.textProperty(), configBean.getSmtpPortProperty());
        Bindings.bindBidirectional(mySQLUrl.textProperty(), configBean.getSqlURLProperty());
        Bindings.bindBidirectional(mySQLPort.textProperty(), configBean.getSqlPortProperty());
        Bindings.bindBidirectional(mySQLDatabase.textProperty(), configBean.getDatabaseNameProperty());
        Bindings.bindBidirectional(mySQLUser.textProperty(), configBean.getSqlUserNameProperty());
        Bindings.bindBidirectional(mySQLPassword.textProperty(), configBean.getSqlPasswordProperty());
        LOG.trace("Created the bindings for the property layout.");
        

    }
    
    /**
     * cancels the action of setting/changing properties, closing the window
     * @param event the event of you clicking on the button.
     */
    @FXML 
    public void cancelAction(MouseEvent event){
        LOG.trace("Closing the properties window.");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * saves the properties you have written to the properties file on the disk
     * @param event the event of you clicking save
     * @throws IOException if it can't save the properties to the correct location
     */
    @FXML
    public void saveAction(MouseEvent event) throws IOException{
        pm.writeTextProperties("", "config", configBean);
        LOG.trace("Saved the properties.");
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
