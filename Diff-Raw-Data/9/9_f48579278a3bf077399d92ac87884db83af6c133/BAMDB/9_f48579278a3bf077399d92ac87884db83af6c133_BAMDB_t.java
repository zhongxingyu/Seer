 
 package lab.mg.DBI;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.util.CloseableIterator;
 import nextgen.core.annotation.Annotation;
 
 /**
  *  Created on 2013-9-18 
  *  
  * BAMDB 
  * simple dbi for bam file .
  * dbi demonstration. 
  * @author zhuxp
  * Example:
  *  @see
  *  How to init database:
  *  Please use interface in DBFactory.
  *  
  *  HashMap config=new HashMap();
  *  config.put("method","contained");
  *  DB db = DBFacotry.init(file,"bam",config);
  *  
  *  Query
  *  Annotation a;
  *  CloseableIterator iter=db.query(a);
  *  while(iter.hasNext())
  *  {
  *     print iter.next();
  *  }  
  *  
  *  
  *  Config Options Update:
  *  "method":"contained"
  *  
  *  TODO:
  *  
  *  "method?":"paired end"
  *  
  *  LOG:
  *  
  *  BUG:
  *  
  */
 
 public class BAMDB implements DB {
 	private SAMFileReader data;
 	private HashMap<String,Object> config;
 	private String method="null";
 
 	public BAMDB(File file)
 	{
 		this.data=new SAMFileReader(file);
 	}
 	public BAMDB(String filename)
 	{
 		this.data=new SAMFileReader(new File(filename));
 	}
 	
 	public BAMDB(File file, HashMap config)
 	{
 		this.data=new SAMFileReader(file);
 		setConfig(config);
 	}
 	
 	public BAMDB(String filename , HashMap config)
 	{
 		this.data=new SAMFileReader(new File(filename));
 	    setConfig(config);
 	}
 	
 
 	@Override
 	/**
 	 *  change config to get different query method.
 	 *  for example: contained or overlap
 	 *               single end or paired end
 	 *               
 	 *  
 	 */
 	public CloseableIterator<SAMRecord> query(Annotation a) {
 		if ("contained".equalsIgnoreCase(method))
 		{
 			return queryContained(a);
 		}
 		else 
 		{
			CloseableIterator<SAMRecord> iter= data.query(a.getChr(),a.getSAMStart(), a.getSAMEnd(), false);
 			return iter;
 		}
 	}
     
 	/**
 	 * Query Fucntions
      * Config: method = contained
      * 
      * @param a
      * @return
      */
 	private CloseableIterator<SAMRecord> queryContained(Annotation a) 
 	{
 		CloseableIterator<SAMRecord> iter= data.query(a.getChr(),a.getStart(), a.getEnd(), true);
 		return iter;
 	}
 	
 	
 	
 	
 	
 	
     @Override
 	public CloseableIterator<SAMRecord> iterator() {
        return data.iterator();	
 	}
 	@Override
 	public void setConfig(HashMap a) {
 		this.config=a;
 		if (config.containsKey("method"))
 		{
 			setMethod((String)config.get("method"));
 		}
 	}
 	
 	void setMethod(String a)
 	{
 		this.method=a;
 	}
 	@Override
 	public HashMap getConfig() {
 		return this.config;
 	}
 
 }
