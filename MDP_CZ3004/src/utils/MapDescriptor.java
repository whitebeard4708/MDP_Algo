package utils;

import maps.Map;
import maps.MapConstants;
import robot.Robot;
import maps.Cell;
import java.io.*;

/**
 * Helper methods for reading & generating map strings.
 *
 * Part 1: 1/0 represents explored state. All cells are represented.
 * Part 2: 1/0 represents obstacle state. Only explored cells are represented.
 *
 */

public class MapDescriptor {
    /**
     * Reads filename.txt from disk and loads it into the passed Map object. Uses a simple binary indicator to
     * identify if a cell is an obstacle.
     */
    public static void loadMapFromDisk(Map map, String filename) {
    	
    	/*
    	 * Map format would be like this
    	 * 
    	 * Number of obstacles found (N)
    	 * 20 lines map cells 0: free cell || 1: obstacles (not necessary to be found)
    	 * N lines of found obstacles (row,col,side,char)
    	 */
    	
    	
    	try {
            InputStream inputStream = new FileInputStream("maps/" + filename + ".txt");
            BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));

            // number of obstacles
            String line = buf.readLine();
            
            
         // Found obstacles
            for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
            	line = buf.readLine();
            	String[] obs = line.split(",");
            	int row = Integer.parseInt(obs[0]);
            	int col = Integer.parseInt(obs[1]);
            	int image_side = convertStringToIntDirection(obs[2]);
            	
            	// add details to map
            	map.addNewImage(row, col, image_side);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
        
    }
    
    
    public static void loadMapFromAndroid(Map map, Robot bot) {
    	// sample message from Android: AND|ALG|ADD,O1,10,6#
    	// received from RPI: ADD,01,10,6
    	
    	// connect already
    	if (bot.getRealBot()) {
    		CommMgr comm = CommMgr.getCommMgr();
    		
    		for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
    			
				String pos = comm.recvMsg();
				String face = comm.recvMsg();
				String[] pos_parts = pos.split(",");
				String[] face_parts = face.split(",");
				
				String obs_id = pos_parts[1];
				
				if (!pos_parts[0].equals(comm.ADD)) {
					comm.sendMsg(String.format("Can't identify add obstacle %s", obs_id), comm.toAndroid);
				}
				else if (!face_parts[0].equals(comm.FACE)) {
					comm.sendMsg(String.format("Can't identify image side %s", obs_id), comm.toAndroid);
				} else {
					int row = Integer.parseInt(pos_parts[2]);
					int col = Integer.parseInt(pos_parts[3]);
					String image_side = face_parts[2];
					map.addNewImage(row, col, convertStringToIntDirection(image_side));
				}
    		}
    	}
    }
    
    public static int convertStringToIntDirection(String s) {
    	if (s.equals("N")) return 90;
    	else if (s.equals("E")) return 0;
    	else if (s.equals("S")) return -90;
    	else if (s.equals("W")) return 180;
    	else return 1;
    }

}

