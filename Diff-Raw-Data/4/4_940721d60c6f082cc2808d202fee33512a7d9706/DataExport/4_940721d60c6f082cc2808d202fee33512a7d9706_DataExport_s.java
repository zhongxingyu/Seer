 package org.concord.CCProbe;
 
 import org.concord.waba.graph.*;
 import waba.util.*;
 import waba.io.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.io.*;
 import org.concord.waba.extra.util.*;
 
 public class DataExport 
 {
 	File file;
 	DataStream ds;
 
 	public DataExport(Bin b)
 	{
 		String name = createNameFile(b);
 		FileDialog fd = FileDialog.getFileDialog(FileDialog.FILE_SAVE, null);
 		if(fd != null){
 			fd.setFile(name);
 			fd.show();
 			name = fd.getFilePath();
 		}
 
 		if(name == null) return;
 
 		file = new File(name, File.CREATE);
 		file.close();
 		file = new File(name, File.READ_WRITE);
 
 		ds = new DataStream(file);
 	}
 
 	static public void showSerialDialog()
 	{
 		waba.io.impl.SerialManager.checkAvailableSerialPorts();
 		waba.io.impl.SerialManager.showSetupDialog();
 	}
 
     static public void writeString(DataStream ds, String s)
     {
 		ds.writeFixedString(s, s.length());
     }
 
 	static String create2DigitString(int n){
 		String retValue = "";
 		if(n < 10) retValue += "0";
 		retValue += n;
 		return retValue;
 	}
 	static  String createNameFile(Bin b){
 		if(b == null ||  b.time == null) return null;
 		String retValue = "Data-" + b.time.year;
 		retValue += "_";
 		retValue += create2DigitString(b.time.month);
 		retValue += "_";
 		retValue += create2DigitString(b.time.day);
 		retValue += "-";
 		retValue += create2DigitString(b.time.hour);
 		retValue += "_";
 		retValue += create2DigitString(b.time.minute);
 		retValue += ".txt";
 		return retValue;
 	} 
 
     public void export(Bin b)
     {
 		int i;
 		if(b == null ||
 		   b.time == null) return;
 
 		Vector points = b.annots;
 
 
 		writeString(ds, b.time.month + "/" + b.time.day + "/" + b.time.year + " " +
 					b.time.hour + ":" + create2DigitString(b.time.minute) + "\r\n");
 		writeString(ds, b.description + "\r\n");
 		if(points != null){
 			writeString(ds, "Marks:\r\n");
 			writeString(ds, "label\ttime\tvalue\r\n");
 			for(i=0; i < points.getCount(); i++){
 				DecoratedValue pt = (DecoratedValue)points.get(i);
 				writeString(ds, pt.getLabel() + "\t" + 
 							pt.getTime() + "\t" + 
 							pt.getValue() + "\r\n");
 			}
 			writeString(ds, "\r\n");
 
 		}
 
 		writeString(ds, b.label + " Data:\r\n");
 		writeString(ds, "time\tvalue\r\n");
 		float curTime = 0f;
 		for(i=0; i < b.lfArray.getCount(); i++){
 			writeString(ds, curTime + "\t" + (b.lfArray.getFloat(i)+b.lfArray.ref) + "\r\n");
 			curTime += b.dT;
 		}
 		writeString(ds, "\r\n\r\n");
     }
 
 	public void close()
 	{
 		file.close();
 	}
 
 }
