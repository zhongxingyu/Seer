 package com.mycompany.vgtu.pages.lectures;
 
 import com.google.inject.Inject;
 import com.mycompany.vgtu.domain.lecture.VideoLectureCategoryJpa;
 import com.mycompany.vgtu.domain.lecture.VideoLectureCategoryService;
 import com.mycompany.vgtu.domain.lecture.VideoLectureJpa;
 import com.mycompany.vgtu.domain.lecture.VideoLectureService;
 import java.util.List;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.link.InlineFrame;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 
 public class LecturesListPanel extends Panel {
 
     private static final long serialVersionUID = 1L;
     @Inject
     private VideoLectureService videoLectureService;
     @Inject
     private VideoLectureCategoryService videoLectureCategoryService;
     private WebMarkupContainer lecturesContainer;
     private IModel<List<VideoLectureJpa>> listOfLecturesModel;
     private VideoLectureCategoryJpa selectedCategory;
 
     public LecturesListPanel(String wicketId) {
         super(wicketId);
         this.listOfLecturesModel = new LoadableDetachableModel<List<VideoLectureJpa>>() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected List<VideoLectureJpa> load() {
                 if (selectedCategory == null) {
                     return videoLectureService.loadAllVideoLectures();
                 } else {
                     return videoLectureService.loadAllVideoLecturesByCategory(selectedCategory.getId());
                 }
             }
         };
     }
 
     @Override
     protected void onInitialize() {
         super.onInitialize();
         Form form = new Form("form");
         form.add(getLectureCategoryDropDown("category"));
         form.add(getAjaxSubmitButtonToLoadLectures("submitSearch"));
         add(form);
        add(initListViewContainer("lecturesContainer", "lecturesRepeater"));
     }
 
     private Component getLectureCategoryDropDown(String wicketId) {
         IChoiceRenderer<VideoLectureCategoryJpa> renderer = new IChoiceRenderer<VideoLectureCategoryJpa>() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public Object getDisplayValue(VideoLectureCategoryJpa object) {
                 return object.getName();
             }
 
             @Override
             public String getIdValue(VideoLectureCategoryJpa object, int index) {
                 return object.getId().toString();
             }
         };
         Model<VideoLectureCategoryJpa> model = new Model<VideoLectureCategoryJpa>() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public VideoLectureCategoryJpa getObject() {
                 return selectedCategory;
             }
 
             @Override
             public void setObject(VideoLectureCategoryJpa object) {
                 super.setObject(object);
                 selectedCategory = object;
             }
         };
         return new DropDownChoice<VideoLectureCategoryJpa>(wicketId, model, new LoadableDetachableModel<List<VideoLectureCategoryJpa>>() {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected List<VideoLectureCategoryJpa> load() {
                 return videoLectureCategoryService.loaddAllVideoLectureCategories();
             }
         }, renderer);
     }
 
     private Component getAjaxSubmitButtonToLoadLectures(String wicketId) {
 
         return new AjaxButton(wicketId) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 super.onSubmit(target, form);
                 target.add(lecturesContainer);
             }
         };
     }
 
     private Component initListViewContainer(String wicketId, String repeaterId) {
         lecturesContainer = new WebMarkupContainer(wicketId);
         ListView<VideoLectureJpa> listView = new ListView<VideoLectureJpa>(repeaterId, listOfLecturesModel) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void populateItem(ListItem<VideoLectureJpa> item) {
                 VideoLectureJpa lecture = item.getModelObject();
                 item.add(getVideoTitle("lectureTitle", lecture.getName()));
                 item.add(getVideoDescription("lectureDescription", lecture.getDescription()));
                 item.add(getVideoFrame("lectureVideoFrame", lecture.getUrl()));
             }
         };
         lecturesContainer.add(listView);
         lecturesContainer.setOutputMarkupId(true);
         return lecturesContainer;
     }
 
     private Component getVideoTitle(String wicketId, String description) {
         return new Label(wicketId, description);
     }
 
     private Component getVideoDescription(String wicketId, String description) {
         return new Label(wicketId, description);
     }
 
     private Component getVideoFrame(final String wicketId, final String url) {
         InlineFrame frame = new InlineFrame(wicketId, WebPage.class) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected CharSequence getURL() {
                 return url;
             }
         };
         frame.add(new AttributeModifier("frameborder", "0"));
         frame.add(new AttributeModifier("height", "315"));
         frame.add(new AttributeModifier("width", "420"));
         return frame;
     }
 }
