 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servlets.actions.get.health.bytitle;
 
 import static util.JsonUtil.ServletPath;
 import health.database.DAO.DatastreamDAO;
 import health.database.DAO.HealthDataStreamDAO;
 import health.database.DAO.SleepDataDAO;
 import health.database.DAO.SubjectDAO;
 import health.database.DAO.UserDAO;
 import health.database.DAO.nosql.HBaseDatapointDAO;
 import health.database.models.Datastream;
 import health.database.models.SleepDataSummary;
 import health.database.models.Subject;
 import health.database.models.Users;
 import health.hbase.models.HBaseDataImport;
 import health.input.jsonmodels.JsonDataPoints;
 import health.input.util.DBtoJsonUtil;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.zip.GZIPOutputStream;
 
 import javax.persistence.NonUniqueResultException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import server.exception.ErrorCodeException;
 import server.exception.ReturnParser;
 import servlets.util.PermissionFilter;
 import servlets.util.ServerUtil;
 import util.AllConstants;
 import util.DateUtil;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.stream.JsonWriter;
 
 /**
  * 
  * @author Leon
  */
 public class GetHealthDataPointsByTitle extends HttpServlet {
 
 	/**
 	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
 	 * methods.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		response.setCharacterEncoding("UTF-8");
 		request.setCharacterEncoding("UTF-8");
 		System.out.println("before checkAndGetLoginFromToken");
 		Users accessUser = null;
 		PermissionFilter filter = new PermissionFilter();
 		String loginID = filter.checkAndGetLoginFromToken(request, response);
 
 		UserDAO userDao = new UserDAO();
 		if (loginID == null) {
 			if (filter.getCheckResult().equalsIgnoreCase(
 					filter.INVALID_LOGIN_TOKEN_ID)) {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_login_token_id,
 						null, null);
 				return;
 			} else if (filter.getCheckResult().equalsIgnoreCase(
 					AllConstants.ErrorDictionary.login_token_expired)) {
 				return;
 			} else {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_login_token_id,
 						null, null);
 				return;
 			}
 		} else {
 			accessUser = userDao.getLogin(loginID);
 		}
 
 		// PrintWriter out = response.getWriter();
 		OutputStream outStream = null;
 		try {
 
 			long start = 0;
 			long end = 0;
 			String blockid = null;
 
 			try {
 				if (request
 						.getParameter(AllConstants.api_entryPoints.request_api_start) != null) {
 					start = Long
 							.parseLong(request
 									.getParameter(AllConstants.api_entryPoints.request_api_start));
 				}
 				if (request
 						.getParameter(AllConstants.api_entryPoints.request_api_end) != null) {
 					end = Long
 							.parseLong(request
 									.getParameter(AllConstants.api_entryPoints.request_api_end));
 				}
 
 				if (request
 						.getParameter(AllConstants.api_entryPoints.request_api_YearMonthDay) != null) {
 					String yearMonthDateString = request
 							.getParameter(AllConstants.api_entryPoints.request_api_YearMonthDay);
 					System.out.println("Date Request " + yearMonthDateString);
 					DateUtil dateUtil = new DateUtil();
 					Date date = dateUtil.convert(yearMonthDateString,
 							dateUtil.YearMonthDay_DateFormat);
 					System.out.println("DateRequest:" + date);
 					Calendar calStart = Calendar.getInstance(DateUtil.UTC);
 					Calendar calEnd = Calendar.getInstance(DateUtil.UTC);
 					calStart.setTime(date);
 					calEnd.setTime(date);
 					calStart.set(Calendar.HOUR_OF_DAY, 0);
 					calStart.set(Calendar.MINUTE, 0);
 					start = calStart.getTimeInMillis();
 					calEnd.set(Calendar.HOUR_OF_DAY, 23);
 					calEnd.set(Calendar.MINUTE, 59);
 					end = calEnd.getTimeInMillis();
 					// System.out.println("Date Request start"+calStart.getTime());
 					// System.out.println("Date Request end"+calEnd.getTime());
 					// System.out.println("using Date Request:"+start+" "+end);
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_date_format, null,
 						null);
 				return;
 			}
 			try {
 				if (request
 						.getParameter(AllConstants.api_entryPoints.request_api_blockid) != null) {
					if (blockid.length() > 5) {
 						blockid = request
 								.getParameter(AllConstants.api_entryPoints.request_api_blockid);
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_Datablock_ID,
 						null, null);
 				return;
 			}
 			// if (!userDao.existLogin(loginID)) {
 			// ReturnParser.outputErrorException(response,
 			// AllConstants.ErrorDictionary.Unauthorized_Access, null,
 			// null);
 			// return;
 			// }
 			SubjectDAO subjDao = new SubjectDAO();
 			Subject subject = (Subject) subjDao.findHealthSubject(loginID); // Retreive
 			if (subject == null) {
 				// ReturnParser.outputErrorException(response,
 				// AllConstants.ErrorDictionary.SYSTEM_ERROR_NO_DEFAULT_HEALTH_SUBJECT,
 				// null,
 				// null);
 				// return;
 				try {
 					subject = subjDao.createDefaultHealthSubject(loginID);
 					HealthDataStreamDAO hdsDao = new HealthDataStreamDAO();
 
 					hdsDao.createDefaultDatastreamsOnDefaultSubject(loginID,
 							subject.getId());
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					ReturnParser.outputErrorException(response,
 							AllConstants.ErrorDictionary.Internal_Fault, null,
 							null);
 					e.printStackTrace();
 				}
 			}
 
 			String streamTitle = ServerUtil
 					.getHealthStreamTitle(ServletPath(request));
 
 			DatastreamDAO dstreamDao = new DatastreamDAO();
 			DBtoJsonUtil dbtoJUtil = new DBtoJsonUtil();
 			Datastream datastream = null;
 			try {
 				datastream = dstreamDao.getHealthDatastreamByTitle(
 						subject.getId(), streamTitle, true, false);
 			} catch (NonUniqueResultException ex) {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Internal_Fault, null,
 						streamTitle);
 				return;
 			}
 			if (datastream == null) {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Unknown_StreamTitle, null,
 						streamTitle);
 				return;
 			}
 			if (blockid != null
 					&& dstreamDao.getDatastreamBlock(blockid) == null) {
 				ReturnParser.outputErrorException(response,
 						AllConstants.ErrorDictionary.Invalid_Datablock_ID,
 						null, blockid);
 				return;
 			}
 			HashMap<String, String> mapUnits = new HashMap<String, String>();
 			HashMap<String, String> allUnits = new HashMap<String, String>();
 			if (request
 					.getParameter(AllConstants.api_entryPoints.request_api_unit_id) != null
 					&& request.getParameter(
 							AllConstants.api_entryPoints.request_api_unit_id)
 							.length() > 0) {
 				String[] unitids = request.getParameter(
 						AllConstants.api_entryPoints.request_api_unit_id)
 						.split(",");
 				System.out.println("unitids:size:" + unitids.length);
 				allUnits = dbtoJUtil.ToDatastreamUnitsMap(datastream);
 				System.out.println("units size:"
 						+ datastream.getDatastreamUnitsList().size());
 				for (String id : unitids) {
 					if (id.length() < 3) {
 						// error
 						return;
 					} else {
 						if (allUnits.get(id) == null) {
 							// error
 							System.out.println("cannot find id" + id + "");
 							return;
 						} else {
 							mapUnits.put(id, id);
 						}
 					}
 				}
 			}
 			System.out.println("mapUnits.size():" + mapUnits.size() + ", "
 					+ mapUnits);
 			Gson gson = new Gson();
 			int debug = 1;
 			if (debug == 1) {
 				System.out.println("debuging.....going to hbase");
 				HBaseDatapointDAO diDao = new HBaseDatapointDAO();
 				System.out.println("datastreamID:" + datastream.getStreamId());
 				HBaseDataImport hbaseexport = null;
 				try {
 
 					if (streamTitle
 							.equalsIgnoreCase(AllConstants.ProgramConts.defaultDS_Name_sleep)) {
 						// sleep record
 						if (request
 								.getParameter(AllConstants.api_entryPoints.request_api_YearMonthDay) == null) {
 							ReturnParser
 									.outputErrorException(
 											response,
 											AllConstants.ErrorDictionary.Invalid_date_format,
 											null, null);
 							return;
 						}
 						DateUtil dateUtil = new DateUtil();
 						String yearMonthDateString = request
 								.getParameter(AllConstants.api_entryPoints.request_api_YearMonthDay);
 						Date date = dateUtil.convert(yearMonthDateString,
 								dateUtil.YearMonthDay_DateFormat);
 
 						SleepDataDAO sleepdataDao = new SleepDataDAO();
 						List<SleepDataSummary> sleepSummaryList = sleepdataDao
 								.getSleepDataSummaries(
 										datastream.getStreamId(), null, date);
 						if (sleepSummaryList.size() < 1) {
 							ReturnParser
 									.outputErrorException(
 											response,
 											AllConstants.ErrorDictionary.NO_SLEEP_RECORD,
 											null, datastream.getStreamId());
 							return;
 						}
 						start = sleepSummaryList.get(0).getStartTime()
 								.getTime();
 						end = sleepSummaryList.get(0).getEndtime()
 								.getTime();
 						for (SleepDataSummary summary : sleepSummaryList) {
 							if(start>summary.getStartTime().getTime())
 							{
 								start=summary.getStartTime().getTime();
 							}
 							if(end<summary.getEndtime().getTime())
 							{
 								end=summary.getEndtime().getTime();
 							}
 						}
 						if (datastream.getDatastreamUnitsList().size() == 0) {
 							ReturnParser
 									.outputErrorException(
 											response,
 											AllConstants.ErrorDictionary.Unknown_StreamID,
 											null, datastream.getStreamId());
 							return;
 						}
 						if (datastream.getDatastreamUnitsList().size() > 1) {
 							ReturnParser
 									.outputErrorException(
 											response,
 											AllConstants.ErrorDictionary.MORE_THAN_ONE_DATASTREAM_UNIT,
 											null, datastream.getStreamId());
 							return;
 						}
 						mapUnits = new HashMap<String, String>();
 						mapUnits.put(datastream.getDatastreamUnitsList().get(0)
 								.getUnitID(), datastream
 								.getDatastreamUnitsList().get(0).getUnitID());
 						hbaseexport = diDao.exportDatapointsForSingleUnit(
 								datastream.getStreamId(), start, end, blockid,
 								datastream.getDatastreamUnitsList().get(0)
 										.getUnitID(), null);
 
 					} else {
 
 						if (request
 								.getParameter(AllConstants.api_entryPoints.request_api_dataformat) != null) {
 							DateUtil dateUtil = new DateUtil();
 							hbaseexport = diDao.exportDatapoints(
 									datastream.getStreamId(), start, end,
 									blockid, mapUnits, dateUtil.millisecFormat);
 						} else {
 							hbaseexport = diDao.exportDatapoints(
 									datastream.getStreamId(), start, end,
 									blockid, mapUnits, null);
 						}
 					}
 				} catch (ErrorCodeException ex) {
 					ex.printStackTrace();
 					ReturnParser.outputErrorException(response,
 							AllConstants.ErrorDictionary.Internal_Fault, null,
 							null);
 					return;
 				} catch (Throwable ex) {
 					ex.printStackTrace();
 					ReturnParser.outputErrorException(response,
 							AllConstants.ErrorDictionary.Internal_Fault, null,
 							null);
 					return;
 				}
 				if (hbaseexport != null) {
 					hbaseexport.setUnits_list(dbtoJUtil.convertDatastream(
 							datastream, mapUnits).getUnits_list());
 				} else {
 					hbaseexport = new HBaseDataImport();
 					hbaseexport.setBlock_id(blockid);
 					hbaseexport.setData_points(new ArrayList<JsonDataPoints>());
 
 					hbaseexport.setDatastream_id(datastream.getStreamId());
 					hbaseexport.setUnits_list(dbtoJUtil.convertDatastream(
 							datastream, mapUnits).getUnits_list());
 					// hbaseexport.setDeviceid(streamID);
 				}
 				outStream = null;
 				boolean iftoZip = true;
 				String encodings = request.getHeader("Accept-Encoding");
 				if (encodings != null && encodings.indexOf("gzip") != -1
 						&& iftoZip == true) {
 					// Go with GZIP
 					response.setHeader("Content-Encoding", "gzip");
 					outStream = new GZIPOutputStream(response.getOutputStream());
 				} else {
 					outStream = response.getOutputStream();
 				}
 				response.setHeader("Vary", "Accept-Encoding");
 				Date timerStart = new Date();
 				JsonElement je = gson.toJsonTree(hbaseexport);
 				JsonObject jo = new JsonObject();
 				jo.addProperty(AllConstants.ProgramConts.result,
 						AllConstants.ProgramConts.succeed);
 				jo.add("datapoints_list", je);
 				OutputStreamWriter osWriter = new OutputStreamWriter(outStream);
 				JsonWriter jwriter = new JsonWriter(osWriter);
 				String callbackStr = null;
 				if (request
 						.getParameter(AllConstants.api_entryPoints.requset_api_callback) != null) {
 					callbackStr = request
 							.getParameter(AllConstants.api_entryPoints.requset_api_callback);
 					osWriter.append(callbackStr + "(");
 				}
 				gson.toJson(jo, jwriter);
 				if (callbackStr != null) {
 					osWriter.append(");");
 				}
 				jwriter.close();
 				Date timerEnd = new Date();
 				System.out.println("Json Time takes:"
 						+ (timerEnd.getTime() - timerStart.getTime())
 						/ (1000.00) + "seconds");
 				osWriter.close();
 				outStream.close();
 			} else {
 				String encodings = request.getHeader("Accept-Encoding");
 				boolean iftoZip = true;
 				if (encodings != null && encodings.indexOf("gzip") != -1
 						&& iftoZip == true) {
 					// Go with GZIP
 					response.setHeader("Content-Encoding", "gzip");
 					outStream = new GZIPOutputStream(response.getOutputStream());
 				} else {
 					outStream = response.getOutputStream();
 				}
 				response.setHeader("Vary", "Accept-Encoding");
 				File inputFile = new File(
 						"E:/IC_Dropbox/Dropbox/java/healthbook/sample_data/download.txt");
 				// File inputFile = new
 				// File("F:/Dropbox/Dropbox/java/healthbook/sample_data/download.txt");
 				BufferedReader reader = new BufferedReader(new FileReader(
 						inputFile));
 				String inputLine;
 				while ((inputLine = reader.readLine()) != null) {
 					outStream.write(inputLine.getBytes());
 					// out.print(inputLine);
 				}
 				outStream.close();
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			ReturnParser.outputErrorException(response,
 					AllConstants.ErrorDictionary.Internal_Fault, null, null);
 			return;
 		} finally {
 			System.out.println("running finally");
 			// out.close();
 			if (outStream != null) {
 				outStream.close();
 			}
 		}
 	}
 
 	// <editor-fold defaultstate="collapsed"
 	// desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
 	/**
 	 * Handles the HTTP <code>GET</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Handles the HTTP <code>POST</code> method.
 	 * 
 	 * @param request
 	 *            servlet request
 	 * @param response
 	 *            servlet response
 	 * @throws ServletException
 	 *             if a servlet-specific error occurs
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		processRequest(request, response);
 	}
 
 	/**
 	 * Returns a short description of the servlet.
 	 * 
 	 * @return a String containing servlet description
 	 */
 	@Override
 	public String getServletInfo() {
 		return "Short description";
 	}// </editor-fold>
 }
