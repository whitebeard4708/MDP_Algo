package robot;

import maps.Map;
import maps.MapConstants;
import maps.Cell;
import utils.CommMgr;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Robot {
	// Center cell
	private int x; // col
	private int y; // row
	
	private int direction; 	// 0 - North || 1 - East || 2 - South || 3 - West
	private int movement;	
	
	private int speed = 100;
	
	// List of sensors
	private Sensor s1;
	private Sensor s2;
	
	private final boolean isRealBot;
	
	public Robot(int row, int col, boolean realBot) {
		this.y = row;
		this.x = col;
		this.isRealBot = realBot;
		this.direction = 1; // East
		
		// instantiate robot sensors
		s1 = new Sensor(1);
		s2 = new Sensor(2);
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public int getRow() {
		return this.y;
	}
	
	public int getCol() {
		return this.x;
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
	
	public void setRobotPos(int row, int col) {
		this.y = row;
		this.x = col;
	}
	
	/*
	 * Convert the movement from integer to words
	 */
	private String movementToWords(int movement) {
		String m = "";
		if (movement == 1)		m = "Forward";
		else if (movement == 2) m = "Backward";
		else if (movement == 3) m = "Left";
		else if (movement == 4) m = "Right";
		else if (movement == 5) m = "Stop";
		else					m = "Unidentified";
		
		return m;
	}
	
	/*
	 * Convert the direction from integer to words
	 */
	private String directionToWords (int direction) {
		String d = "";
		if (direction == 1) 		d = "North";
		else if (direction == 2)	d = "East";
		else if (direction == 3)	d = "South";
		else if (direction == 4)	d = "West";
		else						d = "Unidentified";
		
		return d;
	}
	
	/*
	 * Change the position of robot based on its current position and direction
	 * Send the movement if isRealBot = true
	 */
	public void move(int movement, boolean sendToAndroid) {
		String m = movementToWords(movement); // movement in words
		if (isRealBot) {
			// Emulate real movement by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(speed); // delay
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
		}
		
		switch(movement) {
		
		// forward
		case 1:
			// North
			if (direction == 0)			this.y = Math.max(y++, MapConstants.MAP_ROW - 2); 
			
			// East
			else if (direction == 1) 	this.x = Math.max(x++,  MapConstants.MAP_COL - 2);
			
			// South
			else if (direction == 2)	this.y = Math.min(y--, 1);
			
			// West
			else if (direction == 3)	this.x = Math.min(x--, 1);
			
			else;
			break;
			
		// backward
		case 2:
			// North
			if (direction == 0)			this.y = Math.min(y--, 1); 
			
			// East
			else if (direction == 1) 	this.x = Math.min(x--,  1);
			
			// South
			else if (direction == 2)	this.y = Math.max(y++, MapConstants.MAP_ROW - 2); 
			
			// West
			else if (direction == 3)	this.x = Math.max(x++,  MapConstants.MAP_COL - 2);
			
			else;
			break;
			
		// turn left
		case 3:
			// direction decreased by 1
			// North -> West
			this.direction = (this.direction - 1) % 4;
			break;
			
		// turn right
		case 4:
			// direction increased by 1
			// North -> East
			this.direction = (this.direction + 1) % 4;
			break;
		
		// calibrate
		case 5:
			break;
		
		default:
			System.out.println("Error when Robot moves!");
			break;
		}
		
		if (isRealBot) sendMovement(movement, false);
		else System.out.println("Move: " + m);
	}
	
	public void move(int movement) {
		this.move(movement, true);
	}
	
	private void sendMovement(int m, boolean sendMoveToAndroid) {
		CommMgr comm = CommMgr.getCommMgr();
        comm.sendMsg(movementToWords(m) + "", CommMgr.INSTRUCTIONS);
        System.out.println("Bot Current Position: " + this.y + ", " + this.x);
        
        // send message when the movement isn't "calibrate" and sendMoveToAndroid is set.
        if (m != 5 && sendMoveToAndroid) {
            comm.sendMsg(this.y + "," + this.x + "," + directionToWords(this.direction), CommMgr.BOT_POS);
        }
	}
}
