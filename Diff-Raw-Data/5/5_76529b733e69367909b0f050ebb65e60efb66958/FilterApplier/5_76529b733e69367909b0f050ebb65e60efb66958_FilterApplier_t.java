 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public abstract class FilterApplier extends Command
 {
 
 	private String output = "Incomplete";
 	protected String filterName;
 	protected String vcfName;
 	protected String fileName;
 	protected int filterId;
 	private int failureAllow =0;
 	private int passExactly =0;
 	protected DatabaseConnector connection;	
 	protected DatabaseConnector nestedConnection;
 	protected DatabaseConnector nestedConnection2;
 	protected DatabaseConnector nestedConnection3;
 	protected ArrayList<FilterParameter> entryParameters;
 	protected ArrayList<FilterParameter> individualParameters;
 	private FilterComparison comparisonHandler;
 	
 	public FilterApplier(String vcfName, String filterName)
 	{
 		this.vcfName = vcfName;
 		this.filterName = filterName;		
 	}
 	
 	//Template methods
 	protected abstract String getSuccessMessage();
 	protected abstract void initializeVcf( long vcfId ) throws Exception;
 	protected abstract void processUntestedEntry( ResultSet entries ) throws Exception;
 	protected abstract void processUntestedEntryInfo( 
 			String tableName, ResultSet entryInfoData ) throws Exception;
 	protected abstract void processPassingEntry( ResultSet entries ) throws Exception;
 	protected abstract void processUntestedIndividual(long indId) throws Exception;
 	protected abstract void processUntestedIndividualData( String genotypeName, ResultSet genotypeData ) throws Exception;
 	protected abstract void processPassingIndividual() throws Exception;
 	protected abstract void processFailingIndividual() throws Exception;
 	protected abstract void finializeEntry() throws Exception;
 	protected abstract void finializeEntryFailing() throws Exception;
 	protected abstract void closeFiltering();
 	
 	@Override
 	public String execute() {
 		
 		try {
 			this.connection = new DatabaseConnector();
 			this.nestedConnection = new DatabaseConnector();
 			this.nestedConnection2 = new DatabaseConnector();
 			this.nestedConnection3 = new DatabaseConnector();
 		}
 		// ########### doing this Pokemon exception handling is usually a sign that maybe
 		// it isn't this class's responsibility to handle this exception. Should probably reconsider
 		catch( Exception e)
 		{
 			this.output = "Cannot connect to database";
 			return this.output;
 		}
 	
 		try
 			{
 			loadFilter();
 			this.output = applyFilter();
 			connection.CloseConnection();
 			nestedConnection.CloseConnection();
 			nestedConnection2.CloseConnection();
 			nestedConnection3.CloseConnection();
 			}
 		catch (Exception e)
 			{
 			connection.CloseConnection();
 			nestedConnection.CloseConnection();
 			nestedConnection2.CloseConnection();
 			nestedConnection3.CloseConnection();
 			this.output = e.getMessage();
 			}
 		return this.output;
 	
 	}
 
 	private void loadFilter() throws Exception
 	{
 		this.filterId = -1;
 		if (this.filterName.length() > 0)
 		{
 			this.filterId = this.connection.getFilterID(this.filterName);
 			int[] metadata = this.connection.getFilterMetaData(this.filterId);
 			this.failureAllow = metadata[0];
 			this.passExactly = metadata[1];
 		}
 			
 		this.entryParameters = this.connection.getFilterEntries(this.filterId);
 		
 		this.individualParameters = this.connection.getFilterIndividuals(this.filterId);		
 
 		this.comparisonHandler = new FilterComparison();
 	}
 	
 	private String applyFilter()
 	{
 		try {
 			boolean passing = true;
 			
 			long vcfId = this.connection.getVcfId( this.vcfName);
 			
 			initializeVcf( vcfId );
 			
 			ResultSet entries = this.connection.getVcfEntries( vcfId );
 			
 			while (entries.next() )
 			{
 				long entryId = entries.getLong("EntryId");
 				processUntestedEntry( entries );
 				passing = testEntry(entries);
 
 				if (passing)
 				{
 					processPassingEntry(entries);
 					
 					passing = testIndividual(entries, entryId);
 					if (passing)
 					{
 						finializeEntry();
 					}
 					else
 					{
 						finializeEntryFailing();
 					}
 				}
 				else
 				{
 					finializeEntryFailing();
 				}
 			}
 			entries.close();
 			closeFiltering();
 			return getSuccessMessage();
 			
 		} catch (Exception exception) {
 			closeFiltering();
 			//TODO remove
 			exception.printStackTrace();
 			return exception.getMessage();
 		}
 	}
 
 	private boolean testIndividual( ResultSet entries, long entryId)
 			throws SQLException, Exception {
 		
 		boolean entryPass = true;
 		int failCount = 0;
 		int passCount = 0;
 				
 		//individual level
 		String indFormat = entries.getString("Format");
 		ArrayList<String> genotypes = new ArrayList<String>( 
 				Arrays.asList( indFormat.split(":") ));
 		
 		ResultSet individuals = this.nestedConnection.getIndividuals( entryId );
 		while (individuals.next() )
 		{
 			boolean passing = true;
 			long indId = individuals.getLong("IndID");
 			processUntestedIndividual(indId);
 			for (int k=0; k< genotypes.size(); k++)
 			{
 				String genoName = genotypes.get(k);
 				ResultSet genotypeData = this.nestedConnection2.getIndividualDatum( indId, genoName );
 				
 				//test genotype level
 				passing = filterOnGenotype( genoName, genotypeData);
 				if ( !passing )
 				{
 					break;
 				}
 				
 				processUntestedIndividualData( genoName, genotypeData);
 				genotypeData.close();
 			}
 			
 			if (passing)
 			{
 				processPassingIndividual();
 				passCount++;
 				if (passCount == this.passExactly)
 				{
 					//fail remaining without test
 					while (individuals.next() )
 					{
 						indId = individuals.getLong("IndID");
 						processUntestedIndividual(indId);
 						processFailingIndividual();
 					}
 					break;
 				}
 				
 			}
 			else
 			{
 				processFailingIndividual();
 				failCount++;
 				if (failCount > this.failureAllow )
 				{
 					//individuals failed too many times
 					entryPass = false;
 					break;
 				}
 			}
 		}
 		
 		individuals.close();
 		return entryPass;
 	}
 
 	private boolean testEntry(ResultSet entries)
 			throws SQLException, Exception {
 		
 		boolean passing = true;
 		long entryId = entries.getLong("EntryId");
 		passing = filterOnEntryData(entries);
 		if (!passing)
 		{
 			return passing;
 		}
 		
 		ArrayList<String> tableNames = this.nestedConnection.getInfoTableNames();
 		for (int j=0; j< tableNames.size(); j++)
 		{
 			String infoName = tableNames.get(j);
 			ResultSet entryInfoData = this.nestedConnection.getInfoDatum(entryId, infoName);
 			
 			//Test Info data
 			passing = filterOnInfoTable(infoName, entryInfoData);
 			if ( !passing )
 			{
 				break;
 			}
 			
 			if (entryInfoData!=null)
 			{
 				//writes each info datum; 
 				processUntestedEntryInfo( infoName, entryInfoData );
 			}
 		}
 		return passing;
 	}
 
 
 	private boolean filterOnInfoTable(String infoName, ResultSet entryInfoData) throws Exception {
 
 		for( FilterParameter param : this.entryParameters )
 		{
 			if (param.tableName.equals(infoName))
 			{
 				int type = this.nestedConnection2.getInfoDataType( infoName );
 				String testValue = null;
 				if (entryInfoData.next())
 				{
 				    ResultSetMetaData rsMetaData = entryInfoData.getMetaData();
 				    int numberOfColumns = rsMetaData.getColumnCount();
 			    	
 				    if (numberOfColumns > 1)
 				    {
 				    	testValue = entryInfoData.getString(2);
 				    }
 				    else
 				    {
 				    	testValue = "";
 				    }
 					//move cursor to the first for later uses
 					entryInfoData.previous();
 				}
 
 				
 				boolean pass = comparisonHandler.testFilterComparison(type, param, testValue );
 				if (!pass)
 				{
 					return pass;
 				}
 			}
 		}
 		return true;
 	}
 	
 	private boolean filterOnEntryData(ResultSet entryData) throws Exception {
 
 		for( FilterParameter param : this.entryParameters )
 		{
 			//test if filter is on fixed entry data
 			if ( DatabaseConnector.EntryFixedInfo.contains( param.tableName) )
 			{
 				int type = this.nestedConnection2.getInfoDataType( param.tableName );
 				String testValue = null;
 				//no need to check next()
 				testValue = entryData.getString(param.tableName);				    
 				
 				boolean pass = comparisonHandler.testFilterComparison(type, param, testValue );
 				if (!pass)
 				{
 					return pass;
 				}
 			}
 		}
 		return true;
 	}	
 
 	private boolean filterOnGenotype(String genoName, ResultSet indGenoData) throws Exception {
 		
 
 		for( FilterParameter param : this.individualParameters )
 		{
 			if (param.tableName.equals(genoName))
 			{
 				int type = this.nestedConnection3.getGenotypeDataType( genoName );
 				ArrayList<String> testValues;
 				if (indGenoData.next())
 				{
 					testValues = getAllGenotypeData(genoName, indGenoData);
 					//move cursor to the first for later uses
 					indGenoData.previous();
 				}
 				else
 				{
 					testValues = new ArrayList<String>();
 					testValues.add(null);
 				}
 
 				for (String testValue: testValues)
 				{
 					boolean pass = this.comparisonHandler.testFilterComparison(type, param, testValue );
 					if (!pass)
 					{
 						//if any fail return failure
 						return pass;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	private ArrayList<String> getAllGenotypeData(String genoName, ResultSet indGenoData) throws SQLException
 	{
 		if (genoName.equals("GT"))
 		{
 			return getAllGenoTypeDataGT(indGenoData);
 		}
 		ArrayList<String> genoData = new ArrayList<String>();
 	    ResultSetMetaData rsMetaData = indGenoData.getMetaData();
 	    int numberOfColumns = rsMetaData.getColumnCount();
     	
 	    if (numberOfColumns > 1)
 	    {
 	    	for(int i=2; i<=numberOfColumns; i++)
 	    	{
 	    		genoData.add( indGenoData.getString(i) );
 	    	}
 	    }
 	    else
 	    {
 	    	//exists and not null
 	    	genoData.add("");
 	    }
 	    
 	    return genoData;
 	}
 	
 	private ArrayList<String> getAllGenoTypeDataGT(ResultSet indGenoData) throws SQLException
 	{
 		ArrayList<String> gtDataArray = new ArrayList<String>();
 		String gtData = "";
 		for( int i=2; i<=6; i++)
 		{
 			if (i%2 == 0)
 			{
 				//allele data
				String allele = indGenoData.getString(i);
 				if (indGenoData.wasNull())
 				{
 					gtData += ".";
 				}
 				else
 				{
 					gtData += allele;
 				}
 			}
 			else
 			{
 				//phase data
				String phase = indGenoData.getString(i);
 				if (indGenoData.wasNull())
 				{
 					//end of data
 					break;
 				}
 				else if (phase.equals("0"))
 				{
 					gtData += "/";
 				}
 				else if (phase.equals("1"))
 				{
 					gtData += "|";
 				}
 			}
 		}
 		
 		if (gtData.equals("."))
 		{
 			gtData = "";
 		}
 		gtDataArray.add(gtData);
 		System.out.println(gtData);
 		return gtDataArray;		
 	}
 	
 	//@Override
 	public void pipeOutput() {
 	// TODO Auto-generated method stub.
 	
 	}
 
 }
