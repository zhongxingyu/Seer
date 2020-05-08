 package pbs.parser;
 
 import java.util.ArrayList;
 
 import jig.engine.util.Vector2D;
 
 import pbs.*;
 import pbs.Level.*;
 import pbs.Entity.*;
 import pbs.parser.Statements.*;
 import pbs.parser.BooleanElements.*;
 import pbs.parser.ExpressionElements.*;
 
 public class Elements {
     
     //entity descriptions... 
     public abstract static class ObjectDescription {
 	//returns an entity matching this description
 	//public abstract ALAYERTYPE getLayerType();
 	public abstract void mutate(Level l);	
     }
     
     public static class TriggerDescription extends ObjectDescription {
 	//triggers have a list of statements that get added to the
 	//event queue when they are triggered
 	ArrayList<Statement> stmtlist;
 
 	public TriggerDescription(ArrayList<Statement> sl){
 	    stmtlist = sl;
 	}
 
 	public void mutate(Level l){
 	    //this method SHOULD add a trigger with the specified stmtlist to the event layer
 	    System.out.println("Trigger Description");
 
 	    if(stmtlist != null){
 		for(Statement s : stmtlist){
 		    System.out.print("Not executed yet: ");
 		    s.execute(l);
 		}
 	    }
 	}
     }
 
     public static class timedTrigger extends TriggerDescription {
 	public timedTrigger(ArrayList<Statement> sl){ super(sl); }
 
 	public void mutate(Level l){
 	    System.out.print("Timed ");
 	    super.mutate(l);
 	}
     }
 
     public static class collisionTrigger extends TriggerDescription {
 	public collisionTrigger(ArrayList<Statement> sl){ super(sl); }
 
 	public void mutate(Level l){
 	    System.out.print("Collision ");
 	    super.mutate(l);
 	}
     }
 
     public static class onscreenTrigger extends TriggerDescription {
 	public onscreenTrigger(ArrayList<Statement> sl){ super(sl); }
 
 	public void mutate(Level l){
 	    System.out.print("Onscreen ");
 	    super.mutate(l);
 	}
     }
 
 
     public static class EntityDescription extends ObjectDescription {
 
 	ArrayList<Param> paramlist;
 	String imgsrc;
 	Layer targetLayer;
 
 	public EntityDescription(String img, ArrayList<Param> pl){
 	    imgsrc = img;
 	    paramlist = pl;
 	}
 
 	public void mutate(Level l){
	    Entity e = new Entity(imgsrc);
 	    
	    for(int i = 0; i < paramlist.size(); i++){
 		paramlist.get(i).mutate(e);
 	    }
 	    
 	    l.add(e, targetLayer);
 
 	    System.out.println("Entity Description");
 	}
     }
 
     public static class fxEntity extends EntityDescription {
 	public fxEntity(String img, ArrayList<Param> pl){ super(img, pl); }
 
 	public void mutate(Level l){
 	    //set layer
 	    targetLayer = Layer.FX;
 	    System.out.print("Fx ");
 	    super.mutate(l);
 	}
     }
 
     public static class enemyEntity extends EntityDescription {
 	public enemyEntity(String img, ArrayList<Param> pl){ super(img, pl); }
 
 	public void mutate(Level l){
 	    //set layer
 	    targetLayer = Layer.ENEMY;
 	    System.out.print("Enemy ");
 	    super.mutate(l);
 
 	}
     }
 
     public static class staticEntity extends EntityDescription {
 	public staticEntity(String img, ArrayList<Param> pl){ super(img, pl); }
 
 	public void mutate(Level l){
 	    //set layer
 	    targetLayer = Layer.STATIC;
 	    System.out.print("Static ");
 	    super.mutate(l);
 	}
     }
 
     //parameters for entities
     public interface Param {
 	public abstract boolean mutate(Entity e);
     }
 
     public static abstract class VectorParam implements Param {
 	Vector2D vec;
 	public VectorParam(Vector2D v){ vec = v; }
     }
 
     public static class VelocityParam extends VectorParam {
 	public VelocityParam(Vector2D v){ super(v); }
 	public boolean mutate(Entity e){
 	    e.setVelocity(vec);
 	    return true;
 	}
     }
     
     public static class PositionParam extends VectorParam {
 	public PositionParam(Vector2D p){ super(p); }
 	public boolean mutate(Entity e){
 	    e.setPosition(vec);
 	    return true;
 	}
     }
     
     public static class ScoreValueParam implements Param {
 	int score;
 	public ScoreValueParam(int s){ score = s; }
 	public boolean mutate(Entity e){
 	    e.setValue(score);
 	    return true;
 	}
     } 
 	
 
     public static class UpdateParam implements Param {
 	CustomUpdate cu;
 	public UpdateParam(CustomUpdate u){ cu = u; }
 	public boolean mutate(Entity e){
 	    e.setCustomUpdate(cu);
 	    return true;
 	}
     }
 
     public static class RenderParam implements Param {
 	CustomRender cr;
 	public RenderParam(CustomRender r){ cr = r; }
 	public boolean mutate(Entity e){
 	    e.setCustomRender(cr);
 	    return true;
 	}
     }
 
     public static class WeaponParam implements Param {
 	CustomWeapon cw;
 	public WeaponParam(CustomWeapon w){ cw = w; }
 	public boolean mutate(Entity e){
 	    e.setCustomWeapon(cw);
 	    return true;
 	}
     }
 
 }
