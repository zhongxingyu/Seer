 package ch.bli.mez.controller;
 
 import ch.bli.mez.model.Mission;
 import ch.bli.mez.model.dao.MissionDAO;
 import ch.bli.mez.view.MissionListEntry;
 import ch.bli.mez.view.MissionPanel;
 
 /**
  * @author Michael Brodmann
  * @version 1.0
  */
 
 public class MissionController {
   private MissionPanel view;
   private MissionDAO model;
 
   public MissionController(){
     this.view = new MissionPanel();
     this.model = new MissionDAO();
     addActionListeners();
    //addMissionEntrys();
   }
   
   public MissionPanel getView(){
     return view;
   }
   
   private void addActionListeners(){
     
   }
   
   private void addMissionEntrys(){
     for (Mission mission : model.findAll()) {
       view.addMissionListEntry(new MissionListEntry(mission));
      }
   }
 }
