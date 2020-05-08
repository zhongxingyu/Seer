 /**
  * 
  */
 package com.gooddata.connector;
 
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.SLI;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.naming.N;
 import com.gooddata.util.StringUtil;
 
 class DataSetDiffMaker {
 	private final Set<SourceColumn> remoteColumns  = new HashSet<SourceColumn>();
 	final Set<SourceColumn> sourceColumns  = new HashSet<SourceColumn>();
 	private final List<SourceColumn> deletedColumns = new ArrayList<SourceColumn>();
 	
 	DataSetDiffMaker(GdcRESTApiWrapper gd, SLI sli, SourceSchema ss) {
 		SourceColumn sourceConnectionPoint = null;
 		Map<String, SourceColumn> sourceDateColumns = new HashMap<String, SourceColumn>();
 	    String remoteConnectionPointName = null;
 
 		for (SourceColumn sc : ss.getColumns()) {
 			sc.setName(StringUtil.toIdentifier(sc.getName()));
 			sc.setReference(StringUtil.toIdentifier(sc.getReference()));
 			sc.setSchemaReference(StringUtil.toIdentifier(sc.getSchemaReference()));
 			sourceColumns.add(sc);
 			if (SourceColumn.LDM_TYPE_CONNECTION_POINT.equals(sc.getLdmType())) {
 				sourceConnectionPoint = sc;
 			} else if (SourceColumn.LDM_TYPE_DATE.equals(sc.getLdmType())) {
 				sourceDateColumns.put(sc.getName(), sc);
 			}
 		}
 		String datasetId = sli.getId().replaceAll("^dataset\\.", "");
 		String factPrefix = N.FCT_PFX + datasetId + "." + N.FCT_PFX;
 		String cpPrefix   = N.FCT_PFX + datasetId + "." + N.NM_PFX;
 		String datePrefix   = N.FCT_PFX + datasetId + "." + N.DT_PFX;
 		String lookupPrefix = N.LKP_PFX + datasetId + "_";
 		final List<Column> sliColumns = gd.getSLIColumns(sli.getUri());
 					
 		for (final Column c : sliColumns) {
 			final String ldmType,
 			             name;
 			String reference = null,
 			       schemaReference = null;
 			final int prefixLen;
 			
 			// fields populating a fact table column
 			if (c.getName().startsWith(factPrefix)) {   // FACT
 				prefixLen = factPrefix.length();
 				ldmType = SourceColumn.LDM_TYPE_FACT;
 				name = c.getName().substring(prefixLen).replaceAll("\\..*$", "");
 			} else if (c.getName().startsWith(datePrefix)) { // DATE
 				prefixLen = datePrefix.length();
 				ldmType = SourceColumn.LDM_TYPE_DATE;
 				name = c.getName().substring(prefixLen).replaceAll(".*\\." + N.DT, "").replaceAll("_id$", "");
 				for (String pop : c.getPopulates()) {
				    // HACK - where is this naming convention defined?
					if (pop.endsWith(".date.long") || pop.endsWith(".date.mdyy")) { // date fact
						schemaReference = pop.replaceAll("\\.date\\.(mdyy|long)$", "");
					} else if (pop.startsWith("dt.")) { // date attribute
					    schemaReference = pop.replaceAll("^dt\\..*\\.", "").replaceAll("_date$", "");
 					}
 				}
 				
 			} else if (c.getName().startsWith(cpPrefix)) {  // CONNECTION_POINT (or its LABEL)
 				prefixLen = cpPrefix.length();
 				name = c.getName().substring(prefixLen).replaceAll(".*\\." + N.NM_PFX, "");
 				// we don't support dropping connection points
 				// so this field may be either the same connection 
 				// point as in the local file or a label of it
 				if (sourceConnectionPoint == null) {
 					throw new UnsupportedOperationException("Dropping a connection point is not supported.");
 				}
 				if (name.equals(sourceConnectionPoint.getName())) {
 					remoteConnectionPointName = name;
 					ldmType = SourceColumn.LDM_TYPE_CONNECTION_POINT;
 				} else {
 					ldmType = SourceColumn.LDM_TYPE_LABEL;
 					reference = sourceConnectionPoint.getName();
 				}
 					
 			// fields populating a lookup table column
 			} else if (c.getName().startsWith(lookupPrefix)) {
 				prefixLen = lookupPrefix.length();
 				String nameAndRemoteField = c.getName().substring(prefixLen);
 				String referenceName = nameAndRemoteField.replaceAll("\\..*$", "");
 				name = nameAndRemoteField.replaceAll(".*\\." + N.NM_PFX, "");
 				if (name.equals(referenceName)) {
 					ldmType = SourceColumn.LDM_TYPE_ATTRIBUTE;
 				} else {
 					ldmType = SourceColumn.LDM_TYPE_LABEL;
 					reference = referenceName;
 				}
 				
 			// references to other data sets
 			} else if (c.getName().startsWith(N.LKP_PFX)) {
 				continue; // we cannot detect other changes than facts and attributes yet
 			} else {
 				throw new IllegalStateException(new Formatter().format(
 						"Unsupported naming convention: '%s' field in dataset '%s",
 						c.getName(), datasetId).toString());
 			}
 			final SourceColumn column = new SourceColumn(name, ldmType, name); // title (3rd) arg is ignored in this use case
 			if (reference != null) {
 				column.setReference(reference);
 			}
 			if (schemaReference != null) {
 				column.setSchemaReference(schemaReference);
 			}
 			remoteColumns.add(column);
 			if (!sourceColumns.contains(column)) {
 				deletedColumns.add(column);
 			}
 		}
 		if (sourceConnectionPoint != null && remoteConnectionPointName == null) {
 			throw new UnsupportedOperationException("Adding a new connection point is not supported yet.");
 		}
 	}
 	
 	List<SourceColumn> findNewColumns() {
 		return findDiff(sourceColumns, remoteColumns);
 	}
 	
 	List<SourceColumn> findDeletedColumns() {
 		return deletedColumns;
 	}
 	
 	List<SourceColumn> findDiff(Set<SourceColumn> src, Set<SourceColumn> tgt) {
 		final List<SourceColumn> result = new ArrayList<SourceColumn>();
 		for (final SourceColumn sc : src) {
 			if (!tgt.contains(sc)) {
 				result.add(sc);
 			}
 		}
 		return result;
 	}
 	
 }
