 package com.terradue.dsi;
 
 /*
  *  Copyright 2012 Terradue srl
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import static com.google.inject.Scopes.SINGLETON;
 import static java.lang.String.format;
 import static java.lang.System.exit;
 import static javax.ws.rs.core.UriBuilder.fromUri;
 import static org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE;
 import static org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE;
 import static org.apache.commons.net.ftp.FTPReply.isPositiveCompletion;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.commons.net.ftp.FTPSClient;
 
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.Parameters;
 import com.beust.jcommander.converters.FileConverter;
 import com.terradue.dsi.model.UploadTicket;
 import com.terradue.dsi.wire.FTPSClientProvider;
 
 @Parameters( commandDescription = "Upload an image on DSI Cloud" )
 public final class UploadImage
     extends BaseTool
 {
 
     public static void main( String[] args )
     {
         exit( new UploadImage().execute( args ) );
     }
 
     @Parameter( names = { "--appliance" }, description = "The DSI applicance name" )
     private String applianceName;
 
     @Parameter( names = { "--description" }, description = "The DSI applicance name" )
     private String applianceDescription;
 
     @Parameter( names = { "--provider" }, description = "The DSI provider ID" )
     private String providerId;
 
     @Parameter( names = { "--qualifier" }, description = "The DSI qualifier ID" )
     private String qualifierId;
 
     @Parameter( names = { "--operating-system" }, description = "The DSI applicance Operating System [OPTIONAL]" )
     private String applianceOS = "Linux";
 
     @Parameter( names = { "--appliance-id" }, description = "The DSI applicance OS ID" )
     private String applianceOsId;
 
     @Parameter(
         names = { "--image" },
         description = "Path of the image descriptor (*.vmx) to upload (*.vmdk will be automatically detected)",
         converter = FileConverter.class
     )
     private File image;
 
     @Inject
     private FTPSClient ftpsClient;
 
     @Inject
     @Override
     public void setServiceUrl( @Named( "service.upload" ) String serviceUrl )
     {
         super.setServiceUrl( serviceUrl );
     }
 
     public void setFtpsClient( FTPSClient ftpsClient )
     {
         this.ftpsClient = ftpsClient;
     }
 
     @Override
     protected void bindConfigurations()
     {
         super.bindConfigurations();
         bind( FTPSClient.class ).toProvider( FTPSClientProvider.class ).in( SINGLETON );
     }
 
     @Override
     public void execute()
         throws Exception
     {
         if ( !image.exists() || image.isDirectory() )
         {
             throw new IllegalArgumentException( format( "File %s must be an existing file (directories not supported)",
                                                         image ) );
         }
 
         // extension checker
         int extSeparator = image.getName().lastIndexOf( '.' );
         String extension = image.getName().substring( extSeparator + 1 );
 
         if ( !"vmx".equalsIgnoreCase( extension ) )
         {
             throw new IllegalArgumentException( format( "File %s is not a valid VMware Configuration File (.vmx)",
                                                         image ) );
         }
 
         String imageName = image.getName().substring( 0, extSeparator );
         File physicalImage = new File( image.getParent(), format( "%s.vmdk", imageName ) );
         if ( !physicalImage.exists() )
         {
             throw new IllegalArgumentException( format( "File %s not found!",
                                                         physicalImage ) );
         }
 
         logger.info( "Requesting FTP location where uploading images..." );
 
         UploadTicket uploadTicket = restClient.resource( fromUri( serviceUrl )
                                                          .queryParam( "providerId", providerId )
                                                          .queryParam( "qualifierId", qualifierId )
                                                          .queryParam( "applianceName", applianceName )
                                                          .queryParam( "applianceDescription", applianceDescription )
                                                          .queryParam( "applianceOS", applianceOS )
                                                          .queryParam( "applianceOsId", applianceOsId )
                                                          .build() ).get( UploadTicket.class );
 
         logger.info( "Uploading image: {} on {} (expires on {})...",
                      new Object[]
                      {
                          image.getAbsolutePath(),
                          uploadTicket.getFtpLocation().toString(),
                          uploadTicket.getExpirationDate()
                      } );
 
         logger.info( "Connecting to {}...", uploadTicket.getFtpLocation().getHost() );
 
         ftpsClient.connect( uploadTicket.getFtpLocation().getHost() );
 
         if ( isPositiveCompletion( ftpsClient.getReplyCode() ) )
         {
             logger.info( "Connection extabilished! Logging in..." );
         }
        else
        {
            throw new RuntimeException( format( "Impossible to extabilish an FTP connection with %s server, contact the DSI OPS",
                                                uploadTicket.getFtpLocation().getHost() ) );
        }
 
         try
         {
             if ( ftpsClient.login( "anonymous", "" ) )
             {
                 logger.info( "Successfully logged in! Moving to working directory {}",
                              uploadTicket.getFtpLocation().getPath() );
             }
             else
             {
                 throw new RuntimeException( format( "Impossible to access login to %s, anonymous user not allowed",
                                                     uploadTicket.getFtpLocation().getHost() ) );
             }
 
            sendFtpCommand( "PBSZ 0" );
            sendFtpCommand( "PROT P" );

             ftpsClient.enterLocalPassiveMode();
 
             if ( !ftpsClient.changeWorkingDirectory( uploadTicket.getFtpLocation().getPath() ) )
             {
                 throw new RuntimeException( format( "Impossible to access to %s directory on %s, contact the DSI OPS",
                                                     uploadTicket.getFtpLocation().getPath(),
                                                     uploadTicket.getFtpLocation().getHost() ) );
             }
 
             transferFile( image, ASCII_FILE_TYPE );
             transferFile( physicalImage, BINARY_FILE_TYPE );
         }
         finally
         {
             logger.info( "Disconnecting from {} server...", uploadTicket.getFtpLocation().getHost() );

             ftpsClient.logout();

            if ( ftpsClient.isConnected() )
            {
                ftpsClient.disconnect();
            }

             logger.info( "Connnection closed, bye." );
         }
     }
 
    private void sendFtpCommand( String command )
        throws Exception
    {
        if ( isPositiveCompletion( ftpsClient.sendCommand( command ) ) )
        {
            logger.info( "Payload successfully forced to be encrypted" );
        }
        else
        {
            throw new RuntimeException( "Impossible to force the Payload to be encrypted, contact the DSI OPS" );
        }
    }

     private void transferFile( File toBeTransfered, int fileType )
         throws Exception
     {
         logger.info( "Storing {} file...", toBeTransfered.getName() );
 
         ftpsClient.setFileType( fileType );
 
         InputStream transferStream = null;
         try
         {
             transferStream = new FileInputStream( toBeTransfered );
 
             if ( ftpsClient.storeFile( toBeTransfered.getName(), transferStream ) )
             {
                 logger.info( "File {} successfully stored", toBeTransfered.getName() );
             }
             else
             {
                 throw new RuntimeException( format( "Impossible to store %s file, contact the DSI support team",
                                                     toBeTransfered.getName() ) );
             }
         }
         finally
         {
             if ( transferStream != null )
             {
                 try
                 {
                     transferStream.close();
                 }
                 catch ( IOException e )
                 {
                     // close quietly
                 }
             }
         }
     }
 
 }
