 package com.gmail.mironchik.kos.web;
 
 import com.gmail.mironchik.kos.dto.FriendsRequest;
 import com.gmail.mironchik.kos.dto.HandShake;
 import com.gmail.mironchik.kos.dto.User;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
 import org.springframework.http.client.SimpleClientHttpRequestFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.client.RestTemplate;
 
import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpSession;
 import java.util.*;
 
 /**
  * Date: 17.08.13
  * Time: 15:53
  */
 @Controller
 public class SixHandsWayController {
 
     private Set<Integer> owners = new HashSet<Integer>();
 
    @PostConstruct public void init() {

    }

     @RequestMapping(value = "/getuser/{id}", method = RequestMethod.GET)
     public @ResponseBody User getUser(@PathVariable String id){
         RestTemplate restTemplate = new RestTemplate();
         Map<String, List<Map<String, Object>>> tmp = restTemplate.getForObject("https://api.vk.com/method/users.get?user_ids=" + id + "&fields=screen_name,photo_200_orig", Map.class);
         User user = new User();
        Map resp = (Map) ((List) tmp.get("response")).get(0);
         fillUser(user, resp);
         return user;
     }
 
     private void fillUser(User user, Map resp) {
         user.setId((Integer) resp.get("uid"));
         user.setName((String)resp.get("first_name") + resp.get("last_name"));
         user.setPhlink((String) resp.get("photo_200_orig"));
     }
 
     @RequestMapping(value = "/getchain/{step}/{id}", method = RequestMethod.GET)
     public @ResponseBody Map getChain(@PathVariable Integer step, @PathVariable Integer id, HttpSession session){
         RestTemplate  restTemplate = new RestTemplate();
         List<HandShake> handShakes = (List<HandShake>) session.getAttribute("handShakeList");
         List<User> userList = new ArrayList<User>();
         User user = new User();
         user.setId(id);
         for (int i = step; i > 0; i--) {
 
             Map<String, List<String>> tmp = restTemplate.getForObject("https://api.vk.com/method/friends.get?user_id=" + Integer.valueOf(user.getId()), Map.class);
             List<Integer> curOwners = new ArrayList((Collection) session.getAttribute("step_" + i));
             List tmpResp = tmp.get("response");
             tmpResp.retainAll(curOwners);
             List<Integer> intersection = tmpResp;
             if (!intersection.isEmpty()) {
                 user = new User();
                 user.setId(intersection.get(0));
                 userList.add(user);
             }
         }
         Map<String, Object> result = new HashMap<String, Object>();
         result.put("chain", userList);
         session.invalidate();
         return result;
     }
 
     @RequestMapping( value = "/getfriends", method = RequestMethod.POST)
     public @ResponseBody Map getFriends(@RequestBody FriendsRequest request, HttpSession session) throws InterruptedException {
         HandShake handShake = new HandShake();
         handShake.setStep(request.getStep());
         RestTemplate restTemplate = new RestTemplate();
         Set<String> friendsSet = new HashSet<String>();
         String[] strIds = StringUtils.split(request.getIds(), ",");
         Set<Integer> tmpOwners = new HashSet<Integer>();
         for (String strId : strIds){
             if (session.getAttribute("owners") != null) {
                 owners = (Set<Integer>) session.getAttribute("owners");
 
             }
             tmpOwners.add(Integer.valueOf(strId));
             Map<String, List<String>> tmp = restTemplate.getForObject("https://api.vk.com/method/friends.get?user_id=" + Integer.valueOf(strId), Map.class);
             friendsSet.addAll(tmp.get("response"));
            }
 
         friendsSet.removeAll(owners);
         owners.addAll(tmpOwners);
 
         handShake.setOwners(new ArrayList<Integer>(tmpOwners));
 
 //        saveToSessionHandShakes(session, handShake);
 
 
 
         session.setAttribute("step_" + request.getStep(), tmpOwners);
         session.setAttribute("owners", owners);
         Map<String, Set<String>> result = new HashMap<String, Set<String>>();
         result.put("friends", friendsSet) ;
         return result;
     }
 
 
 
     private void saveToSessionHandShakes(HttpSession session, HandShake handShake) {
         List<HandShake> handShakeList = (List<HandShake>) session.getAttribute("handShakeList");
         if (handShakeList == null) {
             handShakeList = new ArrayList<HandShake>();
 
         }
         handShakeList.add(handShake);
         session.setAttribute("handShakeList", handShakeList);
     }
 }
