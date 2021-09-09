package maps;

public class Cell {
	private final int row;
	private final int col;
	private int image_side;
	/*
	 * 0: no image
	 * 1: North
	 * 2: East
	 * 3: South
	 * 4: West
	 */
	private char image_id;
	private boolean is_obstacle;
	private boolean is_explored;
	private boolean isVirtualWall;
	
	public Cell(int row, int col) {
		this.col = col;
		this.row = row;
		this.image_side = 0;
		this.is_obstacle = false;
	}
	
	public int getRow() {
		return this.row;
	}
	
	public int getCol() {
		return this.col;
	}
	
	public char getImageId() {
		return this.image_id;
	}
	
	public boolean getIsObstacle() {
		return this.is_obstacle;
	}
	
	public boolean getIsExplored() {
		return this.is_explored;
	}
	
	public int getImageSide() {
		return this.image_side;
	}
	
	public boolean getIsVirtualWall() {
        return this.isVirtualWall;
    }
	
	public void setIsObstacled(boolean obstacle) {
		this.is_obstacle = obstacle;
	}
	
	public void setImageSide(int side) {
		this.image_side = side;
	}
	
	public void setIsExplored(boolean explored) {
		this.is_explored = explored;
	}
	
	public void setImageId(char id) {
		this.image_id = id;
	}
	
	public void setVirtualWall(boolean val) {
        if (val) {
            this.isVirtualWall = true;
        } else {
            if (row != 0 && row != MapConstants.MAP_ROW - 1 && col != 0 && col != MapConstants.MAP_COL - 1) {
                this.isVirtualWall = false;
            }
        }
    }
}
