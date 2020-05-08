 package com.badlogic.gdx.tests;
 
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.junit.Assert.assertThat;
 
 import org.hamcrest.core.IsEqual;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
 import com.badlogic.gdx.math.Rectangle;
 
 public class SpriteAtlasTest {
 
 	private Texture texture128x512;
 
 	@Before
 	public void setup() {
 		texture128x512 = org.easymock.EasyMock.createMock(Texture.class);
 		expect(texture128x512.getWidth()).andReturn(128).anyTimes();
 		expect(texture128x512.getHeight()).andReturn(512).anyTimes();
 		replay(texture128x512);
 	}
 	
 	@Test
 	public void shouldHaveSizeBasedOnOriginalSize() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 0, 0, 100, 120, 100, 120, 0, 0);
 
 		AtlasSprite atlasSprite = new AtlasSprite(region);
 
 		assertThat(atlasSprite.getWidth(), IsEqual.equalTo(100f));
 		assertThat(atlasSprite.getHeight(), IsEqual.equalTo(120f));
 	}
 
 	@Test
 	public void shouldHaveSizeBasedOnOriginalSizeEvenIfPackedSizeDiffers() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 0, 0, 120, 120, 200, 200, 0, 0);
 
 		AtlasSprite atlasSprite = new AtlasSprite(region);
 
 		assertThat(atlasSprite.getWidth(), IsEqual.equalTo(200f));
 		assertThat(atlasSprite.getHeight(), IsEqual.equalTo(200f));
 	}
 
 	@Test
 	public void shouldReturnSizeWhenWhiteSpaceNoRotation() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 181, 42, 200, 64, 10, 15, false);
 
 		AtlasSprite atlasSprite = new AtlasSprite(region);
 
 		assertThat(atlasSprite.getWidth(), IsEqual.equalTo(200f));
 		assertThat(atlasSprite.getHeight(), IsEqual.equalTo(64f));
 	}
 
 	@Test
 	public void shouldReturnSizeWhenWhiteSpaceAndRotation() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 42, 181, 200, 64, 10, 15, true);
 
 		AtlasSprite atlasSprite = new AtlasSprite(region);
 
 		assertThat(atlasSprite.getWidth(), IsEqual.equalTo(200f));
 		assertThat(atlasSprite.getHeight(), IsEqual.equalTo(64f));
 	}
 
 	@Test
 	public void shouldReturnSamePositionWhenFlippedHorizontally() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 181, 42, 200, 64, 10, 15, false);
 
 		AtlasSprite sprite = new AtlasSprite(region);
 		
 		sprite.setPosition(50f, 70f);
 		sprite.setOrigin(65f, 85f);
 		sprite.flip(true, false);
 		assertThat(sprite.getX(), IsEqual.equalTo(50f));
 		assertThat(sprite.getY(), IsEqual.equalTo(70f));
 		assertThat(sprite.getOriginX(), IsEqual.equalTo(65f));
 		assertThat(sprite.getOriginY(), IsEqual.equalTo(85f));
 	}
 
 	@Test
 	public void shouldReturnSamePositionWhenFlippedVertically() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 181, 42, 200, 64, 10, 15, false);
 
 		AtlasSprite sprite = new AtlasSprite(region);
 		
 		sprite.setPosition(50f, 70f);
 		sprite.setOrigin(65f, 85f);
 		sprite.flip(false, true);
 		assertThat(sprite.getX(), IsEqual.equalTo(50f));
 		assertThat(sprite.getY(), IsEqual.equalTo(70f));
 		assertThat(sprite.getOriginX(), IsEqual.equalTo(65f));
 		assertThat(sprite.getOriginY(), IsEqual.equalTo(85f));
 	}
 	
 	@Test
 	public void shouldReturnBoundingRectangleForAllTheSpriteIfNoWhitespaceRemoved() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 200, 64, 200, 64, 0, 0, false);
 		AtlasSprite sprite = new AtlasSprite(region);
 		sprite.setPosition(50f, 70f);
 		sprite.setOrigin(65f, 85f);
 		assertThat(sprite.getBoundingRectangle(), RectangleMatcher.isEqualRectangle(new Rectangle(50f, 70f, 200f, 64f)));
 	}
 	
 	@Test
 	public void shouldReturnBoundingRectangleForAllTheSpriteIfNoWhitespaceRemovedButRotated() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 64, 200, 200, 64, 0, 0, true);
 		AtlasSprite sprite = new AtlasSprite(region);
 		sprite.setPosition(50f, 70f);
 		sprite.setOrigin(65f, 85f);
 		assertThat(sprite.getBoundingRectangle(), RectangleMatcher.isEqualRectangle(new Rectangle(50f, 70f, 200f, 64f)));
 	}
 	
 	@Test
 	public void shouldReturnBoundingRectangleForAllTheSprite() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 181, 42, 200, 64, 10, 15, false);
 		AtlasSprite sprite = new AtlasSprite(region);
 		sprite.setPosition(50f, 70f);
 		sprite.setOrigin(65f, 85f);
 		assertThat(sprite.getBoundingRectangle(), RectangleMatcher.isEqualRectangle(new Rectangle(50f + 10, 70f + 15, 181f, 42f)));
 	}
 	
 	@Test
 	public void bugBoundingRectangleShouldNotChangeWhenFlippingHorizontallyWithOriginAtPosition() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 181, 42, 200, 64, 10, 15, false);
 		AtlasSprite sprite = new AtlasSprite(region);
 		sprite.setPosition(50f, 70f);
 		sprite.setOrigin(50f, 70f);
 		sprite.flip(true, false);
		assertThat(sprite.getBoundingRectangle(), RectangleMatcher.isEqualRectangle(new Rectangle(50f + 10, 70f + 15, 181f, 42f)));
 	}
 	
 	@Test
 	public void bugBoundingRectangleShouldNotChangeWhenFlippingVerticallyWithOriginAtPosition() {
 		AtlasRegion region = AtlasRegionFactory.atlasRegion(texture128x512, 2, 97, 181, 42, 200, 64, 10, 15, false);
 		AtlasSprite sprite = new AtlasSprite(region);
 		sprite.setPosition(200f, 200f);
 		sprite.setOrigin(200f, 200f);
 		sprite.flip(false, true);
		assertThat(sprite.getBoundingRectangle(), RectangleMatcher.isEqualRectangle(new Rectangle(200f + 10, 200f + 15, 181f, 42f)));
 	}
 	
 }
