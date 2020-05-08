 import junit.framework.JUnit4TestAdapter;
 import junit.framework.TestCase;
import org.jai.BSON.BSONDocument;
import org.jai.BSON.BSONSerializable;
import org.jai.BSON.BSONSerializer;
 
 
 public class BSONSerializerTests extends TestCase {
     private Human humanA;
     private BSONDocument serializedHumanA;
 
     public static junit.framework.Test suite() {
         return new JUnit4TestAdapter(BSONSerializerTests.class);
     }
 
     public void setUp() {
         Human humanB = new Human(1, 1, false, "Sue", (byte) 50, 'A', null);
        humanA = new Human(0, 19, true, "Mr. Smith", (byte) 180, 'A', humanB);
 
         serializedHumanA = BSONSerializer.serialize(humanA);
     }
 
     public void tearDown() {
     }
 
     public void testSerialize() {
         int id = (int) serializedHumanA.get("id");
         int age = (int) serializedHumanA.get("age");
         boolean gender = (boolean) serializedHumanA.get("gender");
         String name = (String) serializedHumanA.get("name");
         byte height = (byte) serializedHumanA.get("height");
         char healthGroup = (char) serializedHumanA.get("healthGroup");
 
         assertEquals("Unexpected id", humanA.getId(), id);
         assertEquals("Unexpected age", humanA.getAge(), (Integer) age);
         assertEquals("Unexpected gender", humanA.getGender(), gender);
         assertEquals("Unexpected name", humanA.getName(), name);
         assertEquals("Unexpected height", humanA.getHeight(), height);
         assertEquals("Unexpected healthGroup", humanA.getHealthGroup(), healthGroup);
     }
 
     public void testDeserialize() {
        BSONDocument serializedHumanA = BSONSerializer.serialize(humanA);
        Human deserializedHumanA = BSONSerializer.deserialize(Human.class, serializedHumanA);
         checkEquality(humanA, deserializedHumanA);
     }
 
     private <T extends Human> void checkEquality(T a, T b) {
         if (a == null || b == null) {
             assertTrue("Unexpected human value", a == null && b == null);
             return;
         }
 
         assertEquals("Unexpected id", a.getId(), b.getId());
         assertEquals("Unexpected age", a.getAge(), b.getAge());
         assertEquals("Unexpected gender", a.getGender(), b.getGender());
         assertEquals("Unexpected name", a.getName(), b.getName());
         assertEquals("Unexpected height", a.getHeight(), b.getHeight());
         assertEquals("Unexpected health group", a.getHealthGroup(), b.getHealthGroup());
         checkEquality(a.getChild(), b.getChild());
     }
 
     class Human {
         @BSONSerializable
         private final int id;
         @BSONSerializable
         private Integer age;
         @BSONSerializable
         private boolean gender;
         @BSONSerializable
         private String name;
         @BSONSerializable
         private byte height;
         @BSONSerializable
         private char healthGroup;
         @BSONSerializable
         private Human child;
 
         Human(int id, Integer age, boolean gender, String name, byte height, char healthGroup, Human child) {
             this.id = id;
             this.age = age;
             this.gender = gender;
             this.name = name;
             this.height = height;
             this.healthGroup = healthGroup;
             this.child = child;
         }
 
         public Human getChild() {
             return child;
         }
 
         public int getId() {
             return id;
         }
 
         public Integer getAge() {
             return age;
         }
 
         public boolean getGender() {
             return gender;
         }
 
         public String getName() {
             return name;
         }
 
         public byte getHeight() {
             return height;
         }
 
         public char getHealthGroup() {
             return healthGroup;
         }
     }
 }
