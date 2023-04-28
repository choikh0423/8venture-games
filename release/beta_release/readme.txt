Controls for Gale - Alpha Release

1. A/D Key: Horizontal Movement On Platform

2. Mouse Movement: The umbrella tip will follow the mouse and turn accordingly. However, the umbrella will not go below its horizontal axis of rotation.

3. Mouse Left Click: Closing and Opening Umbrella

4. W key: Dash Towards the Direction of the Umbrella

5. Z key: Zooms out for wider scope of view

6. R Key: Reset to Start Position

7. P Key: Pausing the game

8. N Key: Shortcut to next level

9. B Key: Turn on debug mode

===========================================================================
Additional Mechanism Explained (Will be added to the tutorial)
1. Floating Mechanism: user can still move in-air in regions without wind by using the open umbrella. This will be a weak force, yet the user can gain horizontal/vertical drag according to the umbrella orientation. You can get maximum horizontal velocity when facing umbrella 45 degrees to left or right(originating from parachute mechanism).

2. Dash recharges only on ground.

3. Zoom out only works when you are stationary on the platform. All player control will be restricted during this time.

===========================================================================

Basic Interactions

1. Wind/Umbrella Interaction: Orientation of the umbrella and the wind determines the direction and magnitude of wind force acting upon the player. The player will gain most force when the direction of wind and the direction that the umbrella points towards is equal.

2. Platform/Player Interaction: Allows horizontal movement on platform. 

3. Hazard/Player+Umbrella Collision: When the hazard hits either the umbrella or the player, the player will lose health. The umbrella and the player will be given unified health bar. There will be a slight knockback once colliding and IFrame will be applied. The player will blink during IFrame.

4. Win/Lose Condition: You will lose when you fall to the bottom without a platform. You will also lose if you lose all of your health due to collision with hazards. You win when you reach the location with the floating scarf - the goal point.

5. Out of Bound: There is an invisible wall at places that the player can not move in. The invisible walls will later be given with textures that indicate that the region is incommutable.