 package br.com.caelum.auction.service;
 
 import br.com.caelum.auction.builder.AuctionBuilder;
 import br.com.caelum.auction.domain.Auction;
 import br.com.caelum.auction.infra.dao.AuctionRepository;
 import br.com.caelum.auction.infra.mail.MailSender;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InOrder;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.mockito.Mockito.*;
 
 /**
  * Unit test of {@link AuctionCloser}
  * User: helmedeiros
  * Date: 9/26/13
  * Time: 9:05 PM
  */
 public class AuctionCloserTest {
 
     public static final String VALID_AUCTION_NAME = "PLAYSTATION 3";
     public static final int SEVEN_DAYS_AGO = -7;
     public static final int FOURTEEN_DAYS_AGO = -14;
     public static final int ONE_DAY_AGO = -1;
     public static final int TWO_DAYS_AGO = -2;
     public static final boolean IS_NOT_CLOSED = false;
     public static final boolean IS_CLOSED = true;
     private MailSender postmanMock;
     private AuctionRepository mockDao;
 
     @Before
     public void setUp() throws Exception {
         mockDao = mock(AuctionRepository.class);
         postmanMock = mock(MailSender.class);
     }
 
     @Test public void shouldCloseAuctionsBeganMoreThanWeekAgo() throws Exception {
         AuctionRepository auctionDaoMock = mock(AuctionRepository.class);
 
         Calendar twoWeeksAgoDate = giveMeDateFrom(FOURTEEN_DAYS_AGO);
         Calendar oneWeekAgo = giveMeDateFrom(SEVEN_DAYS_AGO);
 
         final Auction openAuction1 = createAuctionAndAssertItIs(VALID_AUCTION_NAME, twoWeeksAgoDate, IS_NOT_CLOSED);
         final Auction openAuction2 = createAuctionAndAssertItIs(VALID_AUCTION_NAME, oneWeekAgo, IS_NOT_CLOSED);
 
         List<Auction> openAuctionList = Arrays.asList(openAuction1, openAuction2);
         when(auctionDaoMock.actuals()).thenReturn(openAuctionList);
 
         final AuctionCloser auctionCloser = new AuctionCloser(auctionDaoMock, postmanMock);
 
         auctionCloser.close();
 
         assertThat(auctionCloser.getClosedTotal(), equalTo(2));
         assertThat(openAuction1.isClosed(), equalTo(IS_CLOSED));
         assertThat(openAuction2.isClosed(), equalTo(IS_CLOSED));
     }
 
     @Test public void shouldNotCloseAuctionsBeganLessThanWeekAgo() throws Exception {
 
         final Calendar yesterday = giveMeDateFrom(ONE_DAY_AGO);
         final Calendar beforeYesterday = giveMeDateFrom(TWO_DAYS_AGO);
 
         final Auction openAuctionFromYesterday = createAuctionAndAssertItIs(VALID_AUCTION_NAME, yesterday, IS_NOT_CLOSED);
         final Auction openAuctionFromBeforeYesterday = createAuctionAndAssertItIs(VALID_AUCTION_NAME, beforeYesterday, IS_NOT_CLOSED);
 
         AuctionRepository mockDao = mock(AuctionRepository.class);
         when(mockDao.actuals()).thenReturn(Arrays.asList(openAuctionFromYesterday, openAuctionFromBeforeYesterday));
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDao, postmanMock);
         auctionCloser.close();
 
         assertThat(auctionCloser.getClosedTotal(), equalTo(0));
         assertThat(openAuctionFromYesterday.isClosed(), equalTo(IS_NOT_CLOSED));
         assertThat(openAuctionFromBeforeYesterday.isClosed(), equalTo(IS_NOT_CLOSED));
 
         verify(mockDao, never()).update(openAuctionFromYesterday);
         verify(mockDao, never()).update(openAuctionFromBeforeYesterday);
     }
 
     @Test public void shouldDoNothingWhenNoOpenAuctionExists() throws Exception {
         final AuctionRepository mockDAO = mock(AuctionRepository.class);
         when(mockDAO.actuals()).thenReturn(new ArrayList<Auction>());
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDAO, postmanMock);
         auctionCloser.close();
 
         assertThat(auctionCloser.getClosedTotal(), equalTo(0));
     }
 
     @Test public void shouldUpdateTheOpenAuctionAfterClosedIt() throws Exception {
         final Auction openAuctionFromLastWeek = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(SEVEN_DAYS_AGO), IS_NOT_CLOSED);
         final Auction openAuctionFromYesterday = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(ONE_DAY_AGO), IS_NOT_CLOSED);
         final Auction openAuctionFromTwoWeeksAgo = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(FOURTEEN_DAYS_AGO), IS_NOT_CLOSED);
 
         when(mockDao.actuals()).thenReturn(Arrays.asList(openAuctionFromLastWeek, openAuctionFromYesterday, openAuctionFromTwoWeeksAgo));
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDao, postmanMock);
         auctionCloser.close();
 
         verify(mockDao, times(1)).update(openAuctionFromLastWeek);
         verify(mockDao, times(1)).update(openAuctionFromTwoWeeksAgo);
     }
 
     @Test public void shouldSendAnAuctionAfterClosedIt() throws Exception {
         final Auction openAuctionFromLastWeek = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(SEVEN_DAYS_AGO), IS_NOT_CLOSED);
 
         when(mockDao.actuals()).thenReturn(Arrays.asList(openAuctionFromLastWeek));
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDao, postmanMock);
         auctionCloser.close();
 
         verify(postmanMock).send(openAuctionFromLastWeek);
     }
 
     @Test public void shouldSendAnAuctionOnlyAfterItHasBeenUpdated() throws Exception {
         final Auction openAuctionFromLastWeek = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(SEVEN_DAYS_AGO), IS_NOT_CLOSED);
 
         when(mockDao.actuals()).thenReturn(Arrays.asList(openAuctionFromLastWeek));
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDao, postmanMock);
         auctionCloser.close();
 
         final InOrder inOrder = inOrder(mockDao, postmanMock);
         inOrder.verify(mockDao, times(1)).update(openAuctionFromLastWeek);
         inOrder.verify(postmanMock).send(openAuctionFromLastWeek);
     }
 
     @Test public void shouldProcessNextAuctionEvenWhenErrorOccursInThePreviousUpdate() throws Exception {
         final Auction openAuctionFromLastWeek = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(SEVEN_DAYS_AGO), IS_NOT_CLOSED);
         final Auction openAuctionFromTwoWeeksAgo = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(FOURTEEN_DAYS_AGO), IS_NOT_CLOSED);
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDao, postmanMock);
 
         when(mockDao.actuals()).thenReturn(Arrays.asList(openAuctionFromTwoWeeksAgo, openAuctionFromLastWeek));
        doThrow(new IllegalStateException()).when(mockDao).update(openAuctionFromTwoWeeksAgo);
 
         auctionCloser.close();
 
         verify(postmanMock, never()).send(openAuctionFromTwoWeeksAgo);
         verify(mockDao).update(openAuctionFromLastWeek);
         verify(postmanMock).send(openAuctionFromLastWeek);
     }
 
     @Test public void shouldProcessNextAuctionEvenWhenErrorOccursInThePreviousMailSend() throws Exception {
         final Auction openAuctionFromLastWeek = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(SEVEN_DAYS_AGO), IS_NOT_CLOSED);
         final Auction openAuctionFromTwoWeeksAgo = createAuctionAndAssertItIs(VALID_AUCTION_NAME, giveMeDateFrom(FOURTEEN_DAYS_AGO), IS_NOT_CLOSED);
 
         final AuctionCloser auctionCloser = new AuctionCloser(mockDao, postmanMock);
 
         when(mockDao.actuals()).thenReturn(Arrays.asList(openAuctionFromTwoWeeksAgo, openAuctionFromLastWeek));
        doThrow(new IllegalStateException()).when(postmanMock).send(openAuctionFromTwoWeeksAgo);
 
         auctionCloser.close();
 
         verify(mockDao).update(openAuctionFromTwoWeeksAgo);
         verify(mockDao).update(openAuctionFromLastWeek);
         verify(postmanMock).send(openAuctionFromLastWeek);
     }
 
     private Auction createAuctionAndAssertItIs(final String auctionName, final Calendar onDate, final boolean closed) {
         final Auction openAuctionFromYesterday = new AuctionBuilder().to(auctionName).onDate(onDate).build();
         assertThat(openAuctionFromYesterday.isClosed(), equalTo(closed));
         return openAuctionFromYesterday;
     }
 
     private Calendar giveMeDateFrom(final int dayAgo) {
         final Calendar expectedDate = Calendar.getInstance(); expectedDate.add(Calendar.DAY_OF_MONTH, dayAgo);
         return expectedDate;
     }
 }
