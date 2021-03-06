 package ibis.satin.impl;
 
 import ibis.ipl.IbisError;
 import ibis.ipl.SendPort;
 import ibis.ipl.WriteMessage;
 import ibis.satin.ActiveTuple;
 import ibis.util.Sequencer;
 import ibis.util.TypedProperties;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 
 public abstract class TupleSpace extends Communication {
 
 	static HashMap space = new HashMap();
 
 	public static boolean use_seq = false;
 
 	public static void initTupleSpace() {
 		if (this_satin != null && !this_satin.closed) {
 			System.err
 					.println("The tuple space currently only works with a closed world. Try running with -satin-closed");
 			System.exit(1);
 			//			throw new IbisError("The tuple space currently only works with a
 			// closed world. Try running with -satin-closed");
 		}
 
 		use_seq = SUPPORT_TUPLE_MULTICAST && this_satin != null
 				&& TypedProperties.booleanProperty("satin.tuplespace.ordened");
 		//				System.err.println("TupleSpace: use_seq = " + use_seq);
 		//		newKeys = new ArrayList();
 		//		newData = new ArrayList();
 	}
 
 	/**
 	 * Adds an element with the specified key to the global tuple space. If a
 	 * tuple with this key already exists, it is overwritten with the new
 	 * element. The propagation to other processors can take an arbitrary amount
 	 * of time, but it is guaranteed that after multiple updates by the same
 	 * processor, eventually all processors will have the latest value.
 	 * <p>
 	 * However, if multiple processors update the value of the same key, the
 	 * value of an updated key can be different on different processors.
 	 * 
 	 * @param key
 	 *            The key of the new tuple.
 	 * @param data
 	 *            The data associated with the key.
 	 */
 	public static void addTuple(String key, Serializable data) {
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name()
 					+ ": added key " + key);
 		}
 
 		//		System.err.println("use seq = " + use_seq + ", this_satin = " +
 		// this_satin);
 
 		if (this_satin != null) { // can happen with sequential versions of
 			// Satin
 			// programs
 			this_satin.broadcastTuple(key, data);
 		}
 
 		if (!use_seq || this_satin == null) {
 			if (data instanceof ActiveTuple) {
 				((ActiveTuple) data).handleTuple(key);
 			} else {
 				synchronized (space) {
 					space.put(key, data);
 				}
 			}
 		}
 
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name()
 					+ ": adding of key " + key + " done");
 		}
 	}
 
 	/**
 	 * Retrieves an element from the tuple space. If the element is not in the
 	 * space yet, this operation blocks until the element is inserted.
 	 * 
 	 * @param key
 	 *            the key of the element retrieved.
 	 * @return the data associated with the key.
 	 */
 	public static Serializable getTuple(String key) {
 		Serializable data = null;
 
 		if(TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name() + ": get key " + key);
 		}
 
 		synchronized (space) {
 			while (data == null) {
 				data = (Serializable) space.get(key);
 				if (data == null) {
 					try {
 						if (TUPLE_DEBUG) {
 							System.err.println("SATIN '"
 									+ this_satin.ident.name() + ": get key "
 									+ key + " waiting");
 						}
 
 						space.wait();
 
 						if (TUPLE_DEBUG) {
 							System.err.println("SATIN '"
 									+ this_satin.ident.name() + ": get key "
 									+ key + " waiting DONE");
 						}
 
 					} catch (Exception e) {
 						// Ignore.
 					}
 				}
 			}
 
 			if(TUPLE_DEBUG) {
 				System.err.println("SATIN '" + this_satin.ident.name() + ": get key " + key + " DONE");
 			}
 
 			return data;
 		}
 	}
 
 	/**
 	 * Removes an element from the tuple space.
 	 * 
 	 * @param key
 	 *            the key of the tuple to be removed.
 	 */
 	public static void removeTuple(String key) {
 
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name()
 					+ ": removed key " + key);
 		}
 		if (this_satin != null) {
 			this_satin.broadcastRemoveTuple(key);
 			if (TUPLE_DEBUG) {
 				System.err.println("SATIN '" + this_satin.ident.name()
 						+ ": bcast remove key " + key + " done");
 			}
 		}
 		if (!use_seq || this_satin == null) {
 			synchronized (space) {
 				if (ASSERTS && !space.containsKey(key)) {
 					throw new IbisError("Key " + key
 							+ " is not in the tuple space");
 				}
 
 				space.remove(key);
 
 				// also remove it from the new lists (if there)
 				//			int index = newKeys.indexOf(key);
 				//			if(index != -1) {
 				//				newData.remove(index);
 				//			}
 			}
 		}
 	}
 
 	public static void remoteAdd(String key, Serializable data) {
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name()
 					+ ": remote add of key " + key);
 			if (data == null) {
 			    System.err.println("data = null!");
 			}
 		}
 
 		synchronized (space) {
 			space.put(key, data);
 			//			newKeys.add(key);
 			//			newData.add(data);
 			space.notifyAll();
 		}
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name()
 					+ ": remote add of key " + key + " DONE");
 		}
 	}
 
 	public static void remoteDel(String key) {
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + this_satin.ident.name()
 					+ ": remote del of key " + key);
 		}
 		synchronized (space) {
 			space.remove(key);
 
 			// also remove it from the new lists (if there)
 			//			int index = newKeys.indexOf(key);
 			//			if(index != -1) {
 			//				newData.remove(index);
 			//			}
 		}
 	}
 
 	/* ------------------- tuple space stuff ---------------------- */
 
 	protected void broadcastTuple(String key, Serializable data) {
 		long count = 0;
 		int size = 0;
 
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + ident.name() + "': bcasting tuple "
 					+ key);
 		}
 
 		synchronized (this) {
 			size = victims.size();
 		}
 
		if (! SUPPORT_TUPLE_MULTICAST && size == 0)
 			return; // don't multicast when there is no-one.
 
 		if (TUPLE_TIMING) {
 			tupleTimer.start();
 		}
 
 		if (SUPPORT_TUPLE_MULTICAST) {
 		        tuple_message_sent = true;
 
 			try {
 				WriteMessage writeMessage = tuplePort.newMessage();
 				writeMessage.writeByte(Protocol.TUPLE_ADD);
 				writeMessage.writeString(key);
 				writeMessage.writeObject(data);
 
 				if (TUPLE_STATS) {
 					tupleMsgs++;
 					count = writeMessage.finish();
 				} else {
 					writeMessage.finish();
 				}
 
 			} catch (IOException e) {
 				if (!FAULT_TOLERANCE) {
 					System.err.println("SATIN '" + ident.name()
 							+ "': Got Exception while sending tuple update: "
 							+ e);
 					e.printStackTrace();
 					System.exit(1);
 				}
 				//always happens after crash
 			}
 
 			// Wait until the message is delivered locally.
 			if (use_seq) {
 				synchronized (this) {
 					while (tuple_message_sent) {
 						try {
 							wait();
 						} catch (Exception e) {
 						    // Ignore
 						}
 					}
 				}
 			}
 		} else {
 			for (int i = 0; i < size; i++) {
 				try {
 					SendPort s = null;
 					synchronized (this) {
 						s = victims.getPort(i);
 					}
 					WriteMessage writeMessage = s.newMessage();
 					writeMessage.writeByte(Protocol.TUPLE_ADD);
 					writeMessage.writeString(key);
 					writeMessage.writeObject(data);
 
 					if (TUPLE_STATS && i == 0) {
 						tupleMsgs++;
 						count = writeMessage.finish();
 					} else {
 						writeMessage.finish();
 					}
 
 				} catch (IOException e) {
 					System.err.println("SATIN '" + ident.name()
 							+ "': Got Exception while sending tuple update: "
 							+ e);
 					System.exit(1);
 				}
 			}
 		}
 
 		tupleBytes += count;
 
 		if (TUPLE_TIMING) {
 			tupleTimer.stop();
 			//			System.err.println("SATIN '" + ident.name() + ": bcast of " +
 			// count + " bytes took: " + tupleTimer.lastTime());
 		}
 
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + ident.name() + "': bcasting tuple "
 					+ key + " DONE");
 		}
 	}
 
 	protected void broadcastRemoveTuple(String key) {
 		long count = 0;
 		int size = 0;
 
 		if (TUPLE_DEBUG) {
 			System.err.println("SATIN '" + ident.name()
 					+ "': bcasting remove tuple" + key);
 		}
 
 		synchronized (this) {
 			size = victims.size();
 		}
 
		if(! SUPPORT_TUPLE_MULTICAST && size == 0)
 			return; // don't multicast when there is no-one.
 
 		if (TUPLE_TIMING) {
 			tupleTimer.start();
 		}
 
 		if (SUPPORT_TUPLE_MULTICAST) {
 		        tuple_message_sent = true;
 
 			try {
 				WriteMessage writeMessage = tuplePort.newMessage();
 				writeMessage.writeByte(Protocol.TUPLE_DEL);
 				writeMessage.writeString(key);
 
 				if (TUPLE_STATS) {
 					tupleMsgs++;
 					count += writeMessage.finish();
 				} else {
 					writeMessage.finish();
 				}
 
 			} catch (IOException e) {
 				if (!FAULT_TOLERANCE) {
 					System.err.println("SATIN '" + ident.name()
 							+ "': Got Exception while sending tuple update: "
 							+ e);
 					System.exit(1);
 				}
 				//always happen after crashes
 			}
 			if (use_seq) {
 				    synchronized(this) {
 					    while (tuple_message_sent) {
 						    try {
 							    wait();
 						     } catch(Exception e) {
 						}
 					}
 				}
 			}
 		} else {
 			for (int i = 0; i < size; i++) {
 				try {
 					SendPort s = victims.getPort(i);
 					WriteMessage writeMessage = s.newMessage();
 					writeMessage.writeByte(Protocol.TUPLE_DEL);
 					writeMessage.writeString(key);
 
 					if (TUPLE_STATS && i == 0) {
 						tupleMsgs++;
 						count += writeMessage.finish();
 					} else {
 						writeMessage.finish();
 					}
 
 				} catch (IOException e) {
 					System.err.println("SATIN '" + ident.name()
 							+ "': Got Exception while sending tuple update: "
 							+ e);
 					System.exit(1);
 				}
 			}
 		}
 
 		tupleBytes += count;
 
 		if (TUPLE_TIMING) {
 			tupleTimer.stop();
 			//			System.err.println("SATIN '" + ident.name() + ": bcast of " +
 			// count + " bytes took: " + tupleTimer.lastTime());
 		}
 	}
 
 	// hold the lock when calling this
 	protected void addToActiveTupleList(String key, Serializable data) {
 		if (ASSERTS) {
 			assertLocked(this);
 		}
 		activeTupleKeyList.add(key);
 		activeTupleDataList.add(data);
 	}
 
 	void handleActiveTuples() {
 		String key = null;
 		ActiveTuple data = null;
 
 		while (true) {
 			synchronized (this) {
 				if (activeTupleKeyList.size() == 0) {
 					gotActiveTuples = false;
 					return;
 				}
 
 				// do upcall
 				key = (String) activeTupleKeyList.remove(0);
 				data = (ActiveTuple) activeTupleDataList.remove(0);
 				if (TUPLE_DEBUG) {
 					System.err.println("calling active tuple key = " + key
 							+ " data = " + data);
 				}
 			}
 
 			try {
 				data.handleTuple(key);
 			} catch (Throwable t) {
 				System.err.println("WARNING: active tuple threw exception: "
 						+ t);
 			}
 		}
 	}
 
 	void enableActiveTupleOrdening() {
 		if (tuplePortLocalConnection)
 			return;
 
 		connect(tuplePort, tupleReceivePort.identifier());
 		tuplePortLocalConnection = true;
 	}
 }
