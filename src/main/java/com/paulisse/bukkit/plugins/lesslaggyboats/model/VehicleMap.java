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

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.entity.Vehicle;

public class VehicleMap extends HashMap<Vehicle, TrackedBoat> {
    private static final long serialVersionUID = 1L;

    /**
     * Generates a list of tracked boats in string format
     * @return List of tracked boats in string format
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d boat%s", this.size(), this.size() == 1 ? "" : "s"));
        if (this.size() > 0) {
            sb.append("[");
            Iterator<Vehicle> i = this.keySet().iterator();
            while (i.hasNext()) {
                Vehicle boat = i.next();
                sb.append(this.get(boat).toString());
                if (i.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
