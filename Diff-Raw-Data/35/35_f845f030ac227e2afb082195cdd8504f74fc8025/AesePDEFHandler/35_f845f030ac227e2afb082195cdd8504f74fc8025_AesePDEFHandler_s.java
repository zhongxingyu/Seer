 /* This file is part of calliope.
  *
  *  calliope is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  calliope is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
  */
 package calliope.handler.get;
 
 import calliope.constants.Database;
 import calliope.constants.MIMETypes;
 import calliope.constants.Params;
 import calliope.exception.AeseException;
 import calliope.Connector;
 import calliope.db.Connection;
 import calliope.export.PDEFArchive;
 import calliope.export.Format;
 import calliope.export.ZipType;
 import javax.servlet.ServletOutputStream;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 
 /**
  * Handle downloading of parts of the web-server
  * @author desmond
  */
 public class AesePDEFHandler extends AeseGetHandler
 {
     /**
      * Set the zip type based on the passed in parameter
      * @param req the servlet request possibly containing a ZIP_TYPE param
      */
     private ZipType getZipType( HttpServletRequest req )
     {
         ZipType zipType = ZipType.TAR_GZ;
         try
         {
             String zipTypeStr = req.getParameter( Params.ZIP_TYPE );
             if ( zipTypeStr == null )
                 zipType = ZipType.TAR_GZ;
             else
                 zipType = ZipType.valueOf( zipTypeStr );
         }
         catch ( Exception e )
         {
             zipType = ZipType.TAR_GZ;
         }
         return zipType;
     }
     /**
      * Read the request format types
      * @param request a servlet request
      * @return an array of PDEF export formats
      */
     private Format[] readFormats( HttpServletRequest request )
     {
         Map map = request.getParameterMap();
         String[] formatStrs = (String[])map.get( Params.FORMAT );
         if ( formatStrs == null )
         {
             formatStrs = new String[1];
             formatStrs[0] = "MVD";
         }
         Format[] formats = new Format[formatStrs.length];
         try
         {
             for ( int i=0;i<formatStrs.length;i++ )
             {
                 formats[i] = Format.valueOf( formatStrs[i] );
             }
         }
         catch ( Exception e )
         {
             formats = new Format[1];
             formats[0] = Format.MVD;
         }
         return formats;
     }
     /**
      * Handle this request
      * @param request the request object
      * @param response the response object
      * @param urn the urn minus the protocol
      * @throws AeseException 
      */
     @Override
     public void handle( HttpServletRequest request, 
         HttpServletResponse response, String urn ) throws AeseException
     {
         Format[] formats = readFormats( request );
         Map map = request.getParameterMap();
         String[] exprs = (String[])map.get( Params.DOC_ID );
         Connection conn = Connector.getConnection();
         String name = request.getParameter( Params.NAME );
         if ( name == null )
             name = PDEFArchive.NAME;
        String host = request.getRequestURL().toString();
         PDEFArchive pdef = new PDEFArchive( name, formats, host );
         for ( int i=0;i<exprs.length;i++ )
         {
             String[] list = conn.listDocuments( Database.CORTEX, exprs[i] );
             for ( int j=0;j<list.length;j++ )
                 pdef.addCorTex( list[j] );
         }
         File zip = pdef.zip( getZipType(request) );
         response.setContentType( MIMETypes.ZIP );
         try
         {
             byte[] buf = new byte[4096];
             int read=0;
             FileInputStream fis = new FileInputStream( zip );
             ServletOutputStream sos = response.getOutputStream();
             while ( read != -1 )
             {
                 read = fis.read( buf );
                 sos.write( buf, 0, read );
             }
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
 }
