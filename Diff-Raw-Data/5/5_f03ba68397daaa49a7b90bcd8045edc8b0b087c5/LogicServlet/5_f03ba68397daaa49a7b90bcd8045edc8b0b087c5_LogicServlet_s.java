 package de.quiz.Servlets;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.catalina.websocket.MessageInbound;
 import org.apache.catalina.websocket.StreamInbound;
 import org.apache.catalina.websocket.WebSocketServlet;
 import org.apache.catalina.websocket.WsOutbound;
 
 import de.fhwgt.quiz.application.Question;
 import de.fhwgt.quiz.application.Quiz;
 import de.fhwgt.quiz.error.QuizError;
 import de.quiz.LoggingManager.ILoggingManager;
 import de.quiz.ServiceManager.ServiceManager;
 import de.quiz.User.IUser;
 import de.quiz.UserManager.IUserManager;
 import de.quiz.Utility.TimeOut;
 
 /**
  * WebSocketServlet implementation class LogicServlet. This servlet handles the
  * in game process.
  * 
  * @author Patrick Na
  */
 @WebServlet(description = "connection to game the logic", urlPatterns = { "/LogicServlet" })
 public class LogicServlet extends WebSocketServlet {
 	private static final long serialVersionUID = 1L;
 	private static CopyOnWriteArrayList<LogicMessageInbound> myInList = new CopyOnWriteArrayList<LogicMessageInbound>();
 	private final AtomicInteger connectionIds = new AtomicInteger(0);
 	
 	private Question currentQuestion;
 
 	@Override
 	protected StreamInbound createWebSocketInbound(String arg0,
 			HttpServletRequest arg1) {
 		return new LogicMessageInbound(connectionIds.incrementAndGet(),
 				arg1.getSession());
 	}
 
 	private class LogicMessageInbound extends MessageInbound {
 		private WsOutbound myOutbound;
 
 		private final int playerID;
 		private final HttpSession playerSession;
 
 		private LogicMessageInbound(int id, HttpSession session) {
 			this.playerID = id;
 			this.playerSession = session;
 		}
 
 		@Override
 		protected void onClose(int status) {
 			IUser tmp = this.getUserObject();
 			if (tmp != null)
 				this.getUserObject().setWSID(-1);
 			myInList.remove(this);
 			ServiceManager.getInstance().getService(ILoggingManager.class)
 					.log("Login client closed.");
 		}
 
 		@Override
 		protected void onOpen(WsOutbound outbound) {
 
 			this.myOutbound = outbound;
 
 			ServiceManager.getInstance().getService(IUserManager.class)
 					.getUserBySession(playerSession).setWSID(playerID);
 			myInList.add(this);
 			ServiceManager.getInstance().getService(ILoggingManager.class)
 					.log("Login client open.");
 		}
 
 		@Override
 		protected void onBinaryMessage(ByteBuffer arg0) throws IOException {
 			// this application does not expect binary data
 			throw new UnsupportedOperationException(
 					"Binary message not supported.");
 		}
 
 		@Override
 		protected void onTextMessage(CharBuffer arg0) throws IOException {
 
 			System.out.println("LogicServlet:");
			System.out.println(arg0.toString());
 
 			if (arg0.toString().equals("8")) {
 				this.onCase8();
 			}
 
 			else if (arg0.toString().equals("110")) {
 				this.onCase11("0");
 			}
 
 			else if (arg0.toString().equals("111")) {
 				this.onCase11("1");
 			}
 
 			else if (arg0.toString().equals("112")) {
 				this.onCase11("2");
 			}
 
 			else if (arg0.toString().equals("113")) {
 				this.onCase11("3");
 			}
 
 		}
 
 		/**
 		 * for future use if a broadcast should be necessary
 		 * 
 		 * @param message
 		 */
 		private void broadcast(String message) {
 			for (LogicMessageInbound connection : myInList) {
 				try {
 					CharBuffer buffer = CharBuffer.wrap(message);
 					connection.getWsOutbound().writeTextMessage(buffer);
 				} catch (IOException ignore) {
 					// Ignore
 				}
 			}
 		}
 
 		/**
 		 * Returns the user object which belongs to this MessageInbound
 		 * 
 		 * @return IUser
 		 */
 		public IUser getUserObject() {
 			return ServiceManager.getInstance().getService(IUserManager.class)
 					.getUserByWSID(playerID);
 		}
 
 		private void onCase8() {
 			QuizError error = new QuizError();
 			currentQuestion = Quiz.getInstance().requestQuestion(
 					this.getUserObject().getPlayerObject(), new TimeOut(),
 					error);
 
			System.out.println("Case 8");

 			if (currentQuestion != null) {
 				System.out.println("Der Index der korrekten Antwort ist"
 						+ String.valueOf(currentQuestion.getCorrectIndex()));
 
 				long timeout = currentQuestion.getTimeout();
 				String question = currentQuestion.getQuestion();
 				String answer1 = currentQuestion.getAnswerList().get(0);
 				String answer2 = currentQuestion.getAnswerList().get(1);
 				String answer3 = currentQuestion.getAnswerList().get(2);
 				String answer4 = currentQuestion.getAnswerList().get(3);
 
 				CharBuffer buffer = CharBuffer.wrap("9");
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				buffer = null;
 				buffer = CharBuffer.wrap(new Long(timeout).toString());
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				buffer = null;
 				buffer = CharBuffer.wrap(question);
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				buffer = null;
 				buffer = CharBuffer.wrap(answer1);
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				buffer = null;
 				buffer = CharBuffer.wrap(answer2);
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				buffer = null;
 				buffer = CharBuffer.wrap(answer3);
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				buffer = null;
 				buffer = CharBuffer.wrap(answer4);
 				try {
 					this.myOutbound.writeTextMessage(buffer);
 					this.myOutbound.flush();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 			else
 			{
 				System.out.println("Unerwarteter Fehler!");
 			}
 		}
 
 		private void onCase11(String answer) {
 			QuizError error = new QuizError();
 			Quiz.getInstance().answerQuestion(
 					this.getUserObject().getPlayerObject(), new Long(answer),
 					error);
 
 			// if(currentQuestion.validateAnswer(new Long(answer)))
 			// {
 			// }
 
 			String test = new String();
 			test = "11";
 			CharBuffer buffer = CharBuffer.wrap(test);
 			try {
 				this.myOutbound.writeTextMessage(buffer);
 				this.myOutbound.flush();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			buffer = null;
 			buffer = CharBuffer.wrap(String.valueOf(currentQuestion.getCorrectIndex()));
 			try {
 				this.myOutbound.writeTextMessage(buffer);
 				this.myOutbound.flush();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 	}
 
 	/**
 	 * Returns the stream object found with the given user id
 	 * 
 	 * @param id
 	 * @return LogicMessageInbound
 	 */
 	public LogicMessageInbound getStreamByUserID(int id) {
 		for (LogicMessageInbound connection : myInList) {
 			if (connection.playerID == id)
 				return connection;
 		}
 		return null;
 	}
 
 }
