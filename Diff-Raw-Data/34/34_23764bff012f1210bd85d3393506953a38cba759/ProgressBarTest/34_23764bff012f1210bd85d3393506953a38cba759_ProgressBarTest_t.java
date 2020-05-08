 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class ProgressBarTest extends InsertClass{
     String project_id;
     String xpathProgressBar ="(//div[@class='projectProgressInfo']/table/tbody/tr/th)";
     String xpathThumbnail = "(//div[@class='eachProject'][last()]/div[@class='projectSummary']/div[@class='thumbnailProgress']/table/tbody/tr/th)";
 
     @Test
     public void initialZeroState(){
         project_id = insertDataForCurrentProject("Checking initial state","everything should have 0","image/images.jpeg","image/children_thumbnail.png","abcdefghij","2012-12-12","2000");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[1]")).getText(), is("2,000\nTarget Number"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[2]")).getText(), is("0\nPledged"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[3]")).getText(), is(daysBetween()+"\nDays Left"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[4]/progress[@value='0']")).isDisplayed() , is(true));
     }
 
     @Test
     public void firstDonation() {
        project_id = insertDataForCurrentProject("First Donation","everything should have 0","image/images.jpeg","image/children_thumbnail.png","abcdefghij","2012-12-12","2000");
         insertDonation(project_id,"950");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[1]")).getText(), is("2,000\nTarget Number"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[2]")).getText(), is("950\nPledged"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[3]")).getText(), is(daysBetween()+"\nDays Left"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[4]/progress[@value='"+percent(950,2000)+"']")).isDisplayed() , is(true));
     }
 
     @Test
     public void multipleDonations() {
        project_id = insertDataForCurrentProject("Multiple Donation","everything should have 0","image/images.jpeg","image/children_thumbnail.png","abcdefghij","2012-12-12","2000");
         insertDonation(project_id,"950");
         insertDonation(project_id,"200");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[1]")).getText(), is("2,000\nTarget Number"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[2]")).getText(), is("1,150\nPledged"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[3]")).getText(), is(daysBetween()+"\nDays Left"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[4]/progress[@value='"+percent(1150,2000)+"']")).isDisplayed() , is(true));
     }
 
     @Test
     public void excessDonation() {
        project_id = insertDataForCurrentProject("Excess Donation","everything should have 0","image/images.jpeg","image/children_thumbnail.png","abcdefghij","2012-12-12","2000");
         insertDonation(project_id,"2110");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[1]")).getText(), is("2,000\nTarget Number"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[2]")).getText(), is("2,110\nPledged"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[3]")).getText(), is(daysBetween()+"\nDays Left"));
         assertThat(webDriver.findElement(By.xpath(xpathProgressBar+"[4]/progress[@value='"+percent(2110,2000)+"']")).isDisplayed() , is(true));
     }
 
     @Test
     public void refreshProgressBar(){
        project_id = insertDataForCurrentProject("Refresh Progress Bar","everything should have 0","image/images.jpeg","image/children_thumbnail.png","abcdefghij","2012-12-12","2000");
         insertDonation(project_id,"950");
         WebDriver webDriver1 = new FirefoxDriver();
         webDriver1.get("http://10.10.4.121:8080/Donor-Connect-App/project?id="+project_id);
         insertDonation(project_id,"200");
         webDriver1.navigate().refresh();
         assertThat(webDriver1.findElement(By.xpath(xpathProgressBar+"[1]")).getText(), is("2,000\nTarget Number"));
         assertThat(webDriver1.findElement(By.xpath(xpathProgressBar+"[2]")).getText(), is("1,150\nPledged"));
         assertThat(webDriver1.findElement(By.xpath(xpathProgressBar+"[3]")).getText(), is(daysBetween()+"\nDays Left"));
         assertThat(webDriver1.findElement(By.xpath(xpathProgressBar+"[4]/progress[@value='"+percent(1150,2000)+"']")).isDisplayed() , is(true));
         webDriver1.close();
     }
 
     @Test
     public void allProjectsProgressBar() {
        project_id = insertDataForCurrentProject("All Projects Progress Bar","everything should have 0","image/images.jpeg","image/children_thumbnail.png","abcdefghij","2012-12-12","2000");
         insertDonation(project_id,"950");
         webDriver.get("http://10.10.4.121:8080/Donor-Connect-App/projects");
         assertThat(webDriver.findElement(By.xpath("//div[@class='eachProject'][last()]/div[@class='projectSummary']/div[@class='thumbnailProgress']/progress[@value='"+percent(950,2000)+"']")).isDisplayed() , is(true));
         assertThat(webDriver.findElement(By.xpath(xpathThumbnail+"[1]")).getText(), is("Target: 2,000"));
         assertThat(webDriver.findElement(By.xpath(xpathThumbnail+"[2]")).getText(), is("Pledged: 950"));
         assertThat(webDriver.findElement(By.xpath(xpathThumbnail+"[3]")).getText(), is("Days: "+daysBetween()));
     }
 
     public double percent(int amount,int target){
         double multi = (amount * 100);
         double percent = multi/target;
         return percent;
     }
 
     public int daysBetween(){
         Date date = new Date();
         Calendar cal1 = new GregorianCalendar();
         Calendar cal2 = new GregorianCalendar();
         Date d1=cal1.getTime();
         Date d2=cal2.getTime();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         try {
             date = sdf.parse(sdf.format(date));
             cal1.setTime(date);
             date = sdf.parse("2012-12-12");
             cal2.setTime(date);
             d1 = cal1.getTime();
             d2 = cal2.getTime();
         } catch (ParseException e) {
             e.printStackTrace();
         }
         return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
     }
 }
