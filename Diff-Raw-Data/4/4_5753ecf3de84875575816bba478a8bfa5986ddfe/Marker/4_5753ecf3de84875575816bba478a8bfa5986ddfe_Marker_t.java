 package com.comsysto.insight.model.options;
 
 
 import java.net.URI;
 
 public class Marker {
 
     private Boolean mEnabled;
     private String mFillColor;
     private String mLineColor;
     private Integer mLineWidth;
     private Integer mRadius;
     private String mSymbol;
 
     public Boolean getEnabled() {
         return mEnabled;
     }
 
    public Marker setEnabled(Boolean pEnabled) {
         mEnabled = pEnabled;
        return this;
     }
 
     public String getFillColor() {
         return mFillColor;
     }
 
     public Marker setFillColor(String pFillColor) {
         mFillColor = pFillColor;
         return this;
     }
 
     public String getLineColor() {
         return mLineColor;
     }
 
     public Marker setLineColor(String pLineColor) {
         mLineColor = pLineColor;
         return this;
     }
 
     public Integer getLineWidth() {
         return mLineWidth;
     }
 
     public Marker setLineWidth(Integer pLineWidth) {
         mLineWidth = pLineWidth;
         return this;
     }
 
     public Integer getRadius() {
         return mRadius;
     }
 
     public Marker setRadius(Integer pRadius) {
         mRadius = pRadius;
         return this;
     }
 
     public String getSymbol() {
         return mSymbol;
     }
 
     public Marker setSymbol(String pSymbol) {
         mSymbol = pSymbol;
         return this;
     }
 
     public Marker setSymbol(URI pURLToSymbol) {
         mSymbol = "url(" + pURLToSymbol.getPath() + ")";
         return this;
     }
 
     public Marker setSymbol(Symbol pSymbol) {
         mSymbol = pSymbol.toString();
         return this;
     }
 
 
 }
