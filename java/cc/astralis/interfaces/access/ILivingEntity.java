package cc.astralis.interfaces.access;


public interface ILivingEntity {
    float serenium_getHeadPitch();

    void serenium_setHeadPitch(float headPitch);

    float serenium_getPrevHeadPitch();

    float serenium_getHeadYaw();

    void serenium_setHeadYaw(float headYaw);

    float serenium_getPrevHeadYaw();

    boolean serenium_isInInventory();
}