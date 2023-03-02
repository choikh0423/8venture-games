# 8venture-games

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

## Basic Architecture
**GameMode**
- State Controls
- Draw Background & Objects
- Performs Update on Every Components
- Gather all assets

**GameplayController**
- Create platforms + walls (Populate Level)
- Enumerates/Add/delete Objects
- Apply Gravity and Force --> Enforce Interaction
- Gather Gameplay-specific Assets

**CollisionController**
Might not be needed according to the role of GameplayController.
- Process/Handles Collision 
- Process/Handles Bounds
- Handle Vector Field Force application

![IMG_D33AB56CBC9C-1](https://user-images.githubusercontent.com/57926472/222020256-adc39bd3-973e-4638-b9fb-5a046d6c2b9c.jpeg)
