 package core.browse;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import core.config.Constants;
 import core.config.ResourceFileConfig;
 import core.model.Resource;
 import core.model.ResourceKind;
 import core.model.ResourceUtil;
 import core.security.AccessRight;
 import core.util.CollectionUtil;
 import core.util.FileUtil;
 import core.util.JsonUtil;
 import core.util.ReflectionUtil;
 import core.util.Validator;
 import core.web.util.FileUpload;
 
 /**
  * Represents a node in the repository, while browsing it. Contains logic for
  * security policy.
  * 
  * @author jani
  * 
  */
 public class Node {
 	/**
 	 * Separator for path parts.
 	 */
 	public static final String PATH_SEPARATOR = "/";
 
 	private List<String> parts;
 	private List<String> resourceParts;
 	private List<String> additionalParts;
 	private NodeType type;
 	private File seriesRoot;
 	private AccessRight access;
 	private String login;
 	private Resource resource;
 
 	/**
 	 * Constructor. Parses the path and determines the node type, according to
 	 * it.
 	 * 
 	 * @param seriesRoot
 	 *            The root directory for series in the repository.
 	 * @param path
 	 *            Path of the node, relative to the root.
 	 * 
 	 * @param login
 	 *            Current user login.
 	 * 
 	 */
 	public Node(File seriesRoot, String path, String login) {
 		parts = new ArrayList<String>();
 		resourceParts = new ArrayList<String>();
 		additionalParts = new ArrayList<String>();
 		type = null;
 		this.seriesRoot = seriesRoot;
 		access = ResourceUtil.getAccessRight(seriesRoot, new File(seriesRoot,
 				path), login);
 		this.login = login;
 
 		init(path);
 	}
 
 	private void init(String path) {
 		assert path != null;
 
 		boolean additionalFile = false;
 		boolean problemFile = false;
 		boolean systemFile = false;
 		for (String part : splitPath(path)) {
 			if (!additionalFile && parts.size() < ResourceKind.NESTING.size()
 					&& part.matches(ResourceFileConfig.RESOURCE_ID)) {
 				// part is a resource identifier
 				parts.add(part);
 				resourceParts.add(part);
 			} else if (!additionalFile
 					&& parts.size() == ResourceKind.NESTING.size()
 					&& part.matches(ResourceFileConfig.PROBLEM_EXTRA_FILES)) {
 				// part is a problem-specific file
 				problemFile = true;
 				parts.add(part);
 				break;
 			} else if (!additionalFile
 					&& parts.size() <= ResourceKind.NESTING.size()
 					&& parts.size() > 0
 					&& (part.equals(ResourceKind.NESTING.get(parts.size() - 1)
 							.getResourceFilenameExt()))
 					|| part.equals(ResourceFileConfig.SECURITY_FILENAME)) {
 				// part is a system file
 				systemFile = true;
 				parts.add(part);
 				break;
 			} else if (additionalFile
 					|| (parts.size() > 0 && part
 							.matches(ResourceFileConfig.ADDITIONAL_FILES))) {
 				// part is an additional file
 				additionalFile = true;
 				parts.add(part);
 				additionalParts.add(part);
 			} else {
 				break;
 			}
 		}
 
 		if (problemFile) {
 			assert parts.size() > ResourceKind.NESTING.size();
 			type = NodeType.PROBLEM_FILE;
 		} else if (systemFile) {
 			assert parts.size() > 1
 					&& parts.size() <= ResourceKind.NESTING.size() + 1;
 			type = NodeType.SYSTEM_FILE;
 		} else if (additionalFile) {
 			assert additionalParts.size() > 0;
 			type = NodeType.SYSTEM_DIRECTORY;
 		} else if (resourceParts.size() <= ResourceKind.NESTING.size()) {
 			if (resourceParts.size() == 0) {
 				type = NodeType.ROOT;
 			} else {
 				type = NodeType.RESOURCE;
 			}
 		}
 
 		if (getResouceKind() != null && access.includes(AccessRight.VIEW)) {
 			resource = ResourceUtil.loadResource(getResourceDirectory(),
 					getResouceKind());
 		}
 	}
 
 	/**
 	 * Gets all path parts of the node.
 	 * 
 	 * @return Path parts.
 	 */
 	public List<String> getParts() {
 		return parts;
 	}
 
 	/**
 	 * Gets resource path parts of the node.
 	 * 
 	 * @return Resource path parts.
 	 */
 	public List<String> getResourceParts() {
 		return resourceParts;
 	}
 
 	/**
 	 * Gets additional path parts of the node.
 	 * 
 	 * @return Additional path parts.
 	 */
 	public List<String> getAdditionalParts() {
 		return additionalParts;
 	}
 
 	/**
 	 * Gets the path.
 	 * 
 	 * @return The path.
 	 */
 	public String getPath() {
 		return CollectionUtil.join(parts, PATH_SEPARATOR);
 	}
 
 	/**
 	 * Gets the resource path.
 	 * 
 	 * @return The resource path.
 	 */
 	public String getResourcePath() {
 		return CollectionUtil.join(resourceParts, PATH_SEPARATOR);
 	}
 
 	/**
 	 * Gets the additional path.
 	 * 
 	 * @return The additional path.
 	 */
 	public String getAdditionalPath() {
 		return CollectionUtil.join(additionalParts, PATH_SEPARATOR);
 	}
 
 	/**
 	 * Gets the type of the node.
 	 * 
 	 * @return {@link NodeType}.
 	 */
 	public NodeType getType() {
 		return type;
 	}
 
 	/**
 	 * Checks if the node is the root node.
 	 * 
 	 * @return true if root.
 	 */
 	public boolean isRoot() {
 		return type == NodeType.ROOT;
 	}
 
 	/**
 	 * Gets the {@link AccessRight} of the node.
 	 * 
 	 * @return {@link AccessRight}
 	 */
 	public AccessRight getAccess() {
 		return access;
 	}
 
 	/**
 	 * Gets the file, pointing to the node.
 	 * 
 	 * @return {@link File}
 	 */
 	public File getFile() {
 		return new File(seriesRoot, getPath());
 	}
 
 	/**
 	 * Gets the resource directory, pointing to the resource.
 	 * 
 	 * @return {@link File}
 	 */
 	public File getResourceDirectory() {
 		return new File(seriesRoot, getResourcePath());
 	}
 
 	/**
 	 * Gets the resource associated with this node.
 	 * 
 	 * @return {@link Resource}
 	 */
 	public Resource getResouce() {
 		return resource;
 	}
 
 	/**
 	 * Gets the resource kind for the node.
 	 * 
 	 * @return {@link ResourceKind}
 	 */
 	public ResourceKind getResouceKind() {
 		int index = Math.min(resourceParts.size(), ResourceKind.NESTING.size());
 		if (index > 0) {
 			return ResourceKind.NESTING.get(index - 1);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the resource kind for the successor node.
 	 * 
 	 * @return {@link ResourceKind}
 	 */
 	public ResourceKind getSuccessorKind() {
 		int index = resourceParts.size();
 		if (index < ResourceKind.NESTING.size()) {
 			return ResourceKind.NESTING.get(index);
 		}
 		return null;
 	}
 
 	/**
 	 * Gets a part of the path.
 	 * 
 	 * @param start
 	 * @param end
 	 * @return Part of the path.
 	 */
 	public String getPath(int start, int end) {
 		if (start >= 0 && end <= parts.size()) {
 			return CollectionUtil.join(parts.subList(start, end),
 					PATH_SEPARATOR);
 		}
 		return "";
 	}
 
 	/**
 	 * Gets the parent path.
 	 * 
 	 * @return Parent path.
 	 */
 	public String getParentPath() {
 		return getPath(0, parts.size() - 1);
 	}
 
 	/**
 	 * Gets the parent node.
 	 * 
 	 * @return {@link Node}
 	 */
 	public Node getParent() {
 		return new Node(seriesRoot, getParentPath(), login);
 	}
 
 	/**
 	 * Finds a child node, specified by the name.
 	 * 
 	 * @param name
 	 * @return child {@link Node}.
 	 */
 	public Node getChild(String name) {
 		List<String> parts = new ArrayList<String>(getParts());
 		parts.add(name);
 		return new Node(seriesRoot, CollectionUtil.join(parts, PATH_SEPARATOR),
 				login);
 	}
 
 	/**
 	 * Gets a list of all visible child files.
 	 * 
 	 * @return List of files.
 	 */
 	public List<File> getFiles() {
 		List<File> res = new ArrayList<File>();
 
 		if (type == NodeType.RESOURCE) {
 			if (resource != null) {
 				res = resource.getFilesList(access);
 			}
 		} else {
 			if (access.includes(AccessRight.VIEW)) {
 				if (type == NodeType.SYSTEM_DIRECTORY
 						&& (access.includes(AccessRight.VIEW_FULL) || additionalParts
 								.get(0)
 								.equals(ResourceFileConfig.RESOURCE_PUBLIC_DIRECTORY_NAME))) {
 					res.addAll(FileUtil.getDirectoryFiles(getFile()));
 				}
 			}
 		}
 
 		return res;
 	}
 
 	/**
 	 * Converts the node to byte array, according to the permissions.
 	 * 
 	 * @return Byte array.
 	 */
 	public byte[] getDownload() {
 		byte[] res = null;
 		if (access.includes(AccessRight.VIEW)) {
 			if (type == NodeType.RESOURCE) {
 				Resource resource = ResourceUtil.loadResource(
 						getResourceDirectory(), getResouceKind());
 
 				if (resource != null) {
 					res = resource.getDownloader(login, access).toZip();
 				}
 			} else if (type == NodeType.PROBLEM_FILE) {
 				Resource problem = ResourceUtil.loadResource(
 						getResourceDirectory(), getResouceKind());
 				List<File> files = problem.getFilesList(access);
 				if (files.contains(getFile())) {
 					res = FileUtil.read(getFile());
 				}
 			} else if (type == NodeType.SYSTEM_FILE) {
 				if (getFile().getName().equals(
 						ResourceFileConfig.SECURITY_FILENAME)) {
 					if (access.includes(AccessRight.VIEW_FULL)) {
 						res = FileUtil.read(getFile());
 					}
 				} else {
 					Resource problem = ResourceUtil.loadResource(
 							getResourceDirectory(), getResouceKind());
 					String json = problem.getJsonString(access);
 					res = json.getBytes();
 				}
 			} else {
 				if (type == NodeType.SYSTEM_DIRECTORY
 						&& (access.includes(AccessRight.VIEW_FULL) || additionalParts
 								.get(0)
 								.equals(ResourceFileConfig.RESOURCE_PUBLIC_DIRECTORY_NAME))) {
 					res = FileUtil.read(getFile());
 				}
 			}
 		}
 
 		return res;
 	}
 
 	/**
 	 * Gets the filename for downloading of the node.
 	 * 
 	 * @return Filename.
 	 */
 	public String getDownloadFilename() {
 		String res = getFile().getName();
 		if (type == NodeType.RESOURCE || getFile().isDirectory()) {
 			res += Constants.ZIP_FILE_EXTENSION;
 		}
 		return res;
 
 	}
 
 	/**
 	 * Adds a subdirectory to the node.
 	 * 
 	 * @param name
 	 *            Name of the directory.
 	 * @return {@link Validator}
 	 */
 	public Validator addDirectory(String name) {
 		Validator res = new Validator();
 		if (getFile().isDirectory()) {
 			if (access.includes(AccessRight.VIEW_FULL)
 					&& getType() == NodeType.SYSTEM_DIRECTORY) {
 				if (name.matches(ResourceFileConfig.NEW_DIRECTORY)) {
 					res = FileUtil.createDirectory(getFile(), name);
 				} else {
 					res.addError("name", "Invalid directory name");
 				}
 			} else {
 				res.addError("access", "No permission to add directory");
 			}
 		} else {
 			res.addError("access", "Parent directory does not exist");
 		}
 		return res;
 	}
 
 	/**
 	 * Adds a file to the node.
 	 * 
 	 * @param file
 	 *            File to add.
 	 * @param overwrite
 	 *            Indicated whether to overwrite an existing file.
 	 * @return {@link Validator}
 	 */
 	public Validator addFile(FileUpload file, boolean overwrite) {
 		return addFile(file.getStream(), file.getFilename(), overwrite);
 	}
 
 	/**
 	 * Adds a file to the node.
 	 * 
 	 * @param input
 	 *            input stream of file
 	 * @param name
 	 *            name of file
 	 * @param overwrite
 	 *            Indicated whether to overwrite an existing file.
 	 * 
 	 * @return {@link Validator}
 	 */
 	public Validator addFile(InputStream input, String name, boolean overwrite) {
 		File destination = new File(getFile(), name);
 		Validator res = new Validator();
 		if (getFile().isDirectory()) {
 			if (access.includes(AccessRight.VIEW_FULL)) {
 
 				boolean isAdditional = (getType() == NodeType.SYSTEM_DIRECTORY);
 				boolean isMainResourceFile = (type == NodeType.RESOURCE)
 						&& destination.getName().equals(
 								getResouceKind().getResourceFilenameExt());
 				boolean isSecurityFile = (type == NodeType.RESOURCE)
 						&& destination.getName().equals(
 								ResourceFileConfig.SECURITY_FILENAME);
 				boolean isProblemFile = (getResouceKind() == ResourceKind.PROBLEM);
 
 				if (isAdditional) {
 					res = FileUtil.saveStream(input, destination, overwrite);
 				} else if (isMainResourceFile) {
 					Resource temp = ResourceUtil.loadResource(input, "temp",
 							getResouceKind());
 					if (temp == null) {
 						res.addError("file", "Invalid json file");
 					} else {
 						temp.setDirectory(getResourceDirectory());
 						temp.save();
 					}
 
 				} else if (isSecurityFile) {
 					Object o = JsonUtil.readJsonFile(input, Object.class);
 					if (o == null) {
 						res.addError("file", "Invalid security file");
 					} else {
 						JsonUtil.writeJsonFile(new File(getResourceDirectory(),
 								ResourceFileConfig.SECURITY_FILENAME), o);
 					}
 				} else if (isProblemFile) {
 					if (destination.getName().matches(
 							ResourceFileConfig.PROBLEM_EXTRA_FILES)) {
 						res = FileUtil
 								.saveStream(input, destination, overwrite);
 					} else {
 						res.addError("file", "File not allowed");
 					}
 					return res;
 				} else {
 					res.addError("file", "File not allowed");
 				}
 			} else {
 				res.addError("access", "No permission ot add file.");
 			}
 		} else {
 			res.addError("parent", "Parent directory does not exist.");
 		}
 		return res;
 	}
 
 	/**
 	 * Extracts a zip to the node.
 	 * 
 	 * @param input
 	 *            Stream of the zip file.
 	 * @return {@link Validator}
 	 */
 	public Validator addZip(InputStream input) {
 		Validator res = new Validator();
 
 		if (getAccess().includes(AccessRight.VIEW_FULL)) {
 			ResourceKind successor = getSuccessorKind();
 
 			File tmp = FileUtil.getTempDirectory();
 			File zip = new File(tmp, Constants.TEMP_ZIP_FILENAME);
 			FileUtil.saveStream(input, zip, false);
 			if (successor == null) {
 				// TODO: non-resource zip
 			} else {
 				// resource
 				if (FileUtil.unzipArchive(zip, tmp)) {
 					List<File> files = FileUtil.getDirectoryFiles(tmp,
 							new FileFilter() {
 								@Override
 								public boolean accept(File arg0) {
 									return arg0.isDirectory();
 								}
 							});
 					if (files.size() == 1) {
 						Resource resource = ResourceUtil.loadResource(
 								files.get(0), successor);
 						if (resource == null) {
 							res.addError("json", "Unable to parse resource.");
 						} else {
 							res = resource.validate();
 							if (res.isValid()) {
 								Node child = getChild(resource.getId());
 								if (child.getFile().exists()) {
 									FileUtil.delete(child.getFile());
 								}
 								if (!FileUtil.copyDirectory(files.get(0),
 										child.getFile())) {
 									res.addError("copy",
 											"Unable to move zipped files");
 								}
 							}
 						}
 
 					} else {
 						res.addError("root",
 								"Zip should contains only one root directory.");
 					}
 				} else {
 					res.addError("zip", "Unable to open zip.");
 				}
 			}
 			FileUtil.delete(tmp);
 		} else {
 			res.addError("access", "No permission to zip.");
 		}
 		return res;
 	}
 
 	/**
 	 * Adds a child resource to the node.
 	 * 
 	 * @param name
 	 *            Name of resource.
 	 * @return {@link Validator}
 	 */
 	public Validator addResource(String name) {
 		Validator res = new Validator();
 		if (getFile().isDirectory()) {
 			if (access.includes(AccessRight.VIEW_FULL)) {
 				ResourceKind successor = getSuccessorKind();
 				if (successor != null) {
 					res = ResourceUtil.createResource(getResourceDirectory(),
 							name, successor);
 				}
 			} else {
 				res.addError("access", "No permission to add resource.");
 			}
 		} else {
 			res.addError("access", "Parent resource does not exist.");
 		}
 		return res;
 	}
 
 	/**
 	 * Updates the resource, associated with this node.
 	 * 
 	 * @param oldValues
 	 *            The old values of the resource.
 	 * @param newValues
 	 *            The new values of the resource.
 	 * 
 	 * @return {@link Validator}
 	 */
	public Validator updateResource(Map<String, Object> oldValues,
 			Map<String, Object> newValues) {
 		Validator res = new Validator();
 		if (getAccess().includes(AccessRight.VIEW_FULL)) {
 			res = ReflectionUtil.updateObjectProps(resource, oldValues,
 					newValues);
 
 			if (res.isValid()) {
 				resource.save();
 			}
 
 		} else {
 			res.addError("access", "No permission to update resource");
 		}
 		return res;
 
 	}
 
 	/**
 	 * Deletes the node.
 	 * 
 	 * @return {@link Validator}
 	 */
 	public Validator delete() {
 		Validator res = new Validator();
 		Node parent = getParent();
 		if (parent.getAccess().includes(AccessRight.VIEW_FULL)) {
 			boolean success = FileUtil.delete(getFile());
 			if (!success) {
 				res.addError("delete", "Unable to delete resource");
 			}
 		} else {
 			res.addError("access", "No permission to delete resource");
 		}
 		return res;
 	}
 
 	/**
 	 * Loads the list of all visible resources that a children to the current.
 	 * 
 	 * @return List of visible resources.
 	 */
 	public List<Resource> loadChildResources() {
 		List<Resource> res = new ArrayList<Resource>();
 		if (getSuccessorKind() != null) {
 			List<Resource> list = new ArrayList<Resource>();
 			if (isRoot()) {
 				list = ResourceUtil.loadChildResources(getResourceDirectory(),
 						ResourceKind.SERIES);
 			} else if (resource != null) {
 				list = resource.loadChildren();
 			}
 			for (Resource resource : list) {
 				File directory = new File(getResourceDirectory(),
 						resource.getId());
 				AccessRight access = ResourceUtil.getAccessRight(seriesRoot,
 						directory, login);
 				if (access.includes(AccessRight.LIST)) {
 					res.add(resource);
 				}
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Validates the resource associated with this node.
 	 * 
 	 * @return {@link Validator}
 	 */
 	public Validator validateResource() {
 		Validator res = new Validator();
 		if (getAccess().includes(AccessRight.VIEW_FULL)) {
 			res = getResouce().validate();
 		} else {
 			res.addError("access", "No permission to validate resource.");
 		}
 		return res;
 	}
 
 	/**
 	 * Splits the given path into parts, ignoring empty parts.
 	 * 
 	 * @param path
 	 *            The given path.
 	 * @return List, containing parts.
 	 */
 	public static List<String> splitPath(String path) {
 		List<String> res = new ArrayList<String>();
 		String[] split = path.split(PATH_SEPARATOR);
 		for (String part : split) {
 			if (!part.isEmpty()) {
 				res.add(part);
 			}
 		}
 		return res;
 	}
 }
