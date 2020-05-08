 package pl.itcrowd.summer_code.test;
 
 import org.jboss.arquillian.graphene.enricher.findby.FindBy;
 import org.openqa.selenium.WebElement;
 
 import java.util.List;
 
 import static org.jboss.arquillian.graphene.Graphene.guardHttp;
 
 public class SearchResults {
 
     @FindBy(css = "input.searchTemplate")
     private WebElement searchInput;
 
     @FindBy(css = "#searchBtn")
     private WebElement searchButton;
 
    @FindBy(css = "#wrap div:nth:of:type(2) div:nth-of-type(2) > h2")
     private WebElement psychiscNoResultsFound;
 
    @FindBy(css = "#wrap div:nth:of:type(3) div:nth-of-type(1) > h2")
     private WebElement marketplaceNoResultsFound;
 
     @FindBy(css = "#panel div.grayBorderedPanel")
     private List<Psychic> psychicsFound;
 
     @FindBy(css = "#rowp_0 div.span2")
     private List<Product> productsFound;
 
    @FindBy(css = "#wrap div:nth:of:type(2) div:nth-of-type(2) div:nth-of-type(3) div:nth-of-type(1) a ")
     private List<WebElement> psychicsScroller;
 
     @FindBy(css = "#wscrollerProducts > a ")
     private List<WebElement> productsScroller;
 
     public void setSearchInput(String string){
         searchInput.clear();
         searchInput.sendKeys(string);
     }
     public void SearchButtonClick(){
         guardHttp(searchButton).click();
     }
     //Psychics
     public boolean psychicNoResultFoundIsDisplayed(){
         return psychiscNoResultsFound.isDisplayed();
     }
     public String getPsychicNoResultFoundText(){
         return psychiscNoResultsFound.getText();
     }
     public int countPsychicsFound(){
         return psychicsFound.size();
     }
     public String getPsychicNickName(int index){
         return psychicsFound.get(index).getNickName();
     }
     public int countPsychicsScrollerIcons(){
         return psychicsScroller.size();
     }
     public void psychicsScrollerIconClick(int index){
         guardHttp(psychicsScroller.get(index)).click();
     }
     //Products
     public boolean marketplaceNoResultFoundIsDisplayed(){
         return marketplaceNoResultsFound.isDisplayed();
     }
     public String getMarketplaceNoResutFoundText(){
         return marketplaceNoResultsFound.getText();
     }
     public int countProductsFound(){
         return productsFound.size();
     }
     public String getProductName(int index){
         return productsFound.get(index).getProductName();
     }
     public String getProductPrice(int index){
         return productsFound.get(index).getProductPrice();
     }
     public int countProductsScrollerIcons(){
         return productsScroller.size();
     }
     public void productsScrollerIconClick(int index){
         guardHttp(productsScroller.get(index)).click();
     }
 
     public static class Psychic {
 
         @FindBy(css = "div.row-fluid h4.psychicNickname")
         private WebElement nickName;
 
         public String getNickName(){
             return nickName.getText();
         }
     }
 
     public static class Product {
         @FindBy(css = "div:nth:of:type(1) div:nth:of:type(2) > p")
         private WebElement productName;
 
         @FindBy(css = "div:nth:of:type(1) div:nth:of:type(3) > p:nth:of:type(2)")
         private WebElement productPrice;
 
         public String getProductName(){
             return productName.getText();
         }
         public String getProductPrice(){
             return productPrice.getText();
         }
     }
 }
