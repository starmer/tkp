package com.badlogicgames.plane;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class PlaneGame implements ApplicationListener {
    public static final int DEFAULT_PLAYER_SIZE = 64;
    public static final int MIN_ENEMY_SIZE = 20;
    public static final int MAX_ENEMY_SIZE = 300;
    private Texture enemyPandaTexture;
    private Texture playerPandaTexture;
    private Texture gameOverTexture;
    private Texture readyTexture;
    //private Sound dropSound;
    //private Music rainMusic;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Rectangle playerPanda;
    private Array<Rectangle> enemyPandas;
    private long lastDropTime;
    private boolean gameover = false;
    private boolean ready = true;
    private Rectangle gameoverRectangle;
    private Rectangle readyRectangle;

    @Override
    public void create() {
        // load the images for the droplet and the playerPanda, 64x64 pixels each
        enemyPandaTexture = new Texture(Gdx.files.internal("squarePanda.png"));
        playerPandaTexture = new Texture(Gdx.files.internal("panda.png"));

        gameOverTexture = new Texture(Gdx.files.internal("gameover.png"));
        readyTexture = new Texture(Gdx.files.internal("ready.png"));

        // load the drop sound effect and the rain background "music"
        //dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        //rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

        // start the playback of the background music immediately
        //rainMusic.setLooping(true);
        //rainMusic.play();

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        // create a Rectangle to logically represent the playerPanda
        playerPanda = new Rectangle();
        playerPanda.x = 800 / 2 - 64 / 2; // center the playerPanda horizontally
        playerPanda.y = 0; // bottom left corner of the playerPanda is 20 pixels above the bottom screen edge
        playerPanda.width = DEFAULT_PLAYER_SIZE;
        playerPanda.height = DEFAULT_PLAYER_SIZE;

        gameoverRectangle = new Rectangle();
        gameoverRectangle.x = 800 / 2 - 348 / 2;
        gameoverRectangle.y = 240;
        gameoverRectangle.width = 348;
        gameoverRectangle.height = 72;

        readyRectangle = new Rectangle();
        readyRectangle.x = 800 / 2 - 232 / 2;
        readyRectangle.y = 180;
        readyRectangle.width = 232;
        readyRectangle.height = 68;

        // create the enemyPandas array and spawn the first raindrop
        enemyPandas = new Array<Rectangle>();
        spawnEnemyPanda();
    }

    private void spawnEnemyPanda() {
        int size = MathUtils.random(MIN_ENEMY_SIZE, MAX_ENEMY_SIZE);
        Rectangle enemyPanda = new Rectangle();
        enemyPanda.x = MathUtils.random(0, 800-size);
        enemyPanda.y = 480;
        enemyPanda.width = size;
        enemyPanda.height = size;
        enemyPandas.add(enemyPanda);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render() {

        setupScreen();

        if(!gameover && ready){
            processUserInput();
            keepBucketInBounds();
            spawnRainDrops();
            moveRainDrops();
        } else {
            processUserInputForGameOver();
        }

        drawDropsAndBucket();

    }

    private void processUserInputForGameOver() {

        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        float touchX = touchPos.x;
        float touchY = touchPos.y;

        boolean readyTouched = readyRectangle.contains(touchX, touchY);
        boolean gameOverTouched = gameoverRectangle.contains(touchX, touchY);

        if(Gdx.input.isTouched() && !gameover && readyTouched) {
            ready = true;
            resetGame();
        } else {
            if(Gdx.input.isTouched() && gameover && gameOverTouched) {
                gameover = false;
                ready = false;
            }
        }

    }

    private void resetGame() {
        enemyPandas.clear();
        playerPanda.x = 800 / 2 - 64 / 2; // center the playerPanda horizontally
        playerPanda.y = 0; // bottom left corner of the playerPanda is 20 pixels above the bottom screen edge
        playerPanda.width = DEFAULT_PLAYER_SIZE;
        playerPanda.height = DEFAULT_PLAYER_SIZE;
    }

    private void moveRainDrops() {
        // move the enemyPandas, remove any that are beneath the bottom edge of
        // the screen or that hit the playerPanda. In the later case we play back
        // a sound effect as well.
        Iterator<Rectangle> iter = enemyPandas.iterator();
        while(iter.hasNext()) {
            Rectangle enemyPanda = iter.next();
            enemyPanda.y -= 200 * Gdx.graphics.getDeltaTime();

            if(enemyPanda.y + enemyPanda.height < 0) {
                iter.remove();
            }

            if(enemyPanda.overlaps(playerPanda)) {
                //dropSound.play();

                if(enemyPanda.area() > playerPanda.area()) {
                    endGame();
                } else {
                    increasePandaSize();
                }
                iter.remove();
            }
        }
    }

    private void increasePandaSize() {
        playerPanda.width = playerPanda.width + 5;
        playerPanda.height = playerPanda.height + 5;
    }

    private void endGame() {
        // bright-red-splatter-hi.png
        gameover = true;

    }

    private void spawnRainDrops() {
        if (enemyPandas.size < 3){
            spawnEnemyPanda();
        }
    }

    private void keepBucketInBounds() {
        // make sure the playerPanda stays within the screen bounds
        if(playerPanda.x < 0) playerPanda.x = 0;
        if(playerPanda.x > 800 - 64) playerPanda.x = 800 - 64;
    }

    private void processUserInput() {
        // process user input
        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            playerPanda.x = touchPos.x - 64 / 2;
        }
        if(Gdx.input.isKeyPressed(Keys.LEFT)) playerPanda.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Keys.RIGHT)) playerPanda.x += 200 * Gdx.graphics.getDeltaTime();
    }

    private void drawDropsAndBucket() {
        // begin a new batch and draw the playerPanda and
        // all drops
        batch.begin();
        batch.draw(playerPandaTexture, playerPanda.x, playerPanda.y, playerPanda.width, playerPanda.height);
        for(Rectangle enemyPanda: enemyPandas) {
            batch.draw(this.enemyPandaTexture, enemyPanda.x, enemyPanda.y, enemyPanda.width, enemyPanda.height);
        }

        if(gameover) {
            batch.draw(this.gameOverTexture, 800 / 2 - 348 / 2, 240);
        }

        if(!gameover && !ready) {
            batch.draw(this.readyTexture, 800 / 2 - 232 / 2, 180);
        }

        batch.end();
    }

    private void setupScreen() {
        // clear the screen with a dark blue color. The
        // arguments to glClearColor are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void dispose() {
        // dispose of all the native resources
        enemyPandaTexture.dispose();
        playerPandaTexture.dispose();
        gameOverTexture.dispose();
        readyTexture.dispose();
        //dropSound.dispose();
        //rainMusic.dispose();
        batch.dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}