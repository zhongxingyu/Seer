 package edu.thu.keg.mdap.restful.dataset;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.naming.OperationNotSupportedException;
 import javax.servlet.ServletContext;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 
 import com.sun.jersey.api.json.JSONWithPadding;
 
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
 
 import edu.thu.keg.mdap.provider.DataProviderException;
 import edu.thu.keg.mdap.restful.jerseyclasses.JDataset;
 import edu.thu.keg.mdap.restful.jerseyclasses.JDatasetName;
 import edu.thu.keg.mdap.restful.jerseyclasses.JField;
 import edu.thu.keg.mdap.restful.jerseyclasses.JFieldName;
 import edu.thu.keg.mdap.restful.jerseyclasses.JGeograph;
 import edu.thu.keg.mdap.restful.jerseyclasses.JStatistic;
 
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
 
 	/**
 	 * get all dataset names list
 	 * 
 	 * @return a list including dataset names
 	 */
 	@GET
 	@Path("/getdss")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getDatasetsNames4 " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager.getDataSetList();
 			int i = 0;
 			for (DataSet dataset : datasets) {
 				// if(i++>=1)
 				// break;
 				JDatasetName dname = new JDatasetName();
 				dname.setDatasetName(dataset.getName());
 				dname.setDescription(dataset.getDescription());
 				ArrayList<String> schema = new ArrayList<>();
 				for (DataField df : dataset.getDataFields()) {
 					schema.add(df.getName());
 				}
 				dname.setSchema(schema);
 				datasetsName.add(dname);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, callback);
 
 	}
 
 	/**
 	 * get all dataset names list
 	 * 
 	 * @return a list including dataset Geo names
 	 */
 	@GET
 	@Path("/getgeodss")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getGeoDatasetsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getGeoDatasetsNames " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getDataSetList(DataFeatureType.GeoFeature);
 			for (DataSet dataset : datasets) {
 				JDatasetName dname = new JDatasetName();
 				dname.setDatasetName(dataset.getName());
 				dname.setDescription(dataset.getDescription());
 				ArrayList<String> schema = new ArrayList<>();
 				for (DataField df : dataset.getDataFields()) {
 					schema.add(df.getName());
 				}
 				dname.setSchema(schema);
 				datasetsName.add(dname);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, callback);
 	}
 
 	/**
 	 * get all dataset names list
 	 * 
 	 * @return a list including dataset Sta names
 	 */
 	@GET
 	@Path("/getstadss")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getStaDatasetsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getStaDatasetsNames " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataSet> datasets = datasetManager
 					.getDataSetList(DataFeatureType.DistributionFeature);
 			int i = 0;
 			for (DataSet dataset : datasets) {
 				// if(i++>=2)
 				// break;
 				JDatasetName dname = new JDatasetName();
 				dname.setDatasetName(dataset.getName());
 				dname.setDescription(dataset.getDescription());
 				ArrayList<String> schema = new ArrayList<>();
 				for (DataField df : dataset.getDataFields()) {
 					schema.add(df.getName());
 				}
 				dname.setSchema(schema);
 				datasetsName.add(dname);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, callback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format all rs array of JDataset
 	 */
 	@GET
 	@Path("/getds/{datasetname}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDataset(@PathParam("datasetname") String dataset,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getDataset " + dataset + " "
 				+ uriInfo.getAbsolutePath());
 		List<JDataset> datasetList = new ArrayList<>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(dataset);
 			DataContent rs = ds.getQuery();
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				JDataset jdataset = new JDataset();
 				List<JField> fields = new ArrayList<>();
 				DataField[] dfs = ds.getDataFields().toArray(new DataField[0]);
 				int j = 0;
 				for (DataField df : dfs) {
 					// if(j++>=2)
 					// break;
 					JField field = new JField();
 					field.setField(rs.getValue(df));
 					fields.add(field);
 				}
 				jdataset.setField(fields);
 				datasetList.add(jdataset);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDataset>>(
 				datasetList) {
 		}, callback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format location array
 	 */
 	@GET
 	@Path("/getgeods/{datasetname}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getGeoDataset(
 			@PathParam("datasetname") String dataset,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getLocDataset " + dataset + " "
 				+ uriInfo.getAbsolutePath());
 		List<JGeograph> datasetList = new ArrayList<JGeograph>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(dataset);
 			DataContent rs = ds.getQuery();
 			DataFeature gds = ds.getFeature(DataFeatureType.GeoFeature);
 			if (gds == null)
 				throw new OperationNotSupportedException(
 						"can't find the geograph Exception");
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				JGeograph location = new JGeograph();
 				location.setLatitude((double) rs.getValue(gds.getKeyFields()[0]));
 				location.setLongitude((double) rs.getValue(gds.getKeyFields()[1]));
 				datasetList.add(location);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JGeograph>>(
 				datasetList) {
 		}, callback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format statistics array
 	 */
 	@GET
 	@Path("/getstads/{datasetname}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getStaDataset(
 			@PathParam("datasetname") String dataset,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getStaDataset " + dataset + " "
 				+ uriInfo.getAbsolutePath());
 		List<JStatistic> datasetList = new ArrayList<JStatistic>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(dataset);
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
 				ArrayList<String> keys = new ArrayList<>();
 				for (DataField key : gds.getKeyFields()) {
 					keys.add(rs.getValue(key).toString());
 				}
 				ArrayList<Double> values = new ArrayList<>();
 				for (DataField value : gds.getValueFields()) {
 					values.add(Double.valueOf(rs.getValue(value).toString()));
 				}
 				statistic.setKey(keys);
 				statistic.setValue(values);
 				datasetList.add(statistic);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JStatistic>>(
 				datasetList) {
 		}, callback);
 	}
 
 	/**
 	 * get the location fields form the dataset
 	 * 
 	 * @param dataset
 	 * @return a json or xml format statistics array
 	 */
 	@GET
 	@Path("/getstatds/{datasetname}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getStaTimeDataset(
 			@PathParam("datasetname") String dataset,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getStaTimeDataset " + dataset + " "
 				+ uriInfo.getAbsolutePath());
 		List<JStatistic> datasetList = new ArrayList<JStatistic>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(dataset);
 			DataContent rs = ds.getQuery();
 			DataFeature gds = ds.getFeature(DataFeatureType.TimeSeriesFeature);
 			// TimeSeriesFeature gds = (TimeSeriesFeature) ds
 			// .getFeature(TimeSeriesFeature.class);
 			if (gds == null)
 				throw new OperationNotSupportedException(
 						"can't find the statisticTime Exception");
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				System.out.println(rs.getValue(ds.getDataFields().get(0))
 						.toString()
 						+ " "
 						+ rs.getValue(ds.getDataFields().get(1)).toString());
 				JStatistic statistic = new JStatistic();
 				ArrayList<String> keys = new ArrayList<>();
 				for (DataField key : gds.getKeyFields()) {
 					keys.add(rs.getValue(key).toString());
 				}
 				ArrayList<Double> values = new ArrayList<>();
 				for (DataField value : gds.getValueFields()) {
 					values.add(Double.valueOf(rs.getValue(value).toString()));
 				}
 				statistic.setKey(keys);
 				statistic.setValue(values);
 				datasetList.add(statistic);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JStatistic>>(
 				datasetList) {
 		}, callback);
 	}
 
 	/**
 	 * get all of the dataset's fields
 	 * 
 	 * @param dataset
 	 * @return a list of all fields name
 	 */
 	@GET
 	@Path("/getdsfds/{datasetname}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetFieldsNames(
 			@PathParam("datasetname") String dataset,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getDatasetFieldsNames " + dataset + " "
 				+ uriInfo.getAbsolutePath());
 		List<JFieldName> all_fn = new ArrayList<JFieldName>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			DataSet ds = datasetManager.getDataSet(dataset);
 			DataField[] dfs = ds.getDataFields().toArray(new DataField[0]);
 			for (DataField df : dfs) {
 				JFieldName jfn = new JFieldName();
 				jfn.setFieldName(df.getName());
 				jfn.setDescription(df.getDescription());
 				jfn.setIsKey(df.isKey());
 				jfn.setType(df.getFieldType().name());
 				all_fn.add(jfn);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 
 		}
 		return new JSONWithPadding(new GenericEntity<List<JFieldName>>(all_fn) {
 		}, callback);
 	}
 
 	/**
 	 * get all dataviews
 	 * 
 	 * @param callback
 	 * @return
 	 */
 	@GET
 	@Path("/getdvs")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDatasetViewsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getDatasetViewsNames " + uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataView> datasets = datasetManager.getDataViewList();
 			int i = 0;
 			for (DataView dataset : datasets) {
 				// if(i++>=1)
 				// break;
 				JDatasetName dname = new JDatasetName();
 				dname.setDatasetName(dataset.getName());
 				dname.setDescription(dataset.getDescription());
 				ArrayList<String> schema = new ArrayList<>();
 				for (DataField df : dataset.getAllFields()) {
 					schema.add(df.getName());
 				}
 				dname.setSchema(schema);
 				datasetsName.add(dname);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, callback);
 
 	}
 
 	/**
 	 * get Statistic dataview
 	 * 
 	 * @param callback
 	 * @return
 	 */
 	@GET
 	@Path("/getstadvs")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getStaDatasetViewsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getStaDatasetViewsNames "
 				+ uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataView> datasets = datasetManager
 					.getDataViewList(DataFeatureType.DistributionFeature);
 			int i = 0;
 			for (DataView dataset : datasets) {
 				// if(i++>=1)
 				// break;
 				JDatasetName dname = new JDatasetName();
 				dname.setDatasetName(dataset.getName());
 				dname.setDescription(dataset.getDescription());
 				ArrayList<String> schema = new ArrayList<>();
 				for (DataField df : dataset.getAllFields()) {
 					schema.add(df.getName());
 				}
 				dname.setSchema(schema);
 				datasetsName.add(dname);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, callback);
 
 	}
 
 	/**
 	 * get all geo dataviews
 	 * 
 	 * @param callback
 	 * @return
 	 */
 	@GET
 	@Path("/getgeodvs")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getGeoDatasetViewsNames(
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getGeoDatasetViewsNames "
 				+ uriInfo.getAbsolutePath());
 		List<JDatasetName> datasetsName = new ArrayList<JDatasetName>();
 		JDatasetName datasetName = new JDatasetName();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
 			Collection<DataView> datasets = datasetManager
 					.getDataViewList(DataFeatureType.GeoFeature);
 			int i = 0;
 			for (DataView dataset : datasets) {
 				// if(i++>=1)
 				// break;
 				JDatasetName dname = new JDatasetName();
 				dname.setDatasetName(dataset.getName());
 				dname.setDescription(dataset.getDescription());
 				ArrayList<String> schema = new ArrayList<>();
 				for (DataField df : dataset.getAllFields()) {
 					schema.add(df.getName());
 				}
 				dname.setSchema(schema);
 				datasetsName.add(dname);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDatasetName>>(
 				datasetsName) {
 		}, callback);
 
 	}
 	@GET
 	@Path("/getdv/{dataviewname}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public JSONWithPadding getDataview(@PathParam("dataviewname") String dataview,
 			@QueryParam("jsoncallback") @DefaultValue("fn") String callback) {
 		System.out.println("getDataset " + dataview + " "
 				+ uriInfo.getAbsolutePath());
 		List<JDataset> datasetList = new ArrayList<>();
 		try {
 			Platform p = (Platform) servletcontext.getAttribute("platform");
 			DataSetManager datasetManager = p.getDataSetManager();
			DataView ds = datasetManager.getDataView(dataview);
 			DataContent rs = ds.getQuery();
 			rs.open();
 			int i = 0;
 			while (rs.next() && i++ < 20) {
 				JDataset jdataset = new JDataset();
 				List<JField> fields = new ArrayList<>();
				DataField[] dfs = ds.getAllFields();
 				int j = 0;
 				for (DataField df : dfs) {
 					// if(j++>=2)
 					// break;
 					JField field = new JField();
 					field.setField(rs.getValue(df));
 					fields.add(field);
 				}
 				jdataset.setField(fields);
 				datasetList.add(jdataset);
 			}
 			rs.close();
 		} catch (OperationNotSupportedException | DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new JSONWithPadding(new GenericEntity<List<JDataset>>(
 				datasetList) {
 		}, callback);
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
 			@QueryParam("jsoncallback") String callback,
 			@QueryParam("acronym") String acronym,
 			@QueryParam("title") String title,
 			@QueryParam("competition") String competition) {
 		String a = "{\"city\":\"Beijing\",\"street\":\" Chaoyang Road \",\"postcode\":100025}";
 		System.out.println(a);
 		return new JSONWithPadding(a, callback);
 	}
 }
