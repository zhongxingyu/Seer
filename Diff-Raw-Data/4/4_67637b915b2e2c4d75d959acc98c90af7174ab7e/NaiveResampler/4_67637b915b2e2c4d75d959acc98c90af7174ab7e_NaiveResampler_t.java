 package com.stefankopieczek.audinance.conversion.resamplers;
 import com.stefankopieczek.audinance.formats.*;
 import com.stefankopieczek.audinance.audiosources.*;
 
 public class NaiveResampler implements Resampler
 {
 	public DecodedAudio resample(DecodedAudio original, 
 	                             Integer targetSampleRate)
 	{
 		DecodedSource[] originalChannels = 
 		                                       original.getChannels();
 		AudioFormat originalFormat = original.getFormat();
 		Integer originalSampleRate = originalFormat.getSampleRate();
 
 		AudioFormat newFormat = 
 			new AudioFormat(targetSampleRate, 
 			                originalFormat.getNumChannels());
 		
 		DecodedSource[] newChannels = 
 	                	   new DecodedSource[originalChannels.length];
 		
 		for (int ii = 0; ii < newChannels.length; ii++)
 		{
 			newChannels[ii] = new ResamplingSource(originalChannels[ii],
			                                       originalSampleRate.intValue(),
												   targetSampleRate.intValue());
 		}
 		
 		DecodedAudio result = new DecodedAudio(newChannels,
 		                                       newFormat);
 											   
 		return result;
 	}
 	
 	class ResamplingSource extends DecodedSource
 	{
 		private final DecodedSource mOriginal;
 		private final float mScaleFactor;
 		
 		public ResamplingSource(DecodedSource original,
 		                        int originalSampleRate,
 								int targetSampleRate)
 		{
 			mOriginal = original;
 			mScaleFactor = originalSampleRate / targetSampleRate;
 		}
 		
 		public double getSample(int idx)
 		{
 			float floatIdx = idx * mScaleFactor;
 			
 			double precursor = mOriginal.getSample((int)floatIdx);
 			
 			double result = 0;
 			
 			boolean hasSuccessor = true;
 			
 			double successor = 0;
 			
 			try
 			{
 				successor = mOriginal.getSample(
                                             (int)Math.ceil(floatIdx));
 			}
 			catch (ArrayIndexOutOfBoundsException e)
 			{
 				hasSuccessor = false;
 			}
 			
 			if (hasSuccessor)
 			{
 				result = (precursor+successor)/2;
 			}
 			else
 			{
 				result = precursor;
 			}
 			
 			return result;
 		}
 	}
 }
 			
