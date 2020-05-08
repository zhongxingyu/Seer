 package main.java.browniepoints.model.helper;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import main.java.browniepoints.model.Attempt;
 import main.java.browniepoints.model.CompositeQuestion;
 import main.java.browniepoints.model.Coupon;
 import main.java.browniepoints.model.Question;
 import main.java.browniepoints.util.Util;
 
 public class QuestionHelper implements SQLConverter {
 
 	private static final QuestionHelper instance = new QuestionHelper();
 	private static Connection conn = null;
 	private static final int VOUCHER_LEN = 10;
 
 	private static final String INSERT_SQL = "insert into public.\"question\" "
 			+ "(uid, title, desc, trivia, url_type, url, option1, option2, option3, "
 			+ "option4, answer, rid, cuisine, likes, played, creation_date, approved_by) "
 			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 	private static final String SELECT_SQL = "select "
 			+ "uid, title, desc, trivia, url_type, url, option1, option2, option3, "
 			+ "option4, answer, rid, cuisine, likes, played, creation_date, approved_by"
 			+ "from public.\"question\"";
 	private static final String UPDATE_SQL = "update public.\"question\" "
 			+ "set uid = ?, title = ?, desc = ?, trivia = ?, "
 			+ "url_type = ?, url = ?, option1 = ?, option2 = ?, option3 = ?, "
 			+ "option4 = ?, answer = ?, rid = ?, cuisine = ?, likes = ?,"
 			+ "played = ?, creation_date = ?, approved_by = ? where qid = ?";
 
 	Map<Integer, List<CompositeQuestion>> questionByUID = new HashMap<Integer, List<CompositeQuestion>>();
 	Map<String, List<CompositeQuestion>> questionByCuisine = new HashMap<String, List<CompositeQuestion>>();
 
 	List<CompositeQuestion> offerQuestions = new ArrayList<CompositeQuestion>();
 	List<CompositeQuestion> noOfferQuestions = new ArrayList<CompositeQuestion>();
 
 	List<CompositeQuestion> questions = new ArrayList<CompositeQuestion>();
 	Map<Integer, CompositeQuestion> questionByQID = new HashMap<Integer, CompositeQuestion>();
 
 	private QuestionHelper() {
 		init();
 	}
 
 	public static QuestionHelper getInstance() {
 		return instance;
 	}
 
 	private void init() {
 		if (null == conn) {
 			conn = ConnectionProvider.getConnection();
 		}
 
 		List<CompositeQuestion> questions = selectQnsByOffer();
 		if (null == questions)
 			return;
 
 		questionByUID.clear();
 		questionByCuisine.clear();
 
 		CompositeQuestion curr = null;
 		for (Integer i = 0; i < questions.size(); ++i) {
 			curr = questions.get(i);
 
 			this.questions.add(curr);
 			questionByQID.put(curr.getQ().getQid(), curr);
 			Util.addToMap(questionByUID, curr.getQ().getUid(), curr);
 
 			String cuisine = curr.getQ().getCuisine();
 			if (cuisine == null || cuisine.trim() == "") {
 				cuisine = curr.getR().getCuisine();
 			}
 			Util.addToMap(questionByCuisine, cuisine, curr);
 
 			if (curr.getC().hasCid()) {
 				offerQuestions.add(curr);
 			} else {
 				noOfferQuestions.add(curr);
 			}
 		}
 	}
 
 	public List<?> convertRSToList(ResultSet rs) {
 		List<Question> questionList = new ArrayList<Question>();
 
 		if (rs == null)
 			return questionList;
 
 		try {
 			while (rs.next()) {
 				Integer qid = rs.getInt("qid");
 				Integer uid = rs.getInt("uid");
 				String title = rs.getString("title");
 				String desc = rs.getString("desc");
 				String trivia = rs.getString("trivia");
 				String urlType = rs.getString("urlType");
 				String url = rs.getString("url");
 				String option1 = rs.getString("option1");
 				String option2 = rs.getString("option2");
 				String option3 = rs.getString("option3");
 				String option4 = rs.getString("option4");
 				String answer = rs.getString("answer");
 				Integer rid = rs.getInt("rid");
 				String cuisine = rs.getString("cuisine");
 				Integer likes = rs.getInt("likes");
 				Integer played = rs.getInt("played");
 				Long creationDate = rs.getLong("creation_date");
 				Integer approvedBy = rs.getInt("approved_by");
 
 				questionList.add(new Question(qid, uid, title, desc, trivia,
 						urlType, url, option1, option2, option3, option4,
 						answer, rid, cuisine, likes, played, creationDate,
 						approvedBy));
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return questionList;
 	}
 
 	public void setColumnsForUpdate(Object entity, PreparedStatement stmt) {
 		if (stmt == null || entity == null)
 			return;
 
 		Question question = (Question) entity;
 		try {
 			stmt.setInt(1, question.getUid());
 			stmt.setString(2, question.getTitle());
 			stmt.setString(3, question.getDesc());
 			stmt.setString(4, question.getTrivia());
 			stmt.setString(5, question.getUrl_type());
 			stmt.setString(6, question.getUrl());
 			stmt.setString(7, question.getOption1());
 			stmt.setString(8, question.getOption2());
 			stmt.setString(9, question.getOption3());
 			stmt.setString(10, question.getOption4());
 			stmt.setString(11, question.getAnswer());
 			stmt.setInt(12, question.getRid());
 			stmt.setString(13, question.getCuisine());
 			stmt.setInt(14, question.getLikes());
 			stmt.setInt(15, question.getPlayed());
 			stmt.setLong(16, question.getCreation_date());
 			stmt.setInt(17, question.getApproved_by());
 			if (question.hasQid())
 				stmt.setInt(18, question.getQid());
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void insert(Question question) {
 		QueryHelper.ExecuteInsertOrUpdate(conn, INSERT_SQL, question, this);
 		// update cache
 		init();
 	}
 
 	public void update(Question question) {
 		QueryHelper.ExecuteInsertOrUpdate(conn, UPDATE_SQL, question, this);
 		// update cache
 		init();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Question> select() {
 		return (List<Question>) QueryHelper.ExecuteSelect(conn, SELECT_SQL,
 				this);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<CompositeQuestion> selectQnsByOffer() {
 		String sql = "select "
 				+ "q.qid as qqid, q.uid as quid, q.title as qt, q.desc as qd, "
 				+ "q.trivia, q.url_type, q.url, "
 				+ "q.option1, q.option2, q.option3, q.option4, q.answer, q.rid as qrid, "
 				+ "q.cuisine, q.likes, q.played, q.creation_date, q.approved_by, "
 				+ "r.rid as rrid, r.name, r.latitude, r.longitude, r.place, "
 				+ "r.phone, r.cuisine, "
 				+ "r.owner_uid, c.cid, c.qid as cqid, c.title as ct, c.desc as cd, "
 				+ "c.quota, "
 				+ "c.discount, c.points, c.start_date, c.end_date "
 				+ "from public.\"question\" as q, public.\"restaurant\" as r, "
 				+ "public.\"coupon\" as c "
 				+ "where q.rid = r.rid and q.qid = c.qid";
 		return (List<CompositeQuestion>) QueryHelper.ExecuteSelect(conn, sql,
 				new SQLConverter() {
 
 					public void setColumnsForUpdate(Object entity,
 							PreparedStatement stmt) {
 						// TODO Auto-generated method stub
 
 					}
 
 					public List<?> convertRSToList(ResultSet rs) {
 						List<CompositeQuestion> questionList = new ArrayList<CompositeQuestion>();
 
 						if (rs == null)
 							return questionList;
 
 						try {
 							while (rs.next()) {
 								CompositeQuestion cq = new CompositeQuestion();
 								cq.getQ().setQid(rs.getInt("qqid"));
 								cq.getQ().setUid(rs.getInt("quid"));
 								cq.getQ().setTitle(rs.getString("qt"));
 								cq.getQ().setDesc(rs.getString("qd"));
 								cq.getQ().setTrivia(rs.getString("trivia"));
 								cq.getQ().setUrl_type(rs.getString("url_type"));
 								cq.getQ().setUrl(rs.getString("url"));
 								cq.getQ().setOption1(rs.getString("option1"));
 								cq.getQ().setOption2(rs.getString("option2"));
 								cq.getQ().setOption3(rs.getString("option3"));
 								cq.getQ().setOption4(rs.getString("option4"));
 								cq.getQ().setAnswer(rs.getString("answer"));
 								cq.getQ().setRid(rs.getInt("qrid"));
 								cq.getQ().setCuisine(rs.getString("cuisine"));
 								cq.getQ().setLikes(rs.getInt("likes"));
 								cq.getQ().setPlayed(rs.getInt("played"));
 								cq.getQ().setCreation_date(
 										rs.getLong("creation_date"));
 								cq.getQ().setApproved_by(
 										rs.getInt("approved_by"));
 
 								cq.getR().setRid(rs.getInt("rrid"));
 								cq.getR().setName(rs.getString("name"));
 								cq.getR().setLatitude(rs.getDouble("latitude"));
 								cq.getR().setLongitude(
 										rs.getDouble("longitude"));
 								cq.getR().setPlace(rs.getString("place"));
 								cq.getR().setPhone(rs.getString("phone"));
 								cq.getR().setCuisine(rs.getString("cuisine"));
 								cq.getR().setOwner_uid(rs.getInt("owner_uid"));
 
 								cq.getC().setCid(rs.getInt("cid"));
 								cq.getC().setDesc(rs.getString("cd"));
 								cq.getC().setDiscount(rs.getInt("discount"));
 								cq.getC().setEnd_date(rs.getLong("end_date"));
 								cq.getC().setStart_date(
 										rs.getLong("start_date"));
 								cq.getC().setPoints(rs.getInt("points"));
 								cq.getC().setQid(rs.getInt("cqid"));
 								cq.getC().setTitle(rs.getString("ct"));
 								cq.getC().setQuota(rs.getInt("quota"));
 
 								questionList.add(cq);
 							}
 						} catch (SQLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 						return questionList;
 					}
 				});
 	}
 
 	public List<CompositeQuestion> getQuestionsForUser(Integer uid) {
 		List<Attempt> attempts = AttemptHelper.getInstance().getAttempt(uid);
 		Attempt last = (attempts != null && attempts.size() > 0) ? attempts
 				.get(0) : null;
 		if (last == null
 				|| Util.dateDiff(System.currentTimeMillis(), last.getDate()) >= 1) {
 			// Return offer questions.
 			return getRandomQuestions(offerQuestions);
 		} else {
 			// return no offer questions as user has already won today.
 			return getRandomQuestions(noOfferQuestions);
 		}
 	}
 
 	private List<CompositeQuestion> getRandomQuestions(
 			List<CompositeQuestion> orig) {
 		List<CompositeQuestion> copy = new LinkedList<CompositeQuestion>(orig);
 		Collections.shuffle(copy);
 		int size = copy.size() >= 3 ? 3 : copy.size();
 		return copy.subList(0, size);
 	}
 
 	public List<CompositeQuestion> getRandomQuestions() {
 		return getRandomQuestions(this.questions);
 	}
 
	public void likeQuestion(Question q) {
		q.setLikes(q.getLikes() + 1);
		insert(q);
	}
 
 	public CompositeQuestion evaluateAnswer(final Integer uid, Integer qid,
 			String userAnswer) throws Exception {
 		CompositeQuestion ret = questionByQID.get(qid);
 
 		if (ret == null)
 			return null;
 
 		if (userAnswer != null && userAnswer.equals(ret.getQ().getAnswer())) {
 			ret.setAnswer_status("Y");
 			// Generate voucher.
 			Coupon c = CouponHelper.getInstance().getCouponByQid(qid);
 			if (c == null) {
 				throw new Exception("Could not retrieve any coupon for qid: "
 						+ qid);
 			}
 			final Integer cid = c.getCid();
 
 			String vCode = UUID.randomUUID().toString().toUpperCase()
 					.replace("-", "");
 			int length = vCode.length() > VOUCHER_LEN ? VOUCHER_LEN : vCode
 					.length();
 			final String code = vCode.substring(0, length);
 			ret.setVoucher_code(code);
 
 			new Thread(new Runnable() {
 				public void run() {
 					VoucherHelper.getInstance().insert(cid, uid, code);
 				}
 			}).start();
 		} else {
 			ret.setAnswer_status("N");
 		}
 
 		// Insert into attempts table.
 
 		return ret;
 	}
 
 	public static void main(String[] args) throws Exception {
 		// List<CompositeQuestion> questions = QuestionHelper.getInstance()
 		// .getRandomQuestions();
 		// if (null == questions || questions.size() == 0)
 		// return;
 		//
 		// List<CompositeQuestion> ret = new ArrayList<CompositeQuestion>();
 		// try {
 		// for (CompositeQuestion q : questions) {
 		// ret.add(Util.toNonRevealMode(q));
 		// }
 		//
 		// System.out.println(Util.convertToJSON(ret));
 		// } catch (CloneNotSupportedException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 
 		System.out.println(QuestionHelper.getInstance().evaluateAnswer(1, 7,
 				"Barbeque-Nation"));
 	}
 }
