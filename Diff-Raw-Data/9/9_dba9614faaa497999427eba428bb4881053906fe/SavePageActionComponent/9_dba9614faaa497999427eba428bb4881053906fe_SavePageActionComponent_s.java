 /*
  * Copyright (C) 2003-2010 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.wiki.webui.control.action;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.exoplatform.container.PortalContainer;
 import org.exoplatform.portal.webui.util.Util;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.web.application.ApplicationMessage;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.UIApplication;
 import org.exoplatform.webui.core.UIComponent;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.Event.Phase;
 import org.exoplatform.webui.exception.MessageException;
 import org.exoplatform.webui.ext.filter.UIExtensionFilter;
 import org.exoplatform.webui.ext.filter.UIExtensionFilters;
 import org.exoplatform.webui.form.UIFormInput;
 import org.exoplatform.webui.form.UIFormSelectBox;
 import org.exoplatform.webui.form.UIFormStringInput;
 import org.exoplatform.webui.form.UIFormTextAreaInput;
 import org.exoplatform.wiki.commons.Utils;
 import org.exoplatform.wiki.mow.api.Page;
 import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
 import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
 import org.exoplatform.wiki.rendering.RenderingService;
 import org.exoplatform.wiki.resolver.TitleResolver;
 import org.exoplatform.wiki.service.WikiPageParams;
 import org.exoplatform.wiki.service.WikiService;
 import org.exoplatform.wiki.webui.EditMode;
 import org.exoplatform.wiki.webui.UIWikiPageControlArea;
 import org.exoplatform.wiki.webui.UIWikiPageEditForm;
 import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
 import org.exoplatform.wiki.webui.UIWikiPortlet;
 import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.UIWikiTemplateDescriptionContainer;
 import org.exoplatform.wiki.webui.WikiMode;
 import org.exoplatform.wiki.webui.control.filter.IsEditAddModeFilter;
 import org.exoplatform.wiki.webui.control.filter.IsEditAddPageModeFilter;
 import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;
 import org.xwiki.rendering.syntax.Syntax;
 
 /**
  * Created by The eXo Platform SAS
  * Author : viet nguyen
  *          viet.nguyen@exoplatform.com
  * Apr 26, 2010  
  */
 @ComponentConfig(
   events = {
     @EventConfig(listeners = SavePageActionComponent.SavePageActionListener.class, phase = Phase.DECODE)
   }
 )
 public class SavePageActionComponent extends UIComponent {
 
   public static final String                   ACTION   = "SavePage";  
   
   private static final Log log = ExoLogger.getLogger("wiki:SavePageActionComponent");
   
   private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
       new IsEditAddModeFilter(), new IsEditAddPageModeFilter() });
 
   @UIExtensionFilters
   public List<UIExtensionFilter> getFilters() {
     return FILTERS;
   }
   
   public void validate(UIFormInput uiInput) throws Exception {
    String invalidCharacters = ": @ / \\ | ^ # ; [ ] { } < > * ' \" +"; // and .
     Object[] args = { invalidCharacters };
     
     if (uiInput.getValue() == null || ((String) uiInput.getValue()).trim().length() == 0) {
       throw new MessageException(new ApplicationMessage("WikiPageNameValidator.msg.EmptyTitle", args, ApplicationMessage.WARNING));
     }
     
     String s = (String) uiInput.getValue();
     for (int i = 0; i < s.length(); i++) {
       char c = s.charAt(i);
       // Does not accept the following characters in the title of a page : @ / \ | ^ # ; [ ] { } < > . * ' " +
       if (Character.isLetter(c)
           || Character.isDigit(c)
           || (c != ':' && c != '@' && c != '/' && c != '\\' && c != '|' && c != '^' && c != '#'
               && c != ';' && c != '[' && c != ']' && c != '{' && c != '}' && c != '<' && c != '>'
              && c != '.' && c != '*' && c != '\'' && c != '\"' && c != '+')
           ) {
         continue;
       }
       throw new MessageException(new ApplicationMessage("WikiPageNameValidator.msg.Invalid-char", args, ApplicationMessage.WARNING));
     }
   }
   
   public static class SavePageActionListener extends
                                             UIPageToolBarActionListener<SavePageActionComponent> {
     @Override
     protected void processEvent(Event<SavePageActionComponent> event) throws Exception {
       WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
       UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
       WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
       UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
       UIWikiPageTitleControlArea pageTitleControlForm = wikiPortlet.findComponentById(UIWikiPageControlArea.TITLE_CONTROL);
       UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
       UIWikiRichTextArea wikiRichTextArea = pageEditForm.getChild(UIWikiRichTextArea.class);
       UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                  .getUIStringInput();      
       UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
       UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
       UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);
       RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
       Page page = Utils.getCurrentWikiPage();
       try {
         event.getSource().validate(titleInput);
       } catch (MessageException ex) {
         uiApp.addMessage(ex.getDetailMessage());
         event.getRequestContext().setProcessRender(true);
       }
       if (event.getRequestContext().getProcessRender()) {
         event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
         Utils.redirect(pageParams, wikiPortlet.getWikiMode());
         return;
       }
 
       String title = titleInput.getValue().trim();
       if (wikiRichTextArea.isRendered()) {
         String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();
         String markupContent = renderingService.render(htmlContent,
                                                        Syntax.XHTML_1_0.toIdString(),
                                                        syntaxTypeSelectBox.getValue(),
                                                        false);
         markupInput.setValue(markupContent);
       }
       String markup = (markupInput.getValue() == null) ? "" : markupInput.getValue();
       markup = markup.trim();
       String syntaxId = syntaxTypeSelectBox.getValue();
       try {
         String newPageId = TitleResolver.getId(title, false);
         if (wikiPortlet.getWikiMode() == WikiMode.EDITPAGE) {
           if (wikiPortlet.getEditMode() == EditMode.SECTION) {
             newPageId = page.getName();
             title = page.getContent().getTitle();
             markup = renderingService.updateContentOfSection(page.getContent().getText(),
                                                              page.getContent().getSyntax(),
                                                              wikiPortlet.getSectionIndex(),
                                                              markup);
           }
           if (!page.getName().equals(newPageId)) {
             wikiService.renamePage(pageParams.getType(),
                                    pageParams.getOwner(),
                                    page.getName(),
                                    newPageId,
                                    title);
           }
           Object minorAtt = event.getRequestContext().getAttribute(MinorEditActionComponent.ACTION);
           if (minorAtt != null) {
             ((PageImpl) page).setMinorEdit(Boolean.parseBoolean(minorAtt.toString()));
           }
           page.getContent().setText(markup);
           page.getContent().setComment(commentInput.getValue());
           page.getContent().setSyntax(syntaxId);
           pageTitleControlForm.getUIFormInputInfo().setValue(title);
 
           if (!pageEditForm.getTitle().equals(title)) {
             page.getContent().setTitle(title);
             ((PageImpl) page).checkin();
             ((PageImpl) page).checkout();
             pageParams.setPageId(newPageId);
           } else {
             ((PageImpl) page).checkin();
             ((PageImpl) page).checkout();
           }
         } else if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
           String pageId = TitleResolver.getId(title, false);
           boolean isExist = wikiService.isExisting(pageParams.getType(),
                                                    pageParams.getOwner(),
                                                    pageId);
           if (isExist) {
             log.error("The title '" + title + "' is already existing!");
             uiApp.addMessage(new ApplicationMessage("SavePageAction.msg.warning-page-title-already-exist",
                                                     null,
                                                     ApplicationMessage.WARNING));
             event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
             Utils.redirect(pageParams, wikiPortlet.getWikiMode());
             return;
           }
           String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
           Page draftPage = wikiService.getExsitedOrNewDraftPageById(null, null, sessionId);
           Collection<AttachmentImpl> attachs = ((PageImpl) draftPage).getAttachments();
 
           Page subPage = wikiService.createPage(pageParams.getType(),
                                                 pageParams.getOwner(),
                                                 title,
                                                 page.getName());
           subPage.getContent().setText(markup);
           subPage.getContent().setSyntax(syntaxId);
           ((PageImpl) subPage).getAttachments().addAll(attachs);
           ((PageImpl) subPage).checkin();
           ((PageImpl) subPage).checkout();
           pageParams.setPageId(pageId);
           ((PageImpl) draftPage).remove();
           return;
         }
       } catch (Exception e) {
         log.error("An exception happens when saving the page with title:" + title, e);
         uiApp.addMessage(new ApplicationMessage("UIPageToolBar.msg.Exception",
                                                 null,
                                                 ApplicationMessage.ERROR));
         event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
       } finally {
         Utils.redirect(pageParams, WikiMode.VIEW);
         super.processEvent(event);
       }
     }
   }
 }
