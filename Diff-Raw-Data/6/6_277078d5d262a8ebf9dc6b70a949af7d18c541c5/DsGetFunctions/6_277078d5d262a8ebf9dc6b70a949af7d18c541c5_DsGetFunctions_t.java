 package edu.thu.keg.mdap.restful.dataset;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import javax.ws.rs.core.Response.Status;
 import com.sun.jersey.api.json.JSONWithPadding;
 import com.sun.org.apache.xerces.internal.util.URI;
 import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;
 
 import edu.thu.keg.mdap.DataSetManager;
 import edu.thu.keg.mdap.Platform;
 import edu.thu.keg.mdap.datafeature.DataFeature;
 import edu.thu.keg.mdap.datafeature.DataFeatureType;
 import edu.thu.keg.mdap.datafeature.DataView;
 import edu.thu.keg.mdap.datamodel.DataContent;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.DataField.FieldFunctionality;
 import edu.thu.keg.mdap.datamodel.DataField.FieldType;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.Query.Operator;
 import edu.thu.keg.mdap.datamodel.Query.Order;
 import edu.thu.keg.mdap.init.MessageInfo;
 
 import edu.thu.keg.mdap.provider.DataProviderException;
 import edu.thu.keg.mdap.restful.exceptions.UserNotInPoolException;
 import edu.thu.keg.mdap.restful.jerseyclasses.dataset.JDatasetLine;
 import edu.thu.keg.mdap.restful.jerseyclasses.dataset.JDatasetName;
 import edu.thu.keg.mdap.restful.jerseyclasses.dataset.JField;
 import edu.thu.keg.mdap.restful.jerseyclasses.dataset.JFieldName;
 import edu.thu.keg.mdap.restful.jerseyclasses.dataset.JGeograph;
 import edu.thu.keg.mdap.restful.jerseyclasses.dataset.JStatistic;
 import edu.thu.keg.mdap_impl.PlatformImpl;
 import edu.thu.keg.mdap_impl.datamodel.DataSetImpl;
 
 /**
  * the functions of dataset's get operations
  * 
  * @author Yuan Bozhi
  * 
  */
 @Path("/dsg")
 public class DsGetFunctions {
 	/**
 	 * 
 	 */
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 	@Context
 	ServletContext servletcontext;
 	@Context
 	HttpServletRequest httpServletRequest;
 	@Context
 	HttpServletResponse httpServletResponse;
 
 	HttpSession session = null;
 	private static Logger log = Logger.getLogger(DsGetFunctions.class
 			.getSimpleName());
 
 	/**
 	 * get all dataset names list
 	 * 
 	 * @return a list including dataset names
 	 */
 	@GET
 	@Path("/getdss")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info(uriInfo.getAbsolutePath());
 		session = httpServletRequest.getSession();
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		System.out.println(session.getId());
 		Platform p = (Platform) servletcontext.getAttribute("platform");
 		DataSetManager datasetManager = p.getDataSetManager();
 		Collection<DataSet> datasets = datasetManager.getDataSetList();
 		int i = 0;
 		for (DataSet dataset : datasets) {
 			JDatasetName dname = new JDatasetName();
 			dname.setId(dataset.getId());
 			dname.setDatasetName(dataset.getName());
 			dname.setOwner(dataset.getOwner());
 			dname.setPermission(DataSetImpl.permissionToString(dataset
 					.getPermission()));
 			dname.setLimitedUsers(dataset.getLimitedUsers());
 			dname.setDescriptionEn(dataset.getDescription(Locale.ENGLISH));
 			dname.setDescriptionZh(dataset.getDescription(Locale.CHINESE));
 			ArrayList<String> keyFields = new ArrayList<>();
 			ArrayList<String> otherFields = new ArrayList<>();
 			for (DataField df : dataset.getPrimaryKeyFields()) {
 				keyFields.add(df.getName());
 			}
 			dname.setKeyFields(keyFields);
 			for (DataField df : dataset.getOtherFields()) {
 				otherFields.add(df.getName());
 			}
 			dname.setOtherFields(otherFields);
 			ArrayList<String> datafeatures = new ArrayList<>();
 			for (DataFeature df : dataset.getFeatures()) {
 				datafeatures.add(df.getFeatureType().name());
 			}
 			dname.setDatafeature(datafeatures);
 			datasetsName.add(dname);
 		}
 
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, jsoncallback);
 
 	}
 
 	/**
 	 * get all dataset names list
 	 * 
 	 * @return a list including dataset Geo names
 	 */
 	@GET
 	@Path("/getgeodss")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getGeoDatasetsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getGeoDatasetsNames " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getDataSetList(DataFeatureType.GeoFeature);
 			for (DataSet dataset : datasets) {
 				JDatasetName dname = new JDatasetName();
 				dname.setId(dataset.getId());
 				dname.setDatasetName(dataset.getName());
 				dname.setOwner(dataset.getOwner());
 				dname.setPermission(DataSetImpl.permissionToString(dataset
 						.getPermission()));
 				dname.setLimitedUsers(dataset.getLimitedUsers());
 				dname.setDescriptionEn(dataset.getDescription(Locale.ENGLISH));
 				dname.setDescriptionZh(dataset.getDescription(Locale.CHINESE));
 				ArrayList<String> keyFields = new ArrayList<>();
 				ArrayList<String> otherFields = new ArrayList<>();
 				for (DataField df : dataset.getPrimaryKeyFields()) {
 					keyFields.add(df.getName());
 				}
 				dname.setKeyFields(keyFields);
 				for (DataField df : dataset.getOtherFields()) {
 					otherFields.add(df.getName());
 				}
 				dname.setOtherFields(otherFields);
 				ArrayList<String> datafeatures = new ArrayList<>();
 				for (DataFeature df : dataset.getFeatures()) {
 					datafeatures.add(df.getFeatureType().name());
 				}
 				dname.setDatafeature(datafeatures);
 				datasetsName.add(dname);
 			}
 
 		} catch (Exception e) {
 			log.warn(e.getMessage());
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, jsoncallback);
 	}
 
 	/**
 	 * get all dataset names list
 	 * 
 	 * @return a list including dataset Sta names
 	 */
 	@GET
 	@Path("/getstadss")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getStaDatasetsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getStaDatasetsNames " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getDataSetList(DataFeatureType.DistributionFeature);
 			int i = 0;
 			for (DataSet dataset : datasets) {
 				JDatasetName dname = new JDatasetName();
 				dname.setId(dataset.getId());
 				dname.setDatasetName(dataset.getName());
 				dname.setOwner(dataset.getOwner());
 				dname.setPermission(DataSetImpl.permissionToString(dataset
 						.getPermission()));
 				dname.setLimitedUsers(dataset.getLimitedUsers());
 				dname.setDescriptionEn(dataset.getDescription(Locale.ENGLISH));
 				dname.setDescriptionZh(dataset.getDescription(Locale.CHINESE));
 
 				ArrayList<String> schema = new ArrayList<>();
 				ArrayList<String> keyFields = new ArrayList<>();
 				ArrayList<String> otherFields = new ArrayList<>();
 				for (DataField df : dataset.getPrimaryKeyFields()) {
 					keyFields.add(df.getName());
 				}
 				dname.setKeyFields(keyFields);
 				for (DataField df : dataset.getOtherFields()) {
 					otherFields.add(df.getName());
 				}
 				dname.setOtherFields(otherFields);
 				datasetsName.add(dname);
 			}
 
 		} catch (Exception e) {
 			log.warn(e.getMessage());
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, jsoncallback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format all rs array of JDataset
 	 */
 	@GET
 	@Path("/getds")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDataset(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getDataset " + id + " " + uriInfo.getAbsolutePath());
 		List<JDatasetLine> datasetList = new ArrayList<>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(id);
 			Query rs = ds.getQuery();
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				JDatasetLine jdataset = new JDatasetLine();
 				List<JField> fields = new ArrayList<>();
 				DataField[] dfs = ds.getDataFields().toArray(new DataField[0]);
 				int j = 0;
 				for (DataField df : dfs) {
 					JField field = new JField();
 					if (rs.getValue(df) == null) {
 						field.setValue("null");
 					} else {
 						field.setValue(rs.getValue(df).toString());
 					}
 					field.setType(df.getFieldType().getJavaClass()
 							.getSimpleName());
 					fields.add(field);
 				}
 				jdataset.setField(fields);
 				datasetList.add(jdataset);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 
 			log.warn(e.getMessage());
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetLine>>(
 				datasetList) {
 		}, jsoncallback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format location array
 	 */
 	@GET
 	@Path("/getgeods")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getGeoDataset(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getLocDataset " + id + " " + uriInfo.getAbsolutePath());
 		List<JGeograph> datasetList = new ArrayList<JGeograph>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(id);
 			DataContent rs = ds.getQuery();
 			DataFeature gds = ds.getFeature(DataFeatureType.GeoFeature);
 			if (gds == null)
 				throw new OperationNotSupportedException(
 						"can't find the geograph Exception");
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				JGeograph location = new JGeograph();
 				location.setLatitude((double) rs.getValue(gds.getKeyFields()
 						.get(0)));
 				location.setLongitude((double) rs.getValue(gds.getKeyFields()
 						.get(1)));
 				List<String> values = new ArrayList<String>();
 				for (DataField df : gds.getValueFields()) {
 					values.add(rs.getValue(df).toString());
 				}
 				location.setValus(values);
 				datasetList.add(location);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			e.printStackTrace();
 			log.warn(e.getMessage());
 		}
 		return new JSONWithPadding(new GenericEntity<List<JGeograph>>(
 				datasetList) {
 		}, jsoncallback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format statistics array
 	 */
 	@GET
 	@Path("/getstads")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getStaDataset(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getStaDataset " + id + " " + uriInfo.getAbsolutePath());
 		List<JStatistic> datasetList = new ArrayList<JStatistic>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(id);
 			DataContent rs = ds.getQuery();
 			DataFeature gds = ds
 					.getFeature(DataFeatureType.DistributionFeature);
 			if (gds == null)
 				throw new OperationNotSupportedException(
 						"can't find the statistic Exception");
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				// System.out.println(rs.getValue(ds.getDataFields()[0])
 				// .toString()
 				// + " "
 				// + rs.getValue(ds.getDataFields()[1]).toString());
 				JStatistic statistic = new JStatistic();
 				ArrayList<String> indentifiers = new ArrayList<>();
 				for (DataField indentifier : gds.getKeyFields()) {
 					indentifiers.add(rs.getValue(indentifier).toString());
 				}
 				ArrayList<Double> values = new ArrayList<>();
 				for (DataField value : gds.getValueFields()) {
 					if (rs.getValue(value) != null)
 						values.add(Double
 								.valueOf(rs.getValue(value).toString()));
 					else
 						values.add(null);
 				}
 				statistic.setIndentifiers(indentifiers);
 				statistic.setValue(values);
 				datasetList.add(statistic);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			e.printStackTrace();
 			log.warn(e.getMessage());
 		}
 		return new JSONWithPadding(new GenericEntity<List<JStatistic>>(
 				datasetList) {
 		}, jsoncallback);
 	}
 
 	/**
 	 * get all of the dataset's fields
 	 * 
 	 * @param dataset
 	 * @return a list of all fields name
 	 */
 	@GET
 	@Path("/getdsfds")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetFieldsNames(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getDatasetFieldsNames " + id + " "
 				+ uriInfo.getAbsolutePath());
 		List<JFieldName> all_fn = new ArrayList<JFieldName>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(id);
 			DataField[] dfs = ds.getDataFields().toArray(new DataField[0]);
 			for (DataField df : dfs) {
 				JFieldName jfn = new JFieldName();
 				jfn.setFieldName(df.getName());
 				jfn.setDescription(df.getDescription());
 				jfn.setIsKey(df.isKey());
 				jfn.setType(df.getFieldType().name());
 				jfn.setFunctionality(df.getFunction().name());
 
 				all_fn.add(jfn);
 			}
 		} catch (Exception e) {
 			log.warn(e.getMessage());
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JFieldName>>(all_fn) {
 		}, jsoncallback);
 	}
 
 	/**
 	 * get a column form the dataset
 	 * 
 	 * @param dataset
 	 * @param fieldname
 	 * @return
 	 */
 	@GET
 	@Path("/getdscs")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetField(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback,
 			@QueryParam("fields") JSONArray jsonFileds,
 			@QueryParam("orderby") String orderby) {
 		log.info(uriInfo.getAbsolutePath());
 		String fieldname = null;
 		List<JDatasetLine> all_dfs = null;
 		List<JField> list_df = null;
 		/**
 		 * fields 存储列名的参数jsonarray orderby 排序的域名
 		 */
 		try {
 			if (jsonFileds == null)
 				throw new JSONException("have not the this Field");
 			all_dfs = new ArrayList<>();
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(id);
 			DataContent rs;
 			if (orderby != null)
 				rs = ds.getQuery().orderBy(orderby, Order.parse(orderby));
 			else
 				rs = ds.getQuery();
 			for (int i = 0; i < jsonFileds.length(); i++) {
 				fieldname = (String) jsonFileds.get(i);
 				System.out.println("getDatasetField " + id + " " + fieldname
 						+ " " + uriInfo.getAbsolutePath());
 				list_df = new ArrayList<JField>();
 				DataField df = ds.getField(fieldname);
 
 				rs.open();
 				int ii = 0;
 				while (rs.next() && ii++ < 20) {
 					JField field = new JField();
 					field.setValue(rs.getValue(df).toString());
 					field.setType(rs.getValue(df).getClass().getSimpleName());
 					list_df.add(field);
 				}
 				rs.close();
 				JDatasetLine jc = new JDatasetLine();
 				jc.setField(list_df);
 				all_dfs.add(jc);
 			}
 
 		} catch (JSONException | OperationNotSupportedException
 				| DataProviderException e) {
 			// TODO Auto-generated catch block
 			log.warn(e.getMessage());
 		}
 		// return all_dfs;
 		return new JSONWithPadding(new GenericEntity<List<JDatasetLine>>(
 				all_dfs) {
 		}, callback);
 	}
 
 	/**
 	 * get the clause result
 	 * 
 	 * @param dataset
 	 * @param fieldname
 	 * @param opr
 	 * @param value
 	 * @return
 	 */
 	@GET
 	@Path("/getdsres")
	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetValueOfOpr(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback,
 			@QueryParam("jsonoper") JSONArray jsonOper) {
 		log.info(uriInfo.getAbsolutePath());
 		System.out.println(id + " " + jsonOper);
 		String func = null;
 		String fieldname = null;
 		String opr = null;
 		String value = null;
 		List<JDatasetLine> all_dfs = null;
 		List<JField> list_df = null;
 		/**
 		 * jsonOper 操作参数jsonarray fieldname 域名 opr 操作符号 value 值
 		 */
 		try {
 			if (jsonOper == null)
 				throw new JSONException("have not the this Operation");
 			all_dfs = new ArrayList<>();
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(id);
 			Query q = ds.getQuery();
 			for (int i = 0; i < jsonOper.length(); i++) {
 				JSONArray ja = (JSONArray) jsonOper.get(i);
				if (ja.get(3) == null)
					continue;
 				if (ja.getString(0).equals("or")) {
 					q = q.whereOr(ja.getString(1),
 							Operator.valueOf(ja.getString(2)), ja.get(3));
 				} else if (ja.getString(0).equals("and")) {
 					if (i == 0)
 						throw new IllegalArgumentException(
 								"the first func must be 'or'!");
 					q = q.whereAnd(ja.getString(1),
 							Operator.valueOf(ja.getString(2)), ja.get(3));
 				}
 			}
 			q.open();
 			int ii = 0;
 			List<JField> line_dfs = new ArrayList<>();
 			JField line_df = null;
 			while (q.next() && ii++ < 20) {
 				for (DataField df : q.getFields()) {
 					line_df = new JField();
 					line_df.setValue(q.getValue(df).toString());
 					line_df.setType(df.getClass().getSimpleName());
 					line_dfs.add(line_df);
 				}
 				JDatasetLine jdsl = new JDatasetLine();
 				jdsl.setField(line_dfs);
 				all_dfs.add(jdsl);
 			}
 		} catch (OperationNotSupportedException | DataProviderException
 				| JSONException | IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			log.warn(e.getMessage());
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetLine>>(
 				all_dfs) {
 		}, callback);
 	}
 
 	@GET
 	@Path("/getdsinfo")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetInfo(@QueryParam("id") String id,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getDatasetInfo " + id + " " + uriInfo.getAbsolutePath());
 		Platform p = (Platform) servletcontext.getAttribute("platform");
 		DataSetManager datasetManager = p.getDataSetManager();
 		DataSet dataset = datasetManager.getDataSet(id);
 		JDatasetName dname = new JDatasetName();
 		dname.setId(dataset.getId());
 		dname.setDatasetName(dataset.getName());
 		dname.setOwner(dataset.getOwner());
 		dname.setPermission(DataSetImpl.permissionToString(dataset
 				.getPermission()));
 		dname.setLimitedUsers(dataset.getLimitedUsers());
 		dname.setDescriptionEn(dataset.getDescription(Locale.ENGLISH));
 		dname.setDescriptionZh(dataset.getDescription(Locale.CHINESE));
 		ArrayList<String> keyFields = new ArrayList<>();
 		ArrayList<String> otherFields = new ArrayList<>();
 		for (DataField df : dataset.getPrimaryKeyFields()) {
 			keyFields.add(df.getName());
 		}
 		dname.setKeyFields(keyFields);
 		for (DataField df : dataset.getOtherFields()) {
 			otherFields.add(df.getName());
 		}
 		dname.setOtherFields(otherFields);
 
 		ArrayList<String> datafeatures = new ArrayList<>();
 		for (DataFeature df : dataset.getFeatures()) {
 			datafeatures.add(df.getFeatureType().name());
 		}
 		dname.setDatafeature(datafeatures);
 		// } catch (Exception e) {
 		// log.warn(e.getStackTrace());
 		// // e.printStackTrace();
 		// }
 
 		return new JSONWithPadding(new GenericEntity<JDatasetName>(dname) {
 		}, jsoncallback);
 	}
 
 	@GET
 	@Path("/getpubdss")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getPublicDatasetNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		log.info("getPublicDatasetNames(String) " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getPublicDataSetList();
 			int i = 0;
 			for (DataSet dataset : datasets) {
 				JDatasetName dname = new JDatasetName();
 				dname.setId(dataset.getId());
 				dname.setDatasetName(dataset.getName());
 				dname.setOwner(dataset.getOwner());
 				dname.setPermission(DataSetImpl.permissionToString(dataset
 						.getPermission()));
 				dname.setLimitedUsers(dataset.getLimitedUsers());
 				dname.setDescriptionEn(dataset.getDescription(Locale.ENGLISH));
 				dname.setDescriptionZh(dataset.getDescription(Locale.CHINESE));
 				ArrayList<String> keyFields = new ArrayList<>();
 				ArrayList<String> otherFields = new ArrayList<>();
 				for (DataField df : dataset.getPrimaryKeyFields()) {
 					keyFields.add(df.getName());
 				}
 				dname.setKeyFields(keyFields);
 				for (DataField df : dataset.getOtherFields()) {
 					otherFields.add(df.getName());
 				}
 				dname.setOtherFields(otherFields);
 				datasetsName.add(dname);
 			}
 
 		} catch (Exception e) {
 			log.warn(e.getMessage());
 		}
 
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, jsoncallback);
 	}
 
 	@GET
 	@Path("/getlimdss")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getLimitedDatasetNames(
 			@QueryParam("userid") String userid,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		session = httpServletRequest.getSession();
 		log.info(uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			userid = (String) session.getAttribute("userid");
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getLimitedDataSetList(userid);
 			int i = 0;
 			if (datasets != null)
 				for (DataSet dataset : datasets) {
 					JDatasetName dname = new JDatasetName();
 					dname.setId(dataset.getId());
 					dname.setDatasetName(dataset.getName());
 					dname.setOwner(dataset.getOwner());
 					dname.setPermission(DataSetImpl.permissionToString(dataset
 							.getPermission()));
 					dname.setLimitedUsers(dataset.getLimitedUsers());
 					dname.setDescriptionEn(dataset
 							.getDescription(Locale.ENGLISH));
 					dname.setDescriptionZh(dataset
 							.getDescription(Locale.CHINESE));
 					ArrayList<String> keyFields = new ArrayList<>();
 					ArrayList<String> otherFields = new ArrayList<>();
 					for (DataField df : dataset.getPrimaryKeyFields()) {
 						keyFields.add(df.getName());
 					}
 					dname.setKeyFields(keyFields);
 					for (DataField df : dataset.getOtherFields()) {
 						otherFields.add(df.getName());
 					}
 					dname.setOtherFields(otherFields);
 					datasetsName.add(dname);
 				}
 
 		} catch (IllegalArgumentException | UserNotInPoolException e) {
 			log.info(e.getStackTrace());
 
 			try {
 				return new JSONWithPadding(new GenericEntity<String>(
 						new JSONObject().put("error", e.getMessage())
 								.toString()) {
 				}, jsoncallback);
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, jsoncallback);
 	}
 
 	@GET
 	@Path("/getowndss")
 	@Produces({ "application/javascript", MediaType.APPLICATION_JSON })
 	public JSONWithPadding getOwnDatasetNames(
 			@QueryParam("userid") String userid,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String jsoncallback) {
 		session = httpServletRequest.getSession();
 		log.info(uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		try {
 			if (session.getAttribute("userid") == null)
 				throw new UserNotInPoolException(MessageInfo.COOKIES_TIMEOUT);
 			userid = (String) session.getAttribute("userid");
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getOwnDataSetList(userid);
 			if (datasets != null)
 				for (DataSet dataset : datasets) {
 					JDatasetName dname = new JDatasetName();
 					dname.setId(dataset.getId());
 					dname.setDatasetName(dataset.getName());
 					dname.setOwner(dataset.getOwner());
 					dname.setPermission(DataSetImpl.permissionToString(dataset
 							.getPermission()));
 					dname.setLimitedUsers(dataset.getLimitedUsers());
 					dname.setDescriptionEn(dataset
 							.getDescription(Locale.ENGLISH));
 					dname.setDescriptionZh(dataset
 							.getDescription(Locale.CHINESE));
 					ArrayList<String> keyFields = new ArrayList<>();
 					ArrayList<String> otherFields = new ArrayList<>();
 					for (DataField df : dataset.getPrimaryKeyFields()) {
 						keyFields.add(df.getName());
 					}
 					dname.setKeyFields(keyFields);
 					for (DataField df : dataset.getOtherFields()) {
 						otherFields.add(df.getName());
 					}
 					dname.setOtherFields(otherFields);
 					datasetsName.add(dname);
 				}
 
 		} catch (IllegalArgumentException | UserNotInPoolException e) {
 			log.warn(e.getStackTrace());
 			try {
 				return new JSONWithPadding(new GenericEntity<String>(
 						new JSONObject().put("error", e.getMessage())
 								.toString()) {
 				}, jsoncallback);
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 		}
 
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, jsoncallback);
 	}
 
 	@GET
 	@Path("/hello")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public String getString() {
 		String a = "{\"city\":\"helloworld_json\"}";
 		System.out.println(a);
 		return a;
 	}
 
 	@GET
 	@Path("/hello")
 	@Produces({ MediaType.TEXT_PLAIN })
 	public String getString2() {
 		System.out.println("{helloworld_json}");
 		return "{helloworld_json}";
 	}
 
 	@GET
 	@Path("/jsonp")
 	@Produces("application/x-javascript")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding readAllP(
 			@QueryParam("jsoncallback") String jsoncallback,
 			@QueryParam("acronym") String acronym,
 			@QueryParam("title") String title,
 			@QueryParam("competition") String competition) {
 		String a = "{\"city\":\"Beijing\",\"street\":\" Chaoyang Road \",\"postcode\":100025}";
 		System.out.println(a);
 		return new JSONWithPadding(a, jsoncallback);
 	}
 }
