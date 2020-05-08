 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.jmx.adaptor.snmp.agent;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.jboss.jmx.adaptor.snmp.config.attribute.ManagedBean;
 import org.jboss.jmx.adaptor.snmp.config.attribute.MappedAttribute;
 import org.jboss.logging.Logger;
 import org.snmp4j.smi.OID;
 import org.snmp4j.smi.OctetString;
 import org.snmp4j.smi.Variable;
 
 /**
  * @author jean.deruelle@gmail.com
  * 
  */
 public class AttributeTableMapper {
 
 	private SortedSet<OID> tables = new TreeSet<OID>();
 //	private SortedSet<OID> tableRowEntrys = new TreeSet<OID>();
 	/**
 	 * keep an index of the OID from attributes.xml
 	 */	
 	private SortedMap<OID, BindEntry> tableMappings = new TreeMap<OID, BindEntry>();
 //	private SortedMap<OID, BindEntry> tableRowEntryMappings = new TreeMap<OID, BindEntry>();
 
 	private MBeanServer server;
 	private Logger log;
 
 	public AttributeTableMapper(MBeanServer server, Logger log) {
 		this.server = server;
 		this.log = log;
 	}
 	
 	/**
 	 * 
 	 * @param oid
 	 * @return
 	 */
 	public BindEntry getTableBinding(OID oid, boolean isRowEntry) {
 		Set<Entry<OID,BindEntry>> entries = null;
 //		if(isRowEntry) {
 //			entries = tableRowEntryMappings.entrySet();
 //		} else {
 			entries = tableMappings.entrySet();
 //		}
 		for (Entry<OID,BindEntry> entry : entries) {			
 			if (oid.startsWith(entry.getKey())) {
 				BindEntry value = entry.getValue();
 				BindEntry bindEntry = (BindEntry) value.clone();
 				int[] oidValue = oid.getValue();
 				int[] subOid = new int[oid.size() - entry.getKey().size()];
 				System.arraycopy(oidValue, entry.getKey().size(), subOid, 0, oid.size() - entry.getKey().size());
 				if(subOid.length > 0) {
 					bindEntry.setTableIndexOID(new OID(subOid));
 				}
 				return bindEntry;
 			}			
 		}
 		return null;
 	}
 	
 	public OID getNextTable(OID oid) {
 		OID currentOID = oid;
 		// means that the oid is the one from the table itself
 		boolean isRowEntry = false;
 		if(tables.contains(oid)) {
 			currentOID = oid.append(1);
 		}
 //		if(tableRowEntrys.contains(currentOID)) {
 //			currentOID = oid.append(1);
 //			isRowEntry = true;
 //		}
 		BindEntry be = getTableBinding(currentOID, isRowEntry);
 //		if(be == null) {
 //			be = getTableBinding(currentOID, true);
 //			isRowEntry = true;
 //		}
 		if(be == null) {
 			return null; // it's not there
 		}
 		Object val = null;
 		try {
 			val = server.getAttribute(be.getMbean(), be.getAttr().getName());
 		} catch(Exception e) {
 			log.error("Impossible to fetch " + be.getAttr().getName());
 			return null;
 		}
 		OID tableIndexOID = be.getTableIndexOID();
 		if(tableIndexOID == null) {
 			if(val instanceof Map) {
				Set<Object> keySet = ((Map)val).keySet();
 				if(keySet.size() > 0) {
 					return new OID(currentOID.append("'" + keySet.iterator().next().toString() + "'"));
 				} else {
 					return null;
 				}
 			} else {
 				return new OID(currentOID).append(1);
 			}			
 		}		
 		if(val instanceof List) {
 			int index = Integer.valueOf(tableIndexOID.toString());
 			if(index - 1 < 0) {
 				return null;
 			}
 			index++;
 			if(index <= ((List)val).size()) { 
 				return new OID(currentOID.trim().append(index));
 			} else {
 //				if(isRowEntry) {
 //					return new OID(currentOID.trim().trim().append(2).append(1));
 //				} else {
 					return null;
 //				}
 			}
 		}
 		if(val instanceof Map) {	
 //			if(tableIndexOID.size() <= 1) {
 //				int index = Integer.valueOf(tableIndexOID.toString());
 //				if(index - 1 < 0) {
 //					return null;
 //				}
 //				index++;
 //				if(index <= ((Map)val).size()) { 
 //					return new OID(currentOID.trim().append(index));
 //				} else {
 //					Set<Object> keySet = ((Map)val).keySet();
 //					if(keySet.size() > 0) {
 //						return new OID(currentOID.trim().trim().append(2).append("'" + keySet.iterator().next().toString() + "'"));
 //					} else {
 //						return null;
 //					}
 //				}
 //		} else {
 				String key = new String(tableIndexOID.toByteArray());
				Iterator<Object> keySet = ((Map)val).keySet().iterator();
 				while (keySet.hasNext()) {
 					Object entryKey = keySet.next();
 					if(entryKey.equals(key)) {
 						if(keySet.hasNext()) {
 							Object nextKey = keySet.next();
 							OID nextOID = new OID(currentOID);
 							nextOID.trim(tableIndexOID.size());
 							nextOID.append("'" + nextKey + "'");
 							return nextOID;
 						} else {
 							return null;
 						}
 					}
 				}
 				return null;
 //			}			
 		}
 		if (val instanceof int[]) {
 			int index = Integer.valueOf(tableIndexOID.toString());
 			if(index - 1 < 0) {
 				return null;
 			}
 			index++;
 			if(index <= ((int[])val).length) { 
 				return new OID(currentOID.trim().append(index));
 			} else {
 				if(isRowEntry) {
 					return new OID(currentOID.trim().trim().append(2).append(1));
 				} else {
 					return null;
 				}
 			}
 		}
 		if (val instanceof long[]) {
 			int index = Integer.valueOf(tableIndexOID.toString());
 			if(index - 1 < 0) {
 				return null;
 			}
 			index++;
 			if(index <= ((long[])val).length) { 
 				return new OID(currentOID.trim().append(index));
 			} else {
 				if(isRowEntry) {
 					return new OID(currentOID.trim().trim().append(2).append(1));
 				} else {
 					return null;
 				}
 			}
 		}
 		if (val instanceof boolean[]) {
 			int index = Integer.valueOf(tableIndexOID.toString());
 			if(index - 1 < 0) {
 				return null;
 			}
 			index++;
 			if(index <= ((boolean[])val).length) { 
 				return new OID(currentOID.trim().append(index));
 			} else {
 				if(isRowEntry) {
 					return new OID(currentOID.trim().trim().append(2).append(1));
 				} else {
 					return null;
 				}
 			}
 		}
 		if (val instanceof Object[]) {
 			int index = Integer.valueOf(tableIndexOID.toString());
 			if(index - 1 < 0) {
 				return null;
 			}
 			index++;
 			if(index <= ((Object[])val).length) { 
 				return new OID(currentOID.trim().append(index));
 			} else {
 				if(isRowEntry) {
 					return new OID(currentOID.trim().trim().append(2).append(1));
 				} else {
 					return null;
 				}
 			}
 		}
 		return null;
 	}	
 
 	/**
 	 * 
 	 * @param mmb
 	 * @param oname
 	 */
 	public void addTableMapping(ManagedBean mmb, MappedAttribute ma) {
 		String oid;
 		String oidPrefix = mmb.getOidPrefix();
 		if (oidPrefix != null) {
 			oid = oidPrefix + ma.getOid();
 		} else {
 			oid = ma.getOid();
 		}
 		OID coid = new OID(oid);
 		BindEntry be = new BindEntry(coid, mmb.getName(), ma.getName());
 		be.setReadWrite(ma.isReadWrite());
 		be.setTable(ma.isAttributeTable());
 
 		if (log.isTraceEnabled())
 			log.trace("New bind entry   " + be);
 		if (tables.contains(coid)) {
 			log.info("Duplicate oid " + coid + RequestHandlerImpl.SKIP_ENTRY);
 		}
 		if (mmb == null || mmb.equals("")) {
 			log.info("Invalid mbean name for oid " + coid + RequestHandlerImpl.SKIP_ENTRY);
 		}
 		if (ma == null || ma.equals("")) {
 			log.info("Invalid attribute name " + ma + " for oid " + coid
 					+ RequestHandlerImpl.SKIP_ENTRY);
 		}
 //		tableRowEntrys.add(coid);
 		tables.add(coid.trim());
 //		tableRowEntryMappings.put(new OID(coid).append(1), be);
 		tableMappings.put(new OID(coid), be);	
 //		tableMappings.put(new OID(coid.trim()), be);
 	}
 
 	public boolean belongsToTables(OID oid) {
 		for (OID attributeOID : tables) {			
 			if (oid.startsWith(attributeOID)) {
 				return true;
 			}			
 		}
 		return false;
 	}
 
 	public void removeTableMapping(ManagedBean mmb, MappedAttribute ma) {
 		
 	}
 
 	public Variable getIndexValue(OID oid) {
 		BindEntry be = getTableBinding(oid, true);
 		Object val = null;
 		if(be == null) {
 			return null;
 		}
 		try {
 			val = server.getAttribute(be.getMbean(), be.getAttr().getName());
 		} catch(Exception e) {
 			log.error("Impossible to fetch " + be.getAttr().getName());
 			return null;
 		}
 		OID tableIndexOID = be.getTableIndexOID();
 		if(val instanceof List) {			
 			return new OctetString("" + oid.get(oid.size()-1));
 		}
 		if(val instanceof Map) {	
 			int index = oid.get(oid.size()-1);
 			int i = 1;
 			for(Object key : ((Map) val).keySet()) {
 				if(i == index) {
 					return new OctetString((String)key);
 				}
 				i++;
 			}			
 		}
 		if (val instanceof int[]) {
 			return new OctetString("" + oid.get(oid.size()-1));
 		}
 		if (val instanceof long[]) {
 			return new OctetString("" + oid.get(oid.size()-1));
 		}
 		if (val instanceof boolean[]) {
 			return new OctetString("" + oid.get(oid.size()-1));
 		}
 		if (val instanceof Object[]) {
 			return new OctetString("" + oid.get(oid.size()-1));
 		}
 		return null;
 	}
 }
