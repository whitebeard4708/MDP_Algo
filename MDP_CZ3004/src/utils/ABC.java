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
		String s = "W10/2.50/1.50/";
		String[] parts = s.split("/");
		System.out.println(parts.length);
		System.out.println(s.substring(1, s.length()-1));
		ar.add(4);
		
		int [] q = {1,2,3};
		int [] p = {4,5,6};
		String sss = "111";
		int[][] a = {{19}};
		System.out.println(Arrays.toString(a[0]));
		f1(a);
		System.out.println(Arrays.toString(a[0]));
		
	}
	
	private static void f1(int[][] s) {
		int[] ss = {321};
		s[0] = ss;
		
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
