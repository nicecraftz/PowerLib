package it.mycraft.powerlib.chat;

import it.mycraft.powerlib.common.utils.ColorAPI;
import it.mycraft.powerlib.common.utils.ServerAPI;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static it.mycraft.powerlib.common.utils.ServerAPI.*;

@Deprecated
public class Message extends it.mycraft.powerlib.common.chat.Message {

    private String message;
    private List<String> messages;

    public Message() {
        this.message = "";
        this.messages = new ArrayList<>();
    }

    @Deprecated
    public Message(String message) {
        this.message = ColorAPI.color(message);
        this.messages = new ArrayList<>();
    }

    @Deprecated
    public Message(String... messages) {
        this.message = "";
        this.messages = new ArrayList<>(ColorAPI.color(Arrays.asList(messages)));
    }

    public Message(List<String> messages) {
        this.message = "";
        this.messages = new ArrayList<>(ColorAPI.color(messages));
    }

    @Deprecated
    public Message addPlaceHolder(String placeholder, Object value) {
        message = message.replace(placeholder, value.toString());

        List<String> newMessages = new ArrayList<>();
        messages.forEach((s) -> newMessages.add(s.replace(placeholder, value.toString())));

        messages = newMessages;

        return this;
    }

    public Message set(String message) {
        this.message = ColorAPI.color(message);
        return this;
    }

    public Message set(List<String> messages) {
        this.messages = ColorAPI.color(messages);
        return this;
    }

    public Message set(String... messages) {
        this.messages = ColorAPI.color(Arrays.asList(messages));
        return this;
    }

    @Nullable
    public String getText() {
        return message;
    }

    @Nullable
    public List<String> getTextList() {
        return messages;
    }

    @Deprecated
    public void send(Object commandSender) {
        switch (ServerAPI.getType()) {
            default:
                break;
            case BUKKIT:
                if (messages.isEmpty())
                    sendBukkitMessage(commandSender, message);
                else
                    messages.forEach((m) -> sendBukkitMessage(commandSender, m));
                break;
            case BUNGEECORD:
                if (messages.isEmpty())
                    sendBungeeMessage(commandSender, message);
                else
                    messages.forEach((m) -> sendBungeeMessage(commandSender, m));
                break;
            case VELOCITY:
                if (messages.isEmpty())
                    sendVelocityMessage(commandSender, message);
                else
                    messages.forEach((m) -> sendVelocityMessage(commandSender, m));
                break;
        }
    }

    public void broadcast(String permission) {
        if (isUsingBukkit()) {
            broadcastBukkit(permission);
        } else if (isStrictlyUsingBungee()) {
            broadcastBungee(permission);
        } else if (isUsingVelocity()) {
            broadcastVelocity(permission);
        }
    }

    public void broadcast() {
        broadcast("");
    }

    public Message color() { // no need by default
        if (messages.isEmpty()) {
            this.message = ColorAPI.color(message);
        } else this.messages = ColorAPI.color(messages);
        return this;
    }

    public Message decolor() {
        if (messages.isEmpty()) {
            this.message = ColorAPI.decolor(message);
        } else this.messages = ColorAPI.decolor(messages);
        return this;
    }

    public Message hex(String pre, String post) {
        if (messages.isEmpty()) {
            this.message = ColorAPI.hex(message, pre, post);
        } else this.messages = ColorAPI.hex(messages, pre, post);
        return this;
    }

    public Message hex() {
        this.hex("&#", "");
        return this;
    }

    private void reset() {
        messages = new ArrayList<>();
        message = null;
    }

    private void sendBukkitMessage(Object commandSender, String message) {
        try {
            Class<?> commandSenderClass = Class.forName("org.bukkit.command.CommandSender");
            Method sendMessage = commandSenderClass.getDeclaredMethod("sendMessage", String.class);
            sendMessage.invoke(commandSender, message);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void broadcastBukkit(String permission) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            for (String line : messages.isEmpty() ? Collections.singletonList(message) : messages) {
                if (permission.isEmpty()) {
                    bukkitClass.getMethod("broadcastMessage", String.class).invoke(null, line);
                } else bukkitClass.getMethod("broadcast", String.class, String.class)
                        .invoke(null, line, permission);
            }
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void sendBungeeMessage(Object commandSender, String message) {
        try {
            Class<?> commandSenderClass = Class.forName("net.md_5.bungee.api.CommandSender");
            Method sendMessage = commandSenderClass.getMethod("sendMessage", String.class);
            sendMessage.invoke(commandSender, message);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void broadcastBungee(String permission) {
        try {
            Class<?> proxiedPlayerClass = Class.forName("net.md_5.bungee.api.connection.ProxiedPlayer");
            Class<?> proxyServerClass = Class.forName("net.md_5.bungee.api.ProxyServer");
            Object proxyServer = proxyServerClass.getMethod("getInstance").invoke(null);
            Collection<Object> onlinePlayers = (Collection<Object>) proxyServerClass
                    .getMethod("getPlayers").invoke(proxyServer);
            for (String line : messages.isEmpty() ? Collections.singletonList(message) : messages) {
                if (permission.isEmpty()) {
                    for (Object proxiedPlayer : onlinePlayers) {
                        proxiedPlayer.getClass().getMethod("sendMessage", String.class).invoke(proxiedPlayer, line);
                    }
                } else for (Object proxiedPlayer : onlinePlayers) {
                    if ((boolean) proxiedPlayerClass.getMethod("hasPermission", String.class)
                            .invoke(proxiedPlayer, permission)) {
                        proxiedPlayer.getClass().getMethod("sendMessage", String.class).invoke(proxiedPlayer, line);
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void sendVelocityMessage(Object commandSender, String message) {
        try {
            Class<?> commandSenderClass = Class.forName("com.velocitypowered.api.command.CommandSource");
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            final Object component = componentClass.getMethod("text", String.class).invoke(null, message);
            commandSenderClass.getMethod("sendMessage", componentClass).invoke(commandSender, component);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void broadcastVelocity(String permission) {
        try {
            Class<?> proxiedPlayerClass = Class.forName("com.velocitypowered.api.proxy.Player");
            Class<?> proxyServerClass = Class.forName("com.velocitypowered.api.proxy.ProxyServer");
            Class<?> powerlibClass = Class.forName("it.mycraft.powerlib.velocity.PowerLib");
            Object powerlib = powerlibClass.getMethod("getInstance").invoke(null);
            Object proxyServer = powerlibClass.getMethod("getProxy").invoke(powerlib);
            Collection<Object> onlinePlayers = (Collection<Object>) proxyServerClass
                    .getMethod("getAllPlayers").invoke(proxyServer);
            for (String line : messages.isEmpty() ? Collections.singletonList(message) : messages) {
                if (permission.isEmpty()) {
                    for (Object proxiedPlayer : onlinePlayers) {
                        proxiedPlayer.getClass().getMethod("sendMessage", String.class).invoke(proxiedPlayer, line);
                    }
                } else for (Object proxiedPlayer : onlinePlayers) {
                    if ((boolean) proxiedPlayerClass.getMethod("hasPermission", String.class)
                            .invoke(proxiedPlayer, permission)) {
                        proxiedPlayer.getClass().getMethod("sendMessage", String.class).invoke(proxiedPlayer, line);
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}