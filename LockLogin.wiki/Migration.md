While using LockLogin, you can migrate from MySQL to File, or from AuthMe mysql/sqlite, to MySQL/File, the File > MySQL migration is automatically executed when the player joins



# MySQL > Files
To migrate from MySQL database to file system, you must run /locklogin migrate MySQL (in spigot) or /migrate (in BungeeCord)<br><br>
Note that migrating in BungeeCord isn't as usual as you may think, to migrate from AuthMe to LockLogin making sure nothing wrong goes on, you should migrate from a spigot server using /locklogin migrate



# AuthMe MySQL/SQLite > MySQL/File
Migrating from AuthMe to LockLogin mysql is a bit more complicated, but still an easy work with a simple command, the only thing you have to do is read your authme mysql config, the most important sections are:
```yaml
DataSource:
   mySQLDatabase: (Default)authme
   mySQLTablename: (Default)authme
   mySQLRealName: (Default)realname
   mySQLColumnPassword: (Default)password
```
After having this info, you will have to run the command /locklogin migrate AuthMe ``mySQLDatabase``authme ``mySQLTablename``authme ``mySQLRealName``realname ``mySQLColumnPassword``password
