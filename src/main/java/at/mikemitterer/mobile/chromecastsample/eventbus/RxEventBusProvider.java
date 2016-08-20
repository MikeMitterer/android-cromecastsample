package at.mikemitterer.mobile.chromecastsample.eventbus;

import at.mikemitterer.mobile.chromecastsample.eventbus.events.AbstractDataEvent;

/**
 * Created by mikemitterer on 02.08.16.
 */
public class RxEventBusProvider {
    private static RxEventBus<AbstractDataEvent> _rxEventBus;

    public static RxEventBus<AbstractDataEvent> get() {
        if(_rxEventBus == null) {
            _rxEventBus = new RxEventBus<>();
        }
        return _rxEventBus;
    }
}