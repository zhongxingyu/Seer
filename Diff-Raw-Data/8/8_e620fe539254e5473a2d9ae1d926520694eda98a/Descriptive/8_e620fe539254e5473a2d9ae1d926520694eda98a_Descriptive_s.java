 package com.android.statscalc.stats;
 
 import java.util.ArrayList;
 
 public class Descriptive {
 	private ArrayList<Double> sample;
 	boolean sorted;
 	
 	public Descriptive(){
 		sample = new ArrayList<Double>();
 		sorted = false;
 	}
 	
 	public void addValue(double value){
 		sample.add(value);
 	}
 	
 	public void clear(){
 		sample.clear();
 	}
 	
 	public int getSampleSize(){
 		return sample.size();
 	}
 	
 	public double getMin(){
 		if(!sorted){
 			sortArray();
 		}
 		return sample.get(0);
 	}
 	
 	public double getMax(){
 		if(!sorted){
 			sortArray();
 		}
 		return sample.get(sample.size() - 1);
 	}
 	
 	public double getSum(){
 		double sum = 0;
 		for(int i = 0; i < sample.size(); i ++){
 			sum += sample.get(i);
 		}
 		return sum;
 	}
 	
 	public double getMean(){
 		double mean = 0;
		for(int i = 0; i < sample.size(); i++){
			mean += sample.get(i); 
		}
 		return mean;
 	}
 	
 	public double getMedian(){
 		if(!sorted){
 			sortArray();
 		}
 		if (sample.size() % 2 == 0) {
 			return (sample.get(sample.size() / 2 - 1) + sample.get(sample.size() / 2)) / 2;
 		} else {
 			return sample.get(sample.size() / 2);
 		}
 	}
 	
 	public double getMode(){
 		if(!sorted){
 			sortArray();
 		}
 		int count = 0;
 		int temp = 0;
 		int index = 0;
 		for(int i = 0; i < sample.size() - 1; i ++){
 			for(int j = 0; j < sample.size(); j ++){
 				if(sample.get(i) == sample.get(j)){
 					count++;
 				}
 			}
 			if(count > temp){
 				temp = count;
 				index = i;
 			}
 		}
 		return sample.get(index);
 	}
 	
 	public double getStandardDeviation(){
 		double mean = this.getMean();
 		double standardDeviation = 0;
 		for(int i = 0; i < sample.size(); i++){
 			standardDeviation += Math.pow(sample.get(i) - mean, 2);
 		}
 		standardDeviation /= this.getSampleSize();
 		standardDeviation = Math.sqrt(standardDeviation);
 		return standardDeviation;
 		
 	}
 	
 	public double getStandardError(){
 		return this.getStandardDeviation() / Math.sqrt(this.getSampleSize());
 	}
 
 	private void sortArray() {
 		for(int i = 0; i < sample.size() - 1; i++){
			for(int j = 1; j < sample.size(); j ++){
 				if(sample.get(j) < sample.get(i)){
 					Double temp = sample.get(j);
 					sample.set(j, sample.get(i));
 					sample.set(i, temp);
 					temp = null;
 				}
 			}
 		}
 		
 	}
 
 }
