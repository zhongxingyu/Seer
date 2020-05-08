 package com.clommunicate.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.accounts.NetworkErrorException;
 
 public class WebApi {
 
 	public static boolean createProject(Project project)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("name", project.getName()));
 		parameter_list.add(new BasicNameValuePair("description", project
 				.getDescription()));
 		parameter_list.add(new BasicNameValuePair("owner", String
 				.valueOf(project.getOwner_id())));
 		parameter_list.add(new BasicNameValuePair("deadline", project
 				.getDeadline()));
 
 		ArrayList<User> users = project.getMember_list();
 		for (int i = 0; i < users.size(); i++) {
 			parameter_list.add(new BasicNameValuePair("members[" + i + "]",
 					String.valueOf(users.get(i).getId())));
 			System.err.println(i);
 		}
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/NewProject.php");
		
		/*System.err.println(project.getName() + "\n" + project.getDescription()
 				+ "\n" + String.valueOf(project.getOwner_id()) + "\n"
				+ project.getEnd_date());*/
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean createTask(Task task) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("name", task.getName()));
 		parameter_list.add(new BasicNameValuePair("description", task
 				.getDescription()));
 		parameter_list.add(new BasicNameValuePair("owner", String.valueOf(task
 				.getOwner())));
 		parameter_list.add(new BasicNameValuePair("type", String.valueOf(task
 				.getType())));
 		parameter_list.add(new BasicNameValuePair("asigned", String
 				.valueOf(task.getAsigned_id())));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/NewTask.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			if (jo.getBoolean("state"))
 				WebApi.fillClommunicateUser(User.user);
 
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean createComment(Comment comment)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("text", comment.getText()));
 		parameter_list.add(new BasicNameValuePair("owner", String
 				.valueOf(comment.getOwner())));
 		parameter_list.add(new BasicNameValuePair("author", String
 				.valueOf(comment.getAuthor())));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/NewComment.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			if (jo.getBoolean("state"))
 				WebApi.fillClommunicateUser(User.user);
 
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean updateTask(Task task) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(task
 				.getId())));
 		parameter_list.add(new BasicNameValuePair("name", task.getName()));
 		parameter_list.add(new BasicNameValuePair("description", task
 				.getDescription()));
 		parameter_list.add(new BasicNameValuePair("type", String.valueOf(task
 				.getType())));
 		parameter_list.add(new BasicNameValuePair("asigned", String
 				.valueOf(task.getAsigned_id())));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/updateTask.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			if (jo.getBoolean("state"))
 				WebApi.fillClommunicateUser(User.user);
 
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	private static HttpEntity DoGet(String acces_token) {
 
 		/* Creare HttRequestului Post si transmiterea acestuia */
 		HttpClient hc = new DefaultHttpClient();
 		HttpGet hg = new HttpGet(
 				"https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="
 						+ acces_token);
 
 		/* Receptionarea raspunsului la request shi returnarea */
 		HttpResponse hr = null;
 		try {
 
 			hr = hc.execute(hg);
 
 		} catch (ClientProtocolException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		}
 		hc = null;
 		hg = null;
 
 		return hr.getEntity();
 
 	}
 
 	private static HttpEntity DoPost(ArrayList<NameValuePair> parameter_list,
 			HttpPost hp) {
 
 		/* Creare HttRequestului Post si transmiterea acestuia */
 		HttpClient hc = new DefaultHttpClient();
 
 		try {
			hp.setEntity(new UrlEncodedFormEntity(parameter_list,"UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		parameter_list.clear();
 		parameter_list = null;
 
 		/* Receptionarea raspunsului la request shi returnarea */
 		HttpResponse hr = null;
 		try {
 
 			hr = hc.execute(hp);
 
 		} catch (ClientProtocolException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		}
 		hc = null;
 		hp = null;
 
 		if (hr == null)
 			return null;
 		else
 			return hr.getEntity();
 
 	}
 
 	public static void fillClommunicateUser(User user)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("email", user.getEmail()));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/UserProjects.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			if (jo.getBoolean("state")) {
 				user.setProjects(jo.getInt("projects"));
 				user.setPartIn(jo.getInt("part_in"));
 			}
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 	}
 
 	public static boolean finishProject(int id) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/FinishProject.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static User getClommunicateUser(String email)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("email", email));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/User.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			if (jo.getBoolean("state")) {
 				return new User(jo.getInt("id"), jo.getString("email"),
 						jo.getString("name"), jo.getBoolean("gender"),
 						jo.getString("locale"), jo.getString("photo"),
 						jo.getInt("projects"), jo.getInt("part_in"));
 			}
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return null;
 
 	}
 
 	public static Task getTask(int id) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/Task.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			if (jo.getBoolean("state")) {
 				return new Task(jo.getInt("id"), jo.getString("name"),
 						jo.getString("description"), jo.getInt("type"),
 						jo.getString("start_date"), jo.getString("end_date"),
 						((jo.getInt("completed") == 1) ? true : false),
 						jo.getInt("owner"), new User(jo.getInt("uid"),
 								jo.getString("uemail"), jo.getString("uname"),
 								jo.getString("uphoto")));
 			}
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return null;
 
 	}
 
 	public static User getGoogleUser(String acces_token, String email)
 			throws NetworkErrorException {
 
 		HttpEntity he = DoGet(acces_token);
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 
 			String picture = null;
 			try {
 				picture = jo.getString("picture");
 			} catch (JSONException e) {
 				picture = null;
 			}
 
 			boolean gen = true;
 			try {
 				gen = (jo.getString("gender").compareToIgnoreCase("male") == 0) ? true
 						: false;
 			} catch (JSONException e) {
 
 			}
 
 			String loc = "en-GB";
 			try {
 				loc = jo.getString("locale");
 			} catch (JSONException e) {
 
 			}
 
 			User aux = new User(email, jo.getString("name"), gen, loc, picture);
 			return aux;
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return null;
 
 	}
 
 	public static ArrayList<Project> getProjectList(int user_id, boolean partIn)
 			throws NetworkErrorException {
 
 		ArrayList<Project> aux = new ArrayList<Project>(0);
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("user_id", String
 				.valueOf(user_id)));
 
 		HttpPost hp = null;
 
 		if (partIn)
 			hp = new HttpPost(
 					"http://clommunicate.freehosting.md/PartInProjects.php");
 		else
 			hp = new HttpPost(
 					"http://clommunicate.freehosting.md/CreatedProjects.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONArray ja = new JSONArray(sb.toString());
 			JSONObject jo = null;
 			for (int i = 0; i < ja.length(); i++) {
 
 				jo = ja.getJSONObject(i);
 				aux.add(new Project(jo.getInt("id"), jo.getString("name"), jo
 						.getString("description"), jo.getString("start_date"),
 						jo.getString("deadline"), jo.getString("end_date"), jo
 								.getInt("owner")));
 
 			}
 
 			return aux;
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return aux;
 
 	}
 
 	public static ArrayList<Task> getTaskList(int id)
 			throws NetworkErrorException {
 
 		ArrayList<Task> aux = new ArrayList<Task>(0);
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = null;
 
 		hp = new HttpPost("http://clommunicate.freehosting.md/TaskList.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONArray ja = new JSONArray(sb.toString());
 			JSONObject jo = null;
 			for (int i = 0; i < ja.length(); i++) {
 
 				jo = ja.getJSONObject(i);
 				aux.add(new Task(jo.getInt("id"), jo.getString("name"), jo
 						.getString("description"), jo.getInt("type"), jo
 						.getString("start_date"), jo.getString("end_date"),
 						((jo.getInt("completed") == 1) ? true : false), jo
 								.getInt("owner")));
 
 			}
 
 			return aux;
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return aux;
 
 	}
 
 	public static ArrayList<Comment> getCommentList(int id)
 			throws NetworkErrorException {
 
 		ArrayList<Comment> aux = new ArrayList<Comment>(0);
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = null;
 
 		hp = new HttpPost("http://clommunicate.freehosting.md/CommentList.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONArray ja = new JSONArray(sb.toString());
 			JSONObject jo = null;
 			for (int i = 0; i < ja.length(); i++) {
 
 				jo = ja.getJSONObject(i);
 				aux.add(new Comment(jo.getInt("id"), jo.getString("text"), jo
 						.getInt("author"), jo.getString("time")));
 
 			}
 
 			return aux;
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return aux;
 
 	}
 
 	public static ArrayList<User> getProjectMembers(int id)
 			throws NetworkErrorException {
 
 		ArrayList<User> aux = new ArrayList<User>(0);
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = null;
 
 		hp = new HttpPost(
 				"http://clommunicate.freehosting.md/ProjectMembers.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONArray ja = new JSONArray(sb.toString());
 			JSONObject jo = null;
 			for (int i = 0; i < ja.length(); i++) {
 
 				jo = ja.getJSONObject(i);
 				aux.add(new User(jo.getInt("id"), jo.getString("email"), jo
 						.getString("name"), jo.getString("photo")));
 
 			}
 
 			return aux;
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return aux;
 
 	}
 
 	public static boolean login(String email) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("email", email));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/CheckUser.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("status");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean register(User user) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("email", user.getEmail()));
 		parameter_list.add(new BasicNameValuePair("name", user.getName()));
 		parameter_list
 				.add(new BasicNameValuePair("location", user.getLocale()));
 		parameter_list
 				.add(new BasicNameValuePair("photo", user.getPictureURL()));
 		parameter_list.add(new BasicNameValuePair("gender",
 				(user.getGender() == Gender.Male) ? "1" : "0"));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/RegisterUser.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean addMember(int pid, int uid)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("pid", String.valueOf(pid)));
 		parameter_list.add(new BasicNameValuePair("uid", String.valueOf(uid)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/AddProjectMember.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean removeMember(int pid, int uid)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("pid", String.valueOf(pid)));
 		parameter_list.add(new BasicNameValuePair("uid", String.valueOf(uid)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/RemoveProjectMember.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean removeProject(int id) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/RemoveProject.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean removeTask(int id) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/RemoveTask.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean removeComment(int id) throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/RemoveComment.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 	public static boolean completeTask(int id, int completed)
 			throws NetworkErrorException {
 
 		ArrayList<NameValuePair> parameter_list = new ArrayList<NameValuePair>(
 				0);
 		parameter_list.add(new BasicNameValuePair("id", String.valueOf(id)));
 		parameter_list.add(new BasicNameValuePair("completed", String
 				.valueOf(completed)));
 
 		HttpPost hp = new HttpPost(
 				"http://clommunicate.freehosting.md/CompleteTask.php");
 
 		/* Receptionarea entitatii din raspuns shi decodarea JSON */
 		HttpEntity he = DoPost(parameter_list, hp);
 
 		if (he == null)
 			throw new NetworkErrorException();
 		try {
 
 			InputStream is = he.getContent();
 			he = null;
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "utf-8"), 8);
 			StringBuilder sb = new StringBuilder();
 
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 
 				sb.append(line + "\n");
 			}
 
 			JSONObject jo = new JSONObject(sb.toString());
 			return jo.getBoolean("state");
 
 		} catch (IllegalStateException e) {
 
 			e.printStackTrace();
 
 		} catch (IOException e) {
 
 			e.printStackTrace();
 
 		} catch (JSONException e) {
 
 			e.printStackTrace();
 
 		}
 
 		return false;
 
 	}
 
 }
