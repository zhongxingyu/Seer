 /**
  * Copyright (c) 2008-2010 Sonatype, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sonatype, Inc. - initial API and implementation
  */
 package com.sonatype.m2e.subversive.internal;
 
 import org.eclipse.core.resources.team.FileModificationValidator;
 import org.eclipse.team.svn.core.connector.ISVNCredentialsPrompt;
 import org.eclipse.team.svn.core.connector.SVNProperty;
 import org.eclipse.team.svn.core.extension.options.IOptionProvider;
 import org.eclipse.team.svn.core.operation.CompositeOperation;
 import org.eclipse.team.svn.core.utility.ILoggedOperationFactory;
 
 public class NonInteractiveOptionProvider
     implements IOptionProvider
 {
 
     private final IOptionProvider delegate;
 
     public NonInteractiveOptionProvider( IOptionProvider delegate )
     {
         this.delegate = ( delegate != null ) ? delegate : IOptionProvider.DEFAULT;
     }
 
     public ISVNCredentialsPrompt getCredentialsPrompt()
     {
         // suppress any dialogs
         return null;
     }
 
     public void addProjectSetCapabilityProcessing( CompositeOperation op )
     {
         delegate.addProjectSetCapabilityProcessing( op );
     }
 
     public SVNProperty[] getAutomaticProperties( String template )
     {
         return delegate.getAutomaticProperties( template );
     }
 
     public String getDefaultBranchesName()
     {
         return delegate.getDefaultBranchesName();
     }
 
     public String getDefaultTagsName()
     {
         return delegate.getDefaultTagsName();
     }
 
     public String getDefaultTrunkName()
     {
         return delegate.getDefaultTrunkName();
     }
 
     public ILoggedOperationFactory getLoggedOperationFactory()
     {
         return delegate.getLoggedOperationFactory();
     }
 
     public String getResource( String key )
     {
         return delegate.getResource( key );
     }
 
     public String getSVNConnectorId()
     {
         return delegate.getSVNConnectorId();
     }
 
     public boolean isAutomaticProjectShareEnabled()
     {
         return delegate.isAutomaticProjectShareEnabled();
     }
 
     public boolean isSVNCacheEnabled()
     {
         return delegate.isSVNCacheEnabled();
     }
 
     public FileModificationValidator getFileModificationValidator()
     {
         return delegate.getFileModificationValidator();
     }
 
     public boolean isTextMIMETypeRequired()
     {
         // return SVNTeamPreferences.getPropertiesBoolean(SVNTeamUIPlugin.instance().getPreferenceStore(),
         // SVNTeamPreferences.FORCE_TEXT_MIME_NAME);
         return true;
     }
 
    public boolean isPersistentSSHEnabled()
    {
    	return false;
    }
 }
