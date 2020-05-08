 /**
  * 
  */
 package pl.edu.pw.rso2012.a1.dvcs.model.workingcopy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import pl.edu.pw.rso2012.a1.dvcs.model.changedata.ChangeData;
 import pl.edu.pw.rso2012.a1.dvcs.model.filesystem.FileSystem;
 import pl.edu.pw.rso2012.a1.dvcs.model.newdata.NewData;
 import pl.edu.pw.rso2012.a1.dvcs.model.snapshot.SnapShot;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 import difflib.Delta;
 import difflib.Patch;
 import difflib.PatchFailedException;
 
 /**
  * @author 
  *
  */
 public class WorkingCopy extends FileSystem {
 
 	/**
 	 * 
 	 */
 	private final String snapshot_dir = ".snapshot";
 	private final String metadata_filename = ".metadata.xml";
 	
 	private SnapShot snapshot = new SnapShot(this.getAddress(), this.getRoot() + File.separatorChar + this.snapshot_dir);
 	
 	/**
 	 * Constructor
 	 * address - repository email address
 	 * root	   - repository location on filesystem
 	 */
 	
 	public WorkingCopy(final String address, final String root) {
 		super(address, root);
 
 		File f = new File(root+ File.separatorChar + this.metadata_filename);
 		if(f.exists())
 			this.setFilelist(this.readMetadata(this.getRoot()));
 	}
 	
 	public SnapShot getSnapshot(){
 		return this.snapshot;
 	}
 	
 	public void storeMetadata(final String pathname){
 		XStream xs = new XStream();
 
 		try {
 			FileOutputStream fs = new FileOutputStream(pathname + File.separatorChar + this.metadata_filename);
 			xs.toXML(this.getFilelist(), fs);
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public Map<String,Map<String,Integer>> readMetadata(final String pathname){
 		 XStream xs = new XStream(new DomDriver());
 		 Map<String,Map<String,Integer>> map = null;
 		 
 		 
 	     try {
 	    	 FileInputStream fs = new FileInputStream(pathname + File.separatorChar + this.metadata_filename);
 	         
 	    	 map = (Map<String, Map<String, Integer>>)xs.fromXML(fs); 
 
 	     } catch (FileNotFoundException ex) {
 	         ex.printStackTrace();
          }
 	     
 	return map;     
 	}
 	
 	/**
 	 * Method for creating snapshot from working directory
 	 * Note: Completely overwrites existing snapshot and versioning metadata
 	 */
 	
 	public void createSnapshot(){
 		this.snapshot = new SnapShot(this.getAddress(),this.getRoot() + File.separatorChar + this.snapshot_dir);
 		this.pRecursiveDelete(this.getRoot() + File.separatorChar + this.snapshot_dir);
 		this.clearFilelist();
 
 		for(String str : this.dirRecursiveRel(this.getRoot())){
 			this.pCreateFile(this.getSnapshot().getRoot() + File.separatorChar + str);
 			this.pCopyFile(this.getRoot() + File.separatorChar + str,this.getSnapshot().getRoot() + File.separatorChar + str);
 			this.addFile(str);
 			this.getFilelist().get(str).put(this.getAddress(),this.getFilelist().get(str).get(this.getAddress()) + 1);
 		}
 		
 		this.storeMetadata(this.getRoot());
 	}
 	
 	/**
 	 * Method for checkout/clone operations
 	 * Note: Works on full set of files, overwrites existing snapshot and versioning metadata
 	 */
 	
 	public void recoverFiles(final List<ChangeData> cd){
 		List<String> ls = new LinkedList<String>();
 		
 		this.clearFilelist();
 		this.dirRecursiveRel(this.getSnapshot().getRoot());
 		
 		for(int i=0;i<cd.size();++i){
 			this.pDeleteFile(this.getRoot() + File.separatorChar + cd.get(i).getFilename());
 			this.pCreateFile(this.getRoot() + File.separatorChar + cd.get(i).getFilename());
 			
 			for(Patch patch: cd.get(i).getDifflist()){
 				try {
 					ls=this.snapshot.applyPatch(ls, patch);
 				} catch (PatchFailedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			
 			}
 			
 			this.getFilelist().put(cd.get(i).getFilename(), cd.get(i).getLclock());	
 			if(!this.getFilelist().get(cd.get(i).getFilename()).containsKey(this.getAddress()))
 				this.getFilelist().get(cd.get(i).getFilename()).put(this.getAddress(), 1);
 			
 			this.writeFile(this.getRoot() + File.separatorChar + cd.get(i).getFilename(), ls);
 			this.pCopyFile(this.getRoot() + File.separatorChar + cd.get(i).getFilename(), this.getSnapshot().getRoot() + File.separatorChar + cd.get(i).getFilename());
 			ls.clear();
 		}
 		this.storeMetadata(this.getRoot());
 
 	}
 	
 	/**
 	 * Method for commit operation
 	 */
 	
 	public Map<String,ChangeData> diffFiles(final List<String> fl){
 		
 		List<String> working = new LinkedList<String>();
 		List<String> snap = new LinkedList<String>();
 		Map<String,ChangeData> cm = new HashMap<String,ChangeData>();
 		List<String> files= fl;
 		
 		if(fl == null || fl.isEmpty())
 			files=this.getFileNames();
 		
 		for (String str : files){
 		File file = new File(this.getRoot()+ File.separatorChar +str);
 		
 		if(this.getFileNames().contains(str) && file.exists()){
 			File f = new File(this.getSnapshot().getRoot() + File.separatorChar +str);
 			if(!f.exists())
 				this.pCreateFile(this.getSnapshot().getRoot() + File.separatorChar +str);
 		
 		cm.put(str, new ChangeData(str));	
 			
 		working = this.readFile(this.getRoot()+ File.separatorChar +str);
 		snap = this.readFile(this.getSnapshot().getRoot() + File.separatorChar +str);
 		
 		cm.get(str).getDifflist().add(this.getSnapshot().getDiff(snap, working));
 		
 		if(this.getFilelist().get(str).get(this.getAddress()) == 0)
 			this.getFilelist().get(str).put(this.getAddress(),1);
 		else if(!cm.get(str).getDifflist().get(0).getDeltas().isEmpty())
 			this.getFilelist().get(str).put(this.getAddress(), this.getFilelist().get(str).get(this.getAddress()) + 1);
 		
 		cm.get(str).getLclock().putAll(this.getFilelist().get(str));
 			
 		
 		this.pCopyFile(this.getRoot()+ File.separatorChar +str, this.getSnapshot().getRoot() + File.separatorChar +str);
 		
 		working.clear();
 		snap.clear();
 			}
 		}
 		
 		for(String str : this.getFileNames()){
 			if(!cm.containsKey(str)){
 				cm.put(str, new ChangeData(str));
 				cm.get(str).getLclock().putAll(this.getFilelist().get(str));
 			}
 		}
 			
 		for(String str : this.dirRecursiveRel(this.getSnapshot().getRoot())){
 			
 			if(!this.getFileNames().contains(str)){
 //				this.pDeleteFile(this.getRoot()+ File.separatorChar +str);
 				this.pDeleteFile(this.getSnapshot().getRoot()+ File.separatorChar +str);
 			}
 		}
 	this.storeMetadata(this.getRoot());	
 		
 	return cm;
 		
 	}
 	
 	/**
 	 * Method for push operation
 	 * Note: Operates on snapshot (commited data)
 	 */
 	
 	public Map<String, NewData> getSnapshotFiles (final List<String> files){
 		Map<String,NewData> w = new HashMap<String,NewData>();
 		List<String> l = new LinkedList<String>();
 		String s="";
 		
 		for (String str : files){
 			w.put(str, new NewData(str));
 			l=this.readFile(this.getSnapshot().getRoot() + File.separatorChar +str);
 			
 			for (String str2 : l)
 				s=s+str2+"\n";
 			
 			w.get(str).setFileContent(s);
 			w.get(str).getLclock().putAll(this.getFilelist().get(str));
 			l.clear();
 		}
 				
 		return w;
 	} 
 	
 	/**
 	 * Method for pull operation
 	 * Note: Pulls into working directory
 	 */
 	
 	public void setWorkingFiles (final Map<String, NewData> mp){
 		List<String> nf = new LinkedList<String>();
 		String s ="";
 		
 		if(mp != null && !mp.isEmpty()){
 		
 			for ( String str : mp.keySet()){
 //				this.getFilelist().remove(str);
 //				this.getFilelist().put(str, mp.get(str).getLclock());
 			
 			
 				if (this.getFilelist().get(str) == null)
 					addFile(str);
 				this.getFilelist().get(str).putAll(mp.get(str).getLclock());
 
 				BufferedReader reader = new BufferedReader(new StringReader(mp.get(str).getFileContent()));
 					try {
 						while ((s = reader.readLine()) != null)
 							nf.add(s);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					this.writeFile(this.getRoot()+ File.separatorChar +str, nf);
 					nf.clear();
 			}
 		}
 		
		this.storeMetadata(this.getRoot());	
 	}
 	
 	//TODO Dodane przez OL
 	//metoda powinna scalic skonfliktowane pliki zaznaczajac
 	//w tresci wynikowego pliku miejsca konfliktow
 	public void mergeConflictedFiles(final Map<String, NewData> conflictedFiles)
 	{
 		List<String> working = new LinkedList<String>();
 		List<String> conflicted = new LinkedList<String>();
 		String s="";
 		Patch diff = new Patch();
 		
 		for(String str:conflictedFiles.keySet()){
 		
 			BufferedReader reader = new BufferedReader(new StringReader(conflictedFiles.get(str).getFileContent()));
 			try {
 				while ((s = reader.readLine()) != null)
 					conflicted.add(s);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		File f = new File(this.getRoot() + File.separatorChar +str);
 		if(!f.exists()){
 			this.pCreateFile(this.getRoot() + File.separatorChar +str);	
 			working=conflicted;
 		}
 		else{
 		working = this.readFile(this.getRoot()+ File.separatorChar +str);
 		
 		diff=this.getSnapshot().getDiff(working, conflicted);
 		
 		while(working.size()<conflicted.size())
 			working.add("");
 		
 		for(Delta delta:diff.getDeltas()){	
 		
 			for(int i=delta.getRevised().getPosition(), j=0;j<delta.getRevised().getLines().size();++i,++j){
 				if(working.get(i)!="")
 					working.set(i, working.get(i)+" <<<O==merge==R>>> "+delta.getRevised().getLines().get(j));
 				else 
 					working.set(i, ""+delta.getRevised().getLines().get(j));
 				}
 			}
 		}
 		this.getFilelist().get(str).putAll(conflictedFiles.get(str).getLclock());
 		
 		this.writeFile(this.getRoot()+ File.separatorChar +str, working);
 		
 		working.clear();
 		conflicted.clear();
 		}

		this.storeMetadata(this.getRoot());	
 	}
 	
 	
 }
