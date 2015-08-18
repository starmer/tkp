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
    private Texture enemyPanda;
    private Texture playerPanda;
    //private Sound dropSound;
    //private Music rainMusic;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Rectangle bucket;
    private Array<Rectangle> enemyPandas;
    private long lastDropTime;

    @Override
    public void create() {
        // load the images for the droplet and the bucket, 64x64 pixels each
        enemyPanda = new Texture(Gdx.files.internal("squarePanda.png"));
        playerPanda = new Texture(Gdx.files.internal("panda.png"));

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

        // create a Rectangle to logically represent the bucket
        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2; // center the bucket horizontally
        bucket.y = 0; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
        bucket.width = 32;
        bucket.height = 64;

        // create the enemyPandas array and spawn the first raindrop
        enemyPandas = new Array<Rectangle>();
        spawnEnemyPanda();
    }

    private void spawnEnemyPanda() {
        int size = MathUtils.random(0, 500);
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
        processUserInput();
        keepBucketInBounds();
        spawnRainDrops();
        moveRainDrops();
        drawDropsAndBucket();
    }

    private void moveRainDrops() {
        // move the enemyPandas, remove any that are beneath the bottom edge of
        // the screen or that hit the bucket. In the later case we play back
        // a sound effect as well.
        Iterator<Rectangle> iter = enemyPandas.iterator();
        while(iter.hasNext()) {
            Rectangle enemyPanda = iter.next();
            enemyPanda.y -= 200 * Gdx.graphics.getDeltaTime();

            if(enemyPanda.y + enemyPanda.height < 0) {
                iter.remove();
            }

            if(enemyPanda.overlaps(bucket)) {
                //dropSound.play();
                iter.remove();
            }
        }
    }

    private void spawnRainDrops() {
        if (enemyPandas.size == 0){
            spawnEnemyPanda();
        }
    }

    private void keepBucketInBounds() {
        // make sure the bucket stays within the screen bounds
        if(bucket.x < 0) bucket.x = 0;
        if(bucket.x > 800 - 64) bucket.x = 800 - 64;
    }

    private void processUserInput() {
        // process user input
        if(Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }
        if(Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if(Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
    }

    private void drawDropsAndBucket() {
        // begin a new batch and draw the bucket and
        // all drops
        batch.begin();
        batch.draw(playerPanda, bucket.x, bucket.y, 64, 64);
        for(Rectangle enemyPanda: enemyPandas) {
            batch.draw(this.enemyPanda, enemyPanda.x, enemyPanda.y, enemyPanda.width, enemyPanda.height);
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
        enemyPanda.dispose();
        playerPanda.dispose();
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