 package webapp;
 
 import java.util.List;
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
 	@Indexed
 	private String _permalink;
 	private int _numEmployees;
 	private String _overview;
 	private String _imageUrl;
 	private double _totalMoneyRaised;
 	
 	@Embedded
 	private List<FundingRound> _fundingRounds;
 
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
 
 	public String getOverview() { return _overview; }
 	protected void setOverview(String overview) { _overview = overview; }
 	
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
				mult = 1000000;
 			} else if (type == 'b') {
				mult = 1000000000;
 			} else if (type == 'k') {
				mult = 1000;
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
 	
 	public List<FundingRound> getFundingRounds() { return _fundingRounds; }
 	protected void setFundingRounds(List<FundingRound> rounds) { _fundingRounds = rounds; }
 }
