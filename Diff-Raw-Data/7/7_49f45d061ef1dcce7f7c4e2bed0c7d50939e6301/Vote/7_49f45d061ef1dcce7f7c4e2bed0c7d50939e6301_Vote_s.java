 package com.rau.evoting.beans;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.mail.MessagingException;
 
 import org.primefaces.model.StreamedContent;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.rau.evoting.ElGamal.BigIntegerTypeAdapter;
 import com.rau.evoting.ElGamal.ChaumPedersen;
 import com.rau.evoting.ElGamal.ChaumPedersenProof;
 import com.rau.evoting.ElGamal.CryptoUtil;
 import com.rau.evoting.ElGamal.ElGamalHelper;
 import com.rau.evoting.data.ElectionDP;
 import com.rau.evoting.data.ElectionVoteDP;
 import com.rau.evoting.data.ElectonAnswerDP;
 import com.rau.evoting.data.UserDP;
 import com.rau.evoting.models.Answer;
 import com.rau.evoting.models.Election;
 import com.rau.evoting.models.User;
 import com.rau.evoting.utils.BarcodeHelper;
 import com.rau.evoting.utils.MailService;
 import com.rau.evoting.utils.Pair;
 import com.rau.evoting.utils.StringHelper;
 import com.rau.evoting.utils.Util;
 
 public class Vote {
 
 	private ArrayList<Answer> answers;
 	private ArrayList<Integer> a1;
 	private ArrayList<Integer> a2;
 	private boolean showEncode;
 	private boolean showShuffle;
 	private String encoded1;
 	private String encoded2;
 	private StreamedContent barcode1;
 	private StreamedContent barcode2;
 	private StreamedContent barcodeReceipt;
 	private int selectedDecodedList;
 	private boolean showDecode;
 	private boolean showDecoded1;
 	private boolean showDecoded2;
 	private int selectedVote;
 	private int elId;
 	private int userId;
 	private String decoded1;
 	private String decoded2;
 	private String hash1;
 	private String hash2;
 	private int receiptId;
 
 	private Election election;
 	private String publicKey;
 	private BigInteger r1;
 	private BigInteger r2;
 	private String chaumPedersen;
 
 	public Vote() {
 		userId = (int) FacesContext.getCurrentInstance().getExternalContext()
 				.getSessionMap().get("userId");
 	}
 
 	public String fromElection() {
 		elId = Integer.valueOf(FacesContext.getCurrentInstance()
 				.getExternalContext().getRequestParameterMap().get("elId"));
 		answers = ElectonAnswerDP.getElectionAnswers(elId);
 		showEncode = false;
 		showShuffle = true;
 		selectedDecodedList = 1;
 		showDecode = false;
 		showDecoded1 = false;
 		showDecoded2 = false;
 		selectedVote = 1;
 		barcode1 = null;
 		barcode2 = null;
 		barcodeReceipt = null;
 		encoded1 = null;
 		encoded2 = null;
 		a1 = new ArrayList<Integer>();
 		a2 = new ArrayList<Integer>();
 		for (Answer ans : answers) {
 			a1.add(ans.getId());
 			a2.add(ans.getId());
 		}
 		election = ElectionDP.getElection(elId);
 		publicKey = election.getPublicKey();
 
 		return "Vote?faces-redirect=true";
 	}
 
 	public void shuffle(AjaxBehaviorEvent event) {
 		Util.shuffle(a1);
 		Util.shuffle(a2);
 		showEncode = true;
 	}
 
 	public void encode(AjaxBehaviorEvent event) {
 		showShuffle = false;
 		decoded1 = StringHelper.converInttListToString(a1);
 		ElGamalHelper e1 = new ElGamalHelper(publicKey);
 		encoded1 = e1.encode(decoded1);
 		System.out.println("enc + dec: " + encoded1 + " " + decoded1);
 		barcode1 = BarcodeHelper.getBarcodeFromString(encoded1);
 		decoded2 = StringHelper.converInttListToString(a2);
 		r1 = e1.getR();
 		ElGamalHelper e2 = new ElGamalHelper(publicKey);
 		encoded2 = e2.encode(decoded2);
 		System.out.println("enc + dec2: " + encoded2 + " " + decoded2);
 		barcode2 = BarcodeHelper.getBarcodeFromString(encoded2);
 		r2 = e2.getR();
 		showEncode = false;
 		showDecode = true;
 	}
 
 	public void decode(AjaxBehaviorEvent event) {
 		showDecode = false;
 		hash1 = StringHelper.getSHA256hash(encoded1);
 		hash2 = StringHelper.getSHA256hash(encoded2);
 		if (selectedDecodedList == 1) {
 			showDecoded1 = true;
 			// make decode logic
 		} else {
 			showDecoded2 = true;
 			// make decode logic
 		}
 	}
 
 	public String vote() {
 		// vote
 
 		ChaumPedersenProof chaum = new ChaumPedersenProof();
 		ChaumPedersen cp = chaum.generate(new BigInteger(publicKey),
				(selectedDecodedList == 1 ? r2 : r1));
		cp.setMessage(selectedDecodedList == 1 ? decoded2 : decoded1);
 
 		Pair<BigInteger, BigInteger> enc = CryptoUtil
				.getEncodedA_B(selectedDecodedList == 1 ? encoded2 : encoded1);
 
 		cp.setA(enc.getFirst());
 		cp.setB(enc.getSecond());
 
 		Gson gson = new GsonBuilder().registerTypeAdapter(BigInteger.class,
 				new BigIntegerTypeAdapter()).create();
 		chaumPedersen = gson.toJson(cp);
 
 		receiptId = ElectionVoteDP.setElectionVote(elId, userId,
 				selectedDecodedList, (selectedDecodedList == 1 ? decoded1
 						: decoded2), encoded1, encoded2, selectedVote,
 				chaumPedersen);
 		barcodeReceipt = BarcodeHelper.getBarcodeFromString(chaumPedersen);
 
 		if (receiptId == -1) {
 			return "Home?faces-redirect=true";
 		}
 
 		String message = "  Reciept Id: " + receiptId + "\n " + " hash1: "
 				+ hash1 + "\n " + " hash2: " + hash2 + "\n "
 				+ " selected audit ballot: " + selectedDecodedList + " - "
 				+ (selectedDecodedList == 1 ? decoded1 : decoded2) + "\n "
 				+ " your choice: " + selectedVote;
 		String subject = "Receipt for " + election.getName() + " election";
 		User user = UserDP.getUser(userId);
 		try {
 			MailService.sendMessage(user.getEmail(), subject, message);
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "AfterVote?faces-redirect=true";
 	}
 
 	public ArrayList<Answer> getAnswers() {
 		return answers;
 	}
 
 	public void setAnswers(ArrayList<Answer> answers) {
 		this.answers = answers;
 	}
 
 	public ArrayList<Integer> getA1() {
 		return a1;
 	}
 
 	public void setA1(ArrayList<Integer> a1) {
 		this.a1 = a1;
 	}
 
 	public ArrayList<Integer> getA2() {
 		return a2;
 	}
 
 	public void setA2(ArrayList<Integer> a2) {
 		this.a2 = a2;
 	}
 
 	public boolean isShowEncode() {
 		return showEncode;
 	}
 
 	public void setShowEncode(boolean showEncode) {
 		this.showEncode = showEncode;
 	}
 
 	public boolean isShowShuffle() {
 		return showShuffle;
 	}
 
 	public void setShowShuffle(boolean showShuffle) {
 		this.showShuffle = showShuffle;
 	}
 
 	public StreamedContent getBarcode1() {
 		return barcode1;
 	}
 
 	public void setBarcode1(StreamedContent encoded1) {
 		this.barcode1 = encoded1;
 	}
 
 	public StreamedContent getBarcode2() {
 		return barcode2;
 	}
 
 	public void setBarcode2(StreamedContent encoded2) {
 		this.barcode2 = encoded2;
 	}
 
 	public StreamedContent getBarcodeReceipt() {
 		return barcodeReceipt;
 	}
 
 	public void setBarcodeReceipt(StreamedContent barcodeReceipt) {
 		this.barcodeReceipt = barcodeReceipt;
 	}
 
 	public int getSelectedDecodedList() {
 		return selectedDecodedList;
 	}
 
 	public void setSelectedDecodedList(int selectedDecodedList) {
 		this.selectedDecodedList = selectedDecodedList;
 	}
 
 	public boolean isShowDecode() {
 		return showDecode;
 	}
 
 	public void setShowDecode(boolean showDecode) {
 		this.showDecode = showDecode;
 	}
 
 	public boolean isShowDecoded1() {
 		return showDecoded1;
 	}
 
 	public void setShowDecoded1(boolean showDecoded1) {
 		this.showDecoded1 = showDecoded1;
 	}
 
 	public boolean isShowDecoded2() {
 		return showDecoded2;
 	}
 
 	public void setShowDecoded2(boolean showDecoded2) {
 		this.showDecoded2 = showDecoded2;
 	}
 
 	public int getSelectedVote() {
 		return selectedVote;
 	}
 
 	public void setSelectedVote(int selectedVote) {
 		this.selectedVote = selectedVote;
 	}
 
 	public String getHash1() {
 		return hash1;
 	}
 
 	public void setHash1(String hash1) {
 		this.hash1 = hash1;
 	}
 
 	public String getHash2() {
 		return hash2;
 	}
 
 	public void setHash2(String hash2) {
 		this.hash2 = hash2;
 	}
 
 	public String getDecoded1() {
 		return decoded1;
 	}
 
 	public void setDecoded1(String decoded1) {
 		this.decoded1 = decoded1;
 	}
 
 	public String getDecoded2() {
 		return decoded2;
 	}
 
 	public void setDecoded2(String decoded2) {
 		this.decoded2 = decoded2;
 	}
 
 	public int getReceiptId() {
 		return receiptId;
 	}
 
 	public void setReceiptId(int receiptId) {
 		this.receiptId = receiptId;
 	}
 
 }
