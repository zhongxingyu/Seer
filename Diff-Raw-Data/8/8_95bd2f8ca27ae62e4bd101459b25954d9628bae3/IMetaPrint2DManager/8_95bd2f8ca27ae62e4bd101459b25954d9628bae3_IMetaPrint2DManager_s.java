 /* *****************************************************************************
  * Copyright (c) 2008 The Bioclipse Project and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ola Spjuth
  *     
  ******************************************************************************/
 package net.bioclipse.metaprint2d.ui.business;
 
  import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.ui.IEditorPart;
 import org.openscience.cdk.exception.CDKException;
 
 import net.sf.metaprint2d.MetaPrintResult;
 import net.sf.metaprint2d.mol2.Molecule;
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.core.PublishedClass;
 import net.bioclipse.core.PublishedMethod;
 import net.bioclipse.core.Recorded;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.domain.IMolecule;
 import net.bioclipse.managers.business.IBioclipseManager;
 import net.bioclipse.metaprint2d.ui.model.MetaPrint2DCalculation;
 
 @PublishedClass( "Contains methods for calling MetaPrint2D")
 /**
  * 
  * @author ola
  *
  */
 public interface IMetaPrint2DManager extends IBioclipseManager{
 
     /**
      * Run MetaPrint2D on a molecule
      * @param molecule The IMolecule to run Metaprint2D on.
      * @throws BioclipseException 
      * @throws InvocationTargetException 
      */
     @PublishedMethod( methodSummary = "Run MetaPrint2D on a molecule.",
                       params = "IMolecule molecule" )
                       @Recorded
                       public List<MetaPrintResult> calculate(IMolecule molecule) throws BioclipseException, InvocationTargetException;
 
     /**
      * Run MetaPrint2D on a molecule
      * @param molecule The IMolecule to run Metaprint2D on.
      * @throws BioclipseException 
      * @throws InvocationTargetException 
      */
     @PublishedMethod( methodSummary = "Run MetaPrint2D on a molecule.",
                       params = "IMolecule molecule, " +
    "storeResults: true if resutlts should be stored as properties in AC." )
     @Recorded
     public List<MetaPrintResult> calculate(IMolecule molecule, boolean storeResults) throws BioclipseException, InvocationTargetException;
 
     /**
      * Run MetaPrint2D on a molecular file
      * @param file a file comprising one or more molecules
      * @throws CoreException 
      * @throws BioclipseException 
      * @throws IOException 
      * @throws InvocationTargetException 
      * @throws CDKException 
      */
     public Map<IMolecule, List<MetaPrintResult>> calculate(IFile file, boolean storeResults) throws IOException, BioclipseException, CoreException, InvocationTargetException, CDKException;
 
     /**
      * Run MetaPrint2D on a molecular file
      * @param file a file comprising one or more molecules
      * @throws CoreException 
      * @throws BioclipseException 
      * @throws IOException 
      * @throws InvocationTargetException 
      */
     public Map<IMolecule, List<MetaPrintResult>> calculate(IFile file) throws IOException, BioclipseException, CoreException, InvocationTargetException;
 
 
     /**
      * Run MetaPrint2D on a molecular file.
      * @param path String that points to a file comprising one or more molecules
      * @throws InvocationTargetException 
      * @throws CoreException 
      * @throws BioclipseException 
      * @throws IOException 
      * @throws CDKException 
      */
     @PublishedMethod( methodSummary = "Run MetaPrint2D on a molecular file.",
                       params = "String path: points to a file comprising " +
     "one or more molecules." )
     @Recorded
     public Map<IMolecule, List<MetaPrintResult>> calculate(String path) throws IOException, BioclipseException, CoreException, InvocationTargetException, CDKException;
 
     /**
      * Run MetaPrint2D on a molecular file.
      * @param path String that points to a file comprising one or more molecules
      * @throws InvocationTargetException 
      * @throws CoreException 
      * @throws BioclipseException 
      * @throws IOException 
      * @throws CDKException 
      */
     @PublishedMethod( methodSummary = "Run MetaPrint2D on a molecular file.",
                       params = "String path: points to a file comprising " +
                       "one or more molecules, " +
    "storeResults: true if resutlts should be stored as properties in AC." )    
     @Recorded
     public Map<IMolecule, List<MetaPrintResult>> calculate(String path, boolean storeResults)
     throws IOException, BioclipseException, CoreException, InvocationTargetException, CDKException;
 
     /**
      * 
      * @param mols
      * @return
      * @throws BioclipseException
      * @throws InvocationTargetException
      */
     @PublishedMethod( methodSummary = "Run MetaPrint2D on a list of IMolecules. " +
                       "Does not store results as properties.",
                       params = "List<IMolecule> molecules: List of molecules" )
                       @Recorded
                       public Map<IMolecule, List<MetaPrintResult>> calculate(List<? extends IMolecule> molecules)
                       throws BioclipseException, InvocationTargetException;
 
     /**
      * 
      * @param mols
      * @return
      * @throws BioclipseException
      * @throws InvocationTargetException
      */
     @PublishedMethod( methodSummary = "Run MetaPrint2D on a list of IMolecules.",
                       params = "List<IMolecule> molecules: List of molecules, " +
    "storeResults: true if resutlts should be stored as properties in AC." )
     @Recorded
     Map<IMolecule, List<MetaPrintResult>> calculate(List<? extends IMolecule> mols,
                                                     boolean storeResults) throws BioclipseException,
                                                     InvocationTargetException;
 
     Map<IEditorPart, MetaPrint2DCalculation> getCalculationMap();
 
     Molecule doSybylAtomTyping(ICDKMolecule cdkmol)
     throws InvocationTargetException;
 
     /**
      * 
      * @param string Database to set, one of 'All', 'Human', 'Dog', and 'Rat'.
      */
     @PublishedMethod( methodSummary = "Set the active MetaPrint2D Database.",
                       params = "database: String to set; one of 'All', 'Human'," +
                       		" 'Dog', or 'Rat'" )
     @Recorded
     public void setDataBase( String database );
 
     /**
      * Gets the currently active MetaPrint2D database.
      */
     @PublishedMethod( methodSummary = "Gets the currently active MetaPrint2D database."
                       )
     @Recorded
     public String getActiveDatabase();
 
     /**
      * Remove the M2D property on the AC
      * @param mol
      */
     @PublishedMethod( methodSummary = "Clear MetaPrint2D results for the molecule"
     )
     @Recorded
     void clear( ICDKMolecule mol );
     
     /**
      * Remove the M2D property on the AC of all mols
      * @param mol
      */
     @PublishedMethod( methodSummary = "Clear MetaPrint2D results for the molecules"
     )
     @Recorded
     void clear( List<? extends ICDKMolecule> mols );
 
 }
