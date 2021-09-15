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
	private int[][] cellsToArrive = new int[MapConstants.NUM_OBSTACLE][3];
	private boolean isDrawCellsToArrive = false;
	
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
    private boolean inStartZone(int row, int col) {
        return row >= 0 && row <= 3 && col >= 0 && col <= 3;
    }
    
    /**
     * Returns true if the row and column values are valid.
     */
    public boolean checkValidCoordinates(int row, int col) {
        return row >= 0 && col >= 0 && row < MapConstants.MAP_ROW && col < MapConstants.MAP_COL;
    }
    
    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean getIsObstacleOrWall(int row, int col) {
        return !checkValidCoordinates(row, col) || getCell(row, col).getIsObstacle();
    }
    
    public boolean isFree(int row, int col) {
    	return checkValidCoordinates(row, col) && !getCell(row, col).getIsObstacle();
    }
    
    public int[] getCellsToArrive(int index) {
    	return this.cellsToArrive[index];
    }
    
    public boolean getIsDrawCellsToArrive() {
    	return isDrawCellsToArrive;
    }
    
    public Cell getCell(int row, int col) {
    	return grid[row][col];
    }
    
    public boolean isObstacleCell(int row, int col) {
        return grid[row][col].getIsObstacle();
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
    
    public void setIsDrawCellsToArrive(boolean draw) {
    	this.isDrawCellsToArrive = draw;
    }
    
    public void addNewImage(int row, int col, int image_side, char image_id) {
    	setObstacleCell(row, col, true);
    	
    	if (image_side != 0) {
    		setImageSide(row, col, image_side);
        	setImageId(row, col, image_id);
    		obstacles[numObstacleFound] = grid[row][col];
        	numObstacleFound++;
        	System.out.println("Add new image to map");
    	}
    	
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
    public void findCellsToArrive() {
    	printObstacle();
    	for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
    		
    		int image_side = obstacles[i].getImageSide();
    		int r = obstacles[i].getRow();
    		int c = obstacles[i].getCol();
    		
    		int cellToArriveRow = -1;
    		int cellToArriveCol = -1;
    		int cellToArriveDirection = 0;
    		
    		switch (image_side) {
    		
    		// north
    		case 1:
    			cellToArriveRow = r+3;	// row
    			cellToArriveCol = c;	// col
    			cellToArriveDirection = 3;	// facing south
    			break;
    			
    		// east
    		case 2:
    			cellToArriveRow = r;	// row
    			cellToArriveCol = c+3;	// col
    			cellToArriveDirection = 4;	// facing west
    			break;
    			
    		// south
    		case 3:
    			cellToArriveRow = r-3;	// row
    			cellToArriveCol = c;	// col
    			cellToArriveDirection = 1;	// facing north
    			break;
    		
    		// west
    		case 4:
    			cellToArriveRow = r;	// row
    			cellToArriveCol = c-3;	// col
    			cellToArriveDirection = 2;	// facing east
    			break;
    		
    		
    		default:
    			break;
    		}
    		
    		// add new cell
    		// check valid
    		if (bot.checkValidRobotCenter(cellToArriveRow, cellToArriveCol)) {
    			cellsToArrive[i][0] = cellToArriveRow;
				cellsToArrive[i][1] = cellToArriveCol;
				cellsToArrive[i][2] = cellToArriveDirection;
				System.out.println("Add new cells to arrive: (" + cellToArriveRow + ", " + cellToArriveCol + ")");
    		}
    	}
    	isDrawCellsToArrive = true;
    }
    
    /*
     * Map checks the condition for a successful movement
     * Robot will execute them without the need to check anything
     */
    
    public void moveRobotForward(int d) {
    	int rd = bot.getRow();
    	int cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
    	for (int i=0; i<d; i++) {
    		int[] arr = checkForwardAvailable(rd, cd);
    		if (arr[0] == 1) {
    			rd = arr[1];
    			cd = arr[2];
    		} else {
    			System.out.println("Can't move forward by " + d + " step(s)");
    			return;
    		}
    	}
    	bot.setRobotPos(rd, cd);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.FORWARD + String.format("%2d", d);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println("Move forward to cell (" + bot.getRow() + "," + bot.getCol() + ").");
		}
    }
    
    public void moveRobotBackward(int d) {
    	int rd = bot.getRow();
    	int cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
    	for (int i=0; i<d; i++) {
    		int[] arr = checkBackwardAvailable(rd, cd);
    		if (arr[0] == 1) {
    			rd = arr[1];
    			cd = arr[2];
    		} else {
    			System.out.println("Can't move backward by " + d + " step(s)");
    			return;
    		}
    	}
    	bot.setRobotPos(rd, cd);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.BACKWARD + String.format("%2d", d);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println("Move backward to cell (" + bot.getRow() + "," + bot.getCol() + ").");
		}
    }
    
    
    public void moveRobotLeftF() {
    	int rd = bot.getRow();
    	int cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
		int[] arr = checkLeftForwardAvailable(rd, cd);
		if (arr[0] == 1) {
			rd = arr[1];
			cd = arr[2];
			
    	} else {
    		System.out.println("Can't move left forward by 90 degree");
    		return;
    	}
    		
    	bot.setRobotPos(rd, cd);
    	bot.setDirection((bot.getDirection() + 2) % 4 + 1);
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.LEFT_FORWARD + String.format("%2d", 90);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println("Move left forward to cell (" + bot.getRow() + "," + bot.getCol() + ").");
		}
    }
    
    public void moveRobotLeftB() {
    	int rd = bot.getRow();
    	int cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
		int[] arr = checkLeftBackwardAvailable(rd, cd);
		if (arr[0] == 1) {
			rd = arr[1];
			cd = arr[2];
    	} else {
    		System.out.println("Can't move left backward by 90 degree");
    		return;
    	}
    		
    	bot.setRobotPos(rd, cd);
    	bot.setDirection((bot.getDirection()) % 4 + 1);
    	
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.LEFT_BACKWARD + String.format("%2d", 90);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println("Move left backward to cell (" + bot.getRow() + "," + bot.getCol() + ").");
		}
    }
    
    public void moveRobotRightF() {
    	int rd = bot.getRow();
    	int cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
		int[] arr = checkRightForwardAvailable(rd, cd);
		if (arr[0] == 1) {
			rd = arr[1];
			cd = arr[2];
    	} else {
    		System.out.println("Can't move right forward by 90 degree");
    		return;
    	}
    	bot.setRobotPos(rd, cd);
    	bot.setDirection((bot.getDirection() % 4) + 1 );
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.RIGHT_FORWARD + String.format("%2d", 90);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println("Move right forward to cell (" + bot.getRow() + "," + bot.getCol() + ").");
		}
    }
    
    public void moveRobotRightB() {
    	int rd = bot.getRow();
    	int cd = bot.getCol();
    	// check if robot can move forward d consecutive steps
		int[] arr = checkRightBackwardAvailable(rd, cd);
		if (arr[0] == 1) {
			rd = arr[1];
			cd = arr[2];
    	} else {
    		System.out.println("Can't move right backward by 90 degree");
    		return;
    	}
    	bot.setRobotPos(rd, cd);
    	bot.setDirection((bot.getDirection() + 2) % 4 + 1 );
    	
    	if (bot.getRealBot()) {
			// send message to STM
			CommMgr comm = CommMgr.getCommMgr();
			String msg = RobotConstants.RIGHT_BACKWARD + String.format("%2d", 90);
	        comm.sendMsg(msg, comm.toSTM);        
		}
		else {
			System.out.println("Move right backward to cell (" + bot.getRow() + "," + bot.getCol() + ").");
		}
    }
    
    private int[] checkForwardAvailable(int r, int c) {
    	int[] arr = {0,r,c};
    	if (bot.getDirection() == 1) {			// north
    		if (!getIsObstacleOrWall(r+2, c-1) && (!getIsObstacleOrWall(r+2, c)) && (!getIsObstacleOrWall(r+2, c+1))) {
    			arr[1]++;	// r++
    			arr[0] = 1;
    		}
    	} else if (bot.getDirection() == 2) {	// east
    		if (!getIsObstacleOrWall(r-1, c+2) && (!getIsObstacleOrWall(r, c+2)) && (!getIsObstacleOrWall(r+1, c+2))) {
    			arr[2]++;	// c++
    			arr[0] = 1;
    		}
    	} else if (bot.getDirection() == 3) {	// south
    		if (!getIsObstacleOrWall(r-2, c-1) && (!getIsObstacleOrWall(r-2, c)) && (!getIsObstacleOrWall(r-2, c+1))) {
    			arr[1]--;
    			arr[0] = 1;
    		}
    	} else if(bot.getDirection() == 4) {
    		if (!getIsObstacleOrWall(r-1, c-2) && (!getIsObstacleOrWall(r, c-2)) && (!getIsObstacleOrWall(r+1, c+2))) {
    			arr[2]--;
    			arr[0] = 1;
    		}
    	}
    	return arr;
    }
    
    private int[] checkBackwardAvailable(int r, int c) {
    	int[] arr = {0,r,c};
    	if (bot.getDirection() == 1) {			// north
    		if (isFree(r-2, c-1) && (isFree(r+2, c)) && (isFree(r-2, c+1))) {
    			arr[1]--;	// r--
    			arr[0] = 1;
    		}
    	} else if (bot.getDirection() == 2) {	// east
    		if (isFree(r-1, c-2) && (isFree(r, c-2)) && (isFree(r+1, c-2))) {
    			arr[2]--;	// c--
    			arr[0] = 1;
    		}
    	} else if (bot.getDirection() == 3) {	// south
    		if (isFree(r+2, c-1) && (isFree(r+2, c)) && (isFree(r+2, c+1))) {
    			arr[1]++;
    			arr[0] = 1;
    		}
    	} else if(bot.getDirection() == 4) {	// west
    		if (isFree(r-1, c+2) && (isFree(r, c+2)) && (isFree(r+1, c-2))) {
    			arr[2]++;
    			arr[0] = 1;
    		}
    	}
    	return arr;
    }
    
    private int[] checkLeftForwardAvailable(int r, int c) {
    	int[] arr = {0, r, c};
    	boolean front = false;
		boolean left = false;
    	// check whole 3x3 region above robot is available and left 3x3 region is also available
		int[] dif = changesWithAngle(90);
		// north
		if (bot.getDirection() == 1) {
			front = isFree(r+4,c-1) && isFree(r+4,c) && isFree(r+4,c+1) &&
					isFree(r+3,c-1) && isFree(r+3,c) && isFree(r+3,c+1) &&
					isFree(r+2,c-1) && isFree(r+2,c) && isFree(r+2,c+1);
			left  = isFree(r+4,c-4) && isFree(r+4,c-3) && isFree(r+4,c-2) &&
					isFree(r+3,c-4) && isFree(r+3,c-3) && isFree(r+3,c-2) &&
					isFree(r+2,c-4) && isFree(r+2,c-3) && isFree(r+2,c-2);
			if (front && left ) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[0];
				arr[2] = arr[2] - dif[1];
			}
			
		} 
		// east
		else if (bot.getDirection() == 2) {
			front = isFree(r+1,c+2) && isFree(r+1,c+3) && isFree(r+1,c+4) &&
					isFree(r  ,c+2) && isFree(r  ,c+3) && isFree(r  ,c+4) &&
					isFree(r-1,c+2) && isFree(r-1,c+3) && isFree(r+2,c+4);
			left  = isFree(r+4,c+2) && isFree(r+4,c+3) && isFree(r+4,c+4) &&
					isFree(r+3,c+2) && isFree(r+3,c+3) && isFree(r+3,c+4) &&
					isFree(r+2,c+2) && isFree(r+2,c+3) && isFree(r+2,c+4);
			if (front && left) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[1];
				arr[2] = arr[2] + dif[0];
			}
			
		}
		// south
		else if (bot.getDirection() == 3) {
			front = isFree(r-2,c-1) && isFree(r-2,c  ) && isFree(r-2,c+1) &&
					isFree(r-3,c-1) && isFree(r-3,c  ) && isFree(r-3,c+1) &&
					isFree(r-4,c-1) && isFree(r-4,c  ) && isFree(r-4,c+1);
			left  = isFree(r-2,c+2) && isFree(r-2,c+3) && isFree(r-2,c+4) &&
					isFree(r-3,c+2) && isFree(r-3,c+3) && isFree(r-3,c+4) &&
					isFree(r-4,c+2) && isFree(r-4,c+3) && isFree(r-4,c+4);
			if (front && left) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[0];
				arr[2] = arr[2] + dif[1];
			}
			
		}
		// west
		else if (bot.getDirection() == 4) {
			front = isFree(r+1,c-4) && isFree(r+1,c-3) && isFree(r+1,c-2) &&
					isFree(r  ,c-4) && isFree(r  ,c-3) && isFree(r  ,c-2) &&
					isFree(r-1,c-4) && isFree(r-1,c-3) && isFree(r-1,c-2);
			left  = isFree(r-2,c-4) && isFree(r-2,c-3) && isFree(r-2,c-2) &&
					isFree(r-3,c-4) && isFree(r-3,c-3) && isFree(r-3,c-2) &&
					isFree(r-4,c-4) && isFree(r-4,c-3) && isFree(r-4,c-2);
			if (front && left) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[1];
				arr[2] = arr[2] - dif[0];
			}
			
		}
    	return arr;
    }
    
    private int[] checkRightForwardAvailable(int r, int c) {
    	int[] arr = {0, r, c};
    	boolean front = false;
		boolean right = false;
    	// check whole 3x3 region above robot is available and left 3x3 region is also available
		int[] dif = changesWithAngle(90);
		// north
		if (bot.getDirection() == 1) {
			front = isFree(r+4,c-1) && isFree(r+4,c) && isFree(r+4,c+1) &&
					isFree(r+3,c-1) && isFree(r+3,c) && isFree(r+3,c+1) &&
					isFree(r+2,c-1) && isFree(r+2,c) && isFree(r+2,c+1);
			right = isFree(r+4,c+2) && isFree(r+4,c+3) && isFree(r+4,c+4) &&
					isFree(r+3,c+2) && isFree(r+3,c+3) && isFree(r+3,c+4) &&
					isFree(r+2,c+2) && isFree(r+2,c+3) && isFree(r+2,c+4);
			if (front && right ) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[0];
				arr[2] = arr[2] + dif[1];
			}
			
		} 
		// east
		else if (bot.getDirection() == 2) {
			front = isFree(r+1,c+2) && isFree(r+1,c+3) && isFree(r+1,c+4) &&
					isFree(r  ,c+2) && isFree(r  ,c+3) && isFree(r  ,c+4) &&
					isFree(r-1,c+2) && isFree(r-1,c+3) && isFree(r+2,c+4);
			right = isFree(r-2,c+2) && isFree(r-2,c+3) && isFree(r-2,c+4) &&
					isFree(r-3,c+2) && isFree(r-3,c+3) && isFree(r-3,c+4) &&
					isFree(r-4,c+2) && isFree(r-4,c+3) && isFree(r-4,c+4);
			if (front && right) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[1];
				arr[2] = arr[2] + dif[0];
			}
			
		}
		// south
		else if (bot.getDirection() == 3) {
			front = isFree(r-2,c-1) && isFree(r-2,c  ) && isFree(r-2,c+1) &&
					isFree(r-3,c-1) && isFree(r-3,c  ) && isFree(r-3,c+1) &&
					isFree(r-4,c-1) && isFree(r-4,c  ) && isFree(r-4,c+1);
			right = isFree(r-2,c-4) && isFree(r-2,c-3) && isFree(r-2,c-2) &&
					isFree(r-3,c-4) && isFree(r-3,c-3) && isFree(r-3,c-2) &&
					isFree(r-4,c-4) && isFree(r-4,c-3) && isFree(r-4,c-2);
			if (front && right) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[0];
				arr[2] = arr[2] - dif[1];
			}
			
		}
		// west
		else if (bot.getDirection() == 4) {
			front = isFree(r+1,c-4) && isFree(r+1,c-3) && isFree(r+1,c-2) &&
					isFree(r  ,c-4) && isFree(r  ,c-3) && isFree(r  ,c-2) &&
					isFree(r-1,c-4) && isFree(r-1,c-3) && isFree(r-1,c-2);
			right = isFree(r+4,c-4) && isFree(r+4,c-3) && isFree(r+4,c-2) &&
					isFree(r+3,c-4) && isFree(r+3,c-3) && isFree(r+3,c-2) &&
					isFree(r+2,c-4) && isFree(r+2,c-3) && isFree(r+2,c-2);
			if (front && right) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[1];
				arr[2] = arr[2] - dif[0];
			}
			
		}
    	return arr;
    }
    
    private int[] checkLeftBackwardAvailable(int r, int c) {
    	int[] arr = {0, r, c};
    	boolean back = false;
		boolean left = false;
    	// check whole 3x3 region above robot is available and left 3x3 region is also available
		int[] dif = changesWithAngle(90);
		// north
		if (bot.getDirection() == 1) {
			back = 	isFree(r-2,c-1) && isFree(r-2,c  ) && isFree(r-2,c+1) &&
					isFree(r-3,c-1) && isFree(r-3,c  ) && isFree(r-3,c+1) &&
					isFree(r-4,c-1) && isFree(r-4,c  ) && isFree(r-4,c+1);
			left =  isFree(r-2,c-4) && isFree(r-2,c-3) && isFree(r-2,c-2) &&
					isFree(r-3,c-4) && isFree(r-3,c-3) && isFree(r-3,c-2) &&
					isFree(r-4,c-4) && isFree(r-4,c-3) && isFree(r-4,c-2);
			if (back && left ) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[0];
				arr[2] = arr[2] - dif[1];
			}
			
		} 
		// east
		else if (bot.getDirection() == 2) {
			back =  isFree(r+1,c-4) && isFree(r+1,c-3) && isFree(r+1,c-2) &&
					isFree(r  ,c-4) && isFree(r  ,c-3) && isFree(r  ,c-2) &&
					isFree(r-1,c-4) && isFree(r-1,c-3) && isFree(r+2,c-2);
			left =  isFree(r+4,c-4) && isFree(r+4,c-3) && isFree(r+4,c-2) &&
					isFree(r+3,c-4) && isFree(r+3,c-3) && isFree(r+3,c-2) &&
					isFree(r+2,c-4) && isFree(r+2,c-3) && isFree(r+2,c-2);
			if (back && left) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[1];
				arr[2] = arr[2] - dif[0];
			}
			
		}
		// south
		else if (bot.getDirection() == 3) {
			back =  isFree(r+4,c-1) && isFree(r+4,c  ) && isFree(r+4,c+1) &&
					isFree(r+3,c-1) && isFree(r+3,c  ) && isFree(r+3,c+1) &&
					isFree(r+2,c-1) && isFree(r+2,c  ) && isFree(r+2,c+1);
			left =  isFree(r+4,c+2) && isFree(r+4,c+3) && isFree(r+4,c+4) &&
					isFree(r+3,c+2) && isFree(r+3,c+3) && isFree(r+3,c+4) &&
					isFree(r+2,c+2) && isFree(r+2,c+3) && isFree(r+2,c+4);
			if (back && left) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[0];
				arr[2] = arr[2] + dif[1];
			}
			
		}
		// west
		else if (bot.getDirection() == 4) {
			back  = isFree(r+1,c+2) && isFree(r+1,c+3) && isFree(r+1,c+4) &&
					isFree(r  ,c+2) && isFree(r  ,c+3) && isFree(r  ,c+4) &&
					isFree(r-1,c+2) && isFree(r-1,c+3) && isFree(r-1,c+4);
			left  = isFree(r-2,c+2) && isFree(r-2,c+3) && isFree(r-2,c+4) &&
					isFree(r-3,c+2) && isFree(r-3,c+3) && isFree(r-3,c+4) &&
					isFree(r-4,c+2) && isFree(r-4,c+3) && isFree(r-4,c+4);
			if (back && left) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[1];
				arr[2] = arr[2] + dif[0];
			}
			
		}
    	return arr;
    }
    
    private int[] checkRightBackwardAvailable(int r, int c) {
    	int[] arr = {0, r, c};
    	boolean back = false;
		boolean right = false;
    	// check whole 3x3 region above robot is available and left 3x3 region is also available
		int[] dif = changesWithAngle(90);
		// north
		if (bot.getDirection() == 1) {
			back = 	isFree(r-2,c-1) && isFree(r-2,c  ) && isFree(r-2,c+1) &&
					isFree(r-3,c-1) && isFree(r-3,c  ) && isFree(r-3,c+1) &&
					isFree(r-4,c-1) && isFree(r-4,c  ) && isFree(r-4,c+1);
			right = isFree(r-2,c+2) && isFree(r-2,c+3) && isFree(r-2,c+4) &&
					isFree(r-3,c+2) && isFree(r-3,c+3) && isFree(r-3,c+4) &&
					isFree(r-4,c+2) && isFree(r-4,c+3) && isFree(r-4,c+4);
			if (back && right) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[0];
				arr[2] = arr[2] + dif[1];
			}
			
		} 
		// east
		else if (bot.getDirection() == 2) {
			back =  isFree(r+1,c-4) && isFree(r+1,c-3) && isFree(r+1,c-2) &&
					isFree(r  ,c-4) && isFree(r  ,c-3) && isFree(r  ,c-2) &&
					isFree(r-1,c-4) && isFree(r-1,c-3) && isFree(r+2,c-2);
			right = isFree(r-2,c-4) && isFree(r-2,c-3) && isFree(r-2,c-2) &&
					isFree(r-3,c-4) && isFree(r-3,c-3) && isFree(r-3,c-2) &&
					isFree(r-4,c-4) && isFree(r-4,c-3) && isFree(r-4,c-2);
			if (back && right) {
				arr[0] = 1;
				arr[1] = arr[1] - dif[1];
				arr[2] = arr[2] - dif[0];
			}
			
		}
		// south
		else if (bot.getDirection() == 3) {
			back =  isFree(r+4,c-1) && isFree(r+4,c  ) && isFree(r+4,c+1) &&
					isFree(r+3,c-1) && isFree(r+3,c  ) && isFree(r+3,c+1) &&
					isFree(r+2,c-1) && isFree(r+2,c  ) && isFree(r+2,c+1);
			right = isFree(r+4,c-4) && isFree(r+4,c-3) && isFree(r+4,c-2) &&
					isFree(r+3,c-4) && isFree(r+3,c-3) && isFree(r+3,c-2) &&
					isFree(r+2,c-4) && isFree(r+2,c-3) && isFree(r+2,c-2);
			if (back && right) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[0];
				arr[2] = arr[2] - dif[1];
			}
			
		}
		// west
		else if (bot.getDirection() == 4) {
			back  = isFree(r+1,c+2) && isFree(r+1,c+3) && isFree(r+1,c+4) &&
					isFree(r  ,c+2) && isFree(r  ,c+3) && isFree(r  ,c+4) &&
					isFree(r-1,c+2) && isFree(r-1,c+3) && isFree(r-1,c+4);
			right = isFree(r+4,c+2) && isFree(r+4,c+3) && isFree(r+4,c+4) &&
					isFree(r+3,c+2) && isFree(r+3,c+3) && isFree(r+3,c+4) &&
					isFree(r+2,c+2) && isFree(r+2,c+3) && isFree(r+2,c+4);
			if (back && right) {
				arr[0] = 1;
				arr[1] = arr[1] + dif[1];
				arr[2] = arr[2] + dif[0];
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
	private int[] changesWithAngle(int angle) {
		int[] dif = {3,3};
		if (angle == 90) {
			return dif;
		}
		return dif;
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
                	case 1:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET;
                		y1 = _mapCells[mapRow][mapCol].cellY;
                		width = GraphicsConstants.CELL_SIZE;
                		height = GraphicsConstants.IMAGE_SIZE;
                		break;
                	
                	// east
                	case 2:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET + GraphicsConstants.CELL_SIZE - GraphicsConstants.IMAGE_SIZE;
                		y1 = _mapCells[mapRow][mapCol].cellY;
                		width = GraphicsConstants.IMAGE_SIZE;
                		height = GraphicsConstants.CELL_SIZE;
                		break;
                		
                	// south
                	case 3:
                		x1 = _mapCells[mapRow][mapCol].cellX + GraphicsConstants.MAP_X_OFFSET;
                		y1 = _mapCells[mapRow][mapCol].cellY + GraphicsConstants.CELL_SIZE - GraphicsConstants.IMAGE_SIZE;
                		width = GraphicsConstants.CELL_SIZE;
                		height = GraphicsConstants.IMAGE_SIZE;
                		break;
                	
                	// west
                	case 4:
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
        
        g.fillOval((int) ((c - 1) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - (r * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET)), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);

        // Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsConstants.C_ROBOT_DIR);
        
        int d = bot.getDirection();
        switch (d) {
        	// North
            case 1:
                g.fillOval( (int) (c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 5), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // East
            case 2:
                g.fillOval( (int) (c * GraphicsConstants.CELL_SIZE + 35 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // South
            case 3:
                g.fillOval((int) (c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 35), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // West
            case 4:
                g.fillOval((int) (c * GraphicsConstants.CELL_SIZE - 15 + GraphicsConstants.MAP_X_OFFSET), (int) (GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10), GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            
        }
        
        if (isDrawCellsToArrive) {
        	// printObstacle();
        	// findCellsToArrive();
        	for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
        		int crow = cellsToArrive[i][0];
        		int ccol = cellsToArrive[i][1];
        		int cdirection = cellsToArrive[i][2];
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
