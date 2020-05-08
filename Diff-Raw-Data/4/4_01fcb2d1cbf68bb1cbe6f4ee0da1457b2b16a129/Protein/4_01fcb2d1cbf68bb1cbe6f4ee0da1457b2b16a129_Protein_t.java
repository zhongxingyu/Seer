 package edu.allatom;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.math.Matrix;
 import edu.math.Vector;
 
 public class Protein {
 	
 	public enum RotationType { PHI, PSI, OMEGA };
 	
 	List<AminoAcid> aaSeq;
 	
 	public Protein(List<AminoAcid> aaSeq) {
 		this.aaSeq = aaSeq;
 	}
 
 	public List<Atom> getAtoms() {
 		List<Atom> atoms = new ArrayList<Atom>();
 		for(AminoAcid aa : aaSeq) {
 			atoms.addAll(aa.getAtoms());
 		}
 		return atoms;
 	}
 	
 	public void transformProtein(Matrix m) {
 		for(Atom a : getAtoms()) {
 			Vector v = new Vector(a.position);
 			a.position = m.applyToIn(v);
 		}
 	}
 
 	public void transformProtein(Matrix m, int aaIdx, RotationType type) {
 		int i = 0;
 		for(AminoAcid aa : aaSeq) {
 			if(i < aaIdx) {
 				;
 			} else if(i == aaIdx) {
 				switch(type) {
 				case PHI:
 					//TODO rotér ikke alle atomer!
 					for(Atom a : aa.getAtoms()) {
 						Vector v = new Vector(a.position);
 						a.position = m.applyToIn(v);
 					}
 					break;
 				case PSI:
 					Atom a = aa.getAtom("O");
 					Vector v = new Vector(a.position);
 					a.position = m.applyToIn(v);
 //					for(Atom a : aa.getAtoms()) {
 ////						if(!(a.name.equals("CA") || a.name.equals("C") || a.name.equals("O"))) {
 //							Vector v = new Vector(a.position);
 //							a.position = m.applyToIn(v);
 ////						}
 //					}					
 					break;
 				case OMEGA:
 					//TODO
 					break;
 				}
 			} else {
 				for(Atom a : aa.getAtoms()) {
 					Vector v = new Vector(a.position);
 					a.position = m.applyToIn(v);
 //					if(a.position.x()!=a.position.x()) {
 //						System.out.println("noooo  "+a.name+"  "+aa.type.name()+"   "+m);
 //					}
 				}
 			}
 			i++;
 		}
 	}
 	
 	public double cATraceRMSD(LinkedList<Atom> trace) {
 		double d = 0;
 		for(int i=0; i<aaSeq.size(); i++) {
 			Atom cA = aaSeq.get(i).getAtom("CA");
 			Atom cAT = trace.get(i);
 			double dx = cA.position.x() - cAT.position.x();
 			double dy = cA.position.y() - cAT.position.y();
 			double dz = cA.position.z() - cAT.position.z();
			d += dx*dx + dy*dy + dz*dz;
 		}
		d = Math.sqrt(d);
 		d /= aaSeq.size();
 		return d;
 	}
 	
 }
