package simulator;

import algo.ExploreMap;
import algo.FastestPath;
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

import static utils.MapDescriptor.loadMapFromDisk;

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
    private static int timeLimit = 4000;            // time limit

    private static final CommMgr comm = CommMgr.getCommMgr();
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
                bot.setDirection(1);
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
            }
        });
        _buttons.add(btn_HamiltonianPath);
        
        // Fastest Path
        JButton btn_FastestPath = new JButton("Fastest path");
        formatButton(btn_FastestPath);
        btn_FastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	map.repaint();
                System.out.println("Fastest path started");
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
            			String coor = comm.recvMsg();	// row,col
            			String side = comm.recvMsg();	// image_side
            			String[] parts = coor.split(",");
            			int r = Integer.parseInt(parts[0]);
            			int c = Integer.parseInt(parts[1]);
            			int image_side = Integer.parseInt(side);
            			map.addNewImage(r, c, image_side, MapConstants.UNIDENTIFED_CHAR);
            		}
            		
            	}
            }
        });
        _buttons.add(btn_ConnectRPI);
        
        // Move Forward Button
        JButton btn_Forward = new JButton("Forward");
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
        JButton btn_Backward = new JButton("Backward");
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
        JButton btn_LeftForward = new JButton("Left F");
        formatButton(btn_LeftForward);
        btn_LeftForward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                map.moveRobotLeftF();
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "REAL_MAP");
                map.repaint();
            }
        });
        _buttons.add(btn_LeftForward);
        
        
        // Move Right Forward Button
        JButton btn_RightForward = new JButton("Right F");
        formatButton(btn_RightForward);
        btn_RightForward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                map.moveRobotRightF();
                CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "REAL_MAP");
                map.repaint();
                //System.out.println("Map printed!");
            }
        });
        _buttons.add(btn_RightForward);
        
        
        // Left Backward
        JButton btn_LeftBackward = new JButton("Left B");
        formatButton(btn_LeftBackward);
        btn_LeftBackward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	map.moveRobotLeftB();
            	CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "REAL_MAP");
            	map.repaint();
                //System.out.println("LeftBackward");
                
            }
        });
        _buttons.add(btn_LeftBackward);
        
        // Right Backward
        JButton btn_RightBackward = new JButton("Right B");
        formatButton(btn_RightBackward);
        btn_RightBackward.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	map.moveRobotRightB();
            	CardLayout cl = ((CardLayout) _mapCards.getLayout());
                cl.show(_mapCards, "REAL_MAP");
            	map.repaint();
                //System.out.println("RightBackward");
                
            }
        });
        _buttons.add(btn_RightBackward);
    }
}