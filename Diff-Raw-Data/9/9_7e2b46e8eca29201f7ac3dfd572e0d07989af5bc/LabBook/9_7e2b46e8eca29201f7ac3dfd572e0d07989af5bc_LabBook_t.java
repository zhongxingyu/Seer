 import waba.io.*;
 import waba.util.*;
 import extra.io.*;
 
 public class LabBook 
 {
     int curDeviceId = 0;
 
     /*
      * Need a hash table of the loaded obj. and
      * match them by the values of the pointers
      *
      * For temporary purposes I could just use a vector
      * and scan through it to find the objects
      */
 
     /*
      * The tricky stuff here is keeping the object reference
      * around or not around.  All un-freed objects need to be kept
      * in the cache, so that multiple references don't exist. 
      * this makes it kind of ugly.  The only other approach is
      * to build this directly into the objects.  If there was 
      * a clean up call when the object was gc'd then this would
      * be cleaner.
      * 
      * How does real Java do this?
      * The other options is to keep all objects in the cache while
      * a transaction is open.  And remove them all when the transaction
      * is closed.  This would mean that when the objects are loaded at
      * the beginning they'd need a name that could be used to identify
      * them when they are saved at the end.  
      *
      * The odmg doesn't apear to handle this at all.  When an object
      * is loaded or lookedup it doesn't necessarly handle 
      */
 
     int objIndex [];
 
     static LabObjectPtr nullLObjPtr = new LabObjectPtr(-1,-1,null);
 
     LabBookDB db;
 
     // Should get a list of the pointer to objects from the 
     // beginning of the file.  This will also help us know
     // what ids are available.  And it will tell the file address of 
     // the object.  This could be stored in an 3-tuple int array
     // this entire array will be loaded at the beginning.
     public void open(LabBookDB db)
     {
 	this.db = db;
 	curDeviceId = db.getDevId();
 
     }
 
     // add this object to list to be stored
     // and return its pointer.
     // if it has a pointer thats easy
     // if not them make a new one using 
     // curDeviceId and nextObjectId
 
     Vector toBeStored = new Vector();
     public LabObjectPtr store(LabObject lObj)
     {
 	LabObjectPtr lObjPtr;
 	
 	if(lObj == null) return nullLObjPtr;
 
 	// Need to check if this object has already been stored
 	if(lObj.ptr != null){
 	    lObjPtr = lObj.ptr;
 	    // Should be check to see if this pointer already exist in store
 	    // list??
 	} else {
 	    System.out.println("Creating new ptr");
 	    lObjPtr = new LabObjectPtr(curDeviceId, db.getNewObjId(),
 				   lObj);
 	    lObj.ptr = lObjPtr;
 	}
 
 	int numObj = toBeStored.getCount();
 	for(int i=0; i<numObj; i++){
 	    if((LabObjectPtr)(toBeStored.get(i)) == lObjPtr){
 		return lObjPtr;
 	    }
 	}
 
 	toBeStored.add(lObjPtr);
 	loaded.add(lObjPtr);
 	return lObjPtr;
     }
 
     /*
      * Need to iterate until all the objects are written
      */
     // write all the "stored" objects
     // in the process this might add more stored objects
     
     // Question: should load look at unstored objects?
     // I guess no, because it won't know about unstored objects
     // until they are commited.
     
     // this calls the writeExternal call of each object
     // it also sets up the bufferstream for the object
     // if a store is called in the middle of the writeExternal
     // a pointer needs to be returned and the object is schedualed
     // for later storage.
 
     // what if there are objects in the database that weren't loaded
     // what do we do with them?  And if they are loaded and changed
     // then we need to replace them.
 
     // One way to handle all this is to write all the "new" objects 
     // to a temporary file and then copy any un-written objects from
     // the old file.  But that is a time consuming operation.  It seems
     // that should be a special function (perhaps compact).  The alternative
     // is write the object in the empty spaces of the file, or be able
     // to write parts of the object to multiple sections of the file.  
     // That would be the best.
     BufferStream bsOut = new BufferStream();
     DataStream dsOut = new DataStream(bsOut);
 
     public void commit()
     {
 	Vector alreadyStored = new Vector();
 
 	int origNumObj = 0;
 	Object [] curObjArr;
 	int numObj = toBeStored.getCount();
 	int numStored = 0;
 	int objType = -1;
 	int i,j;
 	LabObjectPtr curObjPtr;
 	byte [] outBuf;
 
 	while(numObj > origNumObj){
 	    origNumObj = numObj;
 	    curObjArr = toBeStored.toObjectArray();
 
 	    for(i=0; i< numObj; i++){
 		numStored = alreadyStored.getCount();
 		for(j=0; j<numStored; j++){
 		    if(curObjArr[i] == alreadyStored.get(j)){
 			break;
 		    }
 		}
 		if(j == numStored){
 		    // Need to write this object
 		    // this means allocating BufferStream 
 		    // and then sending buffer to LabBookDB
 		    // and writting the object type id to the file
 		    
 		    curObjPtr = (LabObjectPtr)curObjArr[i];
 		    dsOut.writeInt(curObjPtr.obj.objectType);
 		    curObjPtr.obj.writeExternal(dsOut);
 		    outBuf = bsOut.getBuffer();
 		    db.writeObjectBytes(curObjPtr.devId, curObjPtr.objId, 
 					outBuf, 0, outBuf.length);
 
 		    bsOut.setBuffer(null);
 		    alreadyStored.add(curObjArr[i]);
 		}
 	    }
 
 	    numObj = toBeStored.getCount();
 	}
 
 	toBeStored = new Vector();
 	loaded = new Vector();
     }
 
     // increase reference count in lab Object
     // check if value of lObjPtr matches a prev. loaded object
     // or stored object
     // return that object
     // otherwise, 
     // find object in file
     // instanciate object
     // get length of object data from file
     // this requires a bufferDataStream for input and output
     // the input buffer size can be fixed, but the output
     // buffer size needs to be dynamic and beable to report
     // can use wextras DataStream and BufferStream
     // the length of what has been written
     // read object byes 
     // add new object to hashtable
     // send bytes to object readExternal
     // if another object is loaded in the middle of the readExternal
     // this should be ok. Loops won't be formed because the current
     // object is already in the hashtable so it won't be "loaded" again.
 
     Vector loaded = new Vector();
     public LabObject load(LabObjectPtr lObjPtr)
     {
 	int i;
 	int numLoaded = loaded.getCount();
 	LabObjectPtr curObjPtr;
 	if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 
 	for(i=0; i<numLoaded; i++){
 	    curObjPtr = (LabObjectPtr)loaded.get(i);
 	    if(curObjPtr.devId == lObjPtr.devId && 
 	       curObjPtr.objId == lObjPtr.objId){
 		lObjPtr.obj = curObjPtr.obj;
 		return curObjPtr.obj;
 	    }
 	}
 
	BufferStream bsIn = new BufferStream();
	DataStream dsIn = new DataStream(bsIn);

 	// We didn't find it so we need to parse it from the file
 	byte [] buffer = db.readObjectBytes(lObjPtr.devId, lObjPtr.objId);
 	if(buffer == null) return null;
 
 	// set bufferStream buffer
 	// read buffer by
 	bsIn.setBuffer(buffer);
 	
 	int objectType = dsIn.readInt();
 	LabObject lObj = null;
 
 	// We need a way to instanciate object.
 	// We could have a list of objects and every new lab object will
 	// need to be added to this list.
 	lObj = LabObject.getNewObject(objectType);
 	lObj.ptr = lObjPtr;
 
 	// This might be recursive so add this object to the 
 	// loaded array so we don't load it again
 	lObjPtr.obj = lObj;
 	loaded.add(lObjPtr);
 	
 	lObj.readExternal(dsIn);
 
 	return lObj;
     }
 
     public void close()
     {
 	db.close();
     }
 }
