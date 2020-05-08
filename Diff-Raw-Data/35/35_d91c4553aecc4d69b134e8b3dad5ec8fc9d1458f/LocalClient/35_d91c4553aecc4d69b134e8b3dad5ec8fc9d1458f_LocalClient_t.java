 package cz.muni.fi.xtovarn.heimdall.client;
 
 import com.fasterxml.jackson.databind.util.ISO8601Utils;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.Date;
 
 public class LocalClient {
 
 	public static void main(String[] args) throws IOException, InterruptedException {
 
 		Socket socket = new Socket("localhost", 5000);
 
 		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 
		int i = 10000;
 		while (i > 0) {
			Thread.sleep(1);
 			i--;
 			String json = "{\"Event\":{\"occurrenceTime\":\"" + ISO8601Utils.format(new Date(System.currentTimeMillis()), true) + "\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"cron\",\"processId\":\"4219\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":\"http://www.linux.org/schema/monitoring/cron/3.1/events.xsd\",\"schemaVersion\":\"3.1\",\"value\":4648,\"value2\":\"Fax4x46aeEF%aax4x%46aeEF\"}}}";
 			out.write(json);
 			out.newLine();
 			out.flush();
 		}
 
 		out.close();
 		socket.close();
 	}
 
 }
