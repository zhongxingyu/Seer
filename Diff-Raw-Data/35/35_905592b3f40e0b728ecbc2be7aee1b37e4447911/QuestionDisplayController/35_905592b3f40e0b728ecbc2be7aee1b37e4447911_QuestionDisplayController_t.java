 /** 
  * Controller om vragen te weergeven
  * 
  * @author De Meersman Vincent	
  */
 package javaFXpanels.Questions;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javaFXpanels.MessageProvider.MessageProvider;
 import javafx.beans.property.SimpleObjectProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 import javafx.collections.FXCollections;
 import javafx.collections.ListChangeListener;
 import javafx.collections.ObservableList;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Accordion;
 import javafx.scene.control.Button;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.ScrollPane;
 import javafx.scene.control.TextField;
 import javafx.scene.control.TitledPane;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.layout.AnchorPane;
 import javafx.scene.layout.VBox;
 import javafx.scene.text.Text;
 import network.Client;
 import BussinesLayer.resources.PictureResource;
 import Protocol.FxResponseListener;
 import Protocol.FxSubmitListener;
 import Protocol.SubmitManager;
 import Protocol.exceptions.IdRangeException;
 import Protocol.requests.PictureRequest;
 import Protocol.responses.ExceptionResponse;
 import Protocol.responses.PictureResponse;
 import Protocol.responses.Response;
 import Protocol.submits.AnswerSubmit;
 import Protocol.submits.QuestionSubmit;
 import Protocol.submits.QuestionSubmit.QuestionType;
 import Protocol.submits.RoundSubmit;
 import Protocol.submits.Submit;
 
 public class QuestionDisplayController implements Initializable {
 	@FXML
 	private AnchorPane root;
 	@FXML
 	private VBox openQuestionVbox;
 	@FXML
 	private VBox multipleChoiseVbox;
 	@FXML
 	private VBox imageQuestionVbox;
 	@FXML
 	private AnchorPane quizPane;
 	@FXML
 	private AnchorPane scorePane;
 	@FXML
 	private Accordion questionAccordion;
 	@FXML
 	private ScrollPane questionScroll;
 	@FXML
 	private Text roundTitle;
 	@FXML
 	private Text openQuestionText;
 	@FXML
 	private Text multipleChoiceText;
 	@FXML
 	private Text imageQuestionText;
 	@FXML
 	private TextField openAnswer;
 	@FXML
 	private TextField imageAnswer;
 	@FXML
 	private Button openConfirm;
 	@FXML
 	private Button multipleChoiceConfirm;
 	@FXML
 	private Button imageConfirm;
 	@FXML
 	private VBox possebilityVBox;
 	@FXML
 	private ImageView imageQuestionView;
 
 	private MessageProvider messageMaker;
 
 	//variablen voor de rondes te registreren;
 	//en alles vlot te laten verlopen redlijk wat HashMasp eig
 	private HashMap<Integer,RoundSubmit> roundForId;
 	private HashMap<RoundSubmit, ObservableList<QuestionSubmit>> questionsForRound;
 	private HashMap<Integer,PictureResource> pictureForQuestion;
 	private HashMap<QuestionSubmit,AnswerSubmit> answerForQuestion;
 	private HashMap<TitledPane,QuestionSubmit> questionForPane;
 	private SimpleObjectProperty<RoundSubmit> currentPlayingRound;
 	private ListChangeListener<QuestionSubmit> questionListener;
 	private ToggleGroup choises;
 	private int teamId;
 
 	@Override
 	public void initialize(URL location, ResourceBundle resources) {
 		roundForId = new HashMap<Integer,RoundSubmit>();
 		questionsForRound = new HashMap<RoundSubmit, ObservableList<QuestionSubmit>>(2);
 		pictureForQuestion = new HashMap<Integer,PictureResource>();
 		answerForQuestion = new HashMap<QuestionSubmit,AnswerSubmit>();
 		currentPlayingRound = new SimpleObjectProperty<RoundSubmit>();
 		questionForPane = new HashMap<TitledPane,QuestionSubmit>();
 
 		messageMaker = new MessageProvider(root);
 		root.getChildren().removeAll(openQuestionVbox,imageQuestionVbox,multipleChoiseVbox);
 		openQuestionVbox.setVisible(true);
 		multipleChoiseVbox.setVisible(true);
 		imageQuestionVbox.setVisible(true);
 
 		for(VBox box: new VBox[] {openQuestionVbox,multipleChoiseVbox,imageQuestionVbox}) {
 			AnchorPane.setBottomAnchor(box, 0.0);
 			AnchorPane.setLeftAnchor(box, 0.0);
 			AnchorPane.setRightAnchor(box, 0.0);
 			AnchorPane.setTopAnchor(box, 0.0);
 		}
 
 		initBindings();
 		startListening();	
 	}
 
 	private void initBindings() {
 		//zorgen dat de content van de scrollpane de juiste brete aanhoud
 		quizPane.prefWidthProperty().bind(questionScroll.widthProperty().add(-5));
 		//TODO wrappings zetten
 
 		questionListener = new ListChangeListener<QuestionSubmit>() {
 			@Override
 			public void onChanged(
 					javafx.collections.ListChangeListener.Change<? extends QuestionSubmit> c) {
 				for(QuestionSubmit s: c.getList()) {
 					if(!questionForPane.values().contains(s)) {
 						TitledPane p = new TitledPane(s.getQuestion(), new AnchorPane());
 						questionAccordion.getPanes().add(p);
 						questionForPane.put(p, s);
 					}
 				}
 
 			}
 		};
 
 		questionAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
 			@Override
 			public void changed(ObservableValue<? extends TitledPane> observable,
 					TitledPane oldValue, TitledPane newValue) {
 				if(oldValue!= null) {
 					((AnchorPane) oldValue.getContent()).getChildren().clear();
 				}
 				if(newValue!= null) {
 					createQuestionFor(newValue);
 				}
 
 			}
 		});
 
 		currentPlayingRound.addListener(new ChangeListener<RoundSubmit>() {
 			@Override
 			public void changed(
 					ObservableValue<? extends RoundSubmit> observable,
 					RoundSubmit oldValue, RoundSubmit newValue) {
 				removeRound(oldValue);
 				setRound(newValue);
 
 			}
 		});
 
 		quizPane.visibleProperty().bind(currentPlayingRound.isNotNull());
 	}
 
 	private void startListening() {
 		SubmitManager.addSubmitListener(RoundSubmit.class, new FxSubmitListener() {
 			@Override
 			public void handleFxSubmit(Submit r) {
 				ObservableList<QuestionSubmit> questions = FXCollections.observableArrayList();
 				RoundSubmit submit = (RoundSubmit) r;
 				roundForId.put(submit.getRoundId(), submit);
 				questionsForRound.put(submit,questions);
 				if(currentPlayingRound.get() == null) {
 					currentPlayingRound.set(submit);
 				}
 
 			}
 		});
 
 		SubmitManager.addSubmitListener(QuestionSubmit.class, new FxSubmitListener() {
 
 			@Override
 			public void handleFxSubmit(Submit r) {
				System.out.println("Antwoord");
 				QuestionSubmit submit = (QuestionSubmit) r;
 				if(roundForId.containsKey(submit.getQuestionRoundId())) {
 					RoundSubmit s = roundForId.get(submit.getQuestionRoundId());
 					questionsForRound.get(s).add(submit);
 				}
 
 				//TODO: Task maken die de image op de achtergrond laad
 				if(submit.getType().equals(QuestionType.PICTURE)) {
 					loadImage(submit.getQuestionId());
 				}
 
 			}
 		});
 	}
 
 	private void createQuestionFor(TitledPane pane) {
 		QuestionSubmit submit = questionForPane.get(pane);
 		AnchorPane child = (AnchorPane) pane.getContent();
 
 		if(submit.getType().equals(QuestionType.PICTURE)) {
 			child.getChildren().add(createImageQuestion(submit));
 		} else if(submit.getType().equals(QuestionType.MULTIPLECHOISE)) {
 			child.getChildren().add(createMultipleQuestion(submit));
 		} else {
 			child.getChildren().add(createOpenQuestion(submit));
 		}
 
 	}
 
 	private VBox createImageQuestion(QuestionSubmit submit) {
 		imageQuestionText.setText(submit.getQuestion());
 		if(pictureForQuestion.containsKey(submit.getQuestionId())) {
 			Image i = pictureForQuestion.get(submit.getQuestionId())
 					.getImage();
 
 			imageQuestionView.setImage(i);
 			imageQuestionView.setFitWidth(i.getWidth());
 			imageQuestionView.setFitHeight(i.getHeight());
 		}
 
 		if(answerForQuestion.containsKey(submit)) {
 			imageAnswer.setText(answerForQuestion.get(submit).getAnswer());
 		} else {
 			imageAnswer.setText("");
 		}
 
 		return imageQuestionVbox;
 	}
 
 	private VBox createMultipleQuestion(QuestionSubmit submit) {
 		String correct = "";
 		RadioButton button;
 		multipleChoiceText.setText(submit.getQuestion());
 		possebilityVBox.getChildren().clear();
 		choises = new ToggleGroup();
 
 		if(answerForQuestion.containsKey(submit)) {
 			correct = answerForQuestion.get(submit).getAnswer();
 		}
 
 		for(String possibility: submit.getPossibilities()) {
 			button = new RadioButton(possibility);
 			button.setToggleGroup(choises);
 			if(possibility.equals(correct)) {
 				button.selectedProperty().set(true);
 			}
 			possebilityVBox.getChildren().add(button);
 		}
 
 		return multipleChoiseVbox;
 	}
 
 	private VBox createOpenQuestion(QuestionSubmit submit) {
 		openQuestionText.setText(submit.getQuestion());
 
 		if(answerForQuestion.containsKey(submit)) {
 			openAnswer.setText(answerForQuestion.get(submit).getAnswer());
 		} else {
 			openAnswer.setText("");
 		}
 		return openQuestionVbox;
 	}
 
 	//als de ronde is doorgestuurd een nieuwe ronde laden
 	private void setRound(RoundSubmit round) {
 		if(round != null) {
 			questionAccordion.getPanes().clear();
 			questionsForRound.get(round).addListener(questionListener);
 			roundTitle.setText(round.getRoundName());
 		}
 
 	}
 
 	//een oude ronde verwijderen
 	private void removeRound(RoundSubmit round) {
 		if(round!= null) {
 			questionsForRound.get(round).removeListener(questionListener);
 			questionAccordion.getPanes().clear();
 		}
 	}
 
 	private void loadImage(int questionId) {
 		//TODO: imageLoader in een apparte task maken
 		try {
 			PictureRequest request = new PictureRequest(questionId);
 			request.onResponse(new FxResponseListener() {			
 				@Override
 				public void handleFxResponse(Response response) {
 					if(response instanceof ExceptionResponse) {
 						messageMaker.showWarning(((ExceptionResponse) response).getExceptionMessage());
 					} else if (response instanceof PictureResponse) {
 						PictureResponse pic = (PictureResponse) response;
 						try {
 							pictureForQuestion.put(pic.getQuestionId(), pic.getPictureResource());
 						} catch (IOException e) {
 							messageMaker.showWarning("Kon afbeelding niet laden");
 						}
 					}
 				}
 			});
 
 			request.send();
 
 		} catch (UnknownHostException e) {
 			messageMaker.showWarning("Kon afbeelding niet ophalen");
 		} catch (IdRangeException e) {
 			messageMaker.showWarning("Kon afbeelding niet ophalen");
 		} catch (IOException e) {
 			messageMaker.showWarning("Kon afbeelding niet ophalen");
 		}
 
 	}
 
 	@FXML
 	private void submitOpenQuestion() {
 		QuestionSubmit submit = questionForPane.get(questionAccordion.expandedPaneProperty().get());	
 		answerForQuestion.put(submit, submit.createAnswerSubmit(openAnswer.getText(),teamId));
 	}
 
 	@FXML
 	private void submitImageQuestion() {
 		QuestionSubmit submit = questionForPane.get(questionAccordion.expandedPaneProperty().get());	
 		answerForQuestion.put(submit, submit.createAnswerSubmit(imageAnswer.getText(),teamId));
 	}
 
 	@FXML
 	private void submitMultipleQuestion() {
 		String answer = choises.getSelectedToggle() == null? "": ((RadioButton)choises.getSelectedToggle()).getText();
 		QuestionSubmit submit = questionForPane.get(questionAccordion.expandedPaneProperty().get());
 		answerForQuestion.put(submit, submit.createAnswerSubmit(answer,teamId));
 	}
 
 	@FXML
 	private void sendRound() {
 		if(answerForQuestion.size() != currentPlayingRound.get().getAmountOfQuestions()) {
 			messageMaker.showWarning("niet alle vragen zijn ingevuld");
 			return;
 		}
 
 		send(questionsForRound.get(currentPlayingRound.get()));
 
 		//de huidige ronde verwijderen
 		questionsForRound.remove(currentPlayingRound.get());
 		if(questionsForRound.isEmpty()) {
 			currentPlayingRound.set(null);
 		} else {
 			for(RoundSubmit s: questionsForRound.keySet()) {
 				currentPlayingRound.set(s);
 				break;
 			}
 		}
 	}
 
 	private void send(List<QuestionSubmit> list) {
 		
 		//TODO: dit door een task laten uitvoeren
 		
 		try {
 			Client client = Client.getInstance();
 
 			for(QuestionSubmit s: list) {
 				client.send(answerForQuestion.get(s));
 				answerForQuestion.remove(s);
 			}
 		} catch (UnknownHostException e) {
 			messageMaker.showError("kon antwoorden niet doorsturen");
 		} catch (IOException e) {
 			messageMaker.showError("kon antwoorden niet doorsturen");
 		}
 	}
 
 	public void setTeam(int teamId) {
 		this.teamId = teamId;
 	}
 }
