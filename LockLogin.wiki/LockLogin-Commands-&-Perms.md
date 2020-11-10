# Player commands with their correspondent permission
/register - Register a new account
- > args: [password] [password]
- > permission: [none]
- > needs: [The passwords must be the same]
- > aliases: [/r - /reg]
##
/login - Login to your account
- > args: [password]
- > permission: [none]
- > needs: [A registered account]
- > aliases: [/l]
##
/unlog - Close your account session
- > args: [none]
- > permission: [none]
- > needs: [Be logged and registered]
- > aliases: [none]
##
/change - Change your password
- > args: [password] [new password]
- > permission: [none]
- > needs: [The new password mustn't be the same as old]
- > aliases: [/cpass]
##
/delaccount - Removes your account
- > args: [password] [password]
- > permission: [none]
- > needs:
```
[Be logged and registered]
[Passwords must coincide]
```
- > aliases: [/delacc, delcc]
##
/2fa - Main command for 2fa options
- > args:
```
[password] (To enable 2fa)
[code] (To fully login if you have 2fa)
[password] [code] (To disable 2fa)
```
- > permission: [none]
- > needs: [Be logged and registered]
- > aliases: [none]
##
/resetfa - Generates a new 2fa code/token
- > args: [password] [code]
- > permission: [none]
- > needs: [Be logged, registered and auth with 2fa]
- > aliases: [none]
##
/pin - Sets your pin
- > args: [pin]
- > permission: [none]
- > needs: [A 4 digits pin]
- > aliases: [none]
##
/resetpin - Removes your pin
- > args: [pin]
- > permission: [none]
- > needs: [Your pin]
- > aliases: [none]


# Admin commands with their correspondent permission
/unlog - Close a player's session
- > args: [player]
- > permission: [locklogin.forceunlog]
- > needs: [A valid logged and registered player]
- > aliases: [none]
##
/delaccount - Removes a player account
- > args: [player]
- > permission: [locklogin.forcedel]
- > needs: [A valid logged and registered player]
- > aliases: [/delacc, /delcc]
##
/migrate (BungeeCord) - Migrate MySQL accounts to Yaml
- > args: [none]
- > permission: [locklogin.migrate]
- > needs: [A working MySQL connection]
- > aliases: [none]
##
/applyUpdates (BungeeCord) - Apply LockLogin updates or reload the plugin
- > args: [none]
- > permission: [locklogin.applyUpdates]
- > needs: [A LockLogin.jar in /plugins/update/, if not, the command will just reload the plugin]
- > aliases: [none]
##
/locklogin (Spigot) - LockLogin staff commands
- > args: [applyUpdates (same as BungeeCord's /applyUpdates), [migrate](https://github.com/KarmaConfigs/page/wiki/Migration)]
- > permissions: [locklogin.<arg>]
- > needs: [Same as BungeeCord's /migrate and /applyUpdates]
- > aliases: [/ll]
##
/playerinf - Shows player account info
- > args: [player]
- > permission: [locklogin.playerinfo]
- > needs: [A connected player]
- > aliases: [/playerinfo, /playerinformation]
##
/lookup - Show attached accounts to the specified account
- > args: [-p(player) | -a(address)]
- > permission: [locklogin.playerinfo]
- > needs: [A valid address / user name]
- > aliases: [none]
##
