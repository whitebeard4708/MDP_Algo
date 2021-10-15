package algo;

import maps.MapConstants;
import maps.Map;
import robot.Robot;
import robot.RobotConstants;
import utils.CommMgr;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FastestPath {
	private Robot bot;
	private Map map;
	private String[] paths;
	private int[] order;
	private Node[] nodes;
	
	public FastestPath(Map map, Robot bot) {
		this.map = map;
		this.bot = bot;
		getData();
	}
	
	
	public void runFastestPath() {
		// get info from Storage
		
		 // Run path from 1 node to another, take picture
		for (int i=0; i<map.getNumPositions(); i++) {
			System.out.println("===========================================");
			
			int obs_id = (int) map.getPositionToArrive(order[i]-1)[3];
			System.out.println("Obstacle id: " + obs_id);
			
			Node g = nodes[order[i]]; // goal
			Node s = nodes[0]; // start
			if (i != 0) {
				s = nodes[order[i-1]];
			}
			System.out.println(String.format("Move from (%.1f,%.1f,%c) to (%.1f,%.1f,%c)",
					s.row+.5, s.col+.5, charDir(s.direction),
					g.row+.5, g.col+.5, charDir(g.direction)));
			String path = paths[i];
			System.out.println("Simulator path: " + path);
			String rpath = Storage.robotPath(path);
			System.out.println(rpath);
			if (bot.getRealBot()) {
				path = Storage.robotPath(path);
				System.out.println("Robot path: " + path);
			}
			
			
			String[] steps = path.split("/");
			
			// execute path
			for (String step: steps) {
				
				try {
					Thread.sleep(1500);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (bot.getRealBot()) {
					CommMgr.getCommMgr().sendMsg(step, CommMgr.toSTM);
					// successfully bit
					char b = '-';
					while ( b != 'A') {
						String rcvMsg = CommMgr.getCommMgr().recvMsg();
						b = rcvMsg.charAt(rcvMsg.length()-1);
					}
				}
				// get new position after step
				double[] new_pos = bot.posAfterMove(step);
				System.out.println(step + ": " + Arrays.toString(new_pos));
				bot.setRobotPos(new_pos[0], new_pos[1]);
				bot.setDirection((int) new_pos[2]);
				
				map.repaint();
				
				// send new position of robot to ANDROID
				if (bot.getRealBot()) {
					// POS__7__8__N
					String msg = String.format("POS__%d__%d__%c",(int) new_pos[1], (int) new_pos[0], charDir((int)new_pos[2]));
					CommMgr.getCommMgr().sendMsg(msg, CommMgr.toAndroid);
					// successfully bit
					String bAnd = "1";
					while ( ! bAnd.equals("1")) {
						String rcvMsg = CommMgr.getCommMgr().recvMsg();
						bAnd = rcvMsg;
					}
				}
			}
			// take picture
			if (bot.getRealBot()) {
				String msg = "TP__" + obs_id;
				//String msg = "TP__";
				CommMgr.getCommMgr().sendMsg(msg, CommMgr.toRPI);
				// String image_id = MapConstants.BULL_EYE;
				String rcvMsg = CommMgr.getCommMgr().recvMsg();
				// image_id = rcvMsg.substring(2);
				
			}
		}
	}
	
	private void getData() {
		paths = Storage.getFastestPath();
		order = Storage.getFastestOrder();
		nodes = Storage.getNodes();
	}
	
	private char charDir(int dir) {
		if (dir == 0) return 'E';
		else if (dir == 1 || dir == 90) return 'N';
		else if (dir == 2 || dir == 180) return 'W';
		else return 'S';
	} 
	
}
