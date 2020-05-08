 package com.bananity.util;
 
 
 import java.util.Comparator;
 
 
 public class StorageItemComparator implements Comparator<String>
 {
 	private final String token;
 
 	public StorageItemComparator (String token) {
 		this.token = token;
 	}
 
 	public int compare(String sr1, String sr2) {
 		
 		// En primer lugar comparamos SOLO la logitud
 		int result = (sr1.length() > sr2.length())?1:((sr1.length() == sr2.length())?0:-1);
 
 		if (result == 0) {
			// Si tienen la misma longitud, comparamos el número de apariciones del token
			int ctoSr1 = countTokenOccurrences(sr1);
			int ctoSr2 = countTokenOccurrences(sr2);

			result = (ctoSr1 > ctoSr2)?1:((ctoSr1 == ctoSr2)?0:-1);

			if (result == 0) {
				// Si no queda otra: orden lexicográfico
				result = sr1.compareTo(sr2);
			}
 		}
 
		//return result;
		return sr1.compareTo(sr2);
 	}
 
 	private int countTokenOccurrences (String str) {
 		int count = 0;
 		int pos = 0;
 
 		while ( (pos = str.lastIndexOf(token, pos)) != -1 ) {
 			++pos;
 			++count;
 		}
 
 		return count;
 	}
 }
