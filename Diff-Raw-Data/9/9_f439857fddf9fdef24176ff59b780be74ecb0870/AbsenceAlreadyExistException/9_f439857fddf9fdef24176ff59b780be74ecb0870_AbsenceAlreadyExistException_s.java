 package exceptions;
 
 /**
  * @author f.patin
  */
 public class AbsenceAlreadyExistException extends Exception {
 	public AbsenceAlreadyExistException() {
		super("Une demande d'absence existe déjà");
 	}
 }
