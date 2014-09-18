package ru.vasilkov.instatest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class SendDialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_dialog);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.img_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btnSend(View view) {
        EditText sendTo = (EditText)findViewById(R.id.editSendTo);
        EditText sendTitle =(EditText)findViewById(R.id.editTitle);
        Intent CollageActivity = new Intent();

        CollageActivity.putExtra("sendTo", sendTo.getText().toString());
        CollageActivity.putExtra("sendTitle", sendTitle.getText().toString());

        setResult(RESULT_OK, CollageActivity);
        finish();
    }

    public void btnCancel(View view) {
        finish();
    }
}
