// LightAdapter.java

/*
This software is part of the RobotSim library.
It is Open Source Free Software, so you may
- run the code for any purpose
- study how the code works and adapt it to your needs
- integrate all or parts of the code in your own programs
- redistribute copies of the code
- improve the code and release your improvements to the public
However the use of the code is entirely your responsibility.

Author: Aegidius Pluess, www.aplu.ch
*/

package ch.aplu.robotsim;

/**
 * Class with empty callback methods for the light sensor.
 */
public class LightAdapter implements LightListener {
    /**
     * Empty method called when the light becomes brighter than the trigger level.
     * Override it to process the event.
     *
     * @param port  the port where the sensor is plugged in
     * @param value the current light level.
     */
    public void bright(SensorPort port, int value) {
    }

    /**
     * Empty method called when the light becomes darker than the trigger level.
     * Override it to process the event.
     *
     * @param port  the port where the sensor is plugged in
     * @param value the current light value
     */
    public void dark(SensorPort port, int value) {
    }
}

