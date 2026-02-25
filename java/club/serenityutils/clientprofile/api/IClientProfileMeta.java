package club.serenityutils.clientprofile.api;

import club.serenityutils.clientprofile.ClientProfileMeta;
import club.serenityutils.clientprofile.ClientProfileType;

public interface IClientProfileMeta {
    boolean equalsTo(ClientProfileMeta other);

    ClientProfileType getType();
    String getHash();

    double getVersion();
    void setVersion(double version);
}
