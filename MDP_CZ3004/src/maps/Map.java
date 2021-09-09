package maps;

import robot.Robot;
import robot.RobotConstants;

import java.awt.*;
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
	private int[][] cellsToArrive = new int[MapConstants.NUM_OBSTACLE][3];
	
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
        return row >= 0 && row <= 2 && col >= 0 && col <= 2;
    }
    
    /**
     * Returns true if the row and column values are valid.
     */
    public boolean checkValidCoordinates(int row, int col) {
        return row >= 0 && col >= 0 && row < MapConstants.MAP_ROW	 && col < MapConstants.MAP_COL;
    }
    
    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean getIsObstacleOrWall(int row, int col) {
        return !checkValidCoordinates(row, col) || getCell(row, col).getIsObstacle();
    }
    
    public void setAllExplored() {
    	for (int i=0; i<grid.length; i++) {
    		for (int j=0; j<grid[0].length; j++) {
    			grid[i][j].setIsExplored(true);
    		}
    	}
    }
    
    public void setAllUnexplored() {
    	for (int i=0; i<grid.length; i++) {
    		for (int j=0; j<grid[0].length; j++) {
    			if (!inStartZone(i, j)) {
    				grid[i][j].setIsExplored(false);
    			}
    		}
    	}
    }
    
    public Cell getCell(int row, int col) {
    	return grid[row][col];
    }
    
    public boolean isObstacleCell(int row, int col) {
        return grid[row][col].getIsObstacle();
    }
    
    public void setObstacleCell(int row, int col, boolean is_obstacle) {
    	grid[row][col].setIsObstacled(is_obstacle);
    }
    
    public void setImageSide(int row, int col, int image_side) {
    	grid[row][col].setImageSide(image_side);
    }
    
    public void setImageIdForCell(int row, int col, char image_id) {
    	grid[row][col].setImageId(image_id);
    }
    
    /*
     * Cells for fastest path should be 20cm away from the image in the according direction
     * For example, if obstacle cell is (5, 7) and direction "north"
     * then the robot should be at (7, 7) and facing "south"
     *
     */
    private void setCellsToArrive() {
    	
    	for (int i=0; i<this.numObstacleFound; i++) {
    		
    		int image_side = obstacles[i].getImageSide();
    		int r = obstacles[i].getRow();
    		int c = obstacles[i].getCol();
    		
    		switch (image_side) {
    		
    		// north
    		case 1:
    			cellsToArrive[i][0] = r+2;	// row
    			cellsToArrive[i][1] = c;	// col
    			cellsToArrive[i][2] = 3;	// facing south
    			break;
    			
    		// east
    		case 2:
    			cellsToArrive[i][0] = r;	// row
    			cellsToArrive[i][1] = c+2;	// col
    			cellsToArrive[i][2] = 4;	// facing west
    			break;
    			
    		// south
    		case 3:
    			cellsToArrive[i][0] = r-2;	// row
    			cellsToArrive[i][1] = c;	// col
    			cellsToArrive[i][2] = 1;	// facing north
    			break;
    		
    		// west
    		case 4:
    			cellsToArrive[i][0] = r;	// row
    			cellsToArrive[i][1] = c-2;	// col
    			cellsToArrive[i][2] = 2;	// facing east
    			break;
    		
    		
    		default:
    			break;
    		}
    	}
    }
    
    
    
    public void paintComponent(Graphics g) {
        // Create a two-dimensional array of _DisplayCell objects for rendering.
        _DisplayCell[][] _mapCells = new _DisplayCell[MapConstants.MAP_ROW][MapConstants.MAP_COL];
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROW; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COL; mapCol++) {
                _mapCells[mapRow][mapCol] = new _DisplayCell(mapCol * GraphicsConstants.CELL_SIZE, mapRow * GraphicsConstants.CELL_SIZE, GraphicsConstants.CELL_SIZE);
            }
        }

        // Paint the cells with the appropriate colors.
        for (int mapRow = 0; mapRow < MapConstants.MAP_ROW; mapRow++) {
            for (int mapCol = 0; mapCol < MapConstants.MAP_COL; mapCol++) {
                Color cellColor;

                if (inStartZone(mapRow, mapCol))
                    cellColor = GraphicsConstants.C_START;
                else {
                    if (!grid[mapRow][mapCol].getIsExplored())
                        cellColor = GraphicsConstants.C_UNEXPLORED;
                    else if (grid[mapRow][mapCol].getIsObstacle())
                        cellColor = GraphicsConstants.C_OBSTACLE;
                    else
                        cellColor = GraphicsConstants.C_FREE;
                }

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
        int r = bot.getRow();
        int c = bot.getCol();
        g.fillOval((c - 1) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - (r * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);

        // Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsConstants.C_ROBOT_DIR);
        
        int d = bot.getDirection();
        switch (d) {
        	// Forward
            case 1:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 15, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // Backward
            case 2:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 35, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // Turn Left
            case 3:
                g.fillOval(c * GraphicsConstants.CELL_SIZE - 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            // Turn Right
            case 4:
                g.fillOval(c * GraphicsConstants.CELL_SIZE + 35 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                break;
            
            
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
