 package darep.repos;
 
 import java.io.File;
 import java.io.IOException;
 
 import darep.Command;
 import darep.Command.ActionType;
 import darep.Helper;
 
 /*the repository provides methods to access the Database.class
  * and represents the physical folder located at 'location', which contains the db
  * the repository ensures he correctness of the contents of the db !!!!
  * 
  */
 public class Repository {
 	
 	public static final int prettyPrintColWidth = 20;
 	
 	private static final String DEFAULT_LOCATION = System
 			.getProperty("user.home") + "/.data-repository";
 	
 	/**
 	 * The CANONICAL File which contains the Repository
 	 */
 	private File location;
 	private Database db;
 
 	/*
 	 * loads(/creates) the default (hidden) repo in user.home
 	 */
 	public Repository() throws RepositoryException {
 		this(DEFAULT_LOCATION);
 	}
 
 	/*
 	 * loads(/creates) the repo located at path
 	 * 
 	 * @param path where to find/create repo
 	 */
 	public Repository(String path) throws RepositoryException {
 		try {
 			location = new File(path).getCanonicalFile();
 		} catch (IOException e) {
 			throw new RepositoryException("Could not get canonical File for " +
 					path + ". Error: " + e.getMessage());
 		}
 		initRepository(path);
 	}
 
 	private void initRepository(String path) throws RepositoryException {
 		if (!location.exists()) {
 			if (!location.mkdirs()) {
 				throw new RepositoryException("Tried to create Repository " +
 						location.getPath() + " but failed." );
 			}
 			System.out.println("created new repository " + path);
 		}
 		db = new Database(location.getPath());
 	}
 
 	/*
 	 * adds a dataset to the db, as specified by the options in 'command'
 	 * 
 	 * @param command the command object which stores the options
 	 */
 	public void add(Command command) throws RepositoryException {
 		
 		try {
 
 			File file = getInputFile(command);
 			if (!file.exists()) {
 				throw new RepositoryException(file.getCanonicalPath()
 						+ " does not exist.");
 			}
 			if (file.getCanonicalPath().startsWith(location.getCanonicalPath())) {
 				throw new RepositoryException(
 						"Dataset can not contain the repository itself.");
 			}
 	
 			Metadata meta = createMetaData(command);
 			db.add(file, meta, !command.hasFlag("m"));
 		} catch (IOException e) {
 			throw new RepositoryException("There was an IOError while adding: " +
 					e.getMessage());
 		}
 	}
 
 	public boolean delete(Command command) throws RepositoryException {
 		if (db.delete(command.getParams()[0])) {
 			System.out
 					.println("The data set "
 							+ command.getParams()[0]
 							+ " (original name: file/folder name) has been successfully removed from the repository.");
 			return true;
 		} else {
 			throw new RepositoryException("Unknown data set "
 					+ command.getParams()[0]);
 		}
 	}
 
 	/*
 	 * creates a new valid metadata for a new dataset. "valid" means the
 	 * returned Metadata is safe to be entered into the db.
 	 * 
 	 * @ param command
 	 */
 	private Metadata createMetaData(Command command) throws RepositoryException {
 		Metadata meta = new Metadata();
 		try {
 			meta.setOriginalName(getInputFile(command).getName());
 		} catch (IOException e) {
 			throw new RepositoryException("could not set meta data Name" + e.getMessage(),e);
 		}
 
 		if (command.isSet("d"))
 			meta.setDescription(command.getOptionParam("d"));
 
 		String name;
 		if (command.isSet("n")) { // name option set?
 			name = command.getOptionParam("n");
 			if (db.contains(name)) { // if name exists, exit
 				throw new RepositoryException("There is already a data"
 						+ " set named " + name + " name in the repository.");
 			}
 		} else { // no name provided, make a unique name from originalname
 			name = createUniqueName(meta.getOriginalName());
 		}
 		meta.setName(name);
 
 		return meta;
 	}
 
 	private File getInputFile(Command command) throws IOException {
 		// is the input file stored in the second or first parameter?
 		File file;
 		if (command.getAction() == ActionType.replace) {
 			file = new File(command.getParams()[1]);
 		} else {
 			file = new File(command.getParams()[0]);
 		}
 		
 		return file.getCanonicalFile();
 	}
 
 	private String createUniqueName(String name) {
 		name = name.toUpperCase();
 		int max = 40;
 		if (name.length() > max)
 			name = name.substring(0, max);
 		if (db.contains(name)) { // if name exists append number
 			int append = 1;
 			if (name.length() == max)
 				name = name.substring(0, max - 1);
 			while (db.contains(name + append)) {
 				append++;
 				if ((name + append).length() > max)
 					name = name.substring(0, name.length() - 1);
 			}
 			name = name + append;
 		}
 		return name;
 	}
 
 	public String getList(Command command) throws RepositoryException {
 		
 		if (command.hasFlag("p")) {
 			return getPrettyList();
 		} else {
 			return getTabList();
 		}
 		
 	}
 	
 	private String getPrettyList() throws RepositoryException {
 		
 		int totalFiles = 0;
 		long totalSize = 0;
 		
 		Dataset[] datasets = db.getAllDatasets();
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append(getHeaderline());
 		
 		for (Dataset dataset: datasets) {
 			sb.append(dataset.getPrettyString());
 			totalFiles += dataset.getMetadata().getNumberOfFiles();
 			totalSize += dataset.getMetadata().getSize();
 		}
 		
 		sb.append("(" + totalFiles + " data sets, ");
 		sb.append(totalSize + " bytes in total)");
 		
 		return sb.toString();
 	}
 
 	private String getTabList() throws RepositoryException {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("Name\t");
		sb.append("Original Name\t");
 		sb.append("Timestamp\t");
		sb.append("Number of Files\t");
 		sb.append("Size\t");
 		sb.append("Description\t");
 		sb.append("\n");
 		
 		for (Dataset dataset : db.getAllDatasets()) {
 			sb.append(dataset + "\n");
 		}
 		return sb.toString();
 	}
 
 	private String getHeaderline() {		
 		StringBuilder sb = new StringBuilder();
 		sb.append("|");
 		sb.append(Helper.stringToLength("Name", Metadata.getMaxNameLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Original Name", Metadata.getMaxOrigNameLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Timestamp", Metadata.getMaxTimestampLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Number of Files", Metadata.getMaxNumFilesLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Size", Metadata.getMaxSizeLength()));
 		sb.append("|");
 		sb.append(Helper.stringToLength("Description", Metadata.getMaxDescriptionLength()));
 		sb.append("|\n");
 		
 		int numDashes = (Metadata.getTotalMaxWidth()) + 7;
 		for (int i = 0; i < numDashes; i++) {
 			sb.append("-");
 		}
 		sb.append("\n");
 		
 		return sb.toString();
 	}
 
 	public void replace(Command command) throws RepositoryException {
 		delete(command);
 		add(command);
 
 		System.out.println("The data set named " + command.getParams()[0]
 				+ " has been successfully"
 				+ " replaced by the file/folder.");
 	}
 
 	public void export(Command command) throws RepositoryException {
 		String canonicalExportPath;
 		try {
 			canonicalExportPath = new File(command.getParams()[1])
 				.getCanonicalPath();
 		} catch (IOException e) {
 			throw new RepositoryException("Could not get Canonical Path for " +
 					command.getParams()[1] + ". Error: " + e.getMessage());
 		}
 		if (canonicalExportPath.contains(location.getPath()))
 			throw new RepositoryException(
 					"It is not allowed to export something into the "
 							+ location.toString() + " repository folder");
 		if (!db.contains(command.getParams()[0]))
 			throw new RepositoryException("Unknown data set "
 					+ command.getParams()[0]);
 		File exportedFile = db.export(command.getParams()[0],
 				command.getParams()[1]);
 
 		System.out
 				.println("The data set " + command.getParams()[0]
 						+ " (original name: " + exportedFile.getName() + ")"
 						+ " has been successfully exported to "
 						+ command.getParams()[1]);
 	}
 
 	protected File getLocation() {
 		return this.location;
 	}
 
 	public static String getDefaultLocation() {
 		return DEFAULT_LOCATION;
 	}
 
 }
