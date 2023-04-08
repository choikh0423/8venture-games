package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.mygdx.game.mode.*;
import com.mygdx.game.screen.LoseScreen;
import com.mygdx.game.screen.VictoryScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.ScreenListener;


public class GDXRoot extends Game implements ScreenListener {
	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for the main menu screen (CONTROLLER CLASS) */
	private MenuMode menu;
	/** Player mode for the game play (CONTROLLER CLASS) */
	private GameMode playing;

	/** Player mode for pausing the game (CONTROLLER CLASS) */
	private PauseMode pausing;

	/** Screen mode for transitioning between levels */
	private VictoryScreen victory;

	/** Screen mode for transitioning between levels */
	private LoseScreen defeat;

	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() { }

	/**
	 * Called when the Application is first created.
	 *
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas  = new GameCanvas();
		loading = new LoadingMode("assets.json",canvas,1);
		menu = new MenuMode(canvas);
		playing = new GameMode();
		pausing = new PauseMode(canvas);
		victory = new VictoryScreen(canvas);
		defeat = new LoseScreen(canvas);

		loading.setScreenListener(this);
		pausing.setScreenListener(this);
		victory.setScreenListener(this);
		defeat.setScreenListener(this);
		setScreen(loading);
	}

	/**
	 * Called when the Application is destroyed.
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);

		canvas.dispose();
		canvas = null;
		menu.dispose();
		menu = null;
		playing.dispose();
		playing = null;
		pausing.dispose();
		pausing = null;
		victory.dispose();
		victory = null;
		defeat.dispose();
		defeat = null;

		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
	}

	/**
	 * Called when the Application is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.resize();
		super.resize(width,height);
	}

	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			// finish loading assets, let all other controllers gather necessary assets
			directory = loading.getAssets();
			playing.gatherAssets(directory);
			menu.gatherAssets(directory);
			pausing.gatherAssets(directory);
			victory.gatherAssets(directory);
			defeat.gatherAssets(directory);

			// transition to gameplay screen.
			menu.setScreenListener(this);
			setScreen(menu);
			loading.dispose();
			loading = null;
		} else if (screen == menu) {
			switch (exitCode){
				case MenuMode.EXIT_QUIT:
					Gdx.app.exit();
					break;
				case MenuMode.EXIT_PLAY:
					playing.setScreenListener(this);
					playing.setCanvas(canvas);
					// TODO: use exit codes to determine level.
					//  reserve exit codes 1 to 30 for levels.
					playing.setLevel(1);
					playing.reset();
					setScreen(playing);
					break;
//				case MenuMode.EXIT_SETTINGS:

			}
			// Transition might need to change
			// menu.dispose();
		} else if (screen == pausing){
			switch (exitCode){
				case PauseMode.EXIT_RESUME:
					setScreen(pausing.getBackgroundScreen());
					break;
				case PauseMode.EXIT_RESTART:
					playing.reset();
					setScreen(playing);
					break;
				default:
					Gdx.app.exit();
			}

		} else if (screen == playing) {
			switch (exitCode){
				case GameMode.EXIT_VICTORY:
					victory.setBackgroundScreen(playing);
					setScreen(victory);
					break;
				case GameMode.EXIT_FAIL:
					setScreen(defeat);
					break;
				case GameMode.EXIT_PAUSE:
					pausing.setBackgroundScreen(playing);
					setScreen(pausing);
					break;
				case GameMode.EXIT_QUIT:
					Gdx.app.exit();
				default:
					break;
			}
		} else if (screen == victory && exitCode == VictoryScreen.EXIT_RESTART){
			playing.reset();
			setScreen(playing);
		} else if (screen == defeat && exitCode == LoseScreen.EXIT_RESTART){
			playing.reset();
			setScreen(playing);
		}

	}
}
