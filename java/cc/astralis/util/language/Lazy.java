package cc.astralis.util.language;

import com.google.common.base.Suppliers;

import java.util.Objects;
import java.util.function.Supplier;

public class Lazy<T> {
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> delegate) {
        Objects.requireNonNull(delegate);
        this.supplier = Suppliers.memoize(delegate::get);
    }

    public T get() {
        return (T)this.supplier.get();
    }

    public static <T> Lazy<T> of(Supplier<T> delegate) {
        return new Lazy<>(delegate);
    }
}