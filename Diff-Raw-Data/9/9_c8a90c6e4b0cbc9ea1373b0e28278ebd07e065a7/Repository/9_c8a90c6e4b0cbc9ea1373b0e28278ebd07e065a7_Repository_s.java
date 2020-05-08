 package com.undeadscythes.genebase.record;
 
 import com.undeadscythes.gedform.*;
 import com.undeadscythes.genebase.gedcom.*;
import com.undeadscythes.genebase.meta.*;
 import java.util.*;
 
 /**
  * A {@link Repository} is a store of {@link Source}s.
  *
  * @author UndeadScythes
  */
 public class Repository extends UniqueHolder {
     private static final long serialVersionUID = 1L;
 
    private final Map<String, Source> sources;
 
     /**
      * Load this {@link Repository} with the data in the given {@link Cluster}.
      */
     public Repository(final Cluster cluster) {
        super(GEDTag.REPO, cluster);
        sources = new HashMap<String, Source>(0);
     }
 }
