package cc.astralis.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract /* no need to ever create an instance of this class */ class Command {
    private final String[] expressions;
    private final String description;

    public abstract void execute(final String[] args, final String message);
}
