package robot;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Robot {
	// Center cell
	// Will always be calculated to integer value
	private double col; // col
	private double row; // row
	
	private int direction; 	// 90 - North || 0 - East || -90 - South || 180 - West	
	
	private int speed = 100;	
	private boolean isRealBot;
	
	public Robot(double row, double col, boolean realBot) {
		this.row = row;
		this.col = col;
		this.isRealBot = realBot;
		this.direction = 90; // N
		
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public double getRow() {
		return this.row;
	}
	
	public double getCol() {
		return this.col;
	}
	
	public boolean getRealBot() {
		return this.isRealBot;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
	
	/**
	 * Set robot center to position (row, col)
	 * @param row
	 * @param col
	 */
	public void setRobotPos(double row, double col) {
		this.row = row;
		this.col = col;
	}
	
	public void setRealBot(boolean realBot) {
		this.isRealBot = realBot;
	}
	
	/**
	 * Convert the movement from integer to words
	 */
	public String movementToWords(char movement) {
		String m = "";
		if (movement == RobotConstants.FORWARD)				m = "Forward";
		else if (movement == RobotConstants.BACKWARD) 		m = "Backward";
		else if (movement == RobotConstants.LEFT_FORWARD) 	m = "LeftForward";
		else if (movement == RobotConstants.RIGHT_FORWARD) 	m = "RightForward";
		else if (movement == RobotConstants.LEFT_BACKWARD) 	m = "LeftBackward";
		else if (movement == RobotConstants.RIGHT_BACKWARD) m = "RightBackward";
		else if (movement == RobotConstants.STOP)			m = "Stop";
		else					  							m = "Unidentified";
		
		return m;
	}
	
	/**
	 * Convert the direction from integer to words
	 */
	public String directionToWords (int direction) {
		String d = "";
		if (direction == 1) 		d = "North";
		else if (direction == 2)	d = "East";
		else if (direction == 3)	d = "South";
		else if (direction == 4)	d = "West";
		else						d = "Unidentified";
		
		return d;
	}
	
	public double[] posAfterMove(String move) {
		double[] p = {row, col, direction};
		char m = move.charAt(0);
		double radius = RobotConstants.FAKE_RADIUS;
		// north
		if (direction == 90) {
			if (m == RobotConstants.FORWARD)
				p[0] += Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.BACKWARD)
				p[0] -= Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.LEFT_FORWARD) {
				p[0] += radius;
				p[1] -= radius;
				p[2] = 180;
			} else if (m == RobotConstants.RIGHT_FORWARD) {
				p[0] += radius;
				p[1] += radius;
				p[2] = 0;
			} else if (m == RobotConstants.LEFT_BACKWARD) {
				p[0] -= radius;
				p[1] -= radius;
				p[2] = 0;
			} else if (m == RobotConstants.RIGHT_BACKWARD) {
				p[0] -= radius;
				p[1] += radius;
				p[2] = 180;
			}
		}
		// east
		else if (direction == 0) {
			if (m == RobotConstants.FORWARD)
				p[1] += Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.BACKWARD)
				p[1] -= Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.LEFT_FORWARD) {
				p[0] += radius;
				p[1] += radius;
				p[2] = 90;
			} else if (m == RobotConstants.RIGHT_FORWARD) {
				p[0] -= radius;
				p[1] += radius;
				p[2] = -90;
			} else if (m == RobotConstants.LEFT_BACKWARD) {
				p[0] += radius;
				p[1] -= radius;
				p[2] = -90;
			} else if (m == RobotConstants.RIGHT_BACKWARD) {
				p[0] -= radius;
				p[1] -= radius;
				p[2] = 90;
			}
		}
		// south
		else if (direction == -90) {
			if (m == RobotConstants.FORWARD)
				p[0] -= Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.BACKWARD)
				p[0] += Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.LEFT_FORWARD) {
				p[0] -= radius;
				p[1] += radius;
				p[2] = 0;
			} else if (m == RobotConstants.RIGHT_FORWARD) {
				p[0] -= radius;
				p[1] -= radius;
				p[2] = 180;
			} else if (m == RobotConstants.LEFT_BACKWARD) {
				p[0] += radius;
				p[1] += radius;
				p[2] = 180;
			} else if (m == RobotConstants.RIGHT_BACKWARD) {
				p[0] += radius;
				p[1] -= radius;
				p[2] = 0;
			}
		}
		// west
		else if (direction == 180) {
			if (m == RobotConstants.FORWARD)
				p[1] -= Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.BACKWARD)
				p[1] += Double.parseDouble(move.substring(1)) / 10;
			else if (m == RobotConstants.LEFT_FORWARD) {
				p[0] -= radius;
				p[1] -= radius;
				p[2] = -90;
			} else if (m == RobotConstants.RIGHT_FORWARD) {
				p[0] += radius;
				p[1] -= radius;
				p[2] = 90;
			} else if (m == RobotConstants.LEFT_BACKWARD) {
				p[0] -= radius;
				p[1] += radius;
				p[2] = 90;
			} else if (m == RobotConstants.RIGHT_BACKWARD) {
				p[0] += radius;
				p[1] += radius;
				p[2] = -90;
			}
		}
		return p;
	}
	
	
	
	
    
}
