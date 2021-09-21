package algo;

import maps.MapConstants;
import maps.Map;
import robot.Robot;
import robot.RobotConstants;
import utils.CommMgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class Storage {
	private static Storage storage;
	
	private static Map map;
	private static Robot bot;
	private static long astarCount = 0;
	private static int hCount = 0;
	private static boolean init = false;
	private static Node[][][] store;
	private static Node[] nodes;
	private static ArrayList<Integer> notFirstNode;
	private static double[][] pathCost;
	private static String[][] paths; // path[i][j] means from node i to node j
	
	// for fastest path
	private static int[] fastest_order = {1,2,3,4,5};
	private static String[] optimalPath;
	
	// for Hamiltonian path
	private static int[] good_order = {1,2,3,4,5};
	private static String[] goodPath;
	
	private Storage(Map map, Robot bot) {
		this.map = map;
		this.bot = bot;
	}
	
	public static Storage getStorage(Map map, Robot bot) {
		if (storage == null) {
			storage = new Storage(map, bot);
		}
		return storage;
		
	}
	
	public static Node[] getNodes() {
		return nodes;
	}
	
	/**
	 * Update optimalPath & order of nodes to arrive for fastest path
	 */
	public static String[] getFastestPath() {

		// Graph of nodes and weights (pathCost)
		// Find shortest Hamiltonian path
		// return index array of sorted obstacles to run
		int[] order = {1,2,3,4,5};
		double[] min_cost = {RobotConstants.INFINITE_COST};
		hCount = 0;
		findShortestOrder(order, order.length, min_cost);
		// System.out.println("--------------------------------------------------");
		// System.out.println(Arrays.toString(fastest_order));
		
		// print path
		System.out.println("Fastest path: Search through " + hCount + " combinations");
		System.out.println("Minimum cost: " + min_cost[0]);
		System.out.println(Arrays.toString(fastest_order));
		
		optimalPath = new String[MapConstants.NUM_OBSTACLE];
		System.out.println(paths[0][fastest_order[0]]);
		optimalPath[0] = paths[0][fastest_order[0]];
		for (int i=1; i<5; i++) {
			int s = fastest_order[i-1];
			int g = fastest_order[i];
			optimalPath[i] = paths[s][g];
			System.out.println(paths[s][g]);
		}
		
		return optimalPath;
	}
	
	
	public static int[] getFastestOrder() {
		return fastest_order;
	}
	
	public static String[] getGoodPath() {

		// Graph of nodes and weights (pathCost)
		// Find good Hamiltonian path
		// return index array of sorted obstacles to run
		int[] order = {1,2,3,4,5};
		double[] cost = {RobotConstants.INFINITE_COST};
		hCount = 0;
		findGoodOrder(order, order.length, cost);
		// System.out.println("--------------------------------------------------");
		// System.out.println(Arrays.toString(fastest_order));
		
		// print path
		System.out.println("Hamiltonian path: Search through " + hCount + " combinations");
		System.out.println("Cost: " + cost[0]);
		System.out.println(Arrays.toString(good_order));
		
		optimalPath = new String[MapConstants.NUM_OBSTACLE];
		System.out.println(paths[0][good_order[0]]);
		goodPath[0] = paths[0][good_order[0]];
		for (int i=1; i<5; i++) {
			int s = good_order[i-1];
			int g = good_order[i];
			goodPath[i] = paths[s][g];
			System.out.println(paths[s][g]);
		}
		
		return goodPath;
	}
	
	public static int[] getGoodOrder() {
		return good_order;
	}
	
	/**
	 * Initialize values of nodes, paths, pathCosts and notFirstNode
	 */
	public static void initValues() {
		notFirstNode = new ArrayList<Integer>();
		int num = MapConstants.NUM_OBSTACLE;
		nodes = new Node[num+1]; // starting node also count
		pathCost = new double [num+1][num+1];
		paths = new String[num+1][num+1];
		
		store = new Node[MapConstants.MAP_ROW][MapConstants.MAP_ROW][4];
		for (int i=0; i<MapConstants.MAP_ROW; i++) {
			for (int j=0; j<MapConstants.MAP_COL; j++) {
				if (map.getCell(i, j).getIsObstacle() || map.getCell(i, j).getIsVirtualWall()) {
					for (int k=0; k<4; k++) {
						Node nn = new Node(i, j, largeDir(k));
						nn.g = 0;
						store[i][j][k] = nn;
					}
				} else {
					for (int k=0; k<4; k++) {
						Node nn = new Node(i, j, largeDir(k));
						nn.g = RobotConstants.INFINITE_COST;
						store[i][j][k] = nn;
					}
				}
			}
		}
		
		
		// add Nodes
		Node start = new Node(1, 1, 90);
		nodes[0] = start;
		for (int i=1; i<=num; i++) {
			double[] pos = map.getPositionToArrive(i-1);
			Node n = new Node((int)pos[0], (int) pos[1], (int) pos[2]);
			nodes[i] = n;
			pathCost[i][i] = 0;
			
			// find cost when move from start to node, only if manhattan distance is acceptable	or rdif.cdif is small
			Node nn = ASTAR(start, nodes[i]);
			if (nn == null) {
				notFirstNode.add(i);
			}
			else {
				Stack<String> ss = printPath(nn);
				String shortpath = shortenPath(ss);
				pathCost[0][i] = calculatePathCost(shortpath);
				paths[0][i] = shortpath;
			}	
		}
		System.out.println("These positions won't be visited first");
		for (int k: notFirstNode) {
			System.out.println(String.format("(%.1f,%.1f,%c)", nodes[k].row+.5, nodes[k].col+.5, charDir(nodes[k].direction)));
		}
		
		// find path cost
		pathCost[0][0] = 0;
		for (int i=1; i<nodes.length; i++) {
			for (int j=i+1; j<nodes.length; j++) {
				double c1, c2;
				String shortpath1, shortpath2;
				Node nn1 = ASTAR(nodes[i], nodes[j]);
				if (nn1 != null) {
					Stack<String> ss1 = printPath(nn1);
					shortpath1 = shortenPath(ss1);
					c1 = calculatePathCost(shortpath1);
				} else {
					c1 = RobotConstants.INFINITE_COST;
					shortpath1 = ""; 
				}
				
				
				Node nn2 = ASTAR(nodes[j], nodes[i]);
				if (nn2 != null) {
					Stack<String> ss2 = printPath(nn2);
					 shortpath2 = shortenPath(ss2);
					c2 = calculatePathCost(shortpath2);
				} else {
					c2 = RobotConstants.INFINITE_COST;
					shortpath2 = "";
				}
					
				if (c1 > c2) {	// choose c2 and shortpath2 for both i->j and j->i
					pathCost[i][j] = c2;
					pathCost[j][i] = c2;
					
					paths[i][j] = reversePath(shortpath2);
					paths[j][i] = shortpath2;
				} else {
					pathCost[i][j] = c1;
					pathCost[j][i] = c1;
					
					paths[i][j] = shortpath1;
					paths[j][i] = reversePath(shortpath1);
				}
			}
		}
	}
	
	/**
	 * Given the graph (nodes, weight), return the order of nodes robot will travel through
	 * and the path cost would be lowest
	 */
	private static void findShortestOrder(int[] a, int size, double[] min_cost) {
		if(size == 1) {
            // (got a new permutation)
			
        	if (notFirstNode.contains(a[0])) return;
        	hCount++;
        	double cost = hamiltonianPathCost(a);
        	if (cost < min_cost[0]) {
        		fastest_order[0] = a[0];
        		fastest_order[1] = a[1];
        		fastest_order[2] = a[2];
        		fastest_order[3] = a[3];
        		fastest_order[4] = a[4];
        		min_cost[0] = cost;
        		//System.out.println(Arrays.toString(fastest_order));
        		//System.out.println(cost);
        	}
            // System.out.println(Arrays.toString(a));
            return;
        }
        for(int i = 0;i < size ;i++) {
        	findShortestOrder(a, size-1, min_cost);
            // always swap the first when odd,
            // swap the i-th when even
            if(size % 2 == 1) {
            	int temp = a[0];
                a[0] = a[size - 1];
                a[size - 1] = temp;
            }
            else {
            	int temp = a[i];
                a[i] = a[size - 1];
                a[size - 1] = temp;
            }
        }
	}
	
	
	private static void findGoodOrder(int[] a, int size, double[] current_cost) {
		if(size == 1) {
            // (got a new permutation)
			
        	if (notFirstNode.contains(a[0])) return;
        	hCount++;
        	double cost = hamiltonianPathCost(a);
        	if (cost < current_cost[0]) {
        		good_order[0] = a[0];
        		good_order[1] = a[1];
        		good_order[2] = a[2];
        		good_order[3] = a[3];
        		good_order[4] = a[4];
        		current_cost[0] = cost;
        		//System.out.println(Arrays.toString(fastest_order));
        		//System.out.println(cost);
        	}
            // System.out.println(Arrays.toString(a));
            return;
        }
        for(int i = 0;i < size ;i++) {
        	findShortestOrder(a, size-1, current_cost);
            // always swap the first when odd,
            // swap the i-th when even
            if(size % 2 == 1) {
            	int temp = a[0];
                a[0] = a[size - 1];
                a[size - 1] = temp;
            }
            else {
            	int temp = a[i];
                a[i] = a[size - 1];
                a[size - 1] = temp;
            }
        }
	}
	
	/**
	 * Find cost of whole Hamiltonian path
	 * @param order Order of nodes that the path will go through
	 * @return Cost of path
	 */
	private static double hamiltonianPathCost(int[] order) {
		double cost = pathCost[0][order[0]];
		for (int i=1; i<order.length; i++) {
			cost += pathCost[order[i-1]][ order[i]];
		}
		return cost;
	}
	
	/**
	 * Find the 'shortest' path from start to goal using A* algorithm
	 */
	public static Node ASTAR (Node start, Node goal) {
		/*
		System.out.println(String.format("Start find path from (%d,%d,%d) to (%d,%d,%d)",
				start.row, start.col, start.direction, goal.row, goal.col, goal.direction));
		*/
		
		// early termination if start is too far from goal
		if (Math.abs(start.row - goal.row) + Math.abs(start.col-goal.col) > 24 || 
				Math.abs(start.row - goal.row) > 15 ||
				Math.abs(start.col - goal.col) > 15) {
			System.out.println(String.format("(%d,%d,%d) is too far from (%d,%d,%d)",
					start.row, start.col, start.direction, goal.row, goal.col, goal.direction));
			return null;
		}
		
		astarCount = 0;
		long max_steps = 200000000;
		
		PriorityQueue<Node> closedList = new PriorityQueue<Node>();
		PriorityQueue<Node> openList = new PriorityQueue<Node>();
		ArrayList<Node> stuck = new ArrayList<Node>();
		
		
		start.h = estimateCostH(start.getPosition(), goal.getPosition());
		start.f = start.g + start.h;
	    openList.add(start);
		
		
		while (!openList.isEmpty() && astarCount < max_steps) {
			// get the smallest f(n)
			Node n = openList.peek();
			
	        if(n.closeEnough(goal)){
	        	/*
	        	System.out.println(String.format("Reach goal (%.1f,%.1f,%d) after %d loops",
	        			goal.row+.5, goal.col+.5, goal.direction, astarCount));
	        	*/
	        	return n;
	        }
			
	        // find all possible moves (edges) from n
	        ArrayList<String> moves = possibleMoves(n.getPosition(), n.coming_path);
	        /*
	        if (n.row == 7 && n.col == 1 && n.direction==90)
	        	System.out.println(String.format("(%d,%d, %d) has %d moves", n.row, n.col, n.direction, moves.size()));
	        */
	        
	        if (moves.isEmpty()) {
	        	stuck.add(n);
	        	openList.remove(n);
		        closedList.add(n);
	        	astarCount++;
	        	continue;
	        }
	        // generate all n children from moves
	        for (String move: moves) {
	        	/*
	        	if (n.row == 7 && n.col == 1 && n.direction==90)
	        		System.out.println(move);
	        	*/
	        	// path | destination row | destination col
	        	String[] parts = move.split("/");			
	        	// System.out.println(parts[0] + " " + parts[1] + " " + parts[2]);
	        	double rd = Double.parseDouble(parts[1]);
	        	double cd = Double.parseDouble(parts[2]);
	        	int directiond = n.direction;
	        	
	        	// turn left F or right B --> direction += 90
	        	if (parts[0].charAt(0) == RobotConstants.LEFT_FORWARD || parts[0].charAt(0) == RobotConstants.RIGHT_BACKWARD) {
	        		directiond += 90;
	        		if (directiond > 180) directiond -= 360;
	        	} 
	        	
	        	// turn right F or left B --> direction -= 90
	        	else if (parts[0].charAt(0) == RobotConstants.RIGHT_FORWARD || parts[0].charAt(0) == RobotConstants.LEFT_BACKWARD) {
	        		directiond -= 90;
	        		if (directiond <= -180) directiond += 360;
	        	}
	        	Node new_child = store[(int)rd][(int) cd][smallDir(directiond)];
	        	
	        	// add weight to edge
	        	double weight = RobotConstants.TURN_COST;
	        	if (parts[0].charAt(0) == RobotConstants.BACKWARD || parts[0].charAt(0) == RobotConstants.FORWARD)
	        		weight = RobotConstants.MOVE_COST;
	        	
	        	n.addChild(weight, new_child, parts[0]);
	        }
	        
	        // traverse all children of n
	        for (Node.Edge e: n.children) {
	        	Node child = e.node;
	        	double newf = n.g + e.weight;
	        	double child_h = estimateCostH(child.getPosition(), goal.getPosition());
	        	if(!openList.contains(child) && !closedList.contains(child)){
	                child.parent = n;
	                child.g = newf;
	                child.f = child.g + child_h;
	                child.coming_path = e.path;
	                //System.out.println(String.format("Node (%d,%d,%d) -> Child node (%d,%d,%d) with path %s",
	                //		n.row, n.col, n.direction,child.row, child.col, child.direction, child.coming_path));
	                openList.add(child);
	            } else {
	            	// if lower costF, then update new parent
	                if(newf < child.g){
	                	child.parent = n;				// new parent
	                	child.g = newf;					
	                	child.f = child.g + child_h;	// new cost
	                	child.coming_path = e.path;		// new coming path
	                	//System.out.println(String.format("Node (%d,%d,%d) -> Child node (%d,%d,%d) with path %s",
	                	//		n.row, n.col, n.direction, child.row, child.col, child.direction, child.coming_path));
	                    if(!stuck.contains(child) && closedList.contains(child)){
	                        closedList.remove(child);
	                        openList.add(child);
	                    }
	                }
	            }
	        }
	        openList.remove(n);
	        closedList.add(n);
	        
			astarCount++;
		}
		
		return null;
		
	}
	
	/**
	 * Print path from target to start
	 * @param target
	 */
	public static Stack<String> printPath(Node target){
		Stack<String> ss = new Stack<>();
	    Node n = target;
	    if(n==null)
	        return ss;

	    while(n.parent != null){
	        // System.out.println(String.format("(%d,%d,%d) path: %s", n.row, n.col, n.direction, n.coming_path));
	        ss.add(n.coming_path);
	        n = n.parent;
	    }
	    return ss;
	}
	
	/**
	 * Combine 
	 * @param target
	 */
	public static String shortenPath(Stack<String> ss) {
		String shorten = "";
		String lastmove = "*";
		
		if (ss.isEmpty()) return shorten;
		
		while (!ss.isEmpty()) {
			String currmove = ss.pop();
			char c = currmove.charAt(0);
			if ( lastmove.charAt(0) == c && (c == RobotConstants.FORWARD || c == RobotConstants.BACKWARD)) {
				int combine = Integer.parseInt(lastmove.substring(1)) + Integer.parseInt(currmove.substring(1));
				lastmove = String.format("%c%d", c, combine);
			} else {
				shorten = shorten + "/" + lastmove;
				lastmove = currmove;
			}
		}
		shorten = shorten + "/" + lastmove;
		//System.out.println(shorten);
		return shorten.substring(3);
	}
	
	private static double calculatePathCost(String path) {
		
		if (path.length() == 0) return RobotConstants.INFINITE_COST;
		
		double cost = 0; 
		String[] steps = path.split("/");
		for (String step: steps) {
			double x = RobotConstants.TURN_COST;
			if (step.charAt(0) == RobotConstants.FORWARD || step.charAt(0) == RobotConstants.BACKWARD) {
				x = RobotConstants.MOVE_COST * 1.0 * Integer.parseInt(step.substring(1)) / 10;
			}
			cost += x;
		}
		
		return cost;
	}
	
	public static String reversePath(String path) {
		String r = "";
		if (path.length() == 0) return r;
		
		String[] steps = path.split("/");
		
		for (int i=steps.length-1; i>=0; i--) {
			String step = steps[i];
			char cp = step.charAt(0);
			char cr;
			if (cp == RobotConstants.FORWARD) cr = RobotConstants.BACKWARD;
			else if (cp == RobotConstants.BACKWARD) cr = RobotConstants.FORWARD;
			else if (cp == RobotConstants.LEFT_FORWARD) cr = RobotConstants.LEFT_BACKWARD;
			else if (cp == RobotConstants.RIGHT_FORWARD) cr = RobotConstants.RIGHT_BACKWARD;
			else if (cp == RobotConstants.LEFT_BACKWARD) cr = RobotConstants.LEFT_FORWARD;
			else // (cp == RobotConstants.RIGHT_BACKWARD) 
				cr = RobotConstants.RIGHT_FORWARD;
			
			r = r + "/" + cr + step.substring(1);
		}
		
		return r.substring(1);
	}
	
	/**
	 * Find estimated heuristic cost from start to end
	 * @param start Position and direction of start
	 * @param end Position and direction of end
	 * @return 
	 */	
	public static double estimateCostH(double[] start, double[] end) {
		double cost = 0;
		double radius = 3;
		
		// double radius = RobotConstants.TURN_RADIUS;
		
		// depend on direction
		int sdir = (int) start[2];
		int edir = (int) end[2];
		// We find cost by using Manhattan distance |x1-x2| + |y1-y2|
		double rdiff = Math.abs(end[0] - start[0]);
		double cdiff = Math.abs(end[1] - start[1]);
		double r, c;
		
		/*
		// If start and end are very close positions but different direction, cost = 3 turns;
		if (rdiff <= 0.2 && cdiff <= 0.2 && sdir != edir) {
			cost = 3 * RobotConstants.TURN_COST;
			return cost;
		}
		*/
		
		// start and end same direction
		if (sdir == edir) {
			if ( (sdir == 90 || sdir == -90) && (Math.abs(end[1] - start[1]) < 1) ) // same col
				cost = rdiff * RobotConstants.MOVE_COST;
			else if ( (sdir == 0 || sdir == 180) && (Math.abs(end[0] - start[0]) < 1)) // same row
				cost = cdiff * RobotConstants.MOVE_COST;
			else {
				cost = (rdiff +cdiff) * RobotConstants.MOVE_COST + 2*RobotConstants.TURN_COST;
			}
		}
		// start needs turn right
		else if (sdir - 90 == edir || sdir - 90 == edir - 360) {
			if (sdir == 90) { 		// north
				r = start[0] + radius;
				c = start[1] + radius;
			}
			else if (sdir == 0) {	// east
				r = start[0] - radius;
				c = start[1] + radius;
			}
			else if (sdir == -90) { // south
				r = start[0] - radius;
				c = start[1] - radius;
			} else {// west
				r = start[0] + radius;
				c = start[1] - radius;
			}
			cost = (Math.abs(end[0] - r) + Math.abs(end[1] - c)) * RobotConstants.MOVE_COST + RobotConstants.TURN_COST;
		}
		
		// opposite direction
		else if (Math.abs(sdir - edir) == 180) {
			// check if start is 'lower' than end
			boolean check = (sdir==90 && start[0] < end[0])|| (sdir==0 && start[1] < end[1]) || (sdir==-90 && start[0]>end[0]) || (sdir==180 && start[1]>end[1]);
			cost = (rdiff + cdiff) * RobotConstants.MOVE_COST + 2*RobotConstants.TURN_COST;
			
			// if start is 'higher' than end, add 2 more turns
			if (!check) {
				cost += 2 * RobotConstants.TURN_COST;
			}
		}
		
		// start needs to turn left
		else if (sdir + 90 == edir || sdir + 90 == edir + 360) {
			if (sdir == 90) { 		// north
				r = start[0] + radius;
				c = start[1] - radius;
			}
			else if (sdir == 0) {	// east
				r = start[0] + radius;
				c = start[1] + radius;
			}
			else if (sdir == -90) { // south
				r = start[0] - radius;
				c = start[1] + radius;
			} else {// west
				r = start[0] - radius;
				c = start[1] - radius;
			}
			cost = (Math.abs(end[0] - r) + Math.abs(end[1] - c)) * RobotConstants.MOVE_COST + RobotConstants.TURN_COST;
		}
		return cost;
	}


	/**
	 * Find all possible moves that robot can do at start position & direction, except for its last move
	 * Also take obstacles into consideration
	 * @param start
	 * @return List of primary instructions, subset of ["W10", "S10", "A90", "D90", "Z90", "C90"]. Check RobotConstants.java for details
	 */
	private static ArrayList<String> possibleMoves(double[] start, String lastmove) {
		char c = lastmove.charAt(0);
		ArrayList<String> ans = new ArrayList<String>();
		
		// forward 10cm and last move isn't backward
		double[] forward = map.checkForwardAvailable(start, 10);
		if (forward[2] == 1 && c != RobotConstants.BACKWARD) {
			String s = String.format("%c10/%.2f/%.2f", RobotConstants.FORWARD, forward[0], forward[1]);
			ans.add(s);
		}
		
		// backward 10cm and last move isn't forward
		double[] backward = map.checkBackwardAvailable(start, 10);
		if (backward[2] == 1 && c != RobotConstants.FORWARD) {
			String s = String.format("%c10/%.2f/%.2f", RobotConstants.BACKWARD, backward[0], backward[1]);
			ans.add(s);
		}
		
		// left F 90 and last move isn't LB 90
		double[] leftF = map.checkLeftForwardAvailable(start,90);
		if (leftF[2] == 1 && c != RobotConstants.LEFT_BACKWARD) {
			String s = String.format("%c90/%.2f/%.2f", RobotConstants.LEFT_FORWARD, leftF[0], leftF[1]);
			ans.add(s);
		}
		
		// right F 90 and last move isn't RB 90
		double[] rightF = map.checkRightForwardAvailable(start,90);
		if (rightF[2] == 1 && c != RobotConstants.RIGHT_BACKWARD) {
			String s = String.format("%c90/%.2f/%.2f", RobotConstants.RIGHT_FORWARD, rightF[0], rightF[1]);
			ans.add(s);
		}
		
		// left B 90 and last move isn't LF 90
		double[] leftB = map.checkLeftBackwardAvailable(start,90);
		if (leftB[2] == 1 && c != RobotConstants.LEFT_FORWARD) {
			String s = String.format("%c90/%.2f/%.2f", RobotConstants.LEFT_BACKWARD, leftB[0], leftB[1]);
			ans.add(s);
		}
		// right B 90 and last move isn't RF 90
		double[] rightB = map.checkRightBackwardAvailable(start,90);
		if (rightB[2] == 1 && c != RobotConstants.RIGHT_FORWARD) {
			String s = String.format("%c90/%.2f/%.2f", RobotConstants.RIGHT_BACKWARD, rightB[0], rightB[1]);
			ans.add(s);
		}
		
		return ans;
	}
	
	private static int smallDir(int dir) {
		if (dir == 0) return 0;
		else if (dir == 90) return 1;
		else if (dir == 180) return 2;
		else return 3;
	}
	
	private static int largeDir(int dir) {
		if (dir == 0) return 0;
		else if (dir == 1) return 90;
		else if (dir == 2) return 180;
		else return -90;
	}
	
	private static char charDir(int dir) {
		if (dir == 0) return 'E';
		else if (dir == 1 || dir == 90) return 'N';
		else if (dir == 2 || dir == 180) return 'W';
		else return 'S';
	}
}
