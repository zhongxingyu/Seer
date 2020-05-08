 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.ui.eclipse.editors;
 
 import java.rmi.RemoteException;
 import java.text.Collator;
 import java.util.Locale;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 import de.tuilmenau.ics.fog.eclipse.ui.editors.EditorInput;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.facade.Signature;
 import de.tuilmenau.ics.fog.packets.hierarchical.TopologyEnvelope.FIBEntry;
 import de.tuilmenau.ics.fog.routing.Route;
 import de.tuilmenau.ics.fog.routing.hierarchical.Coordinator;
 import de.tuilmenau.ics.fog.routing.hierarchical.CoordinatorCEPDemultiplexed;
 import de.tuilmenau.ics.fog.routing.hierarchical.ElectionProcess;
 import de.tuilmenau.ics.fog.routing.hierarchical.ElectionProcess.ElectionManager;
 import de.tuilmenau.ics.fog.routing.hierarchical.HRMConfig;
 import de.tuilmenau.ics.fog.routing.hierarchical.clusters.AttachedCluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clusters.Cluster;
 import de.tuilmenau.ics.fog.routing.hierarchical.clusters.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.hierarchical.clusters.ClusterManager;
 import de.tuilmenau.ics.fog.routing.hierarchical.clusters.IntermediateCluster;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 /**
  * Editor for showing and editing the internals of a bus.
  */
 public class CoordinatorEditor extends EditorPart
 {
 	private Coordinator mCoordinator = null;
     private Composite mShell = null;
     private ScrolledComposite mScroller = null;
     private Composite mContainer = null;
 	
 	public CoordinatorEditor()
 	{
 	}
 	
 	@Override
 	public void createPartControl(Composite parent)
 	{
 		mShell = parent;
 		mShell.setLayout(new FillLayout());
 		mScroller = new ScrolledComposite(mShell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 		mContainer = new Composite(mScroller, SWT.NONE);
 		mScroller.setContent(mContainer);
 		GridLayout tLayout = new GridLayout(1, true);
 		mContainer.setLayout(tLayout);
 		
 		for(int i = 0; i <= HRMConfig.Routing.HIERARCHY_LEVEL_AMOUNT; i++) {
 			Logging.log(this, "Amount of found clusters: " + mCoordinator.getClusters().size());
 			int j = -1;
 			for(Cluster tCluster : mCoordinator.getClusters()) {
 				j++;
 				Logging.log(this, "Printing cluster " + j + ": " + tCluster.toString());
 				if( !(tCluster instanceof AttachedCluster) && tCluster.getLevel() == i) {
 					printCluster(tCluster);
 				}
 			}
 		}
 		
 		Text overviewText = new Text(mContainer, SWT.BORDER);;
 		overviewText.setText("Approved signatures: " + mCoordinator.getApprovedSignatures());
 		
 		int j = 0;
 		final Table tMappingTable = new Table(mContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		
 		TableColumn tColumnHRMID = new TableColumn(tMappingTable, SWT.NONE, 0);
 		tColumnHRMID.setText("HRMID");
 		TableColumn tColumnNextHop = new TableColumn(tMappingTable, SWT.NONE, 1);
 		tColumnNextHop.setText("next hop");
 		TableColumn tColumnNextCluster = new TableColumn(tMappingTable, SWT.NONE, 2);
 		tColumnNextCluster.setText("next cluster");
 		TableColumn tColumnFarthestCluster = new TableColumn(tMappingTable, SWT.NONE, 3);
 		tColumnFarthestCluster.setText("farthest cluster");
 		TableColumn tColumnRoute = new TableColumn(tMappingTable, SWT.NONE, 4);
 		tColumnRoute.setText("route");
 		TableColumn tColumnOrigin = new TableColumn(tMappingTable, SWT.NONE, 5);
 		tColumnOrigin.setText("origin");
 		
 		if(mCoordinator.getHRS().getRoutingTable() != null && !mCoordinator.getHRS().getRoutingTable().isEmpty()) {
 			for(HRMID tHRMID : mCoordinator.getHRS().getRoutingTable().keySet()) {
 				TableItem item = new TableItem(tMappingTable, SWT.NONE, j);
 				item.setText(0, tHRMID != null ? tHRMID.toString() : "");
 				item.setText(1, mCoordinator.getHRS().getFIBEntry(tHRMID).getNextHop() != null ? mCoordinator.getHRS().getFIBEntry(tHRMID).getNextHop().toString() : "UNKNOWN");
 				item.setText(2, mCoordinator.getHRS().getFIBEntry(tHRMID).getNextCluster()!=null && mCoordinator.getCluster(mCoordinator.getHRS().getFIBEntry(tHRMID).getNextCluster()) != null ? mCoordinator.getCluster(mCoordinator.getHRS().getFIBEntry(tHRMID).getNextCluster()).toString() : "UNKNOWN");
 				item.setText(3, mCoordinator.getHRS().getFIBEntry(tHRMID).getFarthestClusterInDirection()!=null && mCoordinator.getCluster(mCoordinator.getHRS().getFIBEntry(tHRMID).getFarthestClusterInDirection()) != null ? mCoordinator.getCluster(mCoordinator.getHRS().getFIBEntry(tHRMID).getFarthestClusterInDirection()).toString() : "UNKNOWN");
 				item.setText(4, mCoordinator.getHRS().getFIBEntry(tHRMID).getRouteToTarget()!=null ? mCoordinator.getHRS().getFIBEntry(tHRMID).getRouteToTarget().toString() : "UNKNOWN");
 				item.setText(5, mCoordinator.getHRS().getFIBEntry(tHRMID).getSignature()!=null ? mCoordinator.getHRS().getFIBEntry(tHRMID).getSignature().toString() : "UNKNOWN");
 				
 				j++;
 			}
 		}
 		
 		TableColumn[] columns = tMappingTable.getColumns();
 		for(int k=0; k<columns.length; k++) columns[k].pack();
 		tMappingTable.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		
 		tMappingTable.setHeaderVisible(true);
 		tMappingTable.setLinesVisible(true);
 		
 		
 		tColumnHRMID.addListener(SWT.Selection, new Listener() {
 		      public void handleEvent(Event e) {
 		        // sort column 2
 		        TableItem[] items = tMappingTable.getItems();
 		        Collator collator = Collator.getInstance(Locale.getDefault());
 		        for (int i = 1; i < items.length; i++) {
 		          String value1 = items[i].getText(1);
 		          for (int j = 0; j < i; j++) {
 		            String value2 = items[j].getText(1);
 		            if (collator.compare(value1, value2) < 0) {
 		              String[] values = { items[i].getText(0),
 		                  items[i].getText(1) };
 		              items[i].dispose();
 		              TableItem item = new TableItem(tMappingTable, SWT.NONE, j);
 		              item.setText(values);
 		              items = tMappingTable.getItems();
 		              break;
 		            }
 		          }
 		        }
 		      }
 		    });
 		
         mContainer.setSize(mContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 	}
 	
 	public class ElectionOnClusterListener implements Listener
 	{
 		private IntermediateCluster mCluster = null;
 		
 		public ElectionOnClusterListener(IntermediateCluster pCluster)
 		{
 			super();
 			mCluster = pCluster;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			ElectionManager.getElectionManager().getElectionProcess(mCluster.getLevel(), mCluster.getClusterID()).start();
 		}
 		
 	}
 	
 	public class ElectionOnAllClustersListener implements Listener
 	{
 		private IntermediateCluster mCluster = null;
 		
 		public ElectionOnAllClustersListener(IntermediateCluster pCluster)
 		{
 			super();
 			mCluster = pCluster;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			Logging.log("Available Election Processes: ");
 			for(ElectionProcess tProcess : ElectionManager.getElectionManager().getAllElections()) {
 				Logging.log(tProcess.toString());
 			}
 			for(ElectionProcess tProcess : ElectionManager.getElectionManager().getProcessesOnLevel(mCluster.getLevel())) {
 				boolean tStartProcess=true;
 				for(Cluster tCluster : tProcess.getParticipatingClusters()) {
 					for(CoordinatorCEPDemultiplexed tCEP : tCluster.getParticipatingCEPs()) {
 						if(tCEP.isEdgeCEP()) {
 							tStartProcess = false;
 						}
 					}
 				}
 				if(tStartProcess) {
 					tProcess.start();
 				}
 			}
 			
 		}
 		
 	}
 	
 	public class AbovePreparationOnAllClustersListener implements Listener
 	{
 		private IntermediateCluster mCluster = null;
 		
 		public AbovePreparationOnAllClustersListener(IntermediateCluster pCluster)
 		{
 			super();
 			mCluster = pCluster;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			Logging.log("Available Election Processes: ");
 			for(ElectionProcess tProcess : ElectionManager.getElectionManager().getAllElections()) {
 				Logging.log(tProcess.toString());
 			}
 			for(ElectionProcess tProcess : ElectionManager.getElectionManager().getProcessesOnLevel(mCluster.getLevel())) {
 				synchronized(tProcess) {
 					 tProcess.notifyAll();
 				}
 			}
 		}
 		
 	}
 	
 	public class AbovePreparationOnClusterListener implements Listener
 	{
 		private IntermediateCluster mCluster = null;
 		
 		public AbovePreparationOnClusterListener(IntermediateCluster pCluster)
 		{
 			super();
 			mCluster = pCluster;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			synchronized(ElectionManager.getElectionManager().getElectionProcess(mCluster.getLevel(), mCluster.getClusterID())) {
 				ElectionManager.getElectionManager().getElectionProcess(mCluster.getLevel(), mCluster.getClusterID()).notifyAll();
 			}
 		}		
 	}	
 
 	public class AddressDistributionListener implements Listener
 	{
 		private IntermediateCluster mCluster = null;
 		
 		public AddressDistributionListener(IntermediateCluster pCluster)
 		{
 			super();
 			mCluster = pCluster;
 		}
 		
 		@Override
 		public void handleEvent(Event event)
 		{
 			final ClusterManager tManager = new ClusterManager(mCluster, mCluster.getLevel()+1, new HRMID(0));
 			new Thread() {
 	        	public void run()
 	        	{
 	        		try {
 						tManager.distributeAddresses();
 					} catch (RoutingException e) {
 						e.printStackTrace();
 					} catch (RequirementsException e) {
 						e.printStackTrace();
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 	        	}
 	    	}.start();
 		}	
 	}
 	
 	public void printCluster(Cluster pCluster)
 	{
 		Text overviewText = new Text(mContainer, SWT.BORDER);;
 		overviewText.setText(pCluster.toString());
 		
 		if(pCluster instanceof IntermediateCluster) {
 			ToolBar tToolbar = new ToolBar(mContainer, SWT.NONE);
 			
 			ToolItem toolItem1 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem1.setText("Election");
 		    ToolItem toolItem2 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem2.setText("Elect(all)");
 		    ToolItem toolItem3 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem3.setText("Prepare Above");
 		    ToolItem toolItem4 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem4.setText("Prepare Above (all)");
 		    ToolItem toolItem5 = new ToolItem(tToolbar, SWT.PUSH);
 		    toolItem5.setText("Distribute Addresses");
 		    
 		    
 		    toolItem1.addListener(SWT.Selection, new ElectionOnClusterListener((IntermediateCluster)pCluster));
 		    toolItem2.addListener(SWT.Selection, new ElectionOnAllClustersListener((IntermediateCluster)pCluster));
 		    toolItem3.addListener(SWT.Selection, new AbovePreparationOnClusterListener((IntermediateCluster)pCluster));
 		    toolItem4.addListener(SWT.Selection, new AbovePreparationOnAllClustersListener((IntermediateCluster)pCluster));
 		    toolItem5.addListener(SWT.Selection, new AddressDistributionListener((IntermediateCluster)pCluster));
 		    tToolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		}
 		
 		Table tTable = new Table(mContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		TableColumn tColumnCoordinator = new TableColumn(tTable, SWT.NONE, 0);
 		tColumnCoordinator.setText("Coordinator");
 		TableColumn tColumnCEP = new TableColumn(tTable, SWT.NONE, 1);
 		tColumnCEP.setText("Connection Endpoint");
 		TableColumn tColumnTargetCovered = new TableColumn(tTable, SWT.NONE, 2);
 		tColumnTargetCovered.setText("Target Covered");
 		TableColumn tColumnPartofCluster = new TableColumn(tTable, SWT.NONE, 3);
 		tColumnPartofCluster.setText("Part of Cluster");
 		TableColumn tColumnPeerPriority = new TableColumn(tTable, SWT.NONE, 4);
 		tColumnPeerPriority.setText("Peer Priority");
 		TableColumn tColumnNegotiator = new TableColumn(tTable, SWT.NONE, 5);
 		tColumnNegotiator.setText("Negotiatoting Cluster");
 		TableColumn tColumnAnnouncerNegotiator = new TableColumn(tTable, SWT.NONE, 6);
 		tColumnAnnouncerNegotiator.setText("Announcers negotiator");
 		TableColumn tColumnRoute = new TableColumn(tTable, SWT.NONE, 7);
 		tColumnRoute.setText("route");
 		TableColumn tColumnBorder = new TableColumn(tTable, SWT.NONE, 8);
 		tColumnBorder.setText("received BNA");
 		
 		tTable.setHeaderVisible(true);
 		tTable.setLinesVisible(true);
 		
		int j = 0;
 		Logging.log(this, "Amount of participating CEPs is " + pCluster.getParticipatingCEPs().size());
 		for(CoordinatorCEPDemultiplexed tCEP : pCluster.getParticipatingCEPs()) {
 			Logging.log(this, "Printing table item number " + j);
 			TableItem item = new TableItem(tTable, SWT.NONE, j);
 			item.setText(0, (pCluster.getCoordinatorSignature() != null ? pCluster.getCoordinatorSignature().toString() : ""));
 			Name tPeerAddress = tCEP.getPeerName();
 			item.setText(1, tPeerAddress.toString());
 			item.setText(2, tCEP.hasRequestedCoordinator() ? Boolean.toString(tCEP.knowsCoordinator()) : "UNKNOWN");
 			item.setText(3, Boolean.toString(tCEP.isPartOfMyCluster()));
 			item.setText(4, (tCEP.getPeerPriority() != 0 ? Float.toString(tCEP.getPeerPriority()) : "UNKNOWN"));
 			item.setText(5, (tCEP.getRemoteCluster() != null ? tCEP.getRemoteCluster().toString() : "UNKNOWN"));
 			if(tCEP.getRemoteCluster() != null && tCEP.getRemoteCluster() instanceof AttachedCluster && ((AttachedCluster)tCEP.getRemoteCluster()).getAnnouncedCEP(tCEP.getRemoteCluster()) != null && ((AttachedCluster)tCEP.getRemoteCluster()).getAnnouncedCEP(tCEP.getRemoteCluster()).getRemoteCluster() != null) {
 				item.setText(6, ((AttachedCluster)tCEP.getRemoteCluster()).getAnnouncedCEP(tCEP.getRemoteCluster()).getRemoteCluster().toString());
 			}
 			Route tRoute = null;
 			Name tSource = null;
 			Name tTarget = null;
 			try {
 				tSource = tCEP.getSourceName();
 				tTarget = tCEP.getPeerName();
 				if(tSource != null && tTarget != null) {
 					tRoute = mCoordinator.getHRS().getRoute(tCEP.getCoordinator().getPhysicalNode().getCentralFN(), tTarget, new Description(), tCEP.getCoordinator().getPhysicalNode().getIdentity());
 				} else {
 					tRoute = new Route();
 				}
 			} catch (RoutingException tExc) {
 				Logging.err(this, "Unable to compute route to " + tTarget, tExc);
 			} catch (RequirementsException tExc) {
 				Logging.err(this, "Unable to fulfill requirements for route calculation to " + tTarget, tExc);
 			}
 			item.setText(7, (tRoute != null ? tRoute.toString() : "UNKNOWN"));
 			item.setText(8, Boolean.toString(tCEP.receivedBorderNodeAnnouncement()));
 			
 			++j;
 		}
 		
 		TableColumn[] cols = tTable.getColumns();
 		for(int k=0; k<cols.length; k++) cols[k].pack();
 		tTable.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		
 		if((pCluster instanceof IntermediateCluster && ((IntermediateCluster)pCluster).getTopologyEnvelope()!= null && ((IntermediateCluster)pCluster).getTopologyEnvelope().getEntries() != null) || (pCluster instanceof ClusterManager && ((ClusterManager)pCluster).getTopologyEnvelope()!= null && ((ClusterManager)pCluster).getTopologyEnvelope().getEntries() != null) ) {
 			Table tFIB = new Table(mContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 			TableColumn tColumnDestination = new TableColumn(tFIB, SWT.NONE, 0);
 			tColumnDestination.setText("destination");
 			TableColumn tColumnForwardingCluster = new TableColumn(tFIB, SWT.NONE, 1);
 			tColumnForwardingCluster.setText("forwarding cluster");
 			TableColumn tColumnFarthestCluster = new TableColumn(tFIB, SWT.NONE, 2);
 			tColumnFarthestCluster.setText("farthest cluster");
 			TableColumn tColumnNextHop = new TableColumn(tFIB, SWT.NONE, 3);
 			tColumnNextHop.setText("next hop");
 			TableColumn tColumnProposedRoute = new TableColumn(tFIB, SWT.NONE, 4);
 			tColumnProposedRoute.setText("proposed route");
 			TableColumn tColumnOrigin = new TableColumn(tFIB, SWT.NONE, 5);
 			tColumnOrigin.setText("origin");
 			j=0;
 			if(pCluster instanceof IntermediateCluster) {
 				for(FIBEntry tEntry: ((IntermediateCluster)pCluster).getTopologyEnvelope().getEntries()) {
 					TableItem tItem = new TableItem(tFIB, SWT.NONE, j);
 					tItem.setText(0, (tEntry.getDestination() != null ? tEntry.getDestination().toString() : "UNKNOWN"));
 					tItem.setText(1, (tEntry.getNextCluster() != null && mCoordinator.getCluster(tEntry.getNextCluster()) != null ? mCoordinator.getCluster(tEntry.getNextCluster()).toString() : tEntry.getNextCluster().toString()));
 					ClusterDummy tDummy = tEntry.getFarthestClusterInDirection();
 					Cluster tFarthestCluster = null;
 					if(tDummy != null) {
 						tFarthestCluster = mCoordinator.getCluster(tEntry.getFarthestClusterInDirection());
 					}
 					tItem.setText(2, (tFarthestCluster != null ? tFarthestCluster.toString() : "UNKNOWN"));
 					tItem.setText(3, (tEntry.getNextHop() != null ? tEntry.getNextHop().toString() : "UNKNOWN"));
 					tItem.setText(4, (tEntry.getRouteToTarget() != null ? tEntry.getRouteToTarget().toString() : "UNKNOWN"));
 					tItem.setText(5, (tEntry.getSignature() != null ? tEntry.getSignature().toString() : "UNKNOWN"));
 					j++;
 				}
 			} else if(pCluster instanceof ClusterManager) {
 				for(FIBEntry tEntry: ((ClusterManager)pCluster).getTopologyEnvelope().getEntries()) {
 					TableItem tItem = new TableItem(tFIB, SWT.NONE, j);
 					tItem.setText(0, (tEntry.getDestination() != null ? tEntry.getDestination().toString() : "UNKNOWN"));
 					tItem.setText(1, (tEntry.getNextCluster() != null && mCoordinator.getCluster(tEntry.getNextCluster()) != null ? mCoordinator.getCluster(tEntry.getNextCluster()).toString() : tEntry.getNextCluster().toString()));
 					ClusterDummy tDummy = tEntry.getFarthestClusterInDirection();
 					Cluster tFarthestCluster = null;
 					if(tDummy != null) {
 						tFarthestCluster = mCoordinator.getCluster(tEntry.getFarthestClusterInDirection());
 					}
 					tItem.setText(2, (tFarthestCluster != null ? tFarthestCluster.toString() : "UNKNOWN"));
 					tItem.setText(3, (tEntry.getNextHop() != null ? tEntry.getNextHop().toString() : "UNKNOWN"));
 					String tTargetString = (tEntry.getRouteToTarget() != null ? tEntry.getRouteToTarget().toString() : null);
 					if(tTargetString == null) {
 						tTargetString = ((ClusterManager)pCluster).getPathToCoordinator(((ClusterManager)pCluster).getManagedCluster(), pCluster.getCoordinator().getCluster(tEntry.getNextCluster())).toString();
 					}
 					tItem.setText(4, (tEntry.getRouteToTarget() != null ? tEntry.getRouteToTarget().toString() : "UNKNOWN"));
 					tItem.setText(5, (tEntry.getSignature() != null ? tEntry.getSignature().toString() : "UNKNOWN"));
 					j++;
 				}
 			}
 			
 			tFIB.setHeaderVisible(true);
 			tFIB.setLinesVisible(true);
 			TableColumn[] columns = tFIB.getColumns();
 			for(int k=0; k<columns.length; k++) columns[k].pack();
 			tFIB.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		}
 		
 		if(pCluster instanceof ClusterManager) {
 			if(pCluster.getLevel() == 3) {
 				mCoordinator.getLogger().log(this, "Will print cluster manager");
 			}
 			j=0;
 			Table tMappingTable = new Table(mContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 			
 			TableColumn tColumnHRMID = new TableColumn(tMappingTable, SWT.NONE, 0);
 			tColumnHRMID.setText("HRMID");
 			TableColumn tClumnMappedEntry = new TableColumn(tMappingTable, SWT.NONE, 1);
 			tClumnMappedEntry.setText("mapped entity");
 			TableColumn tColumnProvidedPath = new TableColumn(tMappingTable, SWT.NONE, 2);
 			tColumnProvidedPath.setText("provided path");
 			TableColumn tColumnSignature = new TableColumn(tMappingTable, SWT.NONE, 3);
 			tColumnSignature.setText("signature");
 			
 			
 			if(((ClusterManager)pCluster).getMappings() != null && !((ClusterManager)pCluster).getMappings().isEmpty()) {
 				for(HRMID tHRMID : ((ClusterManager)pCluster).getMappings().keySet()) {
 					TableItem item = new TableItem(tMappingTable, SWT.NONE, j);
 					item.setText(0, tHRMID != null ? tHRMID.toString() : "");
 					item.setText(1, ((ClusterManager)pCluster).getVirtualNodeFromHRMID(tHRMID) != null ? ((ClusterManager)pCluster).getVirtualNodeFromHRMID(tHRMID).toString() : "" );
 					item.setText(2, ((ClusterManager)pCluster).getPathFromHRMID(tHRMID) != null ? ((ClusterManager)pCluster).getPathFromHRMID(tHRMID).toString(): "UNKNOWN");
 					Signature tOrigin = null;
 					if(((ClusterManager)pCluster).getTopologyEnvelope() != null && ((ClusterManager)pCluster).getTopologyEnvelope().getEntries() != null) {
 						for(FIBEntry tEntry : ((ClusterManager)pCluster).getTopologyEnvelope().getEntries()) {
 							if(tEntry.equals(tHRMID)) {
 								tOrigin = tEntry.getSignature();
 							}
 						}
 					}
 					item.setText(3, tOrigin != null ? tOrigin.toString() : "UNKNOWN");
 					j++;
 				}
 			}
 			
 			TableColumn[] columns = tMappingTable.getColumns();
 			for(int k=0; k<columns.length; k++) columns[k].pack();
 			tMappingTable.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 			
 			tMappingTable.setHeaderVisible(true);
 			tMappingTable.setLinesVisible(true);
 		}
 		
 		Label separator = new Label (mContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
 		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
 		separator.setVisible(true);
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException
 	{
 		setSite(site);
 		setInput(input);
 		
 		// get selected object to show in editor
 		Object inputObject;
 		if(input instanceof EditorInput) {
 			inputObject = ((EditorInput) input).getObj();
 		} else {
 			inputObject = null;
 		}
 		Logging.log(this, "init editor for " +inputObject + " (class=" +inputObject.getClass() +")");
 		
 		if(inputObject != null) {
 			// update title of editor
 			setTitle(inputObject.toString());
 
 			if(inputObject instanceof Coordinator) {
 				mCoordinator = (Coordinator) inputObject;
 				
 			} else {
 				throw new PartInitException("Invalid input object " +inputObject +". Bus expected.");
 			}
 			setPartName(mCoordinator.toString());
 		} else {
 			throw new PartInitException("No input for editor.");
 		}
 	}
 	
 	@Override
 	public void doSave(IProgressMonitor arg0)
 	{
 	}
 
 	@Override
 	public void doSaveAs()
 	{
 	}
 
 	@Override
 	public boolean isDirty()
 	{
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed()
 	{
 		return false;
 	}
 
 	@Override
 	public void setFocus()
 	{
 	}
 
 	@Override
 	public Object getAdapter(Class required)
 	{
 		if(this.getClass().equals(required)) return this;
 		
 		Object res = super.getAdapter(required);
 		
 		if(res == null) {
 			res = Platform.getAdapterManager().getAdapter(this, required);
 			
 			if(res == null)	res = Platform.getAdapterManager().getAdapter(mCoordinator, required);
 		}
 		
 		return res;
 	}
 
 	public String toString()
 	{
 		return this.getClass().getSimpleName() + "@" + this.hashCode();
 	}
 }
