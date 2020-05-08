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
 	activatedResearches= new Hashtable<String, Research>();
 }
 public void loadResearches(){
	Enumeration<Research> e= allResearches.elements();
	while(e.hasMoreElements()){
		Research r = allResearches.elements().nextElement();
 		if(r.check())
 		activatedResearches.put(r.getName(), r);
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
 		allResearches.put(r.getName(), r);
 	}
 }
 public void disable(){
 	enabled=false;
 }
 public Hashtable<String,Research> getActivatedResearches() {
 	return activatedResearches;
 }
 public Hashtable<String,Research> getAllResearches() {
 	return allResearches;
 }
 }
