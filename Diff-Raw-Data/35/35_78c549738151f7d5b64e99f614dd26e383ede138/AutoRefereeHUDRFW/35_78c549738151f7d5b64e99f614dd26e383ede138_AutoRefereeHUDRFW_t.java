 //START CODE
 package net.minecraft.src;
 
 import net.minecraft.client.Minecraft;
 
 import org.lwjgl.opengl.GL11;
 
 public class AutoRefereeHUDRFW extends AutoRefereeHUD {
 
 	public static void renderPlayerList(Minecraft mc) {
 		ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
 		GL11.glEnable(GL11.GL_ALPHA_TEST);
 		AutoReferee autoReferee = AutoReferee.get();
 		float scale = (float) (scaledResolution.getScaledHeight() - 45) / (float) PLAYER_LIST_BOX_HEIGHT;
 		scale = Math.min(scale, (float) (scaledResolution.getScaledWidth()) / (float) (PLAYER_LIST_BOX_WIDTH * 2 + 20));
 		float height = (scaledResolution.getScaledHeight() - 35) / 2 - PLAYER_LIST_BOX_HEIGHT / 2 * scale;
 		height = Math.max(10, height);
 		AutoRefereeTeam at1 = autoReferee.getLeftTeam(mc.ingameGUI.updateCounter);
 		AutoRefereeTeam at2 = autoReferee.getRightTeam(mc.ingameGUI.updateCounter);
 		// scale down if too large name
 		float widthName = 0, widthName2 = 0;
 		float scaleName = 1.7F;
 		if (at1 != null)
 			widthName = mc.fontRenderer.getStringWidth(at1.getName()) * scaleName;
 		if (at2 != null)
 			widthName2 = mc.fontRenderer.getStringWidth(at2.getName()) * scaleName;
 		if (widthName2 > widthName)
 			widthName = widthName2;
 		if (widthName > (PLAYER_LIST_BOX_WIDTH - PLAYER_LIST_BOX_PADDING * 2))
 			scaleName = (PLAYER_LIST_BOX_WIDTH - PLAYER_LIST_BOX_PADDING * 2) / widthName * scaleName;
 
 		if (at1 != null) {
 			GL11.glPushMatrix();
 			GL11.glTranslatef(scaledResolution.getScaledWidth() / 2 - PLAYER_LIST_BOX_WIDTH * scale, height, 0);
 			GL11.glScalef(scale, scale, scale);
 			renderTeamInPlayerList(at1, scaleName, mc);
 			GL11.glPopMatrix();
 		}
 		if (at2 != null) {
 			GL11.glPushMatrix();
 			GL11.glTranslatef(scaledResolution.getScaledWidth() / 2, height, 0);
 			GL11.glScalef(scale, scale, scale);
 			renderTeamInPlayerList(at2, scaleName, mc);
 			GL11.glPopMatrix();
 		}
 	}
 
 	private static void renderTeamInPlayerList(AutoRefereeTeam at, float scaleName, Minecraft mc) {
 		AutoReferee autoReferee = AutoReferee.get();
 		String name = at.getName();
 		mc.ingameGUI.drawRect(0, 0, PLAYER_LIST_BOX_WIDTH, PLAYER_LIST_BOX_HEIGHT, at.getBoxColor());
 		autoReferee.renderCenteredString(name, PLAYER_LIST_BOX_WIDTH / 2, PLAYER_LIST_TEAM_OFFSET, scaleName, 16777215, true);
 		int i = 0, j;
 		int health, armor, height;
 		for (AutoRefereePlayer apl : autoReferee.getPlayersOfTeam(at)) {
 			GL11.glPushMatrix();
 			int textcolor = 16777215;
 			if (!apl.getLoggedIn()) {
 				GL11.glEnable(GL11.GL_BLEND);
 				GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
 				textcolor = (256 << 24) + (128 << 16) + (128 << 8) + 128;
 			}
 			GL11.glTranslatef(0, PLAYER_LIST_PLAYERS_Y_OFFSET + i * PLAYER_LIST_PLAYER_HEIGHT, 0);
 			autoReferee.renderString(apl.getName(), PLAYER_LIST_NAME_OFFSET, 0, 1F, textcolor, true);
 			autoReferee.renderString(apl.getDeaths() + "", PLAYER_LIST_DEATHS_OFFSET, PLAYER_LIST_HEALTH_Y_OFFSET, 1F, textcolor, true);
 			autoReferee.renderString(apl.getKills() + "", PLAYER_LIST_KILLS_OFFSET, PLAYER_LIST_HEALTH_Y_OFFSET, 1F, textcolor, true);
 			autoReferee.renderString(apl.getAccuracyString(), PLAYER_LIST_ACC_OFFSET, PLAYER_LIST_ARMOR_Y_OFFSET, 1F, textcolor, true);
 			health = apl.getHealth();
 			armor = apl.getArmor();
 			autoReferee.renderHearts(health, PLAYER_LIST_HEALTH_X_OFFSET, PLAYER_LIST_HEALTH_Y_OFFSET, 1F, true);
 			autoReferee.renderArmor(armor, PLAYER_LIST_ARMOR_X_OFFSET, PLAYER_LIST_ARMOR_Y_OFFSET, 1F);
 			autoReferee.renderSkinHead(apl.getName(), PLAYER_LIST_HEAD_X_OFFSET, PLAYER_LIST_HEAD_Y_OFFSET, 1F);
 
 			j = 0;
 			for (AutoRefereeObjective obj : apl.getObjectives()) {
 				mc.ingameGUI.drawRect(PLAYER_LIST_OBJECTIVES_X_OFFSET + PLAYER_LIST_OBJECTIVE_OFFSET * j - PLAYER_LIST_OBJ_BORDER_WIDTH, PLAYER_LIST_OBJECTIVES_Y_OFFSET - PLAYER_LIST_OBJ_BORDER_WIDTH, PLAYER_LIST_OBJECTIVES_X_OFFSET + PLAYER_LIST_OBJECTIVE_OFFSET * j + PLAYER_LIST_OBJ_RECT_WIDTH + PLAYER_LIST_OBJ_BORDER_WIDTH, PLAYER_LIST_OBJECTIVES_Y_OFFSET + PLAYER_LIST_OBJ_RECT_WIDTH + PLAYER_LIST_OBJ_BORDER_WIDTH, 0x33FFFFFF);
 				autoReferee.renderItem(obj.getId(), obj.getData(), PLAYER_LIST_OBJECTIVES_X_OFFSET + PLAYER_LIST_OBJECTIVE_OFFSET * j, PLAYER_LIST_OBJECTIVES_Y_OFFSET, 0.9F);
 				++j;
 			}
 			int yStreak = PLAYER_LIST_DOMINATION_Y_OFFSET;
 			if (apl.getDominationAmount() > 0) {
 				autoReferee.renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_DOMINATION_ICON, apl.getDominationAmount(), PLAYER_LIST_DOMINATION_X_OFFSET, PLAYER_LIST_DOMINATION_Y_OFFSET, 0.9F, textcolor);
 				yStreak = PLAYER_LIST_KILL_STREAK_Y_OFFSET;
 			}
 			if (apl.getKillStreak() > 0)
 				autoReferee.renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_KILL_STREAK_ICON, apl.getKillStreak(), PLAYER_LIST_KILL_STREAK_X_OFFSET, yStreak, 1F, textcolor);
 			GL11.glDisable(GL11.GL_LIGHTING);
 			++i;
 			GL11.glPopMatrix();
 		}
 	}
 
 	public static void renderTeamList(Minecraft mc) {
 		ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
 		GL11.glEnable(GL11.GL_ALPHA_TEST);
 		AutoReferee autoReferee = AutoReferee.get();
 		float scale = (float) (scaledResolution.getScaledHeight() - 45) / (float) TEAM_LIST_BOX_HEIGHT;
 		scale = Math.min(scale, (float) (scaledResolution.getScaledWidth()) / (float) (TEAM_LIST_BOX_WIDTH * 2 + 20));
 		float height = (scaledResolution.getScaledHeight() - 35) / 2 - TEAM_LIST_BOX_HEIGHT / 2 * scale;
 		height = Math.max(10, height);
 		AutoRefereeTeam at1 = autoReferee.getLeftTeam(mc.ingameGUI.updateCounter);
 		AutoRefereeTeam at2 = autoReferee.getRightTeam(mc.ingameGUI.updateCounter);
 		// scale down if too large name
 		float widthName = 0, widthName2 = 0;
 		float scaleName = 1.7F;
 		if (at1 != null)
 			widthName = mc.fontRenderer.getStringWidth(at1.getName()) * scaleName;
 		if (at2 != null)
 			widthName2 = mc.fontRenderer.getStringWidth(at2.getName()) * scaleName;
 		if (widthName2 > widthName)
 			widthName = widthName2;
 		if (widthName > (TEAM_LIST_BOX_WIDTH - TEAM_LIST_BOX_PADDING * 2))
 			scaleName = (TEAM_LIST_BOX_WIDTH - TEAM_LIST_BOX_PADDING * 2) / widthName * scaleName;
 		if (at1 != null) {
 			GL11.glPushMatrix();
 			GL11.glTranslatef(scaledResolution.getScaledWidth() / 2 - TEAM_LIST_BOX_WIDTH * scale, height, 0);
 			GL11.glScalef(scale, scale, scale);
 			renderTeamInTeamList(at1, scaleName, mc);
 			GL11.glPopMatrix();
 		}
 		if (at2 != null) {
 			GL11.glPushMatrix();
 			GL11.glTranslatef(scaledResolution.getScaledWidth() / 2, height, 0);
 			GL11.glScalef(scale, scale, scale);
 			renderTeamInTeamList(at2, scaleName, mc);
 			GL11.glPopMatrix();
 		}
 	}
 
 	private static void renderTeamInTeamList(AutoRefereeTeam at, float nameScale, Minecraft mc) {
 		AutoReferee autoReferee = AutoReferee.get();
 		String name = at.getName();
 		int height = TEAM_LIST_OBJECTIVES_Y_OFFSET + at.getObjectives().size() * TEAM_LIST_OBJECTIVES_HEIGHT + 20;
 		mc.ingameGUI.drawRect(0, 0, TEAM_LIST_BOX_WIDTH, height, at.getBoxColor());
 		autoReferee.renderCenteredString(name, TEAM_LIST_BOX_WIDTH / 2, TEAM_LIST_TEAM_OFFSET, nameScale, 16777215, true);
 		int i = 0;
 		for (AutoRefereeObjective obj : at.getObjectives()) {
 			GL11.glPushMatrix();
 			GL11.glTranslatef(0, TEAM_LIST_OBJECTIVES_Y_OFFSET + i * TEAM_LIST_OBJECTIVES_HEIGHT, 0);
 			mc.ingameGUI.drawRect(TEAM_LIST_OBJ_X_OFFSET - TEAM_LIST_OBJ_BORDER_WIDTH, TEAM_LIST_OBJ_Y_OFFSET - TEAM_LIST_OBJ_BORDER_WIDTH, TEAM_LIST_OBJ_X_OFFSET + TEAM_LIST_OBJ_RECT_WIDTH + TEAM_LIST_OBJ_BORDER_WIDTH, TEAM_LIST_OBJ_Y_OFFSET + TEAM_LIST_OBJ_RECT_WIDTH + TEAM_LIST_OBJ_BORDER_WIDTH, 0x33FFFFFF);
 			autoReferee.renderItem(obj.getId(), obj.getData(), TEAM_LIST_OBJ_X_OFFSET, TEAM_LIST_OBJ_Y_OFFSET, 1F);
 			autoReferee.renderString(obj.getStatus().getName(), TEAM_LIST_OBJ_X_OFFSET + TEAM_LIST_TEXT_OFFSET, 0, 1F, 16777215, true);
 			GL11.glPopMatrix();
 			++i;
 		}
 	}
 
 	public static void renderGeneralHUD(Minecraft mc) {
 		float scale = 1.0F;
 		GL11.glEnable(GL11.GL_ALPHA_TEST);
 		AutoReferee autoReferee = AutoReferee.get();
 		ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
 		// update time
 		if (autoReferee.timeUpdated)
 			autoReferee.updateLastTick(mc.ingameGUI.updateCounter);
 		if (autoReferee.gameRunning)
 			autoReferee.updateTime(mc.ingameGUI.updateCounter);
 		// update countdown
 		if (autoReferee.countdownUpdated)
 			autoReferee.updateLastCountDownTick(mc.ingameGUI.updateCounter);
 		if (autoReferee.countingDown)
 			autoReferee.updateCountdown(mc.ingameGUI.updateCounter);
 		// calculating positions
 		float bigggestWidthTeam = 0;
 		float align = AUTOREFEREE_HUD_ALIGN * scale;
 		int height = AUTOREFEREE_HUD_HEIGHT;
 		AutoRefereeTeam at1 = autoReferee.getLeftTeam(mc.ingameGUI.updateCounter), at2 = autoReferee.getRightTeam(mc.ingameGUI.updateCounter), winners = autoReferee.getWinners();
 		float widthClock = 0;
 		float biggestWidthTeam = 0;
 		if (at1 != null) {
 			biggestWidthTeam = mc.fontRenderer.getStringWidth(at1.getName()) * scale;
 			if (at1 == winners)
 				biggestWidthTeam += AUTOREFEREE_HUD_WIDTH_CUP_ICON;
 		}
 		if (at2 != null) {
 			float widthTeam = mc.fontRenderer.getStringWidth(at2.getName()) * scale;
 			if (at2 == winners)
 				widthTeam += AUTOREFEREE_HUD_WIDTH_CUP_ICON;
 			if (biggestWidthTeam < widthTeam)
 				biggestWidthTeam = widthTeam;
 		}
 		if (!autoReferee.countingDown) {
 			widthClock += align;
 			widthClock += mc.fontRenderer.getStringWidth(autoReferee.getTime()) * scale;
 			widthClock += align;
 		} else if (autoReferee.countingDown) {
 			widthClock += align;
 			widthClock += mc.fontRenderer.getStringWidth(autoReferee.getCountdown()) * scale;
 			widthClock += align;
 		}
 		float xOffset = 0;
 		float width = 0;
 		// display first team
 		if (at1 != null) {
 			AutoRefereeTeam at = at1;
 			width = biggestWidthTeam;
 			if (at == winners) {
 				width += AUTOREFEREE_HUD_WIDTH_CUP_ICON;
 			}
 			xOffset = scaledResolution.getScaledWidth() / 2 - widthClock / 2 - width - align;
 			//RENDER SCORE
 			if(at.getScore() != 0){
 				mc.ingameGUI.drawRect((int) (xOffset - align- PLAYER_LIST_OBJECTIVE_OFFSET*scale/2*at.getScore()-5), 0, (int) (xOffset - align), height, Long.valueOf("EF3F3F3F", 16).intValue());
 				int index = 0;
 				for(AutoRefereeObjective obj : at.getScoredObjectives()){
					if(obj.getId() != 0){
						mc.ingameGUI.drawRect((int)(xOffset - align - 10 - PLAYER_LIST_OBJECTIVE_OFFSET * scale/2 * index), 1, (int)(xOffset - align - 10 - PLAYER_LIST_OBJECTIVE_OFFSET * scale/2 * index + PLAYER_LIST_OBJ_BORDER_WIDTH + PLAYER_LIST_OBJ_RECT_WIDTH * scale/2), 9, 0x33FFFFFF);
						autoReferee.renderItem(obj.getId(), obj.getData(), (int)(xOffset - align - 10 - PLAYER_LIST_OBJECTIVE_OFFSET * scale/2 * index), 1, scale/2);
					    index++;
					}
 				}
 			}
 			//RENDER TEAM NAME
 			mc.ingameGUI.drawRect((int) (xOffset - align), 0, (int) (xOffset + width + AUTOREFEREE_HUD_WIDTH_CUP_ICON + align), height, at.getBoxColor());
 			autoReferee.renderCenteredString(at.getName(), xOffset + width / 2, 1, scale, 16777215, false);
 			if (at == winners) {
 				autoReferee.renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_WINNERS_ICON, 0, xOffset + width - AUTOREFEREE_HUD_WIDTH_CUP_ICON, 0, AUTOREFEREE_HUD_SCALE_CUP_ICON);
 			}
 			xOffset += width + align;
 		}
 		// display time
 		if (at1 != null && at2 != null){
 			xOffset += align;
 			String text = autoReferee.getTime();
 			if(autoReferee.countingDown)
 				text = autoReferee.getCountdown();
 			width = mc.fontRenderer.getStringWidth(text) * scale;
 			mc.ingameGUI.drawRect((int) (xOffset - align), 0, (int) (xOffset + width + align), height, Long.valueOf("EF3F3F3F", 16).intValue());
 			autoReferee.renderString(text, xOffset, 1, scale, 16777215, true);
 			xOffset += width + align;
 		}
 		// display 2nd team
 		if (at2 != null) {
 			AutoRefereeTeam at = at2;
 			xOffset += align;
 			width = biggestWidthTeam;
 			if (at == winners) {
 				width += AUTOREFEREE_HUD_WIDTH_CUP_ICON;
 			}
 			//RENDER TEAM NAME
 			mc.ingameGUI.drawRect((int) (xOffset - align), 0, (int) (xOffset + width + align), height, at.getBoxColor());
 			autoReferee.renderCenteredString(at.getName(), xOffset + width / 2, 1, scale, 16777215, false);
 			if (at == winners) {
 				autoReferee.renderAutoRefereeIcon(AutoReferee.AUTOREFEREE_WINNERS_ICON, 0, xOffset + width - AUTOREFEREE_HUD_WIDTH_CUP_ICON, 0, AUTOREFEREE_HUD_SCALE_CUP_ICON);
 			}
 			//RENDER SCORE
 			if(at.getScore() != 0){
 				mc.ingameGUI.drawRect((int) (xOffset + width + align + 5 + PLAYER_LIST_OBJECTIVE_OFFSET*scale/2*at.getScore()), 0, (int) (xOffset + width + align), height, Long.valueOf("EF3F3F3F", 16).intValue());
 				int index = 0;
 				for(AutoRefereeObjective obj : at.getScoredObjectives()){
					if(obj.getId() != 0){
						mc.ingameGUI.drawRect((int)(xOffset + width + align + 3 + PLAYER_LIST_OBJECTIVE_OFFSET * scale/2 * index), 1, (int)(xOffset + width + align + 3 + PLAYER_LIST_OBJECTIVE_OFFSET * scale/2 * index + PLAYER_LIST_OBJ_BORDER_WIDTH + PLAYER_LIST_OBJ_RECT_WIDTH * scale/2), 9, 0x33FFFFFF);
						autoReferee.renderItem(obj.getId(), obj.getData(), (int)(xOffset+width + align + 3 + PLAYER_LIST_OBJECTIVE_OFFSET * scale/2 * index), 1, scale/2);
					    index++;
					}
 				}
 			}
 		}
 	}
 }
 
 // END CODE
