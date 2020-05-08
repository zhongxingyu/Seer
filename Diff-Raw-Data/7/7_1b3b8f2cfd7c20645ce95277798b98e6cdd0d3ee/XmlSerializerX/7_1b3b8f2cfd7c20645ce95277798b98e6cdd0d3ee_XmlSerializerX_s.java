 package model;
 
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import synclogic.ErrorMessage;
 import synclogic.LoginRequest;
 import synclogic.RoomAvailabilityRequest;
 import synclogic.SyncListener;
 import synclogic.SynchronizationUnit;
 import synclogic.UpdateRequest;
 
 import no.ntnu.fp.model.XmlSerializer;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Elements;
 import nu.xom.ParsingException;
 
 public class XmlSerializerX extends XmlSerializer {
 	static SynchronizationUnit syncUnit;//TODO: Add to class diagram
 	
 	public static void main(String[] args) throws IOException, ParseException, ParsingException {
 		//test for users
 //		list.add(new User("Kalle", "Kanin", "lovebunny666", "b@c.af", Calendar.getInstance().getTime(), 22445588));
 //		list.add(new User("Donald", "Duck", "ducky", "d@d.ab", Calendar.getInstance().getTime(), 42445588));
 		User user = new User("Onkel", "Skrue", "richie", "1234a", "s@r.ab", Calendar.getInstance().getTime(), 22445598);
 		User user3 = new User("Onkel", "Skrue", "richie", "666", "s@r.ab", Calendar.getInstance().getTime(), 22445598);
 		String xml = toXml(user, SaveableClass.User);
 		System.out.println(xml);
 		User user2 = (User) toObject(xml);
 		System.out.println(user2.toString());
 		
 		LoginRequest lr = new LoginRequest(user.getUsername(), user.getPassword());
 		lr.setLoginAccepted(true);
 		xml = toXml(lr, SaveableClass.LoginRequest);
 		System.out.println(xml);
 		LoginRequest lr2 = (LoginRequest) toObject(xml);
 		System.out.println("Uname: " + lr2.getUsername());
 		System.out.println("Password: " + lr2.getPassword());
 		System.out.println("Accepted: " + lr2.getLoginAccepted());
 		
 		//test errormessage
 		ErrorMessage errm = new ErrorMessage(user, user3);
 		xml = toXml(errm, SaveableClass.ErrorMessage);
 		System.out.println(xml);
 		errm = (ErrorMessage) toObject(xml);
 		System.out.println("Valid:\n" + errm.getValidObject() + "Invalid:\n" +  errm.getInvalidObject());
 		
 		//test updaterequest
 		UpdateRequest updt = new UpdateRequest();
 		SaveableClass type = SaveableClass.UpdateRequest;
 		xml = toXml(updt, type);
 		System.out.println(xml);
 		updt = (UpdateRequest) toObject(xml);
 		System.out.println("Received updatereq from client: " + updt.size());
 		updt.addObject(user);
 		xml = toXml(updt, type);
 		System.out.println(xml);
 		updt = (UpdateRequest) toObject(xml);
 		System.out.println("Received updatereq response from server:" + updt.getObject(0));
 		
 		//test roomavailabilityrequest
 		System.out.println("\nStarting test of RoomAvailabilityRequest...");
 		Calendar a = Calendar.getInstance();
 		a.set(2012, 3, 12, 10, 15, 0);
 		Date adate = a.getTime();
 		a.set(Calendar.HOUR_OF_DAY, 16);
 		Date bdate = a.getTime();
 		RoomAvailabilityRequest rar = new RoomAvailabilityRequest(adate, bdate);
 		type = SaveableClass.RoomAvailabilityRequest;
 		xml = toXml(rar, type);
 		System.out.println(xml);
 		rar = (RoomAvailabilityRequest) toObject(xml);
 		System.out.println(rar.getStart());
 		System.out.println(rar.getEnd());
 		ArrayList<Room> rooms = new ArrayList<Room>();
 		rooms.add(new Room(0, "Hepp", 60));
 		rooms.add(new Room(3, "Klaustrofobi", 1));
 		rar.setAvailableRooms(rooms);
 		xml = toXml(rar, type);
 		System.out.println(xml);
 		rar = (RoomAvailabilityRequest) toObject(xml);
 		List<Room> availableRooms = rar.getAvailableRooms();
 		System.out.println("Room count: " + availableRooms.size());
 		System.out.println("Room 1 id: " + availableRooms.get(0).getId() + " and name: " + availableRooms.get(0).getName());
 		Room room = availableRooms.get(1);
 		System.out.println("Room 2 id: " + room.getId() + " and name: " + room.getName());
 		System.out.println(RoomAvailabilityRequest.dateFormat.format(rar.getStart()));
 		
 		Meeting m = new Meeting(adate, adate, bdate, "Haha", "", room, null, user, false);
 		xml = toXml(m, m.getSaveableClass());
 		System.out.println(xml);
 		Meeting m2 = (Meeting) toObject(xml);
 		room = m2.getRoom();
 		System.out.println("Date: " + m2.getDate());
 		System.out.println("Start: " + m2.getStartTime());
 		System.out.println("End: " + m2.getEndTime());
 		System.out.println("Meeting room: ID " + room.getId() + " Name " + room.getName());
 		System.out.println(m2.getOwner().getUsername());
 	}
 	
 	/**
 	 * Turn a list of objects of the same type or a single object
 	 * into a Xml string. Supported objects can be found in the
 	 * SaveableClass class.
 	 */
 	@SuppressWarnings("rawtypes")
 	public static String toXml(Object obj, SaveableClass type) {
 		if (obj instanceof List) {
 			//handle list
 			List list = (List) obj;
 			Element root = new Element("grp:" + type);
 			Iterator it = list.iterator();
 			while (it.hasNext()) {
 				root.appendChild(singleObjToElement(it.hasNext(), type));
 			}
 			return root.toXML();
 		} else {
 			//handle single object
 			return singleObjToElement(obj, type).toXML();
 		}
 	}
 
 	private static Element singleObjToElement(Object obj, SaveableClass type) {
 		switch (type) {
 			case User : {
 				return userToXmlElement((User) obj);
 			}
 			case Null : {
 				return nullToXmlElement();
 			}
 			case LoginRequest : {
 				return loginRequestToXmlElement((LoginRequest) obj);
 			}
 			case UpdateRequest : {
 				return updateRequestToXmlElement((UpdateRequest) obj);
 			}
 			case RoomAvailabilityRequest : {
 				return roomAvailabilityRequestToXmlElement((RoomAvailabilityRequest) obj);
 			}
 			case ErrorMessage : {
 				return errorMessageToXmlElement((ErrorMessage) obj);
 			}
 			case Week : {
 				return weekToXmlElement((Week) obj);
 			}
 			case Appointment : {
 				return appointmentToXmlElement((Appointment) obj);
 			}
 			case Meeting : {
 				return appointmentToXmlElement((Meeting) obj);
 			}
 			case Room : {
 				return roomToXmlElement((Room) obj);
 			}
 			case Invitation : {
 				return invitationToXmlElement((Invitation) obj);
 			}
 			case Notification : {
 				return notificationToXmlElement((Notification) obj);
 			}
 			default : {
 				throw new IllegalArgumentException("Unsupported object type (see SaveableClass, may be around the corner)!");
 			}
 		}
 	}
 	
 	
 
 
 
 	/**
 	 * Get an object or a list of objects parsed from the Xml String.
 	 * @throws IOException
 	 * @throws ParseException
 	 * @throws ParsingException
 	 */
 	public static Object toObject(String xml) throws IOException, ParseException, ParsingException {
 		Document xmldoc = stringToDocument(xml);
 		Element root = xmldoc.getRootElement();
 		String rootName = root.getLocalName();
 //		String prefix = rootName.substring(0, 3);
 //		if (prefix.charAt(3) == ':') {
 		if (rootName.startsWith("grp:")) {
 //			if (prefix.equalsIgnoreCase("grp:")) {
 				//indicates a list of objects
 //				String realRootName = rootName.substring(4);
 				Elements list = root.getChildElements();
 				ArrayList<Object> objs = new ArrayList<Object>();
 				for (int i = 0; i < list.size(); i++) {
 					Element e = list.get(i);
 					objs.add(toObject(e.toXML()));
 				}
 				return objs;
 //			} else {
 //				throw new ParsingException("Invalid prefix, only 'grp:' and no prefix is allowed");
 //			}
 		} else {
 			return assembleObject(root, rootName);
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	private static Element roomAvailabilityRequestToXmlElement(
 			RoomAvailabilityRequest obj) {
 		Element rarE = new Element("" + SaveableClass.RoomAvailabilityRequest);
 		
 		Element startE = new Element(RoomAvailabilityRequest.NAME_PROPERTY_START_DATE);
 		startE.appendChild(RoomAvailabilityRequest.dateFormat.format(obj.getStart()));
 		rarE.appendChild(startE);
 		
 		Element endE = new Element(RoomAvailabilityRequest.NAME_PROPERTY_END_DATE);
 		endE.appendChild(RoomAvailabilityRequest.dateFormat.format(obj.getEnd()));
 		rarE.appendChild(endE);
 		
 		Element listE = new Element(RoomAvailabilityRequest.NAME_PROPERTY_ROOM_LIST);
 		List list = obj.getAvailableRooms();
 		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
 			Room room = (Room) iterator.next();
 			listE.appendChild(roomToXmlElement(room));
 		}
 		rarE.appendChild(listE);
 		return rarE;
 	}
 	
 	private static Object assembleRoomAvailabilityRequest(Element rarE) throws ParseException {
 		RoomAvailabilityRequest rar = null;
 		Date start = null, end = null;
 		Element e = rarE.getFirstChildElement(RoomAvailabilityRequest.NAME_PROPERTY_START_DATE);
 		if (e != null) {
 			start = RoomAvailabilityRequest.dateFormat.parse(e.getValue());
 		}
 		
 		e = rarE.getFirstChildElement(RoomAvailabilityRequest.NAME_PROPERTY_END_DATE);
 		if (e != null) {
 			end = RoomAvailabilityRequest.dateFormat.parse(e.getValue());
 		}
 		
 		ArrayList<Room> availableRooms = new ArrayList<Room>();
 		e = rarE.getFirstChildElement(RoomAvailabilityRequest.NAME_PROPERTY_ROOM_LIST);
 		if (e != null) {
 			Elements list = e.getChildElements();
 			for (int i = 0; i < list.size(); i++) {
 				Element roomE = list.get(i);
 				if (e != null) {
 					availableRooms.add(assembleRoom(roomE));
 				}
 			}
 		}
 		
 		rar = new RoomAvailabilityRequest(start, end);
 		rar.setAvailableRooms(availableRooms);
 		return rar;
 	}
 	
 	private static Element errorMessageToXmlElement(ErrorMessage err) {
 		Element errm = new Element("" + SaveableClass.ErrorMessage);
 		
 		Element valid = new Element(ErrorMessage.NAME_PROPERTY_VALID_OBJECT);
 		SyncListener validObject = err.getValidObject();
		valid.appendChild(singleObjToElement(validObject, validObject.getSaveableClass()));
 		errm.appendChild(valid);
 		
 		Element invalid = new Element(ErrorMessage.NAME_PROPERTY_INVALID_OBJECT);
 		SyncListener invalidObject = err.getInvalidObject();
		invalid.appendChild(singleObjToElement(invalidObject, invalidObject.getSaveableClass()));
 		errm.appendChild(invalid);
 		
 		return errm;
 	}
 	
 	private static Element updateRequestToXmlElement(UpdateRequest obj) {
 		Element updr = new Element("" + SaveableClass.UpdateRequest);
 		Element  list = new Element(UpdateRequest.NAME_PROPERTY_OBJECT_LIST);
 		updr.appendChild(list);
 		List<Object> all = obj.getObjects();
 		Iterator<Object> it = all.iterator();
 		while (it.hasNext()) {
 			Object o = it.next();
 			if (o instanceof SyncListener) {
 				SyncListener sl = (SyncListener) o;
 				SaveableClass type = sl.getSaveableClass();
 				list.appendChild(singleObjToElement(sl, type));
 			} else if (o instanceof ErrorMessage) {
 				list.appendChild(singleObjToElement(o, SaveableClass.ErrorMessage));
 			}
 		}
 		return updr;
 	}
 
 	private static Object assembleObject(Element root, String rootName)
 			throws ParseException, ParsingException {
 		SaveableClass objType = SaveableClass.valueOf(rootName);
 		switch(objType) {
 			case User : {
 				return assembleUser(root);
 			}
 			case LoginRequest : {
 				return assembleLoginRequest(root);
 			}
 			case UpdateRequest : {
 				return assembleUpdateRequest(root);
 			}
 			case RoomAvailabilityRequest : {
 				return assembleRoomAvailabilityRequest(root);
 			}
 			case ErrorMessage : {
 				return assembleErrorMessage(root);
 			}
 			case Week : {
 				return assembleWeek(root);
 			}
 			case Appointment : {}
 			case Meeting : {
 				return assembleAppointment(root);
 			}
 			case Notification : {
 				return assembleNotification(root);
 			}
 			case Invitation : {
 				return assembleInvitation(root);
 			}
 			case Room : {
 				return assembleRoom(root);
 			}
 			case Null : {
 				return null;
 			}
 			default : {
 				throw new ParsingException("Unidentified object type met during parsing");
 			}
 		}
 	}
 	
 	private static Object assembleErrorMessage(Element root) throws ParseException, ParsingException {
 		SyncListener valid = null, invalid = null;
 		Element e = root.getFirstChildElement(ErrorMessage.NAME_PROPERTY_VALID_OBJECT);
 		if (e != null) {
 			Element e2 = e.getChildElements().get(0);
 			if (e2 != null) {
 				valid = (SyncListener) assembleObject(e2, e2.getLocalName());
 			}
 		}
 		e = root.getFirstChildElement(ErrorMessage.NAME_PROPERTY_INVALID_OBJECT);
 		if (e != null) {
 			Element e2 = e.getChildElements().get(0);
 			if (e2 != null) {
 				invalid = (SyncListener) assembleObject(e2, e2.getLocalName());
 			}
 		}
 		ErrorMessage msg = new ErrorMessage(valid, invalid);
 		return msg;
 	}
 
 	private static Object assembleUpdateRequest(Element root) throws ParseException, ParsingException {
 		UpdateRequest ureq = null;
 		ArrayList<Object> objs = new ArrayList<Object>();
 		Element e = root.getFirstChildElement(UpdateRequest.NAME_PROPERTY_OBJECT_LIST);
 		if (e != null) {
 			Elements list = e.getChildElements();
 			for (int i = 0; i < list.size(); i++) {
 				Element objE = list.get(i);
 				if (objE != null) {
 					objs.add(assembleObject(objE, objE.getLocalName()));
 				}
 			}
 		}
 		ureq = new UpdateRequest();
 		ureq.addAllObjects(objs);
 		return ureq;
 	}
 
 	private static Document stringToDocument(String xml) throws java.io.IOException, java.text.ParseException, nu.xom.ParsingException {
 		nu.xom.Builder parser = new nu.xom.Builder(false);
 		nu.xom.Document doc = parser.build(xml, "");
 		return doc;
 	}
 	
 	/**
 	 * Group Xml-supporting objects of the same type in a document.
 	 * @param list An arraylist with saveable objects
 	 * @param classType The correct SaveableClass enum for the object type
 	 * @return Xml document
 	 */
 	@SuppressWarnings("rawtypes")
 	public static Document buildXml(ArrayList list, SaveableClass classType) {
 		Iterator it = list.iterator();
 		Element root = new Element("" + classType);
 		switch (classType) {
 			case User : {
 				while (it.hasNext()) {
 					Element element = userToXmlElement((User) it.next());
 					root.appendChild(element);
 				}
 				break;
 			}
 			case Week : {
 				while (it.hasNext()) {
 					Element element = weekToXmlElement((Week) it.next());
 					root.appendChild(element);
 				}
 				break;
 			}
 			default : {
 				throw new IllegalArgumentException("Unhandled class type");
 			}
 		}
 		return new Document(root);
 	}
 	
 	/**
 	 * Create a login request from a Xml element
 	 */
 	private static LoginRequest assembleLoginRequest(Element lrElement) {
 		String username = null, password = null;
 		boolean accepted = false;
 
 		Element e = lrElement.getFirstChildElement(LoginRequest.NAME_PROPERTY_USERNAME);
 		if (e != null) {
 			username = e.getValue();
 		}
 		
 		e = lrElement.getFirstChildElement(LoginRequest.NAME_PROPERTY_PASSWORD);
 		if (e != null) {
 			password = e.getValue();
 		}
 		
 		e = lrElement.getFirstChildElement(LoginRequest.NAME_PROPERTY_LOGIN_ACCEPTED);
 		if (e != null) {
 			accepted = Boolean.parseBoolean(e.getValue());
 		}
 		LoginRequest lr = new LoginRequest(username, password);
 		lr.setLoginAccepted(accepted);
 		return lr;
 	}
 
 	/**
 	 * Turn a LoginRequest into a Xml element.
 	 */
 	private static Element loginRequestToXmlElement(LoginRequest logreq) {
 		Element lrElement = new Element("" + SaveableClass.LoginRequest);
 		
 		Element username = new Element(LoginRequest.NAME_PROPERTY_USERNAME);
 		username.appendChild(logreq.getUsername());
 		lrElement.appendChild(username);
 		
 		Element password = new Element(LoginRequest.NAME_PROPERTY_PASSWORD);
 		password.appendChild(logreq.getPassword());
 		lrElement.appendChild(password);
 		
 		Element accepted = new Element(LoginRequest.NAME_PROPERTY_LOGIN_ACCEPTED);
 		accepted.appendChild("" + logreq.getLoginAccepted());
 		lrElement.appendChild(accepted);
 		return lrElement;
 	}
 
 	/**
 	 * Copied from XmlSerializer and modified for fields; creates a user from the xml element
 	 * @throws ParseException
 	 */
 	private static User assembleUser(Element userElement) throws ParseException {
 		String firstname = null, surname = null, username = null, password = null, email = null;
 		Date date = null;
 		ArrayList<User> subscribesTo = new ArrayList<User>();
 		int phone = 0;
 		Element element = userElement.getFirstChildElement(User.NAME_PROPERTY_FIRSTNAME);
 		if (element != null) {
 			firstname = element.getValue();
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_SURNAME);
 		if (element != null) {
 			surname = element.getValue();
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_USERNAME);
 		if (element != null) {
 			username = element.getValue();
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_PASSWORD);
 		if (element != null) {
 			password = element.getValue();
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_EMAIL);
 		if (element != null) {
 			email = element.getValue();
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_DATE_OF_BIRTH);
 		if (element != null) {
 			DateFormat format = User.getDateFormat();
 			System.out.println("Date: " + element.getValue());
 			date = format.parse(element.getValue());
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_PHONE);
 		if (element != null) {
 			phone = Integer.parseInt(element.getValue());
 		}
 		element = userElement.getFirstChildElement(User.NAME_PROPERTY_SUBSCRIBES_TO);
 		if (element != null) {
 			Elements els = element.getChildElements();
 			for (int i = 0; i < els.size(); i++) {
 				subscribesTo.add(assembleUser(els.get(i)));
 			}
 		}
 		User u = new User(firstname, surname, username, password, email, date, phone);
 		u.setSubscribesTo(subscribesTo);
 		return u;
 	}
 	
 	/**
 	 * Turn a user object into a Xml element
 	 */
 	private static Element userToXmlElement(User user) {
 		Element element = new Element("" + SaveableClass.User);
 		
 		Element firstname = new Element(User.NAME_PROPERTY_FIRSTNAME);
 		Element surname = new Element(User.NAME_PROPERTY_SURNAME);
 		firstname.appendChild(user.getFirstname());
 		surname.appendChild(user.getSurname());
 		
 		Element usern = new Element(User.NAME_PROPERTY_USERNAME);
 		usern.appendChild(user.getUsername());
 		
 		Element pwd = new Element(User.NAME_PROPERTY_PASSWORD);
 		pwd.appendChild(user.getPassword());
 		
 		Element email = new Element(User.NAME_PROPERTY_EMAIL);
 		email.appendChild(user.getEmail());
 		
 		DateFormat dformat = User.getDateFormat();
 		Element dateOfBirth = new Element(User.NAME_PROPERTY_DATE_OF_BIRTH);
 		dateOfBirth.appendChild(dformat.format(user.getDateOfBirth()));
 
 		Element phone = new Element(User.NAME_PROPERTY_PHONE);
 		phone.appendChild("" + user.getPhone());
 		
 		Element subs = new Element(User.NAME_PROPERTY_SUBSCRIBES_TO);
 		ArrayList<User> list = user.getSubscribesTo();
 		Iterator<User> it = list.iterator();
 		while (it.hasNext()) {
 			subs.appendChild(userToXmlElement(it.next()));
 		}
 		
 		//link fields to object
 		element.appendChild(firstname);
 		element.appendChild(surname);
 		element.appendChild(usern);
 		element.appendChild(pwd);
 		element.appendChild(email);
 		element.appendChild(dateOfBirth);
 		element.appendChild(phone);
 		element.appendChild(subs);
 		return element;
 	}
 	
 	/**
 	 * Turn a week object into a Xml element
 	 */
 	private static Element weekToXmlElement(Week week) {
 		DateFormat dformat = Week.getDateFormat();
 		Element weekElement = new Element("" + SaveableClass.Week);
 		Element start = new Element(Week.NAME_PROPERTY_START_DATE);
 		start.appendChild(dformat.format(week.getStartDate()));
 		
 		Element end = new Element(Week.NAME_PROPERTY_END_DATE);
 		end.appendChild(dformat.format(week.getEndDate()));
 		
 		//add meetings/appointments
 		Element appGrouping = new Element("" + SaveableClass.Appointment);
 		ArrayList<Appointment> appointments = week.getAppointments();
 		if (!appointments.isEmpty()) {
 			Iterator<Appointment> it = appointments.iterator();
 			while (it.hasNext()) {
 				appGrouping.appendChild(appointmentToXmlElement(it.next()));
 			}
 		}
 		
 		weekElement.appendChild(start);
 		weekElement.appendChild(end);
 		weekElement.appendChild(appGrouping);
 		return weekElement;
 	}
 	
 	/**
 	 * Create a week model from the Xml element
 	 * @throws ParseException
 	 * @throws ParsingException 
 	 */
 	private static Week assembleWeek(Element weekElement) throws ParseException, ParsingException {
 		Date start = null, end = null;
 		ArrayList<Appointment> appointments = new ArrayList<Appointment>();
 		
 		DateFormat dformat = Week.getDateFormat();
 		Element e = weekElement.getFirstChildElement(Week.NAME_PROPERTY_START_DATE);
 		if (e != null) {
 			start = dformat.parse(e.getValue());
 		}
 		
 		e = weekElement.getFirstChildElement(Week.NAME_PROPERTY_END_DATE);
 		if (e != null) {
 			end = dformat.parse(e.getValue());
 		}
 				
 		e = weekElement.getFirstChildElement(Week.NAME_PROPERTY_APPOINTMENTS);
 		if (e != null) {
 			Elements apps = e.getChildElements();
 			for (int i = 0; i < e.getChildCount(); i++) {
 				appointments.add(assembleAppointment(apps.get(i)));
 			}
 		}
 		return new Week(start, end, appointments);
 	}
 	
 	/**
 	 * Turn a room into a Xml element
 	 */
 	private static Element roomToXmlElement(Room room) {
 		//TODO: Null?
 		Element roomElement = new Element("" + SaveableClass.Room);
 		
 		Element id = new Element(Room.NAME_PROPERTY_ID);
 		id.appendChild("" + room.getId());
 		
 		Element name = new Element(Room.NAME_PROPERTY_NAME);
 		name.appendChild(room.getName());
 		
 		Element capacity = new Element(Room.NAME_PROPERTY_CAPACITY);
 		capacity.appendChild("" + room.getCapacity());
 		
 		roomElement.appendChild(id);
 		roomElement.appendChild(name);
 		roomElement.appendChild(capacity);
 		
 		return roomElement;
 	}
 	
 	/**
 	 * Turn a room in Xml form into an object
 	 */
 	private static Room assembleRoom(Element roomElement) {
 		int id = 0, capacity = 0;
 		String name = null;
 		if (roomElement.getFirstChildElement("" + SaveableClass.Null) != null) {
 			return null;
 		}
 		Element e = roomElement.getFirstChildElement(Room.NAME_PROPERTY_ID);
 		if (e != null) {
 			id = Integer.parseInt(e.getValue());
 		}
 		e = roomElement.getFirstChildElement(Room.NAME_PROPERTY_NAME);
 		if (e != null) {
 			name = e.getValue();
 		}
 		e = roomElement.getFirstChildElement(Room.NAME_PROPERTY_CAPACITY);
 		if (e != null) {
 			capacity = Integer.parseInt(e.getValue());
 		}
 		return new Room(id, name, capacity);
 	}
 	
 	private static Element nullToXmlElement() {
 		Element n = new Element("" + SaveableClass.Null);
 		return n;
 	}
 	
 	/**
 	 * Turn an appointment or meeting into a Xml element
 	 */
 	private static Element appointmentToXmlElement(Appointment event) {
 		DateFormat dformat = Appointment.getDateFormat();
 		DateFormat tformat = Appointment.getTimeformat();
 		Element appElement;
 		//handle both meetings and appointments 
 		if (event instanceof Meeting) {
 			appElement = new Element("" + SaveableClass.Meeting);
 		} else {
 			appElement = new Element("" + SaveableClass.Appointment);
 		}
 		
 		Element date = new Element(Appointment.NAME_PROPERTY_DATE); 
 		date.appendChild(dformat.format(event.getDate()));
 		
 		Element start = new Element(Appointment.NAME_PROPERTY_START_TIME);
 		start.appendChild(tformat.format(event.getStartTime()));
 		
 		Element end = new Element(Appointment.NAME_PROPERTY_END_TIME);
 		end.appendChild(tformat.format(event.getEndTime()));
 		
 		Element desc = new Element(Appointment.NAME_PROPERTY_DESCRIPTION);
 		desc.appendChild(event.getDescription());
 		
 		Element loc = new Element(Appointment.NAME_PROPERTY_LOCATION);
 		loc.appendChild(event.getLocation());
 		
 		
 		Element room = new Element(Appointment.NAME_PROPERTY_ROOM);
 		Room roomObj = event.getRoom();
 		if (roomObj != null) {
 			room.appendChild(roomToXmlElement(event.getRoom()));
 		} else {
 			room.appendChild(nullToXmlElement());
 		}
 		
 		Element id = new Element(Appointment.NAME_PROPERTY_ID);
 		id.appendChild("" + event.getId());
 		
 		Element owner = new Element(Appointment.NAME_PROPERTY_OWNER);
 		owner.appendChild(userToXmlElement(event.getOwner()));
 		
 		Element delFlag = new Element(Appointment.NAME_PROPERTY_DELETED);
 		delFlag.appendChild("" + event.isDeleted());
 		
 		
 		appElement.appendChild(date);
 		appElement.appendChild(start);
 		appElement.appendChild(end);
 		appElement.appendChild(desc);
 		appElement.appendChild(loc);
 		appElement.appendChild(room);
 		appElement.appendChild(id);
 		appElement.appendChild(owner);
 		appElement.appendChild(delFlag);
 		
 		//Handle meetings
 		if (event instanceof Meeting) {
 			Meeting meeting = (Meeting) event;
 			Element participantGrouping = new Element(Meeting.NAME_PROPERTY_PARTICIPANTS);
 			ArrayList<User> users = meeting.getParticipants();
 			Iterator<User> it = users.iterator();
 			while (it.hasNext()) {
 				participantGrouping.appendChild(userToXmlElement(it.next()));
 			}
 			appElement.appendChild(participantGrouping);
 			
 			//handle existing invitations
 			Element invitationGrouping = new Element(Meeting.NAME_PROPERTY_INVITATIONS);
 			ArrayList<String> invs = meeting.getInvitations();
 			Iterator<String> it2 = invs.iterator();
 			while (it2.hasNext()) {
 				invitationGrouping.appendChild(it2.next());
 			}
 			appElement.appendChild(invitationGrouping);
 			
 			//handle users to invite
 			Element u2inv = new Element(Meeting.NAME_PROPERTY_USERS_TO_INVITE);
 			ArrayList<String> list = meeting.getUsersToInvite();
 			Iterator<String> it3 = list.iterator();
 			while (it3.hasNext()) {
 				Element head = new Element(User.NAME_PROPERTY_USERNAME);
 				head.appendChild(it3.next());
 				u2inv.appendChild(head);
 			}
 			appElement.appendChild(u2inv);
 		}
 		return appElement;
 	}
 	
 	/**
 	 * Create a meeting or appointment from their respective elements
 	 * @throws ParseException
 	 * @throws ParsingException 
 	 */
 	private static Appointment assembleAppointment(Element appElement) throws ParseException, ParsingException {
 		Date date = null, start = null, end = null;
 		String desc = null, loc = null;
 		Room room = null;
 		
 		User owner = null;
 		String id = null;
 		boolean delFlag = false;
 		
 		Element e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_DATE);
 		if (e != null) {
 			DateFormat dFormat = Appointment.getDateFormat();
 			date = dFormat.parse(e.getValue());
 		} else {
 			throw new ParsingException("Could not parse date!");
 		}
 		
 		DateFormat tFormat = Appointment.getTimeformat();
 		Calendar cal = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 		cal.setTime(date);
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_START_TIME);
 		if (e != null) {
 			start = tFormat.parse(e.getValue());
 			cal2.setTime(start);
 			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
 			cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
 			start = cal.getTime();
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_END_TIME);
 		if (e != null) {
 			end = tFormat.parse(e.getValue());
 			cal2.setTime(end);
 			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
 			cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
 			end = cal.getTime();
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_DESCRIPTION);
 		if (e != null) {
 			desc = e.getValue();
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_LOCATION);
 		if (e != null) {
 			loc = e.getValue();
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_ROOM);
 		if (e != null) {
 			System.out.println(e.getLocalName());
 			Element roomE = e.getFirstChildElement("" + SaveableClass.Room);
 			//TODO: Evaluate potensial problems with saved nulls
 			if (roomE != null) {
 				room = assembleRoom(roomE);
 			}
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_OWNER);
 		if (e != null) {
 			System.out.println(e.getLocalName());
 			Element u = e.getFirstChildElement("" + SaveableClass.User);
 			if (u != null) {
 				owner = assembleUser(u);
 			} else {
 				System.out.println("No child found");
 			}
 		} else {
 			System.out.println("Owner element not found!");
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_ID);
 		if (e != null) {
 			id = e.getValue();
 		}
 		
 		e = appElement.getFirstChildElement(Appointment.NAME_PROPERTY_DELETED);
 		if (e != null) {
 			delFlag = Boolean.parseBoolean(e.getValue());
 		}
 		
 		//Handle meetings
 		if (SaveableClass.valueOf(appElement.getLocalName()) == SaveableClass.Meeting) {
 			ArrayList<String> u2invList = new ArrayList<String>();
 			ArrayList<User> participants = new ArrayList<User>();
 			ArrayList<String> invs = new ArrayList<String>();
 			
 			e = appElement.getFirstChildElement(Meeting.NAME_PROPERTY_PARTICIPANTS);
 			if (e != null) {
 				int count = e.getChildCount();
 				Elements users = e.getChildElements();
 				for (int i = 0; i < count; i++) {
 					participants.add(assembleUser(users.get(i)));
 				}
 			}
 			
 			e = appElement.getFirstChildElement(Meeting.NAME_PROPERTY_INVITATIONS);
 			if (e != null) {
 				Elements invites = e.getChildElements();
 				int count = invites.size();
 				for (int i = 0; i < count; i++) {
 					invs.add(invites.get(i).getValue());
 				}
 			}
 			
 			e = appElement.getFirstChildElement(Meeting.NAME_PROPERTY_USERS_TO_INVITE);
 			if (e != null) {
 				Elements usernames = e.getChildElements();
 				for (int i = 0; i < usernames.size(); i++) {
 					Element head = usernames.get(i);
 					if (head != null) {
 						u2invList.add(head.getValue());
 						
 					}
 				}
 			}
 			
 			Meeting m = new Meeting(date, start, end, desc, loc, room, id, owner, delFlag);
 			m.setParticipants(participants);
 			m.setInvitations(invs);
 			m.setUsersToInvite(u2invList);
 			return m;
 		}
 		return new Appointment(date, start, end, desc, loc, room, id, owner, delFlag);
 	}
 	
 	/**
 	 * Turns an invitation into a Xml element
 	 * @param inv
 	 */
 	private static Element invitationToXmlElement(Invitation inv) {
 		Element invElement = new Element("" + SaveableClass.Invitation);
 		
 		Element status = new Element(Invitation.NAME_PROPERTY_STATUS);
 		status.appendChild("" + inv.getStatus());
 		invElement.appendChild(status);
 		
 		Element meeting = new Element(Invitation.NAME_PROPERTY_MEETING);
 		meeting.appendChild(appointmentToXmlElement(inv.getMeeting()));
 		invElement.appendChild(meeting);
 		
 		Element id = new Element(Invitation.NAME_PROPERTY_ID);
 		id.appendChild(inv.getID());
 		invElement.appendChild(id);
 		
 		return invElement;
 	}
 	
 	/**
 	 * Create an invitation object from a Xml element.
 	 * Uses the synchronization unit to retrieve the meeting
 	 * based on the meeting id.
 	 * @throws ParseException 
 	 * @throws ParsingException 
 	 */
 	private static Invitation assembleInvitation(Element invElement) throws ParseException, ParsingException {
 		InvitationStatus status = null;
 		Meeting meeting = null;
 		String id = null;
 		
 		Element e = invElement.getFirstChildElement(Invitation.NAME_PROPERTY_STATUS);
 		if (e != null) {
 			status = InvitationStatus.valueOf(e.getValue());
 		}
 		
 		e = invElement.getFirstChildElement(Invitation.NAME_PROPERTY_MEETING);
 		if (e != null) {
 			meeting = (Meeting) assembleAppointment(e);
 		}
 		
 		e = invElement.getFirstChildElement(Invitation.NAME_PROPERTY_ID);
 		if (e != null) {
 			id = e.getValue();
 		}
 		
 		return new Invitation(status, meeting, id);
 	}
 	
 	/**
 	 * Turn a Notification object into a Xml element,
 	 * the associated User who triggered the notification
 	 * and Invitation is included as well.
 	 */
 	private static Element notificationToXmlElement(Notification notif) {
 		Element notifE = new Element("" + SaveableClass.Notification);
 		
 		Element inv = new Element(Notification.NAME_PROPERTY_INVITATION);
 		inv.appendChild(invitationToXmlElement(notif.getInvitation()));
 		notifE.appendChild(inv);
 		
 		Element type = new Element(Notification.NAME_PROPERTY_TYPE);
 		type.appendChild("" + notif.getType());
 		notifE.appendChild(type);
 		
 		Element id = new Element(Notification.NAME_PROPERTY_ID);
 		id.appendChild(notif.getId());
 		notifE.appendChild(id);
 		
 		Element tBy = new Element(Notification.NAME_PROPERTY_TRIGGERED_BY);
 		tBy.appendChild(userToXmlElement(notif.getTriggeredBy()));
 		notifE.appendChild(tBy);
 		
 		return notifE;
 	}
 	
 	/**
 	 * Create a Notification from a Xml element. It contains the source user
 	 * as well as the related Invitation.
 	 * @throws ParseException
 	 * @throws ParsingException 
 	 */
 	private static Notification assembleNotification(Element notifE) throws ParseException, ParsingException {
 		Invitation inv = null;
 		NotificationType type = null;
 		String id = null;
 		User tBy = null;
 		
 		Element e = notifE.getFirstChildElement(Notification.NAME_PROPERTY_INVITATION);
 		if (e != null) {
 			inv = assembleInvitation(e);
 		}
 		
 		e = notifE.getFirstChildElement(Notification.NAME_PROPERTY_TYPE);
 		if (e != null) {
 			type = NotificationType.valueOf(e.getValue());
 		}
 		
 		e = notifE.getFirstChildElement(Notification.NAME_PROPERTY_ID);
 		if (e != null) {
 			id = e.getValue();
 		}
 		
 		e = notifE.getFirstChildElement(Notification.NAME_PROPERTY_TRIGGERED_BY);
 		if (e != null) {
 			tBy = assembleUser(e);
 		}
 		
 		Notification notif = new Notification(inv, type, id, tBy);
 		return notif;
 	}
 }
