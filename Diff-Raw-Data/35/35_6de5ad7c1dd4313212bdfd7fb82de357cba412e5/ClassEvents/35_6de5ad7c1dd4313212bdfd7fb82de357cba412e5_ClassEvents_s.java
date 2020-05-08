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
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.os.Environment;
 import android.util.Log;
 
 public class ClassEvents {
 
 	private static final String TAG = "ActopsyEvents";
 
 	public ClassEvents(String tag, String type, String str)
 	{
 		try {
 			long ts = System.currentTimeMillis();
 			File root = Environment.getExternalStorageDirectory();
 			if (root.canWrite()){
 				// Create folders
 				File folder = new File(root, ClassConsts.FILES_ROOT);
 				if (!folder.exists()) {
 					folder.mkdirs();
 				}
 				// Initialise events file
 				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
 				File out = new File(folder, "events-" + fmt.format(new Date(ts)) + ".csv");
 				BufferedWriter writer = new BufferedWriter(new FileWriter(out, true));
 				// Write data and close events file 
 				fmt = new SimpleDateFormat(ClassConsts.DATEFMT);
 				Log.d(tag, type + "," +  str + "\n");
 				writer.write(fmt.format(new Date(ts)) + "," + type + "," + str + " (" + tag + ")" + "\n");
 				writer.close();
 			} else {
 				Log.e(TAG, "SD card not writable");
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "Could not write file: " + e.getMessage());
 		}
 	}
 }
