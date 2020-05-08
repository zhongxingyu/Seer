 package com.cemso.service.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jdom.JDOMException;
 
 import com.cemso.dao.DeviceDao;
 import com.cemso.dao.OnebyoneDao;
 import com.cemso.dao.PlaylistDao;
 import com.cemso.dao.ProgramDao;
 import com.cemso.dao.ResourceDao;
 import com.cemso.dao.TemplateDao;
 import com.cemso.dao.impl.ActionlogDaoImpl;
 import com.cemso.dto.ActionlogDTO;
 import com.cemso.dto.DeviceDTO;
 import com.cemso.dto.OnebyoneDTO;
 import com.cemso.dto.PlaylistDTO;
 import com.cemso.dto.ProgramDTO;
 import com.cemso.dto.TemplateDTO;
 import com.cemso.jna.JoymindCommDLL.JoymindCommDLLLib;
 import com.cemso.service.DeviceService;
 import com.cemso.util.CheckDeviceStateExcuter;
 import com.cemso.util.ConstantUtil;
 import com.cemso.util.FileOperationTool;
 import com.cemso.util.SequenceUtil;
 
 public class DeviceServiceImpl implements DeviceService {
 
 	private static final Log log = LogFactory
 			.getLog(com.cemso.service.impl.DeviceServiceImpl.class);
 	private DeviceDao deviceDao;
 	private PlaylistDao playlistDao;
 	private ProgramDao programDao;
 	private TemplateDao templateDao;
 	private ResourceDao resourceDao;
 	private OnebyoneDao onebyoneDao;
 
 	public OnebyoneDao getOnebyoneDao() {
 		return onebyoneDao;
 	}
 
 	public void setOnebyoneDao(OnebyoneDao onebyoneDao) {
 		this.onebyoneDao = onebyoneDao;
 	}
 
 	public ProgramDao getProgramDao() {
 		return programDao;
 	}
 
 	public void setProgramDao(ProgramDao programDao) {
 		this.programDao = programDao;
 	}
 
 	public TemplateDao getTemplateDao() {
 		return templateDao;
 	}
 
 	public void setTemplateDao(TemplateDao templateDao) {
 		this.templateDao = templateDao;
 	}
 
 	public DeviceDao getDeviceDao() {
 		return deviceDao;
 	}
 
 	public void setDeviceDao(DeviceDao deviceDao) {
 		this.deviceDao = deviceDao;
 	}
 
 	public ResourceDao getResourceDao() {
 		return resourceDao;
 	}
 
 	public void setResourceDao(ResourceDao resourceDao) {
 		this.resourceDao = resourceDao;
 	}
 
 	public PlaylistDao getPlaylistDao() {
 		return playlistDao;
 	}
 
 	public void setPlaylistDao(PlaylistDao playlistDao) {
 		this.playlistDao = playlistDao;
 	}
 
 	public boolean addDevice(DeviceDTO device) {
 		int indexid = SequenceUtil.getInstance().getNextKeyValue(
 				ConstantUtil.SequenceName.DEVICE_SEQUENCE);
 		device.setIndexid(indexid);
 		boolean flag =  deviceDao.addDevice(device);
 		if(flag){
 			Integer indexid1 = SequenceUtil.getInstance().getNextKeyValue(
 					ConstantUtil.SequenceName.ACTIONLOG_SEQUENCE);
 			String time = ActionlogDTO.currentTime();
 			String user = ActionlogDTO.username;
 			String action = "豸ID: "+device.getId()+", IP: "+device.getIp();
 			String sql = "";
 			ActionlogDTO actionlog = new ActionlogDTO(indexid1,time,user,action,sql);
 			ActionlogDaoImpl.addActionlog(actionlog);
 		}
 		
 		return flag;
 	}
 
 	public boolean deleteDevice(String deviceId) {
 		boolean flag = deviceDao.deleteDevice(deviceId);
 		if(flag){
 			Integer indexid1 = SequenceUtil.getInstance().getNextKeyValue(
 					ConstantUtil.SequenceName.ACTIONLOG_SEQUENCE);
 			String time = ActionlogDTO.currentTime();
 			String user = ActionlogDTO.username;
 			String action = "ɾ豸ID: "+deviceId;
 			String sql = "";
 			ActionlogDTO actionlog = new ActionlogDTO(indexid1,time,user,action,sql);
 			ActionlogDaoImpl.addActionlog(actionlog);
 		}
 		return flag;
 	}
 
 	public boolean edit(DeviceDTO device) {
 		boolean flag = deviceDao.updateDevice(device);
 		/**
 		if(flag){
 			Integer indexid1 = SequenceUtil.getInstance().getNextKeyValue(
 					ConstantUtil.SequenceName.ACTIONLOG_SEQUENCE);
 			String time = ActionlogDTO.currentTime();
 			String user = ActionlogDTO.username;
 			String action = "޸豸ID: "+device.getId()+", IP: "+ device.getIp();
 			String sql = "";
 			ActionlogDTO actionlog = new ActionlogDTO(indexid1,time,user,action,sql);
 			ActionlogDaoImpl.addActionlog(actionlog);
 		}
 		**/
 		return flag;
 	}
 
 	public List<DeviceDTO> getDevices() {
 		return deviceDao.queryDevices();
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<String, List<DeviceDTO>> getStatedDevices() {
 		List<DeviceDTO> deviceList = deviceDao.queryDevices();
 		List<DeviceDTO> onlineDeviceList = new ArrayList<DeviceDTO>();
 		List<DeviceDTO> offlineDeviceList = new ArrayList<DeviceDTO>();
 		for (DeviceDTO device : deviceList) {
 			if ("".equals(device.getOnlineState())) {
 				offlineDeviceList.add(device);
 			}
 			if ("".equals(device.getOnlineState())) {
 				onlineDeviceList.add(device);
 			}
 		}
 
 		Map deviceMap = new HashMap<String, List<DeviceDTO>>();
 		deviceMap.put("online", onlineDeviceList);
 		deviceMap.put("offline", offlineDeviceList);
 		return deviceMap;
 	}
 
 	public List<DeviceDTO> searchedDevices(String keyword) {
 		List<DeviceDTO> all = deviceDao.queryDevices();
 		List<DeviceDTO> result = new ArrayList<DeviceDTO>();
 		for (DeviceDTO device : all) {
 			if (device.getName().contains(keyword)
 					|| device.getGroupText().contains(keyword)
 					|| device.getRemark().contains(keyword)
 					|| device.getIp().contains(keyword)
 					|| device.getGateway().contains(keyword)) {
 				result.add(device);
 			}
 		}
 		return result;
 	}
 
 	// ͽĿҲ
 	@SuppressWarnings("static-access")
 	public boolean pushPlaylist(String id, String ip) throws Exception {
 		try {
 			CheckDeviceStateExcuter.checkWait();
 			// the resource list need to push
 			List<String> resources = new ArrayList<String>();
 
 			PlaylistDTO pushedPlaylistDTO = playlistDao.queryPlaylist(id);
 
 			if (log.isDebugEnabled()) {
 				log.debug("the palylist you want to push is:"
 						+ pushedPlaylistDTO.getId());
 			}
 
 			// ѯplaylistprogramlist
 			List<ProgramDTO> programs1 = generateProgramList(pushedPlaylistDTO
 					.getProgramids());
 
 			pushedPlaylistDTO.setProgramList(programs1);
 			// add onebyone into proram
 			List<OnebyoneDTO> onebyones = onebyoneDao.queryOnebyones();
 			for (ProgramDTO program : programs1) {
 				List<OnebyoneDTO> onebyoneList = new ArrayList<OnebyoneDTO>();
 				String programid = program.getId();
 				for (OnebyoneDTO onebyoneDTO : onebyones) {
 					if (programid.equals(onebyoneDTO.getProgramid())) {
 						onebyoneList.add(onebyoneDTO);
 					}
 				}
 				program.setOnebyoneList(onebyoneList);
 			}
 
 			// õԴļidname
 			Map<String, String> resourceMap = new HashMap<String, String>();
 
 			// ԵProgramDTO浽programList
 			for (ProgramDTO program1 : programs1) {
 				// ȡtemplateĸ߶ȣ
 				List<TemplateDTO> tempTemplateList = templateDao
 						.queryTemplates();
 				for (TemplateDTO template : tempTemplateList) {
 					if (template.getId().equals(program1.getTemplateId())) {
 						String size = template.getSize();
 						if (log.isDebugEnabled()) {
 							log
 									.debug("the size of this program for preview is : "
 											+ size);
 						}
 						if ("4_3".equals(size)) {
 							program1.setTemplateWidth("800");
 							program1.setTemplateHeight("600");
 						}
 						if ("3_4".equals(size)) {
 							program1.setTemplateWidth("600");
 							program1.setTemplateHeight("800");
 						}
 						if ("5_4".equals(size)) {
 							program1.setTemplateWidth("800");
 							program1.setTemplateHeight("640");
 						}
 						if ("4_5".equals(size)) {
 							program1.setTemplateWidth("640");
 							program1.setTemplateHeight("800");
 						}
 						if ("16_9".equals(size)) {
 							program1.setTemplateWidth("800");
 							program1.setTemplateHeight("450");
 						}
 						if ("1280_1024".equals(size)) {
 							program1.setTemplateWidth("1280");
 							program1.setTemplateHeight("1024");
 						}
 						if ("9_16".equals(size)) {
 							program1.setTemplateWidth("450");
 							program1.setTemplateHeight("800");
 						}
 					}
 				}
 
 				// ȡĿԴname
 				for (OnebyoneDTO onebyone : program1.getOnebyoneList()) {
 					resourceMap.put(onebyone.getResource(), onebyone
 							.getResourceName());
 					String resourceType = onebyone.getResourceType();
 					if (resourceType.contains("video")) {
 						resources.add(FileOperationTool.DEFAULT_VIDEO_DES_PATH
 								+ onebyone.getResourceName());
 					}
 					if (resourceType.contains("audio")) {
 						resources.add(FileOperationTool.DEFAULT_AUDIO_DES_PATH
 								+ onebyone.getResourceName());
 					}
 					if (resourceType.contains("image")) {
 						resources.add(FileOperationTool.DEFAULT_IMG_DES_PATH
 								+ onebyone.getResourceName());
 					}
 				}
 			}
 
 			/** invoke DLL method **/
 			if (log.isDebugEnabled()) {
 				log
 						.debug("*******************************Start to invoke DLL*********************************");
 			}
 			File f = new File("C:/DLLfunctionsTest.txt");
 			JoymindCommDLLLib instance = JoymindCommDLLLib.INSTANCE;
 			FileWriter fw = new FileWriter(f, true);
 			BufferedWriter bw = new BufferedWriter(fw);
 			bw.append(new Date().toString());
 			bw.newLine();
 			bw
 					.append("*******************************Start to invoke DLL*********************************");
 			bw.newLine();
 			// clear DLL TEMP file
 			instance.ResetDLL();
 			bw.append("instance.ResetDLL();");
 			bw.newLine();
 			if (log.isInfoEnabled()) {
 				log.info("ResetDLL finished!");
 			}
 			
 			// clear disk files
 			@SuppressWarnings("unused")
 			int clearMediaFlag = 1;
 //			int clearMediaFlag = instance.ClearMediaFile(ip);
 //			if (log.isDebugEnabled()) {
 //				log.debug("the function is : ClearMediaFile(" + ip + ");");
 //				log.debug("ClearMediaFile result is : " + clearMediaFlag);
 //				bw.append("System.out.println(instance.ClearMediaFile(\"" + ip
 //						+ "\"));");
 //				bw.newLine();
 //				bw.append("//ClearMediaFile result is : " + clearMediaFlag);
 //				bw.newLine();
 //			}
 			
 			// clear screen
 			@SuppressWarnings("unused")
 			int clearScreenResult = 1;
 //			int clearScreenResult = instance.ClearScreen(ip);
 //			if (log.isDebugEnabled()) {
 //				log.debug("the function is : ClearScreen(" + ip + ");");
 //				log.debug("ClearScreen result is : " + clearScreenResult);
 //				bw.append("System.out.println(instance.ClearScreen(\"" + ip
 //						+ "\"));");
 //				bw.newLine();
 //				bw.append("//ClearScreen result is : " + clearScreenResult);
 //				bw.newLine();
 //			}
 
 			boolean stepOneFlag = true;
 			boolean stepTwoFlag = true;
 			boolean stepThreeFlag = true;
 
 			// step1 push resources
 			Set<String> resourceSet = new HashSet<String>();
 			int flag = 0;
 			for (int i = 0; i < resources.size(); i++) {
 				if(resourceSet.contains(resources.get(i))){
 					flag++;
 					continue;
 				}else{
 					resourceSet.add(resources.get(i));
 				}
 				int temp = instance.AddMediaFile(ip, resources.get(i));
				// if send one file failed, return
				if(temp == 0){
					bw.close();
					fw.close();
					return false;
				}
 				Thread.currentThread().sleep(1000);
 				flag += temp;
 				if (log.isInfoEnabled()) {
 					log.info("the function is : AddMediaFile(" + ip + ","
 							+ resources.get(i) + ");");
 					log.info("AddMediaFile result is : " + temp);
 					bw.append("System.out.println(instance.AddMediaFile(\""
 							+ ip + "\",\"" + resources.get(i) + "\"));");
 					bw.newLine();
 					bw.append("//AddMediaFile result is : " + temp);
 					bw.newLine();
 				}
 			}
 			if (flag == resources.size()) {
 				stepOneFlag &= true;
 			} else {
 				stepOneFlag &= false;
 			}
 
 			// step2 add programs
 			List<ProgramDTO> dllPrograms = pushedPlaylistDTO.getProgramList();
 
 			for (int i = 0; i < dllPrograms.size(); i++) {
 				ProgramDTO program = dllPrograms.get(i);
 				// Ļֱ
 				int stepTwoflag1 = 1;
 //				int stepTwoflag1 = instance.SetScreenPara(ip, Integer
 //						.parseInt(program.getTemplateWidth()), Integer
 //						.parseInt(program.getTemplateHeight()));
 //				if (log.isDebugEnabled()) {
 //					log.debug("the function is : SetScreenPara(" + ip + ","
 //							+ Integer.parseInt(program.getTemplateWidth())
 //							+ ","
 //							+ Integer.parseInt(program.getTemplateHeight())
 //							+ ");");
 //					log.debug("SetScreenPara result is : " + stepTwoflag1);
 //					bw.append("System.out.println(instance.SetScreenPara(\""
 //							+ ip + "\","
 //							+ Integer.parseInt(program.getTemplateWidth())
 //							+ ","
 //							+ Integer.parseInt(program.getTemplateHeight())
 //							+ "));");
 //					bw.newLine();
 //					bw.append("//SetScreenPara result is : " + stepTwoflag1);
 //					bw.newLine();
 //				}
 
 				// ýĿ
 				int jno = Integer.parseInt(program.getIndexNum()) + 1;
 				int stepTwoflag2 = instance.AddProgram(jno, Integer
 						.parseInt(program.getLength()), null);
 				Thread.currentThread().sleep(200);
 				if (log.isInfoEnabled()) {
 					log.info("the function is : AddProgram(" + jno + ","
 							+ Integer.parseInt(program.getLength())
 							+ ", null);");
 					log.info("AddProgram result is : " + stepTwoflag2);
 					bw.append("System.out.println(instance.AddProgram(" + jno
 							+ "," + Integer.parseInt(program.getLength())
 							+ ", null));");
 					bw.newLine();
 					bw.append("//AddProgram result is : " + stepTwoflag2);
 					bw.newLine();
 				}
 				
 				// check cycle type
 				String startDate = program.getStartDate();
 				String endDate = program.getEndDate();
 				String startHour1 = program.getStartHour();
 				String startMinute1 = program.getStartMinute();
 				String startSecond1 = program.getStartSecond();
 				String endHour1 = program.getEndHour();
 				String endMinute1 = program.getEndMinute();
 				String endSecond1 = program.getEndSecond();
 				
 				String xingqi1 = program.getXingqi1();
 				String xingqi2 = program.getXingqi2();
 				String xingqi3 = program.getXingqi3();
 				String xingqi4 = program.getXingqi4();
 				String xingqi5 = program.getXingqi5();
 				String xingqi6 = program.getXingqi6();
 				String xingqi7 = program.getXingqi7();
 				
 				
 				if(program.getRadioValue().equals("0")){
 					int flag8 = instance.SetProgTimer(jno, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
 					Thread.currentThread().sleep(200);
 					if (log.isInfoEnabled()) {
 						log.info("the function is : SetProgTimer("+jno+", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
 						log.info("SetProgTimer result is : " + flag8);
 						bw.append("System.out.println(SetProgTimer("+jno+", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
 						bw.newLine();
 						bw.append("//SetProgTimer result is : " + flag8);
 						bw.newLine();
 					}
 					if(flag8 == 1){
 						stepTwoFlag &= true;
 					}else{
 						stepTwoFlag &= false;
 					}
 				}
 				if(program.getRadioValue().equals("1") || program.getRadioValue().equals("2")){
 					int startYear = Integer.parseInt(startDate.split("-")[0]);
 					int startMonth = Integer.parseInt(startDate.split("-")[1]);
 					int startDay = Integer.parseInt(startDate.split("-")[2]);
 					
 					int endYear = Integer.parseInt(endDate.split("-")[0]);
 					int endMonth = Integer.parseInt(endDate.split("-")[1]);
 					int endDay = Integer.parseInt(endDate.split("-")[2]);
 					
 					int startHour = Integer.parseInt(startHour1);
 					int startMinute = Integer.parseInt(startMinute1);
 					int startSecond = Integer.parseInt(startSecond1);
 					int endHour = Integer.parseInt(endHour1);
 					int endMinute = Integer.parseInt(endMinute1);
 					int endSecond = Integer.parseInt(endSecond1);
 					
 					// monday, tuesday, wednesday, thursday, friday, saturday, sunday
 					int monday = Integer.parseInt(xingqi1);
 					int tuesday = Integer.parseInt(xingqi2);
 					int wednesday = Integer.parseInt(xingqi3);
 					int thursday = Integer.parseInt(xingqi4);
 					int friday = Integer.parseInt(xingqi5);
 					int saturday = Integer.parseInt(xingqi6);
 					int sunday = Integer.parseInt(xingqi7);
 
 					int flag8 = 0;
 					if(program.getRadioValue().equals("1")){
 						flag8 = instance.SetProgTimer(jno, 1, startYear, startMonth, startDay, startHour, startMinute, startSecond, endYear, endMonth, endDay, endHour, endMinute, endSecond, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
 					}
 					if(program.getRadioValue().equals("2")){
 						flag8 = instance.SetProgTimer(jno, 2, startYear, startMonth, startDay, startHour, startMinute, startSecond, endYear, endMonth, endDay, endHour, endMinute, endSecond, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
 					}
 					Thread.currentThread().sleep(200);
 					if(flag8 == 1){
 						stepTwoFlag &= true;
 					}else{
 						stepTwoFlag &= false;
 					}
 					
 					if (log.isInfoEnabled()) {
 						log.info("the function is : SetProgTimer("+jno+", 1, "+startYear+", "+startMonth+", "+ startDay+", "+ startHour+", "+ startMinute+", "+ startSecond+", "+ endYear+", "+ endMonth+", "+ endDay+", "+ endHour+", "+ endMinute+", "+ endSecond+", "+ monday+", "+ tuesday+", "+ wednesday+", "+ thursday+", "+ friday+", "+ saturday+", "+ sunday+");");
 						log.info("SetProgTimer result is : " + flag8);
 						bw.append("System.out.println(SetProgTimer("+jno+", 1, "+startYear+", "+startMonth+", "+ startDay+", "+ startHour+", "+ startMinute+", "+ startSecond+", "+ endYear+", "+ endMonth+", "+ endDay+", "+ endHour+", "+ endMinute+", "+ endSecond+", "+ monday+", "+ tuesday+", "+ wednesday+", "+ thursday+", "+ friday+", "+ saturday+", "+ sunday+");");
 						bw.newLine();
 						bw.append("//SetProgTimer result is : " + flag8);
 						bw.newLine();
 					}
 				}
 				
 
 				List<OnebyoneDTO> dllAreas = program.getOnebyoneList();
 				
 				// put video area to the last index
 				int videoAreaIndex = -1;
 				OnebyoneDTO videoOnebyone = null;
 				for (int j = 0; j < dllAreas.size(); j++) {
 					OnebyoneDTO onebyone = dllAreas.get(j);
 					String resourceType = onebyone.getResourceType();
 					if(resourceType.contains("video")){
 						videoAreaIndex = j;
 						videoOnebyone = onebyone;
 					}
 				}
 				if(videoAreaIndex != -1 && videoOnebyone != null){
 					dllAreas.remove(videoAreaIndex);
 					dllAreas.add(videoOnebyone);
 				}
 				
 				if(log.isDebugEnabled()){
 					log.debug("pushPlaylist function --> program's onebyone size is : " + dllAreas.size());
 				}
 				for (int j = 0; j < dllAreas.size(); j++) {
 					OnebyoneDTO onebyone = dllAreas.get(j);
 					String resourceType = onebyone.getResourceType();
 					String dimension = onebyone.getDimension();
 
 					// 꼰ȸ߶
 					int left = 0;
 					int top = 0;
 					int width = 0;
 					int height = 0;
 					double xValue = 1d;
 					if(program.getTemplateWidth().equals("1280")&&program.getTemplateHeight().equals("1024")){
 						xValue = 1.6d;
 						left = Integer.parseInt(String.valueOf(Integer.parseInt(dimension.split(",")[0])*xValue).split("\\.")[0]);
 						top = Integer.parseInt(String.valueOf(Integer.parseInt(dimension.split(",")[1])*xValue).split("\\.")[0]);
 						width = Integer.parseInt(String.valueOf(Integer.parseInt(dimension.split(",")[2])*xValue).split("\\.")[0])
 								- left;
 						height = Integer.parseInt(String.valueOf(Integer.parseInt(dimension.split(",")[3])*xValue).split("\\.")[0])
 								- top;
 					}else{
 						left = Integer.parseInt(dimension.split(",")[0]);
 						top = Integer.parseInt(dimension.split(",")[1]);
 						width = Integer.parseInt(dimension.split(",")[2])
 						- left;
 						height = Integer.parseInt(dimension.split(",")[3])
 						- top;
 					}
 					if(log.isDebugEnabled()){
 						log.debug("left:"+left);
 						log.debug("top:"+top);
 						log.debug("width:"+width);
 						log.debug("height:"+height);
 					}
 
 					// 
 					int qno = j + 1;
 
 					// 
 					if (resourceType.contains("text")) {
 						// ɫ
 						String[] color = onebyone.getFontColor().split("\\.");
 						int fontColor = instance.GetRGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
 						// 
 						String fontName = onebyone.getFontName();
 						// С
 						int fontSize = Integer.parseInt(onebyone.getFontSize());
 						int fontBold = Integer.parseInt(onebyone.getFontBolder());// ϸ(0Ӵ;1Ӵ)
 						int fontItalic = Integer.parseInt(onebyone.getFontItalic()); // б(0б;1б) ݲ֧֡
 						int fontUnder = Integer.parseInt(onebyone.getFontUnder());// »(0;1) ݲ֧֡
 						int line = Integer.parseInt(onebyone.getFontNextLine()); // Զ(0Զ;1Զ)
 						// ؼ
 						int type = 0;
 						// ٶ
 						int speed = Integer.parseInt(onebyone.getSpeed());
 						// ӳ
 						int delay = Integer.parseInt(onebyone.getDelay());
 						String direction = onebyone.getDirection();
 						// ֶ뷽ʽ
 						int hAlign = Integer.parseInt(onebyone.getFontHAlign());
 						int vAlign = Integer.parseInt(onebyone.getFontVAlign());
 						if (direction.equals("static")) {
 							type = 0;
 						}
 						if (direction.equals("right")) {
 							type = 2;
 						}
 						if (direction.equals("left")) {
 							type = 1;
 						}
 						if (direction.equals("up")) {
 							type = 3;
 						}
 						if (direction.equals("down")) {
 							type = 4;
 						}
 
 						// ı
 						String text = resourceDao.queryResource(
 								onebyone.getResourceName()).getParamRemark();
 						
 						if(log.isDebugEnabled()){
 							log.debug("add program's text content is : " + text);
 						}
 						
 						int stepTwoflag3 = instance.AddTextArea(jno, qno, left,
 								top, width, height, fontColor, fontName,
 								fontSize, fontBold, fontItalic, fontUnder,
 								line, hAlign, vAlign, text, type, speed, delay);
 						Thread.currentThread().sleep(200);
 						if (log.isInfoEnabled()) {
 							log.info("the function is : AddTextArea(" + jno
 									+ "," + qno + "," + left + "," + top + ","
 									+ width + "," + height + "," + fontColor
 									+ "," + fontName + "," + fontSize + ","
 									+ fontBold + "," + fontItalic + ","
 									+ fontUnder + "," + line + "," + hAlign
 									+ "," + vAlign + "," + text + "," + type
 									+ "," + speed + "," + delay + ");");
 							log
 									.info("AddTextArea result is : "
 											+ stepTwoflag3);
 							bw
 									.append("System.out.println(instance.AddTextArea("
 											+ jno
 											+ ","
 											+ qno
 											+ ","
 											+ left
 											+ ","
 											+ top
 											+ ","
 											+ width
 											+ ","
 											+ height
 											+ ","
 											+ fontColor
 											+ ",\""
 											+ fontName
 											+ "\","
 											+ fontSize
 											+ ","
 											+ fontBold
 											+ ","
 											+ fontItalic
 											+ ","
 											+ fontUnder
 											+ ","
 											+ line
 											+ ","
 											+ hAlign
 											+ ","
 											+ vAlign
 											+ ",\""
 											+ text
 											+ "\","
 											+ type
 											+ ","
 											+ speed
 											+ "," + delay + "));");
 							bw.newLine();
 							bw.append("//AddTextArea result is : "
 									+ stepTwoflag3);
 							bw.newLine();
 						}
 
 						if (stepTwoflag3 == 1) {
 							stepTwoFlag &= true;
 						} else {
 							stepTwoFlag &= false;
 							throw new Exception("AddTextAreaException");
 						}
 					}
 
 					// 
 					if(resourceType.contains("weather")){
 						
 					}
 					
 					// ʱ
 					if (resourceType.contains("clock")) {
 						// ɫ
 						String[] color = onebyone.getFontColor().split("\\.");
 						int fontColor = instance.GetRGB(Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2]));
 						// 
 						String fontName = onebyone.getFontName();
 						// С
 						int fontSize = Integer.parseInt(onebyone.getFontSize());
 						int fontBold = Integer.parseInt(onebyone.getFontBolder());// ϸ(0Ӵ;1Ӵ)
 						int fontItalic = Integer.parseInt(onebyone.getFontItalic()); // б(0б;1б) ݲ֧֡
 						int fontUnder = Integer.parseInt(onebyone.getFontUnder());// »(0;1) ݲ֧֡
 
 						// ʱʽ
 						int mode = Integer.parseInt(onebyone.getMode());
 						// ʾģʽ
 						int format = Integer.parseInt(onebyone.getFormat());
 
 						int stepTwoflag4 = instance.AddDClockArea(jno, qno,
 								left, top, width, height, fontColor, fontName,
 								fontSize, fontBold, fontItalic, fontUnder,
 								mode, format);
 						Thread.currentThread().sleep(200);
 						if (log.isInfoEnabled()) {
 							log.info("the function is : AddDClockArea(" + jno
 									+ "," + qno + "," + left + "," + top + ","
 									+ width + "," + height + "," + fontColor
 									+ "," + fontName + "," + fontSize + ","
 									+ fontBold + "," + fontItalic + ","
 									+ fontUnder + "," + mode + "," + format
 									+ ");");
 							log.info("AddDClockArea result is : "
 									+ stepTwoflag4);
 							bw
 									.append("System.out.println(instance.AddDClockArea("
 											+ jno
 											+ ","
 											+ qno
 											+ ","
 											+ left
 											+ ","
 											+ top
 											+ ","
 											+ width
 											+ ","
 											+ height
 											+ ","
 											+ fontColor
 											+ ",\""
 											+ fontName
 											+ "\","
 											+ fontSize
 											+ ","
 											+ fontBold
 											+ ","
 											+ fontItalic
 											+ ","
 											+ fontUnder
 											+ ","
 											+ mode
 											+ ","
 											+ format + "));");
 							bw.newLine();
 							bw.append("//AddDClockArea result is : "
 									+ stepTwoflag4);
 							bw.newLine();
 						}
 
 						if (stepTwoflag4 == 1) {
 							stepTwoFlag &= true;
 						} else {
 							stepTwoFlag &= false;
 							throw new Exception("AddDClockAreaException");
 						}
 					}
 
 					// ͼƬ
 					if (resourceType.contains("image")) {
 						// ļţļ
 						int mno = 1;
 
 						// ļ
 						String fileName = onebyone.getResourceName();
 
 						// ؼ
 						int type = 0;
 
 						// ٶ
 						int speed = 0;
 
 						// ӳ
 						int delay = 0;
 
 						String direction = onebyone.getDirection();
 						// ֶ뷽ʽ
 						if (direction == null) {
 							type = 0;
 						} else {
 							if (direction.equals("static")) {
 								type = 0;
 							}
 							if (direction.equals("right")) {
 								type = 2;
 							}
 							if (direction.equals("left")) {
 								type = 1;
 							}
 							if (direction.equals("top")) {
 								type = 3;
 							}
 							if (direction.equals("down")) {
 								type = 4;
 							}
 						}
 
 						int stepTwoflag5 = instance.AddFileArea(jno, qno, left,
 								top, width, height);
 						Thread.currentThread().sleep(200);
 						if (log.isDebugEnabled()) {
 							log.debug("the function is : AddFileArea(" + jno
 									+ "," + qno + "," + left + "," + top + ","
 									+ width + "," + height + ");");
 							log
 									.debug("AddFileArea result is : "
 											+ stepTwoflag5);
 							bw
 									.append("System.out.println(instance.AddFileArea("
 											+ jno
 											+ ","
 											+ qno
 											+ ","
 											+ left
 											+ ","
 											+ top
 											+ ","
 											+ width
 											+ ","
 											+ height + "));");
 							bw.newLine();
 							bw.append("//AddFileArea result is : "
 											+ stepTwoflag5);
 							bw.newLine();
 						}
 
 						int stepTwoflag6 = instance.AddFile2Area(jno, qno, mno,
 								fileName, width, height, type, speed, delay);
 						Thread.currentThread().sleep(200);
 						if (log.isInfoEnabled()) {
 							log.info("the function is : AddFile2Area(" + jno
 									+ "," + qno + "," + mno + "," + fileName
 									+ "," + width + "," + height + "," + type
 									+ "," + speed + "," + delay + ");");
 							log.info("AddFile2Area result is : "
 									+ stepTwoflag6);
 							bw
 									.append("System.out.println(instance.AddFile2Area("
 											+ jno
 											+ ","
 											+ qno
 											+ ","
 											+ mno
 											+ ",\""
 											+ fileName
 											+ "\","
 											+ width
 											+ ","
 											+ height
 											+ ","
 											+ type
 											+ ","
 											+ speed + "," + delay + "));");
 							bw.newLine();
 							bw.append("//AddFile2Area result is : "
 									+ stepTwoflag6);
 							bw.newLine();
 						}
 
 						if (stepTwoflag5 == 1) {
 							stepTwoFlag &= true;
 						} else {
 							stepTwoFlag &= false;
 							throw new Exception("AddFileAreaException");
 						}
 						if (stepTwoflag6 == 1) {
 							stepTwoFlag &= true;
 						} else {
 							stepTwoFlag &= false;
 							throw new Exception("AddFile2AreaException");
 						}
 					}
 
 					// Ƶ
 					if (resourceType.contains("video")) {
 						// Ƶ
 						String fileName = onebyone.getResourceName();
 						int stepTwoflag7 = instance.AddVideoArea(jno, qno,
 								left, top, width, height, fileName);
 						Thread.currentThread().sleep(200);
 						if (log.isInfoEnabled()) {
 							log.info("the function is : AddVideoArea(" + jno
 									+ "," + qno + "," + left + "," + top + ","
 									+ width + "," + height + "," + fileName
 									+ ");");
 							log.info("AddVideoArea result is : "
 									+ stepTwoflag7);
 							bw
 									.append("System.out.println(instance.AddVideoArea("
 											+ jno
 											+ ","
 											+ qno
 											+ ","
 											+ left
 											+ ","
 											+ top
 											+ ","
 											+ width
 											+ ","
 											+ height
 											+ ",\""
 											+ fileName
 											+ "\"));");
 							bw.newLine();
 							bw.append("//AddVideoArea result is : "
 									+ stepTwoflag7);
 							bw.newLine();
 						}
 
 						if (stepTwoflag7 == 1) {
 							stepTwoFlag &= true;
 						} else {
 							stepTwoFlag &= false;
 							throw new Exception("AddVideoAreaException");
 						}
 					}
 
 					if (stepTwoflag1 == 1 && stepTwoflag2 == 1) {
 						stepTwoFlag &= true;
 					} else {
 						stepTwoFlag &= false;
 						throw new Exception("StepTwoException");
 					}
 				}
 			}
 
 			// ŽĿ
 			int stepThree = instance.SendDisplayData(ip);
 			Thread.currentThread().sleep(200);
 			if (log.isInfoEnabled()) {
 				log.info("the function is:SendDisplayData(" + ip + ");");
 				log.info("SendDisplayData result is : " + stepThree);
 				bw.append("System.out.println(instance.SendDisplayData(\"" + ip
 						+ "\"));");
 				bw.newLine();
 				bw.append("//SendDisplayData result is : " + stepThree);
 				bw.newLine();
 				bw
 						.append("*******************************End to invoke DLL*********************************");
 			}
 			bw.flush();
 			bw.close();
 			if (stepThree == 1) {
 				stepThreeFlag &= true;
 			} else {
 				stepThreeFlag &= false;
 			}
 			if (log.isDebugEnabled()) {
 				log
 						.debug("*******************************End to invoke DLL*********************************");
 			}
 			/** invoke DLL method **/
 
 			boolean flag123 = stepOneFlag && stepTwoFlag && stepThreeFlag;
 			if(flag123){
 				if(log.isInfoEnabled()){
 					log.info("push playlis for ip: " + ip +", result is 1");
 				}
 				Integer indexid1 = SequenceUtil.getInstance().getNextKeyValue(
 						ConstantUtil.SequenceName.ACTIONLOG_SEQUENCE);
 				String time = ActionlogDTO.currentTime();
 				String user = ActionlogDTO.username;
 				String action = "˽ĿID: "+id+", IP: "+ ip;
 				String sql = "";
 				ActionlogDTO actionlog = new ActionlogDTO(indexid1,time,user,action,sql);
 				ActionlogDaoImpl.addActionlog(actionlog);
 			}else{
 				if(log.isInfoEnabled()){
 					log.info("push playlis for ip: " + ip +", result is 0");
 				}
 			}
 			CheckDeviceStateExcuter.checkStart();
 			return flag123;
 		} catch (JDOMException e) {
 			e.printStackTrace();
 			if (log.isErrorEnabled()) {
 				log.error(e.getMessage());
 			}
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			if (log.isErrorEnabled()) {
 				log.error(e.getMessage());
 			}
 			return false;
 		}
 	}
 
 	// generate List<ProgramDTO> with pushedPlaylistDTO.getProgramids();
 	private List<ProgramDTO> generateProgramList(String programids) {
 		List<ProgramDTO> programList = new ArrayList<ProgramDTO>();
 		String[] programs = programids.split(",");
 		for (int i = 0; i < programs.length; i++) {
 			ProgramDTO program = programDao.queryProgram(programs[i]);
 			program.setIndexNum(String.valueOf(i));
 			programList.add(program);
 		}
 		return programList;
 	}
 
 	@Override
 	public boolean editOnlineState(String ip, String string) {
 		return deviceDao.updateDevice(ip,"");
 	}
 
 	@Override
 	public DeviceDTO queryDevice(String ip) {
 		return deviceDao.queryDevice(ip);
 	}
 
 	@Override
 	public boolean pullDefaultPic(String imageName, String ip) {
 		// copy image to BG.jpg
 		if(new File(FileOperationTool.DEFAULT_IMG_DES_PATH+"BG.jpg").exists()){
 			FileOperationTool.del(FileOperationTool.DEFAULT_IMG_DES_PATH+"BG.jpg");
 		}
 		FileOperationTool.copy(new File(FileOperationTool.DEFAULT_IMG_DES_PATH+imageName), new File(FileOperationTool.DEFAULT_IMG_DES_PATH+"BG.jpg"));
 		
 		JoymindCommDLLLib instance = JoymindCommDLLLib.INSTANCE;
 		int flag = instance.AddMediaFile(ip,FileOperationTool.DEFAULT_IMG_DES_PATH+"BG.jpg");
 		if(flag == 1){
 			Integer indexid1 = SequenceUtil.getInstance().getNextKeyValue(
 					ConstantUtil.SequenceName.ACTIONLOG_SEQUENCE);
 			String time = ActionlogDTO.currentTime();
 			String user = ActionlogDTO.username;
 			String action = "ĬͼƬ: "+imageName+", IP: "+ ip;
 			String sql = "";
 			ActionlogDTO actionlog = new ActionlogDTO(indexid1,time,user,action,sql);
 			ActionlogDaoImpl.addActionlog(actionlog);
 		}
 		return flag == 1 ? true:false;
 	}
 
 	@Override
 	public boolean deleteAllDevices() {
 		// TODO Auto-generated method stub
 		return deviceDao.deleteAllDevices();
 	}
 }
