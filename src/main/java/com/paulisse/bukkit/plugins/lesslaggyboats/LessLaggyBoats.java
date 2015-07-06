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

package com.paulisse.bukkit.plugins.lesslaggyboats;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Vehicle;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.paulisse.bukkit.plugins.lesslaggyboats.handlers.CommandHandler;
import com.paulisse.bukkit.plugins.lesslaggyboats.handlers.TickHandler;
import com.paulisse.bukkit.plugins.lesslaggyboats.listeners.BoatDestroyListener;
import com.paulisse.bukkit.plugins.lesslaggyboats.listeners.BoatExitListener;
import com.paulisse.bukkit.plugins.lesslaggyboats.listeners.BoatMoveListener;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.LagTracker;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.TrackedBoat;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.VehicleMap;
import com.paulisse.bukkit.plugins.lesslaggyboats.utils.ConfigFileManager;

public class LessLaggyBoats extends JavaPlugin {

    // Configurable settings
    private boolean teleportDuringTravel;
    private boolean teleportUponExit;
    private double teleportDistanceThreshold;
    private boolean trackUnoccupiedBoats;
    private boolean enabled = false;
    private boolean nanoseconds = false;

    // Lag calculation
    private LagTracker lagTracker;
    private BukkitScheduler scheduler;

    // Event handlers and listeners
    private PluginManager pluginManager;
    private TickHandler tickHandler;
    private BoatDestroyListener boatDestroyListener;
    private BoatExitListener boatExitListener;
    private BoatMoveListener boatMoveListener;
    private int tickHandlerTaskId = 0;

    // Entity tracking
    private VehicleMap vehicleMap;
    private Long boatCounter = 0L;

    /**
     * Command handler
     * @return Command successful or not
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return new CommandHandler(this, sender).handle(cmd, label, args);
    }

    /**
     * Disable plugin handler
     */
    @Override
    public void onDisable() {
        this.enabled = false;
        destroyEventListeners();
        destroyLagTracker();
        initializeVehicleMap();
    }

