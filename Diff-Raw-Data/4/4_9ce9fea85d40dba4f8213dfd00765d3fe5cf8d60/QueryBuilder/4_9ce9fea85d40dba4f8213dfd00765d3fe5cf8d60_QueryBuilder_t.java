 package dbs.search.qb;
 import java.util.StringTokenizer;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Set;
 
 import dbs.search.parser.Constraint;
 import dbs.search.graph.GraphUtil;
 import dbs.sql.DBSSql;
 import dbs.util.Validate;
 import edu.uci.ics.jung.graph.Vertex;
 import edu.uci.ics.jung.graph.Edge;
 
 public class QueryBuilder {
 	KeyMap km;
 	//RelationMap rm = new RelationMap();
 	private ArrayList bindValues;
 	private ArrayList bindIntValues;
 	GraphUtil u = null;
 	private String db = "";
 	private String countQuery = "";
 	public QueryBuilder(String db) {
 		this.db = db;
 		bindValues = new ArrayList();
 		bindIntValues = new ArrayList();
 		km = new KeyMap();
 		//u = GraphUtil.getInstance("/home/sekhri/DBS/Servers/JavaServer/etc/DBSSchemaGraph.xml");
 		u = GraphUtil.getInstance("WEB-INF/DBSSchemaGraph.xml");
 	}
 	private String owner() throws Exception {
 		return DBSSql.owner();	
 	}
 	public String getCountQuery() {
 		return countQuery;	
 	}
 
 	public ArrayList getBindValues() {
 		return bindValues;
 	}
 	public ArrayList getBindIntValues() {
 		return bindIntValues;
 	}
 
 	public String genQuery(ArrayList kws, ArrayList cs, ArrayList okws) throws Exception{
 		return genQuery(kws, cs, okws, "", "");
 	}
 	//public String genQuery(ArrayList kws, ArrayList cs, String begin, String end) throws Exception{
 	public String genQuery(ArrayList kws, ArrayList cs, ArrayList okws, String begin, String end) throws Exception{
 		//Store all the keywors both from select and where in allKws
 		String personJoinQuery = "";
 		String parentJoinQuery = "";
 		String childJoinQuery = "";
 		String pathParentWhereQuery = "";
 		String groupByQuery = "";
 		String sumGroupByQuery = "";
 		String sumQuery = "";
 		boolean invalidFile = false;
 		boolean modByAdded = false;
 		boolean createByAdded = false;
 		boolean fileParentAdded = false;
 		boolean fileChildAdded = false;
 		boolean datasetParentAdded = false;
 		boolean procDsParentAdded = false;
 		boolean iLumi = isInList(kws, "ilumi");
 		boolean countPresent = false;
 		boolean sumPresent = false;
 		ArrayList allKws = new ArrayList();
 		if(isInList(kws, "file") || isInList(kws, "file.status")) {
 			invalidFile = true;
 			allKws = addUniqueInList(allKws, "FileStatus");
 
 		}
 		for (int i =0 ; i!= kws.size(); ++i) {
 			String aKw = (String)kws.get(i);
 			if(aKw.toLowerCase().startsWith("count") || aKw.toLowerCase().endsWith("count")) countPresent = true;
 			if(aKw.toLowerCase().startsWith("sum")) sumPresent = true;
 		}
 		String query = "SELECT DISTINCT \n\t";
 		for (int i =0 ; i!= kws.size(); ++i) {
 			String aKw = (String)kws.get(i);
 			if (i!=0) query += "\n\t,";
 			//If path supplied in select then always use block path. If supplied in where then user procDS ID
 			if(Util.isSame(aKw, "ilumi")) {
 				query += getIntLumiSelectQuery();
 			//System.out.println("line 2.1.1");
 			} else if(aKw.toLowerCase().startsWith("sum")) {
 				aKw = aKw.toLowerCase();
 				String keyword = aKw.substring(aKw.indexOf("(") + 1, aKw.indexOf(")"));
 				keyword = keyword.trim();
 				String asKeyword = keyword.replace('.', '_');
 				String entity = (new StringTokenizer(keyword, ".")).nextToken();
 				//System.out.println("entity " + entity);
 				String realName = u.getMappedRealName(entity);
 				allKws = addUniqueInList(allKws, realName);
				if(sumQuery.length() != 0) sumQuery += ",\n\t";
				else sumQuery += "SELECT ";
				sumQuery += "SUM(" + asKeyword + ") AS SUM_" + asKeyword + " ";
 				//query += "SUM(" + km.getMappedValue(keyword, true) + ") AS SUM_" + keyword.replace('.', '_') ;
 				String tmpKw = km.getMappedValue(keyword, true);
 				query +=  tmpKw + " AS " + asKeyword ;
 				if(iLumi) groupByQuery += tmpKw + ",";
 				String tmp =  makeQueryFromDefaults(u.getMappedVertex(entity));
 				tmp = tmp.substring(0, tmp.length() - 1); // To get rid of last space
 				query += "\n\t," + tmp + "_SUM ";
 			} else if(aKw.toLowerCase().startsWith("count")) {
 				aKw = aKw.toLowerCase();
 				String entity = aKw.substring(aKw.indexOf("(") + 1, aKw.indexOf(")"));
 				entity = entity.trim();
 				//System.out.println("entity = " + entity);
 				String realName = u.getMappedRealName(entity);
 				allKws = addUniqueInList(allKws, realName);
 
 				String defaultStr = u.getDefaultFromVertex(u.getVertex(realName));
 				if(defaultStr.indexOf(",") != -1)  throw new Exception("Cannot use count(" + entity + ")");
 				//query += "COUNT(DISTINCT " + realName + "." + defaultStr + ") AS COUNT";
 				query += realName + "." + defaultStr + " AS COUNT_SUB_" + realName;
 				if(sumQuery.length() != 0) sumQuery += ",\n\t";
 				else sumQuery += "SELECT ";
 				sumQuery += "COUNT(DISTINCT COUNT_SUB_" + realName + ") AS COUNT_" + realName;
 				/*if(sumPresent) {
 					sumQuery += ",\n\t COUNT AS COUNT";
 					sumGroupByQuery += " COUNT ,";
 				}*/
 			} else if(Util.isSame(aKw, "dataset")) {
 				allKws = addUniqueInList(allKws, "Block");
 				query += "Block.Path AS PATH";
 				if(iLumi) groupByQuery += "Block.Path,";
 				if(sumPresent || countPresent) {
 					sumQuery += ",\n\t PATH AS PATH";
 					sumGroupByQuery += " PATH ,";
 				}
 			} else {
 			//System.out.println("line 2.2");
 				if(iLumi && (i < 2) ) {
 					allKws = addUniqueInList(allKws, "Runs");
 					allKws = addUniqueInList(allKws, "LumiSection");
 				}
 				
 
 			//System.out.println("line 3");
 				StringTokenizer st = new StringTokenizer(aKw, ".");
 				int count = st.countTokens();
 				String token = st.nextToken();
 				Vertex vFirst = u.getMappedVertex(token);
 				String real = u.getRealFromVertex(vFirst);
 				allKws = addUniqueInList(allKws, real);
 			//System.out.println("line 4");
 				//if(Util.isSame(real, "LumiSection")) allKws = addUniqueInList(allKws, "Runs");
 				if(count == 1) {
 					//Get default from vertex
 			//System.out.println("line 5");
 					String tmp =  makeQueryFromDefaults(vFirst);	
 					query += tmp;
 					if(iLumi) groupByQuery += makeGroupQueryFromDefaults(vFirst);			
 					if(sumPresent || countPresent) {
 						String toSelect = makeSumSelect(tmp);
 						if(toSelect.length() != 0) {
 							sumQuery += ",\n\t" + toSelect + " AS" + toSelect;
 							sumGroupByQuery += toSelect + ",";
 						}
 					}
 						
 				} else {
 
 			//System.out.println("line 6");
 					boolean addQuery = true;
 					String token2 = st.nextToken();
 					String tmpTableName =  token + "_" + token2;
 					/*if(Util.isSame(token2, "algo")) {
 						allKws = addUniqueInList(allKws, "AppFamily");
 						allKws = addUniqueInList(allKws, "AppVersion");
 						allKws = addUniqueInList(allKws, "AppExecutable");
 						allKws = addUniqueInList(allKws, "QueryableParameterSet");
 						query += makeQueryFromDefaults(u.getVertex("AppFamily"));			
 						query += makeQueryFromDefaults(u.getVertex("AppVersion"));			
 						query += makeQueryFromDefaults(u.getVertex("AppExecutable"));			
 						query += makeQueryFromDefaults(u.getVertex("QueryableParameterSet"));
 						adQuery = false;
 					}*/
 					if(Util.isSame(token2, "release") ||
 							Util.isSame(token2, "tier")) {
 						String realName = u.getMappedRealName(token2);//AppVersion
 						allKws = addUniqueInList(allKws, realName);
 						String tmp = makeQueryFromDefaults(u.getVertex(realName));
 						query += tmp;	
 						if(iLumi) groupByQuery += makeGroupQueryFromDefaults(u.getVertex(realName));
 						if(sumPresent || countPresent) {
 							String toSelect = makeSumSelect(tmp);
 							if(toSelect.length() != 0) {
 								sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 								sumGroupByQuery += toSelect + ",";
 							}
 						}
 
 						addQuery = false;
 					}
 
 					if(Util.isSame(token, "release")) {
 						String realName = u.getMappedRealName(token);//AppVersion
 						allKws = addUniqueInList(allKws, realName);
 						String tmp = makeQueryFromDefaults(u.getVertex(realName));
 						query += tmp;			
 						if(iLumi) groupByQuery += makeGroupQueryFromDefaults(u.getVertex(realName));
 						if(sumPresent || countPresent) {
 							String toSelect = makeSumSelect(tmp);
 							if(toSelect.length() != 0) {
 								sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 								sumGroupByQuery += toSelect + ",";
 							}
 						}
 
 						addQuery = false;
 					}
 
 					if(Util.isSame(token2, "count")) {
 						String realName = u.getMappedRealName(token);
 
 						String defaultStr = u.getDefaultFromVertex(u.getVertex(realName));
 						if(defaultStr.indexOf(",") != -1)  throw new Exception("Cannot use count(" + token + ")");
 						query += realName + "." + defaultStr + " AS COUNT_SUB_" + realName;
 						if(sumQuery.length() != 0) sumQuery += ",\n\t";
 						else sumQuery += "SELECT ";
 						sumQuery += "COUNT(DISTINCT COUNT_SUB_" + realName + ") AS COUNT_" + realName;
 
 						/*query += "COUNT(DISTINCT " + realName + "." + defaultStr + ") AS COUNT";
 						if(sumPresent) {
 							sumQuery += ",\n\t COUNT AS COUNT ";
 							sumGroupByQuery += " COUNT ,";
 						}*/
 						addQuery = false;
 					}
 
 					if(Util.isSame(token2, "modby") || Util.isSame(token2, "createby")) {
 						boolean dontJoin = false;
 						String personField = "CreatedBy";
 						if(Util.isSame(token2, "modby")) {
 							if(modByAdded) dontJoin = true;
 							modByAdded = true;
 							personField = "LastModifiedBy";
 						} else {
 							if(createByAdded) dontJoin = true;
 							createByAdded = true;
 						}
 						//String tmpTableName =  token + "_" + token2;
 						if(!dontJoin) {
 							personJoinQuery += "\tJOIN " + owner() + "Person " + tmpTableName + "\n" +
 								"\t\tON " + real + "." + personField + " = " + tmpTableName + ".ID\n";
 						}
 						String fqName = tmpTableName + ".DistinguishedName";
 						query += fqName + makeAs(tmpTableName + "_DN");			
 						if(iLumi) groupByQuery += fqName + ",";
 						if(sumPresent || countPresent) {
 							sumQuery += ",\n\t" + tmpTableName + "_DN AS " + tmpTableName + "_DN ";
 							sumGroupByQuery += tmpTableName + "_DN ,";
 						}
 						addQuery = false;
 					}
 					
 					//if(Util.isSame(token2, "evnum") && Util.isSame(token, "file")) {
 					//	throw new Exception("You can find file based on file.evnum (find file where file.evenum = blah) but cannot find file.evnum");
 					//}
 					if(Util.isSame(token2, "evnum") && Util.isSame(token, "lumi")) {
 						throw new Exception("You can find lumi based on lumi.evnum (find lumi where lumi.evenum = blah) but cannot find lumi.evnum");
 					}
 
 					if(Util.isSame(token2, "parent") && Util.isSame(token, "file")) {
 						boolean dontJoin = false;
 						if(fileParentAdded) dontJoin = true;
 						fileParentAdded = true;
 						if(!dontJoin) parentJoinQuery += handleParent(tmpTableName, "Files", "FileParentage");
 						String fqName = tmpTableName + ".LogicalFileName";
 						query += fqName + makeAs(fqName);			
 						if(iLumi) groupByQuery += fqName + ",";
 						if(sumPresent || countPresent) {
 							String toSelect = makeSumSelect(makeAs(fqName)) + " ";
 							if(toSelect.length() != 0) {
 							       	sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 								sumGroupByQuery += toSelect + ",";
 							}
 						}
 			
 						addQuery = false;
 					}
 		
 					if(Util.isSame(token2, "child") && Util.isSame(token, "file")) {
 						boolean dontJoin = false;
 						if(fileChildAdded) dontJoin = true;
 						fileChildAdded = true;
 						//System.out.println("childJoinQuery " + childJoinQuery+ "  dontJoin " + dontJoin);
 						if(!dontJoin) childJoinQuery += handleChild(tmpTableName, "Files", "FileParentage");
 						String fqName = tmpTableName + ".LogicalFileName";
 						query += fqName + makeAs(fqName);			
 						if(iLumi) groupByQuery += fqName + ",";			
 						if(sumPresent || countPresent) {
 							String toSelect = makeSumSelect(makeAs(fqName)) + " ";
 							if(toSelect.length() != 0) {
 								sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 								sumGroupByQuery += toSelect + ",";
 							}
 						}
 	
 						addQuery = false;
 					}
 				
 					if(Util.isSame(token2, "parent") && Util.isSame(token, "procds")) {
 						boolean dontJoin = false;
 						if(procDsParentAdded) dontJoin = true;
 						procDsParentAdded = true;
 						if(!dontJoin) parentJoinQuery += handleParent(tmpTableName, "ProcessedDataset", "ProcDSParent");
 						String fqName = tmpTableName + ".Name";
 						query += fqName + makeAs(fqName);			
 						if(iLumi) groupByQuery += fqName + ",";	
 						if(sumPresent || countPresent) {
 							String toSelect = makeSumSelect(makeAs(fqName)) + " ";
 							if(toSelect.length() != 0) {
 							       	sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 								sumGroupByQuery += toSelect + ",";
 							}
 						}
 				
 						addQuery = false;
 					}
 
 					if(Util.isSame(token2, "parent") && Util.isSame(token, "dataset")) {
 			//System.out.println("line 8");
 						allKws = addUniqueInList(allKws, "Block");
 						boolean dontJoin = false;
 						if(datasetParentAdded) dontJoin = true;
 						datasetParentAdded = true;
 						if(!dontJoin) pathParentWhereQuery += handlePathParent();
 						String fqName = "Block.Path AS Dataset_Parent";
 						query += fqName;			
 						if(iLumi) groupByQuery +=  "Block.Path ,";		
 						if(sumPresent || countPresent) {
 							sumQuery += ",\n\t Dataset_Parent AS Dataset_Parent ";
 							sumGroupByQuery += " Dataset_Parent ,";
 						}
 			
 						addQuery = false;
 					}
 
 					if(Util.isSame(token, "dataset")) {
 						allKws = addUniqueInList(allKws, "ProcessedDataset");
 					}
 
 					Vertex vCombined = u.getMappedVertex(aKw);
 					if(vCombined == null) {
 						if(addQuery) {
 							String mapVal =  km.getMappedValue(aKw, true);
 							//if(mapVal.equals(aKw)) throw new Exception("The keyword " + aKw + " not yet implemented in Query Builder" );
 							query += mapVal + makeAs(mapVal); 
 							if(iLumi) groupByQuery += mapVal + ",";
 							if(sumPresent || countPresent) {
 								String toSelect = makeSumSelect(makeAs(mapVal));
 								if(toSelect.length() != 0) {
 									sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 									sumGroupByQuery += toSelect + ",";
 								}
 							}
 
 
 						}
 					} else {
 						allKws = addUniqueInList(allKws, u.getRealFromVertex(vCombined));
 						if(addQuery) {
 							String tmp = makeQueryFromDefaults(vCombined);
 							query += tmp;			
 							if(iLumi) groupByQuery += makeGroupQueryFromDefaults(vCombined);
 							if(sumPresent || countPresent) {
 								String toSelect = makeSumSelect(tmp);
 								if(toSelect.length() != 0) {
 									sumQuery += ",\n\t" + toSelect + " AS " + toSelect + " ";
 									sumGroupByQuery += toSelect + ",";
 								}
 							}
 
 
 						}
 						
 					}
 				}
 			}
 		}
 		if(iLumi && (cs.size() > 0) ) {
 			allKws = addUniqueInList(allKws, "Runs");
 			allKws = addUniqueInList(allKws, "LumiSection");
 		}
 
 		for (int i =0 ; i!= cs.size(); ++i) {
 			Object obj = cs.get(i);
 			if(i%2 == 0) {
 				Constraint o = (Constraint)obj;
 				String key = (String)o.getKey();
 				if(Util.isSame(key, "dataset")) {
 				} else if(Util.isSame(key, "release")) {
 					if(isInList(kws, "procds") || isInList(kws, "dataset")) allKws = addUniqueInList(allKws, "ProcAlgo");
 					else addUniqueInList(allKws, "FileAlgo");
 				} else if(Util.isSame(key, "dq")) {
 					allKws = addUniqueInList(allKws, km.getMappedValue(key, false));
 				} else {
 					if(Util.isSame(key, "file.status")) invalidFile = false;
 					StringTokenizer st = new StringTokenizer(key, ".");
 					int count = st.countTokens();
 					allKws = addUniqueInList(allKws, u.getMappedRealName(st.nextToken()));
 					if(count != 1) {
 						Vertex vCombined = u.getMappedVertex(key);
 						if(vCombined != null) allKws = addUniqueInList(allKws, u.getRealFromVertex(vCombined));
 							
 					}
 				} /*else {
 					//allKws = addUniqueInList(allKws, "ProcessedDataset");
 					allKws = addUniqueInList(allKws, "Block");
 					
 				}*/
 			}
 		}
 		
 		//Get the route which determines the join table
 		if(allKws.size() > 0) allKws = makeCompleteListOfVertexs(allKws);
 
 		//If File is not there then add Block
 		//Otherwise
 		for (int i =0 ; i!= cs.size(); ++i) {
 			Object obj = cs.get(i);
 			if(i%2 == 0) {
 				Constraint o = (Constraint)obj;
 				String key = (String)o.getKey();
 				if(Util.isSame(key, "dataset")) {
 					if(!isIn(allKws, "Files")) allKws = addUniqueInList(allKws, "Block");
 				}else if(key.startsWith("dataset")) allKws = addUniqueInList(allKws, "ProcessedDataset");
 			}
 		}
 		if(allKws.size() > 0) {
 			 allKws = makeCompleteListOfVertexs(allKws);
 	 		 allKws = sortVertexs(allKws);
 		}
 		int len = allKws.size();
 		/*for(int i = 0 ; i != len ; ++i ) {
 			System.out.println("kw " + (String)allKws.get(i));
 		}*/
 		if(isInList(kws, "ilumi")) {
 			if(len == 0) query += getIntLumiFromQuery();
 			else {
 				query += genJoins(allKws);
 				query += getIntLumiJoinQuery();
 			}
 		} else query += genJoins(allKws);
 			
 		query += personJoinQuery;
 		query += parentJoinQuery;
 		query += childJoinQuery;
 		personJoinQuery = "";
 		parentJoinQuery = "";
 		childJoinQuery = "";
 		String queryWhere = "";
 		if (cs.size() > 0) queryWhere += "\nWHERE\n";
 		
 		for (int i =0 ; i!= cs.size(); ++i) {
 			Object obj = cs.get(i);
 			if(i%2 == 0) {
 				Constraint co = (Constraint)obj;
 				String key = (String)co.getKey();
 				String op = (String)co.getOp();
 				String val = (String)co.getValue();
 				
 				if(Util.isSame(key, "dataset")) {
 					if(pathParentWhereQuery.length() > 0) {
 						queryWhere += pathParentWhereQuery + "";
 						bindValues.add(val);
 					}else {
 
 						// If path is given in where clause it should op should always be =
 						//if(!Util.isSame(op, "=")) throw new Exception("When Path is provided operater should be = . Invalid operater given " + op);
 						//queryWhere += "\tProcessedDataset.ID " + handlePath(val);
 						if(isIn(allKws, "Files")) queryWhere += "\tFiles.Block ";
 						else queryWhere += "\tBlock.ID ";
 						queryWhere += handlePath(val, op);
 					}
 				} else if(Util.isSame(key, "dq")) {
 					if(!Util.isSame(op, "=")) throw new Exception("When dq is provided operator should be = . Invalid operator given " + op);
 					queryWhere += "\tRuns.ID" + handleDQ(val);	
 				} else if(Util.isSame(key, "release")) {
 					//FIXME add FILEALGO and ProcALgo first
 					boolean useAnd = false;
 					if(isInList(kws, "procds") || isInList(kws, "dataset")) {
 						queryWhere += "\tProcAlgo.Algorithm " + handleRelease(op, val);
 						useAnd = true;
 					} else {
 						if(useAnd)  queryWhere += "\tAND\n";
 						queryWhere += "\tFileAlgo.Algorithm " + handleRelease(op, val);
 					}
 				} else if(Util.isSame(key, "file.release")) {
 					queryWhere += "\tFileAlgo.Algorithm" + handleRelease(op, val);
 				} else if(Util.isSame(key, "file.tier")) {
 					queryWhere += "\tFileTier.DataTier" + handleTier(op, val);
 				} else if(Util.isSame(key, "lumi.evnum")) {
 					if(!Util.isSame(op, "=")) throw new Exception("When evnum is provided operator should be = . Invalid operator given " + op);
 					queryWhere += handleEvNum(val);
 				} else if(Util.isSame(key, "procds.release")) {
 					queryWhere += "\tProcAlgo.Algorithm " + handleRelease(op, val);
 				} else if(Util.isSame(key, "procds.tier")) {
 					queryWhere += "\tProcDSTier.DataTier" + handleTier(op, val);
 				} else if(key.endsWith("createdate") ||  key.endsWith("moddate")) {
 					queryWhere += "\t" + km.getMappedValue(key, true) + handleDate(op, val);
 
 				} else {
 
 
 					//if(key.indexOf(".") == -1) throw new Exception("In specifying constraints qualify keys with dot operater. Invalid key " + key);
 
 					StringTokenizer st = new StringTokenizer(key, ".");
 					int count = st.countTokens();
 					boolean doGeneric = false;
 					if(count == 2) {
 						String token = st.nextToken();
 						String token2 = st.nextToken();
 						String tmpTableName =  token + "_" + token2;
 						if(Util.isSame(token2, "modby") || Util.isSame(token2, "createby")) {
 							boolean dontJoin = false;
 							String personField = "CreatedBy";
 							if(Util.isSame(token2, "modby")) {
 								if(modByAdded) dontJoin = true;
 								personField = "LastModifiedBy";
 								modByAdded = true;
 							} else {
 								if(createByAdded) dontJoin = true;
 								createByAdded = true;
 							}
 							//String tmpTableName =  token + "_" + token2;
 							if(!dontJoin)
 								personJoinQuery += "\tJOIN Person " + tmpTableName + "\n" +
 									"\t\tON " + u.getMappedRealName(token) + "." + personField + " = " + tmpTableName + ".ID\n";
 							queryWhere += tmpTableName + ".DistinguishedName ";			
 						} else	if(Util.isSame(token2, "parent") && Util.isSame(token, "file")) {
 							boolean dontJoin = false;
 							if(fileParentAdded) dontJoin = true;
 							fileParentAdded = true;
 							if(!dontJoin) parentJoinQuery += handleParent(tmpTableName, "Files", "FileParentage");
 							queryWhere += tmpTableName + ".LogicalFileName ";			
 						} else	if(Util.isSame(token2, "parent") && Util.isSame(token, "procds")) {
 							boolean dontJoin = false;
 							if(procDsParentAdded) dontJoin = true;
 							procDsParentAdded = true;
 							//String tmpTableName =  token + "_" + token2;
 							if(!dontJoin) parentJoinQuery += handleParent(tmpTableName, "ProcessedDataset", "ProcDSParent");
 							queryWhere += tmpTableName + ".Name ";			
 						} else	if(Util.isSame(token2, "child") && Util.isSame(token, "file")) {
 							boolean dontJoin = false;
 							if(fileChildAdded) dontJoin = true;
 							fileChildAdded = true;
 							if(!dontJoin) childJoinQuery += handleChild(tmpTableName, "Files", "FileParentage");
 							queryWhere += tmpTableName + ".LogicalFileName ";			
 						} else doGeneric = true;
 					
 					}else doGeneric = true;
 						
 					if(doGeneric) {
 						//Vertex vFirst = u.getMappedVertex(token);
 						Vertex vCombined = u.getMappedVertex(key);
 						if(vCombined == null) {
 							queryWhere += "\t" + km.getMappedValue(key, true) + " " ;
 						} else {
 						        queryWhere += "\t" + u.getRealFromVertex(vCombined) + "." + u.getDefaultFromVertex(vCombined) + " ";
 							//FIXME default can be list
 						}
 					}
 					queryWhere += handleOp(op, val);
 				}
 
 			} else {
 				//System.out.println("REL " + (String)obj);
 				queryWhere += "\n" + ((String)obj).toUpperCase() + "\n";
 			}
 		}
 		//System.out.println("\n\nFINAL query is \n\n" + query);
 		String circularConst = "";
 		boolean useAnd = false;
 		if((queryWhere.length() == 0) && isIn(allKws, "FileRunLumi")) circularConst = "\nWHERE ";
 		
 		if(isIn(allKws, "Files") && isIn(allKws, "FileRunLumi")) {
 			if(queryWhere.length() != 0 || useAnd) circularConst += "\n\tAND ";
 		       	circularConst += "FileRunLumi.Fileid = Files.ID";
 			useAnd = true;
 		}
 		if(isIn(allKws, "Runs") && isIn(allKws, "FileRunLumi")) {
 			if(queryWhere.length() != 0 || useAnd) circularConst += "\n\tAND ";
 			circularConst += "\n\tFileRunLumi.Run = Runs.ID";
 			useAnd = true;
 		}
 		if(isIn(allKws, "LumiSection") && isIn(allKws, "FileRunLumi")) {
 			if(queryWhere.length() != 0 || useAnd) circularConst += "\n\tAND ";
 			circularConst += "\n\tFileRunLumi.Lumi = LumiSection.ID";
 		}
 		
 		String invalidFileQuery = "FileStatus.Status <> ?";
 		String invalidConst = "";
 		if((queryWhere.length() == 0) && (circularConst.length() == 0) && (invalidFile)) {
 			invalidConst = "\nWHERE " + invalidFileQuery;
 			bindValues.add("INVALID");
 		}
 		if(((queryWhere.length() != 0) || (circularConst.length() != 0)) && (invalidFile)) {
 			invalidConst = "\nAND " + invalidFileQuery;
 			bindValues.add("INVALID");
 		}
 		
 		query += personJoinQuery + parentJoinQuery + childJoinQuery + queryWhere + circularConst + invalidConst;
 		if(groupByQuery.length() > 0) {
 			groupByQuery = groupByQuery.substring(0, groupByQuery.length() - 1);// to get rid of extra comma
 			query += "\n GROUP BY " + groupByQuery;
 		}
 
 		boolean orderOnce = false;
 		for(Object o: okws){
 			String orderBy = (String)o;
 			if(!orderOnce) {
 				query += " ORDER BY ";
 			}
 			if(orderOnce) query += ",";
 			String orderToken = "";
 			Vertex vCombined = u.getMappedVertex(orderBy);
 			if(vCombined == null) orderToken = km.getMappedValue(orderBy, true);
 			else orderToken = u.getRealFromVertex(vCombined) + "." + u.getDefaultFromVertex(vCombined);
 
 			query += orderToken;
 			orderOnce = true;
 		}
 		
 		if(sumQuery.length() != 0) {
 			query = sumQuery + " FROM (" +  query + ") sumtable ";
 			if(sumGroupByQuery.length() > 0) {
 				sumGroupByQuery = sumGroupByQuery.substring(0, sumGroupByQuery.length() - 1);// to get rid of extra comma
 				query += "\n GROUP BY " + sumGroupByQuery;
 			}
 		}
 		//countQuery = "SELECT COUNT(*) " + query.substring(query.indexOf("FROM"));
 		countQuery = "SELECT COUNT(*) FROM (" + query + ") x";
 		if(!begin.equals("") && !end.equals("")) {
 			int bInt = Integer.parseInt(begin);
 			int eInt = Integer.parseInt(end);
 			bindIntValues.add(new Integer(bInt));
 			if(db.equals("mysql")) {
 				bindIntValues.add(new Integer(eInt - bInt));
 				query += "\n\tLIMIT ?, ?";
 			}
 			if(db.equals("oracle")) {
 				bindIntValues.add(new Integer(eInt));
 				//query =  "SELECT * FROM (SELECT x.*, rownum as rnum FROM (\n" + query + "\n) x) where rnum between ? and ?";
 				query =  genOraclePageQuery(query);
 			}
 		}
 
 		return query;
 	}
 	
 	private String makeSumSelect(String tmp) {
 		String asStr = "AS";
 		int asIndex = tmp.indexOf(asStr);
 		if(asIndex != -1) {	
 			return  tmp.substring(asIndex + asStr.length(), tmp.length()).trim();
 		}
 		return "";
 	}
 
 	private String makeAs(String in) {
 		return " AS " + in.replace('.', '_') + " ";
 	}
 	
 	private String genOraclePageQuery(String query) {
 		System.out.println(query);
 		String tokenAS = "AS";
 		String tokenFrom = "FROM";
 		String tokenDistinct = "DISTINCT";
 		int indexOfFrom = query.indexOf(tokenFrom);
 		int indexOfDistinct = query.indexOf(tokenDistinct);
 		if(indexOfFrom == -1 || indexOfDistinct == -1) return query;
 		//System.out.println(indexOfFrom);
 		//System.out.println(indexOfDistinct);
 		String tmpStr = query.substring(indexOfDistinct + tokenDistinct.length(), indexOfFrom);
 		//System.out.println("tmp str " +  tmpStr);
 		StringTokenizer st = new StringTokenizer(tmpStr, ",");
 		int numberOfKeywords = st.countTokens();
 		String toReturn = "SELECT ";
 		for(int i = 0; i != numberOfKeywords; ++i) {
 			String tmpToken = st.nextToken();
 			int indexOfAs = tmpToken.indexOf(tokenAS);
 			if(indexOfAs == -1)  return query;
 			String finalKeyword = tmpToken.substring(indexOfAs + tokenAS.length(), tmpToken.length()).trim();
 			//System.out.println("Keyword " + finalKeyword);	
 			if(i != 0) toReturn += ", ";
 			toReturn += finalKeyword;
 
 		}
 		toReturn += " FROM (SELECT x.*, rownum as rnum FROM (\n" + query + "\n) x) where rnum between ? and ?";
 		return toReturn;
 			
 			
 	}
 
 	private String makeQueryFromDefaults(Vertex v){
 		String realVal = u.getRealFromVertex(v);
 		StringTokenizer st = new StringTokenizer(u.getDefaultFromVertex(v), ",");
 		int countDefTokens = st.countTokens();
 		String query = "";
 		for (int j = 0; j != countDefTokens; ++j) {
 			if(j != 0) query += ",";
 			String token = st.nextToken();
 			query += realVal + "." + token + makeAs(realVal + "." + token);
 		}
 		return query;
 
 	}
 	
 	private String makeGroupQueryFromDefaults(Vertex v){
 		String realVal = u.getRealFromVertex(v);
 		StringTokenizer st = new StringTokenizer(u.getDefaultFromVertex(v), ",");
 		int countDefTokens = st.countTokens();
 		String query = "";
 		for (int j = 0; j != countDefTokens; ++j) {
 			String token = st.nextToken();
 			query += realVal + "." + token + ",";
 		}
 		return query;
 
 	}
 
 	private String genJoins(ArrayList lKeywords) throws Exception {
 		//ArrayList uniquePassed = new ArrayList();
 		String prev = "";
 		String query = "\nFROM\n\t"  + owner() + (String)lKeywords.get(0) + "\n";
 		int len = lKeywords.size();
 		for(int i = 1 ; i != len ; ++i ) {
 			
 			for(int j = (i-1) ; j != -1 ; --j ) {
 					String v1 = (String)lKeywords.get(i);
 					String v2 = (String)lKeywords.get(j);
 					//if(! (isIn(uniquePassed, v1 + "," + v2 )) && !(isIn(uniquePassed, v2 + "," + v1))) {
 						if(u.doesEdgeExist(v1, v2)) {
 							//System.out.println("Relation bwteen " + v1 + " and " + v2 + " is " + u.getRealtionFromVertex(v1, v2));
 							String tmp = u.getRealtionFromVertex(v1, v2);
 							query += "\t";
 							if(Util.isSame(v1, "FileChildage")) v1 = "FileParentage";
 							if(Util.isSame(v1, "FileParentage") ||
 									Util.isSame(v1, "ProcDSParent")) query += "LEFT OUTER ";
 							query += "JOIN " + owner() +  v1 + "\n";
 							query += "\t\tON " + tmp + "\n";
 							//uniquePassed.add(v1 + "," + v2);
 							break;
 						}
 					//}
 			}
 		}
 
 		return query;
 	}
 	
 	private boolean isIn(ArrayList aList, String key) {
 		for (int i = 0 ; i != aList.size(); ++i) {
 			if( ((String)(aList.get(i) )).equals(key)) return true;
 		}
 		return false;
 	}
 	
 	/*private String genJoins(String[] routes) {
 		String prev = "";
 		String query = "\nFROM\n\t";
 		for(String s: routes) {
 			if(!prev.equals("")) {
 				//System.out.println(prev + "," + s);
 				String tmp = rm.getMappedValue(prev + "," + s);
 				//System.out.println(tmp);
 				query += "\tJOIN " + s + "\n";
 				query += "\t\tON " + tmp + "\n";
 			} else query += s + "\n";
 			prev = s;
 		}
 		return query;
 	}*/
 
 	private String handleParent(String tmpTableName, String table1, String table2) throws Exception {
 		return ( "\tLEFT OUTER JOIN " + owner() + table1 + " " + tmpTableName + "\n" +
 				"\t\tON " + tmpTableName + ".ID = " + table2 + ".ItsParent\n" );
 
 	}
 	private String handleChild(String tmpTableName, String table1, String table2) throws Exception {
 		return ( "\tLEFT OUTER JOIN " + owner() + table1 + " " + tmpTableName + "\n" +
 				"\t\tON " + tmpTableName + ".ID = " + table2 + ".ThisFile\n" );
 
 	}
 
 	private String handlePathParent() throws Exception {
 		String sql = "Block.ID in  \n" +
 			"\t(" + DBSSql.listPathParent() + ")\n";
 		
 		return sql;
 	}
 	private String handleLike(String val) {
 		bindValues.add(val.replace('*','%'));
 		return "LIKE ?";
 	}
 
 	private String handleIn(String val) {
     		String query = "IN (";
     		StringTokenizer st = new StringTokenizer(val, ",");
 		int count =  st.countTokens();
 		for(int k = 0 ; k != count ; ++k) {
 			if(k != 0) query += ",";
 			//query += "'" + st.nextToken() + "'";
 			query += "?";
 			bindValues.add(st.nextToken());
 		}
 		query += ")";
 		return query;
 	}
 	private String handleOp(String op, String val) {
 		String query = "";
 		if(Util.isSame(op, "in")) query += handleIn(val);
 		else if(Util.isSame(op, "like")) query += handleLike(val);
 		else {
 			query += op + " ?\n";
 			bindValues.add(val);
 		}
 		return query;
 	}
 
 	private String handleEvNum(String val) {
 		String query = "\tLumiSection.StartEventNumber <= ?\n" +
 				"\t AND \n" +
 				"\tLumiSection.EndEventNumber >= ?\n";
 		bindValues.add(val);
 		bindValues.add(val);
 		return query;
 	}
 
 	/*private String handlePath(String path) throws Exception {
 		Validate.checkPath(path);
 		String[] data = path.split("/");
 		if(data.length != 4) {
 			throw new Exception("Invalid path " + path);
 		}
 		ArrayList route = new ArrayList();
 		route.add("PrimaryDataset");
 		route.add("ProcessedDataset");
 		String query = " IN ( \n" +
 			"SELECT \n" +
 			"\tProcessedDataset.ID " + genJoins(route) +
 			"WHERE \n" + 
 			//"\tPrimaryDataset.Name = '" + data[1] + "'\n" +
 			"\tPrimaryDataset.Name = ?\n" +
 			"\tAND\n" +
 			//"\tProcessedDataset.Name = '" + data[2] + "'" +
 			"\tProcessedDataset.Name = ?" +
 			")";
 		bindValues.add(data[1]);
 		bindValues.add(data[2]);
 		return query;
 	}*/
 
 	private String handleDate(String op, String val) throws Exception {
 		if(Util.isSame(op, "in")) throw new Exception("Operator IN not supported with date. Please use =, < or >");
 		if(Util.isSame(op, "like")) throw new Exception("Operator LIKE not supported with date. Please use =, < or >");
 		String query = "";
 		String epoch1 = String.valueOf(DateUtil.dateStr2Epoch(val) / 1000);
 		if(Util.isSame(op, "=")) {
 			String epoch2 = String.valueOf(DateUtil.getNextDate(val).getTime() / 1000);
 			query += " BETWEEN ? AND ?\n";
 			bindValues.add(epoch1);
 			bindValues.add(epoch2);
 
 		} else {
 			query += " " + op + " ?\n";
 			bindValues.add(epoch1);
 		}
 		return query;
 	}
 
 	private String handlePath(String path, String op) throws Exception {
 		String query = " IN ( \n" +
 			"SELECT \n" +
 			"\tBlock.ID FROM " + owner() + "Block" +
 			"\tWHERE \n" + 
 			//"\tBlock.Path " + op + " '" + path + "'\n" +
 			"\tBlock.Path ";// + op + " ?\n" +
 			//")";
 		/*if(Util.isSame(op, "in")) query += handleIn(path);
 		else if(Util.isSame(op, "like")) query += handleLike(path);
 		else {
 			query += op + " ?\n";
 			bindValues.add(path);
 		}*/
 		query += handleOp(op, path) + ")";
 		return query;
 	}
 	private String handleDQ(String val) throws Exception {
 		//System.out.println("VAL is " + val);
 		ArrayList sqlObj = DBSSql.listRunsForRunLumiDQ(null, val);
 		String dqQuery = "";
 		if(sqlObj.size() == 2) {
 			dqQuery = (String)sqlObj.get(0);
 			Vector bindVals = (Vector)sqlObj.get(1);
 			
 			for(Object s: bindVals) bindValues.add((String)s);
 		}
 		//call DQ function
 		//List<String> bindValuesFromDQ = ; //Get from DQ function
 		//for(String s: bindValues) bindValues.add(s);
 		String query = " IN ( \n" + dqQuery + ")";
 		return query;
 	}
 
 	private String handleRelease(String op, String version) throws Exception {
 		Validate.checkWord("AppVersion", version);
 		ArrayList route = new ArrayList();
 		route.add("AlgorithmConfig");
 		route.add("AppVersion");
 		String query = " IN ( \n" +
 			"SELECT \n" +
 			"\tAlgorithmConfig.ID " + genJoins(route) +
 			"WHERE \n" + 
 			//"\tAppVersion.Version = '" + version + "'\n" +
 			"\tAppVersion.Version " + handleOp(op, version) + "\n" +
 			")";
 		return query;
 	}
 
 	private String handleTier(String op, String tier) throws Exception {
 		Validate.checkWord("DataTier", tier);
 		ArrayList route = new ArrayList();
 		String query = " IN ( \n" +
 			"SELECT \n" +
 			"\tDataTier.ID FROM " + owner() + "DataTier "  +
 			"WHERE \n" + 
 			"\tDataTier.Name " + handleOp(op, tier) + "\n" +
 			")";
 		return query;
 	}
 
 	private ArrayList addUniqueInList(ArrayList keyWords, String aKw) {
 		for(Object kw: keyWords) {
 			if(((String)kw).equals(aKw))return keyWords;
 		}
 		keyWords.add(aKw);
 		return keyWords;
 	}
 
 	private boolean isInList(ArrayList keyWords, String aKw) {
 			//System.out.println("line 3.1");
 		for(Object kw: keyWords) {
 			if(((String)kw).equals(aKw))return true;
 		}
 		return false;
 	}
 
 	
 	private String getIntLumiSelectQuery() {
 		return ("\n\tSUM(ldblsum.INSTANT_LUMI * 93 * (1 - ldblsum.DEADTIME_NORMALIZATION) * ldblsum.NORMALIZATION) AS INTEGRATED_LUMINOSITY, " +
 				"\n\tSUM(ldblsum.INSTANT_LUMI_ERR * ldblsum.INSTANT_LUMI_ERR) AS INTEGRATED_ERROR");
 	}
 
 	private String getIntLumiFromQuery() {
 		return ("\n\tFROM CMS_LUMI_PROD_OFFLINE.LUMI_SUMMARIES ldblsum" +
 			"\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_SECTIONS ldbls" +
 			"\n\t\tON ldblsum.SECTION_ID = ldbls.SECTION_ID" +
 			"\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_VERSION_TAG_MAPS ldblvtm" +
 			"\n\t\tON ldblvtm.LUMI_SUMMARY_ID = ldblsum.LUMI_SUMMARY_ID" +
 			"\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_TAGS ldblt" +
 			"\n\t\tON ldblt.LUMI_TAG_ID =  ldblvtm.LUMI_TAG_ID");
 	}
 
 	private String getIntLumiJoinQuery() {
 		return ("\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_SECTIONS ldbls" +
 			"\n\t\tON Runs.RunNumber = ldbls.RUN_NUMBER" +
 			"\n\t\tAND LumiSection.LumiSectionNumber = ldbls.LUMI_SECTION_NUMBER" +
 			"\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_SUMMARIES ldblsum" +
 			"\n\t\tON ldblsum.SECTION_ID = ldbls.SECTION_ID" +
 			"\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_VERSION_TAG_MAPS ldblvtm" +
 			"\n\t\tON ldblvtm.LUMI_SUMMARY_ID = ldblsum.LUMI_SUMMARY_ID" +
 			"\n\tJOIN CMS_LUMI_PROD_OFFLINE.LUMI_TAGS ldblt" +
 			"\n\t\tON ldblt.LUMI_TAG_ID =  ldblvtm.LUMI_TAG_ID");
 	}
 
 
 
 	private ArrayList makeCompleteListOfVertexsOld(ArrayList lKeywords) {
 		int len = lKeywords.size();
 		if(len <= 1) return lKeywords;
 		for(int i = 0 ; i != len ; ++i ) {
 			boolean isEdge = false;
 			for(int j = 0 ; j != len ; ++j ) {
 				if(i != j) {
 					//System.out.println("Checking " + lKeywords.get(i) + " with " + lKeywords.get(j) );
 					if(u.doesEdgeExist((String)lKeywords.get(i), (String)lKeywords.get(j)))	{
 						isEdge = true;
 						break;
 					}
 				}
 			}
 			if(!isEdge) {
 				//System.out.println("Shoertest edge in " + (String)lKeywords.get(i) + " --- " + (String)lKeywords.get((i+1)%len));
 				List<Edge> lEdges =  u.getShortestPath((String)lKeywords.get(i), (String)lKeywords.get((i+1)%len));
 				for (Edge e: lEdges) {
 					//System.out.println("PATH " + u.getFirstNameFromEdge(e) + "  --- " + u.getSecondNameFromEdge(e));
 					lKeywords = addUniqueInList(lKeywords, u.getFirstNameFromEdge(e));
 					lKeywords = addUniqueInList(lKeywords, u.getSecondNameFromEdge(e));
 				}
 				//System.out.println("No edge callin again ---------> \n");
 				lKeywords =  makeCompleteListOfVertexs (lKeywords);
 				return lKeywords;
 
 			}
 		}
 		return lKeywords;
 	}
 
 
 	private ArrayList makeCompleteListOfVertexs(ArrayList lKeywords) {
 		ArrayList myRoute = new ArrayList();
 		myRoute.add(lKeywords.get(0));
 		lKeywords.remove(0);
 		int len = lKeywords.size();
 		int prevLen = 0;
 		while(len != 0) {
 			boolean breakFree = false;
 			for(int i = 0 ; i != len ; ++i ) {
 				int lenRount = myRoute.size();
 				for(int j = 0 ; j != lenRount ; ++j ) {
 					String keyInMyRoute = (String)myRoute.get(j);
 					String keyInArray = (String)lKeywords.get(i);
 					if(keyInArray.equals(keyInMyRoute)) {
 						lKeywords.remove(i);
 						breakFree = true;
 						break;
 					} else if(u.doesEdgeExist(keyInMyRoute, keyInArray))	{
 						myRoute = addUniqueInList(myRoute, keyInArray);
 						lKeywords.remove(i);
 						breakFree = true;
 						break;
 					}
 				}
 				if(breakFree) break;
 				
 			}
 			if(prevLen == len) {
 				//System.out.println("Shortest edge in " + (String)lKeywords.get(0) + " --- " + (String)myRoute.get(0));
 				List<Edge> lEdges =  u.getShortestPath((String)lKeywords.get(0), (String)myRoute.get(0));
 				for (Edge e: lEdges) {
 					//System.out.println("PATH " + u.getFirstNameFromEdge(e) + "  --- " + u.getSecondNameFromEdge(e));
 					myRoute = addUniqueInList(myRoute, u.getFirstNameFromEdge(e));
 					myRoute = addUniqueInList(myRoute, u.getSecondNameFromEdge(e));
 				}
 				if(lEdges.size() > 0) lKeywords.remove(0);
 				else {
 					myRoute = addUniqueInList(myRoute, (String)lKeywords.get(0));
 					lKeywords.remove(0);
 					////System.out.println("Path length is 0");
 				}
 			}
 			
 			prevLen = len;
 			len = lKeywords.size();
 		}
 		return myRoute;
 	}
 
 
 	
 	public ArrayList sortVertexs(ArrayList lKeywords) {
 		//System.out.println("INSIDE sortVertexs");
 		int len = lKeywords.size();
 		String leaf = "";
 		for(int i = 0 ; i != len ; ++i ) {
 			String aVertex = (String)lKeywords.get(i);
 			if(isLeaf(aVertex, lKeywords)) {
 				leaf = aVertex;
 				break;
 			}
 		}
 		//System.out.println("leaf " + leaf);
 		if(leaf.equals("")) leaf = (String)lKeywords.get(0);
 		//System.out.println("leaf again " + leaf);
 		ArrayList toReturn = new ArrayList();
 		toReturn.add(leaf);
 		
 		int reps = -1;
 		while( toReturn.size() != len) {
 			++reps;
 			for(int j = 0 ; j != len ; ++j ) {
 				String aVertex = (String)lKeywords.get(j);
 				if(!aVertex.equals(leaf)) {
 					if(!isIn(toReturn, aVertex)) {
 						if(isLeaf(aVertex, lKeywords)) {
 							//System.out.println(aVertex + " is a leaf toreturn size " + toReturn.size() + " len -1 " + (len - 1));
 							//if(toReturn.size() ==1) System.out.println("toReturn.0 " + (String)toReturn.get(0));
 							if(toReturn.size() == (len - 1)) toReturn = addUniqueInList(toReturn, aVertex);
 							else if(reps > len) {
 								toReturn = addUniqueInList(toReturn, aVertex); 
 								//System.out.println("adding " + aVertex);
 							}
 						} else {
 							for (int k = (toReturn.size() - 1) ; k != -1 ; --k) {
 								//System.out.println("Cheking edge between " + (String)toReturn.get(k) + " and " + aVertex);
 								if(u.doesEdgeExist((String)toReturn.get(k), aVertex)) {
 									toReturn = addUniqueInList(toReturn, aVertex);
 									break;
 								} else {
 									if(reps > len) toReturn = addUniqueInList(toReturn, aVertex);
 									//System.out.println("no edge between " +   (String)toReturn.get(k) + " and " + aVertex);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return toReturn;
 	}
 	private boolean isLeaf(String aVertex, ArrayList lKeyword) {
 		int count = 0;
 		Set s = u.getVertex(aVertex).getNeighbors();
 		for (Iterator eIt = s.iterator(); eIt.hasNext(); ) {
 			String neighbor = u.getRealFromVertex((Vertex) eIt.next());
 			//System.out.println("neighbour " + neighbor);
 			if(isIn(lKeyword, neighbor)) ++count;
 		}
 		if(count == 1) return true;
 		return false;
 
 	}
 
 	public static void main(String args[]) throws Exception{
 		QueryBuilder qb = new QueryBuilder("oracle");
 		ArrayList tmp = new ArrayList();
 		/*GraphUtil u = GraphUtil.getInstance("/home/sekhri/DBS/Servers/JavaServer/etc/DBSSchemaGraph.xml");
 		List<Edge> lEdges =  u.getShortestPath("ProcessedDataset", "LumiSection");
 		for (Edge e: lEdges) {
 			System.out.println("PATH " + u.getFirstNameFromEdge(e) + "  --- " + u.getSecondNameFromEdge(e));
 		}*/
 
 		//tmp.add("PrimaryDataset");
 		tmp.add("file");
 		System.out.println(qb.genQuery(tmp, new ArrayList(),new ArrayList(), "4", "10"));		
 		//tmp.add("Runs");
 		//tmp.add("FileRunLumi");
 		
 		//tmp.add("ProcessedDataset");
 		//tmp.add("FileType");
 		//tmp.add("ProcDSRuns");
 		/*tmp = qb.sortVertexs(tmp);
 		//tmp = qb.makeCompleteListOfVertexs(tmp);
 		for (int i =0 ; i!=tmp.size() ;++i ) {
 			System.out.println("ID " + tmp.get(i));
 		}*/
 	}
 
 }
