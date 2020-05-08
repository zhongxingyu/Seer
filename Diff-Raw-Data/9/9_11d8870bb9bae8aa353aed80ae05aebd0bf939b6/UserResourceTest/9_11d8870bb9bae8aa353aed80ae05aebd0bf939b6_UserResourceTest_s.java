 package org.bloodtorrent.resources;
 
 import org.bloodtorrent.dto.User;
 import org.bloodtorrent.repository.UsersRepository;
 import org.junit.Test;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Set;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 14
  * Time: 오후 1:56
  * To change this template use File | Settings | File Templates.
  */
 public class UserResourceTest {
 
 
     private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
     private final Validator validator = factory.getValidator();
 
     private User createNewUser() {
         User user = new User();
         user.setId("bloodtorrent@naver.com");
         user.setPassword("password");
         user.setRole("donor");
         user.setFirstName("Blood");
         user.setLastName("Torrent");
         user.setCellPhone("0123456789");
         user.setGender("male");
         user.setBloodGroup("A+");
         user.setAnonymous(false);
         user.setAddress("BR Mehta Ln");
         user.setCity("New Delhi");
         user.setState("Delhi");
         user.setDistance("10");
         user.setBirthDay("18031980");
 
         return user;
     }
 
     @Test
     public void shouldCheckEmailDuplication(){
         UsersRepository usersRepository = mock(UsersRepository.class);
         when(usersRepository.get("bloodtorrent@naver.com")).thenReturn(createNewUser());
 
         User user = createNewUser();
         UsersResource usersResource = new UsersResource(usersRepository);
 
        assertThat(usersResource.isEmailDuplicated(user), is(true));
 
         user.setId("test@naver.com");
         assertThat(usersResource.isEmailDuplicated(user), is(false));
     }
 
     @Test
     public void shouldExistUserId() {
         User user = createNewUser();
         setDummyString(user, "id", 0);
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "id");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfUserIdShouldBetween5to100() {
         User user = createNewUser();
         setDummyString(user, "id", 4);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "id");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyString(user, "id", 101);
         constraintViolations = validator.validateProperty(user, "id");
         assertThat(1, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistFirstName() {
         User user = createNewUser();
         setDummyString(user, "firstName", 0);
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "firstName");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfFirstNameShouldUnder35() {
         User user = createNewUser();
         setDummyString(user, "firstName", 36);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "firstName");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyString(user, "firstName", 35);
         constraintViolations = validator.validateProperty(user, "firstName");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistLastName() {
         User user = createNewUser();
         setDummyString(user, "lastName", 0);
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "lastName");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfLastNameShouldUnder35() {
         User user = createNewUser();
         setDummyString(user, "lastName", 36);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "lastName");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyString(user, "lastName", 35);
         constraintViolations = validator.validateProperty(user, "lastName");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistPassword() {
         User user = createNewUser();
         setDummyString(user, "password", 0);
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "password");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfPasswordShouldBetween8to25() {
         User user = createNewUser();
         setDummyNumericString(user, "password", 7);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "password");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyNumericString(user, "password", 26);
         constraintViolations = validator.validateProperty(user, "password");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyNumericString(user, "password", 25);
         constraintViolations = validator.validateProperty(user, "password");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistOneDigitOfPassword() {
         User user = createNewUser();
         user.setPassword("assdd1sasds");
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "password");
         assertThat(0, is(constraintViolations.size()));
 
 
         user.setPassword("assddsasds");
         constraintViolations = validator.validateProperty(user, "password");
         assertThat(1, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistAddress() {
         User user = createNewUser();
         setDummyString(user, "address", 0);
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "address");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfAddressShouldUnder1000() {
         User user = createNewUser();
         setDummyString(user, "address", 1001);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "address");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyString(user, "address", 1000);
         constraintViolations = validator.validateProperty(user, "address");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistCity() {
         User user = createNewUser();
         setDummyString(user, "city", 0);
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "city");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfCityShouldUnder255() {
         User user = createNewUser();
         setDummyString(user, "city", 256);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "city");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyString(user, "city", 255);
         constraintViolations = validator.validateProperty(user, "city");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void sizeOfCellPhoneShouldOnly10() {
         User user = createNewUser();
         setDummyNumericString(user, "cellPhone", 11);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "cellPhone");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyNumericString(user, "cellPhone", 9);
         constraintViolations = validator.validateProperty(user, "cellPhone");
         assertThat(1, is(constraintViolations.size()));
 
         setDummyNumericString(user, "cellPhone", 10);
         constraintViolations = validator.validateProperty(user, "cellPhone");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldHasOnlyDigitOfCellPhone() {
         User user = createNewUser();
 
         setDummyNumericString(user, "cellPhone", 10);
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "cellPhone");
         assertThat(0, is(constraintViolations.size()));
 
         user.setCellPhone("012345678*");
         constraintViolations = validator.validateProperty(user, "cellPhone");
         assertThat(1, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistBloodGroup() {
         User user = createNewUser();
         user.setBloodGroup("");
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "bloodGroup");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldHasSpecificStringOfBloodGroup() {
         User user = createNewUser();
         user.setBloodGroup("A-");
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "bloodGroup");
         assertThat(0, is(constraintViolations.size()));
 
         user.setBloodGroup("C-");
         constraintViolations = validator.validateProperty(user, "bloodGroup");
         assertThat(1, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldExistDistance() {
         User user = createNewUser();
         user.setDistance("");
 
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "distance");
 
         assertThat(2, is(constraintViolations.size()));
     }
 
     @Test
     public void shouldDistanceHasSpecificNumber() {
         User user = createNewUser();
         user.setDistance("10");
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "distance");
         assertThat(0, is(constraintViolations.size()));
 
         user.setDistance("11");
         constraintViolations = validator.validateProperty(user, "distance");
         assertThat(1, is(constraintViolations.size()));
         assertThat("Please check distance", is(constraintViolations.iterator().next().getMessage()));
     }
 
     @Test
     public void shouldBirthDayMatchWithDateFormat() {
         User user = createNewUser();
         user.setBirthDay("11-04-1977");
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(0, is(constraintViolations.size()));
 
         user.setBirthDay("1333-19-99");
         constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(1, is(constraintViolations.size()));
 
         user.setBirthDay("13-19-1199");
         constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(1, is(constraintViolations.size()));
 
         user.setBirthDay("13/12/1999");
         constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(1, is(constraintViolations.size()));
 
         user.setBirthDay("11121981");
         constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(1, is(constraintViolations.size()));
 
         user.setBirthDay("");
         constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(0, is(constraintViolations.size()));
 
         user.setBirthDay(null);
         constraintViolations = validator.validateProperty(user, "birthDay");
         assertThat(0, is(constraintViolations.size()));
     }
 
     @Test
     public void stateNameShouldSpecific(){
         String stateNames[] = new String[]{"Andhra Pradesh" ,"Arunachal Pradesh" ,"Asom (Assam)" ,"Bihar" ,"Karnataka" ,"Kerala" ,"Chhattisgarh" ,"Goa" ,"Gujarat" ,"Haryana" ,"Himachal Pradesh" ,"Jammu And Kashmir" ,"Jharkhand" ,"West Bengal" ,"Madhya Pradesh" ,"Maharashtra" ,"Manipur" ,"Meghalaya" ,"Mizoram" ,"Nagaland" ,"Orissa" ,"Punjab" ,"Rajasthan" ,"Sikkim" ,"Tamilnadu" ,"Tripura" ,"Uttarakhand (Uttaranchal)" ,"Uttar Pradesh"};
 
         User user = createNewUser();
 
         for(String stateName : stateNames){
             user.setState(stateName);
             Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "state");
             assertThat(stateName + " is undefined", 0, is(constraintViolations.size()));
         }
 
         user.setState("");
         Set<ConstraintViolation<User>> constraintViolations = validator.validateProperty(user, "state");
         assertThat(3, is(constraintViolations.size()));
 
         user.setState(null);
         constraintViolations = validator.validateProperty(user, "state");
         assertThat(1, is(constraintViolations.size()));
     }
 
     private <T> void setDummyString(T t, String property, int num) {
         String dummyText = makeDummyString(num);
         invokeMethod(t, property, dummyText);
     }
 
     private <T> void setDummyNumericString(T t, String property, int num) {
         String dummyNumericText = makeDummyNumericString(num);
         invokeMethod(t, property, dummyNumericText);
     }
 
     private <T> void invokeMethod(T t, String property, String dummyText) {
         for (Method method : t.getClass().getMethods()) {
             String methodName = method.getName();
             if(methodName.startsWith("set") && methodName.toLowerCase().endsWith(property.toLowerCase())) {
                 try {
                     method.invoke(t, dummyText);
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private String makeDummyString(int num, String dummyChar) {
         StringBuilder stringBuilder = new StringBuilder();
         for(int i = 0 ; i < num ; i++){
             stringBuilder.append(dummyChar);
         }
         return stringBuilder.toString();
     }
 
     private String makeDummyString(int num) {
         return makeDummyString(num, "*");
     }
 
     private String makeDummyNumericString(int num) {
         return makeDummyString(num, "1");
     }
 
 }
