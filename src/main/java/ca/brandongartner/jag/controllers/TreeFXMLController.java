package ca.brandongartner.jag.controllers;

import ca.brandongartner.jag.beans.EmailFXBean;
import ca.brandongartner.jag.beans.FolderBean;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
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
        
        //setting up how the 
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
            //allow for copying/moving
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }
    
    /**
     * sets the tree's DAO to be the given one
     * @param fakeDAO the DAO
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
     * gets the folders from the fake dao, displays them on the tree
     */

    public void displayTree() throws SQLException {
        LOG.debug("Attempted to begin displaying the folder tree.");
        ObservableList<FolderBean> folders = DAO.getAllFolders();
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
            
    //TODO: ask ken what's up with this
    private void updateTable(TreeItem<FolderBean> folderBean){
        System.out.println(folderBean.getValue().getFolderName());
        ObservableList<EmailFXBean> emails = folderBean.getValue().getEmails();
        tableController.displayTable(emails);
    }
}
