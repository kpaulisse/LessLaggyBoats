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

import java.util.ArrayList;

public class LagTracker extends ArrayList<Long> {
    private static final long serialVersionUID = 1L;
    private boolean useNano = false;
    private final int tickTrackerSize = 20;
    private int tickCounter = 0;

    public Double getTPS() {
        if (this.size() <= 5) {
            return -1.0D;
        }
        Long difference = this.get(this.size() - 1) - this.get(0);
        return ((double) this.size()) / (difference / (useNano ? 1000000000 : 1000));
    }

    public Double getCurrentLag() {
        if (this.size() <= 5) {
            return 1.0D;
        }
        Long difference = this.get(this.size() - 1) - this.get(0);
        Long timePerTick = difference / this.size();
        Long noLag = useNano ? 50000000L : 50L;
        if (timePerTick <= noLag) {
            return 1.0D;
        }
        return 1 + ((double) (timePerTick - noLag)/noLag);
    }

    public void updateLagTracker() {
        if (this.size() >= tickTrackerSize) {
            this.remove(0);
        }
        Long tickTime = useNano ? System.nanoTime() : System.currentTimeMillis();
        this.add(tickTime);
        tickCounter += 1;
        if (tickCounter >= 100) {
            tickCounter = 0;
        }
    }

    public void setUseNano(boolean useNano) {
        this.useNano = useNano;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("lag=").append(Double.toString(getCurrentLag()));
        sb.append(",tickTrackerSize=").append(Integer.toString(tickTrackerSize));
        sb.append(",useNano=").append(Boolean.toString(useNano));
        sb.append(",tableSize=").append(Integer.toString(this.size()));
        return sb.toString();
    }
}
