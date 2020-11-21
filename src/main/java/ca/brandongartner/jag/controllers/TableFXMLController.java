package ca.brandongartner.jag.controllers;

import ca.brandongartner.jag.beans.EmailFXBean;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * controller for the table portion of our email client
 * @author Brandon Gartner
 */

public class TableFXMLController {

    private final static Logger LOG = LoggerFactory.getLogger(TableFXMLController.class);
    
    private DatabaseDAO DAO;
    
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    
    @FXML
    private HTMLEditorFXMLController htmlController;
    
    @FXML
    private AnchorPane emailTablePane;

    @FXML // fx:id="emailTable"
    private TableView<EmailFXBean> emailTable;

    @FXML// fx:id="fromField"
    private TableColumn<EmailFXBean, String> fromField;

    @FXML// fx:id="subjectField"
    private TableColumn<EmailFXBean, String> subjectField;

    @FXML // fx:id="date"
    private TableColumn<EmailFXBean, String> date;
    
    

    @FXML // This method is called by the FXMLLoader when initialization is complete
    public void initialize(){
        assert emailTable != null : "fx:id=\"emailTable\" was not injected: check your FXML file 'TableFXML.fxml'.";
        assert fromField != null : "fx:id=\"fromField\" was not injected: check your FXML file 'TableFXML.fxml'.";
        assert subjectField != null : "fx:id=\"subjectField\" was not injected: check your FXML file 'TableFXML.fxml'.";
        assert date != null : "fx:id=\"date\" was not injected: check your FXML file 'TableFXML.fxml'.";

        fromField.setCellValueFactory(data -> data.getValue().getFromProperty());
        subjectField.setCellValueFactory(data -> data.getValue().getSubjectProperty());
        date.setCellValueFactory(data -> data.getValue().getDateProperty());
        LOG.trace("Set up the process to generate table cells.");
        
        adjustColumnWidths();
        LOG.trace("Adjusted the width of the columns");
        
        emailTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changeHTMLContents(newValue));
    }
    
    /**
     * sets the table controller's fakedao to the fakedao we input as a parameter
     * @param DAO DAO 
     */

    public void setDAO(DatabaseDAO DAO){
        this.DAO = DAO;
        LOG.trace("Added the DAO to the table controller.");
    }
    
    public TableView<EmailFXBean> getTableView(){
        return emailTable;
    }
    
    /**
     * gets emails to display on the table from the fake dao
     */
    public void displayTable(ObservableList<EmailFXBean> emails){
        emailTable.setItems(emails);
        LOG.trace("Displayed the table.");
    }
    
    public void displayTable(){
        ObservableList<EmailFXBean> emails = FXCollections.observableArrayList();
        emailTable.setItems(emails);
        LOG.trace("Displayed nothing on the table.");
    }
    
    /**
     * adjusts the columns of the table so that they're spaced out well
     */

    private void adjustColumnWidths(){
        double width = emailTable.getPrefWidth();
        fromField.setPrefWidth(width * .20);
        subjectField.setPrefWidth(width * .65);
        date.setPrefWidth(width * .15);
    }
    
    /**
     * prints out the details of an emailfxbean
     * @param bean the bean whose details we print
     */

    private void printEmailDetails(EmailFXBean bean){
        System.out.println("Details for: " + bean);
    }
    
    public void setHtmlEditor(HTMLEditorFXMLController htmlController){
        this.htmlController = htmlController;
    }
    
    private void dragDetected(MouseEvent event){
        String selected = "" + emailTable.getSelectionModel().getSelectedItem().getEmailId();
        LOG.trace("Email " + selected + " dragged.");
        if (!selected.equals("")){
            Dragboard dragboard = emailTable.startDragAndDrop(TransferMode.ANY);
            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString(selected);
            dragboard.setContent(clipboard);
            event.consume();
        }
    }
    
    @FXML
    private void changeHTMLContents(EmailFXBean bean){
        htmlController.modifyFields(bean);
    }
}