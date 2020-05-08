 package com.bhaweb.maven.plugin.artifactory;
 
 import java.lang.reflect.Type;
 import java.net.URI;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
import java.util.Arrays;
 import java.util.Date;
 
 /**
  * @author Dan Rollo
  * Date: 11/11/11
  * Time: 12:02 AM
  */
 public class RestAPI {
 
     public static enum OPERATION {
         LIST("api/storage", "list", FolderInfo.class),
         GETREPOSITORIES("api/repositories", null, //"repositories",
                 RepoInfo[].class);
 
         private final String apiURL;
         private final String opName;
         private final Type returnType;
 
         OPERATION(final String apiURL, final String operationName, final Type returnType) {
             this.apiURL = apiURL;
             this.opName = operationName;
             this.returnType = returnType;
         }
 
         public String getApiURL() { return apiURL; }
         public String getOpName() { return opName; }
         public Type getReturnType() { return returnType; }
     }
 
 
 
     public static final class FileInfo {
         public URI getUri() {
             return uri;
         }
 
         public long getSize() {
             return size;
         }
 
         public String getLastModified() {
             return lastModified;
         }
 
         public Date getLastModifiedDate() throws ParseException {
             return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ").parse(lastModified);
         }
 
         public boolean isFolder() {
             return folder;
         }
 
         public String getSha1() {
             return sha1;
         }
 
         private URI uri;
         private long size;
         //private Date lastModified;
         private String lastModified;
         private boolean folder;
         private String sha1;
 
         @Override
         public String toString() {
             return "uri: " + uri
                     + ", size: " + size
                     + ", lastModified: " + lastModified
                     + ", folder: " + folder
                     + ", sha1: " + sha1;
         }
     }
 
     public static final class FolderInfo {
 
         private FileInfo[] files;
 
         public FileInfo[] getFiles() {
            return Arrays.copyOf(files, files.length);
         }
 
         public long getTotalSizeMB() {
             long totalSize = 0;
             for (final FileInfo fileInfo : files) {
                 totalSize += fileInfo.size;
             }
             return totalSize / 1048576;
         }
 
         @Override
         public String toString() {
             return "files (count: " + files.length + ", totalSize: " + getTotalSizeMB() + "mb): "
                     //+ Arrays.asList(files)
                     ;
         }
     }
 
 
     // [{"key":"libs-releases-local","type":"LOCAL","description":"Local repository for in-house libraries","url":"http://repository.myco.com/mycompany/libs-releases-local"},{"key":"libs-snapshots-local","type":"LOCAL","description":"Local repository for in-house snapshots","url":"http://repository.myco.com/mycompany/libs-snapshots-local"},{"key":"plugins-releases-local","type":"LOCAL","description":"Local repository for plugins","url":"http://repository.myco.com/mycompany/plugins-releases-local"},{"key":"plugins-snapshots-local","type":"LOCAL","description":"Local repository for plugins snapshots","url":"http://repository.myco.com/mycompany/plugins-snapshots-local"},{"key":"ext-releases-local","type":"LOCAL","description":"Local repository for third party libraries","url":"http://repository.myco.com/mycompany/ext-releases-local"},{"key":"ext-snapshots-local","type":"LOCAL","description":"Local repository for third party snapshots","url":"http://repository.myco.com/mycompany/ext-snapshots-local"},{"key":"repo1","type":"REMOTE","description":"Central Repo1","url":"http://repo-demo.jfrog.org/artifactory/repo1"},{"key":"MavenRepo2","type":"REMOTE","url":"http://repo2.maven.org/maven2"},{"key":"flex-mojos-repository","type":"REMOTE","url":"http://repository.sonatype.org/content/groups/flexgroup/"},{"key":"spring-maven","type":"REMOTE","description":"Spring Maven Repository","url":"http://maven.springframework.org/"},{"key":"repo2","type":"REMOTE","url":"http://repo1.maven.org/maven2/"},{"key":"repo","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/repo"},{"key":"remote-repos","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/remote-repos"},{"key":"remote-snapshot-repos","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/remote-snapshot-repos"},{"key":"libs-releases","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/libs-releases"},{"key":"plugins-releases","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/plugins-releases"},{"key":"libs-snapshots","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/libs-snapshots"},{"key":"plugins-snapshots","type":"VIRTUAL","url":"http://repository.myco.com/mycompany/plugins-snapshots"}] given the type class java.lang.String
     public static final class RepoInfo {
 
         public String getKey() {
             return key;
         }
 
         public String getType() {
             return type;
         }
 
         public String getUrl() {
             return url;
         }
 
         private String key;
         private String type;
         private String description;
         private String url;
 
         @Override
         public String toString() {
             return "Repo key: " + key
                     + ", type: " + type
                     + ", description: " + description
                     + ", url: " + url;
         }
 
     }
 }
