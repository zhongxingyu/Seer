 package de.wak_sh.client.backend.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import de.wak_sh.client.Utils;
 import de.wak_sh.client.backend.DataService;
 import de.wak_sh.client.backend.model.Message;
 
 public class MessageService {
 	private static final String READL_URL = "/c_email.html?&action=getviewmessagessingle&msg_uid=";
 
 	private DataService dataService;
 	private List<Message> messages;
 
 	public MessageService() {
 		dataService = DataService.getInstance();
 		messages = new ArrayList<Message>();
 	}
 
 	public void fetchMessages() throws IOException {
		String regex = "single&msg_uid=(\\d+)\">(.*?)<.*?<td>(.*?)</td>.*?<td>(.*?)&";
		// String regex =
		// "single&msg_uid=(.*?)\">(.*?)<.*?<td>(.*?)</td>.*?<td>(.*?)&.*?delete&msg_uid=(.*?)\">";
 		List<String[]> nachrichten = Utils.matchAll(regex,
 				dataService.getMessagesPage());
 		for (String[] nachricht : nachrichten) {
 			String id = nachricht[0];
 			String subject = nachricht[1];
 			String date = nachricht[2];
 			String sender = nachricht[3];
 			messages.add(new Message(id, date, sender, subject));
 		}
 	}
 
 	public String getMessagesContent(int index) throws IOException {
 		Message message = messages.get(index);
 		if (message.getContent() == null) {
 			String readUrl = READL_URL + message.getId();
 			message.setContent(Utils.match(dataService.get(readUrl),
 					"Nachricht:.+?<td>(.*?)</td>"));
 		}
 		return message.getContent();
 	}
 
 	public List<Message> getMessages() {
 		return messages;
 	}
 
 }
