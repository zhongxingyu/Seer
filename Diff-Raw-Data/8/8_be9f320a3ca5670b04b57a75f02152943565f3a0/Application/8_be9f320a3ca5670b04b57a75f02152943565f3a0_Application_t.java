 package controllers;
 
 import play.*;
 import play.db.jpa.GenericModel.JPAQuery;
 import play.mvc.*;
 
 import java.util.*;
 
 import com.sun.org.apache.bcel.internal.generic.Select;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	List<School> schools = School.findAll();
     	Long cschools = School.count();
     	Long cteachers = Teacher.count();
     	Long cclassrms = Classroom.count();
     	Long cstudents = StudentUser.count();
     	String noticias = "Currently the system contains " + cschools + " schools, " + cteachers + " teachers, " + cclassrms + " classrooms, and " + cstudents + " students.";
     	
         render( schools, noticias );
     }
         
  
     public static void startActivity( String tuname, String schoolname, String classname, int classyear, String activityname, String src ) {
     	String ret = "FAIL";
     	School s = School.connect(schoolname);
     	Teacher t = Teacher.connect(tuname, s);
     	if (s != null)
     	{
 	    	Classroom c = Classroom.connect(s, t, classname, classyear);
 	 
 	    	if ( c != null  &&  c.isSchool(s) )
 	    	{
 		    	Session a = new Session( c, src );
 		    	if ( a != null )
 		    	{
 			    	a.sessionMessage = activityname;
 			    	a.save();
 			    	Long aid = a.getId();
 			    	ret = String.valueOf(aid);
 		    	}
 	    	}
     	}
     	renderJSON(ret);
     }
     
         
     
     public static void nameActivity( Long aid,  String activityname )
     {
     	Session a = Session.getActivitySession(aid);
     	if ( a == null)
     	{
     		renderJSON("FAIL");
     	}
     	else
     	{
 	    	a.sessionMessage = activityname;
 	    	a.save();
 	    	renderJSON( "Renamed Session To: " + activityname );
     	}
     }
     
     public static void appendAnnotationToActivity( Long aid, String note )
     {
     	Session a = Session.getActivitySession(aid);
     	if ( a == null )
     	{
     		renderJSON("FAIL");
     	}
     	else
     	{
     		a.note += "\n" + note;
 	    	a.save();
 	    	renderJSON( "Added Annotation: '" + note + "' to this session." );
     	}
     }
     
     
     //login with username, schoolname, classname AND classyear.  COULD have an "open classroom" that allows us to return the classroom object's ID in the JSON...
     public static void login(String username, String tuname, String schoolname,  String classname, int classyear ) {
     	
 		System.err.println("Received login request for user: " + username + "\n      attempting to enter class: " + classname + " with classyear = " + classyear);
 
 		School s = School.connect(schoolname);
 		Teacher t = Teacher.connect(tuname, s);
 		
 		if (s != null && t != null)
 		{
 			Classroom croom = Classroom.connect( s, t, classname, classyear );
 			if ( croom == null )
 			{
 				renderJSON("FAILURE-CLASSROOM -- no classroom/classyear combo: '" + classname + "/" + classyear);
 			}
 			else
 			{
 				if ( croom.isSchool( s ) )
 				{
 					StudentUser theGuy = StudentUser.connect(username, croom);
 					if ( theGuy == null )
 					{
 						renderJSON("FAILURE-STUDENT -- no student '" + username + "' in class '" + classname + "'");
 					}
 					else
 					{
 						renderJSON("SUCCESS");
 					}
 				}	
 				else
 				{
 					renderJSON("School/classroom mismatch - school name: " + schoolname + " classname/year: " + classname + "/" + classyear );
 				}
 			}
 
 		}
 		else 
 		{
 			renderJSON("COULDNT FIND SCHOOL" + schoolname );
 		}
     }
     
     
     public static void logContribution(String stype, String username, Long actid, String contribid, String contribution, boolean validity ) {
     	System.err.println("contribution logged");
     	Session act = Session.getActivitySession(actid);
 
     	if (act == null )
     	{
     		renderJSON("FAILURE-no  session with id = " + actid);
     	}
     	else
     	{
     		Classroom croom = act.classroom;
     		StudentUser theGuy = StudentUser.connect(username, croom);
     		if ( theGuy == null )
     		{
     			renderJSON("FAILURE-STUDENT -- no student '" + username + "' in classroom '" + croom.toString() + "'");
     		}
     		else
     		{
     			ContributionType ct = ContributionType.POINT;
     			if ( stype.equals("EQUATION") )
     				ct = ContributionType.EQUATION;
 
     			Contribution c = new Contribution(ct, theGuy, act, contribid, contribution, validity );
     			c.save();
     			renderJSON("Logged contribution from user: " + username + ":" + croom.toString() +  "\nContents: " + contribution );
     		}
     	}
     }
     
     
     public static void getAllActivities()
     {
     	String toReturn = "ACTIVITIES:\n";
     	List<Session> acts = Session.findAll();
     	for (Session a : acts)
     	{
     		toReturn += a.toString() + "\n";
     	}
     	renderJSON( toReturn );
     }
     
     
     
     //methods for getting visualizers...
     
     public static void requestVizPage(String actid, String viz, String cnameandcyear, String tuname, String schoolname)
     {
     	String[] allfields = actid.split(";");
     	String id = allfields[ allfields.length - 1];
     	Long aid = Long.valueOf(id);
     	
     	Session s = Session.findById(aid);
     	if (s == null ){
     		flash.error("ERROR: No session was found with ID:"+id);
     		jumpPageMaker(  cnameandcyear,  tuname,  schoolname);
     	}
     	Date st = s.startTime;
     	String hostip = Http.Request.current().host;
     	String functioncall = "getAllContributionsAfter";
     	
     	if (viz.equalsIgnoreCase("Wave"))
     	{
     		wave(st, hostip, aid, cnameandcyear, tuname, schoolname, functioncall );
     	}
     	else if (viz.equalsIgnoreCase("Spiral"))
     	{
     		spiral(st, hostip, aid, cnameandcyear, tuname, schoolname, functioncall);
     	}
     	else if (viz.equalsIgnoreCase("Graphical"))
     	{
     		gensingViz(aid ); //(st, hostip, aid, cnameandcyear, tuname, schoolname, functioncall);
     	}
     	else //if (viz.equalsIgnoreCase("Spiral"))
     	{
     		renderJSON("NO VISUALIZER BY THE NAME: " + viz);
     	}
     }
     
     public static void gensingViz(Long aid)
     {
     	Session se = Session.getActivitySession(aid);
     	List<Contribution> conts = se.getContributionsAfterNumber( 0);
     	render(conts);
     }
     
     public static void gensingViz( List<Contribution> contribs )
     {
     	render(contribs);
     }
     
     public static void wave(Date starttime, String hostip, Long aid, String cnameandcyear, String tuname, String schoolname, String functioncall) //, String fullrequesturl)
     {
     	render(starttime, hostip, aid, cnameandcyear, tuname, schoolname, functioncall); //, fullrequesturl);
     }
     
     public static void spiral(Date starttime, String hostip, Long aid, String cnameandcyear, String tuname, String schoolname, String functioncall) //, String fullrequesturl)
     {
     	render(starttime, hostip, aid, cnameandcyear, tuname, schoolname, functioncall); //, fullrequesturl);
     }
     
     public static void jumpPageMaker( String cnameandcyear, String tuname, String schoolname)
     {
     	
     	if(cnameandcyear.startsWith("Select a")) {
             flash.error("ERROR: Please choose a classroom");
             classroomPicker(tuname, schoolname);
         }
     	String[] ny = cnameandcyear.split(":");
     	String cname = ny[0];
     	String cyear = ny[1];
     	//System.err.println("teach " + tuname + " and school " + schoolname);
 
     	School s = School.connect(schoolname);
     	Teacher t = Teacher.connect(tuname, s); 
     	Classroom c = Classroom.connect(s, t, cname, Integer.valueOf(cyear) );
     	if (c == null) {
     		flash.error("ERROR: No classroom found with identifier: " + cnameandcyear);
     		classroomPicker(tuname, schoolname);
     	}
     	List<Session> acts = c.sessions;
     	List<String>actids = new ArrayList<String>();
     	for (Session se : acts )
     		actids.add(se.toAltString() );
     	List<String>visualizers = Visualizers.getNames();
     	render(actids, acts, visualizers, cnameandcyear, tuname, schoolname);
     }
     
     public static void classroomPicker( String tuname, String schoolname )
     {
     	if(tuname.startsWith("Select a")) {
             flash.error("ERROR: Please choose a teacher");
             teacherPicker(schoolname);
         }
     	School s = School.connect(schoolname);
     	Teacher t = Teacher.connect(tuname, s);
     	if ( t == null ) {
     		flash.error("ERROR: No teacher was found with user name " + tuname + " in school " + schoolname + ".");
             teacherPicker(schoolname);
     	}
     	List<Classroom> crooms = t.classrooms;
     	render(crooms, tuname, schoolname);
     }
     
     public static void teacherPicker( String schoolname )
     {
     	if(schoolname.startsWith("Select a")) {
             flash.error("ERROR: Please choose a school");
             schoolPicker();
         }
     	School s = School.connect(schoolname);
     	List<Teacher> teachers = s.teachers;
     	render(teachers, schoolname);
     }
     
     public static void schoolPicker( )
     {	
     	List<School> schools = School.all().fetch();
     	render(schools);
     }
     
     
     public static void addAnnotationToContribution( Long sessionId, int sequence, String annot )
     {
     	String result = "Fail";
     	Session se = Session.getActivitySession(sessionId);
     	if (se == null ) { renderJSON(result); }
     	Contribution c = se.getContributionWithSequence( sequence );
     	if (c == null ) { renderJSON(result); }
     	Annotation a = new Annotation( c, annot );
     	c.annotations.add( a );
     	a.save();
     	c.save();
     	result = "Success";
     	renderJSON(result);
     }
     
     
     public static void setAnnotationsForContribution( Long sessionId, int sequence, String annotations )
     {
     	System.err.println("STARTED -- going to add/update annotations for contrib sequence number " + sequence);
     	String result = "Fail\n";
     	
     	Session se = Session.getActivitySession(sessionId);
     	if (se == null ) { renderJSON(result); }
     	Contribution c = se.getContributionWithSequence( sequence );
     	if (c == null ) { renderJSON(result); }
  
     	System.err.println( "contribution number " + sequence + " has body " + c.body);
     	ArrayList<Annotation>annotationList = new ArrayList<Annotation>();
 
     	if ( annotations != null &&  annotations.length() > 0 )
     	{
 	    	String[] annots = annotations.split("\\|");
 	    	
 	    	for ( int i = 0; i<annots.length; i++ )
 	    	{
 	    		String an = annots[i];
 	    		if ( an != null && an.length() >0)
 	    		{
 		    		Annotation a = new Annotation(c, an);
 		    		annotationList.add( a );
 	    		}
 	    	}
     	}
     	
     	List<Annotation>oldones = c.annotations;
     	for ( Annotation todelete : oldones )
     	{
     		todelete.delete();
     	}
     	c.annotations.clear();
     	c.save();
     	for (Annotation toadd : annotationList )
     	{
         	c.annotations.add( toadd );
     	}
     	c.save();
     	for ( Annotation annot : annotationList )
     	{
     		annot.save();
     	}
     	c.save();
     	System.err.println("about to send response....");
     	result = "Success\n\n";
     	renderJSON(result);
     }
     
     
     public static void saveWaveState( String selcodes, String name, Long sid, String selstring, String comments, int whatshowing )
     {
    	System.err.println("whatshowing has value " + whatshowing);
    	WaveSaveState state = new WaveSaveState( selcodes, name, sid, selstring, whatshowing );  
     	state.setComments(comments);
     	state.save();
     	Long theid = state.id;
     	renderJSON( "Success: state id = " + theid );
     }
     
     public static void updateWaveState( Long id, String selcodes, String name, Long sid, String selstring, String comments, int whatshowing )
     {
     	WaveSaveState thestate = WaveSaveState.findWaveSaveState(id);
     	if ( name == null  || name.length() == 0 ) { renderJSON("Failed: name cannot be blank."); }
     	if (thestate == null ) { renderJSON( "Failed to find state with id=" + id ); }
     	thestate.selectedCodes = selcodes;
     	thestate.name = name;
     	thestate.currentSelectionString = selstring;
     	thestate.comments = comments;
     	thestate.whatShowing = whatshowing;
     	thestate.save();
     	renderJSON("Success");
     }
     
     public static void getWaveStates()
     {
     	String retn = "";
     	List<WaveSaveState> states = WaveSaveState.findAll();
     	for ( WaveSaveState state : states )
     	{
     		retn += state.toString() + "\n";
     	}
     	renderJSON( retn );
     }
     
     public static void retrieveWaveState( Long id )
     {
     	String toreturn = "";
     	WaveSaveState thestate = WaveSaveState.findWaveSaveState(id);
     	
     	Long TheSessionId = thestate.sessionId;
     	Session ourSession = Session.findById(TheSessionId);
     	if (ourSession == null )
     		renderJSON( " SESSION ID, " + TheSessionId.toString() + ", IS INVALID ");
     	
     	toreturn += ourSession.classroom.school.schoolName + "\t";
     	toreturn += ourSession.classroom.teacher.username + "\t";
     	toreturn += ourSession.classroom.classname +":"+ ourSession.classroom.startYear + "\t";
     	toreturn += ourSession.startTime.toString() + "\t";
     	
     	/*
     	 * school
     	 * teacher
     	 * cnameandcyear
     	 * starttme
     	 */
     	
     	toreturn += thestate.selectedCodes + "\t";
     	toreturn += thestate.name + "\t";
     	toreturn += thestate.sessionId.toString() + "\t";
     	toreturn += thestate.currentSelectionString + "\t";
    	toreturn += thestate.comments + "\t";
    	toreturn += thestate.whatShowing;
    	System.err.println(toreturn);
     	renderJSON( toreturn );
     }
     
     
     public static void setCommentsForWaveState( Long id, String comments )
     {
     	WaveSaveState thestate = WaveSaveState.findWaveSaveState(id);
     	thestate.setComments( comments );
     	thestate.save();
     }
     
     private boolean isValidCategoryDescriptor( String category, String descriptor )
     {
     	return true;
     	//TODO: do this check.
     }
     
     public static void setCodingsForContribution( Long sessionId, int sequence, String codings )
     {
     	String result = "Fail";
     	Session se = Session.getActivitySession(sessionId);
     	if (se == null ) { renderJSON(result); }
     	Contribution c = se.getContributionWithSequence( sequence );
     	if (c == null ) { renderJSON(result); }
     	
     	
     	ArrayList<Coding>codingList = new ArrayList<Coding>();
 
     	if (codings != null && codings.length() > 0) 
     	{ 
 	    	//this splits out each code:descriptor pair
 	    	String[] codepairs = codings.split("\\|");
 	
 	    	for ( int i = 0; i<codepairs.length; i++ )
 	    	{
 	    		String codingpair = codepairs[i];
 	    		String[] parts = codingpair.split("\\:");
 	    		if ( parts.length != 2 ) { renderJSON("FAIL error in parsing: " + codingpair); }
 	    		else
 	    		{
 		    		String category = parts[0];
 		    		String descriptor = parts[1];
 		    		Coding cod = new Coding(c, category, descriptor);
 		    		codingList.add(cod);
 	    		}
 	    	}
     	}
     	List<Coding>oldones = c.codings;
     	for ( Coding todelete : oldones )
     	{
     		todelete.delete();
     	}
     	c.codings.clear();
     	c.save();
     	for ( Coding toadd : codingList )
     	{
         	c.codings.add( toadd );
     	}
     	c.save();
     	for ( Coding co : codingList )
     	{
     		co.save();
     	}
     	c.save();
     	System.err.println("about to send response....");
     	result = "Success\n\n";
     	renderJSON(result);
     }
     
     
 
     
     public static void addCodingToContribution( Long sessionId, int sequence, String descriptor, String category )
     {
     	//TODO: stub
     	String result = "Fail";
     	Session se = Session.getActivitySession(sessionId);
     	if (se == null ) { renderJSON(result); }
     	Contribution c = se.getContributionWithSequence( sequence );
     	if (c == null ) { renderJSON(result); }
     	JPAQuery q = CodeCategory.all();
     	List<Object>cats = q.fetch();
     	boolean badcat = true;
     	boolean baddes = true;
     	for ( Object o : cats )
     	{
     		if (o instanceof CodeCategory)
     		{
     			CodeCategory cca = (CodeCategory)o;
     			if ( category.equals( cca.category ) )
     			{
     				badcat = false;
     				for (CodeDescriptor d : cca.descriptors )
     				{
     					if (descriptor.equals(d.descri) )
     						baddes = false;
     				}
     				
     			}
     		}
     	}
     	if (badcat) { renderJSON("NON-EXISTENT CATEGORY: "+category); }
     	if (baddes) { renderJSON("NON-EXISTENT DESCRIPTOR: "+descriptor); }
     	
     	//CodeCategory cc = CodeCategory.findByName( category );
     	//if (cc == null ) { renderJSON("NON-EXISTENT CATEGORY: "+category); }
     	//CodeDescriptor cd = CodeDescriptor.connect(cc, descriptor);
     	//CodeDescriptor cd = CodeDescriptor.findByCategoryAndName( cc, descriptor );
     	//if (cd == null ) { renderJSON("NON-EXISTENT DESCRIPTOR: "+descriptor); }
     	//System.err.println( cc.toString() );
     	//System.err.println( cd.toString() );
     	
     	//WORKING FROM BEFORE
     	Coding co = new Coding( c, category, descriptor );
     	//System.err.println( co.toString() );
     	c.codings.add( co );
     	co.save();
     	c.save();
     	result = "Success";
     	renderJSON(result);
     }
     
     public static void getCodeDictionary()
     {
     	String alist = "";
     	JPAQuery q = CodeCategory.all();
     	List<Object>cats = q.fetch();
     	if (cats.size() == 0) { alist = "NO CODING DICTIONARY ENTRIES"; }
     	for ( Object o : cats )
     	{
     		if (o instanceof CodeCategory)
     		{
     			CodeCategory cc = (CodeCategory)o;
     			alist += "CATEGORY: " + cc.category + "\n";
     			List<CodeDescriptor> descs = cc.descriptors;
     			for ( CodeDescriptor d : descs )
     			{
     				alist += "DESCRIPTOR: " + d.descri + "\n";
     			}
     			alist +=  "\n";
     		}
     		else
     		{
     			alist += "OOPS: got " + o.toString() + "\n";
     		}
     	}
     	
     	renderJSON(alist);
     }
 
     
   //test methods...
     public static void getAllTeachers( String  schoolname )
     {
     	School s = School.connect(schoolname);
     	
     	List<Teacher> teachers = s.teachers;
     	String reply = "Teachers:\n";
     	if (teachers.isEmpty())
     		reply = "NO TEACHERS";
     	for ( Teacher t: teachers)
     	{
     		reply += t.toString() + "\n";
     	}
     	renderJSON( reply );
     }
     
     public static void getAllSchools()
     {
     	List<School> schools = School.find("select s from School s").fetch();
     	String reply = "Schools:\n";
     	if (schools.isEmpty())
     		reply = "NO Schools";
     	for ( School s: schools)
     	{
     		reply += s.toString() + "\n";
     	}
     	renderJSON( reply );
     }
     
     public static void getAllStudents()
     {
     	List<StudentUser> ss = StudentUser.find("select s from StudentUser s").fetch();
     	String reply = "Students:\n";
     	if (ss.isEmpty())
     		reply = "NO Students";
     	for ( StudentUser s: ss)
     	{
     		reply += s.toString() + "\n";
     	}
     	renderJSON( reply );
     }
     
     //with null name argument, this one returns a list
     public static void getAllClassroomsMatching( String cname  )
     {
     	String reply = "NO Matching Classrooms Found";
     	List<Classroom> cs = null;
     	if (cname == null)
     	{
     		cs = Classroom.find( "select c from Classroom c" ).fetch();
     	}
     	else
     	{
     		cs = Classroom.find("select c from Classroom c where c.classname like '"+cname+"' order by c.startYear").fetch();
     	}
     	
     	if (cs != null && cs.size() > 0)
     	{
     		reply = "Matching Classrooms:\n";
     		for ( Classroom c : cs )
     		{
     		
     			reply += "School=" +c.school +"; Teacher=" +c.teacher +"; Name=" + c.classname + "; Starting Year=" + c.startYear + "\n";
     		}
     	}
     	renderJSON( reply );
     }
     
     //with null name argument this one puts in "firstperiodmath" -- for testing.
     public static void getAllClassroomsDummy( String cname )
     {
     	if ( cname == null )
     	{
     		cname = "FirstPeriodMath";
     	}
     	String reply = "NO Matching Classrooms Found";
     	List<Classroom> cs = Classroom.find("select c from Classroom c where c.classname like '"+cname+"' order by c.startYear").fetch();
     	if ( cs != null && cs.size() > 0)
     	{
     		reply = "Matching Classroom(s):\n";
 	    	for ( Classroom c : cs )
 			{
 					reply += "Name=" + c.classname + "; Teacher=" + c.teacher + "; Starting Year=" + c.startYear + "\n";
 			}
     	}
     	
     	renderJSON( reply );
     }
     
     public static void getAllStudentsInClassroom( String cname, int year )
     {
     	String reply = "NO Matching Classrooms Found";
     	List<Classroom> cs = Classroom.find("select c from Classroom c where c.classname like '"+cname+"'").fetch();
     	for ( Classroom c : cs )
 		{
 			if ( c.startYear == year )
 			{
 				int i = 0;
 				reply = "Classroom Found:  Name=" + c.classname + "; Year=" + c.startYear + "\n";
 				for ( StudentUser s : c.students)
 				{
 					i++;
 					reply += "#" + i + "Username: " + s.username + "\n";
 				}
 			}
 		}
     	renderJSON( reply );
     }
     
     
     /*stupid method -- need to specify classsroom , then get current session, etc.*/
     public static void getAllContributions( )
     {
     	List<Contribution> allcontribs = Contribution.find("select c from Contribution c order by c.timestamp").fetch();
     	String reply = "Contributions:\n";
     	if (allcontribs.isEmpty())
     		reply = "NO CONTRIBUTIONS";
     	for ( Contribution c : allcontribs )
     	{
     		//reply += c.toString() + "\n";
     		reply += c.toTSVLine() + "\n";
     	}
     	renderJSON(reply);
     }
     
 
     public static void getContributionWithSequenceNumber( Long aid, int ind )
     {
     	Session s = Session.getActivitySession(aid);
     	Contribution c = s.getContributionWithSequence(ind);
     	renderJSON( c.toTSVLine() );
     }
     
     public static void getContributionWithSequenceNumberVerbose( Long aid, int ind )
     {
     	Session s = Session.getActivitySession(aid);
     	Contribution c = s.getContributionWithSequence(ind);
     	renderJSON( c.toTSVLineVerbose() );
     }
     
     public static void getContributionsAfterSequenceNumber( Long aid, int ind )
     {
     	String reply = "OOPS -- problem in looking up Activtity with id:" + aid;
 
 	    Session a = Session.getActivitySession(aid);
 	    if ( a != null )
 	    {
 	    	List<Contribution> afteri = a.getContributionsAfterNumber(ind);
 	    	reply = "Contributions:\n";
 	    	if (afteri.isEmpty())
 	    		reply = "NO CONTRIBUTIONS MATCHING CONDITION";
 	    	for ( Contribution c : afteri )
 	    	{
 	    		reply += c.toTSVLine() + "\n";
 	    	}
     	}
 	    //debugging   System.err.println("about to send " + reply);
     	renderJSON(reply);
     }
     
     
     private static String getFakeCodes( int i, int j  )
     {
     	String[] codes = { "ASMD", "BN", "R1", "MT|BN", "R1|BN", "R1", "VASM|ASMD", "M1|A0", "MT","VASM", "A0", "M1" };
     	String[] annotations = { "Cool idea","Write a paper|Wow that's cool|Hi mom", "I don't know|What does this mean?" } ;
     	int ind = i % codes.length;
     	int dex = j % annotations.length;
     	return "\t"+codes[ ind ]+"\t"+annotations[dex];
     }
     
     
     public static void getContributionsAfterSequenceNumberVerbose( Long aid, int ind )
     {
     	String reply = "OOPS -- problem in looking up Activtity with id:" + aid;
 
 	    Session a = Session.getActivitySession(aid);
 	    if ( a != null )
 	    {
 	    	List<Contribution> afteri = a.getContributionsAfterNumber(ind);
 	    	reply = "Contributions:\n";
 	    	if (afteri.isEmpty())
 	    		reply = "NO CONTRIBUTIONS MATCHING CONDITION";
 	    	for ( Contribution c : afteri )
 	    	{
 	    		reply += c.toTSVLineVerbose() + "\n";  
 	    	}
     	}
 	    //debugging   System.err.println("about to send " + reply.replaceAll("\n", "<<\n").replaceAll("\t", "!"));
     	renderJSON(reply);
     }
     
     public static void getValidContributionsAfterSequenceNumberVerbose( Long aid, int ind )
     {
     	String header =  "Contributions:\n";
     	String reply = "OOPS -- problem in looking up Activtity with id:" + aid;
 	    Session a = Session.getActivitySession(aid);
 	    if ( a != null )
 	    {
 	    	List<Contribution> afteri = a.getContributionsAfterNumber(ind);
 	    	reply = header;
 	    	if (afteri.isEmpty())
 	    		reply = "NO CONTRIBUTIONS MATCHING CONDITION";
 	    	for ( Contribution c : afteri )
 	    	{
 	    		if ( c.isValid )
 	    			reply += c.toTSVLineVerbose() + "\n";  
 	    	}
 	    	if ( reply.equals(header) )
 	    		renderJSON("NO VALID CONTRIBUTIONS MATCHING CONDITION");
     	}
 	    //debugging   System.err.println("about to send " + reply.replaceAll("\n", "<<\n").replaceAll("\t", "!"));
     	renderJSON(reply);
     }
     
     
     public static void getInvalidContributionsAfterSequenceNumberVerbose( Long aid, int ind )
     {
     	String header =  "Contributions:\n";
     	String reply = "OOPS -- problem in looking up Activtity with id:" + aid;
 	    Session a = Session.getActivitySession(aid);
 	    if ( a != null )
 	    {
 	    	List<Contribution> afteri = a.getContributionsAfterNumber(ind);
 	    	reply = header;
 	    	if (afteri.isEmpty())
 	    		reply = "NO CONTRIBUTIONS MATCHING CONDITION";
 	    	for ( Contribution c : afteri )
 	    	{
 	    		if ( ! c.isValid )
 	    			reply += c.toTSVLineVerbose() + "\n";  
 	    	}
 	    	if ( reply.equals(header) )
 	    		renderJSON("NO INVALID CONTRIBUTIONS MATCHING CONDITION");
     	}
     	renderJSON(reply);
     }
     
     
     
     public static void execute( String sql )
     {
     	try
     	{
     		List<Object> results = Utilities.executeQuery(sql);
     		String retn = "";
     		for (Object result : results )
     			retn += result.toString();
     		renderJSON( retn );
     	}
     	catch (Exception e )
     	{
     		String reply =  "ERROR in executing query: " + sql + "\n" + e.getMessage();
     		renderJSON( reply);
     	}
     }
     
     
 
 }
