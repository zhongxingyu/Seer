 package com.mycompany;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.transaction.UserTransaction;
 import org.slf4j.Logger;
 
// @javax.ejb.Stateful// Adding this annotation eliminates need for manual transaction demarcation
 @Model
 public class MemberRegistration
 {
    @Inject
    private Logger log;
 
    @Inject
    @MemberRepository
    private EntityManager em;
 
   @Inject
   private UserTransaction utx;
 
    @Inject
    private Event<Member> memberEventSrc;
    
    private Member newMember;
 
    @Produces
    @Named
    public Member getNewMember()
    {
       return newMember;
    }
 
    public void register() throws Exception
    {
       log.info("Registering " + newMember.getName());
       // UserTransaction only needed when bean is not an EJB
       utx.begin();
       em.joinTransaction();
       em.persist(newMember);
       utx.commit();
       memberEventSrc.fire(newMember);
       initNewMember();
    }
 
    @PostConstruct
    public void initNewMember()
    {
       newMember = new Member();
    }
 }
