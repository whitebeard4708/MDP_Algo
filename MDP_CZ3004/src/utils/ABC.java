package utils;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import maps.Cell;
import maps.Map;
import maps.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import utils.MapDescriptor;

public class ABC {
	private static int count;
	private static ArrayList<Integer> ar = new ArrayList<Integer>();
	
	public static void main (String[] args) {
		
		System.out.println("==================");
		CommMgr comm = CommMgr.getCommMgr();
		comm.openConnection();
		// comm.sendMsg("Test_test ", CommMgr.toAndroid);
		String msg1 = comm.recvMsg();
		comm.sendMsg("Receive " + msg1, CommMgr.toRPI);
		comm.closeConnection();
		System.out.println("==================");
		
		System.out.println(String.format("%04d ---", 9));
		System.out.println(Integer.parseInt("00900"));
	}
	
	public static void heaps(int[] a, int size) {
		System.out.println(Arrays.toString(a) + "----------" + a[0]);
        if(size == 1) {
            // (got a new permutation)
        	if (ar.contains(a[0])) return;
            System.out.println(Arrays.toString(a));
            count++;
            return;
        }
        for(int i = 0;i < size ;i++) {
            heaps(a, size-1);
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

}
