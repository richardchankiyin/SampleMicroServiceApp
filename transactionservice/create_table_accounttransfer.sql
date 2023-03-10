CREATE TABLE IF NOT EXISTS accounttransfer(
   id INT AUTO_INCREMENT PRIMARY KEY,
   accountno VARCHAR(100) NOT NULL,
   amount DECIMAL(20,5) NOT NULL,
   doneby VARCHAR(100) NOT NULL,
   uptime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   UNIQUE(accountno, doneby)
);
