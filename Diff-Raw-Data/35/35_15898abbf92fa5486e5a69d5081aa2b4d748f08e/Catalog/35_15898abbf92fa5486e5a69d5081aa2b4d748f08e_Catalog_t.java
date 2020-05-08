 /*
    Modified by Peter Carroll <kedge@se77en.com>
 
 */
 
 /*
 Copyright (c) 1998, 1999 Wabasoft  All rights reserved.
 
 This software is furnished under a license and may be used only in accordance
 with the terms of that license. This software and documentation, and its
 copyrights are owned by Wabasoft and are protected by copyright law.
 
 THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
 OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
 INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
 SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.
 
 WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
 MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
 ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
 YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
 */
 
 package waba.io;
 
 /*import java.util.Hashtable;
 import java.util.Vector;*/
 import palmos.*;
 
 /**
  * Catalog is a collection of records commonly referred to as a database
  * on small devices.
  * <p>
  * Here is an example showing data being read from records in a catalog:
  *
  * <pre>
  * Catalog c = new Catalog("MyCatalog", Catalog.READ_ONLY);
  * if (!c.isOpen())
  *   return;
  * int count = c.getRecordCount();
  * byte b[] = new byte[10];
  * for (int i = 0; i < count; i++)
  *   {   
  *   c.setRecord(i);
  *   c.readBytes(b, 0, 10);
  *   ...
  *   }
  * c.close();
  * </pre>
  */
 
 public class Catalog extends Stream
 {
 /** Read-only open mode. */
 public static final int READ_ONLY  = 1;
 /** Write-only open mode. */
 public static final int WRITE_ONLY = 2;
 /** Read-write open mode. */
 public static final int READ_WRITE = 3; // READ | WRITE
 /** Create open mode. Used to create a database if one does not exist. */
 public static final int CREATE = 4;
 
 private boolean _isOpen;  //This may get deleted since checking if iDmRef==0 serves the same purpose
 private String _name;
 private int _mode;
 //public static Hashtable _dbHash = new Hashtable();
 //private Vector _records = new Vector();
 private int _recordPos=-1;
 //private int _cursor;
 
 //The following variables were added by Peter Carroll
 private int iDmRef=0;
 private int iRecH=0;
 private int iRecOffset=0;
 private int iRecPtr=0;
 private int iRecSize=0;
 private boolean flgCurRecModified=false;
 
 /**
  * Opens a catalog with the given name and mode. If mode is CREATE, the 
  * catalog will be created if it does not exist.
  * <p>
  * For PalmOS: A PalmOS creator id and type can be specified by appending
  * a 4 character creator id and 4 character type to the name seperated
  * by periods. For example:
  * <pre>
  * Catalog c = new Catalog("MyCatalog.CRTR.TYPE", Catalog.CREATE);
  * </pre>
  * Will create a PalmOS database with the name "MyCatalog", creator id
  * of "CRTR" and type of "TYPE".
  * <p>
  * If no creator id and type is specified, the creator id will default
  * to the creator id of current waba program and the type will default
  * to "DATA".
  * <p>
  * Under PalmOS, the name of the catalog must be 31 characters or less,
  * not including the creator id and type. Windows CE supports a 32
  * character catalog name but to maintain compatibility with PalmOS,
  * you should use 31 characters maximum for the name of the catalog.
  * @param name catalog name
  * @param mode one of READ_ONLY, WRITE_ONLY, READ_WRITE or CREATE
  */
 
 
 public Catalog(String name, int mode)
 {
    //This function is mostly taken from the wabavm source
    int iType=0x44415441; //="DATA"
    //I am not sure of an easy way to get the current program's creator id, so this will default to "WJmp" for now.
    int iCreator=0x574A6D70; //="WJmp"
 
 	_name = name;
 	_mode = mode;
 
    if(name.length()>10 && name.charAt(name.length()-10)=='.' && name.charAt(name.length()-5)=='.'){
       iCreator=((int)name.charAt(name.length()-9))*0x1000000;
       iCreator+=((int)name.charAt(name.length()-8))*0x10000;
       iCreator+=((int)name.charAt(name.length()-7))*0x100;
       iCreator+=(int)name.charAt(name.length()-6);
       iType=((int)name.charAt(name.length()-4))*0x1000000;
       iType+=((int)name.charAt(name.length()-3))*0x10000;
       iType+=((int)name.charAt(name.length()-2))*0x100;
       iType+=(int)name.charAt(name.length()-1);
       _name=name.substring(0,name.length()-10);
    }
 
    iDmRef=openDB(_name,iCreator,iType);
    if(iDmRef==0){
       _isOpen=false;
    }
    else{
       _isOpen=true;
    }
 
    if(iDmRef==0 && mode==CREATE){
       if(Palm.DmCreateDatabase(0,_name,iCreator,iType,false)==0){
          iDmRef=openDB(_name,iCreator,iType);
          if(iDmRef!=0){
             IntHolder iDbId=new IntHolder(0);
             ShortHolder iCardNo=new ShortHolder((short)0);
             ShortHolder iAttr=new ShortHolder((short)0);
             StringBuffer sNameBuffer=new StringBuffer(32);
 
             //set the backup bit on the database
             Palm.DmOpenDatabaseInfo(iDmRef,iDbId,null,null,iCardNo,null);
             //Palm.DmDatabaseInfo((int)iCardNo.value,iDbId.value,sNameBuffer,iAttr,null,null,null,null,null,null,null,null,null);
             iAttr=new ShortHolder((short)(8)); //8=dmHdrAttrBackup
             Palm.DmSetDatabaseInfo((int)iCardNo.value,iDbId.value,null,iAttr,null,null,null,null,null,null,null,null,null);
 
             _isOpen=true;
          }
       }
    }
 }
 
     String errString = null;
 
     public String getFunct()
     {
 	if(errString != null)
 	    return errString;
 
 	return "";
     }
 
     public int getError()
     {
 	return Palm.DmGetLastErr();
     }
 
 
 //this function added by Peter Carroll
 private int openDB(String sName, int iCreator, int iType)
 {
 //   String sDebug="0"; //debug
    try{
       //most of this code is derived from the wabavm source code
       DmSearchState oState=new DmSearchState();
       boolean flgFirst=true;
       ShortHolder iCardNo=new ShortHolder((short)0);
       IntHolder iDbId=new IntHolder(0);
       String sDbName="";
 
       while(true){
 //         sDebug="1"; //debug
          int iErr=Palm.DmGetNextDatabaseByTypeCreator(flgFirst, oState, iType, iCreator, false, iCardNo, iDbId);
          if(iErr==0x207){ //dmErrCantFind
             return 0;
          }
    //      char acName[]=new char[32];
 //         sDebug="2"; //debug
          StringBuffer sNewName=new StringBuffer(32);
          ShortHolder iCrap=new ShortHolder((short)0); //this is so javac knows which DmDatabaseInfo is being used.
          if(Palm.DmDatabaseInfo((int)iCardNo.value, iDbId.value, sNewName, iCrap, null, null, null, null, null, null, null, null, null)==0){
          	//sDbName=String.valueOf(acName);
 //            sDebug="3"; //debug
          	sDbName=sNewName.toString();
 
 //            sDebug="4"; //debug
    	  	   int iPos=sDbName.indexOf(0);
 //            sDebug="5"; //debug
      		   if(iPos>=0){
      	   	   sDbName=sDbName.substring(0,iPos);
      	   	}      	
 
 //            sDebug="6"; //debug
             if(sName.equals(sDbName)){
                return Palm.DmOpenDatabase((int)iCardNo.value, iDbId.value, 3); //3=dmModeReadWrite
             }
          }
          flgFirst=false;
       }
    }catch(Exception e){
       //debug
 //      Palm.WinDrawChars(sDebug, sDebug.length(), 0, 0);
       //waba.sys.Vm.sleep(2000);
       return 0;
    }
 }
 
 /**
  * Adds a record to the end of the catalog. If this operation is successful,
  * the position of the new record is returned and the current position is
  * set to the new record. If it is unsuccessful the current position is
  * unset and -1 is returned.
  * @param size the size in bytes of the record to add
  */
 
 public int addRecord(int size)
 {
    //this function derived from wabavm source
 	if (!_isOpen){
 		return -1;
 	}
 
 	recClose();
 	
 	_recordPos = Palm.DmNumRecords(iDmRef);
 	ShortHolder iPos=new ShortHolder((short)_recordPos);
 	iRecH=Palm.DmNewRecord(iDmRef, iPos, size);
 	if(iRecH==0){
 	   _recordPos=-1;
 	   return -1;
 	}
 	
 	_recordPos=(int)iPos.value;
 	
 	iRecSize=size;
 	iRecOffset=0;
 	
 	return _recordPos;
 }
 
 /**
  * Resizes a record. This method changes the size (in bytes) of the current record.
  * The contents of the existing record are preserved if the new size is larger
  * than the existing size. If the new size is less than the existing size, the
  * contents of the record are also preserved but truncated to the new size.
  * Returns true if the operation is successful and false otherwise.
  * @param size the new size of the record
  */
 
 public boolean resizeRecord(int size)
 {
 	if (_recordPos == -1){
 	    errString = "-1 _recordPos";
 		return false;
 	}
 	if(size<0){
 	    errString = "negative size";
 	   return false;
 	}
 	if(iDmRef==0){
 	    errString = "iDmRef";
 	   return false;
 	}
    
 	int iRecPos=_recordPos;
 	recClose();
 
 	iRecH=Palm.DmGetRecord(iDmRef, iRecPos);
 	if(iRecH==0){
 	    errString = "DmGetRecord";
 	   return false;
 	}
 
 	iRecH=Palm.DmResizeRecord(iDmRef, iRecPos, size);
 	if(iRecH==0){
 	    errString = "DmResizeRecord";
 	   return false;
 	}
 
 	_recordPos = iRecPos;
 	iRecSize=Palm.MemHandleSize(iRecH);
 
 	flgCurRecModified=true;
 
 	/*		
 	byte [] buf = new byte [Palm.MemHandleSize(iRecH)];	
 	int count = size;
 	if(size > Palm.MemHandleSize(iRecH)){
 	    count = Palm.MemHandleSize(iRecH);
 	}
 
 	readBytes(buf, 0, count);
 	
 	Palm.DmReleaseRecord(iDmRef, iRecPos, false);
 
 	if(Palm.DmRemoveRecord(iDmRef, iRecPos) !=0){
 	    errString = "DmRemoveRecord";
 	   return false;
 	}
 
 	ShortHolder iPos=new ShortHolder((short)iRecPos);
 	iRecH=Palm.DmNewRecord(iDmRef, iPos, size);
 	if(iRecH==0){
 	    errString = "DmNewRecord";
 	   _recordPos=-1;
 	   return false;
 	}
 
 	_recordPos = iRecPos;
 	iRecSize=Palm.MemHandleSize(iRecH);
 
 	writeBytes(buf, 0, count);
 	*/
 
 	flgCurRecModified=true;
 	
 	return true;
 }
 
 private void recClose()
 {
    if(_recordPos==-1){
       return;
    }
    
    if(iRecPtr != 0 && iRecH != 0){
        Palm.MemHandleUnlock(iRecH);
    }
    if(iRecH != 0){
        Palm.DmReleaseRecord(iDmRef, _recordPos, flgCurRecModified);
    }
 
    _recordPos=-1;
    flgCurRecModified=false;
    iRecSize=0;
    iRecH=0;
    iRecPtr=0;
 }
 
 /**
  * Closes the catalog. Returns true if the operation is successful and false
  * otherwise.
  */
 
 public boolean close()
 {
 	if (!_isOpen){
 		return false;
    }
    if(iDmRef==0){
       return false;
    }
 
    recClose();
    Palm.DmCloseDatabase(iDmRef);
 
 	_isOpen = false;
 	_recordPos = -1;
 	iDmRef=0;
 	
 	return true;
 }
 
 /**
  * Deletes the catalog. Returns true if the operation is successful and false
  * otherwise.
  */
 
 public boolean delete()
 {
 	if (!_isOpen){
 		return false;
 	}
 	if(iDmRef==0){
 	   return false;
 	}
 	
 	IntHolder iLocalId=new IntHolder(0);
 	ShortHolder iCardNo=new ShortHolder((short)0);
 	Palm.DmOpenDatabaseInfo(iDmRef, iLocalId, null, null, iCardNo, null);
 	this.close();
 	
 	if(Palm.DmDeleteDatabase((int)iCardNo.value, iLocalId.value)!=0){
 	   return false;
 	}
 	
 	return true;
 }
 
 /**
  * Returns the complete list of existing catalogs. If no catalogs exist, this
  * method returns null.
  */
 
 public static String []listCatalogs()
 {
    //This method is done a little differently than the WabaVM.
    //WabaVM will run 2 loops, the first counts the databases and
    //the second populates the String array.
    //This method also does two loops, the first loop counts the databases
    //and appends them to a delimited string. The second loop
    //breaks the delimited string into a String array.
    //This is about 30% quicker than the WabaVM way to do it.
    //There is a lot of room for optimization, this is a slow function.
 
    DmSearchState oState=new DmSearchState();
    boolean flgFirst=true;
    ShortHolder iCardNo=new ShortHolder((short)0);
    IntHolder iDbId=new IntHolder(0);
    int n=0;
 
   	String sList="";
   	  	  	
   	n=0;
   	flgFirst=true;
   	while(Palm.DmGetNextDatabaseByTypeCreator(flgFirst, oState, 0, 0, false, iCardNo, iDbId)==0){
   	
   	   flgFirst=false;
   	
   	   String sName="";
   	   IntHolder iType=new IntHolder(0);
   	   IntHolder iCreator=new IntHolder(0);
    	
 
   	   int iCN=iCardNo.value;
   	   int iDb=iDbId.value;
   	      	
 	   //char[] acName=new char[32];
 	   StringBuffer sNewName=new StringBuffer(32);
 	   Palm.DmDatabaseInfo(iCN, iDb, sNewName, null, null, null, null, null, null, null, null, iType, iCreator);
 
 //  	   sName=String.valueOf(acName);
       sName=sNewName.toString();
   	   int iPos=sName.indexOf(0);
   	   if(iPos>=0){
   	      sName=sName.substring(0,iPos);
   	   }        	
   	
   	   sName+="." + intToString(iCreator.value) + "." + intToString(iType.value);
 	   	
   	   sList+=sName + (char)1;
   	  	
       n++;
    }	
 
    String[] asCatalogs=new String[n];
 
    for(int i=0; i<n; i++){
    	int iPos=sList.indexOf((char)1);
    	if(iPos>=0){
       	asCatalogs[i]=new String(sList.substring(0,iPos));
       	sList=sList.substring(sList.indexOf((char)1),sList.length());
     	}
    }
 
    return asCatalogs;
 }
 
 private static String intToString(int iValue)
 {
    String sReturn=(char)((iValue/0x1000000) & 0xFF) + "";
    sReturn+=(char)((iValue/0x10000) & 0xFF);
    sReturn+=(char)((iValue/0x100) & 0xFF);
    sReturn+=(char)(iValue & 0xFF);
 
    return sReturn;
 }
 
 /**
  * Deletes the current record and sets the current record position to -1.
  * The record is immediately removed from the catalog and all subsequent
  * records are moved up one position.
  */
 
 public boolean deleteRecord()
 {
 	if (_recordPos == -1){
 		return false;
 	}
 	
    int iDelPos=_recordPos;
    
 	recClose();
 	
 	if(Palm.DmRemoveRecord(iDmRef, iDelPos) !=0){
 	   return false;
 	}
 	
 	_recordPos = -1;
 	return true;
 }
 
 /**
  * Returns the number of records in the catalog or -1 if the catalog is not open.
  */
 
 public int getRecordCount()
 {
 	if (!_isOpen){
 		return -1;
 	}
 	
 	return Palm.DmNumRecords(iDmRef);
 }
 
 /**
  * Returns the size of the current record in bytes or -1 if there is no
  * current record.
  */
 
 public int getRecordSize()
 {
 	if (_recordPos == -1){	
 		return -1;
 	}
 	
 	return iRecSize;
 }
 
 /**
  * Returns true if the catalog is open and false otherwise. This can
  * be used to check if opening or creating a catalog was successful.
  */
 
 public boolean isOpen()
 {
 	return _isOpen;
 }
 
 /**
  * Sets the current record position and locks the given record. The value
  * -1 can be passed to unset and unlock the current record. If the operation
  * is succesful, true is returned and the read/write cursor is set to
  * the beginning of the record. Otherwise, false is returned.
  */
 
 public boolean setRecordPos(int pos)
 {	
    recClose();
    if(pos<0){
       return false;
    }
    if(iDmRef==0){
        errString = "iDmRef";
       return false;
    }
 
    int iCount=Palm.DmNumRecords(iDmRef);
    if(pos>=iCount){
        errString = "DmNumRecords";
       return false;
    }
 
    if(_mode==READ_ONLY){
 		iRecH=Palm.DmQueryRecord(iDmRef,pos);
 	}
 	else{
 		iRecH=Palm.DmGetRecord(iDmRef, pos);
 	}
    if(iRecH==0){
 	   // Potentially out of memory
 	   // try a gc and then try again
 	   jump.Runtime.gc();
 	   if(_mode==READ_ONLY){
 		   iRecH=Palm.DmQueryRecord(iDmRef,pos);
 	   } else{
 		   iRecH=Palm.DmGetRecord(iDmRef, pos);
 	   }
 	   if(iRecH == 0){
 		   errString = "DmQuery/GetRecord";
 		   _recordPos = -1;
 		   return false;
 	   }
    }
 
    iRecSize=Palm.MemHandleSize(iRecH);
    iRecOffset=0;	
 	_recordPos = pos;
 	flgCurRecModified=false;
 	
 	return true;
 }
 
 /**
  * Reads bytes from the current record into a byte array. Returns the
  * number of bytes actually read or -1 if an error prevented the
  * read operation from occurring. After the read is complete, the location of
  * the cursor in the current record (where read and write operations start from)
  * is advanced the number of bytes read.
  * @param buf the byte array to read data into
  * @param start the start position in the array
  * @param count the number of bytes to read
  */
 
 public int readBytes(byte buf[], int start, int count)
 {
 	return _readWriteBytes(buf, start, count, true);
 }
 
 private int _readWriteBytes(byte buf[], int start, int count, boolean isRead)
 {
 	if (_recordPos == -1){
 		return -1;
 	}
 	if (start < 0 || count < 0){
 		return -1;
 	}
 	if (start + count > buf.length){
 		return -1;
 	}
    if(iRecOffset+count>iRecSize){
       return -1;
    }
 
    if(iRecH == 0) return -1;
 
    iRecPtr=Palm.MemHandleLock(iRecH);
    if(iRecPtr==0){
 	   return -1;
    }
 
    if(isRead){
      //this next line is counting on a new overloaded MemMove method
      //which should be available in Jump2a8 and higher
     Palm.MemMove(buf, start, iRecPtr + iRecOffset, count);
    }
    else{
       //this might need to be MemMove
      if(Palm.DmWrite(iRecPtr, iRecOffset, buf, start, count)!=0){
          return -1;
       }
       flgCurRecModified=true;
    }
 
    Palm.MemHandleUnlock(iRecH);
    iRecPtr = 0;
 
    iRecOffset+=count;
    return count;
 }
 
 /**
  * Advances the cursor in the current record a number of bytes. The cursor
  * defines where read and write operations start from in the record. Returns
  * the number of bytes actually skipped or -1 if an error occurs.
  * @param count the number of bytes to skip
  */
 
 public int skipBytes(int count)
 {
 	if (_recordPos == -1){
 		return -1;
    }
 	if(iRecOffset+count>iRecSize){
 	   return -1;
 	}
 	
 	iRecOffset+=count;
 	
 	return count;
 }
 
 /**
  * Writes to the current record. Returns the number of bytes written or -1
  * if an error prevented the write operation from occurring.
  * After the write is complete, the location of the cursor in the current record
  * (where read and write operations start from) is advanced the number of bytes
  * written.
  * @param buf the byte array to write data from
  * @param start the start position in the byte array
  * @param count the number of bytes to write
  */
 
 public int writeBytes(byte buf[], int start, int count)
 {
 	return _readWriteBytes(buf, start, count, false);
 }
 }
