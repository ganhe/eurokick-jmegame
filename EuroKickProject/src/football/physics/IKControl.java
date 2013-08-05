package football.physics;


 
import com.jme3.bullet.control.ragdoll.RagdollPreset;
import com.jme3.bullet.control.ragdoll.HumanoidRagdollPreset;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.HullCollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.ragdoll.RagdollUtils;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.TempVars;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**<strong>This control is still a WIP, use it at your own risk</strong><br>
 * To use this control you need a model with an AnimControl and a SkeletonControl.<br>
 * This should be the case if you imported an animated model from Ogre or blender.<br>
 * Note enabling/disabling the control add/removes it from the physic space<br>
 * <p>
 * This control creates collision shapes for each bones of the skeleton when you call spatial.addControl(ragdollControl).
 * <ul>
 *     <li>The shape is HullCollision shape based on the vertices associated with each bone and based on a tweakable weight threshold (see setWeightThreshold)</li>
 *     <li>If you don't want each bone to be a collision shape, you can specify what bone to use by using the addBoneName method<br>
 *         By using this method, bone that are not used to create a shape, are "merged" to their parent to create the collision shape.
 *     </li>
 * </ul>
 *</p>
 *<p>
 *There are 2 modes for this control :
 * <ul>
 *     <li><strong>The kinematic modes :</strong><br>
 *        this is the default behavior, this means that the collision shapes of the body are able to interact with physics enabled objects.
 *        in this mode physic shapes follow the moovements of the animated skeleton (for example animated by a key framed animation)
 *        this mode is enabled by calling setKinematicMode();
 *     </li>
 *     <li><strong>The ragdoll modes :</strong><br>
 *        To enable this behavior, you need to call setRagdollMode() method.
 *        In this mode the charater is entirely controled by physics, so it will fall under the gravity and move if any force is applied to it.
 *     </li>
 * </ul>
 *</p>
 *
 * @author Normen Hansen and Rémy Bouquet (Nehon) and Rickard Edén
 */
public class IKControl implements PhysicsControl, PhysicsCollisionListener {
 
    protected static final Logger logger = Logger.getLogger(IKControl.class.getName());
    protected Map<String, PhysicsBoneLink> boneLinks = new HashMap<String, PhysicsBoneLink>();
    protected Skeleton skeleton;
    protected PhysicsSpace space;
    protected boolean enabled = true;
    protected boolean debug = false;
    protected PhysicsRigidBody baseRigidBody;
    protected float weightThreshold = -1.0f;
    protected Spatial targetModel;
    protected Vector3f initScale;
    protected Mode mode = Mode.Kinetmatic;
    protected boolean blendedControl = false;
    protected float blendTime = 1.0f;
    protected float blendStart = 0.0f;
    protected List<RagdollCollisionListener> listeners;
    protected float eventDispatchImpulseThreshold = 10;
    protected RagdollPreset preset = new HumanoidRagdollPreset();
    protected Set<String> boneList = new TreeSet<String>();
    protected Vector3f modelPosition = new Vector3f();
    protected Quaternion modelRotation = new Quaternion();
    protected float rootMass = 15;
    protected float totalMass = 0;
    protected boolean added = false;
 
    private Map<Bone, Vector3f> targets = new HashMap<Bone, Vector3f>();
 
    private float rotSpeed = 7f;
    private float limbDampening = 0.6f;
 
    public static enum Mode {
 
        Kinetmatic,
        IK
    }
 
    protected class PhysicsBoneLink {
 
        protected Bone bone;
        protected Quaternion initalWorldRotation;
        protected SixDofJoint joint;
        protected PhysicsRigidBody rigidBody;
        protected Quaternion startBlendingRot = new Quaternion();
        protected Vector3f startBlendingPos = new Vector3f();
    }
 
    /**
     * contruct a KinematicRagdollControl
     */
    public IKControl() {
    }
 
