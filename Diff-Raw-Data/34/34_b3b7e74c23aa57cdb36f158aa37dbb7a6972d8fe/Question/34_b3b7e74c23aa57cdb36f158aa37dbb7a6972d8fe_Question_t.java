 package main.java.browniepoints.model;
 
public class Question implements Cloneable {
 	private Integer qid;
 	private Integer uid;
 	private String title;
 	private String desc;
 	private String trivia;
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

 	private String url_type;
 	@Override
 	public String toString() {
 		return "Question [qid=" + qid + ", uid=" + uid + ", title=" + title
 				+ ", desc=" + desc + ", trivia=" + trivia + ", url_type="
 				+ url_type + ", url=" + url + ", option1=" + option1
 				+ ", option2=" + option2 + ", option3=" + option3
 				+ ", option4=" + option4 + ", answer=" + answer + ", rid="
 				+ rid + ", cuisine=" + cuisine + ", likes=" + likes
 				+ ", played=" + played + ", creation_date=" + creation_date
 				+ ", approved_by=" + approved_by + "]";
 	}
 
 	private String url;
 	private String option1;
 	private String option2;
 	private String option3;
 	private String option4;
 	private String answer;
 	private Integer rid;
 	private String cuisine;
 	private Integer likes;
 	private Integer played;
 	private Long creation_date;
 	private Integer approved_by;
 
 	public Question() {
 	}
 
 	public Question(Integer qid, Integer uid, String title, String desc,
 			String trivia, String url_type, String url, String option1,
 			String option2, String option3, String option4, String answer,
 			Integer rid, String cuisine, Integer likes, Integer played,
 			Long creation_date, Integer approved_by) {
 		super();
 		this.qid = qid;
 		this.uid = uid;
 		this.title = title;
 		this.desc = desc;
 		this.trivia = trivia;
 		this.url_type = url_type;
 		this.url = url;
 		this.option1 = option1;
 		this.option2 = option2;
 		this.option3 = option3;
 		this.option4 = option4;
 		this.answer = answer;
 		this.rid = rid;
 		this.cuisine = cuisine;
 		this.likes = likes;
 		this.played = played;
 		this.creation_date = creation_date;
 		this.approved_by = approved_by;
 	}
 
 	public Question(Integer uid, String title, String desc, String trivia,
 			String url_type, String url, String option1, String option2,
 			String option3, String option4, String answer, Integer rid,
 			String cuisine, Integer likes, Integer played, Long creation_date,
 			Integer approved_by) {
 		super();
 		this.uid = uid;
 		this.title = title;
 		this.desc = desc;
 		this.trivia = trivia;
 		this.url_type = url_type;
 		this.url = url;
 		this.option1 = option1;
 		this.option2 = option2;
 		this.option3 = option3;
 		this.option4 = option4;
 		this.answer = answer;
 		this.rid = rid;
 		this.cuisine = cuisine;
 		this.likes = likes;
 		this.played = played;
 		this.creation_date = creation_date;
 		this.approved_by = approved_by;
 	}
 
 	public Integer getQid() {
 		return qid;
 	}
 
 	public void setQid(Integer qid) {
 		this.qid = qid;
 	}
 
 	public Integer getUid() {
 		return uid;
 	}
 
 	public void setUid(Integer uid) {
 		this.uid = uid;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getDesc() {
 		return desc;
 	}
 
 	public void setDesc(String desc) {
 		this.desc = desc;
 	}
 
 	public String getTrivia() {
 		return trivia;
 	}
 
 	public void setTrivia(String trivia) {
 		this.trivia = trivia;
 	}
 
 	public String getUrl_type() {
 		return url_type;
 	}
 
 	public void setUrl_type(String url_type) {
 		this.url_type = url_type;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getOption1() {
 		return option1;
 	}
 
 	public void setOption1(String option1) {
 		this.option1 = option1;
 	}
 
 	public String getOption2() {
 		return option2;
 	}
 
 	public void setOption2(String option2) {
 		this.option2 = option2;
 	}
 
 	public String getOption3() {
 		return option3;
 	}
 
 	public void setOption3(String option3) {
 		this.option3 = option3;
 	}
 
 	public String getOption4() {
 		return option4;
 	}
 
 	public void setOption4(String option4) {
 		this.option4 = option4;
 	}
 
 	public String getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 
 	public Integer getRid() {
 		return rid;
 	}
 
 	public void setRid(Integer rid) {
 		this.rid = rid;
 	}
 
 	public String getCuisine() {
 		return cuisine;
 	}
 
 	public void setCuisine(String cuisine) {
 		this.cuisine = cuisine;
 	}
 
 	public Integer getLikes() {
 		return likes;
 	}
 
 	public void setLikes(Integer likes) {
 		this.likes = likes;
 	}
 
 	public Integer getPlayed() {
 		return played;
 	}
 
 	public void setPlayed(Integer played) {
 		this.played = played;
 	}
 
 	public Long getCreation_date() {
 		return creation_date;
 	}
 
 	public void setCreation_date(Long creation_date) {
 		this.creation_date = creation_date;
 	}
 
 	public boolean hasQid() {
 		return this.qid != null;
 	}
 
 	public Integer getApproved_by() {
 		return approved_by;
 	}
 
 	public void setApproved_by(Integer approved_by) {
 		this.approved_by = approved_by;
 	}
 }
