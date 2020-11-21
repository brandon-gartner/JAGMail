/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Brandon Gartner
 */


public class FormBean {
    private StringProperty toField;
    private StringProperty ccField;
    private StringProperty bccField;
    private StringProperty subjectField;
    
    public FormBean(){
        toField = new SimpleStringProperty();
        ccField = new SimpleStringProperty();
        bccField = new SimpleStringProperty();
        subjectField = new SimpleStringProperty();
        toField.set("");
        ccField.set("");
        bccField.set("");
        subjectField.set("");
        
    }
    
    public String getToField(){
        return this.toField.get();
    }
    
    public StringProperty getToFieldProperty(){
        return toField;
    }
    
    public void setToField(String newToField){
        toField.set(newToField);
    }
    
    public String getCCField(){
        return this.ccField.get();
    }
    
    public StringProperty getCCFieldProperty(){
        return ccField;
    }
    
    public void setCCField(String newccField){
        ccField.set(newccField);
    }
    
    public String getBCCField(){
        return this.bccField.get();
    }
    
    public StringProperty getBCCFieldProperty(){
        return bccField;
    }
    
    public void setBCCField(String newBCCField){
        bccField.set(newBCCField);
    }
    
    public String getSubjectField(){
        return this.subjectField.get();
    }
    
    public StringProperty getSubjectFieldProperty(){
        return subjectField;
    }
    
    public void setSubjectField(String newSubject){
        subjectField.set(newSubject);
    }
}
