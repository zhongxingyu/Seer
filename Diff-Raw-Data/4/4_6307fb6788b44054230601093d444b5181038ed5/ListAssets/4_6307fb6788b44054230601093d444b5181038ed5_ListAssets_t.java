 package com.razie.comm.commands;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.w3c.dom.Element;
 
import razie.assets.AssetKey;

 import com.razie.media.config.MediaConfig;
 import com.razie.pub.base.data.XmlDoc.Reg;
 import com.razie.pub.base.log.Log;
 import com.razie.pub.comms.AgentHandle;
 import com.razie.pub.comms.Agents;
 import com.razie.pub.draw.DrawStream;
 import com.razie.pub.draw.widgets.DrawToString;
 import com.razie.pub.http.SocketCmdHandler;
 import com.razie.pub.lightsoa.HttpAssetSoaBinding;
 import com.razie.sdk.assets.providers.MutantProvider;
 
 public abstract class ListAssets extends SocketCmdHandler.Impl {
 
    /**
     * @param cmdName
     * @param protocol
     * @param args
     * @param socket
     * @param reply
     * @return
     */
    protected void list(String cmdName, String protocol, String args, Properties parms, DrawStream out) {
       // format: type/location
       String type = parms.getProperty("type", "Movie");
       String location = parms.getProperty("location", "");
       String category = parms.getProperty("category", "");
    
       if (location.length() <= 0 && ("Movie".equals(type) || "Series".equals(type))) {
          // now it's interesting - if location not present, get my defaults
          for (Element e : Reg.doc(MediaConfig.MEDIA_CONFIG).listEntities(
                  "/config/storage/host[@name='" + Agents.me().name + "']/media")) {
             location = e.getAttribute("localdir");
    
             if (category != null && category.length() > 0 && !("All".equals(category))) {
                // browse all first level folders and follow only the
                // category...if present
                File f = new File(location);
                File[] entries = f.listFiles();
    
                if (entries != null) {
                   for (File entry : entries) {
                      if (entry.isDirectory()) {
                         String cat = entry.getName();
                         String newLoc = location + "\\" + cat;
    
                         if (cat.equals(category) || ("Rest".equals(category) && !MediaConfig.getInstance().getCategories().containsKey(cat))) {
                            if (!"json".equals(protocol)) {
                               out.write(newLoc + "\n");
                            }
                            HttpAssetSoaBinding.listLocal(type, newLoc, true, out);
                         }
                      }
                   }
                }
             } else {
                // no cat - just list all at the location
                if (!"json".equals(protocol)) {
                   out.write(location + "\n");
                }
                HttpAssetSoaBinding.listLocal(type, location, true, out);
             }
          }
       } else {
          AssetKey loc = AssetKey.fromString(location);
          if (loc.getLocation().isLocal() || "".equals(location)) {
             HttpAssetSoaBinding.listLocal(type, loc.getId(), true, out);
          } else {
             MutantProvider mutant = new MutantProvider(loc.getLocation().getHost());
             if (mutant.isUp()) {
                String otherList = (String) mutant.list(type, category, null, null, loc.getId()).read();
                out.write(new DrawToString(loc.getLocation().getHost() + ":<br>" + otherList));
             } else {
                out.write(loc.getLocation().getHost() + " - not reacheable...<br>");
             }
          }
       }
    
       location = parms.getProperty("location", "");
       if ("listAll".equals(cmdName)) {
          // browse all other hosts...
          // for (Element e :
          // Reg.doc(AgentConfig.AGENT_CONFIG).listEntities("/config/clouds/cloud/host"))
          // {
          // String n = e.getAttribute("name");
          // if (!me.equals(n)
          // && (e.getAttribute("type").equals("laptop") ||
          // e.getAttribute("type").equals("desktop"))) {
          for (AgentHandle e : Agents.homeCloud().agents().values()) {
             if (!Agents.me().name.equals(e.name)) {
                MutantProvider mutant = new MutantProvider(e.name);
                if (mutant.isUp()) {
                   String otherList = (String) mutant.list(type, category, null, null, location).read();
                   out.write(new DrawToString(e.name + ":\n" + otherList));
                } else {
                   out.write(e.name + " - not reacheable...\n");
                }
             }
          }
       }
    }
 
    static final Log logger = Log.Factory.create("assets", ListAssets.class.getName());
 
 }
