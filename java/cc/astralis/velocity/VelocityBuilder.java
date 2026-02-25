package cc.astralis.velocity;

import net.minecraft.world.phys.Vec3;

/**
 *
 * @author Kawase
 * @since 01.10.2025
 */
public class VelocityBuilder {
    private double x, y, z;

    private VelocityBuilder(Vec3 base) {
        this.x = base.x;
        this.y = base.y;
        this.z = base.z;
    }

    public static VelocityBuilder from(Vec3 base) {
        return new VelocityBuilder(base);
    }

    public VelocityBuilder setVelocityX(double x) {
        this.x = x;
        return this;
    }

    public VelocityBuilder setVelocityY(double y) {
        this.y = y;
        return this;
    }

    public VelocityBuilder setVelocityZ(double z) {
        this.z = z;
        return this;
    }

    public VelocityBuilder scale(double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        return this;
    }

    public Vec3 build() {
        return new Vec3(x, y, z);
    }
}
