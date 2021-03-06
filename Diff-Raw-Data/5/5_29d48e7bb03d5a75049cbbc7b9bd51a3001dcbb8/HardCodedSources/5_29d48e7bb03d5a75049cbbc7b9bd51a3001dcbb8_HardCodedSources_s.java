 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.contacts.model;
 
 import com.android.contacts.R;
 import com.android.contacts.model.ContactsSource.DataKind;
 import com.android.contacts.model.ContactsSource.EditField;
 import com.android.contacts.model.ContactsSource.EditType;
 import com.android.contacts.model.ContactsSource.StringInflater;
 import com.android.contacts.model.EntityDelta.ValuesDelta;
 import com.google.android.collect.Lists;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.Groups;
 import android.provider.ContactsContract.CommonDataKinds.Email;
 import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
 import android.provider.ContactsContract.CommonDataKinds.Im;
 import android.provider.ContactsContract.CommonDataKinds.Nickname;
 import android.provider.ContactsContract.CommonDataKinds.Note;
 import android.provider.ContactsContract.CommonDataKinds.Organization;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 import android.provider.ContactsContract.CommonDataKinds.StructuredName;
 import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
 import android.provider.ContactsContract.CommonDataKinds.Website;
 import android.provider.ContactsContract.Contacts.Data;
 import android.view.inputmethod.EditorInfo;
 
 import java.util.ArrayList;
 
 /**
  * Hard-coded definition of some {@link ContactsSource} constraints, since the
  * XML language hasn't been finalized.
  */
 public class HardCodedSources {
     // TODO: finish hard-coding all constraints
 
     public static final String ACCOUNT_TYPE_GOOGLE = "com.google.GAIA";
     public static final String ACCOUNT_TYPE_EXCHANGE = "com.android.exchange";
     public static final String ACCOUNT_TYPE_FACEBOOK = "com.facebook.auth.login";
     public static final String ACCOUNT_TYPE_FALLBACK = "com.example.fallback-contacts";
 
     private static final int FLAGS_PHONE = EditorInfo.TYPE_CLASS_PHONE;
     private static final int FLAGS_EMAIL = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
     private static final int FLAGS_PERSON_NAME = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME;
     private static final int FLAGS_PHONETIC = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_VARIATION_PHONETIC;
     private static final int FLAGS_GENERIC_NAME = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS;
     private static final int FLAGS_NOTE = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
     private static final int FLAGS_WEBSITE = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_VARIATION_URI;
     private static final int FLAGS_POSTAL = EditorInfo.TYPE_CLASS_TEXT
             | EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS
             | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE;
 
     private HardCodedSources() {
         // Static utility class
     }
 
     /**
      * Hard-coded instance of {@link ContactsSource} for fallback use.
      */
     static void buildFallback(Context context, ContactsSource list) {
         {
             // FALLBACK: STRUCTUREDNAME
             DataKind kind = new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                     R.string.nameLabelsGroup, -1, -1, true);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME, R.string.name_given,
                     FLAGS_PERSON_NAME));
             kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME, R.string.name_family,
                     FLAGS_PERSON_NAME));
 
             list.add(kind);
         }
 
         {
             // FALLBACK: PHONE
             DataKind kind = new DataKind(Phone.CONTENT_ITEM_TYPE,
                     R.string.phoneLabelsGroup, android.R.drawable.sym_action_call, 10, true);
             kind.iconAltRes = R.drawable.sym_action_sms;
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionAltHeader = new ActionAltInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Phone.NUMBER);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: EMAIL
             DataKind kind = new DataKind(Email.CONTENT_ITEM_TYPE,
                     R.string.emailLabelsGroup, android.R.drawable.sym_action_email, 15, true);
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Email.DATA);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));
 
             list.add(kind);
         }
     }
 
     /**
      * Hard-coded instance of {@link ContactsSource} for Google Contacts.
      */
     static void buildGoogle(Context context, ContactsSource list) {
         {
             // GOOGLE: STRUCTUREDNAME
             DataKind kind = new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                     R.string.nameLabelsGroup, -1, -1, true);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                     FLAGS_PERSON_NAME, true));
             kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME, R.string.name_given,
                     FLAGS_PERSON_NAME));
             kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME, R.string.name_middle,
                     FLAGS_PERSON_NAME, true));
             kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME, R.string.name_family,
                     FLAGS_PERSON_NAME));
             kind.fieldList.add(new EditField(StructuredName.SUFFIX, R.string.name_suffix,
                     FLAGS_PERSON_NAME, true));
             kind.fieldList.add(new EditField(StructuredName.PHONETIC_GIVEN_NAME,
                     R.string.name_phonetic_given, FLAGS_PHONETIC, true));
             kind.fieldList.add(new EditField(StructuredName.PHONETIC_MIDDLE_NAME,
                     R.string.name_phonetic_middle, FLAGS_PHONETIC, true));
             kind.fieldList.add(new EditField(StructuredName.PHONETIC_FAMILY_NAME,
                     R.string.name_phonetic_family, FLAGS_PHONETIC, true));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: PHOTO
             DataKind kind = new DataKind(Photo.CONTENT_ITEM_TYPE, -1, -1, -1, true);
             list.add(kind);
         }
 
         {
             // GOOGLE: PHONE
             DataKind kind = new DataKind(Phone.CONTENT_ITEM_TYPE,
                     R.string.phoneLabelsGroup, android.R.drawable.sym_action_call, 10, true);
             kind.iconAltRes = R.drawable.sym_action_sms;
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionAltHeader = new ActionAltInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Phone.NUMBER);
 
             kind.typeColumn = Phone.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(Phone.TYPE_HOME, R.string.type_home, R.string.call_home,
                     R.string.sms_home));
             kind.typeList.add(new EditType(Phone.TYPE_MOBILE, R.string.type_mobile,
                     R.string.call_mobile, R.string.sms_mobile));
             kind.typeList.add(new EditType(Phone.TYPE_WORK, R.string.type_work, R.string.call_work,
                     R.string.sms_work));
             kind.typeList.add(new EditType(Phone.TYPE_FAX_WORK, R.string.type_fax_work,
                     R.string.call_fax_work, R.string.sms_fax_work).setSecondary(true));
             kind.typeList.add(new EditType(Phone.TYPE_FAX_HOME, R.string.type_fax_home,
                     R.string.call_fax_home, R.string.sms_fax_home).setSecondary(true));
             kind.typeList.add(new EditType(Phone.TYPE_PAGER, R.string.type_pager,
                     R.string.call_pager, R.string.sms_pager).setSecondary(true));
             kind.typeList.add(new EditType(Phone.TYPE_OTHER, R.string.type_other,
                     R.string.call_other, R.string.sms_other));
             kind.typeList.add(new EditType(Phone.TYPE_CUSTOM, R.string.type_custom,
                     R.string.call_custom, R.string.sms_custom).setSecondary(true).setCustomColumn(
                     Phone.LABEL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: EMAIL
             DataKind kind = new DataKind(Email.CONTENT_ITEM_TYPE,
                     R.string.emailLabelsGroup, android.R.drawable.sym_action_email, 15, true);
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Email.DATA);
 
             kind.typeColumn = Email.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList
                     .add(new EditType(Email.TYPE_HOME, R.string.type_home, R.string.email_home));
             kind.typeList
                     .add(new EditType(Email.TYPE_WORK, R.string.type_work, R.string.email_work));
             kind.typeList.add(new EditType(Email.TYPE_OTHER, R.string.type_other,
                     R.string.email_other));
             kind.typeList.add(new EditType(Email.TYPE_CUSTOM, R.string.type_custom,
                     R.string.email_home).setSecondary(true).setCustomColumn(Email.LABEL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: IM
             DataKind kind = new DataKind(Im.CONTENT_ITEM_TYPE, R.string.imLabelsGroup,
                     android.R.drawable.sym_action_chat, 20, true);
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Im.DATA);
 
             // NOTE: even though a traditional "type" exists, for editing
             // purposes we're using the network to pick labels
 
             kind.defaultValues = new ContentValues();
             kind.defaultValues.put(Im.TYPE, Im.TYPE_OTHER);
 
             kind.typeColumn = Im.PROTOCOL;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(Im.PROTOCOL_AIM, R.string.type_im_aim,
                     R.string.chat_aim));
             kind.typeList.add(new EditType(Im.PROTOCOL_MSN, R.string.type_im_msn,
                     R.string.chat_msn));
             kind.typeList.add(new EditType(Im.PROTOCOL_YAHOO, R.string.type_im_yahoo,
                     R.string.chat_yahoo));
             kind.typeList.add(new EditType(Im.PROTOCOL_SKYPE, R.string.type_im_skype,
                     R.string.chat_skype));
             kind.typeList.add(new EditType(Im.PROTOCOL_QQ, R.string.type_im_qq, R.string.chat_qq));
             kind.typeList.add(new EditType(Im.PROTOCOL_GOOGLE_TALK, R.string.type_im_google_talk,
                     R.string.chat_gtalk));
             kind.typeList.add(new EditType(Im.PROTOCOL_ICQ, R.string.type_im_icq,
                     R.string.chat_icq));
             kind.typeList.add(new EditType(Im.PROTOCOL_JABBER, R.string.type_im_jabber,
                     R.string.chat_jabber));
             kind.typeList.add(new EditType(Im.PROTOCOL_CUSTOM, R.string.type_custom,
                     R.string.chat_other).setSecondary(true).setCustomColumn(Im.CUSTOM_PROTOCOL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Im.DATA, R.string.imLabelsGroup, FLAGS_EMAIL));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: POSTAL
             DataKind kind = new DataKind(StructuredPostal.CONTENT_ITEM_TYPE,
                     R.string.postalLabelsGroup, R.drawable.sym_action_map, 25, true);
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             // TODO: build body from various structured fields
             kind.actionBody = new SimpleInflater(StructuredPostal.FORMATTED_ADDRESS);
 
             kind.typeColumn = StructuredPostal.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(StructuredPostal.TYPE_HOME, R.string.type_home,
                     R.string.map_home));
             kind.typeList.add(new EditType(StructuredPostal.TYPE_WORK, R.string.type_work,
                     R.string.map_work));
             kind.typeList.add(new EditType(StructuredPostal.TYPE_OTHER, R.string.type_other,
                     R.string.map_other));
             kind.typeList
                     .add(new EditType(StructuredPostal.TYPE_CUSTOM, R.string.type_custom,
                             R.string.map_custom).setSecondary(true).setCustomColumn(
                             StructuredPostal.LABEL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(StructuredPostal.STREET, R.string.postal_street,
                     FLAGS_POSTAL));
             kind.fieldList.add(new EditField(StructuredPostal.POBOX, R.string.postal_pobox,
                     FLAGS_POSTAL, true));
             kind.fieldList.add(new EditField(StructuredPostal.NEIGHBORHOOD,
                     R.string.postal_neighborhood, FLAGS_POSTAL, true));
             kind.fieldList.add(new EditField(StructuredPostal.CITY, R.string.postal_city,
                     FLAGS_POSTAL));
             kind.fieldList.add(new EditField(StructuredPostal.REGION, R.string.postal_region,
                     FLAGS_POSTAL));
             kind.fieldList.add(new EditField(StructuredPostal.POSTCODE, R.string.postal_postcode,
                     FLAGS_POSTAL));
             kind.fieldList.add(new EditField(StructuredPostal.COUNTRY, R.string.postal_country,
                     FLAGS_POSTAL, true));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: ORGANIZATION
             DataKind kind = new DataKind(Organization.CONTENT_ITEM_TYPE,
                     R.string.organizationLabelsGroup, R.drawable.sym_action_organization, 30, true);
 
            kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.organizationLabelsGroup);
             // TODO: build body from multiple fields
             kind.actionBody = new SimpleInflater(Organization.TITLE);
 
             kind.typeColumn = Organization.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(Organization.TYPE_WORK, R.string.type_work));
             kind.typeList.add(new EditType(Organization.TYPE_OTHER, R.string.type_other));
             kind.typeList.add(new EditType(Organization.TYPE_CUSTOM, R.string.type_custom)
                     .setSecondary(true).setCustomColumn(Organization.LABEL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Organization.COMPANY, R.string.ghostData_company,
                     FLAGS_GENERIC_NAME));
             kind.fieldList.add(new EditField(Organization.TITLE, R.string.ghostData_title,
                     FLAGS_GENERIC_NAME));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: NOTE
             DataKind kind = new DataKind(Note.CONTENT_ITEM_TYPE,
                     R.string.label_notes, R.drawable.sym_note, 110, true);
             kind.secondary = true;
 
             kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.label_notes);
             kind.actionBody = new SimpleInflater(Note.NOTE);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Note.NOTE, R.string.label_notes, FLAGS_NOTE));
 
             list.add(kind);
         }
 
         {
             // GOOGLE: NICKNAME
             DataKind kind = new DataKind(Nickname.CONTENT_ITEM_TYPE,
                     R.string.nicknameLabelsGroup, -1, 115, true);
             kind.secondary = true;
 
             kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.nicknameLabelsGroup);
             kind.actionBody = new SimpleInflater(Nickname.NAME);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Nickname.NAME, R.string.nicknameLabelsGroup,
                     FLAGS_PERSON_NAME));
 
             list.add(kind);
         }
 
         // TODO: GOOGLE: GROUPMEMBERSHIP
 
         {
             // GOOGLE: WEBSITE
             DataKind kind = new DataKind(Website.CONTENT_ITEM_TYPE,
                     R.string.websiteLabelsGroup, -1, 120, true);
             kind.secondary = true;
 
             kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.websiteLabelsGroup);
             kind.actionBody = new SimpleInflater(Website.URL);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Website.URL, R.string.websiteLabelsGroup, FLAGS_WEBSITE));
 
             list.add(kind);
         }
     }
 
     // TODO: this should come from resource in the future
     private static final String GOOGLE_MY_CONTACTS_GROUP = "System Group: My Contacts";
 
     public static final void attemptMyContactsMembership(EntityDelta state, Context context) {
         final ContentResolver resolver = context.getContentResolver();
         final Cursor cursor = resolver.query(Groups.CONTENT_URI, new String[] { Groups.SOURCE_ID },
                 Groups.TITLE + "=?", new String[] { GOOGLE_MY_CONTACTS_GROUP }, null);
         try {
             if (cursor.moveToFirst()) {
                 final ContentValues values = new ContentValues();
                 final String sourceId = cursor.getString(0);
                 values.put(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                 values.put(GroupMembership.GROUP_SOURCE_ID, sourceId);
                 state.addEntry(ValuesDelta.fromAfter(values));
             }
         } finally {
             cursor.close();
         }
     }
 
     /**
      * The constants below are shared with the Exchange sync adapter, and are
      * currently static. These values should be maintained in parallel.
      */
     private static final int TYPE_EMAIL1 = 20;
     private static final int TYPE_EMAIL2 = 21;
     private static final int TYPE_EMAIL3 = 22;
 
     private static final int TYPE_IM1 = 23;
     private static final int TYPE_IM2 = 24;
     private static final int TYPE_IM3 = 25;
 
     private static final int TYPE_WORK2 = 26;
     private static final int TYPE_HOME2 = 27;
     private static final int TYPE_CAR = 28;
     private static final int TYPE_COMPANY_MAIN = 29;
     private static final int TYPE_MMS = 30;
     private static final int TYPE_RADIO = 31;
 
     /**
      * Hard-coded instance of {@link ContactsSource} for Exchange.
      */
     static void buildExchange(Context context, ContactsSource list) {
         {
             // EXCHANGE: STRUCTUREDNAME
             DataKind kind = new DataKind(StructuredName.CONTENT_ITEM_TYPE,
                     R.string.nameLabelsGroup, -1, -1, true);
             kind.typeOverallMax = 1;
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(StructuredName.PREFIX, R.string.name_prefix,
                     FLAGS_PERSON_NAME, true));
             kind.fieldList.add(new EditField(StructuredName.GIVEN_NAME, R.string.name_given,
                     FLAGS_PERSON_NAME));
             kind.fieldList.add(new EditField(StructuredName.MIDDLE_NAME, R.string.name_middle,
                     FLAGS_PERSON_NAME, true));
             kind.fieldList.add(new EditField(StructuredName.FAMILY_NAME, R.string.name_family,
                     FLAGS_PERSON_NAME));
             kind.fieldList.add(new EditField(StructuredName.SUFFIX, R.string.name_suffix,
                     FLAGS_PERSON_NAME, true));
             kind.fieldList.add(new EditField(StructuredName.PHONETIC_GIVEN_NAME,
                     R.string.name_phonetic_given, FLAGS_PHONETIC, true));
             kind.fieldList.add(new EditField(StructuredName.PHONETIC_FAMILY_NAME,
                     R.string.name_phonetic_family, FLAGS_PHONETIC, true));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: PHOTO
             DataKind kind = new DataKind(Photo.CONTENT_ITEM_TYPE, -1, -1, -1, true);
             kind.typeOverallMax = 1;
             list.add(kind);
         }
 
         {
             // EXCHANGE: PHONE
             DataKind kind = new DataKind(Phone.CONTENT_ITEM_TYPE,
                     R.string.phoneLabelsGroup, android.R.drawable.sym_action_call, 10, true);
             kind.iconAltRes = R.drawable.sym_action_sms;
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionAltHeader = new ActionAltInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Phone.NUMBER);
 
             kind.typeColumn = Phone.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(Phone.TYPE_HOME, R.string.type_home, R.string.call_home,
                     R.string.sms_home).setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_HOME2, R.string.type_home_2, R.string.call_home_2,
                     R.string.sms_home_2).setSecondary(true).setSpecificMax(1));
             kind.typeList.add(new EditType(Phone.TYPE_MOBILE, R.string.type_mobile,
                     R.string.call_mobile, R.string.sms_mobile).setSpecificMax(1));
             kind.typeList.add(new EditType(Phone.TYPE_WORK, R.string.type_work, R.string.call_work,
                     R.string.sms_work).setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_WORK2, R.string.type_work_2, R.string.call_work_2,
                     R.string.sms_work_2).setSecondary(true).setSpecificMax(1));
             kind.typeList.add(new EditType(Phone.TYPE_FAX_WORK, R.string.type_fax_work,
                     R.string.call_fax_work, R.string.sms_fax_work).setSecondary(true)
                     .setSpecificMax(1));
             kind.typeList.add(new EditType(Phone.TYPE_FAX_HOME, R.string.type_fax_home,
                     R.string.call_fax_home, R.string.sms_fax_home).setSecondary(true)
                     .setSpecificMax(1));
             kind.typeList.add(new EditType(Phone.TYPE_PAGER, R.string.type_pager,
                     R.string.call_pager, R.string.sms_pager).setSecondary(true).setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_CAR, R.string.type_car, R.string.call_car,
                     R.string.sms_car).setSecondary(true).setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_COMPANY_MAIN, R.string.type_company_main,
                     R.string.call_company_main, R.string.sms_company_main).setSecondary(true)
                     .setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_MMS, R.string.type_mms, R.string.call_mms,
                     R.string.sms_mms).setSecondary(true).setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_RADIO, R.string.type_radio, R.string.call_radio,
                     R.string.sms_radio).setSecondary(true).setSpecificMax(1));
             kind.typeList.add(new EditType(Phone.TYPE_CUSTOM, R.string.type_assistant,
                     R.string.call_custom, R.string.sms_custom).setSecondary(true).setSpecificMax(1)
                     .setCustomColumn(Phone.LABEL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Phone.NUMBER, R.string.phoneLabelsGroup, FLAGS_PHONE));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: EMAIL
             DataKind kind = new DataKind(Email.CONTENT_ITEM_TYPE,
                     R.string.emailLabelsGroup, android.R.drawable.sym_action_email, 15, true);
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Email.DATA);
 
             kind.typeColumn = Email.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(TYPE_EMAIL1, R.string.type_email_1)
                     .setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_EMAIL2, R.string.type_email_2)
                     .setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_EMAIL3, R.string.type_email_3)
                     .setSpecificMax(1));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Email.DATA, R.string.emailLabelsGroup, FLAGS_EMAIL));
             kind.fieldList.add(new EditField(Email.DISPLAY_NAME, R.string.label_email_display_name,
                     FLAGS_PERSON_NAME));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: IM
             DataKind kind = new DataKind(Im.CONTENT_ITEM_TYPE, R.string.imLabelsGroup,
                     android.R.drawable.sym_action_chat, 20, true);
 
             kind.actionHeader = new ActionInflater(list.resPackageName, kind);
             kind.actionBody = new SimpleInflater(Im.DATA);
 
             kind.typeColumn = Im.TYPE;
             kind.typeList = new ArrayList<EditType>();
             kind.typeList.add(new EditType(TYPE_IM1, R.string.type_im_1).
                     setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_IM2, R.string.type_im_2).
                     setSpecificMax(1));
             kind.typeList.add(new EditType(TYPE_IM3, R.string.type_im_3).
                     setSpecificMax(1));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Im.DATA, R.string.imLabelsGroup, FLAGS_EMAIL));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: NICKNAME
             DataKind kind = new DataKind(Nickname.CONTENT_ITEM_TYPE,
                     R.string.nicknameLabelsGroup, -1, 115, true);
             kind.secondary = true;
             kind.typeOverallMax = 1;
 
             kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.nicknameLabelsGroup);
             kind.actionBody = new SimpleInflater(Nickname.NAME);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Nickname.NAME, R.string.nicknameLabelsGroup,
                     FLAGS_PERSON_NAME));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: WEBSITE
             DataKind kind = new DataKind(Website.CONTENT_ITEM_TYPE,
                     R.string.websiteLabelsGroup, -1, 120, true);
             kind.secondary = true;
             kind.typeOverallMax = 1;
 
             kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.websiteLabelsGroup);
             kind.actionBody = new SimpleInflater(Website.URL);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Website.URL, R.string.websiteLabelsGroup, FLAGS_WEBSITE));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: ORGANIZATION
             DataKind kind = new DataKind(Organization.CONTENT_ITEM_TYPE,
                     R.string.organizationLabelsGroup, R.drawable.sym_action_organization, 30, true);
 
            kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.organizationLabelsGroup);
             // TODO: build body from multiple fields
             kind.actionBody = new SimpleInflater(Organization.TITLE);
 
             kind.typeColumn = Organization.TYPE;
             kind.typeList = Lists.newArrayList();
             kind.typeList.add(new EditType(Organization.TYPE_WORK, R.string.type_work));
             kind.typeList.add(new EditType(Organization.TYPE_OTHER, R.string.type_other));
             kind.typeList.add(new EditType(Organization.TYPE_CUSTOM, R.string.type_custom)
                     .setSecondary(true).setCustomColumn(Organization.LABEL));
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Organization.COMPANY, R.string.ghostData_company,
                     FLAGS_GENERIC_NAME));
             kind.fieldList.add(new EditField(Organization.TITLE, R.string.ghostData_title,
                     FLAGS_GENERIC_NAME));
 
             list.add(kind);
         }
 
         {
             // EXCHANGE: NOTE
             DataKind kind = new DataKind(Note.CONTENT_ITEM_TYPE,
                     R.string.label_notes, R.drawable.sym_note, 110, true);
             kind.secondary = true;
 
             kind.actionHeader = new SimpleInflater(list.resPackageName, R.string.label_notes);
             kind.actionBody = new SimpleInflater(Note.NOTE);
 
             kind.fieldList = Lists.newArrayList();
             kind.fieldList.add(new EditField(Note.NOTE, R.string.label_notes, FLAGS_NOTE));
 
             list.add(kind);
         }
     }
 
     /**
      * Hard-coded instance of {@link ContactsSource} for Facebook.
      */
     static void buildFacebook(Context context, ContactsSource list) {
         // Rely on the fallback source for now, it has a generic set of sources
         buildFallback(context, list);
 
         list.accountType = ACCOUNT_TYPE_FACEBOOK;
         list.readOnly = true;
     }
 
     /**
      * Simple inflater that assumes a string resource has a "%s" that will be
      * filled from the given column.
      */
     public static class SimpleInflater implements StringInflater {
         private final String mPackageName;
         private final int mStringRes;
         private final String mColumnName;
 
         public SimpleInflater(String packageName, int stringRes) {
             this(packageName, stringRes, null);
         }
 
         public SimpleInflater(String columnName) {
             this(null, -1, columnName);
         }
 
         public SimpleInflater(String packageName, int stringRes, String columnName) {
             mPackageName = packageName;
             mStringRes = stringRes;
             mColumnName = columnName;
         }
 
         public CharSequence inflateUsing(Context context, Cursor cursor) {
             final int index = mColumnName != null ? cursor.getColumnIndex(mColumnName) : -1;
             final boolean validString = mStringRes > 0;
             final boolean validColumn = index != -1;
 
             final CharSequence stringValue = validString ? context.getText(mStringRes) : null;
             final CharSequence columnValue = validColumn ? cursor.getString(index) : null;
 
             if (validString && validColumn) {
                 return String.format(stringValue.toString(), columnValue);
             } else if (validString) {
                 return stringValue;
             } else if (validColumn) {
                 return columnValue;
             } else {
                 return null;
             }
         }
 
         public CharSequence inflateUsing(Context context, ContentValues values) {
             final boolean validColumn = values.containsKey(mColumnName);
             final boolean validString = mStringRes > 0;
 
             final CharSequence stringValue = validString ? context.getText(mStringRes) : null;
             final CharSequence columnValue = validColumn ? values.getAsString(mColumnName) : null;
 
             if (validString && validColumn) {
                 return String.format(stringValue.toString(), columnValue);
             } else if (validString) {
                 return stringValue;
             } else if (validColumn) {
                 return columnValue;
             } else {
                 return null;
             }
         }
     }
 
     /**
      * Simple inflater that will combine two string resources, usually to
      * provide an action string like "Call home", where "home" is provided from
      * {@link EditType#labelRes}.
      */
     public static class ActionInflater implements StringInflater {
         private String mPackageName;
         private DataKind mKind;
 
         public ActionInflater(String packageName, DataKind labelProvider) {
             mPackageName = packageName;
             mKind = labelProvider;
         }
 
         public CharSequence inflateUsing(Context context, Cursor cursor) {
             final EditType type = EntityModifier.getCurrentType(cursor, mKind);
             final boolean validString = (type != null && type.actionRes != 0);
             CharSequence actionString;
             if (type.customColumn != null) {
                 final int index = cursor.getColumnIndex(type.customColumn);
                 final String customLabel = cursor.getString(index);
                 actionString = String.format(context.getString(type.actionRes),
                         customLabel);
             } else {
                 actionString = context.getText(type.actionRes);
             }
             return validString ? actionString : null;
         }
 
         public CharSequence inflateUsing(Context context, ContentValues values) {
             final EditType type = EntityModifier.getCurrentType(values, mKind);
             final boolean validString = (type != null && type.actionRes != 0);
             CharSequence actionString;
             if (type.customColumn != null) {
                 final String customLabel = values.getAsString(type.customColumn);
                 actionString = String.format(context.getString(type.actionRes),
                         customLabel);
             } else {
                 actionString = context.getText(type.actionRes);
             }
             return validString ? actionString : null;
         }
     }
 
     public static class ActionAltInflater implements StringInflater {
         private String mPackageName;
         private DataKind mKind;
 
         public ActionAltInflater(String packageName, DataKind labelProvider) {
             mPackageName = packageName;
             mKind = labelProvider;
         }
 
         public CharSequence inflateUsing(Context context, Cursor cursor) {
             final EditType type = EntityModifier.getCurrentType(cursor, mKind);
             final boolean validString = (type != null && type.actionAltRes != 0);
             CharSequence actionString;
             if (type.customColumn != null) {
                 final int index = cursor.getColumnIndex(type.customColumn);
                 final String customLabel = cursor.getString(index);
                 actionString = String.format(context.getString(type.actionAltRes),
                         customLabel);
             } else {
                 actionString = context.getText(type.actionAltRes);
             }
             return validString ? actionString : null;
         }
 
         public CharSequence inflateUsing(Context context, ContentValues values) {
             final EditType type = EntityModifier.getCurrentType(values, mKind);
             final boolean validString = (type != null && type.actionAltRes != 0);
             CharSequence actionString;
             if (type.customColumn != null) {
                 final String customLabel = values.getAsString(type.customColumn);
                 actionString = String.format(context.getString(type.actionAltRes),
                         customLabel);
             } else {
                 actionString = context.getText(type.actionAltRes);
             }
             return validString ? actionString : null;
         }
     }
 }
