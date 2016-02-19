package luna.net.downloadscript;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.ScrollView;
import android.widget.TextView;

import net.luna.common.util.ThreadUtils;


public class MainActivity extends AppCompatActivity {
    Script script;
    TextView print;
    StringBuilder sb = new StringBuilder();
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getApplicationContext().getPackageName();
        print = (TextView) findViewById(R.id.print);
        script = new Script(this, 100, printHandler);
        print.setMovementMethod(ScrollingMovementMethod.getInstance());
        ThreadUtils.execute(script);
    }

    @Override
    protected void onResume() {

        if (script != null) {
            if (script.canLoopScript()) {
                ThreadUtils.execute(script);
            }
        }
        super.onResume();
    }

    Handler printHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (print != null && msg.obj instanceof String) {
                sb.append(msg.obj);
                print.setText(sb.toString());
//                print.scrollTo(0,print.getHeight());

            }
        }
    };
}
