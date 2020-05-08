 package org.atlasapi.output.simple;
 
 import java.util.Set;
 
 import org.atlasapi.application.ApplicationConfiguration;
 import org.atlasapi.media.entity.Actor;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.simple.Person;
 import org.atlasapi.output.Annotation;
 
 public class CrewMemberSimplifier extends IdentifiedModelSimplifier<CrewMember,Person> {
 
     public Person simplify(CrewMember fullCrew, Set<Annotation> annotations, ApplicationConfiguration config) {
         Person person = new Person();
         
         person.setType(Person.class.getSimpleName());
         copyIdentifiedAttributesTo(fullCrew, person, annotations);
 
         if (fullCrew instanceof Actor) {
             Actor fullActor = (Actor) fullCrew;
             person.setCharacter(fullActor.character());
         }
         
         person.setName(fullCrew.name());
         person.setProfileLinks(fullCrew.profileLinks());
        person.setRole(fullCrew.role().key());
         
         return person;
     }
 
 }
