 /**
  * een hulpklassen die vanalles naar de 
  * database wegschrijft en terug ophaalt
  * @author vrolijkx
  */
 package Util;
 
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.persistence.EntityExistsException;
 
 import network.Server;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import BussinesLayer.Answer;
 import BussinesLayer.Jury;
 import BussinesLayer.Player;
 import BussinesLayer.QuestionRound;
 import BussinesLayer.Quiz;
 import BussinesLayer.QuizMaster;
 import BussinesLayer.Team;
 import BussinesLayer.User;
 import BussinesLayer.questions.MultipleChoise;
 import BussinesLayer.questions.MusicQuestion;
 import BussinesLayer.questions.PictureQuestion;
 import BussinesLayer.questions.Question;
 import BussinesLayer.questions.StandardQuestion;
 import BussinesLayer.questions.VideoQuestion;
 import Protocol.requests.AnswerRequest;
 import Protocol.requests.ConnectToQuizRequest;
 import Protocol.requests.CreateTeamRequest;
 import Protocol.requests.CreateUserRequest;
 import Protocol.requests.GetTeamsRequest;
 import Protocol.requests.LoginRequest;
 import Protocol.requests.PictureRequest;
 import Protocol.requests.TeamLoginRequest;
 import Protocol.responses.AnswerResponse;
 import Protocol.responses.GetTeamsResponse;
 import Protocol.responses.LoginResponse;
 import Protocol.responses.LoginResponse.UserType;
 import Protocol.responses.PictureResponse;
 import Protocol.responses.TeamLoginResponse;
 import Protocol.responses.TimeOutResponse;
 import Protocol.submits.CorrectSubmit;
 import Protocol.submits.QuestionSubmit;
 import Protocol.submits.QuestionSubmit.QuestionType;
 import Protocol.submits.RoundSubmit;
 
 public class DatabaseUtil {
 	//map met Al verzonden antwoorden
 	private static ConcurrentHashMap<Integer, Answer> sendAnswers = new ConcurrentHashMap<Integer, Answer>();
 
 	/**
 	 * sla een object op in de database
 	 * @param b object to be saved in the datbase
 	 */
 	public static void saveObject(Object b) {
 		Session s = ConnectionUtil.getSession();
 		Transaction t = s.beginTransaction();
 		s.saveOrUpdate(b);
 		t.commit();
 	}
 
 	/**
 	 * slaat alle meegegeven objecten op.
 	 * een rollback wordt uitgevoerd bij een misluking.
 	 * @param b
 	 */
 	public static void saveObjects(Object... b) {
 		Session s = ConnectionUtil.getSession();
 		Transaction t = s.beginTransaction();
 		try {
 			for(Object o:b) {
 				s.saveOrUpdate(o);
 			}
 			t.commit();
 		} catch(Exception ex) {
 			//rollback uitvoeren en de exeption door geven
 			t.rollback();
 			throw ex;
 		}
 	}
 
 	public static User getUser(String username) {
 		Session s = ConnectionUtil.getSession();
 		User u = (User) s.createQuery("select u from User u where u.userName = :name")
 				.setParameter("name", username)
 				.uniqueResult();
 		return u;
 
 	}
 
 	public static User getUser(int userId) {
 		Session s = ConnectionUtil.getSession();
 		User u = (User) s.createQuery("select u from User u where u.id = :id")
 				.setParameter("id", userId)
 				.uniqueResult();
 		return u;
 	}
 
 	public static Team getTeam(String TeamName, int creatorId) {
 		Session s = ConnectionUtil.getSession();
 		Team t = (Team) s.createQuery("SELECT t FROM Team t WERE t.teamCreator.id = :id AND t.teamName = :name")
 				.setParameter("id", creatorId)
 				.setParameter("name", TeamName)
 				.uniqueResult();
 		return t;
 	}
 
 	public static Team getTeam(int teamId) {
 		Session s = ConnectionUtil.getSession();
 		return  (Team) s.createQuery("select t from Team t where t.teamId = :id")
 				.setParameter("id", teamId)
 				.uniqueResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Team> getTeams(int playerId) {
 		Session s = ConnectionUtil.getSession();
 		return s.createQuery("select p.teams from Player p where p.id = :id")
 				.setParameter("id", playerId)
 				.list();
 	}
 
 	public static Question getQuestion(int questionId) {
 		Session s = ConnectionUtil.getSession();
 		return  (Question) s.createQuery("select q from Question q where q.questionId = :id")
 				.setParameter("id", questionId)
 				.uniqueResult();
 	}
 
 	public static Answer getAnswer(int anwserId) {
 		Session s = ConnectionUtil.getSession();
 		return  (Answer) s.createQuery("select a from Answer a where a.answerId = :id")
 				.setParameter("id", anwserId)
 				.uniqueResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Answer> getUncorrectedAnswers(int quizId, int max_amount) {
 		Session s = ConnectionUtil.getSession();
 		return  s.createQuery("select a from Answer a where a.jury = null and a.quiz.quizID = :id")
 				.setParameter("id", quizId)
 				.setMaxResults(max_amount)
 				.list();
 	}
 
 	public static Quiz getQuiz(int id) {
 		Session s = ConnectionUtil.getSession();
 		Quiz q = (Quiz) s.createQuery("select q from Quiz q where q.quizID =:id")
 				.setParameter("id", id)
 				.uniqueResult();
 		return q;
 	}
 
 	public static List<Answer> getUncorrectedAnswers(int quizId) {
 		return getUncorrectedAnswers(quizId,10);
 	}
 
 	public static void handleLoginRequest(LoginRequest request) {
 		try {
 			User user = getUser(request.getUserName());
 
 			if(user == null) {
 				request.sendException("De user bestaat niet");
 				return;
 			} else if(!user.checkPassword(request.getPassword())) {
 				request.sendException("Incorrect passwoord");
 				return;
 			}
 
 			LoginResponse r = request.createResponse();
 			r.setUserName(user.getUserName());
 			r.setEmail(user.getEmail());
 			r.setFirstname(user.getFirstName());
 			r.setLastName(user.getLastName());
 			r.setUserId(user.getId());
 
 			if(user instanceof Jury) {
 				r.setUserType(UserType.JURY);
 			} else if(user instanceof Player) {
 				r.setUserType(UserType.PLAYER);
 			} else if(user instanceof QuizMaster) {
 				request.sendException("Als quizmaster moet u op de server inloggen");
 				return;
 			}
 			r.send();
 
 
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (Exception e) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}	
 	}
 
 	public static void handleCreateUserRequest(CreateUserRequest request) {
 		User user;
 		try {
 			if(request.getUserType().equals(UserType.PLAYER)) {
 				user = new Player(request.getUserName(), request.getPassword());
 			} else if(request.getUserType().equals(UserType.JURY)) {
 				user = new Jury(request.getUserName(), request.getPassword());	
 			} else {
 				//hier gebeurt iets wat we niet verwachten
 				throw new Exception();
 			}
 
 			user.setFirstName(request.getFirstName());
 			user.setLastName(request.getLastName());
 			user.setEmail(request.getEmail());
 
 			saveObject(user);
 
 
 			LoginResponse r = (LoginResponse) request.createResponse();
 			r.setEmail(user.getEmail());
 			r.setFirstname(user.getFirstName());
 			r.setLastName(user.getLastName());
 			r.setUserId(user.getId());
 
 			if(user instanceof Jury) {
 				r.setUserType(UserType.JURY);
 			} else if(user instanceof Player) {
 				r.setUserType(UserType.PLAYER);
 			} else if(user instanceof QuizMaster) {
 				request.sendException("Als quizmaster moet u op de server inloggen");
 				return;
 			}
 			r.send();
 
 		} catch(EntityExistsException ex) {
 			request.sendException("Deze username is al gebruikt");
 		} catch(HibernateException ex) {
 			request.sendException("Kon de gebruiker niet aanmaken");
 		} catch (Exception e) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 	}
 
 	public static void handleAnswerRequest(AnswerRequest request) {
 		try {
 			for(Answer a: getUncorrectedAnswers(request.getQuizId())) {
 				if(!sendAnswers.containsKey(a.getAnswerId())) {
 					sendAnswers.put(a.getAnswerId(), a);
 					AnswerResponse answer = request.createResponse();
 
 					answer.setQuestionID(a.getQuestion().getQuestionId());
 					answer.setAnswerId(a.getAnswerId());
 					answer.setCorrectAnswer(a.getQuestion().getCorrectAnswer());
 					answer.setQuestion(a.getQuestion().getQuestion());
 					answer.setGivenAnswer(a.getAnswer());
 					answer.setAnswerPerson(a.getTeam().getTeamName());
 					answer.send();
 
 					return;
 				}
 			}
 
 			TimeOutResponse r = new TimeOutResponse(request.getRequestId());
 			r.send();
 
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (Exception e) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 	};
 
 	public static void handleTeamLoginRequest(TeamLoginRequest request) {
 		try {
 			Team t = getTeam(request.getTeamId());
 			if(!t.checkPassword(request.getPassword())) {
 				request.sendException("Het wachwoord is incorrect");
 				return;
 			} 
 
 			TeamLoginResponse r = request.createResponse();
 			r.setCreatorId(t.getTeamCreator().getId());
 			for(Player p: t.getPlayers()) {
 				r.addPlayer(p.getId(), p.getUserName());
 			}
 
 			r.send();
 
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (Exception e) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 	}
 
 	public static void handleCreateTeamRequest(CreateTeamRequest request) {
 		try {
 			Player creator = (Player) getUser(request.getCreatorId());
 
 			Team t = new Team(request.getTeamName(), request.getPassword(), creator);
 
 			TeamLoginResponse r = request.createResponse();
 			r.setCreatorId(creator.getId());
 
 			for(int id : request.getPlayerIds()) {
 				Player p = (Player) getUser(id);
 				t.addPlayer(p);
 				r.addPlayer(p.getId(), p.getUserName());
 			}
 
 			saveObject(t);
 
 			r.setTeamId(t.getTeamId());
 			r.send();
 
 
 		} catch(EntityExistsException ex) {
 			request.sendException("Je hebt al een team met deze naam aangemaakt");
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (ClassCastException ex) {
 			request.sendException("Er is iemand bij die geen speler is");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 	}
 
 	public static void handleGetTeamsRequest(GetTeamsRequest request) {
 		try {
 			List<Team> teams = getTeams(request.getUserId());
 
 			GetTeamsResponse r = (GetTeamsResponse) request.createResponse();
 
 			for(Team t: teams) {
 				r.addTeamItem(t.getTeamName(), t.getTeamId());
 			}
 
 			r.send();
 
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (Exception e) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 
 	}
 
 	public static void handleCorrectSubmit(CorrectSubmit submit) {
 		try {
 			Answer a = getAnswer(submit.getAnswerId());
 			Jury j = (Jury) getUser(submit.getJurryId());
 			a.correct(j,submit.getScore());
 			saveObject(a);
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		sendAnswers.remove(submit.getAnswerId());
 	};
 
 	public static void handlePictureRequest(PictureRequest request) {
 		try {
 
 			//Todo: de afbeeldingen bufferen zodat de datbase niet word belast
 			PictureQuestion pic = (PictureQuestion) getQuestion(request.getQuestionId());
 			PictureResponse r = request.createResponse();
 			r.setPictureResource(pic.getPicture());
 			r.send();
 
 
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (ClassCastException ex) {
 			request.sendException("Dit is helemaal geen foto vraag");
 		} catch (Exception ex) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 	}
 
 	public static void handleConnectToQuiz(ConnectToQuizRequest request) {
 		try {
 			Quiz q = getQuiz(request.getQuizId());
 			Team t = getTeam(request.getTeamId());
 			if(q.getTeams().size() < q.getMaxTeams() || q.getTeams().contains(t) ) {
 				//dit team staat al in de lijst
 				if(!q.getTeams().contains(t)) {
 					q.addTeam(t);
 				}
 				//server zeggen dat dit hier een team zit
 				Server.getInstance().markRequestAsPlayingTeam(request.getRequestId());
 				request.createResponse().send();	
 
 			} else {
 				request.sendException("De quiz zit al aan zijn team limiet");
 			}
 
 
 		} catch(HibernateException ex) {
 			request.sendException("Fout bij het doorzoeken van de database");
 		} catch (ClassCastException ex) {
 			request.sendException("Dit is helemaal geen foto vraag");
 		} catch (Exception ex) {
 			request.sendException("Onverwachte fout voorgedaan");
 		}
 	}
 
 	public static void submitRound(Quiz quiz, QuestionRound round) {
 		RoundSubmit submit = new RoundSubmit(quiz.getQuizID(),
 				round.getRoundId(),
 				round.getQuestions().size(),
 				round.getName());
 
 		Server.getInstance().sendToPlayers(submit);
 
		for(Question q: round.getQuestions()) {
			submitQuestion(quiz, round,q);
 		}
 	}
 
 	public static void submitQuestion(Quiz quiz, QuestionRound round,Question q) {
 		QuestionType type;
 		String[] possibilities = null;
 		if(q instanceof MusicQuestion) {
 			type= QuestionType.MUSIC;
 		} else if (q instanceof MusicQuestion) {
 			type = QuestionType.MULTIPLECHOISE;
 			possibilities = ((MultipleChoise) q).getValues();
 		} else if (q instanceof PictureQuestion ) {
 			type = QuestionType.PICTURE;	
 		} else if(q instanceof VideoQuestion) {
 			type = QuestionType.MOVIE;
 		} else if(q instanceof StandardQuestion) {
 			type = QuestionType.OPEN;
 		} else {
 			//als dit zou gebeuren is er iets geks aan de hand
 			return;
 		}
 
 
 		QuestionSubmit submit = new QuestionSubmit(type,
 				quiz.getQuizID(),
 				round.getRoundId(),
 				q.getQuestionId(),
 				q.getQuestion(),possibilities);

		Server.getInstance().sendToPlayers(submit);
 
 	}
 
 	public static void correctMultipleChoise(Answer a) {
 		if(!(a.getQuestion() instanceof MultipleChoise)) {
 			throw new IllegalArgumentException("kan alleen multiple choise exceptions verbeteren");
 		}
 
 		Jury computer = (Jury) getUser("Computer");
 		MultipleChoise q = (MultipleChoise) a.getQuestion();
 
 		if(a.getAnswer().equals(q.getCorrectAnswer())) {
 			a.correct(computer, a.getMaxScore());
 		} else {
 			a.correct(computer, 0);
 		}
 
 		saveObject(a);
 	}
 
 }
