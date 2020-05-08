 package com.unisender;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.unisender.entities.Campaign;
 import com.unisender.entities.Contacts;
 import com.unisender.entities.EmailMessage;
 import com.unisender.entities.Field;
 import com.unisender.entities.FieldData;
 import com.unisender.entities.FieldType;
 import com.unisender.entities.LogMessage;
 import com.unisender.entities.MailList;
 import com.unisender.entities.Person;
 import com.unisender.entities.SmsMessage;
 import com.unisender.entities.Tag;
 import com.unisender.entities.User;
 import com.unisender.entities.UserInfo;
 import com.unisender.exceptions.MethodExceptionCode;
 import com.unisender.exceptions.UniSenderConnectException;
 import com.unisender.exceptions.UniSenderInvalidResponseException;
 import com.unisender.exceptions.UniSenderMethodException;
 import com.unisender.requests.BatchSendEmailRequest;
 import com.unisender.requests.CheckUserExistsRequest;
 import com.unisender.requests.CreateCampaignRequest;
 import com.unisender.requests.CreateEmailMessageRequest;
 import com.unisender.requests.CreateSmsMessageRequest;
 import com.unisender.requests.ExcludeRequest;
 import com.unisender.requests.ExportContactsRequest;
 import com.unisender.requests.GetCampaignDeliveryStatsRequest;
 import com.unisender.requests.ImportContactsRequest;
 import com.unisender.requests.RegisterRequest;
 import com.unisender.requests.SendEmailRequest;
 import com.unisender.requests.SubscribeRequest;
 import com.unisender.requests.TransferMoneyRequest;
 import com.unisender.requests.ValidateSenderRequest;
 import com.unisender.responses.ActivateContactsResponse;
 import com.unisender.responses.CheckUserExistsResponse;
 import com.unisender.responses.GetCampaignDeliveryStatsResponse;
 import com.unisender.responses.ImportContactsResponse;
 import com.unisender.responses.SendEmailResponse;
 import com.unisender.responses.SendSmsResponse;
 import com.unisender.responses.TransferMoneyResponse;
 import com.unisender.utils.MapUtils;
 import com.unisender.utils.StringUtils;
 import com.unisender.utils.URLEncodedUtils;
 
 public class UniSender {
 	
 	private String apiKey;
 	private String language;
 	private Boolean useHttps;
 	private boolean isTestMode = false;
 	
 	static final String API_HOST = "api.unisender.com";
 	static final String API_ENCODING = "UTF-8";
 	
 	
 	
 	public UniSender(String apiKey) {
 		this(apiKey, "ru", false, false);
 	}
 	
 	public UniSender(String apiKey, boolean isTestMode) {
 		this(apiKey, "ru", isTestMode, false);
 	}
 
 	public UniSender(String apiKey, String language, boolean isTestMode , boolean useHttps) {
 		super();
 		this.apiKey = apiKey;
 		this.language = language;
 		this.useHttps = useHttps;
 		this.isTestMode = isTestMode;
 	}
 	
 	public UniSender(Config config){
 		this(config.getApiKey(), config.getLanguage(), config.isTestMode(), config.useHttps());
 	}
 
 	private URL makeURL(String method) {
 		return makeURL(this.language, method);
 	}
 
 	private URL makeURL(String language, String method) {
 		String file = String.format("/%s/api/%s?format=json%s",
 				language,
 				method,
 				isTestMode ? "&test_mode=1" : "");
 		try {
 			return new URL(useHttps ? "https" : "http", API_HOST, file);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private String makeQuery(Map<String, String> args) {
 		if (args == null){
 			args = new HashMap<String, String>(1);
 		}
 		
 		args.put("api_key", this.apiKey);
 		
 		return URLEncodedUtils.formatQuery(args, API_ENCODING);
 	}
 
 	private void checkErrors(JSONObject response)  throws UniSenderMethodException, UniSenderInvalidResponseException{
 		if (response.has("error")){
 			try {
 				String errorMsg = response.getString("error");
 				String code = response.getString("code");
 				
 				MethodExceptionCode mec = MethodExceptionCode.UNKNOWN;
 				if ("unspecified".equals(code)){
 					mec = MethodExceptionCode.UNSPECIFIED;
 				} else if ("invalid_api_key".equals(code)) {
 					mec = MethodExceptionCode.INVALID_API_KEY;
 				} else if ("access_denied".equals(code)) {
 					mec = MethodExceptionCode.ACCESS_DENIED;
 				} else if ("unknown_method".equals(code)) {
 					mec = MethodExceptionCode.UNKNOWN_METHOD;
 				} else if ("invalid_arg".equals(code)) {
 					mec = MethodExceptionCode.INVALID_ARG;
 				} else if ("not_enough_money".equals(code)) {
 					mec = MethodExceptionCode.NOT_ENOUGH_MONEY;
 				} else if ("too_many_double_optins".equals(code)) {
                     mec = MethodExceptionCode.TOO_MANY_DOUBLE_OPTINS;
                 }
 				throw new UniSenderMethodException(mec, errorMsg);
 				
 			} catch (JSONException e) {
 				throw new UniSenderInvalidResponseException(e);
 			}
 		}
 	}
 	
 	protected JSONObject executeMethod(String method, Map<String, String> args) 
 					throws UniSenderConnectException, UniSenderInvalidResponseException, UniSenderMethodException {
 		URL url = makeURL(method);
 		String output = execute(url, makeQuery(args));
 		try {
 			JSONObject response = new JSONObject(output);
 			checkErrors(response);
 			return response;
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e, output);
 		}
 	}
 
 	protected String execute(URL url, String postQuery) throws UniSenderConnectException {
 		HttpURLConnection urlc = null;
 		try {
 			urlc = (HttpURLConnection) url.openConnection();
 			urlc.setUseCaches(false);
 			urlc.setInstanceFollowRedirects(false);
 
 			urlc.setRequestMethod("POST");
 			urlc.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
 			urlc.setRequestProperty("Content-Length", "" + postQuery.getBytes().length);
 			urlc.setRequestProperty("Accept", "application/json, text/html, text/plain, text/javascript");
 
 			urlc.setDoOutput(true);
 			urlc.setDoInput(true);
 
 			DataOutputStream os = new DataOutputStream(urlc.getOutputStream());
 			os.writeBytes(postQuery);
 			os.flush();
 			os.close();
 
 			InputStreamReader isr = new InputStreamReader(urlc.getInputStream());
 			BufferedReader rd = new BufferedReader(isr);
 
 			char[] buffer = new char[255];
 			int read = 0;
 			StringBuilder sb = new StringBuilder();
 			while ((read = rd.read(buffer)) != -1) {
 				sb.append(buffer, 0, read);
 			}
 			rd.close();
 
 			return sb.toString();
 		} catch (IOException e){
 			throw new UniSenderConnectException(e);
 		} finally {
 			if (urlc != null){
 				urlc.disconnect();
 			}
 		}
 	}
 	private Map<String, String> createMap(){
 		return new HashMap<String, String>();
 	}
 	
 	private Map<String, String> createMap(String argName, String argVal){
 		Map<String, String> m = createMap();
 		m.put(argName, argVal);
 		return m;
 	}
 	
 	public java.util.List<MailList> getLists() throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		JSONObject response = executeMethod("getLists", null);
 		try {
 			JSONArray result = response.getJSONArray("result");
 			java.util.List<MailList> lists = new ArrayList<MailList>();
 			
 			for (int i = 0; i < result.length(); ++i) {
 				JSONObject jso = result.getJSONObject(i);
 				final Integer id = jso.getInt("id");
 				final String title = jso.getString("title");
 				lists.add(new MailList(id, title));
 			}
 			
 			return lists;
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public MailList createList(MailList list) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> p = createMap("title", list.getTitle());
 		
 		JSONObject response = executeMethod("createList", p);
 		try {
 			JSONObject result = response.getJSONObject("result");
 			list.setId(result.getInt("id"));
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 		return list;
 	}
 	
 	public Person subscribe(SubscribeRequest sr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		
 		Person person = sr.getFields();
 		if (person.getEmail() == null && person.getPhone() == null){
 			throw new IllegalStateException("fields.email or fields.phone must be defined");
 		}
 		Map<String, String> map = createMap();
 		
 		MapUtils.putIfNotNull(map, "list_ids", StringUtils.joinMailList(sr.getLists(), ","));
 		
 		MapUtils.putIfNotNull(map, "fields[email]", person.getEmail());
 		MapUtils.putIfNotNull(map, "fields[phone]", person.getPhone());
 		
 		List<Field> fields = person.getFields();
 		if (fields != null && !fields.isEmpty()){
 			for (Field f: fields){
 				MapUtils.putIfNotNull(map,
 						"fields["+ f.getName() +"]",
 						f.getValue());
 			}
 		}
 		
 		MapUtils.putIfNotNull(map, "tags", sr.getTags());
 		MapUtils.putIfNotNull(map, "request_ip", sr.getRequestIp());
 		MapUtils.putIfNotNull(map, "request_time", sr.getRequestTime());
 		MapUtils.putIfNotNull(map, "double_optin", sr.getDoubleOptin());
 		MapUtils.putIfNotNull(map, "confirm_ip", sr.getConfirmIp());
 		MapUtils.putIfNotNull(map, "confirm_time", sr.getConfirmTime());
 		MapUtils.putIfNotNull(map, "overwrite", sr.getOverwrite());
 		
 		JSONObject response = executeMethod("subscribe", map);
 		try {
 			person.setId(response.getInt("person_id"));
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 		return person;
 	}
 	
 	public void exclude(ExcludeRequest er) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		map.put("contact_type", er.getContactType().toString());
 		map.put("contact", er.getContact());
 		List<MailList> l = er.getListsIds();
 
 		if (l != null && !l.isEmpty()){
 			map.put("list_ids", StringUtils.joinMailList(l, ","));
 		}
 		
 		executeMethod("exclude", map);
 	}
 	
 	public void unsubscribe(ExcludeRequest er) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		map.put("contact_type", er.getContactType().toString());
 		map.put("contact", er.getContact());
 		List<MailList> l = er.getListsIds();
 
 		if (l != null && !l.isEmpty()){
 			map.put("list_ids", StringUtils.joinMailList(l, ","));
 		}
 		
 		executeMethod("unsubscribe", map);
 	}
 		
 	public ImportContactsResponse importContacts(ImportContactsRequest ic) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		FieldData fd = ic.getFieldData(); 
 		
 		int filedsC = fd.getFiledCount();
 		for (int i = 0; i < filedsC; ++i)
 		{
 			map.put("field_names[" + i + "]", fd.getField(i));
 		}
 		
 		int dataC = fd.getDataCount();
 		for (int i = 0; i < dataC; ++i)
 		{
 			List<String> data = fd.getData(i);
 			int dn = 0;
 			for (String d: data){
 				map.put(String.format(
 						"data[%s][%s]", i, dn),
 						d);
 				++dn;
 			}
 		}
 		
 		MapUtils.putIfNotNull(map, "double_optin", ic.getDoubleOptin());
 		MapUtils.putIfNotNull(map, "overwrite_tags", ic.getOverwriteTags());
 		MapUtils.putIfNotNull(map, "overwrite_lists", ic.getOverwriteLists());
 		
 		JSONObject response = executeMethod("importContacts", map);
 		try {
 			List<LogMessage> logs = null;
 			JSONObject res = response.getJSONObject("result");
 			JSONArray log  = res.optJSONArray("log");
 			
 			if (log != null){
 				logs = new ArrayList<LogMessage>();
 				for (int i = 0; i < log.length(); ++i)
 				{
 					JSONObject jso = log.getJSONObject(i);
 					logs.add( new LogMessage(
 						jso.getInt("index"),
 						jso.getString("code"),
 						jso.getString("message")
 					));
 				}
 			}
 			
 			return new ImportContactsResponse(
 					res.getInt("total"),
 					res.getInt("inserted"),
 					res.getInt("updated"),
 					res.getInt("deleted"),
 					res.getInt("new_emails"),
 					logs
 			);
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public ActivateContactsResponse activateContacts(Contacts contacts) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		map.put("contact_type", contacts.getContactType().toString());
 		
 		List<MailList> listIds = contacts.getListsIds();
 		if (listIds != null){
 			MapUtils.putIfNotNull(map, "list_ids", StringUtils.joinMailList(listIds, ","));
 		} else {
 			MapUtils.putIfNotNull(map, "contacts", contacts.getContacts());
 		}
         MapUtils.putIfNotNull(map, "invite", contacts.getInvite());
         MapUtils.putIfNotNull(map, "comment", contacts.getComment());
 
 		JSONObject response = executeMethod("activateContacts", map);
 		try {
 			JSONObject result = response.getJSONObject("result");
 
             if(1 == contacts.getInvite()) {
                 return new ActivateContactsResponse(result.getInt("campaign_id"), result.getString("campaign_status"));
             } else {
                 Integer activated = result.getInt("activated");
                 int activationRI = result.optInt("activation_request_id", -1);
                 if (activationRI == -1) {
                     return new ActivateContactsResponse(activated);
                 } else {
                     return new ActivateContactsResponse(activated, activationRI);
                 }
             }
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 		
 	}
 	
 	private void addEmailMessage(Map<String, String> map, EmailMessage em){
 		MapUtils.putIfNotNull(map, "sender_name", em.getSenderName());
 		MapUtils.putIfNotNull(map, "sender_email", em.getSenderEmail());
 		MapUtils.putIfNotNull(map, "subject", em.getSubject());
 		MapUtils.putIfNotNull(map, "body", em.getBody());
 		
 		MapUtils.putIfNotNull(map, "lang", em.getLang());
 		MapUtils.putIfNotNull(map, "attachments", em.getAttachments());
 	}
 	
 	public EmailMessage createEmailMessage(CreateEmailMessageRequest cr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		EmailMessage em = cr.getEmailMessage();
 		
 		addEmailMessage(map, em);
 		MapUtils.putIfNotNull(map, "list_id", cr.getListId().getId());
 
 		MapUtils.putIfNotNull(map, "tag", cr.getTag());
 		MapUtils.putIfNotNull(map, "series_day", cr.getSeriesDay());
 		MapUtils.putIfNotNull(map, "series_time", cr.getSeriesTime());
 		
 		JSONObject response = executeMethod("createEmailMessage", map);
 		try {
 			JSONObject result = response.getJSONObject("result");
 			em.setId(result.getInt("message_id"));
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 		
 		return em; 
 	}
 	
 	public SmsMessage createSmsMessage(CreateSmsMessageRequest cr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		SmsMessage sm = cr.getSmsMessage();
 		
 		MapUtils.putIfNotNull(map, "sender", sm.getSender());
 		MapUtils.putIfNotNull(map, "body", sm.getBody());
 		MapUtils.putIfNotNull(map, "list_id", cr.getListId().getId());
 		
 		MapUtils.putIfNotNull(map, "tag", cr.getTag());
 		MapUtils.putIfNotNull(map, "series_day", cr.getSeriesDay());
 		MapUtils.putIfNotNull(map, "series_time", cr.getSeriesTime());
 		
 		JSONObject response = executeMethod("createSmsMessage", map);
 		try {
 			JSONObject result = response.getJSONObject("result");
 			sm.setId(result.getInt("message_id"));
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 		return sm;
 	}
 	public Campaign createCampaign(CreateCampaignRequest cr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		
 		MapUtils.putIfNotNull(map, "message_id", cr.getMessageId().getId());
 		
 		MapUtils.putIfNotNull(map, "start_time", cr.getStartTime());
 		MapUtils.putIfNotNull(map, "timezone", cr.getTimezone());
 		MapUtils.putIfNotNull(map, "track_read", cr.getTrackRead());
 		MapUtils.putIfNotNull(map, "track_links", cr.getTrackLinks());
 		MapUtils.putIfNotNull(map, "contacts", cr.getContacts());
 		MapUtils.putIfNotNull(map, "defer", cr.getDefer());
 		
 		MapUtils.putIfNotNull(map, "track_ga", cr.getTrackGa());
 		MapUtils.putIfNotNull(map, "ga_medium", cr.getGaMedium());
 		MapUtils.putIfNotNull(map, "ga_source", cr.getGaSource());
 		MapUtils.putIfNotNull(map, "ga_campaign", cr.getGaCampaign());
 		MapUtils.putIfNotNull(map, "ga_content", cr.getGaContent());
 		MapUtils.putIfNotNull(map, "ga_term", cr.getGaTerm());
 		
 		JSONObject response = executeMethod("createCampaign", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return new Campaign(
 					res.getInt("campaign_id"),
 					res.getString("status"),
 					res.getInt("Count")
 			);
 			
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	public GetCampaignDeliveryStatsResponse getCampaignDeliveryStats(GetCampaignDeliveryStatsRequest sr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "campaign_id", sr.getCampaign().getId());
 		MapUtils.putIfNotNull(map, "changed_since", sr.getChanged_since());
 		
 		JSONObject response = executeMethod("getCampaignDeliveryStats", map);
 		try {
 			final JSONObject res = response.getJSONObject("result");
 			final List<String> fields = new ArrayList<String>();
 			final List<List<String>> data = new ArrayList<List<String>>();
 			
 			final JSONArray jsFields = res.getJSONArray("fields");
 			for (int i = 0; i < jsFields.length(); ++i)
 			{
 				fields.add(jsFields.getString(i));
 			}
 			JSONArray jsData = res.getJSONArray("data");
 			for (int i = 0; i < jsData.length(); ++i)
 			{
 				ArrayList<String> info = new ArrayList<String>();
 				data.add(info);
 				JSONArray jsDataFields = jsData.getJSONArray(i);
 				
 				for (int j = 0; j < jsDataFields.length(); ++j)
 				{
 					info.add(jsDataFields.getString(j));
 				}
 			}
 		
 			return new GetCampaignDeliveryStatsResponse(fields, data);			
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 			
 	}	
 	public SendSmsResponse sendSms(
 			String phone,
 			SmsMessage smsMessage) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		
 		MapUtils.putIfNotNull(map, "phone", phone);
 		MapUtils.putIfNotNull(map, "sender", smsMessage.getSender());
 		MapUtils.putIfNotNull(map, "text", smsMessage.getBody());
 		
 		JSONObject response = executeMethod("sendSms", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return new SendSmsResponse(
 					res.getString("currency"),
 					res.getDouble("price"),
 					res.getString("sms_id")
 			);
 			
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	/**
 	 * 
 	 * @param smsId Код сообщения, возвращённый методом sendSms.
 	 * @return status http://www.unisender.com/ru/help/api/checkSms
 	 * <pre>
 	 * ok_sent	 Сообщение отправлено, но статус доставки пока неизвестен. Статус временный и может измениться. 
 	 * ok_delivered	 Сообщение доставлено. Статус окончательный. 
 	 * err_delivery_failed	 Доставка не удалась. Статус окончательный. 
 	 * err_not_allowed	 Доставка невозможна, этот оператор связи не обслуживается. Статус окончательный. 
 	 * err_dest_invalid	 Доставка невозможна, указан неправильный номер. Статус окончательный.
 	 * </pre>
 	 */
 	public String checkSms(String smsId) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "sms_id ", smsId);
 		JSONObject response = executeMethod("checkSms", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return res.getString("status");
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	public List<SendEmailResponse> sendEmail(SendEmailRequest sr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
         Map<String, String> map = createMap();
 
         MapUtils.putIfNotNull(map, "email", sr.getEmail());
         addEmailMessage(map, sr.getEmailMessage());
 
         MapUtils.putIfNotNull(map, "list_id", sr.getListId());
         MapUtils.putIfNotNull(map, "track_read", sr.getTrackRead());
         MapUtils.putIfNotNull(map, "track_links", sr.getTrackLinks());
         MapUtils.putIfNotNull(map, "attach_multi", sr.getAttachMulti());
 
         return executeSendEmail(map);
 	}
 
     /**
      * Calls 'sendEmail' for a batch of messages according to array syntax.
      *
      * @param request batch request
      * @return responses for each sent email, never null
      *
      * @throws UniSenderConnectException when connection error occurs
      * @throws UniSenderMethodException when server returns error
      * @throws UniSenderInvalidResponseException when server response cannot be parsed
      *
      * @see http://www.unisender.com/ru/help/api/sendEmail.html
      */
     public List<SendEmailResponse> batchSendEmail(BatchSendEmailRequest request) throws UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
         Map<String, EmailMessage> messagesByReceiverEmail = request.getMessagesByReceiverEmail();
 
         if (messagesByReceiverEmail.isEmpty()) {
             return Collections.emptyList();
         }
 
         Map<String, String> map = createMap();
         Set<String> allAttachments = new HashSet<String>();
 
         int i = 0;
         for (Map.Entry<String, EmailMessage> entry : messagesByReceiverEmail.entrySet()) {
             String receiverEmail = entry.getKey();
             EmailMessage emailMessage = entry.getValue();
             addIndexedEmailMessage(map, i, receiverEmail, emailMessage);
 
             allAttachments.add(emailMessage.getAttachments());
             i++;
         }
 
         MapUtils.putIfNotNull(map, "lang", request.getLang());
         MapUtils.putIfNotNull(map, "list_id", request.getMailList());
         MapUtils.putIfNotNull(map, "track_read", request.getTrackRead());
         MapUtils.putIfNotNull(map, "track_links", request.getTrackLinks());
         MapUtils.putIfNotNull(map, "attach_multi", allAttachments.size() > 1 ? 1 : 0);
 
         return executeSendEmail(map);
     }
 
     private void addIndexedEmailMessage(Map<String, String> map, int index, String receiverEmail, EmailMessage emailMessage) {
         MapUtils.putIfNotNull(map, indexed("email", index), receiverEmail);
 
         MapUtils.putIfNotNull(map, indexed("sender_name", index), emailMessage.getSenderName());
         MapUtils.putIfNotNull(map, indexed("sender_email", index), emailMessage.getSenderEmail());
         MapUtils.putIfNotNull(map, indexed("subject", index), emailMessage.getSubject());
         MapUtils.putIfNotNull(map, indexed("body", index), emailMessage.getBody());
 
         MapUtils.putIfNotNull(map, indexed("attachments", index), emailMessage.getAttachments());
     }
 
     private static String indexed(String property, int index) {
         return property + '[' + index + ']';
     }
 
     private List<SendEmailResponse> executeSendEmail(Map<String, String> map)
             throws UniSenderConnectException, UniSenderInvalidResponseException, UniSenderMethodException {
         JSONObject response = executeMethod("sendEmail", map);
         try {
             final List<SendEmailResponse> result = new LinkedList<SendEmailResponse>();
             final JSONObject res = response.optJSONObject("result");
             if (res == null) {
                 //we've got an array
                 JSONArray resa = response.getJSONArray("result");
                 for (int i = 0; i < resa.length(); ++i) {
                     final JSONObject jso = resa.getJSONObject(i);
                    result.add(new SendEmailResponse(jso.getString("email"), jso.optString("id"), jso.optString("error")));
                 }
             } else {
                result.add(new SendEmailResponse(res.getString("email"), res.optString("email_id"), res.optString("error")));
             }
 
             return result;
         } catch (JSONException e) {
             throw new UniSenderInvalidResponseException(e);
         }
     }
 
     /**
 	 * 
 	 * @param emailId Код сообщения, возвращённый методом sendEmail.
 	 * @return status 
 	 * @see <a href="http://www.unisender.com/ru/help/api/checkEmail">http://www.unisender.com/ru/help/api/checkEmail</a>
 	 * <pre>
 	 * not_sent	 Сообщение пока ещё не отправлено - находится в очереди на отправку. 
 	 * ok_sent	 Сообщение отправлено, но статус доставки пока неизвестен. Статус временный и может измениться. 
 	 * ok_delivered	 Сообщение доставлено. Может измениться на 'ok_read' или 'ok_link_visited'. 
 	 * err_delivery_failed	 Доставка не удалась. Статус окончательный. 
 	 * err_will_retry	 Одна или несколько попыток доставки оказались неудачными, но попытки продолжаются. Статус неокончательный. 
 	 * err_spam_rejected	 Письмо отклонено сервером как спам. Статус окончательный. 
 	 * err_mailbox_full	 Почтовый ящик получателя переполнен. Статус окончательный.
 	 * </pre>
 	 */
 	public String checkEmail(String emailId) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "email_id", emailId);
 		JSONObject response = executeMethod("checkEmail", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return res.getString("status");
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public Field createField(Field field) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "name", field.getName());
 		MapUtils.putIfNotNull(map, "type", field.getFieldType());
 		MapUtils.putIfNotNull(map, "is_visible", field.getIsVisible());
 		MapUtils.putIfNotNull(map, "view_pos", field.getViewPos());
 		JSONObject response = executeMethod("createField", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			field.setId(res.getInt("id"));
 			return field;
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public List<Field> getFields() throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		JSONObject response = executeMethod("getFields", null);
 		try {
 			final List<Field> result = new ArrayList<Field>();
 			final JSONArray res = response.getJSONArray("result");
 			
 			for (int i = 0; i < res.length(); ++i)
 			{
 				final JSONObject jfield = res.getJSONObject(i);
 				
 				FieldType type = FieldType.STRING;
 				String ttype = jfield.getString("type");
 				if ("text".equals(ttype)) {
 					type = FieldType.TEXT;
 				} else if ("number".equals(ttype)) {
 					type = FieldType.NUMBER;
 				} else if ("bool".equals(ttype)) {
 					type = FieldType.BOOL;
 				}
 				
 				final Field field = new Field(
 						jfield.getInt("id"),
 						jfield.getString("name"),
 						null, // no value in this response
 						type,
 						jfield.getInt("is_visible"),
 						jfield.getInt("view_pos")						
 				);
 				result.add(field);
 			}
 			return result;
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public Field updateFiled(Field field) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "id", field.getId());
 		MapUtils.putIfNotNull(map, "name", field.getName());
 		MapUtils.putIfNotNull(map, "type", field.getFieldType());
 		MapUtils.putIfNotNull(map, "is_visible", field.getIsVisible());
 		MapUtils.putIfNotNull(map, "view_pos", field.getViewPos());
 		JSONObject response = executeMethod("updateFiled", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			field.setId(res.getInt("id"));
 			return field;
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	public void deleteField(Field field) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "id", field.getId());
 		executeMethod("deleteField", map);
 	}
 	
 	
 	public List<Tag> getTags() throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		JSONObject response = executeMethod("getTags", null);
 		try {
 			final List<Tag> result = new ArrayList<Tag>();
 			final JSONArray res = response.getJSONArray("result");
 			
 			for (int i = 0; i < res.length(); ++i)
 			{
 				final JSONObject jtag = res.getJSONObject(i);
 				final Tag tag = new Tag(
 						jtag.getInt("id"),
 						jtag.getString("name")				
 				);
 				result.add(tag);
 			}
 			return result;
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public void deleteTag(Tag tag) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "id", tag.getId());
 		executeMethod("deleteTag", map);
 	}
 	
 	public void validateSender(ValidateSenderRequest vsr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		MapUtils.putIfNotNull(map, "email", vsr.getEmail());
 		MapUtils.putIfNotNull(map, "login", vsr.getLogin());
 		executeMethod("validateSender", map);
 	}
 	/**
 	 * 
 	 * @param RegisterRequest
 	 * @return api_key
 	 * @see <a href="http://www.unisender.com/ru/help/api/register">http://www.unisender.com/ru/help/api/register</a>
 	 */
 	public String register(RegisterRequest rr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		User u = rr.getUser();
 		
 		MapUtils.putIfNotNull(map, "email", u.getEmail());
 		MapUtils.putIfNotNull(map, "login", u.getLogin());
 		
 		MapUtils.putIfNotNull(map, "password", u.getPassword());
 		MapUtils.putIfNotNull(map, "notify", rr.getNotify());
 		MapUtils.putIfNotNull(map, "extra", rr.getExtra());
 		MapUtils.putIfNotNull(map, "timezone", rr.getTimezone());
 		MapUtils.putIfNotNull(map, "country_code", rr.getCountryCode());
 		MapUtils.putIfNotNull(map, "currency_code", rr.getCurrencyCode());
 		MapUtils.putIfNotNull(map, "ip", rr.getIp());
 		MapUtils.putIfNotNull(map, "api_mode", rr.getApiMode());
 			
 		JSONObject response = executeMethod("register", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return res.getString("api_key");
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	public CheckUserExistsResponse checkUserExists(CheckUserExistsRequest uer) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 
 		MapUtils.putIfNotNull(map, "login", uer.getLogin());
 		MapUtils.putIfNotNull(map, "email", uer.getEmail());
 			
 		JSONObject response = executeMethod("checkUserExists", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return new CheckUserExistsResponse(
 					res.getInt("login_exists"),
 					res.getInt("email_exists")
 					);
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public UserInfo getUserInfo(String login) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		
 		MapUtils.putIfNotNull(map, "login", login);
 		
 		JSONObject response = executeMethod("getUserInfo", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return new UserInfo(
 					res.getString("login"),
 					res.getString("master"),
 					res.getDouble("balance"),
 					res.getString("currency"),
 					res.getInt("emails_paid"),
 					res.getInt("emails_used"),
 					res.getInt("period_emails_paid"),
 					res.getInt("period_emails_used"),
 					res.getString("email_period_start"),
 					res.getString("email_period_end")
 			);
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	public TransferMoneyResponse transferMoney(TransferMoneyRequest mr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		
 		MapUtils.putIfNotNull(map, "source_login", mr.getSourceLogin());
 		MapUtils.putIfNotNull(map, "target_login", mr.getTargetLogin());
 		MapUtils.putIfNotNull(map, "sum", mr.getSum());
 		MapUtils.putIfNotNull(map, "currency", mr.getCurrency());
 		
 		JSONObject response = executeMethod("transferMoney", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			return new TransferMoneyResponse(
 					res.getDouble("source_old_balance"),
 					res.getDouble("source_new_balance"),
 					res.getString("source_currency"),
 					res.getDouble("target_old_balance"),
 					res.getDouble("target_new_balance"),
 					res.getString("target_currency")
 			);
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 	
 	public FieldData exportContacts(ExportContactsRequest ecr) throws UniSenderMethodException, UniSenderConnectException, UniSenderMethodException, UniSenderInvalidResponseException {
 		Map<String, String> map = createMap();
 		
 		MailList mailList = ecr.getListId();
 		if (mailList != null){
 			MapUtils.putIfNotNull(map, "list_id", mailList.getId());
 		}
 		
 		List<String> fields = ecr.getFieldNames();
 		if (fields != null){
 			int count = 0;
 			for (String field: fields){
 				MapUtils.putIfNotNull(map, "field_names["+count+"]", field);
 				++count;
 			}
 		}
 		
 		MapUtils.putIfNotNull(map, "offset", ecr.getOffset());
 		MapUtils.putIfNotNull(map, "limit", ecr.getLimit());
 		
 		JSONObject response = executeMethod("exportContacts", map);
 		try {
 			JSONObject res = response.getJSONObject("result");
 			
 			final FieldData fd = new FieldData();
 			
 			JSONArray jfields = res.getJSONArray("field_names");
 			for (int i = 0; i < jfields.length(); ++i)
 			{
 				final String field = jfields.getString(i);
 				fd.addField(field);
 			}
 			
 			final JSONArray jdatas = res.getJSONArray("data");
 			for (int i = 0; i < jdatas.length(); ++i){
 				JSONArray jdata = jdatas.getJSONArray(i);
 				final List<String> values = new ArrayList<String>();
 				for (int j = 0; j < jdata.length(); ++j){
 					values.add(jdata.getString(j));
 				}
 				fd.addValues(values);
 			}
 			return fd;
 			
 		} catch (JSONException e) {
 			throw new UniSenderInvalidResponseException(e);
 		}
 	}
 }
