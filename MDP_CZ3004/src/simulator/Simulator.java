package simulator;

import algo.FastestPath;
import algo.HamiltonPath;
import algo.Node;
import algo.Storage;
import maps.Map;
import maps.MapConstants;
import robot.Robot;
import robot.RobotConstants;


import utils.CommMgr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import static utils.MapDescriptor.loadMapFromDisk;
import static utils.MapDescriptor.loadMapFromAndroid;
import static utils.MapDescriptor.convertStringToIntDirection;

/**
 * Simulator for robot navigation in virtual arena.
 */

public class Simulator {
    private static JFrame _appFrame = null;         // application JFrame

    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _buttons = null;          // JPanel for buttons

    private static Robot bot;
    private static Map map;
    private static int speedLimit = robot.RobotConstants.SPEED;				// speed limit
    private static int timeLimit = 3600;            // time limit

    private static final CommMgr comm = CommMgr.getCommMgr();
    private static Storage storage;
    // private static final boolean realRun = false;

    /**
     * Initialize the different maps and displays the application.
     */
    public static void main(String[] args) {
    	bot = new Robot(13.5, 10.5, false);
    	bot.setDirection(-90);
    	map = new Map(bot);
        displayEverything();
    }


    /**
     * Initializes the different parts of the application.
     */
    private static void displayEverything() {
    	// Initialize main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Simulator");
        _appFrame.setSize(new Dimension(900, 750));
        _appFrame.setResizable(false);

        // Center the main frame in the middle of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        _appFrame.setLocation(dim.width / 2 - _appFrame.getSize().width / 2, dim.height / 2 - _appFrame.getSize().height / 2);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());

        // Create the JPanel for the buttons
        _buttons = new JPanel();

        // Add _mapCards & _buttons to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_buttons, BorderLayout.PAGE_END);

        // Initialize the main map view
        initMainLayout();

        // Initialize the buttons
        initButtonsLayout();

        // Display the application
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
    }

    /**
     * Initialize the main map view by adding the different maps as cards in the CardLayout. Displays realMap
     * by default.
     */
    private static void initMainLayout() {
        _mapCards.add(map, "EXPLORATION");

        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        cl.show(_mapCards, "EXPLORATION");
    }

