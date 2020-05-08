 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.core;
 
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.PatternSyntaxException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.equinox.security.storage.EncodingUtils;
 import org.eclipse.equinox.security.storage.ISecurePreferences;
 import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
 import org.eclipse.equinox.security.storage.StorageException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudAuthException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientException;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudClientImpl;
 import org.jboss.tools.deltacloud.core.client.DeltaCloudNotFoundClientException;
 import org.jboss.tools.deltacloud.core.client.HardwareProfile;
 import org.jboss.tools.deltacloud.core.client.Image;
 import org.jboss.tools.deltacloud.core.client.Instance;
 import org.jboss.tools.deltacloud.core.client.InternalDeltaCloudClient;
 import org.jboss.tools.deltacloud.core.client.Realm;
 
 public class DeltaCloud {
 
 	public final static String MOCK_TYPE = "MOCK"; //$NON-NLS-1$
 	public final static String EC2_TYPE = "EC2"; //$NON-NLS-1$
 
 	private String name;
 	private String username;
 	private String url;
 	private String type;
 	private String lastKeyname = "";
 	private String lastImageId = "";
 	private InternalDeltaCloudClient client;
 	private ArrayList<DeltaCloudInstance> instances;
 	private ArrayList<DeltaCloudImage> images;
 	private IImageFilter imageFilter;
 	private IInstanceFilter instanceFilter;
 	private Map<String, Job> actionJobs;
 	private Object imageLock = new Object();
 	private Object instanceLock = new Object();
 	private Object actionLock = new Object();
 
 	ListenerList instanceListeners = new ListenerList();
 	ListenerList imageListeners = new ListenerList();
 
 	public static interface IInstanceStateMatcher {
 		public boolean matchesState(DeltaCloudInstance instance, String instanceState);
 	}
 
 	public DeltaCloud(String name, String url, String username, String passwd) throws DeltaCloudException {
 		this(name, url, username, passwd, null);
 	}
 
 	public DeltaCloud(String name, String url, String username, String password, String type) throws DeltaCloudException {
 		this(name, url, username, password, type, IImageFilter.ALL_STRING, IInstanceFilter.ALL_STRING);
 	}
 
 	public DeltaCloud(String name, String url, String username, String type, String imageFilterRules,
 			String instanceFilterRules) throws DeltaCloudException {
 		this(name, url, username, null, type, imageFilterRules, instanceFilterRules);
 	}
 
 	public DeltaCloud(String name, String url, String username, String password,
 			String type, String imageFilterRules, String instanceFilterRules) throws DeltaCloudException {
 		this.url = url;
 		this.name = name;
 		this.username = username;
 		this.type = type;
 		storePassword(name, username, password);
 		this.client = createClient(name, url, username, password);
 		imageFilter = createImageFilter(imageFilterRules);
 		instanceFilter = createInstanceFilter(instanceFilterRules);
 	}
 
 	public void editCloud(String name, String url, String username, String password, String type)
 			throws DeltaCloudException {
 		this.url = url;
 		this.name = name;
 		this.username = username;
 		this.type = type;
 		removePassword(name, username);
 		storePassword(name, username, password);
 		client = createClient(name, url, username, password);
 		loadChildren();
 	}
 
 	private InternalDeltaCloudClient createClient(String name, String url, String username, String password) throws DeltaCloudException {
 		try {
 			if (password == null) {
 				password = getPasswordFromPreferences(name, username);
 			}
 			return new DeltaCloudClientImpl(url, username, password);
 		} catch (MalformedURLException e) {
 			throw new DeltaCloudException(MessageFormat.format("Could not access cloud at {0}", url), e);
 		} catch (StorageException e) {
 			throw new DeltaCloudException(MessageFormat.format(
 					"Could not get password for user {0} on cloud at {1} in the preferences", username, url), e);
 		}
 	}
 
 	private String getPasswordFromPreferences(String cloudName, String username) throws StorageException {
 		String key = getPreferencesKey(cloudName, username); // $NON-NLS-1$
 		ISecurePreferences root = SecurePreferencesFactory.getDefault();
 		ISecurePreferences node = root.node(key);
 		String password = node.get("password", null); //$NON-NLS-1$
 		return password;
 	}
 
 	private void storePassword(String cloudName, String username, String passwd) throws DeltaCloudException {
 		if (passwd != null) {
 			ISecurePreferences root = SecurePreferencesFactory.getDefault();
 			String key = getPreferencesKey(cloudName, username);
 			ISecurePreferences node = root.node(key);
 			try {
 				node.put("password", passwd, true /* encrypt */); //$NON-NLS-1$
 			} catch (StorageException e) {
 				// TODO: internationalize string
 				throw new DeltaCloudException("Could not store password", e);
 			}
 		}
 	}
 
 	public void removePassword(String cloudName, String userName) throws DeltaCloudException {
 		String key = getPreferencesKey(cloudName, userName);
 		ISecurePreferences root = SecurePreferencesFactory.getDefault();
 		ISecurePreferences node = root.node(key);
 		if (node == null) {
 			throw new DeltaCloudException(MessageFormat.format(
 					"Could not remove password for cloud {0} from secure preferences store", cloudName));
 		}
 		node.clear();
 	}
 
 	public static String getPreferencesKey(String cloudName, String username) {
 		String key = new StringBuilder("/org/jboss/tools/deltacloud/core/") //$NON-NLS-1$
 				.append(cloudName)
 				.append('/') //$NON-NLS-1$
 				.append(username)
 				.toString();
 		return EncodingUtils.encodeSlashes(key);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getURL() {
 		return url;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public String getLastImageId() {
 		return lastImageId;
 	}
 
 	public void setLastImageId(String lastImageId) {
 		this.lastImageId = lastImageId;
 	}
 
 	public String getLastKeyname() {
 		return lastKeyname;
 	}
 
 	public void setLastKeyname(String lastKeyname) {
 		this.lastKeyname = lastKeyname;
 	}
 
 	public IInstanceFilter getInstanceFilter() {
 		return instanceFilter;
 	}
 
 	public void updateInstanceFilter(String ruleString) throws Exception {
 		String rules = getInstanceFilter().toString();
 		instanceFilter = createInstanceFilter(ruleString);
 		if (!rules.equals(ruleString)) {
 			// save();
 			// TODO: remove notification with all instances, replace by
 			// notifying the changed instance
 			notifyInstanceListListeners();
 		}
 	}
 
 	private IInstanceFilter createInstanceFilter(String ruleString) {
 		IInstanceFilter instanceFilter = null;
 		if (IInstanceFilter.ALL_STRING.equals(ruleString)) {
 			instanceFilter = new AllInstanceFilter();
 		} else {
 			try {
 				instanceFilter = new InstanceFilter();
 				instanceFilter.setRules(ruleString);
 			} catch (PatternSyntaxException e) {
 				instanceFilter.setRules(IInstanceFilter.ALL_STRING);
 			}
 		}
 		return instanceFilter;
 	}
 
 	public IImageFilter getImageFilter() {
 		return imageFilter;
 	}
 
 	public void updateImageFilter(String ruleString) throws Exception {
 		String rules = getImageFilter().toString();
 		this.imageFilter = createImageFilter(ruleString);
 		if (!rules.equals(ruleString)) {
 			// save();
 			notifyImageListListeners(getImages());
 		}
 	}
 
 	private IImageFilter createImageFilter(String ruleString) {
 		IImageFilter imageFilter = null;
 		if (IImageFilter.ALL_STRING.equals(ruleString)) {
 			imageFilter = new AllImageFilter();
 		} else {
 			try {
 				imageFilter = new ImageFilter();
 				imageFilter.setRules(ruleString);
 			} catch (PatternSyntaxException e) {
 				imageFilter.setRules(IImageFilter.ALL_STRING);
 			}
 		}
 		return imageFilter;
 	}
 
 	public void loadChildren() throws DeltaCloudException {
 		DeltaCloudMultiException multiException = new DeltaCloudMultiException(MessageFormat.format(
 				"Could not load children of cloud {0}", getName()));
 		clearImages();
 		clearInstances();
 		try {
 			loadImages();
 		} catch (DeltaCloudException e) {
 			multiException.addError(e);
 		}
 		try {
 			loadInstances();
 		} catch (DeltaCloudException e) {
 			multiException.addError(e);
 		}
 
 		if (!multiException.isEmpty()) {
 			throw multiException;
 		}
 	}
 
 	// public void save() {
 	// // Currently we have to save all clouds instead of just this one
 	// DeltaCloudManager.getDefault().saveClouds();
 	// }
 
 	public void addInstanceListListener(IInstanceListListener listener) {
 		instanceListeners.add(listener);
 	}
 
 	public void removeInstanceListListener(IInstanceListListener listener) {
 		instanceListeners.remove(listener);
 	}
 
 	public void notifyInstanceListListeners(DeltaCloudInstance[] array) {
 		Object[] listeners = instanceListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i) {
 			((IInstanceListListener) listeners[i]).listChanged(this, array);
 		}
 	}
 
 	public void addImageListListener(IImageListListener listener) {
 		imageListeners.add(listener);
 	}
 
 	public void removeImageListListener(IImageListListener listener) {
 		imageListeners.remove(listener);
 	}
 
 	public DeltaCloudImage[] notifyImageListListeners() {
 		DeltaCloudImage[] images = cloneImagesArray();
 		notifyImageListListeners(images);
 		return images;
 	}
 
 	public DeltaCloudInstance[] notifyInstanceListListeners() {
 		DeltaCloudInstance[] instances = cloneInstancesArray();
 		notifyInstanceListListeners(instances);
 		return instances;
 	}
 
 	private DeltaCloudImage[] cloneImagesArray() {
 		DeltaCloudImage[] imageArray = new DeltaCloudImage[images.size()];
 		return images.toArray(imageArray);
 	}
 
 	private DeltaCloudInstance[] cloneInstancesArray() {
 		DeltaCloudInstance[] instanceArray = new DeltaCloudInstance[instances.size()];
 		return instances.toArray(instanceArray);
 	}
 
 	public void notifyImageListListeners(DeltaCloudImage[] array) {
 		Object[] listeners = imageListeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i)
 			((IImageListListener) listeners[i]).listChanged(this, array);
 	}
 
 	public Job getInstanceJob(String id) {
 		synchronized (actionLock) {
 			Job j = null;
 			if (actionJobs != null) {
 				return actionJobs.get(id);
 			}
 			return j;
 		}
 	}
 
 	public void registerInstanceJob(String id, Job j) {
 		synchronized (actionLock) {
 			if (actionJobs == null)
 				actionJobs = new HashMap<String, Job>();
 			actionJobs.put(id, j);
 		}
 	}
 
 	public DeltaCloudInstance waitWhilePending(String instanceId, IProgressMonitor pm) throws InterruptedException,
 			DeltaCloudException {
 		IInstanceStateMatcher differsFromPending = new IInstanceStateMatcher() {
 
 			@Override
 			public boolean matchesState(DeltaCloudInstance instance, String instanceState) {
 				return !DeltaCloudInstance.PENDING.equals(instanceState);
 			}
 		};
 		return waitForState(instanceId, differsFromPending, pm);
 	}
 
 	public DeltaCloudInstance waitForState(String instanceId, final String expectedState, IProgressMonitor pm)
 			throws InterruptedException, DeltaCloudException {
 		IInstanceStateMatcher stateMatcher = new IInstanceStateMatcher() {
 
 			@Override
 			public boolean matchesState(DeltaCloudInstance instance, String instanceState) {
 				return expectedState != null && expectedState.equals(instanceState);
 			}
 		};
 		return waitForState(instanceId, stateMatcher, pm);
 	}
 
 	public DeltaCloudInstance waitForState(String instanceId, IInstanceStateMatcher stateMatcher, IProgressMonitor pm)
 			throws InterruptedException, DeltaCloudException {
 		DeltaCloudInstance instance = getInstance(instanceId);
 		while (!pm.isCanceled()) {
 			if (stateMatcher.matchesState(instance, instance.getState())
 					|| instance.getState().equals(DeltaCloudInstance.TERMINATED)) {
 				return instance;
 			}
 			Thread.sleep(400);
 			instance = refreshInstance(instance.getId());
 		}
 		return instance;
 	}
 
 	public void removeInstanceJob(String id, Job j) {
 		synchronized (actionLock) {
 			if (actionJobs != null && actionJobs.get(id) == j)
 				actionJobs.remove(id);
 		}
 	}
 
 	/**
 	 * Loads the instances from the server and stores them in this instance.
 	 * Furthermore listeners get informed.
 	 * 
 	 * @return the instances
 	 * @throws DeltaCloudException
 	 * 
 	 * @see #notifyInstanceListListeners(DeltaCloudInstance[])
 	 */
 	public DeltaCloudInstance[] loadInstances() throws DeltaCloudException {
 		synchronized (instanceLock) {
 			clearInstances();
 			try {
 				List<Instance> list = client.listInstances();
 				for (Iterator<Instance> i = list.iterator(); i.hasNext();) {
 					DeltaCloudInstance instance = new DeltaCloudInstance(this, i.next());
 					instances.add(instance);
 				}
 				// TODO: remove notification with all instances, replace by
 				// notifying the changed instance
 				return notifyInstanceListListeners();
 			} catch (DeltaCloudClientException e) {
 				throw new DeltaCloudException(MessageFormat.format("Could not load instances of cloud {0}: {1}",
 						getName(), e.getMessage()), e);
 			}
 		}
 	}
 
 	private void clearInstances() {
 		synchronized (instanceLock) {
 			instances = new ArrayList<DeltaCloudInstance>();
 			notifyInstanceListListeners();
 		}
 	}
 
 	public DeltaCloudInstance[] getInstances() throws DeltaCloudException {
 		synchronized (instanceLock) {
 			if (instances == null) {
 				return loadInstances();
 			}
 			return cloneInstancesArray();
 		}
 	}
 
 	public void createKey(String keyname, String keystoreLocation) throws DeltaCloudException {
 		try {
 			client.createKey(keyname, keystoreLocation);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	public void deleteKey(String keyname) throws DeltaCloudException {
 		try {
 			client.deleteKey(keyname);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	public void replaceInstance(DeltaCloudInstance instance) {
 		String instanceId = instance.getId();
 		if (instance != null) {
 			boolean found = false;
 			for (int i = 0; i < instances.size(); ++i) {
 				DeltaCloudInstance inst = instances.get(i);
 				if (inst.getId().equals(instanceId)) {
 					found = true;
 					instances.set(i, instance);
 				}
 			}
 			if (!found) {
 				instances.add(instance);
 			}
 			// TODO: remove notification with all instances, replace by
 			// notifying the changed instance
 			notifyInstanceListListeners();
 		}
 	}
 
 	public DeltaCloudInstance refreshInstance(String instanceId) throws DeltaCloudException {
 		DeltaCloudInstance retVal = null;
 		try {
 			Instance instance = client.listInstances(instanceId);
 			retVal = new DeltaCloudInstance(this, instance);
 			for (int i = 0; i < instances.size(); ++i) {
 				DeltaCloudInstance inst = instances.get(i);
 				if (inst.getId().equals(instanceId)) {
 					// FIXME: remove BOGUS state when server fixes state
 					// problems
 					if (!(retVal.getState().equals(DeltaCloudInstance.BOGUS))
 							&& !(inst.getState().equals(retVal.getState()))) {
 						instances.set(i, retVal);
 						notifyInstanceListListeners();
 						return retVal;
 					}
 				}
 			}
 		} catch (DeltaCloudClientException e) {
 			// will get here when a pending instance is being checked
 		}
 		return retVal;
 	}
 
 	public boolean performInstanceAction(String instanceId, String actionId) throws DeltaCloudException {
 		return performInstanceAction(getInstance(instanceId), actionId);
 	}
 
 	protected boolean performInstanceAction(DeltaCloudInstance instance, String actionId) throws DeltaCloudException {
 		try {
 			if (instance == null) {
 				return false;
 			}
 
 			boolean result = instance.performInstanceAction(actionId, client);
 			if (result) {
 				// TODO: remove notification with all instances, replace by
 				// notifying the changed instance
 				notifyInstanceListListeners();
 			}
 			return result;
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	private DeltaCloudInstance getInstance(String instanceId) {
 		for (DeltaCloudInstance instance : instances) {
 			if (instance.getId().equals(instanceId)) {
 				return instance;
 			}
 		}
 		return null;
 	}
 
 	public DeltaCloudHardwareProfile[] getProfiles() throws DeltaCloudException {
 		ArrayList<DeltaCloudHardwareProfile> profiles = new ArrayList<DeltaCloudHardwareProfile>();
 		try {
 			List<HardwareProfile> list = client.listProfiles();
 			for (Iterator<HardwareProfile> i = list.iterator(); i.hasNext();) {
 				DeltaCloudHardwareProfile profile = new DeltaCloudHardwareProfile(i.next());
 				profiles.add(profile);
 			}
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(MessageFormat.format("Could not list profiles on cloud {0}", name), e);
 		}
 		DeltaCloudHardwareProfile[] profileArray = new DeltaCloudHardwareProfile[profiles.size()];
 		profileArray = profiles.toArray(profileArray);
 		return profileArray;
 	}
 
 	public DeltaCloudImage loadImage(String imageId) throws DeltaCloudException {
 		try {
 			Image image = client.listImages(imageId);
 			DeltaCloudImage deltaCloudImage = addImage(image);
 			return deltaCloudImage;
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 	}
 
 	/**
 	 * Loads the available images from the server and stores them locally.
 	 * Furthermore listeners get informed.
 	 * 
 	 * @return the delta cloud image[]
 	 * @throws DeltaCloudClientException
 	 * 
 	 * @see #notifyImageListListeners(DeltaCloudImage[])
 	 */
 	public DeltaCloudImage[] loadImages() throws DeltaCloudException {
 		synchronized (imageLock) {
 			try {
 				clearImages();
 				List<Image> list = client.listImages();
 				for (Iterator<Image> i = list.iterator(); i.hasNext();) {
 					addImage(i.next());
 				}
 				return notifyImageListListeners();
 			} catch (DeltaCloudClientException e) {
 				throw new DeltaCloudException(MessageFormat.format("Could not load images of cloud {0}: {1}",
 						getName(), e.getMessage()), e);
 			}
 		}
 	}
 
 	private void clearImages() {
 		synchronized (imageLock) {
 			images = new ArrayList<DeltaCloudImage>();
 			notifyImageListListeners();
 		}
 	}
 
 	private DeltaCloudImage addImage(Image image) {
 		DeltaCloudImage deltaCloudImage = new DeltaCloudImage(image, this);
 		images.add(deltaCloudImage);
 		return deltaCloudImage;
 	}
 
 	public DeltaCloudImage[] getImages() throws DeltaCloudException {
 		synchronized (imageLock) {
 			if (images == null) {
 				return loadImages();
 			}
 			return cloneImagesArray();
 		}
 	}
 
 	public DeltaCloudImage getImage(String imageId) {
 		DeltaCloudImage retVal = null;
 		try {
 			Image image = client.listImages(imageId);
 			retVal = new DeltaCloudImage(image, this);
 		} catch (Exception e) {
 			e.printStackTrace();
 			// do nothing and return null
 		}
 		return retVal;
 	}
 
 	public boolean testConnection() throws DeltaCloudException {
 		String instanceId = "nonexistingInstance"; //$NON-NLS-1$
 		try {
 			client.listInstances(instanceId);
 			return true;
 		} catch (DeltaCloudNotFoundClientException e) {
 			return true;
 		} catch (DeltaCloudAuthException e) {
 			return false;
 		} catch (DeltaCloudClientException e) {
 			return false;
 		}
 	}
 
 	public DeltaCloudRealm[] getRealms() throws DeltaCloudException {
 		ArrayList<DeltaCloudRealm> realms = new ArrayList<DeltaCloudRealm>();
 		try {
 			List<Realm> list = client.listRealms();
 			for (Iterator<Realm> i = list.iterator(); i.hasNext();) {
 				DeltaCloudRealm realm = new DeltaCloudRealm(i.next());
 				realms.add(realm);
 			}
 			return realms.toArray(new DeltaCloudRealm[realms.size()]);
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(MessageFormat.format("Could not get realms for cloud {0}", name), e);
 		}
 	}
 
 	public DeltaCloudInstance createInstance(String name, String imageId, String realmId, String profileId,
 			String keyname, String memory, String storage) throws DeltaCloudException {
 		try {
 			Instance instance = null;
 			if (keyname != null) {
 				instance = client.createInstance(imageId, profileId, realmId, name, keyname, memory, storage);
 			} else {
 				instance = client.createInstance(imageId, profileId, realmId, name, memory, storage);
 			}
 			if (instance != null) {
 				DeltaCloudInstance newInstance = new DeltaCloudInstance(this, instance);
 				newInstance.setGivenName(name);
 				getInstances(); // make sure instances are initialized
 				instances.add(newInstance);
 				notifyInstanceListListeners();
 				return newInstance;
 			}
 		} catch (DeltaCloudClientException e) {
 			throw new DeltaCloudException(e);
 		}
 		return null;
 	}

	public void destroyInstance(String id) throws DeltaCloudException{
		throw new DeltaCloudException("Method destroyInstance(String id) has not implemented yet"); //$NON-NLS-1$
	}
 }
