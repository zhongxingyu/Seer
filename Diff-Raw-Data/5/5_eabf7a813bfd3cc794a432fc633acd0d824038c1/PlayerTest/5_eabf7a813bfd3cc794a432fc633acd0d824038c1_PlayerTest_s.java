 package models;
 
 import static domainservices.ServiceLocator.randomHorsesBreeder;
 import static models.PlayerBuilder.PLAYER_ACCESS_TOKEN;
 import static models.PlayerBuilder.PLAYER_AUTH_METHOD;
 import static models.PlayerBuilder.PLAYER_AVATAR_URL;
 import static models.PlayerBuilder.PLAYER_DISPLAY_NAME;
 import static models.PlayerBuilder.PLAYER_EMAIL;
 import static models.PlayerBuilder.PLAYER_EMAIL_VERIFIED;
 import static models.PlayerBuilder.PLAYER_LAST_ACCESS;
 import static models.PlayerBuilder.PLAYER_PASSWORD_HASHED;
 import static models.PlayerBuilder.PLAYER_SECRET;
 import static models.PlayerBuilder.PLAYER_TOKEN;
 import static models.PlayerBuilder.PLAYER_USER_ID;
 import static models.PlayerBuilder.PLAYER_USER_PROVIDER_TYPE;
 import static models.PlayerBuilder.aPlayer;
 import static org.mockito.Mockito.when;
 
 import java.util.Arrays;
 import java.util.List;
 
 import litmus.unit.UnitTest;
 import models.stock.Food;
 
 import org.fest.assertions.Assertions;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 
 import domainservices.ServiceLocator;
 import domainservices.ServiceMocker;
 
 import securesocial.SocialUserFactory;
 import securesocial.provider.ProviderType;
 import securesocial.provider.SocialUser;
 import securesocial.provider.UserId;
 
 public class PlayerTest extends UnitTest {
 
 	@Rule
	public ServiceMocker serviceLocatorStubber = ServiceMocker.create();
 	
 	private Horse horse = new HorseBuilder().build();
 	
 	@Before
 	public void setUp() {
		serviceLocatorStubber.mockRandomHorseBreeder();
 
 		when(randomHorsesBreeder.createRandomHorse()).thenReturn(horse);
 	}
 	
     @Test
     public void create_ReturnsPlayer() {
         SocialUser socialUser = SocialUserFactory.create(aPlayer().build());
 
         Player actualPlayer = Player.create(socialUser);
 
         assertThat(actualPlayer.userId).isEqualTo(PLAYER_USER_ID);
         assertThat(actualPlayer.providerType).isEqualTo(PLAYER_USER_PROVIDER_TYPE);
         assertThat(actualPlayer.displayName).isEqualTo(PLAYER_DISPLAY_NAME);
         assertThat(actualPlayer.email).isEqualTo(PLAYER_EMAIL);
         assertThat(actualPlayer.avatarUrl).isEqualTo(PLAYER_AVATAR_URL);
         assertThat(actualPlayer.lastAccess).isEqualTo(PLAYER_LAST_ACCESS);
         assertThat(actualPlayer.authMethod).isEqualTo(PLAYER_AUTH_METHOD);
         assertThat(actualPlayer.token).isEqualTo(PLAYER_TOKEN);
         assertThat(actualPlayer.secret).isEqualTo(PLAYER_SECRET);
         assertThat(actualPlayer.accessToken).isEqualTo(PLAYER_ACCESS_TOKEN);
         assertThat(actualPlayer.password).isEqualTo(PLAYER_PASSWORD_HASHED);
         assertThat(actualPlayer.isEmailVerified).isEqualTo(PLAYER_EMAIL_VERIFIED);
     }
 
     @Test
     public void create_AddsOneHorseToSet() {
         SocialUser socialUser = SocialUserFactory.create(aPlayer().build());
 
         Player actualPlayer = Player.create(socialUser);
 
         Assertions.assertThat(actualPlayer.getHorses()).containsOnly(horse);
     }
     
 }
