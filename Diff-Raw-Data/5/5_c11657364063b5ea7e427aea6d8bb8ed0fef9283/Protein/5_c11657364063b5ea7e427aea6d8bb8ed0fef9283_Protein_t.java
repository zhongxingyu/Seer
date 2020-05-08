 package edu.allatom;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import edu.geom3D.Sphere;
 import edu.math.Line;
 import edu.math.Matrix;
 import edu.math.Point;
 import edu.math.Superposition;
 import edu.math.TransformationMatrix3D;
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
 
 	public List<Atom> getBackboneAtoms() {
 		List<Atom> atoms = new LinkedList<Atom>();
 		for(AminoAcid aa : aaSeq) {
 			atoms.addAll(aa.getBackboneAtoms());
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
 		d /= aaSeq.size();
 		d = Math.sqrt(d);
 		return d;
 	}
 	
 	public void rotate(float angle, int aaIndex, RotationType rotationType) {
 		AminoAcid aa = aaSeq.get(aaIndex);
 		Atom ca = aa.getAtom("CA");
 		
 		switch(rotationType) {
 		case PHI: {
 			Atom n = aa.getAtom("N");
 			Line rotationAxis = new Line(new Vector(ca.position), n.vectorTo(ca));
 			Matrix rotation = TransformationMatrix3D.createRotation(
 					rotationAxis, angle);
 			transformProtein(rotation, aaIndex, RotationType.PHI);
 			break;
 		} case PSI: {
 			Atom c = aa.getAtom("C");
 			Line rotationAxis = new Line(new Vector(ca.position), ca.vectorTo(c));
 			Matrix rotation = TransformationMatrix3D.createRotation(
 					rotationAxis, angle);
 			transformProtein(rotation, aaIndex, RotationType.PSI);
 			break;
 		} case OMEGA: {
 			throw new NotImplementedException();
 		}}
 	}
 
 
 // private static final float LENGTH_CA_CB = 1.532714f;
 // private static final float LENGTH_CA_HA = 1.0747181f;
 // private static final float LENGTH_N_H = 0.97930175f;
 // private static final float ANGLE_C_O = 2.1054177f;
 // private static final float ANGLE_C_N = 1.07835f;
 // private static final float ANGLE_CA_C = 2.1611323f;
 	// private static final float ANGLE_N_H = 2.0847397f;
 	// private static final float ANGLE_N_CA = 2.114792f;
 // private static final float ANGLE_N_CA_projCB = 2.1617115f;
 // private static final float ANGLE_CB_CA_projCB = 0.9287418f;
 
 
 // private static final float ANGLE_H_N_CA = 2.0761454f;
 // private static final float LENGTH_CA..O = 2.397302f;
 // private static final float LENGTH_CA..N = 2.4298022f;
 // private static final float LENGTH_N..C = 2.4622786f;
 // private static final float LENGTH_C..H = 1.9958001f;
 // private static final float LENGTH_C..CA = 2.4357588f;
 // private static final float LENGTH_H..CA = 2.1230228f;
 	
 
 // private static final float LENGTH_CA..projCB = 0.9118956f;
 // private static final float LENGTH_N..projHA = 1.8863802f;
 // private static final float LENGTH_N..projCB = 2.1127536f;
 // private static final float LENGTH_distCBPlaneCB = 1.2294171f;
 // private static final float LENGTH_C..projH = 1.9950385f;
 
 // private static final float LENGTH_projHAangle = 2.1748586f;
 // private static final float LENGTH_projHangle = 2.086573f;
 
 
 
 
 //---
 private static final float LENGTH_C_O = 1.2259989f;
 private static final float LENGTH_CA_C = 1.5272093f;
 private static final float LENGTH_N_CA = 1.4680145f;
 private static final float LENGTH_C_N = 1.3233874f;
 private static final float ANGLE_N_CA_C = 2.160362f;
 private static final float ANGLE_CA_C_O = 2.1067827f;
 private static final float ANGLE_CA_C_N = 2.0382223f;
 private static final float ANGLE_C_N_CA = 2.1197803f;
 private static final float ANGLE_C_N_H = 2.085841f;
 private static final float ANGLE_N_CA_projCB = 2.1552231f;
 private static final float ANGLE_CB_CA_projCB = 0.932577f;
 private static final float LENGTH_HAplane_HAproj = 0.8833212f;
 private static final float LENGTH_Hplane_Hproj = 0.023912556f;
 private static final float LENGTH_N_Hproj = 0.9777301f;
 private static final float LENGTH_CA_HAproj = 0.6108196f;
 //---
 
 	private static final float ANGLE_CA_HA = 2.1751032f;
 
 
 
 	//TODO: 'H' bliver ikke indsat i den første aminosyre - bizart!
 	public static Protein getUncoiledProtein(List<AminoAcidType> aminoAcidTypes) {
		List<AminoAcid> acids = new ArrayList<AminoAcid>(aminoAcidTypes.size());
 		int i = 0;
 		int initialCollisions = 0;
 		int collisions = 0;
 		
 		AminoAcidType type = aminoAcidTypes.get(i++);
 		AminoAcid aa = new AminoAcid(type);
 		Atom n = new Atom(Atom.Type.N, "N", new Point(0, 0, 0));
 		aa.addAtom(n);
 		Atom ca = new Atom(Atom.Type.C, "CA", new Point(0, LENGTH_N_CA, 0));
 		aa.addAtom(ca);
 		double a = Math.PI/2;
 		float f = 0;
 		String HAname = (aa.type == AminoAcidType.GLY ? "HA2" : "HA");
         double b = a + (Math.PI + ANGLE_CA_HA) * -f;
 		Atom ha = new Atom(Atom.Type.H, HAname, new Point(
 				(float) (ca.position.x() + Math.cos(b)*LENGTH_CA_HAproj),
 				(float) (ca.position.y() + Math.sin(b)*LENGTH_CA_HAproj),
 				LENGTH_HAplane_HAproj * f));
 		//aa.addAtom(ha); //TODO fix position
 		while(i < aminoAcidTypes.size()) {
 			f = i%2 == 0 ? -1 : 1;
 			a += (Math.PI - ANGLE_N_CA_C) * f;
 			Atom c = new Atom(Atom.Type.C, "C", new Point(
 					(float)(ca.position.x() + Math.cos(a)*LENGTH_CA_C),
 					(float)(ca.position.y() + Math.sin(a)*LENGTH_CA_C),
 					0));
 			aa.addAtom(c);
 			b = a + (Math.PI - ANGLE_CA_C_O) *f;
 			Atom o = new Atom(Atom.Type.O, "O", new Point(
 					(float) (c.position.x() + Math.cos(b)*LENGTH_C_O),
 					(float) (c.position.y() + Math.sin(b)*LENGTH_C_O),
 					0));
 			aa.addAtom(o);
 			
 			// place the sidechain
 			Atom cb = null;
 			for(Atom sa : type.sidechainAtoms) {
 				if(!sa.label.equals("N") && !sa.label.equals("CA") && !sa.label.equals("C")
 						&& !sa.label.equals("O") && !sa.label.equals("H") && !sa.label.equals(HAname)) {
 					Point position = new Vector(sa.position).plus(ca.position);
 					Atom atom = new Atom(sa.type, sa.label, position);
 					aa.addAtom(atom);
 					if(atom.label.equals("CB")) {
 						cb = atom;
 					} else if(type == AminoAcidType.GLY && atom.label.equals("HA3")) {
 						cb = atom;
 					}
 				}
 			}
 			// rotate sidechain to point to the N atom
 			Vector rotationVector = ca.vectorTo(cb).cross(ca.vectorTo(n));
 			float rotationAngle = ca.vectorTo(cb).angle(ca.vectorTo(n));
 			Matrix rotationMatrix = TransformationMatrix3D.createRotation(new Vector(ca.position), rotationVector, rotationAngle);
 			for(Atom sa : aa.allatoms.values()) {
 				if(!sa.label.equals("N") && !sa.label.equals("CA") && !sa.label.equals("C")
 						&& !sa.label.equals("O") && !sa.label.equals("H") && !sa.label.equals(HAname)) {
 					sa.position = rotationMatrix.applyTo(new Vector(sa.position));
 				}
 			}
 			// rotate sidechain to point correctly
 			Vector rotationVector1 = ca.vectorTo(c).cross(ca.vectorTo(n));
 			float rotationAngle1 = ANGLE_N_CA_projCB;
 			Matrix rotationMatrix1 = TransformationMatrix3D.createRotation(
 					new Vector(ca.position), rotationVector1, rotationAngle1);
 			for(Atom sa : aa.allatoms.values()) {
 				if(!sa.label.equals("N") && !sa.label.equals("CA") && !sa.label.equals("C")
 						&& !sa.label.equals("O") && !sa.label.equals("H") && !sa.label.equals(HAname)) {
 					sa.position = rotationMatrix1.applyTo(new Vector(sa.position));
 				}
 			}
 			Vector rotationVector2 = ca.position.vectorTo(new Vector(
 					rotationMatrix.applyTo(new Vector(cb.position))));
 			float rotationAngle2 = ANGLE_CB_CA_projCB * f;
 			Matrix rotationMatrix2 = TransformationMatrix3D.createRotation(
 					new Vector(ca.position), rotationVector2, rotationAngle2);
 			for(Atom sa : aa.allatoms.values()) {
 				if(!sa.label.equals("N") && !sa.label.equals("CA") && !sa.label.equals("C")
 						&& !sa.label.equals("O") && !sa.label.equals("H") && !sa.label.equals(HAname)) {
 					sa.position = rotationMatrix2.applyTo(new Vector(sa.position));
 				}
 			}
 			boolean rotamerStatus;
 //			do {
 //				rotamerStatus = aa.nextRotamer();
 				if(aa.collides(new Protein(acids)) != null) {
 					initialCollisions++;
 				}
 				rotamerStatus = aa.nextCollisionlessRotamer(new Protein(acids));
 				if(!rotamerStatus) {
 					collisions++;
 				}
 //			} while(rotamerStatus);
 //			if(!rotamerStatus) {
 //				System.out.println("no more rotamers");
 //			}
 			
 			acids.add(aa);
 			
 			type = aminoAcidTypes.get(i++);
 			aa = new AminoAcid(type);
			a -= (Math.PI - ANGLE_CA_C_N) * f;
 			n = new Atom(Atom.Type.N, "N", new Point(
 					(float) (c.position.x() + Math.cos(a)*LENGTH_C_N),
 					(float) (c.position.y() + Math.sin(a)*LENGTH_C_N),
 					0));
 			aa.addAtom(n);
 			if(type!=AminoAcidType.PRO) {
 				b = a + (Math.PI + ANGLE_C_N_H) * f;		
 				Atom h = new Atom(Atom.Type.H, "H", new Point(
 						(float) (n.position.x() + Math.cos(b)*LENGTH_N_Hproj),
 						(float) (n.position.y() + Math.sin(b)*LENGTH_N_Hproj),
 						LENGTH_Hplane_Hproj * f));
 				aa.addAtom(h);
 			}
 			a += (Math.PI - ANGLE_C_N_CA) * f;
 			ca = new Atom(Atom.Type.C, "CA", new Point(
 					(float) (n.position.x() + Math.cos(a)*LENGTH_N_CA),
 					(float) (n.position.y() + Math.sin(a)*LENGTH_N_CA),
 					0));
 			aa.addAtom(ca);
 
             HAname = (aa.type == AminoAcidType.GLY ? "HA2" : "HA");
             b = a + (Math.PI + ANGLE_CA_HA) * -f;
 			ha = new Atom(Atom.Type.H, HAname, new Point(
 					(float) (ca.position.x() + Math.cos(b)*LENGTH_CA_HAproj),
 					(float) (ca.position.y() + Math.sin(b)*LENGTH_CA_HAproj),
 					LENGTH_HAplane_HAproj * f));
 			//aa.addAtom(ha);
 		}
 //		acids.add(aa);
 		//TODO fix last aa
 		
 		Bonder.bondAtoms(acids);
 		Protein p = new Protein(acids);
 		
 		return p;
 	}
 	
 	public double minRMSD(List<Atom> caTrace) {
 		List<Vector> trace1 = new LinkedList<Vector>();
 		for(AminoAcid aa : aaSeq) {
 			trace1.add(new Vector(aa.getAtom("CA").position));
 		}
 		List<Vector> trace2 = new LinkedList<Vector>();
 		for(Atom a : caTrace.subList(0, caTrace.size() - 1)) {//TODO fix last aa!
 			trace2.add(new Vector(a.position));
 		}
 		return Superposition.minRMSD(trace1, trace2);
 	}
 }
