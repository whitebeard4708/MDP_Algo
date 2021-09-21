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
		for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
			System.out.println("===========================================");
			Node g = nodes[order[i]];
			Node s = nodes[0]; // start
			if (i != 0) {
				s = nodes[order[i-1]];
			}
			System.out.println(String.format("Move from (%.1f,%.1f,%c) to (%.1f,%.1f,%c)",
					s.row+.5, s.col+.5, charDir(s.direction),
					g.row+.5, g.col+.5, charDir(g.direction)));
			String path = paths[i];
			System.out.println(path);
			String[] steps = path.split("/");
			
			// execute path
			for (String step: steps) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (bot.getRealBot()) {
					CommMgr.getCommMgr().sendMsg(step, CommMgr.toSTM);
					// successfully bit
					char b = '-';
					while ( b != '1') {
						String rcvMsg = CommMgr.getCommMgr().recvMsg();
						b = rcvMsg.charAt(rcvMsg.length());
					}
				}
				// get new position after step
				double[] new_pos = bot.posAfterMove(step);
				System.out.println(step + ": " + Arrays.toString(new_pos));
				bot.setRobotPos(new_pos[0], new_pos[1]);
				bot.setDirection((int) new_pos[2]);
				
				map.repaint();
			}
			// take picture
			if (bot.getRealBot()) {
				CommMgr.getCommMgr().sendMsg("TP", CommMgr.toRPI);
				char b = '-';
				while ( b != '1') {
					String rcvMsg = CommMgr.getCommMgr().recvMsg();
					b = rcvMsg.charAt(rcvMsg.length());
				}
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
