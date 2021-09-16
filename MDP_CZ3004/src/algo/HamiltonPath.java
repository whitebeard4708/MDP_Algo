package algo;

import maps.Cell;
import maps.Map;
import maps.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import utils.CommMgr;

public class HamiltonPath {
	private Robot bot;
	private Map map;
	private int timeLimit;
	
	private long startTime;
	private long endTime;
	
	private int numObstacleLeft = MapConstants.NUM_OBSTACLE;
	private Cell[] obstacles;
	private boolean[] found = {false, false, false, false, false};
	
	
	public HamiltonPath(Robot bot, Map map, int timeLimit) {
		this.bot = bot;
		this.map = map;
		this.timeLimit = timeLimit;
		this.obstacles = map.getObstacles();
	}
	
	public void runHamiltonPath() {
		if (bot.getRealBot()) {
	        System.out.println("Waiting for instruction");

	        CommMgr.getCommMgr().recvMsg();
	        
	        while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = CommMgr.getCommMgr().recvMsg();
                String[] msgArr = msg.split("|");
                if (msgArr[0].equals("AND") && msgArr[2].equals("START_Hamilton")) break;
            }
		}
		
		System.out.println("Starting exploration...");

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg("START", CommMgr.toAndroid);
        }
        
        double[] source = {bot.getRow(), bot.getCol(), bot.getDirection()};
        hamiltonPathLoop(source);
	}
	
	/**
	 * Find path to obstacle and move robot to the according position
	 * @param source
	 */
	private void hamiltonPathLoop(double[] source) {
		for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
			// find next stop
        	int index = findDestination(source);
        	double[] destination = getDestination(index);
        	System.out.println("Next location should be (" + destination[0] + "," + destination[1] + "," + destination[2] + ")");
        	
        	// Go to next stop, also count for error, 
        	// so this is a loop until current_location is the desired stop Cell
        	double[] current_location = source.clone();
    		while (!closeEnough(current_location, destination)) {
        		String path = findPath(current_location, destination);
            	current_location = executePath(current_location, path);
        	}
    		// loop stop, so robot should arrive at destination
        	found[index] = true;
        	numObstacleLeft--;
        	System.out.println("Arrive at correct location (" + bot.getRow() + "," + bot.getCol() + "," + bot.getDirection() + ")");
        	
        	// Scan image
        	char [] scanResult = scanImage();
        	// Send scanned imaged to Android;
        	if (scanResult[0] == '1')
        		sendScannedImage(obstacles[index].getRow(), obstacles[index].getCol(), scanResult[1]);
		}
	}
	
	/**
	 * Find Cell index for the next position to IR, based on given source position
	 * @param source: [row, col, direction] of source
	 * @return index of next obstacle to be scanned
	 */
	private int findDestination(double[] source) {
		// find list of index within unfound obstacles;
		int count = 0;
		int[] indexList = new int[numObstacleLeft];
		for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
			if (!found[i]) {
				indexList[count] = i;
				count++;
			}
		}
		assert count == numObstacleLeft: "Counting unfound obstacle wrong!";
		
		// Now we can search in unfound obstacles
		// index should be in this unfound obstacle index list
		int min_loss = 99999;
		int min_loss_index = -1;
		
		for (int i=0; i<count; i++) {
			double[] estimate_destination = map.getPositionToArrive(indexList[i]);
			// find loss when moving
			int loss1 = calculateLoss(source, estimate_destination);
			// get the minimal loss
			if (loss1 < min_loss) {
				min_loss = loss1;
				min_loss_index = i;
			}
		}
		return min_loss_index;
	}
	
	
	
	private double[] getDestination(int index) {
		double[] destination = map.getPositionToArrive(index);
		return destination;
	}
	
	
	/**
	 * Find estimated loss (time) when moving robot from source to destination
	 * @param source
	 * @param destination
	 * @return
	 */
	private int calculateLoss(double[] source, double[] destination) {
		int loss = 0;
		// based on the direction difference of source and destination
		double direction_dif = Math.abs(source[2] - destination[2]);
		double hdif = 0;
		double vdif = 0;
		if (direction_dif == 0) {		// same direction
			// at least 2 turns
			
			if (source[2] == 1 || source[2] == 3) { // facing north or south
				hdif = Math.abs(source[1] - destination[1]);
				vdif = Math.abs(source[0] - destination[1]);
				
			} else if (source[2] == 2 || source[2] == 4) {
				hdif = Math.abs(source[0] - destination[0]);
				vdif = Math.abs(source[1] - destination[1]);
			}
			loss += 2 + Math.abs(2 - hdif) + 2 + Math.abs(2-vdif);
			
		}
		
		else if (direction_dif == 1) {	// turn left or turn right once
			if (source[2] == 1 || source[2] == 3) { // facing north or south
				hdif = Math.abs(source[1] - destination[1]);
				vdif = Math.abs(source[0] - destination[1]);
				
			} else if (source[2] == 2 || source[2] == 4) {
				hdif = Math.abs(source[0] - destination[0]);
				vdif = Math.abs(source[1] - destination[1]);
			}
			loss += 2 + Math.abs(2 - hdif) + 2 + Math.abs(2-vdif);
			
		}
		return loss;
	}
	
	/**
	 * Find a path for robot to move between source and destination
	 * and avoid obstacle at the same time
	 * @param source
	 * @param destination
	 * @return
	 */
	private String findPath(double[] source, double[] destination) {
		String path = "";
		return path;
	}
	
	/**
	 * Guide the robot to move from current position with written path
	 * @param path
	 * @return Return position and direction of robot
	 */
	private double[] executePath(double[] current_position, String path) {
		// find position based on path
		// and send message to keep track of robot location
		
		if (bot.getRealBot()) {
			// do sth
		}
		
		return current_position;
	}
	
	/**
	 * Perform IR at the current position (Cell to arrive)
	 * @return Return 2 chars, succeed char and image_id scanned
	 */
	private char[] scanImage() {
		char[] result = {'0', MapConstants.UNIDENTIFED_CHAR};
		
		// do sth
		if (bot.getRealBot()) {
			// receive message from RPI
			String startScan = "Scan";
			CommMgr.getCommMgr().sendMsg(startScan, CommMgr.toRPI);
			String msg = CommMgr.getCommMgr().recvMsg();
			
		} else {
			char image_id = MapConstants.UNIDENTIFED_CHAR;
			int direction = bot.getDirection();
			switch (direction) {
			case 1:
				image_id = map.getCell(bot.getRow()-3, bot.getCol()).getImageId();
				result[0] = '1';
				break;
			case 2:
				image_id = map.getCell(bot.getRow(), bot.getCol()+3).getImageId();
				result[0] = '1';
				break;
			case 3:
				image_id = map.getCell(bot.getRow()+3, bot.getCol()).getImageId();
				result[0] = '1';
				break;
			case 4:
				image_id = map.getCell(bot.getRow(), bot.getCol()-3).getImageId();
				result[0] = '1';
				break;
			default:
				break;
			}
			
		}
		return result;
	}
	
	/**
	 * Send info of current scanned obstacle and image_id to Android
	 * @param row
	 * @param col
	 * @param image_id
	 */
	public void sendScannedImage(int row, int col, char image_id) { {
		String msg = String.format("%2d,%2d,%c", row, col, image_id);
		if (bot.getRealBot()) {
			CommMgr.getCommMgr().sendMsg(msg, CommMgr.toAndroid);
		}
		System.out.println("Send image id " + image_id + " to Android");
	}
		// send image id at obstacle
	}
	
	/**
	 * Return true if 2 positions have the direction and next to each other
	 * @param p1 position 1 [row1, col1, direction1]
	 * @param p2 position 2 [row2, col2, direction2]
	 * @return
	 */
	private boolean closeEnough(double[] p1, double[] p2) {
		boolean ans = false;
		if (p1[2] == p2[2]) {
			// north/south and same row
			if ((p1[2] == 1 || p1[2] == 3) && p1[0] == p2[0])
				ans = Math.abs(p1[1] - p2[1]) <= 1;
			// east/west and same col
			else if ((p1[2] == 2 || p1[2] == 4) && p1[1] == p2[1])
				ans = Math.abs(p1[0] - p2[0]) <= 1;
		}
		return ans;
	}
}
