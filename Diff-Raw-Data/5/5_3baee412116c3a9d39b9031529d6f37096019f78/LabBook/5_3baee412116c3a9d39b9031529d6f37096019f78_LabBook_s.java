 package org.concord.LabBook;
 
 import waba.io.*;
 import waba.util.*;
 import org.concord.waba.extra.io.*;
 
 public class LabBook 
 {
 //	public static Vector objFactories;
 	public static LabObjectFactory []objFactories = null;
 
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
 
     LabObjectPtr rootPtr = null;
 
     LabBookDB db;
 
     Vector loaded = new Vector();
 
 
 	public static void init()
 	{
 		registerFactory(new DefaultFactory());
 	}
 
 	public LabBook()
 	{
 		LabObjectPtr.lBook = this;
 	}
 
     // Should get a list of the pointer to objects from the 
     // beginning of the file.  This will also help us know
     // what ids are available.  And it will tell the file address of 
     // the object.  This could be stored in an 3-tuple int array
     // this entire array will be loaded at the beginning.
     public void open(LabBookDB db)
     {
 		this.db = db;		
 
 		rootPtr = db.getRootPtr();
     }
 
     public LabObjectPtr getRoot()
     {
 		return rootPtr;
     }
 
 	
 	public static LabObjectFactory findFactoryByType(int factoryType){
 		if(objFactories == null || objFactories.length < 1) return null;
 		for(int i = 0; i < objFactories.length; i++){
 			if(factoryType == objFactories[i].getFactoryType()){
 				return objFactories[i];
 			}
 		}
 		return null;
 	}
 	
 	public static void registerFactory(LabObjectFactory objFact)
 	{
 		if(objFact == null) return;
 		if(findFactoryByType(objFact.getFactoryType()) != null) return;
 		
 		int nFactories = (objFactories == null)?0:objFactories.length;
 		LabObjectFactory []newFactories = new LabObjectFactory[nFactories + 1];
 		if(objFactories != null){
 			waba.sys.Vm.copyArray(objFactories,0,newFactories,0,nFactories);
 		}
 		newFactories[nFactories] = objFact;	
 		objFactories = newFactories;
 	}
 
 	public static LabObject makeNewObj(int type)
 	{
 		return makeNewObj(type, true);
 	}
 
 	public static LabObject makeNewObj(int type, boolean init)
 	{
 		if(objFactories == null) return null;
 		LabObject newObj = null;
 		for(int i=0;i<objFactories.length; i++){
 			LabObjectFactory objFact = objFactories[i];
 			newObj = objFact.makeNewObj(type, init);
 			if(newObj != null) break;
 		}
 
 		return newObj;
 	}
 
 	public LabObjectPtr getNullObjPtr(){ return db.getNewLocalObjPtr(null); }
 
 	LabObjectPtr registerNew(LabObject lObj, LabBookDB objDB)
 	{
 		Debug.println("Creating new ptr");
 		LabObjectPtr lObjPtr = objDB.getNewLocalObjPtr(lObj);
 
 		lObj.ptr = lObjPtr;
 
 		lObj.incRefCount();
 		loaded.add(lObjPtr);
 		return lObjPtr;
 	}
 
     // add this object to list to be stored
     // and return its pointer.
     // if it has a pointer thats easy
     // if not them make a new one using 
     // curDeviceId and nextObjectId
 
     Vector toBeStored = new Vector();
     Vector alreadyStored = null;
     public LabObjectPtr store(LabObject lObj)
     {
 		LabObjectPtr lObjPtr;
 		int i;
 	
 		lObjPtr = lObj.ptr;
 		
 		// if there is a null excpetion here it is because the object has been released
 		// you can't store a released object 
 		lObj = lObjPtr.obj;
 
 		/*
 		// double check to see if this object is in loaded
 		// if not there is bug
 		int numLoaded = loaded.getCount();
 		LabObjectPtr curObjPtr;
 		for(i=0; i<numLoaded; i++){
 			curObjPtr = (LabObjectPtr)loaded.get(i);
 			if(curObjPtr.equals(lObjPtr)) break;
 		}
 		if(i == numLoaded) throw new RuntimeException("stored object not valid");
 		*/
 
 		// Should check to see if this pointer already exist in store
 		Object [] curObjArr;
 		if(alreadyStored != null){
 			curObjArr = alreadyStored.toObjectArray();
 			for(i=0; i< curObjArr.length; i++){
 				if(curObjArr[i] == lObjPtr){
 					// this ptr has already been stored.
 					break;
 				}
 			}
 
 			if(i != curObjArr.length){
 				// This object has already been written out
 				return lObjPtr;
 			}
 		}
 
 		// Need to check if this object has already been stored
 		curObjArr = toBeStored.toObjectArray();
 		for(i=0; i< curObjArr.length; i++){
 			if(curObjArr[i] == lObjPtr){
 				// this ptr will be stored.
 				break;
 			}
 		}
 
 		if(i != curObjArr.length){
 			// This object is already slated to be written out
 			return lObjPtr;
 		}
 
 		toBeStored.add(lObjPtr);
 
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
 
     public boolean commit()
     {
 		Object [] curObjArr;
 		int i,j;
 		LabObjectPtr curObjPtr;
 		byte [] outBuf;
 
 		alreadyStored = new Vector();
 
 		while(toBeStored.getCount() > 0){
 			curObjArr = alreadyStored.toObjectArray();
 
 			curObjPtr = (LabObjectPtr)toBeStored.get(0);
 
 			for(i=0; i< curObjArr.length; i++){
 				if(curObjArr[i] == curObjPtr){
 					// this ptr has already been stored.
 					toBeStored.del(0);
 					break;
 				}
 			}
 			if(i == curObjArr.length){
 				// This object hasn't been stored yet.
 				alreadyStored.add(curObjPtr);
 				toBeStored.del(0);
 
 				writeHeader(curObjPtr.obj, dsOut);
 
 				// This might call store which will change the toBeStored vector
 				curObjPtr.obj.writeExternal(dsOut);
 				outBuf = bsOut.getBuffer();
 				if(!db.writeObjectBytes(curObjPtr, outBuf, 0, outBuf.length)){
 					toBeStored = new Vector();
 					loaded = new Vector();
 					alreadyStored = null;
 		    
 					return false;
 				}
 
 				bsOut.setBuffer(null);
 			}
 		}	       
 
 		toBeStored = new Vector();
 		loaded = new Vector();
 		alreadyStored = null;
 
 		db.save();
 
 		return true;
     }
 
 	boolean commit(LabObjectPtr lObjPtr)
 	{
 		byte [] outBuf;
 
 		// delete this from the to be stored list
 		int index = toBeStored.find(lObjPtr);
 		if(index < 0) return false;
 
 		// write object header
 		writeHeader(lObjPtr.obj, dsOut);
 
 		// This might call store which will change the toBeStored vector
 		lObjPtr.obj.writeExternal(dsOut);
 		outBuf = bsOut.getBuffer();
 		if(!db.writeObjectBytes(lObjPtr, outBuf, 0, outBuf.length)){
 			return false;
 		}
 
 
 		bsOut.setBuffer(null);
 
 		toBeStored.del(index);
 
 		return true;
 	}
 
 	private void writeHeader(LabObject lObj, DataStream dsOut)
 	{
 		dsOut.writeShort(lObj.objectType);
 		dsOut.writeString(lObj.getName());
 		dsOut.writeShort(lObj.getFlags());
 	}
 
 
     public boolean reload(LabObject lObj)
     {
 		if(lObj == null) return false;
 
 		// Need to check if this object has already been stored
 		if(lObj.ptr == null){
 			Debug.println("reload: Null pointer");
 			return false;
 		}
 		int i;
 		int numLoaded = loaded.getCount();
 		LabObjectPtr curObjPtr;
 
 		for(i=0; i<numLoaded; i++){
 			curObjPtr = (LabObjectPtr)loaded.get(i);
 			if(curObjPtr.equals(lObj.ptr)){
 				break;
 			}
 		}
 
 		if(i == numLoaded){
 			// This object hasn't been loaded or there was a commit since it 
 			// was last loaded.  So it should be loaded using the regular load			
 			Debug.println("reload: Not pre-loaded");
 			loaded.add(lObj.ptr);
 			return false;
 		}
 
 		BufferStream bsIn = new BufferStream();
 		DataStream dsIn = new DataStream(bsIn);
 
 		// We didn't find it so we need to parse it from the file
 		byte [] buffer = db.readObjectBytes(lObj.ptr);
 		if(buffer == null) return false;
 
 		// set bufferStream buffer
 		// read buffer by
 		bsIn.setBuffer(buffer);
 	
 		int objectType = dsIn.readShort();		
 
 		if(lObj.objectType != objectType){
 			Debug.println("reload: Non-matching object");
 			return false;
 		}
 
 		//need to read the name string
 		String name = dsIn.readString();
 		lObj.ptr.flags = dsIn.readShort();					
 
 		// probably we should update the object name
 		lObj.setName(name);
 
 		// We should check if the object is in the loaded list
 		//	.. loaded.add(lObjPtr);
 		lObj.setFlags(lObj.ptr.flags);
 		lObj.readExternal(dsIn);
 
 		return true;
 	      
     }
 
 	LabBookSession getSession(LabBookDB sessDB)
 	{
 		return new LabBookSession(this, sessDB);		
 	}
 
     // check if value of lObjPtr matches a prev. loaded object
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
 	boolean getObj(LabObjectPtr lObjPtr, LabBookDB ptrDB, 
 				   boolean checkLoaded)
 	{
 		if(lObjPtr.devId == -1 && lObjPtr.objId == -1){
 			lObjPtr.obj = null;
 			return false;
 		}
 
 		DataStream dsIn = initPointer(lObjPtr, ptrDB, checkLoaded);
 		if(dsIn == null){
 			// this object was already loaded
 			return false;
 		}
 
 		LabObject lObj = null;
 
 		// We need a way to instanciate object.
 		// We could have a list of objects and every new lab object will
 		// need to be added to this list.
 		lObj = makeNewObj(lObjPtr.objType, false);
 		if(lObj == null){
 			Debug.println("error: objectType: " + lObjPtr.objType + " devId: " + lObjPtr.devId +
 						  " objId: " + lObjPtr.objId);
 			lObjPtr.obj = null;
 			return false;
 		}
 
 		lObj.ptr = lObjPtr;
 		lObjPtr.obj = lObj;
 		lObj.setName(lObjPtr.name);
 
 		// Note this shouldn't be recursive so we should
 		// add a check for this
 		lObj.setFlags(lObjPtr.flags);
 		lObj.readExternal(dsIn);
 
 		return true;
 	}
 
     // increase reference count in lab Object
     LabObject load(LabObjectPtr lObjPtr)
     {
 		boolean newObj = getObj(lObjPtr, db, true);
 
 		if(lObjPtr.obj != null){
 			lObjPtr.obj.incRefCount();
 
 			// We need to check if we need to add this to the loaded list
 			if(newObj){
 				loaded.add(lObjPtr);		    
 			}
 		}
 		return lObjPtr.obj;
 	}
 
 	/**
 	 * Check if the object this pointer points to is already loaded
 	 * If so, update this pointers name and type info
 	 * If not, read the heading information from the object
 	 * 
 	 * returns the datastream of the object data if the object hasn't
 	 * already been loaded, otherwise it returns null
 	 */
 	private DataStream initPointer(LabObjectPtr lObjPtr, LabBookDB ptrDB,
 								   boolean checkLoaded)
 	{
 		int i;
 		int numLoaded = loaded.getCount();
 		LabObjectPtr curObjPtr;
 
 		// if this is true we have major problems
 		if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 
 		if(checkLoaded){
 			for(i=0; i<numLoaded; i++){
 				curObjPtr = (LabObjectPtr)loaded.get(i);
 				if(curObjPtr.equals(lObjPtr)){
 					lObjPtr.obj = curObjPtr.obj;
 					lObjPtr.objType = lObjPtr.obj.objectType;
 					lObjPtr.name = lObjPtr.obj.getName();
 					lObjPtr.flags = lObjPtr.obj.getFlags();
 					return null;
 				}
 			}
 		}
 
 		BufferStream bsIn = new BufferStream();
 		DataStream dsIn = new DataStream(bsIn);
 
 		// We didn't find it so we need to parse it from the file
 		byte [] buffer = ptrDB.readObjectBytes(lObjPtr);
 		if(buffer == null){
 			return null;
 		}
 		// set bufferStream buffer
 		// read buffer by
 		bsIn.setBuffer(buffer);
 	
 		lObjPtr.objType = dsIn.readShort();
 		lObjPtr.name = dsIn.readString();
 		lObjPtr.flags = dsIn.readShort();			
 
 		return dsIn;
 	}
 
 	public boolean readHeader(LabObjectPtr lObjPtr)
 	{
 		// if this is true we have major problems
 		if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return false;
 
 		initPointer(lObjPtr, db, true);
 		return true;
 	}
 	/*
 	public void printCaches()
 	{
 		System.out.println("LB: loaded:");
 		for(int i=0; i<loaded.getCount(); i++){
 			System.out.println(" ptr: " + loaded.get(i));
 		}
 
 		System.out.println("LB: stored:");
 		for(int i=0; i<toBeStored.getCount(); i++){
 			System.out.println(" ptr: " + toBeStored.get(i) + 
 							   " obj: " + ((LabObjectPtr)toBeStored.get(i)).obj);
 		}
 	}	
 */
 	public void release(LabObject lObj)
 	{
 		LabObjectPtr curObjPtr = null;
 		LabObjectPtr lObjPtr = lObj.ptr;
 
 		int numLoaded = loaded.getCount();
 		// if this is true we have major problems
 		// if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 		for(int i=0; i<numLoaded; i++){
 			curObjPtr = (LabObjectPtr)loaded.get(i);
 			if(curObjPtr.equals(lObjPtr)){
 				// found it
 				//				System.out.println("LB: releasing: " + lObj);
 				
 				commit(lObjPtr);
 				loaded.del(i);
 				lObjPtr.obj = null;
 				lObj.ptr = null;
 				break;
 			}
 		}		
 	}
 
 	public void export(LabObject lObj, LabBookDB db)
     {
 		// this might switch the root obj pointer
 		LabObjectPtr lObjPtr = null;
 		if(lObj instanceof LObjSubDict){
 			// hmm. we need the subDict
 			lObj = ((LObjSubDict)lObj).getDict();
 		}
 		lObjPtr = lObj.ptr;
 
 		LabObjectPtr destObjPtr = copyAll(lObjPtr, this.db, db);
 
 		db.setRootPtr(destObjPtr);
     }
 
 
     public LabObject importDB(LabBookDB db)
     {
 		LabObjectPtr rootPtr = db.getRootPtr();
 		initPointer(rootPtr, db, false);
 
 		LabObjectPtr lObjPtr = copyAll(rootPtr, db, this.db);
 		
 		// This is hack it should be fixed
 		return load(lObjPtr);
 	}
 
 	public LabObjectPtr copy(LabObjectPtr lObjPtr)
 	{
 		return copyAll(lObjPtr, db, db);
 	}
 
 	private LabObjectPtr copy(LabObjectPtr lObjPtr, 
 							  LabBookDB srcDB, LabBookDB destDB,
 							  Vector trans)
 	{
 		// check if the old pointer has already been copied
 		// by looking in the translation table
 		// if so return translated pointer
 		int numTrans = trans.getCount();
 		LabObjectPtr curObjPtr;
 
 		// if this is true we have major problems
 		// if(lObjPtr.devId == -1 && lObjPtr.objId == -1) return null;
 		for(int i=0; i<numTrans; i+=2){
 			curObjPtr = (LabObjectPtr)trans.get(i);
 			if(curObjPtr.equals(lObjPtr)){
 				// This pointer has already been translated and written
 				return (LabObjectPtr)trans.get(i+1);
 			}
 		}
 
 		// otherwise get a new pointer from the destDB
 		LabObjectPtr newObjPtr = destDB.getNewLocalObjPtr();
 		newObjPtr.objType = lObjPtr.objType;
 
 		trans.add(lObjPtr);
 		trans.add(newObjPtr);
 
 		if(newObjPtr.objType == DefaultFactory.DICTIONARY){
 			return newObjPtr;
 		}
 
 		byte [] buffer = null;
 		// check if the pointer is currently loaded
 		// the pfObjPtrs passed in need to be initialized which 
 		// means their .obj field will tell if they are loaded
 		if(lObjPtr.obj != null){
 			// the object is loaded  
 			// so we need to tell it write it's state out to a buffer
 			// then we will put this buffer in output database
 			// we need to treat dictionaries specially here because
 			// they need to be translated
 
 			bsOut.setBuffer(null);
 
 			// write object header
 			writeHeader(lObjPtr.obj, dsOut);
 
 			// This might call store which will change the toBeStored vector
 			lObjPtr.obj.writeExternal(dsOut);
 			buffer = bsOut.getBuffer();
 			bsOut.setBuffer(null);
 			
 		} else {
 			// the object isn't loaded so we just get the buffer from
 			// the source DB and copy it to the destDB
 			// we need to treat dictionaries specially here because
 			// they need to be translated
 
 			// We didn't find it so we need to parse it from the file
 			buffer = srcDB.readObjectBytes(lObjPtr);
 		}
 
 		if(buffer == null){
 			// maybe error or maybe empty object 
 			// return null pointer
 			return destDB.getNewLocalObjPtr(null);
 		}
 
 		if(!destDB.writeObjectBytes(newObjPtr, buffer, 0, buffer.length)){
 			return null;
 		}
 
 		return newObjPtr;
 	}
 
     private LabObjectPtr copyAll(LabObjectPtr lObjPtr, LabBookDB srcDB,
 								 LabBookDB destDB)
     {	
 		LabObjectPtr retObjPtr = null;
 
 		Vector trans = new Vector();
 		Vector dictionaries = new Vector();
 		LabObjectPtr ptr =  retObjPtr = copy(lObjPtr, srcDB, destDB, trans);
 
 		if(lObjPtr.objType == DefaultFactory.DICTIONARY){
 			dictionaries.add(lObjPtr);
 			dictionaries.add(ptr);
 		} 
 
 		boolean checkLoaded = false;
 		if(srcDB == this.db){
 			checkLoaded = true;
 		}
 
 		int curDict=0;
 		while(curDict < dictionaries.getCount()){
 			LabObjectPtr dictPtr = (LabObjectPtr)dictionaries.get(curDict);
 
 			// We don't need to check if the dict has already been copied/tanslated
 			// because we do this before we add it to the list
 
 			// better than load would be to not save it in the loaded vect
 			// then we wouldn't have to release it
 			boolean newObj = getObj(dictPtr, srcDB, checkLoaded);
 			LObjDictionary srcDict = (LObjDictionary)dictPtr.obj;
 			if(newObj) dictPtr.obj = null;
 
 			// init, storeNew??
 			LObjDictionary destDict = DefaultFactory.createDictionary();
 			destDict.viewType = srcDict.viewType;
 			destDict.hasMainObject = srcDict.hasMainObject;
 			destDict.hideChildren = srcDict.hideChildren;
 			destDict.setName(srcDict.getName());

 			for(int i=0; i<srcDict.getChildCount(); i++){	
 				int oldTransCount = trans.getCount();
 
 				LabObjectPtr srcPtr = (LabObjectPtr)srcDict.objects.get(i);
 				initPointer(srcPtr, srcDB, false);
 
 				ptr = copy(srcPtr, srcDB, destDB, trans);
 				if(ptr.objType == DefaultFactory.DICTIONARY &&
 				   oldTransCount != trans.getCount()){
 					// this is a new dictionary that we haven't
 					// seen before
 					dictionaries.add(srcPtr);
 					dictionaries.add(ptr);
 				} 
 
 				destDict.objects.add(ptr);
 			}
 
 			byte [] buffer = null;
 
 			bsOut.setBuffer(null);
 			// write object header
 			writeHeader(destDict, dsOut);
 
 			destDict.writeExternal(dsOut);
 			buffer = bsOut.getBuffer();
 			bsOut.setBuffer(null);
 
 			if(buffer != null){
				if(!destDB.writeObjectBytes((LabObjectPtr)dictionaries.get(curDict+1), 
 											buffer, 0, buffer.length)){
 					return null;
 				}
 			} else {
 				// empty object ??
 			}
 			curDict+=2;
 		}
 
 		return retObjPtr;
     }
 
     public boolean close()
     {
 		boolean ret = db.save();
 
 		db.close();
 
 		return ret;
     }
 }
