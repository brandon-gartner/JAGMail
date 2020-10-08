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
public class MailConfigBean {

    private String host;
    private String userEmailAddress;
    private String password;
    private String imapURL;
    private String smtpURL;
    private String imapPort = "993";
    private String smtpPort = "465";
    private String mySqlURL;
    private String databaseName;
    private String mySqlPort = "3306";
    private String mySqlUsername;
    private String mySqlPassword;

    /**
     * Default Constructor
     */
    public MailConfigBean() {
        this.host = "";
        this.userEmailAddress = "";
        this.password = "";
        this.imapURL = "";
        this.smtpURL = "";
        this.imapPort = "993";
        this.smtpPort = "465";
        this.mySqlURL = "";
        this.databaseName = "";
        this.mySqlPort = "3306";
        this.mySqlUsername = "";
        this.mySqlPassword = "";
    }
    
    public MailConfigBean(String host, String userEmailAddress, String password){
        this.host = host;
        this.userEmailAddress = userEmailAddress;
        this.password = password;
        this.imapURL = "";
        this.smtpURL = "";
        this.imapPort = "993";
        this.smtpPort = "465";
        this.mySqlURL = "";
        this.databaseName = "";
        this.mySqlPort = "3306";
        this.mySqlUsername = "";
        this.mySqlPassword = "";
    }

    /**
     * @return the host
     */
    public final String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public final void setHost(final String host) {
        this.host = host;
    }

    /**
     * @return the userEmailAddress
     */
    public final String getUserEmailAddress() {
        return userEmailAddress;
    }

    /**
     * @param userEmailAddress
     */
    public final void setUserEmailAddress(final String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
    }

    /**
     * @return the password
     */
    public final String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public final void setPassword(final String password) {
        this.password = password;
    }
    
    public final void setImapURL(final String newURL){
        this.imapURL = newURL;
    }
    
    public final String getImapURL(){
        return this.imapURL;
    }
    
    public final void setSmtpUrl(final String newURL){
        this.smtpURL = newURL;
    }
    
    public final String getSmtpUrl(){
        return this.smtpURL;
    }
    
    public final void setImapPort(final String newPort){
        this.imapPort = newPort;
    }
    
    public final String getImapPort(){
        return this.imapPort;
    }
    
    public final void setSmtpPort(final String newPort){
        this.smtpPort = newPort;
    }
    
    public final String getSmtpPort(){
        return this.smtpPort;
    }
    
    public final void setMySqlURL(final String newURL){
        this.mySqlURL = newURL;
    }
    
    public final String getMySqlURL(){
        return this.mySqlURL;
    }
    
    public final void setDatabaseName(final String newDBName){
        this.databaseName = newDBName;
    }
    
    public final String getDatabaseName(){
        return this.databaseName;
    }
    
    public final void setMySqlPort(final String newPort){
        this.mySqlPort = newPort;
    }
    
    public final String getMySqlPort(){
        return this.mySqlPort;
    }
    
    public final void setMySqlUsername(final String newUser){
        this.mySqlUsername = newUser;
    }
    
    public final String getMySqlUsername(){
        return mySqlUsername;
    }
    
    public final void setMySqlPassword(final String newPass){
        this.mySqlPassword = newPass;
    }
    
    public final String getMySqlPassword(){
        return mySqlPassword;
    }
}


