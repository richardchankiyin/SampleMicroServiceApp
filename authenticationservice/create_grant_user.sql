CREATE USER IF NOT EXISTS 'app'@'%' IDENTIFIED WITH mysql_native_password BY 'apppass';
GRANT ALL PRIVILEGES ON test. * TO 'app'@'%';
