 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
import java.sql.ResultSet;
 
 import com.csvreader.CsvReader;
 
 public class Data {
 	
 	public static void main(String[] args){
 		update();
 		try{
 			DataSet ds = new DataSet();
 			String sMakeInsert = "INSERT INTO data VALUES(";
 			String time = "";
			try{
				ResultSet rs = ds.executeQry("SELECT * FROM data");
				while(rs.next()){
					time=rs.getString("time");
				}
				rs.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
 			CsvReader reader = new CsvReader("wxobservations.csv");
 			reader.readHeaders();
			boolean flag = false;
			int count = 0;
			long startTime = 0;
 			while(reader.readRecord()){
 				String cur = reader.get("Time (UTC)");
 				if(flag){
 					System.out.print(".");
 					sMakeInsert += "\"" + cur + "\",";
 					for(int i = 1; i < 7 ; i++){
 						sMakeInsert += reader.get(i);
 						sMakeInsert += (i==6?"":",");
 					}
 					sMakeInsert+=")";
 					ds.executeStmt(sMakeInsert);
 					sMakeInsert = "INSERT INTO data VALUES(";
					count++;
 				}
				if(time.equals(cur)){
 					flag=true;
 					System.out.println("Adding entries");
					startTime = System.nanoTime();
 				}
 			}
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			System.out.println("Write speed is " + count / (duration/1000000000.0) + " entries per second");
 			ds.closeConnection();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public static void update(){
 		try{
 			System.out.println("Fetching");
 			URL website = new URL("http://weather.gladstonefamily.net/cgi-bin/wxobservations.pl?site=AS221");
 		    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
 		    FileOutputStream fos = new FileOutputStream("wxobservations.csv");
 			long startTime = System.nanoTime();
 		    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 		    long endTime = System.nanoTime();
 		    fos.close();
 		    System.out.println("Finished fetching");
 		    long duration = endTime - startTime;
 		    File f = new File("wxobservations.csv");
 		    double speed=(f.length()/1024.0)/(duration/1000000000);
 		    System.out.println("Speed is "+speed+"kb/s");
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 }
