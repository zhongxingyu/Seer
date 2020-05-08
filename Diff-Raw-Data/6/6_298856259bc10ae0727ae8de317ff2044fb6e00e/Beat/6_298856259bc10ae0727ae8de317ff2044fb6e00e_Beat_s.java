 package net.bluecow.spectro.detection;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 import net.bluecow.spectro.SpectroEditSession;
 
 public class Beat
 {
 	public Beat(long highestIndex, float highestPoint,int indexInList)
 	{
 		soundIntensity = highestPoint;
 		sampleLocation = highestIndex;
 		this.indexInList = indexInList;
 	}
 	public static final int FRAME_SIZE = 1320;
 	double soundIntensity;
 	public long sampleLocation;
 	public boolean predictedBeat;
 	Color col = Color.green;
 	public int indexInList;
 	public String toString()
 	{
 		return ""+sampleLocation;//"b "+(double)(sampleLocation*1320.0/44100.0);
 	}
 
 
 	public static void writeBeatsToFile(ArrayList<Beat> beats)
 	{
 		System.out.println("Writing beats to file");
 		try {
 			String fileName = SpectroEditSession.fileName;
			fileName.substring(0,fileName.indexOf("."));
			File f = new File(SpectroEditSession.fileName);
 			/*
 			int counter = 0;
 			while(f.exists())
 			{
 				f = new File("test"+counter+".txt");
 				counter++;
 			}
 			*/
 			f.createNewFile();
 
 			FileOutputStream output = new FileOutputStream(f);
 			PrintStream print = new PrintStream(output);
 			for(Beat b:beats)
 			{
 				print.println(b.toString());
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
