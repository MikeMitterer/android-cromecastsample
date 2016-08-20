package at.mikemitterer.mobile.chromecastsample.eventbus.events;

/**
 * Versendet im Prinzip den Filenamen
 *
 * Created by mikemitterer on 19.08.16.
 */
public final class MessageEvent extends AbstractDataEvent<String> {
    public MessageEvent(final String message) {
        super(message);
    }
}
