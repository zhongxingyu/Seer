 package tap.practica;
 
 import java.util.ArrayList;
 
 public class Matricula {
 	
 	ArrayList<Asignatura> matriculadas = new ArrayList<Asignatura>();
 	
 	int numCreditos;
 	
	public void matricularAsignatura(Asignatura asig) {
 		matriculadas.add(asig);
 		numCreditos += asig.getCreditos();
 	}
 
 }
