 package com.iprange;
 
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 
 public class IpRange {
 	private Logger log = Logger.getLogger(getClass());
 
 	public void proccessIpRange(String ip1, String ip2) {
		int[] firstIp = toArray(ip1);
		int[] secondIp = toArray(ip2);
 		validateIp(ip1);
 		validateIp(ip2);
 		if (isFirstIpLessThenSecond(firstIp, secondIp) && !Arrays.equals(firstIp, secondIp)) {
 			while (!Arrays.equals(firstIp, secondIp)) {
 				firstIp[firstIp.length - 1]++;
 				for (int i = firstIp.length - 1; i > 0; i--) {
 					if (firstIp[i] > 255) {
 						firstIp[i] = 0;
 						firstIp[i - 1]++;
 					}
 					else {
 						break;
 					}
 				}
 				if (!Arrays.equals(firstIp, secondIp)) {
 					printIp(firstIp);
 				}
 			}
 		}
 	}
 
 	private void validateIp(String ip) {
 		isIpValid(ip);
 		isNotNull(ip);
 	}
 
 	private boolean isFirstIpLessThenSecond(int[] firstIpArray, int[] secondIpArray) {
 		long firstIpSize = 0;
 		long secondIpSize = 0;
 		long multiplier = 1;
 		int range = 256;
 
 		for (int i = firstIpArray.length - 1; i >= 0; i--) {
 			firstIpSize += firstIpArray[i] * multiplier;
 			secondIpSize += secondIpArray[i] * multiplier;
 			multiplier *= range;
 		}
 		if (firstIpSize < secondIpSize) {
 			return true;
 		}
 		else {
 			throw new IllegalArgumentException("First ip adress must be lower than second");
 
 		}
 	}
 
 	private void isIpValid(String ip) {
 		String regex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
 		if (!ip.matches(regex)) {
 			throw new IllegalArgumentException("Bad ipv4 adress format it must be string like 255.255.255.255");
 		}
 	}
 
 	private void isNotNull(String ip1) {
 		if (ip1 == null) {
 			throw new NullPointerException("Ips should be not null");
 		}
 	}
 
 	private void printIp(int[] currentIp) {
 		StringBuilder sb = new StringBuilder();
 		String separator = "";
 		for (int i : currentIp) {
 			sb.append(separator);
 			separator = ".";
 			sb.append(i);
 		}
 		log.debug(sb.toString());
 	}
 
 	private int[] toArray(String ipString) {
 		String[] firstIpString = ipString.split("\\.");
 		int[] ip = new int[4];
 		for (int i = 0; i < firstIpString.length; i++) {
 			ip[i] = Integer.parseInt(firstIpString[i]);
 		}
 		return ip;
 
 	}
 }
