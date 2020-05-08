 package mod.industrialscience;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import mod.industrialscience.modules.research.backend.model.Research;
 
 public class ResearchManager {
 private static ResearchManager instance=null;
 private Hashtable<String,Research> allResearches=null;
 private Hashtable<String,Research> activatedResearches=null;
 private boolean enabled=true;
 private ResearchManager(){
 	allResearches=new Hashtable<String, Research>();
 }
 public void loadResearches(){
 	activatedResearches= new ArrayList<Research>();
 	if(allResearches!=null)
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
 	}
 }
 public void disable(){
 	enabled=false;
 }
public ArrayList<Research> getActivatedResearches() {
 	return activatedResearches;
 }
public ArrayList<Research> getAllResearches() {
 	return allResearches;
 }
 }
