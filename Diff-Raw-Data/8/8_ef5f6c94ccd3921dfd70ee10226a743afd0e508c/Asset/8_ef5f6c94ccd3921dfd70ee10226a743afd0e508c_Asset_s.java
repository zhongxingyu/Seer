 package com.thoughtworks.services;
 
 import org.springframework.stereotype.Service;
 
 public class Asset {
 
      private String asset_type;
       public String getAssetType()
       {
            return asset_type;
       }
      public void setAssetType(String assetType)
      {
              this.asset_type=assetType;
      }
 
 }
