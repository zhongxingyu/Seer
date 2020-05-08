 package com.podio.sample.zendesk;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.podio.ResourceFactory;
 import com.podio.comment.Comment;
 import com.podio.comment.CommentAPI;
 import com.podio.comment.CommentCreate;
 import com.podio.common.Reference;
 import com.podio.common.ReferenceType;
 import com.podio.contact.ContactAPI;
 import com.podio.contact.ProfileField;
 import com.podio.contact.ProfileType;
 import com.podio.file.FileAPI;
 import com.podio.integration.zendesk.APIFactory;
 import com.podio.integration.zendesk.attachment.Attachment;
 import com.podio.integration.zendesk.ticket.Ticket;
 import com.podio.integration.zendesk.ticket.TicketComment;
 import com.podio.integration.zendesk.ticket.TicketFieldEntry;
 import com.podio.integration.zendesk.user.User;
 import com.podio.item.FieldValuesUpdate;
 import com.podio.item.ItemAPI;
 import com.podio.item.ItemBadge;
 import com.podio.item.ItemCreate;
 import com.podio.item.ItemUpdate;
 import com.podio.item.ItemsResponse;
 import com.podio.oauth.OAuthClientCredentials;
 import com.podio.oauth.OAuthUsernameCredentials;
 import com.podio.tag.TagAPI;
 import com.podio.user.UserProfileMini;
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 public class Importer {
 
 	private static final String DEFAULT_PHOTO_FILENAME = "user_sm.png";
	@SuppressWarnings("unused")
 	private static final int VIEW_ALL = 47845;
 	private static final int VIEW_UPDATED_LAST_HOUR = 1992333;
 
 	private static final boolean SILENT = false;
 
 	private static final int SPACE_ID = 207;
 
 	private static final int TICKET_APP_ID = 30910;
 	private static final int TICKET_TITLE = 183671;
 	private static final int TICKET_DESCRIPTION = 183672;
 	private static final int TICKET_LOCATION = 183675;
 	private static final int TICKET_ASSIGNEE = 183674;
 	private static final int TICKET_REQUESTER = 183684;
 	private static final int TICKET_TYPE = 183677;
 	private static final int TICKET_STATUS = 183673;
 	private static final int TICKET_SOURCE = 183678;
 	private static final int TICKET_ZENDESK = 183679;
 
 	private static final int REQUESTER_APP_ID = 30911;
 	private static final int REQUESTER_NAME = 183680;
 	private static final int REQUESTER_MAIL = 183681;
 	private static final int REQUESTER_PHONE = 183682;
 	private static final int REQUESTER_PHOTO = 183683;
 
 	private static final Pattern SUBMITTED_FROM_PATTERN = Pattern
 			.compile("Submitted from: (.*)");
 
 	private static final Map<String, String> TYPE_MAP = new HashMap<String, String>();
 	static {
 		TYPE_MAP.put("question", "Question");
 		TYPE_MAP.put("bug", "Bug");
 		TYPE_MAP.put("freq", "Feature request");
 		TYPE_MAP.put("feature_request", "Feature request");
 	}
 
 	private final APIFactory zendeskAPI;
 
 	private final ResourceFactory podioAPI;
 
 	private Importer(String configFile) throws IOException {
 		super();
 
 		Properties properties = new Properties();
 		properties.load(new FileInputStream(configFile));
 
 		this.zendeskAPI = new APIFactory(
 				properties.getProperty("zendesk.domain"),
 				Boolean.parseBoolean(properties.getProperty("zendesk.ssl")),
 				properties.getProperty("zendesk.username"),
 				properties.getProperty("zendesk.password"));
		this.podioAPI = new ResourceFactory(properties.getProperty("podio.api"),
 				properties.getProperty("podio.upload"), 443, true, false,
 				new OAuthClientCredentials(properties
 						.getProperty("podio.client.mail"), properties
 						.getProperty("podio.client.secret")),
 				new OAuthUsernameCredentials(properties
 						.getProperty("podio.user.mail"), properties
 						.getProperty("podio.user.password")));
 	}
 
 	private ItemBadge getPodioTicket(int zendeskTicketId) {
 		ItemsResponse response = new ItemAPI(podioAPI).getItemsByExternalId(
 				TICKET_APP_ID, Integer.toString(zendeskTicketId));
 		if (response.getFiltered() < 1) {
 			return null;
 		}
 
 		return response.getItems().get(0);
 	}
 
 	private ItemBadge getPodioRequester(int zendeskRequesterId) {
 		ItemsResponse response = new ItemAPI(podioAPI).getItemsByExternalId(
 				REQUESTER_APP_ID, Integer.toString(zendeskRequesterId));
 		if (response.getFiltered() < 1) {
 			return null;
 		}
 
 		return response.getItems().get(0);
 	}
 
 	private UserProfileMini findPodioUser(int zendeskUserId) {
 		User zendeskUser = getZendeskUser(zendeskUserId);
 		if (zendeskUser == null) {
 			return null;
 		}
 
 		if (zendeskUser.getEmail() != null) {
 			List<UserProfileMini> podioUsers = new ContactAPI(podioAPI)
 					.getSpaceContacts(SPACE_ID, ProfileField.MAIL, zendeskUser
 							.getEmail().toLowerCase(), null, null,
 							ProfileType.MINI, null);
 			if (podioUsers.size() == 1) {
 				return podioUsers.get(0);
 			}
 		}
 
 		if (zendeskUser.getName() != null) {
 			List<UserProfileMini> podioUsers = new ContactAPI(podioAPI)
 					.getSpaceContacts(SPACE_ID, ProfileField.NAME,
 							zendeskUser.getName(), null, null,
 							ProfileType.MINI, null);
 			if (podioUsers.size() == 1) {
 				return podioUsers.get(0);
 			}
 		}
 
 		return null;
 	}
 
 	private User getZendeskUser(int zendeskUserId) {
 		try {
 			return zendeskAPI.getUserAPI().getUser(zendeskUserId);
 		} catch (UniformInterfaceException e) {
 			System.out
 					.println("Unable to find Zendesk user: " + e.getMessage());
 			return null;
 		}
 	}
 
 	private List<FieldValuesUpdate> getRequesterFields(User zendeskUser,
 			boolean uploadImage) throws IOException {
 		List<FieldValuesUpdate> fields = new ArrayList<FieldValuesUpdate>();
 		fields.add(new FieldValuesUpdate(REQUESTER_NAME, "value", StringUtils
 				.abbreviate(zendeskUser.getName(), 128)));
 		if (zendeskUser.getEmail() != null) {
 			fields.add(new FieldValuesUpdate(REQUESTER_MAIL, "value",
 					zendeskUser.getEmail()));
 		}
 		if (zendeskUser.getPhone() != null) {
 			fields.add(new FieldValuesUpdate(REQUESTER_PHONE, "value",
 					zendeskUser.getPhone()));
 		}
 		if (zendeskUser.getPhotoURL() != null
 				&& uploadImage
 				&& !zendeskUser.getPhotoURL().getFile()
 						.endsWith(DEFAULT_PHOTO_FILENAME)) {
 			Integer photoImageId = upload(zendeskUser.getPhotoURL(), null, null);
 			if (photoImageId != null) {
 				fields.add(new FieldValuesUpdate(REQUESTER_PHOTO, "value",
 						photoImageId));
 			}
 		}
 
 		return fields;
 	}
 
 	private String trimLocation(String text) {
 		int dashIdx = text.indexOf("------------------");
 		if (dashIdx != -1) {
 			return text.substring(0, dashIdx).trim();
 		} else {
 			return text;
 		}
 	}
 
 	private String trimLineBreaks(String text) {
 		text = text.replace('\n', ' ');
 		text = text.replace('\r', ' ');
 		while (text.contains("  ")) {
 			text = text.replace("  ", " ");
 		}
 
 		return text;
 	}
 
 	private List<FieldValuesUpdate> getTicketFields(Ticket ticket)
 			throws IOException {
 		List<FieldValuesUpdate> fields = new ArrayList<FieldValuesUpdate>();
 		fields.add(new FieldValuesUpdate(TICKET_TITLE, "value",
 				trimLineBreaks(trimLocation(StringUtils.abbreviate(
 						ticket.getDescription(), 96)))));
 		fields.add(new FieldValuesUpdate(TICKET_DESCRIPTION, "value",
 				trimLocation(ticket.getDescription())));
 
 		Matcher matcher = SUBMITTED_FROM_PATTERN.matcher(ticket
 				.getDescription());
 		if (matcher.find()) {
 			String from = matcher.group(1);
 			if (from != null) {
 				fields.add(new FieldValuesUpdate(TICKET_LOCATION, "value", from));
 			}
 		}
 
 		if (ticket.getAssigneeId() != null) {
 			UserProfileMini podioUser = findPodioUser(ticket.getAssigneeId());
 			if (podioUser != null) {
 				fields.add(new FieldValuesUpdate(TICKET_ASSIGNEE, "value",
 						podioUser.getId()));
 			}
 		}
 
 		Integer podioRequesterId = updateRequester(ticket.getRequesterId());
 		if (podioRequesterId != null) {
 			fields.add(new FieldValuesUpdate(TICKET_REQUESTER, "value",
 					podioRequesterId));
 		}
 		if (ticket.getEntries() != null) {
 			for (TicketFieldEntry entry : ticket.getEntries()) {
 				if (entry.getFieldId() == 87154
 						&& StringUtils.isNotBlank(entry.getValue())) {
 					String podioValue = TYPE_MAP.get(entry.getValue());
 					if (podioValue != null) {
 						fields.add(new FieldValuesUpdate(TICKET_TYPE, "value",
 								podioValue));
 					} else {
 						System.out.println("Unknown ticket type "
 								+ entry.getValue());
 					}
 				}
 			}
 		}
 		fields.add(new FieldValuesUpdate(TICKET_STATUS, "value",
 				toPodioState(ticket.getStatus())));
 		if (ticket.getVia() != null) {
 			fields.add(new FieldValuesUpdate(TICKET_SOURCE, "value",
 					toPodioState(ticket.getVia())));
 		}
 		fields.add(new FieldValuesUpdate(TICKET_ZENDESK, "value",
 				"http://hoist.zendesk.com/tickets/" + ticket.getId()));
 
 		return fields;
 	}
 
 	private String toPodioState(Enum<?> en) {
 		String name = en.name().toLowerCase();
 		name = name.replace('_', ' ');
 		return StringUtils.capitalize(name);
 	}
 
 	private Integer updateRequester(int requesterZendeskId) throws IOException {
 		User requesterZendesk = getZendeskUser(requesterZendeskId);
 		if (requesterZendesk == null) {
 			return null;
 		}
 
 		ItemBadge requesterPodio = getPodioRequester(requesterZendeskId);
 		if (requesterPodio != null) {
 			if (requesterPodio.getCurrentRevision().getCreatedOn()
 					.isBefore(requesterZendesk.getUpdatedAt())) {
 				List<FieldValuesUpdate> fields = getRequesterFields(
 						requesterZendesk, false);
 
 				new ItemAPI(podioAPI).updateItem(requesterPodio.getId(),
 						new ItemUpdate(Integer.toString(requesterZendeskId),
 								fields), true);
 			}
 
 			return requesterPodio.getId();
 		} else {
 			List<FieldValuesUpdate> fields = getRequesterFields(
 					requesterZendesk, true);
 
 			return new ItemAPI(podioAPI).addItem(REQUESTER_APP_ID,
 					new ItemCreate(Integer.toString(requesterZendeskId),
 							fields, Collections.<Integer> emptyList(),
 							Collections.<String> emptyList()), true);
 		}
 	}
 
	public void updateTickets() throws IOException {
 		int page = 1;
 		while (true) {
			List<Ticket> tickets = zendeskAPI.getTicketAPI().getTickets(
					VIEW_UPDATED_LAST_HOUR, page);
 			System.out.println("Doing page " + page + " with " + tickets.size()
 					+ " tickets");
 			for (Ticket ticket : tickets) {
 				updateTicket(ticket.getId());
 			}
 
 			page++;
 
 			if (tickets.size() < 30) {
 				return;
 			}
 		}
 	}
 
 	public void updateTicket(int ticketIdZendesk) throws IOException {
 		Ticket ticketZendesk = zendeskAPI.getTicketAPI().getTicket(
 				ticketIdZendesk);
 		updateTicket(ticketZendesk);
 	}
 
 	private void updateTicket(Ticket ticketZendesk) throws IOException {
 		ItemBadge ticketPodio = getPodioTicket(ticketZendesk.getId());
 		if (ticketPodio != null) {
 			boolean before = ticketPodio.getCurrentRevision().getCreatedOn()
 					.isBefore(ticketZendesk.getUpdatedAt());
 			if (before) {
 				List<FieldValuesUpdate> fields = getTicketFields(ticketZendesk);
 
 				new ItemAPI(podioAPI).updateItem(ticketPodio.getId(),
 						new ItemUpdate(Integer.toString(ticketZendesk.getId()),
 								fields), true);
 
 				Reference itemReference = new Reference(ReferenceType.ITEM,
 						ticketPodio.getId());
 
 				new TagAPI(podioAPI).updateTags(itemReference,
 						ticketZendesk.getCurrentTags());
 
 				List<Comment> commentsPodio = new CommentAPI(podioAPI)
 						.getComments(itemReference);
 				updateComments(ticketZendesk, ticketPodio.getId(), false,
 						commentsPodio);
 			}
 		} else {
 			List<FieldValuesUpdate> fields = getTicketFields(ticketZendesk);
 
 			int ticketIdPodio = new ItemAPI(podioAPI).addItem(TICKET_APP_ID,
 					new ItemCreate(Integer.toString(ticketZendesk.getId()),
 							fields, Collections.<Integer> emptyList(),
 							ticketZendesk.getCurrentTags()), SILENT);
 
 			updateComments(ticketZendesk, ticketIdPodio, true,
 					Collections.<Comment> emptyList());
 		}
 	}
 
 	private void updateComments(Ticket ticketZendesk, int ticketIdPodio,
 			boolean newTicket, List<Comment> commentsPodio) throws IOException {
 		System.out.println(ticketZendesk.getComments());
 		if (ticketZendesk.getComments() != null) {
 			for (TicketComment commentZendesk : ticketZendesk.getComments()) {
 				if (commentZendesk.getValue().equals(
 						ticketZendesk.getDescription())) {
 					if (newTicket) {
 						uploadAttachment(
 								commentZendesk.getAttachments(),
 								new Reference(ReferenceType.ITEM, ticketIdPodio));
 					}
 				} else {
 					boolean found = false;
 					for (Comment commentPodio : commentsPodio) {
 						if (commentPodio.getValue().contains(
 								commentZendesk.getValue())) {
 							found = true;
 						}
 					}
 
 					if (!found) {
 						addComment(ticketIdPodio,
 								ticketZendesk.getRequesterId(), commentZendesk);
 					}
 				}
 			}
 		}
 	}
 
 	private void addComment(int ticketIdPodio, int requesterIdZendesk,
 			TicketComment commentZendesk) throws IOException {
 		String commentText = commentZendesk.getValue();
 		commentText += "<br /><br />";
 		if (requesterIdZendesk == commentZendesk.getAuthorId()) {
 			ItemBadge requesterPodio = getPodioRequester(commentZendesk
 					.getAuthorId());
 			if (requesterPodio != null) {
 				commentText += "<a href=\"" + requesterPodio.getLink() + "\">"
 						+ requesterPodio.getTitle() + "</a>";
 			} else {
 				commentText += "Unknown";
 			}
 		} else {
			UserProfileMini podioUser = findPodioUser(commentZendesk.getAuthorId());
 			if (podioUser != null) {
 				commentText += "<a href=\"https://podio.com/contacts/"
 						+ podioUser.getId() + "\">" + podioUser.getName()
 						+ "</a>";
 			} else {
 				User zendeskUser = getZendeskUser(commentZendesk.getAuthorId());
 				commentText += "<a href=\"mailto:" + zendeskUser.getEmail()
 						+ "\">" + zendeskUser.getName() + "</a>";
 			}
 		}
 
 		List<Integer> fileIds = uploadAttachment(
 				commentZendesk.getAttachments(), null);
 
 		new CommentAPI(podioAPI).addComment(new Reference(ReferenceType.ITEM,
 				ticketIdPodio), new CommentCreate(commentText, null,
 				Collections.<Integer> emptyList(), fileIds), SILENT);
 	}
 
 	private List<Integer> uploadAttachment(List<Attachment> attachments,
 			Reference reference) throws IOException {
 		List<Integer> fileIds = new ArrayList<Integer>();
 		for (Attachment attachment : attachments) {
			URL url = new URL("http://hoist.zendesk.com/attachments/token/"
 					+ attachment.getToken() + "/?name="
 					+ attachment.getFilename());
 
 			Integer fileId = upload(url, attachment.getFilename(), reference);
 			if (fileId != null) {
 				fileIds.add(fileId);
 			}
 		}
 
 		return fileIds;
 	}
 
 	private Integer upload(URL url, String name, Reference reference)
 			throws IOException {
 		try {
 			return new FileAPI(podioAPI).uploadImage(url, name, reference);
 		} catch (FileNotFoundException e) {
 			System.err.println(url + " could not be uploaded: "
 					+ e.getMessage());
 			return null;
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		Importer publisher = new Importer(args[0]);
		// publisher.updateTicket(933);
		publisher.updateTickets();
 	}
 }
