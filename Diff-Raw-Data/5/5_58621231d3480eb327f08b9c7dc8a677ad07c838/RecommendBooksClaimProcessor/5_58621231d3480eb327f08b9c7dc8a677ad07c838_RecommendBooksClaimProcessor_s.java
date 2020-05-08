 package com.tridion.smartatget.samples.recommendbooks;
 
 import com.tridion.ambientdata.AmbientDataException;
 import com.tridion.ambientdata.claimstore.ClaimStore;
 import com.tridion.ambientdata.processing.AbstractClaimProcessor;
 import java.net.URI;
 import java.util.*;
 
 
 public class RecommendBooksClaimProcessor extends AbstractClaimProcessor {
 
     private final static URI RECOMMENDED_CATEGORY_URI = URI.create("com:tridion:smarttarget:samples:recommendbooks:category");
 
     @Override
     public void onSessionStart(ClaimStore claimStore) throws AmbientDataException {
         String category = new YourBusinessModel().getMostLookedAtCategory();
         claimStore.put(RECOMMENDED_CATEGORY_URI, category, true);
     }
 

    // dummy implementation of a bussiness model that instead of getting the most lookedat category, gets a random one.
    class YourBusinessModel {
         private List<String> categories = new ArrayList<String>(Arrays.asList("Science Fiction","Fantasy", "Fiction", "Thriller", "Romance"));
 
         private String getMostLookedAtCategory() {
             int random = new Random().nextInt(categories.size());
             return categories.get(random);

         }
     }

 }
 
 
 
