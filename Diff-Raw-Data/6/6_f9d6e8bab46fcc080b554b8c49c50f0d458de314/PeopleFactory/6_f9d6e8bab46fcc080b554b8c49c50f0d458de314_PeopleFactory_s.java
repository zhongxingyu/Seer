 package edu.agh.tunev.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Random;
 import java.util.Vector;
 
 import edu.agh.tunev.model.AbstractPerson;
 
 final class PeopleFactory {
 
 	static Random rng = new Random();
 
 	static Vector<AbstractPerson> random(Class<?> type, int num, double maxX,
 			double maxY) {
 		Vector<AbstractPerson> r = new Vector<AbstractPerson>();
 
 		for (int i = 0; i < num; i++)
 			r.add(newAbstractPerson(type, rng.nextDouble() * maxX,
 					rng.nextDouble() * maxY));
 
 		return r;
 	}
 
 	static AbstractPerson newAbstractPerson(Class<?> type, double x, double y) {
 		try {
 			return (AbstractPerson) type
					.getDeclaredConstructor(double.class, double.class)
					.newInstance(x, y);
 		} catch (InstantiationException | IllegalAccessException
 				| IllegalArgumentException | InvocationTargetException
 				| NoSuchMethodException | SecurityException e) {
 			e.printStackTrace();
 			throw new IllegalArgumentException("Error during instantiation of "
 					+ type.getName() + ".");
 		}
 	}
 
 }
