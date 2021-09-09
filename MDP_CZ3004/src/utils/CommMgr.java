package utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommMgr {
	
		// Android --> PC : waypoint in the format of row +" "+ col  eg. "wp3,5"
	public static final String EX_START = "EX_START";       // Android --> PC : receive "EX_START" from Android to start exploration
	public static final String FP_START = "FP_START";       // Android --> PC : receive "FP_START" from Android to send fastest path
	
	public static final String MAP_STRINGS = "MAP";         // PC --> Android : send "md"+P1+P2+robot(row+column+direction) to Android 
						  //eg. ret: a list of 2 strings of hexidecimal numbers, mapStrings[0] = P1, mapStrings[1] = P2, "3,5,UP"
	public static final String BOT_POS = "BOT_POS";         // PC --> Android : send "E" to Android to signal exploration has finished & P1,P2 are sent over  
	    				  //send robotG row,column,direciton eg. "3,5,UP"
	public static final String BOT_START = "BOT_START";     // PC --> Arduino : (no use)send null (type BOT_START) to Arduino to start the bot
	public static final String INSTRUCTIONS = "INSTR";      // PC --> Arduino : during fastest path - send the whole fastest path movement (fpinstructions) to Arduino eg. "fpath"+"fbrlce"+"z" 
	public static final String SENSOR_DATA = "SDATA";       // Arduino --> PC : receive sensor data from Arduino eg. "pc:obs:5|0|2|1|2|1|msgcount0"

	
	private static CommMgr commMgr = null;
    private static Socket conn = null;

    private BufferedWriter writer;
    private BufferedReader reader;

    private CommMgr() {
    }

    public static CommMgr getCommMgr() {
        if (commMgr == null) {
            commMgr = new CommMgr();
        }
        return commMgr;
    }

    public void openConnection() {
        System.out.println("Opening connection...");

        try {
            String HOST = "192.168.3.3";//18.18
            int PORT = 4957;//5454
            conn = new Socket(HOST, PORT);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            System.out.println("openConnection() --> " + "Connection established successfully!");

            return;
        } catch (UnknownHostException e) {
            System.out.println("openConnection() --> UnknownHostException");
        } catch (IOException e) {
            System.out.println("openConnection() --> IOException");
        } catch (Exception e) {
            System.out.println("openConnection() --> Exception");
            System.out.println(e.toString());
        }

        System.out.println("Failed to establish connection!");
    }

    public void closeConnection() {
        System.out.println("Closing connection...");

        try {
            reader.close();

            if (conn != null) {
                conn.close();
                conn = null;
            }
            System.out.println("Connection closed!");
        } catch (IOException e) {
            System.out.println("closeConnection() --> IOException");
        } catch (NullPointerException e) {
            System.out.println("closeConnection() --> NullPointerException");
        } catch (Exception e) {
            System.out.println("closeConnection() --> Exception");
            System.out.println(e.toString());
        }
    }

    public void sendMsg(String msg, String msgType) {
        System.out.println("Sending a message...");

        try {
            String outputMsg;
            if (msg == null) {
                outputMsg = msgType + "\n";												 
            } 
            else {
                outputMsg = msg;													
            }

            System.out.println("Sending out message:\n" + outputMsg);
            writer.write(outputMsg);
            writer.flush();
        } catch (IOException e) {
            System.out.println("sendMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("sendMsg() --> Exception");
            System.out.println(e.toString());
        }
    }

    public String recvMsg() {
        System.out.println("Receiving a message...");

        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();

            if (input != null && input.length() > 0) {
                sb.append(input);
                System.out.println(sb.toString());
                return sb.toString();
            }
        } catch (IOException e) {
            System.out.println("recvMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("recvMsg() --> Exception");
            System.out.println(e.toString());
        }

        return null;
    }

    public boolean isConnected() {
        return conn.isConnected();
    }
}