    /**
     * Initialize the JPanel for the buttons.
     */
    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout(2,6));
        /*
         * LoadMap	|| Reset position	|| Cells to arrive	|| Hamiltonian Path	|| Fastest Path	|| Connect RPI
         * Forward	|| Backward			|| Left Forward		|| Right Forward	|| Left Backward|| Right Backward
         */
        addButtons();
    }

    /**
     * Helper method to set particular properties for all the JButtons.
     */
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
    }

    /**
     * Initialize and adds the five main buttons. Also creates the relevant classes (for multithreading) and JDialogs
     * (for user input) for the different functions of the buttons.
     */
    private static void addButtons() {
        // Load Map Button
        JButton btn_LoadMap = new JButton("Load Map");
        formatButton(btn_LoadMap);
        btn_LoadMap.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JDialog loadMapDialog = new JDialog(_appFrame, "Load Map", true);
                loadMapDialog.setSize(400, 100);
                loadMapDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton loadMapButton = new JButton("Load");

                loadMapButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        loadMapDialog.setVisible(false);
                        loadMapFromDisk(map, loadTF.getText());
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
                    }
                });

                loadMapDialog.add(new JLabel("File Name: "));
                loadMapDialog.add(loadTF);
                loadMapDialog.add(loadMapButton);
                loadMapDialog.setVisible(true);
            }
        });
        _buttons.add(btn_LoadMap);
        
        // Reset Position
        JButton btn_ResetPosition = new JButton("Reset position");
        formatButton(btn_ResetPosition);
        btn_ResetPosition.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                bot.setRobotPos(1.5, 1.5);
                bot.setDirection(90);
                map.repaint();
                System.out.println("Reset Position to (1.5,1.5), facing North");
                
            }
        });
        _buttons.add(btn_ResetPosition);
        
        // Cells to arrive
        JButton btn_CellsToArrive = new JButton("Cells to arrive");
        formatButton(btn_CellsToArrive);
        btn_CellsToArrive.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                map.findPositionToArrive();
                map.repaint();
                storage.getStorage(map, bot);
                storage.initValues();
            }
        });
        _buttons.add(btn_CellsToArrive);
        
        // FastestPath Class for Multithreading
        class HamiltonPathSim extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                map.repaint();

                if (bot.getRealBot()) {
                    while (true) {
                        System.out.println("Waiting for HP_START...");
                        String msg = comm.recvMsg();
                        if (msg.equals(CommMgr.HP_START)) break;
                    }
                }
                
                HamiltonPath hp = new HamiltonPath(map, bot);
                hp.runHamiltonPath();

                return 111;
            }
        }
        
        // Hamiltonian Path
        JButton btn_HamiltonianPath = new JButton("Hamiltonian path");
        formatButton(btn_HamiltonianPath);
        btn_HamiltonianPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	
            	// map.repaint();
                System.out.println("Hamiltonian path started");
                new HamiltonPathSim().execute();
                System.out.println("Hamiltonian path ended");
            }
        });
        _buttons.add(btn_HamiltonianPath);
        
        
        // FastestPath Class for Multithreading
        class FastestPathSim extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                map.repaint();

                if (bot.getRealBot()) {
                    while (true) {
                        System.out.println("Waiting for FP_START...");
                        String msg = comm.recvMsg();
                        if (msg.equals(CommMgr.FP_START)) break;
                    }
                }
                
                FastestPath fastestPath;
                fastestPath = new FastestPath(map, bot);
                fastestPath.runFastestPath();

                return 222;
            }
        }
        
        
        // Fastest Path
        JButton btn_FastestPath = new JButton("Fastest path");
        formatButton(btn_FastestPath);
        btn_FastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	// do fastest path
            	System.out.println("Fastest path started");
            	
            	CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "REAL_MAP");
    
            	// Node n1 = new Node(1,1,90);
            	// Node n2 = new Node(10,2,90);
            	// move(n5, n6);
            	// checkList();
            	// move(n1, n2);
            	// move(n3, n4);
                new FastestPathSim().execute();
                // checkList();
            	
            	map.repaint();
            	System.out.println("Fastest path end");
            }
        });
        _buttons.add(btn_FastestPath);
        
        // Connect RPI
        JButton btn_ConnectRPI= new JButton("Connect RPI");
        formatButton(btn_ConnectRPI);
        btn_ConnectRPI.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	int a = comm.openConnection();
            	comm.recvMsg();
            	if (a==1) {	// can connect
            		bot.setRealBot(true);
            		// receive obstacles from Android
            		// comm.sendMsg("test_test", comm.toAndroid);
            		// comm.closeConnection();
            		loadMapFromDisk(map, "newmap1");
            		// String aaaa = comm.recvMsg();
            		CardLayout cl = ((CardLayout) _mapCards.getLayout());
                    cl.show(_mapCards, "REAL_MAP");
                    map.repaint();
            		// loadMapFromAndroid(map, bot);
            	}
            }
        });
        _buttons.add(btn_ConnectRPI);
        
        // Move Forward Button
        JButton btn_Forward = new JButton("Forward (W)");
        formatButton(btn_Forward);
        
        btn_Forward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	JDialog distanceDialog = new JDialog(_appFrame, "Distance input", true);
                distanceDialog.setSize(400, 100);
                distanceDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton distanceButton = new JButton("Next");
            	distanceButton.addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e1) {
            			distanceDialog.setVisible(false);
            			double distance = Double.parseDouble(loadTF.getText());
                        map.moveRobotForward(distance);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
            		}
            	});
            	distanceDialog.add(new JLabel("Move forward by __ cm"));
            	distanceDialog.add(loadTF);
            	distanceDialog.add(distanceButton);
            	distanceDialog.setVisible(true);
            }
        });
        _buttons.add(btn_Forward);
        
        // Move Backward Button
        JButton btn_Backward = new JButton("Backward (S)");
        formatButton(btn_Backward);
        btn_Backward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	JDialog distanceDialog = new JDialog(_appFrame, "Distance input", true);
                distanceDialog.setSize(400, 100);
                distanceDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton distanceButton = new JButton("Next");
            	distanceButton.addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e1) {
            			distanceDialog.setVisible(false);
            			double distance = Double.parseDouble(loadTF.getText());
                        map.moveRobotBackward(distance);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
            		}
            	});
            	distanceDialog.add(new JLabel("Move backward by __ cm"));
            	distanceDialog.add(loadTF);
            	distanceDialog.add(distanceButton);
            	distanceDialog.setVisible(true);
            }
        });
        _buttons.add(btn_Backward);
        
        // Move Left Forward Button
        JButton btn_LeftForward = new JButton("Left F (A)");
        formatButton(btn_LeftForward);
        btn_LeftForward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	
            	JDialog angleDialog = new JDialog(_appFrame, "Angle input", true);
            	angleDialog.setSize(400, 100);
            	angleDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton angleButton = new JButton("Next");
                angleButton.addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e1) {
            			angleDialog.setVisible(false);
            			int angle = Integer.parseInt(loadTF.getText());
            			map.moveRobotLeftF(angle);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
            		}
            	});
                angleDialog.add(new JLabel("Move left forward by __ degree"));
                angleDialog.add(loadTF);
                angleDialog.add(angleButton);
                angleDialog.setVisible(true);
            }
        });
        _buttons.add(btn_LeftForward);
        
        
        // Move Right Forward Button
        JButton btn_RightForward = new JButton("Right F (D)");
        formatButton(btn_RightForward);
        btn_RightForward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	JDialog angleDialog = new JDialog(_appFrame, "Angle input", true);
            	angleDialog.setSize(400, 100);
            	angleDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton angleButton = new JButton("Next");
                angleButton.addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e1) {
            			angleDialog.setVisible(false);
            			int angle = Integer.parseInt(loadTF.getText());
            			map.moveRobotRightF(angle);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
            		}
            	});
                angleDialog.add(new JLabel("Move right forward by __ degree"));
                angleDialog.add(loadTF);
                angleDialog.add(angleButton);
                angleDialog.setVisible(true);
            }
        });
        _buttons.add(btn_RightForward);
        
        
        // Left Backward
        JButton btn_LeftBackward = new JButton("Left B (Z)");
        formatButton(btn_LeftBackward);
        btn_LeftBackward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	JDialog angleDialog = new JDialog(_appFrame, "Angle input", true);
            	angleDialog.setSize(400, 100);
            	angleDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton angleButton = new JButton("Next");
                angleButton.addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e1) {
            			angleDialog.setVisible(false);
            			int angle = Integer.parseInt(loadTF.getText());
            			map.moveRobotLeftB(angle);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
            		}
            	});
                angleDialog.add(new JLabel("Move left backward by __ degree"));
                angleDialog.add(loadTF);
                angleDialog.add(angleButton);
                angleDialog.setVisible(true);
                
            }
        });
        _buttons.add(btn_LeftBackward);
        
        // Right Backward
        JButton btn_RightBackward = new JButton("Right B (C)");
        formatButton(btn_RightBackward);
        btn_RightBackward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	JDialog angleDialog = new JDialog(_appFrame, "Angle input", true);
            	angleDialog.setSize(400, 100);
            	angleDialog.setLayout(new FlowLayout());

                final JTextField loadTF = new JTextField(15);
                JButton angleButton = new JButton("Next");
                angleButton.addMouseListener(new MouseAdapter() {
            		public void mousePressed(MouseEvent e1) {
            			angleDialog.setVisible(false);
            			int angle = Integer.parseInt(loadTF.getText());
            			map.moveRobotRightB(angle);
                        CardLayout cl = ((CardLayout) _mapCards.getLayout());
                        cl.show(_mapCards, "REAL_MAP");
                        map.repaint();
            		}
            	});
                angleDialog.add(new JLabel("Move right backward by __ degree"));
                angleDialog.add(loadTF);
                angleDialog.add(angleButton);
                angleDialog.setVisible(true);
                
            }
        });
        _buttons.add(btn_RightBackward);
    }
    
    
    public static void move(Node start, Node goal) {
    	System.out.println(String.format("Robot starts at (%.1f,%.1f,%d)", start.row+.5, start.col+.5, start.direction));
    	Node nn1 = storage.ASTAR(start, goal);
    	System.out.println(nn1.row + "," + nn1.col + "," + nn1.direction);
    	Stack<String> ss1 = storage.printPath(nn1);
    	String path1 = storage.shortenPath(ss1);
    	System.out.println(path1);
    	String[] steps = path1.split("/");
	
		// execute path
		for (String step: steps) {
			try {
				Thread.sleep(500);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (bot.getRealBot()) {
				/*
				if (step.equals("s050")) comm.sendMsg("s060", CommMgr.toSTM);
				else comm.sendMsg(step, CommMgr.toSTM);
				*/
				comm.sendMsg(step, CommMgr.toSTM);
				// successfully bit
				char b = '1';
				while ( b != 'A') {
					String rcvMsg = comm.recvMsg();
					b = rcvMsg.charAt(0);
				}
			}
			// get new position after step
			double[] new_pos = bot.posAfterMove(step);
			System.out.println(step + ": " + Arrays.toString(new_pos));
			bot.setRobotPos(new_pos[0], new_pos[1]);
			bot.setDirection((int) new_pos[2]);
			
			map.repaint();
			
			// send to Android
			if (bot.getRealBot()) {
				String msg = String.format("POS,%d,%d,%c",(int) new_pos[0], (int) new_pos[1], charDir((int)new_pos[2]));
				comm.sendMsg(msg, CommMgr.toAndroid);
				// successfully bit
				char bAnd = '1';
				while ( bAnd != '1') {
					String rcvMsg = comm.recvMsg();
					bAnd = rcvMsg.charAt(rcvMsg.length()-1);
				}
			}
		}
		System.out.println(String.format("Robot reaches (%.1f,%.1f,%d)", goal.row+.5, goal.col+.5, goal.direction));
		System.out.println("===========================");
    }
    
    public static int takePic() {
    	int a = 0;
    	String image_id = MapConstants.BULL_EYE;
    	if (bot.getRealBot()) {
			comm.sendMsg("TP", CommMgr.toRPI);
			String rcvMsg = comm.recvMsg();			// receive message should be TP+image_id
			image_id = rcvMsg.substring(2);
			// image_id = ".";
			if (!image_id.equals(MapConstants.BULL_EYE)) {
				a = 1;
			}
		}
    	return a;
    }
    
    public static void checkList() {
    	// on specific map: newmap1
    	// set bot position to (13.5,10.5,-90)
    	// loadMapFromDisk(map, "newmap1");
    	// comm.recvMsg();
    	bot.setRobotPos(13.5, 10.5);
    	bot.setDirection(-90);
    	map.repaint();
    	int a = 0;
    	
    	// 4 nodes
    	Node[] nodes = new Node[4];
    	nodes[0] = new Node(13,10,-90);
    	nodes[1] = new Node(10,13,180);
    	nodes[2] = new Node(7,10,90);
    	nodes[3] = new Node(10,7,0);
    	a = takePic();
    	int index = 0;
    	while (a != 1 && index < 3) {
    		move(nodes[index], nodes[index+1]);
    		a = takePic();
    		index ++;
    	}
    	System.out.println("Image found");
    }
    
    private static char charDir(int dir) {
		if (dir == 0) return 'E';
		else if (dir == 1 || dir == 90) return 'N';
		else if (dir == 2 || dir == 180) return 'W';
		else return 'S';
	} 
}