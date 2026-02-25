package club.serenityutils.clientprofile;

import club.serenityutils.clientprofile.api.IClientProfileMeta;

public class ClientProfileMeta implements IClientProfileMeta {
    private final ClientProfileType type;
    private final String hash;
    private double version;

    public ClientProfileMeta(ClientProfileType type, double version) {
        this.type = type;
        this.hash = "043bffec6ac86cbf6d926620cf7248f4fc6bf02c41fd491109ea11cacfdf5c445c4f4fac7188c4f6c26c3abb15a878b2deeb6800650f76440e5fbe13b27c9e7e";
        this.version = version;
    }

    @Override
    public boolean equalsTo(ClientProfileMeta other) {
        if (other == null) return false;

        return this.type == other.type &&
                Double.compare(this.version, other.version) == 0 &&
                this.hash.equals(other.hash);
    }

    @Override
    public ClientProfileType getType() {
        return type;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public double getVersion() {
        return version;
    }

    @Override
    public void setVersion(double version) {
        this.version = version;
    }
}
