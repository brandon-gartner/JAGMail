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
DROP TABLE IF EXISTS emails;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS attachments;

CREATE TABLE folders (
    folderId INT AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,

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
    from_who TEXT,
    subject TEXT,
    message LONGTEXT,
    htmlMessage LONGTEXT,
    sendDate DATETIME,
    receiveDate DATETIME,
    folderId INT,
    CONSTRAINT folderExists FOREIGN KEY (folderId) REFERENCES folders (folderId) ON DELETE CASCADE,

    PRIMARY KEY (emailId)
);


CREATE TABLE emailToAddresses (
    emailId INT,
    type VARCHAR(10),
    addressId INT,
    CONSTRAINT emailExistsEmailToAddresses FOREIGN KEY (emailId) REFERENCES emails (emailId) ON DELETE CASCADE,
    CONSTRAINT addressExists FOREIGN KEY (addressId) REFERENCES addresses (addressId) ON DELETE CASCADE
);

INSERT INTO folders (name) VALUES
('Inbox'),
('Sent'),
('Draft');

INSERT INTO addresses (emailAddress, name) VALUES
('bg01test@gmail.com', 'Bg01 Test'),
('bg02test@gmail.com', 'Bg02 Test'),
('bg03test@gmail.com', 'Bg03 Test'),
('bg04test@gmail.com', 'Bg04 Test'),
('bg05test@gmail.com', 'Bg05 Test'),
('bg06test@gmail.com', 'Bg06 Test'),
('bg07test@gmail.com', 'Bg07 Test'),
('bg08test@gmail.com', 'Bg08 Test'),
('bg09test@gmail.com', 'Bg09 Test'),
('bg10test@gmail.com', 'Bg10 Test'),
('bg11test@gmail.com', 'Bg11 Test'),
('bg12test@gmail.com', 'Bg12 Test'),
('bg13test@gmail.com', 'Bg13 Test'),
('bg14test@gmail.com', 'Bg14 Test'),
('bg15test@gmail.com', 'Bg15 Test'),
('bg16test@gmail.com', 'Bg16 Test'),
('bg17test@gmail.com', 'Bg17 Test'),
('bg18test@gmail.com', 'Bg18 Test'),
('bg19test@gmail.com', 'Bg19 Test'),
('bg20test@gmail.com', 'Bg20 Test');

INSERT INTO emails (from_who, subject, message, htmlMessage, folderId) VALUES
('bg02test@gmail.com', 'subject1', 'aaaaaaaaaaaaaaa', '<html>1</html>', 2),
('bg02test@gmail.com', 'subject2', 'bbbbbbbbbbbbb', '<html>2</html>', 3),
('bg02test@gmail.com', 'subject3', 'ccccccccccccccc', '<html>3</html>', 3),
('bg01test@gmail.com', 'subject4', 'ddddddddddddd', '<html>4</html>', 1),
('bg02test@gmail.com', 'subject5', 'eeeeeeeeeeeeeee', '<html>5</html>', 2),
('bg01test@gmail.com', 'subject6', 'ffffffffffffff', '<html>6</html>', 1),
('bg02test@gmail.com', 'subject7', 'ggggggggggggggg', '<html>7</html>', 3),
('bg01test@gmail.com', 'subject8', 'hhhhhhhhhhhhh', '<html>8</html>', 1),
('bg01test@gmail.com', 'subject9', 'iiiiiiiiiiiiiiiiiiiiiiiiii', '<html>9</html>', 1),
('bg02test@gmail.com', 'subject10', 'jjjjjjjjjjjjjjjjjjjjj', '<html>10</html>', 2),
('bg01test@gmail.com', 'subject11', 'kkkkkkkkkkkkkkk', '<html>11</html>', 1),
('bg01test@gmail.com', 'subject12', 'lllllllllllllllllllllllll', '<html>12</html>', 1),
('bg01test@gmail.com', 'subject13', 'mmmmmmmmmm', '<html>13</html>', 1),
('bg01test@gmail.com', 'subject14', 'nnnnnnnnnnnnnnn', '<html>14</html>', 1),
('bg01test@gmail.com', 'subject15', 'ooooooooooooooo', '<html>15</html>', 1),
('bg01test@gmail.com', 'subject16', 'ppppppppppppppp', '<html>16</html>', 1),
('bg01test@gmail.com', 'subject17', 'qqqqqqqqqqqqqqq', '<html>17</html>', 1),
('bg01test@gmail.com', 'subject18', 'rrrrrrrrrrrrrrrrr', '<html>18</html>', 1),
('bg01test@gmail.com', 'subject19', 'ssssssssssssssssss', '<html>19</html>', 1),
('bg01test@gmail.com', 'subject20', 'ttttttttttttttttttt', '<html>20</html>', 1);

INSERT INTO emailToAddresses (emailId, addressId, type) VALUES
(1,1,'to'),
(1,3,'cc'),
(1,4,'bcc'),
(1,5,'to'),
(2,1,'to'),
(2,3,'cc'),
(2,4,'cc'),
(2,5,'to'),
(4,2,'bcc'),
(4,3,'to'),
(4,4,'to'),
(4,5,'cc'),
(5,1,'to'),
(5,3,'to'),
(5,4,'to'),
(5,5,'to'),
(6,2,'cc'),
(6,3,'bcc'),
(6,4,'to'),
(6,5,'to');