 package com.hazelcast.annotation.exe;
 
 import java.io.Serializable;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 
 import com.hazelcast.annotation.*;
 
 import java.util.concurrent.Future;
 
 import com.hazelcast.annotation.builder.HazelcastAnnotationBuilder;
 import com.hazelcast.config.Config;
 import com.hazelcast.core.Hazelcast;
 import com.hazelcast.core.HazelcastInstance;
 import com.hazelcast.core.IMap;
 import com.hazelcast.core.MultiMap;
 
 @Configuration(value="MyHazelcastInstance", port = 8888, autoIncrement = true, multicast = @Multicast)
 public class Main {
 	
 	@IExecutorService(corePoolSize = 2, keepAliveSeconds = 60, maxPoolSize = 5, name = "test-exec-srv")
 	static java.util.concurrent.ExecutorService executorService;
 	
 	@IQueue(backingMapRef = 5, maxSizePerJvm = 0, name = "testQueue")
 	static com.hazelcast.core.IQueue testQueue;
 	
 	@IQueue(backingMapRef = 5, maxSizePerJvm = 0, name = "testQueue2")
 	static com.hazelcast.core.IQueue testQueue2;
 	
 	@ISet(name = "testSet")
 	static com.hazelcast.core.ISet testSet;
 	
 	@ISet(name = "testSet2")
 	static com.hazelcast.core.ISet testSet2;
 	
 	@IList(name = "testList")
 	static com.hazelcast.core.IList testList;
 	
 	@IList(name = "testList2")
 	static com.hazelcast.core.IList testList2;
 	
 	public static void main(String[] args) {
		
		Config cfg = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
        
        HazelcastAnnotationBuilder.build("com.hazelcast.annotation");      
         
         testList.add("TestList 1 ");
         testList.add("TestList 2 ");
         testList.add("TestList 3 ");
         
         testList2.add("TestList2 4 ");
         testList2.add("TestList2 5 ");
         testList2.add("TestList2 6 ");
                 
         IMap<Integer, String> testMap = instance.getMap("testMap1");
 		testMap.put(1, "testMap1");
 		testMap.put(2, "testMap2");
 		
 		MultiMap<Integer, String> testMultiMap = instance.getMultiMap("testMultiMap1");
 		testMultiMap.put(1, "TestMultiMap1");
 		testMultiMap.put(1, "TestMultiMap2");
 		testMultiMap.put(2, "TestMultiMap3");
 		
 		testQueue.add("Test Queue");
         testQueue2.add("Test Queue2");
         
         testSet.add("Test Set");
         testSet2.add("Test Set2");
         
         
         Future<String> task = executorService.submit(new Echo("Eren"));
 		   try {
 			String echoResult = task.get();
 			System.out.println("RESULT : " + echoResult + " with " + executorService);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}		 
         
         Hazelcast.shutdownAll();       
         
 	}
 	
 	static class Echo implements Callable<String>, Serializable {
 	    String input = null;
 
 	    public Echo() {
 	    }
 
 	    public Echo(String input) {
 	        this.input = input;
 	    }
 
 	    public String call() {
 	        return Hazelcast.getCluster().getLocalMember().toString() + ":" + input;
 	    }
 	}
 }
