package algorithms;

import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

public class fastest{
    public static void main(String[] args) {
            int[][] list;
            list = new int[][] {
                     {0,0,'N'},
                     {50, 30, 'N'}, 
                     {50, 100, 'S'}, 
                     {90, 170, 'W'}, 
                     {140, 80, 'E'}, 
                     {180, 30, 'N'}
             		};
             
    		int [][] duplist = list;
    		int [][] shortestPath = getshortestpath(duplist);
//    		for(int i=0;i<shortestPath.length;i++) {
//    			for(int j=0;j<shortestPath[0].length;j++) {
//   				if(j==2) {
//  					System.out.println((char)shortestPath[i][j]);
//    				} else 
//   					System.out.print(shortestPath[i][j]);
//    			}
//   			System.out.println();
//    		}
    		
    }
            
	public static int[][] getshortestpath (int[][]list){
			int[][] duplist = list;
			 int[][] test = new int [6][6];
			 test[0] = duplist[0];
			 for (int i= 1; i < duplist.length; i++) {
				 if (duplist[i][2] == 'N')
				 {
					test[i][0] = duplist[i][0] + 5;
					test[i][1] = duplist[i][1] + 30;
					test[i][2] = 'N';
				}
		                
				else if (duplist[i][2] == 'S')
		         {
		        	 test[i][0] = duplist[i][0] + 5;
		             test[i][1] = duplist[i][1] - 20;
		             test[i][2] = 'S';  
		         }
				else if (duplist[i][2] == 'E'){
					 test[i][0] = duplist[i][0] + 30;		
					 test[i][1] = duplist[i][1] + 5;
					 test[i][2] = 'E';
		         }                		               
				else if (duplist[i][2] == 'W'){
					 test[i][0] = duplist[i][0] - 30;
		             test[i][1] = duplist[i][1] + 5;
		             test[i][2] = 'W';
		            }
		        else {
		           break;
		        }
				// System.out.print(test[i][0]);
				 //System.out.print(test[i][1]);
				 //System.out.println((char)test[i][2]);
			 }
        	int [][] duplist2 = test;
        	int [][] shortestpath = new int [6][6];
       	//System.out.println(duplist2.length);
        	int index = 0;
        	for (int i=0; i < duplist2.length; i++) { 	
	            if(i == duplist2.length-1) { 
	            	//17085E,60175W,5580S,5560N,00N
	            	shortestpath[shortestpath.length-1] = test[i];
	            	// System.out.print("hello");
	           
	            
	            	System.out.print(shortestpath[shortestpath.length-1][0]);
	            	System.out.print(shortestpath[shortestpath.length-1][1]);
	            	System.out.println((char)shortestpath[shortestpath.length-1][2]);
        	}	 
            	int[][]result = find_closest_brute_force(test);
	            shortestpath[index]=result[0];
	            index++;
	            // create new array to exclude result[0]
	            int [][] test2 = new int [6][6];
	            int test2Index = 0;
	            for(int j=0;j<test.length;j++) {
	            	if(test[j]!=result[0]) {
	            		test2[test2Index] = test[j];
	            	}
	            }    
            }
	        return shortestpath;
    	 }
                			 
                		                    		 
        public static int[][] find_closest_brute_force(int[][]array) {
        	int [][] result = new int [6][6];
        	result[0] = array[0];
        	result[1] = array[1]; 
        	int distance = (int) Math.round(Math.sqrt(Math.pow((array[0][0]-array[1][0]),2) + Math.pow((array[0][1]-array[1][1]),2)));
            for (int i= 0; i < array.length-1; i++) {
            	for (int j= i+1; j < array.length; j++) {
            		int distance2 = (int) Math.round(Math.sqrt(Math.pow((array[i][0]-array[j][0]),2) + Math.pow((array[i][1]-array[j][1]),2)));
            		if (distance2 < distance) {
            			result[0] = array[i];
                        result[1] = array[j];
                        distance = distance2;
            		}
            	}
            	return result;
            }
            return result;
        }
    }
        