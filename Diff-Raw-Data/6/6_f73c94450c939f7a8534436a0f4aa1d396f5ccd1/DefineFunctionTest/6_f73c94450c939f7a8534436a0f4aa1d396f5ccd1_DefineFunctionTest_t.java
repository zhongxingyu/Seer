 package org.caruana.silverbirch.server.statements;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.caruana.silverbirch.server.ConnectionImpl;
 import org.caruana.silverbirch.server.SilverBirchImpl;
 import org.caruana.silverbirch.server.TransactionImpl;
 import org.caruana.silverbirch.statements.util.DefineFunction;
 import org.caruana.silverbirch.statements.util.EDN;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.profiler.Profiler;
 
 public class DefineFunctionTest {
 
     private static Logger logger = LoggerFactory.getLogger(DefineFunctionTest.class);
 
     private String repo = "mem://repo_" + System.currentTimeMillis();
     private Profiler profiler;
     private ConnectionImpl conn;
     private TransactionImpl transaction;
 
     @Before
     public void initProfiler()
     {
         profiler = new Profiler("DefineFunctionTest");
         profiler.setLogger(logger);
     }
     
     @Before
     public void initTransaction()
     {
         SilverBirchImpl silverbirch = new SilverBirchImpl();
         boolean created = silverbirch.createRepo(repo);
         assertTrue(created);
         conn = silverbirch.internalConnect(repo);
         assertNotNull(conn);
         transaction = conn.getTransaction();
         assertNotNull(transaction);
     }
     
     @Test
     public void defineFunction()
     {
         profiler.start("createFunction");
         DefineFunction fn = new DefineFunction("test", new String[] {"db", "name"}, "/statements/test_fn.edn");
         transaction.addStatement(fn);
        profiler.start("applyChanges");
        transaction.applyChanges(conn);
         profiler.start("createInvoke");
         EDN invoke = new EDN("/statements/test_fn_invoke.edn");
         transaction.addStatement(invoke);
         profiler.start("applyChanges");
         transaction.applyChanges(conn);
        transaction.addStatement(invoke);
        profiler.start("applyChanges");
        transaction.applyChanges(conn);
         profiler.stop().log();
     }
 
 }
