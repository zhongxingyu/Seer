 package mod.industrialscience;
 
 import java.util.ArrayList;
 
 import mod.industrialscience.modules.research.backend.model.Research;
 
 public class ResearchManager {
 private static ResearchManager instance=null;
 private ArrayList<Research> allResearches=null;
 private ArrayList<Research> activatedResearches=null;
private boolean enabled=true;
 public void loadResearches(){
	activatedResearches= new ArrayList<Research>();
 	for(Research r : allResearches){
 		if(r.check())
 		activatedResearches.add(r);
 	}
 }
 public static ResearchManager getInstance(){
 	if(instance==null){
 		instance= new ResearchManager();
 	}
 	return instance;
 }
 public void registerResearch(Research r){
 	if(enabled){
		if(allResearches==null)allResearches=new ArrayList<Research>();
 		allResearches.add(r);
 	}
 }
 public void disable(){
 	enabled=false;
 }
 public ArrayList<Research> getActivatedResearches() {
 	return activatedResearches;
 }
 }
