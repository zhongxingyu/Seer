 /**
  * Copyright 2013 Jim Hurne and Joseph Kramer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kdt.program;
 
 import static org.hamcrest.CoreMatchers.allOf;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.kdt.CommonConstants.NONE;
 import static org.kdt.Visible.HIDDEN;
 import static org.kdt.Visible.VISIBLE;
 import static org.kdt.program.ProgramView.Message.TAG_PROGRAMMED;
 import static org.kdt.program.ProgramView.Message.TASK_NOT_FOUND;
 import static org.kdt.program.Programer.ThereWas.A_TAG_TO_PROGRAM;
 import static org.kdt.program.Programer.ThereWas.NO_TAG_TO_PROGRAM;
 import static org.kdt.tag.TagType.CELL;
 import static org.kdt.tag.TagType.TASK;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.kdt.Settings;
 import org.kdt.Visible;
 import org.kdt.program.ProgramView.Message;
 import org.kdt.program.Programer.ThereWas;
 import org.kdt.tag.Cell;
 import org.kdt.tag.Empty;
 import org.kdt.tag.Programable;
 import org.kdt.tag.Scanable;
 import org.kdt.tag.TagType;
 import org.kdt.tag.Task;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 public class ProgramPresenterTest {
     private static final RuntimeException TASK_LOOKUP_FAILED = new TaskFinder.LookupFailed(
             new RuntimeException("test"));
 
     private ProgramPresenter presenter;
 
     @Mock
     private ProgramView mockView;
 
     @Mock
     private Programer mockTagProgramer;
 
     @Mock
     private ProgramModel mockModel;
 
     @Mock
     private Settings mockSettings;
 
     @Mock
     private TaskFinder mockFinder;
 
     @Before
     public void given_a_presenter() throws Exception {
         MockitoAnnotations.initMocks(this);
         presenter = new ProgramPresenter(
                 mockView,
                 mockModel,
                 mockSettings,
                 mockTagProgramer,
                 mockFinder);
     }
 
     @Test
     public void shows_cell_fields__when_tag_type_is_set_to_cell()
             throws Exception {
         when.presenter.typeChangedTo(TagType.CELL);
         then.it_should_call(mockView).setTaskDetailsVisible(HIDDEN);
         then.it_should_call(mockView).setCellDetailsVisible(VISIBLE);
     }
 
     @Test
     public void shows_task_fields__when_tag_type_is_set_to_task()
             throws Exception {
         when.presenter.typeChangedTo(TagType.TASK);
         then.it_should_call(mockView).setTaskDetailsVisible(VISIBLE);
         then.it_should_call(mockView).setCellDetailsVisible(HIDDEN);
     }
 
     @Test
     public void shows_task_fields__when_tag_type_is_anything_else()
             throws Exception {
         when.presenter.typeChangedTo(TagType.EMPTY);
         then.it_should_call(mockView).setTaskDetailsVisible(VISIBLE);
         then.it_should_call(mockView).setCellDetailsVisible(HIDDEN);
     }
 
     @Test
     public void programs_a_cell_tag__when_tag_is_tapped() throws Exception {
         given.the_tag_type_is(TagType.CELL);
         and.the_swimline_is_set_to("Android App");
         and.the_queue_is_set_to("Code Review");
         when.presenter.tagTapped();
         then.the_tag_should_be_programmed_using(new Cell("Android App",
                 "Code Review"));
     }
 
     @Test
     public void programs_a_task_tag__when_tag_is_tapped() throws Exception {
         given.the_tag_type_is(TagType.TASK);
         and.the_id_is_set_to("1234");
         and.the_name_is_set_to("User can program a tag");
         and.the_size_is_set_to("XL");
         when.presenter.tagTapped();
         then.the_tag_should_be_programmed_using(new Task("1234",
                 "User can program a tag", "XL"));
     }
 
     @Test
     public void tells_the_user_when_a_cell_tag_is_successfully_programmed()
             throws Exception {
         given.the_tag_type_is(TASK);
         and.the_programer_returns(A_TAG_TO_PROGRAM);
         when.presenter.tagTapped();
         then.it_should_call(mockView).showMessage(TAG_PROGRAMMED);
     }
 
     @Test
     public void tells_the_user_when_programing_a_tag_failed() {
         given.the_tag_type_is(TASK);
         and.the_programer_throws(an_exception());
         when.presenter.tagTapped();
         then.it_should_call(mockView).showException(the_exception());
 
     }
 
     @Test
     public void does_not_tell_the_user_when_a_tag_is_programmed__if_there_was_no_tag_to_program()
             throws Exception {
         given.the_programer_returns(NO_TAG_TO_PROGRAM);
         when.presenter.tagTapped();
         then.the_programed_tag_message_should_not_be_shown();
     }
 
     @Test
     public void updates_view_with_task_details__if_task_selected_when_the_view_is_made_visible()
             throws Exception {
         given.the_model_has_a_tag(1, new Task("1234", "User can program a tag",
                 "XL"));
         and.the_selected_tag_is(1);
         when.presenter.visibilityChanged(VISIBLE);
         then.it_should_call(mockView).setSelectedTagType(TASK);
         then.it_should_call(mockView).setTaskId("1234");
         then.it_should_call(mockView).setTaskName("User can program a tag");
         then.it_should_call(mockView).setTaskSize("XL");
     }
 
     @Test
     public void updates_view_with_cell_details__if_cell_selected_when_the_view_is_made_visible()
             throws Exception {
         given.the_model_has_a_tag(1, new Cell("Android App", "Code Review"));
         and.the_selected_tag_is(1);
         when.presenter.visibilityChanged(VISIBLE);
         then.it_should_call(mockView).setSelectedTagType(CELL);
         then.it_should_call(mockView).setSwimlane("Android App");
         then.it_should_call(mockView).setQueue("Code Review");
     }
 
     @Test
     public void clears_view__if_an_invalid_tag_selected_when_the_view_is_made_visible()
             throws Exception {
         given.the_model_has_a_tag(1, new Empty());
         and.the_selected_tag_is(1);
         when.presenter.visibilityChanged(VISIBLE);
         then.the_view_should_be_cleared_and_task_type_should_be_selected();
     }
 
     @Test
     public void clears_view__if_no_tag_selected_when_the_view_is_made_visible()
             throws Exception {
         given.the_selected_tag_is(-1);
         when.presenter.visibilityChanged(VISIBLE);
         then.the_view_should_be_cleared_and_task_type_should_be_selected();
     }
 
     @Test
     public void replaces_selected_tag__when_tag_programmed()
             throws Exception {
         given.the_selected_tag_is(3);
 
         // TODO the following setup is duplicated on several tests
         and.the_tag_type_is(TagType.CELL);
         and.the_swimline_is_set_to("Android App");
         and.the_queue_is_set_to("Code Review");
         and.the_programer_returns(A_TAG_TO_PROGRAM);
 
         when.presenter.tagTapped();
 
         then.the_selected_tag_should_be_replaced_with(3, new Cell(
                 "Android App",
                 "Code Review"));
     }
 
     @Test
     public void does_not_replace_selected_tag__when_programing_a_tag_throws_an_exception()
             throws Exception {
         and.the_programer_throws(an_exception());
 
         when.presenter.tagTapped();
 
         then.the_selected_tag_should_NOT_be_replaced();
     }
 
     @Test
     public void does_not_replace_selected_tag__when_there_was_no_tag_to_program()
             throws Exception {
         and.the_programer_returns(NO_TAG_TO_PROGRAM);
 
         when.presenter.tagTapped();
 
         then.the_selected_tag_should_NOT_be_replaced();
     }
 
     @Test
     public void does_not_replace_selected_tag__when_no_tag_is_selected()
             throws Exception {
         given.the_selected_tag_is(NONE);
         and.the_programer_returns(A_TAG_TO_PROGRAM);
 
         when.presenter.tagTapped();
 
         then.the_selected_tag_should_NOT_be_replaced();
     }
 
     @Test
     public void shows_the_lookup_button__if_bluetooth_is_enabled_and_supported__when_screen_is_first_displayed()
             throws Exception {
         given.bluetooth_is_enabled(true);
         and.bluetooth_is_supported(true);
         when.presenter.viewInitalized();
         then.the_lookup_button_should_be(VISIBLE);
     }
 
     @Test
     public void hides_the_lookup_button__if_bluetooth_is_disabled__when_screen_is_first_displayed()
             throws Exception {
         given.bluetooth_is_enabled(false);
         and.bluetooth_is_supported(true);
         when.presenter.viewInitalized();
         then.the_lookup_button_should_be(HIDDEN);
     }
 
     @Test
     public void hides_the_lookup_button__if_bluetooth_is_unsupported__when_screen_is_first_displayed()
             throws Exception {
         given.bluetooth_is_supported(false);
         and.bluetooth_is_enabled(true);
         when.presenter.viewInitalized();
         then.the_lookup_button_should_be(HIDDEN);
     }
 
     @Test
     public void shows_the_lookup_button__if_bluetooth_is_enabled_and_supported__when_settings_changed()
             throws Exception {
         given.bluetooth_is_enabled(true);
         and.bluetooth_is_supported(true);
         when.presenter.settingsUpdated();
         then.the_lookup_button_should_be(VISIBLE);
     }
 
     @Test
     public void hides_the_lookup_button__if_bluetooth_is_disabled__when_screen_settings_changed()
             throws Exception {
         given.bluetooth_is_enabled(false);
         and.bluetooth_is_supported(true);
         when.presenter.settingsUpdated();
         then.the_lookup_button_should_be(HIDDEN);
     }
 
     @Test
     public void hides_the_lookup_button__if_bluetooth_is_unsupported__when_screen_settings_changed()
             throws Exception {
         given.bluetooth_is_enabled(true);
         and.bluetooth_is_supported(false);
         when.presenter.settingsUpdated();
         then.the_lookup_button_should_be(HIDDEN);
     }
 
     @Test
     public void looks_up_task_details__when_the_lookup_button_is_clicked()
             throws Exception {
         given.the_id_is_set_to("1234");
         and.the_task_finder_can_find("1234", "Looked up summary", "XXL");
         when.presenter.lookupClicked();
         then.it_should_call(mockView).setTaskName("Looked up summary");
         then.it_should_call(mockView).setTaskSize("XXL");
     }
 
     @Test
     public void does_not_look_up_task_details__when_the_lookup_button_is_clicked__and_the_task_id_is_empty()
             throws Exception {
         given.the_id_is_set_to("");
         and.the_task_finder_can_find("", "Looked up summary", "XXL");
         when.presenter.lookupClicked();
         then.it_should_not_call(mockView).setTaskName("Looked up summary");
         then.it_should_not_call(mockView).setTaskSize("XXL");
     }
 
     @Test
     public void does_not_look_up_task_details__when_the_lookup_button_is_clicked__and_the_task_id_is_null()
             throws Exception {
         given.the_id_is_set_to(null);
         and.the_task_finder_can_find(null, "Looked up summary", "XXL");
         when.presenter.lookupClicked();
         then.it_should_not_call(mockView).setTaskName("Looked up summary");
         then.it_should_not_call(mockView).setTaskSize("XXL");
     }
 
     @Test
     public void displays_task_not_found_message__when_the_lookup_button_is_clicked__but_no_task_is_found()
             throws Exception {
 
         given.the_id_is_set_to("1234");
         and.the_task_finder_does_not_find_a_task_for("1234");
         when.presenter.lookupClicked();
         then.it_should_call(mockView).showMessage(TASK_NOT_FOUND);
     }
 
     @Test
     public void displays_message_and_exception__when_the_lookup_button_is_clicked__but_the_finder_throws_an_exception()
             throws Exception {
 
         given.the_id_is_set_to("1234");
         and.the_task_finder_throws(TASK_LOOKUP_FAILED);
         when.presenter.lookupClicked();
         then.it_should_call(mockView).showException(Message.TASK_LOOKUP_FAILED,
                 TASK_LOOKUP_FAILED);
     }
 
     private void the_task_finder_throws(RuntimeException exception) {
         when(mockFinder.find(Mockito.anyString())).thenThrow(exception);
     }
 
     private void the_task_finder_does_not_find_a_task_for(String id) {
         when(mockFinder.find(id)).thenReturn(Task.NONE);
     }
 
     private void the_task_finder_can_find(String id, String summary,
             String size) {
         when(mockFinder.find(id)).thenReturn(
                 new Task(id, summary, size));
     }
 
     private void the_lookup_button_should_be(Visible visible) {
         verify(mockView).setLookupButtonVisible(visible);
     }
 
     private void bluetooth_is_enabled(boolean isEnabled) {
         when(mockSettings.isBluetoothEnabled()).thenReturn(isEnabled);
     }
 
     private void bluetooth_is_supported(boolean isSupported) {
         when(mockSettings.isBluetoothSupported()).thenReturn(isSupported);
     }
 
     private void the_selected_tag_should_NOT_be_replaced() {
         verify(mockModel, times(0)).replace(
                 Mockito.anyInt(),
                 Mockito.any(Scanable.class));
     }
 
     private void the_selected_tag_should_be_replaced_with(int position,
             Scanable tag) {
         verify(mockModel).replace(Mockito.eq(3), Mockito.refEq(tag));
     }
 
     private void the_model_has_a_tag(int position, Scanable tag) {
         when(mockModel.get(position)).thenReturn(tag);
     }
 
     private void the_selected_tag_is(int position) {
         when(mockModel.getSelectedTagIndex()).thenReturn(position);
         if (position < 0) {
             when(mockModel.get(position)).thenThrow(
                     new IndexOutOfBoundsException());
         }
     }
 
     private void the_programer_throws(ProgramingException an_exception) {
         doThrow(an_exception).when(mockTagProgramer).programTag(
                 Mockito.<Programable> any());
     }
 
     private ProgramingException an_exception() {
         testException = new ProgramingException("Something bad");
         return testException;
     }
 
     private ProgramingException the_exception() {
         return testException;
     }
 
     private void the_tag_should_be_programmed_using(Cell cell) {
         // TODO Pull into a commonly used matcher??
        verify(mockTagProgramer).programTag((Programable)
                Mockito.argThat(allOf(
                        Matchers.<Cell> hasProperty("swimlane",
                                equalTo(cell.getSwimlane())),
                        Matchers.<Cell> hasProperty("queue",
                                equalTo(cell.getQueue())))));
     }
 
     private void the_tag_should_be_programmed_using(Task task) {
         // TODO Pull into a commonly used matcher??
        verify(mockTagProgramer).programTag((Programable)
                Mockito.argThat(allOf(
                        Matchers.<Task> hasProperty("id",
                                equalTo(task.getId())),
                        Matchers.<Task> hasProperty(
                                "name", equalTo(task.getName())),
                        Matchers
                                .<Task> hasProperty("size",
                                        equalTo(task.getSize())))));
     }
 
     private void the_tag_type_is(TagType type) {
         Mockito.when(mockView.getSelectedTagType()).thenReturn(type);
     }
 
     private void the_swimline_is_set_to(String swimlane) {
         Mockito.when(mockView.getSwimlane()).thenReturn(swimlane);
     }
 
     private void the_queue_is_set_to(String queue) {
         Mockito.when(mockView.getQueue()).thenReturn(queue);
     }
 
     private void the_id_is_set_to(String id) {
         Mockito.when(mockView.getTaskId()).thenReturn(id);
     }
 
     private void the_name_is_set_to(String name) {
         Mockito.when(mockView.getTaskName()).thenReturn(name);
     }
 
     private void the_size_is_set_to(String size) {
         Mockito.when(mockView.getTaskSize()).thenReturn(size);
     }
 
     private <T> T it_should_call(T mock) {
         return Mockito.verify(mock);
     }
 
     private <T> T it_should_not_call(T mock) {
         return Mockito.verify(mock, times(0));
     }
 
     private void the_programed_tag_message_should_not_be_shown() {
         Mockito.verify(mockView, times(0)).showMessage(
                 ProgramView.Message.TAG_PROGRAMMED);
     }
 
     private void the_programer_returns(ThereWas whatThereWas) {
         when(mockTagProgramer.programTag(Mockito.any(Programable.class)))
                 .thenReturn(whatThereWas);
     }
 
     private void the_view_should_be_cleared_and_task_type_should_be_selected() {
         it_should_call(mockView).setSelectedTagType(TagType.TASK);
         it_should_call(mockView).setTaskId("");
         it_should_call(mockView).setTaskName("");
         it_should_call(mockView).setTaskSize("");
     }
 
     @SuppressWarnings("unused")
     private ProgramPresenterTest given = this, when = this, then = this,
             and = this, with = this;
 
     private ProgramingException testException;
 }
