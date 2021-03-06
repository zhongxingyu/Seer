 /*
 	This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 	N.B.  the above text was copied from http://www.gnu.org/licenses/gpl.html
 	unmodified. I have not attached a copy of the GNU license to the source...
 
     Copyright (C) 2011 Timo Rantalainen, tjrantal@gmail.com
 */
 
 /*There might be 1 - 3 mice and 4 channels per mouse*/
 package Analyze;
 import ReadWDQ.*;
 import ui.*;
 import Filter.*;	/*Butterworh filtering*/
 import fft.*;		/*Fast Fourier Transform*/
 import java.util.Vector;
 import java.lang.Math;
 import java.io.*;
 public class Analyze{
 	public double[] previousDataPoints;
 	public long position;
 	public int animalsInFile;
 	public int channelsPerAnimal;
 	double voltsToKilos = 0.1;
 	public Analyze(ReadWDQ dataIn,Indeksi2011 mainProgram){
 		String[] calibrations = mainProgram.calibrations.get(mainProgram.calibrationFileNo);
 		String saveName = mainProgram.savePath+"/";
 		position = 0;
 		if (dataIn.channelNo%4 == 0){channelsPerAnimal = 4;}
 		if (dataIn.channelNo%5 == 0){channelsPerAnimal = 5;}
 		animalsInFile =dataIn.channelNo/channelsPerAnimal;
 		
 		
 		System.out.println("Elaimia "+ dataIn.channelNo/channelsPerAnimal);
 		//dataIn.dataAmount = 2000*2*dataIn.channelNo; //Debugging...
 		for (int i = 0; i<animalsInFile; ++i){ /*Loop for going through all of the data...*/
 			mainProgram.status.setText(new String("Started analyzing "+(i+1)+" out of "+animalsInFile));
 			Vector<double[]> grfData = new Vector<double[]>();	//Use this to store the data for this animal. Needs to be cleared for the next...
 			for (int j = 0; j<4;++j){	/*Get filtered data for the particular animal*/
 				grfData.add(scaleFilterData(dataIn,i,j,animalsInFile,channelsPerAnimal,Double.valueOf(mainProgram.lowPass.getText()),mainProgram.subtract,mainProgram.writeFFT));
 			}
 			mainProgram.status.setText(new String("Scaled &  filtered "+(i+1)+" out of "+animalsInFile));
 			/*Do the actual analysis...*/
 			calculateIndex(grfData,Double.valueOf(calibrations[2+i]),1.0/dataIn.samplingInterval,saveName,dataIn.fileName,i,dataIn.measurementInit,dataIn.measurementStop,mainProgram.calibration,mainProgram.writeCoordinates,mainProgram.writeFFT);
 		}
 	}
 	
 	void calculateIndex(Vector<double[]> grfData,double mass,double samplingRate,String saveName,String fileName,int animalNo,String start, String stop,double[] calibration,boolean writeCoordinates, boolean writeFFT){
 		int linenum = 0;
 		int datapisteita = 0;
 		double aks=0;
 		double yy=0;
 		double aksOld=0;
 		double yyOld=0;
 		double acc=0;
 		double accOld=0;
 		double sum=0;
 		double[] corners = new double[4];
 		double[] diffi = new double[grfData.get(0).length];
 		double[] siirtymat = new double[grfData.get(0).length];
 		
 		/*FFT analysis*/
 		if (writeFFT){
 			int windowLength = (int) samplingRate*60*60;			//datapoints in an hour
 			double testi = (Math.log(windowLength) / Math.log(2));	//Check whether datapoints in an hour is power of 2
 			int fftWindow = 1<<(int) Math.ceil(testi);			//Select the next longer power of 2 as window size JATKA TASTA!!
 			double[] freq = new double[fftWindow/2];
 			for (int b = 0;b<freq.length;b++){
 				freq[b] = (double) b*(double)samplingRate/(double) fftWindow;
 			}
 			Vector<double[]> fftData = new Vector<double[]>();
 			fft.Complex[] fftDataIn = new fft.Complex[fftWindow];
 			
 			/*Start going through data for the fft*/
 			int hour = 0;
 			while (linenum < grfData.get(0).length-fftWindow){// 1000){// 
 				//Tahan for kaydaan dataInLength verran dataa kerrallaan FFT:ssa
 				int temp = linenum;
 				fftData.clear();
 				for (int ch = 0; ch<4;++ch){
 					temp = linenum;
 					for (int i = 0; i < fftWindow; ++i) {
 						fftDataIn[i] = new fft.Complex(grfData.get(ch)[temp], 0);
 						++temp;
 					}
 					fftData.add(FFT.calculateAmplitudes(FFT.fft(fftDataIn)));
 				}
 				try{
 					BufferedWriter writerTemp2 = new BufferedWriter(new FileWriter(saveName+"FFT_"+fileName.substring(0,fileName.length()-4)+"_"+Integer.toString(animalNo)+"_"+hour+"h.xls",false));				//Overwrite saveName file
 					for (int i = 0; i < fftWindow/2; ++i) {
 						writerTemp2.write(freq[i]+"\t");
 						for (int j = 0; j<4; ++j){
 							writerTemp2.write(Double.toString(fftData.get(j)[i]));
 							if (j <3){
 								writerTemp2.write("\t");
 							}else{
 								writerTemp2.write("\n");
 							}
 						}
 					}
 					writerTemp2.close();	//Close the file
 				}catch(Exception err){}
 				linenum = temp;
 				++hour;
 			}
 		}
 		
 		/*Index analysis*/
 		BufferedWriter writerTemp = null;
 		try{
 			if (writeCoordinates){
 				writerTemp = new BufferedWriter(new FileWriter(saveName+"Coords_"+fileName.substring(0,fileName.length()-4)+"_"+Integer.toString(animalNo)+".xls",false));	//Overwrite saveName file
 			}
 			/*Start going through data*/
 			while (linenum < grfData.get(0).length){// 1000){// 
 				/*Take values and sum to temp vars...*/
 				for (int i = 0; i<4;++i){
 					corners[i] = grfData.get(i)[linenum];
 				}
 				sum = corners[0]+corners[1]+corners[2]+corners[3];
 				if (sum == 0){}else{
 					aks = (corners[1]+corners[2])/(sum)*calibration[0];
 					yy =(corners[2]+corners[3])/(sum)*calibration[1]; 
 				}
 				acc = sum*voltsToKilos/mass;
 				
 				++datapisteita;
 				if (datapisteita > 1){
 					diffi[datapisteita-1] = Math.abs(acc-accOld);
 					siirtymat[datapisteita-1] =  Math.sqrt(Math.pow(aks-aksOld,2.0)+Math.pow(yy-yyOld,2.0));
 				}
 				
 				if (writeCoordinates){
 					writerTemp.write(corners[0]+"\t"+corners[1]+"\t"+corners[2]+"\t"+corners[3]+"\t"+acc+"\t"+diffi[datapisteita-1]+"\t"+aks+"\t"+yy+"\n");
 				}
 				accOld = acc;
 				aksOld = aks;
 				yyOld = yy;
 				++linenum;
 			}
 			if (writeCoordinates){
 				writerTemp.close();
 			}
 		}catch(Exception err){}
 		
 		/*Calculate and print out results*/
 		/*Calculate distance*/
 		int laskuri =0;
 		double matka = 0.0;
 		double matka2 = 0.0;
 		Vector<Double> matkat = new Vector<Double>();
 		for (int i = 0;i<datapisteita-1;i++){
 			matka += siirtymat[i];
 			++laskuri;
 			if (laskuri == (int) (samplingRate*60.0*60.0)){
				matkat.add(matka);
				matka2+=matka;
 				matka =0.0;
 				laskuri =0;
 			}			
 		}	
 		if (matka != 0.0){
			matkat.add(matka);
			matka2+=matka;
 		}		
 		/*Calculate index*/
 		double aind=0;
 		double aind2 = 0;
 		double vali = 0;
 		double vali2 = 0;
 		long kohta=0;
 		Vector<Double> indeksit = new Vector<Double>();
 		laskuri =0;
 		while (kohta < datapisteita-(int) samplingRate-1){
 			vali = 0;
 			for (int i = 0;i<(int) samplingRate;i++){
 				vali += diffi[(int) kohta];
 				kohta++;
 			}
 			aind += vali/samplingRate;
 			++laskuri;
 			if (laskuri == 60*60){
 				indeksit.add(aind);
 				aind2+=aind;
 				aind = 0;
 				laskuri = 0;
 			}
 		}
 		if (aind != 0.0){
 			indeksit.add(aind);
 			aind2+=aind;
 		}
 		/*Print results*/
 		BufferedWriter writer;
 		try{
 			writer = new BufferedWriter(new FileWriter(saveName+fileName.substring(0,fileName.length()-4)+"_"+Integer.toString(animalNo)+".xls",false));	//Overwrite saveName file
 			writer.write("FileName\tMouseNo\tStartTime\tStopTime\n");
 			writer.write(fileName+"\t"+animalNo+"\t"+start+"\t"+stop+"\n");
 			writer.write("Hour\tDistance [mm]\tIndex\n");
 			for (int i =0; i<matkat.size();++i){
 				writer.write(i+"\t"+matkat.get(i)+"\t"+indeksit.get(i)+"\n");
 			}
 			writer.close();
 		}catch(Exception err){}
 		/*For debugging... print total sums out...*/
 		try{
 			writer = new BufferedWriter(new FileWriter(saveName+"SUM_ALL_ANIMALS.xls",true));	//Append to saveName file
 			linenum = 0;
 			writer.write(fileName+"\t"+animalNo+"\t"+start+"\t"+stop+"\t"+samplingRate+"\t"+mass+"\t");
 			writer.write(Double.toString(matka2)+"\t"+Double.toString(aind2)+"\n");
 			writer.close();
 		}catch(Exception err){}
 	}
 	
 	double[] scaleFilterData(ReadWDQ data, int animal, int channel, int animalsInFile,int channelsPerAnimal, double lowPassFrequency,double[] subtract, boolean writeFFT){
 		double[] scaledFiltered = new double[(int)data.dataAmount/(2*data.channelNo)]; /*Reserve Memory for one channel*/
 		/*Read data from file to save memomry*/
 		int headerRead;
 		try{
 			DataInputStream inFile = new DataInputStream(new BufferedInputStream(new FileInputStream(data.fileIn)));
 			inFile.skip((int) data.dataInHeader);
 			inFile.skip(animal*channelsPerAnimal*2+channel*2);
 			for (int j = 0;j<(int)data.dataAmount/(2*data.channelNo);++j){
 				scaledFiltered[j] = ((double) Short.reverseBytes(inFile.readShort()))
 										*data.scalings[animal]*0.25+subtract[channel];
 				inFile.skip((animalsInFile-1)*channelsPerAnimal*2+(channelsPerAnimal-1)*2);	/*skip other animals and the three other channels*/		
 			}
 			inFile.close();			
 		} catch (Exception err) {System.out.println("Can't read "+err.getMessage());}
 
 		/*Filter the data...*/
 		if (!writeFFT){ //Don't filter, if writing FFT has been requested...
 			ButterworthCoefficients butterworthCoefficients = new  ButterworthCoefficients();
 			String[] args = {"Bu","Lp","o","2","a",Double.toString(lowPassFrequency*data.samplingInterval)};
 			butterworthCoefficients.butter(args);	/*Get butterworth coeffiecients*/
 			System.out.println("Coefficients obtained");
 			scaledFiltered = butterworthCoefficients.filtfilt(scaledFiltered);
 		}
 		return scaledFiltered;
 	}
 }
