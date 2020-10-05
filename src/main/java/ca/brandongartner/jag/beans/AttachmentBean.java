/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.brandongartner.jag.beans;

/**
 *
 * @author Brandon Gartner
 */
public class AttachmentBean {
    private int id;
    private String fileName;
    private byte[] attachment;
    private boolean isEmbedded;
    
    public void setFileName(String newName){
        this.fileName = newName;
    }
    
    public void setAttachment(byte[] bytes){
        this.attachment = bytes;
    }
    
    public void setId(int newId){
        this.id = newId;
    }
    
    public void setIsEmbedded(boolean isItEmbedded){
        this.isEmbedded = isItEmbedded;
    }
    
    public int getId(){
        return this.id;
    }
    
    public String getFileName(){
        return this.fileName;
    }
    
    public byte[] getAttachment(){
        return this.attachment;
    }
    
    public boolean getIsEmbedded(){
        return this.isEmbedded;
    }
}
