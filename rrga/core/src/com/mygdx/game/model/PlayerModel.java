/*
 * PlayerModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.graphics.g2d.Animation;

import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.*;
import com.mygdx.game.utility.obstacle.CapsuleObstacle;

import java.text.DecimalFormat;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class PlayerModel extends CapsuleObstacle {
	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** The factor to multiply by the input */
	private final float force;
	/** The amount to slow the character down */
	private final float damping;
	/** The maximum character horizontal speed in the air */
	private final float maxspeed_x_air_wind;
	private final float maxspeed_x_air_drag;
	/** The maximum character horizontal speed on the ground */
	private final float maxspeed_x_ground;
	/** The maximum character vertical speed */
	private final float maxspeed_up;
	private final float maxspeed_down_open;
	private final float maxspeed_down_closed;
	/** Identifier to allow us to track the sensor in ContactListener */
	private final String sensorName;

	/** The current horizontal movement of the character */
	private float   movement;
	/** Which direction is the character facing */
	private boolean faceRight;
	/** How long until we can jump again */
	private int jumpCooldown;
	/** Whether we are actively jumping */
	private boolean isJumping;
	/** Whether our feet are on the ground */
	private boolean isGrounded;
	/** The physics shape of this object */
	private PolygonShape sensorShape;
	/** The size of the player in physics units (up to scaling by shrink factor) */
	private float size;
	/** Player Mass */
	private float FINAL_MASS = 1.8f;
	/** Max player hp */
	private int MAX_HEALTH;
	/** Player hp */
	private int health;
	public BitmapFont healthFont;

	private static final DecimalFormat formatter = new DecimalFormat("0.00");
	
	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/**
	 * The number i-frames the player currently has
	 */
	private int iFrames;

	/** health point texture */
	private TextureRegion[] hpTexture;

	/** health point temporary texture */
	private TextureRegion[][] hpTempTexture;

	/** health point texture region film strip */
	private Texture hpFilmStrip;

	/** The player's front view texture (this is the main texture for air) */
	private TextureRegion frontTexture;

	/** The player's side view texture (this is the main texture for platform) */
	private TextureRegion sideTexture;

	/** The number of updates before the texture is switched when I-Frames are active */
	private int iFrameCountdown = 7;

	/** When the player is hit, whether or not the all white texture is drawn.
	 * Swaps between all white and regular */
	private boolean drawIFrameTexture = true;

	/** The magnitude of the lighter force */
	private float lighterForce;
	private final float maxLighterFuel;
	private float lighterFuel;
	private float lighterChangeRate;

	// <=============================== Animation objects start here ===============================>
	/** Player walk animation filmstrip texture */
	private Texture walkTexture;

	/** Player walk animation frames */
	private TextureRegion[][] walkTmpFrames;

	/** Player walk animation frames */
	private TextureRegion[] walkAnimationFrames;

	/** Player walk animation*/
	private Animation<TextureRegion> walkAnimation;

	/** Player walk animation elapsed time */
	float walkElapsedTime;

	/** Player fall animation filmstrip texture */
	private Texture fallTexture;

	/** Player fall animation frames */
	private TextureRegion[][] fallTmpFrames;

	/** Player fall animation frames */
	private TextureRegion[] fallAnimationFrames;

	/** Player fall animation*/
	private Animation<TextureRegion> fallAnimation;

	/** Player fall animation elapsed time */
	private float fallElapsedTime;

	/**
	 * Returns left/right movement of this character.
	 * 
	 * This is the result of input times player force.
	 *
	 * @return left/right movement of this character.
	 */
	public float getMovement() {
		return movement;
	}
	
	/**
	 * Sets left/right movement of this character.
	 * 
	 * This is the result of input times player force.
	 *
	 * @param value left/right movement of this character.
	 */
	public void setMovement(float value) {
		movement = value; 
		// Change facing if appropriate
		if (movement < 0) {
			faceRight = false;
		} else if (movement > 0) {
			faceRight = true;
		}
	}


	/**
	 * Returns true if the player is actively jumping.
	 *
	 * @return true if the player is actively jumping.
	 */
	public boolean isJumping() {
		return isJumping && isGrounded && jumpCooldown <= 0;
	}

	/**
	 * Sets whether the player is actively jumping.
	 *
	 * @param value whether the player is actively jumping.
	 */
	public void setJumping(boolean value) {
		isJumping = value;
	}

	/**
	 * Returns true if the player is on the ground.
	 *
	 * @return true if the player is on the ground.
	 */
	public boolean isGrounded() {
		return isGrounded;
	}
	
	/**
	 * Sets whether the player is on the ground.
	 *
	 * @param value whether the player is on the ground.
	 */
	public void setGrounded(boolean value) {
		isGrounded = value; 
	}

	/**
	 *
	 * */
	public boolean isMoving() {
		return this.movement != 0f;
	}
	/**
	 * Returns how much force to apply to get the player moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the player moving
	 */
	public float getForce() {
		return force;
	}

	/**
	 * Returns ow hard the brakes are applied to get a player to stop moving
	 *
	 * @return ow hard the brakes are applied to get a player to stop moving
	 */
	public float getDamping() {
		return damping;
	}
	
	/**
	 * Returns the upper limit on player left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on player left-right movement.
	 */
	public float getMaxSpeedXAirWind() {
		return maxspeed_x_air_wind;
	}

	public float getMaxSpeedXAirDrag() {
		return maxspeed_x_air_drag;
	}

	public float getMaxSpeedXGround() {
		return maxspeed_x_ground;
	}

	/**
	 * Returns the upper limit on player upwards movement.
	 *
	 * This does NOT apply to horizontal movement.
	 *
	 * @return the upper limit on player upwards movement.
	 */
	public float getMaxSpeedUp() {
		return maxspeed_up;
	}
	public float getMaxSpeedDownOpen() {
		return maxspeed_down_open;
	}
	public float getMaxSpeedDownClosed() {
		return maxspeed_down_closed;
	}

	/**
	 * Returns the name of the ground sensor
	 *
	 * This is used by ContactListener
	 *
	 * @return the name of the ground sensor
	 */
	public String getSensorName() { 
		return sensorName;
	}

	/**
	 * Returns true if this character is facing right
	 *
	 * @return true if this character is facing right
	 */
	public boolean isFacingRight() {
		return faceRight;
	}

	/**
	 * Returns the player's max hp. Should only be used when initializing the player
	 *
	 * @return the max hp
	 */
	public int getMaxHealth(){return MAX_HEALTH;}

	/**
	 * Sets the player's max hp. Does not allow max health to be less than 1;
	 * we want the player to have hp!
	 *
	 * @param hp the new max hp value
	 */
	public void setMaxHealth(int hp){
		if (hp >= 1) MAX_HEALTH = hp;
		else MAX_HEALTH = 1;
		if (getHealth() > MAX_HEALTH) setHealth(MAX_HEALTH);
	}

	/**
	 * Returns the player's current hp
	 *
	 * @return the current hp
	 */
	public int getHealth(){return health;}

	/**
	 * Sets the player's current hp. If this value is above the maximum,
	 * sets it to the maximum. If this value is below 0, sets it to 0.
	 *
	 * @param hp the new hp value
	 */
	public void setHealth(int hp){
		if (hp < 0) health = 0;
		else if (hp > MAX_HEALTH) health = MAX_HEALTH;
		else health = hp;
	}

	/**
	 * Returns the player's i-frames
	 *
	 * @return the current i-frames
	 */
	public int getiFrames(){return iFrames;}

	/**
	 * Sets the player's i-frames
	 *
	 * @param f the new i-frames value
	 */
	public void setiFrames(int f){
		iFrames = f;
	}


	/**
	 * sets the player's HP texture.
	 * @param texture the HP texture
	 */
	public void setHpTexture(Texture texture){
		this.hpFilmStrip = texture;
		hpTempTexture = TextureRegion.split(hpFilmStrip, 250, 250);
		hpTexture = new TextureRegion[4];

		// Ordering Texture Tile
		int count = 0;
		for (int i = 1; i > -1; i--) {
			for (int j = 1; j > -1; j--){
				hpTexture[count] = hpTempTexture[i][j];
				count ++;
			}
		}

	}

	/**
	 * sets the player's in-air texture.
	 * @param texture front view texture
	 */
	public void setFrontTexture(TextureRegion texture){
		this.frontTexture = texture;
	}

	/**
	 * sets the player's platform/ground texture.
	 * @param texture side view texture
	 */
	public void setSideTexture(TextureRegion texture){this.sideTexture = texture;}

	/**
	 * Sets player walk animation
	 * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
	 * */
	public void setWalkAnimation(Texture texture) {
		this.walkTexture = texture;
		//TODO maybe find a way to do this without constants?
		this.walkTmpFrames = TextureRegion.split(walkTexture, 252, 352);
		this.walkAnimationFrames = new TextureRegion[8];

		// PLacing animation frames in order
		int index = 0;
		for (int i=0; i<walkTmpFrames.length; i++) {
			for (int j=0; j<walkTmpFrames[0].length; j++) {
				this.walkAnimationFrames[index] = walkTmpFrames[i][j];
				index++;
			}
		}

		// Adjust walk speed here
		this.walkAnimation = new Animation<>(1f/12f, walkAnimationFrames);
	}

	/**
	 * Sets player falling animation
	 * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
	 * */
	public void setFallingAnimation(Texture texture) {
		this.fallTexture = texture;
		//TODO maybe find a way to do this without constants?
		this.fallTmpFrames = TextureRegion.split(fallTexture, 252, 352);
		this.fallAnimationFrames = new TextureRegion[4];

		// PLacing animation frames in order
		int index = 0;
		for (int i=0; i<fallTmpFrames.length; i++) {
			for (int j=0; j<fallTmpFrames[0].length; j++) {
				this.fallAnimationFrames[index] = fallTmpFrames[i][j];
				index++;
			}
		}

		// Adjust walk speed here
		this.fallAnimation = new Animation<>(1f/12f, fallAnimationFrames);
	}

	/**
	 * sets the texture to be frontal view for drawing purposes.
	 *
	 * No update occurs if the current texture is already the front view texture.
	 */
	public void useFrontTexture(){
		if (texture != frontTexture){
			setTexture(frontTexture);
		}
	}

	/**
	 * sets the texture to be side view for drawing purposes.
	 *
	 * No update occurs if the current texture is already the side view texture
	 */
	public void useSideTexture(){
		if (texture != sideTexture){
			setTexture(sideTexture);
		}
	}

	/**
	 * Creates a new player avatar with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param data  	The physics constants for this player
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public PlayerModel(JsonValue data, Vector2 pos, float width, float height, int maxHp) {
		// The shrink factors fit the image to a tigher hitbox
		super(	pos.x,
				pos.y,
				width*data.get("shrink").getFloat( 0 ),
				height*data.get("shrink").getFloat( 1 ));

		float density = FINAL_MASS / (width * height);
		setDensity(density);
		setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		maxspeed_x_ground = data.getFloat("maxspeed_x_ground", 0);
		maxspeed_x_air_wind = data.getFloat("maxspeed_x_air_wind", 0);
		maxspeed_x_air_drag = data.getFloat("maxspeed_x_air_drag", 0);
		maxspeed_up = data.getFloat("maxspeed_up", 0);
		maxspeed_down_open = data.getFloat("maxspeed_down_open", 0);
		maxspeed_down_closed = data.getFloat("maxspeed_down_closed", 0);
		damping = data.getFloat("damping", 0);
		force = data.getFloat("force", 0);
		size = data.getFloat("size", 1f);
		sensorName = "PlayerGroundSensor";
		lighterForce = data.getFloat("lighter_force");
		maxLighterFuel = data.getFloat("lighter_fuel");
		lighterFuel = maxLighterFuel;
		lighterChangeRate = data.getFloat("lighter_change_rate");
		this.data = data;

		// Gameplay attributes
		isGrounded = false;
		isJumping = false;
		faceRight = true;
		setMaxHealth(maxHp);
		setHealth(getMaxHealth());
		jumpCooldown = 0;
		setName("player");
		iFrames = 0;

		walkElapsedTime = 0f;
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// create the box from our superclass
		if (!super.activatePhysics(world)) {
			return false;
		}

		// Ground Sensor
		// -------------
		// We only allow the player to jump when he's on the ground.
		// Double jumping is not allowed.
		//
		// To determine whether or not the player is on the ground,
		// we create a thin sensor under his feet, which reports 
		// collisions with the world but has no collision response.
		Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.density = data.getFloat("density",0);
		sensorDef.isSensor = true;
		sensorShape = new PolygonShape();
		JsonValue sensorjv = data.get("sensor");
		sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
								 sensorjv.getFloat("height",0), sensorCenter, 0.0f);
		sensorDef.shape = sensorShape;

		// Ground sensor to represent our feet
		Fixture sensorFixture = body.createFixture( sensorDef );
		sensorFixture.setUserData(getSensorName());
		
		return true;
	}
	

	/**
	 * Applies force to the body of this player as given by movement.
	 *
	 * This method should be called after the movement attribute is set.
	 */
	public void applyWalkingForce() {
		if (!isActive()) {
			return;
		}

		// Don't want to be moving. Damp out player motion
		if (getMovement() == 0f) {
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (Math.abs(getVX()) >= getMaxSpeedXGround()) {
			setVX(Math.signum(getVX())*getMaxSpeedXGround());
		}{
			forceCache.set(getMovement(),0);
			body.applyForce(forceCache,getPosition(),true);
		}
	}

	/**
	 * Applies wind force to the body of this player.
	 *
	 * Horizontal component of force is ignored if player has reached horizontal maximum speed.
	 *
	 */
	public void applyWindForce(float fx, float fy) {
		if (!isActive()) {
			return;
		}
		//if force in same direction as currently moving and at max horiz speed, clamp
		if (Math.signum(fx) == Math.signum(getVX()) && Math.abs(getVX()) >= getMaxSpeedXAirWind()) {
			setVX(Math.signum(getVX())*getMaxSpeedXAirWind());
		}
		else {
			forceCache.set(fx,0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (Math.abs(getVY()) >= getMaxSpeedUp()) {
			setVY(Math.signum(getVY())*getMaxSpeedUp());
		} else {
			forceCache.set(0,fy);
			body.applyForce(forceCache, getPosition(), true);
		}
	}

	/**
	 * Applies drag force to the body of this player.
	 */
	public void applyDragForce(float fx) {
		if (!isActive()) {
			return;
		}
		if ((Math.signum(fx) == Math.signum(getVX()) && Math.abs(getVX()) < getMaxSpeedXAirDrag())
				|| Math.signum(fx) == -Math.signum(getVX()) || getVX() == 0){
			forceCache.set(fx, 0);
			float scl = Math.signum(fx) == -Math.signum(getVX()) ? Math.abs(getVX())/2 + 1 : .6f;
			forceCache.scl(scl);
			body.applyForce(forceCache, getPosition(), true);
		}
	}

	/**
	 * Applies lighter force to the body of this player.
	 */
	public void applyLighterForce(float umbAng) {
		if(lighterFuel == maxLighterFuel) {
			lighterFuel = 0;
			float umbrellaX = (float) Math.cos(umbAng);
			float umbrellaY = (float) Math.sin(umbAng);

			//switch between lighter boost as a force or setting velocity directly
			boolean force = false;

			if(force) {
				//determine X
				if (Math.signum(umbrellaX) == Math.signum(getVX()) && Math.abs(getVX()) >= getMaxSpeedXAirWind()) {
					setVX(Math.signum(getVX()) * getMaxSpeedXAirWind());
					forceCache.x = 0;
				} else {
					forceCache.x = umbrellaX * lighterForce;
				}
				//determine Y
				if (Math.abs(getVY()) >= getMaxSpeedUp()) {
					setVY(Math.signum(getVY()) * getMaxSpeedUp());
					forceCache.y = 0;
				} else {
					forceCache.y = umbrellaY * lighterForce;
				}
				body.applyLinearImpulse(forceCache, getPosition(), true);
			}
			else{
				//determine X
				if (Math.signum(umbrellaX) == Math.signum(getVX()) && Math.abs(getVX()) >= getMaxSpeedXAirWind()) {
					forceCache.x = getMaxSpeedXAirWind();
				} else {
					forceCache.x = umbrellaX * lighterForce;
				}
				//determine Y
				if (Math.abs(getVY()) >= getMaxSpeedUp()) {
					forceCache.y = getMaxSpeedUp();
				} else {
					forceCache.y = umbrellaY * lighterForce;
				}
				body.setLinearVelocity(forceCache);
			}
		}
	}

	public void refillLighter(){
		if(lighterFuel != maxLighterFuel){
			if(lighterFuel + lighterChangeRate > maxLighterFuel) lighterFuel = maxLighterFuel;
			else lighterFuel += lighterChangeRate;
		}
	}

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Apply cooldowns
		if(iFrames!=0) iFrames--;

		super.update(dt);
	}

	/**
	 * auxillary method to simplify draw method below.
	 */
	private void drawAux(GameCanvas canvas, Color tint){
		// mirror left or right (if player is facing left, this should be -1)
		float effect = faceRight ? -1.0f : 1.0f;
		TextureRegion t;
		if (isGrounded() && isMoving()) {
			fallElapsedTime = 0;

			// Walk animation
			walkElapsedTime += Gdx.graphics.getDeltaTime();
			t = walkAnimation.getKeyFrame(walkElapsedTime, true);
			canvas.draw(t, tint, t.getRegionWidth()/2f, t.getRegionHeight()/2f,
					getX() * drawScale.x, getY() * drawScale.y, getAngle(),
					effect * size/ t.getRegionWidth() * drawScale.x, size/t.getRegionHeight() * drawScale.y);
		} else {
			// Reset walk animation elapsed time
			walkElapsedTime = 0f;

			fallElapsedTime += Gdx.graphics.getDeltaTime();
			//TODO put an idle animation here. I just picked a frame that looked like Gale was standing
			t = isGrounded() ? walkAnimationFrames[2] : fallAnimation.getKeyFrame(fallElapsedTime, true);
			canvas.draw(t, tint, t.getRegionWidth()/2f, t.getRegionHeight()/2f,
					getX() * drawScale.x, getY() * drawScale.y, getAngle(),
					effect * size/ t.getRegionWidth() * drawScale.x, size/t.getRegionHeight() * drawScale.y);
		}
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		if(iFrames>0){
			if (iFrameCountdown == 0){
				iFrameCountdown = 7;
				drawIFrameTexture = !drawIFrameTexture;
			}
			if(drawIFrameTexture){
				drawAux(canvas, Color.BLACK);
				iFrameCountdown--;
			}
			else{
				drawAux(canvas, Color.WHITE);
				iFrameCountdown--;
			}
		}
		else{
			drawAux(canvas, Color.WHITE);
		}
	}

	/**
	 * Draws player HP information on screen and
	 * TODO: possibly other status information
	 * @param canvas the game canvas
	 */

	public void drawInfo(GameCanvas canvas){
		// draw health info
		if (hpTexture == null){
			return;
		}
		float height = hpTexture[health].getRegionHeight();
		float width = hpTexture[health].getRegionWidth();

		// TODO: HP Texture is manually scaled at the moment
		canvas.draw(hpTexture[health],Color.WHITE,width/2f,height/2f, drawScale.x,
					canvas.getHeight() - drawScale.y,0,0.3f,0.3f);

		// draw lighter info
		float lighter_capac = lighterFuel / maxLighterFuel;
		healthFont.setColor(Color.WHITE);
		canvas.drawText(formatter.format(lighter_capac), healthFont, 10,
				canvas.getHeight() - 90);
	}
	
	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		super.drawDebug(canvas);
		canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
	}


}