    /**
     * Enable plugin handler
     */
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        ConfigFileManager.setValuesFromConfig(this);
        this.enabled = true;
        this.scheduler = getServer().getScheduler();
        this.pluginManager = getServer().getPluginManager();
        initializeVehicleMap();
        initializeEventListeners();
    }

    /**
     * Tear down event listeners
     */
    private void destroyEventListeners() {
        this.boatDestroyListener = null;
        this.boatExitListener = null;
        this.boatMoveListener = null;
    }

    /**
     * Tear down lag tracker
     */
    private void destroyLagTracker() {
        if (this.tickHandlerTaskId > 0) {
            scheduler.cancelTask(this.tickHandlerTaskId);
            this.tickHandlerTaskId = 0;
        }
        this.tickHandler = null;
        this.setLagTracker(null);
    }

    /**
     * @return the boatCounter
     */
    public Long getBoatCounter() {
        this.boatCounter += 1;
        return boatCounter;
    }

    /**
     * Get boat move listener
     * @return Boat move listener
     */
    public BoatMoveListener getBoatMoveListener() {
        return boatMoveListener;
    }

    /**
     * True if the plugin is currently enabled
     * @return True if the plugin is currently enabled
     */
    public boolean getIsEnabled() {
        return this.enabled;
    }

    /**
     * @return the lagTracker
     */
    public LagTracker getLagTracker() {
        return lagTracker;
    }

    /**
     * @return the teleportDistanceThreshold
     */
    public double getTeleportDistanceThreshold() {
        return teleportDistanceThreshold;
    }

    /**
     * @return the trackUnoccupiedBoats
     */
    public boolean getTrackUnoccupiedBoats() {
        return trackUnoccupiedBoats;
    }

    /**
     * @return the vehicleMap
     */
    public VehicleMap getVehicleMap() {
        if (vehicleMap == null) {
            initializeVehicleMap();
        }
        return vehicleMap;
    }

    /**
     * Initialize event listeners
     */
    private void initializeEventListeners() {
        this.boatDestroyListener = new BoatDestroyListener(this);
        pluginManager.registerEvents(this.boatDestroyListener, this);
        this.boatExitListener = new BoatExitListener(this);
        pluginManager.registerEvents(this.boatExitListener, this);
        this.boatMoveListener = new BoatMoveListener(this);
        pluginManager.registerEvents(this.boatMoveListener, this);
    }

    /**
     * Initialize vehicle map
     */
    private void initializeVehicleMap() {
        if (vehicleMap == null) {
            vehicleMap = new VehicleMap();
        } else {
            vehicleMap.clear();
        }
        boatCounter = 0L;
    }

    /**
     * @return the nanoseconds
     */
    public boolean getNanoseconds() {
        return nanoseconds;
    }

    /**
     * @return the teleportDuringTravel
     */
    public boolean getTeleportDuringTravel() {
        return teleportDuringTravel;
    }

    /**
     * @return the teleportUponExit
     */
    public boolean getTeleportUponExit() {
        return teleportUponExit;
    }

    public void log(Level level, String message) {
        this.getLogger().log(level, message);
    }


    /**
     * @param lagTracker the lagTracker to set
     */
    public void setLagTracker(LagTracker lagTracker) {
        this.lagTracker = lagTracker;
    }

    /**
     * @param nanoseconds the nanoseconds to set
     */
    public void setNanoseconds(Boolean nanoseconds) {
        if (nanoseconds != this.nanoseconds) {
            this.getLagTracker().clear();
            this.nanoseconds = nanoseconds;
            ConfigFileManager.saveConfigValue(this, "nanoseconds", nanoseconds);
        }
    }

    /**
     * @param teleportDistanceThresold the teleportDistanceThresold to set
     */
    public void setTeleportDistanceThreshold(Double teleportDistanceThreshold) {
        this.teleportDistanceThreshold = teleportDistanceThreshold;
        ConfigFileManager.saveConfigValue(this, "teleport-distance-threshold", teleportDistanceThreshold);
    }

    /**
     * @param teleportDuringTravel the teleportDuringTravel to set
     */
    public void setTeleportDuringTravel(Boolean teleportDuringTravel) {
        this.teleportDuringTravel = teleportDuringTravel;
        ConfigFileManager.saveConfigValue(this, "teleport-during-travel", teleportDuringTravel);
    }

    /**
     * @param teleportUponExit the teleportUponExit to set
     */
    public void setTeleportUponExit(Boolean teleportUponExit) {
        this.teleportUponExit = teleportUponExit;
        ConfigFileManager.saveConfigValue(this, "teleport-upon-exit", teleportUponExit);
    }

    /**
     * @param trackUnoccupiedBoats the trackUnoccupiedBoats to set
     */
    public void setTrackUnoccupiedBoats(Boolean trackUnoccupiedBoats) {
        this.trackUnoccupiedBoats = trackUnoccupiedBoats;
        ConfigFileManager.saveConfigValue(this, "track-unoccupied-boats", trackUnoccupiedBoats);
        if (trackUnoccupiedBoats == false) {
            Set<Vehicle> removeSet = new HashSet<Vehicle>();
            VehicleMap v = this.getVehicleMap();
            for (Vehicle boat : v.keySet()) {
                if (boat.getPassenger() == null) {
                    removeSet.add(boat);
                }
            }
            for (Vehicle boat : removeSet) {
                v.remove(boat);
            }
        }
    }

    /**
     * Run the lag tracker
     */
    private void startLagTracker() {
        if (this.tickHandlerTaskId > 0) {
            return;
        }
        LagTracker lagTracker = new LagTracker();
        lagTracker.setUseNano(nanoseconds);
        this.setLagTracker(lagTracker);
        this.tickHandler = new TickHandler(this);
        this.tickHandlerTaskId = scheduler.scheduleSyncRepeatingTask(this, tickHandler, 0L, 1L);
     }

    public void startTrackingBoat(TrackedBoat trackedBoat) {
        startLagTracker();
        if (vehicleMap.containsKey(trackedBoat.getBoat())) {
            return;
        }
        vehicleMap.put(trackedBoat.getBoat(), trackedBoat);
    }

    public void stopTrackingBoat(Vehicle vehicle) {
        vehicleMap.remove(vehicle);
        if (vehicleMap.size() == 0) {
            destroyLagTracker();
        }
    }
}
