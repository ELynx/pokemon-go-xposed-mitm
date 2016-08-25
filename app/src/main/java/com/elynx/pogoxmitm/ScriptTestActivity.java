package com.elynx.pogoxmitm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.ruboto.JRubyAdapter;

public class ScriptTestActivity extends org.ruboto.EntryPointActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_test);

        Button rubyButton = (Button) findViewById(R.id.rubyRun);
        rubyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.rubyRun)
            return;

        EditText scriptEdit = (EditText) findViewById(R.id.rubyScript);
        EditText resultEdit = (EditText) findViewById(R.id.rubyResult);

        String scriptToRun = scriptEdit.getText().toString();
        String runResult;

        try {
            Object rc = JRubyAdapter.runScriptlet(scriptToRun);

            if (rc == null)
                runResult = "Null returned";
            else
                runResult = rc.toString();
        } catch (Throwable e) {
            String message = e.getMessage();

            if (message == null)
                message = "No message\n";
            else
                message = message + "\n";

            message += e.toString();

            runResult = "Exception:\n" + message;
        }

        resultEdit.setText(runResult);
    }
}
