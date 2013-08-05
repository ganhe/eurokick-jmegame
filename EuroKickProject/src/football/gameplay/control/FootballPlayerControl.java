package football.gameplay.control;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import football.gameplay.info.*;


/**
 *
 * @author cuong.nguyenmanh2
 */
public class FootballPlayerControl extends AbstractControl {

    Spatial targetSpatial;
    Spatial leaderSpatial;
    Spatial ball;
    private FootballPlayerInfo playerInfo;
    private AnimControl animControl;
    private AnimChannel animChannel;
    private boolean chasingBall;

    public void initPlayerControl(FootballPlayerInfo playerInfo, Spatial ball) {
        this.playerInfo = playerInfo;
        this.ball = ball;

    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        setupPlayerControl();
    }

    // Gerneral function
    void setupPlayerControl() {
        this.animControl = ((Node) spatial).getChild("PlayerMesh").getControl(AnimControl.class);
        animChannel = animControl.createChannel();
        if (playerInfo.getRole().equals(PlayerRole.GoalKeeper)) {
            animChannel.setAnim("Idle");
            chasingBall = false;
        } else {
            animChannel.setAnim("RunFast");
            chasingBall = true;
        }
        
    }

    void setPose(String pose) {
    }

    void moveTo(Vector3f pos) {
        float speed = playerInfo.getRealSpeed();
        Vector3f location = spatial.getLocalTranslation();
        Vector3f target = pos.clone();


        Vector3f distance = target.subtract(location);
        if (distance.length() < 0.3f) {
            return;
        }
        Vector3f newVel = distance.normalize().mult(speed);
        //Vector3f steering = desierdVel.subtract(velocity).negate();

        spatial.setLocalTranslation(location.add(newVel));

    }

    void lookAt(Vector3f pos) {
        Vector3f location = spatial.getLocalTranslation();
        Vector3f target = pos.clone();


        Vector3f distance = target.subtract(location);
        if (distance.length() < 0.3f) {
            return;
        }
        Vector3f newVel = distance.normalize();
        Quaternion q = new Quaternion();
        q.lookAt(newVel, Vector3f.UNIT_Y);
        //q.addLocal(playerInfo.initalRot);
        spatial.setLocalRotation(q);
    }

    void move() {
    }

    // Action
    void think() {
    }

    void tell() {
    }

    void hurt() {
    }

    void shoot() {
    }

    void pass() {
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (chasingBall) {
            //moveTo(ball.getWorldTranslation());
        }
        lookAt(ball.getWorldTranslation());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        return this;
    }
}
