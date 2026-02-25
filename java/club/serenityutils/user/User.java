package club.serenityutils.user;

import club.serenityutils.user.api.IUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User implements IUser {
    private String name;
    private int uid;
}
