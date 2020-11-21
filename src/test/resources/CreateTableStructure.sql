USE EMAILDB;


DROP TABLE IF EXISTS emailToAddresses;
DROP TABLE IF EXISTS attachments;
DROP TABLE IF EXISTS emails;
DROP TABLE IF EXISTS folders;
DROP TABLE IF EXISTS addresses;

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


CREATE TABLE emails(
    emailId INT NOT NULL AUTO_INCREMENT,
    from_who TEXT,
    subject TEXT,
    message LONGTEXT,
    htmlMessage LONGTEXT,
    sendDate DATETIME,
    receiveDate DATETIME,
    folderId INT NOT NULL,
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

INSERT INTO folders(name) VALUES
("Inbox"),
("Sent"),
("Drafts");


INSERT INTO addresses(emailAddress, name) VALUES
("bg01test@gmail.com", "Bg01 Test"),
("bg02test@gmail.com", "Bg02 Test"),
("bg03test@gmail.com", "Bg03 Test"),
("bg04test@gmail.com", "Bg04 Test"),
("bg05test@gmail.com", "Bg05 Test"),
("bg06test@gmail.com", "Bg06 Test"),
("bg07test@gmail.com", "Bg07 Test"),
("bg08test@gmail.com", "Bg08 Test"),
("bg09test@gmail.com", "Bg09 Test"),
("bg10test@gmail.com", "Bg10 Test"),
("bg11test@gmail.com", "Bg11 Test"),
("bg12test@gmail.com", "Bg12 Test"),
("bg13test@gmail.com", "Bg13 Test"),
("bg14test@gmail.com", "Bg14 Test"),
("bg15test@gmail.com", "Bg15 Test"),
("bg16test@gmail.com", "Bg16 Test"),
("bg17test@gmail.com", "Bg17 Test"),
("bg18test@gmail.com", "Bg18 Test"),
("bg19test@gmail.com", "Bg19 Test"),
("bg20test@gmail.com", "Bg20 Test"),
("bg21test@gmail.com", "Bg21 Test"),
("bg22test@gmail.com", "Bg22 Test"),
("bg23test@gmail.com", "Bg23 Test"),
("bg24test@gmail.com", "Bg24 Test"),
("bg25test@gmail.com", "Bg25 Test"),
("bg26test@gmail.com", "Bg26 Test"),
("bg27test@gmail.com", "Bg27 Test"),
("bg28test@gmail.com", "Bg28 Test"),
("bg29test@gmail.com", "Bg29 Test"),
("bg30test@gmail.com", "Bg30 Test");

INSERT INTO emails(from_who, subject, message, htmlMessage, folderId) VALUES
("bg01test@gmail.com", "subject1", "aaaaaaaaaaaaaaaaaaa", "<html>1</html>", 1),
("bg01test@gmail.com", "subject2", "bbbbbbbbbbbbbbbbb", "<html>2</html>", 1),
("bg02test@gmail.com", "subject3", "ccccccccccccccccccc", "<html>3</html>", 2),
("bg01test@gmail.com", "subject4", "ddddddddddddddddd", "<html>4</html>", 1),
("bg02test@gmail.com", "subject5", "eeeeeeeeeeeeeeeeeee", "<html>5</html>", 2),
("bg01test@gmail.com", "subject6", "fffffffffffffffffff", "<html>6</html>", 1),
("bg01test@gmail.com", "subject7", "ggggggggggggggggggg", "<html>7</html>", 1),
("bg01test@gmail.com", "subject8", "hhhhhhhhhhhhhhhhh", "<html>8</html>", 1),
("bg02test@gmail.com", "subject9", "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii", "<html>9</html>", 3),
("bg01test@gmail.com", "subject10", "jjjjjjjjjjjjjjjjjjjjjjjjjjj", "<html>10</html>", 1),
("bg02test@gmail.com", "subject11", "kkkkkkkkkkkkkkkkkkkk", "<html>11</html>", 3),
("bg01test@gmail.com", "subject12", "lllllllllllllllllllllllllllllllll", "<html>12</html>", 1),
("bg02test@gmail.com", "subject13", "mmmmmmmmmmmmmm", "<html>13</html>", 2),
("bg01test@gmail.com", "subject14", "nnnnnnnnnnnnnnnnnnn", "<html>14</html>", 1),
("bg01test@gmail.com", "subject15", "ooooooooooooooooooo", "<html>15</html>", 1),
("bg01test@gmail.com", "subject16", "ppppppppppppppppppp", "<html>16</html>", 1),
("bg01test@gmail.com", "subject17", "qqqqqqqqqqqqqqqqqqq", "<html>17</html>", 1),
("bg02test@gmail.com", "subject18", "rrrrrrrrrrrrrrrrrrrrrr", "<html>18</html>", 3),
("bg01test@gmail.com", "subject19", "ssssssssssssssssssssss", "<html>19</html>", 1),
("bg01test@gmail.com", "subject20", "tttttttttttttttttttttt", "<html>20</html>", 1);

INSERT INTO emailToAddresses(emailId, addressId, type) VALUES
(1,6, "to"),
(1,2, "to"),
(1,3, "to"),
(1,4, "to"),
(1,5, "to"),
(2,2, "cc"),
(2,3, "cc"),
(2,4, "cc"),
(3,1, "to"),
(3,3, "cc"),
(3,4, "bcc"),
(3,5, "to"),
(4,2, "to"),
(4,3, "cc"),
(4,5, "bcc"),
(4,4, "cc"),
(5,1, "to"),
(5,18, "to"),
(5,3, "to"),
(5,4, "to"),
(5,5, "cc"),
(5,6, "to"),
(5,7, "to"),
(5,8, "to"),
(5,9, "to");