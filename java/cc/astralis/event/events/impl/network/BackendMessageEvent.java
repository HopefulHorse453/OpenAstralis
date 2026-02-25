package cc.astralis.event.events.impl.network;

import cc.astralis.event.events.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BackendMessageEvent implements Event {
    private final String message;
}
