 package net.sf.laja.cdd.state;
 
 import net.sf.laja.cdd.testgen.state.AddressState;
 import net.sf.laja.cdd.validator.ValidationErrors;
 import org.junit.Test;
 
 import java.util.Iterator;
 
 import static net.sf.laja.cdd.testgen.HairColor.BROWN;
 import static net.sf.laja.cdd.testgen.PersonCreator.buildPerson;
 import static net.sf.laja.cdd.testgen.PersonCreator.createPerson;
 import static net.sf.laja.cdd.testgen.state.PersonState.PersonMutableState;
 import static net.sf.laja.cdd.validator.ValidationErrors.ErrorType.NULL;
 import static net.sf.laja.cdd.validator.ValidationErrors.ValidationError;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class PersonMutableStateTest {
 
     @Test
     public void shouldNotBeValidIfValidatingWithMissingAddress() {
         AddressState.AddressMutableState address = null;
         PersonMutableState state = buildPerson().withAddress(address).asMutableState();
 
         assertThat(state.isValid(), is(false));
     }
 
     @Test
     public void shouldReturnIsNullValidationErrors() {
         PersonMutableState mutableState = buildPerson().withName(null).asMutableState();
         ValidationErrors errors = mutableState.validate();
 
         ValidationErrors expectedErrors = ValidationErrors.builder()
                 .addIsNullError(mutableState, "name", "")
                 .addIsNullError(mutableState, "hairColor", "")
                 .addIsNullError(mutableState, "address", "")
                .addIsNullError(mutableState, "children", "")
                .addIsNullError(mutableState, "groupedAddresses", "")
                .addIsNullError(mutableState, "listOfSetOfMapOfIntegers", "")
                 .build();
 
         assertThat(errors, equalTo(expectedErrors));
     }
 
     @Test
     public void invalidCollectionShouldReturnNullValidationErrors() {
         PersonMutableState mutableState = createPerson().name(null).hairColor(BROWN).children(
                 createPerson().name(null).hairColor(BROWN).children().defaults(),
                 createPerson().name(null).hairColor(BROWN).children().defaults()
         ).defaults().asMutableState();
 
         ValidationErrors errors = mutableState.validate();
         Iterator<ValidationError> iterator = errors.iterator();
 
         assertThat(errors.size(), is(2));
         assertThat(iterator.next(), equalTo(new ValidationError("name", NULL, "Attribute 'name' can not be NULL", mutableState)));
         assertThat(iterator.next(), equalTo(new ValidationError("children.name", NULL, "Attribute 'children.name' can not be NULL", mutableState)));
     }
 }
