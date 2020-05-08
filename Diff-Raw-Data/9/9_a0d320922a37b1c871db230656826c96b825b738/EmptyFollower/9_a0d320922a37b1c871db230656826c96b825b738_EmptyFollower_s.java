 package com.wemakestuff.diablo3builder.sectionlist;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import android.os.AsyncTask;
 import android.os.ParcelUuid;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.wemakestuff.diablo3builder.R;
 import com.wemakestuff.diablo3builder.model.D3Application;
 import com.wemakestuff.diablo3builder.model.Skill;
 import com.wemakestuff.diablo3builder.sectionlist.EntrySkillAdapter.RowType;
 import com.wemakestuff.diablo3builder.string.Replacer;
 import com.wemakestuff.diablo3builder.string.Replacer.D3Color;
 import com.wemakestuff.diablo3builder.util.Util;
 
 public class EmptyFollower implements Item
 {
     private final String         name;
     private final String         iconName;
     private final String         description;
     private final UUID           followerUUID;
     private String               url;
     private List<ParcelUuid>     skills = new ArrayList<ParcelUuid>(); ;
     private final LayoutInflater inflater;
 
     public EmptyFollower(LayoutInflater inflater, String name, String shortDescription, String icon, UUID followerUUID, String url)
     {
         this.name = name;
         this.description = shortDescription;
         this.iconName = icon;
         this.followerUUID = followerUUID;
         this.url = url;
         this.inflater = inflater;
     }
 
     private static class ViewHolder
     {
         final LinearLayout emptyFollowerSkills;
         final TextView     emptyFollowerSkill1;
         final TextView     emptyFollowerSkill2;
         final TextView     emptyFollowerSkill3;
         final TextView     emptyFollowerSkill4;
         final TextView     emptyFollowerSkill1Description;
         final TextView     emptyFollowerSkill2Description;
         final TextView     emptyFollowerSkill3Description;
         final TextView     emptyFollowerSkill4Description;
 
         final ImageView    icon;
         final TextView     emptyItemName;
         final TextView     emptyFollowerDescription;
 
         public ViewHolder(LinearLayout emptyFollowerSkills, TextView emptyFollowerSkill1, TextView emptyFollowerSkill2, TextView emptyFollowerSkill3,
                 TextView emptyFollowerSkill4, TextView emptyFollowerSkill1Description, TextView emptyFollowerSkill2Description,
                 TextView emptyFollowerSkill3Description, TextView emptyFollowerSkill4Description, ImageView icon, TextView emptyItemName,
                 TextView emptyFollowerDescription)
         {
             super();
             this.emptyFollowerSkills = emptyFollowerSkills;
             this.emptyFollowerSkill1 = emptyFollowerSkill1;
             this.emptyFollowerSkill2 = emptyFollowerSkill2;
             this.emptyFollowerSkill3 = emptyFollowerSkill3;
             this.emptyFollowerSkill4 = emptyFollowerSkill4;
             this.emptyFollowerSkill1Description = emptyFollowerSkill1Description;
             this.emptyFollowerSkill2Description = emptyFollowerSkill2Description;
             this.emptyFollowerSkill3Description = emptyFollowerSkill3Description;
             this.emptyFollowerSkill4Description = emptyFollowerSkill4Description;
             this.icon = icon;
             this.emptyItemName = emptyItemName;
             this.emptyFollowerDescription = emptyFollowerDescription;
         }
 
     }
 
     public void setUrl(String url)
     {
         this.url = url;
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public String getIcon()
     {
         return iconName;
     }
 
     public String getName()
     {
         return name;
     }
 
     public UUID getFollowerUUID()
     {
         return followerUUID;
     }
 
     public List<ParcelUuid> getSkills()
     {
         return skills;
     }
 
     public String getFollowerUrl()
     {
         return url;
     }
 
     public void setSkills(List<ParcelUuid> list)
     {
         this.skills = list;
     }
 
     public ViewHolder setSkills(ViewHolder holder, List<ParcelUuid> list)
     {
         this.skills = list;
 
         List<TextView> skillTitles = new ArrayList<TextView>();
         List<TextView> skillDescriptions = new ArrayList<TextView>();
 
         skillTitles.add(holder.emptyFollowerSkill1);
         skillTitles.add(holder.emptyFollowerSkill2);
         skillTitles.add(holder.emptyFollowerSkill3);
         skillTitles.add(holder.emptyFollowerSkill4);
         skillDescriptions.add(holder.emptyFollowerSkill1Description);
         skillDescriptions.add(holder.emptyFollowerSkill2Description);
         skillDescriptions.add(holder.emptyFollowerSkill3Description);
         skillDescriptions.add(holder.emptyFollowerSkill4Description);
 
         int index = 0;
         for (ParcelUuid u : skills)
         {
             Skill s = D3Application.getInstance().getFollowerByName(name).getSkillByUUID(u.getUuid());
             if (s != null)
             {
                 loadTextAsync(holder, skillTitles.get(index), "Level " + s.getRequiredLevel() + ": " + s.getName(), ".+", D3Color.DIABLO_GOLD);
                 loadTextAsync(holder, skillDescriptions.get(index), s.getDescription().trim(), "\\d+%?", D3Color.DIABLO_GREEN);
             }
             index++;
         }
 
         holder.emptyFollowerSkills.setVisibility(View.VISIBLE);
 
         return holder;
     }
 
     @Override
     public int getViewType()
     {
         return RowType.EMPTY_FOLLOWER.ordinal();
     }
 
     @Override
     public View getView(View convertView)
     {
         ViewHolder holder;
         View view;
         if (convertView == null)
         {
             ViewGroup v = (ViewGroup) inflater.inflate(R.layout.list_item_empty_follower, null);
             //@formatter:off
             holder = new ViewHolder((LinearLayout) v.findViewById(R.id.empty_follower_skills)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_1)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_2)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_3)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_4)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_1_description)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_2_description)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_3_description)
                     , (TextView) v.findViewById(R.id.empty_follower_skill_4_description)
                     , (ImageView) v.findViewById (R.id.empty_follower_icon)
                     , (TextView) v.findViewById(R.id.empty_follower_name)
                     , (TextView) v.findViewById(R.id.empty_follower_description));
             //@formatter:on
             v.setTag(holder);
             view = v;
         }
         else
         {
             view = convertView;
             holder = (ViewHolder) convertView.getTag();
         }
 
         holder.emptyItemName.setText(name);
         holder.emptyFollowerDescription.setText(description);
 
         final int iconImg = Util.findImageResource(iconName);
         loadIconAsync(holder, iconImg);
 
         if (skills != null && skills.size() > 0)
             holder = setSkills(holder, skills);
         else
             holder.emptyFollowerSkills.setVisibility(View.GONE);
 
         return view;
     }
 
     private void loadIconAsync(ViewHolder holder, final int iconImg)
     {
         AsyncTask<ViewHolder, Void, Integer> loadImage = new AsyncTask<ViewHolder, Void, Integer>() {
             private ViewHolder v;
 
             @Override
             protected Integer doInBackground(ViewHolder... params)
             {
                 v = params[0];
                 return iconImg;
             }
 
             protected void onPostExecute(Integer result)
             {
                 super.onPostExecute(result);
                 v.icon.setImageResource(iconImg);
             }
 
         }.execute(holder);
     }
 
     private void loadTextAsync(ViewHolder holder, final TextView textView, final CharSequence text, final String regEx, final D3Color color)
     {
         AsyncTask<ViewHolder, Void, CharSequence> loadText = new AsyncTask<ViewHolder, Void, CharSequence>() {
             private ViewHolder v;
 
             @Override
             protected CharSequence doInBackground(ViewHolder... params)
             {
                 v = params[0];
                 return Replacer.replace(text, regEx, color);
             }
 
             protected void onPostExecute(CharSequence result)
             {
                 super.onPostExecute(result);
                 textView.setText(result);
                 textView.setVisibility(View.VISIBLE);
             }
 
         }.execute(holder);
     }
 
 }
