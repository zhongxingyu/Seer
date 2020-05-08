 package org.softwaresynthesis.mytalk.server.abook.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.softwaresynthesis.mytalk.server.AbstractController;
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.DataPersistanceManager;
 
 /**
  * Controller che ha il compito di
  * cercare un contatto nel sistema
  * MyTalk
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
 public class SearchController extends AbstractController 
 {
 	/**
 	 * Esegue la ricerca di un contatto
 	 * registrato nel sistema MyTalk
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException 
 	{
 		DataPersistanceManager dao = null;
 		Iterator<IUserData> iterator = null;
 		IUserData entry = null;
 		List<IUserData> resultSet = null;
 		PrintWriter writer = null;
 		String parameter = null;
 		String result = null;
 		try
 		{
 			parameter = request.getParameter("param");
			dao = getDAOFactory();
 			resultSet = dao.getUserDatas(parameter, parameter, parameter);
 			iterator = resultSet.iterator();
 			result = "{";
 			while (iterator.hasNext())
 			{
 				entry = iterator.next();
 				result += "\"" + entry.getId() + "\":";
 				result += "{\"name\":\"" + entry.getName() + "\"";
 				result += ", \"surname\":\"" + entry.getSurname() + "\"";
 				result += ", \"email\":\"" + entry.getMail() + "\"";
 				result += ", \"id\":\"" + entry.getId() + "\"";
 				result += ", \"picturePath\":\"" + entry.getPath() + "\"";
 				result += ", \"blocked\":false}";
 				if (iterator.hasNext() == true)
 				{
 					result += ",";
 				}
 				result += "}";
 			}
 		}
 		catch (Exception ex)
 		{
 			result = "null";
 		}
 		finally
 		{
 			writer = response.getWriter();
 			writer.write(result);
 		}
 	}
 }
