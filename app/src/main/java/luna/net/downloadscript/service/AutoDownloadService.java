package luna.net.downloadscript.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipboardManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

import luna.net.downloadscript.GlobalSwitchs;


public class AutoDownloadService extends AccessibilityService {
    private static final String TAG = "luna";


    private ArrayList<AccessibilityNodeInfo> mNodeInfoList = new ArrayList<AccessibilityNodeInfo>();

    private AccessibilityNodeInfo loginNode;

    private ClipboardManager mClipboardManager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.prepareDownload || GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.downloadComplete) {
            final int eventType = event.getEventType();
            switch (eventType) {
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    log("window state changed");
                    if (GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.prepareDownload || GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.downloadComplete) {
                        AccessibilityNodeInfo nodeInfo = event.getSource();
                        if (null != nodeInfo) {
                            mNodeInfoList.clear();
                            traverseNode(nodeInfo);
                        }
                    }
                    break;
                case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                    log("TYPE_WINDOW_CONTENT_CHANGED:" + GlobalSwitchs.downloadStatus);
                    if (GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.prepareDownload || GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.downloadComplete) {
                        AccessibilityNodeInfo nodeInfo = event.getSource();
                        if (null != nodeInfo) {
                            mNodeInfoList.clear();
                            traverseNode(nodeInfo);
                        }
                    }
                    break;
                case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                    log("TYPE_VIEW_SCROLLED");
                    break;

                default:
                    break;
            }
        }
    }


    synchronized private void traverseNode(AccessibilityNodeInfo node) {
        if (null == node) return;

        final int count = node.getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);
                if (GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.prepareDownload || GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.downloadComplete) {
                    traverseNode(childNode);
                }
            }
        } else {
            CharSequence text = node.getText();
            if (null != text && text.length() > 0) {
                if (GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.prepareDownload) {
                    String str = text.toString();
                    log("traverseNode:child : " + str);
                    if (node.getClassName().equals("android.widget.Button")) {
                        if (str.equals("下载")) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            log("traverseNode:child click");
                            GlobalSwitchs.downloadStatus = GlobalSwitchs.DownloadStatus.downloading;
                        }
                    }
                }
            } else {
//                if (GlobalSwitchs.downloadStatus == GlobalSwitchs.DownloadStatus.downloadComplete) {
//                    if (node.getClassName().equals("android.widget.ImageView")) {
//                        log("traverseNode:child2: " + node.getClassName() + "  parent: " + node.getParent().getClassName());
//                        if (node.getParent() != null && node.getParent().getClassName().equals("android.widget.LinearLayout")) {
//                            log("traverseNode:child3");
//                            AccessibilityNodeInfo pNode = node.getParent();
//                            if (pNode.isClickable() && pNode.getChildCount() == 1) {
//                                log("traverseNode:child4");
//                                pNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                                GlobalSwitchs.downloadStatus = GlobalSwitchs.DownloadStatus.prepareDownload;
//                            }
//                        }
//                    }
//                }
            }
        }

    }


    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }

    private void log(Object obj) {
        Log.d(TAG, ">>>" + obj);
    }

}
