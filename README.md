# Gale by 8venture Games

![Banner](https://github.com/choikh0423/8venture-games/assets/57926472/aaade5c6-5267-40e0-a2f3-9b0e35848cb9)

## Game Description
Gale is a puzzle platformer in which the player's primary mechanism of movement is parachute physics represented by an umbrella catching gusts of wind. By aiming their umbrella in the right position at the right time, the player is able to propel themselves from platform to platform. They must also be aware of hazards that can inhibit their progress or damage the umbrella.

[Official Gale Game Manual](https://drive.google.com/file/d/18eWzLGW1wK-oyUedte51pWnlAx2Vm5ib/view?usp=sharing)

[Official Gale Game Trailer](https://www.youtube.com/watch?v=lg16KX-aXWo)

---
## Controls

| Control | Description |
| ------------- | ------------- |
| A/D Key  | Horizontal movement on platform |
| Mouse Movement | Umbrella rotation according to mouse position |
| Mouse Left Click  | Closing and opening umbrella |
| W Key| Dash towards the direction of the umbrella |
| Space key | Zooms out for wider scope of view|
| Escape Key| Pausing the game |

---
## Running the Game Release
1. Install the official release(v5.1.0 official) from the "Releases" tab
2. Choose the appropriate download for your operating system (Windows/OS)
3. Click on Gale file to run!

---
## Getting Started for Developers
1. Clone this repository
2. On Intellij: Open project with build.gradle, then press "Open as Project"
3. Add new run configuration: Press "Edit Configuration Button," then press "Add Configuration" (Note: You might have to press build on the build panel on libgdx
4. Modify configuration option: Modify the configuration to the following detail:
  - Name: Gale
  - Java Version: Java11
  - Class Path: -cp "rrga.desktop.main"
  - If using macbook, add VM Options: -XstartOnFirstThread
  - Main Class: com.mygdx.game.DesktopLauncher
  - Working Directory: 8venture-games/rrga/assets
5. Confirming configuration and run game: Press "OK," then press "Build/Run"
