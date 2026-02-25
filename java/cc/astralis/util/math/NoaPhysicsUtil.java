package cc.astralis.util.math;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class NoaPhysicsUtil {
    public Vector3d impulseVector = new Vector3d(0.0, 0.0, 0.0);
    public Vector3d forceVector = new Vector3d(0.0, 0.0, 0.0);
    public Vector3d velocityVector = new Vector3d(0.0, 0.0, 0.0);
    public Vector3d gravityVector = new Vector3d(0.0, -10.0, 0.0);
    public double gravityMul = 2.0;
    public final double mass = 1.0;
    private final double delta = 0.03333333333333333;

    public Vector3d getMotionForTick() {
        double massDiv = 1.0;
        this.forceVector.mul(massDiv);
        this.forceVector.add((Vector3dc)this.gravityVector);
        this.forceVector.mul(this.gravityMul);
        this.impulseVector.mul(massDiv);
        this.forceVector.mul(this.delta);
        this.impulseVector.add((Vector3dc)this.forceVector);
        this.velocityVector.add((Vector3dc)this.impulseVector);
        this.forceVector.set(0.0, 0.0, 0.0);
        this.impulseVector.set(0.0, 0.0, 0.0);
        return this.velocityVector;
    }
}
