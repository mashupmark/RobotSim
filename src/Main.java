import ch.aplu.robotsim.*;
import ch.aplu.util.QuitPane;

public class Main {
    static {
        RobotContext.setLocation(10, 10);
        RobotContext.setStartDirection(5);
        RobotContext.setStartPosition(100, 240);
        RobotContext.useObstacle(RobotContext.channel);
    }

    static public void main(String[] args) {
        LegoRobot robot = new LegoRobot();
        Gear gear = new Gear();
        robot.addPart(gear);
        TouchSensor ts1 = new TouchSensor(SensorPort.S1); // right sensor
        TouchSensor ts2 = new TouchSensor(SensorPort.S2); // left sensor
        robot.addPart(ts1);
        robot.addPart(ts2);
        gear.forward();

        while (!QuitPane.quit()) {
            boolean t1 = ts1.isPressed();
            boolean t2 = ts2.isPressed();

            if (t1 && t2) {
                gear.backward(500);
                gear.left(400);
                gear.forward();
            } else if (t1) {
                gear.backward(500);
                gear.left(400);
                gear.forward();
            } else if (t2) {
                gear.backward(500);
                gear.right(100);
                gear.forward();
            }

            Tools.delay(20);
        }
        robot.exit();
        System.exit(0);
    }
}