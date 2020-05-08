 /*******************************************************************************
  * Copyright (c) 14 jun. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.logic.internal;
 
 import static org.ops4j.peaberry.Peaberry.service;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Singleton;
 import com.netxforge.netxstudio.common.properties.IPropertiesProvider;
 import com.netxforge.netxstudio.data.IQueryService;
import com.netxforge.netxstudio.data.cdo.CDOQueryService;
 import com.netxforge.netxstudio.server.IDPNoCacheProvider;
 import com.netxforge.netxstudio.server.IDPProvider;
 import com.netxforge.netxstudio.server.Server;
 import com.netxforge.netxstudio.server.ServerNoCache;
 import com.netxforge.netxstudio.server.logic.monitoring.MonitoringEngine;
 import com.netxforge.netxstudio.server.logic.monitoring.MonitoringService.ResourceMonitoringRunner;
 import com.netxforge.netxstudio.server.logic.monitoring.NodeMonitoringLogic;
 import com.netxforge.netxstudio.server.logic.monitoring.RFSServiceMonitoringJobImplementation;
 import com.netxforge.netxstudio.server.logic.monitoring.RFSServiceMonitoringLogic;
 import com.netxforge.netxstudio.server.logic.profile.ProfileEngine;
 import com.netxforge.netxstudio.server.logic.reporting.NodeReportingJobImplementation;
 import com.netxforge.netxstudio.server.logic.reporting.NodeResourceReportingLogic;
 import com.netxforge.netxstudio.server.logic.reporting.OperatorReportingJobImplementation;
 import com.netxforge.netxstudio.server.logic.reporting.RFSServiceDashboardReportingLogic;
 import com.netxforge.netxstudio.server.logic.reporting.RFSServiceDistributionReportingLogic;
 import com.netxforge.netxstudio.server.logic.reporting.RFSServiceReportingJobImplementation;
 import com.netxforge.netxstudio.server.logic.reporting.RFSServiceSummaryReportingLogic;
 import com.netxforge.netxstudio.server.logic.reporting.RFSServiceUserReportingLogic;
 import com.netxforge.netxstudio.server.logic.retention.AddonHandler;
 import com.netxforge.netxstudio.server.logic.retention.AggregationEngine;
 import com.netxforge.netxstudio.server.logic.retention.AggregationLogic;
 import com.netxforge.netxstudio.server.logic.retention.RetentionEngine;
 import com.netxforge.netxstudio.server.logic.retention.RetentionLogic;
 
 public class LogicModule extends AbstractModule {
 
 	// Bind our expression engine.
 	@Override
 	protected void configure() {
 
 		// //////////////////////////////////////////////
 		// INTERNAL SERVICES
 
 		this.bind(ResourceMonitoringRunner.class);
 
 		this.bind(RFSServiceMonitoringJobImplementation.class);
 		this.bind(OperatorReportingJobImplementation.class);
 		this.bind(RFSServiceReportingJobImplementation.class);
 		this.bind(NodeReportingJobImplementation.class);
 
 		this.bind(RFSServiceMonitoringLogic.class);
 		this.bind(RFSServiceSummaryReportingLogic.class);
 		this.bind(RFSServiceDashboardReportingLogic.class);
 		this.bind(RFSServiceDistributionReportingLogic.class);
 		this.bind(RFSServiceUserReportingLogic.class);
 		this.bind(NodeResourceReportingLogic.class);
 		this.bind(NodeMonitoringLogic.class);
 		this.bind(RetentionLogic.class);
 		this.bind(AggregationLogic.class);
 
 		this.bind(MonitoringEngine.class);
 		this.bind(ProfileEngine.class);
 		this.bind(RetentionEngine.class);
 		this.bind(AggregationEngine.class);
 
 		this.bind(AddonHandler.class).in(Singleton.class);
 
 		// / ///////////////////////////////
 		// IMPORT SERVICES
 
 		// {@link CDODataServiceModule}
 		bind(IQueryService.class).toProvider(
				service(CDOQueryService.class).single());
 
 		// {@link ServerModule}
 		bind(IDPProvider.class).annotatedWith(Server.class).toProvider(
 				service(IDPProvider.class).single());
 
 		// {@link ServerModule}
 		bind(IDPNoCacheProvider.class).annotatedWith(ServerNoCache.class)
 				.toProvider(service(IDPNoCacheProvider.class).single());
 
 		// {@link ServerModule}
 		bind(IPropertiesProvider.class).toProvider(
 				service(IPropertiesProvider.class).single());
 
 	}
 }
