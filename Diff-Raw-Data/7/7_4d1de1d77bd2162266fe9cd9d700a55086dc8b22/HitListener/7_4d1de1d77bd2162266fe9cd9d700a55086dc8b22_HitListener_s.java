 package com.thorgaming.throwme;
 
 import org.jbox2d.collision.Shape;
 import org.jbox2d.dynamics.ContactListener;
 import org.jbox2d.dynamics.contacts.ContactPoint;
 import org.jbox2d.dynamics.contacts.ContactResult;
 
 import com.thorgaming.throwme.displayobjects.Sensor;
 import com.thorgaming.throwme.displayobjects.game.Character;
import com.thorgaming.throwme.displayobjects.game.cloud.Cloud;
 
 public class HitListener implements ContactListener {
 
 	@Override
 	public void add(ContactPoint arg0) {
 		Shape i = arg0.shape1;
 		Shape j = arg0.shape2;
 		Shape t = null;
		if (i.getUserData() instanceof Cloud) {
 			t = i;
 			i = j;
 			j = t;
 		}
 		if (i.getUserData() instanceof Character && j.getUserData() instanceof Sensor) {
 			((Sensor) j.getUserData()).hit(i);
 		}
 	}
 
 	@Override
 	public void persist(ContactPoint arg0) {
 		Shape i = arg0.shape1;
 		Shape j = arg0.shape2;
 		Shape t = null;
		if (i.getUserData() instanceof Cloud) {
 			t = i;
 			i = j;
 			j = t;
 		}
 		if (i.getUserData() instanceof Character && j.getUserData() instanceof Sensor) {
 			((Sensor) j.getUserData()).persistContact(i);
 		}
 	}
 
 	@Override
 	public void remove(ContactPoint arg0) {
 
 	}
 
 	@Override
 	public void result(ContactResult arg0) {
 
 	}
 
 }
