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

package com.paulisse.bukkit.plugins.lesslaggyboats.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;

public enum LessLaggyBoatsParameters {
    TELEPORT_DISTANCE_THRESHOLD("teleport-distance-threshold", 3.0D, "setTeleportDistanceThreshold", "getTeleportDistanceThreshold"),
    TELEPORT_DURING_TRAVEL("teleport-during-travel", true, "setTeleportDuringTravel", "getTeleportDuringTravel"),
    TELEPORT_UPON_EXIT("teleport-upon-exit", true, "setTeleportUponExit", "getTeleportUponExit"),
    TRACK_UNOCCUPIED_BOATS("track-unoccupied-boats", false, "setTrackUnoccupiedBoats", "getTrackUnoccupiedBoats"),
    NANOSECONDS("nanoseconds", false, "setNanoseconds", "getNanoseconds");

    private String name;
    private Double defaultDoubleValue;
    private Boolean defaultBooleanValue;
    private Class<?> clazz;
    private Method setterMethod;
    private Method getterMethod;

    private LessLaggyBoatsParameters(String name, Boolean defaultBooleanValue, String setterMethodName, String getterMethodName) {
        this.name = name;
        this.defaultBooleanValue = defaultBooleanValue;
        this.clazz = Boolean.class;
        try {
            setterMethod = (LessLaggyBoats.class).getMethod(setterMethodName, clazz);
            getterMethod = (LessLaggyBoats.class).getMethod(getterMethodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Reflection error: " + e.getMessage());
        }
    }

    private LessLaggyBoatsParameters(String name, Double defaultDoubleValue, String setterMethodName, String getterMethodName) {
        this.name = name;
        this.defaultDoubleValue = defaultDoubleValue;
        this.clazz = Double.class;
        try {
            setterMethod = (LessLaggyBoats.class).getMethod(setterMethodName, clazz);
            getterMethod = (LessLaggyBoats.class).getMethod(getterMethodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Reflection error: " + e.getMessage());
        }
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Boolean getDefault(Boolean x) {
        return defaultBooleanValue;
    }

    public Double getDefault(Double x) {
        return defaultDoubleValue;
    }

    public String getName() {
        return name;
    }

    public Boolean getParameter(LessLaggyBoats plugin, Boolean x) {
        try {
            return (Boolean) getterMethod.invoke(plugin);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            plugin.log(Level.SEVERE, "Failure getting configuration value " + this.toString() + ": " + e.getMessage());
        }
        return false;
    }

    public Double getParameter(LessLaggyBoats plugin, Double x) {
        try {
            return (Double) getterMethod.invoke(plugin);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            plugin.log(Level.SEVERE, "Failure getting configuration value " + this.toString() + ": " + e.getMessage());
        }
        return 0.0D;
    }

    public boolean setParameter(LessLaggyBoats plugin, Boolean x) {
        try {
            setterMethod.invoke(plugin, x);
            return true;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            plugin.log(Level.SEVERE, "Failure setting configuration value " + this.toString() + ": " + e.getMessage());
        }
        return false;
    }

    public boolean setParameter(LessLaggyBoats plugin, Double x) {
        try {
            setterMethod.invoke(plugin, x);
            return true;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            plugin.log(Level.SEVERE, "Failure setting configuration value " + this.toString() + ": " + e.getMessage());
        }
        return false;
    }
}
