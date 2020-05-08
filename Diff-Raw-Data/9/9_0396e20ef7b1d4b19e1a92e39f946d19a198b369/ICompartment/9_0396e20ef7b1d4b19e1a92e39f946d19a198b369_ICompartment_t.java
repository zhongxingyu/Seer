 
 package uk.ac.ed.inf.Metabolic.ndomAPI;
 
 import java.util.List;
 
 public interface ICompartment extends IModelObject {
   List<ICompound> getCompoundList() ;
   List<IMacromolecule> getMacromoleculeList() ;
   String getGOTerm() ;
   double getVolume() ;
  List<ICompartment>getChildCompartments();
  ICompartment getParentCompartment();
 }
