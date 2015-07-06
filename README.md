# LessLaggyBoats [![Build Status](https://drone.io/github.com/kpaulisse/LessLaggyBoats/status.png)](https://drone.io/github.com/kpaulisse/LessLaggyBoats/latest)

**Minecraft (Spigot) plugin to make boat travel possible even on laggy servers**

Download: [Builds](https://drone.io/github.com/kpaulisse/LessLaggyBoats/files)

Author: [Kevin Paulisse](https://github.com/kpaulisse)

Please report bugs or make suggestions: [Issue Tracker](https://github.com/kpaulisse/LessLaggyBoats/issues)

Tested on: Spigot 1.8.7, Java 8

## Introduction

In Minecraft, the motion of boats is calculated on the client. When the server is running at 20 ticks per second, the server's calculation of the boat position is accurate. When the you leave the boat, you are standing (or swimming) at the position where the boat was. When your boat hits the shore or an object, it breaks. This is the correct behavior.

When the server has lag (i.e., it is running at less than 20 ticks per second), the server's knowledge of the boat position begins to diverge from the client. The more laggy the server, and the longer the boat ride, the more divergence occurs. When this happens, unexpected behavior ensues. You may eject from your boat only to find yourself in the middle of the ocean, or your boat may randomly break.

The approach taken by the popular [ToughBoats plugin](http://dev.bukkit.org/bukkit-plugins/toughboats/) is to synchronize the server and client every 60 seconds by teleporting the boat to the location where the server thinks it is. On a laggy server, this results in a journey where (for example) your boat travels 100 blocks, then is teleported 50 blocks back, travels another 100 blocks, is teleported 50 blocks back, and so on.

The "LessLaggyBoats" plug-in works differently. This plug-in estimates the position for the boat based on the velocity and the server's lag. When the server position and the estimated position diverge, the server teleports the boat to the *estimated* position. In my tests, the estimated position has been very close (within a block or two) of the position displayed on the client. Therefore, instead of jumping around, the journey is smooth, with only an occasional glitch when the teleport occurs.

## Commands

### Reload

    llb reload

Re-reads the configuration file, re-initializes all tracking data and lag calculations.

### Enable

    llb enable

Enables the plug-in.

### Disable

    llb disable

Disables the plug-in.

### Display configuration

    llb config

Displays the current run-time configuration.

### Set configuration value

    llb set teleport-distance-threshold #.#
    llb set teleport-during-travel true/false
    llb set teleport-upon-exit true/false
    llb set track-unoccupied-boats true/false
    llb set nanoseconds true/false

See the parameter descriptions below.

### List tracked boats

    llb list
    listboats

Lists boats being tracked, with their position, offset (distance divergence between server position and calculated position), and occupant.

### Show current lag

    llb lag
    showlag

Shows current ticks per second of the server, calculated on a 1-second rolling interval. 20 ticks per second indicates no lag.

## Parameters

| Parameter Name  | Data Type  | Default  | Description  |
|-----------------|------------|----------|--------------|
| `teleport-distance-threshold` | Numeric (float) | 3.0 | Determines when to synchronize client and server position, based on distance and velocity. See further description below in algorithm section. |
| `teleport-during-travel` | Boolean | true | Whether or not to synchronize client and server position during travel. |
| `teleport-upon-exit` | Boolean | true | Whether to teleport the player to the calculated location when the player exits the boat. |
| `track-unoccupied-boats` | Boolean | true | Whether to track (and correct position of) boats that do not have an entity in them. |
| `nanoseconds` | Boolean | false | Whether to use nanosecond resolution when calculating lag, instead of millisecond resolution. In all my tests, millisecond resolution was plenty accurate. |

## Algorithm

The following assumptions are made:

* The client displays boat motion based on an assumption that the server is running at 20 ticks/second
* Boats can travel on water but not land

The algorithm calculates the position of the boat by multiplying the velocity of the boat by an adjustment based on the server's current lag.

    calculatedVelocity = boatVelocity * (1 + (milliseconds/20 - 50)/50 )

Where:

* `boatVelocity` is the "server" knowledge of the boat velocity
* `milliseconds` is the number of milliseconds required for the last 20 ticks
* 50 comes from the no-lag situation, 1000 milliseconds/20 ticks = 50 milliseconds/tick

If the server has no lag, `milliseconds` = 1000, and the formula reduces to:

    calculatedVelocity = boatVelocity * ( 1 + (1000/20 - 50)/50 )
    calculatedVelocity = boatVelocity * ( 1 + (50-50)/50 )
    calculatedVelocity = boatVelocity * 1
    calculatedVelocity = boatVelocity

If the server is operating at 10 ticks per second, it takes 2000 milliseconds for 20 ticks. Then:

    calculatedVelocity = boatVelocity * ( 1 + (2000/20 - 50)/50 )
    calculatedVelocity = boatVelocity * ( 1 + (100-50)/50 )
    calculatedVelocity = boatVelocity * ( 1 + 1 )
    calculatedVelocity = boatVelocity * 2

The calculated distance is incremented by "distance = rate times time":

    calculatedDistance = calculatedDistance + (ticks * calculatedVelocity)

Where `ticks` is the number of ticks that have elapsed between events. (One might think that because the boat is moving steadily, the `onMove` event fires once per tick. However on laggy servers this is not the case. Sometimes the event fires once every 2 or 3 ticks.)

The offset between the calculated distance and the server's distance is calculated by the pythagorean theorem:

    offset = sqrt( (calculatedDistance.x - serverDistance.x)^2 + (calculatedDistance.z - serverDistance.z)^2 )

In an effort to make the boat teleport less frequently the slower it is moving, the "adjusted velocity" for teleport purposes is calculated as follows:

    adjustedVelocity = (1 + calculatedVelocity.x)^2 + (1 + calculatedVelocity.z)^2

The teleport takes place when the following is true:

    offset > adjustedVelocity * teleport-distance-threshold

Since the teleport causes a "glitch" on the screen, you don't want to do it too frequently so you do not annoy the user. But at the same time you want to do it frequently enough that the positions are sufficiently synchronized. I have found a value of 3.0 for the `teleport-distance-threshold` works well for me. This is configurable.

## Limitations

* This plug-in will probably not work well with boats going up or down, because it does not "correct" the Y-coordinate.
