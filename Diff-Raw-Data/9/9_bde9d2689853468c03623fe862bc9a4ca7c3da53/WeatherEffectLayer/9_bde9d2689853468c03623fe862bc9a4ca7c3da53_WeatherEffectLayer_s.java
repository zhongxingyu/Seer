 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madthrax.ridiculousRPG.animation;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.madthrax.ridiculousRPG.TextureRegionLoader;
 import com.madthrax.ridiculousRPG.TextureRegionLoader.TextureRegionRef;
 
 /**
  * A Layer for a weather effect. Every effect layer is simulated by a texture.
  * The texture is tiled to fill the map (or whatever display-region is used).
  * 
  * @see WeatherEffectService
  * @author Alexander Baumgartner
  */
 public class WeatherEffectLayer extends EffectLayer {
 	private static final Random randomNumberGenerator = new Random();
 
 	private TextureRegionRef tRef;
 	private int width, height;
 	private int tileWidth, tileHeight;
 	private ArrayList<ArrayList<Rectangle>> tileLayer = new ArrayList<ArrayList<Rectangle>>();
 	private boolean play = true;
 	private boolean flip = false;
 	private boolean fill = false;
 	private boolean initializeEffect = true;
 
 	// Initialization of new tiles
 	private float newRowWindSpeed;
 	private int newRowTilesPerRow;
 	private int newRowTilesOffset;
 	private float fadeAlpha;
 	private float fadeBy;
 	private float tilePositionVarianz = .2f;
 	private float effectSpeedMin, effectSpeedMax;
 	private float windSpeedMin, windSpeedMax;
 
 	// Start values for the effect
 	private float effectSpeed;
 	private float effectAcceleratrion = 0f;
 	private float randomWindCountAll = 999999f;
 	private float randomWindCountTile = 999999f;
 
 	// Internally used, only computed once to enhance performance
 	private float internalRemoveRow;
 	private float internalNewRow;
 	private float internalWindAcceleration;
 	private float internalEffectAcceleration;
 	private ArrayList<Rectangle> recycleRow;
 
 	/**
 	 * Creates a new effect-layer. Layers are used for weather effects.
 	 * 
 	 * @param path
 	 *            The texture path (e.g. data/snow.png)
 	 * @param pixelOverlap
 	 *            For a smoother effect you can overlap the tiled effect-layer.<br>
 	 *            Therefore the texture has to be less dense at the edges. (The
 	 *            bound is defined by pixelOverlap)<br>
 	 *            Say 0 if you don't know what to do. (Negative values are
 	 *            allowed but not useful in most cases)
 	 * @param pixelWidth
 	 *            Width of the entire weather effect in pixel
 	 * @param pixelHeight
 	 *            Height of the entire weather effect in pixel
 	 * @param effectSpeed
 	 *            The speed of the effect. A negative value flips the effect.<br>
 	 *            For most situations the value should be between 0.1 and 5
 	 * @param windSpeed
 	 *            The speed of the wind. Positive values generate wind from west
 	 *            to east and negative values from east to west.<br>
 	 *            For most situations the value should be between -2.5 and 2.5
 	 * @param fadeInTime
 	 *            The time used to fade in the effect. If <=0, fade in will be
 	 *            disabled.
 	 * @param fillLayer
 	 *            If the layer should be prefilled on startup.
 	 */
 	public WeatherEffectLayer(String path, int pixelOverlap, int pixelWidth,
 			int pixelHeight, float effectSpeed, float windSpeed,
 			float fadeInTime, boolean fillLayer) {
 		this(TextureRegionLoader.load(path), pixelOverlap, pixelWidth,
 				pixelHeight, effectSpeed, windSpeed, fadeInTime, fillLayer);
 	}
 
 	/**
 	 * Attention: TextureRef tRef is unloaded automatically by calling the
 	 * dispose method. You have to load it every time you use this constructor.
 	 * (The texture dictionary counts the references by loading and unloading a
 	 * texture)<br>
 	 * Conclusion: Use the other constructor if it's possible!!!
 	 * 
 	 * @see TextureDict.loadTexture(...)
 	 * @see public WeatherEffectLayer(String path, ...)
 	 */
 	protected WeatherEffectLayer(TextureRegionRef tRef, int pixelOverlap,
 			int pixelWidth, int pixelHeight, float effectSpeed,
 			float windSpeed, float fadeInTime, boolean fillLayer) {
 		if (effectSpeed < .01) {
 			if (effectSpeed < -.01) {
 				effectSpeed = -effectSpeed;
 				flip = true;
 			} else {
 				effectSpeed = 0;
 				windSpeed = 0;
 				fill = true;
 			}
 		}
 		this.tRef = tRef;
 		this.fill = fillLayer;
 		if (fadeInTime > 0) {
 			fadeBy = 1f / fadeInTime;
 		} else {
 			fadeAlpha = 1f;
 		}
 		tileWidth = tRef.getRegionWidth() - pixelOverlap;
 		tileHeight = tRef.getRegionHeight() - pixelOverlap;
 		width = pixelWidth;
 		height = pixelHeight;
 
 		// row length is relative to map height and the speed/wind ratio (caused
 		// by the wind-effect)
 		float offset = Math.abs(height * windSpeed / effectSpeed) * 2f;
 		newRowTilesPerRow = (int) ((width + offset) / tileWidth + 1.7);
 		newRowTilesOffset = (int) (-tileWidth * .7f);
 		if (windSpeed > 0) {
 			newRowTilesOffset -= offset;
 		}
 		this.effectSpeed = effectSpeed;
 		this.newRowWindSpeed = windSpeed;
 		internalEffectAcceleration = effectSpeed * .3f;
 		internalWindAcceleration = newRowWindSpeed * .2f;
 		internalRemoveRow = -height * .4f - tileHeight;
 		internalNewRow = height * 1.3f - tileHeight;
 
 		// compute bounds for speed-varianz
 		float tmp;
 		if (effectSpeed == 0) {
 			tmp = 0;
 			effectSpeedMin = -.01f;
 			effectSpeedMax = .01f;
 		} else {
 			tmp = effectSpeed / (float) Math.sqrt(effectSpeed);
 			effectSpeedMin = Math.max(effectSpeed * .5f, effectSpeed - tmp
 					* .7f);
 			effectSpeedMax = effectSpeed + tmp;
 		}
 		windSpeedMin = windSpeed - (effectSpeed - effectSpeedMin) * .5f - tmp;
 		windSpeedMax = windSpeed + (effectSpeed - effectSpeedMin) + tmp * .5f;
 
 		// generate first row of tiles or fill entire layer with tiles
 		fillLayer();
 	}
 
 	@Override
 	public void stop() {
 		setStopRequested(true);
 		play = false;
 	}
 
 	/**
 	 * Resize this effect to a new width and height.
 	 */
 	public void resize(int pixelWidth, int pixelHeight) {
 		width = pixelWidth;
 		height = pixelHeight;
 		// row length is relative to map height and the speed/wind ratio (caused
 		// by the wind-effect)
 		float offset = Math.abs(height * newRowWindSpeed / effectSpeed) * 2f;
 		newRowTilesPerRow = (int) ((width + offset) / tileWidth + 1.7);
 		newRowTilesOffset = (int) (-tileWidth * .7f);
 		if (newRowWindSpeed > 0) {
 			newRowTilesOffset -= offset;
 		}
 		internalRemoveRow = -height * .4f - tileHeight;
 		internalNewRow = height * 1.3f - tileHeight;
 
 		// refill the layer
 		tileLayer.clear();
 		// (that's a best effort solution - not perfect)
 		fillLayer();
 	}
 
 	private void fillLayer() {
 		if (initializeEffect && !fill) {
 			// generate first row of tiles
 			tileLayer.add(newRow(height));
 		} else {
 			// generate all rows of tiles
 			recycleRow = null;
 			for (int i = -tileHeight / 2; i < height; i += tileHeight) {
 				tileLayer.add(newRow(i));
 			}
 		}
 		initializeEffect = true;
 	}
 
 	/**
 	 * Checks if this effect-layer is empty
 	 * 
 	 * @return true if this effect-layer is empty
 	 */
 	@Override
 	public boolean isFinished() {
 		return tileLayer.isEmpty();
 	}
 
 	/**
 	 * Generates a virtual animation row over the entire map (only the subset
 	 * which is in the viewPort will be displayed)
 	 */
 	private ArrayList<Rectangle> newRow(int startY) {
 		int startX = newRowTilesOffset;
 		ArrayList<Rectangle> row = recycleRow;
 		if (row == null) {
 			row = new ArrayList<Rectangle>(newRowTilesPerRow);
 			for (int i = 0; i < newRowTilesPerRow; i++, startX += tileWidth) {
 				float x = randomNumberGenerator.nextFloat() * tileWidth
 						* tilePositionVarianz;
 				float y = randomNumberGenerator.nextFloat() * tileHeight
 						* tilePositionVarianz;
 				row.add(new Rectangle(startX + x, startY + y, newRowWindSpeed,
 						0f));
 			}
 		} else {
 			recycleRow = null; // consume recycled row
 			for (int i = 0; i < newRowTilesPerRow; i++, startX += tileWidth) {
 				// We have already random values. It should be much faster to
 				// reuse it ;)
 				Rectangle tileRect = row.get(i);
 				float x = tileRect.x < 0f ? -tileRect.x : tileRect.x;
 				float y = tileRect.y < 0f ? -tileRect.y : tileRect.y;
 				x %= tileWidth * tilePositionVarianz;
 				y %= tileHeight * tilePositionVarianz;
 				tileRect.x = startX + x;
 				tileRect.y = startY + y;
 				tileRect.width = newRowWindSpeed;
 				tileRect.height = 0f;
 			}
 		}
 		return row;
 	}
 
 	/**
 	 * For tile position randomization. Use small values to get an acceptable
 	 * result (default = 0.2) You can change this if you want.
 	 * 
 	 * @return the variance for positioning new tiles
 	 */
 	public float getTilePositionVarianz() {
 		return tilePositionVarianz;
 	}
 
 	/**
 	 * For tile position randomization. Use small values to get an acceptable
 	 * result (default = 0.2) You can change this if you want.
 	 * 
 	 * @param tilePositionVarianz
 	 *            the variance for positioning new tiles
 	 */
 	public void setTilePositionVarianz(float tilePositionVarianz) {
 		this.tilePositionVarianz = tilePositionVarianz;
 	}
 
 	/**
 	 * Bounds for speed variance are automatically computed on instantiation.
 	 * You can change this if you want.
 	 * 
 	 * @return the minimum speed for this weather effect
 	 */
 	public float getEffectSpeedMin() {
 		return effectSpeedMin;
 	}
 
 	/**
 	 * Bounds for speed variance are automatically computed on instantiation.
 	 * You can change this if you want.
 	 * 
 	 * @param effectSpeedMin
 	 *            the minimum speed for this weather effect
 	 */
 	public void setEffectSpeedMin(float effectSpeedMin) {
 		this.effectSpeedMin = effectSpeedMin;
 	}
 
 	/**
 	 * Bounds for speed variance are automatically computed on instantiation.
 	 * You can change this if you want.
 	 * 
 	 * @return the maximum speed for this weather effect
 	 */
 	public float getEffectSpeedMax() {
 		return effectSpeedMax;
 	}
 
 	/**
 	 * Bounds for speed variance are automatically computed on instantiation.
 	 * You can change this if you want.
 	 * 
 	 * @param effectSpeedMax
 	 *            the maximum speed for this weather effect
 	 */
 	public void setEffectSpeedMax(float effectSpeedMax) {
 		this.effectSpeedMax = effectSpeedMax;
 	}
 
 	/**
 	 * Bounds for wind variance are automatically computed on instantiation. You
 	 * can change this if you want.
 	 * 
 	 * @return the minimum wind speed for this weather effect
 	 */
 	public float getWindSpeedMin() {
 		return windSpeedMin;
 	}
 
 	/**
 	 * Bounds for wind variance are automatically computed on instantiation. You
 	 * can change this if you want.
 	 * 
 	 * @param windSpeedMin
 	 *            the minimum wind speed for this weather effect
 	 */
 	public void setWindSpeedMin(float windSpeedMin) {
 		this.windSpeedMin = windSpeedMin;
 	}
 
 	/**
 	 * Bounds for wind variance are automatically computed on instantiation. You
 	 * can change this if you want.
 	 * 
 	 * @return the maximum wind speed for this weather effect
 	 */
 	public float getWindSpeedMax() {
 		return windSpeedMax;
 	}
 
 	/**
 	 * Bounds for wind variance are automatically computed on instantiation. You
 	 * can change this if you want.
 	 * 
 	 * @param windSpeedMax
 	 *            the maximum wind speed for this weather effect
 	 */
 	public void setWindSpeedMax(float windSpeedMax) {
 		this.windSpeedMax = windSpeedMax;
 	}
 
 	/**
 	 * Computes the animation for this layer.
 	 */
 	@Override
 	public void compute(float deltaTime, boolean actionKeyDown) {
 		// Skip first call after initialization and resize
 		// to avoid huge deltatimes with great impact
 		if (initializeEffect) {
 			initializeEffect = false;
 			return;
 		}
 
		float deltaSpeed = deltaTime * 100;
 		randomWindCountTile += deltaTime;
 		randomWindCountAll += deltaTime;
 
 		// variable effect acceleration
 		if (randomWindCountAll > 2.7f) {
 			effectAcceleratrion = (randomNumberGenerator.nextFloat() - .5f)
 					* internalEffectAcceleration;
 			randomWindCountAll = 0f;
 		}
 
 		effectSpeed += effectAcceleratrion * deltaTime;
 		if (effectSpeed < effectSpeedMin)
 			effectSpeed = effectSpeedMin;
 		else if (effectSpeed > effectSpeedMax)
 			effectSpeed = effectSpeedMax;
 
 		for (int i = 0; i < tileLayer.size();) {
 			ArrayList<Rectangle> row = tileLayer.get(i);
 			float yPos = 0f;
 			for (Rectangle clip : row) {
 				// variable wind acceleration (part 1)
 				if (randomWindCountTile > 1.4f) {
 					clip.height = (randomNumberGenerator.nextFloat() - .5f)
 							* internalWindAcceleration;
 				}
 				clip.width += clip.height * deltaTime;
 				if (clip.width < windSpeedMin)
 					clip.width = windSpeedMin;
 				else if (clip.width > windSpeedMax)
 					clip.width = windSpeedMax;
				clip.y -= (effectSpeed + (newRowWindSpeed < 0 ? -clip.width
						: clip.width) * .4)
 						* deltaSpeed;
 				clip.x += clip.width * deltaSpeed;
 				yPos += clip.y;
 			}
 			yPos /= row.size();
 			// remove rows out of view
 			if (i == 0 && yPos < internalRemoveRow) {
 				recycleRow = tileLayer.remove(0);
 			} else {
 				i++;
 				if (i == tileLayer.size() && play && yPos < internalNewRow) {
 					tileLayer.add(newRow((int) yPos + tileHeight));
 				}
 			}
 		}
 		// variable wind acceleration (part 2)
 		if (randomWindCountTile > 1.4f) {
 			randomWindCountTile = 0f;
 		}
 	}
 
 	/**
 	 * Draw the effect-layer.
 	 */
 	public void draw(SpriteBatch batch, Camera cam, boolean debug) {
 		if (flip) {
 			batch.setTransformMatrix(batch.getTransformMatrix().translate(0,
 					cam.viewportHeight + 2 * cam.position.y, 0).rotate(1, 0, 0,
 					180));
 		}
 		if (fadeAlpha < 1f) {
 			Color c = batch.getColor();
 			c.a *= fadeAlpha;
 			batch.setColor(c);
 			fadeAlpha += fadeBy * Gdx.graphics.getDeltaTime();
 			if (fadeAlpha > 1f)
 				fadeAlpha = 1f;
 		}
 		// load frequently used variables into registers
 		Texture t = tRef.getTexture();
 		int tWidth = tRef.getRegionWidth();
 		int tHeight = tRef.getRegionHeight();
 		float x1 = Math.max(0f, cam.position.x);
 		float y1 = Math.max(0f, cam.position.y);
 		float x2 = Math.min(width, x1 + cam.viewportWidth);
 		float y2 = Math.min(height, y1 + cam.viewportHeight);
 		float x3 = x1 - tWidth;
 		float y3 = y1 - tHeight;
 		float x4 = x2 - tWidth;
 		float y4 = y2 - tHeight;
 
 		for (ArrayList<Rectangle> row : tileLayer)
 			for (Rectangle clip : row) {
 				float x = clip.x;
 				float y = clip.y;
 				if (x > x3 && y > y3 && x < x2 && y < y2) {
 					// texel space origin is upper left corner (unlike screen
 					// space)
 					int srcX = 0;
 					int srcY = 0;
 					int srcWidth = tWidth;
 					int srcHeight = tHeight;
 					if (x < x1) {
 						srcX = (int) (x1 - x + .7f);
 						srcWidth -= srcX;
 						x += srcX;
 					}
 					if (x > x4) {
 						srcWidth -= x - x4;
 					}
 					if (y < y1) {
 						srcHeight -= y1 - y;
 						y = y1;
 					}
 					if (y > y4) {
 						srcY = (int) (y - y4);
 						srcHeight -= srcY;
 					}
 					batch.draw(t, x, y, srcX, srcY, srcWidth, srcHeight);
 				}
 			}
 		if (flip) {
 			batch.setTransformMatrix(batch.getTransformMatrix().idt());
 		}
 	}
 
 	public void dispose() {
 		tileLayer = null;
 		tRef.dispose();
 	}
 }
