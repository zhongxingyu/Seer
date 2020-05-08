 package edu.agh.tunev.model.cellular.agent;
 
import java.awt.geom.Point2D;

 import edu.agh.tunev.model.AbstractPerson;
 
 /**
  * Tutaj możesz pamiętać sobie jakieś dane o osobie, które mają znaczenie tylko
  * w implementowanym modelu. Wszystkie cechy "osobnicze", które mają sens poza
  * konkretnym modelem, powinny być dodane do AbstractPerson. Przykład:
  * 
  * Do <code>AbstractPerson</code>: wiek, rasa, orientacja, religious views
  * 
  * Do <code>Person extends AbstractPerson</code>: informacja o tym co osoba
  * sądzi nt. konkretnych pól automatu komórkowego
  * 
  */
 public final class Person extends AbstractPerson {
 
	public Person(Point2D.Double position) {
		super(position);
 	}
 
 }
