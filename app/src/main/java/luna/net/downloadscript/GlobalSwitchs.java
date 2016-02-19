package luna.net.downloadscript;

/**
 * Created by bintou on 15/12/17.
 */
public class GlobalSwitchs {


    public enum DownloadStatus {prepareDownload, downloading, downloadComplete}

    public static DownloadStatus downloadStatus = DownloadStatus.prepareDownload;


}
