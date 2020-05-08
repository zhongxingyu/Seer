 package nl.kennisnet.arena.services.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import nl.kennisnet.arena.client.domain.ActionDTO;
 import nl.kennisnet.arena.client.domain.QuestDTO;
 import nl.kennisnet.arena.client.domain.QuestItemDTO;
 import nl.kennisnet.arena.client.domain.RoundDTO;
 import nl.kennisnet.arena.client.domain.TeamDTO;
 import nl.kennisnet.arena.model.Image;
 import nl.kennisnet.arena.model.Information;
 import nl.kennisnet.arena.model.Participant;
 import nl.kennisnet.arena.model.Participation;
 import nl.kennisnet.arena.model.ParticipationLog;
 import nl.kennisnet.arena.model.Positionable;
 import nl.kennisnet.arena.model.Quest;
 import nl.kennisnet.arena.model.Question;
 import nl.kennisnet.arena.model.Round;
 import nl.kennisnet.arena.model.Video;
 
 public class DTOFactory {
 
    public static QuestDTO create(Quest quest) {
       if (quest == null) {
          return null;
       }
       QuestDTO result = new QuestDTO();
       result.setId(quest.getId());
       result.setName(quest.getName());
       result.setEmailOwner(quest.getEmailOwner());
       if (quest.getPositionables() != null) {
          ArrayList<QuestItemDTO> items = new ArrayList<QuestItemDTO>();
          for (Positionable positionable : quest.getPositionables()) {
             items.add(create(positionable));
          }
          result.setItems(items);
       }
       result.setBorder(GeomUtil.createSimplePolygon(quest.getBorder()));
      if(quest.getActiveRound() != null){
    	  result.setActiveRound(new RoundDTO(quest.getActiveRound().getId(), quest.getActiveRound().getName()));  
      }
       result.setRounds(roundsToRoundDto(quest.getRounds()));
       return result;
    }
 
    public static QuestItemDTO create(Positionable positionable) {
       QuestItemDTO result = null;
       if (positionable!=null){
          if (positionable instanceof Information) {
             result = new QuestItemDTO(positionable.getName(), "Verhaal");
             result.setDescription(((Information) positionable).getText());
          } else if (positionable instanceof Question) {
             Question question = (Question) positionable;
             result = new QuestItemDTO(question.getName(), "Vraag");
             result.setDescription(question.getText());
             result.setOption1(question.getAnswer1());
             result.setOption2(question.getAnswer2());
             result.setOption3(question.getAnswer3());
             result.setOption4(question.getAnswer4());
             result.setQuestionType(question.getQuestionType());
             result.setName(question.getName());
             result.setCorrectOption(question.getCorrectAnswer());
          } else if (positionable instanceof Image){
             Image image=(Image)positionable;
             result = new QuestItemDTO(image.getName(), "Foto");
             result.setObjectURL(image.getUrl());
          } else if (positionable instanceof Video){
         	 Video video = (Video)positionable;
         	 result = new QuestItemDTO(video.getName(), "Video");
         	 result.setObjectURL(video.getVideoUrl());
          }
          if (result!=null){
             result.setRadius(positionable.getLocation().getRadius());
             result.setVisibleRadius(positionable.getLocation().getVisibleRadius());            
             result.setPoint(GeomUtil.createSimplePoint(positionable.getLocation().getPoint()));
             result.setId(positionable.getId());
          }
       }
 
       return result;
    }
 
    public static ActionDTO create(ParticipationLog log){
       ActionDTO action= new ActionDTO();
       action.setAction(log.getAction());
       if (log.getAnswer()!=null){
          action.setAnswer(log.getAnswer());
       }
       action.setPoint(GeomUtil.createSimplePoint(log.getLocation()));
       action.setTeamName(log.getParticipation().getParticipant().getName());
       action.setQuestId(log.getParticipation().getQuest().getId());
       action.setTimeInMillis(log.getTime().getTime());
       if (log.getPositionable()!=null){
          action.setPositionableId(log.getPositionable().getId());
       }
       return action;
    }
 
    public static TeamDTO create(Participation participation) {
       TeamDTO team=new TeamDTO();
       team.setName(participation.getParticipant().getName());
       team.setScore(participation.getScore());
       team.setColor(participation.getParticipant().getHexColor());
       team.setId(participation.getParticipant().getId());
       return team;
    }
 
    public static TeamDTO create(Participant participant) {
       TeamDTO team=new TeamDTO();
       team.setName(participant.getName());
       team.setColor(participant.getHexColor());
       team.setId(participant.getId());
       return team;
    }
 
    private static List<RoundDTO> roundsToRoundDto(List<Round> rounds){
 	   List<RoundDTO> result = new ArrayList<RoundDTO>();
 	   for(Round round : rounds){
 		   RoundDTO roundDTO = new RoundDTO(round.getId(), round.getName());
 		   result.add(roundDTO);
 	   }
 	   return result;
    }
 
 }
