 import java.io.*;
 import java.util.*;
 import Jama.*;
 
 public class CalculatePMOI {
 
     ArrayList<Residue> newResArray;
     
     // NEW PMoI variable declarations
     int pastResidue = 0, currentResidue=0;
     double firstTermOfIxx = 0, firstTermOfIyy = 0, firstTermOfIzz = 0, firstTermOfIxy = 0, firstTermOfIxz = 0, firstTermOfIzx = 0, firstTermOfIyx = 0, firstTermOfIyz = 0, firstTermOfIzy = 0;
     
     double secondTermOfIxx = 0, secondTermOfIyy = 0, secondTermOfIzz = 0, secondTermOfIxy = 0, secondTermOfIxz = 0, secondTermOfIzx = 0, secondTermOfIyx = 0, secondTermOfIyz = 0, secondTermOfIzy = 0;
     
     double thirdTermOfIxx = 0, thirdTermOfIyy = 0, thirdTermOfIzz = 0, thirdTermOfIxy = 0, thirdTermOfIxz = 0, thirdTermOfIzx = 0, thirdTermOfIyx = 0, thirdTermOfIyz = 0, thirdTermOfIzy = 0;
     
     double Ixx = 0, Iyy = 0, Izz = 0, Ixy = 0, Iyx = 0, Ixz = 0, Izx = 0, Iyz = 0, Izy = 0;
     double totalAtomicWeight = 0;
     int countBFactorGreaterThanOne = 0;
     boolean discardBFactor = false;
 
     public ArrayList<Residue> CalculatePMOI(ArrayList<Residue> residueList, ArrayList<Atom> atomList) {
 	// END PMoI variable declaration
 	for (int i = 0; i<residueList.size(); ++i) {
 	    if (residueList.get(i).getCTerm() || residueList.get(i).getNTerm() == true) { 
 		// if C-terminus or N-terminus => calculate PMOI
 		//set up calculations for Ixx term by term
 		//Has been abstracted to a pretty clear point
 		//this also ideally will be in a helper method. 
 		ArrayList<Atom> currentAtomListOfResidue = residueList.get(i).getAtomListOfResidue();
 		String pdbNum = residueList.get(i).getResNum();
 		
 		for(int j = 0; j<currentAtomListOfResidue.size(); j++){
 			Atom currentAtom = currentAtomListOfResidue.get(j)
 			double aw = getAtomicWeight(currentAtom);
 			double x = currentAtom.getCoords().getX();
 			double y = currentAtom.getCoords().getY();
 			double z = currentAtom.getCoords().getZ();
 			double xSq = Math.pow(x,2);
 			double ySq = Math.pow(y,2);
 			double zSq = Math.pow(z,2);
 			// calc Ixx term by term
 			firstTermOfIxx += (aw) * (ySq + zSq);
 			secondTermOfIxx += Math.pow((aw * y), 2);
 			thirdTermOfIxx += Math.pow((aw * z), 2);
 			//calc Iyy term by term
 			firstTermOfIyy += aw * (xSq + zSq);
 			secondTermOfIyy += Math.pow((aw * x),2);
 			thirdTermOfIyy += Math.pow((aw * z), 2);
 			//set up calculations for Izz term by term
 			firstTermOfIzz += aw * (xSq + ySq);
 			secondTermOfIyy += Math.pow((aw * x), 2);
 			thirdTermOfIyy += Math.pow((aw * y), 2);
 			//set up calculations for Ixy/Iyx term by term
 			firstTermOfIxy += aw * x * y;
 			secondTermOfIxy += aw * x;
 			thirdTermOfIxy += aw * y;
 			//set up calculations for Ixz/Izx term by term
 			firstTermOfIxz += aw * x * z;
 			secondTermOfIxz += aw * x;
 			thirdTermOfIxz += aw * z;
 			//set up calculations for Iyz/Izy term by term
 			firstTermOfIxz += aw * y * z;
 			secondTermOfIxz += aw * y;
 			thirdTermOfIxz += aw * z;
 			// calculate total sum of atom weights of a C-terminus/N-terminus for later calculation
			totalAtomicWeight += aw;
 			//BEGIN calculate PMoI
 			Ixx = firstTermOfIxx - (1 / totalAtomicWeight) * (secondTermOfIxx) - (1 / totalAtomicWeight)
 		    * (thirdTermOfIxx);
 			Iyy = firstTermOfIyy - (1 / totalAtomicWeight)	* (secondTermOfIyy) - (1 / totalAtomicWeight)
 		    * (thirdTermOfIyy);
 			Izz = firstTermOfIzz - (1 / totalAtomicWeight)
 		    * (secondTermOfIzz) - (1 / totalAtomicWeight)
 		    * (thirdTermOfIzz);
 			Ixy = -firstTermOfIxy + (1 / totalAtomicWeight)
 		    * (secondTermOfIxy) * (thirdTermOfIxy);
 			Iyx = Ixy;
 			Ixz = -firstTermOfIxz + (1 / totalAtomicWeight)	* (secondTermOfIxz) * (thirdTermOfIxz);
 			Izx = Ixz;
 			Iyz = -firstTermOfIyz + (1 / totalAtomicWeight) * (secondTermOfIyz) * (thirdTermOfIyz);
 			Izy = Iyz;
		} //end iterating through the list of atoms of a residue
 		
 		double[][] populateMatrix = new double[][] { { Ixx, Ixy, Izz },{ Iyx, Iyy, Iyz }, { Izx, Izy, Izz } };
 		Matrix matrixForEigen = new Matrix(populateMatrix);
 		EigenvalueDecomposition ed = matrixForEigen.eig();
 		double[] getRealEigenvalues = ed.getRealEigenvalues();
 		double[] getImgEigenvalues = ed.getImagEigenvalues(); 
 		CartesianCoord principalMomentsOfInertia = new CartesianCoord(getRealEigenvalues[1], 
 			getRealEigenvalues[2], getRealEigenvalues[3]);
 		//reset terms
 		Ixx=0;
 		Iyy=0; 
 		Izz=0; 
 		Ixy=0; 
 		Iyx=0; 
 		Ixz=0;
 		Izx=0; 
 		Izy=0; 
 		Iyz=0;
 		//reset first terms
 		firstTermOfIxx=0; 
 		firstTermOfIyy=0; 
 		firstTermOfIzz=0;
 		firstTermOfIxy=0; 
 		firstTermOfIyx=0; 
 		firstTermOfIxz=0; 
 		firstTermOfIzx=0; 
 		firstTermOfIzy=0; 
 		firstTermOfIyz=0;
 		//reset second terms
 		secondTermOfIxx=0; 
 		secondTermOfIyy=0; 
 		secondTermOfIzz=0; 
 		secondTermOfIxy=0; 
 		secondTermOfIyx=0; 
 		secondTermOfIxz=0; 
 		secondTermOfIzx=0; 
 		secondTermOfIzy=0; 
 		secondTermOfIyz=0;
 		//reset third terms
 		thirdTermOfIxx=0; 
 		thirdTermOfIyy=0; 
 		thirdTermOfIzz=0; 
 		thirdTermOfIxy=0; 
 		thirdTermOfIyx=0; 
 		thirdTermOfIxz=0; 
 		thirdTermOfIzx=0; 
 		thirdTermOfIzy=0;
 		thirdTermOfIyz=0;
 		totalAtomicWeight=0;
 		//end calculate PMoI
 		//need xyz coordinates to calculate geometries
 		newResArray.add(new Residue(pdbNum, principalMomentsOfInertia));
 	    }// end if
 	}// end for
 	return newResArray;
     }// end method
 
 
     public double getAtomicWeight(Atom currentAtom) { //NEW PMoI method to get atomic weight given atom type
 
 	double atomWeight = 0;
 	String atomType = Character.toString(currentAtom.getAtomType().charAt(0));
 	String[] atomTypeArray = {"H","N","C","O","S"};
 	double[] atomWeightArray = {1.00794, 14.0067, 12.0107, 15.9994, 32.065};
 	
 	for(int i=0; i<atomTypeArray.length; ++i) {
 	    if (atomType.equals(atomTypeArray[i])) {
 		atomWeight = atomWeightArray[i];
 	    }
 	}	
 	return atomWeight;
     }
 
 }// end class
