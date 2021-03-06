// TouchSensor.java

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

import ch.aplu.jgamegrid.*;
import java.awt.*;
import javax.swing.JOptionPane;

/**
 * Class that represents a touch sensor.
 */
public class TouchSensor extends Part
{

  /**
   * Thread for the TouchSensor
   *
   * Alternative 1:
   *  An alternative for paralleling TouchSensor would be to implement the Runnable interface.
   *
   *  Advantages:
   *   1. Runnable consumes less memory than extending the Thread class. This is especially important on devices with little memory, like the EV3.
   *   2. By implementing the Runnable Interface, the class can extend an other class and implement other interfaces.
   *   3. Runnables can be represented using lambda streams, which increases flexibility and readability of the code.
   *
   *  Disadvantages:
   *   1. Doesn't have its own separated object.
   *
   * Alternative 2:
   *  By extending the full Thread class an independent thread for every TouchSensor could be created.
   *
   *  Advantages:
   *   1. By extending the class Thread each SensorThread is its own separate object.
   *
   *  Disadvantages:
   *   1. Creating a full thread consumes more memory which can be pretty important on smaller devices.
   *   2. By extending the class Thread, SensorThread can not extend any other class.
   */
  private class SensorThread extends Thread
  {
    public void run()
    {
      while (isRunning)
      {
        synchronized (monitor)
        {
          try
          {
            monitor.wait();
          }
          catch (InterruptedException ex)
          {
          }
        }
        if (!isPressNotified)
        {
          isPressNotified = true;
          touchListener.pressed(port);
        }
        if (!isReleaseNotified)
        {
          isReleaseNotified = true;
          touchListener.released(port);
        }
      }
      touchListener = null;
    }
  }
  // -------------- End of inner class ------------------
  //
  private static final Location pos1 = new Location(6, 10);
  private static final Location pos2 = new Location(6, -10);
  private static final Location pos3 = new Location(6, 0);
  private static final Location pos4 = new Location(-35, 0);
  private static final Point startPoint = new Point(5, -4);
  private static final Point endPoint = new Point(5, 4);
  private static final Point startPointRear = new Point(-5, -4);
  private static final Point endPointRear = new Point(-5, 4);
  private int nbObstacles = 0;
  private TouchListener touchListener = null;
  private SensorPort port;
  private Actor collisionActor = null;
  private final SensorThread st = new SensorThread();
  private volatile boolean isRunning = false;
  private volatile boolean isPressNotified = true;
  private volatile boolean isReleaseNotified = true;
  private final Object monitor = new Object();

  /**
   * Creates a sensor instance connected to the given port.
   * The port selection determines the position of the sensor:
   * S1: right; S2: left, S3: middle, S4: rear-middle.
   * @param port the port where the sensor is plugged-in
   */
  public TouchSensor(SensorPort port)
  {
    super("sprites/touchsensor"
      + (port == SensorPort.S1 ? ".gif"
      : (port == SensorPort.S2 ? ".gif"
      : (port == SensorPort.S3 ? ".gif" : "_rear.gif"))),
      port == SensorPort.S1 ? pos1
      : (port == SensorPort.S2 ? pos2
      : (port == SensorPort.S3 ? pos3 : pos4)));
    this.port = port;
    if (port == SensorPort.S4)
      setCollisionLine(startPointRear, endPointRear);
    else
      setCollisionLine(startPoint, endPoint);
  }

  protected void cleanup()
  {
    isRunning = false;
  }

  /**
   * Register the given TouchListener to detect press or release events.
   * Starts an internal sensor thread that polls the sensor level and runs the
   * sensor callbacks.
   * @param listener the LightListener to register; null, to terminate any running
   * sensor thread
   */
  public void addTouchListener(TouchListener listener)
  {
    if (listener != null)
    {
      if (touchListener == null)
      {
        isRunning = true;
        st.start();
      }
      touchListener = listener;
    }
    else
      isRunning = false;
  }

  /**
   * For internal use only (overrides Actor.act()).
   */
  public void act()
  {
    // Add new obstacles as collision actor
    int nb = RobotContext.obstacles.size();
    if (nb > nbObstacles)
    {
      for (int i = nb - 1; i >= nbObstacles; i--)
        addCollisionActor(RobotContext.obstacles.get(i));
      nbObstacles = nb;
    }

    if (touchListener != null)
    {
      if (collisionActor == null && isPressed())
      {
        isPressNotified = false;
        synchronized (monitor)
        {
          monitor.notify();
        }
      }
      if (collisionActor != null && !isPressed())
      {
        isReleaseNotified = false;
        synchronized (monitor)
        {
          monitor.notify();
        }
      }
    }
  }

  protected Actor getCollisionActor()
  {
    return collisionActor;
  }

  /**
   * Polls the touch sensor and returns true, if there is a collision
   * with any of the collision obstacles.
   * Calls Thread.sleep(1) to prevent CPU overload in short polling loops.
   * @return true, if the sensor is pressed; otherwise false
   */
  public boolean isPressed()
  {
    checkPart();
    Tools.delay(1);
    for (Actor a : RobotContext.obstacles)
    {
      if (gameGrid.isActorColliding(a, this))
      {
        collisionActor = a;
        return true;
      }
    }
    collisionActor = null;
    return false;
  }

  private void checkPart()
  {
    if (robot == null)
    {
      JOptionPane.showMessageDialog(null,
        "TouchSensor is not part of the LegoRobot.\n"
        + "Call addPart() to assemble it.",
        "Fatal Error", JOptionPane.ERROR_MESSAGE);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.TerminateOnClose
        || GameGrid.getClosingMode() == GameGrid.ClosingMode.AskOnClose)
        System.exit(1);
      if (GameGrid.getClosingMode() == GameGrid.ClosingMode.DisposeOnClose)
        throw new RuntimeException("TouchSensor is not part of the LegoRobot.\n"
          + "Call addPart() to assemble it.");
    }
  }
}
