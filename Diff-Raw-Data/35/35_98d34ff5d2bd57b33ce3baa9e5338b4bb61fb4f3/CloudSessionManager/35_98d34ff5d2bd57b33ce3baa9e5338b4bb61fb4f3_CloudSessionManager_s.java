 package org.bioinfo.gcsa.lib.account;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import org.bioinfo.commons.log.Logger;
 import org.bioinfo.commons.utils.StringUtils;
 import org.bioinfo.gcsa.lib.account.beans.Acl;
 import org.bioinfo.gcsa.lib.account.beans.AnalysisPlugin;
 import org.bioinfo.gcsa.lib.account.beans.Bucket;
 import org.bioinfo.gcsa.lib.account.beans.Job;
 import org.bioinfo.gcsa.lib.account.beans.ObjectItem;
 import org.bioinfo.gcsa.lib.account.beans.Session;
 import org.bioinfo.gcsa.lib.account.db.AccountFileManager;
 import org.bioinfo.gcsa.lib.account.db.AccountManagementException;
 import org.bioinfo.gcsa.lib.account.db.AccountManager;
 import org.bioinfo.gcsa.lib.account.db.AccountMongoDBManager;
 import org.bioinfo.gcsa.lib.account.io.FileIOManager;
 import org.bioinfo.gcsa.lib.account.io.IOManagementException;
 import org.bioinfo.gcsa.lib.storage.alignment.BamManager;
 import org.bioinfo.infrared.lib.common.Region;
 import org.dom4j.DocumentException;
 
 public class CloudSessionManager {
 
 	private AccountManager accountManager;
 	private FileIOManager ioManager;
 
 	private Logger logger;
 	private Properties properties;
 
 	public CloudSessionManager() throws FileNotFoundException, IOException, AccountManagementException {
 		this(System.getenv("GCSA_HOME"));
 	}
 
 	public CloudSessionManager(String gcsaHome) throws FileNotFoundException, IOException, AccountManagementException {
 		logger = new Logger();
 		properties = new Properties();
 
 		File propertiesFile = new File(gcsaHome + "/conf/account.properties");
 		if (gcsaHome != null && propertiesFile.exists()) {
 			properties.load(new FileInputStream(propertiesFile));
 			if (properties.getProperty("GCSA.ACCOUNT.MODE").equals("file")) {
 				accountManager = (AccountManager) new AccountFileManager(properties);// TODO
 			} else {
 				accountManager = new AccountMongoDBManager(properties);
 			}
 			ioManager = new FileIOManager(properties);
 
 			logger.info(properties.toString());
 		} else {
 			logger.error("properties file not found");
 		}
 	}
 
 	/**
 	 * @throws IOManagementException
 	 *****************************/
 	public void createAccount(String accountId, String password, String name, String email, String sessionIp)
 			throws AccountManagementException, IOManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(password, "password");
 		checkParameter(name, "name");
 		checkEmail(email);
 		checkParameter(sessionIp, "sessionIp");
 		Session session = new Session(sessionIp);
 
 		ioManager.createAccount(accountId);
 
 		try {
 			accountManager.createAccount(accountId, password, name, email, session);
 		} catch (AccountManagementException e) {
 			ioManager.deleteAccount(accountId);
 			throw e;
 		}
 
 	}
 
 	public String createAnonymousAccount(String sessionIp) throws AccountManagementException, IOManagementException {
 		checkParameter(sessionIp, "sessionIp");
 		Session session = new Session(sessionIp);
 
 		String password = StringUtils.randomString(10);
 		String accountId = "anonymous_" + password;
 
 		ioManager.createAccount(accountId);
 		try {
 			return accountManager.createAnonymousAccount(accountId, password, session);
 		} catch (AccountManagementException e) {
 			ioManager.deleteAccount(accountId);
 			throw e;
 		}
 
 	}
 
 	public String login(String accountId, String password, String sessionIp) throws AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(password, "password");
 		checkParameter(sessionIp, "sessionIp");
 		Session session = new Session(sessionIp);
 		return accountManager.login(accountId, password, session);
 	}
 
 	public void logout(String accountId, String sessionId) throws AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		accountManager.logout(accountId, sessionId);
 	}
 
 	public void logoutAnonymous(String sessionId) throws AccountManagementException, IOManagementException {
 		String accountId = "anonymous_" + sessionId;
 		System.out.println("-----> el accountId del anonimo es: " + accountId + " y la sesionId: " + sessionId);
 
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 
 		// TODO check inconsistency
 		ioManager.deleteAccount(accountId);
 		accountManager.logoutAnonymous(accountId, sessionId);
 	}
 
 	public void changePassword(String accountId, String sessionId, String password, String nPassword1, String nPassword2)
 			throws AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		checkParameter(password, "password");
 		checkParameter(nPassword1, "nPassword1");
 		checkParameter(nPassword2, "nPassword2");
 		if (!nPassword1.equals(nPassword2)) {
 			throw new AccountManagementException("the new pass is not the same in both fields");
 		}
 		accountManager.changePassword(accountId, sessionId, password, nPassword1, nPassword2);
 	}
 
 	public void changeEmail(String accountId, String sessionId, String nEmail) throws AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		checkEmail(nEmail);
 		accountManager.changeEmail(accountId, sessionId, nEmail);
 	}
 
 	public void resetPassword(String accountId, String email) throws AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkEmail(email);
 		accountManager.resetPassword(accountId, email);
 	}
 
 	public String getAccountInfo(String accountId, String sessionId, String lastActivity)
 			throws AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		// lastActivity can be null
 		return accountManager.getAccountInfo(accountId, sessionId, lastActivity);
 	}
 
 	public void deleteAccount(String accountId, String sessionId) throws AccountManagementException,
 			IOManagementException {
 		// TODO
 	}
 
 	public Path getAccountPath(String accountId) {
 		return ioManager.getAccountPath(accountId);
 	}
 
 	public Path getBucketPath(String accountId, String bucketId) {
 		return ioManager.getBucketPath(accountId, bucketId);
 	}
 
 	public String getObjectPath(String accountId, String bucketId, Path ObjectId) {
 		return ioManager.getObjectPath(accountId, bucketId, ObjectId).toString();
 	}
 
 	public void createBucket(String accountId, Bucket bucket, String sessionId) throws AccountManagementException,
 			IOManagementException {
 		checkParameter(bucket.getName(), "bucketName");
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 
 		ioManager.createBucket(accountId, bucket.getName());
 		try {
 			accountManager.createBucket(accountId, bucket, sessionId);
 		} catch (AccountManagementException e) {
 			ioManager.deleteBucket(accountId, bucket.getName());
 			throw e;
 		}
 	}
 
 	public String createObjectToBucket(String accountId, String bucketId, Path objectId, ObjectItem objectItem,
 			InputStream fileIs, boolean parents, String sessionId) throws AccountManagementException,
 			IOManagementException, IOException {
 		checkParameter(bucketId, "bucket");
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		checkParameter(objectId.toString(), "objectId");
 		checkObj(objectItem, "objectItem");
 
 		objectId = ioManager.createObject(accountId, bucketId, objectId, objectItem, fileIs, parents);
 
 		// set id and name to the itemObject
 		objectItem.setId(objectId.toString());
 		objectItem.setFileName(objectId.getFileName().toString());
 
 		try {
 			accountManager.createObjectToBucket(accountId, bucketId, objectItem, sessionId);
 			return objectId.toString();
 		} catch (AccountManagementException e) {
 			ioManager.deleteObject(accountId, bucketId, objectId);
 			throw e;
 		}
 	}
 
 	public String createFolderToBucket(String accountId, String bucketId, Path objectId, ObjectItem objectItem,
 			boolean parents, String sessionId) throws AccountManagementException, IOManagementException {
 		checkParameter(bucketId, "bucket");
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		checkParameter(objectId.toString(), "objectId");
 		checkObj(objectItem, "objectItem");
 
 		ioManager.createFolder(accountId, bucketId, objectId, parents);
 
 		// set id and name to the itemObject
 		objectItem.setId(objectId.toString());
 		objectItem.setFileName(objectId.getFileName().toString());
 
 		try {
 			accountManager.createObjectToBucket(accountId, bucketId, objectItem, sessionId);
 			return objectId.toString();
 		} catch (AccountManagementException e) {
 			ioManager.deleteObject(bucketId, accountId, objectId);
 			throw e;
 		}
 	}
 
 	public void deleteDataFromBucket(String accountId, String bucketId, Path objectId, String sessionId)
 			throws AccountManagementException, IOManagementException {
 		checkParameter(bucketId, "bucket");
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		checkParameter(objectId.toString(), "objectId");
 
 		objectId = ioManager.deleteObject(accountId, bucketId, objectId);
 		accountManager.deleteObjectFromBucket(accountId, bucketId, objectId, sessionId);
 
 	}
 
 	public void shareObject(String accountId, String bucketId, Path objectId, String toAccountId, boolean read,
 			boolean write, boolean execute, String sessionId) throws AccountManagementException {
 		checkParameters(accountId, "accountId", bucketId, "bucketId", objectId.toString(), "objectId", toAccountId,
 				"toAccountId", sessionId, "sessionId");
 		
 		Acl acl = new Acl(toAccountId, "", read, write, execute);
 		accountManager.shareObject(accountId, bucketId, objectId, acl, sessionId);
 	}
 
 	public String checkJobStatus(String accountId, String jobId, String sessionId) throws AccountManagementException {
 		Path jobPath = getAccountPath(accountId).resolve(accountManager.getJobPath(accountId, jobId));
 		if (Files.exists(jobPath.resolve("result.xml"))) {
 			accountManager.incJobVisites(accountId, jobId);
 			return "DONE";
 		}
 		return "RUNNING";
 	}
 
 	public String region(String accountId, String bucketId, Path objectId, String regionStr,
 			Map<String, List<String>> params, String sessionId) throws AccountManagementException,
 			IOManagementException, IOException {
 
 		checkParameter(bucketId, "bucket");
 		checkParameter(accountId, "accountId");
 		checkParameter(sessionId, "sessionId");
 		checkParameter(objectId.toString(), "objectId");
 		checkParameter(regionStr, "regionStr");
 		Region region = Region.parseRegion(regionStr);
 		checkObj(region, "region");
 
 		Path fullFilePath = ioManager.getObjectPath(accountId, bucketId, objectId);
 		ObjectItem objectItem = accountManager.getObjectFromBucket(accountId, bucketId, objectId, sessionId);
 
 		String result = "";
 		switch (objectItem.getFileFormat()) {
 		case "bam":
 			BamManager bamManager = new BamManager();
 			result = bamManager.getByRegion(fullFilePath, region.getChromosome(), region.getStart(), region.getEnd(),
 					params);
 			break;
 		}
 
 		return result;
 	}
 
 	public String getJobResult(String accountId, String jobId) throws IOException, DocumentException,
 			IOManagementException, AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(jobId, "jobId");
 
 		Path jobPath = getAccountPath(accountId).resolve(accountManager.getJobPath(accountId, jobId));
 
 		return ioManager.getJobResult(jobPath);
 	}
 
 	public String getFileTableFromJob(String accountId, String jobId, String filename, String start, String limit,
 			String colNames, String colVisibility, String callback, String sort) throws IOManagementException,
 			IOException, AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(jobId, "jobId");
 		checkParameter(filename, "filename");
 
 		Path jobPath = getAccountPath(accountId).resolve(accountManager.getJobPath(accountId, jobId));
 
 		return ioManager.getFileTableFromJob(jobPath, filename, start, limit, colNames, colVisibility, callback, sort);
 	}
 
 	public DataInputStream getFileFromJob(String accountId, String jobId, String filename, String zip)
 			throws IOManagementException, IOException, AccountManagementException {
 		checkParameter(accountId, "accountId");
 		checkParameter(jobId, "jobId");
 		checkParameter(filename, "filename");
 		checkParameter(zip, "zip");
 
 		Path jobPath = getAccountPath(accountId).resolve(accountManager.getJobPath(accountId, jobId));
 
 		return ioManager.getFileFromJob(jobPath, filename, zip);
 	}
 
 	public String getAccountBuckets(String accountId, String sessionId) throws AccountManagementException {
 		return accountManager.getAllBucketsBySessionId(accountId, sessionId);
 	}
 
 	public String createJob(String jobName, String jobFolder, String toolName, List<String> dataList,
 			String commandLine, String sessionId) throws AccountManagementException, IOManagementException {
 
 		checkParameter(jobName, "jobName");
 		checkParameter(toolName, "toolName");
 		checkParameter(sessionId, "sessionId");
 		String accountId = accountManager.getAccountIdBySessionId(sessionId);
 
 		String jobId = StringUtils.randomString(15);
 		boolean jobFolderCreated = false;
 
 		if (jobFolder == null) {
 			ioManager.createJob(accountId, jobId);
 			jobFolder = Paths.get("jobs").resolve(jobId).toString();
 			jobFolderCreated = true;
 		}
 		checkParameter(jobFolder, "jobFolder");
 
 		Job job = new Job(jobId, jobName, jobFolder, toolName, Job.QUEUED, commandLine, "", dataList);

 		try {
 			accountManager.createJob(accountId, job, sessionId);
 		} catch (AccountManagementException e) {
 			if (jobFolderCreated) {
 				ioManager.removeJob(accountId, jobId);
 			}
 			throw e;
 		}
 
 		return jobId;
 	}
 
 	public String getJobFolder(String accountId, String jobId) {
 		return ioManager.getJobPath(accountId, null, jobId).toString();
 	}
 
 	public List<AnalysisPlugin> getUserAnalysis(String sessionId) throws AccountManagementException {
 		return accountManager.getUserAnalysis(sessionId);
 	}
 
 	public void setJobCommandLine(String accountId, String jobId, String commandLine) throws AccountManagementException {
		accountManager.setJobCommandLine(accountId, jobId, commandLine);
 	}
 
 	/********************/
 	private void checkEmail(String email) throws AccountManagementException {
 		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
 				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 		if (!pattern.matcher(email).matches()) {
 			throw new AccountManagementException("email not valid");
 		}
 	}
 
 	private void checkParameter(String param, String name) throws AccountManagementException {
 		if (param == null || param.equals("") || param.equals("null")) {
 			throw new AccountManagementException("Error in parameter: parameter '" + name + "' is null or empty: "
 					+ param + ".");
 		}
 	}
 
 	private void checkParameters(String... args) throws AccountManagementException {
 		if (args.length % 2 == 0) {
 			for (int i = 0; i < args.length; i += 2) {
 				checkParameter(args[i], args[i + 1]);
 			}
 		} else {
 			throw new AccountManagementException("Error in parameter: parameter list is not multiple of 2");
 		}
 	}
 
 	private void checkObj(Object obj, String name) throws AccountManagementException {
 		if (obj == null) {
 			throw new AccountManagementException("parameter '" + name + "' is null.");
 		}
 	}
 }
