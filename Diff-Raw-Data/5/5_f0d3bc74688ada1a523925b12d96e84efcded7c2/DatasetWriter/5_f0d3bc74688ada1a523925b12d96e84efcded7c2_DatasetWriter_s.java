 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.voet.datasetcreator.io;
 
 import com.voet.datasetcreator.data.entities.ColumnMapper;
 import com.voet.datasetcreator.data.entities.SchemaMapper;
 import com.voet.datasetcreator.data.entities.TableMapper;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author dev
  */
 public class DatasetWriter {
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy/MM/dd HH:mm:SS" );
     private final static String LINE_SEP = System.getProperty("line.separator");
     private final SchemaMapper schema;
 
     public DatasetWriter( SchemaMapper schema ) {
         this.schema = schema;
     }
 
     public void writeDataset( File outfile, String fieldChoice, int numRows, boolean includeDefaults ) {
         BufferedWriter writer = null;
         try {
             writer = new BufferedWriter( new FileWriter( outfile ) );
             // write file header
             writer.write( "<?xml version='1.0' encoding='UTF-8'?>" );
             writer.write( LINE_SEP );
             writer.write( "<dataset>" );
             writer.write( LINE_SEP );
 
             for ( TableMapper table : schema.getTables() ) {
                 for ( int i = 0; i < numRows; i++ ){
                     writer.write( "\t<" );
                     writer.write( table.getName() );
 
                     for ( ColumnMapper column : table.getColumms()) {
                         if ( fieldChoice.equals( "ALL" ) ){
                             writer.write( " " );
                             writer.write( column.getColumnName() );
                             writer.write( "=\"");
                             if ( includeDefaults ){
                                 writer.append( getDefault( column.getType() ));
                             }
                             writer.write( "\"" );
 
                         } else if ( fieldChoice.equals( "REQ" ) ){
                             if ( column.isRequired() ){
                                 writer.write( " " );
                                 writer.write( column.getColumnName() );
                                 writer.write( "=\"");
                                 if ( includeDefaults ){
                                     writer.append( getDefault( column.getType() ));
                                 }
                                 writer.write( "\"" );
                             }
                         } else if ( fieldChoice.equals( "NONE" ) ) {
 
                         }
                     }
                     writer.write( "/>");
                     writer.write( LINE_SEP );
                 }
             }
 
             // write file footer
             writer.write( "</dataset>" );
             writer.flush();
         } catch ( IOException ex ) {
             Logger.getLogger( DatasetWriter.class.getName() ).log( Level.SEVERE, null, ex );
         } finally {
             try {
                 writer.close();
             } catch ( IOException ex ) {
                 Logger.getLogger( DatasetWriter.class.getName() ).log( Level.SEVERE, null, ex );
             }
         }
     }
 
     private String getDefault( Integer type ) {
         switch ( type ) {
             case Types.VARCHAR: return "";
             case Types.INTEGER: return String.valueOf( 1 );
             case Types.DATE: return DATE_FORMAT.format( new Date() );
            default: return "";
         }
     }
 }
