 /*
  *
  *  Copyright (C) 2009-2011 GSyC/LibreSoft, Universidad Rey Juan Carlos
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see http://www.gnu.org/licenses/. 
  *
  *  Author : Roberto Calvo Palomino <rocapal@libresoft.es>
  *
  */
 
 package com.libresoft.sdk.ARviewer.Types;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 import com.libresoft.sdk.ARviewer.Utils.BitmapUtils;
 
 
 public class Photo extends GeoNode implements Serializable {
 
 	// Serializable UID
 	private static final long serialVersionUID = -9121943636715457236L;
 	
 	private String mName = null;
 	private String mDescription = null;
 	private String mPhotoUrl = null;
 	private String mPhotoPath = null;
 	
 	byte[]  mByteBitMapImageThumb, mByteBitMapImageMedium, mByteBitMapImageLarge, mByteBitMapImage;
 	
 	private User mUploader;
 	
 	public Photo (Integer id, Double latitude, Double longitude, Double altitude, Double radius, 
 			      String name, String description, String url, String since, User uploader,
 			      String position_since, Double distance) 
 	{
 		super(id, latitude, longitude, altitude, radius, since, position_since);
 		
 		mName = name;
 		mDescription = description;
 		mPhotoUrl = url;
 		
 		mUploader = uploader;
 		
 		mByteBitMapImageThumb = null;
 		mByteBitMapImageMedium = null;
 		mByteBitMapImageLarge = null;
 		mByteBitMapImage = null;
 
 
 	}
 
 	public String getName() {
 		return mName;
 	}
 
 	public void setName(String name) {
 		mName = name;
 	}
 
 	public String getDescription() {
 		return mDescription;
 	}
 
 	public void setDescription(String description) {
 		mDescription = description;
 	}
 
 	public String getPhotoUrl() {
 		return mPhotoUrl;
 	}
 
 	public void setPhotoUrl(String url) {
 		mPhotoUrl = url;
 	}
 	
 	public String getPhotoPath()
 	{
 		return mPhotoPath;
 	}
 	
 	public void setPhotoPath (String path)
 	{
 		mPhotoPath = path;
 	}
 	
 	public boolean isBitmapPhoto()
 	{
 		return mByteBitMapImageThumb != null;
 	}
 
 	public Bitmap getBitmapPhoto ()
 	{
		if (mByteBitMapImageThumb != null)
 		{
 			Bitmap bitmapImage = null;
 
 			if (mPhotoUrl != null)
 				bitmapImage = BitmapUtils.loadBitmap(mPhotoUrl);
 
 			else if (mPhotoPath != null)
 				bitmapImage = BitmapUtils.loadBitMapFromFile(mPhotoPath);
 
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			if (!bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos))
 			{
 				Log.e("getBitmapImageThumb","Error: Don't compress de image");
 				return null;
 			}
 			
 			mByteBitMapImageThumb = baos.toByteArray();
 		}
 		
 		return BitmapFactory.decodeStream( new ByteArrayInputStream( mByteBitMapImageThumb) );
 	}
 	
 
 	public int describeContents() {
 		return 0;
 	}
 
 	
 	
 }
