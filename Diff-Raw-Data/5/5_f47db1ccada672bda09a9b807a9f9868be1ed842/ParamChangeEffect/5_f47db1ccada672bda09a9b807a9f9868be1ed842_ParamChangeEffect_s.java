 package org.sankozi.rogueland.model.effect;
 
 import org.sankozi.rogueland.model.effect.AccessManager;
 import org.sankozi.rogueland.model.effect.Effect;
 import java.util.EnumMap;
 import java.util.Map;
 import org.sankozi.rogueland.model.Destroyable;
 
 /**
  * 
  * @author sankozi
  */
 public final class ParamChangeEffect extends Effect {
     private final String name;
     private final Map<Destroyable.Param, Float> changes;
 
     /** creates infinite version of effect (usually for items) */
     public ParamChangeEffect(String name, Map<Destroyable.Param, Float> changes) {
 		super(Float.POSITIVE_INFINITY);
         this.name = name;
        this.changes = new EnumMap(changes);
 	}
 
 	public ParamChangeEffect(String name, float finishTime, Map<Destroyable.Param, Float> changes) {
 		super(finishTime);
         this.name = name;
        this.changes = new EnumMap(changes);
 	}
 	
 	@Override
 	public void start(AccessManager manager) {
         for(Map.Entry<Destroyable.Param, Float> entry : changes.entrySet()){
             manager.accessDestroyableParam(entry.getKey()).setChange(entry.getValue());
         }
 	}
 
 	@Override
 	public void end(AccessManager manager) {
         for(Destroyable.Param param : changes.keySet()){
             manager.accessDestroyableParam(param).setChange(0f);
         }
 	}
 
 	@Override
 	public String getObjectName() {
 		return name;
 	}
 }
