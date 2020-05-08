 /*
  File: DownloadableInfo.java 
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 package cytoscape.plugin;
 
 import java.net.URL;
 
 import cytoscape.util.URLUtil;
 import java.util.Set;
 import java.util.HashSet;
 
 public abstract class DownloadableInfo {
 	protected String versionMatch = "^\\d+\\.\\d+";
 
 	protected String versionSplit = "\\.";
 
 	private String releaseDate;
 
 	private String uniqueID;
 
 	private String name = "";
 
 	private String description;
 
 	private String objVersion;
 
 	private String cytoscapeVersion;
 
 	private String downloadURL = "";
 
 	private String objURL = "";
 
 	private String category;
 
 	private License license;
 
 	private boolean licenseRequired = false;
 
 	private Set<String> compatibleCyVersions; 
 	
 	private DownloadableInfo parentObj = null;
 
 	public DownloadableInfo() {
 		compatibleCyVersions = new HashSet<String>();
 	}
 
 	public DownloadableInfo(String ID) {
 		this.uniqueID = ID;
 		compatibleCyVersions = new HashSet<String>();
 	}
 
 	public DownloadableInfo(String ID, DownloadableInfo ParentObj) {
 		this.uniqueID = ID;
 		this.parentObj = ParentObj;
 		compatibleCyVersions = new HashSet<String>();
 	}
 
 	/* --- SET --- */
 	
 	/**
 	 * Sets the license information for the plugin. Not required.
 	 * 
 	 * @param java.net.URL
 	 *            object where license can be downloaded from.
 	 */
 	public void setLicense(URL url) {
 		license = new License(url);
 	}
 
 	/**
 	 * Sets the license information for the plugin. Not required.
 	 * 
 	 * @param Text
 	 *            string of license.
 	 * @param alwaysRequired
 	 *            If the user expects the license to be required for both
 	 *            install and update at all times (true) or only at install
 	 *            (false)
 	 */
 	public void setLicense(String licenseText, boolean alwaysRequired) {
 		license = new License(licenseText);
 		licenseRequired = alwaysRequired;
 	}
 	
 	
 	/**
 	 * @param Category
 	 *            Sets the category of the downloadable object.
 	 */
 	public void setCategory(String Category) {
 		this.category = Category;
 	}
 
 	public void setCategory(Category cat) {
 		this.category = cat.toString();
 	}
 
 	public void setParent(DownloadableInfo ParentObj) {
 		this.parentObj = ParentObj;
 	}
 
 	/**
 	 * @param name
 	 *            Sets the name of the downloadable object.
 	 */
 	public void setName(String Name) {
 		this.name = Name;
 	}
 
 	/**
 	 * @param description
 	 *            Sets the descriptoin of the downloadable object.
 	 */
 	public void setDescription(String Description) {
 		this.description = Description;
 	}
 
 	/**
 	 * @param url
 	 *            Sets the URL for the xml file describing all downloadable
 	 *            objects from any given project. (ex.
 	 *            http://cytoscape.org/plugins/plugin.xml)
 	 */
 	public void setDownloadableURL(String url) {
 		this.downloadURL = url;
 	}
 
 	/**
 	 * @param url
 	 *            Set the URL where this object can be downloaded from.
 	 */
 	public void setObjectUrl(String url) {
 		this.objURL = url;
 	}
 
 	/**
 	 * Contains a list of all the Cytoscape versions this object
 	 * is compatible with.
 	 * 
 	 * @param cyVersion
 	 * @throws NumberFormatException
 	 */
 	public void addCytoscapeVersion(String cyVersion) throws NumberFormatException {	
 		if (versionOk(cyVersion, false)) {
 			compatibleCyVersions.add(cyVersion);
 		} else {
 			throw new NumberFormatException(
 					"Cytoscape version numbers must be in the format: \\d+.\\d+  optional to add: .\\d+-[a-z]");
 		}
 	}
 
 	/**
 	 * @param version
 	 *            Sets the version of this object.
 	 */
 	public void setObjectVersion(double objVersion)
 			throws NumberFormatException {
 		String Version = Double.toString(objVersion);
 		if (versionOk(Version, true)) {
 			this.objVersion = Version;
 		} else {
 			throw new NumberFormatException("Bad version '" + Version + "'."
 					+ this
 					+ " version numbers must be in the format: \\d+.\\d+");
 		}
 	}
 
 	/**
 	 * TODO - would probably be better to use a date object
 	 * 
 	 * @param date
 	 *            Sets the release date of this object.
 	 */
 	public void setReleaseDate(String date) {
 		this.releaseDate = date;
 	}
 
 	/* --- GET --- */
 
 	/**
 	 * @return The text of the license for this plugin if available.
 	 */
 	public String getLicenseText() {
 		if (license != null)
 			return license.getLicense();
 		else
 			return null;
 	}
 
 	/**
 	 * @return If the license is always required to be accepted for installs and
 	 *         updates this returns true. If it only is required at install time
 	 *         (never at update) returns false.
 	 */
 	public boolean isLicenseRequired() {
 		return licenseRequired;
 	}
 
 	public abstract Installable getInstallable();
 		
 	/**
 	 * Return the downloadable type of this object.
 	 */
 	public abstract DownloadableType getType();
 
 	/**
 	 * @return The parent DownloadableInfo object for this object if it has one.
 	 */
 	public DownloadableInfo getParent() {
 		return this.parentObj;
 	}
 
 	/**
 	 * @return The URL this object can be downloaded from.
 	 */
 	public String getObjectUrl() {
 		return this.objURL;
 	}
 
 	/**
 	 * @return Category that describes this downloadable object.
 	 */
 	public String getCategory() {
 		return this.category;
 	}
 
 	/**
 	 * @return Url that returns the document of available downloadable objects
 	 *         this object came from. Example
 	 *         http://cytoscape.org/plugins/plugins.xml
 	 */
 	public String getDownloadableURL() {
 		return this.downloadURL;
 	}
 
 	/**
 	 * @return Version of the downloadable object.
 	 */
 	public String getObjectVersion() {
 		return this.objVersion;
 	}
 
 	/**
 	 * @return Name of the downloadable object.
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * @return Description of the downloadable object.
 	 */
 	public String getDescription() {
 		return this.description;
 	}
 
 	/**
 	 * @return Compatible Cytocape version of this object.
 	 */
 	public String getCytoscapeVersion() {
 		String CurrentVersion = null;
 		for (String v: this.compatibleCyVersions) {
 			if (CurrentVersion != null)
 				CurrentVersion = compareCyVersions(v, CurrentVersion);
 			else 
 				CurrentVersion = v;
 		}
 		return CurrentVersion;
 	}
 
 	/**
 	 * @return All compatible Cytoscape versions.
 	 */
 	public java.util.List<String> getCytoscapeVersions() {
 		return new java.util.ArrayList<String>(this.compatibleCyVersions);
 	}
 	
 	public boolean containsVersion(String cyVersion) {
 		return compatibleCyVersions.contains(cyVersion);
 	}
 	
 	/**
 	 * @return Release date for this object.
 	 */
 	public String getReleaseDate() {
 		return this.releaseDate;
 	}
 
 	/**
 	 * @return Unique identifier for the downloadable object.
 	 */
 	public String getID() {
 		return this.uniqueID;
 	}
 
 	/**
 	 * Compare the version of the object to the given object.
 	 * 
 	 * @param New
 	 *            Potentially newer DownloadableInfo object
 	 * @return true if given version is newer
 	 */
 	public boolean isNewerObjectVersion(DownloadableInfo New) {
 		String[] CurrentVersion = this.getObjectVersion().split(versionSplit);
 		String[] NewVersion = New.getObjectVersion().split(versionSplit);
 
 		// make sure it's the same object first
 		if ( !(this.getID().equals(New.getID()) && this.getDownloadableURL().equals(New.getDownloadableURL())) ) {
 			return false;
 		}
 		
 		int CurrentMajor = Integer.valueOf(CurrentVersion[0]).intValue();
 		int NewMajor = Integer.valueOf(NewVersion[0]).intValue();
 
 		int CurrentMinor = Integer.valueOf(CurrentVersion[1]).intValue();
 		int NewMinor = Integer.valueOf(NewVersion[1]).intValue();
 
 		if ((CurrentMajor > NewMajor || (CurrentMajor == NewMajor && CurrentMinor >= NewMinor))) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * @return true if the plugin is compatible with the current Cytoscape
 	 *         version major.minor (bugfix is only checked if the plugin
 	 *         specifies a bugfix version)
 	 */
 	public boolean isCytoscapeVersionCurrent() {
 		String[] CyVersion = new cytoscape.CytoscapeVersion().getFullVersion()
 				.split(versionSplit);
 		String[] PlVersion = getCytoscapeVersion().split(versionSplit);
 
 		for (int i = 0; i < PlVersion.length; i++) {
 			if (Integer.valueOf(CyVersion[i]).intValue() != Integer.valueOf(
 					PlVersion[i]).intValue())
 				return false;
 		}
 		return true;
 	}
 
 	
 	// careful, this overwrites the Object.equals method
 	/**
 	 * Compare the two info objects.  If the ID, downloadable url and object version
 	 * are the same they are considered to be the same object.
 	 */
 	public boolean equals(Object Obj) {
 		DownloadableInfo obj = (DownloadableInfo) Obj;
 
		if (this.getID() != null && obj.getID() != null) {
 			if (this.getID().equals(obj.getID()) &&
 				this.getDownloadableURL().equals(obj.getDownloadableURL()) &&
 				this.getObjectVersion().equals(obj.getObjectVersion()))
 				return true;
 		} else if (this.getDownloadableURL().equals(obj.getDownloadableURL()) &&
 				   this.getObjectVersion().equals(obj.getObjectVersion())) {
 			// should I do this?? Without an id there is no other good way to tell I suppose
 			return true;
 		}
 			
 		return false;
 	}
 
 	public boolean equalsDifferentObjectVersion(Object Obj) {
 		DownloadableInfo obj = (DownloadableInfo) Obj;
 		if (this.getID().equals(obj.getID()) &&
 			this.getDownloadableURL().equals(obj.getDownloadableURL()))
 			return true;
 
 		return false;
 	}
 	
 	
 	/**
 	 * @return Returns String of downloadable name and version
 	 * 		ex. MyPlugin v.1.0
 	 */
 	public String toString() {
 		return getName() + " v." + getObjectVersion();
 	}
 	
 	
 	public abstract String htmlOutput();
 	
 	// yea, it's ugly...styles taken from cytoscape website
 	protected String basicHtmlOutput() {
 		String Html = "<html><style type='text/css'>";
 		Html += "body,th,td,div,p,h1,h2,li,dt,dd ";
 		Html += "{ font-family: Tahoma, \"Gill Sans\", Arial, sans-serif; }";
 		Html += "body { margin: 0px; color: #333333; background-color: #ffffff; }";
 		Html += "#indent { padding-left: 30px; }";
 		Html += "ul {list-style-type: none}";
 		Html += "</style><body>";
 
 		Html += "<b>" + getName() + "</b><p>";
 		Html += "<b>Version:</b>&nbsp;" + getObjectVersion() + "<p>";
 		Html += "<b>Category:</b>&nbsp;" + getCategory() + "<p>";
 		Html += "<b>Description:</b><br>" + getDescription();
 		
 		if (!isCytoscapeVersionCurrent()) {
 			Html += "<br><b>Verified with the following Cytoscape versions:</b> " + getCytoscapeVersions().toString() + "<br>";
 			Html += "<font color='red'><i>" + toString() + " is not verfied to work in the current version (" 
 				+ cytoscape.CytoscapeVersion.version + ") of Cytoscape.</i></font>";
 		}
 		Html += "<p>";
 
 		if (getReleaseDate() != null && getReleaseDate().length() > 0) {
 			Html += "<b>Release Date:</b>&nbsp;" + getReleaseDate() + "<p>";
 		}
 		
 		return Html;
 	}
 
 	/**
 	 * Return the most recent of the two versions.
 	 * @param arg0
 	 * @param arg1
 	 * @return
 	 */
 	String compareCyVersions(String arg0, String arg1) {
 		String MostRecentVersion = null;
 		int max = 3;
 		
 		String[] SplitVersionA = arg0.split(versionSplit);
 		String[] SplitVersionB = arg1.split(versionSplit);
 		
 		for (int i=0; i<max; i++) {
 			int a = 0;
 			int b = 0;
 			
 			if (i == (max-1)) {
 				System.out.println("A length: " + SplitVersionA.length + " B length: " + SplitVersionB.length);
 				a = (SplitVersionA.length == max)? Integer.valueOf(SplitVersionA[i]) : 0;
 				b = (SplitVersionB.length == max)? Integer.valueOf(SplitVersionB[i]) : 0;
 			} else {
 				a = Integer.valueOf(SplitVersionA[i]);
 				b = Integer.valueOf(SplitVersionB[i]);
 			}
 		
 			if (a != b) {
 				MostRecentVersion = (a > b)? arg0: arg1;
 				break;
 			}
 		}
 		return MostRecentVersion;
 	}
 	
 	// this just checks the downloadable object version and the cytoscape
 	// version
 	boolean versionOk(String version, boolean downloadObj) {
 		// \d+.\+d ok
 		String Match = versionMatch;
 		String Split = versionSplit;
 
 		if (downloadObj) {
 			Match = Match + "$";
 		} else { // cytoscape version
 			Match = Match + "(\\.\\d+)?$";
 			Split = "\\.|-";
 		}
 
 		if (!version.matches(Match)) {
 			return false;
 		}
 
 		String[] SplitVersion = version.split(Split);
 
 		int max = 2;
 		if (!downloadObj) {
 			max = 3; // cytoscape version numbers
 			// if there's a fourth is must be alpha
 			if (SplitVersion.length == 4) {
 				if (!SplitVersion[3].matches("[a-z]+")) {
 					return false;
 				}
 			}
 		}
 
 		// can't be longer than the accepted version types
 		if (SplitVersion.length > max) {
 			return false;
 		}
 
 		// must be digits
 		for (int i = 0; i < max && i < SplitVersion.length; i++) {
 			if (!SplitVersion[i].matches("\\d+")) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Fetches and keeps a plugin license if one is available.
 	 */
 	protected class License {
 		private java.net.URL url;
 
 		private String text;
 
 		public License(java.net.URL Url) {
 			url = Url;
 		}
 
 		public License(String LicenseText) {
 			text = LicenseText;
 		}
 
 		/**
 		 * Get the license text as a string. Will download from url if License
 		 * was not initialized with text string.
 		 * 
 		 * @return String
 		 */
 		public String getLicense() {
 			if (text == null) {
 				try {
 					text = URLUtil.download(url);
 				} catch (java.io.IOException E) {
 					E.printStackTrace();
 				}
 			}
 			return text;
 		}
 
 	}
 
 	
 }
