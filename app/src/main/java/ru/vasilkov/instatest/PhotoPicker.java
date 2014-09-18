package ru.vasilkov.instatest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PhotoPicker extends Activity {

    public static final String APIURL = "https://api.instagram.com/v1";

    Context context = this;
    static String getUserId = "";
    static String token = "";

    String[] imageUrls;
    int[] posCheck;
    int count;
    Bitmap[] image;
    GetInstagramPhotoAsyncTask getPhoto;

    //
    DisplayImageOptions options;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_picker);

        //
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();


        token = getResources().getString(R.string.token);

        String userName = getIntent().getStringExtra("user");

        getUserId = APIURL + "/users/search?q=" + userName + "&amp;amp;count=1&amp;amp;access_token=" + token;

        getPhoto = new GetInstagramPhotoAsyncTask();
        getPhoto.execute();

    }

    public void makeCollage(View view) {
        String str = "";
        int imgCount = 0;
        int iter = 0;
        //str += Выберите 4 фотографии для коллажа;
        Bitmap[] imgBit;// = new Bitmap[0];
        for (int index = 0; index < count; index ++) {
            if (posCheck[index] == 1) {
                imgCount += 1;

                str += String.valueOf(index);
            }
        }
        imgBit = new Bitmap[imgCount];
        for (int index = 0; index < count; index ++){
            if (posCheck[index] == 1){
            imgBit[iter] = image[index];
            iter ++;
            }
        }

        if (imgCount == 4){
            Intent collageActivity = new Intent(PhotoPicker.this,CollageActivity.class);
            for (int index = 0; index < 4; index ++) {
                String extr = "ImgBitmap" + index;
                collageActivity.putExtra(extr, getByteArrayfromBitmap(imgBit[index]));
            }

            startActivity(collageActivity);
        }

        else {
            Toast.makeText(context, str, Toast.LENGTH_LONG).show();
        }
    }

    public byte[] getByteArrayfromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }

    class GetInstagramPhotoAsyncTask extends AsyncTask<Void, Void, Void> {
        String userID;
        int Err = 0;
        @Override
        protected Void doInBackground(Void... params) {


            try {
                URL url = new URL(getUserId);
                InputStream inputStream = url.openConnection().getInputStream();

                String response = streamToString(inputStream);

                JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();


                JSONArray jsonArray1 = jsonObject.getJSONArray("data");

                if (!(jsonArray1.length() < 1)) {
                    userID = jsonArray1.getJSONObject(0).getString("id");

                    String photoURL = APIURL + "/users/" + userID + "/media/recent/?access_token=" + token;

                    URL imageUrl = new URL(photoURL);
                    InputStream imageInputStream = imageUrl.openConnection().getInputStream();

                    String imageResponse = streamToString(imageInputStream);

                    JSONObject jsonObject2 = (JSONObject) new JSONTokener(imageResponse).nextValue();
                    JSONArray jsonArray = jsonObject2.getJSONArray("data");

                    count = 5;

                    imageUrls = new String[count];
                    image = new Bitmap[count];
                    posCheck = new int[count];

                    for (int index = 0; index < count; index ++) {
                        JSONObject mainImageJsonObject =
                                jsonArray.getJSONObject(index).getJSONObject("images").getJSONObject("low_resolution");
                        imageUrls[index] = mainImageJsonObject.getString("url");
                        image[index] = BitmapFactory.decodeStream((InputStream) new URL(imageUrls[index]).getContent());
                    }
                    //finish();
                } else {
                    Err = 1;
                }



            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

                if (Err == 0) {
                    AbsListView photoView = (GridView) findViewById(R.id.photoView);
                    ((GridView) photoView).setAdapter(new ImageAdapter());
                } else {
                    Intent backToMain = new Intent(PhotoPicker.this,MainActivity.class);
                    backToMain.putExtra("err", 1);
                    startActivity(backToMain);
                    finish();
                }


        }
    }


    public String streamToString(InputStream is) throws IOException {
        String string = "";

        if (is != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            string = stringBuilder.toString();
        }

        return string;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_picker, menu);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backToMain = new Intent(PhotoPicker.this,MainActivity.class);
        startActivity(backToMain);
        finish();
    }

    private class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imageUrls.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            ImageView imageView;
            ProgressBar progressBar;
            CheckBox checkbox;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.cellitem, parent, false);
                holder = new ViewHolder();
                assert view != null;

                holder.checkbox = (CheckBox) view.findViewById(R.id.check);
                holder.imageView = (ImageView) view.findViewById(R.id.image);
                holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.checkbox.setText(String.valueOf(position));
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.checkbox.toggle();
                    if(holder.checkbox.isChecked()){
                        posCheck[position] = 1;
                    }else {
                        posCheck[position] = 0;
                    }
                }
            });

            imageLoader.displayImage(imageUrls[position], holder.imageView, options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            holder.progressBar.setProgress(0);
                            holder.progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            holder.progressBar.setVisibility(View.GONE);
                        }
                    }, new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String imageUri, View view, int current,
                                                     int total) {
                            holder.progressBar.setProgress(Math.round(100.0f * current / total));
                        }
                    }
            );

            return view;
        }
    }
}
