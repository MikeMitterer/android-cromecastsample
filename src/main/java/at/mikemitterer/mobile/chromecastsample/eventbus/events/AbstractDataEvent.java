package at.mikemitterer.mobile.chromecastsample.eventbus.events;

/**
 * Created by mikemitterer on 19.08.16.
 */

public abstract class AbstractDataEvent<T> {
    private final T data;

    public AbstractDataEvent(final T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
