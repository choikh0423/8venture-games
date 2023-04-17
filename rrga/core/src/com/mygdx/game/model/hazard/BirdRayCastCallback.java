package com.mygdx.game.model.hazard;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.mygdx.game.model.PlayerModel;
import com.mygdx.game.model.UmbrellaModel;
import com.mygdx.game.model.WindModel;

import java.util.SortedMap;

public class BirdRayCastCallback implements RayCastCallback {

    public ObjectMap<Fixture, Float> collisions = new ObjectMap<>();


    @Override
    public float reportRayFixture(Fixture fixture, Vector2 v1, Vector2 v2, float v) {
        //add see-through objects here
        if (!(fixture.getBody().getUserData() instanceof UmbrellaModel) &&
                !(fixture.getBody().getUserData() instanceof WindModel) &&
                !(fixture.getBody().getUserData() instanceof LightningHazard) &&
                !(fixture.getBody().getUserData() instanceof BirdHazard)) {
            collisions.put(fixture, v);
        }
        return 1;
    }
}
