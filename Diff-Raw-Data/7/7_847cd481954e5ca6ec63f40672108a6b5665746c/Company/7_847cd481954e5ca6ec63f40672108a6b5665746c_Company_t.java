 package webapp;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.code.morphia.annotations.Indexed;
 
 /*
  * Holds data for a single company
  */
 @Entity()
 public class Company {
 	@Id ObjectId _id;
 	private String _name, _homepageUrl, _blogUrl;
 	@Indexed(unique=true, dropDups=true)
 	private String _permalink;
 	private int _numEmployees;
 	private String _overview;
 	private String _imageUrl;
 	private double _totalMoneyRaised, _fiveYearMoneyRaised;
 	private String _industry;
 	private int _yearFounded;
 	
 	private List<String> _competitors;
 	
 	@Embedded
 	private List<FundingRound> _fundingRounds;
 	@Embedded
 	private List<Office> _offices;
 
 	public Company() {
 		_id = new ObjectId();
 	}
 		
 	public String getName() { return _name; }
 	protected void setName(String name) { _name = name; }
 
 	public String getPermalink() { return _permalink; }
 	protected void setPermalink(String permalink) { _permalink = permalink; }
 	
 	public String getHomepageUrl() { return _homepageUrl; }
 	protected void setHomepageUrl(String url) { _homepageUrl = url; }
 
 	public String getBlogUrl() { return _blogUrl; }
 	protected void setBlogUrl(String url) { _blogUrl = url; }
	
	public String getOverview() { return _overview.replaceAll("href=\"/", "href=\"http://www.crunchbase.com/").replaceAll("<br/>", ""); }
 	public void setOverview(String overview) { _overview = overview; }
 	
 	public int getNumEmployees() { return _numEmployees; }
 	protected void setNumEmployees(int n) { _numEmployees = n; }
 		
 	public String getImageUrl() { return _imageUrl; }
 	protected void setImageUrl(Map<String, List<List<Object>>> json) {
 		if (json == null) {
 			_imageUrl = null;
 			return;
 		}
 		List<List<Object>> l = json.get("available_sizes");
 		if (l == null || l.size() == 0) {
 			_imageUrl = null;
 		} else {
 			_imageUrl = (String) l.get(0).get(1);
 		}
 	}
 		
 	public double getTotalMoneyRaised() { return _totalMoneyRaised; }
 	protected void setTotalMoneyRaised(String totalMoney) {
 		int len = totalMoney.length();
 		char type = totalMoney.charAt(len - 1);
 		double mult = 1;
 		if (!Character.isDigit(type)) {
 			type = Character.toLowerCase(type);
 			totalMoney = totalMoney.substring(1, len - 1); // Ignore dollar sign and type
 			if (type == 'm') {
 				mult = 1e6;
 			} else if (type == 'b') {
 				mult = 1e9;
 			} else if (type == 'k') {
 				mult = 1e3;
 			}
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		for (char c : totalMoney.toCharArray()) {
 			if (Character.isDigit(c) || c == '.') {
 				sb.append(c);
 			}
 		}
 
 		_totalMoneyRaised = Double.parseDouble(sb.toString()) * mult;
 	}
 	
 	
 	public int getYearFounded() { return _yearFounded; }
 	protected void setYearFounded(int year) { _yearFounded = year; }
 	
 	public double getFiveYearMoneyRaised() { return _fiveYearMoneyRaised; }
 	public void setFiveYearMoneyRaised(double money) { _fiveYearMoneyRaised = money; }
 	
 	public String getIndustry() { return _industry; }
 	protected void setIndustry(String industry) { _industry = industry; }
 	
 	public List<FundingRound> getFundingRounds() { return _fundingRounds; }
 	protected void setFundingRounds(List<FundingRound> rounds) { _fundingRounds = rounds; }
 	
 	public List<Office> getOffices() {return _offices;}
 	protected void setOffices(List<Office> offices) { _offices = offices;}
 	
 	public List<String> getCompetitors() {return _competitors;}
 	protected void setCompetitors(List<Map<String, Map<String, Object>>> json) {
 		if(json==null){
 			_competitors=null;
 			return;
 		}
		List<String> competitors = new ArrayList<String>();
 		for(Map<String, Map<String, Object>> comps: json){
 			String comp = (String)comps.get("competitor").get("permalink");
 			competitors.add(comp);
 		}
 		_competitors = competitors;
 	}
 }
