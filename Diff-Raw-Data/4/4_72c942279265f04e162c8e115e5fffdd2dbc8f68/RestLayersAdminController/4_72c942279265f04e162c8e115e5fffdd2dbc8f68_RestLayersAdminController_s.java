 /*
  * RestLayersAdminController.java
  * 
  * Copyright (C) 2012
  * 
  * This file is part of Proyecto persistenceGeo
  * 
  * This software is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * As a special exception, if you link this library with other files to produce
  * an executable, this library does not by itself cause the resulting executable
  * to be covered by the GNU General Public License. This exception does not
  * however invalidate any other reasons why the executable file might be covered
  * by the GNU General Public License.
  * 
  * Authors:: Alejandro Díaz Torres (mailto:adiaz@emergya.com)
  */
 package com.emergya.persistenceGeo.web;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.collections.ListUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.emergya.persistenceGeo.dto.AuthorityDto;
 import com.emergya.persistenceGeo.dto.LayerDto;
 import com.emergya.persistenceGeo.dto.MapConfigurationDto;
 import com.emergya.persistenceGeo.dto.SimplePropertyDto;
 import com.emergya.persistenceGeo.dto.UserDto;
 import com.emergya.persistenceGeo.service.LayerAdminService;
 import com.emergya.persistenceGeo.service.MapConfigurationAdminService;
 
 /**
  * Rest controller to admin and load layer and layers context
  * 
  * @author <a href="mailto:adiaz@emergya.com">adiaz</a>
  */
 @Controller
 public class RestLayersAdminController extends RestPersistenceGeoController
 		implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3028127910300105478L;
 	@Resource
 	private LayerAdminService layerAdminService;
 	@Resource
 	private MapConfigurationAdminService mapConfigurationAdminService;
 	
 	private Map<Long, File> loadedLayers = new ConcurrentHashMap<Long, File>();
 	private Map<Long, File> loadFiles = new ConcurrentHashMap<Long, File>();
 
 	public static final String LOAD_FOLDERS_BY_USER = "user";
 	public static final String LOAD_FOLDERS_BY_GROUP = "group";
 	public static final String LOAD_FOLDERS_STYLE_TREE = "tree";
 	public static final String LOAD_FOLDERS_STYLE_STRING = "string";
 
 	/**
 	 * This method loads mapConfiguration
 	 * 
 	 * @return JSON file with map configuration
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadMapConfiguration", method = RequestMethod.GET, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> loadMapConfiguration(){
 		Map<String, Object> result = new HashMap<String, Object>();
 		MapConfigurationDto mapConfiguration = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			mapConfiguration = mapConfigurationAdminService.loadConfiguration();
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, mapConfiguration != null ? 1: 0);
 		result.put(ROOT, mapConfiguration);
 
 		return result;
 	}
 
 	/**
 	 * This method update mapConfiguration
 	 * 
 	 * @return JSON file with map configuration updated
 	 */
 	@RequestMapping(value = "/persistenceGeo/updateMapConfiguration", method = RequestMethod.GET, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> updateMapConfiguration(@RequestParam("bbox") String bbox,
 			@RequestParam("iProj") String iProj, 
 			@RequestParam("res") String res){
 		Map<String, Object> result = new HashMap<String, Object>();
 		MapConfigurationDto mapConfiguration = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			mapConfiguration = mapConfigurationAdminService.loadConfiguration();
 			mapConfigurationAdminService.updateMapConfiguration(mapConfiguration.getId(), bbox, iProj, res);
 			mapConfiguration = mapConfigurationAdminService.loadConfiguration();
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, mapConfiguration != null ? 1: 0);
 		result.put(ROOT, mapConfiguration);
 
 		return result;
 	}
 
 	/**
 	 * This method loads layers.json related with a user
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/persistenceGeo/loadLayers/{username}", method = RequestMethod.GET, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> loadLayers(@PathVariable String username){
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<LayerDto> layers = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			if(username != null){
 				layers = new LinkedList<LayerDto>();
 				UserDto userDto = userAdminService.obtenerUsuario(username);
 				if(userDto.getId() != null){
 					layers = layerAdminService.getLayersByUser(userDto.getId());
 				}else{
 					layers = ListUtils.EMPTY_LIST;
 				}
 				for(LayerDto layer: layers){
 					if(layer.getId() != null && layer.getData() != null){
 						loadedLayers.put(layer.getId(), layer.getData());
 						layer.setData(null);
 						layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+layer.getId());
 					}
 				}
 			}
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, layers.size());
 		result.put(ROOT, layers);
 
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/persistenceGeo/loadPublicLayers/{userId}", method = RequestMethod.GET, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> loadPublicLayers(@PathVariable Long userId){
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<LayerDto> layers = null;
 		try{
 			
 //			//TODO: Secure with logged user
 //			String loggedUser = ((UserDetails) SecurityContextHolder.getContext()
 //					.getAuthentication().getPrincipal()).getUsername(); 
 //			
 			if(userId != null){
 				UserDto userDto = (UserDto) userAdminService.getById(userId);
 				
 				if(userDto==null) {
 					result.put(SUCCESS, false);
 					return result;
 				}
 				
 				if(userDto.getAdmin()){
 					if(!userDto.getAdmin()){
 						result.put(SUCCESS, false);
 						return result;
 					}
 					
 					layers = layerAdminService.getPublicLayers();
 				}else{
 					layers = ListUtils.EMPTY_LIST;
 				}
 				
 				for(LayerDto layer: layers){
 					layer.setEnabled(false);
 					if(layer.getId() != null && layer.getData() != null){
 						loadedLayers.put(layer.getId(), layer.getData());
 						layer.setData(null);
 						layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+layer.getId());
 					}
 				}
 			}
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 			return result;
 		}
 		
 		result.put(RESULTS, layers.size());
 		result.put(ROOT, layers);
 
 		return result;
 	}
 
 	
 	/**
 	 * This method loads layers.json related with a user
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value = "/persistenceGeo/loadLayersByGroup/{groupId}", 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> loadLayersByGroup(@PathVariable String groupId){
 		Map<String, Object> result = new HashMap<String, Object>();
 		List<LayerDto> layers = null;
 		try{
 			if(groupId != null 
 					&& canAccess(Long.decode(groupId))){
 				layers = layerAdminService.getLayersByAuthority(Long.decode(groupId));
 			}else{
 				layers = ListUtils.EMPTY_LIST;
 			}
 			for(LayerDto layer: layers){
 				if(layer.getId() != null && layer.getData() != null){
 					loadedLayers.put(layer.getId(), layer.getData());
 					layer.setData(null);
 					layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+layer.getId());
 				}
 			}
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, layers.size());
 		result.put(ROOT, layers);
 
 		return result;
 	}
 
 	/**
 	 * This method loads json file related with a user
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layer type properties
 	 */
 	@RequestMapping(value = "/persistenceGeo/getLayerTypeProperties/{layerType}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> getLayerTypeProperties(@PathVariable String layerType) {
 		Map<String, Object> result = new HashMap<String, Object>();
 
 		List<String> listRes = layerAdminService
 				.getAllLayerTypeProperties(layerType);
 		
 		List<SimplePropertyDto> list = new LinkedList<SimplePropertyDto>();
 		
 		if(listRes != null){
 			for(String property: listRes){
 				list.add(new SimplePropertyDto(property));
 			}
 		}
 		result.put(SUCCESS, true);
 		result.put(RESULTS, list.size());
 		result.put(ROOT, list);
 
 		return result;
 	}
 
 	/**
 	 * This method loads json file with layer types
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layer types
 	 */
 	@RequestMapping(value = "/persistenceGeo/getLayerTypes", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody
 	Map<String, Object> getLayerTypes() {
 		Map<String, Object> result = new HashMap<String, Object>();
 
 		List<String> listRes = layerAdminService.getAllLayerTypes();
 
 		List<SimplePropertyDto> list = new LinkedList<SimplePropertyDto>();
 		
 		if(listRes != null){
 			for(String property: listRes){
 				list.add(new SimplePropertyDto(property));
 			}
 		}
 		result.put(SUCCESS, true);
 		result.put(RESULTS, list.size());
 		result.put(ROOT, list);
 
 		return result;
 	}
 
 	/**
 	 * This method loads layers.json related with a user
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@RequestMapping(value = "/persistenceGeo/getLayerResource/{layerId}", method = RequestMethod.GET, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public void loadLayer(@PathVariable String layerId,
 					HttpServletResponse response){
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			response.setContentType("application/xml");
 			response.setHeader("Content-Disposition",
 					"attachment; filename=test.xml");
 			IOUtils.copy(new FileInputStream(loadedLayers.get(Long.decode(layerId))), response
 						.getOutputStream());
 			response.flushBuffer();
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * This method loads layers.json related with a group
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadLayersGroup/{group}", method = RequestMethod.GET)
 	public @ResponseBody 
 	List<LayerDto> loadLayersGroup(@PathVariable String group){
 		List<LayerDto> layers = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			if(group != null){
 				layers = new LinkedList<LayerDto>();
 				List<AuthorityDto> authosDto = userAdminService.obtenerGruposUsuarios();
 				List<String> namesList = null;
 				if(authosDto != null){
 					for(AuthorityDto authoDto: authosDto){
 						if(authoDto.getNombre().equals(group)){
 							namesList = authoDto.getLayerList();
 							break;
 						}
 					}
 					if(namesList != null){
 						layers = layerAdminService.getLayersByName(namesList);
 					}
 				}
 			}
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		return layers;
 	}
 
 	/**
 	 * This method loads layers.json related with a folder
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@RequestMapping(value = "/persistenceGeo/loadLayersFolder/{folder}", method = RequestMethod.GET, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	List<LayerDto> loadLayersFolder(@PathVariable String folder){
 		List<LayerDto> layers = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		return layers;
 	}
 
 	/**
 	 * This method loads layers.json related with a folder
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layers
 	 */
 	@RequestMapping(value = "/persistenceGeo/moveLayerTo", method = RequestMethod.POST, 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> moveLayerTo(@RequestParam("layerId") String layerId,
 			@RequestParam("toFolder") String toFolder,
 			@RequestParam(value="toOrder",required=false) String toOrder){
 		Map<String, Object> result = new HashMap<String, Object>();
 		LayerDto layer = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			Long idLayer = Long.decode(layerId);
 			layer = (LayerDto) layerAdminService.getById(idLayer);
 			layer.setFolderId(Long.decode(toFolder));
 			
 			if(toOrder != null){
 				Map<String, String> properties = layer.getProperties() != null ? layer
 						.getProperties() : new HashMap<String, String>();
 				properties.put("order", toOrder);
 				layer.setProperties(properties);
 			}
 			
 			layer = (LayerDto) layerAdminService.update(layer);
 			
 			//Must already loaded in RestLayerAdminController
 			if(layer.getId() != null && layer.getData() != null){
 				layer.setData(null);
 				layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+layer.getId());
 			}
 			
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, layer != null ? 1: 0);
 		result.put(ROOT, layer);
 
 		return result;
 	}
 
 	/**
 	 * This method saves layer visibility, layerOpacity...
 	 * 
 	 * @param layerId
 	 * @param properties
 	 * 
 	 * @return JSON file with layer modified
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveLayerSimpleProperties", 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> saveLayerSimpleProperties(@RequestParam("layerId") String layerId,
 			@RequestParam(value="name", required = false) String name,
 			@RequestParam(value="properties", required = false) String properties){
 		Map<String, Object> result = new HashMap<String, Object>();
 		LayerDto layer = null;
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			Long idLayer = Long.decode(layerId);
 			layer = (LayerDto) layerAdminService.getById(idLayer);
 			
 			// override name
 			if(name != null){
 				layer.setName(name);
 			}
 			
 			// override properties
 			if(properties != null){
 				Map<String, String> propertyMap = getMapFromString(properties);
 				Map<String, String> layerProperties = layer.getProperties() != null ? layer
 						.getProperties() : new HashMap<String, String>();
 				for(String key: propertyMap.keySet()){
 					layerProperties.put(key, propertyMap.get(key));
 				}
 				layer.setProperties(layerProperties);
 			}
 			
 			//layer update
 			layer = (LayerDto) layerAdminService.update(layer);
 			
 			//Must already loaded in RestLayerAdminController
 			if(layer.getId() != null && layer.getData() != null){
 				layer.setData(null);
 				layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+layer.getId());
 			}
 			
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, layer != null ? 1: 0);
 		result.put(ROOT, layer);
 
 		return result;
 	}
 
 	/**
 	 * This method loads json file with layer types
 	 * 
 	 * @param username
 	 * 
 	 * @return JSON file with layer types
 	 */
 	@RequestMapping(value = "/persistenceGeo/updateLayer", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
 	public @ResponseBody 
 	LayerDto updateLayerByUser(
 			@RequestParam("name") String name,
 			@RequestParam("type") String type,
 			@RequestParam(value="layer_id") String layerId,
 			@RequestParam(value="properties", required=false) String properties,
 			@RequestParam(value="enabled", required=false) String enabled,
 			@RequestParam(value="order_layer", required=false) String order_layer,
 			@RequestParam(value="is_channel", required=false) String is_channel,
 			@RequestParam(value="publicized", required=false) String publicized,
 			@RequestParam(value="server_resource", required=false) String server_resource,
 			@RequestParam(value="folderId", required=false) String folderId,
 			@RequestParam(value="idFile", required=false) String idFile){
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			// Create the layerDto
 			LayerDto layer = (LayerDto) layerAdminService.getById(Long.decode(layerId));
 			
 			//Copy layerData
 			layer = copyDataToLayer(name, type, properties, enabled, order_layer,
 					is_channel, publicized, server_resource, idFile, layer, folderId);
 			
 			return layer;
 		}catch (Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private static final Random RANDOM = new Random();
 	
 	/**
 	 * This method saves a layer related with a user
 	 * 
 	 * @param uploadfile
 	 */
 	@RequestMapping(value = "/persistenceGeo/uploadFile", method = RequestMethod.POST)
 	public ModelAndView uploadFile(
 			@RequestParam(value="uploadfile") MultipartFile uploadfile){
 		ModelAndView model = new ModelAndView();
 		String result = null;
 		if(uploadfile != null){
 			Long id = RANDOM.nextLong();
 			result = "{\"results\": 1, \"data\": \""+ id +"\", \"success\": true}";
 			byte[] data;
 			try {
 				data = IOUtils.toByteArray(uploadfile.getInputStream());
 				File temp = com.emergya.persistenceGeo.utils.FileUtils
 						.createFileTemp("tmp", "xml");
 				org.apache.commons.io.FileUtils.writeByteArrayToFile(temp, data);
 				loadFiles.put(id, temp);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			result = "{\"results\": 0, \"data\": \"\", \"success\": false}";
 		}
 		model.addObject("resultado", result);
 		model.setViewName("resultToJSON");
 		return model;
 	}
 
 	/**
 	 * This method saves a layer related with a user
 	 * 
 	 * @param username
 	 * @param uploadfile
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveLayerByUser/{username}", method = RequestMethod.POST)
 	public @ResponseBody 
 	LayerDto saveLayerByUser(@PathVariable String username,
 			@RequestParam("name") String name,
 			@RequestParam("type") String type,
 			@RequestParam(value="properties", required=false) String properties,
 			@RequestParam(value="enabled", required=false) String enabled,
 			@RequestParam(value="order_layer", required=false) String order_layer,
 			@RequestParam(value="is_channel", required=false) String is_channel,
 			@RequestParam(value="publicized", required=false) String publicized,
 			@RequestParam(value="server_resource", required=false) String server_resource,
 			@RequestParam(value="folderId", required=false) String folderId,
 			@RequestParam(value="idFile", required=false) String idFile){
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			// Create the layerDto
 			LayerDto layer = new LayerDto();
 			// Assign the user
 			layer.setUser(username);
 			
 			//Copy layerData
 			layer = copyDataToLayer(name, type, properties, enabled, order_layer,
 					is_channel, publicized, server_resource, idFile, layer, folderId);
 			
 			return layer;
 		}catch (Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * This method saves a layer related with a user
 	 * 
 	 * @param username
 	 * @param uploadfile
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveLayerByGroup/{idGroup}", method = RequestMethod.POST)
 	public @ResponseBody 
 	LayerDto saveLayerByGroup(@PathVariable String idGroup,
 			@RequestParam("name") String name,
 			@RequestParam("type") String type,
 			@RequestParam(value="properties", required=false) String properties,
 			@RequestParam(value="enabled", required=false) String enabled,
 			@RequestParam(value="order_layer", required=false) String order_layer,
 			@RequestParam(value="is_channel", required=false) String is_channel,
 			@RequestParam(value="publicized", required=false) String publicized,
 			@RequestParam(value="server_resource", required=false) String server_resource,
 			@RequestParam(value="folderId", required=false) String folderId,
 			@RequestParam(value="idFile", required=false) String idFile){
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			// Create the layerDto
 			LayerDto layer = new LayerDto();
 			// Assign the user group
 			AuthorityDto group = userAdminService.obtenerGrupoUsuarios(Long.decode(idGroup));
 			layer.setAuthId(group.getId());
 			
 			//Copy layerData
 			layer = copyDataToLayer(name, type, properties, enabled, order_layer,
 					is_channel, publicized, server_resource, idFile, layer, folderId, true);
 			
 			return layer;
 		}catch (Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 
 	/**
 	 * Copy layer data 
 	 * 
 	 * @param name
 	 * @param type
 	 * @param properties
 	 * @param enabled
 	 * @param order_layer
 	 * @param is_channel
 	 * @param publicized
 	 * @param server_resource
 	 * @param uploadfile
 	 * @param layer
 	 * @param update
 	 * 
 	 * @throws IOException
 	 */
 	private LayerDto copyDataToLayer(String name, String type,
 			String properties, String enabled, String order_layer,
 			String is_channel, String publicized, String server_resource,
 			String idFile, LayerDto layer, String folderId, Boolean update) {
 			// Add request parameter
 			layer.setName(name);
 			layer.setType(type);
 			layer.setServer_resource(server_resource);
 			layer.setEnabled(enabled != null ? enabled.toLowerCase().equals("true")
 					: false);
 			layer.setOrder(order_layer);
 			layer.setPertenece_a_canal(is_channel != null ? is_channel
 					.toLowerCase().equals("true") : false);
 			layer.setPublicized(publicized != null ? publicized.toLowerCase()
 					.equals("true") : false);
 			//Folder id
 			if(!StringUtils.isEmpty(folderId) 
 					&& StringUtils.isNumeric(folderId)){
 				layer.setFolderId(Long.decode(folderId));
 			}
 
 			// Layer properties
 			if (properties != null) {
				layer.setProperties(getMapFromString(properties));
 			}
 			
 			//Only if a file has been saved
 			if(idFile != null){
 				File temp = loadFiles.get(Long.decode(idFile));
 				// Layer data
 				if (temp != null) {
 					layer.setData(temp);
 				}
 			}
 
 			// Save the layer
 			if(update != null){
 				if(update){
 					layer = (LayerDto) layerAdminService.update(layer);	
 				}else{
 					layer = (LayerDto) layerAdminService.create(layer);
 				}
 			}else{
 				// temp layer
 				loadedLayers.put(Long.decode(idFile), layer.getData());
 				layer.setData(null);
 				layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+idFile);
 			}
 			
 			if(layer.getId() != null && layer.getData() != null){
 				// 
 				loadedLayers.put(layer.getId(), layer.getData());
 				layer.setData(null);
 				layer.setServer_resource("rest/persistenceGeo/getLayerResource/"+layer.getId());
 			}
 			
 			if(idFile != null 
 					&& loadFiles.containsKey(idFile)){
 				loadFiles.remove(idFile);
 			}
 			
 			return layer;
 	}
 
 	/**
 	 * Copy layer data 
 	 * 
 	 * @param name
 	 * @param type
 	 * @param properties
 	 * @param enabled
 	 * @param order_layer
 	 * @param is_channel
 	 * @param publicized
 	 * @param server_resource
 	 * @param uploadfile
 	 * @param layer
 	 * @throws IOException
 	 */
 	private LayerDto copyDataToLayer(String name, String type, String properties,
 			String enabled, String order_layer, String is_channel,
 			String publicized, String server_resource,
 			String idFile, LayerDto layer, String folderId) throws IOException {
 		return copyDataToLayer(name, type, properties, enabled, 
 				order_layer, is_channel, publicized, server_resource, 
 				idFile, layer, folderId, false);
 	}
 	
 	private static final String PROPERTIES_SEPARATOR = ",,,";
 	private static final String PROPERTIES_NAM_VALUE_SEPARATOR = "===";
 	
 	/**
 	 * Parse a string as 'test===valueTest,,,test2===value2' to map of values 
 	 * 
 	 * @param properties to be parsed
 	 * 
 	 * @return map with values
 	 */
 	private static Map<String, String> getMapFromString(String properties){
 		Map<String,String> map = new HashMap<String, String>();
 		if(properties.split(PROPERTIES_SEPARATOR) != null){
 			for(String property: properties.split(PROPERTIES_SEPARATOR)){
 				if(property != null 
 						&& property.split(PROPERTIES_NAM_VALUE_SEPARATOR) != null
 						&& property.split(PROPERTIES_NAM_VALUE_SEPARATOR).length == 2){
 					map.put(property.split(PROPERTIES_NAM_VALUE_SEPARATOR)[0], property.split(PROPERTIES_NAM_VALUE_SEPARATOR)[1]);
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * This method saves a layer related with a group
 	 * 
 	 * @param group
 	 * @param uploadfile
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveLayer/{group}", method = RequestMethod.POST)
 	public @ResponseBody 
 	void saveLayerByGroup(@PathVariable Long group,
 			@RequestParam("name") String name,
 			@RequestParam("type") String type,
 			@RequestParam(value="layerData", required=false) LayerDto layerData,
 			@RequestParam(value="uploadfile", required=false) MultipartFile uploadfile){
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			// Get the group and his layers
 			AuthorityDto auth = userAdminService.obtenerGrupoUsuarios(group);
 			List<String> layersFromGroup = auth.getLayerList();
 			// Add the new layer
 			if(layersFromGroup != null){
 				layersFromGroup.add(name);
 				auth.setLayerList(layersFromGroup);
 			}
 			// Save the group
 			userAdminService.modificarGrupoUsuarios(auth);
 			// Create the layerDto
 			LayerDto layer = new LayerDto();
 			// Assign the authority
 			layer.setAuthId(auth.getId());
 			// Add the request parameters
 			layer.setName(name);
 			layer.setType(type);
 			// Load the layer depend on the layer type 
 			// Save the layer
 			layerAdminService.create(layer);
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value = "/persistenceGeo/deleteLayerByLayerId/{layerId}", method = RequestMethod.POST)
 	public @ResponseBody
 	Map<String, Object> deleteLayerByLayerId(@PathVariable String layerId){
 		Map<String, Object> result = new HashMap<String, Object>();
 		try{
 			/*
 			//TODO: Secure with logged user
 			String username = ((UserDetails) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal()).getUsername(); 
 			 */
 			
 			// TODO: Delete layer from geoserver
 			Long idLayer = Long.decode(layerId);
 			layerAdminService.deleteLayerById(idLayer);
 			result.put(SUCCESS, true);
 			result.put(ROOT, new HashMap<String, Object>());
 		}catch (Exception e) {
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 			result.put(ROOT, "No se ha podido borrar la capa");
 		}
 		return result;
 	}
 
 	/**
 	 * This method save a temp layer
 	 * 
 	 * @return JSON file layer information
 	 */
 	@RequestMapping(value = "/persistenceGeo/saveLayerTempLayer", 
 			produces = {MediaType.APPLICATION_JSON_VALUE})
 	public @ResponseBody
 	Map<String, Object> saveLayerTempLayer(@RequestParam("name") String name,
 			@RequestParam("type") String type,
 			@RequestParam(value="properties", required=false) String properties,
 			@RequestParam(value="enabled", required=false) String enabled,
 			@RequestParam(value="order_layer", required=false) String order_layer,
 			@RequestParam(value="is_channel", required=false) String is_channel,
 			@RequestParam(value="publicized", required=false) String publicized,
 			@RequestParam(value="server_resource", required=false) String server_resource,
 			@RequestParam(value="folderId", required=false) String folderId,
 			@RequestParam(value="idFile", required=false) String idFile){
 		Map<String, Object> result = new HashMap<String, Object>();
 		LayerDto layer = new LayerDto();
 		try{
 			//Copy layerData
 			layer = copyDataToLayer(name, type, properties, enabled, order_layer,
 					is_channel, publicized, server_resource, idFile, layer, folderId, null);
 			result.put(SUCCESS, true);
 		}catch (Exception e){
 			e.printStackTrace();
 			result.put(SUCCESS, false);
 		}
 		
 		result.put(RESULTS, layer != null ? 1: 0);
 		result.put(ROOT, layer);
 
 		return result;
 	}
 }