    public IKControl(float weightThreshold) {
        this.weightThreshold = weightThreshold;
    }
 
    public IKControl(RagdollPreset preset, float weightThreshold) {
        this.preset = preset;
        this.weightThreshold = weightThreshold;
    }
 
    public IKControl(RagdollPreset preset) {
        this.preset = preset;
    }
 
    public void update(float tpf) {
        if (!enabled) {
            return;
        }
        TempVars vars = TempVars.get();
 
        Quaternion tmpRot1 = vars.quat1;
        Quaternion[] tmpRot2 = new Quaternion[]{vars.quat2,new Quaternion()};
 
        //if the ragdoll has the control of the skeleton, we update each bone with its position in physic world space.
        if (mode == mode.IK && targetModel.getLocalTranslation().equals(modelPosition)) {
            Iterator i = targets.keySet().iterator();
            float distance;
            Bone bone;
            while(i.hasNext()){
                bone = (Bone) i.next();
                //if(!bone.hasUserControl()) continue;
                    
                distance = bone.getModelSpacePosition().distance(targets.get(bone));
                if(distance < 0.1f){
                    continue;
                }
                updateBone(boneLinks.get(bone.getParent().getName()), tpf*(float) FastMath.sqrt(distance), vars, tmpRot1, tmpRot2, bone, targets.get(bone));
 
                Vector3f position = vars.vect1;
 
                for(PhysicsBoneLink link2: boneLinks.values()){
                    matchPhysicObjectToBone(link2, position, tmpRot1);
                }
            }
 
        } else {
            //the ragdoll does not have the controll, so the keyframed animation updates the physic position of the physic bonces
            for (PhysicsBoneLink link : boneLinks.values()) {
 
                Vector3f position = vars.vect1;
 
                //if blended control this means, keyframed animation is updating the skeleton,
                //but to allow smooth transition, we blend this transformation with the saved position of the ragdoll
                if (blendedControl) {
                    Vector3f position2 = vars.vect2;
                    //initializing tmp vars with the start position/rotation of the ragdoll
                    position.set(link.startBlendingPos);
                    tmpRot1.set(link.startBlendingRot);
 
                    //interpolating between ragdoll position/rotation and keyframed position/rotation
                    tmpRot2[0].set(tmpRot1).nlerp(link.bone.getModelSpaceRotation(), blendStart / blendTime);
                    position2.set(position).interpolate(link.bone.getModelSpacePosition(), blendStart / blendTime);
                    tmpRot1.set(tmpRot2[0]);
                    position.set(position2);
 
                    //updating bones transforms
                    if (boneList.isEmpty()) {
                        //we ensure we have the control to update the bone
                        link.bone.setUserControl(true);
                        link.bone.setUserTransformsWorld(position, tmpRot1);
                        //we give control back to the key framed animation.
                        link.bone.setUserControl(false);
                    } else {
                        RagdollUtils.setTransform(link.bone, position, tmpRot1, true, boneList);
                    }
 
                }
                //setting skeleton transforms to the ragdoll
                matchPhysicObjectToBone(link, position, tmpRot1);
                modelPosition.set(targetModel.getLocalTranslation());
 
            }
 
            //time control for blending
            if (blendedControl) {
                blendStart += tpf;
                if (blendStart > blendTime) {
                    blendedControl = false;
                }
            }
        }
        vars.release();
 
    }
 
