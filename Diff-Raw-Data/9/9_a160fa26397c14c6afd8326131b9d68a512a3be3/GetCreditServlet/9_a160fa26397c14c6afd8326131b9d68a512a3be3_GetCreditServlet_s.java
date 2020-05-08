 package com.megadevs.savey.apis;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteStatement;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 import com.megadevs.savey.apis.SaveyUtils.IDS;
 
 public class GetCreditServlet extends HttpServlet {
 
 	private static final long serialVersionUID = 32224455872939913L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 	
 		SQLiteConnection db = null;
 		try {
 			String request = req.getParameter("value");
 
 			JsonReader jsonReader = new JsonReader(new StringReader(request));
 			jsonReader.beginObject();
 
 			String userID = "";
 			while (jsonReader.hasNext()) {
 				String name = jsonReader.nextName();
 				if (name.equals(IDS.USER_TASK_ID.getID()))
 					userID = jsonReader.nextString();
 			}
 
 			jsonReader.endObject();
 			jsonReader.close();
 
 			db = new SQLiteConnection(new File(SaveyUtils.getDatabasePath()));
 			db.open();
 			SQLiteStatement query = db.prepare("SELECT id_task,created_at FROM users_to_tasks WHERE users_to_tasks.id = " + userID);
 			
 			int idTask = -1;
 			String createdAt = "";
 			
 			if (query.step()) {
 				idTask = query.columnInt(0);
 				createdAt = query.columnString(1);
 			}
 			
 			// idTask should now have a value greater than 0 (an actual ID)
 
 			query.reset();
 			query = db.prepare("SELECT credit,duration FROM tasks WHERE tasks.id = " + idTask);
 			
 			double credit = -1;
 			String duration = "";
 			
 			if (query.step()) {
 				credit = query.columnDouble(0);
 				duration = query.columnString(1);
 			}
 			
 			System.out.println("Current time: " + System.currentTimeMillis());
 			System.out.println("Created at  : " + Long.parseLong(createdAt));
 			System.out.println("Delta       : " + (System.currentTimeMillis() - Long.parseLong(createdAt)));
 			System.out.println("Duration     : " + Long.parseLong(duration));
 			
 			boolean isValid = (System.currentTimeMillis() - Long.parseLong(createdAt) < Long.parseLong(duration) ? true : false); 
 			
 			JsonWriter writer = new JsonWriter(resp.getWriter());
 			writer.beginObject();
 			writer.name("valid");
 			writer.value(isValid);
 			writer.name("credit");
 			writer.value(credit);
 			writer.endObject();
 			
 			query.dispose();
 			
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		finally {
 			db.dispose();
 		}
 		
 	}
 	
 }
