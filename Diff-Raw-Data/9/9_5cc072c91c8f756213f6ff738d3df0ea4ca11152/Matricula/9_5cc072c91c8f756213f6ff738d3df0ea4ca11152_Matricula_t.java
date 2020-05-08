 package tap.practica;
 
 import java.util.ArrayList;
 
 public class Matricula {
 	
 	ArrayList<Asignatura> matriculadas = new ArrayList<Asignatura>();
 	
 	int numCreditos;
 	
	public Boolean matricularAsignatura(Asignatura asig) {
		if ((numCreditos + asig.getCreditos()) > 60) return false;
 		matriculadas.add(asig);
 		numCreditos += asig.getCreditos();
		return true;
	}
	
	public Boolean comprobar() {
		return ((numCreditos > 60) || (numCreditos < 12)) ? false : true;
 	}
 
 }
