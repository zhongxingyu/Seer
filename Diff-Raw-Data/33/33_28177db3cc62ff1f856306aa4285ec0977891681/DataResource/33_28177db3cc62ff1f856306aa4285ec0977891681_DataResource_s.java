 package election.stats;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 @Path("/")
 public class DataResource {
 
 	@Context
 	private ServletContext S;
 
 	@GET
 	@Path("party/{partyname}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Party getParty(@PathParam("partyname") String partyname)
 			throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		Party toReturn = new Party(partyname, null, null, null, null, null, 0);
 
 		// Querying Party Info
 		String query = "select * from party where name = '" + partyname + "';";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 			throw new WebApplicationException(404);
 		} else {
 			// Ideology of the party
 			String ideology = rs.getString(2);
 			if (ideology == null)
 				toReturn.setIdeology("N/A");
 			else
 				toReturn.setIdeology(ideology);
 
 			int followers = rs.getInt(4);
 			toReturn.setFollowers(followers);
 			// Chairperson of the party
 			String chairpersonname = rs.getString(8);
 			String chairpersondob = rs.getString(9);
 			if (chairpersonname == null) {
 				chairpersonname = "N/A";
 				chairpersondob = "N/A";
 			} else if (chairpersondob == null) {
 				chairpersondob = "N/A";
 			}
 			toReturn.setChairPerson(new Person(chairpersonname, chairpersondob,
 					"N/A", "N/A", "N/A", null));
 
 			// History of the party
 			String history = rs.getString(3);
 			if (history == null) {
 				history = "N/A";
 			}
 			toReturn.setHistory(history);
 
 			// Image of the party
 			String image = rs.getString(6);
 			if (image == null) {
 				image = "0.jpg";
 			}
 			toReturn.setImage(image);
 		}
 
 		// Notable people of the party
 		ArrayList<Person> notablePerson = new ArrayList<Person>();
 		query = "select * from NotablePerson where partyname = '" + partyname
 				+ "';";
 		rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			String notablePersonName = rs.getString(2);
 			String notablePersonDOB = rs.getString(3);
 			if (notablePersonDOB == null) {
 				notablePersonDOB = "N/A";
 			}
 			notablePerson.add(new Person(notablePersonName, notablePersonDOB,
 					"N/A", "N/A", "N/A", null));
 		}
 		toReturn.setNotablePerson(notablePerson);
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("person/{personname}/{personDOB}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Person getPerson(@PathParam("personname") String personname,
 			@PathParam("personDOB") String persondob) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		Person toReturn = new Person(personname, persondob, null, null, null,
 				null);
 
 		// Querying Person Info
 		String query = "select * from Person where name = '" + personname
 				+ "' and DOB = '" + persondob + "';";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 			throw new WebApplicationException(404);
 		} else {
 			// Photo of the person
 			String photo = rs.getString(3);
 			if (photo == null)
 				photo = "0.jpg";
 			toReturn.setPhoto(photo);
 
 			// History of the person
 			String history = rs.getString(4);
 			if (history == null)
 				history = "N/A";
 			toReturn.setHistory(history);
 
 			// Sex of the person
 			String sex = rs.getString(6);
 			if (sex == null)
 				sex = "N/A";
 			toReturn.setSex(sex);
 		}
 
 		// Active relatives of the person
 		ArrayList<Person> activeRelatives = new ArrayList<Person>();
 		query = "select * from ActiveRelatives where name1 = '" + personname
 				+ "' and DOB1 = '" + persondob + "';";
 		rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			String name = rs.getString(2);
 			String dob = rs.getString(4);
 
 			Person person = new Person(name, dob, null, null, null, null);
 
 			activeRelatives.add(person);
 		}
 
 		query = "select * from ActiveRelatives where name2 = '" + personname
 				+ "' and DOB2 = '" + persondob + "';";
 		rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			String name = rs.getString(1);
 			String dob = rs.getString(3);
 
 			Person person = new Person(name, dob, null, null, null, null);
 
 			activeRelatives.add(person);
 		}
 		toReturn.setActiveRelatives(activeRelatives);
 		return toReturn;
 	}
 
 	@GET
 	@Path("state/{stateName}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public State getState(@PathParam("stateName") String statename)
 			throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		State toReturn = new State(statename, null);
 
 		// Querying State Info
 		String query = "select * from State where name = '" + statename + "';";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 			throw new WebApplicationException(404);
 		} else {
 			// Major party of the state
 			String majorParty = rs.getString(2);
 			if (majorParty == null)
 				majorParty = "N/A";
 			toReturn.setMajorParty(majorParty);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("search/{personName}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<Person> getPerson(
 			@PathParam("personName") String personname) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<Person> toReturn = new ArrayList<Person>();
 		personname = personname.toLowerCase();
 
 		// Querying State Info
 		String query = "select name, dob from person where name like '%"
 				+ personname + "%';";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 
 		for (int i = 0; i < 10 && rs.next(); i++) {
 			toReturn.add(new Person(rs.getString(1), rs.getString(2), "0.jpg",
 					"N/A", "N/A", null));
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("election/{electionYear}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getElection(@PathParam("electionYear") String electionyear)
 			throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		String toReturn = null;
 
 		// Querying State Info
 		String query = "select * from Election where year = " + electionyear
 				+ ";";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 			throw new WebApplicationException(404);
 		} else {
 			toReturn = rs.getString(2);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("constituency/{stateName}/{constituencyName}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Constituency getConstituency(
 			@PathParam("stateName") String statename,
 			@PathParam("constituencyName") String constituencyname)
 					throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		Constituency toReturn = new Constituency(constituencyname, statename,
 				null, null, null, null);
 
 		// Querying State Info
 		String query = "select * from Constituency where name = '"
 				+ constituencyname + "' and statename = '" + statename + "';";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 			throw new WebApplicationException(404);
 		} else {
 			// facts of the constituency
 			String facts = rs.getString(3);
 			if (facts == null)
 				facts = "N/A";
 			toReturn.setFacts(facts);
 
 			// foundation year of the constituency
 			String foundationYear = "N/A";
 			System.out.println(rs.getInt(4));
 			if (rs.getInt(4) != 0)
 				foundationYear = String.valueOf(rs.getInt(4));
 			toReturn.setFoundationYear(foundationYear);
 
 			// major party of the constituency
 			String majorParty = rs.getString(5);
 			if (majorParty == null)
 				majorParty = "N/A";
 			toReturn.setMajorParty(majorParty);
 		}
 
 		// Notable people of the party
 		ArrayList<Person> notablePerson = new ArrayList<Person>();
 		query = "select * from NotablePersonConstituency where name = '"
 				+ constituencyname + "' and statename = '" + statename + "';";
 		rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			String notablePersonName = rs.getString(3);
 			String notablePersonDOB = rs.getString(4);
 			if (notablePersonDOB == null) {
 				notablePersonDOB = "N/A";
 			}
 			notablePerson.add(new Person(notablePersonName, notablePersonDOB,
 					"N/A", "N/A", "N/A", null));
 		}
 		toReturn.setNotablePerson(notablePerson);
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("list/state")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<String> getStateList() throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<String> toReturn = new ArrayList<String>();
 
 		// Querying State List
 		String query = "select Name from State";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			// name of the state
 			String name = rs.getString(1);
 			toReturn.add(name);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("list/election")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<String> getElectionList() throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<String> toReturn = new ArrayList<String>();
 
 		// Querying State List
 		String query = "select year from Election";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			// name of the state
 			String name = String.valueOf(rs.getInt(1));
 			toReturn.add(name);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("list/party")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<String> getPartyList() throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<String> toReturn = new ArrayList<String>();
 
 		// Querying State List
 		String query = "select name from Party";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			// name of the state
 			String name = rs.getString(1);
 			toReturn.add(name);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("list/{stateName}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<String> getConstituencyList(
 			@PathParam("stateName") String statename) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<String> toReturn = new ArrayList<String>();
 
 		// Querying State List
 		String query = "select Name from Constituency where StateName = '"
 				+ statename + "';";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			// name of the state
 			String name = rs.getString(1);
 			toReturn.add(name);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("candidate/")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<Candidate> getCandidateList(
 			@QueryParam("constituency") String constituencyName,
 			@QueryParam("state") String stateName,
 			@QueryParam("election") String electionYear,
 			@QueryParam("party") String partyName,
 			@QueryParam("personname") String personName,
 			@QueryParam("persondob") String personDOB) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<Candidate> toReturn = new ArrayList<Candidate>();
 
 		// Querying State List
 		String query = "select * from Candidate where candidateID > 0";
 		if (!constituencyName.isEmpty()) {
 			query = query + " and constituencyName = '" + constituencyName
 					+ "'";
 		}
 		if (!stateName.isEmpty()) {
 			query = query + " and stateName = '" + stateName + "'";
 		}
 		if (!electionYear.isEmpty()) {
 			query = query + " and year = " + String.valueOf(electionYear);
 		}
 		if (!partyName.isEmpty()) {
 			query = query + " and partyName = '" + partyName + "'";
 		}
 		if (!personName.isEmpty()) {
 			query = query + " and personName = '" + personName + "'";
 		}
 		if (!personDOB.isEmpty()) {
 			query = query + " and personDOB = '" + personDOB + "'";
 		}
 		query = query + ";";
 
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			Candidate candidate = new Candidate(0, 0, 0.0, 0, null, null, null,
 					null, null);
 
 			// id of the candidate
 			int id = rs.getInt(1);
 			candidate.setCandidateID(id);
 
 			// votes of the candidate
 			int votes = rs.getInt(2);
 			candidate.setVotes(votes);
 
 			// election year of the candidate
 			int year = rs.getInt(3);
 			candidate.setYear(year);
 
 			// result of the candidate
 			boolean res = rs.getBoolean(4);
 			String result;
 			if (res) {
 				result = "Won";
 			} else {
 				result = "Lost";
 			}
 			candidate.setResult(result);
 
 			// party of the candidate
 			String party = rs.getString(5);
 			if (party == null) {
 				party = "N/A";
 			}
 			candidate.setParty(party);
 
 			// constituency of the candidate
 			String constituency = rs.getString(6);
 			if (constituency == null) {
 				constituency = "N/A";
 			}
 			candidate.setConstituency(constituency);
 
 			// State of the candidate
 			String state = rs.getString(7);
 			if (state == null) {
 				state = "N/A";
 			}
 			candidate.setState(state);
 
 			// Person information of the candidate
 			String personname = rs.getString(8);
 			String persondob = rs.getString(9);
 
 			Person person = new Person(personname, persondob, "N/A", "N/A",
 					"N/A", null);
 			candidate.setPerson(person);
 
 			toReturn.add(candidate);
 		}
 
 		for (int i = 0; i < toReturn.size(); i++) {
 
 			Person person = toReturn.get(i).getPerson();
 
 			// Person info of candidate
 			query = "Select * from person where name = '" + person.getName()
 					+ "' and DOB = '" + person.getDob() + "';";
 			ResultSet rs2 = queryDB.executeQuery(query);
 			if (!rs2.next()) {
 
 			} else {
 				String Photo = rs2.getString(3);
 				if (Photo == null)
 					Photo = "0.jpg";
 				person.setPhoto(Photo);
 
 				String sex = rs2.getString(6);
 				if (sex == null)
 					sex = "N/A";
 				person.setSex(sex);
 
 				toReturn.get(i).setPerson(person);
 			}
 
 			// Percent votes for the candidate
 			query = "Select * from stats where electionYear = '"
 					+ String.valueOf(toReturn.get(i).getYear())
 					+ "' and constituencyName = '"
 					+ toReturn.get(i).getConstituency() + "' and stateName = '"
 					+ toReturn.get(i).getState() + "';";
 			rs2 = queryDB.executeQuery(query);
 			System.out.println(query);
 			if (!rs2.next()) {
 				toReturn.get(i).setPercentVotes(24.5);
 			} else {
 				Double percentage = 24.0;
 				int totalVotes = rs2.getInt(5);
 				if (totalVotes != 0)
 					percentage = (double) (toReturn.get(i).getVotes() * 100)
 					/ totalVotes;
 				System.out.println(percentage);
 				System.out.println(totalVotes);
 				toReturn.get(i).setPercentVotes(percentage);
 			}
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("discussion/{index}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<Discussion> getDiscussionList(
 			@PathParam("index") String index,
 			@QueryParam("constituency") String constituencyName,
 			@QueryParam("state") String stateName,
 			@QueryParam("election") String electionYear,
 			@QueryParam("party") String partyName,
 			@QueryParam("personname") String personName,
 			@QueryParam("persondob") String personDOB) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<Discussion> toReturn = new ArrayList<Discussion>();
 		ArrayList<Integer> discussionIds = new ArrayList<Integer>();
 
 		String query = "";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs;
 
 		// Discussions about constituency
 		if (!constituencyName.isEmpty() && !stateName.isEmpty()) {
 			query = "select id from constituencyStarter where constituencyname = '"
 					+ constituencyName
 					+ "' and stateName = '"
 					+ stateName
 					+ "';";
 			rs = queryDB.executeQuery(query);
 			while (rs.next()) {
 				discussionIds.add(rs.getInt(1));
 			}
 		}
 
 		// Discussion about election
 		if (!electionYear.isEmpty()) {
 			query = "select id from electionStarter where year = "
 					+ electionYear + ";";
 			rs = queryDB.executeQuery(query);
 			while (rs.next()) {
 				discussionIds.add(rs.getInt(1));
 			}
 		}
 
 		// Discussion about party
 		if (!partyName.isEmpty()) {
 			query = "select id from partyStarter where partyName = '"
 					+ partyName + "';";
 			rs = queryDB.executeQuery(query);
 			while (rs.next()) {
 				discussionIds.add(rs.getInt(1));
 			}
 		}
 
 		// Discussion about person
 		if (!personName.isEmpty() && !personDOB.isEmpty()) {
 			query = "select id from personStarter where personName = '"
 					+ personName + "' and personDOB = '" + personDOB + "';";
 			rs = queryDB.executeQuery(query);
 			while (rs.next()) {
 				discussionIds.add(rs.getInt(1));
 			}
 		}
 
 		Collections.sort(discussionIds);
 
 		for (int i = discussionIds.size() - 1; i > discussionIds.size()
 				- Integer.parseInt(index)
 				&& i >= 0; i--) {
 			int id = discussionIds.get(i);
 			Discussion discussion = new Discussion(id, null, null);
 			query = "select * from discussion where id = " + id + ";";
 			rs = queryDB.executeQuery(query);
 			String emailId = "";
 			if (!rs.next()) {
 
 			} else {
 				String content = rs.getString(2);
 				discussion.setContent(content);
 
 				emailId = rs.getString(3);
 			}
 
 			query = "select * from users where emailid = '" + emailId + "';";
 			rs = queryDB.executeQuery(query);
 			if (!rs.next()) {
 
 			} else {
 				String name = rs.getString(2);
 				discussion.setName(name);
 			}
 
 			toReturn.add(discussion);
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("stats/votePercentage")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<VotePercentage> getVotePercentageList(
 			@QueryParam("state") List<String> states,
 			@QueryParam("year") List<String> years,
 			@QueryParam("party") List<String> parties) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<VotePercentage> toReturn = new ArrayList<VotePercentage>();
 
 		String query = "";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs;
 
 		if (states.get(0).equals("") || years.get(0).equals("")
 				|| parties.get(0).equals("")) {
 
 		} else {
 			String stateSet = "(";
 			for (int i = 0; i < states.size(); i++) {
 				stateSet += "'" + states.get(i) + "',";
 			}
 			stateSet = stateSet.substring(0, stateSet.length() - 1) + ")";
 
 			String partySet = "(";
 			for (int i = 0; i < parties.size(); i++) {
 				partySet += "'" + parties.get(i) + "',";
 			}
 			partySet = partySet.substring(0, partySet.length() - 1) + ")";
 
 			String yearSet = "(";
 			for (int i = 0; i < years.size(); i++) {
 				yearSet += years.get(i) + ",";
 			}
 			yearSet = yearSet.substring(0, yearSet.length() - 1) + ")";
 
 			query = "select sum(votes), statename, partyname, year from (select * from candidate where statename in "
 					+ stateSet
 					+ " and partyname in "
 					+ partySet
 					+ " and year in "
 					+ yearSet
 					+ ") group by statename, partyname, year";
 			rs = queryDB.executeQuery(query);
 
 			while (rs.next()) {
 				toReturn.add(new VotePercentage(rs.getInt(1), 0.0, rs
 						.getString(2), rs.getString(3), rs.getString(4)));
 			}
 
 			for (int i = 0; i < toReturn.size(); i++) {
 				query = "select sum(votes) from stats where statename = '"
 						+ toReturn.get(i).getState() + "' and electionYear = "
 						+ toReturn.get(i).getYear() + ";";
 				rs = queryDB.executeQuery(query);
 				toReturn.get(i).setPercentage(
 						(double) (toReturn.get(i).getVotes() * 100)
 						/ rs.getInt(1));
 			}
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("stats/femaleCandidates")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<FemaleCandidates> getFemaleCandidateList(
 			@QueryParam("state") List<String> states,
 			@QueryParam("year") List<String> years,
 			@QueryParam("party") List<String> parties) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<FemaleCandidates> toReturn = new ArrayList<FemaleCandidates>();
 
 		String query = "";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs;
 
 		if (states.get(0).equals("") || years.get(0).equals("")
 				|| parties.get(0).equals("")) {
 
 		} else {
 			String stateSet = "(";
 			for (int i = 0; i < states.size(); i++) {
 				stateSet += "'" + states.get(i) + "',";
 			}
 			stateSet = stateSet.substring(0, stateSet.length() - 1) + ")";
 
 			String partySet = "(";
 			for (int i = 0; i < parties.size(); i++) {
 				partySet += "'" + parties.get(i) + "',";
 			}
 			partySet = partySet.substring(0, partySet.length() - 1) + ")";
 
 			String yearSet = "(";
 			for (int i = 0; i < years.size(); i++) {
 				yearSet += years.get(i) + ",";
 			}
 			yearSet = yearSet.substring(0, yearSet.length() - 1) + ")";
 
 			query = "select count(*), statename, partyname, year from (select * from candidate, person where statename in "
 					+ stateSet
 					+ " and partyname in "
 					+ partySet
 					+ " and year in "
 					+ yearSet
 					+ " and person.sex = 'F' and candidate.personname = person.name and candidate.persondob = person.dob) as x group by statename, partyname, year;";
 			System.out.println(query);
 			rs = queryDB.executeQuery(query);
 
 			while (rs.next()) {
 				toReturn.add(new FemaleCandidates(rs.getInt(1),
 						rs.getString(2), rs.getString(3), rs.getString(4)));
 			}
 		}
 
 		return toReturn;
 	}
 
 	@GET
 	@Path("stats/winners")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<Winners> getWinnersList(
 
 			@QueryParam("state") List<String> states,
 			@QueryParam("year") List<String> years,
 			@QueryParam("party") List<String> parties) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<Winners> toReturn = new ArrayList<Winners>();
 
 		String query = "";
 		Statement queryDB = connectDB.conn.createStatement();
 		ResultSet rs;
 
 		if (states.get(0).equals("") || years.get(0).equals("")
 				|| parties.get(0).equals("")) {
 
 		} else {
 			String stateSet = "(";
 			for (int i = 0; i < states.size(); i++) {
 				stateSet += "'" + states.get(i) + "',";
 			}
 			stateSet = stateSet.substring(0, stateSet.length() - 1) + ")";
 
 			String partySet = "(";
 			for (int i = 0; i < parties.size(); i++) {
 				partySet += "'" + parties.get(i) + "',";
 			}
 			partySet = partySet.substring(0, partySet.length() - 1) + ")";
 
 			String yearSet = "(";
 			for (int i = 0; i < years.size(); i++) {
 				yearSet += years.get(i) + ",";
 			}
 			yearSet = yearSet.substring(0, yearSet.length() - 1) + ")";
 
 			query = "select count(*), statename, partyname, year from (select * from candidate where statename in "
 					+ stateSet
 					+ " and partyname in "
 					+ partySet
 					+ " and year in "
 					+ yearSet
 					+ " and results = true) AS x group by statename, partyname, year;";
 			System.out.println(query);
 			rs = queryDB.executeQuery(query);
 
 			while (rs.next()) {
 				toReturn.add(new Winners(rs.getInt(1), rs.getString(2), rs
 						.getString(3), rs.getString(4)));
 			}
 		}
 
 		return toReturn;
 	}
 	
 	@POST
 	@Path("comment/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	
 	public Discussion addComment(
 			@PathParam("id") String parentid,
 			@FormParam("content") String content,
 			@FormParam("emailid") String emailid			
 			) throws SQLException {
 
 		
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 
 		
 		// get email id of the user
 		String query;
 		Statement queryDB = connectDB.conn.createStatement(); 	
 		
         PreparedStatement pstmt;
         
 
         query = "Insert into Discussion (Content, EmailID) VALUES (?,?)";
         pstmt = connectDB.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
         pstmt.setString(1, content);
         pstmt.setString(2, emailid);
         pstmt.executeUpdate();
         ResultSet keys = pstmt.getGeneratedKeys();
         int key = 0;
         if(keys.next()){
         	key = keys.getInt(1);
         }
         
         query = "Insert into Comment values ("  + Integer.toString(key) + ", " + parentid + ");";
         queryDB.executeUpdate(query);
         
         Discussion toReturn = new Discussion(key, content, "");
         query = "select * from users where emailid = '" + emailid + "';";
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 
 		} else {
 			String name = rs.getString(2);
 			toReturn.setName(name);
 		}
         return toReturn;
         
 	}
 	
 	@POST
 	@Path("discussion")
 	@Produces(MediaType.APPLICATION_JSON)
 	
 	public Discussion createDiscussion(
 			@FormParam("constituency") String constituencyName,
 			@FormParam("state") String stateName,
 			@FormParam("election") String electionYear,
 			@FormParam("party") String partyName,
 			@FormParam("personname") String personName,
 			@FormParam("persondob") String personDOB,
 			@FormParam("content") String content,
 			@FormParam("emailid") String emailid) throws SQLException {
 		
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 
 		// get email id of the user
 		String query;
 		Statement queryDB = connectDB.conn.createStatement(); 	
 				
 		//get the id of the inserted discussion
 		PreparedStatement pstmt;
         query = "Insert into Discussion (Content, EmailID) VALUES (?,?)";
         pstmt = connectDB.conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
         pstmt.setString(1, content);
         pstmt.setString(2, emailid);
         pstmt.executeUpdate();
         ResultSet keys = pstmt.getGeneratedKeys();
         int key = 0;
         if(keys.next()){
         	key = keys.getInt(1);
         }
        
         //add the discussion to the starter
         //Discussions about constituency
   		if(!constituencyName.isEmpty() && !stateName.isEmpty()){
  			query = "insert into starter values (" + key + ", '" + constituencyName  + "');";
  			queryDB.executeUpdate(query);	
  			query = "insert into starter values (" + key + ", '" + stateName  + "');";
  			queryDB.executeUpdate(query); 
   			query = "insert into constituencyStarter values (" + key + ", '" + constituencyName + "', '" + stateName  + "');";
   			queryDB.executeUpdate(query); 
   		}
       		
       	//Discussion about election
   		if(!electionYear.isEmpty()){
  			query = "insert into starter values (" + key + ", " + electionYear  + ");";
  			queryDB.executeUpdate(query);
   			query = "insert into electionStarter values (" + key + ", '" + electionYear + "');";
   			queryDB.executeUpdate(query);
   		}
       		
       	//Discussion about party
   		if(!partyName.isEmpty()){
  			query = "insert into starter values (" + key + ", " + partyName  + ");";
   			queryDB.executeUpdate(query);
   		}
       		
   		//Discussion about person
   		if(!personName.isEmpty() && !personDOB.isEmpty()){
  			query = "insert into starter values (" + key + ", " + personName  + ");";
   			queryDB.executeUpdate(query);
  		} 		
   		Discussion toReturn = new Discussion(key, content, "");
         query = "select * from users where emailid = '" + emailid + "';";
 		ResultSet rs = queryDB.executeQuery(query);
 		if (!rs.next()) {
 
 		} else {
 			String name = rs.getString(2);
 			toReturn.setName(name);
 		}
         return toReturn;
 	}
 	
 	@GET
 	@Path("comment/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<Discussion> getComments(@PathParam("id") String parentid) throws SQLException {
 
 
 		
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 		ArrayList<Discussion> toReturn = new ArrayList<Discussion>();
 		
 		// Querying for comments on a discussion
 		String query = "select Discussion.id, Discussion.content, Discussion.emailid from Comment,Discussion where parentDiscussionId = " + parentid + " and Discussion.id = comment.id;";
 		Statement queryDB = connectDB.conn.createStatement(); 	
 		ResultSet rs = queryDB.executeQuery(query);
 		while (rs.next()) {
 			int id = rs.getInt(1);
 			String content = rs.getString(2);
 			String email = rs.getString(3);
 			toReturn.add(new Discussion(id, content, email));			
 		}
 		
 		for(int i = 0; i < toReturn.size(); i++){
 			query = "select name from users where emailid = '" + toReturn.get(i).getName() + "';";
 			rs = queryDB.executeQuery(query);
 			if(!rs.next()){
 				
 			}
 			else{
 				toReturn.get(i).setName(rs.getString(1));
 			}
 		}
 		
 		return toReturn;
 	}
 	
 	@POST
 	@Path("user")
 	@Produces(MediaType.APPLICATION_JSON)
 	
 	public void addUser(@FormParam("username") String username, 
 
 
 			@FormParam("emailid") String emailid) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 				
 		// Querying for comments on a discussion
 		String query = "select count(*) from Users where emailid = '" + emailid + "';";
 		Statement queryDB = connectDB.conn.createStatement(); 	
 		ResultSet rs = queryDB.executeQuery(query);
 		
 		int count = 0;
 		if (!rs.next()) {
 		}
 		else{
 			count = rs.getInt(1);						
 		}
 		if(count == 0){
 			query = "Insert into Users Values('" + emailid + "','"+ username + "');";
 			queryDB.executeUpdate(query);
 		}
 	}
 	
 	@POST
 	@Path("follow")
 	@Produces(MediaType.APPLICATION_JSON)
 	
 	public int followParty(@FormParam("emailid") String emailid,
 			@FormParam("partyname") String partyname) throws SQLException {
 
 		ConnectDB connectDB = (ConnectDB) S.getAttribute("connectDB");
 				
 		// Querying for comments on a discussion
 		String query = "select count(*) from Follows where emailid = '" + emailid + "' and partyname = '" + partyname +"';";
 		Statement queryDB = connectDB.conn.createStatement(); 	
 		ResultSet rs = queryDB.executeQuery(query);
 		
 		int count = 0;
 		if (!rs.next()) {
 		}
 		else{
 			count = rs.getInt(1);						
 		}
 		if(count == 0){
 			query = "Insert into Follows Values('" + emailid + "','"+ partyname + "');";
 			queryDB.executeUpdate(query);
 			query = "select count(*) from Follows where emailid = '" + emailid + "' and partyname = '" + partyname +"';";
 			rs = queryDB.executeQuery(query);
 			if (!rs.next()) {
 				count = 0;
 			}
 			else{
 				count = rs.getInt(1);						
 			}
 			query = "update party set numberoffollowers="+ count +"where party='"+partyname+";";
 			queryDB.executeUpdate(query);
 		}
 		return count;
 	}
 }
