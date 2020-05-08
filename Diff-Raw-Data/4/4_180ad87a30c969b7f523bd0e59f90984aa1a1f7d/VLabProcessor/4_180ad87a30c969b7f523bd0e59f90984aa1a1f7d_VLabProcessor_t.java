 /*
  * Copyright (C) 2010-2013 Netcetera Switzerland (info@netcetera.com)
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option)
  * any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see http://www.gnu.org/licenses/
  *
  * @(#) $Id: $
  */
 
 package com.netcetera.vlab;
 
 import com.netcetera.vlab.IVLabProcessor;
 import com.netcetera.vlab.VLabJythonFactory;
 import com.netcetera.vlab.VLabUi;
 
 import com.bc.ceres.core.ProgressMonitor;
 
 import org.apache.commons.lang.StringUtils;
 import org.esa.beam.framework.processor.Processor;
 import org.esa.beam.framework.processor.ProcessorException;
 import org.esa.beam.framework.processor.ui.ProcessorUI;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 public class VLabProcessor extends Processor {
 	
     private static final String JYTHON_PROC_ICLASSNAME = "com.netcetera.vlab.IVLabProcessor";
     private static final String JYTHON_IMPL_CLASSNAME  = "VLabImpl";
     private static final String VLAB_SYMNAME           = "beam-vlab";
     public  static final String HELP_ID                = "vlab";
     private static final String VLAB_LOGGERID          = "beam.processor.vlab";
     private final Logger _logger                       = Logger.getLogger(VLAB_LOGGERID);
 
     public static File            auxdataInstallDir;
     private ProcessorUI           _processorUI;
     private int                   _progressBarDepth;
     private static IVLabProcessor delegate;
 
     public VLabProcessor() {
         auxdataInstallDir = super.getDefaultAuxdataInstallDir();
         _logger.info("instantiating VLabProcessor delegate...");
         try {
			installAuxdata();
 			delegate = (IVLabProcessor) VLabJythonFactory.getJythonObject(
 					JYTHON_PROC_ICLASSNAME, new File(auxdataInstallDir,
 							JYTHON_IMPL_CLASSNAME + ".py").getAbsolutePath(),
 					JYTHON_IMPL_CLASSNAME);
         } catch (Exception e) {
         	throw new RuntimeException(e.getMessage() + "\n__________\n" + StringUtils.join(e.getStackTrace(), "\n"));
         }
         _progressBarDepth = 3;
         setDefaultHelpId(HELP_ID);
     }
 
     public void setProgressBarDepth(int progessBarDepth)  { _progressBarDepth = progessBarDepth; }
    public void initProcessor() throws ProcessorException { }
     @Override public void   process(ProgressMonitor pm)   throws ProcessorException {
     	try {
 			delegate.process(pm, getRequest());
 		} catch (Exception e) {
 			throw new ProcessorException(e.getMessage() + "\n__________\n" + StringUtils.join(e.getStackTrace(), "\n"));
 		}
     }
     @Override public String getUITitle()                  { return delegate.getUITitle(); }
     @Override public String getName()                     { return delegate.getName(); }
     @Override public String getVersion()                  { return delegate.getVersion(); }
     @Override public String getCopyrightInformation()     { return delegate.getCopyrightInformation(); }
     @Override public int getProgressDepth()               { return _progressBarDepth; }
     // @TODO: see why delegate.getSymbolicName() doesn't work
     @Override public String getSymbolicName()             { return VLAB_SYMNAME; }
   
     @Override
     public ProcessorUI createUI() throws ProcessorException {
         if (_processorUI == null) { _processorUI = new VLabUi(); }
         return _processorUI;
     }
 }
