 /*
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.parworks.androidlibrary.response;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * An object specifying all the information about an ARSite. This contains all
  * of the attributes returned by the Get Site Info endpoint.
  * 
  * @author Adam Hickey
  * 
  */
 public class SiteInfo {
 
 	public enum FeatureType {
 		SIFT, SURF, FREAK
 	}
 
 	private String mName;
 	private String mId;
 	private String mChannel;
 	private String mDescription;
 	private String mGeoHash;
 	private BaseImageState mSiteState;
 	private String mLatitude;
 	private String mLongitude;
 	private OverlayState mBimState;
 	private String mFeatureType;
 	private String mS3Bucket;
 	private String mLastModificationTime;
 	private String mOwnerApiKey;
 	private int mTotalImages;
 	private FeatureType mFeatureDescriptorType;
 	private String mProcessingProfile;
 	private String mAddress;
 	private String posterImageOverlayContent;
 	private String posterImageURL;
 	private AugmentedImage augmentedPosterImage;
 	private List<String> tags;
 
 	public enum OverlayState {
 		PROCESSING, PROCESSED, PROCESSING_FAILED, NOT_PROCESSED
 	}
 
 	public enum BaseImageState {
 		PROCESSING, PROCESSED, PROCESSING_FAILED, NOT_PROCESSED
 	}
 	public Map<String,String> toParameterMap() {
 		Map<String, String> parameterMap = new HashMap<String, String>();
 		parameterMap.put("id", getId());
 		parameterMap.put("name", getName());
 		parameterMap.put("lon", getLon());
 		parameterMap.put("lat", getLat());
 		parameterMap.put("description", getDescription());
 		parameterMap.put("feature", getFeatureType());
 		parameterMap.put("channel", getChannel());
 		parameterMap.put("address", getAddress());
 		parameterMap.put("posterImageOverlayContent",getPosterImageOverlayContent());
		parameterMap.put("sitestate", getSiteState().name());
		parameterMap.put("bimstate", getBimState().name());
 		parameterMap.put("posterImageUrl", getPosterImageURL());
 		return parameterMap;
 	}
 
 	public SiteInfo() {
 	}
 
 	public SiteInfo(String name, String id, String channel, String description,
 			String geoHash, BaseImageState siteState, String lat, String lon,
 			OverlayState bimState, String featureType, String s3Bucket,
 			String lastModificationTime) {
 		mName = name;
 		mId = id;
 		mChannel = channel;
 		mDescription = description;
 		mGeoHash = geoHash;
 		mSiteState = siteState;
 		mLatitude = lat;
 		mLongitude = lon;
 		mBimState = bimState;
 		mFeatureType = featureType;
 		mS3Bucket = s3Bucket;
 		mLastModificationTime = lastModificationTime;
 	}
 
 	public void setOwnerApiKey(String ownerApiKey) {
 		mOwnerApiKey = ownerApiKey;
 	}
 
 	public String getOwnerApiKey() {
 		return mOwnerApiKey;
 	}
 
 	public String getName() {
 		return mName;
 	}
 
 	public void setName(String name) {
 		this.mName = name;
 	}
 
 	public String getId() {
 		return mId;
 	}
 
 	public void setId(String id) {
 		this.mId = id;
 	}
 
 	public String getChannel() {
 		return mChannel;
 	}
 
 	public void setChannel(String channel) {
 		this.mChannel = channel;
 	}
 
 	public String getDescription() {
 		return mDescription;
 	}
 
 	public void setDescription(String description) {
 		this.mDescription = description;
 	}
 
 	public String getGeoHash() {
 		return mGeoHash;
 	}
 
 	public void setGeoHash(String geoHash) {
 		this.mGeoHash = geoHash;
 	}
 
 	public BaseImageState getSiteState() {
 		return mSiteState;
 	}
 
 	public void setSiteState(BaseImageState siteState) {
 		this.mSiteState = siteState;
 	}
 
 	public String getLat() {
 		return mLatitude;
 	}
 
 	public void setLat(String lat) {
 		this.mLatitude = lat;
 	}
 
 	public String getLon() {
 		return mLongitude;
 	}
 
 	public void setLon(String lon) {
 		this.mLongitude = lon;
 	}
 
 	public OverlayState getBimState() {
 		return mBimState;
 	}
 
 	public void setBimState(OverlayState bimState) {
 		this.mBimState = bimState;
 	}
 
 	public String getFeatureType() {
 		return mFeatureType;
 	}
 
 	public void setFeatureType(String featureType) {
 		this.mFeatureType = featureType;
 	}
 
 	public String getS3Bucket() {
 		return mS3Bucket;
 	}
 
 	public void setS3Bucket(String s3Bucket) {
 		this.mS3Bucket = s3Bucket;
 	}
 
 	public String getLastModificationTime() {
 		return mLastModificationTime;
 	}
 
 	public void setLastModificationTime(String lastModificationTime) {
 		this.mLastModificationTime = lastModificationTime;
 	}
 
 	public int getTotalImages() {
 		return mTotalImages;
 	}
 
 	public void setTotalImages(int totalImages) {
 		this.mTotalImages = totalImages;
 	}
 
 	public FeatureType getFeatureDescriptorType() {
 		return mFeatureDescriptorType;
 	}
 
 	public void setFeatureDescriptorType(FeatureType featureDescriptorType) {
 		mFeatureDescriptorType = featureDescriptorType;
 	}
 
 	public String getProcessingProfile() {
 		return mProcessingProfile;
 	}
 
 	public void setProcessingProfile(String processingProfile) {
 		mProcessingProfile = processingProfile;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if(o == null) {
 			return false;
 		}
 		SiteInfo otherInfo = (SiteInfo)o;
 		if(!mId.equals(otherInfo.getId())) {
 			return false;
 		} else if (!mName.equals(otherInfo.getName())){
 			return false;
 //		} else if (!mBimState.equals(otherInfo.getBimState())) {
 //			return false;
 //		} else if (!mChannel.equals(otherInfo.getChannel())) {
 //			return false;
 		} else if (!mDescription.equals(otherInfo.getDescription()) ) {
 			return false;
 		} else if (!mFeatureDescriptorType.equals(otherInfo.getFeatureDescriptorType()) ) {
 			return false;
 //		} else if (!mFeatureType.equals(otherInfo.getFeatureType()) ) {
 //			return false;
 //		} else if (!mGeoHash.equals(otherInfo.getGeoHash()) ) {
 //			return false;
 //		} else if (!mLastModificationTime.equals(otherInfo.getLastModificationTime()) ) {
 //			return false;
 		} else if (!mLatitude.equals(otherInfo.getLat())) {
 			return false;
 		} else if (!mLongitude.equals(otherInfo.getLon())) {
 			return false;
 //		} else if (!mOwnerApiKey.equals(otherInfo.getOwnerApiKey())) {
 //			return false;
 		} else if (!mProcessingProfile.equals(otherInfo.getProcessingProfile()) ) {
 			return false;
 //		} else if (!mS3Bucket.equals(otherInfo.getS3Bucket())) {
 //			return false;
 //		} else if (!mSiteState.equals(otherInfo.getSiteState())) {
 //			return false;
 //		} else if (! (mTotalImages == otherInfo.getTotalImages())) {
 //			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public String getAddress() {
 		return mAddress;
 	}
 
 	public void setAddress(String mAddress) {
 		this.mAddress = mAddress;
 	}
 
 	public String getPosterImageOverlayContent() {
 		return posterImageOverlayContent;
 	}
 
 	public void setPosterImageOverlayContent(String posterImageOverlayContent) {
 		this.posterImageOverlayContent = posterImageOverlayContent;
 	}
 
 	public String getPosterImageURL() {
 		return posterImageURL;
 	}
 
 	public void setPosterImageURL(String posterImageURL) {
 		this.posterImageURL = posterImageURL;
 	}
 
 	public List<String> getTags() {
 		return tags;
 	}
 
 	public void setTags(List<String> tags) {
 		this.tags = tags;
 	}
 
 	public AugmentedImage getAugmentedPosterImage() {
 		return augmentedPosterImage;
 	}
 
 	public void setAugmentedPosterImage(AugmentedImage augmentedPosterImage) {
 		this.augmentedPosterImage = augmentedPosterImage;
 	}
 
 
 }
