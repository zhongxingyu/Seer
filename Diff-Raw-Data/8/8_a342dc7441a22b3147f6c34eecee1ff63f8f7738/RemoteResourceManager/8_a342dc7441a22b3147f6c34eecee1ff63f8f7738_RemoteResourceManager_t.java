 package org.geworkbench.builtin.projects.remoteresources;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
import org.geworkbench.util.FilePathnameUtils;

 /**
  * Remote resource manager.
  * 
  * @author xiaoqing, zji
  * @version $Id: RemoteResourceManager.java,v 1.25 2008-10-28 16:55:18 keshav Exp $
  */
 public class RemoteResourceManager {
 	private ArrayList<RemoteResource> existedResources;
 	private static final String DEFAULTRESOURCEFILE = "defaultResources.csv";
 	private String filename;
 	private String cloumnseparator = ",";
 
 	public RemoteResourceManager() {
 		existedResources = new ArrayList<RemoteResource>();
		filename = FilePathnameUtils.getTemporaryFilesDirectoryPath() + DEFAULTRESOURCEFILE;
 		if (filename != null && new File(filename).canRead()) {
 			init(new File(filename));
 
 			deleteRemoteResource("caARRAYStage");
 			RemoteResource rr = new RemoteResource("caARRAYStage",
 					"array-stage.nci.nih.gov", "8080", "http:", "", "");
 			existedResources.add(rr);
 		} else {
 			init();
 		}
 
 	}
 
 	/**
 	 * A default setup when no property file is found. init
 	 */
 	protected void init() {
 		RemoteResource rr1 = new RemoteResource("caARRAY", "array.nci.nih.gov",
 				"8080", "http:", "", "");
 		existedResources.add(rr1);
 		RemoteResource rr2 = new RemoteResource("caARRAYStage",
 				"array-stage.nci.nih.gov", "8080", "http:", "", "");
 		existedResources.add(rr2);
 	}
 
 	/**
 	 * Init the existed resources from a file.
 	 * 
 	 * @param propertyfilename
 	 *            File
 	 */
 	private void init(File propertyfilename) {
 		try {
 
 			InputStream input = new FileInputStream(propertyfilename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(input));
 			String line = null;
 
 			while ((line = br.readLine()) != null) {
 
 				String[] cols = line.split(",");
 				if (cols.length > 0) {
 					RemoteResource rr = RemoteResource.createNewInstance(cols);
 					if (rr != null) {
 						existedResources.add(rr);
 
 					}
 				}
 			}
 			br.close();
 			input.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 
 		}
 
 	}
 
 	/**
 	 * Init the existed resources from a Index service.
 	 * 
 	 * @param url
 	 *            name
 	 */
 	protected boolean init(String urlname) {
 		try {
 			String test = null;
 
 			if (test == null) {
 				return false;
 			}
 			removeIndexResources();
 
 			String[] lists = test.split("!");
 			if (lists != null) {
 				for (String s : lists) {
 					String[] cols = s.split(",");
 					if (cols != null && cols.length > 0) {
 						RemoteResource rr = RemoteResource
 								.createNewInstance(cols);
 						if (rr != null) {
 							rr.setEditable(false);
 							existedResources.add(rr);
 
 						}
 					}
 
 				}
 			}
 			return true;
 
 		} catch (Exception e) {
 			System.out.println(e + "RemoteResourceManager.init" + urlname);
 			e.printStackTrace();
 		}
 		return false;
 
 	}
 
 	public void removeIndexResources() {
 		int size = existedResources.size();
 		ArrayList<RemoteResource> newExistedResources = new ArrayList<RemoteResource>();
 		boolean[] removeIndex = new boolean[size];
 		for (int i = 0; i < existedResources.size(); i++) {
 			RemoteResource rr = existedResources.get(i);
 			removeIndex[i] = rr.isEditable();
 			if (rr.isEditable()) {
 				newExistedResources.add(rr);
 			}
 
 		}
 		existedResources = newExistedResources;
 	}
 
 	/**
 	 * getFristItem
 	 */
 	public String getFirstItemName() {
 		if (existedResources != null && existedResources.size() > 0) {
 			return existedResources.get(0).getShortname();
 		}
 		return null;
 
 	}
 
 	public String[] getItems() {
 		int size = existedResources.size();
 		String[] shortnames = new String[size];
 		for (int i = 0; i < size; i++) {
 			shortnames[i] = existedResources.get(i).getShortname();
 		}
 		return shortnames;
 	}
 
 	public RemoteResource getSelectedResouceByName(String name) {
 		for (RemoteResource rr : existedResources) {
 			if (rr.getShortname().equals(name)) {
 				return rr;
 			}
 		}
 		return null;
 
 	}
 
 	/**
 	 * Edit the properties of a romoteResource
 	 */
 	public void editRemoteResource(int i, RemoteResource rResource) {
 		RemoteResource rr = existedResources.get(i);
 		rr.update(rResource);
 	}
 
 	/**
 	 * Delete one resource object
 	 * 
 	 * @param rResource
 	 *            RemoteResource
 	 * @return boolean
 	 */
 	public boolean deleteRemoteResource(RemoteResource rResource) {
 		if (existedResources.remove(rResource)) {
 			saveToFile();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Delete one resource based on its index position.
 	 * 
 	 * @param rResourceIndex
 	 *            int
 	 * @return boolean
 	 */
 	public boolean deleteRemoteResource(int rResourceIndex) {
 		if (existedResources.remove(rResourceIndex) != null) {
 			saveToFile();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Delete one resource based on its shortname.
 	 * 
 	 * @param name
 	 *            String
 	 * @return boolean
 	 */
 	public boolean deleteRemoteResource(String name) {
 		if (getSelectedResouceByName(name) != null) {
 			existedResources.remove(getSelectedResouceByName(name));
 		}
 		return false;
 	}
 
 	/**
 	 * Add one new resource.
 	 * 
 	 * @param newResource
 	 *            RemoteResource
 	 * @return boolean
 	 */
 	public boolean addRemoteResource(RemoteResource newResource) {
 		if (existedResources.contains(newResource)) {
 			deleteRemoteResource(newResource);
 		}
 		return existedResources.add(newResource);
 	}
 
 	/**
 	 * saveToFile
 	 */
 	public void saveToFile() {
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
 					filename)));
 			if (existedResources.size() == 0) {
 
 				return;
 			}
 
 			for (RemoteResource s : existedResources) {
 
 				writer.write(s.getShortname() + cloumnseparator + s.getUri()
 						+ cloumnseparator + s.getPortnumber() + cloumnseparator
 						+ s.getConnectProtocal() + cloumnseparator
 						+ s.getUsername() + cloumnseparator + s.getPassword()
 						+ cloumnseparator + s.isEditable());
 				writer.newLine();
 			}
 
 			writer.flush();
 			writer.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 	}
 
 }
