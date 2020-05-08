 package org.softwaresynthesis.mytalk.server.authentication.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.abook.UserData;
 import org.softwaresynthesis.mytalk.server.authentication.security.AESAlgorithm;
 import org.softwaresynthesis.mytalk.server.authentication.security.ISecurityStrategy;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 /**
  * Controller che gestisce la registrazione
  * di un nuovo utente
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
 public final class RegisterController extends AbstractController 
 {
 	/**
 	 * Esegue la registrazione di un nuovo utente
 	 * nel sistema MyTalk
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException 
 	{
 		DataPersistanceManager dao = null;
 		ISecurityStrategy strategy = null;
 		IUserData user = null;
 		PrintWriter writer = null;
 		String answer = null;
 		String mail = null;
 		String name = null;
 		String password = null;
 		String path = null;
 		String question = null;
 		String result = null;
 		String surname = null;
 		try
 		{
 			strategy = super.getSecurityStrategyFactory();
 			mail = this.getParameter(request, "username");
 			password = this.getParameter(request, "password");
 			password = strategy.encode(password);
 			question = this.getParameter(request, "question");
 			answer = this.getParameter(request, "answer");
 			answer = strategy.encode(answer);
 			name = this.getParameter(request, "name");
 			surname = this.getParameter(request, "surname");
 			path = this.getParameter(request, "picturePath");
 			user = new UserData();
 			user.setMail(mail);
 			user.setPassword(password);
 			user.setQuestion(question);
 			user.setAnswer(answer);
 			user.setName(name);
 			user.setSurname(surname);
 			if (path.equals("") == true)
 			{
 				path = "img/contactImg/Default.png";
 			}
 			else
 			{
 				//TODO Salvare immagine utente
 			}
 			user.setPath(path);
 			dao = super.getDAOFactory();
 			dao.insert(user);
 			result = "{\"name\":\"" + user.getName() + "\"";
 			result += ", \"surname\":\"" + user.getSurname() + "\"";
 			result += ", \"email\":\"" + user.getMail() + "\"";
 			result += ", \"id\":\"" + user.getId() + "\"";
 			result += ", \"picturePath\":\"" + user.getPath() + "\"}";
 		}
 		catch (Exception ex)
 		{
			result = "false";
 		}
 		finally
 		{
 			writer = response.getWriter();
 			writer.write(result);
 		}
 	}
 	
 	/**
 	 * Ridefinizione del metodo di controllo
 	 * 
 	 * @return	true
 	 */
 	@Override
 	protected boolean check(HttpServletRequest request)
 	{
 		return true;
 	}
 	
 	/**
 	 * Metodo factory per la creazione dell'algoritmo
 	 * di crittografia usato durante la procedura di
 	 * login
 	 * 
 	 * @return	{@link ISecurityStrategy} algoritmo di
 	 * 			crittografia
 	 */
 	ISecurityStrategy strategyFactory()
 	{
 		return new AESAlgorithm();
 	}
 	
 	/**
 	 * Metodo factory per la creazione del punto di
 	 * accesso verso il database
 	 * 
 	 * @return 	{@link DataPersistanceManager} punto di accesso
 	 * 			verso il database
 	 */
 	DataPersistanceManager DAOFactory()
 	{
 		return new DataPersistanceManager();
 	}
 	
 	/**
 	 * Restituisce il parametro richiesto per completare
 	 * la registrazione
 	 * 
 	 * @param request
 	 * @param parameter
 	 * @return
 	 */
 	private String getParameter(HttpServletRequest request, String parameter)
 	{
 		String result = request.getParameter(parameter);
 		if (result == null)
 		{
 			result = "";
 		}
 		return result;
 	}
 }
