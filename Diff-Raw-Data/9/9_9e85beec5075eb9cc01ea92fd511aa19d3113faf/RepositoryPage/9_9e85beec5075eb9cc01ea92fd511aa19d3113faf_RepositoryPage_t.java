 package com.codeborne.example2;
 
 import com.codeborne.selenide.SelenideElement;
 import org.openqa.selenium.By;
 
 import static com.codeborne.selenide.Selenide.$;
 
 public class RepositoryPage {
   public void openSource(String path) {
     $(By.xpath("//td[contains(@class,'content')]//a[text()='" + path + "']")).click();
 //    $$(".tree-browser .content").findBy(text(path)).followLink();
   }
 
   public SelenideElement getSourceLines() {
    return $(".blob-line-code");
   }
 }
