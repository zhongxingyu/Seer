 package org.ocha.hdx.rest;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.security.PermitAll;
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.glassfish.jersey.server.mvc.Viewable;
 import org.ocha.hdx.persistence.entity.curateddata.Entity;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator.Periodicity;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.rest.helper.BubbleChartConfigurer;
 import org.ocha.hdx.rest.helper.IndicatorAndSourceChartConfigurer;
 import org.ocha.hdx.service.CuratedDataService;
 import org.ocha.hdx.service.ExporterService;
 import org.ocha.hdx.tools.GSONBuilderWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.google.visualization.datasource.base.TypeMismatchException;
 import com.google.visualization.datasource.datatable.DataTable;
 import com.google.visualization.datasource.render.CsvRenderer;
 import com.google.visualization.datasource.render.JsonRenderer;
 import com.ibm.icu.util.ULocale;
 
 @RolesAllowed({ "admin", "api" })
 @Path("/api")
 @Component
 public class APIResource {
 
 	private static Logger logger = LoggerFactory.getLogger(APIResource.class);
 
 	@Context
 	private HttpServletRequest request;
 
 	@Autowired
 	private CuratedDataService curatedDataService;
 
 	@Autowired
 	private ExporterService exporterService;
 
 	@GET
 	@Produces({ "text/csv" })
 	@Path("/yearly/source/{sourceCode}/indicatortype/{indicatorTypeCode}/csv")
 	public String getYearlyDataForSourceAndIndicatorTypeAsCSV(@PathParam("sourceCode") final String sourceCode, @PathParam("indicatorTypeCode") final String indicatorTypeCode,
 			@QueryParam("c") final List<String> countryCodes) throws TypeMismatchException {
 		final DataTable dataTable = curatedDataService.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, sourceCode, indicatorTypeCode, countryCodes);
 		final String result = CsvRenderer.renderDataTable(dataTable, ULocale.ENGLISH, ",").toString();
 		logger.debug("about to return from getYearlyDataForSourceAndIndicatorType");
 		logger.debug(result);
 
 		return result;
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/yearly/source/{sourceCode}/indicatortype/{indicatorTypeCode}/json")
 	public String getYearlyDataForSourceAndIndicatorType(@PathParam("sourceCode") final String sourceCode, @PathParam("indicatorTypeCode") final String indicatorTypeCode,
 			@QueryParam("c") final List<String> countryCodes) throws TypeMismatchException {
 		final DataTable dataTable = curatedDataService.listIndicatorsByPeriodicityAndSourceAndIndicatorType(Periodicity.YEAR, sourceCode, indicatorTypeCode, countryCodes);
 		final String result = JsonRenderer.renderDataTable(dataTable, true, false, false).toString();
 		logger.debug("about to return from getYearlyDataForSourceAndIndicatorType");
 		logger.debug(result);
 
 		return result;
 	}
 
 	/**
 	 * The actual data is fetched in a separate call
 	 * 
 	 * @see #getYearlyDataForSourceAndIndicatorType
 	 */
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	@Path("/yearly/source/{sourceCode}/indicatortype/{indicatorTypeCode}/{chartType}/")
 	public Response getChartWithYearlyDataForSourceAndIndicatorType(@PathParam("sourceCode") final String sourceCode, @PathParam("indicatorTypeCode") final String indicatorTypeCode,
 			@PathParam("chartType") final String chartType) {
 
 		final IndicatorAndSourceChartConfigurer iascc = new IndicatorAndSourceChartConfigurer();
 
 		final Map<String, String> model = new HashMap<String, String>();
 		model.put("chartType", chartType);
 		final IndicatorType indicatorType = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode);
 		// FIXME we could have a display language here
 		model.put("title", indicatorType.getDisplayableTitle() + " according to " + curatedDataService.getSourceByCode(sourceCode).getName().getDefaultValue());
 		if ("BarChart".equals(chartType)) {
 			model.put("vAxisTitle", "year");
 			model.put("hAxisTitle", indicatorType.getName().getDefaultValue());
 		} else if ("ColumnChart".equals(chartType) || "AreaChart".equals(chartType)) {
 			model.put("vAxisTitle", indicatorType.getName().getDefaultValue());
 			model.put("hAxisTitle", "year");
 		}
 
 		iascc.setModel(model);
 		iascc.setIndicatorTypes(curatedDataService.listIndicatorTypes());
 		iascc.setSources(curatedDataService.getExistingSourcesForIndicatorType(indicatorTypeCode));
 		iascc.setSource(sourceCode);
 		iascc.setIndicatorType(indicatorTypeCode);
 
 		return Response.ok(new Viewable("/analytical/IndicatorAndSourceChart", iascc)).build();
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/yearly/year/{year}/source/{sourceCode}/indicatortype/{indicatorTypeCode}/json")
 	public String getDataForYearAndSourceAndIndicatorType(@PathParam("year") final int year, @PathParam("sourceCode") final String sourceCode,
 			@PathParam("indicatorTypeCode") final String indicatorTypeCode) throws TypeMismatchException {
 		final DataTable dataTable = curatedDataService.listIndicatorsByYearAndSourceAndIndicatorType(year, sourceCode, indicatorTypeCode);
 		final String result = JsonRenderer.renderDataTable(dataTable, true, false, false).toString();
 
 		logger.debug("about to return from getDataForYearAndSourceAndIndicatorType");
 		logger.debug(result);
 
 		return result;
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	@Path("/yearly/year/{year}/source/{sourceCode}/indicatortype/{indicatorTypeCode}/{chartType}")
 	public Response getChartForYearAndSourceAndIndicatorType(@PathParam("sourceCode") final String sourceCode, @PathParam("indicatorTypeCode") final String indicatorTypeCode,
 			@PathParam("chartType") final String chartType) throws TypeMismatchException {
 
 		final Map<String, String> model = new HashMap<String, String>();
 		model.put("chartType", chartType);
 		final IndicatorType indicatorType = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode);
 		model.put("title", indicatorType.getDisplayableTitle());
 		return Response.ok(new Viewable("/analytical/charts", model)).build();
 	}
 
 	@GET
 	@Produces({ "text/csv" })
 	@Path("/yearly/entity/{entityType}/{entityCode}/indicatortype/{indicatorTypeCode}/csv")
 	public String getYearlyDataForEntityAndIndicatorTypeAsCSV(@PathParam("entityType") final String entityType, @PathParam("entityCode") final String entityCode,
 			@PathParam("indicatorTypeCode") final String indicatorTypeCode) throws TypeMismatchException {
 		final DataTable dataTable = curatedDataService.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, entityType, entityCode, indicatorTypeCode);
 		final String result = CsvRenderer.renderDataTable(dataTable, ULocale.ENGLISH, ",").toString();
 		logger.debug("about to return from getYearlyDataForSourceAndIndicatorType");
 		logger.debug(result);
 
 		return result;
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/yearly/entity/{entityType}/{entityCode}/indicatortype/{indicatorTypeCode}/json")
 	public String getYearlyDataForEntityAndIndicatorType(@PathParam("entityType") final String entityType, @PathParam("entityCode") final String entityCode,
 			@PathParam("indicatorTypeCode") final String indicatorTypeCode) throws TypeMismatchException {
 		final DataTable dataTable = curatedDataService.listIndicatorsByPeriodicityAndEntityAndIndicatorType(Periodicity.YEAR, entityType, entityCode, indicatorTypeCode);
 		final String result = JsonRenderer.renderDataTable(dataTable, true, false, false).toString();
 		logger.debug("about to return from getYearlyDataForSourceAndIndicatorType");
 		logger.debug(result);
 
 		return result;
 	}
 
 	/**
 	 * The actual data is fetched in a separate call
 	 * 
 	 * @see #getYearlyDataForSourceAndIndicatorType
 	 */
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	@Path("/yearly/entity/{entityType}/{entityCode}/indicatortype/{indicatorTypeCode}/{chartType}")
 	public Response getChartWithYearlyDataForEntityAndIndicatorType(@PathParam("entityType") final String entityType, @PathParam("entityCode") final String entityCode,
 			@PathParam("indicatorTypeCode") final String indicatorTypeCode, @PathParam("chartType") final String chartType) {
 		final Map<String, String> model = new HashMap<String, String>();
 		model.put("chartType", chartType);
 		final IndicatorType indicatorType = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode);
 		final Entity entity = curatedDataService.getEntityByCodeAndType(entityCode, entityType);
 		model.put("title", indicatorType.getDisplayableTitle() + " for " + entity.getName());
 		if ("BarChart".equals(chartType)) {
 			model.put("vAxisTitle", "year");
 			model.put("hAxisTitle", indicatorType.getName().getDefaultValue());
 		} else if ("ColumnChart".equals(chartType) || "AreaChart".equals(chartType)) {
 			model.put("vAxisTitle", indicatorType.getName().getDefaultValue());
 			model.put("hAxisTitle", "year");
 		}
 		return Response.ok(new Viewable("/analytical/charts", model)).build();
 	}
 
 	// @GET
 	// @Produces({ "text/csv" })
 	// @Path("/yearly/year/{year}/source1/{sourceCode1}/source2/{sourceCode2}/source3/{sourceCode3}/indicatortype1/{indicatorTypeCode1}/indicatortype2/{indicatorTypeCode2}/indicatortype3/{indicatorTypeCode3}/csv")
 	// public String
 	// getDataForYearAndSourceAndIndicatorTypesAsCSV(@PathParam("year") final
 	// int year, @PathParam("sourceCode") final String sourceCode,
 	// @PathParam("indicatorTypeCode1") final String indicatorTypeCode1,
 	// @PathParam("indicatorTypeCode2") final String indicatorTypeCode2) throws
 	// TypeMismatchException {
 	// List<String> indicatorTypes = new ArrayList<>();
 	// indicatorTypes.add(indicatorTypeCode1);
 	// indicatorTypes.add(indicatorTypeCode2);
 	//
 	// final DataTable dataTable =
 	// curatedDataService.listIndicatorsByYearAndSourcesAndIndicatorTypes(year,
 	// sourceCode, indicatorTypes);
 	//
 	// final String result = CsvRenderer.renderDataTable(dataTable,
 	// ULocale.ENGLISH, ",").toString();
 	//
 	// logger.debug("about to return from getDataForYearAndSourceAndIndicatorType");
 	// logger.debug(result);
 	//
 	// return result;
 	// }
 
 	// @GET
 	// @Produces(MediaType.APPLICATION_JSON)
 	// @Path("/yearly/year/{year}/source/{sourceCode}/indicatortype1/{indicatorTypeCode1}/indicatortype2/{indicatorTypeCode2}/json")
 	// public String getDataForYearAndSourceAndIndicatorTypes(@PathParam("year")
 	// final int year, @PathParam("sourceCode") final String sourceCode,
 	// @PathParam("indicatorTypeCode1") final String indicatorTypeCode1,
 	// @PathParam("indicatorTypeCode2") final String indicatorTypeCode2) throws
 	// TypeMismatchException {
 	// List<String> indicatorTypes = new ArrayList<>();
 	// indicatorTypes.add(indicatorTypeCode1);
 	// indicatorTypes.add(indicatorTypeCode2);
 	//
 	// final DataTable dataTable =
 	// curatedDataService.listIndicatorsByYearAndSourceAndIndicatorTypes(year,
 	// sourceCode, indicatorTypes);
 	//
 	// final String result = JsonRenderer.renderDataTable(dataTable, true,
 	// false, false).toString();
 	//
 	// logger.debug("about to return from getDataForYearAndSourceAndIndicatorType");
 	// logger.debug(result);
 	//
 	// return result;
 	// }
 
 	// @GET
 	// @Produces(MediaType.TEXT_HTML)
 	// @Path("/yearly/year/{year}/source1/{sourceCode1}/indicatortype1/{indicatorTypeCode1}/source2/{sourceCode2}/indicatortype2/{indicatorTypeCode2}/source3/{sourceCode3}/indicatortype3/{indicatorTypeCode3}/{chartType}")
 	// public Response getChartForYearAndSourceAndIndicatorTypes(@PathParam("sourceCode1") final String sourceCode1,
 	// @PathParam("indicatorTypeCode1") final String indicatorTypeCode1,
 	// @PathParam("sourceCode2") final String sourceCode2, @PathParam("indicatorTypeCode2") final String indicatorTypeCode2,
 	// @PathParam("sourceCode3") final String sourceCode3,
 	// @PathParam("indicatorTypeCode3") final String indicatorTypeCode3, @PathParam("chartType") final String chartType) throws
 	// TypeMismatchException {
 	//
 	// final Map<String, String> model = new HashMap<String, String>();
 	// model.put("chartType", chartType);
 	// // FIXME Build a title based on the different indicator types
 	// final IndicatorType indicatorType = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode1);
 	// model.put("title", indicatorType.getDisplayableTitle());
 	// return Response.ok(new Viewable("/analytical/charts", model)).build();
 	// }
 
 	@GET
 	@Produces({ "text/csv" })
 	@Path("/yearly/year/{year}/source1/{sourceCode1}/indicatortype1/{indicatorTypeCode1}/source2/{sourceCode2}/indicatortype2/{indicatorTypeCode2}/source3/{sourceCode3}/indicatortype3/{indicatorTypeCode3}/csv")
 	public String getDataForYearAndSourceAnd3IndicatorTypesAsCSV(@PathParam("year") final int year, @PathParam("sourceCode1") final String sourceCode1,
 			@PathParam("indicatorTypeCode1") final String indicatorTypeCode1, @PathParam("sourceCode2") final String sourceCode2, @PathParam("indicatorTypeCode2") final String indicatorTypeCode2,
 			@PathParam("sourceCode3") final String sourceCode3, @PathParam("indicatorTypeCode3") final String indicatorTypeCode3, @QueryParam("c") final List<String> countryCodes)
 			throws TypeMismatchException {
 
 		final DataTable dataTable = curatedDataService.listIndicatorsByYearAndSourcesAndIndicatorTypes(year, sourceCode1, indicatorTypeCode1, sourceCode2, indicatorTypeCode2, sourceCode3,
 				indicatorTypeCode3, countryCodes);
 
 		final String result = CsvRenderer.renderDataTable(dataTable, ULocale.ENGLISH, ",").toString();
 
 		logger.debug(result);
 
 		return result;
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/yearly/year/{year}/source1/{sourceCode1}/indicatortype1/{indicatorTypeCode1}/source2/{sourceCode2}/indicatortype2/{indicatorTypeCode2}/source3/{sourceCode3}/indicatortype3/{indicatorTypeCode3}/json")
 	public String getDataForYearAndSourceAnd3IndicatorTypes(@PathParam("year") final int year, @PathParam("sourceCode1") final String sourceCode1,
 			@PathParam("indicatorTypeCode1") final String indicatorTypeCode1, @PathParam("sourceCode2") final String sourceCode2, @PathParam("indicatorTypeCode2") final String indicatorTypeCode2,
 			@PathParam("sourceCode3") final String sourceCode3, @PathParam("indicatorTypeCode3") final String indicatorTypeCode3, @QueryParam("c") final List<String> countryCodes)
 			throws TypeMismatchException {
 
 		final DataTable dataTable = curatedDataService.listIndicatorsByYearAndSourcesAndIndicatorTypes(year, sourceCode1, indicatorTypeCode1, sourceCode2, indicatorTypeCode2, sourceCode3,
 				indicatorTypeCode3, countryCodes);
 
 		final String result = JsonRenderer.renderDataTable(dataTable, true, false, false).toString();
 
 		logger.debug(result);
 
 		return result;
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	@Path("/yearly/year/{year}/source1/{sourceCode1}/indicatortype1/{indicatorTypeCode1}/source2/{sourceCode2}/indicatortype2/{indicatorTypeCode2}/source3/{sourceCode3}/indicatortype3/{indicatorTypeCode3}/{chartType}")
 	public Response getChartForYearAndSourceAnd3IndicatorTypes(@PathParam("sourceCode1") final String sourceCode1, @PathParam("indicatorTypeCode1") final String indicatorTypeCode1,
 			@PathParam("sourceCode2") final String sourceCode2, @PathParam("indicatorTypeCode2") final String indicatorTypeCode2, @PathParam("sourceCode3") final String sourceCode3,
 			@PathParam("indicatorTypeCode3") final String indicatorTypeCode3, @PathParam("chartType") final String chartType) throws TypeMismatchException {
 
 		final BubbleChartConfigurer bcc = new BubbleChartConfigurer();
 
 		final Map<String, String> model = new HashMap<String, String>();
 		model.put("chartType", chartType);
 		final IndicatorType indicatorType1 = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode1);
 		final IndicatorType indicatorType2 = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode2);
 		final IndicatorType indicatorType3 = curatedDataService.getIndicatorTypeByCode(indicatorTypeCode3);
 		model.put("hAxisTitle", indicatorType1.getDisplayableTitle());
 		model.put("vAxisTitle", indicatorType2.getDisplayableTitle());
 		model.put("title", indicatorType3.getDisplayableTitle());
 
 		bcc.setModel(model);
 		bcc.setIndicatorTypes(curatedDataService.listIndicatorTypes());
 		bcc.setSources(curatedDataService.listSources());
 		bcc.setSource1(sourceCode1);
 		bcc.setSource2(sourceCode2);
 		bcc.setSource3(sourceCode3);
 		bcc.setIndicatorType1(indicatorTypeCode1);
 		bcc.setIndicatorType2(indicatorTypeCode2);
 		bcc.setIndicatorType3(indicatorTypeCode3);
 		return Response.ok(new Viewable("/analytical/BubbleChart", bcc)).build();
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/sources/year/{year}/indicatorTypeCode/{indicatorTypeCode}/")
 	public String getExistingSourcesForYearAndIndicatorType(@PathParam("year") final int year, @PathParam("indicatorTypeCode") final String indicatorTypeCode) {
 		final List<Source> sources = curatedDataService.getExistingSourcesForYearAndIndicatorType(year, indicatorTypeCode);
 
 		return GSONBuilderWrapper.getGSON().toJson(sources);
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	@Path("/sources/indicatorTypeCode/{indicatorTypeCode}/")
 	public String getExistingSourcesForIndicatorType(@PathParam("indicatorTypeCode") final String indicatorTypeCode) {
 		final List<Source> sources = curatedDataService.getExistingSourcesForIndicatorType(indicatorTypeCode);
 
 		return GSONBuilderWrapper.getGSON().toJson(sources);
 	}
 
 	// //////////////////////
 	// Export functionalities
 	// //////////////////////
 
 	/**
 	 * Export a country-centric report.
 	 * 
 	 * @param countryCode
 	 *            The code of the country (e.g. BEL)
 	 * @param fromYear
 	 *            The year from which the data will be collected (e.g. 1998), inclusive
 	 * @param toYear
 	 *            The year to which the data will be collected (e.g. 2014), inclusive
 	 * @param language
 	 *            The language the report will be written into. TODO Not supported yet. All texts will be given in the default language.
 	 * @return A XSSF workbook containing the data as requested
 	 */
 	@GET
 	@Path("/exporter/country/xlsx/{countryCode}/fromYear/{fromYear}/toYear/{toYear}/language/{language}/{filename}.xlsx")
 	@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
 	@PermitAll
 	public XSSFWorkbook exportCountry_XLSX(@PathParam("countryCode") final String countryCode, @PathParam("fromYear") final Integer fromYear, @PathParam("toYear") final Integer toYear,
 			@PathParam("language") final String language) {
 		return exporterService.exportCountry_XLSX(countryCode, fromYear, toYear, language);
 	}
 
 	/**
 	 * Export an indicator-centric report.
 	 * 
 	 * @param indicatorTypeCode
 	 *            The code of the indicator (e.g. PVF020)
 	 * @param fromYear
 	 *            The year from which the data will be collected (e.g. 1998), inclusive
 	 * @param toYear
 	 *            The year to which the data will be collected (e.g. 2014), inclusive
 	 * @param language
 	 *            The language the report will be written into. TODO Not supported yet. All texts will be given in the default language.
 	 * @return A XSSF workbook containing the data as requested
 	 */
 	@GET
 	@Path("/exporter/indicator/xlsx/{indicatorTypeCode}/source/{sourceCode}/fromYear/{fromYear}/toYear/{toYear}/language/{language}/{filename}.xlsx")
 	@Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public XSSFWorkbook exportIndicator_XLSX(@PathParam("indicatorTypeCode") final String indicatorTypeCode, @PathParam("sourceCode") final String sourceCode, @PathParam("fromYear") final Long fromYear, @PathParam("toYear") final Long toYear,
			@PathParam("language") final String language) {
 		return exporterService.exportIndicator_XLSX(indicatorTypeCode, sourceCode, fromYear, toYear, language);
 	}
 
 }
