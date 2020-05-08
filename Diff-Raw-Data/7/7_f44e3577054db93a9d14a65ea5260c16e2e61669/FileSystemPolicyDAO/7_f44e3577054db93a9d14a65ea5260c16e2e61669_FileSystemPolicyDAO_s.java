 package org.glite.authz.pap.repository.dao.filesystem;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.glite.authz.pap.common.xacml.PolicyTypeString;
 import org.glite.authz.pap.common.xacml.TypeStringUtils;
 import org.glite.authz.pap.common.xacml.utils.PolicyHelper;
 import org.glite.authz.pap.repository.dao.PolicyDAO;
 import org.glite.authz.pap.repository.exceptions.AlreadyExistsException;
 import org.glite.authz.pap.repository.exceptions.NotFoundException;
 import org.glite.authz.pap.repository.exceptions.RepositoryException;
 import org.opensaml.xacml.policy.PolicyType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FileSystemPolicyDAO implements PolicyDAO {
 
     private static final Map<String, Map<String, PolicyTypeString>> cache = new ConcurrentHashMap<String, Map<String, PolicyTypeString>>();
     private static final String FILE_EXT = FileSystemRepositoryManager.getFileNameExt();
     private static FileSystemPolicyDAO instance = null;
     private static final Logger log = LoggerFactory.getLogger(FileSystemPolicyDAO.class);
     private static final String POLICY_FILE_NAME_PREFIX = FileSystemRepositoryManager.getPolicyFileNamePrefix();
     private static final PolicyHelper policyHelper = PolicyHelper.getInstance();
 
     private FileSystemPolicyDAO() {}
 
     public static FileSystemPolicyDAO getInstance() {
         if (instance == null) {
             instance = new FileSystemPolicyDAO();
         }
         return instance;
     }
 
     private static String getPolicyFileAbsolutePath(String papId, String policyId) {
         return FileSystemRepositoryManager.getPAPDirAbsolutePath(papId) + getPolicyFileName(policyId);
     }
 
     private static String getPolicyFileName(String policyId) {
         return POLICY_FILE_NAME_PREFIX + policyId + FILE_EXT;
     }
 
     private static String getPolicyIdFromFileName(String fileName) {
         int start = POLICY_FILE_NAME_PREFIX.length();
         int end = fileName.length() - FILE_EXT.length();
         return fileName.substring(start, end);
     }
 
     private static String papDirNotFoundExceptionMsg(String papDirPAth) {
         return "Not found PAP directory: " + papDirPAth;
     }
 
     // TODO: maybe it's better to create different exception classes instead of
     // different exception messages
     private static String policyExceptionMsg(String policyId) {
         return String.format("policyId=\"%s\"", policyId);
     }
 
     private static String policyNotFoundExceptionMsg(String policyId) {
         String msg = "Not found: " + policyExceptionMsg(policyId);
         return msg;
     }
 
     public synchronized void delete(String papId, String policyId) {
 
         Map<String, PolicyTypeString> papCache = cache.get(papId);
 
         if (papCache != null) {
             papCache.remove(policyId);
         }
 
         String policyFileName = getPolicyFileAbsolutePath(papId, policyId);
 
         if (exists(papId, policyId)) {
 
             File policyFile = new File(policyFileName);
 
             if (papCache.size() == 0) {
                 cache.remove(papId);
             }
 
             if (!policyFile.delete()) {
                 throw new RepositoryException("Cannot delete file: " + policyFile.getAbsolutePath());
             }
 
         } else {
             throw new NotFoundException(policyNotFoundExceptionMsg(policyId));
         }
     }
 
     public synchronized int deleteAll(String papId) {
 
         Map<String, PolicyTypeString> papCache = cache.get(papId);
 
         if (papCache != null) {
             papCache.clear();
             cache.remove(papId);
         }
 
         String papDirAbsolutePath = FileSystemRepositoryManager.getPAPDirAbsolutePath(papId);
 
         File papDir = new File(papDirAbsolutePath);
 
         if (!papDir.exists()) {
             throw new RepositoryException(papDirNotFoundExceptionMsg(papDirAbsolutePath));
         }
 
         int numOfDeletedPolicies = 0;
 
         for (File file : papDir.listFiles()) {
 
             if (file.isDirectory()) {
                 continue;
             }
 
             String fileName = file.getName();
             if (fileName.startsWith(POLICY_FILE_NAME_PREFIX)) {
                 file.delete();
                 numOfDeletedPolicies++;
             }
         }
         return numOfDeletedPolicies;
     }
 
     public synchronized boolean exists(String papId, String policyId) {
 
         String policyFilePath = getPolicyFileAbsolutePath(papId, policyId);
 
         File policyFile = new File(policyFilePath);
 
         return policyFile.exists();
     }
 
     public synchronized List<PolicyType> getAll(String papId) {
 
         File papDir = new File(FileSystemRepositoryManager.getPAPDirAbsolutePath(papId));
 
         if (!papDir.exists()) {
             throw new RepositoryException(papDirNotFoundExceptionMsg(papDir.getAbsolutePath()));
         }
 
         Map<String, PolicyTypeString> papCache = getPAPCache(papId);
 
         List<PolicyType> policyList = new LinkedList<PolicyType>();
 
         for (File file : papDir.listFiles()) {
 
             if (file.isDirectory()) {
                 continue;
             }
 
             String fileName = file.getName();
 
             if (fileName.startsWith(POLICY_FILE_NAME_PREFIX)) {
 
                 String policyId = getPolicyIdFromFileName(fileName);
                 
                 PolicyTypeString policy = papCache.get(policyId);
 
                 if (policy == null) {
                     try {
                         policy = new PolicyTypeString(policyId, policyHelper.readFromFileAsString(file));
                     } catch (Throwable e) {
                         throw new RepositoryException(e);
                     }
                     papCache.put(policyId, policy);
                 }
                 policyList.add(new PolicyTypeString(policyId, policy.getPolicyString()));
                 
                 if (policy.isDOMLoaded()) {
                     log.warn("DOM not released for Policy id=" + policyId);
                 }
             }
         }
         return policyList;
     }
 
     public synchronized PolicyType getById(String papId, String policyId) {
 
         Map<String, PolicyTypeString> papCache = getPAPCache(papId);
 
         PolicyTypeString policy = papCache.get(policyId);
 
         if (policy == null) {
 
             File policyFile = new File(getPolicyFileAbsolutePath(papId, policyId));
 
             if (!policyFile.exists()) {
                 if (papCache.size() == 0) {
                     cache.remove(papId);
                 }
                 throw new NotFoundException(policyNotFoundExceptionMsg(policyId));
             }
 
             try {
                 policy = new PolicyTypeString(policyId, policyHelper.readFromFileAsString(policyFile));
             } catch (Throwable e) {
                 throw new RepositoryException(e);
             }
 
             papCache.put(policyId, policy);
         }
 
         if (policy.isDOMLoaded()) {
             log.warn("DOM not released for id=" + policyId);
         }
         return new PolicyTypeString(policyId, policy.getPolicyString());
     }
 
     public int getNumberOfPolicies(String papId) {
 
         File papDir = new File(FileSystemRepositoryManager.getPAPDirAbsolutePath(papId));
 
         if (!papDir.exists()) {
             return 0;
         }
 
         int numOfPolicies = 0;
 
         for (File file : papDir.listFiles()) {
 
             if (file.isDirectory()) {
                 continue;
             }
 
             if (file.getName().startsWith(POLICY_FILE_NAME_PREFIX)) {
                 numOfPolicies++;
             }
         }
         return numOfPolicies;
     }
 
     public synchronized void store(String papId, PolicyType policy) {
 
         PolicyTypeString policyTypeString = TypeStringUtils.cloneAsPolicyTypeString(policy);
 
         File papDir = new File(FileSystemRepositoryManager.getPAPDirAbsolutePath(papId));
 
         if (!papDir.exists()) {
             throw new RepositoryException(papDirNotFoundExceptionMsg(papDir.getAbsolutePath()));
         }
 
         String policyId = policyTypeString.getPolicyId();
         
 
         File policyFile = new File(getPolicyFileAbsolutePath(papId, policyId));
 
         if (policyFile.exists()) {
             throw new AlreadyExistsException("Already exists: policyId=" + policyId);
         }
 
         PolicyHelper.toFile(policyFile, policyTypeString);
 
         getPAPCache(papId).put(policyId, policyTypeString);
 
         TypeStringUtils.releaseUnneededMemory(policyTypeString);
         
         log.debug("Stored policy: " + policyId);
     }
 
     public synchronized void update(String papId, String policyVersion, PolicyType newPolicy) {
 
         PolicyTypeString newPolicyTypeString = TypeStringUtils.cloneAsPolicyTypeString(newPolicy);
 
        String policyId = newPolicy.getPolicyId();
 
         File policyFile = new File(getPolicyFileAbsolutePath(papId, policyId));
         if (!policyFile.exists()) {
             throw new NotFoundException(policyNotFoundExceptionMsg(policyId));
         }
 
         Map<String, PolicyTypeString> papCache = getPAPCache(papId);
 
         PolicyTypeString oldPolicy = papCache.get(policyId);
 
         if (oldPolicy == null) {
             try {
                 oldPolicy = new PolicyTypeString(policyHelper.buildFromFile(policyFile));
             } catch (Throwable e) {
                 throw new RepositoryException(e);
             }
         }
 
         if (!(oldPolicy.getVersion().equals(policyVersion))) {
             throw new RepositoryException(
                 String.format("Attempting to update the wrong version of PolicyId=\"%s\" (requestedVersion=\"%s\", repositoryVersion=\"%s\")",
                               policyId,
                               policyVersion,
                               oldPolicy.getVersion()));
         }
 
         TypeStringUtils.releaseUnneededMemory(oldPolicy);
 
         PolicyHelper.toFile(policyFile, newPolicyTypeString);
 
         papCache.put(policyId, newPolicyTypeString);
 
         TypeStringUtils.releaseUnneededMemory(newPolicyTypeString);
     }
 
     private Map<String, PolicyTypeString> getPAPCache(String papId) {
         Map<String, PolicyTypeString> papCache = cache.get(papId);
 
         if (papCache == null) {
             log.debug("New HashMap for papId=" + papId);
             papCache = new ConcurrentHashMap<String, PolicyTypeString>();
             cache.put(papId, papCache);
         }
         return papCache;
     }
 }
