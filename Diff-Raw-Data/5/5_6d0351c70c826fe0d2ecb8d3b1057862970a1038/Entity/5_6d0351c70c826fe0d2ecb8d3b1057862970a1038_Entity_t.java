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
 
 package de.raptor2101.GalDroid.WebGallery.Gallery3.JSON;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.sax.RootElement;

 
 import de.raptor2101.GalDroid.WebGallery.Gallery3.DownloadObject;
 import de.raptor2101.GalDroid.WebGallery.Gallery3.Gallery3Imp;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 
 public abstract class Entity implements GalleryObject {
 	private final Pattern mPatternExtractTagId = Pattern.compile("tag_item/(\\d+),\\d+");
 	private final String mRootLink;
 	private final String mTitle;
 	private final String mLink;
 	private final int mId;
 	
 	private final Date mUploadDate;
 	private final ArrayList<String> mTags;
 	protected String mLink_Full;
 	protected String mLink_Thumb;
 	
 	protected int mFileSize_Full;
 	protected int mFileSize_Thumb;
 	
 	public Entity(JSONObject jsonObject, Gallery3Imp gallery3) throws JSONException
 	{
 		JSONObject entity = jsonObject.getJSONObject("entity");
 		
 		mId = entity.getInt("id");
 		
 		long msElapsed = entity.getLong("created")*1000;
 		mUploadDate = new Date(msElapsed);
 		
 		mTitle = entity.getString("title");
 		mLink = gallery3.getItemLink(mId);
 		mRootLink = gallery3.getRootLink();
 		
 		
 		JSONObject relationShips  = jsonObject.getJSONObject("relationships");
 		JSONObject tagsSection = relationShips.getJSONObject("tags");
 		JSONArray tags = tagsSection.getJSONArray("members");
 		
 		mTags = new ArrayList<String>(tags.length());
 		for(int i=0;i<tags.length();i++) {
 			Matcher matcher = mPatternExtractTagId.matcher(tags.getString(i));
 			while(matcher.find()) {
 				String tagId = matcher.group(1);
 				mTags.add(String.format(gallery3.LinkRest_LoadTag, tagId));
 			}
 		}
 	}
 	
 	public String getTitle() {
 		return mTitle;
 	}
 	
 	public int getId() {
 		return mId;
 	}
 	
 	public String getObjectId() {
		return String.format("%s/%d", mRootLink, mId);
 	}
 	
 	public String getObjectLink() {
 		return mLink;
 	}
 	
 	public Date getDateUploaded() {
 		return mUploadDate;
 	}
 	
 	public DownloadObject getImage() {
 		return createDownloadObject(mLink_Full, mFileSize_Full);
 	}
 	
 	public DownloadObject getThumbnail() {
 		return createDownloadObject(mLink_Thumb, mFileSize_Thumb);
 	}
 	
 	private DownloadObject createDownloadObject(String link, int fileSize) {
 		return !link.equals("")? new DownloadObject(mRootLink, link, fileSize) : null;
 	}
 	
 	public List<String> getTagLinks() {
 		return mTags;
 	}
 	
 	@Override
 	public String toString() {
 		return mLink;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if(o instanceof Entity) {
 			Entity entity = (Entity) o;
 			return entity.mId == mId;
 		}
 		else {
 			return false;
 		}
 	}
 }
