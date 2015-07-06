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

import com.paulisse.bukkit.plugins.lesslaggyboats.LessLaggyBoats;
import com.paulisse.bukkit.plugins.lesslaggyboats.model.LagTracker;

public class TickHandler implements Runnable {

    private final LessLaggyBoats plugin;
    private LagTracker lagTracker;

    public TickHandler(LessLaggyBoats lessLaggyBoats) {
        this.plugin = lessLaggyBoats;
        this.lagTracker = plugin.getLagTracker();
    }

    @Override
    public void run() {
        if (plugin.getIsEnabled() == false) {
            return;
        }
        lagTracker.updateLagTracker();
    }
}
