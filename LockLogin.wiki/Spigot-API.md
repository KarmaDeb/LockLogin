### Version 2.1.7
SINCE THIS VERSION SPIGOT WILL HAVE API, LOWERS VERSION WON'T HAVE THE API

### Events:
SPIGOT WON'T HAVE ANY EVENT UNTIL: 3.0.9

# Usage:
The first thing you have to do to use LockLogin API is check if the plugin is being used by the server (to avoid errors)

![](https://i.imgur.com/NoZUVOB.png)

Then you can create a PlayerAPI method witch return PlayerAPI (make sure to use io.github.karmaconfigs.Spigot.API.PlayerAPI)

![](https://i.imgur.com/FWho61j.png)

The full class would be something like this:
![](https://i.imgur.com/dgbdcUO.png)

If you've done everything correctly, and you have LockLogin in your plugins, you will be able to use the API like this:

![](https://i.imgur.com/7qhAAeI.png)

## Events example
There're two events || PlayerVerifyEvent & PlayerRegisterEvent
MAKE SURE YOU USE SPIGOT.API.EVENTS AND NOT BUNGEECORD.API.EVENTS
![](https://i.imgur.com/daCOaox.png)