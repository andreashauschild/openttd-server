package de.litexo.events;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andreas Hauschild
 */
@ApplicationScoped
public class EventBus {

    private Map<String, PublishSubject> subjects = new HashMap<>();
    private Map<Object, CompositeDisposable> subscriptions = new HashMap<>();
    private Scheduler subScribeOn;
    private Scheduler observeOn;

    @PostConstruct
    void init() {
        subScribeOn = Schedulers.newThread();
        observeOn = Schedulers.newThread();
    }

    private synchronized <T extends BaseEvent> PublishSubject<T> getSubject(Class<T> clazz) {
        initSubject(clazz);
        return this.subjects.get(clazz.getCanonicalName());
    }

    private synchronized <T> Observable<Object> getCastableSubject(Class<T> clazz) {
        initSubject(clazz);
        return this.subjects.get(clazz.getCanonicalName()).observeOn(observeOn);

    }

    private synchronized <T> void initSubject(Class<T> clazz) {
        PublishSubject<T> subject = subjects.get(clazz.getCanonicalName());
        if (subject == null) {
            subject = PublishSubject.create();
            subject.subscribeOn(this.subScribeOn);
            subjects.put(clazz.getCanonicalName(), subject);
        }

    }


    private CompositeDisposable getCompositeDisposable(Object object) {
        CompositeDisposable compositeDisposable = subscriptions.get(object);
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
            subscriptions.put(object, compositeDisposable);
        }
        return compositeDisposable;
    }

    public <T extends BaseEvent> void observe(Class<T> clazz, @NonNull Object subscribingInstance, @NonNull Consumer<T> action) {
        Disposable disposable = getSubject(clazz).subscribe(action);
        getCompositeDisposable(subscribingInstance).add(disposable);
    }


    /**
     * Method which supports more customization of observer
     * Disposable subscribe = eventBus.observe(ScreenshotEvent.class).throttleLatest(configService.getConfig().getVisualProcessingIntervall(), TimeUnit.MILLISECONDS).subscribe(i -> this.process(i.getMessage()));
     * eventBus.addDisposable(subscribe, this);
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Observable<T> observe(Class<T> clazz) {
        return (Observable<T>) getCastableSubject(clazz).observeOn(observeOn);
    }


    public void addDisposable(Disposable disposable, @NonNull Object subscribingInstance) {
        getCompositeDisposable(subscribingInstance).add(disposable);
    }

    public void unregister(@NonNull Object subscribingInstance) {
        CompositeDisposable compositeDisposable = subscriptions.remove(subscribingInstance);
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public void publish(@NonNull BaseEvent message) {
        getSubjectTypless(message.getClass()).onNext(message);
    }

    private PublishSubject<Object> getSubjectTypless(Class clazz) {
        PublishSubject<Object> subject = subjects.get(clazz.getCanonicalName());
        if (subject == null) {
            subject = PublishSubject.create();
            subject.subscribeOn(this.subScribeOn);
            subjects.put(clazz.getCanonicalName(), subject);
        }
        return subject;
    }


    public enum Events {
        SCREENSHOT_TAKEN
    }
}


