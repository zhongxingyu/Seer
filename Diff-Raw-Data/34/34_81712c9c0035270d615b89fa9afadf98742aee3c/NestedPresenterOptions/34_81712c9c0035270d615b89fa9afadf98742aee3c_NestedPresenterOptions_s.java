 /**
  * Copyright 2013 ArcBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.arcbees.plugin.template.domain.presenter;
 
 public class NestedPresenterOptions {
     private boolean revealContentEvent;
     private boolean revealContentRootEvent;
     private boolean revealRootLayoutContentEvent;
     
     private String revealType;
     private String contentSlot;
     private String contentSlotImport;
     
     private boolean isPlace;
     private String nameToken;
     private boolean crawlable;
     private boolean codeSplit;
     private String nameTokensPackageName;
     
     public NestedPresenterOptions() {
     }
 
     public boolean isRevealContentEvent() {
         return revealContentEvent;
     }
 
     public void setRevealContentEvent(boolean revealContentEvent) {
         this.revealContentEvent = revealContentEvent;
     }
 
     public boolean isRevealContentRootEvent() {
         return revealContentRootEvent;
     }
 
     public void setRevealContentRootEvent(boolean revealContentRootEvent) {
         this.revealContentRootEvent = revealContentRootEvent;
     }
 
     public boolean isRevealRootLayoutContentEvent() {
         return revealRootLayoutContentEvent;
     }
 
     public void setRevealRootLayoutContentEvent(boolean revealRootLayoutContentEvent) {
         this.revealRootLayoutContentEvent = revealRootLayoutContentEvent;
     }
 
     public String getContentSlot() {
         return contentSlot;
     }
 
     public void setContentSlot(String contentSlot) {
         this.contentSlot = contentSlot;
     }
 
     public boolean getIsPlace() {
         return isPlace;
     }
 
     public void setPlace(boolean isPlace) {
         this.isPlace = isPlace;
     }
 
     public String getNameToken() {
         return nameToken;
     }
 
     public void setNameToken(String nameToken) {
         this.nameToken = nameToken;
     }
 
     public boolean getCodeSplit() {
         return codeSplit;
     }
 
     public void setCodeSplit(boolean codeSplit) {
         this.codeSplit = codeSplit;
     }
 
     public boolean getCrawlable() {
         return crawlable;
     }
 
     public void setCrawlable(boolean crawlable) {
         this.crawlable = crawlable;
     }
     
     public String getRevealType() {
         return revealType;
     }
 
     public void setRevealType(String revealType) {
         this.revealType = revealType;
     }
 
     public String getContentSlotImport() {
         return contentSlotImport;
     }
 
     public void setContentSlotImport(String contentSlotImport) {
         this.contentSlotImport = contentSlotImport;
     }
 
     public void setNameTokensPackageName(String nameTokensPackageName) {
         this.nameTokensPackageName = nameTokensPackageName;
     }
     
     public String getNameTokensPackageName() {
         return nameTokensPackageName;
     }
 }
