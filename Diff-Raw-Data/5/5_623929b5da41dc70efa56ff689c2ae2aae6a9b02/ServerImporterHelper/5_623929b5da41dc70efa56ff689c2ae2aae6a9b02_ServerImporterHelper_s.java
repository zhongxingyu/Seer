 /*******************************************************************************
  * Copyright (c) May 20, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Martin Taal - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.metrics;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.emf.cdo.eresource.CDOResource;
 import org.eclipse.emf.cdo.eresource.CDOResourceFolder;
 import org.eclipse.emf.cdo.eresource.CDOResourceNode;
 import org.eclipse.emf.cdo.util.CDOUtil;
 import org.eclipse.emf.cdo.util.CommitException;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.osgi.framework.BundleActivator;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 
 import com.google.inject.Inject;
 import com.netxforge.netxstudio.common.model.ModelUtils;
 import com.netxforge.netxstudio.data.IDataProvider;
 import com.netxforge.netxstudio.data.importer.AbstractMetricValuesImporter;
 import com.netxforge.netxstudio.data.importer.ComponentLocator;
 import com.netxforge.netxstudio.data.importer.IImporterHelper;
 import com.netxforge.netxstudio.data.importer.ResultProcessor;
 import com.netxforge.netxstudio.data.internal.DataActivator;
 import com.netxforge.netxstudio.generics.GenericsFactory;
 import com.netxforge.netxstudio.generics.Value;
 import com.netxforge.netxstudio.library.Component;
 import com.netxforge.netxstudio.library.LibraryFactory;
 import com.netxforge.netxstudio.library.NetXResource;
 import com.netxforge.netxstudio.metrics.KindHintType;
 import com.netxforge.netxstudio.metrics.MappingColumn;
 import com.netxforge.netxstudio.metrics.Metric;
 import com.netxforge.netxstudio.metrics.MetricsPackage;
 import com.netxforge.netxstudio.metrics.ValueDataKind;
 import com.netxforge.netxstudio.server.Server;
 import com.netxforge.netxstudio.server.job.JobHandler;
 import com.netxforge.netxstudio.server.metrics.internal.MetricsActivator;
 
 /**
  * The main entry class for the Metrics importing.
  * 
  * @author Martin Taal
  */
 public class ServerImporterHelper implements IImporterHelper {
 
 	/* We need the importer to set the data provider */
 	private AbstractMetricValuesImporter importer;
 
 	@Inject
 	private ResultProcessor resultProcessor;
 
 	/*
 	 * Keep a reference to our Job Handler.
 	 */
 	private JobHandler jobHandler;
 
 	public ServerImporterHelper() {
 	}
 
 	public void setImporter(AbstractMetricValuesImporter importer) {
 		this.importer = importer;
 	}
 
 	@Inject
 	private ModelUtils modelUtils;
 
 	private CDOResourceFolder netXResourceFolder = null;
 
 	private BundleActivator activator;
 
 
 	public void initializeProviders(ComponentLocator networkElementLocator) {
 		// force that the same dataprovider is used
 		// so that components retrieved by the networkElementLocator
 		// participate in the same transaction
 		networkElementLocator.setDataProvider(importer.getDataProvider());
 	}
 
 	public IDataProvider getDataProvider() {
 		// get it from the serverside
 		IDataProvider dataProvider = MetricsActivator.getInstance()
 				.getInjector().getInstance(LocalDataProviderProvider.class)
 				.getDataProvider();
 		// Set in the importer so we are called only once.
 		// importer.setDataProvider(dataProvider);
 		return dataProvider;
 	}
 
 	public static class LocalDataProviderProvider {
 
 		@Inject
 		@Server
 		private IDataProvider dataProvider;
 
 		public IDataProvider getDataProvider() {
 			return dataProvider;
 		}
 	}
 
 	public void addToValueRange(NetXResource foundNetXResource, int periodHint,
 			KindHintType kindHintType, List<Value> newValues, Date start,
 			Date end) {
 		resultProcessor.addToValueRange(foundNetXResource, periodHint,
 				kindHintType, newValues, start, end);
 	}
 
 	public void addMetricValue(MappingColumn column, Date timeStamp,
 			Component locatedComponent, Double dblValue, int intervalHint,
 			ComponentLocator.IdentifierDescriptor lastDescriptor) {
 
 		if (netXResourceFolder == null) {
 			CDOResourceNode folder = importer.getDataProvider()
 					.getTransaction().getResourceNode("/Node_/");
 
 			// Delete any resource, which has the name "/Node_/", accidently
 			// created.
 			if (folder instanceof CDOResource) {
 				try {
 					folder.delete(null);
 					importer.getDataProvider().getTransaction().commit();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (CommitException e) {
 					e.printStackTrace();
 				}
 			} else if (folder instanceof CDOResourceFolder) {
 				// remember the folder, so we can create resources directly.
 				netXResourceFolder = (CDOResourceFolder) folder;
 			}
 		}
 
 		// String path =
 		// String cdoCalculateResourcePathII =
 		// modelUtils.cdoCalculateResourcePathII(locatedComponent);
 
 		// if (path == null) {
 		// if (DataActivator.DEBUG) {
 		// DataActivator.TRACE.trace(
 		// DataActivator.TRACE_IMPORT_HELPER_OPTION,
 		// "-- invalid CDO Resource path, should not happen.");
 		// }
 		// throw new java.lang.IllegalStateException(
 		// "Invalid CDO Resource path, should not happen.");
 		// }
 
 		// final Resource emfNetxResource = importer.getDataProvider()
 		// .getResource(path);
 
 		String cdoCalculateResourceName;
 		try {
 			cdoCalculateResourceName = modelUtils
 					.cdoCalculateResourceName(locatedComponent);
 
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(
 						DataActivator.TRACE_IMPORT_HELPER_OPTION,
 						"-- looking for CDO resource with name:"
 								+ cdoCalculateResourceName);
 			}
 
 			Resource emfNetxResource = null;
 
 			if (netXResourceFolder != null) {
 				for (CDOResourceNode n : netXResourceFolder.getNodes()) {
					if (n.getName().equals(cdoCalculateResourceName)
 							&& n instanceof CDOResource) {
 						emfNetxResource = (Resource) n;
 						if (DataActivator.DEBUG) {
 							DataActivator.TRACE.trace(
 									DataActivator.TRACE_IMPORT_HELPER_OPTION,
 									"-- found:"
 											+ emfNetxResource.getURI()
 													.toString());
 						}
 						break;
 					}
 				}
 			}
 			if (emfNetxResource == null) {
 				emfNetxResource = netXResourceFolder
 						.addResource(cdoCalculateResourceName);
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_HELPER_OPTION,
 							"-- created resource:"
 									+ emfNetxResource.getURI().toString());
 				}
 			}
 
 			// Set a prefetch policy so we can load a lot of objects in the
 			// first
 			// fetch.
 			// this should affect the Value objects being loaded. NetXResource->
 			// MVR
 			// -> Value.
 
 			if (emfNetxResource instanceof CDOResource) {
 				((CDOResource) emfNetxResource)
 						.cdoView()
 						.options()
 						.setRevisionPrefetchingPolicy(
 								CDOUtil.createRevisionPrefetchingPolicy(24));
 			}
 
 			final ValueDataKind valueDataKind = importer
 					.getValueDataKind(column);
 			final Metric metric = valueDataKind.getMetricRef();
 
 			NetXResource foundNetXResource = null;
 			EList<EObject> objects = emfNetxResource.getContents();
 			for (final Object object : objects) {
 				final NetXResource netXResource = (NetXResource) object;
 
 				// Match the resource on component, metric and also the name as
 				// per
 				// last identifier value.
 				// Note, manually created resources, do not necessarly have a
 				// metric
 				// reference:
 				// see http://work.netxforge.com/issues/264
 				if (netXResource.getComponentRef() != null
 						&& netXResource.getComponentRef().cdoID()
 								.equals(locatedComponent.cdoID())
 						&& netXResource.getMetricRef() != null
 						&& netXResource.getMetricRef().cdoID() == metric
 								.cdoID()) {
 					if (lastDescriptor != null) {
 						if (!netXResource.getShortName().equals(
 								lastDescriptor.getValue())) {
 							// Try the next one, we have a descriptor, but no
 							// matching value.
 							continue;
 						}
 					}
 					foundNetXResource = netXResource;
 
 					if (DataActivator.DEBUG) {
 						DataActivator.TRACE.trace(
 								DataActivator.TRACE_IMPORT_HELPER_OPTION,
 								"-- matching resource: "
 										+ foundNetXResource.getShortName()
 										+ " , for component: "
 										+ locatedComponent.getName()
 										+ ", on metric: " + metric.getName());
 					}
 					break;
 				}
 			}
 			if (foundNetXResource == null) {
 
 				if (DataActivator.DEBUG) {
 					DataActivator.TRACE.trace(
 							DataActivator.TRACE_IMPORT_HELPER_OPTION,
 							"-- No Matching resource for: "
 									+ locatedComponent.getName()
 									+ ", on metric: " + metric.getName()
 									+ " creating resource:");
 				}
 				foundNetXResource = LibraryFactory.eINSTANCE
 						.createNetXResource();
 				foundNetXResource.setComponentRef(locatedComponent);
 				foundNetXResource.setMetricRef(metric);
 
 				if (lastDescriptor != null) {
 					foundNetXResource.setShortName(lastDescriptor.getValue());
 					foundNetXResource
 							.setLongName(" Auto created resource from identifier with value "
 									+ lastDescriptor.getValue());
 					foundNetXResource.setExpressionName(importer
 							.toValidExpressionName(lastDescriptor.getValue()));
 				} else {
 					foundNetXResource.setShortName(metric.getName());
 					if (metric
 							.eIsSet(MetricsPackage.Literals.METRIC__DESCRIPTION)) {
 						foundNetXResource.setLongName(metric.getDescription());
 					} else {
 						foundNetXResource.setLongName(metric.getName());
 					}
 
 					foundNetXResource.setExpressionName(importer
 							.toValidExpressionName(metric.getName()));
 
 				}
 
 				foundNetXResource.setUnitRef(metric.getUnitRef());
 				locatedComponent.getResourceRefs().add(foundNetXResource);
 				emfNetxResource.getContents().add(foundNetXResource);
 
 				// DISABLE, DOESN'T WORK FOR AUTO-CREATED COMPONENTS>
 				// importer.addToNode(networkElement, networkElement, new
 				// ArrayList<Integer>(),
 				// foundNetXResource);
 			}
 			// objects = emfNetxResource.getContents();
 
 			final Value value = GenericsFactory.eINSTANCE.createValue();
 			value.setTimeStamp(modelUtils.toXMLDate(timeStamp));
 			value.setValue(dblValue);
 
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(
 						DataActivator.TRACE_IMPORT_HELPER_OPTION,
 						"-- try to add value to resource : "
 								+ foundNetXResource.getShortName()
 								+ " , value =" + value.getValue()
 								+ " timestamp="
 								+ modelUtils.dateAndTime(value.getTimeStamp())
 								+ ", kind="
 								+ valueDataKind.getKindHint().getName()
 								+ " , interval =" + intervalHint);
 			}
 
 			addToValueRange(foundNetXResource, intervalHint,
 					valueDataKind.getKindHint(),
 					Collections.singletonList(value), null, null);
 			if (DataActivator.DEBUG) {
 				DataActivator.TRACE.trace(
 						DataActivator.TRACE_IMPORT_HELPER_OPTION,
 						"-- value added ");
 			}
 
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public boolean cancelled() {
 		Scheduler scheduler = jobHandler.getScheduler();
 		try {
 			if (scheduler.isInStandbyMode()) {
 				return true;
 			}
 		} catch (SchedulerException e) {
 			return false;
 		}
 		return false;
 	}
 
 	public JobHandler getJobHandler() {
 		return jobHandler;
 	}
 
 	public void setJobHandler(JobHandler jobHandler) {
 		this.jobHandler = jobHandler;
 	}
 
 	public BundleActivator getActivator() {
 		return activator;
 	}
 
 	public void setActivator(BundleActivator a) {
 		this.activator = a;
 		
 	}
 }
