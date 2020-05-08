 /*
  * Decisions class
  */
 
 package edu.wpi.cs.jburge.SEURAT.rationaleData;
 
 import instrumentation.DataLog;
 
 import java.util.*;
 import java.io.*;
 
 import java.sql.Connection; 
 import java.sql.SQLException; 
 import java.sql.Statement;
 import java.sql.ResultSet;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.w3c.dom.*;
 
 import SEURAT.events.*;
 import edu.wpi.cs.jburge.SEURAT.editors.EditDecision;
 import edu.wpi.cs.jburge.SEURAT.editors.SelectConstraint;
 import edu.wpi.cs.jburge.SEURAT.inference.DecisionInferences;
 
 /**
  * This defines the structure of the Decision rationale element. Decisions
  * refer to decision problems that the user has to make that involve selecting
  * from alternatives. 
  * @author burgeje
  *
  */
 public class Decision extends RationaleElement implements Serializable
 {
 	// class variables
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4162618972367711832L;
 	// instance variables
 	/**
 	 * The type of decision - some decisions require choosing one alternative while
 	 * others might allow multiples. For example, a decision on what kind of multi-modal
 	 * interfaces should be used might let the user choose several different ones. In the 
 	 * single choice type, an example might be if the data should be stored in
 	 * a database or in a flat file format.
 	 */
 	DecisionType type;
 	/**
 	 * What development phase were we in when we needed to make the decision
 	 */
 	Phase devPhase;
 //	String artifact;
 	/**
 	 * The unique ID of our parent. Decisions can be at the top level or they
 	 * can be sub-decisions spawned when an alternative was selected. For example, if
 	 * the user decides to use a database rather than a flat file, they would then need
 	 * to decide which database.
 	 */
 	int parent;
 	/**
 	 * What type of parent (an alternative or maybe a decision broken into
 	 * sub-decisions)
 	 */
 	RationaleElementType ptype; 
 	/**
 	 * Alternatives to solve the decision problem
 	 */
 	boolean alts;
 	/**
 	 * The designer posing the decision (or maybe the designer assigned
 	 * responsibility for the decision? - here's a decision problem we need to solve!)
 	 */
 	Designer designer;
 	/**
 	 * Any constraints on this decision (weight limitations, etc.)
 	 */
 	Vector<Constraint> constraints;
 	
 	DecisionStatus status;		//don't know if maybe this should be an integer?
 	Vector<Alternative> alternatives;    //the decision will have alternatives *or* sub-decisions
 	Vector<Decision> subDecisions;
 	Vector<Question> questions;   
 	Vector<AlternativePattern> alternativePatterns;
 
 	private RationaleElementUpdateEventGenerator<Decision> m_eventGenerator = 
 		new RationaleElementUpdateEventGenerator<Decision>(this);
 	
 	public Decision()
 	{
 		super();
 		status = DecisionStatus.UNRESOLVED;
 		alts = true;
 		alternatives = new Vector<Alternative>();
 		subDecisions = new Vector<Decision>();
 		questions = new Vector<Question>();
 		constraints = new Vector<Constraint>();
 		alternativePatterns = new Vector<AlternativePattern>();
 	} 
 	
 	public Element toXML(Document ratDoc)
 	{
 		Element decE;
 		RationaleDB db = RationaleDB.getHandle();
 		String decID = db.getRef(id);
 //		System.out.println("decision to XML");
 		if (decID != null)
 		{		
 //			System.out.println("decision with non-unique ID???");
 			//this should never be the case but just in case...
 			decE = ratDoc.createElement("decref");
 			//set the reference contents
 			Text text = ratDoc.createTextNode(decID);
 			decE.appendChild(text);
 		}
 		else 
 		{
 //			System.out.println("saving our decision");
 			decE = ratDoc.createElement("DR:decisionproblem");
 			decID = db.addRef(id);
 			decE.setAttribute("id", decID);
 			decE.setAttribute("name", name);
 			decE.setAttribute("type", type.toString());
 			decE.setAttribute("phase", devPhase.toString());
 			decE.setAttribute("status", status.toString());
 			
 			//save our description
 			Element descE = ratDoc.createElement("DR:description");
 			//set the reference contents
 			Text descText = ratDoc.createTextNode(description);
 			descE.appendChild(descText);
 			decE.appendChild(descE);
 
 			Enumeration alts = alternatives.elements();
 			while (alts.hasMoreElements())
 			{
 				Alternative alt = (Alternative) alts.nextElement();
 				decE.appendChild(alt.toXML(ratDoc));
 			}
 			
 			Enumeration decs = subDecisions.elements();
 			while (decs.hasMoreElements())
 			{
 				Decision dec = (Decision) decs.nextElement();
 				decE.appendChild(dec.toXML(ratDoc));
 			}
 			
 			Enumeration quests = questions.elements();
 			while (quests.hasMoreElements())
 			{
 				Question quest = (Question) quests.nextElement();
 				decE.appendChild(quest.toXML(ratDoc));
 			}
 			
 			//finally, the history
 			
 			Element ourHist = ratDoc.createElement("DR:history");
 			Enumeration hist = history.elements();
 			while (hist.hasMoreElements())
 			{
 				History his = (History) hist.nextElement();
 				ourHist.appendChild(his.toXML(ratDoc));
 			}
 			decE.appendChild(ourHist);
 		}
 		 
 		return decE;
 	}
 	public RationaleElementType getElementType()
 	{
 		return RationaleElementType.DECISION;
 	}
 	
 	public DecisionType getType()
 	{
 		return type;
 	}
 	public void setType(DecisionType dtp)
 	{
 		type = dtp;
 	}
 	
 	public DecisionStatus getStatus()
 	{
 		return status;
 	}
 	public void setStatus(DecisionStatus stat)
 	{
 		status = stat;
 	}
 	public Phase getPhase()
 	{
 		return devPhase;
 	}
 	public void setPhase(Phase ph)
 	{
 		devPhase = ph;
 	}
 	
 	public int getParent()
 	{
 		return parent;
 	}
 	
 	public RationaleElementType getPtype()
 	{
 		return ptype;
 	}
 	public boolean getAlts()
 	{
 		return alts;
 	}
 	public void setAlts(boolean al)
 	{
 		alts = al;
 	}
 	public Vector getAlternatives()
 	{
 		return alternatives;
 	}
 	public Vector getSubDecisions()
 	{
 		return subDecisions;
 	}
 	public Vector getQuestions()
 	{
 		return questions;
 	}
 	public void addAlternative(Alternative alt)
 	{
 		alternatives.addElement(alt);
 	}
 	public void addSubDecision(Decision dec)
 	{
 		subDecisions.addElement(dec);
 	}
 	public void addQuestion(Question quest)
 	{
 		questions.addElement(quest);
 	}
 	public void delAlternative(Alternative alt)
 	{
 		alternatives.remove(alt);
 	}
 	public void delSubDecision(Decision dec)
 	{
 		subDecisions.remove(dec);
 	}
 	public void delQuestion(Question quest)
 	{
 		questions.remove(quest);
 	}
 	
 	
 	public Vector getConstraints() {
 		return constraints;
 	}
 	
 	public void addConstraint(Constraint con)
 	{
 		constraints.addElement(con);
 	}
 	public void setConstraints(Vector<Constraint> constraints) {
 		this.constraints = constraints;
 	}
 	
 	public Designer getDesigner() {
 		return designer;
 	}
 	
 	public void setDesigner(Designer designer) {
 		this.designer = designer;
 	}
 	
 	/**
 	 * Gets a list of elements associated with this decision. These might
 	 * be decisions, alternatives, or questions)
 	 */
 	public Vector getList(RationaleElementType type)
 	{
 		if (type.equals(RationaleElementType.DECISION))
 		{
 			return subDecisions;
 		}
 		else if (type.equals(RationaleElementType.ALTERNATIVE))
 		{
 			return alternatives;
 		}
 		else if (type.equals(RationaleElementType.QUESTION))
 		{
 			return questions;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	/**
 	 * Find which alternative is selected for the decision
 	 * @return the selected alternative
 	 */
 	public Alternative getSelected()
 	{
 		boolean found = false;
 		Alternative alt = null;
 		Iterator altI = alternatives.iterator();
 		while ((altI.hasNext() && !found))
 		{
 			alt = (Alternative) altI.next();
 			found = (alt.getStatus() == AlternativeStatus.ADOPTED);
 		}
 		return alt;
 	}
 	
 	/**
 	 * Add a pattern to the database (on decisions table) to associate a patternID
 	 * to a decision (given decision name)
 	 * @param name
 	 * @param patternsID
 	 * @return
 	 */
 	public int savePatterns(String name, String patternsID){
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		Statement stmt = null;
 		String updateQuery = "";
 		
 		updateQuery = "Update decisions set patterns = '" + patternsID + "' where name = '" + name + "'";
 		try {
 			stmt = conn.createStatement();
 			stmt.execute(updateQuery);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return 0;
 	}
 	
 	/**
 	 * Save our decision to the database.
 	 * @param parent - the parent of the decision
 	 * @param ptype - the parent's type
 	 * @return the unique ID
 	 */
 	public int toDatabase(int parent, RationaleElementType ptype)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		String updateQuery = "";
 		int ourid = 0;
 		
 		RationaleUpdateEvent l_updateEvent;
 		
 		//find out if this requirement is already in the database
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		
 		String subsReq = "No";
 		if (!alts)
 			subsReq = "Yes";
 		
 		try {
 			stmt = conn.createStatement(); 
 			
 			if (inDatabase(parent,ptype))
 			{
 				//set up Designer update string
 				String updateD;
 				if (designer == null)
 					updateD = "D.designer = null";
 				else
 					updateD = "D.designer = " + designer.getID();
 				
 				updateQuery = "UPDATE decisions D " +
 				"SET D.parent = " + new Integer(parent).toString() +
 				", D.ptype = '" + ptype.toString() + 
 				"', D.phase = '" + devPhase.toString() +
 				"', D.description = '" + RationaleDBUtil.escape(description) +
 				"', D.type = '" + type.toString() +
 				"', D.name = '" + RationaleDBUtil.escape(name) +
 				"', D.status = '" + status.toString() + 
 				"', D.subdecreq = '" + subsReq +
 				"', " + updateD +
 				" WHERE " +
 				"D.id = " + this.id + " " ;
 				stmt.execute(updateQuery);
 				
 				l_updateEvent = m_eventGenerator.MakeUpdated();
 			}
 			else 
 			{
 				String parentSt;
 				String parentTSt;
 				
 				//now, we have determined that the decision is new
 				if ((this.parent < 0) || (ptype == null))
 				{
 					parentSt = "NULL";
 					parentTSt = "None";
 				}
 				else
 				{
 				     parentSt = new Integer(this.parent).toString();
 				     parentTSt = ptype.toString();
 				}
 				String updateD;
 				if (designer == null)
 					updateD = "null";
 				else
 					updateD = new Integer(designer.getID()).toString();
 				
 				updateQuery = "INSERT INTO Decisions "+
 				"(name, description, type, status, phase, subdecreq, parent, ptype, designer) " +
 				"VALUES ('" +
 				RationaleDBUtil.escape(this.name) + "', '" +
 				RationaleDBUtil.escape(this.description) + "', '" +
 				this.type.toString() + "', '" +
 				this.status.toString() + "', '" +
 				this.devPhase.toString() + "', '" +
 				subsReq + "', " +
 				parentSt + ", '" +
 				parentTSt + "', " +
 				updateD + ")";
 				
 				stmt.execute(updateQuery); 
 				
 				l_updateEvent = m_eventGenerator.MakeCreated();				
 			}
 			//in either case, we want to update any sub-requirements in case
 			//they are new!
 			//now, we need to get our ID
 			updateQuery = "SELECT id FROM decisions where name='" +
 			RationaleDBUtil.escape(this.name) + "'";
 			rs = stmt.executeQuery(updateQuery); 
 			
 			if (rs.next())
 			{
 				ourid = rs.getInt("id");
 				rs.close();
 			}
 			else
 			{
 				ourid = -1;
 			}
 			this.id = ourid;
 			
 			Enumeration alts = alternatives.elements();
 			while (alts.hasMoreElements())
 			{
 				Alternative alt = (Alternative) alts.nextElement();
 //				System.out.println("Saving alternative from decision");
 				alt.toDatabase(ourid, RationaleElementType.DECISION);
 			}
 			
 			Enumeration decs = subDecisions.elements();
 			while (decs.hasMoreElements())
 			{
 				Decision dec = (Decision) decs.nextElement();
 				dec.toDatabase(ourid, RationaleElementType.DECISION);
 			}
 			
 			Enumeration quests = questions.elements();
 			while (quests.hasMoreElements())
 			{
 				Question quest = (Question) quests.nextElement();
 				quest.toDatabase(ourid, RationaleElementType.DECISION);
 			}
 			
 			//finally, the history
 			
 			Enumeration hist = history.elements();
 			while (hist.hasMoreElements())
 			{
 				History his = (History) hist.nextElement();
 				his.toDatabase(ourid, RationaleElementType.DECISION);
 			}
 			
 			
 			//need to update our relationships with the constraints
 			Enumeration conkids = this.constraints.elements();
 			while (conkids.hasMoreElements())
 			{
 				Constraint kid = (Constraint) conkids.nextElement();
 				//if the parent ID is not zero, then update the parent-child relationship
 				
 				updateQuery = "SELECT * from ConDecRelationships WHERE " +
 				"constr = " + new Integer(kid.getID()).toString() +
 				" and decision = " + new Integer(ourid).toString();
 				
 				rs = stmt.executeQuery(updateQuery);
 				if (rs.next())
 				{
 					rs.close();
 				}
 				else
 				{
 					String insertRel = "INSERT INTO ConDecRelationships (constr, decision) " +
 					"VALUES (" +
 					new Integer(kid.getID()).toString() + ", " +
 					new Integer(ourid).toString() + ")";
 					System.out.println(insertRel);
 					stmt.execute(insertRel);
 				}
 				kid.toDatabase(ourid);
 			} //checking parent
 			
 			m_eventGenerator.Broadcast(l_updateEvent);	
 		} catch (SQLException ex) {
 			// handle any errors 
 			RationaleDB.reportError(ex,"Error in Decision.toDatabase", updateQuery);
 			
 		}
 		
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		
 		return ourid;	
 		
 	}	
 	
 	/**
 	 * Get our alternative from the database, given its unique id
 	 * @param decID - the decision ID
 	 */
 	public void fromDatabase(int decID)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		this.id = decID;
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		String findQuery = ""; 
 		try {
 			stmt = conn.createStatement();
 			
 			findQuery = "SELECT name  FROM " +
 			"decisions where id = " +
 			new Integer(decID).toString();
 //			***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			if (rs.next())
 			{
 				name = RationaleDBUtil.decode(rs.getString("name"));
 				rs.close();
 				this.fromDatabase(name);
 			}
 			
 		} catch (SQLException ex) {
 			// handle any errors 
 			RationaleDB.reportError(ex,"Error in Decision.fromDatabase(1)", findQuery);
 		}
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		
 	}
 
 	public RationaleElement getParentElement()
 	{
 		return RationaleDB.getRationaleElement(this.parent, this.ptype);
 	}
 	
 	/**
 	 * Get the decision from the database, given its name
 	 * @param name the decision name
 	 */
 	public void fromDatabase(String name)
 	{
 		String findQuery = "";
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		this.name = name;
 		name = RationaleDBUtil.escape(name);
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " +
 			"decisions where name = '" +
 			name + "'";
 //			***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			if (rs.next())
 			{
 				id = rs.getInt("id");
 				description = RationaleDBUtil.decode(rs.getString("description"));
 				type = (DecisionType) DecisionType.fromString(rs.getString("type"));
 				devPhase = (Phase) Phase.fromString(rs.getString("phase"));
 				ptype = RationaleElementType.fromString(rs.getString("ptype"));
 				parent = rs.getInt("parent");
 //				artifact = rs.getString("artifact");
 //				enabled = rs.getBoolean("enabled");
 				status = (DecisionStatus) DecisionStatus.fromString(rs.getString("status"));
 				String subdecs = rs.getString("subdecreq");
 				if (subdecs.compareTo("Yes") == 0)
 				{
 					alts = false;
 				}
 				else
 				{
 					alts = true;
 				}
 				
 				try {
 					int desID = rs.getInt("designer");
 					if (rs.wasNull()) 
 						throw new SQLException();
 					
 					designer = new Designer();
 					designer.fromDatabase(desID);
 				} catch (SQLException ex)
 				{
 					designer = null; //nothing...
 				}
 				
 				
 			}
 			rs.close();
 			//need to read in the rest - recursive routines?
 			subDecisions.removeAllElements();
 			alternatives.removeAllElements();
 			if (!alts)
 			{				
 				Vector<String> decNames = new Vector<String>();
 				findQuery = "SELECT name from DECISIONS where " +
 				"ptype = '" + RationaleElementType.DECISION.toString() +
 				"' and parent = " + new Integer(id).toString();
 //				***					System.out.println(findQuery2);
 				rs = stmt.executeQuery(findQuery);	
 				while (rs.next())				
 				{
 					decNames.add(RationaleDBUtil.decode(rs.getString("name")));
 				}
 				Enumeration decs = decNames.elements();
 				while (decs.hasMoreElements())
 				{
 					Decision subDec = new Decision();
 					subDec.fromDatabase((String) decs.nextElement());
 					subDecisions.add(subDec);
 				}
 				
 			}
 			else
 			{
 				Vector<String> altNames = new Vector<String>();
 				findQuery = "SELECT name from ALTERNATIVES where " +
 				"ptype = '" + RationaleElementType.DECISION.toString() +
 				"' and parent = " + new Integer(id).toString();
 //				***					System.out.println(findQuery2);
 				rs = stmt.executeQuery(findQuery);	
 				while (rs.next())				
 				{
 					altNames.add(RationaleDBUtil.decode(rs.getString("name")));
 				}
 				Enumeration alts = altNames.elements();
 				while (alts.hasMoreElements())
 				{
 					Alternative alt = new Alternative();
 					alt.fromDatabase((String) alts.nextElement());
 					alternatives.add(alt);
 				}
 				
 			}
 			
 			//need to do questions too 
 			Vector<String> questNames = new Vector<String>();
 			findQuery = "SELECT name from QUESTIONS where " +
 			"ptype = '" + RationaleElementType.DECISION.toString() +
 			"' and parent = " + new Integer(id).toString();
 //			***				System.out.println(findQuery3);
 			rs = stmt.executeQuery(findQuery);
 			while (rs.next())
 			{
 				questNames.add(RationaleDBUtil.decode(rs.getString("name")));
 			}
 			Enumeration quests = questNames.elements();
 			questions.removeAllElements();
 			while (quests.hasMoreElements())
 			{
 				Question quest = new Question();
 				quest.fromDatabase((String) quests.nextElement());
 				questions.add(quest);
 			}
 			
 			//no, not last - need history too
 			findQuery = "SELECT * from HISTORY where ptype = 'Decision' and " +
 			"parent = " + Integer.toString(id);
 //			***			  System.out.println(findQuery5);
 			rs = stmt.executeQuery(findQuery);
 			history.removeAllElements();
 			while (rs.next())
 			{
 				History nextH = new History();
 				nextH.setStatus(rs.getString("status"));
 				nextH.setReason(RationaleDBUtil.decode(rs.getString("reason")));
 				nextH.dateStamp = rs.getTimestamp("date");
 //				nextH.dateStamp = rs.getDate("date");
 				history.add(nextH);
 			}
 			
 			
 			//now, get our constraints
 			findQuery = "SELECT * from ConDecRelationships WHERE " +
 			"decision = " + new Integer(id).toString();
 			
 			rs = stmt.executeQuery(findQuery);
 			constraints.removeAllElements();
 			if (rs != null)
 			{
 				while (rs.next())
 				{
 					int ontID = rs.getInt("constr");
 					Constraint cont = new Constraint();
 					cont.fromDatabase(ontID);
 					this.addConstraint(cont);
 				}
 				rs.close();
 			}
 			
 			findQuery = "SELECT name FROM alternativepatterns WHERE ptype = 'decision' and parent = " + this.id;
 			rs = stmt.executeQuery(findQuery);
 			if (rs != null){
 				while(rs.next()){
 					AlternativePattern altPattern = new AlternativePattern();
 					altPattern.fromDatabase(RationaleDBUtil.decode(rs.getString("name")));
 					addAlternativePattern(altPattern);
 				}
 			}
 			
 		} catch (SQLException ex) {
 			// handle any errors 
 			RationaleDB.reportError(ex,"Error in Decision.fromDatabase", findQuery);
 			
 		}
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		
 	}
 	
 	
 	/**
 	 * Display our decision by bringing up the decision editor.
 	 * @param disp - points back to our display
 	 * @return true if the user cancels the editor
 	 */
 	public boolean display(Display disp)
 	{
 		EditDecision ar = new EditDecision(disp, this, false);
 //		System.out.println("this after = " + this.getStatus().toString());
 //		System.out.println(ar.getCanceled());
 		String msg = "Edited decision " + this.getName() + " " + ar.getCanceled();
 		DataLog d = DataLog.getHandle();
 		d.writeData(msg);
 		return ar.getCanceled(); //can I do this?
 		
 	}
 	/**
 	 * Create a new decision using the decision editor.
 	 * @param disp - points to the display
 	 * @param parent - the parent of our decision
 	 * @return true if the user cancels out
 	 */
 	public boolean create(Display disp, RationaleElement parent)
 	{
 //		System.out.println("create decision");
 		if (parent != null)
 		{
 			this.parent = parent.getID();
 			this.ptype = parent.getElementType();
 		}
 		else
 		{
 			this.parent = 0;
 		}
 
 		EditDecision ar = new EditDecision(disp, this, true);
 		System.out.println("name in create = " + this.getName());
 		return ar.getCanceled(); //can I do this?
 	}
 	
 	/**
 	 * Deletes a decision from SEURAT and the database. This only works
 	 * if there aren't any associated alternatives, questions, or subdecisions.
 	 */
 	public boolean delete()
 	{
 		//need to have a way to inform if delete did not happen
 		//can't delete if there are dependencies...
 		if ((this.alternatives.size() > 0) ||
 				(this.questions.size() > 0) ||
 				(this.subDecisions.size() > 0))
 		{
 			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are sub-elements.");
 			return true;
 		}
 		m_eventGenerator.Destroyed();
 		
 		RationaleDB db = RationaleDB.getHandle();
 		
 		db.deleteRationaleElement(this);
 		return false;
 		
 	}
 	
 	/**
 	 * Associates a constraint with the decision. This is done by bringing
 	 * up the constraint selection display.
 	 * @param disp - the constraint selection display
 	 * @return the constraint associated
 	 */
 	public RationaleElement associateElement(Display disp)
 	{
 		Constraint newCont = null;
 		SelectConstraint ar = new SelectConstraint(disp, true);
 		newCont = ar.getSelConstraint();
 		if (newCont != null)
 		{
 			addConstraint(newCont);
 			this.toDatabase(this.parent, this.ptype);
 		}
 		return newCont;
 	}
 	
 	/**
 	 * Used to set the parent data of the rationale element without
 	 * brigning up the edit alternative GUI (in conjunction with the
 	 * new editor GUI).
 	 * @param parent
 	 */
 	public void setParent(RationaleElement parent) {
 		if (parent != null)
 		{
 			this.parent = parent.getID();
 			this.ptype = parent.getElementType();
 		}
 	}
 	
 	public boolean hasParent()
 	{
 		if (parent > 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	/**
 	 * Inference over the decision to determine if the status should
 	 * be changed.
 	 * @return the status updates
 	 */
 	public Vector<RationaleStatus> updateStatus()
 	{
 		DecisionInferences inf = new DecisionInferences();
 		Vector<RationaleStatus> newStat = inf.updateDecisionStatus( this);
 		return newStat;
 	}
 	
 	
 	/**
 	 * If we delete a decision, the status of other elements may change so
 	 * we need to run the appropriate inferences.
 	 * @return the status updates
 	 */
 	public Vector<RationaleStatus> updateOnDelete()
 	{
 		DecisionInferences inf = new DecisionInferences();
 		Vector<RationaleStatus> newStat = inf.updateOnDelete( this);
 		return newStat;
 	}
 	
 	/**
 	 * Reads in a decision stored in XML.
 	 * @param decN - the XML element.
 	 */
 	public void fromXML(Element decN) {
 		
 		this.fromXML = true;
 		
 		RationaleDB db = RationaleDB.getHandle();
 		
 		//add idref ***from the XML***
 		String idref = decN.getAttribute("id");
 		
 		//get our name
 		name = decN.getAttribute("name");
 		
 		//get our status
 		status = DecisionStatus.fromString(decN.getAttribute("status"));
 		
 		//get our type
 		type = DecisionType.fromString(decN.getAttribute("type"));
 		
 		//get our phase
 		devPhase = Phase.fromString(decN.getAttribute("phase"));
 		
 		Node descN = decN.getFirstChild();
 		//get the description
 		//the text is actually the child of the element, odd...
 		Node descT = descN.getFirstChild();
 		if (descT instanceof Text) {
 			Text text = (Text) descT;
 			String data = text.getData();
 			setDescription(data);
 		}
 		
 		//and last....
 		db.addRef(idref, this); //important to use the ref from the XML file!
 		
 		Element child = (Element) descN.getNextSibling();
 		
 		while (child != null) {
 			
 			String nextName;
 			
 			nextName = child.getNodeName();
 			//here we check the type, then process
 			if (nextName.compareTo("DR:alternative") == 0) {
 				Alternative alt = new Alternative();
 				alts = true;
 				db.addAlternative(alt);
 				addAlternative(alt);
 				alt.fromXML(child);
 			}  else if (nextName.compareTo("DR:question") == 0) {
 				Question quest = new Question();
 				db.addQuestion(quest);
 				addQuestion(quest);	
 				quest.fromXML(child);
 			} else if (nextName.compareTo("DR:decisionproblem") == 0) {
 				Decision dec = new Decision();
 				alts = false;
 				db.addDecision(dec);
 				addSubDecision(dec);
 				dec.fromXML(child);
 			} else if (nextName.compareTo("DR:history") == 0) {
 				History hist = new History();
 				historyFromXML(child);
 				updateHistory(hist);
 				
 			} else if (nextName.compareTo("altref") == 0) {
 				Node childRef = child.getFirstChild(); //now, get the text
 				//decode the reference
 				Text refText = (Text) childRef;
 				String stRef = refText.getData();
 				alts = true;
 				addAlternative((Alternative) db.getRef(stRef));
 				
 			} else if (nextName.compareTo("decref") == 0) {
 				Node childRef = child.getFirstChild(); //now, get the text
 				//decode the reference
 				Text refText = (Text) childRef;
 				String stRef = refText.getData();
 				alts = false;
 				addSubDecision((Decision) db.getRef(stRef));
 				
 			} else if (nextName.compareTo("questref") == 0) {
 				Node childRef = child.getFirstChild(); //now, get the text
 				//decode the reference
 				Text refText = (Text) childRef;
 				String stRef = refText.getData();
 				addQuestion((Question) db.getRef(stRef));
 				
 			} else {
 				System.out.println("unrecognized element under argument!");
 			}
 			
 			child = (Element) child.getNextSibling();
 		}  
 	}
 	
 	/**
 	 * Check if our element is already in the database. The check is different
 	 * if you are reading it in from XML because you can do a query on the name.
 	 * Otherwise you can't because you run the risk of the user having changed the
 	 * name.
 	 * @param parentID the parent ID
 	 * @param ptype the parent type
 	 * @return true if in the database already
 	 */	
 	private boolean inDatabase(int parentID, RationaleElementType ptype)
 	{
 		boolean found = false;
 		String findQuery = "";
 		
 		if (fromXML)
 		{
 			RationaleDB db = RationaleDB.getHandle();
 			Connection conn = db.getConnection();
 			
 			//find out if this decision is already in the database
 			Statement stmt = null; 
 			ResultSet rs = null; 
 			
 			try {
 				stmt = conn.createStatement(); 
 				findQuery = "SELECT id, parent FROM decisions where name='" +
 				this.name + "'";
 				System.out.println(findQuery);
 				rs = stmt.executeQuery(findQuery); 
 				
 				if (rs.next())
 				{
 					int ourid;
 					ourid = rs.getInt("id");
 					this.id = ourid;
 					found = true;
 				}
 			}
 			catch (SQLException ex) {
 				// handle any errors 
 				RationaleDB.reportError(ex, "Decision.inDatabase", findQuery); 
 			}
 			finally { 
 				RationaleDB.releaseResources(stmt, rs);
 			}
 		}
 		//If we aren't reading it from the XML, just check the ID
 		//checking the name like above won't work because the user may 
 		//have modified the name!
 		else if (this.getID() >= 0)
 		{
 			found = true;
 		}
 		return found;
 	}
 	
 	public void setParent(int parent) {
 		this.parent = parent;
 	}
 
 
 	public void setPtype(RationaleElementType ptype) {
 		this.ptype = ptype;
 	}
 	
 //trivial pattern stuff
 	public Vector<AlternativePattern> getAlternativePatterns() {
 		return alternativePatterns;
 	}
 
 	public void setCandidatePatterns(Vector<AlternativePattern> alternativePatterns) {
 		this.alternativePatterns = alternativePatterns;
 	}
 	
 	public void addAlternativePattern(AlternativePattern alterPattern){
 		alternativePatterns.add(alterPattern);
 	}
 	/**
 	 * Save alternative patterns of type decisions to the db
 	 */
 	public void saveAlterPatterns(){
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 				
 		Statement stmt = null; 
 		ResultSet rs = null;		
 		
 		try {
 			stmt = conn.createStatement();
 //			String oldones = "SELECT patternID FROM pattern_decision WHERE decisionID = " + this.id + " AND parentType = 'decisions'";
 //			rs = stmt.executeQuery(oldones);
 //			if(rs.next()){
 //			String deleteonts = "DELETE FROM pattern_decision where decisionID = " + this.id + " AND parentType = 'decision'";
 //			stmt.execute(deleteonts);
 //			}
 			Enumeration patterns = getAlternativePatterns().elements();
 			while(patterns.hasMoreElements()){
 				String altPatternName = ((AlternativePattern)patterns.nextElement()).getName();					
 				String updateQuery = "INSERT INTO alternativepatterns (name, ptype, parent) VALUES ('" + altPatternName + "','decision'," + this.id + " )";
 				stmt.execute(updateQuery);
 			} 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}	
 	
 }
