 
 package lab.mg.DBI;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import broad.core.datastructures.Pair;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMRecordIterator;
 import net.sf.samtools.util.CloseableIterator;
 import nextgen.core.annotation.Annotation;
 
 /**
  *  Created on 2013-9-18 
  *  
  * BAM2DB 
  * simple dbi for paired end bam file .
  * dbi demonstration. 
  * @author zhuxp
  * Example:
  *  @see
  *  How to init database:
  *  Please use interface in DBFactory.
  *  
  *  HashMap config=new HashMap();
  *  DB db = DBFacotry.init(file,"bam2",config);
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
  *  
  *  TODO:
  *  
  *  
  *  LOG:
  *  
  *  BUG:
  *  
  */
 
 public class BAM2DB implements DB<Pair<SAMRecord>> {
 	private static final Logger logger = Logger.getLogger(BAM2DB.class);
 	
 	private SAMFileReader data;
 	private SAMFileReader data2;// for query iterator.
 	private HashMap<String,Object> config;
 	private String method="null";
 
 	public BAM2DB(File file)
 	{
 		this.data=new SAMFileReader(file);
 		this.data2 = new SAMFileReader(file);
 	}
 	public BAM2DB(String filename)
 	{
 		this.data=new SAMFileReader(new File(filename));
 		this.data2=new SAMFileReader(new File(filename));
 	}
 	
 	public BAM2DB(File file, HashMap config)
 	{
 		this.data=new SAMFileReader(file);
 		this.data2 = new SAMFileReader(file);
 		setConfig(config);
 	}
 	
 	public BAM2DB(String filename , HashMap config)
 	{
 		this.data=new SAMFileReader(new File(filename));
 		this.data2=new SAMFileReader(new File(filename));
 	    setConfig(config);
 	}
 	
 
 	@Override
 	/**
 	 *  change config to get different query method.
 	 *               
 	 */
 	public CloseableIterator<Pair<SAMRecord>> query(Annotation a) {
			BAMPairedEndIterator iter= new BAMPairedEndIterator(data,data2.query(a.getChr(),a.getStart(), a.getEnd(), false));  //is this memory consuming?
 			
 			return iter;
 	}
     
     @Override
 	public CloseableIterator<Pair<SAMRecord>> iterator() {
        return new BAMPairedEndIterator(data,data2.iterator());	
 	}
 	@Override
 	public void setConfig(HashMap a) {
 		this.config=a;
 		//TODO add config setup
 	}
 	
 	@Override
 	public HashMap<String,Object> getConfig() {
 		return this.config;
 	}
 
 }
 
 
 
 
 
 /**
  * 
  * @author zhuxp
  *
  * iterate sam record into list
  * try to pair them
  * report the first one if it is paired
  * shift it
  * and so on
  * 
  * if list is too long
  * search the first one's mate in bam file
  * pair them report it 
  * and note its name and don't add its mate to list if they are already reported
  * 
  * iterate bam file fragment in order
  * but if query a region, it might be some reads' mate is in the region's left side, which not guarantee the order of fragments.
  * 
  * TODO:
  * 
  *  smart reduce IO assess frequency if there are too many reads have the same start and end ,  read their mates together. 
  *  (memory control might be a hard problem for this , since each query might be to much reads).
  *   
  */
 
 class BAMPairedEndIterator implements CloseableIterator<Pair<SAMRecord>>
 {
 	private static final Logger logger = Logger
 			.getLogger(BAMPairedEndIterator.class);
 	private SAMFileReader data;
 	private  SAMFileReader data3;
 	private CloseableIterator<SAMRecord> iter;
 	private ArrayList<String> bufferReadsList;
 	private HashMap<String,Pair<SAMRecord>> bufferPairReads; // the reads who haven't be report yet. 
 	private HashMap<String, Integer> reported; //in case buffer is out of memory dump by searching the mate in bam file
 	private Pair<SAMRecord> curr; //current
 	private static int MAXBUFFERREADS=500000; // max buffer reads.
 	
 	
 	private long startTime; //debug
 	public BAMPairedEndIterator(SAMFileReader data, SAMRecordIterator iter) {
 		logger.setLevel(Level.WARN);
 		logger.debug("init iter");
 		long startTime = System.currentTimeMillis(); 
 		this.data=data;
 		this.iter=iter;
 		bufferReadsList = new ArrayList<String>();
 		bufferPairReads = new HashMap<String,Pair<SAMRecord>>();
 		reported = new HashMap<String,Integer>();
 		advance();
 		//debug
 	}
 	/**
 	 *  make sure that curr has value2 ?
 	 */
 	private void advance()  
 	{
 		//if end?
 		//logger.info(bufferPairReads.size());
 		while(iter.hasNext())
 		{
 			
 		// if there are two much buffer 
 		 if (bufferReadsList.size() > MAXBUFFERREADS)
 		 {
 			SAMRecord a = bufferPairReads.get(bufferReadsList.get(0)).getValue1();
 		    SAMRecord b = data.queryMate(a);	
 			curr=new Pair<SAMRecord>(a,b);
 			reported.put(strip_mate_id(a.getReadName()),1);
 			bufferPairReads.remove(bufferReadsList.get(0));
 			bufferReadsList.remove(0);
 			return;
 		 }
 		 
 		 else
 		 {	
 			SAMRecord sam=iter.next();
 			logger.debug("reading next;");
 		    logger.debug(System.currentTimeMillis()-startTime);
 			String readName=strip_mate_id(sam.getReadName());
 		    //if has been reported
 		    if (reported.containsKey(readName))
 		    {
 		    	reported.remove(readName);
 		    }
 		    //if not been reported
 		    else
 		    {	
 		    if (!bufferPairReads.containsKey(readName))
 			{	
 		    logger.debug("getting a mate1");
 		    logger.debug(System.currentTimeMillis()-startTime);
 			Pair<SAMRecord> pair=new Pair<SAMRecord>();
 		    pair.setValue1(sam);
 			bufferReadsList.add(readName);
 			bufferPairReads.put(readName,pair); 
 			}
 			else
 			{
 				logger.debug("getting a mate2");
 			    logger.debug(System.currentTimeMillis()-startTime);
 				bufferPairReads.get(readName).setValue2(sam);
 			}
 		    }
 		 }	
 		 
 		 if (bufferPairReads.get(bufferReadsList.get(0)).hasValue2())
 			 {
 				
 				 curr=bufferPairReads.get(bufferReadsList.get(0));
 				 bufferPairReads.remove(bufferReadsList.get(0));
 				 bufferReadsList.remove(0);
 				 return;
 				
 			 }
 		 
 		}
 		
 		// out of iterator list. 
 		
 		if (bufferReadsList.size()==0)
 		 {
 			curr=null;
 		 }
 		
 		else
 		{
 			SAMRecord a = bufferPairReads.get(bufferReadsList.get(0)).getValue1();
 		    SAMRecord b = data.queryMate(a);
 		 	logger.debug("getting mate2 in file");
 		    logger.debug(System.currentTimeMillis()-startTime);
 			   
 			curr=new Pair<SAMRecord>(a,b);
 			reported.put(strip_mate_id(a.getReadName()), 1);
 			bufferPairReads.remove(bufferReadsList.get(0));
 			bufferReadsList.remove(0);
 		
 		}
 		
 		
 		return;
 		
 	}
 	/**
 	 * strip mate id.
 	 * some paired reads have ends with  ( /1 and /2 ) or ( #1 or #2 ), strip them ,make sure that reads in pair have same name
 	 * @param readName
 	 * @return
 	 */
 	private static String strip_mate_id(String readName)
 	{
 		if ( readName.endsWith("/1") || readName.endsWith("/2") || readName.endsWith("#1") || readName.endsWith("#2"))
 		{
 			readName = readName.substring(0,readName.length()-2); 
 		}
 		return readName;
 	}
 	@Override
 	public boolean hasNext() {
 		if (curr==null) {return false;}
 		return true;
 	}
 
 	@Override
 	public Pair<SAMRecord> next() {
 		Pair<SAMRecord> retv = new Pair<SAMRecord>(curr.getValue1(),curr.getValue2());
 		advance();
 		return retv;
 	}
 
 	@Override
 	public void remove() {
 		
 	}
 
 	@Override
 	public void close() {
 	 iter.close();	
 	}
 	
 }
