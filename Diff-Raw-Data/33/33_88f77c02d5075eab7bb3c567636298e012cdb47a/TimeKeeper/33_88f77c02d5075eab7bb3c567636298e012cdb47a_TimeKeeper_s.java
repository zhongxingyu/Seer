 package utilities;
 
 import packets.ccsds.CcsdsTlmPkt;
 import packets.parameters.DataType;
 
 public class TimeKeeper {
 	private static long startTime;
 
 	public static final void reset() {
 		startTime = System.currentTimeMillis();
 	}
 
 	public static final String getElapsedTime() {
 		long time = getLongTime();
 		int intTime = 0;
 		if (time < Integer.MAX_VALUE)
 			intTime = (int) time;
 
 		int sec = intTime / 1000;
 		return getTimeFormatted(sec);
 	}
 
 	public static final String getEventTime(byte[] data) throws Exception {
 		return getTimeFormatted(CcsdsTlmPkt.getTime(data));
 	}
 
	public static final String getTimeSince(long time) {
		return getTimeFormatted((int)((getLongTime() - time)/1000));
	}
	
 	public static final String getTimeFormatted(int sec) {
 		int min = sec / 60;
 		sec = sec % 60;
 		int hrs = min / 60;
 		min = min % 60;
 		int days = hrs / 24;
 		hrs = hrs % 24;
 
 		String output = days + "-" + hrs + ":" + min + ":" + sec;
 		return output;
 	}
 
 	public static final long getLongTime() {
 		return System.currentTimeMillis() - startTime;
 	}
 }