    public void updateBone(PhysicsBoneLink link, float tpf, TempVars vars, Quaternion tmpRot1, Quaternion[] tmpRot2, Bone tipBone, Vector3f target){
        if(link.bone.getParent() == null) return;
        Quaternion preQuat = link.bone.getLocalRotation();
        link.bone.setUserControl(true);
        
        Vector3f vectorAxis;
        float[] measureDist = new float[]{Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY};
        for(int dirIndex = 0; dirIndex < 3; dirIndex++){
            if (dirIndex == 0) vectorAxis = Vector3f.UNIT_Z;
            else if (dirIndex == 1) vectorAxis = Vector3f.UNIT_X;
            else vectorAxis = Vector3f.UNIT_Y;
 
            for(int posOrNeg = 0; posOrNeg < 2; posOrNeg++){
 
                tmpRot1.fromAngleAxis(rotSpeed*tpf/(link.rigidBody.getMass()*2), vectorAxis);
                tmpRot2[posOrNeg] = link.bone.getLocalRotation().mult(tmpRot1);
                tmpRot2[posOrNeg].normalizeLocal();
 
                rotSpeed = -rotSpeed;
 
                link.bone.setUserTransforms(Vector3f.ZERO.clone(), tmpRot2[posOrNeg],Vector3f.UNIT_XYZ.clone() );
                link.bone.updateWorldVectors();
                measureDist[posOrNeg] = tipBone.getModelSpacePosition().distance(target);
                link.bone.setUserTransforms(Vector3f.ZERO.clone(), preQuat,Vector3f.UNIT_XYZ.clone() );
            }
 
            if(measureDist[0] < measureDist[1]){
                link.bone.setUserTransforms(Vector3f.ZERO.clone(), tmpRot2[0] ,Vector3f.UNIT_XYZ.clone() );
            }
            else if(measureDist[0] > measureDist[1]){
                link.bone.setUserTransforms(Vector3f.ZERO.clone(), tmpRot2[1] ,Vector3f.UNIT_XYZ.clone() );
            }
 
        }
        link.bone.getLocalRotation().normalizeLocal();
 
        link.bone.updateWorldVectors();
 
        if (link.bone.getParent() != null){
            updateBone(boneLinks.get(link.bone.getParent().getName()), tpf*limbDampening, vars, tmpRot1, tmpRot2, tipBone, target);
        }
    }
 
    /**
     * Set the transforms of a rigidBody to match the transforms of a bone.
     * this is used to make the ragdoll follow the skeleton motion while in Kinematic mode
     * @param link the link containing the bone and the rigidBody
     * @param position just a temp vector for position
     * @param tmpRot1  just a temp quaternion for rotation
     */
    private void matchPhysicObjectToBone(PhysicsBoneLink link, Vector3f position, Quaternion tmpRot1) {
        //computing position from rotation and scale
        targetModel.getWorldTransform().transformVector(link.bone.getModelSpacePosition(), position);
 
        //computing rotation
        tmpRot1.set(link.bone.getModelSpaceRotation()).multLocal(link.bone.getWorldBindInverseRotation());
        targetModel.getWorldRotation().mult(tmpRot1, tmpRot1);
        tmpRot1.normalizeLocal();
 
        //updating physic location/rotation of the physic bone
        link.rigidBody.setPhysicsLocation(position);
        link.rigidBody.setPhysicsRotation(tmpRot1);
 
    }
 
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 
    /**
     * rebuild the ragdoll
     * this is useful if you applied scale on the ragdoll after it's been initialized
     */
    public void reBuild() {
        setSpatial(targetModel);
        addToPhysicsSpace();
    }
 
    public void setSpatial(Spatial model) {
        if (model == null) {
            removeFromPhysicsSpace();
            clearData();
            return;
        }
        targetModel = model;
        Node parent = model.getParent();
 
        Vector3f initPosition = model.getLocalTranslation().clone();
        Quaternion initRotation = model.getLocalRotation().clone();
        initScale = model.getLocalScale().clone();
 
        model.removeFromParent();
        model.setLocalTranslation(Vector3f.ZERO);
        model.setLocalRotation(Quaternion.IDENTITY);
        model.setLocalScale(1);
        //HACK ALERT change this
        //I remove the skeletonControl and readd it to the spatial to make sure it's after the ragdollControl in the stack
        //Find a proper way to order the controls.
        SkeletonControl sc = model.getControl(SkeletonControl.class);
        model.removeControl(sc);
        model.addControl(sc);
        //---- 
 
        removeFromPhysicsSpace();
        clearData();
        // put into bind pose and compute bone transforms in model space
        // maybe dont reset to ragdoll out of animations?
        scanSpatial(model);
 
        if (parent != null) {
            parent.attachChild(model);
 
        }
        model.setLocalTranslation(initPosition);
        model.setLocalRotation(initRotation);
        model.setLocalScale(initScale);
 
        logger.log(Level.INFO, "Created physics ragdoll for skeleton {0}", skeleton);
    }
 
