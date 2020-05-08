 /*******************************************************************************
  * Copyright (c) 2009 Ola Spjuth.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ola Spjuth - initial API and implementation
  ******************************************************************************/
 package net.bioclipse.metaprint2d.ui.actions;
 
  import java.awt.Color;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 
 import net.bioclipse.cdk.domain.CDKMolecule;
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.cdk.ui.sdfeditor.editor.IRenderer2DConfigurator;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.metaprint2d.Metaprinter;
 import net.bioclipse.metaprint2d.ui.Activator;
 import net.bioclipse.metaprint2d.ui.MetaPrint2DHelper;
 import net.bioclipse.metaprint2d.ui.Metaprint2DConstants;
 import net.bioclipse.metaprint2d.ui.Metaprint2DPropertyColorer;
 import net.bioclipse.metaprint2d.ui.business.IMetaPrint2DManager;
 import net.sf.metaprint2d.MetaPrintResult;
 
 import org.openscience.cdk.interfaces.IAtom;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.renderer.RendererModel;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
 import org.openscience.cdk.renderer.generators.IGeneratorParameter;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomColorer;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomRadius;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactAtom;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator.CompactShape;
 import org.openscience.cdk.renderer.generators.BasicAtomGenerator.KekuleStructure;
 import org.openscience.cdk.renderer.generators.BasicSceneGenerator.BackGroundColor;
 
 public class MetaPrint2DAtomColorConfigurator implements IRenderer2DConfigurator{
 
     //The bondcolor in M2d results
     Color bondcolor=new Color(33,33,33);
 
     /**
      * Add tooltip read from M2D property
      */
     public void configure(RendererModel model, IAtomContainer ac) {
 
         //Get the managers via OSGI
         IMetaPrint2DManager m2d = Activator.getDefault().getMetaPrint2DManager();
 
         //Calculate M2D and store as property
         ICDKMolecule cdkmol=new CDKMolecule(ac);
         try {
             m2d.calculate( cdkmol, true );
             if (!(ac.equals( cdkmol.getAtomContainer() ))){
                 ac=cdkmol.getAtomContainer();
             }
         } catch ( Exception e ) {
             e.printStackTrace();
             return;
         }
         
         //Read M2D property from ac
         String acprop=(String)ac.getProperty( Metaprint2DConstants.METAPRINT_RESULT_PROPERTY );
         
         if (acprop==null || acprop.length()<=0){
             System.out.println("No M2D property found for molecule. No M2D tooltip to have.");
             return;
         }
         
         List<MetaPrintResult> reslist = MetaPrint2DHelper.getResultFromProperty( acprop );
 
         //Store tooltips Atom -> String
         HashMap<IAtom, String> currentToolTip=new HashMap<IAtom, String>();
 
         //Start by coloring all grey
 //        for (int i=0; i< ac.getAtomCount(); i++){
 //            ac.getAtom( i ).setProperty( Metaprint2DConstants.COLOR_PROPERTY, Metaprinter.BLACK_COLOR );
 //        }
 
         //Add tooltip for atoms with result
         for (MetaPrintResult res : reslist){
 
             StringWriter sw=new StringWriter();
             PrintWriter pw=new PrintWriter(sw);
             pw.printf( "%d/%d %1.2f" , res.getReactionCentreCount(), res.getSubstrateCount(), res.getNormalisedRatio());
             String tt=sw.getBuffer().toString();
             currentToolTip.put( ac.getAtom( res.getAtomNumber() ), tt );
 
         }
 
         //Set the PropertyColorer as colorer for the renderermodel
         AtomColorer atomColorer = model.getRenderingParameter( AtomColorer.class );
         if(!(atomColorer.getValue() instanceof Metaprint2DPropertyColorer))
             atomColorer.setValue( new Metaprint2DPropertyColorer() );
 
         //Configure JCP
         model.getRenderingParameter( CompactAtom.class ).setValue( true );
         model.setShowAtomTypeNames( false );
         model.setShowImplicitHydrogens( false );
         model.setShowExplicitHydrogens(  false );
         model.setToolTipTextMap( currentToolTip );
                model.getRenderingParameter( AtomRadius.class ).setValue( 0.0 );
         //       model.setCompactShape(RenderingParameters.AtomShape.OVAL);
 
         //Update drawing
         model.fireChange();
 
     }
 
 
     public void configure(RendererModel model, ICDKMolecule cdkmol) {
         System.out.println("M2d color calculator processing mol: " + cdkmol);
 
         //Get the managers via OSGI
         IMetaPrint2DManager m2d = Activator.getDefault().getMetaPrint2DManager();
 
         IAtomContainer ac=cdkmol.getAtomContainer();
 
         List<MetaPrintResult> scores=null;
         try {
             scores = m2d.calculate(cdkmol);
         } catch (BioclipseException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
 
         //Store tooltips Atom -> String
         HashMap<IAtom, String> currentToolTip=new HashMap<IAtom, String>();
 
         //Color by metaprint
        model.getColorHash().clear();
         for (int i=0; i< ac.getAtomCount(); i++){
             //            Atom metat=metamol.getAtom( i );
             MetaPrintResult res=scores.get(i);
             Color color=Metaprinter.getColorByMetprint(res);
             //            System.out.println("Atom: " + i + " Color:" + color);
             if (color !=null){
                 //For IAtomColorer
                 ac.getAtom( i ).setProperty( Metaprint2DConstants.COLOR_PROPERTY, color );
                 System.out.println("Coloring atom: " + i + " to " + color.toString());
 
                 //For Background
                 //                model.getColorHash().put(ac.getAtom( i ), color);
 
             }
             else{
                 //For IAtomColorer
                 ac.getAtom( i ).setProperty( Metaprint2DConstants.COLOR_PROPERTY, Metaprinter.BLACK_COLOR );
                 System.out.println("Coloring atom: " + i + " to " + Metaprinter.BLACK_COLOR);
 
                 //For Background
                 //                model.getColorHash().put(ac.getAtom( i ), Metaprinter.WHITE_COLOR);
 
             }
 
             //Add tooltip
             StringWriter sw=new StringWriter();
             PrintWriter pw=new PrintWriter(sw);
             pw.printf( "%d/%d %1.2f" , res.getReactionCentreCount(), res.getSubstrateCount(), res.getNormalisedRatio());
             String tt=sw.getBuffer().toString();
             //            String s= String.format( "%1$/%2$ %3$.2f", res.getReactionCentreCount(), res.getSubstrateCount(), res.getNormalisedRatio() );
             currentToolTip.put( ac.getAtom( i ), tt );
 
         }
 
         model.getRenderingParameter( AtomColorer.class ).setValue(new Metaprint2DPropertyColorer());
 
         //Configure JCP
         setRendererParameter( model, BackGroundColor.class, bondcolor);
         setRendererParameter( model, KekuleStructure.class,  true );
         model.setShowAtomTypeNames( false );
         model.setShowImplicitHydrogens( false );
         model.setShowExplicitHydrogens(  false );
         model.setToolTipTextMap( currentToolTip );
         setRendererParameter( model, AtomRadius.class, 8d );
         setRendererParameter( model, CompactShape.class,
                               BasicAtomGenerator.Shape.OVAL);
 
         //Update drawing
         model.fireChange();
 
         System.out.println("M2d color calculator processing mol: " + cdkmol + " finished");
 
 
     }
 
     private <T extends IGeneratorParameter<S>, S>
       void setRendererParameter(RendererModel model, Class<T > param, S value) {
         T parameter = model.getRenderingParameter( param );
         parameter.setValue( value );
     }
 
 }
