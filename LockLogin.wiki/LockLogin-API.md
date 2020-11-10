# LockLogin API Info

Since LockLogin's version 2.0.5, LockLogin will implement an API, wich won't be fully made until 2.1.7, in where the API will be developed for Spigot and BungeeCord with more features

(Until 2.1.6 the only thing you can do with API is log the player and check if he is logged)
Since 2.1.7 you can:

* Log the player with a custom message
* Log the player without message
* Register the player
* Remove player account
* Check if the player is logged
* Check if the player is registered
* Check if player has login attempts
* Rest a login attempt
* Get the player country and country code
* An event wich is fired when the player logs (Only in bungeecord until: undefined)


Before knowing how to use the API on Spigot or BungeeCord, you must know there're two ways to use the API

1 - 
Importing the .jar

2 - 
Maven (pom.xml)

```
<repository>
  <id>github</id>
  <url>https://karmaconfigs.github.io/Respositories/</url>
</repository>

<dependency>
  <groupId>ml.karmaconfigs</groupId>
  <artifactId>LockLogin</artifactId>
  <version>3.1.5</version>
</dependency>
```

# How to use API

### [Spigot API](https://github.com/KarmaConfigs/page/wiki/Spigot-API)
## 
### [BungeeCord API](https://github.com/KarmaConfigs/page/wiki/Bungee-API)
