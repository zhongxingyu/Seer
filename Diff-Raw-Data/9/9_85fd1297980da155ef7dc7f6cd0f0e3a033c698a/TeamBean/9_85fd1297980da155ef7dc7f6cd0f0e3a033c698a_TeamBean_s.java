 package de.objectcode.time4u.server.web.ui.admin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.objectcode.time4u.server.ejb.seam.api.IPersonServiceLocal;
 import de.objectcode.time4u.server.entities.PersonEntity;
 import de.objectcode.time4u.server.entities.TeamEntity;
 
 public class TeamBean
 {
   TeamEntity m_team;
   List<String> m_owners;
   List<String> m_members;
 
   public TeamBean(final TeamEntity team)
   {
     m_team = team;
     m_owners = new ArrayList<String>();
     for (final PersonEntity owner : m_team.getOwners()) {
      m_owners.add(owner.getId());
     }
     m_members = new ArrayList<String>();
     for (final PersonEntity member : m_team.getMembers()) {
      m_members.add(member.getId());
     }
   }
 
   public List<String> getOwners()
   {
     return m_owners;
   }
 
   public List<String> getMembers()
   {
     return m_members;
   }
 
   public void setOwners(final List<String> owners)
   {
     m_owners = owners;
   }
 
   public void setMembers(final List<String> members)
   {
     m_members = members;
   }
 
   public TeamEntity getTeam()
   {
     return m_team;
   }
 
   public String getDescription()
   {
     return m_team.getDescription();
   }
 
   public String getId()
   {
     return m_team.getId();
   }
 
   public String getName()
   {
     return m_team.getName();
   }
 
   public void setDescription(final String description)
   {
     m_team.setDescription(description);
   }
 
   public void setName(final String name)
   {
     m_team.setName(name);
   }
 
   public void updateTeam(final IPersonServiceLocal personService)
   {
     m_team.getOwners().clear();
     for (final String id : m_owners) {
       m_team.getOwners().add(personService.getPerson(id));
     }
     m_team.getMembers().clear();
     for (final String id : m_members) {
       m_team.getMembers().add(personService.getPerson(id));
     }
   }
 }
