 package ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.components;
 
 import com.extjs.gxt.ui.client.Style;
 import com.extjs.gxt.ui.client.core.XTemplate;
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.widget.*;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.layout.FitData;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
 import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.*;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.utils.BaseAsyncCallback;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.mvc.events.CommonEvents;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.students.client.services.StudentService;
 import ru.sgu.csit.inoc.deansoffice.webui.gxt.common.shared.model.StudentDetailsModel;
 
 /**
  * User: KhurtinDN ( KhurtinDN@gmail.com )
  * Date: 12/28/10
  * Time: 3:04 PM
  */
 public class StudentAccountWindow extends Window {
     private Html headerHtml = new Html();
     private StudentDataForm studentDataForm = new StudentDataForm();
     private StudentDetailsModel studentDetailsModel;
 
     private StudentDetailsLoaderAsyncCallback studentDetailsLoaderAsyncCallback =
             new StudentDetailsLoaderAsyncCallback();
     private StudentDetailsSaverAsyncCallback studentDetailsSaverAsyncCallback =
             new StudentDetailsSaverAsyncCallback();
 
     public StudentAccountWindow() {
         setSize(800, 600);
         setModal(true);
         setBlinkModal(true);
         setMaximizable(true);
         setButtonAlign(Style.HorizontalAlignment.RIGHT);
 
         addButton(new Button("Сохранить", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 if (studentDataForm.isValid()) {
                     saveStudentDetailsModel();
                 } else {
                     Dispatcher.forwardEvent(CommonEvents.InfoWithConfirmation,
                             "Пожалуйста, корректно заполните все поля!");
                 }
             }
         }));
 
         addButton(new Button("Сгенерировать в PDF", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                final String url = new StringBuilder(GWT.getHostPageBaseURL())
                        .append("documents/dossier.pdf")
                        .append("?studentId=").append(studentDetailsModel.getId())
                        .toString();
                 com.google.gwt.user.client.Window.open(url, "_blank", "");
             }
         }));
 
         addButton(new Button("Закрыть", new SelectionListener<ButtonEvent>() {
             @Override
             public void componentSelected(ButtonEvent ce) {
                 StudentAccountWindow.this.hide();
             }
         }));
     }
 
     @Override
     protected void onRender(Element parent, int pos) {
         super.onRender(parent, pos);
 
         setLayout(new FitLayout());
 
         TabPanel tabPanel = new TabPanel();
         tabPanel.add(createFaceTabItem());
 //        tabPanel.add(createBackTabItem());
 
         add(tabPanel, new FitData(4));
     }
 
     private TabItem createFaceTabItem() {
         Viewport viewport = new Viewport();
         viewport.setLayout(new VBoxLayout(VBoxLayout.VBoxLayoutAlign.STRETCH));
 
         headerHtml.setStyleName("studentAccount");
         viewport.add(headerHtml);
 
         VBoxLayoutData flex = new VBoxLayoutData(10, 0, 0, 0);
         flex.setFlex(1);
         studentDataForm.setScrollMode(Style.Scroll.AUTOY);
         viewport.add(studentDataForm, flex);
 
         TabItem faceTabItem = new TabItem("Учетная карточка студента");
         faceTabItem.setLayout(new FitLayout());
         faceTabItem.add(viewport);
 
         return faceTabItem;
     }
 
     /*private TabItem createBackTabItem() {
         TabItem backTabItem = new TabItem("Задняя сторона");
         backTabItem.addText("Здесь была задняя сторона");
         return backTabItem;
     }*/
 
     private String getHeaderTemplate() {
         return new StringBuilder()
                 .append("<img align=\"left\" width=\"100px\" src=\"photos/{photoId}.jpg\" />")
                 .append("<h1>Учетная карточка студента</h1>")
                 .append("<div class=\"stFIO\">")
                 .append("<span class=\"stLabel\">ФИО:</span>")
                 .append("<span class=\"stValue\">{lastName}   {firstName}   {middleName}</span> ")
                 .append("</div>")
                 .append("<div class=\"stSpeciality\">")
                 .append("<span class=\"stLabel\">Специальность (направление):</span>")
                 .append("<span class=\"stValue\">{specialityFullName} ({specialityName})</span>")
                 .append("<span class=\"stLabel\">Шифр (по ОКСО):</span><span class=\"stValue\">{specialityCode}</span>")
                 .append("</div>")
                 .toString();
     }
 
     public void showStudentAccount(Long studentId) {
         StudentService.Util.getInstance().loadStudentDetails(studentId, studentDetailsLoaderAsyncCallback);
     }
 
     private void showStudentAccountWindow(StudentDetailsModel studentDetailsModel) {
         this.studentDetailsModel = studentDetailsModel;
         setHeading("Данные студента: " + studentDetailsModel.getFullName());
         XTemplate template = XTemplate.create(getHeaderTemplate());
 
         prepareStudentDetail(studentDetailsModel);
         headerHtml.setHtml(template.applyTemplate(com.extjs.gxt.ui.client.util.Util.getJsObject(studentDetailsModel, 1)));
 
         studentDataForm.addStyleName("studentDataForm");
         studentDataForm.setStudentDetails(studentDetailsModel);
 
         show();
     }
 
     private void saveStudentDetailsModel() {
         studentDetailsModel = studentDataForm.getStudentDetails();
         StudentService.Util.getInstance().saveStudentDetails(studentDetailsModel, studentDetailsSaverAsyncCallback);
     }
 
     private void prepareStudentDetail(StudentDetailsModel studentDetailsModel) {
         studentDetailsModel.set("specialityFullName", studentDetailsModel.getSpeciality().getFullName());
         studentDetailsModel.set("specialityName", studentDetailsModel.getSpeciality().getName());
         studentDetailsModel.set("specialityCode", studentDetailsModel.getSpeciality().getCode());
     }
 
     @Override
     public void show() {
         super.show();
         maximize();
         layout(true);
     }
 
     private class StudentDetailsLoaderAsyncCallback extends BaseAsyncCallback<StudentDetailsModel> {
         @Override
         public void onSuccess(StudentDetailsModel studentDetailsModel) {
             showStudentAccountWindow(studentDetailsModel);
         }
     }
 
     private class StudentDetailsSaverAsyncCallback extends BaseAsyncCallback<Void> {
         @Override
         public void onSuccess(Void result) {
             Info.display("Информация", "Информация о студенте обнавлена успешно!");
         }
     }
 }
