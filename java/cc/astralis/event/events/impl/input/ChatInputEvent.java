package cc.astralis.event.events.impl.input;

import cc.astralis.event.events.callables.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatInputEvent extends EventCancellable {
    private String input;
}
