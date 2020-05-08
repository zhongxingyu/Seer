 package org.bgcokehack.github_recommender.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.ParseException;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.util.EntityUtils;
 import org.bgcokehack.github_recommender.recommender.FullDescriptionBasedRecommender;
 import org.bgcokehack.github_recommender.recommender.Recommender;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.builder.api.Api;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 @SessionAttributes(value = "requestToken", types = Token.class)
 public class StartPageController {
 
 	CloseableHttpClient httpclient = HttpClients.createDefault();
 
 	@RequestMapping("/start")
 	public ModelAndView startPage(Model model) {
 		OAuthService service = new ServiceBuilder()
 				.provider((Class<? extends Api>) XingApi.class)
 				.apiKey("0d640e73bb4266f3b284")
 				.apiSecret("44d8402996c9106f8c656a269bf1c49bcc359e55").build();
 		Token requestToken = service.getRequestToken();
 
 		model.addAttribute("requestToken", requestToken);
 		model.addAttribute("authUrl", service.getAuthorizationUrl(requestToken));
 
 		return new ModelAndView("startpage", "command", new UserCommand());
 	}
 
 	@RequestMapping(value = "/processUserPreferences", method = RequestMethod.POST)
 	public ModelAndView getGitHubDescription(Model model,
 			@ModelAttribute("verifierValue") UserCommand user,
 			@ModelAttribute("requestToken") Token requestToken) {
 		
 		String allInterests="";
 		if(user.getInterests().length()>0){
 			allInterests+=user.getInterests()+",";
 		}
 
 		String interestsString = allInterests; 
 		if(!user.getVerifierCode().isEmpty()) {
 			interestsString = interestsString  + fetchInterestsFromXING(requestToken, user);
 		}
 				
 		Set<String> userInterests = new HashSet<String>(
 				Arrays.asList(interestsString.split(",")));
 
 		Set<Repository> repos = processProjectDataFromGitHub(user
 				.getGithubAuthCode());
 
 		Recommender recommender = new FullDescriptionBasedRecommender();
 		List<Repository> recommendations = recommender.recommend(userInterests,
 				repos, 5);
 
 		return new ModelAndView("recommendations", "recommendations",
 				recommendations);
 	}
 
 	private String fetchInterestsFromXING(Token requestToken, UserCommand user) {
 		String interests = "";
 
 		OAuthService service = new ServiceBuilder()
 				.provider((Class<? extends Api>) XingApi.class)
 				.apiKey("0d640e73bb4266f3b284")
 				.apiSecret("44d8402996c9106f8c656a269bf1c49bcc359e55").build();
 
 		Verifier verifier = new Verifier(user.getVerifierCode());
 
 		// Trade the Request Token and Verifier for the Access Token
 		Token accessToken = service.getAccessToken(requestToken, verifier);
 
 		OAuthRequest request = new OAuthRequest(Verb.GET,
 				"https://api.xing.com/v1/users/me");
 		service.signRequest(accessToken, request);
 		Response response = request.send();
 		String jsonResponse = response.getBody();
 
 		try {
 			@SuppressWarnings("unchecked")
 			Map<String, ArrayList<Map<String, String>>> results = new ObjectMapper()
 					.readValue(jsonResponse, HashMap.class);
 			interests = results.get("users").get(0).get("haves");
 
 		} catch (JsonParseException e) {
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return interests;
 
 	}
 
 	private Set<Repository> processProjectDataFromGitHub(String authCode) {
 
 		String readmeUrlSuffix = "/readme";
 
 		Set<Repository> repositories = new HashSet<Repository>();
 
 		for (String repoUrl :  fetchRepoUrls()) {
 
			Repository repo = new Repository(repoUrl.replaceFirst("https://api.github.com/repos/", ""));
 			
 			HttpGet httpGetDescription = new HttpGet(repoUrl);			
 			HttpGet httpGetReadme = new HttpGet(repoUrl + readmeUrlSuffix);
 			
 			if(authCode != null && !authCode.isEmpty()) {
 			httpGetDescription.addHeader("Authorization", "token " + authCode);
 				
 				httpGetReadme.addHeader("Authorization", "token " + authCode);
 			
 			}
 			CloseableHttpResponse repositoryInformation = null;
 			try {
 				repositoryInformation = httpclient.execute(httpGetDescription);
 				HttpEntity entityDescription = repositoryInformation
 						.getEntity();
 				if (entityDescription != null) {
 					@SuppressWarnings("unchecked")
 					Map<String, String> resultProfile = new ObjectMapper()
 							.readValue(EntityUtils.toString(entityDescription),
 									HashMap.class);
					
 					repo.setDescription(resultProfile.get("description"));
 
 				}
 				repositoryInformation = httpclient.execute(httpGetReadme);
 				HttpEntity entityReadme = repositoryInformation.getEntity();
 				if (entityReadme != null) {
 					@SuppressWarnings("unchecked")
 					Map<String, String> resultReadme = new ObjectMapper()
 							.readValue(EntityUtils.toString(entityReadme),
 									HashMap.class);
 
 
 					if(resultReadme == null || resultReadme.get("content") ==
 							null) {
 						repo.setReadme("");
 					} else {
 						String readme = new
 								String(javax.xml.bind.DatatypeConverter.parseBase64Binary(resultReadme.get("content")));
 						if(readme.length() > 1000) {
 							repo.setReadme(readme.substring(0, 1000));
 						} else {
 							repo.setReadme(readme);
 						}
 					}
 				}
 			} catch (ParseException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (repositoryInformation != null) {
 						repositoryInformation.close();
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			repositories.add(repo);
 		}
 		return repositories;
 	}
 
 	private List<String> fetchRepoUrls() {
 		
 		List<String> repos = new ArrayList<String>();
 		HttpGet httpGetRepositories = new HttpGet("https://api.github.com/repositories");
 
 		CloseableHttpResponse repositoryInformation = null;
 		try {
 			repositoryInformation = httpclient.execute(httpGetRepositories);
 			HttpEntity entityRepos = repositoryInformation
 					.getEntity();
 			if (entityRepos != null) {
 				@SuppressWarnings("unchecked")
 				List<Map<String, String>> parsedRepos = new ObjectMapper()
 						.readValue(EntityUtils.toString(entityRepos),
 								List.class);
 				for(Map<String, String> rawRepoData: parsedRepos) {
 					repos.add(rawRepoData.get("url"));
 				}
 							
 			}
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (repositoryInformation != null) {
 					repositoryInformation.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return repos;
 	}
 }
