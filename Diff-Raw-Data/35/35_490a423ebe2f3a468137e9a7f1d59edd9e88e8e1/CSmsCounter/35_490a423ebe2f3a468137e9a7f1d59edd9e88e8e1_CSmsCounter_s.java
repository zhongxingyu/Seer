 package com.monster.pocketsafe;
 
 import android.util.Log;
 
 public class CSmsCounter {
	public static final int LEN_SMALL = 65;
 	public static final int LEN_LONG = 160;
 	
 	private String mText;
 	private int mLen;
 	private int mLenOne;
 	private int mCount;
 	private int mOst;
 	private int mNextLimit;
 	
 	public CSmsCounter() {
 		setSms("");
 	}
 	
 	public CSmsCounter(String sms) {
 		setSms(sms);
 	}
 	
 	public void setSms(String sms) {
 		mText = new String( sms );
 		mLen = mText.length();
 		
 		byte[] bit = mText.getBytes();
 		mLenOne = LEN_LONG;
 		for (int i=0; i<bit.length; i++)
 			if (bit[i]>127 || bit[i]<0) {
 				mLenOne = LEN_SMALL;
 				break;
 			}
 
 		if (mLen==0)
 			mCount=1;
 		else
 			mCount = (mLen-1)/mLenOne+1;
 		
 		if (mLen%mLenOne==0 && mLen>0)
 			mOst=0;
 		else
 			mOst = mLenOne - mLen%mLenOne;
 		
 		mNextLimit = mCount*mLenOne;		
 	}
 	
 	public int getLen() 		{ return mLen; 		}
 	public int getLenOne() 		{ return mLenOne; 	}
 	public int getCount() 		{ return mCount; 	}
 	public int getOst() 		{ return mOst; 		}
 	public int getNextLimit() 	{ return mNextLimit;}
 	
 	public String toString() {
 		String res = new String(mOst+"/"+mNextLimit);
 		if (mCount>1) 
 			res += "("+mCount+")";
 		
 		return res;
 	}
 	
 }
