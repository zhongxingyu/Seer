 package syndeticlogic.tiro.monitor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Random;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import syndeticlogic.tiro.stat.CpuStats;
 import syndeticlogic.tiro.stat.IOStats;
 import syndeticlogic.tiro.stat.LinuxCpuStats;
 import syndeticlogic.tiro.stat.LinuxIOStats;
 
 public class LinuxIOMonitor extends AbstractMonitor implements IOMonitor {
    private static final Log log = LogFactory.getLog(OsxIOMonitor.class);
     private String[] devices;
     private LinuxIOStats[] iostats;
     private LinuxCpuStats cpustats;
 
 	public LinuxIOMonitor(String... devices) {
 		super();
 		this.devices = devices;
 		String[] command = new String[devices.length+3];
 		command[0] = "iostat";
 		command[1] = "-dc";
 		command[devices.length+2] = "5";
 		System.arraycopy(devices, 0, command, 2, devices.length);
 		setCommandAndArgs(command);
 		iostats = new LinuxIOStats[devices.length];
 		for(int i = 0; i < devices.length; i++) {
 		    iostats[i] = new LinuxIOStats(devices[i]);
 		}
 		cpustats = new LinuxCpuStats();
 	}
 	
     @Override
 	protected void processMonitorOutput(BufferedReader reader) throws IOException {
 		reader.readLine();
 		reader.readLine();
 		reader.readLine();
 		while(true) {
 			String line = reader.readLine();
			if(line == null) {
 				break;
 			}
 			reader.readLine();
 			log.info(line);
 			line = line.trim();
 			String[] values = line.split("\\s+");
 			System.out.println("CPU Values = ");
 			for(String value : values) {
 				System.out.println("cpu values = "+value);
 			}
 			int i = 0;
 			Double user = Double.parseDouble(values[i++]);
 			i++; // skip %nice
 			Double system = Double.parseDouble(values[i++]);
 			Double iowait = Double.parseDouble(values[i++]);
 			i++; // skip %steal
 			Double idle = Double.parseDouble(values[i++]);
 			log.warn("droping the iowait value on the floor");
 			cpustats.addRawRecord(user, system, iowait, idle);			
 			line = reader.readLine();
 			if(line == null) {
 				break;
 			}
 			reader.readLine();
 			log.info(line);
 			line = line.trim();
 			values = line.split("\\s+");
 			System.out.println("IO devices Values = ");
 			for(String value : values) {
 				System.out.println("device values = "+value);
 			}
 			assert values.length == 3*devices.length+6;
 			i = 1; // skip the first value...
 			for(LinuxIOStats iostat : iostats) {
 			    Double tps = Double.parseDouble(values[i++]);
 			    Double kbsRead = Double.parseDouble(values[i++]);
 			    Double kbsWritten = Double.parseDouble(values[i++]);
 			    iostat.addRawRecord(tps, kbsRead, kbsWritten);
 			}
 		}
 	}
     
     @Override
     public void dumpData() {
         for(LinuxIOStats iostat : iostats) {
             iostat.dumpData();
         } 
         cpustats.dumpData();
     }
     
     @Override
     public IOStats[] getIOStats() {
         return iostats;
     }
     
     @Override
     public String[] getDevices() {
         return devices;
     }
     
     @Override
     public CpuStats getCpuStats() {
         return cpustats;
     }
     
 	public static void useDisk() throws IOException {
 		File file = new File("iomonitor.perf");
 		FileOutputStream out = new FileOutputStream(file);
 		byte[] bytes = new byte[1024*1024*10];
 		for(int j = 0; j < 1024*1024*10; j++) {
 			bytes[j] = (byte)(23 * j);
 		}
 		out.write(bytes);
 		out.close();
 		assert file.delete();
 	}
 	
 	public static void useCpu() {
 		Random r = new Random();
 		long first = r.nextLong();
 		int secondBound = (int)first;
 		if(secondBound < 0) {
 			secondBound = -secondBound;
 		}
 		long second = r.nextInt(secondBound);
 		long rem = -1;
 		while (rem != 0) {
 			rem = first % second;
 			first = second;
 			second = rem;
 		}
 	}
 	
 	public static void main(String[] args) throws Throwable {
 		try {
 			long starttime = System.currentTimeMillis();
 			LinuxIOMonitor iom = new LinuxIOMonitor("/dev/sda");//, "disk1");
 			System.out.println("Starting..");
 			iom.start();
 			Thread.sleep(1000);
 			if (args.length == 0) {
 				while (starttime + 25522 > System.currentTimeMillis()) {
 					useDisk();
 				}
 			} else {
 				while (starttime + 25522 > System.currentTimeMillis()) {
 					useCpu();
 				}
 			}
 			iom.finish();
 			iom.dumpData();
 			long duration = iom.getDurationMillis();
 			System.out.println("Duration = " + duration);
 		} catch (Throwable t) {
 			log.error("exception: ", t);
 			throw t;
 		}
 	}
 }
