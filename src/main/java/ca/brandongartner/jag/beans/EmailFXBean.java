/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import jodd.mail.ReceivedEmail;

/**
 * bean that contains essential email properties, for use with the table component
 * @author Brandon Gartner
 */
public class EmailFXBean {
    private StringProperty emailId;
    private StringProperty fromField;
    private StringProperty subjectField;
    private StringProperty htmlField;
    //TODO: figure out date, too tired atm
    private StringProperty sentReceivedDate;
    
    
    public EmailFXBean(){
        emailId = new SimpleStringProperty();
        fromField = new SimpleStringProperty();
        subjectField = new SimpleStringProperty();
        sentReceivedDate = new SimpleStringProperty();
        htmlField = new SimpleStringProperty();
        emailId.set("");
        fromField.set("");
        subjectField.set("");
        sentReceivedDate.set("");
        htmlField.set("");
    }
    
    public EmailFXBean(String id, String from, String subject, String htmlMessage, String date){
        emailId = new SimpleStringProperty();
        fromField = new SimpleStringProperty();
        subjectField = new SimpleStringProperty();
        sentReceivedDate = new SimpleStringProperty();
        htmlField = new SimpleStringProperty();
        emailId.set(id);
        fromField.set(from);
        subjectField.set(subject);
        sentReceivedDate.set(date);
        htmlField.set(htmlMessage);
    }
    
    public String getEmailId(){
        return emailId.get();
    }
    
    public void setEmailId(String id){
        emailId.set(id);
    }
    
    public StringProperty getIdProperty(){
        return emailId;
    }
    
    public String getFrom(){
        return fromField.get();
    }
    
    public void setFromField(String newFrom){
        fromField.set(newFrom);
    }
    
    public StringProperty getFromProperty(){
        return fromField;
    }
    
    public String getSubject(){
        return subjectField.get();
    }
    
    public void setSubjectField(String newSubject){
        subjectField.set(newSubject);
    }
    
    public StringProperty getSubjectProperty(){
        return subjectField;
    }
    
    public String getDate(){
        return sentReceivedDate.get();
    }
    
    public void setDate(String newDate){
        sentReceivedDate.set(newDate);
    }
    
    public StringProperty getDateProperty(){
        return sentReceivedDate;
    }
    
    public String getHtmlField(){
        return htmlField.get();
    }
    
    public void setHtmlField(String newHtml){
        htmlField.set(newHtml);
    }
    
    public StringProperty getHtmlFieldProperty(){
        return htmlField;
    }
    
    public EmailFXBean(ReceivedEmail received){
        this.setDate(received.receivedDate().toString());
        this.setFromField(received.from().toString());
        this.setSubjectField(received.subject());
        this.setHtmlField(received.messages().get(1).toString());
    }
}
