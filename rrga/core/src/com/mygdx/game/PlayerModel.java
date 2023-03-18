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
package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;

import com.mygdx.game.obstacle.*;

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
	/** The maximum character horizontal speed */
	private final float maxspeed_x;
	/** The maximum character vertical speed */
	private final float maxspeed_y;
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
	/** The scale to multiply the texture by for drawing */
	private float textureScale;
	/** Max player hp */
	private int MAX_HEALTH;
	/** Player hp */
	private int health;
	public BitmapFont healthFont = new BitmapFont();
	
	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/**
	 * The number i-frames the player currently has
	 */
	private int iFrames;

	/** The number of updates before the texture is switched when I-Frames are active */
	private int iFrameCountdown = 7;

	/** When the player is hit, whether or not the all white texture is drawn.
	 * Swaps between all white and regular */
	private boolean drawIFrameTexture = true;


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
	public float getMaxSpeedX() {
		return maxspeed_x;
	}

	/**
	 * Returns the upper limit on player vertical movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on player left-right movement.
	 */
	public float getMaxSpeedY() {
		return maxspeed_y;
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
	public PlayerModel(JsonValue data, float width, float height, int maxHp) {
		// The shrink factors fit the image to a tigher hitbox
		super(	data.get("pos").getFloat(0),
				data.get("pos").getFloat(1),
				width*data.get("shrink").getFloat( 0 ),
				height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
		setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		maxspeed_x = data.getFloat("maxspeed_x", 0);
		maxspeed_y = data.getFloat("maxspeed_y", 0);
		damping = data.getFloat("damping", 0);
		force = data.getFloat("force", 0);
		textureScale = data.getFloat("texturescale", 1.0f);
		sensorName = "PlayerGroundSensor";
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
	public void applyInputForce() {
		if (!isActive()) {
			return;
		}

		// Don't want to be moving. Damp out player motion
		if (getMovement() == 0f) {
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (Math.abs(getVX()) >= getMaxSpeedX()) {
			setVX(Math.signum(getVX())*getMaxSpeedX());
		}{
			forceCache.set(getMovement(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		// TODO: consider/remove Jump!
//		if (isJumping()) {
//			forceCache.set(0, jump_force);
//			body.applyLinearImpulse(forceCache,getPosition(),true);
//		}
	}

	/**
	 * Applies some external force to the body of this player.
	 *
	 * Horizontal component of force is ignored if player has reached horizontal maximum speed.
	 *
	 */
	public void applyExternalForce(float fx, float fy) {
		if (!isActive()) {
			return;
		}
		if (Math.abs(getVX()) >= getMaxSpeedX()) {
			setVX(Math.signum(getVX())*getMaxSpeedX());
		}
		else {
			forceCache.set(fx,0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (Math.abs(getVY()) >= getMaxSpeedY()) {
			setVY(Math.signum(getVY())*getMaxSpeedY());
		} else {
			forceCache.set(0,fy);
			body.applyForce(forceCache, getPosition(), true);
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
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		float effect = faceRight ? 1.0f : -1.0f;
		if(iFrames>0){
			if (iFrameCountdown == 0){
				iFrameCountdown = 7;
				drawIFrameTexture = !drawIFrameTexture;
			}
			if(drawIFrameTexture){
				canvas.draw(texture,Color.BLACK,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*textureScale,textureScale);
				iFrameCountdown--;
			}
			else{
				canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*textureScale,textureScale);
				iFrameCountdown--;
			}
		}
		else{
			canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),effect*textureScale,textureScale);
		}
		canvas.drawText("HP: " + getHealth(), healthFont, 25, canvas.getHeight()-25);
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