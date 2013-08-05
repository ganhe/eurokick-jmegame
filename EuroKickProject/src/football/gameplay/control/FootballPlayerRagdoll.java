/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.gameplay.control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import football.FootballGameStageManager;
import football.physics.BombControl;

/**
 *
 * @author hungcuong
 */
public class FootballPlayerRagdoll extends AbstractControl implements AnimEventListener, ActionListener {

    KinematicRagdollControl ragdoll;
    boolean leftStrafe = false, rightStrafe = false, forward = false, backward = false,
            leftRotate = false, rightRotate = false;
    AnimControl animControl;
    AnimChannel animChannel;
    private float moveSpeed;
    private CollisionShape bulletCollisionShape;
    private Sphere bullet;
    private Material mat2;
    Node model;
    private FootballGameStageManager stageManager;
    AssetManager assetManager;

    public FootballPlayerRagdoll(KinematicRagdollControl ragdoll, AnimControl animControl, AnimChannel animChannel, float moveSpeed, CollisionShape bulletCollisionShape, Sphere bullet, Material mat2, Node model, FootballGameStageManager stageManager) {
        this.ragdoll = ragdoll;
        this.animControl = animControl;
        this.animChannel = animChannel;
        this.moveSpeed = moveSpeed;
        this.bulletCollisionShape = bulletCollisionShape;
        this.bullet = bullet;
        this.mat2 = mat2;
        this.model = model;
        this.stageManager = stageManager;
    }

    void setupBomb(Node rootNode, AssetManager assetManager) {
        mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        mat2.setTexture("ColorMap", tex2);
        bullet = new Sphere(32, 32, 0.4f, true, false);
        bullet.setTextureMode(TextureMode.Projected);
        bulletCollisionShape = new SphereCollisionShape(0.4f);
    }

    void setupRagDoll(Node rootNode, AssetManager assetManager, PhysicsSpace space) {

        model = (Node) assetManager.loadModel("Models/Sinbad/Sinbad.mesh.xml");
        model.lookAt(new Vector3f(0, 0, -1), Vector3f.UNIT_Y);
        model.setLocalTranslation(4, 8, 2f);

        ragdoll = new KinematicRagdollControl(0.5f);
        model.addControl(ragdoll);

        space.add(ragdoll);
        moveSpeed = 20f;
        rootNode.attachChild(model);


        AnimControl control = model.getControl(AnimControl.class);
        animChannel = control.createChannel();
        animChannel.setAnim("IdleTop");
        control.addListener(this);
    }

    private void setupKeys(InputManager inputManager) {
        inputManager.addMapping("Rotate Left",
                new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rotate Right",
                new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Walk Forward",
                new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Walk Backward",
                new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Slice",
                new KeyTrigger(KeyInput.KEY_SPACE),
                new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Strafe Left", "Strafe Right");
        inputManager.addListener(this, "Rotate Left", "Rotate Right");
        inputManager.addListener(this, "Walk Forward", "Walk Backward");
        inputManager.addListener(this, "Slice");
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "shoot");
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

        if (channel.getAnimationName().equals("SliceHorizontal")) {
            channel.setLoopMode(LoopMode.DontLoop);
            channel.setAnim("IdleTop", 5);
            channel.setLoopMode(LoopMode.Loop);
        }

    }

    @Override
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Rotate Left")) {
            if (value) {
                leftRotate = true;
            } else {
                leftRotate = false;
            }

        } else if (binding.equals("Rotate Right")) {
            if (value) {
                rightRotate = true;
            } else {
                rightRotate = false;
            }
        } else if (binding.equals("Walk Forward")) {
            if (value) {
                forward = true;
            } else {
                forward = false;
            }
            animChannel.setAnim("RunBase");
            animChannel.setSpeed(1f);
        } else if (binding.equals("Walk Backward")) {
            if (value) {
                backward = true;
            } else {
                backward = false;
            }
            animChannel.setAnim("RunBase");
            animChannel.setSpeed(1f);
        } else if (binding.equals("Slice")) {
            if (value) {
                animChannel.setAnim("SliceHorizontal");
                animChannel.setSpeed(0.3f);
            }
        }

        if (binding.equals("shoot") && !value) {
            Camera cam = stageManager.getCurrentActiveCamera();
            Geometry bulletg = new Geometry("bullet", bullet);
            bulletg.setMaterial(mat2);
            bulletg.setShadowMode(ShadowMode.CastAndReceive);
            bulletg.setLocalTranslation(cam.getLocation());
            RigidBodyControl bulletNode = new BombControl(assetManager, bulletCollisionShape, 1);
//                RigidBodyControl bulletNode = new RigidBodyControl(bulletCollisionShape, 1);
            bulletNode.setLinearVelocity(cam.getDirection().mult(25));
            bulletg.addControl(bulletNode);
            //stadium.attachChild(bulletg);
            //getPhysicsSpace().add(bulletNode);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (forward) {
            model.move(model.getLocalRotation().multLocal(new Vector3f(0, 0, moveSpeed)).multLocal(tpf));
        } else if (backward) {
            model.move(model.getLocalRotation().multLocal(new Vector3f(0, 0, moveSpeed)).multLocal(-tpf));
        } else if (leftRotate) {
            model.rotate(0, 8f * tpf, 0);
        } else if (rightRotate) {
            model.rotate(0, 8f * -tpf, 0);
        }
        //fpsText.setText(cam.getLocation() + "/" + cam.getRotation());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        return this;
    }
}
