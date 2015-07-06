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

package com.paulisse.bukkit.plugins.lesslaggyboats.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.LagTracker;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.TrackedBoat;
import com.paulisse.bukkit.plugins.lesslaggyboats.utils.BoatUtils;

public class BoatMoveListener implements Runnable, Listener {

    private LessLaggyBoats plugin;
    private HashMap<Vehicle, TrackedBoat> vehicleMap;

    public BoatMoveListener(LessLaggyBoats plugin) {
        this.plugin = plugin;
        this.vehicleMap = plugin.getVehicleMap();
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (plugin.getIsEnabled() == false) {
            return;
        }
        Vehicle vehicle = event.getVehicle();
        if (vehicle.getType() != EntityType.BOAT) {
            return;
        }
        Entity passenger = vehicle.getPassenger();
        if (passenger == null && !plugin.getTrackUnoccupiedBoats()) {
            return;
        }

        Boat boat = (Boat) event.getVehicle();
        if (vehicleMap.containsKey(boat)) {
            TrackedBoat trackedBoat = vehicleMap.get(boat);
            calculateBoatPosition(trackedBoat, plugin.getLagTracker());
            if (trackedBoat.getDistance() > (trackedBoat.getAdjustedVelocity() * plugin.getTeleportDistanceThreshold())) {
                BoatUtils.moveBoat(trackedBoat, plugin);
            }
        } else {
            plugin.startTrackingBoat(new TrackedBoat(boat, "boat" + plugin.getBoatCounter().toString()));
        }
    }

    /**
     * Calculate the position of the moved boat
     * @param trackedBoat The tracked boat
     * @param lagTracker The lag tracker
     */
    public void calculateBoatPosition(TrackedBoat trackedBoat, LagTracker lagTracker) {
        if (lagTracker == null) {
            trackedBoat.setCalculatedLocation(trackedBoat.getBoat().getLocation().clone());
            trackedBoat.setCalculatedVelocity(trackedBoat.getBoatVelocity().clone());
            trackedBoat.setLastTickCounter(0);
            return;
        }

        Double currentLag = lagTracker.getCurrentLag();
        if (currentLag <= 1.0D) {
            currentLag = 1.0D;
        }

        Boat boat = trackedBoat.getBoat();
        int tickCounterTracked = trackedBoat.getLastTickCounter();
        int tickCounter = lagTracker.getTickCounter();
        int multiplier = 1;
        if (tickCounterTracked > -1) {
            if (tickCounter >= tickCounterTracked) {
                multiplier = tickCounter - tickCounterTracked;
            } else {
                multiplier = 100 - tickCounterTracked + tickCounter;
            }
        }
        trackedBoat.setLastTickCounter(tickCounter);

        Vector calculatedVelocity = boat.getVelocity().clone();
        calculatedVelocity.multiply(currentLag * multiplier);
        calculatedVelocity.setY(boat.getVelocity().getY());
        Location calculatedLocation = trackedBoat.getCalculatedLocation();
        calculatedLocation.add(calculatedVelocity);
        calculatedLocation.setY(boat.getLocation().getY());
        trackedBoat.setCalculatedLocation(calculatedLocation);
    }

    @Override
    public void run() {

    }
}
