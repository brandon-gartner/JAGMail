/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  Brandon Gartner
 * Created: Oct. 2, 2020
 */


DROP TABLE IF EXISTS emailToAddresses;
DROP TABLE IF EXISTS emailAttachments;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS emails;

CREATE TABLE folders (
    folderId INT AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL,

    PRIMARY KEY (folderId)
);

CREATE TABLE addresses (
    addressId INT AUTO_INCREMENT,
    emailAddress VARCHAR(50) UNIQUE NOT NULL,
    name TEXT,
    
    PRIMARY KEY (addressId)
);

CREATE TABLE attachments (
    attachmentId INT AUTO_INCREMENT,
    file BLOB,
    isEmbedded BOOLEAN,
    
    PRIMARY KEY (attachmentId)
);


CREATE TABLE emails (
    emailId INT AUTO_INCREMENT,
    subject TEXT,
    message LONGTEXT,
    htmlMessage LONGTEXT,
    sendDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    toAddress INT, 
    ccAddress INT,
    bccAddress INT,
    -- add constraints --
    --CONSTRAINT toAddressExists FOREIGN KEY (emailId) REFERENCES emails (emailId),
    --CONSTRAINT ccAddressExists FOREIGN KEY (addressId) REFERENCES addresses (addressId)
    --CONSTRAINT bccAddressExio FOREIGN KEY (emailId) REFERENCES emails (emailId),
    
    PRIMARY KEY (emailId)
);


CREATE TABLE emailToAddresses (
    emailId INT,
    type VARCHAR(10),
    addressId INT,
    CONSTRAINT emailExistsEmailToAddresses FOREIGN KEY (emailId) REFERENCES emails (emailId),
    CONSTRAINT addressExists FOREIGN KEY (addressId) REFERENCES addresses (addressId)
);


CREATE TABLE emailAttachments (
    emailId INT,
    attachmentId INT,
    CONSTRAINT emailExistsEmailAttachments FOREIGN KEY (emailId) REFERENCES emails (emailId),
    CONSTRAINT attachmentExists FOREIGN KEY (attachmentId) REFERENCES attachments (attachmentId)
);