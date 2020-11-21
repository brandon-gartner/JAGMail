/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.gui_business;

import ca.brandongartner.jag.beans.MailConfigFXMLBean;
import ca.brandongartner.jag.controllers.RootFXMLController;
import ca.brandongartner.jag.mail_database.DatabaseDAO;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main class, primarily creates the root layout and then applies all of the different layouts on top of the root layout
 * @author Brandon Gartner
 */
public class MainAppFX extends Application {
 
    private final static Logger LOG = LoggerFactory.getLogger(MainAppFX.class);
    
    private Stage primaryStage;
    private Parent rootLayout;
    private Locale currentLocale;
    private final String propertyName = "config";
    private MailConfigFXMLBean configBean;
    private Parent propertyLayout;
    private PropertyManagement pm;
    private DatabaseDAO DAO;
    
    /**
     * starts it off, displays the stage and then the root layout.
     * @param primaryStage the primary stage to display
     * @throws IOException if it's unable to open the layout
     */
    @Override
    public void start(Stage primaryStage) throws IOException{
        this.primaryStage = primaryStage;
        Scene scene;
        if (!retrieveMailConfig()){
            initPropertyLayout();
            LOG.trace("Initialized property layout.");
            scene = new Scene(propertyLayout);
            
            primaryStage.setScene(scene);
            primaryStage.show();
            //do stuff in event handlers here, for the event of saving the properties
        } else {
            DAO = new DatabaseDAO(configBean);
            initRootLayout();
        
            LOG.debug(">>>>>>" + (rootLayout == null));
            this.primaryStage.setTitle(ResourceBundle.getBundle("MessagesBundle").getString("Title"));
        
            scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            LOG.trace("Showing the scene.");
        }
    }
    
    //when the window closes
    @Override
    public void stop() {
        LOG.info("Window closed.");
    }
    
    //TODO
    private boolean retrieveMailConfig() throws IOException {
        pm = new PropertyManagement();
        configBean = new MailConfigFXMLBean();
        return pm.loadTextProperties(configBean, "", propertyName);
    }
    
     /**
     * Load the layout and controller. When the RootLayoutController runs its
     * initialize method all the other containers are created.
     */
    public void initRootLayout() {
        currentLocale = Locale.getDefault();
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            
            //RootFXMLController rootController = loader.getController();
            //rootController.setDAO(DAO);
            //LOG.debug("Connected DAO");
            
            LOG.debug(">>>>>>>>>" + ResourceBundle.getBundle("MessagesBundle").getString("Title"));
            
            loader.setResources(ResourceBundle.getBundle("MessagesBundle"));

            loader.setLocation(MainAppFX.class.getResource("/fxml/RootFXML.fxml"));
            LOG.trace("Set the location of the RootFXML file.");
            rootLayout = (BorderPane) loader.load();
            LOG.debug("Survived looking for root fxml.");
            
  

        } catch (IOException ex) {
            errorAlert("initRootLayout()");
            LOG.error("Unable to locate files.", ex);
        }
    }
    
    /**
     * initializes the property layout
     */
    public void initPropertyLayout() {
        try{
            FXMLLoader loader = new FXMLLoader();
            
            loader.setResources(ResourceBundle.getBundle("MessagesBundle"));
            loader.setLocation(MainAppFX.class.getResource("/fxml/PropertiesFXML.fxml"));
            LOG.trace("Set the location of the PropertiesFXML file.");
            
            propertyLayout = (BorderPane) loader.load();
        }
        catch (IOException e){
            errorAlert("initPropertyLayout()");
            LOG.error("Unable to locatel files.", e);
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
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("ioError"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("ioError"));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString(msg));
        dialog.show();
    }
    
    /**
     * Where it all begins
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}


