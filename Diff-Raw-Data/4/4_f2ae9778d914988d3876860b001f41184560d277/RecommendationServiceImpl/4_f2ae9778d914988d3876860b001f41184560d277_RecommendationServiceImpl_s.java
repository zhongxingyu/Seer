 package org.openinfinity.tagcloud.domain.service;
 
 import java.util.List;
 
 import org.openinfinity.tagcloud.domain.entity.Profile;
 import org.openinfinity.tagcloud.domain.entity.Settings;
 import org.openinfinity.tagcloud.domain.entity.Target;
 import org.openinfinity.tagcloud.domain.entity.query.NearbyTarget;
 import org.openinfinity.tagcloud.domain.entity.query.Recommendation;
 import org.openinfinity.tagcloud.domain.entity.query.TagQuery;
 import org.openinfinity.tagcloud.utils.Utils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class RecommendationServiceImpl implements RecommendationService{
 
 	@Autowired
 	private ScoreService scoreService;
 
     public void updateRecommendationScore(Recommendation recommendation, Profile user, List<String> friendFacebookIds) {
         Settings settings = Settings.NO_USER_SETTINGS;
         if(user != null) settings = user.getSettings();
     	
         //calculate partial scores as values 0..1
     	double distanceScore = calcDistanceScore(recommendation);
 		double preferredScore = calcPreferredScore(recommendation);
         double nearScore = calcNearScore(recommendation);
         double avgScore = recommendation.getTarget().getScore()/10.0;
         double ownScore = calcOwnScore(recommendation, user);
         double friendScore = calcFriendScore(recommendation, friendFacebookIds);
 
         if(recommendation.getTarget().getScores().size() == 0) {
        	avgScore = 5;
         }
         
         //calculate weighted sum of partial scores
         double weightSum = (
         		settings.getDistanceScoreWeight() + 
         		settings.getPreferredScoreWeight() +
         		settings.getNearScoreWeight() +
         		settings.getOwnScoreWeight()); 
         
         double recommendationScoreSum = (
         		distanceScore * settings.getDistanceScoreWeight() +
                 preferredScore * settings.getPreferredScoreWeight() +
                 avgScore * settings.getAvgScoreWeight() +
                 nearScore * settings.getNearScoreWeight() +
                 ownScore * settings.getOwnScoreWeight());
         
         if(friendFacebookIds.size() > 0) {
         	weightSum += settings.getFriendScoreWeight();
         	recommendationScoreSum += friendScore * settings.getFriendScoreWeight();
         }
         
         
         
         recommendation.setRecommendationScore(recommendationScoreSum / weightSum);
 	}
 
     private double calcDistanceScore(Recommendation recommendation) {
 		Target target = recommendation.getTarget();
 		TagQuery query = recommendation.getQuery();
     	double dist = Utils.calcDistanceGCS(target.getLocation()[0], target.getLocation()[1], query.getLongitude(), query.getLatitude());
 		return 1 - dist/query.getRadius();
 	}
 
 	private double calcPreferredScore(Recommendation recommendation) {
 		TagQuery query = recommendation.getQuery();
 		if(query.getPreferred().size() > 0)
             return 1.0*recommendation.getPreferredTags().size()/query.getPreferred().size();
         else return 1;
     }
 
     private double calcNearScore(Recommendation recommendation) {
         if(recommendation.getQuery().getNearby().size() == 0) return 1;
 
         double score = 0;
         for(NearbyTarget nearbyTarget : recommendation.getNearbyTargetsList()){
             score += NearbyTarget.MAX_DISTANCE - nearbyTarget.getDistance();
         }
         score /= NearbyTarget.MAX_DISTANCE;
         score /= recommendation.getNearbyTargetsList().size();
 
         return score;
     }
 
 
 	private double calcOwnScore(Recommendation recommendation, Profile user) {
 		if(user != null) {
         	return scoreService.getOwnScore(user, recommendation.getTarget()).getStars()/10;
         }
 		else return 1;
 	}
 	
 	private double calcFriendScore(Recommendation recommendation, List<String> friendFacebookIds) {
 		if(friendFacebookIds.size()>0) {
         	return scoreService.getAverageFriendScore(friendFacebookIds, recommendation.getTarget())/10;
         }
 		else return 1;
 	}
 
 }
