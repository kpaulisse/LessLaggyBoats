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

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.TrackedBoat;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.VehicleMap;
import com.paulisse.bukkit.plugins.lesslaggyboats.utils.BoatUtils;

public class BoatExitListener implements Runnable, Listener {

    private LessLaggyBoats plugin;
    private VehicleMap vehicleMap;

    public BoatExitListener(LessLaggyBoats lessLaggyBoats)  {
        this.plugin = lessLaggyBoats;
        this.vehicleMap = plugin.getVehicleMap();
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (plugin.getIsEnabled() == false) {
            return;
        }
        TrackedBoat trackedBoat = vehicleMap.get(event.getVehicle());
        if (trackedBoat != null && ! trackedBoat.isTeleportingNow()) {
            Boat boat = trackedBoat.getBoat();
            Location calculatedLocation = trackedBoat.getCalculatedLocation();
            Location teleportLocation = BoatUtils.getLiquidTargetBlock(trackedBoat.getBoat().getWorld(), boat.getLocation(), calculatedLocation);

            // If there is a passenger, cancel the vehicle exit and teleport to calculated location
            Entity passenger = boat.getPassenger();
            if (passenger == null) {
                // Weird, an exit event with no passenger
                return;
            }

            trackedBoat.setTeleportingNow(true);
            calculatedLocation.setYaw(passenger.getLocation().getYaw());
            calculatedLocation.setPitch(passenger.getLocation().getPitch());
            passenger.setVelocity(new Vector(0.0D, 0.0D, 0.0D));
            passenger.teleport(teleportLocation);
            trackedBoat.setCalculatedLocation(calculatedLocation);
            trackedBoat.setCalculatedVelocity(new Vector(0.0D, 0.0D, 0.0D));
            trackedBoat.setTeleportingNow(false);

            // Spawn a new, clean, unmoving boat at the calculated location
            BoatUtils.replaceBoat(trackedBoat, plugin, new Vector(0.0D, 0.0D, 0.0D));

            // This boat is no longer occupied. Stop tracking if we are not tracking unoccupied boats.
            if (! plugin.getTrackUnoccupiedBoats()) {
                plugin.stopTrackingBoat(trackedBoat.getBoat());
                trackedBoat = null;
            }
        }
    }

    @Override
    public void run() { }

}
