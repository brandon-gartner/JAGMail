package ca.brandongartner.jag.controllers;

import ca.brandongartner.jag.beans.EmailBean;
import ca.brandongartner.jag.beans.EmailFXBean;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
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
    public void initialize() throws SQLException {
        assert emailTable != null : "fx:id=\"emailTable\" was not injected: check your FXML file 'TableFXML.fxml'.";
        assert fromField != null : "fx:id=\"fromField\" was not injected: check your FXML file 'TableFXML.fxml'.";
        assert subjectField != null : "fx:id=\"subjectField\" was not injected: check your FXML file 'TableFXML.fxml'.";
        assert date != null : "fx:id=\"date\" was not injected: check your FXML file 'TableFXML.fxml'.";

        LOG.trace("Creating the cell factories for the table.");
        fromField.setCellValueFactory(data -> data.getValue().getFromProperty());
        subjectField.setCellValueFactory(data -> data.getValue().getSubjectProperty());
        date.setCellValueFactory(data -> data.getValue().getDateProperty());
        
        LOG.trace("Creating the context menu.");
        ContextMenu contextMenu = new ContextMenu();
        
        LOG.trace("Creating the reply button.");
        MenuItem menuItem1 = new MenuItem("Reply");
        menuItem1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                EmailFXBean eBean = emailTable.getSelectionModel().getSelectedItem();
                htmlController.replyEmail(eBean);
            }
        });
        
        LOG.trace("Creating the forward button.");
        MenuItem menuItem2 = new MenuItem("Forward");
        menuItem2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                EmailFXBean eBean = emailTable.getSelectionModel().getSelectedItem();
                htmlController.forwardEmail(eBean);
            }
        });
        
        LOG.trace("Creating the delete button.");
        MenuItem menuItem3 = new MenuItem("Delete");
        menuItem3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                EmailFXBean eBean = emailTable.getSelectionModel().getSelectedItem();
                deleteAnEmail(eBean);
            }
        });
        
        LOG.trace("Adding the buttons to the context menu, connecting context meny to table.");
        contextMenu.getItems().add(menuItem1);
        contextMenu.getItems().add(menuItem2);
        contextMenu.getItems().add(menuItem3);
        
        emailTable.setContextMenu(contextMenu);
        
        adjustColumnWidths();
        LOG.trace("Adjusted the width of the columns");
        
        emailTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> changeHTMLContents(newValue));
    }
    
    /**
     * sets the table controller's dao to the dao we input as a parameter
     * @param DAO DAO 
     */
    public void setDAO(DatabaseDAO DAO){
        this.DAO = DAO;
        LOG.trace("Added the DAO to the table controller.");
    }
    
    /**
     * the tableview, so other controllers can affect it
     * @return 
     */
    public TableView<EmailFXBean> getTableView(){
        LOG.trace("Getting the table view.");
        return emailTable;
    }
    
    /**
     * sets the table controller's reference to an htmlcontroller
     * @param htmlController the htmlController we want to store a reference to
     */
    public void setHTMLController(HTMLEditorFXMLController htmlController){
        this.htmlController = htmlController;
    }
    
    /**
     * gets emails to display on the table from the fake dao
     * @param emails an observable list of emailfxbeans which you display to the table
     */
    public void displayTable(ObservableList<EmailFXBean> emails){
        emailTable.setItems(emails);
        LOG.trace("Displayed the table.");
    }
    
    /**
     * displays the table, but with no emails on it
     */
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
        subjectField.setPrefWidth(width * .63);
        date.setPrefWidth(width * .15);
        LOG.trace("Adjusting column widths.");
    }
    
    
    public void setHtmlEditor(HTMLEditorFXMLController htmlController){
        this.htmlController = htmlController;
    }
    
    /**
     * copies relevant information to the dragboard, so that the location we drag to can read it and
     * act base on what's inside
     * @param event the event that is the user dragging an email
     */
    @FXML
    public void dragDetected(MouseEvent event){
        LOG.trace("Attempting to drag an email.");
        String selected = "" + emailTable.getSelectionModel().getSelectedItem().getEmailId();
        LOG.trace("Email " + selected + " dragged.");
        if (!selected.equals("")){
            Dragboard dragboard = emailTable.startDragAndDrop(TransferMode.ANY);
            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString(selected);
            LOG.trace("Dragboard content:" + selected);
            dragboard.setContent(clipboard);
            event.consume();
        }
    }
    
    /**
     * updates the contents of the htmlcontroller to fit those inside of the emailFXBean
     * @param bean the bean whose contents you want to set to the htmlcontroller
     */
    @FXML
    private void changeHTMLContents(EmailFXBean bean){
        LOG.trace("Updating html editor contents.");
        htmlController.modifyFields(bean);
    }
    
    /**
     * called when the user right-clicks and selects delete
     * deletes the given email from the database
     * @param emailBean the emailBean of the email you wish to delete
     */
    private void deleteAnEmail(EmailFXBean emailBean) {
        LOG.trace("Attempting to delete an email via menu.");
        try {
            DAO.deleteEmail(Integer.parseInt(emailBean.getEmailId()));
        }
        catch (SQLException e){
            
        }
    }
}