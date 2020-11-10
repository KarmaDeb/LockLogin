# Config guide for Spigot
**The config.yml for Spigot looks like this:**
* ServerName: [String]
* Register:
  - Blind: false
  - TimeOut: 60
  - Max: 2
* Login:
  - Blind: true
  - TimeOut: 30
  - MaxTries: 5
* AntiBot: true
* AllowSameIp: true
* Pin: true
* Updater:
  - CheckTime: 10
  - ChangeLog: false
  - AutoUpdate: true
* 2FA: true
* Spawn:
  - Manage: false
  - TakeBack: false
* ClearChat: false
* AccountsPerIp: 4
* Lang: en_EN
* AccountSys: File
* BungeeProxy: "&cPlease, connect through bungeecord proxy!"

## How does the config works?
* ServerName is the name 2fa will use
##
* Register.Blind means if the player will be blinded if he is not registered
* Register.TimeOut is the time the player has to register
##
* Login.Blind means if the player will be blinded if he is not logged
* Login.TimeOut is the time the player has to login
* Login.MaxTries is the tries amount the player has to login
##
* AntiBot will manage LockLogin (very simple) bot protection system
##
* AllowSameIp will allow the player to bypass the "Already playing in that server" protection if it has the same IP
##
* Pin will enable/disable the pin gui auth
##
* To see specified info about updater, click [here](https://github.com/KarmaConfigs/page/wiki/Updater)
* Updater.CheckTime is the time per update check (in minutes)
* Updater.ChangeLog means if the plugin should send the changelog to the console if an update is available
* Updater.AutoUpdate manages the plugin self-updater (true = Will enable automatic updates when no players online or using /applyUpdates, or even when you restart the server | false = won't update the plugin  )
##
* 2FA manages globally the 2FA system in the plugin
##
* Spawn.Manage means if the plugin should send the player to a defined spawn when he joins
* Spawn.TakeBack means if the player should be teleported back to his latest location after verifying
##
* ClearChat means if the chat should be cleared (Only for the player) when the player joins
##
* AccountsPerIp is the amount of accounts per IP
##
* Lang is the plugin lang, more info [here](https://github.com/KarmaConfigs/page/wiki/Languages)
##
* AccountSys means how the plugin will manage players data [File,MySQL], more info [here](https://github.com/KarmaConfigs/page/wiki/Account-system-management)
##
* BungeeProxy is the message when you are using BungeeCord and you don't join through the proxy
#
# Config guide for Bungee
**The config.yml for Bungee looks like this:**
* ServerName: [String]
* Register:
  - Blind: false
  - TimeOut: 60
  - Max: 2
* Login:
  - Blind: true
  - TimeOut: 30
  - MaxTries: 5
* Updater:
  - CheckTime: 10
  - ChangeLog: false
  - AutoUpdate: true
* Pin: true
* AccountsPerIp: 4
* 2FA: true
* ClearChat: false
* Servers:
  - AuthLobby: [String]
  - MainLobby: [String]
* FallBack:
  - AuthLobby: [String]
  - MainLobby: [String]
* Lang: en_EN
* AccountSys: File

## How does the config works?
* ServerName is the name 2fa will use
##
* Register.TimeOut is the time the player has to register
##
* Login.TimeOut is the time the player has to login
* Login.MaxTries is the tries amount the player has to login
##
* Pin will enable/disable the pin gui auth
##
* To see specified info about updater, click [here](https://github.com/KarmaConfigs/page/wiki/Updater)
* Updater.CheckTime is the time per update check (in minutes)
* Updater.ChangeLog means if the plugin should send the changelog to the console if an update is available
* Updater.AutoUpdate manages the plugin self-updater (true = Will enable automatic updates when no players online or using /applyUpdates, or even when you restart the server | false = won't update the plugin  )
##
* AccountsPerIp is the amount of accounts per IP
##
* 2FA manages globally the 2FA system in the plugin
##
* ClearChat means if the chat should be cleared (Only for the player) when the player joins
##
* Servers are the servers the plugin will use
##
* FallBack are the servers the plugin will use, if the "Servers" don't work (If none found [or null], LockLogin will use the player's server as auth and lobby server)
##
* Lang is the plugin lang, more info [here](https://github.com/KarmaConfigs/page/wiki/Languages)
##
* AccountSys means how the plugin will manage players data [File,MySQL], more info [here](https://github.com/KarmaConfigs/page/wiki/Account-system-management)