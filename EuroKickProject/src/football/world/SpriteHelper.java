/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.world;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import engine.sprites.Sprite;
import engine.sprites.SpriteAnimation;
import engine.sprites.SpriteImage;
import engine.sprites.SpriteManager;
import engine.sprites.SpriteMesh;
import engine.util.FileUtilities;
import engine.util.ImageUtilities;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author hungcuong
 */
public class SpriteHelper {

    Node rootNode;
    AssetManager assetManager;
    private SpriteManager spriteManager;
    private String[] npcList;
    private int SPRITES = 4000; //num of columns = *6.
    public static String NPC_IMAGE_DIRECTORY = "2d/FootballFan/";
    public static Color COLOR_TO_MAKE_TRANSPARENT = new Color(120, 195, 128);
    public static boolean RANDOM_FACING_DIRECTION = true;
    ArrayList<SpriteAnimation> animArray = new ArrayList<SpriteAnimation>();

    public SpriteHelper(Node rootNode, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;

    }

    void initSprite() {
        spriteManager = new SpriteManager(1024, 1024, SpriteMesh.Strategy.ALLOCATE_NEW_BUFFER, rootNode, assetManager);

        File npcLocation = new File(FileUtilities.ASSET_DIRECTORY + NPC_IMAGE_DIRECTORY);
        npcList = npcLocation.list(FileUtilities.SUPPORTED_IMAGES);
        for (int i = 0; i < npcList.length; i++) {
            npcList[i] = NPC_IMAGE_DIRECTORY + npcList[i];
        }

        loadSheet();
        //createSpriteField();


    }

    void loadSheet() {
        //int numSpritesX = 12;
        //int numSpritesY = 8;
        //int numSubSpritesX = 3;
        //int numSubSpritesY = 4;
        int numSpritesX = 4;
        int numSpritesY = 4;
        int numSubSpritesX = 2;
        int numSubSpritesY = 2;
        int numSpriteSheets = (numSpritesX / numSubSpritesX) * (numSpritesY / numSubSpritesY);

        BufferedImage image = ImageUtilities.loadImage(npcList[0], assetManager);

        BufferedImage transparentImage = ImageUtilities.transformColorToTransparency(image, COLOR_TO_MAKE_TRANSPARENT);
        BufferedImage[][] split = ImageUtilities.split(transparentImage, numSpritesX, numSpritesY);
        //ImageUtilities.viewImage(ImageUtilities.merge(split));
        for (int index = 0; index < numSpriteSheets; index++) {
            BufferedImage[][] sheet = ImageUtilities.getSubsheet(split, numSubSpritesX, numSubSpritesY, index);
            //ImageUtilities.viewImage(ImageUtilities.merge(sheet));

            BufferedImage[] images = ImageUtilities.asSingleArray(sheet, false);
            //ImageUtilities.viewImage(ImageUtilities.merge(images));

            SpriteImage[] sprites = new SpriteImage[images.length];
            for (int i = 0; i < images.length; i++) {
                sprites[i] = spriteManager.createSpriteImage(images[i], false);
            }

            SpriteAnimation anim = new SpriteAnimation(sprites, 0.3f);
            animArray.add(anim);
        }
    }

    public void attachSprite(AppStateManager stateManager) {
        stateManager.attach(spriteManager);
    }

    void createSpriteField(float boxX, float boxY) {
        //spriteManager.putAnimation("rotateAroundSelf"+index, anim);


        for (int i = 0; i < SPRITES; i++) {
            int startFrame = (RANDOM_FACING_DIRECTION) ? i : 0;
            int ranSheet = FastMath.rand.nextInt(6);
            Sprite sprite = new Sprite(animArray.get(ranSheet), startFrame);
            //sprite.setPosition(index * 2, 0, i*1.5f);
            float x = FastMath.rand.nextFloat();
            float y = FastMath.rand.nextFloat();
            sprite.setPosition(x * boxX * 2f - boxX / 2, 0, y * boxY * 1.5f - boxY / 2);
        }

    }

    void makeSprite(SpriteAnimation anim, int startFrame, Vector3f pos) {
        Sprite sprite = new Sprite(anim, startFrame);
        sprite.setPosition(pos);
    }
}
