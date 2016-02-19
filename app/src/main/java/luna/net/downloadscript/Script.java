package luna.net.downloadscript;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.luna.common.util.PackageUtils;
import net.luna.common.util.ShellUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bintou on 15/12/15.
 */
public class Script implements Runnable {

    public static final String QIHOO_PN = "com.qihoo.appstore";

    public static final String BAIDU_PN = "com.baidu.appsearch";

    public static final String SPEEDDOG_PN = "android.luna.net.videohelptools";

    public static final String KU_PN = "com.coolapk.market";

    public static final String NAME_MARKET = "酷市场";

    public static final String TARGET_PATH = "/data/data/" + KU_PN;

        public static final String NAME_APK = "ku.apk";
//    public static final String NAME_APK = "baidu.apk";


    private Context context;
    public int curCount = 0;
    private Handler handler;

    public Script(Context context, int loopCount, Handler handler) {
        this.context = context;
        this.loopCount = loopCount;
        this.handler = handler;

    }

    public int loopCount = 0;

    private void startProcess() {
        try {
            ArrayList<String> pns = new ArrayList<>();
            List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

            for (int j = 0; j < packages.size(); j++) {
                PackageInfo packageInfo = packages.get(j);
                pns.add(packageInfo.packageName);
            }
            int speedDogDelete, deleteResult;
//        if (pns.contains(BAIDU_PN)) {
//            baiduDelete = PackageUtils.uninstallSilent(context, BAIDU_PN);
//            print("delete 百度 result : " + baiduDelete);
//        }

            if (pns.contains(SPEEDDOG_PN)) {
                speedDogDelete = PackageUtils.uninstallSilent(context, SPEEDDOG_PN);
                print("delete 加速狗 result : " + speedDogDelete);
            }
            if (pns.contains(KU_PN)) {
                deleteResult = PackageUtils.uninstallSilent(context, KU_PN);
                print("delete " + NAME_MARKET + " result : " + deleteResult);
            }

            print("delete cool cache file");
            String cachePath = TARGET_PATH;
            ShellUtils.CommandResult result = ShellUtils.execCommand("rm -r " + cachePath + " \n", true);
            print(result.errorMsg);
            print(result.errorMsg);


            print("start install package");
//        String baiduPath = Environment.getExternalStorageDirectory() + "/scriptApk/baidu.apk";
//        int baiduInstall = PackageUtils.installSilent(context, baiduPath);
//        print("install 百度 result : " + baiduInstall);


            String kuPath = Environment.getExternalStorageDirectory() + "/scriptApk/" +NAME_APK;
            int kuInstall = PackageUtils.installSilent(context, kuPath);
            print("install "+NAME_MARKET+" result : " + kuInstall);
            GlobalSwitchs.downloadStatus = GlobalSwitchs.DownloadStatus.prepareDownload;
            Intent viewIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse("market://details?id=" + SPEEDDOG_PN));
            context.startActivity(viewIntent);
            curCount++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(String str) {
        Log.d("script",str);
        Message msg = new Message();
        msg.obj = str + " \n";
        if (handler != null)
            handler.sendMessage(msg);
    }

    public void changeDeviceParameters() {

    }

    public boolean canLoopScript() {
        print("resume activity");
        print("执行循环第 " + curCount + "次");
        if (curCount != 0 && curCount < loopCount) {
            return true;
        }
        if (curCount == loopCount) {
            print("执行完毕");
        }
        return false;
    }

    @Override
    public void run() {
        startProcess();
    }
}