    /**
     * Add a bone name to this control
     * Using this method you can specify which bones of the skeleton will be used to build the collision shapes.
     * @param name
     */
    public void addBoneName(String name) {
        boneList.add(name);
    }
 
    private void scanSpatial(Spatial model) {
        AnimControl animControl = model.getControl(AnimControl.class);
        Map<Integer, List<Float>> pointsMap = null;
        if (weightThreshold == -1.0f) {
            pointsMap = RagdollUtils.buildPointMap(model);
        }
 
        skeleton = animControl.getSkeleton();
        skeleton.resetAndUpdate();
        for (int i = 0; i < skeleton.getRoots().length; i++) {
            Bone childBone = skeleton.getRoots()[i];
            if (childBone.getParent() == null) {
                logger.log(Level.INFO, "Found root bone in skeleton {0}", skeleton);
                baseRigidBody = new PhysicsRigidBody(new BoxCollisionShape(Vector3f.UNIT_XYZ.mult(0.1f)), 1);
                baseRigidBody.setKinematic(mode == Mode.Kinetmatic);
                boneRecursion(model, childBone, baseRigidBody, 1, pointsMap);
            }
        }
    }
 
    private void boneRecursion(Spatial model, Bone bone, PhysicsRigidBody parent, int reccount, Map<Integer, List<Float>> pointsMap) {
        PhysicsRigidBody parentShape = parent;
        if (boneList.isEmpty() || boneList.contains(bone.getName())) {
 
            PhysicsBoneLink link = new PhysicsBoneLink();
            link.bone = bone;
 
            //creating the collision shape
            HullCollisionShape shape = null;
            if (pointsMap != null) {
                //build a shape for the bone, using the vertices that are most influenced by this bone
                shape = RagdollUtils.makeShapeFromPointMap(pointsMap, RagdollUtils.getBoneIndices(link.bone, skeleton, boneList), initScale, link.bone.getModelSpacePosition());
            } else {
                //build a shape for the bone, using the vertices associated with this bone with a weight above the threshold
                shape = RagdollUtils.makeShapeFromVerticeWeights(model, RagdollUtils.getBoneIndices(link.bone, skeleton, boneList), initScale, link.bone.getModelSpacePosition(), weightThreshold);
            }
 
            PhysicsRigidBody shapeNode = new PhysicsRigidBody(shape, rootMass / (float) reccount);
 
            shapeNode.setKinematic(mode == Mode.Kinetmatic);
            totalMass += rootMass / (float) reccount;
 
            link.rigidBody = shapeNode;
            link.initalWorldRotation = bone.getModelSpaceRotation().clone();
 
            if (parent != null) {
                //get joint position for parent
                Vector3f posToParent = new Vector3f();
                if (bone.getParent() != null) {
                    bone.getModelSpacePosition().subtract(bone.getParent().getModelSpacePosition(), posToParent).multLocal(initScale);
                }
 
                SixDofJoint joint = new SixDofJoint(parent, shapeNode, posToParent, new Vector3f(0, 0, 0f), true);
                preset.setupJointForBone(bone.getName(), joint);
 
                link.joint = joint;
                joint.setCollisionBetweenLinkedBodys(false);
            }
            boneLinks.put(bone.getName(), link);
            shapeNode.setUserObject(link);
            parentShape = shapeNode;
        }
 
        for (Iterator<Bone> it = bone.getChildren().iterator(); it.hasNext();) {
            Bone childBone = it.next();
            boneRecursion(model, childBone, parentShape, reccount + 1, pointsMap);
        }
    }
 
