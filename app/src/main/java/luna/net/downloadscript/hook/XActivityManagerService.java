package luna.net.downloadscript.hook;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

@SuppressLint("InlinedApi")
public class XActivityManagerService extends XHook {
	private Methods mMethod;

	private static Semaphore mOndemandSemaphore;
	private static boolean mFinishedBooting = false;
	private static boolean mLockScreen = false;
	private static boolean mSleeping = false;
	private static boolean mShutdown = false;

	private XActivityManagerService(Methods method) {
		super(null, method.name(), null);
		mMethod = method;
	}

	@Override
	public boolean isVisible() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			if (mMethod == Methods.goingToSleep || mMethod == Methods.wakingUp)
				return false;

		return (mMethod != Methods.appNotResponding && mMethod != Methods.finishBooting && mMethod != Methods.updateSleepIfNeededLocked);
	}

	public String getClassName() {
		return "com.android.server.am.ActivityManagerService";
	}

	// @formatter:off

	// 4.2+ public long inputDispatchingTimedOut(int pid, final boolean aboveSystem, String reason)
	// 4.3+ public boolean inputDispatchingTimedOut(final ProcessRecord proc, final ActivityRecord activity, final ActivityRecord parent, final boolean aboveSystem, String reason)
	// 4.0.3+ final void appNotResponding(ProcessRecord app, ActivityRecord activity, ActivityRecord parent, boolean aboveSystem, final String annotation)
	// 4.0.3+ public void systemReady(final Runnable goingCallback)
	// 4.0.3+ final void finishBooting()
	// 4.1+ public void setLockScreenShown(boolean shown)
	// 4.0.3-5.0.x public void goingToSleep()
	// 4.0.3-5.0.x public void wakingUp()
	// 4.0.3+ public boolean shutdown(int timeout)
	// 4.2+ public final void activityResumed(IBinder token)
	// public final void activityPaused(IBinder token)
	// http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.0.2_r1/com/android/server/am/ActivityManagerService.java/
	// http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.0.2_r1/com/android/server/am/ActivityRecord.java/

	// @formatter:on

	// @formatter:off
	private enum Methods {
		inputDispatchingTimedOut, appNotResponding,
		systemReady, finishBooting, setLockScreenShown, goingToSleep, wakingUp, shutdown,
		updateSleepIfNeededLocked
	};
	// @formatter:on

	public static List<XHook> getInstances() {
		List<XHook> listHook = new ArrayList<XHook>();
		listHook.add(new XActivityManagerService(Methods.inputDispatchingTimedOut));
		listHook.add(new XActivityManagerService(Methods.appNotResponding));
		listHook.add(new XActivityManagerService(Methods.systemReady));
		listHook.add(new XActivityManagerService(Methods.finishBooting));
		// setLockScreenShown appears to be not present in some 4.2.2 ROMs
		listHook.add(new XActivityManagerService(Methods.setLockScreenShown));
		listHook.add(new XActivityManagerService(Methods.goingToSleep));
		listHook.add(new XActivityManagerService(Methods.wakingUp));
		listHook.add(new XActivityManagerService(Methods.shutdown));
		listHook.add(new XActivityManagerService(Methods.updateSleepIfNeededLocked));
		return listHook;
	}

	public static void setSemaphore(Semaphore semaphore) {
		mOndemandSemaphore = semaphore;
	}

	public static boolean canOnDemand() {
		return (mFinishedBooting && !mLockScreen && !mSleeping);
	}

	public static boolean canWriteUsageData() {
		return !mShutdown;
	}

	@Override
	public void before(XParam param) throws Throwable {
		switch (mMethod) {
		case inputDispatchingTimedOut:
			// Delay foreground ANRs while on demand dialog open
			if (mOndemandSemaphore != null && mOndemandSemaphore.availablePermits() < 1) {
				param.setResult(5 * 1000); // 5 seconds
			}
			break;

		case appNotResponding:
			// Ignore background ANRs while on demand dialog open
			if (mOndemandSemaphore != null && mOndemandSemaphore.availablePermits() < 1) {
				param.setResult(null);
			}
			break;

		case systemReady:
			// Do nothing
			break;

		case finishBooting:
			// Do nothing
			break;

		case setLockScreenShown:
			if (param.args.length > 0 && param.args[0] instanceof Boolean)
				try {
					if ((Boolean) param.args[0]) {
						mLockScreen = true;
					}
				} catch (Throwable ex) {
				}
			break;

		case goingToSleep:
			mSleeping = true;
			break;

		case wakingUp:
			// Do nothing
			break;

		case shutdown:
			mShutdown = true;
			break;

		case updateSleepIfNeededLocked:
			// Do nothing;
			break;
		}
	}

	@Override
	public void after(XParam param) throws Throwable {
		switch (mMethod) {
		case inputDispatchingTimedOut:
		case appNotResponding:
			break;

		case systemReady:
			// Do nothing
			break;

		case finishBooting:
			mFinishedBooting = true;
			break;

		case setLockScreenShown:
			if (param.args.length > 0 && param.args[0] instanceof Boolean)
				if (!(Boolean) param.args[0]) {
					mLockScreen = false;
				}
			break;

		case goingToSleep:
			// Do nothing
			break;

		case wakingUp:
			mSleeping = false;
			break;

		case shutdown:
			// Do nothing
			break;

		case updateSleepIfNeededLocked:
			if (param.thisObject != null) {
				Field methodSleeping = param.thisObject.getClass().getDeclaredField("mSleeping");
				methodSleeping.setAccessible(true);
				mSleeping = (Boolean) methodSleeping.get(param.thisObject);
			}
			break;
		}
	}

	// Helper methods

	private int getUidANR(XParam param) throws IllegalAccessException {
		int uid = -1;
		try {
			Class<?> pr = Class.forName("com.android.server.am.ProcessRecord");
			if (param.args.length > 0 && param.args[0] != null && param.args[0].getClass().equals(pr)) {
				Field fUid = pr.getDeclaredField("uid");
				fUid.setAccessible(true);
				uid = (Integer) fUid.get(param.args[0]);
			}
		} catch (ClassNotFoundException ignored) {
		} catch (NoSuchFieldException ignored) {
		} catch (Throwable ex) {
		}
		return uid;
	}
}
