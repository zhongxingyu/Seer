 package com.codeborne.example1;
 
 import org.openqa.selenium.By;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static com.codeborne.selenide.Condition.text;
 import static com.codeborne.selenide.Selenide.*;
 import static java.util.Arrays.asList;
 
 public class GitHubSpec {
 
   @BeforeMethod
   public void openGitHub() {
     open("http://github.com/search");
   }
 
   @Test
   public void userCanSearchRepositories() {
     $("#search_form").find(By.name("q")).setValue("bdd-tools").pressEnter();
     $(".main-content .repolist").shouldHave(text("mcgray/bdd-tools"));
   }
 
   @Test
   public void userCanSearchUsers() {
     $("#search_form").find(By.name("q")).setValue("mcgray").pressEnter();
     $(".search-menu-container").find(By.partialLinkText("Users")).click();
     $(".main-content .user-list .user-list-item").shouldHave(text("Oleksiy Rezchykov"));
     $(".main-content .user-list .user-list-item").find(By.linkText("mcgray")).click();
     $(".tabnav-tabs").find(By.linkText("Repositories")).click();
     $(".repolist").shouldHave(text("bdd-tools"));
   }
 
   @Test
   public void userCanBrowseProjectSources() {
     open("https://github.com/mcgray");
     $(".tabnav-tabs").find(By.linkText("Repositories")).click();
     $(".repolist").find(By.linkText("bdd-tools")).click();
     $(".tree-browser").shouldHave(text("selenide"));
     $(".tree-browser").find(By.linkText("selenide")).click();
     for (String path : asList("src", "test", "java", "com", "codeborne", "example1", "GitHubSpec.java")) {
       $(By.xpath("//td[contains(@class,'content')]//a[text()='" + path + "']")).click();
 //      $$(".tree-browser .content").findBy(text(path)).followLink();
     }
    $(".lines").shouldHave(text("hidden treasure"));
   }
 
 }