    /**
     * Set the joint limits for the joint between the given bone and its parent.
     * This method can't work before attaching the control to a spatial
     * @param boneName the name of the bone
     * @param maxX the maximum rotation on the x axis (in radians)
     * @param minX the minimum rotation on the x axis (in radians)
     * @param maxY the maximum rotation on the y axis (in radians)
     * @param minY the minimum rotation on the z axis (in radians)
     * @param maxZ the maximum rotation on the z axis (in radians)
     * @param minZ the minimum rotation on the z axis (in radians)
     */
    public void setJointLimit(String boneName, float maxX, float minX, float maxY, float minY, float maxZ, float minZ) {
        PhysicsBoneLink link = boneLinks.get(boneName);
        if (link != null) {
            RagdollUtils.setJointLimit(link.joint, maxX, minX, maxY, minY, maxZ, minZ);
        } else {
            logger.log(Level.WARNING, "Not joint was found for bone {0}. make sure you call spatial.addControl(ragdoll) before setting joints limit", boneName);
        }
    }
 
    /**
     * Return the joint between the given bone and its parent.
     * This return null if it's called before attaching the control to a spatial
     * @param boneName the name of the bone
     * @return the joint between the given bone and its parent
     */
    public SixDofJoint getJoint(String boneName) {
        PhysicsBoneLink link = boneLinks.get(boneName);
        if (link != null) {
            return link.joint;
        } else {
            logger.log(Level.WARNING, "Not joint was found for bone {0}. make sure you call spatial.addControl(ragdoll) before setting joints limit", boneName);
            return null;
        }
    }
 
    private void clearData() {
        boneLinks.clear();
        baseRigidBody = null;
    }
 
    private void addToPhysicsSpace() {
        if (space == null) {
            return;
        }
        if (baseRigidBody != null) {
            space.add(baseRigidBody);
            added = true;
        }
        for (Iterator<PhysicsBoneLink> it = boneLinks.values().iterator(); it.hasNext();) {
            PhysicsBoneLink physicsBoneLink = it.next();
            if (physicsBoneLink.rigidBody != null) {
                space.add(physicsBoneLink.rigidBody);
                if (physicsBoneLink.joint != null) {
                    space.add(physicsBoneLink.joint);
 
                }
                added = true;
            }
        }
    }
 
    protected void removeFromPhysicsSpace() {
        if (space == null) {
            return;
        }
        if (baseRigidBody != null) {
            space.remove(baseRigidBody);
        }
        for (Iterator<PhysicsBoneLink> it = boneLinks.values().iterator(); it.hasNext();) {
            PhysicsBoneLink physicsBoneLink = it.next();
            if (physicsBoneLink.joint != null) {
                space.remove(physicsBoneLink.joint);
                if (physicsBoneLink.rigidBody != null) {
                    space.remove(physicsBoneLink.rigidBody);
                }
            }
        }
        added = false;
    }
 
