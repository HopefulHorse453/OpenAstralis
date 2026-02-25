package cc.astralis.property;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

@Getter
@Setter
public class Property<P> {
    //property attributes
    private final String name;
    private Supplier<Boolean> visible = () -> true;

    private P property;

    public Property(String name, P property) {
        this.name = name;
        this.property = property;
    }

    @SuppressWarnings("unchecked")
    public <P extends Property> P setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
        return (P) this;
    }
}
