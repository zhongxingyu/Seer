 
 package edu.wustl.common.query.pvmanager.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 //import java.util.Map;
 
 import edu.wustl.common.dao.DAOFactory;
 import edu.wustl.common.dao.JDBCDAO;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.query.util.global.Constants;
 
 public final class MedLookUpManager
 {
 
 	//private Map<String, List<String>> pvMap = null;
 	private static MedLookUpManager mLookUpMgrObj = null;
 
 	//private String lookUpTable=null;
 
 	private MedLookUpManager()
 	{
 
 	}
 
 	public static MedLookUpManager instance()
 	{

		if (mLookUpMgrObj == null)
 		{
			synchronized (mLookUpMgrObj)
 			{
 				mLookUpMgrObj = new MedLookUpManager();
 				//medLookUpManager.lookUpTable = Variables.properties.getProperty("med.lookup.table");
 				//medLookUpManager.init();
 			}
 		}
 
 		return mLookUpMgrObj;
 
 	}
 
 	/*private void init()
 	{
 		JDBCDAO dao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 		try
 		{
 			dao.openSession(null);
 			List<List<String>> dataList = dao.executeQuery("select synonym,id from MED_LOOKUP_TABLE", null, false, false, null);
 			if(!dataList.isEmpty())
 			{
 				pvMap = new HashMap<String, List<String>>();
 				String pvFilter;
 				for(int i=0; i < dataList.size(); i++)
 				{
 					pvFilter = dataList.get(i).get(0).substring(0,dataList.get(i).get(0).indexOf("^") + 1) + "%";
 					if(pvMap.containsKey(pvFilter))
 					{
 						pvMap.get(pvFilter).add(dataList.get(i).get(1));
 					}
 					else
 					{
 						List<String> pvList = new ArrayList<String>();
 						pvList.add(dataList.get(i).get(1));
 						pvMap.put(pvFilter,pvList);
 					}
 				}
 			}
 		}
 		catch (DAOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ClassNotFoundException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}*/
 
 	/*public List<String> getPermissibleValues(String pvFilter)
 	{
 		if(pvMap != null)
 			return pvMap.get(pvFilter);
 		else
 			return null;
 	}*/
 	//change signature to accept pv_filter and pv_view.
 	//if pv_filter = null then dont go in for like condition.
 	public List<String> getPermissibleValues(List<String> pvFilter, String pvView)
 			throws PVManagerException
 	{
 		List<String> pvList = null;
 		JDBCDAO dao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 		String query = "";
 		try
 		{
 			dao.openSession(null);
 			//if a pvFilter value is give i.e a synonym is given 
 			if (pvFilter != null && (!pvFilter.equals("")))
 			{
 				query = getQueryWithSynonym(pvFilter, pvView);
 			}
 			else
 			{
 				query = getQueryDirectFromView(pvView);
 			}
 			List<List<String>> dataList = dao.executeQuery(query, null, false, false, null);
 			if (!dataList.isEmpty())
 			{
 				pvList = new ArrayList<String>();
 
 				for (int i = 0; i < dataList.size(); i++)
 				{
 					pvList.add(dataList.get(i).get(1));
 				}
 			}
 		}
 		catch (DAOException e)
 		{
 
			throw new PVManagerException(ErrMsg, e);
 
 		}
 		catch (ClassNotFoundException e)
 		{
			throw new PVManagerException(ErrMsg, e);
 		}
 		finally
 		{
 			try
 			{
 				dao.closeSession();
 			}
 			catch (DAOException e)
 			{
				throw new PVManagerException(ErrMsg, e);
 			}
 		}
 
 		return pvList;
 
 	}
 
 	public int getPermissibleValuesCount(List<String> pvFilterList, String pvView)
 			throws PVManagerException
 	{
 		JDBCDAO dao = (JDBCDAO) DAOFactory.getInstance().getDAO(Constants.JDBC_DAO);
 		StringBuffer countQuery = new StringBuffer();
 		int count = 0;
 		try
 		{
 			dao.openSession(null);
 			//if a pvFilter value is give i.e a synonym is given 
 			if (pvFilterList != null && (!pvFilterList.isEmpty()))
 			{
 				//countQuery="select count(*) from "+pvView+" where ";
 				countQuery.append("select count(*) from " + pvView + " where ");
 				//+pvFilter+"'";
 				int indx = 0;
 				for (; indx < pvFilterList.size() - 1; indx++)
 				{
 					countQuery.append("synonym like '");
 					countQuery.append(pvFilterList.get(indx));
 					countQuery.append("' or ");
 				}
 				countQuery.append("synonym like '");
 				countQuery.append(pvFilterList.get(indx));
 				countQuery.append("' ");
 
 			}
 			else
 			{
 				countQuery.append("select count(*) from ");
 				countQuery.append(pvView);
 			}
 			List<List<String>> dataListCount = dao.executeQuery(countQuery.toString(), null, false,
 					false, null);
 			if (!dataListCount.isEmpty())
 			{
 				count = Integer.parseInt(dataListCount.get(0).get(0));
 			}
 		}
 		catch (DAOException e)
 		{
 
			throw new PVManagerException(ErrMsg, e);
 
 		}
 		catch (ClassNotFoundException e)
 		{
			throw new PVManagerException(ErrMsg, e);
 		}
 		finally
 		{
 			try
 			{
 				dao.closeSession();
 			}
 			catch (DAOException e)
 			{
				throw new PVManagerException(ErrMsg, e);
 			}
 		}
 
 		return count;
 
 	}
 
 	//accept the name of the view as well
 	//rename method.
 	private String getQueryWithSynonym(List<String> pvFilterList, String pvView)
 	{
 
 		StringBuffer query = new StringBuffer(100);
 		query.append("select synonym,id from ");
 		query.append(pvView);
 		query.append(Constants.WHERE);
 		int indx = 0;
 		for (; indx < pvFilterList.size() - 1; indx++)
 		{
 			query.append("synonym like '");
 			query.append(pvFilterList.get(indx));
 			query.append("' or ");
 		}
 		query.append("synonym like '");
 		query.append(pvFilterList.get(indx));
 		query.append("' ");
 
 		return query.toString();
 	}
 
 	//get pv from view directly without like synonym condition.
 	private String getQueryDirectFromView(String pvView)
 	{
 		StringBuffer query = new StringBuffer("select name,id from ");
 		query.append(pvView);
 		return query.toString();
 	}

 	private static final String ErrMsg = "Error occured while fetching the permissible concept codes from MED.";
 }
