 package com.eucalyptus.webui.server;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import com.eucalyptus.webui.client.service.EucalyptusServiceException;
 import com.eucalyptus.webui.client.service.SearchRange;
 import com.eucalyptus.webui.client.service.SearchResult;
 import com.eucalyptus.webui.client.service.SearchResultRow;
 import com.eucalyptus.webui.client.session.Session;
 import com.eucalyptus.webui.server.config.ViewSearchTableServerConfig;
 import com.eucalyptus.webui.server.db.ResultSetWrapper;
 import com.eucalyptus.webui.server.device.DeviceTemplateService;
 import com.eucalyptus.webui.server.user.UserAppDBProcWrapper;
 import com.eucalyptus.webui.server.user.UserAppSyncException;
 import com.eucalyptus.webui.server.vm.VITDBProcWrapper;
 import com.eucalyptus.webui.server.vm.VITSyncException;
 import com.eucalyptus.webui.server.vm.VmImageType;
 import com.eucalyptus.webui.shared.config.EnumService;
 import com.eucalyptus.webui.shared.config.LanguageSelection;
 import com.eucalyptus.webui.shared.config.SearchTableCol;
 import com.eucalyptus.webui.shared.dictionary.DBTableColName;
 import com.eucalyptus.webui.shared.dictionary.Enum2String;
 import com.eucalyptus.webui.shared.resource.AppResources;
 import com.eucalyptus.webui.shared.resource.device.TemplateInfo;
 import com.eucalyptus.webui.shared.user.EnumUserAppStatus;
 import com.eucalyptus.webui.shared.user.LoginUserProfile;
 import com.eucalyptus.webui.shared.user.UserApp;
 import com.eucalyptus.webui.shared.user.UserAppStateCount;
 import com.google.common.base.Strings;
 
 public class UserAppServiceProcImpl {
 	  
 	  public void addUserApp(Session session, UserApp userApp) throws EucalyptusServiceException {
 		  
 		  userApp.setStatus(EnumUserAppStatus.APPLYING);
 		  
 		  Calendar cal = Calendar.getInstance();
 		  Date date = cal.getTime();
 		  userApp.setAppTime(date);
 		  
 		  long srvDuration = userApp.getSrvEndingTime().getTime() - userApp.getSrvStartingTime().getTime();
 		  
 		  if (srvDuration < 0)
 			  throw new EucalyptusServiceException("service time error");;
 		  
 		  try {
 			  userAppDBProc.addUserApp(userApp);
 		  }
 		  catch (UserAppSyncException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new EucalyptusServiceException("Failed to add user app");
 		  }
 	  }
 	  
 	  /**
 	   * Search keys.
 	   * 
 	   * @param session
 	   * @param search
 	   * @param range
 	   * @return
 	   * @throws EucalyptusServiceException
 	 * @throws UserAppSyncException 
 	   */
 	  public SearchResult lookupUserApp( LoginUserProfile curUser, String search, SearchRange range, EnumUserAppStatus state ) throws EucalyptusServiceException {
 		  boolean isRootAdmin = curUser.isSystemAdmin();
 		  
 		  ResultSetWrapper rs;
 		  try {
 			  if (isRootAdmin) {
 				  rs = userAppDBProc.queryUserApp(0, 0, state, range);
 			  }
 			  else {		  
 				  rs = userAppDBProc.queryUserApp(curUser.getAccountId(), curUser.getUserId(), state, range);
 			  }
 		  } catch (UserAppSyncException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				throw new EucalyptusServiceException("Fail to query user apps");
 			}
 		  if (rs == null)
 			  return null;
 		  
 		  return getSearchResult(isRootAdmin, rs, range);
 	  }
 	  
 	  public void deleteUserApps(ArrayList<String> ids ) throws EucalyptusServiceException {
 		  try {
 			  userAppDBProc.delUserApps(ids);
 		} catch (UserAppSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to delete user apps");
 		}
 	  }
 	  
 	  public void updateUserApp(UserApp userApp) throws EucalyptusServiceException {
 		  try {
 			  userAppDBProc.updateUserApp(userApp);
 		  } catch (UserAppSyncException e) {
 			  // TODO Auto-generated catch block
 			  e.printStackTrace();
 			  throw new EucalyptusServiceException("Failed to update user apps");
 		  }
 	  }
 	  /**
 	   * 1. get vm instance id
 	   * 2. call createApp, and sync resources
 	   * 
 	   * @param session
 	   * @param userAppId
 	   * @return UserApp with instance_id and cs, ms, ds id
 	   * @throws EucalyptusServiceException
 	   */
 	  public UserApp runVMInstance(Session session, int userAppId) throws EucalyptusServiceException {
 		  try {
 			  UserApp userApp = this.userAppDBProc.lookupUserApp(userAppId);
 			  			  
 			  int userId = userApp.getUserId();
 			  String keyPair = userApp.getKeyPair();
 			  String securityGroup = userApp.getSecurityGroup();
 			  
 			  String euca_vit_id = null;	  
 			  VmImageType vit = this.vitDBProc.lookupVIT(userApp.getVmIdImageTypeId());
 			  if (vit != null)
 				  euca_vit_id = vit.getEucaVITId();
 			  
 			  if (keyPair != null && securityGroup != null && euca_vit_id != null) {
				  TemplateInfo template = new TemplateInfo();
				  template.template_ncpus = userApp.getNcpus();
				  template.template_mem = userApp.getMem();
				  template.template_disk = userApp.getDisk();
				  template.template_bw = userApp.getBw();
				  
 				  String euca_intance_id = EucaServiceWrapper.getInstance().runVM(session, userId, template, keyPair, securityGroup, euca_vit_id);
 				  userApp.setEucaVMInstanceKey(euca_intance_id);
 				  
 				  if (!Strings.isNullOrEmpty(euca_intance_id)) {
 					  //obtain the server id where the resources are allocated
 					  int serverId = EucaServiceWrapper.getInstance().getServerID(session, userId, euca_intance_id);
 					  
 					  if (serverId < 0)
 						  throw new EucalyptusServiceException("Server id error!");
 				  
 					  //synch resources
 					  AppResources appRes = DeviceTemplateService.createApp(null, userApp.getNcpus(), userApp.getMem(), userApp.getDisk(), userApp.getBw(), serverId, userId);
 					  userApp.setCPUSrvId(appRes.cs_id);
 					  userApp.setMemSrvId(appRes.ms_id);
 					  userApp.setDiskSrvId(appRes.ds_id);
 					  userApp.setBwSrvId(appRes.bs_id_public);
 					  userApp.setPubIpSrvId(appRes.ip_id_public);
 					  userApp.setPriIpSrvId(appRes.ip_id_private);
 						  
 					  return userApp;
 				  }
 				  else
 					  throw new EucalyptusServiceException("Failed to get eucalyptus vm instance key");
 			  }
 			  else
 				  throw new EucalyptusServiceException("User's key_pair or security group para error");
 		  } catch (UserAppSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to query user app");
 		  } catch (VITSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to query eucalyptus vm image type id");
 		}
 	  }
 	  
 	  public ArrayList<UserAppStateCount> countUserApp(LoginUserProfile curUser) throws EucalyptusServiceException {
 		  try {
 			  if (curUser.isSystemAdmin())
 				  return userAppDBProc.countUserAppByState(0,0);
 			  else if (curUser.isAccountAdmin())
 				  return userAppDBProc.countUserAppByState(curUser.getAccountId(), 0);
 			  else
 				  return userAppDBProc.countUserAppByState(curUser.getAccountId(), curUser.getUserId());
 			  
 		} catch (UserAppSyncException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new EucalyptusServiceException("Failed to update user apps");
 		}
 	  }
 	  
 	  private SearchResult getSearchResult(boolean isRootAdmin, ResultSetWrapper rs, SearchRange range) {
 		  
 		  DATA = resultSet2List(rs);
 		  
 		  int resultLength = Math.min( range.getLength( ), DATA.size( ) - range.getStart( ) );
 		  SearchResult result = new SearchResult( DATA.size( ), range );
 		  result.setDescs( ViewSearchTableServerConfig.instance().getConfig(EnumService.USER_APP_SRV, LanguageSelection.instance().getCurLanguage())  );
 		  result.setRows( DATA.subList( range.getStart( ), range.getStart( ) + resultLength ) );
 			
 		  return result;
 	  }
 	  
 	  private List<SearchResultRow> resultSet2List(ResultSetWrapper rsWrapper) {
 		  ResultSet rs = rsWrapper.getResultSet();
 		  int index = 1;
 		  List<SearchResultRow> result = null;
 		  
 		  try {
 			  if (rs != null) {
 				  result = new ArrayList<SearchResultRow>();
 				  
 				  ArrayList<SearchTableCol> tableCols = ViewSearchTableServerConfig.instance().getConfig(EnumService.USER_APP_SRV);
 				  String[] dbFields = new String[tableCols.size()];
 				  
 				  for (int i=0; i<dbFields.length; i++)
 					  dbFields[i] = tableCols.get(i).getDbField();
 				  
 				  while (rs.next()) {
 					  ArrayList<String> rowValue = new ArrayList<String>();
 					  
 					  String bw=null, cpuCount=null, mem=null, disk=null;
 					  String os=null, ver=null;
 					  
 					  for (int i=0; i<dbFields.length; i++) {
 						  String value = null;
 						  if (!dbFields[i].equalsIgnoreCase("null")) {
 							  if (dbFields[i].equalsIgnoreCase("TEMPLATE_DETAILS")) {
 								  value = "...";
 							  }
 							  else if (dbFields[i].equalsIgnoreCase("VM_IMAGE_DETAILS")) {
 								  value = "...";
 							  }
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.USER_APP.STATUS))
 								  value = Enum2String.getInstance().getUserAppStateName(rs.getString(dbFields[i]));
 							  else
 								  value = rs.getString(dbFields[i]);
 							  
 							  if (dbFields[i].equalsIgnoreCase(DBTableColName.USER_APP.NCPUS))
 								  cpuCount = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.USER_APP.MEM))
 								  mem = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.USER_APP.DISK))
 								  disk = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.USER_APP.BW))
 								  bw = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.VM_IMAGE_TYPE.OS))
 								  os = value;
 							  else if (dbFields[i].equalsIgnoreCase(DBTableColName.VM_IMAGE_TYPE.VER))
 								  ver = value;
 						  }
 						  else
 							  value = Integer.toString(index++);
 					  			 
 						  rowValue.add(value);
 					  }
 					  
 					  String template_details = formatTemplateInfo(cpuCount, mem, disk, bw);
 					  String vm_details = formatVMImageTypeInfo(os, ver);
 
 					  List<String> links = Arrays.asList(null, null, null, null, template_details, vm_details,	null, null, null, null, null, null);
 					  
 					  SearchResultRow row = new SearchResultRow(rowValue, links);
 					  
 					  result.add(row);
 				  }
 			  }
 			rsWrapper.close();			
 		  } catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 		  }
 		  
 		  return result;
 	  }
 	  
 	  private String formatTemplateInfo(String cpuCount, String mem, String disk, String bw) {
 		  StringBuilder str = new StringBuilder();
 		  
 		  str.append("模板详细信息：").append("\n");
 		  str.append("    CPU数量： ").append(cpuCount).append("\n");
 		  str.append("    内存： ").append(mem).append("GB").append("\n");
 		  str.append("    磁盘： ").append(disk).append("GB").append("\n");
 		  str.append("    带宽： ").append(bw).append("MB/s");
 		  
 		  return str.toString();
 	  }
 	  
 	  private String formatVMImageTypeInfo(String os, String version) {
 		  StringBuilder str = new StringBuilder();
 		  
 		  str.append("镜像详细信息：").append("\n");
 		  str.append("    操作系统： ").append(os).append("\n");
 		  str.append("    版本： ").append(version);
 		  
 		  return str.toString();
 	  }
 	  
 	  private SorterProxy sorterProxy = new SorterProxy(EnumService.USER_APP_SRV);
 	  private UserAppDBProcWrapper userAppDBProc = new UserAppDBProcWrapper(this.sorterProxy);
 	  
 	  private VITDBProcWrapper vitDBProc = new VITDBProcWrapper();
 	  
 	  private static List<SearchResultRow> DATA = null;
 }
