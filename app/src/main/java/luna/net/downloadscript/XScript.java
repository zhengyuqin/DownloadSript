package luna.net.downloadscript;

import android.os.Build;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.luna.common.debug.LunaLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import luna.net.downloadscript.bean.XDeviceInfo;
import luna.net.downloadscript.hook.CRestriction;
import luna.net.downloadscript.hook.Hook;
import luna.net.downloadscript.hook.PrivacyManager;
import luna.net.downloadscript.hook.PrivacyService;
import luna.net.downloadscript.hook.Util;
import luna.net.downloadscript.hook.XHook;
import luna.net.downloadscript.hook.XPackageManager;
import luna.net.downloadscript.hook.XParam;
import luna.net.downloadscript.hook.XTelephonyManager;
import luna.net.downloadscript.hook.XUtilHook;

import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by bintou on 15/12/16.
 */
public class XScript implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static String mSecret = null;
    private static List<String> mListHookError = new ArrayList<String>();
    private static List<CRestriction> mListDisabled = new ArrayList<CRestriction>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LunaLog.d("pn:" + loadPackageParam.packageName);
        if (Util.hasLBE())//不能有LBE
            return;

        if (loadPackageParam.packageName.equals("com.coolapk.market")) {
            hookPackage(loadPackageParam.packageName, loadPackageParam.classLoader);
        }
    }

    private void hookPackage(String packageName, ClassLoader classLoader) {
        LunaLog.d("Hooking package=" + packageName);

        // Skip hooking self
        String self = XScript.class.getPackage().getName();
        if (packageName.equals(self)) {
            hookAll(XUtilHook.getInstances(), classLoader, mSecret, false);
            return;
        }

        // Build SERIAL
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || Process.myUid() != Process.SYSTEM_UID)
            if (PrivacyManager.getRestrictionExtra(null, Process.myUid(), PrivacyManager.cIdentification, "SERIAL",
                    null, Build.SERIAL, mSecret))
                try {
                    Field serial = Build.class.getField("SERIAL");
                    serial.setAccessible(true);
                    serial.set(null, PrivacyManager.getDefacedProp(Process.myUid(), "SERIAL"));
                } catch (Throwable ex) {
                    LunaLog.e(ex);
                }


        // Phone interface manager
//        if ("com.android.phone".equals(packageName)) {
//            hookAll(XTelephonyManager.getPhoneInstances(), classLoader, mSecret, false);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                hookAll(XTelephonyManager.getInstances(null, true), classLoader, mSecret, false);
//        }


//        XposedHelpers.findAndHookMethod(TelephonyManager.class, "getDeviceId", new XC_MethodReplacement() {
//            @Override
//            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                return "this is imei";
//            }
//        });


        XposedHelpers.findAndHookMethod(TelephonyManager.class, "getDeviceId", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                String imei = getImei();
                LunaLog.d("return imei: " + imei);
                return imei;
            }
        });

//        XposedHelpers.findAndHookMethod(Build.class, "getString", new XC_MethodReplacement() {
//            @Override
//            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                LunaLog.e("hooking getString");
//
//                for (Object oj : param.args) {
//                    LunaLog.e(oj.getClass().getName());
//                }
//                return param.getResult();
//
//            }
//        });

        XDeviceInfo info = getRandomDeviceinfo();

        LunaLog.d("change fied");
//        XposedHelpers.setStaticObjectField(Build.class, "BRAND", info.BRAND);
//        XposedHelpers.setStaticObjectField(Build.class, "MANUFACTURER", info.MANUFACTURER);
//        XposedHelpers.setStaticObjectField(Build.class, "MODEL", info.MODEL);
//        XposedHelpers.setStaticIntField(Build.VERSION.class, "SDK_INT", info.SDK_INT);
//        XposedHelpers.setStaticObjectField(Build.VERSION.class, "RELEASE", info.RELEASE);

        try {
            Field model = Build.class.getField("MODEL");
            model.setAccessible(true);
            model.set(null, info.MODEL);

            Field brand = Build.class.getField("BRAND");
            brand.setAccessible(true);
            brand.set(null, info.BRAND);

            Field man = Build.class.getField("MANUFACTURER");
            man.setAccessible(true);
            man.set(null, info.MANUFACTURER);

            Field realse = Build.VERSION.class.getField("RELEASE");
            realse.setAccessible(true);
            realse.set(null, info.RELEASE);

            Field sdk = Build.VERSION.class.getField("SDK_INT");
            sdk.setAccessible(true);
            sdk.set(null, info.SDK_INT);

        } catch (Throwable ex) {
            LunaLog.e(ex);
        }

