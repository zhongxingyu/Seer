 package com.amee.domain;
 
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.item.BaseItemValue;
 import com.amee.domain.item.profile.NuProfileItem;
 import com.amee.domain.profile.Profile;
 import com.amee.platform.science.StartEndDate;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 public interface IProfileItemService extends IItemService {
 
     @Override
     public NuProfileItem getItemByUid(String uid);
 
     public boolean hasNonZeroPerTimeValues(NuProfileItem profileItem);
 
     public boolean isSingleFlight(NuProfileItem profileItem);
 
     public int getProfileItemCount(Profile profile, DataCategory dataCategory);
 
     public List<NuProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, Date profileDate);
 
     public List<NuProfileItem> getProfileItems(Profile profile, DataCategory dataCategory, StartEndDate startDate, StartEndDate endDate);
 
    public boolean equivalentProfileItemExists(NuProfileItem profileItem);
 
     public Collection<Long> getProfileDataCategoryIds(Profile profile);
 
     public void persist(NuProfileItem profileItem);
 
     public void persist(BaseItemValue itemValue);
 }
