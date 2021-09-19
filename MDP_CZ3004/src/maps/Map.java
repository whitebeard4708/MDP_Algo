package maps;

import robot.Robot;
import robot.RobotConstants;
import utils.CommMgr;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import maps.GraphicsConstants;

public class Map extends JPanel{
	private Cell[][] grid;
	
	private int numObstacleFound = 0;
	private Cell[] obstacles = new Cell[MapConstants.NUM_OBSTACLE];
	
	private Robot bot;
	
	// for each obstacle, we have its corresponding position for fastest path
	// [row, col, facing_direction]
	// facing_direction is the direction should be faced to image
	// 1 cell can recognize multiple images if robot rotates
	// so we have 1 more number to control the number of cellToArrive
	private double[][] positionToArrive = new double[MapConstants.NUM_OBSTACLE][3];
	private boolean isDrawPositionToArrive = false;
	
	public Map(Robot bot) {
		this.bot = bot;
		
		grid = new Cell[MapConstants.MAP_ROW][MapConstants.MAP_COL];
		
		// assign cell with its (y,x)
		for (int row=0; row < grid.length; row++) {
			for (int col=0; col < grid[0].length; col++) {
				grid[row][col] = new Cell(row, col);
				
				// Set the virtual walls of the arena
                if (row == 0 || col == 0 || row == MapConstants.MAP_ROW - 1 || col == MapConstants.MAP_COL - 1) {
                    grid[row][col].setVirtualWall(true);
                }
			}
		}
	}
	
	/**
     * Returns true if the row and column values are in the start zone.
     */
    private boolean inStartZone(float row, float col) {
        return row >= 0 && row <= 3 && col >= 0 && col <= 3;
    }
    
    /**
     * Returns true if the row and column values are valid.
     */
    public boolean checkValidCoordinates(double row, double col) {
        return row >= 0 && col >= 0 && row < MapConstants.MAP_ROW && col < MapConstants.MAP_COL;
    }
    
    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean getIsObstacleOrWall(double row, double col) {
    	int rcell = (int) Math.floor(row);
    	int ccell = (int) Math.floor(col);
        return !checkValidCoordinates(row, col) || getCell(rcell, ccell).getIsObstacle();
    }
    
    public boolean isFree(double row, double col) {
    	int rcell = (int) Math.floor(row);
    	int ccell = (int) Math.floor(col);
    	return checkValidCoordinates(row, col) && !getCell(rcell, ccell).getIsObstacle();
    }
    
    public boolean checkValidRobotCenter(double r, double c) {
		return checkFreeRegion(r-1.5, c-1.5, r+1.5, c+1.5);
	}
    
    public double[] getPositionToArrive(int index) {
    	return this.positionToArrive[index];
    }
    
    public boolean getIsDrawpositionToArrive() {
    	return isDrawPositionToArrive;
    }
    
    public Cell getCell(double row, double col) {
    	int rcell = (int) Math.floor(row);
    	int ccell = (int) Math.floor(col);
    	return grid[rcell][ccell];
    }
    
    public boolean isObstacleCell(double row, double col) {
    	int rcell = (int) Math.floor(row);
    	int ccell = (int) Math.floor(col);
        return grid[rcell][ccell].getIsObstacle();
    }
    
    public Cell[] getObstacles() {
    	return this.obstacles;
    }
    
    public void setObstacleCell(int row, int col, boolean is_obstacle) {
    	grid[row][col].setIsObstacled(is_obstacle);
    }
    
    public void setImageSide(int row, int col, int image_side) {
    	grid[row][col].setImageSide(image_side);
    }
    
    public void setImageId(int row, int col, char image_id) {
    	grid[row][col].setImageId(image_id);
    }
    
    public void setIsDrawPositionToArrive(boolean draw) {
    	this.isDrawPositionToArrive = draw;
    }
    
    public void addNewImage(int row, int col, int image_side) {
    	setObstacleCell(row, col, true);
    	
		setImageSide(row, col, image_side);
    	
		obstacles[numObstacleFound] = grid[row][col];
    	numObstacleFound++;
    	System.out.println("Add new image to map");  	
    }
    
