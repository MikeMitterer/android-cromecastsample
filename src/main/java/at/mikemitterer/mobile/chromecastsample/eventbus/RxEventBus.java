package at.mikemitterer.mobile.chromecastsample.eventbus;

import at.mikemitterer.mobile.chromecastsample.eventbus.events.AbstractDataEvent;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Intro to RX-Programming
 *      https://goo.gl/3gXmsB
 *
 * Mehr auf:
 *      https://github.com/nuuneoi/RxEventBus
 *      https://github.com/ReactiveX/RxAndroid
 *      https://metova.com/blog/dev/how-to-use-rxjava-as-an-event-bus/
 *      http://goo.gl/GykfsP
 *      http://goo.gl/IqyRMV
 *
 * Slides mit Dagger
 *      http://goo.gl/oFv3VC
 */
public class RxEventBus<T extends AbstractDataEvent> {
    private final Subject<T, T> _bus = new SerializedSubject<>(PublishSubject.<T>create());

    public void fire(final T o) {
        _bus.onNext(o);
    }

    public Observable<T> observe() {
        return _bus;
    }


}
