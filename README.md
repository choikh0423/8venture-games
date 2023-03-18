# 8venture-games
## Game Description
Rain, Rain, Go Away is a puzzle platformer in which the player's primary mechanism of movement is parachute physics represented by an umbrella catching gusts of wind. By aiming their umbrella in the right position at the right time, the player is able to propel themselves from platform to platform. They must also be aware of hazards that can inhibit their progress or damage the umbrella.

## External Links
Team Notion: https://glen-crabapple-3a3.notion.site/Essentials-1095ec178b324087bc984890605b5b20

Programming Sprint Board: https://glen-crabapple-3a3.notion.site/84f28aac67f1412ca7d8f1563ed1392e?v=63916ed0dc8a4220a1dfec5196c8d02e

API Documentation for Designers: https://glen-crabapple-3a3.notion.site/API-Documentation-for-Design-Team-bc768fd29d5c4cbdb35d02380e9d778c

## Getting Started
1. Open project with build.gradle > "Open as Project"
2. Edit Configuration > Add Configuration
3. "Add New" > Application
4. Change the configuration setting to following:
  - name: rrga, 
  - cp "rrga.desktop.main"
  - java version: java11
  - main class: desktoplauncher
  - working directory: rrga/assets
5. Press OK > Build/Run

## Running Executables
For MacOS:
java -XstartOnFirstThread -jar rrga_gameplay_prototype.jar

For Windows:
java -jar rrga_gameplay_prototype.jar

## Basic Architecture
**GameMode**
- State Controls
- Gather all assets - calls GameplayController gather function
- Draws everything
- Performs Update on Every Components

**GameplayController**
- Create Objects - Walls, Platform, Player (aka. everything)
- Enumerates/Add/delete Objects
- Apply Gravity and Force --> Enforce Interaction
- Gather Gameplay-specific Assets

**CollisionController**
Might not be needed according to the role of GameplayController.
- Process/Handles Collision 
- Process/Handles Bounds
- Handle Vector Field Force application

![IMG_D33AB56CBC9C-1](https://user-images.githubusercontent.com/57926472/222020256-adc39bd3-973e-4638-b9fb-5a046d6c2b9c.jpeg)
