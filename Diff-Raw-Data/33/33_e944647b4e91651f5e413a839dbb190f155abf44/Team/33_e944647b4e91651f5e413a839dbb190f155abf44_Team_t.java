 package net.codjo.tools.pyp.model;
 /**
  *
  */
 public enum Team {
    wam("WAM"),
    focs("FOCS"),
     gacpa("GACPA"),
     transverse("TRANSVERSE");
 
     private String team;
 
 
     private Team(String team) {
         this.team = team;
     }
 
 
     public String getTeam() {
         return team;
     }
 }
