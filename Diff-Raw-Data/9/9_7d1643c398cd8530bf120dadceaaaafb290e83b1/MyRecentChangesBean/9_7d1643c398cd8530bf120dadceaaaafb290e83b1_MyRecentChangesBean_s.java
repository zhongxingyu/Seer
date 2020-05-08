 package org.sakaiproject.sdata.services.mra;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.announcement.api.AnnouncementMessage;
 import org.sakaiproject.announcement.api.AnnouncementService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.db.api.SqlReader;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.message.api.MessageChannel;
 import org.sakaiproject.sdata.tool.api.ServiceDefinition;
 import org.sakaiproject.search.api.SearchList;
 import org.sakaiproject.search.api.SearchResult;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.SiteService.SelectionType;
 import org.sakaiproject.site.api.SiteService.SortType;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 
 /**
  * A service definition bean for recent changes that the current user can see.
  * 
  * @author
  */
 public class MyRecentChangesBean implements ServiceDefinition
 {
 
 	private static final Log log = LogFactory.getLog(MyRecentChangesBean.class);
 
 	private List<Site> mySites;
 
 	private ArrayList<String> arlSiteId = new ArrayList<String>();
 
 	private SearchService searchService;
 
 	private ContentHostingService contentHostingService;
 
 	private AnnouncementService announcementService;
 
 	private SiteService siteService;
 
 	private SqlService sqlService;
 
 	Map<String, Object> resultMap = new HashMap<String, Object>();
 
 	private SessionManager sessionManager;
 
 	/**
 	 * Create a recent changes bean with the number of pages.
 	 * 
 	 * @param paging
 	 */
 	public MyRecentChangesBean(int paging)
 	{
 		this.announcementService = Kernel.announcementService();
 		this.contentHostingService = Kernel.contentHostingService();
 		this.searchService = Kernel.searchService();
 		this.sqlService = Kernel.sqlService();
 		this.siteService = Kernel.siteService();
 		this.sessionManager = Kernel.sessionManager();
 
 		try
 		{
 			search(paging);
 		}
 		catch (PermissionException e)
 		{
 			log.warn(e.getMessage());
 		}
 		catch (IdUnusedException e)
 		{
 			log.warn(e.getMessage());
 		}
 		catch (TypeException e)
 		{
 			log.warn(e.getMessage());
 		}
 		catch (ParseException e)
 		{
 			log.warn(e.getMessage());
 		}
 
 	} // END CONSTRUCTOR
 
 	/**
 	 * Perform a search for recent changes using the search engine
 	 * 
 	 * @param paging the number of results to load
 	 * @throws PermissionException
 	 * @throws IdUnusedException
 	 * @throws TypeException
 	 * @throws ParseException
 	 */
 	private void search(int paging) throws PermissionException, IdUnusedException,
 			TypeException, ParseException
 	{
 		try
 		{
 
 			Date lastLogin = null;
 			List<?> lstLastLogin;
 			SearchResult searchResult = null;
 
 			List<Map<String, String>> myRecentResults = new ArrayList<Map<String, String>>();
 
 			List<MyRecentChangesResult> results = new ArrayList<MyRecentChangesResult>();
 
 			try
 			{
 
 				Session currentSession = sessionManager.getCurrentSession();
 				String currentUser = currentSession.getUserId();
 
 				lstLastLogin = sqlService.dbRead(
 						"select userdate from sdata_lastlogin where userid= ? ",
 						new Object[] { currentUser }, new SqlReader()
 						{
 
 							public Object readSqlResultRecord(ResultSet result)
 							{
 								try
 								{
 									return result.getString(1);
 								}
 								catch (SQLException e)
 								{
 									return null;
 								}
 							}
 
 						});
 
 				if (lstLastLogin.size() != 0)
 				{
 					String ll = (String) lstLastLogin.get(0);
 					lastLogin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ll
 							.substring(0, 19));
 
 				}
 
 				mySites = siteService.getSites(SelectionType.ACCESS, null, null, null,
 						SortType.TITLE_ASC, null);
 
 				try
 				{
 					mySites.add(0, (siteService.getSite(siteService
 							.getUserSiteId(currentSession.getUserId()))));
 
 				}
 				catch (IdUnusedException e)
 				{
 					log.debug("Failed to find site ",e);
 				}
 				for (int i = 0; i < mySites.size(); i++)
 				{
 
 					arlSiteId.add(mySites.get(i).getId());
 				}
 
 				arlSiteId.add(siteService.getUserSiteId(currentSession.getUserId())
 						.substring(1));
 
 			}
 			catch (Exception e)
 			{
 				log.warn("Failed in My RecentChanges ",e);
 			}
 
 			// ////////
 			// //////// DELETE INDEXED FILES FROM INDEX QUEUE
 			// ////////
 			SearchList searchList = null;
 			if (searchService != null)
 			{
 				searchList = searchService.search("tool:content tool:announcement",
 						arlSiteId, 0, 50, null, "dateRelevanceSort");
 
 				int ii = -1;
 				do
 				{
 					ii += 1;
 					if (ii < searchList.size())
 					{
 						if (searchList.get(ii) == null)
 						{
 							break;
 						}
 						else
 						{
 							searchResult = ((SearchResult) searchList.get(ii));
 							if (searchResult.getId() != null
 									&& !searchResult.getId().equals(""))
 							{
 
 								for (int r = 0; r < searchResult.getFieldNames().length; r++)
 								{
 									String s = "";
 									if (searchResult.getFieldNames()[r]
 											.equals("indexdate"))
 									{
 										for (int y = 0; y < searchResult
 												.getValues(searchResult.getFieldNames()[r]).length; y++)
 										{
 											s += searchResult.getValues(searchResult
 													.getFieldNames()[r])[y];
 										}
 										long l = Long.parseLong(s);
 										s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 												.format(new java.util.Date(l));
 										sqlService
 												.dbWrite(
 														"delete from sdata_indexqueue where version<  ? ",
 														new Object[] { s });
 									}
 								}
 
 								break;
 							}
 						}
 					}
 					else
 					{
 
 						break;
 
 					}
 
 				}
 
 				while ((searchResult.getId() == null || searchResult.getId().equals(""))
 						&& ii < 50);
 			}
 
 			// ////////
 			// //////// GET INDEXED RESULTS FROM INDEXQUEUE
 			// ////////
 
 			int totalrecordsshown = 0;
 			String sites = "";
 			for (int i = 0; i < arlSiteId.size(); i++)
 			{
 				if (i == 0)
 				{
 					sites += "context= ? ";
 				}
 				else
 				{
 					sites += "OR context= ? ";
 				}
 			}
 			Object[] params = arlSiteId.toArray();
 
 			List<?> lst = sqlService.dbRead("select * from sdata_indexqueue where "
 					+ sites + " order by version desc", params,
 					new MyRecentChangesSqlreader());
 
 			ArrayList<String> arlUsed = new ArrayList<String>();
 
 			// BEGIN NEW STUFF
 
 			for (int i = 0; i < lst.size(); i++)
 			{
 
 				MyRecentChangesSqlresult mres = (MyRecentChangesSqlresult) lst.get(i);
 
 				if (mres.getTool().equals("content"))
 				{
 					try
 					{
 
 						String eid = mres.getName().substring(8);
 
 						if (!contentHostingService.isCollection(eid))
 						{
 
 							if (contentHostingService.allowGetResource(eid)
 									&& !arlUsed.contains(mres.getName()))
 							{
 
 								if (totalrecordsshown >= (paging - 1) * 5
 										&& totalrecordsshown < (paging) * 5)
 								{
 
 									ContentResource cres = contentHostingService
 											.getResource(eid);
 
 									MyRecentChangesResult mrcs = new MyRecentChangesResult();
 
 									for (Site s : mySites)
 									{
 										if (mres.getContext().equals(s.getId()))
 										{
 
 											mrcs.setSitename(s.getTitle());
 
 										}
 									}
 
 									mrcs.setTool(mres.getTool());
 									mrcs.setVersion(mres.getVersion());
 									mrcs.setContext(mres.getContext());
									mrcs.setName(cres.getUrl().substring(
											cres.getUrl().lastIndexOf("/") + 1));
 									mrcs.setReference(cres.getReference());
 									results.add(mrcs);
 
 								}
 
 								totalrecordsshown += 1;
 								arlUsed.add(mres.getName());
 								if (totalrecordsshown == 50)
 								{
 									break;
 								}
 
 							}
 						}
 					}
 					catch (Exception ex)
 					{
 					};
 				}
 				else if (mres.getTool().equals("announcement")
 						&& announcementService != null)
 				{
 
 					try
 					{
 
 						if (!arlUsed.contains(mres.getName()))
 						{
 
 							MessageChannel messageChannel = announcementService
 									.getChannel("/announcement/channel/"
 											+ mres.getContext() + "/main");
 							AnnouncementMessage announcementMessage = (AnnouncementMessage) messageChannel
 									.getMessage(mres.getName().substring(
 											mres.getName().lastIndexOf('/') + 1));
 
 							if (!announcementMessage.getAnnouncementHeader().getDraft())
 							{
 
 								if (totalrecordsshown >= (paging - 1) * 5
 										&& totalrecordsshown < (paging) * 5)
 								{
 
 									MyRecentChangesResult mrcs = new MyRecentChangesResult();
 
 									for (Site s : mySites)
 									{
 										if (mres.getContext().equals(s.getId()))
 										{
 
 											mrcs.setSitename(s.getTitle());
 
 										}
 									}
 
 									mrcs.setTool(mres.getTool());
 									mrcs.setVersion(mres.getVersion());
 									mrcs.setContext(mres.getContext());
 									mrcs.setName(announcementMessage
 											.getAnnouncementHeader().getSubject());
 									mrcs.setReference(announcementMessage.getReference());
 									mrcs.setCleanVersion(mres.getVersion());
 									results.add(mrcs);
 
 								}
 
 								totalrecordsshown += 1;
 								arlUsed.add(mres.getName());
 								if (totalrecordsshown == 50)
 								{
 									break;
 								}
 
 							}
 
 						}
 
 					}
 					catch (Exception ex)
 					{
 					};
 
 				}
 
 			}
 
 			// ////////
 			// //////// FILL WITH INDEXED RESULTS
 			// ////////
 
 			if (totalrecordsshown < 50 && searchList != null)
 			{
 
 				int ii = -1;
 				do
 				{
 					ii += 1;
 
 					if (ii < searchList.size())
 					{
 						if (searchList.get(ii) == null)
 						{
 							break;
 						}
 						else
 						{
 							SearchResult srl = (SearchResult) searchList.get(ii);
 
 							if (srl.getId() != null && !srl.getId().equals("")
 									&& !arlUsed.contains(srl.getReference()))
 							{
 								// log.error("+++++ " + srl.getTool());
 								if (srl.getTool().equals("content"))
 								{
 
 									if (totalrecordsshown >= (paging - 1) * 5
 											&& totalrecordsshown < (paging) * 5)
 									{
 
 										long l = 0;
 										String s = "";
 										String s2 = "";
 										for (int r = 0; r < srl.getFieldNames().length; r++)
 										{
 											if (srl.getFieldNames()[r].equals("siteid"))
 											{
 												for (int y = 0; y < srl.getValues(srl
 														.getFieldNames()[r]).length; y++)
 												{
 													s += srl.getValues(srl
 															.getFieldNames()[r])[y];
 												}
 											}
 											else if (srl.getFieldNames()[r]
 													.equals("indexdate"))
 											{
 												for (int y = 0; y < srl.getValues(srl
 														.getFieldNames()[r]).length; y++)
 												{
 													s2 += srl.getValues(srl
 															.getFieldNames()[r])[y];
 												}
 												l = Long.parseLong(s2);
 												s2 = new SimpleDateFormat(
 														"yyyy-MM-dd HH:mm:ss")
 														.format(new java.util.Date(l));
 											}
 										}
 
 										MyRecentChangesResult mrcs = new MyRecentChangesResult();
 
 										mrcs.setName(srl.getTitle());
 
 										for (Site ss : mySites)
 										{
 
 											if (s.equals(ss.getId()))
 											{
 
 												mrcs.setSitename(ss.getTitle());
 												mrcs.setContext(ss.getId());
 											}
 										}
 
 										mrcs.setTool(srl.getTool());
 										mrcs.setVersion(s2);
 										mrcs.setReference(srl.getReference());
 										results.add(mrcs);
 
 									}
 
 									totalrecordsshown += 1;
 									arlUsed.add(srl.getReference());
 
 								}
 								else if (srl.getTool().equals("announcement"))
 								{
 
 									if (totalrecordsshown >= (paging - 1) * 5
 											&& totalrecordsshown < (paging) * 5)
 									{
 
 										long l = 0;
 										String s = "";
 										String s2 = "";
 										for (int r = 0; r < srl.getFieldNames().length; r++)
 										{
 											if (srl.getFieldNames()[r].equals("siteid"))
 											{
 												for (int y = 0; y < srl.getValues(srl
 														.getFieldNames()[r]).length; y++)
 												{
 													s += srl.getValues(srl
 															.getFieldNames()[r])[y];
 												}
 											}
 											else if (srl.getFieldNames()[r]
 													.equals("indexdate"))
 											{
 												for (int y = 0; y < srl.getValues(srl
 														.getFieldNames()[r]).length; y++)
 												{
 													s2 += srl.getValues(srl
 															.getFieldNames()[r])[y];
 												}
 												l = Long.parseLong(s2);
 												s2 = new SimpleDateFormat(
 														"yyyy-MM-dd HH:mm:ss")
 														.format(new java.util.Date(l));
 											}
 										}
 
 										String[] arr = srl.getTitle().substring(9).split(
 												" From ");
 										String arrResult = "";
 										for (int i = 0; i < arr.length - 1; i++)
 										{
 											arrResult += arr[i];
 										}
 
 										MyRecentChangesResult mrcs = new MyRecentChangesResult();
 										mrcs.setContext(s);
 										mrcs.setName(arrResult);
 
 										for (Site ss : mySites)
 										{
 											if (mrcs.getContext().equals(ss.getId()))
 											{
 
 												mrcs.setSitename(ss.getTitle());
 
 											}
 										}
 
 										mrcs.setTool(srl.getTool());
 										mrcs.setVersion(s2);
 										mrcs.setReference(srl.getReference());
 										results.add(mrcs);
 
 									}
 
 									totalrecordsshown += 1;
 									arlUsed.add(searchResult.getReference());
 
 								}
 
 							}
 						}
 					}
 					else
 					{
 
 						break;
 
 					}
 				}
 				while (totalrecordsshown < 50 && ii < 50);
 
 			}
 
 			// END NEW STUFF
 			for (MyRecentChangesResult mrcsr : results)
 			{
 
 				Map<String, String> mrcsr_map = new HashMap<String, String>();
 
 				// log.error("the site name: " + mrcsr.getSitename());
 				if (mrcsr.getSitename().equals("My Workspace"))
 				{
 					mrcsr_map.put("siteName", "Personal Tools");
 					// log.error("a my workspace file");
 
 				}
 				else
 				{
 					mrcsr_map.put("siteName", mrcsr.getSitename());
 				}
 				// mrcsr_map.put("siteName", mrcsr.getSitename());
 				mrcsr_map.put("context", mrcsr.getContext());
 				mrcsr_map.put("name", mrcsr.getName());
 				mrcsr_map.put("tool", mrcsr.getTool());
 				mrcsr_map.put("version", mrcsr.getVersion());
 				mrcsr_map.put("reference", mrcsr.getReference());
 
 				// today in format brengen om te compairen
 				mrcsr.setCleanVersion(mrcsr.getVersion().substring(0,
 						mrcsr.getVersion().lastIndexOf('-') + 3));
 
 				if (mrcsr.getVersion().substring(0,
 						mrcsr.getVersion().lastIndexOf('-') + 3).equals(
 						new SimpleDateFormat("yyyy-MM-dd").format(new Date())))
 				{
 
 					mrcsr.setCleanVersion("Today");
 
 				}
 				else
 				{
 					String test = mrcsr.getVersion();
 					Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(test);
 					mrcsr.setCleanVersion(new SimpleDateFormat("dd MMM yy HH:mm")
 							.format(d));
 
 				}
 
 				mrcsr_map.put("cleanVersion", mrcsr.getCleanVersion());
 				myRecentResults.add(mrcsr_map);
 
 			}
 
 			if (lastLogin == null)
 			{
 				resultMap.put("lastLogin", "NO USER LOGGED IN");
 
 			}
 			else
 			{
 
 				resultMap.put("lastLogin", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 						.format(lastLogin));
 			}
 
 			resultMap.put("total", totalrecordsshown);
 
 			resultMap.put("items", myRecentResults);
 
 			// JSONArray jsonArray = JSONArray.fromObject(lst);
 
 			// return jsonArray.toString();
 
 		}
 		catch (Exception e)
 		{
 			Map<String, String> mrcsr_map2 = new HashMap<String, String>();
 
 			resultMap.put("items", mrcsr_map2);
 			resultMap.put("total", 0);
 			resultMap.put("lastLogin", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
 					.format(new Date()));
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
 	 */
 	public Map<String, Object> getResponseMap()
 	{
 		return resultMap;
 	}
 
 }
