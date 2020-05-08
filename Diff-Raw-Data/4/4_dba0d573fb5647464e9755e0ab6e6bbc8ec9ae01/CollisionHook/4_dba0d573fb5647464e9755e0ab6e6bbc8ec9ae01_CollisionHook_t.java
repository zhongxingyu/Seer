 package org.openengsb.framework.edb.collisionHook.internal;
 
import java.util.ArrayList;
 import java.util.List;
 
 import org.openengsb.core.api.edb.EDBCollisionHook;
 import org.openengsb.core.api.edb.EDBObject;
 import org.openengsb.similarity.Searcher;
 
 public class CollisionHook implements EDBCollisionHook {
 
     private Searcher searcher;
 
     @Override
    public List<ArrayList<String>> findCollisions(List<EDBObject> samples) {
         return searcher.findCollisions(samples);
     }
 }
