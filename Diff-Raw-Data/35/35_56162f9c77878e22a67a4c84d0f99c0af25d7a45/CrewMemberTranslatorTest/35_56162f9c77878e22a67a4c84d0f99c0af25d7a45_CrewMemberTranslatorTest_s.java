 package org.atlasapi.persistence.media.entity;
 
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import org.atlasapi.media.entity.Actor;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.CrewMember.Role;
 import org.atlasapi.media.entity.Publisher;
 import org.junit.Test;
 
 import com.mongodb.DBObject;
 
 public class CrewMemberTranslatorTest {
 
     private final CrewMemberTranslator translator = new CrewMemberTranslator();
     
     @Test
     public void testEncodesAndDecodesCrewMember() {
         CrewMember member = new CrewMember("uri","curie",Publisher.PA)
             .withName("name")
             .withProfileLink("profileLink")
             .withRole(Role.ABRIDGED_BY);
 
         DBObject encoded = translator.toDBObject(null, member);
         
         CrewMember decoded = translator.fromDBObject(encoded, null);
         
         assertThat(decoded.getCanonicalUri(), is(member.getCanonicalUri()));
         assertThat(decoded.getCurie(), is(member.getCurie()));
         assertThat(decoded.publisher(), is(member.publisher()));
         assertThat(decoded.name(), is(member.name()));
         assertThat(decoded.profileLinks(), is(member.profileLinks()));
         assertThat(decoded.role(), is(member.role()));
         
     }
 
     @Test
     public void testEncodesAndDecodesCrewMemberWithAbsentRole() {
         CrewMember member = new CrewMember("uri","curie",Publisher.PA)
         .withName("name")
         .withProfileLink("profileLink");
         
         DBObject encoded = translator.toDBObject(null, member);
         
         CrewMember decoded = translator.fromDBObject(encoded, null);
         
         assertThat(decoded.getCanonicalUri(), is(member.getCanonicalUri()));
         assertThat(decoded.getCurie(), is(member.getCurie()));
         assertThat(decoded.publisher(), is(member.publisher()));
         assertThat(decoded.name(), is(member.name()));
         assertThat(decoded.profileLinks(), is(member.profileLinks()));
         assertThat(decoded.role(), is(nullValue()));
         assertThat(decoded, is(not(instanceOf(Actor.class))));
     }
 
     @Test
     public void testEncodesAndDecodesActor() {
         Actor member = new Actor("uri","curie",Publisher.PA);
         member
             .withName("name")
             .withProfileLink("profileLink")
             .withCharacter("character");
         
         DBObject encoded = translator.toDBObject(null, member);
         
        CrewMember decoded = translator.fromDBObject(encoded, null);
         
        assertThat(decoded, is(Actor.class));
         assertThat(decoded.getCanonicalUri(), is(member.getCanonicalUri()));
         assertThat(decoded.getCurie(), is(member.getCurie()));
         assertThat(decoded.publisher(), is(member.publisher()));
         assertThat(decoded.name(), is(member.name()));
         assertThat(decoded.profileLinks(), is(member.profileLinks()));
         assertThat(decoded.role(), is(Role.ACTOR));
         assertThat(((Actor)decoded).character(), is(member.character()));
         
     }
 
 }
