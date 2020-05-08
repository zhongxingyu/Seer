 package nl.kennisnet.arena.services.factories;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import nl.kennisnet.arena.client.domain.QuestDTO;
 import nl.kennisnet.arena.client.domain.QuestItemDTO;
 import nl.kennisnet.arena.client.domain.RoundDTO;
 import nl.kennisnet.arena.model.Image;
 import nl.kennisnet.arena.model.Information;
 import nl.kennisnet.arena.model.Location;
 import nl.kennisnet.arena.model.ParticipantAnswer;
 import nl.kennisnet.arena.model.Positionable;
 import nl.kennisnet.arena.model.Quest;
 import nl.kennisnet.arena.model.Question;
 import nl.kennisnet.arena.model.Round;
 import nl.kennisnet.arena.model.Video;
 
 public class DomainObjectFactory {
 
 	public static Quest create(QuestDTO questDTO) {
 		Quest result = new Quest();
 		result.setId(questDTO.getId());
 		result.setName(questDTO.getName());
 		result.setEmailOwner(questDTO.getEmailOwner());
 		if (questDTO.getItems() != null) {
 			List<Positionable> items = new ArrayList<Positionable>();
 			for (QuestItemDTO questItemDTO : questDTO.getItems()) {
 				Positionable positionable = create(questItemDTO, result);
 				if (positionable != null) {
 					items.add(positionable);
 				}
 			}
 			result.setPositionables(items);
 		}
 
 		result.setBorder(GeomUtil.createJTSPolygon(questDTO.getBorder()));
 		result.getRounds().clear();
 		for(RoundDTO round: questDTO.getRounds()){
 			result.addRound(new Round(round.getId(), round.getName(), result));
 		}
		result.setActiveRound(result.getRounds().get(0));
 		return result;
 	}
 
 	private static Positionable create(QuestItemDTO questItemDTO, Quest quest) {
 		Positionable result = null;
 		if (questItemDTO.getTypeName().equals("Verhaal")) {
 			result = new Information(questItemDTO.getName(),
 					questItemDTO.getDescription());
 		} else if (questItemDTO.getTypeName().equals("Vraag")) {
 			Question question = new Question(questItemDTO.getDescription(),
 					questItemDTO.getOption1(), questItemDTO.getOption2(),
 					questItemDTO.getOption3(), questItemDTO.getOption4(), questItemDTO.getQuestionType());
 			question.setCorrectAnswer(questItemDTO.getCorrectOption());
 			result = question;
 		} else if (questItemDTO.getTypeName().equals("Foto")) {
 			result = new Image();
 			((Image) result).setUrl(questItemDTO.getObjectURL());
 		} else if (questItemDTO.getTypeName().equals("Video")) {
 			result = new Video();
 			((Video) result).setVideoUrl(questItemDTO.getObjectURL());
 		}
 		if (result != null) {
 			result.setName(questItemDTO.getName());
 			Location location = new Location(
 					GeomUtil.createJTSPoint(questItemDTO.getPoint()),
 					questItemDTO.getAlt(), questItemDTO.getRadius(), questItemDTO.getVisibleRadius());
 			result.setLocation(location);
 		}
 		result.setQuest(quest);
 		return result;
 	}
 
 	public static Quest update(QuestDTO questDTO, Quest originalQuest) {
 		Quest result = new Quest();
 		result.setId(questDTO.getId());
 		result.setName(questDTO.getName());
 		result.setEmailOwner(questDTO.getEmailOwner());
 
 		if (questDTO.getItems() != null) {
 			List<Positionable> items = new ArrayList<Positionable>();
 			for (QuestItemDTO questItemDTO : questDTO.getItems()) {
 				Positionable positionable = create(questItemDTO, result);
 				if (positionable != null ) {
 					for(Positionable orriginalPos : originalQuest.getPositionables()){
 						if(orriginalPos.equals(positionable)){
 							positionable.setId(orriginalPos.getId());
 						}
 						if(orriginalPos instanceof Question && positionable instanceof Question){
 							List<ParticipantAnswer> originalAnswers = ((Question)orriginalPos).getParticipantAnswers();
 							List<ParticipantAnswer> participantAnswer = new ArrayList<ParticipantAnswer>(originalAnswers.size());
 							Collections.copy(originalAnswers, participantAnswer);
 							((Question)positionable).setParticipantAnswers(participantAnswer);
 						}						
 					}
 					items.add(positionable);
 				}
 			}
 			result.setPositionables(items);
 		}
 		result.setBorder(GeomUtil.createJTSPolygon(questDTO.getBorder()));
 		RoundDTO activeRoundDTO = questDTO.getActiveRound();
 		result.setActiveRound(new Round(activeRoundDTO.getId(), activeRoundDTO.getName(), result));
 		result.getRounds().clear();
 		for(RoundDTO round: questDTO.getRounds()){
 			Round r = new Round(round.getId(), round.getName(), result);
 			result.addRound(r);
 		}
 		return result;
 	}
 	
 	public static List<Positionable> delete(Quest quest, Quest originalQuest){
 		List<Positionable> result = new ArrayList<Positionable>();
 		for(Positionable orriginalPos: originalQuest.getPositionables()){
 			if(!quest.getPositionables().contains(orriginalPos)){
 				result.add(orriginalPos);
 			}
 		}
 		System.out.println(result);
 		return result;
 	}
 
 }
