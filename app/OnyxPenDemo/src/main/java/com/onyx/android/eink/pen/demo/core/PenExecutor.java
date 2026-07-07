package com.onyx.android.eink.pen.demo.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.rx.RxManager;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.utils.ResManager;

import io.reactivex.Observable;

/**
 * Serial pen-thread work queue. All bitmap / TouchHelper / shape mutations should go through here
 * so tasks do not race across independent schedulers.
 */
public final class PenExecutor {

    @FunctionalInterface
    public interface PenTask {
        void run(@NonNull PenManager penManager) throws Exception;
    }

    private final PenManager penManager;
    private final RxManager rxManager;

    private PenExecutor(@NonNull PenManager penManager, @NonNull RxManager rxManager) {
        this.penManager = penManager;
        this.rxManager = rxManager;
    }

    @NonNull
    public static PenExecutor create(@NonNull PenManager penManager) {
        return new PenExecutor(penManager, RxManager.Builder.sharedSingleThreadManager());
    }

    @NonNull
    public PenManager getPenManager() {
        return penManager;
    }

    @NonNull
    public RxManager getRxManager() {
        return rxManager;
    }

    public void submit(@NonNull PenTask task) {
        submit(task, null);
    }

    public void submit(@NonNull PenTask task, @Nullable Runnable onPenThreadComplete) {
        rxManager.enqueue(wrap(task), onPenThreadComplete == null ? null : new RxCallback<RxRequest>() {
            @Override
            public void onNext(@NonNull RxRequest request) {
                onPenThreadComplete.run();
            }
        });
    }

    @NonNull
    public Observable<PenManager> submitObservable(@NonNull PenTask task) {
        return Observable.create(emitter -> rxManager.enqueue(wrap(task), new RxCallback<RxRequest>() {
            @Override
            public void onNext(@NonNull RxRequest request) {
                if (!emitter.isDisposed()) {
                    emitter.onNext(penManager);
                    emitter.onComplete();
                }
            }
        }));
    }

    public void submitRequest(@NonNull RxRequest request) {
        rxManager.enqueue(request, null);
    }

    public void submitRequest(@NonNull RxRequest request, @Nullable RxCallback<RxRequest> callback) {
        rxManager.enqueue(request, callback);
    }

    @NonNull
    private RxRequest wrap(@NonNull PenTask task) {
        return new RxRequest() {
            @Override
            public void execute() throws Exception {
                setContext(ResManager.getAppContext());
                task.run(penManager);
            }
        };
    }
}
