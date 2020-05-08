 package models;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class TimeSheet
 {
 	private int _sheetId;
	private String _caseId;
 	private User _user;
 	private Client _client;
 	private ArrayList<DataEntry> _dataEntries;
 	private String _note;
 	private Date _creationDate;
 	private Date _editedDate;
 
 	public int getSheetId()
 	{ return _sheetId; }
 	public void setSheetId(int sheetId)
 	{ _sheetId = sheetId; }
	
	public String getCaseId()
	{ return _caseId; }
	public void setCaseId(String caseId)
	{ _caseId = caseId; }
 
 	public User getUser()
 	{ return _user; }
 	public void setUser(User user)
 	{ _user = user; }
 
 	public Client getClient()
 	{ return _client; }
 	public void setClient(Client client)
 	{ _client = client; }
 
 	public ArrayList<DataEntry> getDataEntries()
 	{ return _dataEntries; }
 	public void setDataEntries(ArrayList<DataEntry> dataEntry)
 	{ _dataEntries = dataEntry; }
 
 	public String getNote()
 	{ return _note; }
 	public void setNote(String note)
 	{ _note = note; }
 
 	public Date getCreationDate()
 	{ return _creationDate; }
 	public void setCreationDate(Date createdDate)
 	{ _creationDate = createdDate; }
 
 	public Date getEditedDate()
 	{ return _editedDate; }
 	public void setEditedDate(Date editedDate)
 	{ _editedDate = editedDate; }
 	// end getters and setters
 	
 
	public TimeSheet(int sheetId, String caseId, User user, Client client, String note, Date createdDate, Date editedDate)
 	{
 		_sheetId = sheetId;
         _caseId = caseId;
 		_user = user;
         _client = client;
 		_dataEntries = new ArrayList<DataEntry>();
 		_note = note;
 		_creationDate = createdDate;
 		_editedDate = editedDate;
 	}
 
 	
	public TimeSheet(String caseId, User user, Client client, String note, Date createdDate, Date editedDate)
 	{
         _caseId = caseId;
 		_user = user;
         _client = client;
 		_dataEntries = new ArrayList<DataEntry>();
 		_note = note;
 		_creationDate = createdDate;
 		_editedDate = editedDate;
 	}
 	
 		
 	// add a data entry object to list of data entries
 	public void addDataEntry(DataEntry dataEntry)
 	{
 		_dataEntries.add(dataEntry);
 	}
 	
 	// remove a data entry object from list of data entries
 	public void removeDataEntry(DataEntry dataEntry)
 	{
 		_dataEntries.remove(dataEntry);
 	}
 }
