package robot;

public class RobotConstants {
    public static final int START_ROW = 1;                          // row no. of start cell
    public static final int START_COL = 1;                          // col no. of start cell
    public static final int MOVE_COST = 10;                         // cost of FORWARD, BACKWARD movement
    public static final int TURN_COST = 20;                         // cost of RIGHT, LEFT movement
    public static final int TURN_RADIUS = 3;
    public static final int SPEED = 100;                            // delay between movements (ms)
    public static final int INFINITE_COST = 99999;
    public static final int SENSOR_SHORT_RANGE_L = 1;               // range of short range sensor (cells)
    public static final int SENSOR_SHORT_RANGE_H = 2;               // range of short range sensor (cells)
    public static final int SENSOR_LONG_RANGE_L = 1;                // range of long range sensor (cells)
    public static final int SENSOR_LONG_RANGE_H = 4;                // range of long range sensor (cells)
    public static final int START_DIR = 1;      					// start direction
    // DIRECTION: 1 - North || 2 - East || 3 - South || 4 - West || 5 - Calibrate
    
    public static final char FORWARD = 'W';
    public static final char BACKWARD = 'S';
    public static final char LEFT_FORWARD = 'A';
    public static final char RIGHT_FORWARD = 'D';
    public static final char LEFT_BACKWARD = 'Z';
    public static final char RIGHT_BACKWARD = 'C';
    public static final char STOP = 'H';
    public static final char UNIDENTIFIED = 'U';
}
