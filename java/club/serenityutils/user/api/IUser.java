package club.serenityutils.user.api;

public interface IUser {
    String getName();
    int getUid();

    void setName(String name);
    void setUid(int uid);
}
