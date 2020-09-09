package ml.karmaconfigs.LockLogin.Spigot.Utils.Inventory;

import ml.karmaconfigs.LockLogin.Spigot.LockLoginSpigot;
import ml.karmaconfigs.LockLogin.WarningLevel;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

public final class PropertyManager implements LockLoginSpigot {

    private Class<?> property;

    public PropertyManager() {
        try {
            property = Class.forName("org.spongepowered.api.profile.property.ProfileProperty");
        } catch (Exception exe) {
            try {
                property = Class.forName("com.mojang.authlib.properties.Property");
            } catch (Exception e) {
                try {
                    property = Class.forName("net.md_5.bungee.connection.LoginResult$Property");
                } catch (Exception ex) {
                    try {
                        property = Class.forName("net.minecraft.util.com.mojang.authlib.properties.Property");
                    } catch (Exception exc) {
                        try {
                            property = Class.forName("com.velocitypowered.api.util.GameProfile$Property");
                        } catch (Exception exce) {
                            out.Alert("Could not find any skin provider", WarningLevel.ERROR);
                        }
                    }
                }
            }
        }
    }

    public Object createProperty(String name, String value, String signature) {
        try {
            return ReflectionUtil.invokeConstructor(property,
                    new Class<?>[]{String.class, String.class, String.class}, name, value, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
