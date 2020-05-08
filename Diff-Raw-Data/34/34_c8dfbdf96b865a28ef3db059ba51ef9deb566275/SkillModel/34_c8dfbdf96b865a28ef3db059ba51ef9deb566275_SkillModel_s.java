 /*
  * Skill Model: Contains information about an specific skill under the tree
  *
  * Author: Santiago Torres, Miguel Kelly
  * Date: 29 apr 2013
  */
 import java.lang.*;
 
 public class SkillModel{
 	private String name;
 	private SkillModel[] parent;
 	private SkillModel[] sons;
 	private String[] details;
 	private int level;
 	private int maxLevel;
 	private String[] milestones;
 	private int[] achieved;
 	private String[] requirements;
 	private boolean isActive;
 	//empty constructor, avoid tthis one, use the one with arguments
 	public SkillModel(){
 		String emptyDetails[] = {"empty"};
 		SkillModel[] emptySkills = new SkillModel[1];
 		emptySkills[0]=this;
 		System.out.println("Skills are not meant to be created with an empty constructor");
 		this.name = new String("Empty");
 		this.parent = emptySkills;
 		this.sons = emptySkills;
 		this.details = emptyDetails;
 		this.level=0;
 		this.maxLevel=10;
 		this.milestones=emptyDetails;
 		this.achieved = new int[1];
 		this.requirements = emptyDetails;
 		this.isActive=false;
 	}
 
 	//full constructor it's not validated yet
         public SkillModel(String name, String[] details, SkillModel[] parent, SkillModel[] sons,int maxLevel, String[] milestones,String[] requirements){
 		this.name = name;
 		this.parent = parent;
 		this.sons = sons;
 		this.details = details;
 		this.level=0;
 		this.maxLevel=maxLevel;
 		this.milestones=milestones;
 		this.achieved = new int[milestones.length];
 		this.requirements = requirements;
 		this.isActive=false;
 	}
         
         //setParentLinks: resets link array	
 	public int setParentLinks(SkillModel[] newLinks){
 		this.parent = newLinks;
 		return 0;
 	}
 	
 	public SkillModel[] getParentLinks(){
 		return this.parent;
 	}
     
 	public String getName(){
 		return this.name;
 	}
 
 
 	//simple method for debugging purposes.
 	public String toString(){
 		return this.name +  
			"\n Parent:       " + this.parent + 
 			"\n Sons:         " + this.sons + 
			"\n Details:      " + this.details +
 		       	"\n Level:        " + this.level + "/" + this.maxLevel + 
			"\n Milestones:   " + this.milestones + 
			"\n Achievement:  " + this.achieved + 
			"\n requirements: " + this.requirements + 
 			"\n isActive?     " + this.isActive;
 	}
 
     public String[] getDetails(){
 	return this.details;
     }
 
     public String[] getMilestones(){
 	return this.milestones;
     }
 
     public int[] getAchieved(){
 	return this.achieved;
     }
     
     public int getLevel(){
 	return this.level;
     }
     
     public int getMaxLevel(){
 	return this.maxLevel;
     }
     
     public String[] getRequirements(){
 	return this.requirements;
     }
 
     public SkillModel[] getParents(){
 	return this.parent;
     }
     public SkillModel[] getSons(){
 	return this.sons;
     }
     public void setAchieved(int num){
 	if(this.achieved[num]==0){
 	    this.achieved[num]=1;
 	}else{
 	    this.achieved[num]=0;
 	}
 	for(int i=0;i<this.achieved.length;i++){
 	    System.out.println(this.achieved[i]);
 	}
     }
 }
