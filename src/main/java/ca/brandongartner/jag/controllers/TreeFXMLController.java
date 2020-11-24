package ca.brandongartner.jag.controllers;

import ca.brandongartner.jag.beans.EmailFXBean;
import ca.brandongartner.jag.beans.FolderBean;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * controller for the tree component of the email program
 * @author Brandon Gartner
 */

public class TreeFXMLController {

    private final static Logger LOG = LoggerFactory.getLogger(TreeFXMLController.class);
    
    private DatabaseDAO DAO;
    private TableFXMLController tableController;
    
    @FXML
    private TreeView<FolderBean> treeComponent;
   
    @FXML // fx:id="treePane"
    private AnchorPane treePane; // Value injected by FXMLLoader

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    
    @FXML  // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    
    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    private void initialize(){
        
        assert treePane != null : "fx:id=\"treePane\" was not injected: check your FXML file 'TreeFXML.fxml'.";
        assert treeComponent != null : "fx:id=\"treeComponent\" was not injected: check your FXML file 'TreeFXML.fxml'.";

        
        //initializing the root
        FolderBean rootFolder = new FolderBean();
        
        
        rootFolder.setFolderName("Folders");
        treeComponent.setRoot(new TreeItem<FolderBean>(rootFolder));
        
        LOG.trace("Set the root tree component.");
        
        LOG.trace("Setting up the tree's factory.");
        treeComponent.setCellFactory((a) -> new TreeCell<FolderBean>() {
            @Override
            protected void updateItem(FolderBean bean, boolean empty){
                super.updateItem(bean, empty);
                if (bean != null) {
                    setText(bean.getFolderName());
                } 
            }
        });
        LOG.trace("Finished setting up the tree cell settings.");
    }
           
    @FXML
    private void dragDropped(DragEvent event){
        LOG.trace("Dropped onto the tree.");
        Dragboard dragboard = event.getDragboard();
        boolean succeeded = false;
        if (dragboard.hasString()){
            String emailId = dragboard.getString();
            LOG.debug("The email ID is: " + emailId);
            succeeded = true;
        }
        
        event.setDropCompleted(succeeded);
        event.consume();
    }
    
    
    @FXML
    private void draggedOnto(DragEvent event){
        LOG.trace("Dragged something onto a folder.");
        
        //only accept it if it is dragged from somethign else, and has a string
        if ((event.getGestureSource() != treeComponent) && (event.getDragboard().hasString())){
            LOG.debug(event.getGestureTarget().toString());
            LOG.debug("Will be accepting drag transfer.");
            //allow for copying/moving
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }
    
    /**
     * sets the tree's DAO to be the given one
     * @param DatabaseDAO the DAO to be set to this controller
     */
    public void setDAO(DatabaseDAO DAO){
        this.DAO = DAO;
        LOG.trace("Added the DAO to the tree component.");
    }
    
    /**
     * sets this tree's tablecontroller to be the given one
     * @param tableController sets that tablecontroller to be associated with this treecontroller
     */
    public void setTableController(TableFXMLController tableController){
        this.tableController = tableController;
    }
    
    /**
     * gets the folders from the DAO, displays them on the tree
     */
    public void displayTree() throws SQLException {
        treeComponent.getRoot().getChildren().clear();
        LOG.trace("Cleared the old tree.");
        LOG.debug("Attempted to begin displaying the folder tree.");
        ObservableList<FolderBean> folders = FXCollections.observableArrayList();
        LOG.debug("Created the folder list.");
        
        LOG.trace("Got all folderbeans from the table.");
        folders = DAO.getAllFolders();
        LOG.debug("Is the list of folders null? " + (folders == null));
        if (folders != null){
            for (FolderBean folder : folders){
                LOG.debug(folder.getFolderName());
                TreeItem<FolderBean> item = new TreeItem<>(folder);
                treeComponent.getRoot().getChildren().add(item);
            }
        }
        
        treeComponent.getRoot().setExpanded(true);
        LOG.trace("Began displaying tree components.");
        
        treeComponent.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateTable(newValue));
        LOG.trace("Added the event listener for replacing a value.");
        
    }
            
    /**
     * takes a folderBean and displays all of its emails onto the able.
     * @param folderBean the folderBean you'd like to display on the table
     */
    private void updateTable(TreeItem<FolderBean> folderBean){
        LOG.trace("Swapped to: " + folderBean.getValue().getFolderName());
        ObservableList<EmailFXBean> emails = folderBean.getValue().getEmails();
        tableController.displayTable(emails);
    }
    
    /**
     * opens a dialog box asking if the user really wants to delete X folder or not
     * credit to: https://code.makery.ch/blog/javafx-dialogs-official/
     * @return 
     */
    private boolean getConfirmationOnDelete(){
        LOG.trace("Building the confirmation dialog.");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm delete.");
        String folderName = treeComponent.getSelectionModel().getSelectedItem().getValue().getFolderName();
        alert.setHeaderText("Delete " + folderName + "?");
        alert.setContentText("Are you really sure you'd like to delete " + folderName + "?");
        
        LOG.trace("Waiting for the user's input.");
        Optional<ButtonType> clicked = alert.showAndWait();
        if (clicked.get() == ButtonType.OK){
            return true;
        }
        return false;
    }
    
    /**
     * gets a string from the user, which is to be the new name of a folder.
     * credit to: https://code.makery.ch/blog/javafx-dialogs-official/
     * @return the string the user chose
     */
    private String getNewFolderName(){
        LOG.trace("Creating the dialog to get user input.");
        TextInputDialog dialog = new TextInputDialog("New folder name");
        dialog.setTitle("New folder");
        dialog.setHeaderText("What would you like to name the new folder?");
        dialog.setContentText("Please enter the name of the new folder.");
        
        LOG.trace("Waiting for user input.");
        Optional<String> name = dialog.showAndWait();
        if (name.isPresent()){
            return name.get();
        }
        return null;
    }
    
    /**
     * gets the name the user wants for a new folder, and then creates it, updates the database, and reloads the tree
     * @param event the event of the user clicking the 'create folder' button
     * @throws SQLException 
     */
    @FXML
    private void createNewFolder(MouseEvent event) throws SQLException {
        String newFolderName = getNewFolderName();
        LOG.trace("Got the name of the new folder.");
        //if the user didn't enter a name (closed window, for example), it is null, which this filters out
        if (newFolderName != null){
            DAO.createFolder(newFolderName);
            displayTree();
        }
    }
    
    /**
     * verifies with the user that they want to delete the selected folder
     * if they do, deletes the folder from the database.
     * @param event the event of you clicking the delete folder button
     * @throws SQLException
     */
    @FXML
    private void deleteSelectedFolder(MouseEvent event) throws SQLException {
        boolean deleteOk = getConfirmationOnDelete();
        if (deleteOk){
            LOG.trace("Delete accepted.");
            LOG.trace("Deleting folder and contained emails from database.");
            FolderBean toBeDeleted = treeComponent.getSelectionModel().getSelectedItem().getValue();
            DAO.deleteFolder(toBeDeleted.getFolderName());
            
            LOG.trace("Removing the now-deleted folder from the folder tree.");
            treeComponent.getRoot().getChildren().remove(toBeDeleted);
            
            LOG.trace("Reloading the folder tree.");
            displayTree();
        }
    }
}
