 package com.stefankopieczek.audinance.formats.wav;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteOrder;
 
 import com.stefankopieczek.audinance.audiosources.*;
 import com.stefankopieczek.audinance.formats.AudioFormat;
 import com.stefankopieczek.audinance.formats.DecodedAudio;
 import com.stefankopieczek.audinance.utils.AudinanceUtils;
 
 public class WavDecoder
 {
 	private EncodedSource mWavSource;
 	
 	public WavDecoder(EncodedSource wavSource)
 	{
 		mWavSource = wavSource;
 	}
 	
 	protected byte[] getRange(int start, int length)
 	{				
 		byte[] result = new byte[length];
 		
 		for (int idx = 0; idx < length; idx++)
 		{
 			result[idx] = mWavSource.getByte(start + idx);
 		}
 		
 		return result;
 	}
 	
 	public DecodedAudio getDecodedAudio()
 		throws InvalidWavDataException, UnsupportedWavEncodingException
 	{
 		
 		RiffChunk riffChunk = new RiffChunk(0);
 		final FmtSubchunk fmtChunk = new FmtSubchunk(RiffChunk.DATA_IDX_OFFSET, 
 				                                     riffChunk);
 		final DataSubchunk dataChunk = new DataSubchunk(fmtChunk.getEndIndex(), 
 				                                        riffChunk,
 				                                        fmtChunk.getBitsPerSample());
 		
 		DecodedSource[] channels = new DecodedSource[fmtChunk.getNumChannels()];		
 		
 		for (int channel = 0; channel < fmtChunk.getNumChannels(); channel++)
 		{
 			final int finalChannel = channel;
 			channels[channel] = new DecodedSource()
 			{
 				public double getSample(int idx) 
 					throws InvalidWavDataException, NoMoreDataException
 				{
 					int frameStartIdx = fmtChunk.getBitsPerSample() * 
 							            idx * 
 							            fmtChunk.getNumChannels();
 					int offsetToSample = finalChannel * 
 							                          fmtChunk.getBitsPerSample();
 					return dataChunk.getSample((frameStartIdx + offsetToSample) / 8);
 				}
 			};
 		}
 		
 		AudioFormat format = new AudioFormat(fmtChunk.getSampleRate(),
 				                             (int)fmtChunk.getNumChannels());
 		
 		return new DecodedAudio(channels, format);		
 	}
 	
 	public WavFormat getFormat()
 		throws InvalidWavDataException, UnsupportedWavEncodingException
 	{
 		RiffChunk riffChunk = new RiffChunk(0);
 		final FmtSubchunk fmtChunk = new FmtSubchunk(RiffChunk.DATA_IDX_OFFSET, 
 				                                     riffChunk);
 		return new WavFormat(fmtChunk.getSampleRate(),
 				             (int)fmtChunk.getNumChannels(),
 				             fmtChunk.getEncodingType(),
 				             fmtChunk.getBitsPerSample());
 	}
 	
 	private abstract class Chunk
 	{
 		private Integer mStartIdx;
 		private Integer mEndIdx;
 		private Integer mLength;
 		
 		protected abstract int getChunkSizeIdxOffset();
 		
 		public Chunk(int startIdx) throws InvalidWavDataException
 		{
 			mStartIdx = startIdx;
 		}
 		
 		public abstract ByteOrder getEndianism() throws InvalidWavDataException; 
 		
 		protected int getStartIndex()
 		{
 			return mStartIdx.intValue();
 		}
 		
 		protected int getEndIndex() throws InvalidWavDataException
 		{
 			if (mEndIdx == null)
 				mEndIdx = mStartIdx + getLength();
 				
 			return mEndIdx.intValue();
 		}
 		
 		protected int getLength() throws InvalidWavDataException
 		{
 			int chunkSizeIdx = mStartIdx + getChunkSizeIdxOffset();
 			
 			if (mLength == null)
 			{
 				int lengthValue = AudinanceUtils.intFromBytes(
 						                   getRange(chunkSizeIdx, 4), getEndianism());
 				 mLength = getChunkSizeIdxOffset() + 4 + lengthValue;			                     
 			}
 			
 			return mLength.intValue();
 		}
 		
 		protected short getShort(int idx) throws InvalidWavDataException
 		{
 			byte[] bytes = getRange(getStartIndex() + idx, 2);
 			return AudinanceUtils.shortFromBytes(bytes, getEndianism());
 		}
 		
 		protected int getInt(int idx) throws InvalidWavDataException
 		{
 			byte[] bytes = getRange(getStartIndex() + idx, 4);
 			return AudinanceUtils.intFromBytes(bytes, getEndianism());
 		}
 	}
 	
 	private class RiffChunk extends Chunk
 	{
 		private static final int ID_IDX_OFFSET = 0;
 		private static final int CHUNK_SIZE_IDX_OFFSET = 4;
 		private static final int DATA_IDX_OFFSET = 12;
 		
 		private ByteOrder mEndianism;
 		
 		public RiffChunk(int startIdx) throws InvalidWavDataException
 		{
 			super(startIdx);
 		}		
 		
 		public ByteOrder getEndianism() throws InvalidWavDataException
 		{
 			if (mEndianism == null)
 			{
 				String chunkId = null;
 				try
 				{
 					chunkId = AudinanceUtils.stringFromBytes(getRange(ID_IDX_OFFSET, 4));
 				}
 				catch (UnsupportedEncodingException e)
 				{
 					throw new InvalidWavDataException("Invalid RIFF header ID format.", 
 						                          	  e);
 				}
 				
 				ByteOrder endianism = null;
 				if (chunkId.equals("RIFF"))
 				{
 					endianism = ByteOrder.LITTLE_ENDIAN;
 				}
 				else if (chunkId.equals("RIFX"))
 				{
 					endianism = ByteOrder.BIG_ENDIAN;
 				}
 				else
 				{
 					throw new InvalidWavDataException("Invalid RIFF header ID " + 
 			                                          chunkId);				
 				}
 			}
 			
 			return mEndianism;
 		}
 		
 		protected int getChunkSizeIdxOffset()
 		{
 			return CHUNK_SIZE_IDX_OFFSET;
 		}		
 	}
 	
 	private class FmtSubchunk extends Chunk
 	{
 		private static final int CHUNK_SIZE_IDX_OFFSET = 4;
 		private static final int FORMAT_CODE_IDX_OFFSET = 8;
 		private static final int NUM_CHANNELS_IDX_OFFSET = 10;
 		private static final int SAMPLE_RATE_IDX_OFFSET = 12;
 		private static final int BYTE_RATE_IDX_OFFSET = 16;
 		private static final int BLOCK_ALIGN_IDX_OFFSET = 20;
 		private static final int BITS_PER_SAMPLE_IDX_OFFSET = 22;
 		private static final int EXTRA_PARAMS_SIZE_IDX_OFFSET = 24;
 		private static final int EXTRA_PARAMS_IDX_OFFSET = 26;
 		
 		private RiffChunk mParent;
 		private Short mFormatCode;
 		private Short mNumChannels;
 		private Integer mSampleRate;
 		private Integer mByteRate;
 		private Short mBlockAlign;
 		private Short mBitsPerSample;
 		private Integer mExtraParamsSize;
 
 		public FmtSubchunk(int startIdx, RiffChunk parent)
 			throws InvalidWavDataException
 		{
 			super(startIdx);
 			mParent = parent;
 		}
 		
 		public ByteOrder getEndianism() throws InvalidWavDataException
 		{
 			return mParent.getEndianism();
 		}
 
 		protected int getChunkSizeIdxOffset()
 		{
 			return CHUNK_SIZE_IDX_OFFSET;
 		}				
 		
 		public short getFormatCode() throws InvalidWavDataException
 		{
 			if (mFormatCode == null)
 			{
 				mFormatCode = getShort(FORMAT_CODE_IDX_OFFSET);
 			}
 			
 			return mFormatCode.shortValue();
 		}
 		
 		public WavEncodingType getEncodingType()
 			throws UnsupportedWavEncodingException, InvalidWavDataException
 		{
 			return WavEncodingType.getEncodingTypeFromCode(getFormatCode());
 		}
 		
 		public short getNumChannels() throws InvalidWavDataException
 		{
 			if (mNumChannels == null)
 			{
 				mNumChannels = getShort(NUM_CHANNELS_IDX_OFFSET);
 			}
 			
 			return mNumChannels.shortValue();
 		}
 				
 		public int getSampleRate() throws InvalidWavDataException
 		{
 			if (mSampleRate == null)
 			{
 				mSampleRate = getInt(SAMPLE_RATE_IDX_OFFSET);
 			}
 			
 			return mSampleRate.intValue();
 		}
 		
 		public int getByteRate() throws InvalidWavDataException
 		{
 			if (mByteRate == null)
 			{
 				mByteRate = getInt(BYTE_RATE_IDX_OFFSET);
 			}
 			
 			return mByteRate.intValue();
 		}
 		
 		public short getBlockAlign() throws InvalidWavDataException
 		{
 			if (mBlockAlign == null)
 			{
 				mBlockAlign = getShort(BLOCK_ALIGN_IDX_OFFSET);
 			}
 			
 			return mBlockAlign.shortValue();
 		}
 		
 		public short getBitsPerSample() throws InvalidWavDataException
 		{
 			if (mBitsPerSample == null)
 			{
 				mBitsPerSample = getShort(BITS_PER_SAMPLE_IDX_OFFSET);
 			}
 			
 			return mBitsPerSample.shortValue();
 		}
 	}
 	
 	private class DataSubchunk extends Chunk
 	{
 		private static final int CHUNK_SIZE_IDX_OFFSET = 4;
 		private static final int DATA_IDX_OFFSET = 8;		
 		
 		private RiffChunk mParent;
 		private int mBitsPerSample;
 
 		public DataSubchunk(int startIdx, 
 				            RiffChunk parent,
 				            int bitsPerSample)
 			throws InvalidWavDataException
 		{
 			super(startIdx);
 			mParent = parent;
 			mBitsPerSample = bitsPerSample;
 		}
 
 		public ByteOrder getEndianism() throws InvalidWavDataException
 		{
 			return mParent.getEndianism();
 		}
 		
 		protected int getChunkSizeIdxOffset()
 		{
 			return CHUNK_SIZE_IDX_OFFSET;
 		}
 		
 		public double getSample(int byteIdx) 
 				throws InvalidWavDataException, NoMoreDataException
 		{
 			double result;						
 			int endOfSample = (int)(byteIdx + Math.ceil(mBitsPerSample / 8.0)); 
 			if (endOfSample >= getLength())
 			{
 				throw new NoMoreDataException();
 			}
 			
 			if (mBitsPerSample == 8)
 			{			
 				int tempResult = mWavSource.getByte(getStartIndex() + byteIdx);				
 				tempResult &= 0xFF; // Don't treat the byte as signed.
 				result = tempResult * 2; // Normalise energy of sample to match 16bitPCM.
 				
 			}
 			else if (mBitsPerSample == 16)
 			{
 				byte[] bytes = getRange(getStartIndex() + byteIdx, 2);
 				result = AudinanceUtils.shortFromBytes(bytes, getEndianism());
 			}
 			else if (mBitsPerSample == 32)
 			{
 				byte[] bytes = getRange(getStartIndex() + byteIdx, 4);
				result = AudinanceUtils.intFromBytes(bytes, getEndianism());				
 			}
 			else
 			{
 				throw new InvalidWavDataException("Unsupported bit depth: " + 
 			                                                      mBitsPerSample);
 			}
 			
 			return result;
 		}
 	}
 }
