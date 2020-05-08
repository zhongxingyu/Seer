 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.user;
 
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.EnumSet;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import nl.surfnet.bod.domain.BodAccount;
 import nl.surfnet.bod.domain.oauth.AccessToken;
 import nl.surfnet.bod.domain.oauth.AccessTokenResponse;
 import nl.surfnet.bod.domain.oauth.NsiScope;
 import nl.surfnet.bod.repo.BodAccountRepo;
 import nl.surfnet.bod.service.OAuthServerService;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.security.Security;
 
 import org.apache.http.client.utils.URIBuilder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 @Controller
 @RequestMapping("/oauth2")
 public class OAuthTokenController {
 
   private static final String CLIENT_REDIRECT = "/redirect";
   private static final String ADMIN_REDIRECT = "/authredirect";
 
   @Resource
   private BodAccountRepo bodAccountRepo;
 
   @Resource(name = "bodEnvironment")
   private Environment env;
 
   @Resource
   private OAuthServerService oAuthServerService;
 
   @RequestMapping("/tokens")
   public String index(Model model) throws URISyntaxException {
     final BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());
 
     if (!account.getAuthorizationServerAccessToken().isPresent()) {
      return retreiveAuthorizationServerAccessToken();
     }
 
     Collection<AccessToken> tokens = oAuthServerService.getAllAccessTokensForUser(account);
 
     model.addAttribute("accessToken", Iterables.getFirst(tokens, null));
 
     return "oauthResult";
   }
 
  private String retreiveAuthorizationServerAccessToken() throws URISyntaxException {
     String uri = buildAuthorizeUri(
         env.getAdminClientId(),
         adminRedirectUri(),
         Lists.newArrayList("read", "write"));
 
     return "redirect:".concat(uri);
   }
 
   @RequestMapping(value = "/token/delete",  method = RequestMethod.DELETE)
   public String deleteAccessToken(@RequestParam String tokenId, Model model) {
     BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());
 
     oAuthServerService.deleteAccessToken(account, tokenId);
 
     return "oauthResult";
   }
 
   @RequestMapping("/token")
  public String retreiveToken(Model model) throws URISyntaxException {
     Collection<String> scopes = Collections2.transform(EnumSet.allOf(NsiScope.class), new Function<NsiScope, String>() {
       @Override
       public String apply(NsiScope scope) {
         return scope.name().toLowerCase();
       }
     });
 
     String uri = buildAuthorizeUri(
         env.getClientClientId(),
         redirectUri(),
         scopes);
 
     return "redirect:".concat(uri);
   }
 
   private String buildAuthorizeUri(String clientId, String redirectUri, Collection<String> scopes) throws URISyntaxException {
     return new URIBuilder(env.getOauthServerUrl().concat("/oauth2/authorize"))
       .addParameter("response_type", "code")
       .addParameter("client_id", clientId)
       .addParameter("redirect_uri", redirectUri)
       .addParameter("scope", Joiner.on(',').join(scopes)).build().toASCIIString();
   }
 
   @RequestMapping(ADMIN_REDIRECT)
   public String authRedirect(HttpServletRequest request, Model model) {
     AccessTokenResponse tokenResponse = oAuthServerService.getAdminAccessToken(
         request.getParameter("code"),
         adminRedirectUri()).get();
 
     BodAccount account = bodAccountRepo.findByNameId(Security.getNameId());
     account.setAuthorizationServerAccessToken(tokenResponse.getAccessToken());
     bodAccountRepo.save(account);
 
     return "redirect:/oauth2/tokens";
   }
 
   @RequestMapping(CLIENT_REDIRECT)
   public String redirect(HttpServletRequest request, Model model) {
     oAuthServerService.getClientAccessToken(request.getParameter("code"), redirectUri());
 
     return "redirect:/oauth2/tokens";
   }
 
   private String adminRedirectUri() {
    return env.getExternalBodUrl().concat("/oauth2" + ADMIN_REDIRECT);
   }
 
   private String redirectUri() {
    return env.getExternalBodUrl().concat("/oauth2" + CLIENT_REDIRECT);
   }
 
 }
