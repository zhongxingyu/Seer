 package gov.usgs.gdp.servlet;
 
 import gov.usgs.gdp.analysis.GridStatistics;
 import gov.usgs.gdp.analysis.GridStatisticsCSVWriter;
 import gov.usgs.gdp.analysis.GridStatisticsWriter;
 import gov.usgs.gdp.analysis.NetCDFUtility;
 import gov.usgs.gdp.analysis.StationDataCSVWriter;
 import gov.usgs.gdp.analysis.GridStatisticsCSVWriter.Statistic;
 import gov.usgs.gdp.bean.AckBean;
 import gov.usgs.gdp.bean.AvailableFilesBean;
 import gov.usgs.gdp.bean.EmailMessageBean;
 import gov.usgs.gdp.bean.ErrorBean;
 import gov.usgs.gdp.bean.MessageBean;
 import gov.usgs.gdp.bean.OutputStatisticsBean;
 import gov.usgs.gdp.bean.ShapeFileSetBean;
 import gov.usgs.gdp.bean.THREDDSInfoBean;
 import gov.usgs.gdp.bean.UploadFileCheckBean;
 import gov.usgs.gdp.bean.FileLocationBean;
 import gov.usgs.gdp.bean.XmlReplyBean;
 import gov.usgs.gdp.helper.EmailHandler;
 import gov.usgs.gdp.helper.FileHelper;
 import gov.usgs.gdp.servlet.FileProcessServlet.GroupBy.StationOption;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.FileDataStoreFinder;
 import org.geotools.data.shapefile.shp.ShapeType;
 import org.geotools.data.shapefile.shp.ShapefileWriter;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureCollections;
 import org.geotools.feature.SchemaException;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.filter.text.cql2.CQL;
 import org.geotools.filter.text.cql2.CQLException;
 import org.geotools.geometry.jts.JTSFactoryFinder;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.operation.TransformException;
 import org.xml.sax.SAXException;
 
 import thredds.catalog.InvAccess;
 import thredds.catalog.InvCatalog;
 import thredds.catalog.InvCatalogFactory;
 import thredds.catalog.ServiceType;
 import ucar.ma2.Array;
 import ucar.ma2.IndexIterator;
 import ucar.ma2.InvalidRangeException;
 import ucar.ma2.Range;
 import ucar.ma2.Section;
 import ucar.ma2.StructureData;
 import ucar.ma2.StructureMembers;
 import ucar.nc2.Dimension;
 import ucar.nc2.ProxyReader;
 import ucar.nc2.Variable;
 import ucar.nc2.VariableSimpleIF;
 import ucar.nc2.constants.FeatureType;
 import ucar.nc2.dataset.CoordinateAxis1D;
 import ucar.nc2.dataset.CoordinateAxis1DTime;
 import ucar.nc2.dataset.VariableDS;
 import ucar.nc2.dt.GridCoordSystem;
 import ucar.nc2.dt.GridDatatype;
 import ucar.nc2.dt.grid.GeoGrid;
 import ucar.nc2.dt.grid.GridDataset;
 import ucar.nc2.ft.FeatureDataset;
 import ucar.nc2.ft.FeatureDatasetFactoryManager;
 import ucar.nc2.ft.FeatureDatasetPoint;
 import ucar.nc2.ft.PointFeature;
 import ucar.nc2.ft.PointFeatureCollection;
 import ucar.nc2.ft.PointFeatureIterator;
 import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
 import ucar.nc2.units.DateRange;
 import ucar.nc2.util.CancelTask;
 import ucar.nc2.util.NamedObject;
 
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryCollection;
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 /**
  * Servlet implementation class FileProcessServlet
  */
 public class FileProcessServlet extends HttpServlet {
 	private static org.apache.log4j.Logger log = Logger.getLogger(FileProcessServlet.class);
     /**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Long start = new Date().getTime();
 		String command = request.getParameter("command");
 
 		if ("getoutputstats".equals(command)) {
 			OutputStatisticsBean outputStats = OutputStatisticsBean.getOutputStatisticsBean();
 			XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, outputStats);
 			RouterServlet.sendXml(xmlReply, start, response);
 			return;
 		}
 		
 		if ("submitforprocessing".equals(command)) {
 			
 			// Check to see if their user directory is still around.
 			if (!FileHelper.doesDirectoryOrFileExist(System.getProperty("applicationUserSpaceDir") + 
 					request.getParameter("userdirectory"))) {
				
				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_USER_DIRECTORY_DOES_NOT_EXIST)); 
				RouterServlet.sendXml(xmlOutput, start, response);
 			}
 				
 			FileHelper.createUserDirectory(System.getProperty("applicationUserSpaceDir"));
 			
 			FileLocationBean fileLocations = null;
 			try {
 				fileLocations = populateFileUpload(request);
 			} catch (InvalidRangeException e) {
 				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_BOX_NO_INTERSECT_GRID));
 				RouterServlet.sendXml(xmlOutput, start, response);
 				return;
 			} catch (AddressException e) {
 				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_EMAIL_ERROR_INCORRECT_ADDRESS));
 				RouterServlet.sendXml(xmlOutput, start, response);
 				return;
 			} catch (MessagingException e) {
 				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_EMAIL_ERROR));
 				RouterServlet.sendXml(xmlOutput, start, response);
 				return;
 			} catch (Exception e) {
 				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(e.getMessage()));
 				RouterServlet.sendXml(xmlOutput, start, response);
                 e.printStackTrace();
 				return;
             }
 
 			// We are, for the moment, assuming there is a file at this location
 			// The link is sent out as just the file name. When the user sends the request
 			// back, we go to the directory at String baseFilePath = System.getProperty("applicationTempDir");
 			// + the file specified by the user ((fileForUpload.getName()) and we send that
 			// back to the user
 			XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, fileLocations);
 			RouterServlet.sendXml(xmlReply, start, response);
 			return;
 		}
 
 		if ("checkuploadfile".equals(command)) {
 			String file = request.getParameter("file");
 			String baseFilePath = System.getProperty("applicationTempDir");
 	    	baseFilePath = baseFilePath + FileHelper.getSeparator();
 	    	String fullFilePath = baseFilePath + "upload-repository" +FileHelper.getSeparator()+file;
 	    	boolean fileExists = FileHelper.doesDirectoryOrFileExist(fullFilePath);
 	    	boolean hasBytes = false;
 	    	File tempFile = new File(fullFilePath);
 	    	hasBytes = tempFile.length() > 0;
 	    	boolean fileExistsAndHasBytes = fileExists & hasBytes;
 	    	UploadFileCheckBean ufcb = new UploadFileCheckBean(file, fileExistsAndHasBytes);
 
 			XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, ufcb);
 			RouterServlet.sendXml(xmlReply, start, response);
 			return;
 		}
 
 		// User wishes to grab a file. Send this file if available.
 		if ("getfile".equals(command)) {
 			String file = request.getParameter("file");
 			String baseFilePath = System.getProperty("applicationTempDir");
 	    	baseFilePath = baseFilePath + FileHelper.getSeparator();
 	    	String fullFilePath = baseFilePath + "upload-repository" +FileHelper.getSeparator()+file;
 	    	File fileToUpload = null;
 
 	    	if (!FileHelper.doesDirectoryOrFileExist(fullFilePath)) {
 				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
 				RouterServlet.sendXml(xmlOutput, start, response);
 				return;
 	    	}
 	    	fileToUpload = new File(fullFilePath);
 
 	    	// Set the headers.
 	    	response.setContentType("application/x-download");
 	    	response.setHeader("Content-Disposition", "attachment; filename=" + fileToUpload.getName());
 	    	response.setCharacterEncoding("UTF-8");
 
 	    	// Send the file.
 	    	ServletOutputStream out = null;
 	    	BufferedInputStream buf = null;
 	    	try {
 		    	out = response.getOutputStream();
 		    	response.setContentLength((int) fileToUpload.length());
 		    	FileInputStream input = new FileInputStream(fileToUpload);
 		    	buf = new BufferedInputStream(input);
 		    	int readBytes = 0;
 		    	while ((readBytes = buf.read()) != -1) out.write(readBytes);
 		    	out.close();
 		    	buf.close();
 	    	} catch (IOException ioe) {
 	    	      throw new ServletException(ioe.getMessage());
 	    	} finally {
 	    		if (out != null)
 	    			out.close();
 	    	      if (buf != null)
 	    	    	  buf.close();
 	    	}
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	    doGet(request, response);
 	}
 
 	@SuppressWarnings("unused")
 	private final static class ShapedGridReader implements ProxyReader {
 
         private GeoGrid grid;
         private Geometry shape;
         private GeometryFactory geometryFactory;
         private CoordinateAxis1D xAxis;
         private CoordinateAxis1D yAxis;
         private double[][] percentOfShapeOverlappingCells;
 
 	public ShapedGridReader(GeoGrid gridParam, Geometry shapeParam) {
             this.grid = gridParam;
             this.shape = shapeParam;
             this.geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
 
             GridCoordSystem gcs = this.grid.getCoordinateSystem();
             this.xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
             this.yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();
 
             this.percentOfShapeOverlappingCells = new double[(int) this.xAxis.getSize()][(int) this.yAxis.getSize()];
             for (int x = 0; x < this.xAxis.getSize(); ++x) {
                 for (int y = 0; y < this.yAxis.getSize(); ++y) {
                     this.percentOfShapeOverlappingCells[x][y] = Double.NaN;
                 }
             }
         }
 
         private double getPercentOfShapeOverlappingCell(int xIndex, int yIndex) {
             if (!Double.isNaN(this.percentOfShapeOverlappingCells[xIndex][yIndex])) {
                 return this.percentOfShapeOverlappingCells[xIndex][yIndex];
             }
 
             double[] xCellEdges = this.xAxis.getCoordEdges(xIndex);
             double[] yCellEdges = this.yAxis.getCoordEdges(yIndex);
 
             Envelope envelope = new Envelope(xCellEdges[0], xCellEdges[1], yCellEdges[0], yCellEdges[1]);
             Geometry cellRectangle = this.geometryFactory.toGeometry(envelope);
 
             Geometry intersection = cellRectangle.intersection(this.shape);
             this.percentOfShapeOverlappingCells[xIndex][yIndex] = intersection.getArea() / this.shape.getArea();
             return this.percentOfShapeOverlappingCells[xIndex][yIndex];
         }
 
         @Override
         public Array read(Variable mainv, CancelTask cancelTask) throws IOException {
             if (!(mainv instanceof VariableDS) || ((VariableDS) mainv).getOriginalVariable() != this.grid.getVariable()) {
                 throw new RuntimeException("mainv is not a proxy for the grid's underlying Variable.");
             }
 
             Array data = this.grid.getVariable().read();
 
             Dimension xDim = this.grid.getXDimension();
             Dimension yDim = this.grid.getYDimension();
             List<Dimension> varDims = this.grid.getDimensions();
 
             int xDimIndex = varDims.indexOf(xDim);  // The index of the X dimension in grid's list of dimensions.
             int yDimIndex = varDims.indexOf(yDim);  // The index of the Y dimension in grid's list of dimensions.
 
             for (IndexIterator indexIter = data.getIndexIterator(); indexIter.hasNext();) {
                 indexIter.next();
                 int[] iterPos = indexIter.getCurrentCounter();  // The position of indexIter in data.
                 int xDimPos = iterPos[xDimIndex];               // The X component of iterPos.
                 int yDimPos = iterPos[yDimIndex];               // The Y component of iterPos.
 
                 double percentOfShapeOverlappingCell = getPercentOfShapeOverlappingCell(xDimPos, yDimPos);
                 if (percentOfShapeOverlappingCell == 0) {
                     indexIter.setDoubleCurrent(-999.0);
                 }
             }
 
             return data;
         }
 
         @Override
         public Array read(Variable mainv, Section section, CancelTask cancelTask)
                 throws IOException, InvalidRangeException {
             if (!(mainv instanceof VariableDS) || ((VariableDS) mainv).getOriginalVariable() != this.grid.getVariable()) {
                 throw new RuntimeException("mainv is not a proxy for the grid's underlying Variable.");
             }
 
             Array data = this.grid.getVariable().read(section);
 
             Dimension xDim = this.grid.getXDimension();
             Dimension yDim = this.grid.getYDimension();
             List<Dimension> varDims = this.grid.getDimensions();
 
             int xDimIndex = varDims.indexOf(xDim);  // The index of the X dimension in grid's list of dimensions.
             int yDimIndex = varDims.indexOf(yDim);  // The index of the Y dimension in grid's list of dimensions.
 
             int[] origin = section.getOrigin();
             int xDimOffset = origin[xDimIndex];
             int yDimOffset = origin[yDimIndex];
 
             for (IndexIterator indexIter = data.getIndexIterator(); indexIter.hasNext();) {
                 indexIter.next();
                 int[] iterPos = indexIter.getCurrentCounter();  // The position of indexIter in data.
                 int xDimPos = xDimOffset + iterPos[xDimIndex];  // The X component of iterPos.
                 int yDimPos = yDimOffset + iterPos[yDimIndex];  // The Y component of iterPos.
 
                 double percentOfShapeOverlappingCell = getPercentOfShapeOverlappingCell(xDimPos, yDimPos);
                 if (percentOfShapeOverlappingCell == 0) {
                     indexIter.setFloatCurrent(-999f);
                 }
             }
 
             return data;
         }
     }
 
 
 	private FileLocationBean populateFileUpload(HttpServletRequest request)
             throws IOException, InvalidRangeException, AddressException,
             MessagingException, FactoryException, TransformException,
             org.opengis.coverage.grid.InvalidRangeException, SchemaException {
         String email = request.getParameter("email");
         String finalUrlEmail = request.getParameter("finalurlemail");
 
         // Create a File Which represents the output we are looking for.
         FileLocationBean uploadFiles = populateSummary(request);
 
         // If user specified an E-Mail address, send an E-Mail to the user with the provided link
         if (email != null && !"".equals(email)) sendEmail(email, finalUrlEmail);
 
         // Set that file as the result to be returned to the calling function
         // switch uploadDirectory return statement with the file we are looking for
         return uploadFiles;
     }
 
 	////  START - MOVEME
 	// IVAN, move this out where ever you see fit... values in this enum should
 	// be reported-to/used-by front end in some manner...  right now, too much loose
 	// coupling.
 	public enum DelimiterOption {
 	    c("[comma]", ","),
         t("[tab]", "\t"),
         s("[space]", " ");
 	    public final String description;
 	    public final String delimiter;
 	    private DelimiterOption(String description, String value) {
 	        this.description = description;
 	        this.delimiter = value;
 	    }
 	    public static DelimiterOption getDefault() { return c; }
 	    @Override public String toString() { return description; }
 	}
 
 	public static class GroupBy {
 	    public enum StationOption {
 	        station("Station"),
 	        variable("Variable");
 	        public final String description;
 	        private StationOption(String description) {
 	            this.description = description;
 	        }
 	        @Override public String toString() { return description; }
 	        public static StationOption getDefault() { return station; }
 	    }
 	    public enum GridOption {
 	        attributes("Attributes"),
 	        statistics("Statistics");
 	        public final String description;
 	        private GridOption(String description) {
                 this.description = description;
             }
             public static GridOption getDefault() { return attributes; }
 	    }
 	}
     ////  END - MOVEME
 
 	private FileLocationBean populateSummary(HttpServletRequest request) throws IOException, InvalidRangeException, FactoryException, TransformException, org.opengis.coverage.grid.InvalidRangeException, SchemaException {
 
 	    String shapeSet = request.getParameter("shapeset");
 	    String attribute = request.getParameter("attribute");
 	    String[] features = request.getParameterValues("feature");
 	    String[] outputStats = request.getParameterValues("outputstat");
 	    String dataset = request.getParameter("dataset");
 	    String[] dataTypes = request.getParameterValues("datatype");
 	    String from = request.getParameter("from");
 	    String to = request.getParameter("to");
 	    String output = request.getParameter("outputtype");
 	    String outputFile = request.getParameter("outputfile");
 	    String userDirectory = request.getParameter("userdirectory");
 	    String groupById	= request.getParameter("groupby");
 	    String delimId	= request.getParameter("delim");
 	    String lat = request.getParameter("lat");
 	    String lon = request.getParameter("lon");
 
 
 	    FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
 
 	    String baseFilePath = System.getProperty("applicationTempDir");
         String userspacePath = System.getProperty("applicationUserSpaceDir");
     	baseFilePath = baseFilePath + FileHelper.getSeparator();
     	File uploadDirectory = FileHelper.createFileRepositoryDirectory(baseFilePath);
     	if (!uploadDirectory.exists()) return null;
     	
 
     	String attributeName = attribute;
     	
     	String shapefilePath = null;
 
 	    if (lat != null && lon != null) {
 	    	
 	    	// Get reachcode containing lat/lon point from the EPA WATERS web service
 	    	InputStream reachJson = sendPacket(new URL("http://iaspub.epa.gov/waters10/waters_services.PointIndexingService?" +
 	    			"pGeometry=POINT(" + lon + "%20" + lat + ")" + "&pGeometryMod=WKT,SRID=8307" +
 	                "&pPointIndexingMethod=RAINDROP" + "&pPointIndexingRaindropDist=25"),
     				"GET", null, null, new String[]{});
 
 	    	String reachCode = parseJSON(reachJson, "reachcode");
 
 	    	// Get geometry of reachcode
     		InputStream json = sendPacket(new URL("http://iaspub.epa.gov/waters10/waters_services.navigationDelineationService?" +
     				"pNavigationType=UT&pStartReachCode=" + reachCode + "&optOutGeomFormat=GEOGML&pFeatureType=CATCHMENT_TOPO&pMaxDistance=999999999"),
     				"GET", null, null, new String[]{});// "pNavigationType", "UM", "pStartReachcode", reachCode, "optOutGeomFormat", "GEOGML");
 
     		String gml = parseJSON(json, "shape");
 
     		attributeName = "blah";
     		
     		String fullUserDir = System.getProperty("applicationUserSpaceDir") +
     				userDirectory + FileHelper.getSeparator();
     		
     		
     		try {
     			GeometryCollection g = parseGML(gml);
     			
     			// Write to a shapefile so GeoServer can load the geometry
     			shapefilePath = fullUserDir + "latlon.shp";
     			
     			File shpFile = new File(shapefilePath);
     			File shxFile = new File(fullUserDir + "latlon.shx");
     			
     			if (shpFile.exists()) shpFile.delete();
     			if (shxFile.exists()) shxFile.delete();
     			
     			shpFile.createNewFile();
     			shxFile.createNewFile();
     			
     			FileOutputStream shpFileInputStream = new FileOutputStream(shpFile);
     			FileOutputStream shxFileInputStream = new FileOutputStream(shxFile);
     			ShapefileWriter sw = new ShapefileWriter(shpFileInputStream.getChannel(), 
     					shxFileInputStream.getChannel());
     			sw.write(g, ShapeType.POLYGON);
     			
     			featureCollection = createFeatureCollection(g);
     			
 			} catch (SAXException e) {
 				e.printStackTrace();
 				return null;
 			} catch (ParserConfigurationException e) {
 				e.printStackTrace();
 				return null;
 			}
 
     	} else {
 
     		FileHelper.deleteFile(uploadDirectory.getPath() + outputFile);
 
 
     		FileDataStore shapeFileDataStore;
     		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;
 
     		// Set up the shapefile
     		String appTempDir = System.getProperty("applicationTempDir");
     		String userDir = userDirectory;
     		if (userDir != null && !"".equals(userspacePath + userDir)) {
     			if (FileHelper.doesDirectoryOrFileExist(userspacePath + userDir)) {
     				FileHelper.updateTimestamp(userspacePath + userDir, false); // Update the timestamp
                                 userDir = userspacePath + userDir;
     			} else {
     				userDir = "";
     			}
     		}
     		AvailableFilesBean afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDir);
     		List<ShapeFileSetBean> shapeBeanList = afb.getShapeSetList();
     		File shapeFile = null;
     		for (ShapeFileSetBean sfsb : shapeBeanList) {
     			if (shapeSet.equals(sfsb.getName())) {
     				shapeFile = sfsb.getShapeFile();
     				shapefilePath = shapeFile.getAbsolutePath();
     			}
     		}
 
     		shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFile);
     		featureSource = shapeFileDataStore.getFeatureSource();
 
     		if (features[0].equals("*")) {
     			featureCollection = featureSource.getFeatures();
     		} else {
     			//Implementing a filter using the CQL language
     			// http://docs.codehaus.org/display/GEOTOOLS/CQL+Parser+Design
     			String cqlQuery = attribute + " == '" + features[0] + "'";
     			Filter attributeFilter = null;
     			for (int index = 1;index < features.length;index++) {
     				cqlQuery = cqlQuery + " OR " + attribute + " == '" + features[index] + "'";
     			}
 
     			try {
     				attributeFilter = CQL.toFilter(cqlQuery);
     			} catch (CQLException e) {
     				log.debug(e);
     			}
     			featureCollection = featureSource.getFeatures(
     					new DefaultQuery(
     							featureSource.getSchema().getTypeName(),
     							attributeFilter
     					)
     			);
     		}
     	}
 
 
 	    DelimiterOption delimiterOption = null;
 	    if (delimId != null) {
 	        try {
 	            delimiterOption = DelimiterOption.valueOf(delimId);
 	        } catch (IllegalArgumentException e) { /* failure handled below */}
 	    }
 	    if (delimiterOption == null) {
 	        delimiterOption = DelimiterOption.getDefault();
 	    }
 
 
 	    String fromTime = from;
 		String toTime = to;
 
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         df.setTimeZone(TimeZone.getTimeZone("UTC"));
 		Date toDate = new Date();
 		Date fromDate = new Date();
 		boolean parsedDates = false;
 		if (toTime == null || fromTime == null) {
 			toDate = null;
 			fromDate = null;
 			parsedDates = true;
 		} else {
 			try {
 				toDate = df.parse(toTime);
 				fromDate = df.parse(fromTime);
 				parsedDates = true;
 			} catch (ParseException e1) {
 				parsedDates = false;
 				log.debug(e1.getMessage());
 			}
 		}
 
 		if (!parsedDates) {
 			// return some sort of error
 		}
 
 
 		String datasetUrl = dataset;
 		Formatter errorLog = new Formatter();
 		FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(FeatureType.ANY, datasetUrl, null, errorLog);
 
 		if (featureDataset.getFeatureType() == FeatureType.GRID && featureDataset instanceof GridDataset) {
 			GridDataset gridDataset = (GridDataset)featureDataset;
 			String gridName = dataTypes[0];
 			try {
 				GridDatatype gdt = gridDataset.findGridByName(gridName);
 				Range timeRange = null;
 				try {
 					CoordinateAxis1DTime timeAxis = gdt.getCoordinateSystem().getTimeAxis1D();
 					int timeIndexMin = 0;
 					int timeIndexMax = 0;
 					if (fromDate != null && toDate != null) {
 						timeIndexMin = timeAxis.findTimeIndexFromDate(fromDate);
 						timeIndexMax = timeAxis.findTimeIndexFromDate(toDate);
 						timeRange = new Range(timeIndexMin, timeIndexMax);
 					}
 
 				} catch (NumberFormatException e) {
 					log.error(e.getMessage());
 				} catch (InvalidRangeException e) {
 					log.error(e.getMessage());
 				}
 
 		        GroupBy.GridOption groupBy = null;
 		        if (groupById != null) {
 		            try {
 		                groupBy = GroupBy.GridOption.valueOf(groupById);
 	                } catch (IllegalArgumentException e) { /* failure handled below */}
 		        }
 		        if (groupBy == null) {
 		            groupBy = GroupBy.GridOption.getDefault();
 		        }
 
                 List<Statistic> statisticList = new ArrayList<Statistic>();
                 if (outputStats != null && outputStats.length > 0) {
                     for(int i = 0; i < outputStats.length; ++i) {
                         // may throw exception if outputStats value doesn't
                         // map to Statistic enum value, ivan says let percolate up.
                         statisticList.add(Statistic.valueOf(outputStats[i]));
                     }
                 }
 
                 if (statisticList.size() == 0) {
                     throw new IllegalArgumentException("no output statistics selected");
                 }
 
 				GridStatistics gs = null;
 				// *** long running task ***
 				gs = GridStatistics.generate(
 						featureCollection,
 						attributeName,
 						gridDataset,
 						gridName,
 						timeRange);
 				GridStatisticsWriter ouputFileWriter = null;
 
 				if ("ascii".equals(output.toLowerCase()) || "xml".equals(output.toLowerCase())) {
 					ouputFileWriter = new GridStatisticsCSVWriter(
                             gs,
                             statisticList,
                             groupBy == GroupBy.GridOption.statistics,
                             delimiterOption.delimiter);
 				}
 
 				BufferedWriter writer = null;
 				try {
 					// Delete the file there previously
 					writer = new BufferedWriter(new FileWriter(new File(System.getProperty("applicationWorkDir"), outputFile)));
 					ouputFileWriter.write(writer);
 				} finally {
 					if (writer != null) {
 						try { writer.close(); } catch (IOException e) { /* get bent */ }
 					}
 				}
 
 			} finally {
 				try {
 					if (gridDataset != null) gridDataset.close();
 				} catch (IOException e) { /* get bent */ }
 			}
 		} else if (featureDataset.getFeatureType() == FeatureType.STATION && featureDataset instanceof FeatureDatasetPoint) {
 			FeatureDatasetPoint fdp = (FeatureDatasetPoint)featureDataset;
 			List<ucar.nc2.ft.FeatureCollection> fcl = fdp.getPointFeatureCollectionList();
 			if (fcl != null && fcl.size() == 1) {
 				ucar.nc2.ft.FeatureCollection fc = fcl.get(0);
 				if (fc != null && fc instanceof StationTimeSeriesFeatureCollection) {
 
 					StationTimeSeriesFeatureCollection stsfc =
 						(StationTimeSeriesFeatureCollection)fc;
 
 					List<VariableSimpleIF> variableList = new ArrayList<VariableSimpleIF>();
 					for(String variableName : dataTypes) {
 						VariableSimpleIF variable = featureDataset.getDataVariable(variableName);
 						if (variable != null) {
 							variableList.add(variable);
 						} else {
 							// do we care?
 						}
 					}
 
 	                GroupBy.StationOption groupBy = null;
 	                if (groupById != null) {
 	                    try {
 	                        groupBy = GroupBy.StationOption.valueOf(groupById);
 	                    } catch (IllegalArgumentException e) { /* failure handled below */}
 	                }
 	                if (groupBy == null) {
 	                    groupBy = GroupBy.StationOption.getDefault();
 	                }
 
 					BufferedWriter writer = null;
 					try {
 						writer = new BufferedWriter(new FileWriter(new File(System.getProperty("applicationWorkDir"), outputFile)));
 						StationDataCSVWriter.write(
 								featureCollection,
 								stsfc,
 								variableList,
 								new DateRange(fromDate, toDate),
 								writer,
                                 groupBy == StationOption.variable,
                                 delimiterOption.delimiter);
 					} finally {
 						if (writer != null) { try { writer.close(); } catch (IOException e) { /* swallow, don't mask exception */ } }
 					}
 
 				} else {
 					// wtf?  I am gonna punch Ivan...
 				}
 			} else {
 				// error, what do we do when more than one FeatureCollection?  does this happen?  If yes, punch Ivan.
 			}
 
 		}
 		FileHelper.copyFileToFile(new File(System.getProperty("applicationWorkDir") + outputFile), uploadDirectory.getPath(), true);
 		
     	File outputDataFile = new File(uploadDirectory.getPath(), outputFile);
     	if (!outputDataFile.exists()) return null;
     	
     	FileLocationBean flb = new FileLocationBean(outputDataFile.getName(), shapefilePath);
     	
 		return flb;
 	}
 
 	static InputStream sendPacket(URL url, String requestMethod, String contentType, String content,
 			String... requestProperties) throws IOException {
 
 		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
 		httpConnection.setDoOutput(true);
 		httpConnection.setRequestMethod(requestMethod);
 
 		if (contentType != null)
 			httpConnection.addRequestProperty("Content-Type", contentType);
 
 		for (int i = 0; i < requestProperties.length; i += 2) {
 			httpConnection.addRequestProperty(requestProperties[i], requestProperties[i+1]);
 		}
 
 		if (content != null) {
 			OutputStreamWriter workspacesWriter = new OutputStreamWriter(httpConnection.getOutputStream());
 			workspacesWriter.write(content);
 			workspacesWriter.close();
 		}
 
 		return httpConnection.getInputStream();
 
 //		BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
 //		StringBuilder sb = new StringBuilder();
 //		String line = null;
 //		while ((line = reader.readLine()) != null) {
 //			sb.append(line + "\n");
 //		}
 //		reader.close();
 
 //		return sb.toString();
 	}
 
 	private static String parseJSON(InputStream json, String element)
 	throws JsonParseException, IOException {
 
 		JsonFactory f = new JsonFactory();
 		JsonParser jp = f.createJsonParser(new InputStreamReader(json));
 
 		while (true) {
 			jp.nextToken();
 
 			if (!jp.hasCurrentToken())
 				break;
 
 			if (element.equals(jp.getCurrentName())) {
 				jp.nextToken();
 				return jp.getText();
 			}
 		}
 
 		System.out.println("\"" + element + "\" not found.");
 		return null;
 	}
 
 	private static GeometryCollection parseGML(String gml)
 	throws SchemaException, IOException, SAXException, ParserConfigurationException {
 
 		if (gml == null)
 			throw new IOException();
 
 		//create the parser with the gml 2.0 configuration
 		org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
 		org.geotools.xml.Parser parser = new org.geotools.xml.Parser( configuration );
 
 
 		// TODO: parse fails with when gml is only a single polygon with "Authority "SDO" is unknown".
 		//parse
 		GeometryCollection geom = (GeometryCollection) parser.parse( new StringReader(gml) );
 
 		return geom;
 	}
 	
 	private static FeatureCollection<SimpleFeatureType, SimpleFeature> 
 	createFeatureCollection(GeometryCollection geom)
 	throws NoSuchAuthorityCodeException, FactoryException {
 		
 		FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();
 
 		for (int i = 0; i < geom.getNumGeometries(); i++) {
 			Geometry g = geom.getGeometryN(i);
 
 			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
 			typeBuilder.setName("testType");
 			typeBuilder.setCRS(CRS.decode("EPSG:4326"));
 			typeBuilder.add("blah", Integer.class);
 			typeBuilder.add("geom", Geometry.class);
 			typeBuilder.setDefaultGeometry("geom");
 
 			SimpleFeatureType type = typeBuilder.buildFeatureType();
 
 
 //			GeometryFactory geomFactory = new GeometryFactory();
 			SimpleFeatureBuilder build = new SimpleFeatureBuilder( type );
 
 //			for (Coordinate c : g.getCoordinates()) {
 //				System.out.println(c.x + ", " + c.y);
 //				build.add( geomFactory.createPoint( c ));
 //			}
 
 			build.set("geom", g);
 			build.set("blah", i);
 
 			SimpleFeature sf = build.buildFeature(null);
 			sf.getBounds();
 
 //			SimpleFeature sf = SimpleFeatureBuilder.build(type, g.getCoordinates(), null);
 
 			fc.add(sf);
 		}
 
 		return fc;
 	}
 
 	private static final long serialVersionUID = 1L;
 
     /**
      * Writes a collection of {@link PointFeature}s to a file in CSV format. PointFeatures are written one per line
      * in the order that they appear in {@code points}'s iterator.
      *
      * @param points    a collection of point features. Before being passed to this method, a PointFeatureCollection
      *                  can be subset in space and/or time with {@link PointFeatureCollection#subset}.
      * @param outFile   the file to write output to.
      * @throws IOException  if an I/O error occurs.
      */
     public static void writePointsToFile(PointFeatureCollection points, File outFile)
             throws IOException {
         boolean columnNamesWritten = false;
         BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
         String fieldSep = ", ";
         String eol = "\n";
 
         try {
             for (PointFeatureIterator iter = points.getPointFeatureIterator(-1); iter.hasNext();) {
                 StringBuilder strBuilder = new StringBuilder();
 
                 PointFeature pointFeature = iter.next();
                 StructureData data = pointFeature.getData();
 
                 if (!columnNamesWritten) {
                     for (StructureMembers.Member member : data.getMembers()) {
                         String memberName = member.getName().trim();
                         strBuilder.append(memberName).append(fieldSep);
                     }
 
                     // Replace trailing fieldSep with eol.
                     strBuilder.replace(strBuilder.length() - fieldSep.length(), strBuilder.length(), eol);
                     columnNamesWritten = true;
                 }
 
                 for (StructureMembers.Member member : data.getMembers()) {
                     String memberValue = data.getArray(member).toString().trim();
                     strBuilder.append(memberValue).append(fieldSep);
                 }
 
                 // Replace trailing fieldSep with eol.
                 strBuilder.replace(strBuilder.length() - fieldSep.length(), strBuilder.length(), eol);
                 writer.write(strBuilder.toString());
             }
         } finally {
             writer.close();
         }
     }
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public FileProcessServlet() {
         super();
     }
 
     @SuppressWarnings("unused")
     private ShapeFileSetBean getShapeFilesSetBean(String fileSetSelection, List<ShapeFileSetBean> shapeFileSetBeanList) {
         ShapeFileSetBean result = null;
 
         for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanList) {
             if (shapeFileSetBean.getName().equals(fileSetSelection)) {
                 result = shapeFileSetBean;
             }
         }
 
         return result;
     }
 
 
 
     @SuppressWarnings("unused")
     private List<ShapeFileSetBean> getShapeFilesSetSubList(String[] checkboxItems, List<ShapeFileSetBean> shapeFileSetBeanList) {
         List<ShapeFileSetBean> result = new ArrayList<ShapeFileSetBean>();
 
         for (String item : checkboxItems) {
             for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanList) {
                 if (shapeFileSetBean.getName().equals(item)) {
                     result.add(shapeFileSetBean);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Gets called after user has chosen a ShapeFile set to work with
      *
      * @param request
      * @return
      */
     @SuppressWarnings({ "unchecked", "unused" })
     private String populateAttributeList(HttpServletRequest request) {
         String fileSelection = request.getParameter("fileName");
         List<ShapeFileSetBean> shapeFileSetBeanList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanList");
         ShapeFileSetBean shpFileSetBean = (ShapeFileSetBean) request.getSession().getAttribute("shapeFileSetBean");
 
         MessageBean errorBean = new MessageBean();
         MessageBean messageBean = new MessageBean();
 
         if (shapeFileSetBeanList == null) {
             errorBean.addMessage("Unable to retrieve shape file set lists. Please choose new shape file(s).");
             request.setAttribute("messageBean", messageBean);
             request.setAttribute("errorBean", errorBean);
             return "/jsp/fileSelection.jsp";
         }
 
         if (fileSelection == null || "".equals(fileSelection)) {
             errorBean.addMessage("You must select at least one file to process.");
             request.setAttribute("messageBean", messageBean);
             request.setAttribute("errorBean", errorBean);
             return "/jsp/fileSelection.jsp";
         }
 
         // Get the correct shapeFileSet out of the list...
         for (ShapeFileSetBean shapeFileSubset : shapeFileSetBeanList) {
             if (fileSelection.equals(shapeFileSubset.getName())) {
                 shpFileSetBean = shapeFileSubset;
             }
         }
 
         try {
             shpFileSetBean.setAttributeList(ShapeFileSetBean.getAttributeListFromBean(shpFileSetBean));
         } catch (IOException e) {
             log.debug(e.getMessage());
             errorBean.addMessage("An error was encountered. Please try again.");
             errorBean.addMessage("ERROR: \n" + e.getMessage());
             return "/jsp/fileSelection.jsp";
         }
 
         request.getSession().setAttribute("shapeFileSetBean", shpFileSetBean);
         return "/jsp/attributeSelection.jsp";
     }
 
     @SuppressWarnings("unused")
     private String populateDataSet(HttpServletRequest request) {
         MessageBean errorBean = new MessageBean();
 
         THREDDSInfoBean threddsInfoBean = new THREDDSInfoBean();
         String THREDDSUrl = request.getParameter("THREDDSUrl");
 
         if (THREDDSUrl == null || "".equals(THREDDSUrl)) {
             errorBean.getMessages().add("You must select a THREDDS URL to work with..");
             request.setAttribute("errorBean", errorBean);
             return "/jsp/THREDDSSelection.jsp";
         }
 
         threddsInfoBean.setTHREDDSServer(THREDDSUrl);
 
         // Grab the THREDDS catalog
         URI catalogURI = URI.create(THREDDSUrl);
         InvCatalogFactory factory = new InvCatalogFactory("default", true);
         InvCatalog catalog = factory.readXML(catalogURI);
         StringBuilder buff = new StringBuilder();
         if (!catalog.check(buff)) {
             errorBean.getMessages().add(buff.toString());
             request.setAttribute("errorBean", errorBean);
             return "/jsp/THREDDSSelection.jsp";
         }
 
         // Grab dataset handles from the THREDDS catalog
         List<InvAccess> datasetHandles = NetCDFUtility.getDatasetHandles(catalog, ServiceType.OPENDAP);
         if (datasetHandles == null) {
             errorBean.getMessages().add("Could not pull information from THREDDS Server");
             request.setAttribute("errorBean", errorBean);
             return "/jsp/THREDDSSelection.jsp";
         }
 
         for (InvAccess datasetHandle : datasetHandles) {
             threddsInfoBean.getDatasetUrlList().add(datasetHandle.getStandardUrlName());
             threddsInfoBean.getDatasetNameList().add(datasetHandle.getDataset().getName());
         }
         request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);
         return "/jsp/DataSetSelection.jsp";
     }
 
     /**
      * Set up a feature list
      *
      * @param request
      * @return
      */
     @SuppressWarnings("unused")
     private String populateFeatureList(HttpServletRequest request) {
         // Attribute chosen, set up feature list
         String attributeSelection = request.getParameter("attributeSelection");
         ShapeFileSetBean shpFileSetBean = (ShapeFileSetBean) request.getSession().getAttribute("shapeFileSetBean");
 
         // Set the chosen attribute on the ShapeFileSetBeans
         //String attributeAppliesTo = attributeSelection.substring(0, attributeSelection.indexOf("::"));
         String attribute = attributeSelection.substring(attributeSelection.indexOf("::") + 2);
         shpFileSetBean.setChosenAttribute(attribute);
 
         MessageBean errorBean = new MessageBean();
         MessageBean messageBean = new MessageBean();
         // Pull Feature Lists
         try {
             shpFileSetBean.setFeatureList(ShapeFileSetBean.getFeatureListFromBean(shpFileSetBean));
         } catch (IOException e) {
             errorBean.addMessage("Unable to attain feature list for shape file. Please try again.");
             request.setAttribute("messageBean", messageBean);
             request.setAttribute("errorBean", errorBean);
             return "/jsp/attributeSelection.jsp";
         }
 
         request.getSession().setAttribute("shapeFileSetBean", shpFileSetBean);
         return "/jsp/featureSelection.jsp";
     }
 
     @SuppressWarnings("unused")
     private String populateGrid(HttpServletRequest request) {
         THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");
         MessageBean errorBean = new MessageBean();
 
         String dataSetSelection = request.getParameter("datasetSelection");
         if (dataSetSelection == null || "".equals(dataSetSelection)) {
             errorBean.getMessages().add("Did not get a DataSet selection. Please try again.");
             request.setAttribute("errorBean", errorBean);
             return "/jsp/DataSetSelection.jsp";
         }
 
         // Throw the settings into the THREDDSInfoBean
         String dataSetUrl = dataSetSelection.substring(0, dataSetSelection.indexOf(":::"));
         String dataSetName = dataSetSelection.substring(dataSetSelection.indexOf(":::") + 3);
         threddsInfoBean.setDataSetUrlSelection(dataSetUrl);
         threddsInfoBean.setDataSetNameSelection(dataSetName);
 
         // Grab the grid dataset
         Formatter errorLog = new Formatter();
 
 
         FeatureDataset featureDataset;
         try {
             featureDataset = FeatureDatasetFactoryManager.open(
                     null, dataSetUrl, null, errorLog);
         } catch (IOException e) {
             errorBean.getMessages().add("Could not pull Feature Data. Please try again.");
             request.setAttribute("errorBean", errorBean);
             return "/jsp/DataSetSelection.jsp";
         }
 
         if (featureDataset != null) {
 
             List<String> gridNames = new ArrayList<String>();
 //            if(featureDataset instanceof GridDataset) {
 //                // Grab the grid items
 //                for (GridDatatype grid : ((GridDataset)featureDataset).getGrids()) {
 //                    dataSelectItemList.add(grid.getName());
 //                }
 //                ((GridDataset)featureDataset).close();
 //            } else
 //            if(featureDataset instanceof FeatureDatasetPoint) {
 //                for (ucar.nc2.ft.FeatureCollection fc : ((FeatureDatasetPoint)featureDataset).getPointFeatureCollectionList()) {
 //                    System.out.println(fc.getName());
 //                }
 //            }
             for (VariableSimpleIF vs : featureDataset.getDataVariables()) {
                 gridNames.add(vs.getName());
             }
             threddsInfoBean.setDatasetGridItems(gridNames);
 
         } else {
 
             errorBean.getMessages().add("Could not open a grid at location: " + dataSetUrl);
             errorBean.getMessages().add("Reason: " + errorLog);
             request.setAttribute("errorBean", errorBean);
             return "/jsp/DataSetSelection.jsp";
 
         }
 
         request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);
         return "/jsp/GridSelection.jsp";
     }
 
     /**
      * Populates the ShapeFileSetbean with the THREDDS servers
      *
      * @param request
      * @return
      */
     @SuppressWarnings("unused")
     private String populateTHREDDSSelections(HttpServletRequest request) {
         ShapeFileSetBean shpFileSetBean = (ShapeFileSetBean) request.getSession().getAttribute("shapeFileSetBean");
 
         // Set the chosen feature to work with on the bean
         String featureSelection = request.getParameter("featureSelection");
 
         // Set the chosen feature on the ShapeFileSetBeans
         //String featureAppliesTo = featureSelection.substring(0, featureSelection.indexOf("::"));
         String feature = featureSelection.substring(featureSelection.indexOf("::") + 2);
         shpFileSetBean.setChosenFeature(feature);
 
         // Pull the THREDDS urls from the properties files
         Map<String, String> threddsMap = THREDDSInfoBean.getTHREDDSUrlMap();
         request.setAttribute("threddsMap", threddsMap);
 
         request.getSession().setAttribute("shapeFileSetBean", shpFileSetBean);
         return "/jsp/THREDDSSelection.jsp";
     }
 
     @SuppressWarnings("unused")
     private String populateTimeSelection(HttpServletRequest request) {
         // Set up time selection
         THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");
         MessageBean errorBean = new MessageBean();
 
         String gridSelection = request.getParameter("gridSelection");
         if (gridSelection == null || "".equals(gridSelection)) {
             errorBean.getMessages().add("Did not get a Grid selection. Please try again.");
             request.setAttribute("errorBean", errorBean);
             return "/jsp/GridSelection.jsp";
         }
 
         threddsInfoBean.setGridItemSelection(gridSelection);
         Formatter errorLog = new Formatter();
         FeatureDataset featureDataset = null;
         try {
             featureDataset = FeatureDatasetFactoryManager.open(
                     null, threddsInfoBean.getDataSetUrlSelection(), null, errorLog);
         } catch (IOException e1) {
             errorBean.getMessages().add("Could not open a feature data set");
             errorBean.getMessages().add("Reason: " + e1.getMessage());
             request.setAttribute("errorBean", errorBean);
             return "/jsp/DataSetSelection.jsp";
         }
 
         if (featureDataset != null) {
 
             List<String> timeStrList = new ArrayList<String>();
             if (featureDataset instanceof GridDataset) {
                 GeoGrid grid = ((GridDataset) featureDataset).findGridByName(gridSelection);
 
                 for (NamedObject time : grid.getTimes()) {
                     timeStrList.add(time.getName());
                 }
             } else {
                 errorBean.getMessages().add("Could not open a feature data set");
                 errorBean.getMessages().add("Reason: " + errorLog);
                 request.setAttribute("errorBean", errorBean);
                 return "/jsp/DataSetSelection.jsp";
             }
 
             try {
                 featureDataset.close();
             } catch (IOException e) {
                 log.debug(e.getMessage());
             }
 
             threddsInfoBean.setDatasetGridTimes(timeStrList);
 
             request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);
             return "/jsp/TimePeriodSelection.jsp";
 
         }
         errorBean.getMessages().add("Could not open a grid at location: " + threddsInfoBean.getDataSetUrlSelection());
         errorBean.getMessages().add("Reason: " + errorLog);
         request.setAttribute("errorBean", errorBean);
         return "/jsp/DataSetSelection.jsp";
     }
 
 	private boolean sendEmail(String email, String finalUrlEmail) throws AddressException, MessagingException {
 		String content = "Your file is ready: " + finalUrlEmail;
 		String subject = "Your file is ready";
 		String from = "gdp_data@usgs.gov";
 		EmailMessageBean emBean = new EmailMessageBean(from, email, new ArrayList<String>(), subject, content);
 		EmailHandler emh = new EmailHandler();
 		return emh.sendMessage(emBean);
 	}
 }
 
