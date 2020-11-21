/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * bean that primarily exists to bind with the htmleditor
 * @author Brandon Gartner
 */
public class HTMLEditorFXBean {
    private StringProperty containedHTMLText;
    
    public HTMLEditorFXBean(){
        containedHTMLText = new SimpleStringProperty();
        containedHTMLText.set("");
    }
    
    public void setHTML(String newHTML){
        containedHTMLText.set(newHTML);
    }
    
    public String getHTML(){
        return containedHTMLText.get();
    }
    
    public StringProperty getHTMLProperty(){
        return containedHTMLText;
    }
}