    public void printObstacle() {
    	for (int i=0; i<numObstacleFound; i++) {
    		obstacles[i].printInfo();
    	}
    }
    
    /**
     * Cells for image recognition should be 20cm away from the image in the according direction
     * based on obstacle positions
     * 
     * For example, if obstacle cell is (5, 7) and direction "north"
     * then the robot should be at (8, 7) and facing "south"
     *
     */
    public void findPositionToArrive() {
    	printObstacle();
    	for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
    		
    		int image_side = obstacles[i].getImageSide();
    		double r = obstacles[i].getRow();
    		double c = obstacles[i].getCol();
    		
    		double cellToArriveRow = -1;
    		double cellToArriveCol = -1;
    		int cellToArriveDirection = 0;
    		
    		switch (image_side) {
    		
    		// north
    		case 90:
    			cellToArriveRow = r+3;	// row
    			cellToArriveCol = c;	// col
    			cellToArriveDirection = -90;	// facing south
    			break;
    			
    		// east
    		case 0:
    			cellToArriveRow = r;	// row
    			cellToArriveCol = c+3;	// col
    			cellToArriveDirection = 180;	// facing west
    			break;
    			
    		// south
    		case -90:
    			cellToArriveRow = r-3;	// row
    			cellToArriveCol = c;	// col
    			cellToArriveDirection = 90;	// facing north
    			break;
    		
    		// west
    		case 180:
    			cellToArriveRow = r;	// row
    			cellToArriveCol = c-3;	// col
    			cellToArriveDirection = 0;	// facing east
    			break;
    		
    		
    		default:
    			break;
    		}
    		
    		// add new cell
    		// check valid
    		if (checkValidRobotCenter(cellToArriveRow, cellToArriveCol)) {
    			positionToArrive[i][0] = cellToArriveRow;
				positionToArrive[i][1] = cellToArriveCol;
				positionToArrive[i][2] = cellToArriveDirection;
				System.out.println("Add new cells to arrive: (" + cellToArriveRow + ", " + cellToArriveCol + ", " + cellToArriveDirection + ")");
    		}
    	}
    	isDrawPositionToArrive = true;
    }
    
    /*
     * Map checks the condition for a successful movement
     * Robot will execute them without the need to check anything
     */
    
    public void moveRobotForward(double d) {
    	double rd = bot.getRow();
    	double cd = bot.getCol();
    	double[] pos = {rd, cd, bot.getDirection()};
    	// check if robot can move forward d consecutive steps
		double[] arr = checkForwardAvailable(pos, d);
		if (arr[2] == 1) {
			rd = arr[0];
			cd = arr[1];
		} else {
			System.out.println("Can't move forward by " + d + "cm");
			return;
		}
    	bot.setRobotPos(rd, cd);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.FORWARD + String.format("%2d", d);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println(String.format("Move forward to position (%.1f,%.1f).", bot.getRow(), bot.getCol()));
		}
    }
    
    public void moveRobotBackward(double d) {
    	double rd = bot.getRow();
    	double cd = bot.getCol();
    	double[] pos = {rd, cd, bot.getDirection()};
    	// check if robot can move forward d consecutive steps
		double[] arr = checkBackwardAvailable(pos, d);
		if (arr[2] == 1) {
			rd = arr[0];
			cd = arr[1];
		} else {
			System.out.println("Can't move backward by " + d + "cm");
			return;
		}
    	bot.setRobotPos(rd, cd);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.BACKWARD + String.format("%2d", d);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println(String.format("Move backward to position (%.1f,%.1f)",bot.getRow(), bot.getCol()));
		}
    }
    
    
    public void moveRobotLeftF(int angle) {
    	// early stopping
    	if (angle != 45 && angle != 90) {
    		System.out.println("Angle not available");
    		return;
    	}
    	
    	double rd = bot.getRow();
    	double cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
    	double[] pos = {rd, cd, bot.getDirection()};
		double[] arr = checkLeftForwardAvailable(pos, angle);
		if (arr[2] == 1) {
			rd = arr[0];
			cd = arr[1];
			
    	} else {
    		System.out.println("Can't move left forward by " + angle + " degree");
    		return;
    	}
    		
    	bot.setRobotPos(rd, cd);
    	int new_direction = bot.getDirection() + angle;
    	if (new_direction > 180) new_direction -= 360;
    	bot.setDirection(new_direction);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.LEFT_FORWARD + String.format("%2d", angle);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println(String.format("Move left forward to position (%.1f,%.1f).", bot.getRow(), bot.getCol()));
		}
    }
    
    public void moveRobotLeftB(int angle) {
    	// early stopping
    	if (angle != 45 && angle != 90) {
    		System.out.println("Angle not available");
    		return;
    	}
    	
    	double rd = bot.getRow();
    	double cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
    	double[] pos = {rd,cd,bot.getDirection()};
		double[] arr = checkLeftBackwardAvailable(pos, angle);
		if (arr[2] == 1) {
			rd = arr[0];
			cd = arr[1];
    	} else {
    		System.out.println("Can't move left backward by " + angle + " degree");
    		return;
    	}
    		
    	bot.setRobotPos(rd, cd);
    	int new_direction = bot.getDirection() - angle;
    	if (new_direction <= -180) new_direction += 360;
    	bot.setDirection(new_direction);
    	
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.LEFT_BACKWARD + String.format("%2d", angle);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println(String.format("Move left backward to position (%.1f,%.1f).", bot.getRow(), bot.getCol()));
		}
    }
    
    public void moveRobotRightF(int angle) {
    	// early stopping
    	if (angle != 45 && angle != 90) {
    		System.out.println("Angle not available");
    		return;
    	}
    	
    	double rd = bot.getRow();
    	double cd = bot.getCol();
    	double[] pos = {rd,cd,bot.getDirection()};
    	// check if robot can move forward d consecutive steps
		double[] arr = checkRightForwardAvailable(pos, angle);
		if (arr[2] == 1) {
			rd = arr[0];
			cd = arr[1];
    	} else {
    		System.out.println("Can't move right forward by " + angle + " degree");
    		return;
    	}
    	bot.setRobotPos(rd, cd);
    	int new_direction = bot.getDirection() - angle;
    	if (new_direction <= -180) new_direction += 360;
    	bot.setDirection(new_direction);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.RIGHT_FORWARD + String.format("%2d", angle);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println(String.format("Move right forward to position (%.1f,%.1f).", bot.getRow(), bot.getCol()));
		}
    }
    
    public void moveRobotRightB(int angle) {
    	// early stopping
    	if (angle != 45 && angle != 90) {
    		System.out.println("Angle not available");
    		return;
    	}
    	
    	double rd = bot.getRow();
    	double cd = bot.getCol();
    	double[] pos = {rd,cd,bot.getDirection()};
    	// check if robot can move forward d consecutive steps
		double[] arr = checkRightBackwardAvailable(pos, angle);
		if (arr[2] == 1) {
			rd = arr[0];
			cd = arr[1];
    	} else {
    		System.out.println("Can't move right backward by " + angle + " degree");
    		return;
    	}
    	bot.setRobotPos(rd, cd);
    	int new_direction = bot.getDirection() + angle;
    	if (new_direction > 180) new_direction -= 360;
    	bot.setDirection(new_direction);
    	
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.RIGHT_BACKWARD + String.format("%2d", 90);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println(String.format("Move right backward to position (%.1f,%.1f).", bot.getRow(), bot.getCol()));
		}
    }
    
    public double[] checkForwardAvailable(double[] position, double d) {
    	double r = position[0];
    	double c = position[1];
    	int dir = (int) position[2];
    	double[] arr = {r,c,0};
    	if (dir == 90) {			// north
    		if (checkFreeRegion(r+1.5, c-1.5, r+d/10+1.5, c+1.5)) {
    			arr[2] = 1;
    			arr[0] = r+d/10;
    		}
    		
    	} else if (dir == 0) {	// east
    		if (checkFreeRegion(r-1.5, c+1.5, r+1.5, c+d/10+1.5)) {
    			arr[2] = 1;
    			arr[1] = c+d/10;
    		}
    	} else if (dir == -90) {	// south
    		if (checkFreeRegion(r-1.5, c-1.5, r-d/10-1.5, c+1.5)) {
    			arr[2] = 1;
    			arr[0] = r-d/10;
    		}
    	} else if(dir == 180) {
    		if (checkFreeRegion(r-1.5, c-1.5, r+1.5, c-d/10-1.5)) {
    			arr[2] = 1;
    			arr[1] = c-d/10;
    		}
    	}
    	return arr;
    }
    
    public double[] checkBackwardAvailable(double[] position, double d) {
    	double r = position[0];
    	double c = position[1];
    	int dir = (int) position[2];
    	double[] arr = {r,c,0};
    	if (dir == 90) {			// north
    		if (checkFreeRegion(r-1.5, c-1.5, r-d/10-1.5, c+1.5)) {
    			arr[2] = 1;
    			arr[0] = r-d/10;
    		}	
    	} else if (dir == 0) {	// east
    		if (checkFreeRegion(r-1.5, c-1.5, r+1.5, c-d/10-1.5)) {
    			arr[2] = 1;
    			arr[1] = c-d/10;
    		}
    	} else if (dir == -90) {	// south
    		if (checkFreeRegion(r+1.5, c-1.5, r+d/10+1.5, c+1.5)) {
    			arr[2] = 1;
    			arr[0] = r+d/10;
    		}
    	} else if(dir == 180) {
    		if (checkFreeRegion(r-1.5, c+1.5, r+1.5, c+d/10+1.5)) {
    			arr[2] = 1;
    			arr[1] = c+d/10;
    		}
    	}
    	
    	return arr;
    }
    
    public double[] checkLeftForwardAvailable(double [] position,  int angle) {
    	double r = position[0];
    	double c = position[1];
    	int dir = (int) position[2];
    	double[] arr = {position[0], c, 0};
		boolean side = false;
		
		// find new position when turning
		double[] dif = changeWithAngle(angle);
		double rd, cd;
		
		// north
		if (dir == 90) {
			
			// early check
			side = checkFreeRegion(r+0.5, c-2.5, r-1.5, c-1.5);
			if (!side) return arr;
			
			rd = r + dif[0];
			cd = c - dif[1];
			if (checkFreeRegion(r+.5, c+1.5, rd+1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		} 
		// east
		else if (dir == 0) {
			
			// early check
			side = checkFreeRegion(r+1.5, c-1.5, r+2.5, c+0.5);
			if (!side) return arr;
			
			rd = r + dif[1];
			cd = c + dif[0];
			if (checkFreeRegion(r-1.5, c+1.5, rd+1.5, cd+.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		}
		// south
		else if (dir == -90) {
			
			// early check
			side = checkFreeRegion(r-0.5, c+1.5, r+1.5, c+2.5);
			if (!side) return arr;
			
			rd = r - dif[0];
			cd = c + dif[1];
			if (checkFreeRegion(r-.5, c-1.5, rd-1.5, cd+1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
		}
		// west
		else if (dir == 180) {
			
			// early check
			side = checkFreeRegion(r-2.5, c-0.5, r-1.5, c+1.5);
			if (!side) return arr;
			
			rd = r - dif[1];
			cd = c - dif[0];
			if (checkFreeRegion(r+1.5, c-.5, rd-1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
		}
    	return arr;
    }
    
    public double[] checkRightForwardAvailable(double[] position, int angle) {
    	double r = position[0];
    	double c = position[1];
    	int dir = (int) position[2];
    	double[] arr = {r, c, 0};
		boolean side = false;
    	
		double[] dif = changeWithAngle(angle);
		double rd, cd;
		
		// north
		if (dir == 90) {
			
			// early stopping
			side = checkFreeRegion(r+0.5, c+1.5, r-1.5, c+2.5);
			if (!side) return arr;
			
			rd = r + dif[0];
			cd = c + dif[1];
			if (checkFreeRegion(r+.5, c-1.5, rd+1.5, cd+1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		} 
		// east
		else if (dir == 0) {
			
			// early stopping
			side = checkFreeRegion(r-2.5, c+.5, r-1.5, c-1.5);
			if (!side) return arr;
			
			rd = r - dif[0];
			cd = c + dif[1];
			if (checkFreeRegion(r+1.5, c+.5, rd-1.5, cd+1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		}
		// south
		else if (dir == -90) {
			
			// early stopping
			side = checkFreeRegion(r-.5, c-2.5, r+1.5, c-1.5);
			if (!side) return arr;
			
			rd = r - dif[0];
			cd = c - dif[1];
			if (checkFreeRegion(r-.5, c+1.5, rd-1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		}
		// west
		else if (dir == 180) {
			
			// early stopping
			side = checkFreeRegion(r+2.5, c-0.5, r+1.5, c+1.5);
			if (!side) return arr;
			
			rd = r + dif[1];
			cd = c - dif[0];
			if (checkFreeRegion(r-1.5, c-.5, rd+1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
		}
    	return arr;
    }
    
    public double[] checkLeftBackwardAvailable(double[] position, int angle) {
    	double r = position[0];
    	double c = position[1];
    	int dir = (int) position[2];
    	double[] arr = {r, c, 0};
		boolean side = false;
		double[] dif = changeWithAngle(angle);
		double rd, cd;
		
		// north
		if (dir == 90) {

			// early stopping
			side = checkFreeRegion(r-.5, c-2.5, r+1.5, c-1.5);
			if (!side) return arr;
			
			rd = r - dif[0];
			cd = c - dif[1];
			if (checkFreeRegion(r-.5, c+1.5, rd-1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		} 
		// east
		else if (dir == 0) {

			// early stopping
			side = checkFreeRegion(r+2.5, c-0.5, r+1.5, c+1.5);
			if (!side) return arr;
			
			rd = r + dif[1];
			cd = c - dif[0];
			if (checkFreeRegion(r-1.5, c-0.5, rd+1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		}
		// south
		else if (dir == -90) {
			// early stopping
			side = checkFreeRegion(r+0.5, c+1.5, r-1.5, c+2.5);
			if (!side) return arr;
			
			rd = r + dif[0];
			cd = c + dif[1];
			if (checkFreeRegion(r+.5, c-1.5, rd+1.5, cd+1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			} 
			
		}
		// west
		else if (dir == 180) {
			
			// early stopping
			side = checkFreeRegion(r-2.5, c+.5, r-1.5, c-1.5);
			if (!side) return arr;
			
			rd = r - dif[0];
			cd = c + dif[1];
			if (checkFreeRegion(r+1.5, c+.5, rd-1.5, cd+1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
			
		}
    	return arr;
    }
    
    public double[] checkRightBackwardAvailable(double[] position, int angle) {
    	double r = position[0];
    	double c = position[1];
    	int dir = (int) position[2];
    	double[] arr = {r, c, 0};
		boolean side = false;
    	// check whole 3x3 region above robot is available and left 3x3 region is also available
		double[] dif = changeWithAngle(angle);
		double rd, cd;
		
		// north
		if (dir == 90) {

			// early check
			side = checkFreeRegion(r-0.5, c+1.5, r+1.5, c+2.5);
			if (!side) return arr;
			
			rd = r - dif[0];
			cd = c + dif[1];
			if (checkFreeRegion(r-0.5, c-1.5, rd-1.5, cd+1.5)) {
				arr[0] = dif[0];
				arr[1] = dif[1];
				arr[2] = 1;
			}	
		} 
		// east
		else if (dir == 0) {

			// early check
			side = checkFreeRegion(r-2.5, c-0.5, r-1.5, c+1.5);
			if (!side) return arr;
			
			rd = r - dif[1];
			cd = c - dif[0];
			if (checkFreeRegion(r+1.5, c-0.5, rd-1.5, cd-1.5)) {
				arr[0] = dif[0];
				arr[1] = dif[1];
				arr[2] = 1;
			}
		}
		// south
		else if (dir == -90) {			
			// early check
			side = checkFreeRegion(r+0.5, c-2.5, r-1.5, c-1.5);
			if (!side) return arr;
			
			rd = r + dif[0];
			cd = c - dif[1];
			if (checkFreeRegion(r+0.5, c+1.5, rd+1.5, cd-1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}
		}
		// west
		else if (dir == 180) {
			// early check
			side = checkFreeRegion(r+1.5, c-1.5, r+2.5, c+0.5);
			if (!side) return arr;
			
			rd = r + dif[1];
			cd = c + dif[0];
			if (checkFreeRegion(r-1.5, c+0.5, rd+1.5, cd+1.5)) {
				arr[0] = rd;
				arr[1] = cd;
				arr[2] = 1;
			}	
		}
    	return arr;
    }
    
    /**
	 * Find the differences in front distance and side distance when a robot turns a certain angle
	 * The movement functions need to use the return value according to the direction which robot is facing.
	 * @param angle 0 <= angle <= 90
	 * @return array of 2 int numbers: front distance and side distance
	 */
	private double[] changeWithAngle(int angle) {
		// vertical dif, horizontal dif
		double radius = 3;
		// double radius = RobotConstants.TURN_RADIUS;
		double[] ans = {0,0};
		double r_angle = Math.toRadians(angle);
		ans[0] = radius * Math.sin(r_angle);
		ans[1] = radius * (1 - Math.cos(r_angle));
		return ans;
	}
	
	/**
	 * Check if any obstacle lies in rectangular area limited by y=r1, y=r2, x=c1, x=c2
	 * @param r1 
	 * @param c1 
	 * @param r2 
	 * @param c2
	 * @return Return true if no obstacles found, false otherwise
	 */
	private boolean checkFreeRegion(double r1, double c1, double r2, double c2) {
		double offset = 0.1;
		double rsmall = Math.min(r1, r2) + offset;
		double rlarge = Math.max(r1, r2) - offset;
		double csmall = Math.min(c1, c2) + offset;
		double clarge = Math.max(c1, c2) - offset;
		
		if (rsmall < 0 || csmall < 0 || rlarge >= 20 || clarge >= 20) return false;
		
		int rr1 = (int) Math.floor(rsmall);
		int cc1 = (int) Math.floor(csmall);
		int rr2 = (int) Math.floor(rlarge);
		int cc2 = (int) Math.floor(clarge);
		for (int i=rr1; i<=rr2; i++) {
			for (int j=cc1; j<=cc2; j++) {
				if (grid[i][j].getIsObstacle()) return false;
			}
		}
		
		return true;
	}
    
    public void paintComponent(Graphics g) {
        // Create a two-dimensional array of _DisplayCell objects for rendering.
        _DisplayCell[][] _mapCells = new _DisplayCell[MapConstants.MAP_ROW][MapConstants.MAP_COL];
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROW; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COL; mapCol++) {
                _mapCells[mapRow][mapCol] = new _DisplayCell(mapCol * GraphicsConstants.CELL_SIZE, mapRow * GraphicsConstants.CELL_SIZE, GraphicsConstants.CELL_SIZE);
            }
        }
        	
        // Paint background to white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 900, 700);
        
        // Paint the cells with the appropriate colors.
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROW; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COL; mapCol++) {
                Color cellColor;

                if (inStartZone(mapRow, mapCol))
                    cellColor = GraphicsConstants.C_START;
                else if (grid[mapRow][mapCol].getIsObstacle())
                    cellColor = GraphicsConstants.C_OBSTACLE;
                else
                    cellColor = GraphicsConstants.C_FREE;

                g.setColor(cellColor);
                g.fillRect(_mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET, _mapCells[mapRow][mapCol].cellY, _mapCells[mapRow][mapCol].cellSize, _mapCells[mapRow][mapCol].cellSize);
                
                // add image color to obstacle
                if (grid[mapRow][mapCol].getIsObstacle()) {
                	
                	// check image side
                	int image_side = grid[mapRow][mapCol].getImageSide();
                	int x1, y1, width, height; // x2 = x1 + width, y2 = y1 + height
                	switch (image_side) {
                	
                	// north
                	case 90:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET;
                		y1 = _mapCells[mapRow][mapCol].cellY;
                		width = GraphicsConstants.CELL_SIZE;
                		height = GraphicsConstants.IMAGE_SIZE;
                		break;
                	
                	// east
                	case 0:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET + GraphicsConstants.CELL_SIZE - GraphicsConstants.IMAGE_SIZE;
                		y1 = _mapCells[mapRow][mapCol].cellY;
                		width = GraphicsConstants.IMAGE_SIZE;
                		height = GraphicsConstants.CELL_SIZE;
                		break;
                		
                	// south
                	case -90:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET;
                		y1 = _mapCells[mapRow][mapCol].cellY + GraphicsConstants.CELL_SIZE - GraphicsConstants.IMAGE_SIZE;
                		width = GraphicsConstants.CELL_SIZE;
                		height = GraphicsConstants.IMAGE_SIZE;
                		break;
                	
                	// west
                	case 180:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET;
                		y1 = _mapCells[mapRow][mapCol].cellY;
                		width = GraphicsConstants.IMAGE_SIZE;
                		height = GraphicsConstants.CELL_SIZE;
                		break;
                		
                	default:
                		x1 = y1 = width = height = 0;
                		break;
                			
                	}
                	g.setColor(GraphicsConstants.C_IMAGE);
                	g.fillRect(x1, y1, width, height);
                }

            }
        }

        // Paint the robot on-screen.
        g.setColor(GraphicsConstants.C_ROBOT);
        double r = bot.getRow();
        double c = bot.getCol();
        
        g.fillOval((int) ((c - 1.5) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - ((r-0.5) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET)), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);

        // Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsConstants.C_ROBOT_DIR);
        
        int d = bot.getDirection();
        switch (d) {
            case 90:		// North
                g.fillOval( (int) ((c-0.5) * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - (r-0.5) * GraphicsConstants.CELL_SIZE - 5), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            case 45:	// Northeast
                g.fillOval( (int) ((c-0.5) * GraphicsConstants.CELL_SIZE + 28 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - (r-0.5) * GraphicsConstants.CELL_SIZE  + 2), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
                
            // East
            case 0:
                g.fillOval( (int) ((c-0.5) * GraphicsConstants.CELL_SIZE + 35 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - (r-0.5) * GraphicsConstants.CELL_SIZE + 10), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // South
            case -90:
                g.fillOval((int) ((c-0.5) * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - (r-0.5) * GraphicsConstants.CELL_SIZE + 35), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // West
            case 180:
                g.fillOval((int) ((c-0.5) * GraphicsConstants.CELL_SIZE - 15 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - (r-0.5) * GraphicsConstants.CELL_SIZE + 10), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            
        }
        
        if (isDrawPositionToArrive) {
        	// printObstacle();
        	// findpositionToArrive();
        	for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
        		int crow = (int) Math.floor(positionToArrive[i][0]);
        		int ccol = (int) Math.floor(positionToArrive[i][1]);
        		g.setColor(GraphicsConstants.C_ARRIVE);
        		g.fillRect(_mapCells[crow][ccol].cellX + GraphicsConstants.MAP_X_OFFSET, _mapCells[crow][ccol].cellY, GraphicsConstants.CELL_SIZE, GraphicsConstants.CELL_SIZE);
        	}
        }
    }

    private class _DisplayCell {
        public final int cellX;
        public final int cellY;
        public final int cellSize;

        public _DisplayCell(int borderX, int borderY, int borderSize) {
            this.cellX = borderX + GraphicsConstants.CELL_LINE_WEIGHT;
            this.cellY = GraphicsConstants.MAP_H - (borderY - GraphicsConstants.CELL_LINE_WEIGHT);
            this.cellSize = borderSize - (GraphicsConstants.CELL_LINE_WEIGHT * 2);
        }
    }
}
