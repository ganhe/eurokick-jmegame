/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package football.state;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import football.FootballGUIManager;
import football.FootballGame;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class EditTeamState extends AbstractAppState {

    private FootballGame app;
    private FootballGUIManager gameGUIManager;
    Node model;
    private float speed = 1;
    private float angle = 0;
    private Node playerModel;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (FootballGame) app; // can cast Application to something more specific
        if (this.app.getStageManager() == null) {
            this.app.initGUI();
        }
        this.gameGUIManager = (FootballGUIManager) this.app.getGameGUIManager();
        //System.out.println("initialize!");
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        // Pause and unpause
        super.setEnabled(enabled);
        if (enabled) {
            gameGUIManager.loadAndGotoScreen("editPlayer");
            model = (Node) app.getAssetManager().loadModel("Models/Player/PlayerAni.j3o");
            app.getRootNode().attachChild(model);
            app.getFlyByCamera().setDragToRotate(true);
            playerModel = (Node) model.getChild("PlayerMesh");
            AnimControl animControl = playerModel.getControl(AnimControl.class);
            AnimChannel animChannel = animControl.createChannel();
            animChannel.setAnim("Idle");
            //System.out.println("Call me!");
            app.getCamera().setLocation(model.getChild("CamNode").getWorldTranslation());
            app.getCamera().lookAt(model.getChild("LookAtNode").getWorldTranslation(), Vector3f.UNIT_Y);
        } else {
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        Quaternion quat = new Quaternion();
        quat.fromAngleAxis(angle, Vector3f.UNIT_Y);
        angle += speed * tpf;
        model.setLocalRotation(quat);






    }
}
