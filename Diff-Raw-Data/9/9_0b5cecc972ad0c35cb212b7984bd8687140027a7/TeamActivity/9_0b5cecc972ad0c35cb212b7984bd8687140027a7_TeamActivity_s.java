 package in.ccl.ui;
 
 import in.ccl.adapters.TeamGridAdapter;
 import in.ccl.adapters.TeamImagePagerAdapter;
 import in.ccl.database.CCLPullService;
 import in.ccl.helper.Category;
 import in.ccl.helper.PageChangeListener;
 import in.ccl.helper.Util;
 import in.ccl.model.TeamMember;
 import in.ccl.model.Teams;
 import in.ccl.util.Constants;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 public class TeamActivity extends TopActivity {
 
 	private static final int NO_OF_PAGES = 2;
 
 	protected RelativeLayout teamLayout;
 
 	// private ArrayList <TeamItems> items;
 
 	private ImageView indicatorOneImage;
 
 	private ImageView indicatorTwoImage;
 
 	private ImageView indicatorThreeImage;
 
 	private ImageView indicatorFourImage;
 
 	private ViewPager pager;
 
 	private LinearLayout IndicatorLayout;
 
 	private int previousState;
 
 	private int currentState;
 
 	private TextView teamTitle;
 
 	private TextView teamName;
 
 	private ViewPager teamMemberViewPager;
 
 	private ViewPager teamAmbassadorViewPager;
 
 	private ArrayList <Teams> teamLogosList;
 
 	private ArrayList <TeamMember> allTeamMembersTotalList;
 
 	private ArrayList <TeamMember> mumbaiTeamMembersTotalList;
 
 	private ArrayList <TeamMember> mumbaiTeamMembersList;
 
 	private ArrayList <TeamMember> mumbaiTeamAmbassadorsList;
 
 	private ArrayList <TeamMember> chennaiTeamMembersTotalList;
 
 	private ArrayList <TeamMember> chennaiTeamMembersList;
 
 	private ArrayList <TeamMember> chennaiTeamAmbassadorsList;
 
 	private ArrayList <TeamMember> teluguTeamMembersTotalList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> teluguTeamMembersList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> teluguTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> karanatakaTeamMembersTotalList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> karanatakaTeamMembersList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> karanatakaTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 	// kerala
 	private ArrayList <TeamMember> keralaTeamMembersTotalList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> keralaTeamMembersList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> keralaTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 	// bengal
 	private ArrayList <TeamMember> bengalTeamMembersTotalList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> bengalTeamMembersList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> bengalTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 	// veer
 	private ArrayList <TeamMember> veerTeamMembersTotalList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> veerTeamMembersList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> veerTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 	// bhojpuri
 	private ArrayList <TeamMember> bhojpuriTeamMembersTotalList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> bhojpuriTeamMembersList = new ArrayList <TeamMember>();
 
 	private ArrayList <TeamMember> bhojpuriTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addContent(R.layout.team_layout);
 		super.disableAds();
 		pager = (ViewPager) findViewById(R.id.team_view_pager);
 		teamMemberViewPager = (ViewPager) findViewById(R.id.team_member_pager);
 		teamAmbassadorViewPager = (ViewPager) findViewById(R.id.team_ambassadors_pager);
 		// teamLayout = (RelativeLayout) findViewById(R.id.team_desc);
 		indicatorOneImage = (ImageView) findViewById(R.id.indicator_one);
 		indicatorTwoImage = (ImageView) findViewById(R.id.indicator_two);
 		indicatorThreeImage = (ImageView) findViewById(R.id.indicator_three);
 		indicatorFourImage = (ImageView) findViewById(R.id.indicator_four);
 		IndicatorLayout = (LinearLayout) findViewById(R.id.team_page_indicator_layout);
 		teamName = (TextView) findViewById(R.id.txt_team_name);
 		// Util.setTextFont(this, teamName);
 
 		teamTitle = (TextView) findViewById(R.id.team_title);
		// Util.setTextFont(this, teamTitle);
 
 		TextView txtTheTeam = (TextView) findViewById(R.id.txt_theteam);
 		// Util.setTextFont(this, txtTheTeam);
 		TextView txtBrandTitle = (TextView) findViewById(R.id.txt_brand_ambassadors);
 		// Util.setTextFont(this, txtBrandTitle);
 
 		if (getIntent().hasExtra(Constants.EXTRA_TEAM_LOGO_KEY)) {
 			teamLogosList = getIntent().getParcelableArrayListExtra(Constants.EXTRA_TEAM_LOGO_KEY);
 			teamName.setText(teamLogosList.get(0).getName().toUpperCase());
 
 			allTeamMembersTotalList = getIntent().getParcelableArrayListExtra(Constants.EXTRA_TEAM_MEMBER_KEY);
 		}
 		// ****
 		mumbaiTeamMembersTotalList = new ArrayList <TeamMember>();
 		mumbaiTeamMembersList = new ArrayList <TeamMember>();
 		mumbaiTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 		chennaiTeamMembersTotalList = new ArrayList <TeamMember>();
 		chennaiTeamMembersList = new ArrayList <TeamMember>();
 		chennaiTeamAmbassadorsList = new ArrayList <TeamMember>();
 
 		// 1
 		if (allTeamMembersTotalList != null && allTeamMembersTotalList.size() > 0) {
 			for (int i = 0; i < allTeamMembersTotalList.size(); i++) {
 				String teamName = allTeamMembersTotalList.get(i).getTeamName();
 				if (teamName != null) {
 					if (teamName.equals(getResources().getString(R.string.mumbai))) {
 						mumbaiTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.chennai))) {
 						chennaiTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.telugu))) {
 						teluguTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.karanataka))) {
 						karanatakaTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.kerala))) {
 						keralaTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.bengal))) {
 						bengalTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.veer))) {
 						veerTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 					if (teamName.equals(getResources().getString(R.string.bhojpuri))) {
 						bhojpuriTeamMembersTotalList.add(allTeamMembersTotalList.get(i));
 					}
 				}
 			}
 		}
 		// 2
 		setTeamMembersList(mumbaiTeamMembersTotalList, mumbaiTeamMembersList, mumbaiTeamAmbassadorsList);
 		setTeamMembersList(chennaiTeamMembersTotalList, chennaiTeamMembersList, chennaiTeamAmbassadorsList);
 		setTeamMembersList(teluguTeamMembersTotalList, teluguTeamMembersList, teluguTeamAmbassadorsList);
 		setTeamMembersList(karanatakaTeamMembersTotalList, karanatakaTeamMembersList, karanatakaTeamAmbassadorsList);
 		setTeamMembersList(keralaTeamMembersTotalList, keralaTeamMembersList, keralaTeamAmbassadorsList);
 		setTeamMembersList(bengalTeamMembersTotalList, bengalTeamMembersList, bengalTeamAmbassadorsList);
 		setTeamMembersList(veerTeamMembersTotalList, veerTeamMembersList, veerTeamAmbassadorsList);
 		setTeamMembersList(bhojpuriTeamMembersTotalList, bhojpuriTeamMembersList, bhojpuriTeamAmbassadorsList);
 
 		/*
 		 * if (mumbaiTeamMembersTotalList != null && mumbaiTeamMembersTotalList.size() > 0) {
 		 * 
 		 * for (int i = 0; i < mumbaiTeamMembersTotalList.size(); i++) { String role = mumbaiTeamMembersTotalList.get(i).getRole().trim(); String dummyRole = getResources().getString(R.string.ambassadors); if (role.equals(dummyRole)) { mumbaiTeamAmbassadorsList.add(mumbaiTeamMembersTotalList.get(i)); }
 		 * else { mumbaiTeamMembersList.add(mumbaiTeamMembersTotalList.get(i));
 		 * 
 		 * } } }
 		 */
 		/*
 		 * if (chennaiTeamMembersTotalList != null && chennaiTeamMembersTotalList.size() > 0) {
 		 * 
 		 * for (int i = 0; i < chennaiTeamMembersTotalList.size(); i++) { String role = chennaiTeamMembersTotalList.get(i).getRole().trim(); String dummyRole = getResources().getString(R.string.ambassadors); if (role.equals(dummyRole)) {
 		 * chennaiTeamAmbassadorsList.add(chennaiTeamMembersTotalList.get(i)); } else { chennaiTeamMembersList.add(chennaiTeamMembersTotalList.get(i));
 		 * 
 		 * } } }
 		 */
 		setTeamMembersToAdapter(mumbaiTeamMembersList);
 		setTeamAmbassadorsToAdapter(mumbaiTeamAmbassadorsList);
 
 		pager.setAdapter(new TeamPagerAdapter(this, teamLogosList));
 
 		pager.setOnPageChangeListener(new OnPageChangeListener() {
 
 			@Override
 			public void onPageScrolled (int pageno, float arg1, int arg2) {
 				indicatorOneImage.setVisibility(View.VISIBLE);
 				indicatorTwoImage.setVisibility(View.INVISIBLE);
 				indicatorThreeImage.setVisibility(View.INVISIBLE);
 				indicatorFourImage.setVisibility(View.INVISIBLE);
 
 				if (pageno == 0) {
 					teamName.setText(teamLogosList.get(0).getName().toUpperCase());
 
 					setTeamMembersToAdapter(mumbaiTeamMembersList);
 					setTeamAmbassadorsToAdapter(mumbaiTeamAmbassadorsList);
 				}
 				else {
 					teamName.setText(teamLogosList.get(4).getName().toUpperCase());
 
 					setTeamMembersToAdapter(keralaTeamMembersList);
 					setTeamAmbassadorsToAdapter(keralaTeamAmbassadorsList);
 				}
 			}
 
 			@Override
 			public void onPageScrollStateChanged (int state) {
 				int currentPage = pager.getCurrentItem();
 				if (currentPage == 1 || currentPage == 0) {
 					previousState = currentState;
 					currentState = state;
 					if (previousState == 1 && currentState == 0) {
 						pager.setCurrentItem(currentPage == 0 ? 2 : 0);
 					}
 				}
 			}
 
 			@Override
 			public void onPageSelected (int position) {
 				if (IndicatorLayout != null) {
 					Util.setTeamPageIndicator(position, IndicatorLayout);
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onResume () {
 		super.onResume();
 		if (Util.getInstance().isOnline(this)) {
 			Intent mServiceIntent = new Intent(this, CCLPullService.class).setData(Uri.parse(getResources().getString(R.string.team_url)));
 			startService(mServiceIntent);
 		}
 		else {
 			Toast.makeText(this, getResources().getString(R.string.network_error_message), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void setTeamMembersList (ArrayList <TeamMember> totalTeamMembersList, ArrayList <TeamMember> teamMembersList, ArrayList <TeamMember> teamAmbassadorsList) {
 		if (totalTeamMembersList != null && totalTeamMembersList.size() > 0) {
 
 			for (int i = 0; i < totalTeamMembersList.size(); i++) {
 				String role = totalTeamMembersList.get(i).getRole().trim();
 				String dummyRole = getResources().getString(R.string.ambassadors);
 				if (role.equals(dummyRole)) {
 					System.out.println("nagesh team ambassidors list"+totalTeamMembersList.get(i));
 					teamAmbassadorsList.add(totalTeamMembersList.get(i));
 				}
 				else {
 					teamMembersList.add(totalTeamMembersList.get(i));
 
 				}
 			}
 		}
 	}
 
 	private void setTeamMembersToAdapter (ArrayList <TeamMember> teamMembersList) {
 		if (teamMembersList != null && teamMembersList.size() > 0) {
 			teamMemberViewPager.setAdapter(new TeamImagePagerAdapter(this, teamMembersList, Category.TEAM_MEMBERS));
 			teamMemberViewPager.setOnClickListener(null);
 			teamMemberViewPager.setOnPageChangeListener(new PageChangeListener(null, teamMemberViewPager));
 		}
 	}
 
 	private void setTeamAmbassadorsToAdapter (ArrayList <TeamMember> teamAmbassadorsList) {
 		if (teamAmbassadorsList != null) {
 			teamAmbassadorViewPager.setAdapter(new TeamImagePagerAdapter(this, teamAmbassadorsList, Category.TEAM_AMBASSADORS));
 			teamAmbassadorViewPager.setOnClickListener(null);
 			teamAmbassadorViewPager.setOnPageChangeListener(new PageChangeListener(null, teamAmbassadorViewPager));
 		}
 	}
 
 	public class TeamPagerAdapter extends PagerAdapter {
 
 		private Activity activity;
 
 		// private ArrayList <TeamItems> teamItems;
 
 		private LayoutInflater inflater;
 
 		public TeamPagerAdapter (Activity ctx, ArrayList <Teams> list) {
 			activity = ctx;
 			teamLogosList = list;
 			inflater = activity.getLayoutInflater();
 
 		}
 
 		@Override
 		public int getCount () {
 			return NO_OF_PAGES;
 		}
 
 		@Override
 		public View instantiateItem (ViewGroup views, final int position) {
 
 			View imageLayout = null;
 			imageLayout = inflater.inflate(R.layout.team_grid_view, null);
 			GridView gridView = (GridView) imageLayout.findViewById(R.id.grid_view);
 			/*
 			 * final ArrayList <TeamItems> items = new ArrayList <TeamItems>();
 			 * 
 			 * items.add(teamItems.get(NO_OF_PAGES * position)); items.add(teamItems.get((NO_OF_PAGES * position) + 1)); items.add(teamItems.get((NO_OF_PAGES * position) + 2)); items.add(teamItems.get((NO_OF_PAGES * position) + 3));
 			 */// int[] teams = new int[4];
 			String[] teamLogoUrls = new String[4];
 
 			if (position == 0) {
 				if (teamLogosList != null) {
 					teamLogoUrls[0] = teamLogosList.get(0).getLogo();
 					teamLogoUrls[1] = teamLogosList.get(1).getLogo();
 					teamLogoUrls[2] = teamLogosList.get(2).getLogo();
 					teamLogoUrls[3] = teamLogosList.get(3).getLogo();
 				}
 				else {
 					Log.e("TeamActivity", "Teams Logo are not availble");
 				}
 
 			}
 			else {
 				if (teamLogosList != null) {
 					teamLogoUrls[0] = teamLogosList.get(4).getLogo();
 					teamLogoUrls[1] = teamLogosList.get(5).getLogo();
 					teamLogoUrls[2] = teamLogosList.get(6).getLogo();
 					teamLogoUrls[3] = teamLogosList.get(7).getLogo();
 				}
 				else {
 					Log.e("TeamActivity", "Teams Logo are not availble");
 				}
 
 			}
 			gridView.setAdapter(new TeamGridAdapter(TeamActivity.this, teamLogoUrls));
 			gridView.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick (AdapterView <?> arg0, View arg1, int pos, long arg3) {// 3
 					switch (pos) {
 						case 0:
 							indicatorOneImage.setVisibility(View.VISIBLE);
 							indicatorTwoImage.setVisibility(View.INVISIBLE);
 							indicatorThreeImage.setVisibility(View.INVISIBLE);
 							indicatorFourImage.setVisibility(View.INVISIBLE);
 							if (position == 0) {
 								teamName.setText(teamLogosList.get(pos).getName().toUpperCase());
 								setTeamMembersToAdapter(mumbaiTeamMembersList);
 								setTeamAmbassadorsToAdapter(mumbaiTeamAmbassadorsList);
 								/*
 								 * if (mumbaiTeamMembersList != null && mumbaiTeamMembersList.size() > 0) { teamMemberViewPager.setAdapter(new TeamImagePagerAdapter(TeamActivity.this, mumbaiTeamMembersList, Category.TEAM_MEMBERS)); teamMemberViewPager.setOnClickListener(null);
 								 * teamMemberViewPager.setOnPageChangeListener(new PageChangeListener(null, teamMemberViewPager)); } if (mumbaiTeamAmbassadorsList != null && mumbaiTeamAmbassadorsList.size() > 0) { teamAmbassadorViewPager.setAdapter(new TeamImagePagerAdapter(TeamActivity.this,
 								 * mumbaiTeamAmbassadorsList, Category.TEAM_AMBASSADORS)); teamAmbassadorViewPager.setOnClickListener(null); teamAmbassadorViewPager.setOnPageChangeListener(new PageChangeListener(null, teamAmbassadorViewPager)); }
 								 */
 							}
 							else {
 								teamName.setText(teamLogosList.get(pos + 4).getName().toUpperCase());
 								setTeamMembersToAdapter(keralaTeamMembersList);
 								setTeamAmbassadorsToAdapter(keralaTeamAmbassadorsList);
 							}
 							break;
 						case 1:
 							indicatorOneImage.setVisibility(View.INVISIBLE);
 							indicatorTwoImage.setVisibility(View.VISIBLE);
 							indicatorThreeImage.setVisibility(View.INVISIBLE);
 							indicatorFourImage.setVisibility(View.INVISIBLE);
 							if (position == 0) {
 								teamName.setText(teamLogosList.get(pos).getName().toUpperCase());
 								setTeamMembersToAdapter(chennaiTeamMembersList);
 								setTeamAmbassadorsToAdapter(chennaiTeamAmbassadorsList);
 								/*
 								 * System.out.println("kranthi chennai team list"+chennaiTeamMembersList); System.out.println("kranthi chennai team amblist size"+chennaiTeamAmbassadorsList);
 								 * 
 								 * if (chennaiTeamMembersList != null && chennaiTeamMembersList.size() > 0) { System.out.println("kranthi chennai team list size"+chennaiTeamMembersList.size());
 								 * 
 								 * teamMemberViewPager.setAdapter(new TeamImagePagerAdapter(TeamActivity.this, chennaiTeamMembersList, Category.TEAM_MEMBERS)); teamMemberViewPager.setOnClickListener(null); teamMemberViewPager.setOnPageChangeListener(new PageChangeListener(null, teamMemberViewPager)); } if
 								 * (chennaiTeamAmbassadorsList != null && chennaiTeamAmbassadorsList.size() > 0) { System.out.println("kranthi chennai team amblist size"+chennaiTeamAmbassadorsList.size());
 								 * 
 								 * teamAmbassadorViewPager.setAdapter(new TeamImagePagerAdapter(TeamActivity.this, chennaiTeamAmbassadorsList, Category.TEAM_AMBASSADORS)); teamAmbassadorViewPager.setOnClickListener(null); teamAmbassadorViewPager.setOnPageChangeListener(new PageChangeListener(null,
 								 * teamAmbassadorViewPager)); }
 								 */
 							}
 							else {
 								teamName.setText(teamLogosList.get(pos + 4).getName().toUpperCase());
 								setTeamMembersToAdapter(bengalTeamMembersList);
 								setTeamAmbassadorsToAdapter(bengalTeamAmbassadorsList);
 							}
 							break;
 						case 2:
 							indicatorOneImage.setVisibility(View.INVISIBLE);
 							indicatorTwoImage.setVisibility(View.INVISIBLE);
 							indicatorThreeImage.setVisibility(View.VISIBLE);
 							indicatorFourImage.setVisibility(View.INVISIBLE);
 							if (position == 0) {
 								teamName.setText(teamLogosList.get(pos).getName().toUpperCase());
 								setTeamMembersToAdapter(teluguTeamMembersList);
 								setTeamAmbassadorsToAdapter(teluguTeamAmbassadorsList);
 							}
 							else {
 								teamName.setText(teamLogosList.get(pos + 4).getName().toUpperCase());
 								setTeamMembersToAdapter(veerTeamMembersList);
 								setTeamAmbassadorsToAdapter(veerTeamAmbassadorsList);
 							}
 							break;
 						case 3:
 							indicatorOneImage.setVisibility(View.INVISIBLE);
 							indicatorTwoImage.setVisibility(View.INVISIBLE);
 							indicatorThreeImage.setVisibility(View.INVISIBLE);
 							indicatorFourImage.setVisibility(View.VISIBLE);
 							if (position == 0) {
 								teamName.setText(teamLogosList.get(pos).getName().toUpperCase());
 								setTeamMembersToAdapter(karanatakaTeamMembersList);
 								setTeamAmbassadorsToAdapter(karanatakaTeamAmbassadorsList);
 							}
 							else {
 								teamName.setText(teamLogosList.get(pos + 4).getName().toUpperCase());
 								setTeamMembersToAdapter(bhojpuriTeamMembersList);
 								setTeamAmbassadorsToAdapter(bhojpuriTeamAmbassadorsList);
 							}
 							break;
 
 						default:
 							break;
 					}
 
 					int index = (3 * position) + (position + pos);
 					/*
 					 * teamLayout.setBackgroundResource(items.get(index).getTeamDetailsResource()); ImageView teamPlayers = (ImageView) teamLayout.findViewById(R.id.team_player); teamPlayers.setBackgroundResource(items.get(index).getTeamPlayersResource());
 					 */
 				}
 			});
 			((ViewPager) views).addView(imageLayout, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
 
 			return imageLayout;
 		}
 
 		@Override
 		public boolean isViewFromObject (View view, Object object) {
 			return view.equals(object);
 		}
 
 	}
 
 }
