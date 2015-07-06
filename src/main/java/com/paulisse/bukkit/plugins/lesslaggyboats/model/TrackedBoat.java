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

import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class TrackedBoat {

    private Boat boat;
    private Location calculatedLocation;
    private Vector calculatedVelocity;
    private boolean isTeleportingNow = false;
    private int lastTickCounter = -1;
    private String boatName;
    private int teleportCounter;

    /**
     * Constructor
     * @param boat Boat entity
     * @param boatName Name for the boat
     */
    public TrackedBoat(Boat boat, String boatName) {
        this.setBoat(boat);
        this.setCalculatedLocation(boat.getLocation().clone());
        this.setCalculatedVelocity(boat.getVelocity().clone());
        this.setBoatName(boatName);
        this.setTeleportCounter(0);
    }

    /**
     * Returns the calculated location of the boat
     * @return Location
     */
    public Location getCalculatedLocation() {
        return calculatedLocation.clone();
    }

    /**
     * Sets the calculated location of the boat
     * @param calculatedLocation Calculated location of the boat
     */
    public void setCalculatedLocation(Location calculatedLocation) {
        this.calculatedLocation = calculatedLocation;
    }

    /**
     * Returns true if the tracked boat is currently teleporting
     * @return isTeleportingNow
     */
    public boolean isTeleportingNow() {
        return isTeleportingNow;
    }

    /**
     * Set the current teleporting setting of the tracked boat
     * @param isTeleportingNow
     */
    public void setTeleportingNow(boolean isTeleportingNow) {
        this.isTeleportingNow = isTeleportingNow;
    }

    /**
     * Returns the underlying boat object
     * @return boat
     */
    public Boat getBoat() {
        return boat;
    }

    /**
     * Sets the underlying boat object
     * @param newBoat the boat object
     */
    public void setBoat(Boat newBoat) {
        this.boat = newBoat;
    }

    /**
     * Return the distance between calculated position and server position
     * @return distance
     */
    public Double getDistance() {
        return this.calculatedLocation.distance(boat.getLocation());
    }

    /**
     * @return the unadjustedVelocity
     */
    public Vector getBoatVelocity() {
        return boat.getVelocity().clone();
    }

    /**
     * @return the unadjustedVelocity
     */
    public void setBoatVelocity(Vector v) {
        boat.setVelocity(v);
    }

    /**
     * @return the calculatedVelocity
     */
    public Vector getCalculatedVelocity() {
        return calculatedVelocity;
    }

    /**
     * @param calculatedVelocity the calculatedVelocity to set
     */
    public void setCalculatedVelocity(Vector calculatedVelocity) {
        this.calculatedVelocity = calculatedVelocity;
    }

    /**
     *
     */
    public Double getAdjustedVelocity() {
        return Math.pow(1+calculatedVelocity.getX(), 2) + Math.pow(1+calculatedVelocity.getY(), 2) + Math.pow(1+calculatedVelocity.getZ(), 2);
    }

    /**
     * @return the lastTickCounter
     */
    public int getLastTickCounter() {
        return lastTickCounter;
    }

    /**
     * @param lastTickCounter the lastTickCounter to set
     */
    public void setLastTickCounter(int lastTickCounter) {
        this.lastTickCounter = lastTickCounter;
    }

    /**
     * Validate the boat
     * @return True if boat is valid
     */
    public boolean validate(Boolean trackUnoccupiedBoats) {
        // If boat is null, it's not valid
        if (boat == null) {
            return false;
        }
        // If boat is not in this world, it's not valid
        World world = boat.getWorld();
        Collection<Boat> boats = world.getEntitiesByClass(Boat.class);
        if (boats == null || ! boats.contains(boat)) {
            return false;
        }
        // Validate boat passenger - passenger is in the vehicle
        Entity p = boat.getPassenger();
        if (p != null) {
            Entity v = p.getVehicle();
            if (v != boat) {
                return false;
            }
        } else if (trackUnoccupiedBoats == false) {
            return false;
        }
        return true;
    }

    /**
     * @return the boatName
     */
    public String getBoatName() {
        return boatName;
    }

    /**
     * @param boatName Name of boat
     */
    public void setBoatName(String boatName) {
        this.boatName = boatName;
    }

    /**
     * @return the teleportCounter
     */
    public int getTeleportCounter() {
        return teleportCounter;
    }

    /**
     * Set teleport counter
     * @param New teleport counter value
     */
    private void setTeleportCounter(int teleportCounter) {
        this.teleportCounter = teleportCounter;
    }

    /**
     * Increase the teleport counter by 1
     */
    public void incrementTeleportCounter() {
        this.setTeleportCounter(this.getTeleportCounter()+1);
    }
}
