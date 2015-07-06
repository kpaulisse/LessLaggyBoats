/*
 * Copyright (c) 2015, Kevin Paulisse
 *
 * This file is part of LessLaggyBoats.
 *
 * LessLaggyBoats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LessLaggyBoats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LessLaggyBoats.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.paulisse.bukkit.plugins.lesslaggyboats.handlers;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.LagTracker;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.LessLaggyBoatsParameters;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.TrackedBoat;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.VehicleMap;
import com.paulisse.bukkit.plugins.lesslaggyboats.utils.BoatUtils;

public class CommandHandler {

    private CommandSender sender;
    private LessLaggyBoats plugin;

    /**
     * Constructor of command handler
     * @param plugin LessLaggyBoats plugin object
     * @param sender Command Sender
     */
    public CommandHandler(LessLaggyBoats plugin, CommandSender sender) {
        this.setPlugin(plugin);
        this.setSender(sender);
    }

    /**
     * Disable the plugin
     * @return True
     */
    private boolean disable() {
        if (plugin.getIsEnabled() == true) {
            plugin.onDisable();
            if (sender != plugin.getServer().getConsoleSender()) {
                sender.sendMessage("[LessLaggyBoats] Disabled plug-in");
            }
            plugin.log(Level.INFO, "Disabled plug-in via 'disable' command");
        } else {
            sender.sendMessage("[LessLaggyBoats] Plug-in is already disabled");
        }
        return true;
    }

    /**
     * Enable the plugin
     * @return True
     */
    private boolean enable() {
        if (plugin.getIsEnabled() == false) {
            plugin.onEnable();
            if (sender != plugin.getServer().getConsoleSender()) {
                sender.sendMessage("[LessLaggyBoats] Enabled plug-in");
            }
            plugin.log(Level.INFO, "Enabled plug-in via 'enable' command");
        } else {
            sender.sendMessage("[LessLaggyBoats] Plug-in is already enabled");
        }
        return true;
    }

    /**
     * Handle a command
     * @param cmd Command
     * @param label Label
     * @param args Arguments
     * @return True if command was executed, false otherwise
     */
    public boolean handle(Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("showlag")) {
            return showLag();
        }
        if (cmd.getName().equalsIgnoreCase("listboats")) {
            return showList();
        }
        if (cmd.getName().equalsIgnoreCase("lesslaggyboats")) {
            if (args == null || args.length < 1) {
                return help(null);
            }
            if (args[0].equalsIgnoreCase("reload")) {
                return reload();
            }
            if (args[0].equalsIgnoreCase("set")) {
                return set(args);
            }
            if (args[0].equalsIgnoreCase("disable")) {
                return disable();
            }
            if (args[0].equalsIgnoreCase("enable")) {
                return enable();
            }
            if (args[0].equalsIgnoreCase("config")) {
                return showConfig();
            }
            if (args[0].equalsIgnoreCase("lag")) {
                return showLag();
            }
            if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("boats")) {
                return showList();
            }
            return help(args);
        }
        return false;
    }

    /**
     * Print help
     * @param args Arguments
     * @return True
     */
    private boolean help(String[] args) {
        StringBuilder sb = new StringBuilder();
        if (args == null || args.length <= 1 || !args[1].equalsIgnoreCase("set")) {
            sb.append("[LessLaggyBoats] Available Commands:");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("reload").append(ChatColor.GREEN.toString()).append(": Reload configuration from file");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("disable").append(ChatColor.GREEN.toString()).append(": Disable plug-in");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("enable").append(ChatColor.GREEN.toString()).append(": Enable plug-in");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("config").append(ChatColor.GREEN.toString()).append(": Show current configuration");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("list").append(ChatColor.GREEN.toString()).append(": List boats being tracked/managed");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("lag").append(ChatColor.GREEN.toString()).append(": Display current server lag");
            sb.append("\n   ");
            sb.append(ChatColor.YELLOW.toString()).append("set").append(ChatColor.RED.toString()).append(" parameter");
            sb.append(ChatColor.BLUE.toString()).append(" value").append(ChatColor.GREEN.toString()).append(": Change a parameter value");
        } else {
            sb.append("[LessLaggyBoats] set").append(ChatColor.RED.toString()).append(" parameter");
            sb.append(ChatColor.BLUE.toString()).append(" value");
            for (LessLaggyBoatsParameters cfgvalue : LessLaggyBoatsParameters.values()) {
                sb.append("\n   ").append(ChatColor.YELLOW.toString()).append(cfgvalue.getName()).append(": ");
                sb.append(ChatColor.GREEN.toString());
                if (cfgvalue.getClazz() == Boolean.class) {
                    sb.append("true/false");
                } else if (cfgvalue.getClazz() == Double.class) {
                    sb.append("0.0 - ???");
                }
            }
        }
        sender.sendMessage(sb.toString());
        return true;
    }

    /**
     * Reload the plugin
     * @return True
     */
    private boolean reload() {
        plugin.onDisable();
        plugin.onEnable();
        plugin.reloadConfig();
        if (sender != plugin.getServer().getConsoleSender()) {
            sender.sendMessage("[LessLaggyBoats] Reloaded plugin and config.yml");
        }
        plugin.log(Level.INFO, "Reloaded plugin and config.yml");
        return true;
    }

    /**
     * Handle the "set" command
     * @param args Arguments
     * @return True
     */
    private boolean set(String[] args) {
        if (args.length != 3) {
            sender.sendMessage("[LessLaggyBoats] Usage: lesslaggyboats set <parameter> <value>");
            return true;
        }
        Boolean result = false;
        String parameter = args[1];
        String value = args[2];

        for (LessLaggyBoatsParameters cfgvalue : LessLaggyBoatsParameters.values()) {
            if (parameter.equalsIgnoreCase(cfgvalue.getName())) {
                if (cfgvalue.getClazz() == Boolean.class) {
                    Boolean x = Boolean.parseBoolean(value);
                    result = cfgvalue.setParameter(plugin, x);
                } else if (cfgvalue.getClazz() == Double.class) {
                    Double x = Double.parseDouble(value);
                    result = cfgvalue.setParameter(plugin, x);
                }
            }
        }

        if (result == true) {
            if (sender != plugin.getServer().getConsoleSender()) {
                sender.sendMessage(String.format("[LessLaggyBoats] Parameter %s set to %s", parameter, value));
            }
            plugin.log(Level.INFO, String.format("Set '%s' to '%s'", parameter, value));
            return true;
        }

        StringBuilder params = new StringBuilder();
        for (LessLaggyBoatsParameters cfgvalue : LessLaggyBoatsParameters.values()) {
            if (params.length() > 0) {
                params.append("|");
            }
            params.append(cfgvalue.getName());
        }
        sender.sendMessage("[LessLaggyBoats] Invalid parameter. Valid parameters: " + params.toString());
        return true;
    }

    /**
     * @param plugin the plugin to set
     */
    private void setPlugin(LessLaggyBoats lessLaggyBoats) {
        this.plugin = lessLaggyBoats;
    }

    /**
     * @param sender the sender to set
     */
    private void setSender(CommandSender sender) {
        this.sender = sender;
    }

    /**
     * Show the current configuration of the plugin
     * @return True
     */
    private boolean showConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.YELLOW.toString()).append("enabled: ");
        sb.append(ChatColor.WHITE.toString()).append(Boolean.toString(plugin.getIsEnabled()));
        for (LessLaggyBoatsParameters param : LessLaggyBoatsParameters.values()) {
            sb.append("\n    ");
            sb.append(ChatColor.YELLOW.toString()).append(param.getName()).append(": ");
            sb.append(ChatColor.WHITE.toString());
            if (param.getClazz() == Boolean.class) {
                sb.append(Boolean.toString(param.getParameter(plugin, true)));
            } else if (param.getClazz() == Double.class) {
                sb.append(Double.toString(param.getParameter(plugin, 0.0D)));
            }
        }
        sender.sendMessage(String.format("[LessLaggyBoats] Current settings:\n    %s", sb.toString()));
        return true;
    }

    /**
     * Show the current lag of the server
     * @return True
     */
    private boolean showLag() {
        LagTracker lagTracker = plugin.getLagTracker();
        StringBuilder sb = new StringBuilder();
        if (lagTracker == null) {
            sb.append("[LessLaggyBoats] ").append(ChatColor.BLUE.toString());
            sb.append("Lag is not currently being tracked");
        } else {
            Double tps = lagTracker.getTPS();
            if (tps < 0.0D) {
                sb.append("[LessLaggyBoats] ").append(ChatColor.BLUE.toString());
                sb.append("Not enough data collected yet to calculate lag");
            } else {
                sb.append("[LessLaggyBoats] Current Ticks Per Second: ");
                if (tps >= 19.0D) {
                    sb.append(ChatColor.GREEN.toString());
                } else if (tps >= 15.0D) {
                    sb.append(ChatColor.YELLOW.toString());
                } else {
                    sb.append(ChatColor.RED.toString());
                }
                sb.append(Double.toString(Math.round(tps * 100.0D) / 100.0D));
                sb.append(" [");
                sb.append(Double.toString(Math.round(tps * 5.0D)));
                sb.append(" %]");
            }
        }
        sender.sendMessage(sb.toString());
        return true;
    }

    /**
     * Show the list of boats being tracked
     * @return True
     */
    private boolean showList() {
        BoatUtils.cleanVehicleMap(plugin);
        VehicleMap vehicleMap = plugin.getVehicleMap();
        StringBuilder sb = new StringBuilder();
        if (vehicleMap == null) {
            sb.append("[LessLaggyBoats] ").append(ChatColor.BLUE.toString());
            sb.append("Vehicle map has not been initialized");
        } else if (vehicleMap.size() == 0) {
            sb.append("[LessLaggyBoats] ").append(ChatColor.BLUE.toString());
            sb.append("No boats are currently being tracked");
        } else {
            sb.append("[LessLaggyBoats] ").append(ChatColor.WHITE.toString());
            sb.append("Currently tracking ").append(ChatColor.GREEN.toString());
            sb.append(Integer.toString(vehicleMap.size())).append(ChatColor.WHITE.toString()).append(" boat");
            if (vehicleMap.size() > 1) {
                sb.append("s");
            }
            sb.append(":");
            for (TrackedBoat trackedBoat : vehicleMap.values()) {
                Entity occupant = trackedBoat.getBoat().getPassenger();
                sb.append(ChatColor.WHITE.toString()).append("\n - ");
                sb.append(ChatColor.GOLD.toString()).append(trackedBoat.getBoatName());
                sb.append(ChatColor.WHITE.toString()).append(" at ").append(ChatColor.GREEN.toString());
                sb.append(Integer.toString((int)trackedBoat.getCalculatedLocation().getX()));
                sb.append(ChatColor.WHITE.toString()).append(",").append(ChatColor.GREEN.toString());
                sb.append(Integer.toString((int)trackedBoat.getCalculatedLocation().getY()));
                sb.append(ChatColor.WHITE.toString()).append(",").append(ChatColor.GREEN.toString());
                sb.append(Integer.toString((int)trackedBoat.getCalculatedLocation().getZ()));
                sb.append(ChatColor.WHITE.toString()).append(" offset ").append(ChatColor.GREEN.toString());
                sb.append(Double.toString(Math.round(trackedBoat.getDistance()*100.0D)/100.0D));
                sb.append(ChatColor.WHITE.toString()).append(" (");
                if (occupant != null && occupant instanceof Player) {
                    sb.append(ChatColor.BLUE.toString()).append(((Player) occupant).getName());
                } else if (occupant == null) {
                    sb.append(ChatColor.RED.toString()).append("Empty Boat");
                } else {
                    sb.append(ChatColor.YELLOW.toString()).append(occupant.toString());
                }
                sb.append(ChatColor.WHITE.toString()).append(")");
            }
        }
        sender.sendMessage(sb.toString());
        return true;
    }
}
