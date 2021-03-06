 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Feb 2, 2005
  */
 package org.eclipse.mylar.bugs.search;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.mylar.bugs.BugzillaSearchManager;
 import org.eclipse.mylar.bugs.BugzillaStructureBridge;
 import org.eclipse.mylar.bugs.MylarBugsPlugin;
 import org.eclipse.mylar.bugzilla.ui.tasklist.BugzillaReportNode;
 import org.eclipse.mylar.core.AbstractRelationshipProvider;
 import org.eclipse.mylar.core.IMylarContextNode;
 import org.eclipse.mylar.core.search.IActiveSearchListener;
 import org.eclipse.mylar.core.search.IMylarSearchOperation;
 import org.eclipse.ui.PlatformUI;
 
 
 /**
  * @author Shawn Minto
  */
 public class BugzillaReferencesProvider extends AbstractRelationshipProvider {
 
     public static final String ID = "org.eclipse.mylar.bugs.search.references";
    public static final String NAME = "referenced by";
     public static final int DEFAULT_DEGREE = 0;
     
     public BugzillaReferencesProvider() {
         super(BugzillaStructureBridge.CONTENT_TYPE, ID);
     }
 
     protected boolean acceptElement(IJavaElement javaElement) {
         return javaElement != null 
             && (javaElement instanceof IMember || javaElement instanceof IType) && javaElement.exists();
     }
     
     /**
      * HACK: checking kind as string - don't want the dependancy to mylar.java
      */
     @Override
     protected void findRelated(final IMylarContextNode node, int degreeOfSeparation) {
         if (!node.getContentKind().equals("java")) return; 
         IJavaElement javaElement = JavaCore.create(node.getElementHandle());
         if (!acceptElement(javaElement)) {
             return; 
         }
         runJob(node,   degreeOfSeparation);
     }
 
 	@Override
 	public IMylarSearchOperation getSearchOperation(IMylarContextNode node, int limitTo, int degreeOfSepatation) {
 		IJavaElement javaElement = JavaCore.create(node.getElementHandle());
 		return new BugzillaMylarSearch(degreeOfSepatation, javaElement); 
 	}
     
 	private void runJob(final IMylarContextNode node,  final int degreeOfSeparation) {
 		BugzillaMylarSearch search = (BugzillaMylarSearch)getSearchOperation(node, 0, degreeOfSeparation);        
 		
         search.addListener(new IActiveSearchListener(){
 
         	private boolean gathered = false;
         	
             public void searchCompleted(List<?> nodes) {
                 Iterator<?> itr = nodes.iterator();
 
                 if(MylarBugsPlugin.getDefault() == null)
                 	return;
                 
                 while(itr.hasNext()) {
                     Object o = itr.next();
                     if(o instanceof BugzillaReportNode){
                         BugzillaReportNode bugzillaNode = (BugzillaReportNode)o;
                         final String handle = bugzillaNode.getElementHandle();
                         if(MylarBugsPlugin.getDefault().getCache().getCached(handle) == null)
                         	cache(handle, bugzillaNode);
                         
                         PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){
 							public void run() {
 								incrementInterest(node, BugzillaStructureBridge.CONTENT_TYPE, handle, degreeOfSeparation);
 							}
                         });
                     }
                 }
                 gathered = true;
                 BugzillaReferencesProvider.this.searchCompleted(node);
             }
 
 			public boolean resultsGathered() {
 				return gathered;
 			}
             
         });
         search.run(new NullProgressMonitor(), Job.DECORATE);
 	}
 
 	@Override
 	public String getGenericId() {
 		return ID;
 	}
 	
 	@Override
     protected String getSourceId() {
         return ID;
     }
 
     @Override
     public String getName() {
         return NAME;
     }
     
     /*
      * 
      * STUFF FOR TEMPORARILY CACHING A PROXY REPORT
      * 
      * TODO remove the proxys and update the BugzillaStructureBridge cache so that on restart, 
      * we dont have to get all of the bugs
      * 
      */
     private static final Map<String, BugzillaReportNode> reports = new HashMap<String, BugzillaReportNode>();
 	
 	public BugzillaReportNode getCached(String handle){
 		return reports.get(handle);
 	}
 	
     protected void cache(String handle, BugzillaReportNode bugzillaNode) {
     	reports.put(handle, bugzillaNode);
 	}
     
     public void clearCachedReports(){
     	reports.clear();    	
     }
 
 	public Collection<? extends String> getCachedHandles() {
 		return reports.keySet();
 	}
 
 	@Override
 	public void stopAllRunningJobs() {
 		BugzillaSearchManager.cancelAllRunningJobs();
 		
 	}
 	
 	@Override
 	protected int getDefaultDegreeOfSeparation() {
 		return DEFAULT_DEGREE;
 	}
 }
