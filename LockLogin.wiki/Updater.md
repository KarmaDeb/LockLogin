Maybe when you open config.yml, you see the update section a bit confusing, here I will explain you, how to use it and what does each thing

```
You can just disable updater, so LockLogin will only check the version when is loaded, but I don't recommend that if you won't check manually for updates in Spigot
```

You can configure updater in config.yml

(In config.yml)
* Updater:
   * Check: true
   * CheckTime: 10
   * ChangeLog: false
   * AutoUpdate: true

### How to configure it
* Check: true = Enable version checker / false = Won't check for updates
* CheckTime: [From 5 - 60] The time between an update check is done (in minutes)
* ChangeLog: true = Send the changelog (list of changes) when an update is found / false = Just display a new version is available
* AutoUpdate: true = Enable LockLogin built-in auto update system [Will download LockLogin.jar] the .jar update will be applied once a server restart, when the server is empty, or manually executing /locklogin applyUpdates (in spigot) | /applyUpdates (in BungeeCord) / false = Won't download anything and you will have to download the latest version manually

# How to update ( manually )
There're two ways to update LockLogin, if you don't have enabled "AutoUpdate" or LockLogin can't update itself

## Running /locklogin applyUpdates
/locklogin applyUpdates, will refresh LockLogin and check plugins/update folder for any LockLogin.jar, if the found .jar is an instance of LockLogin and its version is higher than the actual one, the plugin will be updated, either, it will be only reloaded

## Old-fashioned
Just drop and drag the new LockLogin.jar replacing the actual one (while LockLogin disabled) and load the updated .jar using a plugin loader or restarting your server if it's already started