# (Make sure LockLogin is in the server before using any API method)

### Version 2.0.5
THE ONLY THING YOU CAN DO WITH THE API IN 2.0.5 - 2.1.7 IS LOG THE PLAYER AND CHECK IF THE PLAYER IS LOGGED

### Version 2.1.5 and UP
SINCE THIS VERSION BUNGEECORD API DOESN'T NEED TO SPECIFY A LOCKLOGIN INSTANCE

### Version 2.1.7
SINCE THIS VERSION BUNGEECORD API WILL BE MORE EXTENSE


# Usage:
The first thing you have to do to use LockLogin API is check if the plugin is being used by the server (to avoid errors)

![](https://i.imgur.com/wSGMuZM.png)

Once your plugin registered the commands/events after checking if LockLogin will be able to use them, you can proceed to use the API

## Events example
There're two events || PlayerVerifyEvent & PlayerRegisterEvent
MAKE SURE YOU USE BUNGEECORD.API.EVENTS AND NOT SPIGOT.API.EVENTS
![](https://i.imgur.com/daCOaox.png)

## Api methods
![](https://i.imgur.com/pd1c1qI.png)