 package com.wovenware.aee.breakdown.reporting.bean;
 
 /**
  * <i>Main Bean.</i>
  * 
  * Wovenware, Inc 2013
  * Created on June 06, 2013
  * @author Alberto Aresti, Nelson Perez
  */
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.model.SelectItem;
 
 import com.wovenware.aee.breakdown.reporting.Constants;
 import com.wovenware.aee.breakdown.reporting.db.dao.Bk2UsersDAO;
 import com.wovenware.aee.breakdown.reporting.db.dao.BksReportedDAO;
 import com.wovenware.aee.breakdown.reporting.db.dao.CityAreasDAO;
 import com.wovenware.aee.breakdown.reporting.db.dao.UserAreasDAO;
 import com.wovenware.aee.breakdown.reporting.db.dao.to.Bk2UsersTO;
 import com.wovenware.aee.breakdown.reporting.db.dao.to.CityAreasTO;
 import com.wovenware.aee.breakdown.reporting.db.dao.to.UserAreasTO;
 import com.wovenware.aee.breakdown.reporting.util.ConnectionUtil;
 import com.wovenware.aee.breakdown.reporting.util.FeedbackUtil;
 
 @ManagedBean
 @SessionScoped
 public class Main extends GenericBean {
 	private static final long serialVersionUID = 1L;
 	
 	private String _relevantBreakdownsCount = null;
 	private String _averageBreakdownDuration = null;
 	private String _totalBreakdownsCount = null;
 	private String _userAreas = null;
 	private String _areaToUpdate = null;
 	private String _newName = null;
 	private String _areaToDelete = null;
 	private String _city = null;
 	private String _area = null;
 	private String _name = null;
 	
 	private List<SelectItem> _cityList = null;
 	private List<SelectItem> _areaList = null;
 	
 	private boolean _emptyUserAreas = true;
 	
 	// Relevant Breakdowns Count
 	public String getRelevantBreakdownsCount() {
 		findRelevantBreakdownsCount(false);
 		
 		return _relevantBreakdownsCount;
 	}
 	
 	// Average Breakdown Duration
 	public String getAverageBreakdownDuration() {
 		findAverageBreakdownDuration(false);
 		
 		return _averageBreakdownDuration;
 	}
 	
 	// Total Breakdowns Count
 	public String getTotalBreakdownsCount() {
 		findTotalBreakdownsCount(false);
 		
 		return _totalBreakdownsCount;
 	}
 	
 	// User Areas
 	public String getUserAreas() {
 		loadUserAreas(false);
 		
 		return _userAreas;
 	}
 	
 	// Area to Update
 	public String getAreaToUpdate() {
 		return _areaToUpdate;
 	}
 	
 	public void setAreaToUpdate(String areaToUpdate) {
 		_areaToUpdate = areaToUpdate;
 	}
 	
 	// New Name
 	public String getNewName() {
 		return _newName;
 	}
 	
 	public void setNewName(String newName) {
 		_newName = newName;
 	}
 	
 	// Area to Delete
 	public String getAreaToDelete() {
 		return _areaToDelete;
 	}
 	
 	public void setAreaToDelete(String areaToDelete) {
 		_areaToDelete = areaToDelete;
 	}
 	
 	// City
 	public String getCity() {
 		return _city;
 	}
 	
 	public void setCity(String city) {
 		_city = city;
 	}
 	
 	public List<SelectItem> getCityList() {
 		loadCityList(false);
 		
 		return _cityList;
 	}
 	
 	// Area
 	public String getArea() {
 		return _area;
 	}
 	
 	public void setArea(String area) {
 		_area = area;
 	}
 	
 	public List<SelectItem> getAreaList() {
 		loadAreaList(false);
 		
 		return _areaList;
 	}
 	
 	// Name
 	public String getName() {
 		return _name;
 	}
 	
 	public void setName(String name) {
 		_name = name;
 	}
 	
 	private void updateDashboard() {
 		findRelevantBreakdownsCount(true);
 		findAverageBreakdownDuration(true);
 		findTotalBreakdownsCount(true);
 	}
 	
 	private void findRelevantBreakdownsCount(boolean force) {
     	if(_relevantBreakdownsCount == null || force) {
 			_relevantBreakdownsCount = "0";
 	    	
 	    	Connection connection = null;
 	    	
 	    	try {
 	    		connection = ConnectionUtil.createConnection(
 	    				Constants.Services.JNDI_JDBC_APP, false);
 		    		
 	    		UserAreasDAO userAreasDAO = new UserAreasDAO(connection);
 	    		
 	    		_relevantBreakdownsCount = String.valueOf(
 	    				userAreasDAO.countBreakdowns(_userEmail));
 		    		
 		    	connection.commit();
 	    	} catch(Exception e) {
 	    		try {
 	    			if(connection != null && !connection.isClosed()) {
 	    				connection.rollback();
 	    			}
 	    		} catch (Exception e1) {
 	    			// Do nothing...
 	    		}
 	    	} finally {
 	    		try {
 	    			if(connection != null && !connection.isClosed()) {
 	    				connection.close();
 	    			}
 	    		} catch (Exception e) {
 	    			// Do nothing...
 	    		} finally {
 	    			connection = null;
 	    		}
 	    	}
     	}
     }
 
 	private void findAverageBreakdownDuration(boolean force) {
     	if(_averageBreakdownDuration == null || force) {
     		_averageBreakdownDuration = "0";
 	    	
 	    	Connection connection = null;
 	    	
 	    	try {
 	    		connection = ConnectionUtil.createConnection(
 	    				Constants.Services.JNDI_JDBC_APP, false);
 		    		
 	    		UserAreasDAO userAreasDAO = new UserAreasDAO(connection);
 	    		
 	    		List<UserAreasTO> results = userAreasDAO.findBreakdowns(_userEmail);
 	    		
 	    		long hours = 0;
 	    		
 	    		for(UserAreasTO userAreasTO : results) {
 	    			long difference = userAreasTO.getCloseTs().getTime() - userAreasTO.getOpenTs().getTime();
 	    			
 	    			hours += (difference / 3600000);
 	    		}
 	    		
 	    		if(results.size() > 0) {
 	    			hours = hours / results.size();
 	    		}
 	    		
 	    		_averageBreakdownDuration = String.valueOf(hours);
 		    		
 		    	connection.commit();
 	    	} catch(Exception e) {
 	    		try {
 	    			if(connection != null && !connection.isClosed()) {
 	    				connection.rollback();
 	    			}
 	    		} catch (Exception e1) {
 	    			// Do nothing...
 	    		}
 	    	} finally {
 	    		try {
 	    			if(connection != null && !connection.isClosed()) {
 	    				connection.close();
 	    			}
 	    		} catch (Exception e) {
 	    			// Do nothing...
 	    		} finally {
 	    			connection = null;
 	    		}
 	    	}
     	}
     }
 	
 	private void findTotalBreakdownsCount(boolean force) {
     	if(_totalBreakdownsCount == null || force) {
     		_totalBreakdownsCount = "0";
 	    	
 	    	Connection connection = null;
 	    	
 	    	try {
 	    		connection = ConnectionUtil.createConnection(
 	    				Constants.Services.JNDI_JDBC_APP, false);
 		    		
 	    		BksReportedDAO bksReportedDAO = new BksReportedDAO(connection);
 	    		
 	    		_totalBreakdownsCount = String.valueOf(
 	    				bksReportedDAO.count());
 		    		
 		    	connection.commit();
 	    	} catch(Exception e) {
 	    		try {
 	    			if(connection != null && !connection.isClosed()) {
 	    				connection.rollback();
 	    			}
 	    		} catch (Exception e1) {
 	    			// Do nothing...
 	    		}
 	    	} finally {
 	    		try {
 	    			if(connection != null && !connection.isClosed()) {
 	    				connection.close();
 	    			}
 	    		} catch (Exception e) {
 	    			// Do nothing...
 	    		} finally {
 	    			connection = null;
 	    		}
 	    	}
     	}
     }
 	
 	private void loadUserAreas(boolean force) {
 		if(_userAreas == null || _emptyUserAreas || force) {
 			StringBuilder userAreas = new StringBuilder();
 			
 			List<UserAreasTO> results = findUserAreas(_userEmail);
 			
 			int i = 0;
 			
 			if(results != null) {
 				for(UserAreasTO userAreasTO : results) {
 					i++;
 					
 					boolean hasBreakdown = userAreasTO.getStatus() != null;
 					
 					userAreas.append("<div class=\"row-fluid item\">");
 					userAreas.append("<div class=\"span11\">");
 					userAreas.append("<h4 id=\"title" + i + "\">");
 					
 					if(hasBreakdown) {
 						userAreas.append("<i class=\"icon-bell text-error\"></i>");
 					} else {
 						userAreas.append("<i class=\"icon-check text-info\"></i>");
 					}
 					
 					userAreas.append("&nbsp;");
 					userAreas.append("<a id=\"update" + i + "\"" +
 							" href=\"javascript:;\"" +
 							" data-toggle=\"tooltip\"" +
 							" title=\"Actualizar\" " +
 							" onmouseover=\"$('#update" + i + "').tooltip('show');\"" +
 							" onclick=\"editArea('" + i + "','" + userAreasTO.getName() + "');\">");
 					userAreas.append(userAreasTO.getName());
 					userAreas.append("</a>");
 					
 					if(!userAreasTO.getName().equals(userAreasTO.getArea() + ", " + userAreasTO.getCity())) {
 						userAreas.append("&nbsp;<small>");
 						userAreas.append("(" + userAreasTO.getArea() + ",&nbsp;" + userAreasTO.getCity() + ")");
 						userAreas.append("</small>");
 					}
 					
 					userAreas.append("</h4>");
 					userAreas.append("<div id=\"updateForm" + i + "\" class=\"update-form\" style=\"display: none;\">");
 					userAreas.append("<input id=\"updateOriginalName" + i + "\" type=\"hidden\" />");
 					userAreas.append("<input id=\"updateName" + i + "\" type=\"text\" />");
 					userAreas.append("&nbsp;");
 					userAreas.append("<a href=\"javascript:;\" class=\"btn btn-primary btn-small\" onclick=\"executeUpdate('" + i + "');\">Actualizar</a>");
 					userAreas.append("&nbsp;");
 					userAreas.append("<a href=\"javascript:;\" class=\"btn btn-small\" onclick=\"cancelUpdate('" + i + "');\">Cancelar</a></div>");
 					userAreas.append("<div class=\"status\">");
 					
 					if(hasBreakdown) {
 						userAreas.append("<span class=\"label label-important\">");
 						userAreas.append(userAreasTO.getStatus());
 						userAreas.append("</span>");
 						userAreas.append("&nbsp;-&nbsp;");
 						userAreas.append(userAreasTO.getRptdLastUpdateTs());
 					} else {
 						userAreas.append("<span class=\"label label-info\">");
 						userAreas.append("No hay aver&iacute;as reportadas para esta &aacute;rea");
 						userAreas.append("</span>");
 					}
 					
 					userAreas.append("</div></div>");
 					userAreas.append("<div class=\"span1 remove\">");
 					userAreas.append("<a id=\"delete" + i + "\"" +
 							" href=\"javascript:;\"" +
 							" class=\"close\" " +
 							" data-toggle=\"tooltip\"" +
 							" title=\"Borrar\" " +
 							" onmouseover=\"$('#delete" + i + "').tooltip('show');\"" +
 							" onclick=\"removeArea('" + userAreasTO.getName() + "');\">");
 					userAreas.append("<i class=\"icon-remove-circle\"></i>");
 					userAreas.append("</a></div></div>");
 				}
 			}
 			
 			if(i == 0) {
 				_emptyUserAreas = true;
 				
 				userAreas.append(FeedbackUtil.formatGeneralFeedback(
 						Constants.AlertTypes.WARNING,
 						"¡Atenci&oacute;n!",
 						"Usted no tiene ning&uacute;n &aacute;rea relevante configurada en este momento. Por favor utilize la forma de la derecha para a&ntilde;adir las &aacute;reas relevantes para usted.",
 						false));
 			} else {
 				_emptyUserAreas = false;
 			}
 			
 			_userAreas = userAreas.toString();
 		}
 	}
  
     private List<UserAreasTO> findUserAreas(String email) {
     	List<UserAreasTO> results = null;
     	
     	Connection connection = null;
     	
     	try {
     		connection = ConnectionUtil.createConnection(
     				Constants.Services.JNDI_JDBC_APP, false);
 	    		
     		UserAreasDAO userAreasDAO = new UserAreasDAO(connection);
 	    	results = userAreasDAO.find(email);
 	    		
 	    	connection.commit();
     	} catch(Exception e) {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.rollback();
     			}
     		} catch (Exception e1) {
     			// Do nothing...
     		}
     	} finally {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.close();
     			}
     		} catch (Exception e) {
     			// Do nothing...
     		} finally {
     			connection = null;
     		}
     	}
     	
     	return results;
     }
     
     private void loadCityList(boolean force) {
     	if(_cityList == null || force) {
 	    	List<SelectItem> cityList = new ArrayList<SelectItem>();
 	    	
 	    	List<CityAreasTO> results = findCities(_userEmail);
 	    	
 	    	boolean isFirst = true;
 	    	
 	    	for(CityAreasTO cityAreasTO : results) {
 	    		if(isFirst){
 	    			isFirst = false;
 	    			
 	    			_city = cityAreasTO.getCity();
 	    		}
 	    		
 	    		cityList.add(new SelectItem(cityAreasTO.getCity()));
 	    	}
 	        
 			_cityList = cityList;
     	}
 	}
     
     private List<CityAreasTO> findCities(String email) {
     	List<CityAreasTO> results = null;
     	
     	Connection connection = null;
     	
     	try {
     		connection = ConnectionUtil.createConnection(
     				Constants.Services.JNDI_JDBC_APP, false);
 	    		
     		CityAreasDAO cityAreasDAO = new CityAreasDAO(connection);
 	    	results = cityAreasDAO.findCities(email);
 	    		
 	    	connection.commit();
     	} catch(Exception e) {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.rollback();
     			}
     		} catch (Exception e1) {
     			// Do nothing...
     		}
     	} finally {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.close();
     			}
     		} catch (Exception e) {
     			// Do nothing...
     		} finally {
     			connection = null;
     		}
     	}
     	
     	return results;
     }
     
     private void loadAreaList(boolean force) {
     	if(_areaList == null || force) {
 	    	List<SelectItem> areaList = new ArrayList<SelectItem>();
 	    	
 	    	List<CityAreasTO> results = findAreas(_city, _userEmail);
 	    	
 	    	for(CityAreasTO cityAreasTO : results) {
 	    		areaList.add(new SelectItem(cityAreasTO.getArea()));
 	    	}
 	        
 			_areaList = areaList;
     	}
 	}
     
     private List<CityAreasTO> findAreas(String city, String email) {
     	List<CityAreasTO> results = null;
     	
     	Connection connection = null;
     	
     	try {
     		connection = ConnectionUtil.createConnection(
     				Constants.Services.JNDI_JDBC_APP, false);
 	    		
     		CityAreasDAO cityAreasDAO = new CityAreasDAO(connection);
 	    	results = cityAreasDAO.findAreas(city, email);
 	    		
 	    	connection.commit();
     	} catch(Exception e) {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.rollback();
     			}
     		} catch (Exception e1) {
     			// Do nothing...
     		}
     	} finally {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.close();
     			}
     		} catch (Exception e) {
     			// Do nothing...
     		} finally {
     			connection = null;
     		}
     	}
     	
     	return results;
     }
     
     public void updateAreaOptions() {
     	loadAreaList(true);
 	}
     
     public void updateArea() {
     	_feedback = null;
     	
     	Connection connection = null;
     	
     	try {
     		connection = ConnectionUtil.createConnection(
     				Constants.Services.JNDI_JDBC_APP, false);
     		
     		Bk2UsersDAO bk2UsersDAO = new Bk2UsersDAO(connection);
 	    	bk2UsersDAO.update(_userEmail, _areaToUpdate, _newName);
 	    		
 	    	connection.commit();
 	    	
 	    	_feedback = FeedbackUtil.formatGeneralFeedback(
 					Constants.AlertTypes.SUCCESS,
 					"¡Confirmaci&oacute;n!",
 					"El &aacute;rea <i><strong>" + _newName + "</strong></i> fue actualizada exitosamente.");
 	    	
 	    	updateDashboard();
 	    	loadUserAreas(true);
 	    	
 	    	_areaToDelete = null;
 	    	_name = null;
     	} catch(Exception e) {
     		_feedback = FeedbackUtil.formatGeneralFeedback(
 					Constants.AlertTypes.ERROR,
 					"¡Error!",
 					"El &aacute;rea <i><strong>" + _areaToUpdate + "</strong></i> no pudo ser actualizada en este momento. Por favor intente mas tarde.");
     		
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.rollback();
     			}
     		} catch (Exception e1) {
     			// Do nothing...
     		}
     	} finally {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.close();
     			}
     		} catch (Exception e) {
     			// Do nothing...
     		} finally {
     			connection = null;
     		}
     	}
     }
     
     public void deleteArea() {
     	_feedback = null;
     	
     	Connection connection = null;
     	
     	try {
     		connection = ConnectionUtil.createConnection(
     				Constants.Services.JNDI_JDBC_APP, false);
     		
     		Bk2UsersDAO bk2UsersDAO = new Bk2UsersDAO(connection);
 	    	bk2UsersDAO.delete(_userEmail, _areaToDelete);
 	    		
 	    	connection.commit();
 	    	
 	    	_feedback = FeedbackUtil.formatGeneralFeedback(
 					Constants.AlertTypes.SUCCESS,
 					"¡Confirmaci&oacute;n!",
 					"El &aacute;rea <i><strong>" + _areaToDelete + "</strong></i> fue removida exitosamente.");
 	    	
 	    	updateDashboard();
 	    	loadUserAreas(true);
 	    	
 	    	_areaToDelete = null;
 	    	
 	    	loadCityList(true);
 	    	loadAreaList(true);
 	    	
 	    	_name = null;
     	} catch(Exception e) {
     		_feedback = FeedbackUtil.formatGeneralFeedback(
 					Constants.AlertTypes.ERROR,
 					"¡Error!",
 					"El &aacute;rea <i><strong>" + _name + "</strong></i> no pudo ser removida en este momento. Por favor intente mas tarde.");
     		
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.rollback();
     			}
     		} catch (Exception e1) {
     			// Do nothing...
     		}
     	} finally {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.close();
     			}
     		} catch (Exception e) {
     			// Do nothing...
     		} finally {
     			connection = null;
     		}
     	}
     }
     
     public void addArea() {
     	_feedback = null;
     	
     	Connection connection = null;
     	
     	try {
     		connection = ConnectionUtil.createConnection(
     				Constants.Services.JNDI_JDBC_APP, false);
     		
     		Bk2UsersTO bk2UsersTO = new Bk2UsersTO();
     		bk2UsersTO.setCity(_city);
     		bk2UsersTO.setArea(_area);
     		bk2UsersTO.setFkUserId(_userEmail);
     		
     		if(_name == null || _name.trim().isEmpty()) {
     			_name = _area + ", " + _city;
     		}
     		
     		bk2UsersTO.setName(_name);
 	    		
     		Bk2UsersDAO bk2UsersDAO = new Bk2UsersDAO(connection);
 	    	bk2UsersDAO.create(bk2UsersTO);
 	    		
 	    	connection.commit();
 	    	
 	    	_feedback = FeedbackUtil.formatGeneralFeedback(
 					Constants.AlertTypes.SUCCESS,
 					"¡Confirmaci&oacute;n!",
 					"El &aacute;rea <i><strong>" + _name + "</strong></i> fue a&ntilde;adida exitosamente.");
 	    	
 	    	updateDashboard();
 	    	loadUserAreas(true);
 	    	loadCityList(true);
 	    	loadAreaList(true);
	    	
	    	_name = null;
     	} catch(Exception e) {
     		_feedback = FeedbackUtil.formatGeneralFeedback(
 					Constants.AlertTypes.ERROR,
 					"¡Error!",
 					"El &aacute;rea <i><strong>" + _name + "</strong></i> no pudo ser a&ntilde;adida en este momento. Por favor intente mas tarde.");
     		
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.rollback();
     			}
     		} catch (Exception e1) {
     			// Do nothing...
     		}
     	} finally {
     		try {
     			if(connection != null && !connection.isClosed()) {
     				connection.close();
     			}
     		} catch (Exception e) {
     			// Do nothing...
     		} finally {
     			connection = null;
     		}
     	}
 	}
 }