//        Settings.System.getString(.getContentResolver(), "android_id");
    }

    private final int[] SDK_INTS = {15, 16, 17, 18, 19, 20, 21};
    private final String[] RRELEASES = {"4.0.1", "4.1.1", "4.2.2", "4.3.1", "4.4.4", "5.0.1", "5.1.1"};
    private final String[] MANUFACTURERS = {"HUAWEI", "XIAOMMI", "MEIZU", "SAMSUNG", "LENOVO", "VIVO", "SAMSUNG", "LENOVO", "XIAOMMI"};
    private final String[] MODELS = {"HUAWEI B199", "MI 4LTE", "meizu mx4 ", "sm-i9300", "Lenvov a378t", "vivo Y13iL", "SM-G920P", "Lenovo A630T", "HM_NOTE 1S"};

    private XDeviceInfo getRandomDeviceinfo() {
        XDeviceInfo info = new XDeviceInfo();
        try {
            Random r = new Random();
            int sdkint = r.nextInt(SDK_INTS.length);
            info.SDK_INT = SDK_INTS[sdkint];
            info.RELEASE = RRELEASES[sdkint];
            int brand = r.nextInt(MANUFACTURERS.length);
            info.MANUFACTURER = MANUFACTURERS[brand];
            info.BRAND = MANUFACTURERS[brand];
            info.MODEL = MODELS[brand];
        } catch (Exception e) {

        }
        return info;
    }


    private String getImei() {
        Random r = new Random();
        // http://en.wikipedia.org/wiki/Reporting_Body_Identifier
        String[] rbi = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "30", "33",
                "35", "44", "45", "49", "50", "51", "52", "53", "54", "86", "91", "98", "99"};
        String imei = rbi[r.nextInt(rbi.length)];
        while (imei.length() < 14)
            imei += Character.forDigit(r.nextInt(10), 10);
        imei += getLuhnDigit(imei);
        return imei;
    }

    private static char getLuhnDigit(String x) {
        // http://en.wikipedia.org/wiki/Luhn_algorithm
        int sum = 0;
        for (int i = 0; i < x.length(); i++) {
            int n = Character.digit(x.charAt(x.length() - 1 - i), 10);
            if (i % 2 == 0) {
                n *= 2;
                if (n > 9)
                    n -= 9; // n = (n % 10) + 1;
            }
            sum += n;
        }
        return Character.forDigit((sum * 9) % 10, 10);
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        LunaLog.d("initZygote");
        boolean selinux = Util.isSELinuxEnforced();
        if ("true".equals(Util.getXOption("ignoreselinux"))) {
            selinux = false;
            Log.w("Xprivacy", "Ignoring SELinux");
        }
        mSecret = Long.toHexString(new Random().nextLong());

		/*
         * ActivityManagerService is the beginning of the main "android"
		 * process. This is where the core java system is started, where the
		 * system context is created and so on. In pre-lollipop we can access
		 * this class directly, but in lollipop we have to visit ActivityThread
		 * first, since this class is now responsible for creating a class
		 * loader that can be used to access ActivityManagerService. It is no
		 * longer possible to do so via the normal boot class loader. Doing it
		 * like this will create a consistency between older and newer Android
		 * versions.
		 *
		 * Note that there is no need to handle arguments in this case. And we
		 * don't need them so in case they change over time, we will simply use
		 * the hookAll feature.
		 */

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Class<?> at = Class.forName("android.app.ActivityThread");
                XposedBridge.hookAllMethods(at, "systemMain", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                            Class<?> am = Class.forName("com.android.server.am.ActivityManagerService", false, loader);
                            XposedBridge.hookAllConstructors(am, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    try {
                                        PrivacyService.register(mListHookError, loader, mSecret, param.thisObject);
                                        hookSystem(loader);
                                    } catch (Throwable ex) {
                                        LunaLog.e(ex);
                                    }
                                }
                            });
                        } catch (Throwable ex) {
                            LunaLog.e(ex);
                        }
                    }
                });

            } else {
                Class<?> cSystemServer = Class.forName("com.android.server.SystemServer");
                Method mMain = cSystemServer.getDeclaredMethod("main", String[].class);
                XposedBridge.hookMethod(mMain, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            PrivacyService.register(mListHookError, null, mSecret, null);
                        } catch (Throwable ex) {
                            LunaLog.e(ex);
                        }
                    }
                });
            }


            hookZygote();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                hookSystem(null);
        } catch (Throwable ex) {
            LunaLog.e(ex);
        }
    }


    private void hookZygote() throws Throwable {
        LunaLog.d("Hooking Zygote");

		/*
         * Add nixed User Space / System Server hooks
		 */
// Package manager service
        hookAll(XPackageManager.getInstances(null, false), null, mSecret, false);

        // Telephone service
        hookAll(XTelephonyManager.getInstances(null, false), null, mSecret, false);


    }

    private void hookSystem(ClassLoader classLoader) throws Throwable {
        Log.w("XPrivacy", "Hooking system");

		/*
         * Add nixed User Space / System Server hooks
		 */

        // Package manager service
        hookAll(XPackageManager.getInstances(null, false), null, mSecret, false);

        // Telephone service
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            hookAll(XTelephonyManager.getInstances(null, true), classLoader, mSecret, false);
        hookAll(XTelephonyManager.getRegistryInstances(), classLoader, mSecret, false);

    }


    public static void hookAll(List<XHook> listHook, ClassLoader classLoader, String secret, boolean dynamic) {
        for (XHook hook : listHook)
            if (hook.getRestrictionName() == null)
                hook(hook, classLoader, secret);
            else {
                CRestriction crestriction = new CRestriction(0, hook.getRestrictionName(), null, null);
                CRestriction mrestriction = new CRestriction(0, hook.getRestrictionName(), hook.getMethodName(), null);
                if (mListDisabled.contains(crestriction) || mListDisabled.contains(mrestriction)) {
                } else {
                    hook(hook, classLoader, secret);
                }
            }
    }

    private static void hook(final XHook hook, ClassLoader classLoader, String secret) {
        // Get meta data
        Hook md = PrivacyManager.getHook(hook.getRestrictionName(), hook.getSpecifier());
        if (md == null) {
            String message = "Not found hook=" + hook;
            mListHookError.add(message);
        } else if (!md.isAvailable())
            return;

        // Provide secret
        if (secret == null)
            LunaLog.d("Secret missing hook=" + hook);
        hook.setSecret(secret);

        try {
            // Find class
            Class<?> hookClass = null;
            try {
                hookClass = findClass(hook.getClassName(), classLoader);
            } catch (Throwable ex) {
                String message = "Class not found hook=" + hook;
                int level = (md != null && md.isOptional() ? Log.WARN : Log.ERROR);
                if ("isXposedEnabled".equals(hook.getMethodName()))
                    level = Log.WARN;
                if (level == Log.ERROR)
                    mListHookError.add(message);
                return;
            }

            // Get members
            List<Member> listMember = new ArrayList<Member>();
            List<Class<?>[]> listParameters = new ArrayList<Class<?>[]>();
            Class<?> clazz = hookClass;
            while (clazz != null && !"android.content.ContentProvider".equals(clazz.getName()))
                try {
                    if (hook.getMethodName() == null) {
                        for (Constructor<?> constructor : clazz.getDeclaredConstructors())
                            if (!Modifier.isAbstract(constructor.getModifiers())
                                    && Modifier.isPublic(constructor.getModifiers()) ? hook.isVisible() : !hook
                                    .isVisible())
                                listMember.add(constructor);
                        break;
                    } else {
                        for (Method method : clazz.getDeclaredMethods())
                            if (method.getName().equals(hook.getMethodName())
                                    && !Modifier.isAbstract(method.getModifiers())
                                    && (Modifier.isPublic(method.getModifiers()) ? hook.isVisible() : !hook.isVisible())) {

                                // Check for same function in sub class
                                boolean different = true;
                                for (Class<?>[] parameters : listParameters) {
                                    boolean same = (parameters.length == method.getParameterTypes().length);
                                    for (int p = 0; same && p < parameters.length; p++)
                                        if (!parameters[p].equals(method.getParameterTypes()[p])) {
                                            same = false;
                                            break;
                                        }
                                    if (same) {
                                        different = false;
                                        break;
                                    }
                                }

                                if (different) {
                                    listMember.add(method);
                                    listParameters.add(method.getParameterTypes());
                                }
                            }
                    }
                    clazz = clazz.getSuperclass();
                } catch (Throwable ex) {
                    if (ex.getClass().equals(ClassNotFoundException.class)
                            || ex.getClass().equals(NoClassDefFoundError.class))
                        break;
                    else
                        throw ex;
                }

            // Hook members
            for (Member member : listMember)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        if ((member.getModifiers() & Modifier.NATIVE) != 0)
                            LunaLog.d("Native method=" + member);
                    XposedBridge.hookMethod(member, new XMethodHook(hook));
                } catch (NoSuchFieldError ex) {
                    LunaLog.d(ex.toString());
                } catch (Throwable ex) {
                    mListHookError.add(ex.toString());
                }

            // Check if members found
            if (listMember.isEmpty() && !hook.getClassName().startsWith("com.google.android.gms")) {
                String message = "Method not found hook=" + hook;
                int level = (md != null && md.isOptional() ? Log.WARN : Log.ERROR);
                if ("isXposedEnabled".equals(hook.getMethodName()))
                    level = Log.WARN;
                if (level == Log.ERROR)
                    mListHookError.add(message);
            }
        } catch (Throwable ex) {
            mListHookError.add(ex.toString());
        }
    }

    // Helper classes

    private static class XMethodHook extends XC_MethodHook {
        private XHook mHook;

        public XMethodHook(XHook hook) {
            mHook = hook;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                // Do not restrict Zygote
                if (android.os.Process.myUid() <= 0)
                    return;

                // Pre processing
                XParam xparam = XParam.fromXposed(param);

                long start = System.currentTimeMillis();

                // Execute hook
                mHook.before(xparam);

                long ms = System.currentTimeMillis() - start;
                if (ms > PrivacyManager.cWarnHookDelayMs)
                    LunaLog.d(String.format("%s %d ms", param.method.getName(), ms));

                // Post processing
                if (xparam.hasResult())
                    param.setResult(xparam.getResult());
                if (xparam.hasThrowable())
                    param.setThrowable(xparam.getThrowable());
                param.setObjectExtra("xextra", xparam.getExtras());
            } catch (Throwable ex) {
                LunaLog.e(ex);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            if (!param.hasThrowable())
                try {
                    // Do not restrict Zygote
                    if (Process.myUid() <= 0)
                        return;

                    // Pre processing
                    XParam xparam = XParam.fromXposed(param);
                    xparam.setExtras(param.getObjectExtra("xextra"));

                    long start = System.currentTimeMillis();

                    // Execute hook
                    mHook.after(xparam);

                    long ms = System.currentTimeMillis() - start;
                    if (ms > PrivacyManager.cWarnHookDelayMs)
                        LunaLog.d(String.format("%s %d ms", param.method.getName(), ms));

                    // Post processing
                    if (xparam.hasResult())
                        param.setResult(xparam.getResult());
                    if (xparam.hasThrowable())
                        param.setThrowable(xparam.getThrowable());
                } catch (Throwable ex) {
                    LunaLog.e(ex);
                }
        }
    }
}
