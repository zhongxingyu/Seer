 package org.atlasapi.remotesite.tvblob;
 
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 
 public class TVBlobService {
 
     private List<String> dvb_ids;
     private String name;
     private String logo;
     private Integer lcn;
     private String timezone;
     private String twitter;
     private String type;
     private String slug;
    private Integer hd;
     
     public List<String> getDvb_ids() {
         return dvb_ids;
     }
     public void setDvb_ids(List<String> dvb_ids) {
         this.dvb_ids = dvb_ids;
     }
     public String getName() {
         return name;
     }
     public void setName(String name) {
         this.name = name;
     }
     public String getLogo() {
         return logo;
     }
     public void setLogo(String logo) {
         this.logo = logo;
     }
     public Integer getLcn() {
         return lcn;
     }
     public void setLcn(Integer lcn) {
         this.lcn = lcn;
     }
     public String getTimezone() {
         return timezone;
     }
     public void setTimezone(String timezone) {
         this.timezone = timezone;
     }
     public String getTwitter() {
         return twitter;
     }
     public void setTwitter(String twitter) {
         this.twitter = twitter;
     }
     public String getType() {
         return type;
     }
     public void setType(String type) {
         this.type = type;
     }
     public String getSlug() {
         return slug;
     }
     public void setSlug(String slug) {
         Preconditions.checkNotNull(slug);
         this.slug = slug;
     }
    public Integer getHd() {
         return hd;
     }
    public void setHd(Integer hd) {
         this.hd = hd;
     }
     
     @Override
     public String toString() {
         return "TVBlob service "+name+" with slug: "+slug;
     }
     
     @Override
     public int hashCode() {
         return slug.hashCode();
     }
     
     @Override
     public boolean equals(Object obj) {
     	if (obj instanceof TVBlobService) {
     		return slug.equals(((TVBlobService) obj).slug);
     	}
     	return false;
     }
 }
