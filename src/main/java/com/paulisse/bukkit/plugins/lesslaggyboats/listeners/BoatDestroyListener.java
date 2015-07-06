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

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.TrackedBoat;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.VehicleMap;

public class BoatDestroyListener implements Runnable, Listener {

    private LessLaggyBoats plugin;
    private VehicleMap vehicleMap;

    public BoatDestroyListener(LessLaggyBoats lessLaggyBoats)  {
        this.plugin = lessLaggyBoats;
        this.vehicleMap = plugin.getVehicleMap();
    }

    @EventHandler
    public void onVehicleDestroyed(VehicleDestroyEvent event) {
        if (plugin.getIsEnabled() == false) {
            return;
        }
        if (event.getVehicle().getType() != EntityType.BOAT) {
            return;
        }
        TrackedBoat trackedBoat = vehicleMap.get(event.getVehicle());
        if (trackedBoat != null && ! trackedBoat.isTeleportingNow()) {
            plugin.stopTrackingBoat(event.getVehicle());
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

}
