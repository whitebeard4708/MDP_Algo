package algo;

import robot.RobotConstants;
import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node>{
	public int row;
	public int col;
	public int direction;
	public double h = 0;
	public double g = RobotConstants.INFINITE_COST;
	public double f = RobotConstants.INFINITE_COST;
	public String coming_path;
	public Node parent;
	public List<Edge> children;
	
	public Node(int row, int col, int direction) {
		this.row = row;
		this.col = col;
		this.direction = direction;
		this.coming_path = "+";
		children = new ArrayList<Edge>();
	}
	
	@Override
    public int compareTo(Node n) {
          return Double.compare(this.f, n.f);
    }
	
	public double[] getPosition() {
		double[] ans = {row+0.5, col+0.5, direction};
		return ans;
	}
	
	public boolean closeEnough(Node n) {
		double distance = Math.sqrt(Math.pow(this.row - n.row, 2) + Math.pow(this.col - n.col, 2));
		return distance < 0.3 && this.direction == n.direction;
	}
	
	public static class Edge {
        Edge(double weight, Node node, String path){
              this.weight = weight;
              this.node = node;
              this.path = path;
        }

        public double weight;
        public Node node;
        public String path;		// path from parent to child
  }
	
	 public void addChild(double weight, Node node, String path){
        Edge newEdge = new Edge(weight, node, path);
        children.add(newEdge);
	 }

}
