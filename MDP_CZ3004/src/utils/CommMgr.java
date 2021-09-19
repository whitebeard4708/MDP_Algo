package utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommMgr {
	
		// Android --> PC : waypoint in the format of row +" "+ col  eg. "wp3,5"
	public static final String toSTM = "ALG|STM|";
	public static final String toAndroid = "ALG|AND|";
	public static final String toRPI = "ALG|RPI|TP";
	
	
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

    public int openConnection() {
        System.out.println("Opening connection...");

        try {
            String HOST = "192.168.3.3";//18.18
            int PORT = 4957;//5454
            conn = new Socket(HOST, PORT);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            System.out.println("openConnection() --> " + "Connection established successfully!");

            return 1;
        } catch (UnknownHostException e) {
            System.out.println("openConnection() --> UnknownHostException");
        } catch (IOException e) {
            System.out.println("openConnection() --> IOException");
        } catch (Exception e) {
            System.out.println("openConnection() --> Exception");
            System.out.println(e.toString());
        }

        System.out.println("Failed to establish connection!");
        return 0;
    }

    public int closeConnection() {
        System.out.println("Closing connection...");

        try {
            reader.close();

            if (conn != null) {
                conn.close();
                conn = null;
                return 1;
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
        return 0;
    }

    public int sendMsg(String msg, String destination) {
        System.out.println("Sending a message...");

        try {
            String outputMsg = "";
            if (msg == null) {
                outputMsg = "ALG|" + destination + "|\n";												 
            } 
            else {
                outputMsg = "ALG|" + destination + "|" + msg + "\n";													
            }

            System.out.println("Sending out message:\n" + outputMsg);
            writer.write(outputMsg);
            writer.flush(); // send message
            return 1;
        } catch (IOException e) {
            System.out.println("sendMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("sendMsg() --> Exception");
            System.out.println(e.toString());
        }
        return 0;
    }

    public String recvMsg() {
        System.out.println("Receiving a message...");

        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();

            if (input != null && input.length() > 0) {
                sb.append(input);
                String s= sb.toString();
                // parts: SOURCE | DESTINATION | MESSAGE
                String[] parts = s.split("|");
                System.out.println(String.format("Message from %s %s ", parts[0], parts[2]));
                return parts[2];
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