    /**
     * enable or disable the control
     * note that if enabled is true and that the physic space has been set on the ragdoll, the ragdoll is added to the physic space
     * if enabled is false the ragdoll is removed from physic space.
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (!enabled && space != null) {
            removeFromPhysicsSpace();
        } else if (enabled && space != null) {
            addToPhysicsSpace();
        }
    }
 
    /**
     * returns true if the control is enabled
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }
 
    protected void attachDebugShape(AssetManager manager) {
        for (Iterator<PhysicsBoneLink> it = boneLinks.values().iterator(); it.hasNext();) {
            PhysicsBoneLink physicsBoneLink = it.next();
            //physicsBoneLink.rigidBody.createDebugShape(manager);
        }
        debug = true;
    }
 
    protected void detachDebugShape() {
        for (Iterator<PhysicsBoneLink> it = boneLinks.values().iterator(); it.hasNext();) {
            PhysicsBoneLink physicsBoneLink = it.next();
            //physicsBoneLink.rigidBody.detachDebugShape();
        }
        debug = false;
    }
 
    /**
     * For internal use only
     * specific render for the ragdoll(if debugging)
     * @param rm
     * @param vp
     */
    public void render(RenderManager rm, ViewPort vp) {
        /*
        if (enabled && space != null && space.getDebugManager() != null) {
            if (!debug) {
                attachDebugShape(space.getDebugManager());
            }
            for (Iterator<PhysicsBoneLink> it = boneLinks.values().iterator(); it.hasNext();) {
                PhysicsBoneLink physicsBoneLink = it.next();
                Spatial debugShape = physicsBoneLink.rigidBody.debugShape();
                if (debugShape != null) {
                    debugShape.setLocalTranslation(physicsBoneLink.rigidBody.getMotionState().getWorldLocation());
                    debugShape.setLocalRotation(physicsBoneLink.rigidBody.getMotionState().getWorldRotationQuat());
                    debugShape.updateGeometricState();
                    rm.renderScene(debugShape, vp);
                }
            }
        }*/
    }
 
