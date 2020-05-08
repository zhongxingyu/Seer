 package com.strollimo.android.controller;
 
 import android.content.Context;
 import com.novoda.imageloader.core.ImageManager;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.model.Mystery;
 import com.strollimo.android.model.Secret;
 
 import java.util.*;
 
 public class PlacesController {
     private final StrollimoPreferences mPrefs;
     private final ImageManager mImageManager;
     private Map<String, Mystery> mMysteries;
     private Context mContext;
     private LinkedHashMap<String, Secret> mSecrets = new LinkedHashMap<String, Secret>();
 
     public PlacesController(Context context) {
         mContext = context;
         mMysteries = new HashMap<String, Mystery>();
         mPrefs = StrollimoApplication.getService(StrollimoPreferences.class);
         mImageManager = StrollimoApplication.getService(ImageManager.class);
         preloadPlaces();
     }
 
     public void preloadPlaces() {
         List<Mystery> mysteries = mPrefs.getMysteries();
         if (mysteries != null) {
             for (Mystery mystery : mysteries) {
                 addMystery(mystery);
                 Mystery myMystery = getMysteryById(mystery.getId());
                 for (String secretId : mystery.getChildren()) {
                     Secret secret = mPrefs.getSecret(secretId);
                     myMystery.addChild(secretId);
                     addSecret(secret, myMystery);
                 }
             }
         }
         preloadImages(new ArrayList<Mystery>(mMysteries.values()));
     }
 
     public Mystery getFirstMystery() {
         if (mMysteries.size() > 0) {
             return mMysteries.values().iterator().next();
         } else {
             return null;
         }
     }
 
     private void preloadImages(final List<Mystery> mysteries) {
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 for (Mystery mystery : mysteries) {
                    mImageManager.cacheImage(mystery.getImgUrl(), 800, 600);
                 }
             }
         }).start();
     }
 
     public void start() {
 
     }
 
     public void addMystery(Mystery mystery) {
         mMysteries.put(mystery.getId(), mystery);
     }
 
     public void addSecret(Secret secret, Mystery mystery) {
         mSecrets.put(secret.getId(), secret);
         mystery.addChild(secret.getId());
     }
 
     public Secret getSecretById(String id) {
         return mSecrets.get(id);
     }
 
     public Mystery getMysteryById(String id) {
         return mMysteries.get(id);
     }
 
     public Map<String, Secret> getAllSecrets() {
         return mSecrets;
     }
 
     public List<Mystery> getAllMysteries() {
         return new ArrayList<Mystery>(mMysteries.values());
     }
 
     public int getMysteriesCount() {
         return mMysteries.size();
     }
 
     public void saveAllData() {
         mPrefs.saveMissions(getAllMysteries(), getAllSecrets());
     }
 
     public void loadDemoData() {
         for (Mystery mystery : mPrefs.getHardcodedMysteries()) {
             addMystery(mystery);
         }
     }
 }
