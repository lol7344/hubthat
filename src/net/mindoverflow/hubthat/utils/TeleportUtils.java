package net.mindoverflow.hubthat.utils;

import net.mindoverflow.hubthat.HubThat;
import net.mindoverflow.hubthat.utils.files.FileUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class TeleportUtils
{
    private static HubThat plugin;
    public TeleportUtils(HubThat givenPlugin)
    {
        plugin = givenPlugin;
    }
    // Initialize the debugger so I can debug the plugin.
    private static Debugger debugger = new Debugger(TeleportUtils.class.getName());

    // Method to teleport a player, given the location coordinates, the world name and the player's name.
    public static void teleportPlayer(double x, double y, double z, double yaw, double pitch, String worldName, String playerName)
    {
        Location location = new Location(plugin.getServer().getWorld(worldName), x, y, z, (float)yaw, (float)pitch);
        Player player = plugin.getServer().getPlayer(playerName);
        player.teleport(location);
    }

    // Method to teleport a player, given its username and defined if it's a hub or a spawn.
    public static void teleportPlayer(CommandSender sender, Player player, FileUtils.FileType type, String currentWorldName)
    {
        // Get the Player object from his playername.
        //Player player = plugin.getServer().getPlayer(playerName);

        // If the player is null, give a warning and stop the method.
        if(player == null)
        {
            debugger.sendDebugMessage(Level.SEVERE, "Error: player who tried to teleport is NULL!");
            return;
        }

        // Initialize various needed variables.
        String worldName;
        double x, y, z, yaw, pitch;

        // Check if the given file type is a hub file.
        if(type == FileUtils.FileType.HUB_YAML)
        {
            // Load hub location.
            x = type.yaml.getDouble("hub.x");
            y = type.yaml.getDouble("hub.y");
            z = type.yaml.getDouble("hub.z");
            yaw = type.yaml.getDouble("hub.yaw");
            pitch = type.yaml.getDouble("hub.pitch");
            worldName = type.yaml.getString("hub.world");
        }
        // Check if the given file type is a spawn file.
        else if(type == FileUtils.FileType.SPAWN_YAML)
        {
            // Load the spawn location.
            x = type.yaml.getDouble("spawn.x." + currentWorldName);
            y = type.yaml.getDouble("spawn.y." + currentWorldName);
            z = type.yaml.getDouble("spawn.z." + currentWorldName);
            yaw = type.yaml.getDouble("spawn.yaw." + currentWorldName);
            pitch = type.yaml.getDouble("spawn.pitch." + currentWorldName);
            worldName = type.yaml.getString("spawn.world." + currentWorldName);
        }
        // Else, set the world to null because there was a problem.
        else
        {
            worldName = null;
            x = 0; y = 0; z = 0; yaw = 0; pitch = 0;
        }

        // Check if the world name is null.
        if(worldName == null)
        {
            // Send a debug message about it.
            debugger.sendDebugMessage(Level.SEVERE, "Error: could not find world!");
            if(type == FileUtils.FileType.HUB_YAML)
            {
                // send a message about the hub being not set
                MessageUtils.sendLocalizedMessage(sender, LocalizedMessages.ERROR_HUB_NOT_SET);
            }
            else if(type == FileUtils.FileType.SPAWN_YAML)
            {
                // send a message about the spawn being not set.
                MessageUtils.sendLocalizedMessage(sender, LocalizedMessages.ERROR_SPAWN_NOT_SET);
            }
            else
            {
                MessageUtils.sendColorizedMessage(sender, "&cError in code. Contact the developer!");
            }
            // Stop.
            return;
        } else
        {
            debugger.sendDebugMessage(Level.INFO, "Found world name: " + worldName);

            // Check if the hub is not set.
            if(worldName.equals("__UNSET__") && type == FileUtils.FileType.HUB_YAML)
            {
                // Warn the player about the hub not being set.
                MessageUtils.sendLocalizedMessage(sender, LocalizedMessages.ERROR_HUB_NOT_SET);
                // Stop.
                return;
            }
        }

        // Check if the world actually exists.
        World destinationWorld = plugin.getServer().getWorld(worldName);
        if(destinationWorld == null)
        {
            // Tell the player that the world does not exist.
            String errorWorldNotExistingMessage = MessageUtils.getLocalizedMessage(LocalizedMessages.ERROR_WORLD_NOT_EXISTING, false);
            errorWorldNotExistingMessage = errorWorldNotExistingMessage.replace("%w%", worldName);
            MessageUtils.sendColorizedMessage(player, errorWorldNotExistingMessage);
            return;
        }

        // Store the location in a variable and teleport the player to it.
        Location finalLocation = new Location(destinationWorld, x, y, z, (float)yaw, (float)pitch);
        player.teleport(finalLocation);

        // Check if the player is teleporting to the hub.
        if(type == FileUtils.FileType.HUB_YAML)
        {
            // Send a message to the player about him being successfully teleported.
            MessageUtils.sendLocalizedMessage(player, LocalizedMessages.INFO_HUB_TELEPORTED);

            if(sender != player)
            {
                String message = MessageUtils.getLocalizedMessage(LocalizedMessages.INFO_HUB_TELEPORTED_OTHER, true).replace("%player%", player.getName());
                sender.sendMessage(message);
            }
        }
        else if(type == FileUtils.FileType.SPAWN_YAML)
        {
            MessageUtils.sendLocalizedMessage(player, LocalizedMessages.INFO_SPAWN_TELEPORTED);

            if(sender != player)
            {
                String message = MessageUtils.getLocalizedMessage(LocalizedMessages.INFO_SPAWN_TELEPORTED_OTHER, true).replace("%player%", player.getName()).replace("%world%", worldName);
                sender.sendMessage(message);
            }
        }
    }
}