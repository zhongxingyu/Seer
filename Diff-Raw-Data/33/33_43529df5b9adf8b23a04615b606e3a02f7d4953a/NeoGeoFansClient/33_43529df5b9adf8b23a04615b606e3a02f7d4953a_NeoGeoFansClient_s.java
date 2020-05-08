 /**
  *     Copyright (C) 2011 Julien SMADJA <julien dot smadja at gmail dot com>
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *             http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  */
 
 package com.anzymus.neogeo.hiscores.clients;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import com.anzymus.neogeo.hiscores.domain.Score;
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
 import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
 import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
 import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 import com.google.common.base.Preconditions;
 
 public class NeoGeoFansClient {
 
     private HtmlTextInput loginTextfield;
     private HtmlPasswordInput passwordTextfield;
     private String login;
     private String password;
     private WebClient webClient;
 
     private static final Logger LOG = Logger.getLogger(NeoGeoFansClient.class.getName());
 
     public boolean authenticate(String login, String password) throws AuthenticationFailed {
         Preconditions.checkNotNull(login, "login is mandatory");
         Preconditions.checkNotNull(password, "password is mandatory");
         this.login = login;
         this.password = password;
         try {
             webClient = new WebClient();
             HtmlPage loginPage = (HtmlPage) webClient.getPage("http://www.neogeofans.com/leforum/index.php");
             HtmlForm loginForm = fillForm(loginPage);
             String contentResult = submitForm(loginForm);
             return contentResult.contains("Merci de vous être identifié, " + login + ".");
         } catch (IOException e) {
             LOG.log(Level.SEVERE, login + " has not successfully logged in", e);
             throw new AuthenticationFailed(e);
         }
     }
 
     private HtmlForm fillForm(HtmlPage loginPage) {
         HtmlForm loginForm = (HtmlForm) loginPage.getForms().get(0);
         passwordTextfield = (HtmlPasswordInput) loginForm.getInputByName("vb_login_password");
         passwordTextfield.setValueAttribute(password);
         loginTextfield = (HtmlTextInput) loginForm.getInputByName("vb_login_username");
         loginTextfield.setValueAttribute(login);
         return loginForm;
     }
 
     private String submitForm(HtmlForm loginForm) throws IOException {
         HtmlSubmitInput submitButton = (HtmlSubmitInput) loginForm.getInputByValue("S'identifier");
         HtmlPage resultPage = (HtmlPage) submitButton.click();
         String contentResult = resultPage.asText();
         return contentResult;
     }
 
     public void post(String message, long postId) {
         try {
             HtmlPage topicPage = (HtmlPage) webClient.getPage("http://www.neogeofans.com/leforum/showthread.php?t="
                     + postId);
             activateForm(topicPage);
             addMessage(message, topicPage);
             removeSignature(topicPage);
             submitPost(topicPage);
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Can't post message:'" + message + "' in postId:" + postId + " by " + login, e);
         }
     }
 
     private void activateForm(HtmlPage topicPage) throws IOException {
         HtmlAnchor anchor = findAnchor(topicPage);
         anchor.click();
     }
 
     private HtmlAnchor findAnchor(HtmlPage topicPage) {
         List<HtmlAnchor> anchors = topicPage.getAnchors();
         for (HtmlAnchor anchor : anchors) {
             String id = anchor.getId();
             if (id.startsWith("qr_")) {
                 return anchor;
             }
         }
         return null;
     }
 
     private void addMessage(String message, HtmlPage topicPage) {
         HtmlTextArea textArea = (HtmlTextArea) topicPage.getHtmlElementById("vB_Editor_QR_textarea");
         textArea.setText(message);
     }
 
     private void removeSignature(HtmlPage topicPage) throws IOException {
         try {
             HtmlCheckBoxInput signature = (HtmlCheckBoxInput) topicPage.getHtmlElementById("cb_signature");
             if (signature.isChecked()) {
                 signature.click();
             }
         } catch (ElementNotFoundException e) {
             LOG.log(Level.SEVERE, "Can't find signature check box in:\n" + topicPage.asXml());
         }
     }
 
     private void submitPost(HtmlPage topicPage) throws IOException {
         try {
             HtmlForm postForm = (HtmlForm) topicPage.getForms().get(2);
             HtmlSubmitInput submitButton = (HtmlSubmitInput) postForm.getInputsByName("sbutton").get(0);
             submitButton.click();
         } catch (ElementNotFoundException e) {
             LOG.log(Level.SEVERE, "Can't find post button in:\n" + topicPage.asXml());
         }
     }
 
     public static String toMessage(Score score) {
         String scoreValue = score.getValue();
         String message = score.getMessage();
         String url = score.getPictureUrl();
 
        String postMessage = "";
         postMessage += "[SIZE=\"3\"]" + scoreValue + "[/SIZE]\n";
         postMessage += "[I]" + message + "[/I]\n";
         postMessage += "[IMG]" + url + "[/IMG]\n";
         postMessage += "[SIZE=\"1\"]posté depuis [url]www.neogeo-hiscores.com[/url][/SIZE]";
         return postMessage;
     }
 
 }
