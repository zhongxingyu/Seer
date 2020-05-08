 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery.Gallery3;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.FloatMath;
 
 public class PictureEntity extends Entity {
 	
 	
 	public PictureEntity(JSONObject jsonObject, Gallery3Imp gallery3, float maxImageDiag)
 			throws JSONException {
 		super(jsonObject, gallery3);
 		jsonObject = jsonObject.getJSONObject("entity");
 		int resizeHeight = jsonObject.getInt("resize_height");
 		int resizeWidth = jsonObject.getInt("resize_width");
 		
 		float imageDiag = FloatMath.sqrt(resizeHeight*resizeHeight+resizeWidth*resizeWidth);
 		if(imageDiag > maxImageDiag) {
 			mLink_Full = String.format(gallery3.LinkRest_LoadPicture, getId(), "full");
			mFileSize_Thumb = jsonObject.getInt("file_size");
 		} else {
 			mLink_Full = String.format(gallery3.LinkRest_LoadPicture, getId(), "resize");
			mFileSize_Thumb = jsonObject.getInt("resize_size");
 		}
 			
 		
 		mLink_Thumb = String.format(gallery3.LinkRest_LoadPicture, getId(), "thumb");
 		mFileSize_Thumb = jsonObject.getInt("thumb_size");
 	}
 
 	public boolean hasChildren() {
 		// A Image never have childs
 		return false;
 	}
 }
