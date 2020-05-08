 package org.gsoft.openserv.web.loanprogram.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.gsoft.openserv.domain.loan.DefaultLoanProgramSettings;
 import org.gsoft.openserv.domain.loan.LoanProgram;
 import org.gsoft.openserv.repositories.loan.DefaultLoanProgramSettingsRepository;
 import org.gsoft.openserv.repositories.loan.LoanProgramRepository;
 import org.gsoft.openserv.repositories.rates.RateRepository;
 import org.gsoft.openserv.service.loanprogram.LoanProgramSettingsService;
 import org.gsoft.openserv.util.time.FrequencyType;
 import org.gsoft.openserv.web.loanprogram.model.DefaultLoanProgramSettingsModel;
 import org.gsoft.openserv.web.loanprogram.model.LoanProgramModel;
 import org.gsoft.openserv.web.loanprogram.model.LoanProgramsModel;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.stereotype.Service;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 @Service
 @Controller
 @RequestMapping("loanprogram")
 public class ManageLoanProgramSettingsController {
 	private static final Logger LOG = LogManager.getLogger(ManageLoanProgramSettingsController.class);
 	
 	@Resource
 	private LoanProgramRepository loanProgramRepository;
 	@Resource
 	private DefaultLoanProgramSettingsRepository defaultLoanProgramSettingsRepository;
 	@Resource
 	private RateRepository rateRepostory;
 	@Resource
 	private LoanProgramSettingsService loanProgramSettingsService;
 	@Resource
 	private ConversionService conversionService;
 	@Resource
 	private ObjectMapper objectMapper;
 	
 	@RequestMapping(value="/allloanprograms.do", method=RequestMethod.GET)
 	public ModelAndView loadLoanProgramsModel(){
 		List<LoanProgram> loanPrograms = loanProgramRepository.findAll();
 		LoanProgramsModel loanProgramsModel = new LoanProgramsModel();
 		List<LoanProgramModel> loanProgramModelList = new ArrayList<>();
 		loanProgramsModel.setLoanProgramModelList(loanProgramModelList);
 		for(LoanProgram loanProgram:loanPrograms){
 			loanProgramModelList.add(conversionService.convert(loanProgram, LoanProgramModel.class));
 		}
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setView(new MappingJackson2JsonView());
 		modelAndView.getModel().put("loanprograms", loanProgramsModel);
 		return modelAndView;
 	}
 	
 	@RequestMapping(value="/allloanprograms.do", method={RequestMethod.POST,RequestMethod.PUT})
	public void save(@RequestBody String model) throws JsonParseException, JsonMappingException, IOException{
 		LoanProgramsModel loanProgramsModel = objectMapper.readValue(model, LoanProgramsModel.class);
 		for(LoanProgramModel lpm:loanProgramsModel.getLoanProgramModelList()){
 			LoanProgram loanProgram = conversionService.convert(lpm, LoanProgram.class);
 			loanProgramRepository.save(loanProgram);
 			LOG.debug("Saved Loan Program");
 		}
 	}
 	
 	@RequestMapping(value="/loanprogramsettings.do", method={RequestMethod.GET})
 	public ModelAndView loanLoanProgramSettingsModels(@RequestParam("loanprogramid") String loanProgramID){
 		List<DefaultLoanProgramSettings> defaultSettings = defaultLoanProgramSettingsRepository.findAllDefaultLoanProgramSettingsByLoanProgramID(Long.valueOf(loanProgramID));
 		List<DefaultLoanProgramSettingsModel> defaultSettingsModelList = new ArrayList<>();
 		for(DefaultLoanProgramSettings settings:defaultSettings){
 			defaultSettingsModelList.add(conversionService.convert(settings, DefaultLoanProgramSettingsModel.class));
 		}
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setView(new MappingJackson2JsonView());
 		modelAndView.addObject(defaultSettingsModelList);
 		return modelAndView;
 	}
 	
 	@RequestMapping(value="/loanprogramsettings.do", method={RequestMethod.POST,RequestMethod.PUT})
 	public void saveSettings(@RequestBody String model) throws JsonParseException, JsonMappingException, IOException{
 		List<DefaultLoanProgramSettingsModel> loanProgramSettingsList = objectMapper.readValue(model, new TypeReference<List<DefaultLoanProgramSettingsModel>>(){});
 		for(DefaultLoanProgramSettingsModel lpm:loanProgramSettingsList){
 			loanProgramSettingsService.saveDefaultLoanProgramSettings(conversionService.convert(lpm, DefaultLoanProgramSettings.class));
 			LOG.debug("Saved Loan Program Settings");
 		}
 	}
 	
 	@RequestMapping(value="/loanprogramsettings/frequencytype", method={RequestMethod.GET})
 	public ModelAndView getAllFrequencyTypes(){
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setView(new MappingJackson2JsonView());
 		modelAndView.addObject(FrequencyType.values());
 		return modelAndView;
 	}
 }
