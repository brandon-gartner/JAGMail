/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * bean for use with the tree component
 * @author Brandon Gartner
 */
public class FolderBean {
    private StringProperty folderName;
    private ObservableList<EmailFXBean> folderBeanList;
    
    public FolderBean(){
        folderName = new SimpleStringProperty();
        folderName.set("");
        folderBeanList = FXCollections.observableArrayList();
    }
    
    public String getFolderName(){
        return folderName.get();
    }
    
    public void setFolderName(String newName){
        folderName.set(newName);
    }
    
    public void addEmail(EmailFXBean emailBean){
        folderBeanList.add(emailBean);
    }
    
    public ObservableList<EmailFXBean> getEmails(){
        ObservableList<EmailFXBean> newBeanList = FXCollections.observableArrayList();
        for (EmailFXBean bean : folderBeanList){
            newBeanList.add(new EmailFXBean(bean.getEmailId(), bean.getFrom(), bean.getSubject(), bean.getDate()));
        }
        return newBeanList;
    }
}
