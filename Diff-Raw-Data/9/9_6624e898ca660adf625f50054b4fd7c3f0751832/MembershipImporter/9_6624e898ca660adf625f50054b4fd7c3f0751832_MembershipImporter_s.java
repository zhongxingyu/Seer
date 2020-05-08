 package org.yajug.users.bulkimport.importer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yajug.users.bulkimport.reader.DomainReader;
 import org.yajug.users.domain.Event;
 import org.yajug.users.domain.Member;
 import org.yajug.users.domain.Membership;
 import org.yajug.users.domain.utils.KeyValidator;
 import org.yajug.users.service.DataException;
 import org.yajug.users.service.EventService;
 import org.yajug.users.service.MemberService;
 
 import com.google.inject.Inject;
 
 /**
  * Importer for {@link Membership} 
  * 
  * @author Bertrand Chevrier <bertrand.chevrier@yajug.org>
  */
 public class MembershipImporter implements DomainImporter {
 
 	private final static Logger logger = LoggerFactory.getLogger(MembershipImporter.class);
 	
 	@Inject private DomainReader<Membership> reader;
 	@Inject private MemberService memberService;
 	@Inject private EventService eventService;
 	
 	@Override
 	public int doImport(String fileName) {
 		
 		int imported = 0;
 		
 		Collection<Membership> memberships = new ArrayList<>();
 		try {
 			
 			Collection<Event> events = eventService.getAll();
 			
 			memberships = reader.read(fileName);
 			for(Membership membership : memberships){
 				if(membership.getMember() != null){
 					//get member
 					Collection<Member> foundMembers = memberService.findAll(membership.getMember().getEmail());
 					if(foundMembers.size() == 0){
 						logger.warn("No members found for email " + membership.getMember().getEmail() + ", skipped.");
 					} else if(foundMembers.size() > 1){
 						logger.warn("Multiple members found for email " + membership.getMember().getEmail() + ", skipped.");
 					} else {
 						Member member = foundMembers.iterator().next();
						if(KeyValidator.validate(member.getKey())){
 							logger.warn("Invalid key " + member.getKey() + " for member " + membership.getMember().getEmail() + ", skipped.");
 						} else {
 							membership.setMember(member);
 						}
 					}
 					//get event if paiementDate match
 					for(Event event : events){
 						if(membership.getPaiementDate() != null && event.getDate() != null 
 								&& event.getDate().compareTo(membership.getPaiementDate()) == 0){
 							membership.setEvent(event);
 							break;
 						}
 					}
 				}
 			}
 			
 			if(memberService.saveMemberships(memberships)){
 				imported = memberships.size();
 			}
 			
 		} catch (IOException | DataException e) {
 			e.printStackTrace();
 		}
 
 		return imported;
 	}
 
 }
