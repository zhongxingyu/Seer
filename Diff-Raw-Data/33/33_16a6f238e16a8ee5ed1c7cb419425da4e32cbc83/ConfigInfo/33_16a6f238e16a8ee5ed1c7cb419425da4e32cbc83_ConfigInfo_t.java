 package confdb.data;
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 /**
  * ConfigInfo
  * ----------
  * @author Philipp Schieferdecker
  *
  * Information about a configuration.
  */
 public class ConfigInfo implements Comparable<ConfigInfo>
 {
     //
     // member data
     //
     
     /** name of the configuration */
     private String name = null;
 
     /** parent directory */
     private Directory parentDir = null;
     
     /** current release tag */
     private String releaseTag = null;
     
     /** versions of the configuration */
     private ArrayList<ConfigVersion> versions = new ArrayList<ConfigVersion>();
     
     /** selected version index */
     private int versionIndex = -1;
 
     /** user who is currently locking this configuration */
     private String lockedByUser = "";
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public ConfigInfo(String name,Directory parentDir,int dbId,int version,
 		      String created,String creator,
 		      String releaseTag,String processName,
 		      String comment)
     {
 	this.name         = name;
 	this.parentDir    = parentDir;
 	this.releaseTag   = releaseTag;
 	this.versionIndex = 0;
 	versions.add(new ConfigVersion(dbId,version,created,creator,
 				       releaseTag,processName,comment));
     }
     
     /** constructor without a version */
     public ConfigInfo(String name,Directory parentDir,String releaseTag)
     {
 	this.name         = name;
 	this.parentDir    = parentDir;
 	this.releaseTag   = releaseTag;
 	this.versionIndex = -1;
     }
 
 
     //
     // member functions
     //
     
     /** toString overload */
     public String toString() { return name; }
     
    /** full qualifier */
    public String fullName() { return parentDir().name()+"/"+name()+"/V"+version(); }
    
     /** Comparable::compareTo() */
     public int compareTo(ConfigInfo ci)
     {
 	String full1 = parentDir().name()+"/"+name();
 	String full2 = ci.parentDir().name()+"/"+ci.name();
 	return full1.compareTo(full2);
     }
 
     /** get configuration name */
     public String name() { return name; }
 
     /** get the parent directory */
     public Directory parentDir() { return parentDir; }
     
     /** get database id */
     public int dbId()
     {
 	return (versionIndex<0) ? -1 : versions.get(versionIndex).dbId();
     }
     
     /** get latest configuration version */
     public int version()
     {
 	return (versionIndex<0) ? 0 : versions.get(versionIndex).version();
     }
     
     /** get latest configuration creation date */
     public String created()
     {
 	return (versionIndex<0) ? "" : versions.get(versionIndex).created();
     }
     
     /** get latest configuration creator */
     public String creator()
     {
 	return (versionIndex<0) ? "" : versions.get(versionIndex).creator();
     }
     
     /** get the current releaseTag */
     public String releaseTag() { return releaseTag; }
 
     /** get the current process name */
     public String processName()
     {
 	return (versionIndex<0) ? "" : versions.get(versionIndex).processName();
     }
     
     /** get the comment for the current version */
     public String comment()
     {
 	return (versionIndex<0) ? "" : versions.get(versionIndex).comment();
     }
 
     /** number of versions */
     public int versionCount() { return versions.size(); }
     
     /** get the i-th version */
     public ConfigVersion version(int i) { return versions.get(i); }
     
     /** number of versions with specified releaseTag */
     public int versionCount(String releaseTag)
     {
 	int result = 0;
 	Iterator<ConfigVersion> itCV = versions.iterator();
 	while (itCV.hasNext()) {
 	    ConfigVersion version = itCV.next();
 	    if (releaseTag.equals("")||
 		releaseTag.equals(version.releaseTag())) result++;
 	}
 	return result;
     }
     
     /** get the i-th version */
     public ConfigVersion version(String releaseTag, int i)
     {
 	if (releaseTag.equals("")) return versions.get(i);
 	int index = 0;
 	Iterator<ConfigVersion> itCV = versions.iterator();
 	while (itCV.hasNext()) {
 	    ConfigVersion version = itCV.next();
 	    if (releaseTag.equals(version.releaseTag())) {
 		if (index==i) return version;
 		index++;
 	    }
 	}
 	return null;
     }
     
     /** check if configuration and all versions are locked */
     public boolean isLocked() { return (lockedByUser.length()>0); }
     
     /** user who currently locks this configuration and all versions */
     public String lockedByUser() { return lockedByUser; }
     
     /** lock this configuration and all its versions */
     public boolean lock(String userName)
     {
 	if (isLocked()||userName.equals(new String())) return false;
 	lockedByUser = userName;
 	return true;
     }
     
     /** unlock this configuration and all its versions */
     public void unlock() { this.lockedByUser = ""; }
 
     /** add an new version of this configuration */
     public void addVersion(int dbId,int version,
 			   String created,String creator,
 			   String releaseTag,String processName,
 			   String comment)
     {
 	for (ConfigVersion v : versions) {
 	    if (v.version()==version) {
 		System.err.println("addVersion ERROR: version exists already.");
 		return;
 	    }
 	}
 	
 	ConfigVersion configVersion = new ConfigVersion(dbId,
 							version,
 							created,
 							creator,
 							releaseTag,
 							processName,
 							comment);
 	versions.add(configVersion);
 	Collections.<ConfigVersion>sort(versions);
 	this.releaseTag = releaseTag;
     }
     
     /** the the index of the selected version */
     public void setVersionIndex(int index)
     {
 	versionIndex = index;
 	releaseTag   = versions.get(versionIndex).releaseTag();
     }
     
     /** the the index of the selected version w.r.t. to specified release tag */
     public void setVersionIndex(String releaseTag, int index)
     {
 	if (releaseTag.equals("")) { versionIndex = index; }
 	else {
 	    int i = -1; int j = 0;
 	    while (j<versions.size()&&i<index) {
 		if (versions.get(j).releaseTag().equals(releaseTag)) i++;
 		j++;
 	    }
	    versionIndex = j-1;
 	}
 	this.releaseTag = versions.get(versionIndex).releaseTag();
     }
     
     /** move to the next available version number */
     public int nextVersion()
     {
 	return (versionCount()>0) ? versions.get(0).version()+1 : 1;
     }
     
     /** set the parent directory */
     public void setParentDir(Directory parentDir){this.parentDir=parentDir;}
 
     /** set the release tag */
     public void setReleaseTag(String releaseTag){this.releaseTag=releaseTag;}
 }
