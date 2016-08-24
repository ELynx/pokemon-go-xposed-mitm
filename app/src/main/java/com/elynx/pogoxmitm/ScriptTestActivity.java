package com.elynx.pogoxmitm;

import org.jruby.embed.ScriptingContainer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ScriptTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_test);

        Button rubyButton = (Button) findViewById(R.id.rubyButton);
        rubyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText scriptEdit = (EditText) findViewById(R.id.rubyText);
                EditText resultEdit = (EditText) findViewById(R.id.rubyResult);

                String scriptToRun = scriptEdit.getText().toString();
                ScriptingContainer container = MitmProvider.scriptContainer.get();

                try {
                    Object retval = container.runScriptlet(scriptToRun);

                    if (retval != null) {
                        resultEdit.setText(retval.toString());
                    } else {
                        resultEdit.setText("null returned");
                    }
                }
                catch (Throwable e) {
                    String message = e.getMessage();

                    if (message == null)
                        message = "No message\n";
                    else
                        message = message + "\n";

                    message += e.toString();

                    resultEdit.setText(message);
                }
            }
        });
    }
}
