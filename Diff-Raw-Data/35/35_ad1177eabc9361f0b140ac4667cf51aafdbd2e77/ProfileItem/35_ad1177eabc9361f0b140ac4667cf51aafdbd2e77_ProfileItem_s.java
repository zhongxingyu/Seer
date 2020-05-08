 package gc.carbon.domain.profile;
 
 import gc.carbon.builder.Builder;
 import gc.carbon.builder.domain.BuildableProfileItem;
 import gc.carbon.builder.domain.current.ProfileItemBuilder;
 import gc.carbon.domain.EngineUtils;
 import gc.carbon.domain.ObjectType;
 import gc.carbon.domain.data.DataCategory;
 import gc.carbon.domain.data.DataItem;
 import gc.carbon.domain.data.Item;
 import org.hibernate.annotations.Index;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.persistence.*;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 
 @Entity
 @DiscriminatorValue("PI")
 public class ProfileItem extends Item implements BuildableProfileItem {
 
     // 999,999,999,999,999,999.999
     public final static int PRECISION = 18;
     public final static int SCALE = 3;
     public final static RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
     public final static BigDecimal ZERO = BigDecimal.valueOf(0, SCALE);
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "PROFILE_ID")
     private Profile profile;
 
     @ManyToOne(fetch = FetchType.LAZY, optional = true)
     @JoinColumn(name = "DATA_ITEM_ID")
     private DataItem dataItem;
 
     @Column(name = "END")
     private Boolean end = false;
 
     @Column(name = "AMOUNT", precision = PRECISION, scale = SCALE)
     private BigDecimal amount = ZERO;
 
     @Column(name = "UNIT")
     private String unit;
 
     @Column(name = "PER_UNIT")
     private String perUnit;
 
     @Transient
     private Builder builder;
 
     public ProfileItem() {
         super();
         setBuilder(new ProfileItemBuilder(this));
     }
 
     public ProfileItem(Profile profile, DataItem dataItem) {
         super(dataItem.getDataCategory(), dataItem.getItemDefinition());
         setProfile(profile);
         setDataItem(dataItem);
         setBuilder(new ProfileItemBuilder(this));
     }
 
     public ProfileItem(Profile profile, DataCategory dataCategory, DataItem dataItem) {
         super(dataCategory, dataItem.getItemDefinition());
         setProfile(profile);
         setDataItem(dataItem);
         setBuilder(new ProfileItemBuilder(this));
     }
 

     public void setBuilder(Builder builder) {
         this.builder = builder;
     }
 
     public String toString() {
         return "ProfileItem_" + getUid();
     }
 
     @Transient
     public ProfileItem getCopy() {
         ProfileItem profileItem = new ProfileItem(getProfile(), getDataCategory(), getDataItem());
         profileItem.setStartDate(getStartDate());
         profileItem.setEndDate(getEndDate());
         profileItem.setEnd(isEnd());
         profileItem.setAmount(getAmount());
         profileItem.setName(getName());
         profileItem.setCreated(getCreated());
         profileItem.setModified(getModified());
         profileItem.setUid(getUid());
         profileItem.setId(getId());
         return profileItem;
     }
 
     @Transient
     public void updateAmount(BigDecimal newAmount) {
         setAmount(newAmount);
     }
 
     @Transient
     public void addToAmount(BigDecimal difference) {
         updateAmount(getAmount().add(difference));
     }
 
     @Transient
     public String getPath() {
         return getUid();
     }
 
     @Transient
     public String getDisplayPath() {
         return EngineUtils.getDisplayPath(this);
     }
 
     @Transient
     public String getDisplayName() {
         return EngineUtils.getDisplayName(this);
     }
 
     public Profile getProfile() {
         return profile;
     }
 
     public void setProfile(Profile profile) {
         this.profile = profile;
     }
 
     public DataItem getDataItem() {
         return dataItem;
     }
 
     public void setDataItem(DataItem dataItem) {
         if (dataItem != null) {
             this.dataItem = dataItem;
         }
     }
 
     public boolean isEnd() {
         return end;
     }
 
     public void setEnd(boolean end) {
         this.end = end;
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public void setAmount(BigDecimal amount) {
         if (amount == null) {
             amount = ZERO;
         }
         this.amount = amount;
     }
 
     public JSONObject getJSONObject(boolean b) throws JSONException {
         return builder.getJSONObject(b);
     }
 
     public Element getElement(Document document, boolean b) {
         return builder.getElement(document, b);
     }
 
     public String getUnit() {
         return unit;
     }
 
     public void setUnit(String unit) {
         this.unit = unit;
     }
 
     public String getPerUnit() {
         return perUnit;
     }
 
     public void setPerUnit(String perUnit) {
         this.perUnit = perUnit;
     }
 
     @Transient
     public String getType() {
         return ObjectType.PI.toString();
     }
 
     @Transient
     public ObjectType getObjectType() {
         return ObjectType.PI;
     }
 }
