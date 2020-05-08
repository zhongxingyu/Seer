 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web.noc;
 
 import static nl.surfnet.bod.web.WebUtils.ID_KEY;
 import static nl.surfnet.bod.web.WebUtils.PAGE_KEY;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 import javax.validation.constraints.Pattern;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 
 import nl.surfnet.bod.domain.EnniPort;
 import nl.surfnet.bod.domain.NbiPort;
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.VlanRanges;
 import nl.surfnet.bod.nsi.NsiConstants;
 import nl.surfnet.bod.service.AbstractFullTextSearchService;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.service.ReservationService;
 import nl.surfnet.bod.util.Functions;
 import nl.surfnet.bod.web.WebUtils;
 import nl.surfnet.bod.web.base.AbstractSearchableSortableListController;
 import nl.surfnet.bod.web.base.MessageManager;
 import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortCommand;
 import nl.surfnet.bod.web.noc.PhysicalPortController.PhysicalPortFilter;
 import nl.surfnet.bod.web.security.RichUserDetails;
 import nl.surfnet.bod.web.view.PhysicalPortView;
 
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @Controller
 @RequestMapping("/noc/physicalports/enni")
 public class EnniPortController extends AbstractSearchableSortableListController<PhysicalPortView, EnniPort> {
 
   @Resource private PhysicalPortService physicalPortService;
   @Resource private ReservationService reservationService;
   @Resource private MessageManager messageManager;
 
   @RequestMapping(method = RequestMethod.GET)
   @Override
   public String list(
       @RequestParam(value = PAGE_KEY, required = false) Integer page,
       @RequestParam(value = "sort", required = false) String sort,
       @RequestParam(value = "order", required = false) String order, Model model) {
 
     addEnniPortFilter(model);
 
     return super.list(page, sort, order, model);
   }
 
   private void addEnniPortFilter(Model model) {
     model.addAttribute(WebUtils.FILTER_SELECT, PhysicalPortFilter.ENNI_ALLOCATED);
     model.addAttribute(WebUtils.FILTER_LIST, PhysicalPortFilter.getAvailableFilters());
   }
 
   @Override
   @RequestMapping(value = "search", method = RequestMethod.GET)
   public String search(Integer page, String sort, String order, String search, Model model) {
     addEnniPortFilter(model);
     return super.search(page, sort, order, search, model);
   }
 
   @RequestMapping(method = RequestMethod.POST)
   public String create(@Valid CreateEnniPortCommand command, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
     Optional<NbiPort> nbiPort = physicalPortService.findNbiPort(command.getNmsPortId());
     if (!nbiPort.isPresent()) {
       return "redirect:";
     }
 
     EnniPort enniPort = (EnniPort) PhysicalPort.create(nbiPort.get());
 
     if (enniPort.isVlanRequired() && StringUtils.isEmpty(command.getVlanRanges())) {
       result.rejectValue("vlanRanges", "validation.not.empty");
     }
     if (physicalPortService.findByBodPortId(command.getBodPortId()) != null) {
       result.rejectValue("bodPortId", "validation.not.unique");
     }
     if(!PhysicalPortController.containsLetters(command.getBodPortId())) {
       result.rejectValue("bodPortId", "validation.should.contain.letter");
     }
 
     if (result.hasErrors()) {
       model.addAttribute("createPortCommand", command);
       model.addAttribute("vlanRequired", enniPort.isVlanRequired());
       return "noc/physicalports/enni/create";
     }
 
     enniPort.setNocLabel(command.getNocLabel());
     enniPort.setBodPortId(command.getBodPortId());
     if (enniPort.isVlanRequired()) {
       enniPort.setVlanRanges(command.getVlanRanges());
     }
     enniPort.setOutboundPeer(command.getOutboundPeer());
     enniPort.setInboundPeer(command.getInboundPeer());
 
     physicalPortService.save(enniPort);
 
     messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_enni_created", enniPort.getNocLabel());
 
     return "redirect:/noc/physicalports/enni";
   }
 
   @RequestMapping(value = WebUtils.EDIT, params = ID_KEY, method = RequestMethod.GET)
   public String updateForm(@RequestParam(ID_KEY) Long id, Model model) {
     EnniPort port = physicalPortService.findEnniPort(id);
 
     if (port == null) {
       return "redirect:";
     }
 
     model.addAttribute("updateEnniPortCommand", new UpdateEnniPortCommand(port));
     model.addAttribute("vlanRequired", port.isVlanRequired());
 
     return "noc/physicalports/enni/update";
   }
 
   @RequestMapping(method = RequestMethod.PUT)
   public String update(@Valid UpdateEnniPortCommand command, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
     EnniPort enniPort = (EnniPort) physicalPortService.findByNmsPortId(command.getNmsPortId());
 
     if (enniPort == null) {
       return "redirect:";
     }
 
     if (enniPort.isVlanRequired() && StringUtils.isEmpty(command.getVlanRanges())) {
       result.rejectValue("vlanRanges", "validation.not.empty");
     }
     if (!enniPort.getBodPortId().equals(command.getBodPortId()) && physicalPortService.findByBodPortId(command.getBodPortId()) != null) {
       result.rejectValue("bodPortId", "validation.not.unique");
     }
     if (!PhysicalPortController.containsLetters(command.getBodPortId())) {
       result.rejectValue("bodPortId", "validation.should.contain.letter");
     }
 
     if (result.hasErrors()) {
       model.addAttribute("updateEnniPortCommand", command);
       model.addAttribute("vlanRequired", enniPort.isVlanRequired());
       return "noc/physicalports/enni/update";
     }
 
     enniPort.setNocLabel(command.getNocLabel());
     enniPort.setBodPortId(command.getBodPortId());
     enniPort.setInboundPeer(command.getInboundPeer());
     enniPort.setOutboundPeer(command.getOutboundPeer());
     if (enniPort.isVlanRequired()) {
       enniPort.setVlanRanges(command.getVlanRanges());
     }
     physicalPortService.save(enniPort);
 
     model.asMap().clear();
 
     messageManager.addInfoFlashMessage(redirectAttributes, "info_physicalport_enni_updated", enniPort.getNocLabel());
 
     return "redirect:/noc/physicalports/enni";
   }
 
   @Override
   protected List<? extends PhysicalPortView> transformToView(List<? extends EnniPort> entities, RichUserDetails user) {
     return Functions.transformAllocatedPhysicalPorts(entities, reservationService);
   }
 
   @Override
   protected List<String> translateSortProperty(String sortProperty) {
     if (sortProperty.equals("instituteName")) {
       return ImmutableList.of("physicalResourceGroup.institute.name");
     }
 
     return super.translateSortProperty(sortProperty);
   }
 
   @Override
   protected String listUrl() {
     return "noc/physicalports/enni/list";
   }
 
   @Override
   protected List<EnniPort> list(int firstPage, int maxItems, Sort sort, Model model) {
     return physicalPortService.findAllocatedEnniEntries(firstPage, maxItems, sort);
   }
 
   @Override
   protected String getDefaultSortProperty() {
     return "nocLabel";
   }
 
   @Override
   protected long count(Model model) {
     return physicalPortService.countEnniPorts();
   }
 
   @Override
   protected List<Long> getIdsOfAllAllowedEntries(Model model, Sort sort) {
     return physicalPortService.findEnniIds(Optional.<Sort> fromNullable(sort));
   }
 
   @Override
   protected AbstractFullTextSearchService<PhysicalPort> getFullTextSearchableService() {
     return physicalPortService;
   }
 
   public static class CreateEnniPortCommand extends PhysicalPortCommand {
 
     @NotEmpty
    @Pattern(regexp = NsiConstants.NURN_PATTERN_REGEXP, message = "nl.surfnet.bod.domain.urn.message")
     private String inboundPeer;
 
     @NotEmpty
    @Pattern(regexp = NsiConstants.NURN_PATTERN_REGEXP, message = "nl.surfnet.bod.domain.urn.message")
     private String outboundPeer;
 
     @VlanRanges private String vlanRanges;
 
     public CreateEnniPortCommand() {
     }
 
     public CreateEnniPortCommand(EnniPort port) {
       setNmsPortId(port.getNmsPortId());
       setNocLabel(port.getNocLabel());
       setBodPortId(port.getBodPortId());
 
       this.inboundPeer = port.getInboundPeer();
       this.outboundPeer = port.getOutboundPeer();
       this.vlanRanges = port.getVlanRanges();
     }
 
     public String getInboundPeer() {
       return inboundPeer;
     }
 
     public void setInboundPeer(String inboundPeer) {
       this.inboundPeer = inboundPeer;
     }
 
     public String getOutboundPeer() {
       return outboundPeer;
     }
 
     public void setOutboundPeer(String outboundPeer) {
       this.outboundPeer = outboundPeer;
     }
 
     public String getVlanRanges() {
       return vlanRanges;
     }
 
     public void setVlanRanges(String vlanRanges) {
       this.vlanRanges = vlanRanges;
     }
   }
 
   public static class UpdateEnniPortCommand extends CreateEnniPortCommand {
     private Long id;
     private Integer version;
 
     public UpdateEnniPortCommand() {
     }
 
     public UpdateEnniPortCommand(EnniPort enniPort) {
       super(enniPort);
 
       this.version = enniPort.getVersion();
       this.id = enniPort.getId();
     }
 
     public Long getId() {
       return id;
     }
 
     public void setId(Long id) {
       this.id = id;
     }
 
     public Integer getVersion() {
       return version;
     }
 
     public void setVersion(Integer version) {
       this.version = version;
     }
   }
 }
