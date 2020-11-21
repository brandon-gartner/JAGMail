package ca.brandongartner.jag.controllers;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import javafx.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Sample Skeleton for 'RootFXML.fxml' Controller Class
 */


import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * controller for the root of the entire email program, that the other components are drawn onto
 * @author Brandon Gartner
 */

public class RootFXMLController {

    private final static Logger LOG = LoggerFactory.getLogger(RootFXMLController.class);
    
    @FXML // fx:id="backPane"
    private BorderPane backPane;
    
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="folderTreeSpace"
    private AnchorPane folderTreeSpace;

    @FXML // fx:id="emailTableSpace"
    private AnchorPane emailTableSpace;

    @FXML // fx:id="htmlEditorSpace"
    private AnchorPane htmlEditorSpace;

    @FXML // fx:id="addAttachmentButton"
    private MenuItem addAttachmentButton;

    @FXML // fx:id="saveAttachmentButton"
    private MenuItem saveAttachmentButton;
    
    private DatabaseDAO DAO;
    private TreeFXMLController treeController;
    private HTMLEditorFXMLController htmlController;
    private TableFXMLController tableController;
    private PropertiesFXMLController propertyController;
    
    
    public void setDAO(DatabaseDAO dao){
        this.DAO = dao;
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() throws SQLException {
        assert backPane != null : "fx:id=\"backPane\" was not injected: check your FXML file 'RootFXML.fxml'.";
        assert folderTreeSpace != null : "fx:id=\"folderTreeSpace\" was not injected: check your FXML file 'RootFXML.fxml'.";
        assert emailTableSpace != null : "fx:id=\"emailTableSpace\" was not injected: check your FXML file 'RootFXML.fxml'.";
        assert htmlEditorSpace != null : "fx:id=\"htmlEditorSpace\" was not injected: check your FXML file 'RootFXML.fxml'.";
        assert addAttachmentButton != null : "fx:id=\"addAttachmentButton\" was not injected: check your FXML file 'RootFXML.fxml'.";
        assert saveAttachmentButton != null : "fx:id=\"saveAttachmentButton\" was not injected: check your FXML file 'RootFXML.fxml'.";

        
        initUpperRightLayout();
        LOG.trace("Initialized the table layout.");
        initLowerRightLayout();
        LOG.trace("Initialized the HTMLEditor layout.");
        initLeftLayout();
        LOG.trace("Initialized the tree layout.");
        
        
        sendTableControllerToTree();
        LOG.trace("Added the table controller to the tree layout.");
        
        LOG.debug("is treeController null? " + ( treeController == null ));
        //try catch for sql exception later, here
        treeController.displayTree();
        LOG.trace("Displayed the tree.");
    }
    
    /**
     * transfers the rootfxmlcontroller's tablecontroller to the treeController
     */
    private void sendTableControllerToTree(){
        treeController.setTableController(tableController);
    }
    
    /**
     * initializes the portion of the layout that the table goes onto
     */

    private void initUpperRightLayout(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootFXMLController.class
                    .getResource("/fxml/TableFXML.fxml"));
            AnchorPane tableView = (AnchorPane) loader.load();
            LOG.trace("Loaded the table view.");

            // Give the controller the data object.
            tableController = loader.getController();
            tableController.setDAO(DAO);

            emailTableSpace.getChildren().add(tableView);
            LOG.trace("Added the table view to the table layout.");
            
        } catch (IOException ex) {
            errorAlert("initUpperRightLayout()");
            LOG.error("Failed to generate upper right layout.", ex);
        }
    }
    
    
    /**
     * initializes the portion of the layout that the foldertree goes on
     */

    private void initLeftLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootFXMLController.class
                    .getResource("/fxml/TreeFXML.fxml"));
            AnchorPane treeView = (AnchorPane) loader.load();
            LOG.trace("Loaded the tree view.");
            
            // Give the controller the data object.
            treeController = loader.getController();
            LOG.debug("is treeController null 2? " + ( treeController == null));
            
            treeController.setDAO(DAO);

            folderTreeSpace.getChildren().add(treeView);
            LOG.trace("Added the tree view to the tree layout.");
            
        } catch (IOException ex) {
            errorAlert("initLeftLayout()");
            LOG.error("Failed to generate left layout.", ex);
        }
    }
    
    /**
     * initializes the portion of the screen that the htmleditor is on
     */

    private void initLowerRightLayout(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootFXMLController.class
                    .getResource("/fxml/HTMLEditorFXML.fxml"));
            BorderPane htmlView = (BorderPane) loader.load();
            LOG.trace("Loaded the HTMLEditor view.");

            // Give the controller the data object.
            htmlController = loader.getController();
            htmlController.setDAO(DAO);

            htmlEditorSpace.getChildren().add(htmlView);
            LOG.trace("Added the HTMLEditor view to the HTMLEditor layout.");
            
        } catch (IOException ex) {
            errorAlert("initLowerRightLayout()");
            LOG.error("Failed to generate lower right layout.", ex);
        }
    }
    
    /**
     * creates an alert error on the screen, getting the text from the resource bundle
     * @param msg message we search the messagebundle for
     */

    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(resources.getString("ioError"));
        dialog.setHeaderText(resources.getString("ioError"));
        dialog.setContentText(resources.getString(msg));
        dialog.show();
    }
    
    /**
     * adds a file to an email (add it to a database once we connect to the database)
     * @param event 
     */

    @FXML
    public void addFile(ActionEvent event) {
        Stage stage = new Stage();
        stage = (Stage) backPane.getScene().getWindow();
        
        FileChooser selector = new FileChooser();
        File file = selector.showOpenDialog(stage);
        LOG.trace("User selected file.");
        
        if (file != null){
            System.out.println("Absolute Path: " + file.getAbsolutePath());
        }
    }
    
    /**
     * will save files from emails once we know how/there are files to save
     * @param event 
     */

    @FXML 
    public void saveFile(ActionEvent event) {
        LOG.trace("Will save file of current email once we know how.");
    }
    
    /**
     * closes the client
     * @param event 
     */

    @FXML
    public void closeClient(ActionEvent event){
        Platform.exit();
    }

    /**
     * will open an html file when I know how
     * @param event 
     */

    @FXML
    public void handleAbout(ActionEvent event){
        String info = resources.getString("Help");
        Alert alert = new Alert(AlertType.INFORMATION, info, ButtonType.CLOSE);
        alert.setTitle(resources.getString("About"));
        alert.setHeaderText(resources.getString("About"));
        alert.showAndWait();
    }
    
    /**
     * creates a window that has the propertiesfxml layout
     * @throws IOException 
     */

    @FXML
    public void createPropertyGUI() throws IOException{
        displayPropertyLayout();
    }
    
    /**
     * creates a stage, a scene, etc. to generate a window containing the propertiesfxml layout
     * @throws IOException if it can't find the file
     */

    private void displayPropertyLayout() throws IOException{
        Stage secondaryStage = new Stage();
        Parent propertyLayout;
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("MessagesBundle"));
        loader.setLocation(RootFXMLController.class.getResource("/fxml/PropertiesFXML.fxml"));
        LOG.trace("Set the properties layout.");
        propertyLayout = (BorderPane) loader.load();
        Scene scene = new Scene(propertyLayout);
        secondaryStage.setScene(scene);
        secondaryStage.show();
        LOG.trace("Displaying the properties window.");
    }
}

