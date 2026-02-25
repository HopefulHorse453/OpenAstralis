package cc.astralis.friends;

import java.util.ArrayList;

public class FriendManager {
    public ArrayList<String> friends = new ArrayList<>();

    public void addFriend(String name) {
        friends.add(name.toLowerCase());
    }

    public void remove(String name) {
        friends.removeIf(friend -> friend.toLowerCase().contains(name));
    }

    public boolean isFriend(String name) {
        return friends.contains(name.toLowerCase());
    }
}
