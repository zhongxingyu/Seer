 package net.bluecow.spectro.detection;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import ddf.minim.effects.IIRFilter;
 
 
 public class BeatDetector
 {
 	//this is the instantaneous VEdata
 	public ArrayList<float[]> VEdata = new ArrayList<float[]>();
 	//this is the average VEdata (over 43 Energy histories
 	public ArrayList<Double> AveragedEnergydata = new ArrayList<Double>();
 
 	int historyLength = 20;//43;
 
 	private double[] EnergyHistory = new double[historyLength];
 	int currentHistoryIndex = 0;
 //	private long sampleIndex;
 	ArrayList<Beat> detectedBeats = new ArrayList<Beat>();
 
 	double maxEnergy = 0;
 
 	private IIRFilter filter;
 
 
 	int shiftAvg = 10;//this is used so that some of the future values are computed in the average of the current value
 
 	//values used for the actual beat detection
 	boolean aboveAverage = false;
 	long currentIndex = 0;
 	long highestIndex = 0;//the index of the highest point when it is above the beat
 	float highestPoint;
 	//the senstitivity of the beat detector:  smaller numbers remove more beats and is more strict
 	double senstitivity = 0.8;
 
 	boolean doOnce = true;
 
 
 	int type = 0;
 	private TempoDetector tempoDetection;
 	static int counter = 0;
 
 
 
 	public BeatDetector(IIRFilter bandPass)
 	{
 		filter = bandPass;
 		type = counter;
 		counter++;
 	}
 
 	public void calculateVE(float[] timeData)
    	{
 		if(filter!=null)
 		{
 			preFilter(timeData);
 		}
    		//the size of bits that the array is taken over
    		int averageSize = Beat.FRAME_SIZE;
    		//number of values
    		int length = timeData.length/averageSize;
    		int index = 0;
    		for(int k = 0;k<length;k++)
    		{
    			float[] result = new float[2];
    			float volume = 0;
    			float energy = 0;
    			for(int q = 0; q<averageSize;q++)
    			{
    				float data = timeData[index];
    				volume+=data;
    				energy+=data*data;
 
    				index++;
    			}
    			maxEnergy = Math.max(maxEnergy, energy);
    			result[0] = volume;
    			result[1] = energy;
    			VEdata.add(result);
 
    			EnergyHistory[currentHistoryIndex] = energy;
 
    			double value = 0;
    			for(int q=0;q<historyLength;q++)
    			{
    				value+=EnergyHistory[q];
    			}
    			value/=historyLength;
    			AveragedEnergydata.add(value);
 
    			currentHistoryIndex++;
    			currentHistoryIndex%=historyLength;
 
    			beatDetectionAlgorithm();
    		}
 
    	}
 
 	public void draw(Graphics2D g2,int startY,int scale)
 	{
 		this.tempoDetection.setSignificanceBeats();
 		double ratio = scale/maxEnergy;
 		g2.setColor(Color.black);
 		int length = VEdata.size();
 
 		//VE
 		float[] old = VEdata.get(0);
 		float[] current = VEdata.get(0);
 		//average
 		double oldAvg = AveragedEnergydata.get(0);
 		double currentAvg = AveragedEnergydata.get(0);
 
 		g2.setColor(Color.black);
 		for(int k = 0; k<length;k++)
 		{
 			old = current;
 			current = VEdata.get(k);
 
 			oldAvg = currentAvg;
 			if(k>10)
 				currentAvg = AveragedEnergydata.get(k-shiftAvg);
 
 	//		System.out.println(old[0]+"\n"+old[1]);
 	//		g2.setColor(Color.blue);
 	//		g2.drawLine((k-1)*4, (int)(startY - old[0]*ratio), k*4, (int)(startY - current[0]*ratio));
 			g2.setColor(Color.red);
			g2.drawLine((k-1)*2, (int)(startY - old[1]*ratio), k*2, (int)(startY- current[1]*ratio));
 	//		g2.setColor(Color.green);
 	//		g2.drawLine((k-1)*4, (int)(startY - oldAvg*ratio), k*4, (int)(startY- currentAvg*ratio));
 		}
 		int beatLength = detectedBeats.size();
 		for(int k=0; k<beatLength;k++)
 		{
 			Beat b = detectedBeats.get(k);
 			if(!b.predictedBeat)
 			g2.setColor(Color.GRAY);
 			else
 				g2.setColor(b.col);
			g2.drawLine((int)b.sampleLocation*2, startY, (int) b.sampleLocation*2,(int)( startY-75));
 		}
 		g2.setColor(Color.black);
 		g2.drawLine(0, startY, length*4, startY);
 
 		if(doOnce)
 		{
 			tempoDetection.printDistanceSets();
 			doOnce = false;
 			Beat.writeBeatsToFile(detectedBeats);
 		}
 
 	}
 
 	public void preFilter(float[] timeData)
 	{
 		filter.process(timeData);
 	}
 
 	/**
 	 * Goes through each point once and sees if it is large enough away from the average to be considered a beat
 	 * Need to make this static and go through all beats to detirmine Major beats
 	 */
 	public void beatDetectionAlgorithm()
 	{
 		if(currentIndex<shiftAvg)
 		{
 			currentIndex++;
 			return;
 		}
 
 		float instantEnergy = VEdata.get((int) (currentIndex))[1];
 		double averageEnergy = AveragedEnergydata.get((int) (currentIndex-shiftAvg));
 		if(instantEnergy>=averageEnergy)
 		{
 			if(instantEnergy>highestPoint)
 			{
 				highestPoint = instantEnergy;
 				highestIndex = currentIndex;
 			}
 			aboveAverage = true;
 		}else if(aboveAverage)
 		{
 			double avgEnergy = AveragedEnergydata.get((int) (highestIndex-shiftAvg));
 			double division = avgEnergy/highestPoint;
 		//	System.out.println(division);
 			aboveAverage = false;
 			if(division<senstitivity)
 			{
 				if(detectedBeats.size() == 0)
 				{
 					tempoDetection = new TempoDetector(detectedBeats);
 				}
 				detectedBeats.add(new Beat(highestIndex,highestPoint,detectedBeats.size()));
 		//		detectTempo2();
 		//		detectTempo();
 				tempoDetection.detectTempo3();
 			}
 			highestPoint = 0;
 			highestIndex = -1;
 		}
 		/**
 		 * Will look for spikes that are above the average...
 		 * Every spike above the average is a minor beat
 		 * one the energy level crosses the average energy level we only take one spike until it falls back below
 		 * This one spike is the maximum spike
 		 *
 		 * (maybe take two averages?)
 		 */
 
 		currentIndex++;
 	}
 }
