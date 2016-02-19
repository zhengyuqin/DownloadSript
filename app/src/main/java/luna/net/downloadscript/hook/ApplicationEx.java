package luna.net.downloadscript.hook;

import android.app.Application;

public class ApplicationEx extends Application {
    private Thread.UncaughtExceptionHandler mPrevHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        mPrevHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                if (mPrevHandler != null)
                    mPrevHandler.uncaughtException(thread, ex);
            }
        });
    }

}
