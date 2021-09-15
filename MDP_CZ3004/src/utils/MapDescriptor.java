package utils;

import maps.Map;
import maps.MapConstants;
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
            int num_found_obstacles = 0;
            String line = buf.readLine();
            num_found_obstacles = Integer.parseInt(line);
            
         // Found obstacles
            for (int i=0; i<num_found_obstacles; i++) {
            	line = buf.readLine();
            	String [] obs = new String[4];
            	obs = line.split(",");
            	int row = Integer.parseInt(obs[0]);
            	int col = Integer.parseInt(obs[1]);
            	int image_side = Integer.parseInt(obs[2]);
            	char image_id = obs[3].charAt(0);
            	
            	// add details to map
            	map.addNewImage(row, col, image_side, image_id);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
        
    }

}

