 package com.gooki.webapp.controller;
 
 import com.gooki.model.Block;
import com.gooki.model.Cong;
 import com.gooki.service.BlockManager;
 import com.gooki.service.CongManager;
 import com.gooki.webapp.constant.TerritoryConstants;
 import com.gooki.webapp.exception.BlockExistsException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 @Controller
 @RequestMapping("/blocks")
 public class BlockController extends TerritoryBaseController {
 	private BlockManager blockManager;
     private CongManager congManager;
 
     @Autowired
     public void setBlockManager(BlockManager blockManager) {
         this.blockManager = blockManager;
     }
 
     @Autowired
     public void setCongManager(CongManager congManager) {
         this.congManager = congManager;
     }
 
     @RequestMapping(value = "/edit/{cong}", method = RequestMethod.GET)
     public ModelAndView handleEditBlockCongRequest(@PathVariable String cong) throws Exception {
         Model model = new ExtendedModelMap();
         model.addAttribute(TerritoryConstants.BLOCK_LIST.getValue(), blockManager.getBlocksByCongName(cong));
 
         return new ModelAndView("editBlocks", model.asMap());
     }
 
     @RequestMapping(value = "/editBlocks", method = RequestMethod.GET)
     public ModelAndView handleEditBlockRequest() throws Exception {
         Model model = new ExtendedModelMap();
        model.addAttribute(TerritoryConstants.CONGREGATION_LIST.getValue(), congManager.getAllUniqueCongs());
 
         return new ModelAndView("editBlocksDefault", model.asMap());
     }
 
     @RequestMapping(value = "/update", method = RequestMethod.POST)
     public @ResponseBody Map<String, Object> handleUpdateBlockRequest(@RequestBody Block block) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         try {
             blockManager.updateBlock(block);
             map.put("status", TerritoryConstants.JSON_OK_RESPONSE.getValue());
             map.put("message", "Successfully updated!");
         } catch (AccessDeniedException ade) {
             map.put("status", TerritoryConstants.JSON_NO_PERMISSION_RESPONSE.getValue());
             map.put("message", "Oops. It looks like you don't have permission for this operation.");
         } catch (Exception e) {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Oops. Something went wrong and could not update the block info. Please try later.");
         }
 
         return map;
     }
 
     @RequestMapping(value = "/delete", method = RequestMethod.POST)
     public @ResponseBody Map<String, Object> handleDeleteBlockRequest(@RequestBody String id) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         try {
             blockManager.removeBlock(blockManager.get(Long.parseLong(id)));
             map.put("status", TerritoryConstants.JSON_OK_RESPONSE.getValue());
             map.put("message", "Successfully deleted!");
         } catch (AccessDeniedException ade) {
             map.put("status", TerritoryConstants.JSON_NO_PERMISSION_RESPONSE.getValue());
             map.put("message", "Oops. It looks like you don't have permission for this operation.");
         } catch (Exception e) {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Oops. Something went wrong and could not delete the block. Please try later.");
         }
 
         return map;
     }
 
     @RequestMapping(value = "/{cong}", method = RequestMethod.GET)
     public @ResponseBody List<Block> handleGetBlocksRequest(@PathVariable String cong) throws Exception {
         try {
            return blockManager.getBlocksByCongName(cong);
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
 
     }
 
     @RequestMapping(value = "new", method = RequestMethod.POST)
     public @ResponseBody Map<String, Object> handleRequest(@RequestBody Block block) throws Exception {
         Map<String, Object> map = new HashMap<String, Object>();
 
         try {
             blockManager.saveBlock(block);
             map.put("status", TerritoryConstants.JSON_OK_RESPONSE.getValue());
         } catch (BlockExistsException bee) {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Oops. It looks like the same block already exists. Please try another block name / number.");
         } catch (AccessDeniedException ade) {
             map.put("status", TerritoryConstants.JSON_NO_PERMISSION_RESPONSE.getValue());
             map.put("message", "Oops. It looks like you don't have permission for this operation.");
         } catch (Exception e) {
             map.put("status", TerritoryConstants.JSON_FAIL_RESPONSE.getValue());
             map.put("message", "Oops. Something went wrong and could not save the block info. Please try later.");
         }
 
         return map;
     }
 
 }