    /**
     * set the physic space to this ragdoll
     * @param space
     */
    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            removeFromPhysicsSpace();
            this.space = space;
        } else {
            if (this.space == space) {
                return;
            }
            this.space = space;
            if(enabled){
                addToPhysicsSpace();
                this.space.addCollisionListener(this);
            }
        }
    }
 
    /**
     * returns the physic space
     * @return
     */
    public PhysicsSpace getPhysicsSpace() {
        return space;
    }
 
    /**
     * serialize this control
     * @param ex
     * @throws IOException
     */
    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 
    /**
     * de-serialize this control
     * @param im
     * @throws IOException
     */
    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
 
    /**
     * For internal use only
     * callback for collisionevent
     * @param event
     */
    public void collision(PhysicsCollisionEvent event) {
        PhysicsCollisionObject objA = event.getObjectA();
        PhysicsCollisionObject objB = event.getObjectB();
 
        //excluding collisions that involve 2 parts of the ragdoll
        if (event.getNodeA() == null && event.getNodeB() == null) {
            return;
        }
 
        //discarding low impulse collision
        if (event.getAppliedImpulse() < eventDispatchImpulseThreshold) {
            return;
        }
 
        boolean hit = false;
        Bone hitBone = null;
        PhysicsCollisionObject hitObject = null;
 
        //Computing which bone has been hit
        if (objA.getUserObject() instanceof PhysicsBoneLink) {
            PhysicsBoneLink link = (PhysicsBoneLink) objA.getUserObject();
            if (link != null) {
                hit = true;
                hitBone = link.bone;
                hitObject = objB;
            }
        }
 
        if (objB.getUserObject() instanceof PhysicsBoneLink) {
            PhysicsBoneLink link = (PhysicsBoneLink) objB.getUserObject();
            if (link != null) {
                hit = true;
                hitBone = link.bone;
                hitObject = objA;
 
            }
        }
 
        //dispatching the event if the ragdoll has been hit
        if (hit) {
            for (RagdollCollisionListener listener : listeners) {
                listener.collide(hitBone, hitObject, event);
            }
        }
 
    }
 
    /**
     * Enable or disable the ragdoll behaviour.
     * if ragdollEnabled is true, the character motion will only be powerd by physics
     * else, the characted will be animated by the keyframe animation,
     * but will be able to physically interact with its physic environnement
     * @param ragdollEnabled
     */
    protected void setMode(Mode mode) {
        this.mode = mode;
        TempVars vars = TempVars.get();
 
        /**/
        if(mode == Mode.IK){
            /*Bone bone = targetBone;
            while(bone.getParent() != null){
 
                Quaternion tmpRot1 = vars.quat1;
                Vector3f position = vars.vect1;
                //making sure that the ragdoll is at the correct place.
                matchPhysicObjectToBone(boneLinks.get(bone.getName()), position, tmpRot1);
                bone.setUserControl(true);
                bone = bone.getParent();
            }*/
        } else {
            for (Bone bone : skeleton.getRoots()) {
                RagdollUtils.setUserControl(bone, mode == Mode.IK);
            }
        }
        vars.release();
    }
 
    /**
     * Smoothly blend from Ragdoll mode to Kinematic mode
     * This is useful to blend ragdoll actual position to a keyframe animation for example
     * @param blendTime the blending time between ragdoll to anim.
     */
    public void blendToKinematicMode(float blendTime) {
        if (mode == Mode.Kinetmatic) {
            return;
        }
        blendedControl = true;
        this.blendTime = blendTime;
        mode = Mode.Kinetmatic;
        AnimControl animControl = targetModel.getControl(AnimControl.class);
        animControl.setEnabled(true);
 
        TempVars vars = TempVars.get();
        for (PhysicsBoneLink link : boneLinks.values()) {
 
            Vector3f p = link.rigidBody.getMotionState().getWorldLocation();
            Vector3f position = vars.vect1;
 
            targetModel.getWorldTransform().transformInverseVector(p, position);
 
            Quaternion q = link.rigidBody.getMotionState().getWorldRotationQuat();
            Quaternion q2 = vars.quat1;
            Quaternion q3 = vars.quat2;
 
            q2.set(q).multLocal(link.initalWorldRotation).normalizeLocal();
            q3.set(targetModel.getWorldRotation()).inverseLocal().mult(q2, q2);
            q2.normalizeLocal();
            link.startBlendingPos.set(position);
            link.startBlendingRot.set(q2);
            link.rigidBody.setKinematic(true);
        }
        vars.release();
 
        for (Bone bone : skeleton.getRoots()) {
            RagdollUtils.setUserControl(bone, false);
        }
 
        blendStart = 0;
    }
 
    /**
     * Set the control into Kinematic mode
     * In theis mode, the collision shapes follow the movements of the skeleton,
     * and can interact with physical environement
     */
    public void setKinematicMode() {
        if (mode != Mode.Kinetmatic) {
            setMode(Mode.Kinetmatic);
        }
    }
 
    /**
     * Sets the control into Ragdoll mode
     * The skeleton is entirely controlled by physics.
     */
    public void setIKMode() {
        if (mode != Mode.IK) {
            setMode(Mode.IK);
        }
    }
 
    /**
     * retruns the mode of this control
     * @return
     */
    public Mode getMode() {
        return mode;
    }
 
    /**
     * add a
     * @param listener
     */
    public void addCollisionListener(RagdollCollisionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<RagdollCollisionListener>();
        }
        listeners.add(listener);
    }
 
    public void setRootMass(float rootMass) {
        this.rootMass = rootMass;
    }
 
    public float getTotalMass() {
        return totalMass;
    }
 
    public float getWeightThreshold() {
        return weightThreshold;
    }
 
    public void setWeightThreshold(float weightThreshold) {
        this.weightThreshold = weightThreshold;
    }
 
    public float getEventDispatchImpulseThreshold() {
        return eventDispatchImpulseThreshold;
    }
 
    public void setEventDispatchImpulseThreshold(float eventDispatchImpulseThreshold) {
        this.eventDispatchImpulseThreshold = eventDispatchImpulseThreshold;
    }
 
    public Bone getBone(String boneName){
        return boneLinks.get(boneName).bone;
    }
 
    /**
     * Set the CcdMotionThreshold of all the bone's rigidBodies of the ragdoll
     * @see PhysicsRigidBody#setCcdMotionThreshold(float)
     * @param value
     */
    public void setCcdMotionThreshold(float value) {
        for (PhysicsBoneLink link : boneLinks.values()) {
            link.rigidBody.setCcdMotionThreshold(value);
        }
    }
 
    /**
     * Set the CcdSweptSphereRadius of all the bone's rigidBodies of the ragdoll
     * @see PhysicsRigidBody#setCcdSweptSphereRadius(float)
     * @param value
     */
    public void setCcdSweptSphereRadius(float value) {
        for (PhysicsBoneLink link : boneLinks.values()) {
            link.rigidBody.setCcdSweptSphereRadius(value);
        }
    }
 
    /**
     * Set the CcdMotionThreshold of the given bone's rigidBodies of the ragdoll
     * @see PhysicsRigidBody#setCcdMotionThreshold(float)
     * @param value
     * @deprecated use getBoneRigidBody(String BoneName).setCcdMotionThreshold(float) instead
     */
    @Deprecated
    public void setBoneCcdMotionThreshold(String boneName, float value) {
        PhysicsBoneLink link = boneLinks.get(boneName);
        if (link != null) {
            link.rigidBody.setCcdMotionThreshold(value);
        }
    }
 
    /**
     * Set the CcdSweptSphereRadius of the given bone's rigidBodies of the ragdoll
     * @see PhysicsRigidBody#setCcdSweptSphereRadius(float)
     * @param value
     * @deprecated use getBoneRigidBody(String BoneName).setCcdSweptSphereRadius(float) instead
     */
    @Deprecated
    public void setBoneCcdSweptSphereRadius(String boneName, float value) {
        PhysicsBoneLink link = boneLinks.get(boneName);
        if (link != null) {
            link.rigidBody.setCcdSweptSphereRadius(value);
        }
    }
 
    /**
     * return the rigidBody associated to the given bone
     * @param boneName the name of the bone
     * @return the associated rigidBody.
     */
    public PhysicsRigidBody getBoneRigidBody(String boneName) {
        PhysicsBoneLink link = boneLinks.get(boneName);
        if (link != null) {
            return link.rigidBody;
        }
        return null;
    }
 
    /*
     * Useful for static targets, or targets that are rarely updated
     *
     */
    public void setTarget(Bone bone, Vector3f worldPoint){
        targets.put(bone, worldPoint.subtract(targetModel.getWorldTranslation()));
        while(bone.getParent() != null){
            Quaternion tmpRot1 = new Quaternion();
            Vector3f position = new Vector3f();
            //if(!bone.hasUserControl()){
                matchPhysicObjectToBone(boneLinks.get(bone.getName()), position, tmpRot1);
                bone.setUserControl(true);
            //}
            bone = bone.getParent();
        }
 
        setIKMode();
    }
 
    public void clearTarget(Bone bone){
        targets.remove(bone);
        applyUserControl();
    }
 
    public void applyUserControl(){
        for (Bone bone : skeleton.getRoots()) {
            RagdollUtils.setUserControl(bone, false);
        }
 
        if(targets.isEmpty()) setKinematicMode();
        else {
        Iterator iterator = targets.keySet().iterator();
 
            TempVars vars = TempVars.get();
 
            while(iterator.hasNext()){
                Bone bone = (Bone) iterator.next();
                while(bone.getParent() != null){
 
                    Quaternion tmpRot1 = vars.quat1;
                    Vector3f position = vars.vect1;
                    matchPhysicObjectToBone(boneLinks.get(bone.getName()), position, tmpRot1);
                    bone.setUserControl(true);
                    bone = bone.getParent();
                }
            }
            vars.release();
        }
    }
 
    public boolean isWithinConstraint(Quaternion boneRot, PhysicsBoneLink link){
        float[] angles = new float[3];
        boneRot.toAngles(angles);
        for(int i = 0; i < 3; i++){
            if      (angles[0] < link.joint.getRotationalLimitMotor(0).getLoLimit()) return false;
            else if (angles[0] > link.joint.getRotationalLimitMotor(0).getHiLimit()) return false;
        }
        return true;
    }
}