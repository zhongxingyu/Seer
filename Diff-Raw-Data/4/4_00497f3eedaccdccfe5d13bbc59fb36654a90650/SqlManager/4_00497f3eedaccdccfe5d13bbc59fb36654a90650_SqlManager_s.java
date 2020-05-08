 /* * Copyright 2012 Oregon State University.
  * All Rights Reserved. 
  *  
  * Permission to use, copy, modify, and distribute this software and its 
  * documentation for educational, research and non-profit purposes, without fee, 
  * and without a written agreement is hereby granted, provided that the above 
  * copyright notice, this paragraph and the following three paragraphs appear in 
  * all copies. 
  *
  * Permission to incorporate this software into commercial products may be 
  * obtained by contacting OREGON STATE UNIVERSITY Office for 
  * Commercialization and Corporate Development.
  *
  * This software program and documentation are copyrighted by OREGON STATE
  * UNIVERSITY. The software program and documentation are supplied "as is", 
  * without any accompanying services from the University. The University does 
  * not warrant that the operation of the program will be uninterrupted or errorfree. 
  * The end-user understands that the program was developed for research 
  * purposes and is advised not to rely exclusively on the program for any reason. 
  *
  * IN NO EVENT SHALL OREGON STATE UNIVERSITY BE LIABLE TO ANY PARTY 
  * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
  * DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS 
  * SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE OREGON STATE  
  * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
  * OREGON STATE UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE AND ANY 
  * STATUTORY WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE PROVIDED 
  * HEREUNDER IS ON AN "AS IS" BASIS, AND OREGON STATE UNIVERSITY HAS 
  * NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
  * ENHANCEMENTS, OR MODIFICATIONS. 
  * 
  */
 package cgrb.eta.server.mysql;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import cgrb.eta.server.CommunicationImpl;
 import cgrb.eta.server.Notifier;
 import cgrb.eta.server.settings.Setting;
 import cgrb.eta.server.settings.Settings;
 import cgrb.eta.shared.ETAEvent;
 import cgrb.eta.shared.FavoriteSearchItem;
 import cgrb.eta.shared.JobSearchItem;
 import cgrb.eta.shared.RequestResponse;
 import cgrb.eta.shared.ResultSearchItem;
 import cgrb.eta.shared.ResultSettings;
 import cgrb.eta.shared.SearchResultItem;
 import cgrb.eta.shared.WrapperSearchItem;
 import cgrb.eta.shared.etatype.Cluster;
 import cgrb.eta.shared.etatype.ETATypeEvent;
 import cgrb.eta.shared.etatype.Job;
 import cgrb.eta.shared.etatype.JobNote;
 import cgrb.eta.shared.etatype.PendingCluster;
 import cgrb.eta.shared.etatype.Plugin;
 import cgrb.eta.shared.etatype.RequestItem;
 import cgrb.eta.shared.etatype.Share;
 import cgrb.eta.shared.etatype.User;
 import cgrb.eta.shared.etatype.UserResult;
 import cgrb.eta.shared.etatype.UserWrapper;
 import cgrb.eta.shared.pipeline.PipeComponent;
 import cgrb.eta.shared.pipeline.PipeWrapper;
 import cgrb.eta.shared.pipeline.Pipeline;
 import cgrb.eta.shared.pipeline.PipelineWrapper;
 import cgrb.eta.shared.pipeline.UserPipeline;
 import cgrb.eta.shared.wrapper.Input;
 import cgrb.eta.shared.wrapper.Output;
 import cgrb.eta.shared.wrapper.Wrapper;
 
 import com.mysql.jdbc.CommunicationsException;
 
 /**
  * @author boyda
  * 
  */
 public class SqlManager {
 	private static SqlManager instance = null;
 	private Connection connect;
 	private String database = "eta";
 	private Settings settings = Settings.getInstance();
 
 	public static String attemptConection(String sqlServerLoc, String sqlServerSchemata, String sqlServerUsername, String sqlServerUserPass) {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			DriverManager.getConnection("jdbc:mysql://" + sqlServerLoc + "/", sqlServerUsername, sqlServerUserPass).close();
 
 		} catch (Exception e) {
 			System.out.println(e.getLocalizedMessage());
 			return e.getLocalizedMessage();
 		}
 		Settings settings = Settings.getInstance();
 		settings.putSetting("sqlserver", new Setting(sqlServerLoc));
 		settings.putSetting("sqlusername", new Setting(sqlServerUsername));
 		settings.putSetting("sqluserpass", new Setting(sqlServerUserPass));
 		settings.putSetting("sqlschemata", new Setting(sqlServerSchemata));
 		// instance = new SqlManager();
 		return "";
 	}
 
 	/**
 	 * Returns a SqlManager object. This is the only way to get this object
 	 * 
 	 * @return The singleton instance of SqlManager
 	 */
 	public static SqlManager getInstance() {
 		return (instance == null) ? instance = new SqlManager() : instance;
 	}
 
 	/**
 	 * The constructor for SqlManager its private so this can't be created outside of this class and only one can ever be created
 	 */
 	private SqlManager() {
 		getConnection();
 	}
 
 	public Connection getConnect() {
 		try {
 			if (connect == null || connect.isClosed()) {
 				getConnection();
 			} else {
 				connect.setAutoCommit(true);
 			}
 		} catch (CommunicationsException e2) {
 			getConnection();
 		} catch (SQLException e) {
 
 		}
 		return connect;
 	}
 
 	private void getConnection() {
 		String sqlServerLoc = settings.getSetting("sqlserver").getStringValue();
 		String sqlServerUsername = settings.getSetting("sqlusername").getStringValue();
 		String sqlServerUserPass = settings.getSetting("sqluserpass").getStringValue();
 		String sqlServerSchemata = settings.getSetting("sqlschemata").getStringValue();
 		database = sqlServerSchemata;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			connect = DriverManager.getConnection("jdbc:mysql://" + sqlServerLoc + "/" + sqlServerSchemata + "?autoReconnect=true", sqlServerUsername, sqlServerUserPass);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			try {
 				connect = DriverManager.getConnection("jdbc:mysql://" + sqlServerLoc + "/", sqlServerUsername, sqlServerUserPass);
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 
 		}
 	}
 
 	/**
 	 * This will add the job that is submitted to the database and return the id of the job in the database
 	 * 
 	 * @param job
 	 *          the job object to be added to the database
 	 * 
 	 * @return int the id of the job in the sql database
 	 */
 	public int addJob(Job job) {
 		String status = "Submitted";
 		if (job.getWaitingFor() > 0)
 			status = "Waiting for " + job.getWaitingFor();
 		if (job.getWrapper() != null) {
 			int jobId = executeUpdate(getPreparedStatement("insert into job values (null,?," + job.getUserId() + ",'" + status + "',null,'N/A'," + job.getWrapper().getId() + ",?," + job.getParent() + ",now(),null,null," + job.getWaitingFor() + ",?," + job.getPipeline() + ",false,?,0)", job.getName(),
 					job.getWorkingDir(), job.getSpecs(), job.isSaveStd() ? "!" : job.getStdoutPath()));
 			Vector<Input> inputs = job.getWrapper().getInputs();
 			for (Input input : inputs) {
 				if (input.getValue() != null && !input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 					executeUpdate(getPreparedStatement("insert into job_value values(null," + jobId + "," + input.getId() + ",? )", input.getValue()));
 				}
 			}
 			return jobId;
 		} else if (job.getPipelineObject() != null) {
 			int jobId = executeUpdate(getPreparedStatement("insert into job values (null,?," + job.getUserId() + ",'" + status + "',null,'N/A'," + 0 + ",?," + job.getParent() + ",now(),null,null," + job.getWaitingFor() + ",?," + job.getPipeline() + ",false,?,0)", job.getName(), job.getWorkingDir(), "",
 					job.getStdoutPath()));
 			Vector<Input> inputs = job.getPipelineObject().getInputs();
 			for (Input input : inputs) {
 				if (input.getValue() != null && !input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 					executeUpdate(getPreparedStatement("insert into job_value values(null," + jobId + "," + input.getId() + ",? )", input.getValue()));
 				}
 			}
 			return jobId;
 		}
 		return executeUpdate(getPreparedStatement("insert into job values (null,?," + job.getUserId() + ",'" + status + "',null,'N/A'," + 0 + ",?," + job.getParent() + ",now(),null,null," + job.getWaitingFor() + ",?," + job.getPipeline() + ",false,?,0)", job.getName(), "", "", job.getStdoutPath()));
 	}
 
 	/**
 	 * @param name
 	 *          The display name of the user to add. this is their full name
 	 * @param userName
 	 *          The login name that users will use to login. this can't be duplicated in the database
 	 * @param password
 	 *          The password for this account. If it is a local account there will be no password stored
 	 * @param level
 	 *          the security level for this user to have. 0- default, no access to admin functions 8- can edit public wrappers and add plugins 9- can edit ETA settings
 	 * @return int the id of the user that was createdF
 	 */
 	public int addUser(String name, String userName, String password, int level) {
 		Vector<String[]> temp = runQuery(getPreparedStatement("select id from user where username=?", userName));
 		if (temp.size() > 0) {
 			return Integer.parseInt(temp.get(0)[0]);
 		}
 		return executeUpdate(getPreparedStatement("insert into user (username,name,password,permission,email,phone) values  (?,?,password(?)," + level + ",'','')", userName, name, password));
 	}
 
 	public String[] checkUser(String user, String password) {
 		PreparedStatement statement;
 		try {
 			statement = getConnect().prepareStatement("select * from user u where u.username=? and password=password(?)");
 			statement.setString(1, user);
 			statement.setString(2, password);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				System.out.println("verified");
 				String[] data = new String[] { rs.getString("name"), "" + rs.getInt("permission"), "" + rs.getInt("id") };
 				rs.close();
 				statement.close();
 				return data;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public int executeUpdate(PreparedStatement statement) {
 		try {
 			statement.executeUpdate();
 			ResultSet rs = statement.getGeneratedKeys();
 			int key = -1;
 			if (rs.next()) {
 				key = rs.getInt(1);
 			}
 			rs.close();
 			statement.close();
 			return key;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return 0;
 	}
 
 	public Wrapper getWrapperFromId(int id) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs;
			rs = statement.executeQuery("select u.name,w.name,w.description,w.program,w.public,w.modified,w.id  from wrapper w left join user u on u.id=w.creator where w.id=" + id);
 			Wrapper ret = null;
 			if (rs.next()) {
 				ret = new Wrapper(rs.getInt("w.id"));
 				ret.setCreator(rs.getString("u.name"));
 				ret.setDescription(rs.getString("w.description"));
 				ret.setProgram(rs.getString("w.program"));
 				ret.setPublic(rs.getBoolean("w.public"));
 				ret.setName(rs.getString("w.name"));
 				rs.close();
 
 				rs = statement.executeQuery("select * from wrapper_input i where  i.wrapper=" + ret.getId());
 				while (rs.next()) {
 					ret.addInput(new Input(rs.getInt("i.id"), rs.getString("i.description"), rs.getString("i.name"), rs.getString("i.defaultValue"), rs.getString("i.flag"), rs.getBoolean("i.required"), rs.getInt("i.order"), rs.getString("i.displayType"), rs.getString("i.type")));
 				}
 				rs.close();
 				rs = statement.executeQuery("select * from wrapper_output o left join filetype t on t.id=o.type where  o.wrapper=" + ret.getId());
 				while (rs.next()) {
 					ret.addOutput(new Output(rs.getString("o.name"), rs.getString("t.type"), rs.getString("o.description"), rs.getString("o.value"), rs.getInt("o.id")));
 				}
 				rs.close();
 				rs = statement.executeQuery("select * from wrapper_env where wrapper=" + ret.getId());
 				while (rs.next()) {
 					ret.addVar(rs.getString("name"), rs.getString("value"));
 				}
 			}
 			rs.close();
 			statement.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public HashMap<String, String> getFavorites(int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select value,name from favorite u where u.userid=" + user + " and type=1");
 			HashMap<String, String> data = new HashMap<String, String>();
 			while (rs.next()) {
 				data.put(rs.getString("name"), rs.getString("value"));
 			}
 			rs.close();
 			statement.close();
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public Vector<Share> getMyShares(int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			// get the jobs that have been shared by this user
 
 			ResultSet rs = statement.executeQuery("select j.name,js.user,j.id,u.name from jobs js left join job j on j.id=js.job left join user u on js.user=u.id where j.user=" + user + " and js.user<>" + user);
 			Vector<Share> data = new Vector<>();
 			data.add(new Share(Share.RESULT, "", 0, 0, "Results"));
 			data.add(new Share(Share.FILE, "", 0, 0, "Files"));
 
 			while (rs.next()) {
 				data.add(new Share(data.size(), rs.getString("u.name"), rs.getInt("js.user"), Share.RESULT, rs.getString("j.name")));
 			}
 			rs.close();
 			rs = statement.executeQuery("select ss.file,u.name,u.id from shares s left join session ss on ss.id=s.session left join user u on u.id=s.user where s.user<>" + user + " and ss.creator=" + user);
 			while (rs.next()) {
 				data.add(new Share(data.size(), rs.getString("u.name"), rs.getInt("u.id"), Share.FILE, rs.getString("ss.file")));
 			}
 			rs.close();
 			statement.close();
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public Vector<Share> getOtherShares(int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			// get the jobs that have been shared by this user
 
 			ResultSet rs = statement.executeQuery("select j.name,js.user,j.id,u.name from jobs js left join job j on j.id=js.job left join user u on js.user=u.id where j.user<>" + user + " and js.user=" + user);
 			Vector<Share> data = new Vector<>();
 			data.add(new Share(Share.RESULT, "", 0, 0, "Results"));
 			data.add(new Share(Share.FILE, "", 0, 0, "Files"));
 
 			while (rs.next()) {
 				data.add(new Share(data.size(), rs.getString("u.name"), rs.getInt("js.user"), Share.RESULT, rs.getString("j.name")));
 			}
 			rs.close();
 			rs = statement.executeQuery("select ss.file,u.name,u.id from shares s left join session ss on ss.id=s.session left join user u on u.id=s.user where s.user=" + user + " and ss.creator<>" + user);
 			while (rs.next()) {
 				data.add(new Share(data.size(), rs.getString("u.name"), rs.getInt("u.id"), Share.FILE, rs.getString("ss.file")));
 			}
 			rs.close();
 			statement.close();
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param user
 	 *          the user whose jobs are being fetched
 	 * @return a list of jobs that are not finished the job objects have blank wrapper object and blank inputs
 	 */
 	public Vector<Job> getJobs(int user) {
 		Vector<Job> ret = new Vector<Job>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select * from job j left join wrapper w on w.id=j.wrapper where parent=0 and j.user=" + user + " and (j.status!='Finished') order by j.time asc");
 
 			while (rs.next()) {
 				Job job = new Job();
 				job.setId(rs.getInt("j.id"));
 				job.setDate(rs.getDate("j.time"));
 				job.setMachine(rs.getString("j.machine"));
 				job.setName(rs.getString("j.name"));
 				job.setStatus(rs.getString("j.status"));
 				job.setUserId(rs.getInt("j.user"));
 				job.setWorkingDir(rs.getString("j.working_dir"));
 				job.setWrapper(new Wrapper(rs.getInt("j.wrapper")));
 				job.getWrapper().setName(rs.getString("w.name"));
 				job.setParent(rs.getInt("j.parent"));
 				job.setPipeline(rs.getInt("j.pipeline"));
 				job.setStdoutPath(rs.getString("j.stdoutPath"));
 				ret.add(job);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return ret;
 
 	}
 
 	public String[] getUserInfo(String token) {
 		PreparedStatement statement;
 		try {
 			statement = getConnect().prepareStatement("select * from token t left join user u on t.user=u.id where t.token=?");
 			statement.setString(1, token);
 			ResultSet rs = statement.executeQuery();
 
 			if (rs.next()) {
 				String[] data = new String[] { rs.getString("u.name"), "" + rs.getInt("u.permission"), "" + rs.getInt("u.id"), rs.getString("u.email"), rs.getString("u.phone"), "" + rs.getInt("u.byEmail"), "" + rs.getInt("u.byText"), rs.getString("username") };
 				rs.close();
 				statement.close();
 				return data;
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	// returns array [ w.id,w.parent,w.name,commandid,public,description,user name,user id]
 
 	public Vector<UserWrapper> getUsersWrappers(int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select w.id,w.parent,c.name,c.public,u.name,c.id,w.name,w.wrapper from wrappers w left join wrapper c on c.id=w.wrapper left join user u on u.id=c.creator where  w.user=" + user + "");
 			Vector<UserWrapper> data = new Vector<UserWrapper>();
 			while (rs.next()) {
 				UserWrapper wrapper = (new UserWrapper(rs.getString("c.name"), rs.getInt("c.id"), rs.getString("u.name"), rs.getBoolean("c.public"), rs.getInt("w.parent"), rs.getInt("w.id")));
 				if (rs.getInt("w.wrapper") == 0) {
 					wrapper.setName(rs.getString("w.name"));
 				}
 				data.add(wrapper);
 			}
 			rs.close();
 			statement.close();
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public Vector<UserResult> getUsersResults(int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select js.id,js.parent,j.name,js.job,j.time,js.name,j.finished_time,w.name from " + database + ".jobs js left join job j on j.id=js.job left join wrapper w on w.id=j.wrapper where js.user=" + user);
 			Vector<UserResult> data = new Vector<UserResult>();
 			// int user,int parent,String name,int job,int id
 			while (rs.next()) {
 				UserResult res = (new UserResult(user, rs.getInt("js.parent"), rs.getString("j.name"), rs.getInt("js.job"), rs.getInt("js.id")));// { "" + rs.getInt("js.id"), "" + rs.getInt("js.parent"), rs.getString("js.name"), "" + rs.getInt("js.job"), rs.getString("j.program"), rs.getString("j.time") });
 				res.setFinishDate(formatDate(rs.getTimestamp("j.finished_time")));
 				res.setWrapperName(rs.getString("w.name"));
 				if (res.getJob() == 0)
 					res.setName(rs.getString("js.name"));
 				data.add(res);
 			}
 			rs.close();
 			statement.close();
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new Vector<UserResult>();
 	}
 
 	public void removeFavorite(int user, int type, String value) {
 		try {
 			PreparedStatement statement = getConnect().prepareStatement("delete from favorite where userid=" + user + " and type=" + type + " and value=?");
 			statement.setString(1, value);
 			statement.executeUpdate();
 			statement.close();
 		} catch (SQLException e) {
 
 			e.printStackTrace();
 		}
 	}
 
 	public Wrapper saveAsWrapper(Wrapper wrapper) {
 		int wrapperId = executeUpdate(getPreparedStatement("insert into wrapper values(null,?,?,?," + wrapper.getCreatorId() + "," + wrapper.isPublic() + ",null,0)", wrapper.getName(), wrapper.getDescription(), wrapper.getProgram()));
 		Vector<Input> inputs = wrapper.getInputs();
 		for (Input input : inputs) {
 			input.setId(executeUpdate(getPreparedStatement("insert into wrapper_input values (null,?," + wrapperId + ",?,?,?," + input.isRequired() + "," + input.getOrder() + ",?,?)", input.getDescription(), input.getName(), input.getDefaultValue(), input.getFlag(), input.getType(),
 					input.getDisplayType())));
 		}
 		Vector<Output> outputs = wrapper.getOutputs();
 		for (Output output : outputs) {
 			Vector<String[]> fileTypes = runQuery(getPreparedStatement("select id from filetype where type=?", output.getType()));
 			int type = 0;
 			if (fileTypes.size() == 0)
 				type = executeUpdate(getPreparedStatement("insert into filetype values(null,?)", output.getType()));
 			else
 				type = Integer.parseInt(fileTypes.get(0)[0]);
 			output.setId(executeUpdate(getPreparedStatement("insert into wrapper_output values(null," + wrapperId + ",?,?,?," + type + ")", output.getName(), output.getDescription(), output.getValue())));
 		}
 		wrapper.setId(wrapperId);
 		Iterator<String> it = wrapper.getEnvVars().keySet().iterator();
 		while (it.hasNext()) {
 			String key = it.next();
 			executeUpdate(getPreparedStatement("insert into wrapper_env values (null," + wrapper.getId() + ",?,?)", key, wrapper.getEnvVars().get(key)));
 		}
 		return wrapper;
 	}
 
 	/**
 	 * @param wrapper
 	 *          the wrapper to save
 	 * @return the saved wrapper with the ids of inputs,outputs and its wrapper id updated
 	 */
 	public Wrapper saveWrapper(Wrapper wrapper) {
 		if (wrapper.getId() == 0) {
 			return saveAsWrapper(wrapper);
 		}
 		executeUpdate(getPreparedStatement("update wrapper set name=?, description=?, program=? where id=" + wrapper.getId(), wrapper.getName(), wrapper.getDescription(), wrapper.getProgram()));
 
 		String inputs = "(";
 		for (Input input : wrapper.getInputs()) {
 			if (input.getId() <= 0) {
 				input.setName(input.getName().replaceAll("'", "").replaceAll("\\$", ""));
 				// the input is new insert it instead of updating
 				int inputId = executeUpdate(getPreparedStatement("insert into wrapper_input values (null,?," + wrapper.getId() + ",?,?,?," + input.isRequired() + "," + input.getOrder() + ",?,?)", input.getDescription(), input.getName(), input.getDefaultValue(), input.getFlag(), input.getType(),
 						input.getDisplayType()));
 				input.setId(inputId);
 			} else {
 				// just update the input
 				executeUpdate(getPreparedStatement("update wrapper_input i set description=?, name=?, defaultValue=?, flag=?, required=" + input.isRequired() + ", i.order=" + input.getOrder() + ", type=?, displayType=? where id=" + input.getId(), input.getDescription(), input.getName(),
 						input.getDefaultValue(), input.getFlag(), input.getType(), input.getDisplayType()));
 			}
 			inputs += input.getId() + ",";
 		}
 		// now get rid of the inputs that have been deleted
 		inputs = inputs.substring(0, inputs.length() - 1);
 		if (!inputs.equals(""))
 			executeUpdate(getPreparedStatement("delete from wrapper_input where id not in " + inputs + ") and wrapper=" + wrapper.getId()));
 		else
 			executeUpdate(getPreparedStatement("delete from wrapper_input where wrapper=" + wrapper.getId()));
 
 		String outputs = "(";
 		for (Output output : wrapper.getOutputs()) {
 			if (output.getId() <= 0) {
 				// the output is new insert it instead of updating
 				Vector<String[]> fileTypes = runQuery(getPreparedStatement("select id from filetype where type=?", output.getType()));
 				int type = 0;
 				if (fileTypes.size() == 0)
 					type = executeUpdate(getPreparedStatement("insert into filetype values(null,?)", output.getType()));
 				else
 					type = Integer.parseInt(fileTypes.get(0)[0]);
 				int outputId = executeUpdate(getPreparedStatement("insert into wrapper_output values(null," + wrapper.getId() + ",?,?,?," + type + ")", output.getName(), output.getDescription(), output.getValue()));
 				output.setId(outputId);
 			} else {
 				// just update the input
 				Vector<String[]> fileTypes = runQuery(getPreparedStatement("select id from filetype where type=?", output.getType()));
 				int type = 0;
 				if (fileTypes.size() == 0)
 					type = executeUpdate(getPreparedStatement("insert into filetype values(null,?)", output.getType()));
 				else
 					type = Integer.parseInt(fileTypes.get(0)[0]);
 				executeUpdate(getPreparedStatement("update wrapper_output set description=?, name=?, value=?, type=" + type + " where id=" + output.getId(), output.getDescription(), output.getName(), output.getValue()));
 			}
 			outputs += output.getId() + ",";
 		}
 		outputs = outputs.substring(0, outputs.length() - 1);
 		if (!outputs.equals(""))
 			executeUpdate(getPreparedStatement("delete from wrapper_output where id not in " + outputs + ") and wrapper=" + wrapper.getId()));
 		else
 			executeUpdate(getPreparedStatement("delete from wrapper_output where wrapper=" + wrapper.getId()));
 
 		executeUpdate(getPreparedStatement("delete from wrapper_env where wrapper=" + wrapper.getId()));
 		Iterator<String> it = wrapper.getEnvVars().keySet().iterator();
 		while (it.hasNext()) {
 			String key = it.next();
 			executeUpdate(getPreparedStatement("insert into wrapper_env values (null," + wrapper.getId() + ",?,?)", key, wrapper.getEnvVars().get(key)));
 		}
 
 		return wrapper;
 	}
 
 	public int saveFavorite(int user, int type, String value, String name) {
 		if (runQuery(getPreparedStatement("select * from favorite where userid=" + user + " and name=? and value=? and type=" + type, name, value)).size() > 0)
 			return -1;
 		return executeUpdate(getPreparedStatement("insert into favorite values (null," + user + "," + type + ",?,?)", value, name));
 	}
 
 	public void saveUser(String name, int user, String password, String email, String phone, String useEmail, String usePhone) {
 		try {
 			PreparedStatement statement = getConnect().prepareStatement("update user set name=?, email=?, phone=? ,  byEmail=" + useEmail + " , byText=" + usePhone + " where id=" + user + "");
 			statement.setString(1, name);
 			statement.setString(2, email);
 			statement.setString(3, phone);
 			statement.executeUpdate();
 			statement.close();
 		} catch (SQLException e) {
 
 			e.printStackTrace();
 		}
 
 	}
 
 	public boolean doesCommandExist(int user, boolean isPublic, String name) {
 		try {
 			String userS = isPublic ? "" : "and creator=" + user;
 			PreparedStatement st = getConnect().prepareStatement("select id,name from command where name=? " + userS);
 			st.setString(1, "name");
 			ResultSet rs = st.executeQuery();
 			if (rs.next()) {
 				rs.close();
 				st.close();
 				return true;
 			}
 			rs.close();
 			st.close();
 			return false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	public void updateJobStatus(final int job, String status) {
 		PreparedStatement st;
 		try {
 			st = getConnect().prepareStatement("update job set status=? where id=" + job);
 			st.setString(1, status);
 			st.executeUpdate();
 			if (status.startsWith("Running")) {
 				executeUpdate(getPreparedStatement("update job set run_time=now() where id=" + job));
 				final String[] temp = runQuery(getPreparedStatement("select parent from job where id=" + job)).get(0);
 				if (!temp[0].equals("0")) {
 					Vector<String[]> tempStatus = runQuery(getPreparedStatement("Select status from job where parent=" + temp[0]));
 					int of = tempStatus.size();
 					int on = 0;
 					for (String[] stat : tempStatus) {
 						if (stat[0].equals("Finished") || stat[0].startsWith("Running")) {
 							on++;
 						}
 						updateJobStatus(Integer.parseInt(temp[0]), "Running " + on + " of " + of);
 
 					}
 				}
 			} else if (status.equals("Finished")) {
 				executeUpdate(getPreparedStatement("update job set finished_time=now() where id=" + job));
 				final String[] temp = runQuery(getPreparedStatement("select name,user,parent from job where id=" + job)).get(0);
 				if (temp[2].equals("0")) {
 					int id = st.executeUpdate("insert into jobs values (null," + temp[1] + ",0," + job + ",'" + temp[0] + "')");
 					UserResult result = new UserResult(Integer.parseInt(temp[1]), 0, temp[0], job, id);
 					CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.ADDED, result)), result.getUser());
 				} else {
 					Vector<String[]> tempList = runQuery(getPreparedStatement("select id from job where parent=" + temp[2] + " and status not like 'Finished%'"));
 					if (tempList.size() == 0) {
 						
 						updateJobStatus(Integer.parseInt(temp[2]), "Finished");
 						CommunicationImpl.getInstance().runNextJobs(Integer.parseInt(temp[2]));
 					}
 				}
 				final Vector<String[]> notifies = runQuery(getPreparedStatement("select email,byEmail,phone,byText,user from notification n left join user u on u.id=n.user where job=" + job));
 				new Thread(new Runnable() {
 					public void run() {
 						for (String[] not : notifies) {
 							if (!not[4].equals(temp[1])) {
 								executeUpdate(getPreparedStatement("insert into jobs values (null," + not[4] + ",0," + job + ",?)", temp[0]));
 							}
 							if (not[1].equals("true")) {
 								Notifier.sendEmail(not[0], not[0], "ETA job#" + job + " finished", "ETA job#" + job + " finished. You can view the results at " + Settings.getInstance().getSetting("etaHost").getStringValue() + "#home,cr#" + job);
 							}
 							if (not[3].equals("true")) {
 								Notifier.sendTextNotification(not[2], job);
 							}
 						}
 						executeUpdate(getPreparedStatement("delete from notification where job=" + job));
 						// check to see if this was an external job
 						Vector<String[]> results = runQuery(getPreparedStatement("Select r.key,n.email,r.referer from public_result r,external_notifications n where r.job=" + job + " and n.job=" + job));
 						for (String[] line : results) {
 							Notifier.sendEmail(line[1], line[1], "Job finished", "Your job has finished. You can view the results at " + line[2] + "?job=" + line[0]);
 						}
 					}
 				}).start();
 
 			} else if (status.equals("Failed") || status.equals("Cancelled")) {
 				executeUpdate(getPreparedStatement("delete from notification where job=" + job));
 			}
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void addShareResult(int user, int job) {
 		PreparedStatement st;
 		try {
 			String[] temp = runQuery(getPreparedStatement("select name from job where id=" + job)).get(0);
 			st = getConnect().prepareStatement("insert into jobs values (null," + user + ",0," + job + ",?)");
 			st.setString(1, temp[0]);
 			st.executeUpdate();
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeShareResult(int user, int job) {
 		Statement st;
 		try {
 			st = getConnect().createStatement();
 			st.executeUpdate("remove from jobs where job=" + job + "  and user=" + user);
 			st.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String[] getFileTypes() {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select * from filetype");
 			Vector<String> data = new Vector<String>();
 			while (rs.next()) {
 				data.add(rs.getString("type"));
 			}
 			rs.close();
 			statement.close();
 			String[] ret = new String[data.size()];
 			for (int i = 0; i < data.size(); i++) {
 				ret[i] = data.get(i);
 			}
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public PreparedStatement getPreparedStatement(String sql, String... strings) {
 		try {
 
 			PreparedStatement st;
 			st = getConnect().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
 			for (int i = 0; i < strings.length; i++) {
 				st.setString(i + 1, strings[i]);
 			}
 			return st;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public Vector<String[]> runQuery(PreparedStatement statement) {
 		Vector<String[]> ret = new Vector<String[]>();
 		try {
 			ResultSet rs = statement.executeQuery();
 			ResultSetMetaData rsMetaData = rs.getMetaData();
 			int numberOfColumns = rsMetaData.getColumnCount();
 
 			while (rs.next()) {
 				String[] temp = new String[numberOfColumns];
 				for (int i = 0; i < numberOfColumns; i++) {
 					Object obj = rs.getObject(i + 1);
 					if (obj == null)
 						temp[i] = null;
 					else if (obj instanceof String)
 						temp[i] = (String) obj;
 					else if (obj instanceof Integer)
 						temp[i] = "" + ((Integer) obj).intValue();
 					else
 						temp[i] = obj.toString();
 
 				}
 				ret.add(temp);
 			}
 			statement.close();
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public ResultSettings getResultSettings(String sessionId) {
 		try {
 			PreparedStatement statement = getPreparedStatement("select s.id,s.plugin,r.url,s.public,r.id from session s left join result r on r.session=s.id where s.token=?");
 			statement.setString(1, sessionId);
 			ResultSet rs = statement.executeQuery();
 			ResultSettings ret = null;
 			rs.next();
 			ret = new ResultSettings(new HashMap<Integer, String>(), rs.getBoolean("s.public"), rs.getString("r.url"), rs.getInt("s.id"), rs.getInt("r.id"));
 			rs.close();
 			if (ret.getResultId() != 0) {
 				rs = statement.executeQuery("select u.id,u.name from shares s left join user u on u.id=s.user where s.session=" + ret.getSessionId());
 				while (rs.next()) {
 					ret.addUser(rs.getString("u.name"), rs.getInt("u.id"));
 				}
 			}
 			rs.close();
 			statement.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new ResultSettings(new HashMap<Integer, String>(), false, null, 0, 0);
 	}
 
 	public ResultSettings getJobResultSettings(int job, int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select jb.user,jb.job,j.name,u.name from jobs jb left join job j on j.id=jb.job left join user u on u.id=jb.user where j.user=" + user + " and jb.user!=" + user + " and jb.job=" + job);
 			ResultSettings ret = new ResultSettings(new HashMap<Integer, String>(), false, "", -1, job);
 			while (rs.next()) {
 
 				ret.addUser(rs.getString("u.name"), rs.getInt("jb.user"));
 			}
 			rs.close();
 			statement.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new ResultSettings(new HashMap<Integer, String>(), false, null, 0, 0);
 	}
 
 	// /getSearchResults [type][id][string name]
 	public HashMap<String, Vector<SearchResultItem>> getSearchResults(String search, int user) {
 		HashMap<String, Vector<SearchResultItem>> results = new HashMap<String, Vector<SearchResultItem>>();
 		// first lets search for wrappers public or the users
 		try {
 			Vector<SearchResultItem> wrappers = new Vector<SearchResultItem>();
 			ResultSet rs = getPreparedStatement("select name,id,public,program,description from wrapper where (creator=" + user + " or public=1) and (match(name,description,program) against (?) or name like ?)", search, "%" + search + "%").executeQuery();
 			while (rs.next()) {
 				String found;
 				String name = rs.getString("name");
 				String description = rs.getString("description");
 				if (description == null)
 					description = "";
 				String program = rs.getString("program");
 				if (program.contains(search)) {
 					found = "Uses : " + program;
 				} else if (description.contains(search)) {
 					found = "Description: " + description;
 				} else {
 					found = "Named: " + name;
 				}
 				found = found.replaceAll(search, "<b>" + search + "</b>");
 				wrappers.add(new WrapperSearchItem(rs.getInt("id"), name, found, rs.getBoolean("public")));
 			}
 			rs.getStatement().close();
 			rs.close();
 			if (wrappers.size() > 0)
 				results.put("Wrappers", wrappers);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		// now get the results
 		try {
 			Vector<SearchResultItem> results2 = new Vector<SearchResultItem>();
 			ResultSet rs = getPreparedStatement("select j.id,j.name,w.program,j.time from jobs js left join job j on j.id=js.job left join wrapper w on w.id=j.wrapper where js.user=" + user + " and job>0 and status='Finished' and (w.program like ? or j.name like ?) order by time desc",
 					"%" + search + "%", "%" + search + "%").executeQuery();
 			SimpleDateFormat format = new SimpleDateFormat("M/d/y");
 			String today = format.format(new Date(new java.util.Date().getTime()));
 			while (rs.next()) {
 				Date timestamp = rs.getDate("j.time");
 				String date = format.format(timestamp);
 				if (date.equals(today)) {
 					date = new SimpleDateFormat("H:m").format(timestamp);
 				}
 				results2.add(new ResultSearchItem(rs.getInt("j.id"), rs.getString("j.name"), date));
 			}
 			rs.getStatement().close();
 			rs.close();
 
 			if (results2.size() > 0)
 				results.put("Results", results2);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		// now get running jobs
 		try {
 			Vector<SearchResultItem> results2 = new Vector<SearchResultItem>();
 			ResultSet rs = getPreparedStatement("select id,name,status,time,machine from job where user=" + user + " and status<>'Finished' and name like ? order by time desc", "%" + search + "%").executeQuery();
 			SimpleDateFormat format = new SimpleDateFormat("M/d/y");
 			String today = format.format(new Date(new java.util.Date().getTime()));
 			while (rs.next()) {
 				Date timestamp = rs.getDate("time");
 				String date = format.format(timestamp);
 				if (date.equals(today)) {
 					date = new SimpleDateFormat("H:m").format(timestamp);
 				}
 				results2.add(new JobSearchItem(rs.getInt("id"), rs.getString("name"), date, rs.getString("machine"), rs.getString("status")));
 			}
 			rs.getStatement().close();
 			rs.close();
 
 			if (results2.size() > 0)
 				results.put("Jobs", results2);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		// now get favorite folders
 		try {
 			Vector<SearchResultItem> results2 = new Vector<SearchResultItem>();
 			ResultSet rs = getPreparedStatement("select name,value from favorite where match(value,name) against (?) and userid=" + user, search).executeQuery();
 			while (rs.next()) {
 				results2.add(new FavoriteSearchItem(rs.getString("name"), rs.getString("value")));
 			}
 			rs.getStatement().close();
 			rs.close();
 
 			if (results2.size() > 0)
 				results.put("Favorites", results2);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		// now get servers that are on the cloud
 		Vector<SearchResultItem> serverResults = new Vector<SearchResultItem>();
 //		String[] machines = GangliaParser.getInstance().getMachines();
 //		for (String machine : machines) {
 //			if (machine.contains(search)) {
 //				String[][] machinestat = GangliaParser.getInstance().getData(new String[] { machine });
 //				serverResults.add(new ServerSearchItem(machine, machinestat[0][3], machinestat[0][2]));
 //			}
 //		}
 		if (serverResults.size() > 0)
 			results.put("Servers", serverResults);
 		return results;
 	}
 
 	SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yy kk:mm");
 
 	public String formatDate(java.util.Date date) {
 		if (date == null)
 			return "";
 		return formater.format(date);
 	}
 
 	public String formatDate(Date date) {
 		if (date == null)
 			return "";
 		return formater.format(date);
 	}
 
 	public String formatDate(Timestamp date) {
 		if (date == null)
 			return "";
 		return formater.format(new java.util.Date(date.getTime()));
 	}
 
 	/**
 	 * @param jobId
 	 *          the id of the job object to retrieve
 	 * @return the job object of the request id. null if the job doesn't exist
 	 */
 	public Job getJob(int jobId) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs;
 			rs = statement.executeQuery("select j.stdoutPath,j.time,j.id,j.machine,j.status,j.user,j.working_dir,j.wrapper,j.pipeline,u.name,j.name,j.submit_time,j.run_time,j.finished_time,j.specs,j.waiting_for,j.parent from job j left join user u on j.user=u.id where j.id=" + jobId);
 			Job job = new Job();
 			if (rs.next()) {
 				job.setDate(rs.getDate("j.time"));
 				job.setId(rs.getInt("j.id"));
 				job.setName(rs.getString("j.name"));
 				job.setMachine(rs.getString("j.machine"));
 				job.setStatus(rs.getString("j.status"));
 				job.setUserId(rs.getInt("j.user"));
 				job.setWaitingFor(rs.getInt("j.waiting_for"));
 				job.setPipeline(rs.getInt("j.pipeline"));
 				job.setParent(rs.getInt("j.parent"));
 				job.setFinishedTime(formatDate(rs.getTimestamp("j.finished_time")));
 				job.setSubmitTime(formatDate(rs.getTimestamp("j.submit_time")));
 				job.setRunTime(formatDate(rs.getTimestamp("j.run_time")));
 				job.setSpecs(rs.getString("j.specs"));
 				job.setUser(rs.getString("u.name"));
 				job.setStdoutPath(rs.getString("j.stdoutPath"));
 				job.setWorkingDir(rs.getString("j.working_dir"));
 				int wrapper = rs.getInt("j.wrapper");
 				if (wrapper > 0)
 					job.setWrapper(getWrapperFromId(wrapper));
 				if (job.getPipeline() > 0)
 					job.setPipeline(getPipelineFromId(job.getPipeline()));
 				rs.getStatement().close();
 				rs.close();
 				rs = getConnect().createStatement().executeQuery("select input,value from job_value where job=" + jobId);
 				while (rs.next()) {
 					job.setInput(rs.getInt("input"), rs.getString("value"));
 				}
 				rs.getStatement().close();
 				rs.close();
 				rs = getPreparedStatement("select * from job_note n left join user u on u.id=n.user where n.job=" + jobId).executeQuery();
 				while (rs.next()) {
 					job.addNote(new JobNote(rs.getString("n.note"), formatDate(rs.getTimestamp("n.date")), rs.getString("u.name"), rs.getInt("n.id"), rs.getInt("u.id")));
 				}
 				rs.close();
 			}
 			statement.close();
 			return job;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * @return
 	 */
 	public Vector<RequestItem> getRequests(int user) {
 		Vector<RequestItem> ret = new Vector<RequestItem>();
 		try {
 			ResultSet rs = getPreparedStatement(
 					"select r.id,r.status,r.timestamp,r.type,r.summary,u.name,(select count(s.id) from request_star s where s.request=r.id) as stars,(select  count(s.id) from request_star s where s.request=r.id and s.user=" + user
 							+ ") as stared from request_new r left join user u on u.id=r.reporter order by r.timestamp desc").executeQuery();
 			while (rs.next()) {
 				ret.add(new RequestItem(rs.getInt("r.id"), rs.getString("r.status"), rs.getString("u.name"), rs.getString("r.type"), rs.getString("r.summary"), rs.getDate("r.timestamp"), rs.getInt("stars"), rs.getInt("stared")));
 			}
 			rs.getStatement().close();
 			rs.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @param request
 	 * @return
 	 */
 	public RequestItem getRequest(int request, int user) {
 		try {
 			RequestItem ret = null;
 			ResultSet rs = getPreparedStatement(
 					"select r.id,r.status,r.timestamp,r.type,r.summary,u.name,r.description,(select count(*) from request_star s where s.user=" + user
 							+ " and s.request=r.id) as stared,(select count(s.id) from request_star s where s.request=r.id) as stars from request_new r left join user u on u.id=r.reporter where r.id=" + request).executeQuery();
 			while (rs.next()) {
 				ret = new RequestItem(rs.getInt("r.id"), rs.getString("r.status"), rs.getString("u.name"), rs.getString("r.type"), rs.getString("r.summary"), rs.getDate("r.timestamp"), rs.getInt("stars"), rs.getInt("stared"));
 				ret.setDescription(rs.getString("r.description"));
 			}
 			rs.getStatement().close();
 			rs.close();
 			rs = getPreparedStatement("select * from request_item r left join user u on u.id=r.user where request=" + request).executeQuery();
 			while (rs.next()) {
 				ret.addResponse(new RequestResponse(rs.getString("r.response"), rs.getDate("r.date"), rs.getString("u.name"), rs.getInt("r.id")));
 			}
 			rs.getStatement().close();
 			rs.close();
 			rs = getPreparedStatement("select file from request_file where request=" + request).executeQuery();
 			while (rs.next()) {
 				ret.addFile(rs.getString("file"));
 			}
 			rs.getStatement().close();
 			rs.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param requestItem
 	 * @return
 	 */
 	public RequestResponse getRequestResponse(int requestItem) {
 		try {
 			ResultSet rs = getPreparedStatement("select * from request_item r left join user u on u.id=r.user where r.id=" + requestItem).executeQuery();
 			RequestResponse ret = null;
 			while (rs.next()) {
 				ret = (new RequestResponse(rs.getString("r.response"), rs.getDate("r.date"), rs.getString("u.name"), rs.getInt("id")));
 			}
 			rs.getStatement().close();
 			rs.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public User getUserInfoFromUsername(String user) {
 		User ret;
 		PreparedStatement statement;
 		try {
 			statement = getConnect().prepareStatement("select * from user u where u.username=?");
 			statement.setString(1, user);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				ret = (new User(rs.getString("u.name"), rs.getString("u.email"), rs.getString("u.username"), rs.getInt("u.permission")));
 				ret.setPhone(rs.getString("phone"));
 				ret.setId(rs.getInt("u.id"));
 				ret.setnEmail(rs.getBoolean("byEmail"));
 				ret.setnPhone(rs.getBoolean("byText"));
 				rs.close();
 				statement.close();
 				return ret;
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param token
 	 * @return
 	 */
 	public User getUser(String token) {
 		try {
 			if (token == null)
 				return null;
 			ResultSet rs = getPreparedStatement("select * from token t left join user u on t.user=u.id where t.token=?", token).executeQuery();
 			User ret = null;
 			if (rs.next()) {
 				ret = (new User(rs.getString("u.name"), rs.getString("u.email"), rs.getString("u.username"), rs.getInt("u.permission")));
 				ret.setPhone(rs.getString("phone"));
 				ret.setId(rs.getInt("u.id"));
 				ret.setnEmail(rs.getBoolean("byEmail"));
 				ret.setnPhone(rs.getBoolean("byText"));
 				rs.getStatement().close();
 				rs.close();
 				rs = getPreparedStatement("select * from user_setting where user=" + ret.getId()).executeQuery();
 				while (rs.next()) {
 					ret.setSetting(rs.getString("setting"), rs.getString("value"));
 				}
 				rs.getStatement().close();
 				rs.close();
 				rs = getPreparedStatement("select * from user_cluster u left join global_cluster g on g.id=u.global where u.user=" + ret.getId()).executeQuery();
 				while (rs.next()) {
 					ret.addCluster(new Cluster(rs.getInt("g.id"), rs.getString("u.key"), rs.getString("g.address"), rs.getString("u.company"), rs.getString("g.key")));
 				}
 			}
 			rs.getStatement().close();
 			rs.close();
 			return ret;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public Wrapper getWrapper(int id, int user) {
 		Wrapper ret = null;
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select u.name,w.description,w.id,w.name,w.public,w.program,s.rating from wrapper w left join user u on u.id=w.creator left join wrapper_star s on (s.wrapper=w.id and s.user=" + user + ") where w.id=" + id);
 			if (rs.next()) {
 				Wrapper wrapper = new Wrapper();
 				wrapper.setCreator(rs.getString("u.name"));
 				wrapper.setDescription(rs.getString("w.description"));
 				wrapper.setId(rs.getInt("w.id"));
 				wrapper.setName(rs.getString("w.name"));
 				wrapper.setPublic(rs.getBoolean("w.public"));
 				wrapper.setProgram(rs.getString("w.program"));
 				int rating = rs.getInt("s.rating");
 				wrapper.setStared(rating > 0);
 				wrapper.setRating(rating);
 				ret = wrapper;
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @return
 	 */
 	public Vector<Wrapper> getWrappers(int user) {
 		Vector<Wrapper> ret = new Vector<Wrapper>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs;
 			if (user > 0) {
 				rs = statement.executeQuery("Select u.name,w.description,w.id,w.name,w.public,w.program,s.rating from wrapper w left join user u on u.id=w.creator left join wrapper_star s on (s.wrapper=w.id and s.user=" + user + ") where public=1 or w.creator=" + user + " order by w.name ASC");
 			} else {
 				rs = statement.executeQuery("Select u.name,w.description,w.id,w.name,w.public,w.program,s.rating from wrapper w left join user u on u.id=w.creator left join wrapper_star s on (s.wrapper=w.id and s.user=" + user + ") order by w.name ASC");
 			}
 			while (rs.next()) {
 				Wrapper wrapper = new Wrapper();
 				wrapper.setCreator(rs.getString("u.name"));
 				wrapper.setDescription(rs.getString("w.description"));
 				wrapper.setId(rs.getInt("w.id"));
 				wrapper.setName(rs.getString("w.name"));
 				wrapper.setPublic(rs.getBoolean("w.public"));
 				wrapper.setProgram(rs.getString("w.program"));
 				int rating = rs.getInt("s.rating");
 				wrapper.setStared(rating > 0);
 				wrapper.setRating(rating);
 				ret.add(wrapper);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @return
 	 */
 	public Vector<User> getUsers() {
 		Vector<User> ret = new Vector<User>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select name,username,id from user");
 			while (rs.next()) {
 				User user = new User();
 				user.setId(rs.getInt("id"));
 				user.setName(rs.getString("name"));
 				user.setUsername(rs.getString("username"));
 				ret.add(user);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @param name
 	 *          : the name of the wrapper to retrieve
 	 */
 	public Wrapper getWrapper(String name) {
 		Wrapper ret = null;
 		try {
 			ResultSet rs = getPreparedStatement("Select id from wrapper where name=?", name).executeQuery();
 			if (rs.next()) {
 				ret = getWrapperFromId(rs.getInt(1));
 			}
 			rs.getStatement().close();
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @param user
 	 *          the id of the user that is querying
 	 * @return
 	 */
 	public Vector<UserPipeline> getUsersPipelines(int user) {
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select ps.parent,ps.name,p.public,ps.id,ps.pipeline,u.name,p.description,p.name from pipelines ps left join pipeline p on p.id=ps.pipeline left join user u on p.creator=u.id where ps.user=" + user);
 			Vector<UserPipeline> data = new Vector<UserPipeline>();
 			while (rs.next()) {
 				UserPipeline pipeline = new UserPipeline(rs.getInt("ps.parent"), rs.getString("ps.name"), rs.getBoolean("p.public"), rs.getInt("ps.pipeline"), rs.getString("u.name"), rs.getString("p.description"), rs.getInt("ps.id"));
 				if (pipeline.getName() == null || pipeline.getName().equals(""))
 					pipeline.setName(rs.getString("p.name"));
 				data.add(pipeline);
 			}
 			rs.close();
 			statement.close();
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * @param pipelineId
 	 * @return
 	 */
 	public Pipeline getPipelineFromId(int pipelineId) {
 		Pipeline ret = null;
 		try {
 			ResultSet rs = getPreparedStatement("Select * from pipeline p left join user u on u.id=p.creator where p.id=" + pipelineId).executeQuery();
 			if (rs.next()) {
 				ret = new Pipeline(rs.getString("p.name"), rs.getString("p.description"), rs.getInt("p.creator"), rs.getBoolean("p.public"), rs.getInt("p.id"));
 				ret.setCreator(rs.getString("u.name"));
 				rs.getStatement().close();
 				rs.close();
 				rs = getPreparedStatement("select * from pipeline_input i where  i.pipeline=" + ret.getId()).executeQuery();
 				while (rs.next()) {
 					ret.addInput(new Input(rs.getInt("i.id"), rs.getString("i.description"), rs.getString("i.name"), rs.getString("i.defaultValue"), "", rs.getBoolean("i.required"), rs.getInt("i.order"), rs.getString("i.displayType"), rs.getString("i.type")));
 				}
 				rs.close();
 
 				rs = getPreparedStatement("select * from pipeline_output  where pipeline=" + ret.getId()).executeQuery();
 				while (rs.next()) {
 					ret.addOutput(new Output(rs.getString("name"), rs.getString("type"), rs.getString("description"), rs.getString("value"), rs.getInt("id")));
 				}
 				rs.close();
 
 				rs = getPreparedStatement("select * from pipeline_component where pipeline=" + ret.getId() + " order by step ASC").executeQuery();
 				while (rs.next()) {
 					PipeComponent component = null;
 					if (rs.getInt("wrapper") == 0) {
 						// must be a pipeline get the pipeline!
 						PipelineWrapper pipelineWrapper = new PipelineWrapper(rs.getInt("id"), rs.getInt("pipe"), rs.getInt("step"));
 						pipelineWrapper.setPipeline(getPipelineFromId(pipelineWrapper.getPipelineId()));
 						ResultSet rs2 = getPreparedStatement("Select * from pipeline_component_values where pipeline_component=" + pipelineWrapper.getId()).executeQuery();
 						while (rs2.next()) {
 							pipelineWrapper.getPipeline().setInput(rs2.getInt("input"), rs2.getString("value"));
 						}
 						component = pipelineWrapper;
 						rs2.getStatement().close();
 						rs2.close();
 					} else {
 						PipeWrapper pipeWrapper = new PipeWrapper(rs.getInt("id"), rs.getInt("wrapper"), rs.getInt("step"));
 						pipeWrapper.setJobOptions(rs.getString("job_options"));
 						Wrapper wrapper = getWrapperFromId(pipeWrapper.getWrapperId());
 						ResultSet rs2 = getPreparedStatement("Select * from pipeline_component_values where pipeline_component=" + pipeWrapper.getId()).executeQuery();
 						while (rs2.next()) {
 							wrapper.setInput(rs2.getInt("input"), rs2.getString("value"));
 						}
 						pipeWrapper.setWrapper(wrapper);
 						component = pipeWrapper;
 						rs2.getStatement().close();
 						rs2.close();
 					}
 					ret.addStep(component);
 				}
 
 			}
 			rs.getStatement().close();
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 
 	}
 
 	/**
 	 * @param id
 	 * @return
 	 */
 	public Vector<Pipeline> getPipelines(int user) {
 		Vector<Pipeline> ret = new Vector<Pipeline>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select u.name,p.description,p.id,p.name,p.public,s.rating from pipeline p left join user u on u.id=p.creator left join pipeline_star s on (s.pipeline=p.id and s.user=" + user + ") where public=1  and p.creator>0 or p.creator=" + user
 					+ " order by p.name ASC");
 			while (rs.next()) {
 				Pipeline pipeline = new Pipeline();
 				pipeline.setCreator(rs.getString("u.name"));
 				pipeline.setDescription(rs.getString("p.description"));
 				pipeline.setId(rs.getInt("p.id"));
 				pipeline.setName(rs.getString("p.name"));
 				pipeline.setPublic(rs.getBoolean("p.public"));
 				int rating = rs.getInt("s.rating");
 				pipeline.setStared(rating > 0);
 				pipeline.setRating(rating);
 				ret.add(pipeline);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @param pipeline
 	 * @param user
 	 * @return
 	 */
 	public Pipeline getPipeline(int pipelineId, int user) {
 		Pipeline ret = null;
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select u.name,p.description,p.id,p.name,p.public,s.rating from pipeline p left join user u on u.id=p.creator left join pipeline_star s on (s.pipeline=p.id and s.user=" + user + ") where p.id=" + pipelineId);
 			if (rs.next()) {
 				Pipeline pipeline = new Pipeline();
 				pipeline.setCreator(rs.getString("u.name"));
 				pipeline.setDescription(rs.getString("p.description"));
 				pipeline.setId(rs.getInt("p.id"));
 				pipeline.setName(rs.getString("p.name"));
 				pipeline.setPublic(rs.getBoolean("p.public"));
 				int rating = rs.getInt("s.rating");
 				pipeline.setStared(rating > 0);
 				pipeline.setRating(rating);
 				ret = pipeline;
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	
 	private ArrayList<Input> getUserInputs(Pipeline pipeline){
 		ArrayList<Input> inputs = new ArrayList<Input>();
 		for (PipeComponent wrapper2 : pipeline.getSteps()) {
 			if (wrapper2 instanceof PipeWrapper) {
 				PipeWrapper wrapper = (PipeWrapper) wrapper2;
 					for (Input input : wrapper.getWrapper().getInputs()) {
 						if (!input.getValue().equals("") && input.getValue().equals("$'User Input'")) {
 							Input newPipelineInput = input.clone();
 							newPipelineInput.setId(-1);
 							input.setValue("$'"+input.getName()+"'");
 							inputs.add(newPipelineInput);
 						}
 				}
 			}else if (wrapper2 instanceof PipelineWrapper) {
 				PipelineWrapper wrapper = (PipelineWrapper) wrapper2;
 				inputs.addAll(getUserInputs(wrapper.getPipeline()));
 			}
 		}
 		return inputs;
 	}
 	/**
 	 * @param pipeline
 	 * @return
 	 */
 	public Pipeline savePipeline(Pipeline pipeline) {
 		if (pipeline.getId() <= 0) {
 			return saveAsPipeline(pipeline);
 		}
 
 		executeUpdate(getPreparedStatement("update pipeline set name=?, description=? where id=" + pipeline.getId(), pipeline.getName(), pipeline.getDescription()));
 		for(Input inp:getUserInputs(pipeline)){
 			pipeline.addInput(inp);
 		}
 
 		String inputs = "(";
 		for (Input input : pipeline.getInputs()) {
 			if (input.getId() <= 0) {
 				input.setName(input.getName().replaceAll("'", "").replaceAll("\\$", ""));
 				// the input is new insert it instead of updating
 				int inputId = executeUpdate(getPreparedStatement("insert into pipeline_input values (null,?," + pipeline.getId() + ",?,?," + input.isRequired() + "," + input.getOrder() + ",?,?)", input.getDescription(), input.getName(), input.getDefaultValue(), input.getType(), input.getDisplayType()));
 				input.setId(inputId);
 			} else {
 				// just update the input
 				executeUpdate(getPreparedStatement("update pipeline_input i set description=?, name=?, defaultValue=?, required=" + input.isRequired() + ", i.order=" + input.getOrder() + ", type=?, displayType=? where id=" + input.getId(), input.getDescription(), input.getName(), input.getDefaultValue(),
 						input.getType(), input.getDisplayType()));
 			}
 			inputs += input.getId() + ",";
 		}
 		// now get rid of the inputs that have been deleted
 		inputs = inputs.substring(0, inputs.length() - 1);
 		if (!inputs.equals(""))
 			executeUpdate(getPreparedStatement("delete from pipeline_input where id not in " + inputs + ") and pipeline=" + pipeline.getId()));
 		else
 			executeUpdate(getPreparedStatement("delete from pipeline_input where pipeline=" + pipeline.getId()));
 
 		String outputs = "(";
 		for (Output output : pipeline.getOutputs()) {
 			if (output.getId() <= 0) {
 				// the input is new insert it instead of updating
 				int outputId = executeUpdate(getPreparedStatement("insert into pipeline_output values (null," + pipeline.getId() + ",?,?,?,?", output.getName(), output.getDescription(), output.getValue(), output.getType()));
 				output.setId(outputId);
 			} else {
 				// just update the input
 				executeUpdate(getPreparedStatement("update pipeline_output set name=?, description=?, value=?, type=?", output.getName(), output.getDescription(), output.getValue(), output.getType()));
 			}
 			outputs += output.getId() + ",";
 		}
 		// now get rid of the outputs that have been deleted
 		outputs = outputs.substring(0, outputs.length() - 1);
 		if (!outputs.equals(""))
 			executeUpdate(getPreparedStatement("delete from pipeline_output where id not in " + outputs + ") and pipeline=" + pipeline.getId()));
 		else
 			executeUpdate(getPreparedStatement("delete from pipeline_output where pipeline=" + pipeline.getId()));
 
 		Vector<String[]> temp = runQuery(getPreparedStatement("select id from pipeline_component where pipeline=" + pipeline.getId()));
 		HashMap<Integer, Integer> oldIds = new HashMap<Integer, Integer>();
 		for (String[] arrr : temp) {
 			oldIds.put(Integer.parseInt(arrr[0]), 0);
 		}
 
 		for (PipeComponent wrapper2 : pipeline.getSteps()) {
 			if (wrapper2 instanceof PipeWrapper) {
 				PipeWrapper wrapper = (PipeWrapper) wrapper2;
 				if (wrapper.getId() < 1) {
 					int stepId = executeUpdate(getPreparedStatement("insert into pipeline_component values( null," + wrapper.getWrapperId() + "," + pipeline.getId() + "," + wrapper.getPosition() + ",?," + 0 + ")", wrapper.getJobOptions()));
 					wrapper.setId(stepId);
 					for (Input input : wrapper.getWrapper().getInputs()) {
 						if (!input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 							if (input.getValue().equals("$'User Input'")) {
 								// get the input and add it to the pipeline inputs
 								Input newPipelineInput = input.clone();
 								newPipelineInput.setId(-1);
 
 							}
 							executeUpdate(getPreparedStatement("insert into pipeline_component_values values(null," + stepId + "," + input.getId() + ",? )", input.getValue()));
 						}
 					}
 				} else {
 					oldIds.remove(wrapper.getId());
 					// this isn't new so update stuff
 					executeUpdate(getPreparedStatement("update pipeline_component set job_options=?, pipe=" + 0 + ",step=" + wrapper.getPosition() + " where id=" + wrapper.getId(), wrapper.getJobOptions()));
 					// delete old values and add the new ones
 					executeUpdate(getPreparedStatement("delete from pipeline_component_values where pipeline_component=" + wrapper.getId()));
 					for (Input input : wrapper.getWrapper().getInputs()) {
 						if (!input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 							executeUpdate(getPreparedStatement("insert into pipeline_component_values values(null," + wrapper.getId() + "," + input.getId() + ",? )", input.getValue()));
 						}
 					}
 				}
 			} else if (wrapper2 instanceof PipelineWrapper) {
 				PipelineWrapper wrapper = (PipelineWrapper) wrapper2;
 				if (wrapper.getId() < 1) {
 					savePipeline(wrapper.getPipeline());
 					int stepId = executeUpdate(getPreparedStatement("insert into pipeline_component values( null," + 0 + "," + pipeline.getId() + "," + wrapper.getPosition() + ",?," + wrapper.getPipelineId() + ")", ""));
 					wrapper.setId(stepId);
 					for (Input input : wrapper.getInputs()) {
 						if (!input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 							executeUpdate(getPreparedStatement("insert into pipeline_component_values values(null," + stepId + "," + input.getId() + ",? )", input.getValue()));
 						}
 					}
 				} else {
 					savePipeline(wrapper.getPipeline());
 					oldIds.remove(wrapper.getId());
 					// this isn't new so update stuff
 					executeUpdate(getPreparedStatement("update pipeline_component set job_options=?, pipe=" + wrapper.getPipelineId() + ",step=" + wrapper.getPosition() + " where id=" + wrapper.getId(), ""));
 					// delete old values and add the new ones
 					executeUpdate(getPreparedStatement("delete from pipeline_component_values where pipeline_component=" + wrapper.getId()));
 					for (Input input : wrapper.getPipeline().getInputs()) {
 						if (!input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 							executeUpdate(getPreparedStatement("insert into pipeline_component_values values(null," + wrapper.getId() + "," + input.getId() + ",? )", input.getValue()));
 						}
 					}
 				}
 			}
 		}
 
 		String ids = "";
 		Iterator<Integer> it = oldIds.keySet().iterator();
 		while (it.hasNext()) {
 			if (!ids.equals("")) {
 				ids += ",";
 			}
 			ids += "" + it.next();
 		}
 		if (ids.length() > 0) {
 			executeUpdate(getPreparedStatement("delete from pipeline_component where id in (" + ids + ")"));
 			executeUpdate(getPreparedStatement("delete from pipeline_component_values where pipeline_component  in (" + ids + ")"));
 		}
 		return pipeline;
 	}
 
 	public Pipeline saveAsPipeline(Pipeline pipeline) {
 		int pipelineId = executeUpdate(getPreparedStatement("insert into pipeline values(null,?,?," + pipeline.getCreatorId() + "," + pipeline.isPublic() + ",0,null)", pipeline.getName(), pipeline.getDescription()));
 		pipeline.setId(pipelineId);
 
 		for (Input input : pipeline.getInputs()) {
 			input.setId(executeUpdate(getPreparedStatement("insert into pipeline_input values (null,?," + pipelineId + ",?,?," + input.isRequired() + "," + input.getOrder() + ",?,?)", input.getDescription(), input.getName(), input.getDefaultValue(), input.getType(), input.getDisplayType())));
 		}
 
 		for (Output output : pipeline.getOutputs()) {
 			output.setId(executeUpdate(getPreparedStatement("insert into pipeline_output values (null," + pipelineId + ",?,?,?,?)", output.getName(), output.getDescription(), output.getValue(), output.getType())));
 		}
 
 		for (PipeComponent wrapper2 : pipeline.getSteps()) {
 			if (wrapper2 instanceof PipeWrapper) {
 				PipeWrapper wrapper = (PipeWrapper) wrapper2;
 				int stepId = executeUpdate(getPreparedStatement("insert into pipeline_component values( null," + wrapper.getWrapperId() + "," + pipelineId + "," + wrapper.getPosition() + ",?," + 0 + ")", wrapper.getJobOptions()));
 				wrapper.setId(stepId);
 				Vector<Input> inputs = wrapper.getWrapper().getInputs();
 				for (Input input : inputs) {
 					if (!input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 						executeUpdate(getPreparedStatement("insert into pipeline_component_values values(null," + stepId + "," + input.getId() + ",? )", input.getValue()));
 					}
 				}
 			} else if (wrapper2 instanceof PipelineWrapper) {
 				PipelineWrapper wrapper = (PipelineWrapper) wrapper2;
 				Pipeline pipelinePeice = wrapper.getPipeline();
 				if (pipelinePeice.getId() > 0) {
 					savePipeline(pipelinePeice);
 				} else {
 					saveAsPipeline(pipelinePeice);
 					System.out.println("saved pipeline with id of " + pipelinePeice.getId());
 				}
 				int stepId = executeUpdate(getPreparedStatement("insert into pipeline_component values( null," + 0 + "," + pipelineId + "," + wrapper.getPosition() + ",?," + pipelinePeice.getId() + ")", ""));
 				wrapper.setId(stepId);
 				Vector<Input> inputs = wrapper.getInputs();
 				for (Input input : inputs) {
 					if (!input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 						executeUpdate(getPreparedStatement("insert into pipeline_component_values values(null," + stepId + "," + input.getId() + ",? )", input.getValue()));
 					}
 				}
 			}
 		}
 		return pipeline;
 	}
 
 	public Vector<Plugin> getPlugins() {
 		Vector<Plugin> ret = new Vector<>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select * from plugin");
 			while (rs.next()) {
 				Plugin tempPlugin = new Plugin();
 				tempPlugin.setAuthor(rs.getString("author"));
 				tempPlugin.setName(rs.getString("name"));
 				tempPlugin.setDescription(rs.getString("description"));
 				tempPlugin.setVersion(rs.getString("version"));
 				tempPlugin.setEmail(rs.getString("email"));
 				tempPlugin.setIcon(rs.getString("icon"));
 				tempPlugin.setIdentifier(rs.getString("identifier"));
 				tempPlugin.setId(rs.getInt("id"));
 				tempPlugin.setIndex(rs.getString("index"));
 				Vector<String[]> tempFileTypes = runQuery(getPreparedStatement("select type from plugin_filetype where plugin=" + tempPlugin.getId()));
 				Vector<String[]> tempPermissions = runQuery(getPreparedStatement("select permission from plugin_permission where plugin=" + tempPlugin.getId()));
 				tempPlugin.setPermissions(new Vector<String>());
 				tempPlugin.setFileTypes(new Vector<String>());
 				for (String[] tempType : tempFileTypes) {
 					tempPlugin.getFileTypes().add(tempType[0]);
 				}
 				for (String[] tempPer : tempPermissions) {
 					tempPlugin.getPermissions().add(tempPer[0]);
 				}
 				ret.add(tempPlugin);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 
 	}
 
 	public PendingCluster getPendingCluster(int request) {
 		PendingCluster ret = null;
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select * from pending_cluster where id=" + request);
 			if (rs.next()) {
 				ret = new PendingCluster(rs.getString("email"), rs.getString("request"), rs.getString("organization"), rs.getString("status"), rs.getString("username"));
 				ret.setServer(rs.getString("server"));
 				ret.setUserId(rs.getInt("user"));
 				ret.setId(rs.getInt("id"));
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public Vector<PendingCluster> getPendingClusters() {
 		Vector<PendingCluster> ret = new Vector<>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select * from pending_cluster where type=" + 1);
 			while (rs.next()) {
 				PendingCluster temp = new PendingCluster(rs.getString("email"), rs.getString("request"), rs.getString("organization"), rs.getString("status"), rs.getString("username"));
 				temp.setServer(rs.getString("server"));
 				temp.setId(rs.getInt("id"));
 				temp.setUserId(rs.getInt("user"));
 				ret.add(temp);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public Vector<PendingCluster> getUserPendingClusters(int id) {
 		Vector<PendingCluster> ret = new Vector<>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("Select * from pending_cluster where type=" + 0 + " and user=" + id);
 			while (rs.next()) {
 				PendingCluster temp = new PendingCluster(rs.getString("email"), rs.getString("request"), rs.getString("organization"), rs.getString("status"), rs.getString("username"));
 				temp.setServer(rs.getString("server"));
 				temp.setId(rs.getInt("id"));
 				temp.setUserId(rs.getInt("user"));
 				ret.add(temp);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public PendingCluster getPendingCluster(String request) {
 		PendingCluster ret = null;
 		PreparedStatement statement;
 		try {
 			statement = getPreparedStatement("Select * from pending_cluster where request=?", request);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				ret = new PendingCluster(rs.getString("email"), rs.getString("request"), rs.getString("organization"), rs.getString("status"), rs.getString("username"));
 				ret.setServer(rs.getString("server"));
 				ret.setId(rs.getInt("id"));
 				ret.setUserId(rs.getInt("user"));
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public Cluster getCluster(int globalCluster, int user) {
 		Cluster ret = null;
 		PreparedStatement statement;
 		try {
 			statement = getPreparedStatement("select * from user_cluster u left join global_cluster g on g.id=u.global where u.user=" + user + " and g.id=" + globalCluster);
 			ResultSet rs = statement.executeQuery();
 			if (rs.next()) {
 				ret = new Cluster(rs.getInt("g.id"), rs.getString("u.key"), rs.getString("g.address"), rs.getString("u.company"), rs.getString("g.key"));
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public ArrayList<Job> getChildJobs(int jobId) {
 		ArrayList<Job> ret = new ArrayList<Job>();
 		Statement statement;
 		try {
 			statement = getConnect().createStatement();
 			ResultSet rs = statement.executeQuery("select * from job j  where parent=" + jobId + " order by j.id asc");
 			while (rs.next()) {
 				Job job = new Job();
 				job.setDate(rs.getDate("j.time"));
 				job.setId(rs.getInt("j.id"));
 				job.setName(rs.getString("j.name"));
 				job.setMachine(rs.getString("j.machine"));
 				job.setStatus(rs.getString("j.status"));
 				job.setUserId(rs.getInt("j.user"));
 				job.setWaitingFor(rs.getInt("j.waiting_for"));
 				job.setPipeline(rs.getInt("j.pipeline"));
 				job.setParent(rs.getInt("j.parent"));
 				job.setFinishedTime(formatDate(rs.getTimestamp("j.finished_time")));
 				job.setSubmitTime(formatDate(rs.getTimestamp("j.submit_time")));
 				job.setRunTime(formatDate(rs.getTimestamp("j.run_time")));
 				job.setSpecs(rs.getString("j.specs"));
 				job.setStdoutPath(rs.getString("j.stdoutPath"));
 				job.setWorkingDir(rs.getString("j.working_dir"));
 				int wrapper = rs.getInt("j.wrapper");
 				if (wrapper > 0)
 					job.setWrapper(getWrapperFromId(wrapper));
 				ResultSet rs2 = getConnect().createStatement().executeQuery("select input,value from job_value where job=" + job.getId());
 				while (rs2.next()) {
 					job.setInput(rs2.getInt("input"), rs2.getString("value"));
 				}
 				rs2.getStatement().close();
 				rs2.close();
 				ret.add(job);
 			}
 			rs.close();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		int size = ret.size();
 		for (int i = 0; i < size; i++) {
 			Job job = ret.get(i);
 			if (job.getPipeline() != 0) {
 				ret.addAll(getChildJobs(job.getId()));
 			}
 		}
 
 		return ret;
 	}
 
 	public void saveJob(Job job) {
 		executeUpdate(getPreparedStatement("update job set name=?, working_dir=?, specs=? where id=" + job.getId(), job.getName(), job.getWorkingDir(), job.getSpecs()));
 		executeUpdate(getPreparedStatement("delete from job_value where job=" + job.getId()));
 		if (job.getWrapper() != null) {
 			Vector<Input> inputs = job.getWrapper().getInputs();
 			for (Input input : inputs) {
 				if (input.getValue() != null && !input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 					executeUpdate(getPreparedStatement("insert into job_value values(null," + job.getId() + "," + input.getId() + ",? )", input.getValue()));
 				}
 			}
 			return;
 		} else if (job.getPipelineObject() != null) {
 			Vector<Input> inputs = job.getPipelineObject().getInputs();
 			for (Input input : inputs) {
 				if (input.getValue() != null && !input.getValue().equals("") && !input.getValue().equals(input.getDefaultValue())) {
 					executeUpdate(getPreparedStatement("insert into job_value values(null," + job.getId() + "," + input.getId() + ",? )", input.getValue()));
 				}
 			}
 			return;
 		}
 	}
 }
