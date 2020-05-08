 package edu.fiu.cs.seniorproject.utils;
 
 import android.location.Location;
 import edu.fiu.cs.seniorproject.data.Event;
 import edu.fiu.cs.seniorproject.data.Place;
 
 public class DataUtils {
 
 	static private int[][] matrix = null;
 	
 	public static int max( int a , int b) {
 		return a > b ? a : b;
 	}
 	
 	public static int min( int a, int b) {
 		return a < b ? a : b;
 	}
 	
 	static private int[][] getMatrix(int size) {
 		if ( !(matrix != null && matrix[0].length >= size) ) {
 			matrix = new int[2][size];
 		}
 		return matrix;
 	}
 	
 	public static int LongestCommonSubsecuence(String a, String b) {
 		int result = 0;
 		
 		if ( a != null && b != null ) {
 			a = a.toLowerCase();
			b = b.toLowerCase();
 			
 			int m[][] = getMatrix(b.length() + 1);
 			
 			for ( int i = 0; i <= a.length(); i ++ )
 				for ( int j = 0; j <= b.length(); j++ ) {
 					if ( i == 0 || j == 0) {
 						m[i % 2][j] = 0;
 					} else if ( b.charAt(j-1) == a.charAt(i-1) ) {
 						m[i % 2][j] = m[(i - 1) % 2][ j - 1] + 1;
 					} else {
 						m[i % 2][j] = max(m[i % 2][ j - 1], m[(i - 1) % 2][ j ] );
 					}
 				}
 			result = m[a.length() % 2][b.length()];
 		}
 		return result;
 	}
 	
 	public static boolean isSameEvent( Event a, Event b ) {
 		
 		float[] distance = new float[3];
 		Location.distanceBetween( Double.valueOf(a.getLocation().getLatitude()), Double.valueOf(a.getLocation().getLongitude()), Double.valueOf(b.getLocation().getLatitude()), Double.valueOf(b.getLocation().getLongitude()), distance);
 		
 		int lcs = LongestCommonSubsecuence(a.getName(), b.getName());
 		int minLength = min(a.getName().length(), b.getName().length());
 		
 		//Logger.Debug("compare events nameA=" + a.getName() + " nameB=" + b.getName() + " lcs=" + lcs + " distance=" + distance[0] );
 		return ( lcs / minLength >= 0.8 && distance[0] <= 50.0 && Math.abs( Long.valueOf( a.getTime() ) - Long.valueOf( b.getTime() ) ) <= 30 * 60 );
 	}
 	
 	public static boolean isSamePlace( Place a, Place b) {
 		boolean result = false;
 		float[] distance = new float[3];
 		Location.distanceBetween( Double.valueOf(a.getLocation().getLatitude()), Double.valueOf(a.getLocation().getLongitude()), Double.valueOf(b.getLocation().getLatitude()), Double.valueOf(b.getLocation().getLongitude()), distance);
 		
 		String aName = a.getName().toLowerCase();
 		String bName = b.getName().toLowerCase();
 		
 		if ( distance[0] < 2000 && aName.equals(bName)) {
 			// same name and closer than 2Km, same place
 			result = true;
 		} else {
 			int lcs = LongestCommonSubsecuence(aName, bName);
 			int minLength = min(aName.length(), bName.length());
 			
 //			if ( a.getName().equals("Delano Hotel") && b.getName().equals("Delano Hotel") ) {
 //				Logger.Debug("compare events nameA=" + a.toString() + " nameB=" + b.toString() + " lcs=" + lcs + " distance=" + distance[0] );
 //			}
 			result = ( lcs / minLength >= 0.8 && distance[0] <= 100.0);
			if ( result ) {
				Logger.Debug("compare events nameA=" + aName + " nameB=" + bName + " lcs=" + lcs + " distance=" + distance[0] );
			}
 		}
 		return result;
 	}
 }
