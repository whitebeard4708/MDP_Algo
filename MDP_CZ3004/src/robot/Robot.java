package robot;

import maps.Map;
import maps.MapConstants;
import maps.Cell;
import utils.CommMgr;
import utils.MapDescriptor;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Robot {
	// Center cell
	// Will always be calculated to integer value
	private int col; // col
	private int row; // row
	
	private int direction; 	// 0 - North || 1 - East || 2 - South || 3 - West
	private char movement;	
	
	private int speed = 100;	
	private final boolean isRealBot;
	
	public Robot(int row, int col, boolean realBot) {
		this.row = row;
		this.col = col;
		this.isRealBot = realBot;
		this.direction = 1; // N
		
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public int getCol() {
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
	public void setRobotPos(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public boolean checkValidRobotCenter(int r, int c) {
		return ((1<=r) && (r<=MapConstants.MAP_ROW-2) && (1<=c) && (c<=MapConstants.MAP_COL-2));
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
	
	
	
	
	
	
    
}
