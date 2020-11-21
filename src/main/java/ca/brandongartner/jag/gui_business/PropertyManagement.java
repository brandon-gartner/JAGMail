/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.gui_business;

import ca.brandongartner.jag.beans.MailConfigFXMLBean;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import static java.nio.file.Paths.get;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class that controls creation/modification of the properties file, via the properties fxml component
 * @author Brandon Gartner
 */
public class PropertyManagement {
    
    private final static Logger LOG = LoggerFactory.getLogger(PropertyManagement.class);
    
    /**
     * loads the properties from a file into a mailconfigbean
     * @param config mailconfigbean to load it into
     * @param path the path where the file is located
     * @param propertyFileName the name of the file, wherever it is stored
     * @return a boolean stating whether or not the file exists
     * @throws IOException if it is unable to load the file
     */
    public final boolean loadTextProperties(final MailConfigFXMLBean config, final String path, final String propertyFileName) throws IOException {
        boolean found = false;
        Properties properties = new Properties();
        
        Path txtFile = get(path, propertyFileName + ".properties");
        
        //Only if file exists, returns false otherwise
        if (Files.exists(txtFile)) {
            LOG.trace("Found the properties file.");
            try (InputStream propertyFileStream = newInputStream(txtFile);) {
                properties.load(propertyFileStream);
            }
                config.setUserName(properties.getProperty("userName"));
                config.setUserEmailAddress(properties.getProperty("emailAddress"));
                config.setPassword(properties.getProperty("mailPassword"));
                config.setImapURL(properties.getProperty("imapURL"));
                config.setSmtpURL(properties.getProperty("smtpURL"));
                config.setImapPort(properties.getProperty("imapPort"));
                config.setSmtpPort(properties.getProperty("smtpPort"));
                config.setMySqlURL(properties.getProperty("mysqlURL"));
                config.setDatabaseName(properties.getProperty("mysqlDatabase"));
                config.setMySqlPort(properties.getProperty("mysqlPort"));
                config.setMySqlUserName(properties.getProperty("mysqlUser"));
                config.setMySqlPassword(properties.getProperty("mysqlPassword"));
                LOG.trace("Finished setting the properties into the MailConfigBean.");
                
                found = true;
            }
        return found;
        }
    
    
    /**
     * writes the properties in a mailconfigbean into a file
     * @param path of where the file should be saved
     * @param propertyFileName name of the file
     * @param config the mailconfigbean whose properties are being saved
     * @throws IOException throws exception when attempting to save
     */
    public final void writeTextProperties(final String path, final String propertyFileName, final MailConfigFXMLBean config) throws IOException {
        Properties properties = new Properties();
        LOG.trace("Started setting the properties of the MailConfigBean.");
        properties.setProperty("userName", config.getUserName());
        properties.setProperty("emailAddress", config.getUserName());
        properties.setProperty("mailPassword", config.getPassword());
        properties.setProperty("imapURL", config.getImapURL());
        properties.setProperty("smtpURL", config.getSmtpURL());
        properties.setProperty("imapPort", config.getImapPort());
        properties.setProperty("smtpPort", config.getSmtpPort());
        properties.setProperty("mysqlURL", config.getMySqlURL());
        properties.setProperty("mysqlDatabase", config.getDatabaseName());
        properties.setProperty("mysqlPort", config.getMySqlPort());
        properties.setProperty("mysqlUser", config.getMySqlUserName());
        properties.setProperty("mysqlPassword", config.getMySqlPassword());
        
        Path txtFile = get(path, propertyFileName + ".properties");
        
        //Creates file or reduces to length 0 before writing.
        LOG.trace("Started trying to write properties to the file.");
        try ( OutputStream propertyFileStream = newOutputStream(txtFile)) {
            properties.store(propertyFileStream, "SMTP Properties");
        }   
    }
}
