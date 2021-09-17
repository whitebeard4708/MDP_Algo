package algo;

import maps.Cell;
import maps.Map;
import maps.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import utils.CommMgr;

public class ExploreMap {
	private Map map;
    private Robot bot;
    private int coverageLimit;
    private int timeLimit;
    private int areaExplored;
    private long startTime;
    private long endTime;
    private int lastCalibrate;
    private boolean calibrationMode;
    
    public void ExplorationAlgo(Map map, Robot bot, int coverageLimit, int timeLimit) {
        this.map = map;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }    
    
    /**
     * Main method that is called to start the exploration.
     */
    public void runExploration() {
        if (bot.getRealBot()) {
            System.out.println("Starting calibration...");

            CommMgr.getCommMgr().recvMsg();
            /*
            if (bot.getRealBot()) {
                bot.move(MOVEMENT.LEFT, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.LEFT, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.RIGHT, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                CommMgr.getCommMgr().recvMsg();
                bot.move(MOVEMENT.RIGHT, false);
            }
            */

            while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = CommMgr.getCommMgr().recvMsg();
                String[] msgArr = msg.split(";");
                if (msgArr[0].equals("adad")) break;
            }
        }

        System.out.println("Starting exploration...");

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(null, "START");
        }

    }
    
    
    /**
     * Returns true if the right side of the robot is free to move into.
     */
    private boolean lookRight() {
        switch (bot.getDirection()) {
            case 1:		// north
                return eastFree();
            case 2:		// east
                return southFree();
            case 3:		// south
                return westFree();
            case 4:		// west
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    private boolean lookForward() {
        switch (bot.getDirection()) {
            case 1: 	// north
                return northFree();
            case 2:		// east
                return eastFree();
            case 3:		// south
                return southFree();
            case 4:		// west
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move into.
     */
    private boolean lookLeft() {
        switch (bot.getDirection()) {
            case 1:		// north
                return westFree();
            case 2:		// east
                return northFree();
            case 3:		// south
                return eastFree();
            case 4:		// west
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = (int) bot.getRow();
        int botCol = (int) bot.getCol();
        return (isObstacle(botRow + 1, botCol - 1) && isFree(botRow + 1, botCol) && isObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = (int) bot.getRow();
        int botCol = (int) bot.getCol();
        return (isFree(botRow - 1, botCol + 1) && isFree(botRow, botCol + 1) && isFree(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = (int) bot.getRow();
        int botCol = (int) bot.getCol();
        return (isFree(botRow - 1, botCol - 1) && isFree(botRow - 1, botCol) && isFree(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = (int) bot.getRow();
        int botCol = (int) bot.getCol();
        return (isObstacle(botRow - 1, botCol - 1) && isFree(botRow, botCol - 1) && isObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {
        
    }
    
    /**
    * Returns true for cells that are obstacles.
    */
   private boolean isObstacle(int r, int c) {
       return map.checkValidCoordinates(r, c) && map.getCell(r, c).getIsObstacle();
   }
   
   
   private boolean isFree(int r, int c) {
	   return map.checkValidCoordinates(r, c) && !map.getCell(r, c).getIsObstacle();
   }


   /**
    * Moves the bot from (r,d) to (rd, cd) without changing facing too much
    */
   private void moveBot(int rd, int cd) {
	   
	   double r = bot.getRow();
	   double c = bot.getCol();
	   
	   
   }

   /**
    * 
    */
}
