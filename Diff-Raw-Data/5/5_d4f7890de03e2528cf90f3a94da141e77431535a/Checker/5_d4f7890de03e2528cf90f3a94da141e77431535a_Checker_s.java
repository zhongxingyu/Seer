 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import static java.util.Collections.*;
 import java.lang.ref.*;
 
 public class Checker{
 	private static Map<Object, WeakReference<Thread>> owners =
 		synchronizedMap(new WeakHashMap<Object, WeakReference<Thread>>());
 
 	private static Map<Object, WeakReference<Thread>> roOwners =
 		synchronizedMap(new WeakHashMap<Object, WeakReference<Thread>>());
 
 	public static void check(Object o){
 		if(o == null)
 			return;
 
 		Thread ct = Thread.currentThread();
 		WeakReference<Thread> ref = owners.get(o);
 
 		if(ref == null){
 			owners.put(o, new WeakReference<Thread>(ct));
 //			System.err.printf("OK claim \"%s\" -> %s\n", o, ct);
 			return;
 		}
 
 		Thread owner = ref.get();
		if(owner == null){
			owners.put(o, new WeakReference<Thread>(ct));
 			throw new IllegalAccessError(String.format("BAD re-thread \"%s\" -> %s\n", o, ct));
		}
 
 		if(owner.equals(ct)){
 //			System.err.printf("OK access (%s -> %s)\n", o, ct);
 			return;
 		}
 
 		throw new IllegalAccessError(String.format("BAD access (%s -> %s)\n", o, ct));
 	}
 
 	public static void roCheck(Object o){
 		if(o == null)
 			return;
 
 		Thread ct = Thread.currentThread();
 		WeakReference<Thread> ref = roOwners.get(o);
 
 		if(ref == null){
 			roOwners.put(o, new WeakReference<Thread>(ct));
 //			System.err.printf("OK ro-claim \"%s\" -> %s\n", o, ct);
 			return;
 		}
 
 		Thread owner = ref.get();
 		if(owner == null){
 			roOwners.put(o, new WeakReference<Thread>(ct));
 //			System.err.printf("OK ro-re-thread \"%s\" -> %s\n", o, ct);
 			return;
 		}
 
 		if(owner.equals(ct)){
 //			System.err.printf("OK ro-access (%s -> %s)\n", o, ct);
 			return;
 		}
 
 		throw new IllegalAccessError(String.format("BAD access (%s -> %s)", o, ct));
 	}
 
 	public static void guardByThis(Object o){
 		if(o == null)
 			return;
 
 		if(!Thread.holdsLock(o))
 			throw new IllegalAccessError(String.format(
 				"BAD unguarded-access (%s -> %s)", o, Thread.currentThread()));
 	}
 
 	public static void release(Object o){
 		if(o == null)
 			return;
 
 		Thread ct = Thread.currentThread();
 
 		WeakReference<Thread> ref = roOwners.get(o);
 		if(ref != null && ref.get() != null)
 			throw new IllegalAccessError(
 				String.format("BAD ro-release (%s <- %s)", o, ct));
 
 		ref = owners.get(o);
 		if(ref == null)
 			throw new IllegalAccessError(
 				String.format("BAD release-unowned (%s <- %s)", o, ct));
 
 		Thread owner = ref.get();
 		if(owner == null){
 //			System.err.printf("OK release-again (%s <- %s)", o, ct);
 			return;
 		}
 
 		if(owner.equals(ct)){
 			owners.remove(o);
 //			System.err.printf("OK release (%s <- %s)\n", o, ct);
 			return;
 		}
 
 		throw new IllegalAccessError(String.format("BAD release (%s <- %s)\n", o, ct));
 	}
 }
