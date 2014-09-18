package ru.vasilkov.instatest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.vasilkov.instatest.util.MailSenderClass;

public class CollageActivity extends Activity {

    Context context = this;

    String where = "";
    String title = "";
    String attach = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        Bundle extras = getIntent().getExtras();
        String extr = "";
        Bitmap[] imgBitmap = new Bitmap[4];
        for (int index = 0; index < 4; index ++) {
            extr = "ImgBitmap" + index;
            imgBitmap[index] = getBitmapfromByteArray(extras.getByteArray(extr));
        }

        final ImageView imgV1 = (ImageView)findViewById(R.id.imageView1);
        ImageView imgV2 = (ImageView)findViewById(R.id.imageView2);
        ImageView imgV3 = (ImageView)findViewById(R.id.imageView3);
        ImageView imgV4 = (ImageView)findViewById(R.id.imageView4);


        imgV1.setImageBitmap(imgBitmap[0]);
        imgV2.setImageBitmap(imgBitmap[1]);
        imgV3.setImageBitmap(imgBitmap[2]);
        imgV4.setImageBitmap(imgBitmap[3]);

    }

    public Bitmap getBitmapfromByteArray(byte[] bitmap) {
        return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
    }

    private Bitmap getScreenViewBitmap(View v) {
        Canvas canvas;
        Bitmap returnedBitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE);
        v.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.collage, menu);
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

    public void bSend(View view) {
        Intent toSendDialog = new Intent(CollageActivity.this,SendDialogActivity.class);
        startActivityForResult(toSendDialog, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                where = data.getStringExtra("sendTo");
                title = data.getStringExtra("sendTitle");

                //String fileLocation = Environment.getExternalStorageDirectory().toString() + "/instatest/";
                attach = "/mnt/sdcard/collage.jpg";

                final View relativeLayout = findViewById(R.id.rl1);
                Bitmap collageBitmap = getScreenViewBitmap(relativeLayout);

                FileOutputStream fos;
                try {
                    /*final File sddir = new File(fileLocation);
                    if (!sddir.exists()) {
                        sddir.mkdirs();
                    }*/

                    fos = new FileOutputStream("/mnt/sdcard/collage.jpg");

                    if (fos != null) {
                        if (!collageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)) {
                            //Log.d(LOGTAG, "Compress/Write failed");
                        }
                        fos.flush();
                        fos.close();
                    }

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                sender_mail_async async_sending = new sender_mail_async();
                async_sending.execute();

            }
        }
    }

    private class sender_mail_async extends AsyncTask<Object, String, Boolean> {
        ProgressDialog WaitingDialog;
        @Override
        protected void onPreExecute() {
            // Выводим пользователю процесс загрузки
            WaitingDialog = ProgressDialog.show(CollageActivity.this, "Отправка данных", "Отправляем сообщение...", true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Прячем процесс загрузки
            WaitingDialog.dismiss();
            Toast.makeText(context, "Отправка завершена!!!", Toast.LENGTH_LONG).show();
            ((Activity)context).finish();
        }
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                String text = "";
                String from = "instatest.for.mad@gmail.com";

                // Вызываем конструктор и передаём в него наши логин и пароль от ящика на gmail.com
                MailSenderClass sender = new MailSenderClass("instatest.for.mad@gmail.com", "a1029384756");

                // И вызываем наш метод отправки
                sender.sendMail(title, text, from, where, attach);
            } catch (Exception e) {
                Toast.makeText(context, "Ошибка отправки сообщения!", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

}
