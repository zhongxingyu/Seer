 package models.database.importers;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import models.Answer;
 import models.Question;
 import models.User;
 import models.database.Database;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class XMLParser extends DefaultHandler {
 	private final Map<Integer, User> idUserBase;
 	private final Map<Integer, Question> idQuestionBase;
 
 	private final List<ProtoAnswer> protoanswers;
 	private final List<ProtoQuestion> protoquestions;
 
 	private final ElementParser parser;
 
 	private final Action createUser;
 	private final Action createQuestion;
 	private final Action createAnswer;
 	private final Action makeConnections;
 
 	public XMLParser() {
 		this.idUserBase = new HashMap<Integer, User>();
 		this.idQuestionBase = new HashMap<Integer, Question>();
 
 		this.protoanswers = new LinkedList();
 		this.protoquestions = new LinkedList();
 
 		this.makeConnections = new Action() {
 			public void call(Element e) throws SemanticError {
 				makeConnections();
 			}
 		};
 
 		this.createAnswer = new Action() {
 			public void call(Element e) throws SemanticError {
 				createAnswer(e);
 			}
 		};
 
 		this.createUser = new Action() {
 			public void call(Element e) throws SemanticError {
 				createUser(e);
 			}
 		};
 
 		this.createQuestion = new Action() {
 			public void call(Element e) throws SemanticError {
 				createQuestion(e);
 			}
 		};
 
 		this.parser = getSyntax();
 	}
 
 	/**
 	 * Defines the standard syntax for the xml import.
 	 * 
 	 * @return
 	 */
 	private ElementParser getSyntax() {
 		Syntax syntax = new Syntax("syntax");
 		Syntax qa = syntax.by("QA");
 		qa
 				.by("users")
 					.by("user")
 						.read("displayname")
 						.read("age")
 						.read("ismoderator")
 						.read("email")
 						.read("password")
 						.read("aboutme")
 						.read("location")
 						.read("website")
 						.call(this.createUser);
 		qa
 				.by("questions")
 					.by("question")
 						.read("ownerid")
 						.read("creationdate")
 						.read("lastactivity")
 						.read("body")
 						.read("title")
 						.read("lastedit")
 						.read("acceptedanswer")
 						.by("tags")
 							.read("tag")
 						.end()
 						.call(this.createQuestion);
 		qa
 				.by("answers")
 					.by("answer")
 						.read("ownerid")
 						.read("questionid")
 						.read("creationdate")
 						.read("lastactivity")
 						.read("body")
 						.read("title")
 						.read("lastedit")
 						.read("accepted")
 						.call(this.createAnswer);
 		qa.call(this.makeConnections);
 		return new ElementParser(syntax);
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes atts) {
 		try {
 			Map<String, String> attributes = new HashMap();
 			for (int i = 0; i < atts.getLength(); i++) {
 				String attrname = atts.getLocalName(i);
 				attributes.put(attrname, atts.getValue(i));
 			}
 			this.parser.start(qName, attributes);
 		} catch (SemanticError e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void characters(char[] str, int start, int length) {
 		this.parser.text(String.copyValueOf(str, start, length));
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName) {
 		try {
 			this.parser.end();
 		} catch (SemanticError e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create an User from the given element or throws a SemanticError if the
 	 * element does not define an User.
 	 * 
 	 * @param e
 	 */
 	private void createUser(Element e) throws SemanticError {
 		String name = e.getText("displayname");
 		String password = e.getText("password");
 		User user = Database.get().users().register(name, password);
 		user.setEmail(e.getText("email"));
 		Integer age = new Integer(e.getText("age"));
 		if (age != -1) {
 			Calendar pseudobirthday = GregorianCalendar.getInstance();
 			pseudobirthday.add(Calendar.YEAR, -age);
 			user.setDateOfBirth(pseudobirthday.getTime());
 		}
 		user.setBiography(e.getText("aboutme"));
 		user.setWebsite(e.getText("website"));
 		user.setModerator(e.getText("ismoderator").equals("true"));
 		int id = new Integer(e.getArg("id"));
 		this.idUserBase.put(id, user);
 	}
 
 	/**
 	 * Create a question or throw a SemanticError, if it does
 	 * not define a valid question.
 	 * 
 	 * @param e
 	 */
 	private void createQuestion(Element e) throws SemanticError {
 		ProtoQuestion question = new ProtoQuestion();
 		try {
 			question.body = e.getText("body");
 			question.id = new Integer(e.getArg("id"));
 			question.creation = new Date(new Integer(e.getText("creationdate")));
 			question.ownerid = e.getText("ownerid").equals("") ? -1
 					: new Integer(e.getText("ownerid"));
			for (Element tagE : e.get("tags").get(0).get("tag")) {
				question.tags += " " + tagE.getText();
 			}
 			this.protoquestions.add(question);
 		} catch (Throwable t) {
 			throw new SemanticError("Question #" + e.getArg("id")
 					+ " is invalid");
 		}
 	}
 
 	protected void createAnswer(Element e) throws SemanticError {
 		ProtoAnswer answer = new ProtoAnswer();
 		try {
 			answer.ownerid = new Integer(e.getText("ownerid"));
 			answer.questionid = new Integer(e.getText("questionid"));
 			answer.body = e.getText("body");
 			answer.accepted = e.getText("accepted").equals("true");
 			answer.id = e.getArg("id") == null ? -1 : new Integer(e
 					.getArg("id"));
 			answer.creation = new Date(new Integer(e.getText("creationdate")));
 			this.protoanswers.add(answer);
 		} catch (Throwable t) {
 			throw new SemanticError("Answer #" + e.getText("ownerid")
 					+ " is invalid");
 		}
 	}
 
 	protected void makeConnections() throws SemanticError {
 		for (ProtoQuestion protoquestion : this.protoquestions) {
 			User owner = this.idUserBase.get(protoquestion.ownerid);
 
 			if (owner == null && protoquestion.ownerid != -1)
 				throw new SemanticError("No valid user: "
 						+ protoquestion.ownerid);
 
 			Question question = Database.get().questions().add(owner,
 					protoquestion.body);
 			question.setTimestamp(protoquestion.creation);
 			this.idQuestionBase.put(protoquestion.id, question);
			question.setTagString(protoquestion.tags);
 		}
 		for (ProtoAnswer ans : this.protoanswers) {
 			Question question = this.idQuestionBase.get(ans.questionid);
 			if (question == null)
 				throw new SemanticError("No valid question: "
 						+ ans.ownerid);
 
 			User owner = this.idUserBase.get(ans.ownerid);
 
 			Answer answer = question.answer(owner, ans.body);
 			answer.setTimestamp(ans.creation);
 			if (ans.accepted) {
 				question.setBestAnswer(answer);
 			}
 		}
 	}
 
 	private class ProtoQuestion {
		private String tags = "";
 		private int ownerid;
 		private Date creation;
 		private String body;
 		private int id;
 	}
 
 	private class ProtoAnswer {
 		private int ownerid, questionid;
 		private Date creation;
 		private String body;
 		private boolean accepted;
 		@SuppressWarnings("unused")
 		private int id;
 	}
 }
