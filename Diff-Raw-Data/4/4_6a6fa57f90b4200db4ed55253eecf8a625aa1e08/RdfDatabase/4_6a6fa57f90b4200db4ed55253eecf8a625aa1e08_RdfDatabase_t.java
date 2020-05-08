 /**
  * 
  */
 package sim.server;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.QueryResultTable;
 import org.ontoware.rdf2go.model.Statement;
 import org.ontoware.rdf2go.model.node.DatatypeLiteral;
 import org.ontoware.rdf2go.model.node.Node;
 import org.ontoware.rdf2go.model.node.PlainLiteral;
 import org.ontoware.rdf2go.model.node.URI;
 import org.ontoware.rdf2go.util.RDFTool;
 import org.openrdf.rdf2go.RepositoryModel;
 import org.openrdf.repository.http.HTTPRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sim.data.ApplicationId;
 import sim.data.Context;
 import sim.data.MethodMetrics;
 import sim.data.MetricsVisitor;
 import sim.data.PlatformMetrics;
 import sim.data.SystemId;
 import sim.data.SystemMetrics;
 import sim.server.data.CompoundMetric;
 import sim.server.data.Metric;
 import sim.server.util.SPARQLQueryContentAnalyzer;
 
 
 /**
  * @author valer
  *
  */
 public class RdfDatabase implements MetricsVisitor {
 
 	private static final Logger logger = LoggerFactory.getLogger(RdfDatabase.class);
 	
 	public static final String QUERY_CONTENT = "QueryContent";
 	public static final String NUMBER_OF_PLUGINS = "WorkflowNumberOfPlugins";
 	
 	private Model model;
 
 	private String simNS;
 	private String rdfNS;
 	private String rdfsNS;	
 	private String xsdNS;
 	
 	private URI typePredicateURI;
 	private URI rdfLiURI;
 	private URI hasSystemMetricURI;
 	private URI hasNameURI;
 	private URI hasDataValueURI;
 	private URI hasTimeStampURI;
 	
 	private URI hasMethodNameURI;
 	private URI hasClassNameURI;
 	private URI hasExceptionURI;
 	private URI hasEndedWithErrorURI;
 	private URI hasBeginExecutionTimeURI;
 	private URI hasEndExecutionTimeURI;
 	
 	private URI hasParentContextURI;
 	private URI hasContextURI;
 	private URI hasMetricsURI;
 	
 	private URI longDatatypeURI;
 	private URI doubleDatatypeURI;
 	private URI dateTimeDatatypeURI;
 	private URI booleanDatatypeURI;
 	private URI integerDatatypeURI;
 
 	private URI hasMethodExecutionURI;
 	private URI isMethodExecutionOfURI;
 	private URI hasMeasurementURI;
 	private URI isMeasurementOfURI;
 	private URI includesURI;
 	
 	private URI methodTypeURI;
 	private URI methodExecutionTypeURI;
 	
 	private URI systemTypeURI;
 	private URI applicationTypeURI;
 	
 	private URI hasPlatformMetricURI;
 	
 	private URI hasTotalMemoryURI;
 	private URI hasCpuCountURI;
 	
 	private URI bagTypeURI;
 	
 	private static final HashMap<String, URI> methodURICache = new HashMap<String, URI>();
 	private static final HashMap<String, URI> systemURICache = new HashMap<String, URI>();
 	private static final HashMap<String, URI> applicationURICache = new HashMap<String, URI>();
 		
 	public RdfDatabase() {
 	}
 	
 	public void open() {
 		this.model = new RepositoryModel(new HTTPRepository("http://" + Main.storage_server_domain + ":" + Main.storage_server_port + "/openrdf-sesame", Main.storage_repository_id));
 		this.model.open();
 		this.model.setAutocommit(false);
 		
 		simNS = model.getNamespace("sim");
 		if (simNS == null) {
 			model.setNamespace("sim", "http://www.larkc.eu/ontologies/IMOntology.rdf#");
 			model.commit();
 			simNS = model.getNamespace("sim");
 			if (simNS == null) {
 				simNS = "http://www.larkc.eu/ontologies/IMOntology.rdf#"; //this is for OWLIM, the get returned null check http://www.mail-archive.com/owlim-discussion@ontotext.com/msg00772.html
 			}
 		}
 		rdfNS = model.getNamespace("rdf");
 		if (rdfNS == null) {
 			model.setNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
 			model.commit();
 			rdfNS = model.getNamespace("rdf");
 		}
 		rdfsNS = model.getNamespace("rdfs");
 		if (rdfsNS == null) {
 			model.setNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 			model.commit();
 			rdfsNS = model.getNamespace("rdfs");
 		}
 
 		xsdNS = model.getNamespace("xsd");
 		if (xsdNS == null) {
 			model.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
 			model.commit();
 			xsdNS = model.getNamespace("xsd");
 		}
 		
 		typePredicateURI = model.createURI(rdfNS + "type");
 		rdfLiURI = model.createURI(rdfNS + "li");
 		hasSystemMetricURI = model.createURI(simNS + "hasSystemMetric");
 		hasNameURI = model.createURI(simNS + "hasName");
 		hasDataValueURI = model.createURI(simNS + "hasDataValue");
 		hasTimeStampURI = model.createURI(simNS + "hasTimeStamp");
 		
 		hasMethodNameURI = model.createURI(simNS + "hasMethodName");
 		hasClassNameURI = model.createURI(simNS + "hasClassName");
 		hasExceptionURI = model.createURI(simNS + "hasException");
 		hasEndedWithErrorURI = model.createURI(simNS + "hasEndedWithError");
 		hasBeginExecutionTimeURI = model.createURI(simNS + "hasBeginExecutionTime");
 		hasEndExecutionTimeURI = model.createURI(simNS + "hasEndExecutionTime");
 		
 		hasParentContextURI = model.createURI(simNS + "hasParentContext");
 		hasContextURI = model.createURI(simNS + "hasContext");
 		hasMetricsURI = model.createURI(simNS + "hasMetrics");
 		
 		longDatatypeURI = model.createURI(xsdNS + "long");
 		doubleDatatypeURI = model.createURI(xsdNS + "double");
 		dateTimeDatatypeURI = model.createURI(xsdNS + "dateTime");
 		booleanDatatypeURI = model.createURI(xsdNS + "boolean");
 		integerDatatypeURI = model.createURI(xsdNS + "int");
 		
 		hasMethodExecutionURI = model.createURI(simNS + "hasMethodExecution");
 		isMethodExecutionOfURI = model.createURI(simNS + "isMethodExecutionOf");
 		hasMeasurementURI = model.createURI(simNS + "hasMeasurement");
 		isMeasurementOfURI = model.createURI(simNS + "isMeasurementOf");
 		includesURI = model.createURI(simNS + "includes");
 		
 		methodTypeURI = model.createURI(simNS + "Method");
 		methodExecutionTypeURI = model.createURI(simNS + "MethodExecution");
 		
 		systemTypeURI = model.createURI(simNS + "System");
 		applicationTypeURI = model.createURI(simNS + "Application");
 		
 		hasPlatformMetricURI = model.createURI(simNS + "hasPlatformMetric");
 		
		hasTotalMemoryURI = model.createURI(simNS + "hasTotalMemory");
		hasCpuCountURI = model.createURI(simNS + "hasCpuCount");
 		
 		bagTypeURI = model.createURI(simNS + "Bag");
 	}
 	
 	public void close() {
 		this.model.close();
 	}
 
 	private PlainLiteral getStringTypeURI(String value) {
 		return model.createPlainLiteral(value);
 	}
 
 	private DatatypeLiteral getLongTypeURI(long value) {
 		return model.createDatatypeLiteral(String.valueOf(value), longDatatypeURI);
 	}
 
 	private DatatypeLiteral getDoubleTypeURI(double value) {
 		return model.createDatatypeLiteral(String.valueOf(value), doubleDatatypeURI);
 	}
 
 	public long getDateTimeLong(DatatypeLiteral datatypeLiteral){
 		long result = 0;
 		try {
 			result = RDFTool.string2Date(datatypeLiteral.getValue()).getTime();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}		
 		return result;
 	}
 	
 	private DatatypeLiteral getDateTimeTypeURI(long dateTimeLong) {
 		Date dateTime = new Date(dateTimeLong);
 		return model.createDatatypeLiteral(RDFTool.dateTime2String(dateTime), dateTimeDatatypeURI);
 	}
 
 	private DatatypeLiteral getBooleanTypeURI(boolean value) {
 		return model.createDatatypeLiteral(String.valueOf(value), booleanDatatypeURI);
 	}
 
 	private DatatypeLiteral getIntegerTypeURI(int value) {
 		return model.createDatatypeLiteral(String.valueOf(value), integerDatatypeURI);
 	}
 
 	@Override
 	public void visit(MethodMetrics methodMetrics) {
 		List<Statement> statements = new ArrayList<Statement>();
 		
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(methodMetrics.getCreationTime());
 		
 		URI idSystemURI = addSystem(methodMetrics.getSystemId(), statements);
 		URI idApplicationURI = addApplication(methodMetrics.getMethod().getApplicationId(), statements);
 
 		String methodID = methodMetrics.getMethod().getSignature();
 		
 		URI idMethodURI = methodURICache.get(methodID);
 		if(idMethodURI == null){		
 			idMethodURI = model.createURI(simNS + methodID);			
 			statements.add(model.createStatement(idMethodURI, typePredicateURI, methodTypeURI));
 			statements.add(model.createStatement(idMethodURI, hasMethodNameURI, model.createPlainLiteral(methodMetrics.getMethod().getMethodName())));
 			statements.add(model.createStatement(idMethodURI, hasClassNameURI, model.createPlainLiteral(methodMetrics.getMethod().getClassName())));
 			methodURICache.put(methodID, idMethodURI);
 		}
 		
 		URI idMethodMetricsURI = generateURI();
 		statements.add(model.createStatement(idMethodMetricsURI, typePredicateURI, methodExecutionTypeURI));
 		statements.add(model.createStatement(idMethodMetricsURI, isMethodExecutionOfURI, idMethodURI));
 		statements.add(model.createStatement(idMethodMetricsURI, hasBeginExecutionTimeURI, getLongTypeURI(methodMetrics.getBeginExecutionTime())));
 		statements.add(model.createStatement(idMethodMetricsURI, hasEndExecutionTimeURI, getLongTypeURI(methodMetrics.getEndExecutionTime())));
 		statements.add(model.createStatement(idMethodMetricsURI, hasEndedWithErrorURI, getBooleanTypeURI(methodMetrics.endedWithError())));
 		if (methodMetrics.getException() != null) {
 			statements.add(model.createStatement(idMethodMetricsURI, hasExceptionURI, model.createPlainLiteral(methodMetrics.getException())));
 		}
 		
 		URI idContextURI;
 		if (methodMetrics.getContextId() == null)
 			idContextURI = null;
 		else
 			idContextURI = model.createURI(simNS + methodMetrics.getContextId());
 
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "WallClockTime", getLongTypeURI(methodMetrics.getWallClockTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadCount", getLongTypeURI(methodMetrics.getThreadCount()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "ProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "AllocatedMemoryBefore", getLongTypeURI(methodMetrics.getAllocatedMemoryBefore()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "UsedMemoryBefore", getLongTypeURI(methodMetrics.getUsedMemoryBefore()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "FreeMemoryBefore", getLongTypeURI(methodMetrics.getFreeMemoryBefore()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "UnallocatedMemoryBefore", getLongTypeURI(methodMetrics.getUnallocatedMemoryBefore()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "AllocatedMemoryAfter", getLongTypeURI(methodMetrics.getAllocatedMemoryAfter()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "UsedMemoryAfter", getLongTypeURI(methodMetrics.getUsedMemoryAfter()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "FreeMemoryAfter", getLongTypeURI(methodMetrics.getFreeMemoryAfter()));
 		addMethodMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodMetricsURI, dateTimeLiteral, "UnallocatedMemoryAfter", getLongTypeURI(methodMetrics.getUnallocatedMemoryAfter()));
 		
 		/**
 		 * if the method execution is the method execution of specific methods that correspond to
 		 * atomic metrics than the corresponding atomic metrics are written in the RDF storage
 		 */
 		extractAtomicMetricsFromMethodMetric(statements, methodMetrics, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral);
 
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 	
 	@Override
 	public void visit(SystemMetrics systemMetrics) {
 		List<Statement> statements = new ArrayList<Statement>();
 		
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(systemMetrics.getCreationTime());
 		
 		//System metric
 		URI idSystemURI = addSystem(systemMetrics.getSystemId(), statements);
 		
 		//SystemLoadAverage
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "SystemLoadAverage", getDoubleTypeURI(systemMetrics.getSystemLoadAverage()));
 		
 		//TotalSystemFreeMemory
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "TotalSystemFreeMemory", getLongTypeURI(systemMetrics.getTotalSystemFreeMemory()));
 		
 		//TotalSystemUsedMemory
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "TotalSystemUsedMemory", getLongTypeURI(systemMetrics.getTotalSystemUsedMemory()));
 		
 		//TotalSystemUsedSwap
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "TotalSystemUsedSwap", getLongTypeURI(systemMetrics.getTotalSystemUsedSwap()));
 
 		//SystemOpenFileDescriptors
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "SystemOpenFileDescriptorCount", getLongTypeURI(systemMetrics.getSystemOpenFileDescriptors()));
 		
 		//SwapIn
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "SwapIn", getLongTypeURI(systemMetrics.getSwapIn()));
 
 		//SwapOut
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "SwapOut", getLongTypeURI(systemMetrics.getSwapOut()));
 
 		//IORead
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "IORead", getLongTypeURI(systemMetrics.getIORead()));
 
 		//IOWrite
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "IOWrite", getLongTypeURI(systemMetrics.getIOWrite()));
 
 		//UserPerc
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "UserCPULoad", getDoubleTypeURI(systemMetrics.getUserPerc()));
 
 		//SysPerc
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "SystemCPULoad", getDoubleTypeURI(systemMetrics.getSysPerc()));
 
 		//IdlePerc
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "IdleCPULoad", getDoubleTypeURI(systemMetrics.getIdlePerc()));
 
 		//WaitPerc
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "WaitCPULoad", getDoubleTypeURI(systemMetrics.getWaitPerc()));
 		
 		//IrqPerc
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "IrqCPULoad", getDoubleTypeURI(systemMetrics.getIrqPerc()));
 		
 		//User
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "UserCPUTime", getDoubleTypeURI(systemMetrics.getUser()));
 		
 		//Sys
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "SystemCPUTime", getDoubleTypeURI(systemMetrics.getSys()));
 		
 		//Idle
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "IdleCPUTime", getDoubleTypeURI(systemMetrics.getIdle()));
 		
 		//Wait
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "WaitCPUTime", getDoubleTypeURI(systemMetrics.getWait()));
 		
 		//Irq
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "IrqCPUTime", getDoubleTypeURI(systemMetrics.getIrq()));
 		
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "ProcessesCount", getLongTypeURI(systemMetrics.getProcessesCount()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "RunningProcessesCount", getLongTypeURI(systemMetrics.getRunningProcessesCount()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "ThreadsCount", getLongTypeURI(systemMetrics.getThreadsCount()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "TcpInbound", getLongTypeURI(systemMetrics.getTcpInbound()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "TcpOutbound", getLongTypeURI(systemMetrics.getTcpOutbound()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "NetworkReceived", getLongTypeURI(systemMetrics.getNetworkReceived()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "NetworkSent", getLongTypeURI(systemMetrics.getNetworkSent()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "LoopbackNetworkReceived", getLongTypeURI(systemMetrics.getLoopbackNetworkReceived()));
 		addSystemMetrictatements(statements, idSystemURI, dateTimeLiteral, "LoopbackNetworkSent", getLongTypeURI(systemMetrics.getLoopbackNetworkSent()));
 
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 	
 	private URI addMetricStatements(List<Statement> statements, URI idSystemURI, URI idApplicationURI, URI idContextURI, URI idMethodURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		URI idURI = generateURI();
 		statements.add(model.createStatement(idURI, typePredicateURI, model.createURI(simNS + type)));
 		statements.add(model.createStatement(idURI, hasDataValueURI, value));
 		statements.add(model.createStatement(idURI, hasTimeStampURI, dateTimeLiteral));
 		if (idContextURI != null)
 			statements.add(model.createStatement(idURI, hasContextURI, idContextURI));
 		if (idMethodURI != null)
 			statements.add(model.createStatement(idMethodURI, hasMeasurementURI, idURI));
 		if (idApplicationURI != null)
 			statements.add(model.createStatement(idApplicationURI, hasMeasurementURI, idURI));
 		if (idSystemURI != null)
 			statements.add(model.createStatement(idSystemURI, hasMeasurementURI, idURI));
 		return idURI;
 	}
 
 	private URI addSystemMetrictatements(List<Statement> statements, URI idSystemURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		return addMetricStatements(statements, idSystemURI, null, null, null, dateTimeLiteral, type, value);
 	}
 
 	private URI addMethodMetricStatements(List<Statement> statements, URI idSystemURI, URI idApplicationURI, URI idContextURI, URI idMethodURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		return addMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, idMethodURI, dateTimeLiteral, type, value);
 	}
 
 	private URI addAtomicMetricStatements(List<Statement> statements, URI idSystemURI, URI idApplicationURI, URI idContextURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		return addMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, null, dateTimeLiteral, type, value);
 	}
 
 
 	private URI addPlatformMetricStatements(List<Statement> statements, URI idSystemURI, URI idApplicationURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		return addMetricStatements(statements, idSystemURI, idApplicationURI, null, null, dateTimeLiteral, type, value);
 	}
 	
 	private URI addContextMetricStatement(List<Statement> statements, URI idSystemURI, URI idApplicationURI, URI idContextURI, DatatypeLiteral dateTimeLiteral, String type, Node value) {
 		return addMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, null, dateTimeLiteral, type, value);
 	}
 
 	private URI addSystem(SystemId systemId, List<Statement> statements) {
 		//System metric
 		URI idSystemURI = systemURICache.get(systemId.getId());
 		if (idSystemURI == null) {
 			idSystemURI = model.createURI(simNS + systemId.getId());
 			systemURICache.put(systemId.getId(), idSystemURI);
 			statements.add(model.createStatement(idSystemURI, typePredicateURI, systemTypeURI));
 			statements.add(model.createStatement(idSystemURI, hasNameURI, getStringTypeURI(systemId.getName())));
 			statements.add(model.createStatement(idSystemURI, hasTotalMemoryURI, getLongTypeURI(systemId.getTotalMemory())));
 			statements.add(model.createStatement(idSystemURI, hasCpuCountURI, getLongTypeURI(systemId.getCpuCount())));
 
 		}
 		return idSystemURI;
 	}
 
 	private URI addApplication(ApplicationId applicationId, List<Statement> statements) {
 		URI idApplicationURI = applicationURICache.get(applicationId.getId());
 		if (idApplicationURI == null) {
 			idApplicationURI = model.createURI(simNS + applicationId.getId());
 			applicationURICache.put(applicationId.getId(), idApplicationURI);
 			statements.add(model.createStatement(idApplicationURI, typePredicateURI, applicationTypeURI));
 			statements.add(model.createStatement(idApplicationURI, hasNameURI, getStringTypeURI(applicationId.getName())));
 			statements.add(model.createStatement(idApplicationURI, hasTotalMemoryURI, getLongTypeURI(applicationId.getTotalMemory())));
 			statements.add(model.createStatement(idApplicationURI, hasCpuCountURI, getLongTypeURI(applicationId.getCpuCount())));
 		}
 		return idApplicationURI;
 	}
 
 	
 	private URI addContextMetrics(Context context, List<Statement> statements) {
 		if (context == null) {
 			return null;
 		}
 		URI idContextURI = model.createURI(simNS + context.getId());
 		statements.add(model.createStatement(idContextURI, typePredicateURI, model.createURI(simNS + context.getName())));
 		if (context.getParentContextId() != null) {
 			URI idParentContextURI = model.createURI(simNS + context.getParentContextId());
 			statements.add(model.createStatement(idContextURI, hasParentContextURI, idParentContextURI));
 		}
 		URI idBagURI = model.createURI(simNS + context.getId() + "-metrics");
 		statements.add(model.createStatement(idBagURI, typePredicateURI, bagTypeURI));
 		statements.add(model.createStatement(idContextURI, hasMetricsURI, idBagURI));
 
 		DatatypeLiteral timeURI = getDateTimeTypeURI(context.getCreationTime());
 		
 		URI idSystemURI = addSystem(context.getSystemId(), statements);
 		URI idApplicationURI = addApplication(context.getApplicationId(), statements);
 		
 		for (Entry<String, Object> entry : context.entrySet()) {
 			URI idContextMetric = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, entry.getKey(), model.createPlainLiteral(entry.getValue().toString()));
 			statements.add(model.createStatement(idBagURI, rdfLiURI, idContextMetric));
 		}
 		
 
 		/*	check if the context contains information about SPARQL query;
 		if that's the case the SPARQL query is parsed and query atomic metrics
 		are created for this query
 		 */
 		if(context.containsKey(QUERY_CONTENT)){
 			String query = context.get(QUERY_CONTENT).toString();
 			SPARQLQueryContentAnalyzer sqa = new SPARQLQueryContentAnalyzer(query);
 			if (sqa.parseQuery()) {
 	
 				URI idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryDataSetSourcesNb", getIntegerTypeURI(sqa.getQueryDataSetSourcesNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 	
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryNamespaceNb", getIntegerTypeURI(sqa.getQueryNamespaceNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryOperatorsNb", getIntegerTypeURI(sqa.getQueryOperatorsNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryLiteralsNb", getIntegerTypeURI(sqa.getQueryLiteralsNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryResultLimitNb", getIntegerTypeURI(sqa.getQueryResultLimitNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryResultOffsetNb", getIntegerTypeURI(sqa.getQueryResultOffsetNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryResultOrderingNb", getIntegerTypeURI(sqa.getQueryResultOrderingNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QuerySizeInCharacters", getIntegerTypeURI(sqa.getQuerySizeInCharacters()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryVariablesNb", getIntegerTypeURI(sqa.getQueryVariablesNb()));
 				statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 
 				if (sqa.getQueryDataSetSources() != null && sqa.getQueryDataSetSources().size() > 0) {
 					StringBuilder sb = new StringBuilder("[");
 					boolean firstTime = true;
 					for(String dataSource:sqa.getQueryDataSetSources()) {
 						if (firstTime)
 							firstTime = false;
 						else
 							sb.append(", ");
 						sb.append(dataSource);
 					}
 					sb.append("]");
 					idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryDataSetSources", getStringTypeURI(sb.toString()));
 					statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 				}
 
 				if (sqa.getQueryNamespaceValues() != null && sqa.getQueryNamespaceValues().size() > 0) {
 					StringBuilder sb = new StringBuilder("[");
 					boolean firstTime = true;
 					for(String namespace:sqa.getQueryNamespaceValues()) {
 						if (firstTime)
 							firstTime = false;
 						else
 							sb.append(", ");
 						sb.append(namespace);
 					}
 					sb.append("]");
 					idURI = addContextMetricStatement(statements, idSystemURI, idApplicationURI, idContextURI, timeURI, "QueryNamespaceValues", getStringTypeURI(sb.toString()));
 					statements.add(model.createStatement(idBagURI, rdfLiURI, idURI));
 				}
 
 			}
 		}
 
 		return idContextURI;
 	}
 
 	@Override
 	public void visit(Context context) {
 		List<Statement> statements = new ArrayList<Statement>();	
 		addContextMetrics(context, statements);
 		model.addAll(statements.iterator());
 		model.commit();			
 	}
 
 	public URI generateURI(){
 		return model.createURI(simNS + UUID.randomUUID().toString());
 	}
 	
 	public QueryResultTable sparqlSelect(String queryString){
 		return model.sparqlSelect(queryString);
 	}
 
 	public URI createURI(String uriString){
 		return model.createURI(simNS + uriString);
 	}
 
 	public String getSimNS() {
 		return simNS;
 	}
 
 	public String getRdfNS() {
 		return rdfNS;
 	}
 
 	public String getRdfsNS() {
 		return rdfsNS;
 	}
 
 	public String getXsdNS() {
 		return xsdNS;
 	}
 	
 	public void visit(CompoundMetric compoundMetric) {
 		List<Statement> statements = new ArrayList<Statement>();
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(compoundMetric.getCreationTime());
 				
 		URI idCompoundMetricURI = generateURI();
 		compoundMetric.setId(idCompoundMetricURI);
 		
 		statements.add(model.createStatement(idCompoundMetricURI, typePredicateURI, compoundMetric.getType()));		
 		statements.add(model.createStatement(idCompoundMetricURI, hasTimeStampURI, dateTimeLiteral));
 		
 		for(Metric m : compoundMetric.getConstituentMetrics()){
 			statements.add(model.createStatement(idCompoundMetricURI, includesURI, m.getId()));
 		}		
 		statements.add(model.createStatement(idCompoundMetricURI, hasDataValueURI, getDoubleTypeURI(new Double(compoundMetric.getValue()).doubleValue())));
 		
 		logger.info("Writting compound metric "+idCompoundMetricURI+" of type "+compoundMetric.getType().toString()+" created at "+dateTimeLiteral.getValue()+" having value "+ compoundMetric.getValue());
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 	
 	@Override
 	public void visit(PlatformMetrics pm) {
 		List<Statement> statements = new ArrayList<Statement>();
 
 		DatatypeLiteral dateTimeLiteral = getDateTimeTypeURI(pm.getCreationTime());
 		
 		URI idSystemURI = addSystem(pm.getSystemId(), statements);
 		URI idApplicationURI = addApplication(pm.getApplicationId(), statements);
 
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformAvgCPUUsage", getDoubleTypeURI(pm.getAvgCpuUsage()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformCPUTime", getLongTypeURI(pm.getCpuTime()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformTotalCPUTime", getLongTypeURI(pm.getTotalCpuTime()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformCPUUsage", getDoubleTypeURI(pm.getCpuUsage()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformGccCount", getLongTypeURI(pm.getGccCount()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformGccTime", getLongTypeURI(pm.getGccTime()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformTotalGccCount", getLongTypeURI(pm.getTotalGccCount()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformTotalGccTime", getLongTypeURI(pm.getTotalGccTime()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformUptime", getLongTypeURI(pm.getUptime()));		
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformAllocatedMemory", getLongTypeURI(pm.getAllocatedMemory()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformUsedMemory", getLongTypeURI(pm.getUsedMemory()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformFreeMemory", getLongTypeURI(pm.getFreeMemory()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformUnallocatedMemory", getLongTypeURI(pm.getUnallocatedMemory()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformThreadsCount", getLongTypeURI(pm.getThreadsCount()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformThreadsStarted", getLongTypeURI(pm.getThreadsStarted()));
 		addPlatformMetricStatements(statements, idSystemURI, idApplicationURI, dateTimeLiteral, "PlatformTotalThreadsStarted", getLongTypeURI(pm.getTotalThreadsStarted()));
 
 		model.addAll(statements.iterator());
 		model.commit();
 	}
 
 	private void addAtomicMetrics(List<Statement> statements, URI idSystemURI, URI idApplicationURI, URI idContextURI, DatatypeLiteral dateTimeLiteral, String prefix, MethodMetrics methodMetrics) {
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "BeginExecutionTime", getLongTypeURI(methodMetrics.getBeginExecutionTime()));						
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "EndExecutionTime", getLongTypeURI(methodMetrics.getEndExecutionTime()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ErrorStatus", getBooleanTypeURI(methodMetrics.endedWithError()));
 		if (methodMetrics.getException() != null)
 			addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ErrorMessage", getStringTypeURI(methodMetrics.getException()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "TotalResponseTime", getLongTypeURI(methodMetrics.getWallClockTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadUserCPUTime", getLongTypeURI(methodMetrics.getThreadUserCpuTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadSystemCPUTime", getLongTypeURI(methodMetrics.getThreadSystemCpuTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadTotalCPUTime", getLongTypeURI(methodMetrics.getThreadTotalCpuTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadCount", getLongTypeURI(methodMetrics.getThreadCount()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadBlockCount", getLongTypeURI(methodMetrics.getThreadBlockCount()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadBlockTime", getLongTypeURI(methodMetrics.getThreadBlockTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadWaitCount", getLongTypeURI(methodMetrics.getThreadWaitCount()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadWaitTime", getLongTypeURI(methodMetrics.getThreadWaitTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadGccCount", getLongTypeURI(methodMetrics.getThreadGccCount()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ThreadGccTime", getLongTypeURI(methodMetrics.getThreadGccTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "ProcessTotalCPUTime", getLongTypeURI(methodMetrics.getProcessTotalCpuTime()));			
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "AllocatedMemoryBefore", getLongTypeURI(methodMetrics.getAllocatedMemoryBefore()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "UsedMemoryBefore", getLongTypeURI(methodMetrics.getUsedMemoryBefore()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "FreeMemoryBefore", getLongTypeURI(methodMetrics.getFreeMemoryBefore()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "UnallocatedMemoryBefore", getLongTypeURI(methodMetrics.getUnallocatedMemoryBefore()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "AllocatedMemoryAfter", getLongTypeURI(methodMetrics.getAllocatedMemoryAfter()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "UsedMemoryAfter", getLongTypeURI(methodMetrics.getUsedMemoryAfter()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "FreeMemoryAfter", getLongTypeURI(methodMetrics.getFreeMemoryAfter()));
 		addAtomicMetricStatements(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, prefix + "UnallocatedMemoryAfter", getLongTypeURI(methodMetrics.getUnallocatedMemoryAfter()));
 	}
 	
 	private void extractAtomicMetricsFromMethodMetric(List<Statement> statements, MethodMetrics methodMetrics, URI idSystemURI, URI idApplicationURI, URI idContextURI, DatatypeLiteral dateTimeLiteral){
 		String methodID = methodMetrics.getMethod().getSignature();
 
 		/**
 		 * if the method execution is the method execution of 
 		 * eu.larkc.core.endpoint.sparql.SparqlHandler.handle
 		 * we write query related atomic metrics
 		 */		
 		if(methodID.equals("eu.larkc.core.endpoint.sparql.SparqlHandler.handle")){					
 			addAtomicMetrics(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, "Query", methodMetrics);
 		}
 		
 		/**
 		 * if the method execution is the method execution of 
 		 * eu.larkc.core.executor.Executor.execute
 		 * we write workflow related atomic metrics
 		 */		
 		if(methodID.equals("eu.larkc.core.executor.Executor.execute") || methodID.equals("eu.larkc.core.executor.Executor.getNextResults")){	
 			addAtomicMetrics(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, "Workflow", methodMetrics);
 		}
 		
 		/**
 		 * if the method execution is the method execution of 
 		 * eu.larkc.plugin.Plugin.invoke
 		 * we write plugin related atomic metrics
 		 */		
 		if(methodID.equals("eu.larkc.plugin.Plugin.invoke")){
 			addAtomicMetrics(statements, idSystemURI, idApplicationURI, idContextURI, dateTimeLiteral, "Plugin", methodMetrics);
 		}
 	}
 
 	public Model getModel() {
 		return model;
 	}
 
 }
