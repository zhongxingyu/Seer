 package app.sbaloan;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 public class SBALoanDataInterface {
 	private static SBALoanDataInterface _INSTANCE;
 	private static Map<String, Map<String, LoanGrantDto>> _loansGrants;
 
 	private SBALoanDataInterface() throws Exception {
 		_loansGrants = new HashMap<String, Map<String, LoanGrantDto>>();
 
 		// cache federal data. we will cache states as we go
 		populateFedList();
 	}
 
 	private static void populateFedList() throws Exception {
 		_loansGrants.put("Federal", new HashMap<String, LoanGrantDto>());
 		JSONTokener tokenizer = new JSONTokener(httpGet("http://api.sba.gov/loans_grants/federal.json"));
 
 		// get all fed info
 		while (tokenizer.more()) {
 			JSONArray jsonArray = (JSONArray) tokenizer.nextValue();
 			for (int i = 0; i < jsonArray.length(); i++) {
 				LoanGrantDto dto = new LoanGrantDto((JSONObject) jsonArray.get(i));
 
 				if (dto.getGovType().equals("Federal"))
					_loansGrants.put(dto.getStateName(), new HashMap<String, LoanGrantDto>());

				_loansGrants.get("Federal").put(dto.getTitle(), dto);
 			}
 		}
 
 		// go over all industries and assign values
 		for (String industry : SBALoanConstants.INDUSTRIES) {
 			tokenizer = new JSONTokener(httpGet("http://api.sba.gov/loans_grants/nil/for_profit/" + industry.replace(" ", "%20") + "/nil.json"));
 			while (tokenizer.more()) {
 				JSONArray jsonArray = (JSONArray) tokenizer.nextValue();
 				for (int i = 0; i < jsonArray.length(); i++) {
 					LoanGrantDto dto = new LoanGrantDto((JSONObject) jsonArray.get(i));
 
 					if (dto.getGovType().equals("Federal")) {
 						if (_loansGrants.get("Federal").get(dto.getTitle()) == null)
 							_loansGrants.get("Federal").put(dto.getTitle(), dto);
 						else
 							_loansGrants.get("Federal").get(dto.getTitle()).setIndustry(dto.getIndustry());
 					}
 				}
 			}
 		}
 	}
 
 	private static void populateStateList(String state) throws Exception {
 		if (!_loansGrants.containsKey(state)) {
 			try {
 				_loansGrants.put(state, new HashMap<String, LoanGrantDto>());
 
 				// get all state info
 				JSONTokener tokenizer = new JSONTokener(httpGet("http://api.sba.gov/loans_grants/state_financing_for/" + state + ".json"));
 
 				while (tokenizer.more()) {
 					JSONArray jsonArray = (JSONArray) tokenizer.nextValue();
 					for (int i = 0; i < jsonArray.length(); i++) {
 						LoanGrantDto dto = new LoanGrantDto((JSONObject) jsonArray.get(i));
 
 						_loansGrants.get(state).put(dto.getTitle(), dto);
 					}
 				}
 
 				// go over all industries and assign values
 				for (String industry : SBALoanConstants.INDUSTRIES) {
 					try {
 						tokenizer = new JSONTokener(httpGet("http://api.sba.gov/loans_grants/" + state + "/for_profit/" + industry + "/nil.json"));
 						while (tokenizer.more()) {
 							JSONArray jsonArray = (JSONArray) tokenizer.nextValue();
 							for (int i = 0; i < jsonArray.length(); i++) {
 								LoanGrantDto dto = new LoanGrantDto((JSONObject) jsonArray.get(i));
 
 								// dto should exist already but if not then ok not my fault...bad api.
 								if (_loansGrants.get(state).get(dto.getTitle()) == null)
 									_loansGrants.get(state).put(dto.getTitle(), dto);
 								else
 									_loansGrants.get(state).get(dto.getTitle()).setIndustry(dto.getIndustry());
 							}
 						}
 					} catch (IOException ioException) {
 						// do nothing. forget about it. stupid api.
 					}
 				}
 			} catch (Exception e) {
 				throw new Exception("Error populating state data: " + e.getMessage());
 			}
 		}
 	}
 
 	public static Map<String, LoanGrantDto> search(SearchDto searchDto, String state) throws Exception {
 		Map<String, LoanGrantDto> fed_state_map;
 		Map<String, LoanGrantDto> returnMap = new HashMap<String, LoanGrantDto>();
 
 		if (searchDto.getGovType() != null && searchDto.getGovType().equals("State")) {
 			populateStateList(state);
 			fed_state_map = _loansGrants.get(state);
 		} else
 			fed_state_map = _loansGrants.get("Federal");
 
 		for (Map.Entry<String, LoanGrantDto> entry : fed_state_map.entrySet()) {
 			boolean meetscriteria = true;
 
 			if (searchDto.getIndustry() != null && !searchDto.getIndustry().equals(entry.getValue().getIndustry()))
 				meetscriteria = false;
 			if (searchDto.getLoanType() != null && !searchDto.getLoanType().equals(entry.getValue().getLoanType()))
 				meetscriteria = false;
 			if (searchDto.getIsContractor() != null && searchDto.getIsContractor() != entry.getValue().getIsContractor())
 				meetscriteria = false;
 			if (searchDto.getIsDevelopment() != null && searchDto.getIsDevelopment() != entry.getValue().getIsDevelopment())
 				meetscriteria = false;
 			if (searchDto.getIsExporting() != null && searchDto.getIsExporting() != entry.getValue().getIsExporting())
 				meetscriteria = false;
 			if (searchDto.getIsGeneralPurpose() != null && searchDto.getIsGeneralPurpose() != entry.getValue().getIsGeneralPurpose())
 				meetscriteria = false;
 			if (searchDto.getIsDisabled() != null && searchDto.getIsDisabled() != entry.getValue().getIsDisabled())
 				meetscriteria = false;
 			if (searchDto.getIsDisaster() != null && searchDto.getIsDisaster() != entry.getValue().getIsDisaster())
 				meetscriteria = false;
 			if (searchDto.getIsExporting() != null && searchDto.getIsExporting() != entry.getValue().getIsExporting())
 				meetscriteria = false;
 			if (searchDto.getIsGreen() != null && searchDto.getIsGreen() != entry.getValue().getIsGreen())
 				meetscriteria = false;
 			if (searchDto.getIsMilitary() != null && searchDto.getIsMilitary() != entry.getValue().getIsMilitary())
 				meetscriteria = false;
 			if (searchDto.getIsMinority() != null && searchDto.getIsMinority() != entry.getValue().getIsMinority())
 				meetscriteria = false;
 			if (searchDto.getIsRural() != null && searchDto.getIsRural() != entry.getValue().getIsRural())
 				meetscriteria = false;
 			if (searchDto.getIsWoman() != null && searchDto.getIsWoman() != entry.getValue().getIsWoman())
 				meetscriteria = false;
			if (searchDto.getLoanType() != null && searchDto.getLoanType() != entry.getValue().getLoanType())
				meetscriteria = false;
			
 			if (meetscriteria)
 				returnMap.put(entry.getKey(), entry.getValue());
 		}
 
 		return returnMap;
 	}
 
 	public static SBALoanDataInterface getInstance() throws Exception {
 		if (_INSTANCE == null)
 			_INSTANCE = new SBALoanDataInterface();
 
 		return _INSTANCE;
 	}
 
 	private static String httpGet(String urlStr) throws IOException {
 		URL url = new URL(urlStr);
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
 		if (conn.getResponseCode() != 200) {
 			throw new IOException(conn.getResponseMessage());
 		}
 
 		// Buffer the result into a string
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = rd.readLine()) != null) {
 			sb.append(line);
 		}
 		rd.close();
 
 		conn.disconnect();
 		return sb.toString();
 	}
 }
