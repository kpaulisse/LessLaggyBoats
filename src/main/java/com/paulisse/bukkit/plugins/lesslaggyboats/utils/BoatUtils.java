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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

public class BoatUtils {

    /**
     * Move a boat to a new location
     * @param boat Boat that needs to be moved
     * @param toLocation Location to move the boat to
     * @param velocity Initial apparent velocity for the boat
     * @return Newly created boat object
     */
    public static void moveBoat(TrackedBoat trackedBoat, LessLaggyBoats plugin) {
        Vehicle boat = trackedBoat.getBoat();
        Location oldLocation = boat.getLocation().clone();
        Location toLocation = trackedBoat.getCalculatedLocation();
        Location passengerLocation = toLocation.clone();

        // Set yaw, pitch, and direction to match passenger if there is one
        Player passenger = (Player) boat.getPassenger();
        if (passenger != null) {
            passengerLocation.setYaw(passenger.getLocation().getYaw());
            passengerLocation.setPitch(passenger.getLocation().getPitch());
        }
        toLocation.setYaw(boat.getLocation().getYaw());
        toLocation.setPitch(boat.getLocation().getPitch());

        // Avoid onVehicleExit() listener teleporting the player
        if (passenger != null) {
            trackedBoat.setTeleportingNow(true);
            passenger.leaveVehicle();
        }

        // Teleport boat to the new location
        Vector fakeBoatVelocity = trackedBoat.getCalculatedVelocity().clone();
        Vector realBoatVelocity = trackedBoat.getBoat().getVelocity().clone();
        toLocation.setY(trackedBoat.getBoat().getLocation().getY());
        World world = boat.getWorld();
        Location boatSpawnLocation = getLiquidTargetBlock(world, oldLocation, toLocation);
        boat.teleport(boatSpawnLocation);
        trackedBoat.incrementTeleportCounter();

        // Put the passenger in the new boat
        if (passenger != null) {
            passenger.teleport(passengerLocation);
            passenger.setVelocity(fakeBoatVelocity);
            boat.setPassenger(passenger);
            trackedBoat.setTeleportingNow(false);
        }

        // Set the boat velocity back to what the server thinks it is
        boat.setVelocity(realBoatVelocity);
    }

    /**
     * Respawn a new boat at a given location and start tracking it
     * @param trackedBoat Tracked boat
     * @param plugin Main plugin
     * @param vel Velocity of spawned boat
     */
    public static void replaceBoat(TrackedBoat trackedBoat, LessLaggyBoats plugin, Vector vel) {
        Boat oldBoat = trackedBoat.getBoat();
        Location location = oldBoat.getLocation().clone();
        World world = trackedBoat.getBoat().getWorld();
        plugin.getVehicleMap().remove(oldBoat);
        oldBoat.remove();
        Boat newBoat = world.spawn(location, Boat.class);
        newBoat.setVelocity(vel);
        trackedBoat.setBoat(newBoat);
        plugin.getVehicleMap().put(newBoat, trackedBoat);
    }

    /**
     * Checks for solid blocks between current location and target location
     * @param currentLocation Location teleporting from
     * @param toLocation Location teleporting to
     * @return Closest water block location to toLocation
     */
    public static Location getLiquidTargetBlock(World world, Location currentLocation, Location toLocation) {
        Double dx = toLocation.getX() - currentLocation.getX();
        Double dz = toLocation.getZ() - currentLocation.getZ();
        Double distance = Math.sqrt(dx*dx + dz*dz);
        Location returnLocation = currentLocation.clone();
        returnLocation.setPitch(toLocation.getPitch());
        returnLocation.setYaw(toLocation.getYaw());
        for (int i = 0; i <= distance; i++) {
            Block block = world.getBlockAt(returnLocation);
            if (!block.isLiquid()) {
                returnLocation.add(-dx/distance, 0, -dz/distance);
                return returnLocation;
            }
            returnLocation.add(dx/distance, 0, dz/distance);
        }
        return toLocation;
    }

    /**
     * Clean the vehicle map
     */
    public static void cleanVehicleMap(LessLaggyBoats plugin) {
        VehicleMap v = plugin.getVehicleMap();
        Iterator<Entry<Vehicle, TrackedBoat>> i = v.entrySet().iterator();
        Set<Vehicle> toRemove = new HashSet<Vehicle>();
        while (i.hasNext()) {
            Entry<Vehicle, TrackedBoat> x = i.next();
            Vehicle boat = x.getKey();
            TrackedBoat trackedBoat = x.getValue();
            if (!trackedBoat.validate(plugin.getTrackUnoccupiedBoats())) {
                toRemove.add(boat);
            }
        }
        for (Vehicle boat : toRemove) {
            v.remove(boat);
        }
    }
}
