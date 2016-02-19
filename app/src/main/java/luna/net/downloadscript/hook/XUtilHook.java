package luna.net.downloadscript.hook;

import java.util.ArrayList;
import java.util.List;

public class XUtilHook extends XHook {

    private XUtilHook(String methodName, String restrictionName) {
        super(restrictionName, methodName, null);
    }

    public String getClassName() {
        return Util.class.getName();
    }

    // isXposedEnabled
    public static List<XHook> getInstances() {
        List<XHook> listHook = new ArrayList<XHook>();
        listHook.add(new XUtilHook("isXposedEnabled", null));
        return listHook;
    }

    @Override
    public void before(XParam param) throws Throwable {
        param.setResult(true);
    }

    @Override
    public void after(XParam param) throws Throwable {
        // Do nothing
    }
}
