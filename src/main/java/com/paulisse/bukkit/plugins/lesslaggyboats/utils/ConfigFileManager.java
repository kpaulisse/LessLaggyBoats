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

package com.paulisse.bukkit.plugins.lesslaggyboats.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.LessLaggyBoatsParameters;

public class ConfigFileManager extends LessLaggyBoats {

    private static final String filename = "config.yml";

    /**
     * Set values in plugin from config file
     * @param plugin Plugin
     */
    public static void setValuesFromConfig(LessLaggyBoats plugin) {
        FileConfiguration config = plugin.getConfig();
        for (LessLaggyBoatsParameters cfgvalue : LessLaggyBoatsParameters.values()) {
            if (cfgvalue.getClazz() == Double.class) {
                Double x = config.getDouble(cfgvalue.getName(), cfgvalue.getDefault(0.0D));
                cfgvalue.setParameter(plugin, x);
            } else if (cfgvalue.getClazz() == Boolean.class) {
                Boolean x = config.getBoolean(cfgvalue.getName(), cfgvalue.getDefault(true));
                cfgvalue.setParameter(plugin, x);
            }
        }
    }

    /**
     * Save a boolean configuration value to running config and config file
     * @param plugin Plugin
     * @param configName Name of configuration
     * @param value Value of configuration
     */
    public static void saveConfigValue(LessLaggyBoats plugin, String configName, Boolean value) {
        if (plugin.getConfig().getBoolean(configName) != value) {
            plugin.getConfig().set(configName, value);
            writeFile(plugin);
        }
    }

    /**
     * Save a double configuration value to running config and config file
     * @param plugin Plugin
     * @param configName Name of configuration
     * @param value Value of configuration
     */
    public static void saveConfigValue(LessLaggyBoats plugin, String configName, Double value) {
        if (plugin.getConfig().getDouble(configName) != value) {
            plugin.getConfig().set(configName, value);
            writeFile(plugin);
        }
    }

    /**
     * Write current settings to configuration file
     * @param plugin Plugin
     */
    private static void writeFile(LessLaggyBoats plugin) {
        try {
            plugin.getConfig().save(plugin.getDataFolder() + File.separator + filename);
        } catch (IOException e) {
            plugin.log(Level.SEVERE, String.format("Failed to save %s: %s", filename, e.getMessage()));
        }
    }
}
