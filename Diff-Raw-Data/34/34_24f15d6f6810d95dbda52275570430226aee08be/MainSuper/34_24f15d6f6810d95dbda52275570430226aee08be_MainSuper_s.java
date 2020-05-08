 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: MainSuper.java,v 1.25 2006-05-08 16:13:52 aracic Exp $
  */
 
 package org.caesarj.compiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import org.caesarj.classfile.ClassFileFormatException;
 import org.caesarj.classfile.ClassInfo;
 import org.caesarj.compiler.ast.phylum.JCompilationUnit;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.export.CSourceClass;
 import org.caesarj.compiler.optimize.BytecodeOptimizer;
 import org.caesarj.compiler.types.CStdType;
 import org.caesarj.compiler.types.KjcSignatureParser;
 import org.caesarj.compiler.types.KjcTypeFactory;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.InconsistencyException;
 import org.caesarj.util.Messages;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.UnpositionedError;
 import org.caesarj.util.Utils;
 
 /**
  * This class implements the entry point of the Java compiler
  */
 public abstract class MainSuper extends CompilerBase {
 
     // ----------------------------------------------------------------------
     // ENTRY POINT
     // ----------------------------------------------------------------------
 
     /**
      * Creates a new compiler instance.
      *
      * @param	workingDirectory	the working directory
      * @param	diagnosticOutput	the diagnostic output stream
      */
     public MainSuper(String workingDirectory, PrintWriter diagnosticOutput) {
         super(workingDirectory, diagnosticOutput);
     }
 
     // --------------------------------------------------------------------
     // Language
     // --------------------------------------------------------------------
 
     /**
      * Sets the version of the source code 
      * 
      * @param     version        version  of the source code
      */
     public void setSourceVersion(int version) {
         this.version = version;
     }
 
     /**
      * Returns the version of the source code
      * 
      * @return     version of the code
      */
     public int getSourceVersion() {
         return this.version;
     }
     // ----------------------------------------------------------------------
     // RUN FROM COMMAND LINE
     // ----------------------------------------------------------------------
 
     /**
      * Runs a compilation session
      *
      * @param	args		the command line arguments
      */
     public abstract boolean run(String[] args);
 
     protected KjcEnvironment createEnvironment(KjcOptions options) {
         KjcClassReader reader =
             new KjcClassReader(
                 options.classpath,
                 options.extdirs,
                 new KjcSignatureParser());
         return new KjcEnvironment(
             this,
             reader,
             new KjcTypeFactory(reader),
             options);
     }
 
     /**
      * Parse the argument list
      */
     public boolean parseArguments(String[] args) {
         options = new KjcOptions();
         if (!options.parseCommandLine(args)) {
             return false;
         }
         infiles = Utils.toVector(options.nonOptions);
         return true;
     }
 
     /**
      * Generates the code from an array of compilation unit and
      * a destination
      *
      * @param	destination	the directory where to write classfiles
      */
     public void genCode(TypeFactory factory, JCompilationUnit cus[]) {
     	ArrayList<CSourceClass> classes = new ArrayList<CSourceClass>(100);
     	
     	for (JCompilationUnit cu : cus) {
     		classes.addAll(cu.getContext().getAndCleanSourceClasses());
     	}    	                
         
         BytecodeOptimizer optimizer = new BytecodeOptimizer(options.optimize);
 
         try {        	
 //        	int 
 //        		javaTypeCounter = 0,
 //        		caesarTypeCounter = 0,
 //        		abstractImplicitTypeCounter = 0,
 //        		topLevelCClassCounter = 0,
 //        		explicitCaesarTypeCounter = 0;        	
         	
             for (CSourceClass clazz : classes) {
             	
 //            	if(clazz.isMixinInterface()) {
 //            		caesarTypeCounter++;
 //            		
 //            		if(!clazz.isImplicit()) {
 //                		explicitCaesarTypeCounter++;
 //                		if(!clazz.isNested()) {
 //                			topLevelCClassCounter++;
 //                		}            			
 //                	}
 //            	}
 //            	else if(!clazz.isMixin() && !clazz.isImplicit()) {
 //            		javaTypeCounter++;
 //            	}
             	
            	if(!clazz.isMixinInterface() && clazz.isImplicit()) {
            		if(
        				clazz.isAbstract()
        				|| (clazz.isNested() && clazz.getOwner().isAbstract())
    				) {
//	            		abstractImplicitTypeCounter++;
	                    continue;
            		}
                 }            	
             	
                 // IVICA
                 // removed: classes[count].genCode(optimizer, options.destination, factory);
                 ClassInfo classInfo = 
                     clazz.genClassInfo(optimizer, options.destination, factory);
                 
                 classInfo.write(options.destination);
                 byte[] codeBuf = classInfo.getByteArray();
                 
                 byteCodeMap.addSourceClass(clazz, codeBuf);                
                 // IVICA:END
                 
                 //Log.verbose("class file generated: "+classes[count].getQualifiedName());                
             }
             
 //            System.out.println("JAVA TYPES              : "+javaTypeCounter);
 //            System.out.println("CAESAR TYPES            : "+caesarTypeCounter);
 //            System.out.println("IMPLICIT ABSTRACT TYPES : "+abstractImplicitTypeCounter);
 //            System.out.println("EXPLICIT CAESAR TYPES   : "+explicitCaesarTypeCounter);
 //            System.out.println("TOP-LEVEL CAESAR TYPES  : "+topLevelCClassCounter);            
         }
         catch (PositionedError e) {
             reportTrouble(e);
         }
         catch (ClassFileFormatException e) {
             e.printStackTrace();
             reportTrouble(
                 new UnpositionedError(
                     Messages.FORMATTED_ERROR,
                     e.getMessage()));
         }
         catch (IOException e) {
             reportTrouble(new UnpositionedError(
                 Messages.IO_EXCEPTION,
                 "classfile",
             //!!!FIXME !!!
             e.getMessage()));
         }
     }
 
     /**
      * Initialize the compiler (read classpath, check classes.zip)
      */
     protected void initialize(KjcEnvironment environment) {
         //    ClassPath.init(options.classpath);
         CStdType.init(this, environment);
     }
 
     /**
      * returns true iff compilation in verbose mode is requested.
      */
     public boolean verboseMode() {
         return options.verbose;
     }
 
     // ----------------------------------------------------------------------
     // PROTECTED METHODS
     // ----------------------------------------------------------------------
 
     /**
      * parse the givven file and return a compilation unit
      * side effect: increment error number
      * @param	file		the name of the file (assert exists)
      * @return	the compilation unit defined by this file
      */
     protected abstract JCompilationUnit parseFile(
         File file,
         KjcEnvironment environment);
    
     /**
      * check that interface of a given compilation unit is correct
      * side effect: increment error number
      * @param	cunit		the compilation unit
      */
     protected void checkInterface(JCompilationUnit cunit) {
         try {
             cunit.checkInterface(this);
         }
         catch (PositionedError e) {
             reportTrouble(e);
         }
     }
 
     /**
      * check that interface of a given compilation unit is correct
      * side effect: increment error number
      * @param	cunit		the compilation unit
      */
     protected void checkInitializers(JCompilationUnit cunit) {
         try {
             cunit.checkInitializers(this);
         }
         catch (PositionedError e) {
             reportTrouble(e);
         }
     }
 
     /**
      * check that body of a given compilation unit is correct
      * side effect: increment error number
      * @param	cunit		the compilation unit
      */
     protected void checkBody(JCompilationUnit cunit) {
         try {
             cunit.checkBody(this);
             //Log.verbose("body checked: "+cunit.getFileName());
         }
         catch (PositionedError e) {            
             reportTrouble(e);
         }        
     }
     /**
      * check the conditions
      * side effect: increment error number
      * @param	cunit		the compilation unit
      */
     protected void checkCondition(JCompilationUnit cunit) {
         try {
             cunit.analyseConditions();
         }
         catch (PositionedError e) {
             reportTrouble(e);
         }
     }
 
 
 
     // --------------------------------------------------------------------
     // COMPILER
     // --------------------------------------------------------------------
 
     /**
      * Reports a trouble (error or warning).
      *
      * @param	trouble		a description of the trouble to report.
      */
     public void reportTrouble(PositionedError trouble) {
         if (trouble instanceof CWarning) {
             if (options.warning != 0 && filterWarning((CWarning)trouble)) {
                 Log.warning(trouble.getMessage());
             }
         }
         else {
             Log.error(trouble.getMessage());
             errorFound = true;            
         }
     }
 
     /**
      * Reports a trouble.
      *
      * @param	trouble		a description of the trouble to report.
      */
     public void reportTrouble(UnpositionedError trouble) {
         Log.error(trouble.getMessage());
         errorFound = true;
     }
 
     protected boolean filterWarning(CWarning warning) {
         WarningFilter filter = getFilter();
         int value = filter.filter(warning);
 
         switch (value) {
             case WarningFilter.FLT_REJECT :
                 return false;
             case WarningFilter.FLT_FORCE :
                 return true;
             case WarningFilter.FLT_ACCEPT :
                 return warning.getSeverityLevel() <= options.warning;
             default :
                 throw new InconsistencyException();
         }
     }
 
     protected WarningFilter getFilter() {
         if (filter == null) {
             if (options.filter != null) {
                 try {
                     filter =
                         (WarningFilter)Class
                             .forName(options.filter)
                             .newInstance();
                 }
                 catch (Exception e) {
                     Log.error(KjcMessages.FILTER_NOT_FOUND.format(new Object[]{options.filter}));
                 }
             }
             if (filter == null) {
                 filter = new DefaultFilter();
             }
         }
 
         return filter;
     }
 
     /**
      * Returns true iff comments should be parsed (false if to be skipped)
      */
     public boolean parseComments() {
     	return false;
     }
 
     // ----------------------------------------------------------------------
     // PROTECTED DATA MEMBERS
     // ----------------------------------------------------------------------
 
     protected Vector infiles = new Vector();
     protected boolean errorFound;
 
     protected KjcOptions options;
     private int version = 0;
 
     // it must be initialized to null otherwise the filter option is not used
     private WarningFilter filter = null;
 
     // VAIDAS byteCodeMap
     protected ByteCodeMap byteCodeMap = null;
 }
