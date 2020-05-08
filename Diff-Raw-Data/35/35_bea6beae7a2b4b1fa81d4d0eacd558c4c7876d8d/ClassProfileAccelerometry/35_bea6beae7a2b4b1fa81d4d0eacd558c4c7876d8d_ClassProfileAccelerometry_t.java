 //
 // Copyright (C) 2013, Maxim Osipov
 //
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without modification,
 // are permitted provided that the following conditions are met:
 //
 //  - Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 //  - Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 //  - Neither the name of the University of Oxford nor the names of its
 //    contributors may be used to endorse or promote products derived from this
 //    software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 // IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 // LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 // OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 
 package com.ibme.android.actopsy;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TimeZone;
 
 import com.google.gson.Gson;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 public class ClassProfileAccelerometry {
 
 	private static final String TAG = "ActopsyProfileAccelerometry";
 
 	public static final int LENGTH = 24*60;		// 1 minutes intervals
 	public static final long MILLIPERIOD = ClassConsts.MILLIDAY/LENGTH;
 
 	static public class Values {
 		public long t;
 		public float x;
 		public float y;
 		public float z;
 		public long n;
 		public float l;
 		public Values(long time, float xv, float yv, float zv, long vn, float lv)
 			{ t = time; x = xv; y = yv; z = zv; n = vn; l = lv; }
 	}
 
 	private long mPeriodLimit;
 	private double mPeriodSumX, mPeriodSumY, mPeriodSumZ, mPeriodSumL;
 	private long mPeriodNum;
 
 	private Context mContext;
 
 	public ClassProfileAccelerometry(Context context)
 	{
 		mContext = context;
 	}
 
 	public void init(long ts)
 	{
 		// TODO: Fix it to check and wait for storage
 		try {
 			File root = mContext.getFilesDir();
 			if (!root.exists()) {
 				root.mkdirs();
 			}
 			if (root.canWrite()){
 				File folder = new File(root, ClassConsts.PROFILES_ROOT);
 				if (!folder.exists()) {
 					folder.mkdirs();
 				}
 			}
 		} catch (Exception e) {
 			new ClassEvents(TAG, "ERROR", "Could not open file " + e.getMessage());
 		}
 		mPeriodLimit =  ((long)ts/MILLIPERIOD)*MILLIPERIOD + MILLIPERIOD;
 		mPeriodSumX = 0;
 		mPeriodSumY = 0;
 		mPeriodSumZ = 0;
 		mPeriodSumL = 0;
 		mPeriodNum = 0;
 	}
 
 	public void fini()
 	{
 		mPeriodLimit =  MILLIPERIOD;
 		mPeriodSumX = 0;
 		mPeriodSumY = 0;
 		mPeriodSumZ = 0;
 		mPeriodSumL = 0;
 		mPeriodNum = 0;
 	}
 
	// Get profile values for a day identified by ts
	public Values[] get(long ts_utc)
 	{
		long ts_off = TimeZone.getDefault().getOffset(new Date().getTime());
		long ts_local = ts_utc + ts_off;

 		ArrayList<Values> vals = new ArrayList<Values>();
		vals = readVals(getFile(ts_local));
 		return vals.toArray(new Values[vals.size()]);
 	}
 
 	// Update acceleration value
 	public void update(long ts, float x, float y, float z)
 	{
 		// Detect profile period roll-over and save (average) data
 		if (ts > mPeriodLimit) {
 			if (mPeriodNum > 0) {
				// Save averaged for all history profile data with local timestamps
				long ts_off = TimeZone.getDefault().getOffset(new Date().getTime());
				long ts_local = mPeriodLimit + ts_off;

 				Bundle params = new Bundle();
				params.putLong("time", ts_local);
 				params.putFloat("x", (float)(mPeriodSumX/mPeriodNum));
 				params.putFloat("y", (float)(mPeriodSumY/mPeriodNum));
 				params.putFloat("z", (float)(mPeriodSumZ/mPeriodNum));
 				params.putFloat("l", (float)(mPeriodSumL/mPeriodNum));
 				params.putLong("n", mPeriodNum);
 				new UpdaterTask().execute(params);
 			}
 			mPeriodSumX = x;
 			mPeriodSumY = y;
 			mPeriodSumZ = z;
 			mPeriodSumL = Math.abs(Math.sqrt(x*x + y*y + z*z) - ClassConsts.G);
 			mPeriodNum = 1;
 			mPeriodLimit = ((long)ts/MILLIPERIOD)*MILLIPERIOD + MILLIPERIOD;
 		} else {
 			mPeriodSumX += Math.abs(x);
 			mPeriodSumY += Math.abs(y);
 			mPeriodSumZ += Math.abs(z);
 			mPeriodSumL += Math.abs(Math.sqrt(x*x + y*y + z*z) - ClassConsts.G);
 			mPeriodNum ++;
 		}
 	}
 
 	// Get profiles ready for upload
 	public File[] getUploads()
 	{
 		File[] files = new File[0];
 		File root = mContext.getFilesDir();
 		File folder = new File(root, ClassConsts.PROFILES_ROOT);
 		long now = System.currentTimeMillis();
 
 		// First, remove old files (yes - hack, I know)
 		files = folder.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.toLowerCase().matches(".*");
 			}
 		});
 		for (int i = 0; i < files.length; i++) {
 			long age = files[i].lastModified();
 			// TODO: change 10 to proper consts definition
 			if ((age > 0) && (age + 10*ClassConsts.MILLIDAY < now)) {
 				if (files[i].delete()) {
 					new ClassEvents(TAG, "INFO", "Deleted " + files[i].getAbsolutePath());
 				} else {
 					new ClassEvents(TAG, "ERROR", "Delete failed " + files[i].getAbsolutePath());
 				}
 			}
 		}
 
 		// Now prepare list of not uploaded files and tag as uploaded
 		ArrayList<File> uploads = new ArrayList<File>();
 		files = folder.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.toLowerCase().matches(".*json$");
 			}
 		});
 		for (int i = 0; i < files.length; i++) {
 			long age = files[i].lastModified();
 			// At least 1 day old files
 			if ((age > 0) && (age + 1*ClassConsts.MILLIDAY < now)) {
 				File uploaded = new File(files[i].getAbsolutePath() + ".uploaded");
 				if (!uploaded.exists()) {
 					try {
 						uploaded.createNewFile();
 						uploads.add(files[i]);
 					} catch (IOException e) {
 						new ClassEvents(TAG, "ERROR", "Couldn't create " + uploaded.getAbsolutePath());
 					}
 				}
 			}
 		}
 
 		return uploads.toArray(new File[uploads.size()]);
 	}
 
 
 	private class UpdaterTask extends AsyncTask<Bundle, Void, Void> {
 		@Override
 		protected Void doInBackground(Bundle... params) {
 			ArrayList<Values> vals = new ArrayList<Values>();
 			long time = params[0].getLong("time");
 			float x = params[0].getFloat("x");
 			float y = params[0].getFloat("y");
 			float z = params[0].getFloat("z");
 			float l = params[0].getFloat("l");
 			long n = params[0].getLong("n");
 
 			// Update profile data
 			vals.add(new Values(time, x, y, z, n, l));
 			writeVals(getFile(time), vals);
 
 			return null;
 		}
 	}
 
 	///////////////////////////////////////////////////////////////////////////
 	// Serialization support functions
 	///////////////////////////////////////////////////////////////////////////
 	public ArrayList<Values> readVals(File file) {
 		ArrayList<Values> vals = new ArrayList<Values>();
 		FileInputStream stream;
 		BufferedReader reader;
 		// Do nothing if cannot open file
 		try {
 			stream = new FileInputStream(file);
 			reader = new BufferedReader(new InputStreamReader(stream));
 		} catch (Exception e) {
 			return vals;
 		}
 		// Handle read errors
 		try {
 			String line = reader.readLine();
 			while (line != null) {
 				String[] str = line.split(",");
 				long t, n;
 				float x, y, z, l;
 				Values v;
 				t = Long.parseLong(str[0]);
 				x = Float.parseFloat(str[1]);
 				y = Float.parseFloat(str[2]);
 				z = Float.parseFloat(str[3]);
 				n = Long.parseLong(str[4]);
 				if (str.length > 5) {
 					l = Float.parseFloat(str[5]);
 				} else {
 					l = (float)Math.abs(Math.sqrt(x*x + y*y + z*z) - ClassConsts.G);
 				}
 				v = new Values(t, x, y, z, n, l);
 				vals.add(v);
 				line = reader.readLine();
 			}
 			reader.close();
 		} catch (Exception e) {
 			new ClassEvents(TAG, "ERROR", "Couldn't read " + file.getAbsolutePath());
 		} finally {
 			try {
 			    stream.close();
 			}
 			catch (IOException e) {
 				new ClassEvents(TAG, "ERROR", "Couldn't close " + file.getAbsolutePath());
 		    }
 		}
 		return vals;
 	}
 
 	public void writeVals(File file, ArrayList<Values> vals) {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
 			for (Values v : vals) {
 				writer.write(Long.toString(v.t) +
 						"," + Float.toString(v.x) +
 						"," + Float.toString(v.y) +
 						"," + Float.toString(v.z) +
 						"," + Long.toString(v.n) +
 						"," + Float.toString(v.l) +
 						"\n");
 			}
 			writer.close();
 		} catch (Exception e) {
 			new ClassEvents(TAG, "ERROR", "Couldn't write " + file.getAbsolutePath());
 		}
 	}
 
	private File getFile(long ts_local) {
 		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		String name = new String("profile-activity-" + fmt.format(new Date(ts_local)) + ".json");
 		File root = mContext.getFilesDir();
 		File folder = new File(root, ClassConsts.PROFILES_ROOT);
 		File file = new File(folder, name);
 		return file;
 	}
 }
