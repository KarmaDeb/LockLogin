package ml.karmaconfigs.lockloginmodules.shared.channel;

import ml.karmaconfigs.api.common.utils.StringUtils;
import ml.karmaconfigs.lockloginmodules.shared.channel.channeling.Channel;
import ml.karmaconfigs.lockloginmodules.shared.channel.channeling.ChannelKey;
import ml.karmaconfigs.lockloginmodules.shared.channel.channeling.ChannelPair;
import ml.karmaconfigs.lockloginmodules.shared.channel.messaging.Message;
import ml.karmaconfigs.lockloginmodules.shared.channel.messaging.MessageData;
import ml.karmaconfigs.lockloginsystem.bungee.LockLoginBungee;
import ml.karmaconfigs.lockloginsystem.shared.CurrentPlatform;
import ml.karmaconfigs.lockloginsystem.shared.PlatformUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class LockLoginChannels {

    private final static Map<String, ChannelPair> channels = new HashMap<>();

    public static void registerChannel(final String name, final Channel channel) {
        /*
        ChannelPair registered = channels.getOrDefault(name, null);

        //Avoid channel overwriting
        if (registered == null) {
            ChannelPair pair = new ChannelPair();
            pair.addChannel(channel);

            channels.put(name, pair);
        }

        CurrentPlatform current = new CurrentPlatform();

        switch (current.getRunning()) {
            case BUKKIT:
            case BUNGEE:
                
                break;
            default:
                return;
        }*/

        //Channels are not done yet, I'm currently searching an efficient way of sending
        //messages through LockLogin instances
    }

    public static boolean sendMessage(final Channel sender, final MessageData message) {
        ChannelKey current_key = null;
        Message msg = new Message(sender, message);
        Field sender_key_field = null;

        try {
            sender_key_field = sender.getClass().getField("key");
            sender_key_field.setAccessible(true);

            Object registered_key_field_get = sender_key_field.get(null);
            if (registered_key_field_get != null) {
                if (registered_key_field_get instanceof ChannelKey) {
                    current_key = (ChannelKey) registered_key_field_get;
                }
            }
        } catch (Throwable ignored) {}

        if (sender_key_field != null) {
            boolean send = true;

            if (current_key == null) {
                String random_string = StringUtils.randomString(12, StringUtils.StringGen.ONLY_LETTERS, StringUtils.StringType.ALL_UPPER);

                current_key = ChannelKey.fromString(random_string);
                try {
                    sender_key_field.setInt(sender_key_field, sender_key_field.getModifiers() & ~Modifier.FINAL);
                    sender_key_field.set(sender, current_key);
                    sender_key_field.setInt(sender_key_field, sender_key_field.getModifiers() & Modifier.FINAL);
                    send = true;
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    send = false;
                }
            }

            if (send) {
                for (String name : channels.keySet()) {
                    ChannelPair assigned = channels.getOrDefault(name, null);

                    if (assigned != null) {
                        if (assigned.getFirst() != null && assigned.getSecond() != null) {
                            if (assigned.getFirst().equals(sender) || assigned.getSecond().equals(sender)) {
                                Channel target;

                                if (assigned.getFirst().equals(sender))
                                    target = assigned.getSecond();
                                else
                                    target = assigned.getFirst();

                                String target_token = "";

                                Field registered_key_field = null;
                                try {
                                    registered_key_field = target.getClass().getField("key");
                                    registered_key_field.setAccessible(true);

                                    Object registered_key_field_get = registered_key_field.get(null);
                                    if (registered_key_field_get != null) {
                                        if (registered_key_field_get instanceof ChannelKey) {
                                            ChannelKey key = (ChannelKey) registered_key_field_get;

                                            target_token = key.getKey();
                                        }
                                    } else {
                                        target_token = "";
                                    }
                                } catch (Throwable ignored) {
                                }

                                if (registered_key_field != null) {
                                    boolean access = false;

                                    if (target_token.replaceAll("\\s", "").isEmpty()) {
                                        try {
                                            registered_key_field.setInt(sender_key_field, sender_key_field.getModifiers() & ~Modifier.FINAL);
                                            registered_key_field.set(sender, current_key);
                                            registered_key_field.setInt(sender_key_field, sender_key_field.getModifiers() & Modifier.FINAL);

                                            access = true;
                                        } catch (Throwable ignored) {
                                        }
                                    } else {
                                        access = current_key.getKey().equals(target_token);
                                    }

                                    //The channel as given access to the message
                                    if (access) {
                                        try {
                                            Method onMessageReceive = target.getClass().getMethod("onMessageReceive", Message.class);
                                            onMessageReceive.invoke(target, msg);

                                            return true;
                                        } catch (Throwable ignored) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
