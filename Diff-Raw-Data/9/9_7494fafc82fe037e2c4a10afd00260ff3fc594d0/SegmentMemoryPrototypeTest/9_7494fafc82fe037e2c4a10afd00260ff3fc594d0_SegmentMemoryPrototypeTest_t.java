 package org.drools.compiler.integrationtests;
 
 import org.drools.core.impl.InternalKnowledgeBase;
import org.junit.Ignore;
 import org.junit.Test;
 import org.kie.api.io.ResourceType;
 import org.kie.api.runtime.KieSession;
 import org.kie.api.runtime.rule.FactHandle;
 import org.kie.internal.KnowledgeBaseFactory;
 import org.kie.internal.builder.KnowledgeBuilder;
 import org.kie.internal.builder.KnowledgeBuilderFactory;
 import org.kie.internal.io.ResourceFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.drools.compiler.integrationtests.DynamicRulesChangesTest.*;
 
@Ignore
 public class SegmentMemoryPrototypeTest {
     private static final String DRL =
             "import " +  DynamicRulesChangesTest.class.getCanonicalName() + "\n " +
             "global java.util.List events\n" +
             "rule \"Raise the alarm when we have one or more fires\"\n" +
             "when\n" +
             "    exists DynamicRulesChangesTest.Fire()\n" +
             "then\n" +
             "    insert( new DynamicRulesChangesTest.Alarm() );\n" +
             "    events.add( \"Raise the alarm\" );\n" +
             "end" +
             "\n" +
             "rule \"When there is a fire turn on the sprinkler\"\n" +
             "when\n" +
             "    $fire: DynamicRulesChangesTest.Fire($room : room)\n" +
             "    $sprinkler : DynamicRulesChangesTest.Sprinkler( room == $room, on == false )\n" +
             "then\n" +
             "    modify( $sprinkler ) { setOn( true ) };\n" +
             "    events.add( \"Turn on the sprinkler for room \" + $room.getName() );\n" +
             "end" +
             "\n" +
             "rule \"When the fire is gone turn off the sprinkler\"\n" +
             "when\n" +
             "    $room : DynamicRulesChangesTest.Room( )\n" +
             "    $sprinkler : DynamicRulesChangesTest.Sprinkler( room == $room, on == true )\n" +
             "    not DynamicRulesChangesTest.Fire( room == $room )\n" +
             "then\n" +
             "    modify( $sprinkler ) { setOn( false ) };\n" +
             "    events.add( \"Turn off the sprinkler for room \" + $room.getName() );\n" +
             "end" +
             "\n" +
             "rule \"Cancel the alarm when all the fires have gone\"\n" +
             "when\n" +
             "    not DynamicRulesChangesTest.Fire()\n" +
             "    $alarm : DynamicRulesChangesTest.Alarm()\n" +
             "then\n" +
             "    retract( $alarm );\n" +
             "    events.add( \"Cancel the alarm\" );\n" +
             "end" +
             "\n" +
             "rule \"Status output when things are ok\"\n" +
             "when\n" +
             "    not DynamicRulesChangesTest.Fire()\n" +
             "    not DynamicRulesChangesTest.Alarm()\n" +
             "    not DynamicRulesChangesTest.Sprinkler( on == true )\n" +
             "then\n" +
             "    events.add( \"Everything is ok\" );\n" +
             "end";
 
     @Test
     public void testSegmentMemoryPrototype() throws Exception {
         KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
         kbuilder.add( ResourceFactory.newByteArrayResource(DRL.getBytes()),
                       ResourceType.DRL );
 
         InternalKnowledgeBase kbase = (InternalKnowledgeBase) KnowledgeBaseFactory.newKnowledgeBase();
         kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
 
         KieSession ksession = kbase.newKieSession();
         checkKieSession(ksession);
 
         // Create a 2nd KieSession (that will use segment memory prototype) and check that it works as the former one
         KieSession ksession2 = kbase.newKieSession();
         checkKieSession(ksession2);
     }
 
     private void checkKieSession(KieSession ksession) {
         final List<String> events = new ArrayList<String>();
 
         ksession.setGlobal("events", events);
 
         // phase 1
         Room room1 = new Room("Room 1");
         ksession.insert(room1);
         FactHandle fireFact1 = ksession.insert(new Fire(room1));
         ksession.fireAllRules();
         assertEquals(1, events.size());
 
         // phase 2
         Sprinkler sprinkler1 = new Sprinkler(room1);
         ksession.insert(sprinkler1);
         ksession.fireAllRules();
         assertEquals(2, events.size());
 
         // phase 3
         ksession.delete(fireFact1);
         ksession.fireAllRules();
         assertEquals(5, events.size());
     }
 }
