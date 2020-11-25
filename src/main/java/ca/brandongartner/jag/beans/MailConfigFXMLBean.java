package ca.brandongartner.jag.beans;

/**
 * Class to contain the information for an email account. This is sufficient for
 * this project but will need more fields if you wish the program to work with
 * mail systems other than GMail. This should be stored in properties file. If
 * you are feeling adventurous you can look into how you might encrypt the
 * password as it will be in a simple text file.
 *
 * @author Ken Fogel
 * @author Brandon Gartner
 *
 */
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MailConfigFXMLBean {
    private StringProperty userName = new SimpleStringProperty();
    private StringProperty userEmailAddress = new SimpleStringProperty();
    private StringProperty password = new SimpleStringProperty();
    private StringProperty imapURL = new SimpleStringProperty();
    private StringProperty smtpURL = new SimpleStringProperty();
    private StringProperty imapPort = new SimpleStringProperty();
    private StringProperty smtpPort = new SimpleStringProperty();
    private StringProperty mySqlURL = new SimpleStringProperty();
    private StringProperty databaseName = new SimpleStringProperty();
    private StringProperty mySqlPort = new SimpleStringProperty();
    private StringProperty mySqlUsername = new SimpleStringProperty();
    private StringProperty mySqlPassword = new SimpleStringProperty();

    /**
     * Default Constructor
     */
    public MailConfigFXMLBean() {
        this.userName.setValue("");
        this.userEmailAddress.setValue("");
        this.password.setValue("");
        this.imapURL.setValue("");
        this.smtpURL.setValue("");
        this.imapPort.setValue("993");
        this.smtpPort.setValue("465");
        this.mySqlURL.setValue("");
        this.databaseName.setValue("");
        this.mySqlPort.setValue("3306");
        this.mySqlUsername.setValue("");
        this.mySqlPassword.setValue("");
    }
    
    public MailConfigFXMLBean(String userEmailAddress, String password){
        this.userName.setValue("");
        this.userEmailAddress.setValue(userEmailAddress);
        this.password.setValue(password);
        this.imapURL.setValue("");
        this.smtpURL.setValue("");
        this.imapPort.setValue("993");
        this.smtpPort.setValue("465");
        this.mySqlURL.setValue("");
        this.databaseName.setValue("");
        this.mySqlPort.setValue("3306");
        this.mySqlUsername.setValue("");
        this.mySqlPassword.setValue("");
    }
    
    public final void setUserName(final String userName){
        this.userName.set(userName);
    }
    
    public final String getUserName(){
        return userName.get();
    }
    
    public final StringProperty getUserNameProperty(){
        return userName;
    }

    /**
     * @return the userEmailAddress
     */
    public final String getUserEmailAddress() {
        return userEmailAddress.get();
    }
    
    public final StringProperty getEmailAddressProperty(){
        return userEmailAddress;
    }

    /**
     * @param userEmailAddress
     */
    public final void setUserEmailAddress(final String userEmailAddress) {
        this.userEmailAddress.set(userEmailAddress);
    }

    /**
     * @return the password
     */
    public final String getPassword() {
        return password.get();
    }
    
    public final StringProperty getPasswordProperty(){
        return password;
    }

    /**
     * @param password the password to set
     */
    public final void setPassword(final String password) {
        this.password.set(password);
    }
    
    public final void setImapURL(final String newURL){
        this.imapURL.set(newURL);
    }
    
    public final String getImapURL(){
        return this.imapURL.get();
    }
    
    public final void setSmtpURL(final String newURL){
        this.smtpURL.set(newURL);
    }
    
    public final StringProperty getImapURLProperty(){
        return imapURL;
    }
    
    public final String getSmtpURL(){
        return this.smtpURL.get();
    }
    
    public final StringProperty getSmtpURLProperty(){
        return smtpURL;
    }
    
    public final void setImapPort(final String newPort){
        this.imapPort.set(newPort);
    }
    
    public final String getImapPort(){
        return this.imapPort.get();
    }
    
    public final StringProperty getImapPortProperty(){
        return imapPort;
    }
    
    public final void setSmtpPort(final String newPort){
        this.smtpPort.set(newPort);
    }
    
    public final String getSmtpPort(){
        return this.smtpPort.get();
    }
    
    public final StringProperty getSmtpPortProperty(){
        return smtpPort;
    }
    
    public final void setMySqlURL(final String newURL){
        this.mySqlURL.set(newURL);
    }
    
    public final String getMySqlURL(){
        return this.mySqlURL.get();
    }
    
    public final StringProperty getSqlURLProperty(){
        return mySqlURL;
    }
    
    public final void setDatabaseName(final String newDBName){
        this.databaseName.set(newDBName);
    }
    
    public final String getDatabaseName(){
        return this.databaseName.get();
    }
    
    public final StringProperty getDatabaseNameProperty(){
        return databaseName;
    }
    
    public final void setMySqlPort(final String newPort){
        this.mySqlPort.set(newPort);
    }
    
    public final String getMySqlPort(){
        return this.mySqlPort.get();
    }
    
    public final StringProperty getSqlPortProperty(){
        return mySqlPort;
    }
    
    public final void setMySqlUserName(final String newUser){
        this.mySqlUsername.set(newUser);
    }
    
    public final String getMySqlUserName(){
        return mySqlUsername.get();
    }
    
    public final StringProperty getSqlUserNameProperty(){
        return mySqlUsername;
    }
    
    public final void setMySqlPassword(final String newPass){
        this.mySqlPassword.setValue(newPass);
    }
    
    public final String getMySqlPassword(){
        return mySqlPassword.get();
    }
    
    public final StringProperty getSqlPasswordProperty(){
        return mySqlPassword;
    }
}


