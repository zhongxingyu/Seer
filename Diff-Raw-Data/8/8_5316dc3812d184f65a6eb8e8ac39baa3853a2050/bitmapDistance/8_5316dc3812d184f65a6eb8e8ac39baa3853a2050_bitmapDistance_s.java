 /**
  * A distance metric that calculates distance based on 
  * weighed union of a bit map interception
  *
  * @author Dimitry Kongevold(dimitryk)
  * 
  */
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import com.sun.security.auth.login.ConfigFile;
 
 /**
  * A distance metric that calculates distance based on 
  * weighed union of a bit map interception
  * @version 240911.01 
  * @author dimitryk
  */
 public class bitmapDistance implements DistanceMetric{
 	private static Properties weightsConfig;
 	
 	bitmapDistance(Properties weights){
 		weightsConfig = weights;
 	}
 	
 	/**
 	 * internal method that makes a bit map of 6 ints
 	 * each place in an array corresponds to a spesfic value
 	 * of a Recipient field
 	 *  
 	 * @author dimitryk
 	 * @param list input arraylist of ReciPients for method...
 	 * @return Map as bitmap
 	 */
 	
 	private double[] MakeRecipMap(ArrayList<Recipient> list){
 		double[] Map = new double[6];
 		for(int d=0;d<6;d++)Map[d]=0;
 		for(int i=0;i<list.size();i++){
 			switch (list.get(i)){
 			case OURS: //weights can come in here 2
 				Map[0]= Double.parseDouble(weightsConfig.getProperty("ours"));
 				break;
 			case DELIVERY:			
 				Map[1]=Double.parseDouble(weightsConfig.getProperty("delivery"));
 				break;
 			case SAME:
 				Map[2]=Double.parseDouble(weightsConfig.getProperty("same"));
 				break;
 			case OTHER_RECIPIENT:
 				Map[3]=Double.parseDouble(weightsConfig.getProperty("other_recipient"));
 				break;
 			case UNRELATED:
 				Map[4]=Double.parseDouble(weightsConfig.getProperty("unrelated"));
 				break;
 			case PUBLIC:
 				Map[5]=Double.parseDouble(weightsConfig.getProperty("public"));
 				break;
 			default:break;
 			}
 		}
 		return Map;
 	}
 	
 	/**
 	 * internal method that makes a bit map of 12 doubles
 	 * each place in an array corresponds to a spesfic value
 	 * of a Purpose Enum
 	 * @author dimitryk
 	 * @param list input arraylist of Purpose for method...
 	 * @return Map as bitmap
 	 */
 	
 	private double[] MakePurposeMap(ArrayList<Purpose> list){
 		double[] Map = new double[12];
 		for(int d=0;d<12;d++)Map[d]=0;
 		for(int i=0;i<list.size();i++){
 			switch (list.get(i)){
 			case CURRENT:
 				Map[0]=Double.parseDouble(weightsConfig.getProperty("current"));
 				break;
 			case ADMIN:
 				Map[1]=Double.parseDouble(weightsConfig.getProperty("admin"));
 				break;
 			case DEVELOP:
 				Map[2]=Double.parseDouble(weightsConfig.getProperty("develop"));
 				break;
 			case TAILORING:
 				Map[3]=Double.parseDouble(weightsConfig.getProperty("tailoring"));
 				break;
 			case PSEUDO_ANALYSIS:
 				Map[4]=Double.parseDouble(weightsConfig.getProperty("pseudo_analysys"));
 				break;
 			case PSEUDO_DECISION:
 				Map[5]=Double.parseDouble(weightsConfig.getProperty("pseudo_decision"));
 				break;
 			case INDIVIDUAL_ANALYSIS:
 				Map[6]=Double.parseDouble(weightsConfig.getProperty("individual_analysis"));
 				break;
 			case INDIVIDUAL_DECISION:
 				Map[7]=Double.parseDouble(weightsConfig.getProperty("individual_decision"));
 				break;
 			case CONTACT:
 				Map[8]=Double.parseDouble(weightsConfig.getProperty("contact"));
 				break;
 			case HISTORICAL:
 				Map[9]=Double.parseDouble(weightsConfig.getProperty("historical"));
 				break;
 			case TELEMARKETING:
 				Map[10]=Double.parseDouble(weightsConfig.getProperty("telemarketing"));
 				break;
 			case OTHER_PURPOSE:
 				Map[11]=Double.parseDouble(weightsConfig.getProperty("other_purpose"));
 				break;
 			default:break;
 			}
 		}
 		return Map;
 	}
 	
 	/**
 	 * internal method that makes a bit map of 5 ints
 	 * each place in an array corresponds to a spesfic value
 	 * of a Retention Enum
 	 * @author dimitryk
 	 * @param list input arraylist of Purpose for method...
 	 * @return Map as bitmap
 	 */
 	
 	private double[] MakeRetentionMap(ArrayList<Retention> list){
 		double[] Map = new double[5];
 		for(int d=0;d<5;d++)Map[d]=0;
 		for(int i=0;i<list.size();i++){
 			switch (list.get(i)){
 			case NO_RETENTION: //weights can come in here 2
 				Map[0]=Double.parseDouble(weightsConfig.getProperty("no_retention"));
 				break;
 			case STATED_PURPOSE:
 				Map[1]=Double.parseDouble(weightsConfig.getProperty("stated_purpose"));
 				break;
 			case LEGAL_REQUIREMENT:
 				Map[2]=Double.parseDouble(weightsConfig.getProperty("legal_requirement"));
 				break;
 			case BUSINESS_PRACTICES:
 				Map[3]=Double.parseDouble(weightsConfig.getProperty("business_practices"));
 				break;
 			case INDEFINITELY:
 				Map[4]=Double.parseDouble(weightsConfig.getProperty("idefinitely"));
 				break;
 			default:break;
 			}
 		}
 		return Map;
 	}
 	
 	/**
 	 * Calculates distance in the Recipien field from case a to case b
 	 * using bit map distance = a interseption b * weight
 	 * @author dimitryk
 	 * @param a input case.
 	 * @param b input case.
 	 * @return dis double for the distance between to.
 	 */
 	
 	private double getDistRecip(Case a, Case b) {
 		double[] MapA, MapB;
 		MapA = MakeRecipMap(a.getRecipients());
 		MapB = MakeRecipMap(b.getRecipients());
 		double dis=0;
 		
 		for(int i=0;i<6;i++){
 			if(MapA[i]!=MapB[i]){
 				dis=dis+Math.max(MapA[i], MapB[i]);
 				//dis=dis+1; //for now... weights are jet not operational.
 			}
 		}
 		
 		
		return dis;// dis*weight later
 	}
 
 	/**
 	 * Calculates distance in the Retention field from case a to case b
 	 * using bit map distance = a interseption b * weight
 	 * @author dimitryk
 	 * @param a input case.
 	 * @param b input case.
 	 * @return dis double for the distance between to.
 	 */
 
 	private double getDistReten(Case a, Case b) {
 		double[] MapA, MapB;
 		MapA = MakeRetentionMap(a.getRetentions());
 		MapB = MakeRetentionMap(b.getRetentions());
 		double dis=0;
 		
 		for(int i=0;i<5;i++){
 			if(MapA[i]!=MapB[i]){
 				dis=dis+Math.max(MapA[i], MapB[i]);
 			}
 		}
 		
 		
		return dis;// dis*weight later
 	}
 
 	/**
 	 * Calculates distance in the Purpose field from case a to case b
 	 * using bit map distance = a interseption b * weight
 	 * @author dimitryk
 	 * @param a input case.
 	 * @param b input case.
 	 * @return dis double for the distance between to.
 	 */
 	
 	private double getDistPurpose(Case a, Case b) {
 		double[] MapA, MapB;
 		MapA = MakePurposeMap(a.getPurposes());
 		MapB = MakePurposeMap(b.getPurposes());
 		double dis=0;
 		
 		for(int i=0;i<12;i++){
 			if(MapA[i]!=MapB[i]){
 				dis=dis+Math.max(MapA[i], MapB[i]);
 			}
 		}
 		
 		
		return dis;// dis*weight later
 	}
 
 	private double getSumDistance(Case a, Case b) {
 		
 		return getDistPurpose(a,b)+getDistRecip(a,b)+getDistReten(a,b);
 	}
 
 	@Override
 	public double getTotalDistance(PolicyObject a, PolicyObject b) {
 		ArrayList<Case> CasesA, CasesB;
 		CasesA=a.getCase();
 		CasesB=b.getCase();
 		double minDist;
 		double Distance = 0;
 		for(int i=0;i<CasesA.size();i++){
 			minDist=Double.MAX_VALUE;
 			for(int d=0;d<CasesB.size();d++){
 				minDist=Math.min(getSumDistance(CasesA.get(i), CasesB.get(d)),minDist);
 			}
 			Distance += minDist;
 		}
 		
 		return Distance;
 	}
 
 }
