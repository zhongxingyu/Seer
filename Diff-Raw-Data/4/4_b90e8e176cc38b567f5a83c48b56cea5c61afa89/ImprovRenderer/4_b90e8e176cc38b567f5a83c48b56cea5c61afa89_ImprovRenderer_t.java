 package org.bullecarree.improv.model;
 
 
 public class ImprovRenderer {
     private Improv improv;
     private final String compared;
     private final String mixt;
     private final String unlimited;
     private final String categoryFree;
     
     public ImprovRenderer(String compared, String mixt, String unlimited, String categoryFree) {
         this.compared = compared;
         this.mixt = mixt;
         this.unlimited = unlimited;
         this.categoryFree = categoryFree;
     }
     
     public void setImprov(Improv improv) {
         this.improv = improv;
     }
     
     public String getTitle() {
         return improv.getTitle();
     }
     
     public String getType() {
         if (improv.getType() == ImprovType.COMPARED) {
             return this.compared;
         } else {
             return this.mixt;
         }
     }
     
     public String getCategory() {
         String res = improv.getCategory();
         if (res == null || "".equals(res)) {
             res = categoryFree;
         }
         return res;
     }
     
     public String getPlayerCount() {
         if (improv.getPlayerCount() == 0) {
             return this.unlimited;
         } else {
             return String.valueOf(improv.getPlayerCount());
         }
     }
     
     public String getDuration() {
         return displayTime(improv.getDuration());
     }
     
     public String displayTime(int duration) {
         StringBuffer res = new StringBuffer();
         int minutes = duration / 60;
         int seconds = duration % 60;
         
         if (minutes > 0) {
             res.append(minutes).append("m ");
         }
        if (seconds > 0 && duration != 0) {
            res.append(seconds).append("s");
        }
         return res.toString();
     }
     
 }
