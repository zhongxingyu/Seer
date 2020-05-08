 package org.openarchives.resourcesync;
 
 import java.util.List;
 
 public class ResourceSyncDescription extends UrlSet
 {
     public ResourceSyncDescription()
     {
         super(ResourceSync.CAPABILITY_RESOURCESYNC);
     }
 
     public ResourceSyncDescription(String describedby, String describedByContentType)
     {
         this();
         ResourceSyncLn ln = this.addLn(ResourceSync.REL_DESCRIBED_BY, describedby);
         ln.setType(describedByContentType);
     }
 
     public void addCapabilityList(URL caplist)
     {
         if (!ResourceSync.CAPABILITY_CAPABILITYLIST.equals(caplist.getCapability()))
         {
             throw new SpecComplianceException("URL added to ResourceSyncDescription is not a Capability List");
         }
         this.addUrl(caplist);
     }
 
     public URL addCapabilityList(String loc)
     {
         return this.addCapabilityList(loc, null);
     }
 
     public URL addCapabilityList(String loc, String describedby)
     {
         URL caplist = new URL();
         caplist.setLoc(loc);
        if (describedby != null)
        {
            caplist.addLn(ResourceSync.REL_DESCRIBED_BY, describedby);
        }
         caplist.setCapability(ResourceSync.CAPABILITY_CAPABILITYLIST);
         this.addCapabilityList(caplist);
         return caplist;
     }
 
     public List<ResourceSyncEntry> getCapabilityLists()
     {
         return this.getUrls();
     }
 }
