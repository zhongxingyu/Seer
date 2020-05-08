 import java.io.*;
 import java.util.*;
 import java.sql.*;
 
 public class vcfWriter
 {
     private BufferedWriter writer;
     private int infoCount = 0;
     private String writeBuffer = "";
 	private boolean individualMiddle;
     
     public vcfWriter( String filename ) throws IOException
     {
         try
         {
             File outFile = new File(filename);
             this.writer = new BufferedWriter(new FileWriter(outFile));
             System.out.println("writer created");
         }
         catch (IOException e)
         {
             throw new IOException("Could not open file:" + filename);
         }
     }
     
     public void writeHeader(String header) throws IOException
     {
         this.writer.write( header);
         this.writer.newLine();
     }
     
     public void writeEntryStart( ResultSet entryData ) throws SQLException
     {
         try {
         	this.writeBuffer = "";
             this.writeBuffer +=( entryData.getString("Chrom") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Pos") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Id") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Ref") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Alt") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Qual") );
             this.writeBuffer +=( '\t' );
             this.writeBuffer +=( entryData.getString("Filter") );
             this.writeBuffer +=( '\t' );
 
             this.infoCount = 0;
         
         } catch (SQLException exception) {
         	throw new SQLException("VCF Entry data improperly formatted");
         }
     }
     
     public void writeEntryEnd( ResultSet entryData ) throws IOException, SQLException
     {
     	this.writer.write( this.writeBuffer );
     	this.writer.write("\t");
     	this.writer.write( entryData.getString("Format") );
     	this.writer.write( "\n");
     }
     
     public void writeInfoSection( String infoName, ResultSet infoData) throws IOException, SQLException
     {
 	    if (this.infoCount!= 0)
 	    {
 	    	this.writer.write(";");
 	    }
 	    
 	    String infoDatum = "";
 	    if (infoData.next()) 
 		{
 		    ResultSetMetaData rsMetaData = infoData.getMetaData();
 
 		    int numberOfColumns = rsMetaData.getColumnCount();
 	    	
 		    if (numberOfColumns > 1)
 		    {
		    	infoDatum = infoData.getNString(2);
 		    	this.writer.write( infoName+"="+infoDatum );
 		    }
 		    else
 		    {
 		    	this.writer.write( infoName );
 		    }
 		}
 	    infoData.close();
     }
     
     public void writeIndividual( 
             ArrayList<ResultSet> genotypeData,
             ArrayList<String> genotypeName ) throws IOException, SQLException
 	{
     	this.writeBuffer +=("\t");
     	if ( genotypeData == null)
     	{
     		return;
     	}
     	
     	for (int i=0; i < genotypeName.size(); i++)
     	{
     		if ( i!= 0)
     		{
     			this.writeBuffer +=(":");
     		}
     		if ( isSpecialCase(genotypeName.get(i) ) )
     		{
     				
     			this.writeBuffer +=( formatSpecialCase( genotypeName.get(i), genotypeData.get(i) ) );
     		}
     		else
     		{
     			this.writeBuffer +=( formatStandardCase( genotypeName.get(i), genotypeData.get(i) ) );
     		}	
     		
     		genotypeData.get(i).close();
     	} 	
     	
 	}
     
     public void writeIndividualStart()
     {
     	this.writeBuffer = "\t";
     	this.individualMiddle = false;
     	
     }
     
     public void writeIndividualEnd() throws IOException
     {
     	this.writer.write(this.writeBuffer);
     }
     
     public void writeIndividualDatum( 
             ResultSet genotypeData,
             String genotypeName ) throws SQLException
 	{
     	if ( genotypeData == null)
     	{
     		return;
     	}
     	
 
 		if ( this.individualMiddle )
 		{
 			this.writeBuffer +=(":");
 		}
 		else
 		{
 			this.individualMiddle = true;
 		}
 		
 		if ( isSpecialCase(genotypeName ) )
 		{
 				
 			this.writeBuffer +=( formatSpecialCase( genotypeName, genotypeData ) );
 		}
 		else
 		{
 			this.writeBuffer +=( formatStandardCase( genotypeName, genotypeData ) );
 		}	
 		
 		genotypeData.close();	
     	
 	}
     
 	private String formatStandardCase(String genotypeName, ResultSet rs) throws SQLException {
 
 	    ResultSetMetaData rsMetaData = rs.getMetaData();
 
 	    int numberOfColumns = rsMetaData.getColumnCount();
 	    
 	    String indData = "";
 	    String nullValues = null;
 	    
 	    for (int i=2; i<= numberOfColumns; i++)
 	    {
 	    	String separator = ",";
 	    	if (i==2)
 	    	{
 	    		separator = "";
 	    	}
 	    	
	    	String genoDatum = rs.getNString(i);
 	    	if ( rs.wasNull() )
 	    	{
 	    		if ( nullValues == null )
 	    			nullValues = separator + ".";
 	    		else
 	    			nullValues += separator + ".";
 	    	}
 	    	else
 	    	{
 	    		if ( nullValues == null )
 	    		{
 	    			//valid value; append stored nulls
 	    			indData += nullValues;
 	    			nullValues = null;
 	    		}
 	    		indData += separator + genoDatum;
 	    	}
 	    	
 	    }
 	    if (indData.isEmpty())
 	    {
 	    	indData = ".";
 	    }
 	    
 		return indData;
 	}
 
 	private boolean isSpecialCase(String genotypeName ) throws SQLException
 	{
 		return genotypeName.equals( "GT");
 	}
 	
 	private String formatSpecialCase(String genotypeName, ResultSet data) throws SQLException {
 		if ( genotypeName.equals( "GT") )
 		{
 			int i = 0;
 			String gtData = "";
 			if (data.next() )
 			{
 				appendAllele( gtData, data, "1") ;
 				if ( appendPhase( gtData, data, "1") )
 				{
 					appendAllele( gtData, data, "2") ;
 					if (appendPhase( gtData, data, "2") )
 					{
 						appendAllele( gtData, data, "3");
 					}
 				}
 				return gtData;
 			}
 			else
 			{
 				return ".";
 			}
 			
 		}
 		return "";
 	}
 	
 	private void appendAllele( String gtData, ResultSet data, String count )throws SQLException
 	{
 		String allele = data.getString("Allele" +count);
 		if ( data.wasNull() )
 		{
 			gtData += ".";
 		}
 		else
 		{
 			gtData += allele;
 		}
 		
 	}
 	
 	private boolean appendPhase( String gtData, ResultSet data, String count )throws SQLException
 	{
 		byte phase = data.getByte("Phase" +count);
 		if ( data.wasNull() )
 		{
 			//end of data
 			return false;
 		}
 		else
 		{
 			if ( phase == 0 )
 			{
 				gtData = "/";
 			}
 			else if ( phase == 1 )
 			{
 				gtData = "|";
 			}
 			else
 			{
 				throw new SQLException("Invalid GT data");
 			}
 			return true;
 		}
 	}
 
 	public void writeEOL() throws IOException
     {
     	this.writer.write("\n");
     	
     }
     
     public void closeWriter()
     {
         if ( this.writer != null )
         {
             try {
                 this.writer.close();
             } catch (IOException exception) {
                 //Do nothing
             }
         }
     }
 }
