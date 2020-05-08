 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest;
 
 import com.mahn42.framework.BlockPosition;
 import java.util.ArrayList;
 
 /**
  *
  * @author andre
  */
 public class QuestWorldAllocator extends ArrayList<QuestWorldAllocation> {
     
     public void cleanUp() {
         synchronized(this) {
             int lIndex = size() - 1;
             while (lIndex >= 0 && get(lIndex).usedFrom == null) {
                 remove(lIndex);
                 lIndex--;
             }
         }        
     }
     
     public QuestWorldAllocation alloc(BuildingQuest aUsage) {
         QuestWorldAllocation lResult = null;
         synchronized(this) {
             for(QuestWorldAllocation lA : this) {
                 if (lA.usedFrom == null && lA.width >= (aUsage.quest.width + 5)) {
                     lA.usedFrom = aUsage;
                     lResult = lA;
                     break;
                 }
             }
             if (lResult == null) {
                 BlockPosition lPos;
                 if (size() > 0) {
                     QuestWorldAllocation lA = get(size() - 1);
                     lPos = lA.pos.clone();
                     lPos.add(5,0,0);
                 } else {
                     lPos = new BlockPosition();
                 }
                 QuestWorldAllocation lA = new QuestWorldAllocation();
                 lA.pos = lPos;
                 lA.usedFrom = aUsage;
                 lA.width = aUsage.quest.width;
                 add(lA);
             }
         }
        //TODO cleanup region?
         return lResult;
     }
     
     public void free(BuildingQuest aUsage) {
         synchronized(this) {
             for(QuestWorldAllocation lA : this) {
                 if (lA.usedFrom == aUsage) {
                     lA.usedFrom = null;
                     break;
                 }
             }
         }        
     }
 }
