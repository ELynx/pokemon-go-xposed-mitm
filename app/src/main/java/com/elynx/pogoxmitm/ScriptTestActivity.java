package com.elynx.pogoxmitm;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.elynx.pogoxmitm.test.NetworkImitation;

import org.ruboto.JRubyAdapter;

import java.nio.ByteBuffer;

public class ScriptTestActivity extends Activity implements View.OnClickListener {
    protected static String dump;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_test);

        JRubyAdapter.setUpJRuby(this);

        JRubyAdapter.put("@test_variable", "we can pass data into ruby scripts with `JRubyAdapter.put`");

        Button rubyButton = (Button) findViewById(R.id.rubyRun);
        rubyButton.setOnClickListener(this);

        Button netButton = (Button) findViewById(R.id.mitmRun);
        netButton.setOnClickListener(this);

        Button dumpButton = (Button) findViewById(R.id.resultDump);
        dumpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rubyRun) {

            EditText scriptEdit = (EditText) findViewById(R.id.rubyScript);
            EditText resultEdit = (EditText) findViewById(R.id.rubyResult);

            String scriptToRun = scriptEdit.getText().toString();
            String runResult = "Nothingness\n";

            try {
                runResult = "Trying run...\n";
                Object rc = JRubyAdapter.runScriptlet(scriptToRun);

                if (rc == null)
                    runResult += "Null returned";
                else
                    runResult += rc.toString();
            } catch (Throwable e) {
                String message = e.getMessage();

                if (message == null)
                    message = "No message\n";
                else
                    message = message + "\n";

                message += e.toString();

                if (e.getCause() != null) {
                    message += "\nCaused by " + e.getCause().toString();
                }

                runResult += "Exception:\n" + message;
            }

            resultEdit.setText(runResult);
        }

        if (v.getId() == R.id.mitmRun) {
            NetworkImitation.clearResults();

            String[] datas = {"space!!!", "100500 CP caterpie", "mew", "mewtwo", "mewthree"};

            for (int i = 0; i < datas.length; ++i) {
                byte[] bytes = datas[i].getBytes();

                ByteBuffer wrapper = ByteBuffer.wrap(bytes);

                NetworkImitation.pushData(wrapper.asReadOnlyBuffer());

                long interval = Math.round(Math.random() * 500.0);

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (v.getId() == R.id.resultDump) {
            EditText resultEdit = (EditText) findViewById(R.id.rubyResult);
            resultEdit.setText(NetworkImitation.getResults());
        }
    }
}
