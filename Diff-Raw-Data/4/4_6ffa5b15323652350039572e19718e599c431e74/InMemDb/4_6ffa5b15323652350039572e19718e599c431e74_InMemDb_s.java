 /**
  * Razvan's public code. Copyright 2008 based on Apache license (share alike) see LICENSE.txt for
  * details.
  */
 package com.razie.dist.db;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.razie.pub.base.AttrAccess;
 import com.razie.pub.base.data.Pair;
 
 /**
  * simple in memory map of attributes - has complete distributed db functionality though
  * 
  * @author razvanc
  */
 @SuppressWarnings("unchecked")
 public class InMemDb implements DistDoc {
     public String      name;
     public int         version = 0;
    private AttrAccess attrs   = new AttrAccess.Impl();
    private List<Pair> diffs   = new ArrayList<Pair>();
     private int lastSyncVersion=0;
 
     public InMemDb(String name) {
         this.name = name;
     }
 
     public void create() {
     }
 
     public void setAttr(String name, String value) {
         this.attrs.setAttr(name, value);
         this.version++;
         this.diffs.add(new Pair<String, String>(name, value));
     }
 
     public String getAttr(String name) {
         return (String) attrs.getAttr(name);
     }
 
     /** ******************** DistDb implementation ******************* */
 
     public void applyDiffs(int fromVersion, int toVersion, String sdiffs) {
         // sanity check
         if (fromVersion != version)
             throw new IllegalStateException("versions don't match: " + fromVersion + ":" + version);
 
         List<Pair> diffs;
         try {
             diffs = Pair.fromJson(new ArrayList<Pair>(), new JSONObject(sdiffs));
         } catch (JSONException e) {
             throw new RuntimeException(e);
         }
         for (Pair p : (List<Pair>) diffs)
             setAttr((String) p.a, (String) p.b);
 
         // did i get there?
         assert (this.version == toVersion);
         this.lastSyncVersion=this.version;
     }
 
     public String getDiffs(int fromVersion, int toVersion) {
         // need this for sync-ing newly created databases...
         List<Pair> ret = (List<Pair>) diffs.subList(fromVersion, toVersion);
         if (ret != null) {
             return Pair.toJson(ret, null).toString();
         }
         return null;
     }
 
     public int getFormatVersion() {
         return 0;
     }
 
     public int getLocalVersion() {
         return version;
     }
 
     public void upgradeFormat(int fromFormatVer, int toFromatVer) {
     }
 
     public int getLastSyncVersion() {
         return lastSyncVersion;
     }
 
     public void purgeDiffs(int fromVersion, int toVersion) {
         // TODO Auto-generated method stub
     }
 }
