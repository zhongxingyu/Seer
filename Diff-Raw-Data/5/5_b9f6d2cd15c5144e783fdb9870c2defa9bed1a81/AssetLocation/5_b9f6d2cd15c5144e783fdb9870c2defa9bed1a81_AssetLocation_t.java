 /**
  * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
  * details.
  */
 package com.razie.pub.assets;
 
 import java.io.File;
 import java.io.Serializable;
 import java.net.URL;
 
 import com.razie.pub.FileUtils;
 import com.razie.pub.comms.AgentHandle;
 import com.razie.pub.comms.Agents;
 import com.razie.pub.comms.Comms;
 
 /**
  * the location of an asset, either a remote url like below or a directory.
  * 
  * a location is always on the form: protocol://host:portPATH
  * 
  * for mutant, the format is mutant://host:port::PATH
  * 
  * for others it's http://host:port/url
  * 
  * <p>
  * inspired from OSS/J's application environment, highly simplified.
  * 
  * @author razvanc99
  */
 @SuppressWarnings("serial")
 public class AssetLocation implements Serializable {
 
    protected String remoteUrl = null;
    protected String localPath = null;
 
    public AssetLocation() {
    }
 
    /**
     * pretty smart extraction of NewAppEnv from whatever you can thorw my way.
     * 
     * @see AssetKey.NewAppEnv
     */
    public AssetLocation(String o) {
       this.setURL(o);
    }
 
    public AssetLocation(String protocol, String host, String port, String path) {
       if ("http".equals(protocol)) {
          this.remoteUrl = "http://" + host + ":" + port + path;
       } else if ("mutant".equals(protocol)) {
          this.remoteUrl = "mutant://" + host + ":" + port + "::" + path;
       }
    }
 
    /** points to a mutant location */
    public boolean isMutant() {
       return this.remoteUrl != null && this.remoteUrl.startsWith("mutant://");
    }
 
    /** returns true if this NewAppEnv points to a local directory */
    public boolean isLocal() {
       if (isMutant()) {
          return this.getHost().equals(Agents.getMyHostName()) || "local".equals(this.getHost())
                || Comms.isLocalhost(this.getHost());
       } else {
          return this.localPath != null && this.localPath.length() > 0;
       }
    }
 
    /** returns true if this points to a remote server */
    public boolean isRemote() {
       if (isMutant()) {
          return !isLocal();
       } else {
          return this.remoteUrl != null && this.remoteUrl.length() > 0;
       }
    }
 
    @Override
    public String toString() {
       return this.remoteUrl != null ? this.remoteUrl : this.localPath;
    }
 
    /**
     * make an http URL, if remote. Normally the remote reference is in Weblogic's t3:// format. This
     * will convert it to http://
     */
    public String toHttp() {
       if (isMutant()) {
          return "http://" + this.getHost() + ":" + this.getPort();
       }
       return this.remoteUrl;
    }
 
    /** like a clone */
    public AssetLocation copy(AssetLocation newEnv) {
       if (newEnv != null) {
          this.localPath = newEnv.localPath;
          this.remoteUrl = newEnv.remoteUrl;
       }
       return this;
    }
 
    /** actually clone */
    @Override
    public Object clone() {
       AssetLocation a = new AssetLocation();
       a.localPath = this.localPath;
       a.remoteUrl = this.remoteUrl;
       return a;
    }
 
    /** smart setting of the actual URL */
    public void setURL(String url) throws IllegalArgumentException {
       if (url == null) {
          this.remoteUrl = null;
       } else {
          // it's an URL. factory is then the default, I take it?
          if (url.indexOf("mutant:") >= 0) {
             // format: mutant://computer:port::localpath
             this.remoteUrl = url;
             this.localPath = null;
          } else if (url.indexOf("http:") >= 0) {
             this.remoteUrl = url;
             this.localPath = null;
          } else {
             setLocalPath(url);
             // this is a local PATH, need to make sure it ends with a "/" - all other code will
             // simply concatenate file names to it
             if (this.localPath != null && !"".equals(this.localPath) && !this.localPath.endsWith("/")
                   && !this.localPath.endsWith("\\")) {
                this.localPath += "/";
             }
          }
 
          // TODO not sure why i do this
          if (!isMutant() && this.remoteUrl != null && this.remoteUrl.endsWith("/")) {
             this.remoteUrl = this.remoteUrl.substring(0, this.remoteUrl.length() - 1);
          }
       }
    }
 
    public String getLocalPath() {
       if (this.remoteUrl != null) {
          if (this.remoteUrl.contains("::")) {
             String[] sp = this.remoteUrl.split("::");
             return sp.length > 1 ? sp[1] : null;
          }
          return null;
       } else
          return this.localPath;
    }
 
    /** will get canonic path unless the path is in the classpath */
    public void setLocalPath(String lp) {
       if (lp != null) {
          this.localPath = lp.startsWith("jar:") ? lp : FileUtils.toCanonicalPath(lp);
       }
    }
 
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
       final int PRIME = 31;
       // int result = super.hashCode();
       int result = 1;
       result = PRIME * result + ((localPath == null) ? 0 : localPath.hashCode());
       result = PRIME * result + ((remoteUrl == null) ? 0 : remoteUrl.hashCode());
       return result;
    }
 
    /** turn into a URL. If looking for files local or classpath, use toUrl (fileName) */
    public URL toURL() {
       URL url = null;
 
       try {
          if (this.isLocal()) {
             File f = new File(this.localPath);
             if (f.exists()) {
                url = f.getCanonicalFile().toURL();
             }
          } else {
             url = new URL(this.remoteUrl);
          }
       } catch (Exception e) {
          throw new IllegalStateException("can't turn into URL, NewAppEnv=" + this.toString(), e);
       }
       return url;
    }
 
    /**
     * toUrl when this NewAppEnv localpath contains the directory and the parameter is the actual
     * filename. For remote appoEnv, the fileName is ignored
     */
    public URL toURL(String fileName) {
       URL url = null;
 
       try {
          if (this.isLocal()) {
             // treat classpath url's differently
             if (this.localPath.startsWith("jar:")) {
                url = new URL(this.localPath + fileName);
             } else {
                File f = new File(this.localPath + fileName);
                if (f.exists()) {
                   url = f.getCanonicalFile().toURL();
                }
             }
          } else {
             url = new URL(this.remoteUrl);
          }
       } catch (Exception e) {
          throw new IllegalStateException("can't turn into URL, NewAppEnv=" + this.toString() + " fileName="
                + fileName, e);
       }
       return url;
    }
 
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
       if (this == obj)
          return true;
       if (obj == null)
          return false;
       if (!AssetLocation.class.isAssignableFrom(obj.getClass()))
          return false;
       final AssetLocation other = (AssetLocation) obj;
       if (localPath == null) {
          if (other.localPath != null)
             return false;
       } else if (!localPath.equals(other.localPath))
          return false;
       if (remoteUrl == null) {
          if (other.remoteUrl != null)
             return false;
       } else if (!remoteUrl.equals(other.remoteUrl))
          return false;
       return true;
    }
 
    public String getHost() {
       if (this.remoteUrl != null && this.remoteUrl.contains("://")) {
          String l = this.remoteUrl.split("://")[1];
         String ipport = l.split("/", 2)[0]; // remove any uninteresting path in a URL
         ipport = ipport.split("::", 2)[0]; // mutant://host:port::localpath
          
          // TODO is there always a port? I guess nobody would run at the default 8080...
          int colon = ipport.lastIndexOf(":");
 
          if (colon > 0) {
             String port = ipport.substring(colon + 1);
             String srcIp = ipport.substring(0, colon);
             return srcIp;
          }
       }
 
       // default to me
       return Agents.getMyHostName();
    }
 
    public String getPort() {
       if (this.remoteUrl != null && this.remoteUrl.contains("://")) {
          String l = this.remoteUrl.split("://")[1];
          String ipport = l.split("/", 2)[0];
         ipport = ipport.split("::", 2)[0]; // mutant://host:port::localpath
          
          // TODO is there always a port? I guess nobody would run at the default 8080...
          int colon = ipport.lastIndexOf(":");
 
          if (colon > 0) {
             String port = ipport.substring(colon + 1);
             String srcIp = ipport.substring(0, colon);
             return port;
          }
       }
 
       // default to my port
       return Agents.me().port;
    }
 
    public String getProtocol() {
       if (this.remoteUrl != null && this.remoteUrl.contains("://")) {
          return this.remoteUrl.split("://")[0];
       }
       return null;
    }
 
    public static AssetLocation mutantEnv(String host, String dir) {
       AgentHandle d = Agents.agent(host);
       if (d == null) {
          // try by ip
          d = Agents.agentByIp(host);
       }
 
       if (d == null) {
          throw new IllegalArgumentException("Unknown host/ip: " + host);
       }
 
       return new AssetLocation("mutant://" + host + ":" + d.port + "::" + prepLocalDir(dir));
    }
 
    public static AssetLocation mutantEnv(String dir) {
       AgentHandle me = Agents.agent(Agents.getMyHostName());
       // NOTE mutant URLs must contain hostname not IP !!!
       return new AssetLocation("mutant://" + me.hostname + ":" + me.port + "::" + prepLocalDir(dir));
    }
 
    private static String prepLocalDir(String dir) {
       dir = dir.startsWith("jar:") ? dir : FileUtils.toCanonicalPath(dir);
 
       // this is a local PATH, need to make sure it ends with a "/" - all other code will
       // simply concatenate file names to it
       if (dir != null && !"".equals(dir) && !dir.endsWith("/") && !dir.endsWith("\\")) {
          return dir + "/";
       }
       return dir;
    }
 }
