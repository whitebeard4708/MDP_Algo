package simulator;

import algo.FastestPath;
import algo.HamiltonPath;
import algo.Node;
import algo.Storage;
import maps.Map;
import maps.MapConstants;
import maps.Visited;
import robot.Robot;
import robot.RobotConstants;


import utils.CommMgr;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import static utils.MapDescriptor.loadMapFromDisk;
import static utils.MapDescriptor.convertCharToIntDirection;

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
    	bot = new Robot(1.5, 1.5, false);
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
        
        // Hamiltonian Path
        JButton btn_HamiltonianPath = new JButton("Hamiltonian path");
        formatButton(btn_HamiltonianPath);
        btn_HamiltonianPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	
            	// map.repaint();
                System.out.println("Hamiltonian path started");
                HamiltonPath hp = new HamiltonPath(map, bot);
                hp = new HamiltonPath(map, bot);
                hp.runHamiltonPath();
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
            	
            	new FastestPathSim().execute();
            	
            	/*
            	FastestPath fp = new FastestPath(map, bot);
            	fp.runFastestPath();
            	 */
            	
            	/*
            	Node start = new Node(5,7,0);
            	Node end = new Node(7,17,-90);
            	Node n1 = fp.ASTAR(start, end);
            	Stack<String> ss1 = fp.printPath(n1);
            	String s1 = fp.shortenPath(ss1);
            	System.out.println(s1);
            	System.out.println(fp.reversePath(s1));
            	
            	System.out.println("From end to start");
            	
            	Node n2 = fp.ASTAR(end, start);
            	Stack<String> ss2 = fp.printPath(n2);
            	String s2 = fp.shortenPath(ss2);
            	System.out.println(s2);
            	System.out.println(fp.reversePath(s2));
            	*/
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
            	if (a==1) {	// can connect
            		bot.setRealBot(true);
            		// receive obstacles from Android
            		for (int i=0; i<MapConstants.NUM_OBSTACLE; i++) {
            			String coor = comm.recvMsg().split("|")[2];	// row,col
            			char side = comm.recvMsg().split("|")[2].charAt(0);	// image_side
            			String[] parts = coor.split(",");
            			int r = Integer.parseInt(parts[0]);
            			int c = Integer.parseInt(parts[1]);
            			int image_side = convertCharToIntDirection(side);
            			map.addNewImage(r, c, image_side);
            		}
            		
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
}