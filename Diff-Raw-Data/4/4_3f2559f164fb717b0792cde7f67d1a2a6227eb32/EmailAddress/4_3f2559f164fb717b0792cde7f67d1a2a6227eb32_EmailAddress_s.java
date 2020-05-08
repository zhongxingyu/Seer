 package module.contacts.domain;
 
import java.util.ArrayList;
 import java.util.List;
 
 import module.organization.domain.Party;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.groups.PersistentGroup;
 
 import org.joda.time.DateTime;
 
 import pt.ist.fenixWebFramework.services.Service;
 
 public class EmailAddress extends EmailAddress_Base {
 
     public EmailAddress(String emailAddress, Party party, Boolean defaultContact, PartyContactType partyContactType,
 	    List<PersistentGroup> visibilityGroups) {
 	super();
 
 	super.setVisibleTo(visibilityGroups);
 
 	super.setValue(emailAddress);
 	super.setParty(party);
 	super.setDefaultContact(defaultContact);
 	super.setType(partyContactType);
 
 	super.setLastModifiedDate(new DateTime());
     }
 
     /**
      * Creates, returns and associates an EmailAddress with the given party
      * 
      * @param emailAddress the string with the email value e.g.
      *            johndoe@nonexisting.com
      * @param party the party to which the contact is associated
      * @param defaultContact if this is the default contact of the given party
      * @param partyContactType the partytype, see {@link PartyContactType}
      * @param visibilityGroups the visibility groups to which this contact is
      *            going to be visible to.
      * @return an EmailAddress with the given parameters
      */
     @Service
     public static EmailAddress createNewEmailAddress(String emailAddress, Party party, Boolean defaultContact,
	    PartyContactType partyContactType, User userCreatingTheContact, ArrayList<PersistentGroup> visibilityGroups) {
 	// validate that the user can actually create this contact
 	validateUser(userCreatingTheContact, party, partyContactType);
 
 	// making sure the list of visibility groups is a valid one
 	validateVisibilityGroups(visibilityGroups);
 
 	// make sure that this isn't a duplicate contact for this party
 	for (PartyContact partyContact : party.getPartyContacts()) {
 	    if (partyContact instanceof EmailAddress && partyContact.getValue() == emailAddress
 		    && partyContactType.equals(partyContact.getType())) {
 		throw new DomainException("error.duplicate.partyContact");
 	    }
 	}
 
 	return new EmailAddress(emailAddress, party, defaultContact, partyContactType, visibilityGroups);
     }
 
     @Override
     public String getDescription() {
 	return getValue();
     }
 
 }
