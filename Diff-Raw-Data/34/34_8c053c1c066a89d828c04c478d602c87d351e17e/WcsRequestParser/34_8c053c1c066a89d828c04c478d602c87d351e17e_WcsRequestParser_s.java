 package thredds.server.wcs.v1_0_0_1;
 
import thredds.wcs.v1_0_0_Plus.*;
 import thredds.servlet.ServletUtil;
 import thredds.servlet.DatasetHandler;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ucar.nc2.dt.GridDataset;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.net.URI;
 
 /**
  * Parse an incoming WCS 1.0.0+ request.
  *
  * @author edavis
  * @since 4.0
  */
 public class WcsRequestParser
 {
   private static org.slf4j.Logger log =
           org.slf4j.LoggerFactory.getLogger( WcsRequestParser.class );
 
   public static WcsRequest parseRequest( String version, URI serverURI, HttpServletRequest req, HttpServletResponse res )
           throws WcsException
   {
     // These are handled in WcsServlet. Don't need to validate here.
 //    String serviceParam = ServletUtil.getParameterIgnoreCase( req, "Service" );
 //    String versionParam = ServletUtil.getParameterIgnoreCase( req, "Version" );
 //    String acceptVersionsParam = ServletUtil.getParameterIgnoreCase( req, "AcceptVersions" );
 
     // General request info
     WcsRequest.Operation operation;
     String datasetPath = req.getPathInfo();
     GridDataset gridDataset = openDataset( req, res );
     if ( gridDataset == null )
     {
       log.error( "parseRequest(): Failed to open dataset (???).");
       throw new WcsException( WcsException.Code.CoverageNotDefined, "", "Failed to open dataset.");
     }
     WcsDataset wcsDataset = new WcsDataset( gridDataset, datasetPath);
 
     // Determine the request operation.
     String requestParam = ServletUtil.getParameterIgnoreCase( req, "Request" );
     try
     {
       operation = WcsRequest.Operation.valueOf( requestParam );
     }
     catch ( IllegalArgumentException e )
     {
       throw new WcsException( WcsException.Code.InvalidParameterValue, "Request", "Unsupported operation request <" + requestParam + ">." );
     }
 
     // Handle "GetCapabilities" request.
     if ( operation.equals( WcsRequest.Operation.GetCapabilities ) )
     {
       String sectionParam = ServletUtil.getParameterIgnoreCase( req, "Section" );
       String updateSequenceParam = ServletUtil.getParameterIgnoreCase( req, "UpdateSequence" );
 
       if ( sectionParam == null)
         sectionParam = "";
       GetCapabilities.Section section = null;
       try
       {
         section = GetCapabilities.Section.getSection( sectionParam);
       }
       catch ( IllegalArgumentException e )
       {
         throw new WcsException( WcsException.Code.InvalidParameterValue, "Section", "Unsupported GetCapabilities section requested <" + sectionParam + ">." );
       }
 
       return new GetCapabilities( operation, version, wcsDataset, serverURI, section, updateSequenceParam, null);
     }
     // Handle "DescribeCoverage" request.
     else if ( operation.equals( WcsRequest.Operation.DescribeCoverage ) )
     {
       String coverageIdListParam = ServletUtil.getParameterIgnoreCase( req, "Coverage" );
       List<String> coverageIdList = splitCommaSeperatedList( coverageIdListParam );
 
       return new DescribeCoverage( operation, version, wcsDataset, coverageIdList);
     }
     // Handle "GetCoverage" request.
     else if ( operation.equals( WcsRequest.Operation.GetCoverage ) )
     {
       String coverageId = ServletUtil.getParameterIgnoreCase( req, "Coverage" );
       String crs = ServletUtil.getParameterIgnoreCase( req, "CRS" );
       String responseCRS = ServletUtil.getParameterIgnoreCase( req, "RESPONSE_CRS" );
       String bbox = ServletUtil.getParameterIgnoreCase( req, "BBOX" );
       String time = ServletUtil.getParameterIgnoreCase( req, "TIME" );
       // ToDo The name of this parameter is dependent on the coverage (see WcsCoverage.getRangeSetAxisName()).
       String parameter = ServletUtil.getParameterIgnoreCase( req, "Vertical" );
       String format = ServletUtil.getParameterIgnoreCase( req, "FORMAT" );
 
       return new GetCoverage( operation, version, wcsDataset, coverageId,
                               crs, responseCRS, bbox, time, parameter, format);
     }
     else
       throw new WcsException( WcsException.Code.InvalidParameterValue, "Request", "Invalid requested operation <" + requestParam + ">." );
   }
 
   private static List<String> splitCommaSeperatedList( String identifiers )
   {
     List<String> idList = new ArrayList<String>();
     String[] idArray = identifiers.split( ",");
     for ( int i = 0; i < idArray.length; i++ )
     {
       idList.add( idArray[i].trim());
     }
     return idList;
   }
 
   private static GridDataset openDataset( HttpServletRequest req, HttpServletResponse res )
           throws WcsException
   {
 //    String datasetURL = ServletUtil.getParameterIgnoreCase( req, "dataset" );
 //    boolean isRemote = ( datasetURL != null );
 //    String datasetPath = isRemote ? datasetURL : req.getPathInfo();
 //
 //    // convert to a GridDataset
 //    GridDataset gd = isRemote ? ucar.nc2.dt.grid.GridDataset.open( datasetPath ) : DatasetHandler.openGridDataset( req, res, datasetPath );
 //    if ( gd == null ) return null;
 
     GridDataset dataset;
     String datasetPath = req.getPathInfo();
     try
     {
       dataset = DatasetHandler.openGridDataset( req, res, datasetPath );
     }
     catch ( IOException e )
     {
       log.warn( "WcsRequestParser(): Failed to open dataset <" + datasetPath + ">: " + e.getMessage() );
       throw new WcsException( "Failed to open dataset, \"" + datasetPath + "\"." );
     }
     if ( dataset == null )
     {
       log.debug( "WcsRequestParser(): Unknown dataset <" + datasetPath + ">." );
       throw new WcsException( "Unknown dataset, \"" + datasetPath + "\"." );
     }
     return dataset;
   }
 }
