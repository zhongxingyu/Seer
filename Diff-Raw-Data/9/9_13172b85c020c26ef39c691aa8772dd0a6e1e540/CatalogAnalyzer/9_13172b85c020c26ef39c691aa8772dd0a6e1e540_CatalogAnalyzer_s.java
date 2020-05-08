 package eu.trentorise.opendata.ckanalyze.analyzers;
 
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 
 import eu.trentorise.opendata.ckanalyze.jpa.CatalogStringDistribution;
 import eu.trentorise.opendata.ckanalyze.managers.PersistencyManager;
 
 /**
  * This class extracts statistics of the entire catalog and its analyze() method should be called after
  * all resources are already added to the database
  * @author a.zanella
  * Last modified by azanella On 17/lug/2013
  *
  */
 public class CatalogAnalyzer {
 	/**
 	 * This method performs statistics on given catalog
 	 * @param catalog -- catalog JPA object
 	 */
 	private List<CatalogStringDistribution> catalogStringDistribution;
 	private double avgStringLength = 0;
 	private double avgColumnCount = 0;
 	private double avgRowCount = 0;
 	
 	public void analyze()
 	{
 		computeStringDistribution();
 		computeAvgStringLength();
 		computeRowsAndColumnAvgCount();
 	}
 	
 	private void computeStringDistribution()
 	{
 		Session ss = PersistencyManager.getSessionFactory().openSession();
 		String hql = "select new CatalogStringDistribution(length,sum(freq)) from ResourceStringDistribution group by length";
 		Query q = ss.createQuery(hql);
		List<CatalogStringDistribution> clist = (List<CatalogStringDistribution>)q.list();
 		ss.close();
 		catalogStringDistribution = clist;
 	}
 	
 	private void computeAvgStringLength()
 	{
 		long sum = 0;
 		long numString = 0;
 		for (CatalogStringDistribution csd : catalogStringDistribution) {
 			sum = sum + (csd.getLength()*csd.getFreq());
 			numString = numString + csd.getFreq();
 			avgStringLength = sum / numString; 
 		}
 		
 	}
 	
 	private void computeRowsAndColumnAvgCount()
 	{
 		Session ss = PersistencyManager.getSessionFactory().openSession();
 		String hql = "select avg(rowCount) from Resource";
 		Query q = ss.createQuery(hql);
 		if(!q.list().isEmpty()) avgRowCount = (Double)q.list().get(0);
 		hql = "select avg(columnCount) from Resource";
 		q = ss.createQuery(hql);
 		if(!q.list().isEmpty()) avgColumnCount = (Double)q.list().get(0);
 		ss.close();
 	}
 	
 	
 	
 	public double getAvgStringLength() {
 		return avgStringLength;
 	}
 
 	public double getAvgColumnCount() {
 		return avgColumnCount;
 	}
 
 	public double getAvgRowCount() {
 		return avgRowCount;
 	}
 	
 	public List<CatalogStringDistribution> getCatalogStringDistribution() {
 		return catalogStringDistribution;
 	}
 	
 }
