
package luna.net.downloadscript.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import net.luna.common.debug.LunaLog;
import net.luna.common.util.ShellUtils;

import luna.net.downloadscript.GlobalSwitchs;
import luna.net.downloadscript.Script;

public class PackageChange extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            // Check uri
            LunaLog.e("receiver ");
            Uri inputUri = intent.getData();
            if (inputUri.getScheme().equals("package")) {
                LunaLog.e("receiver package add");
                LunaLog.e("监听到安装完成");
                if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                    LunaLog.e("receiver package add step 2");
                    String packageName = inputUri.getSchemeSpecificPart();
                    if (packageName.equals(Script.SPEEDDOG_PN)) {
                        LunaLog.e("is my package !");
                        GlobalSwitchs.downloadStatus = GlobalSwitchs.DownloadStatus.downloadComplete;
//                        context.startActivity(new Intent(context, MainActivity.class));
                        ShellUtils.execCommand("am force-stop " + Script.KU_PN + " \n",true);
                    }
                }
            }
        } catch (Throwable ex) {
        }
    }
}
