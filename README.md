# 8venture-games

## Basic Architecture
**GameMode**
- Enumerates/Add/delete Objects
- Draw Background & Objects
- Performs Update on Every Components
- Gather all assets

**GameplayController**
- Create platforms + walls (Populate Level)
- Apply Gravity and Force --> Enforce Interaction
- Gather Gameplay-specific Assets

**CollisionController**
Might not be needed according to the role of GameplayController.
- Process/Handles Collision 
- Process/Handles Bounds
- Handle Vector Field Force application

![IMG_D33AB56CBC9C-1](https://user-images.githubusercontent.com/57926472/222020256-adc39bd3-973e-4638-b9fb-5a046d6c2b9c.jpeg)
