package com.tntadvance.testcamera;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnCamera;
    private static final int REQUEST_CODE = 0x11;
    private ImageView imgView;
    private Uri mImageCaptureUri;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;

    private static final int CAMERA_REQUEST = 1888;
    private static final int PICK_FROM_FILE2 = 1999;

    private Bitmap bitmapCrop;
    private Button btnURL;
    private Button btnCamera2;
    private Button btnCamera3;
    private TextView tvFileName;
    private Button btnSaveImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StorageDirectory();
        initInstances();
    }

    private void StorageDirectory() {
        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE); // without sdk version check

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "testCamera");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success
            Log.d("success=", "true");
        } else {
            // Do something else on failure
            Log.d("success=", "false");
        }

    }

    private void initInstances() {
        btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(this);
        btnCamera2 = (Button) findViewById(R.id.btnCamera2);
        btnCamera2.setOnClickListener(this);
        btnCamera3 = (Button) findViewById(R.id.btnCamera3);
        btnCamera3.setOnClickListener(this);
        btnURL = (Button) findViewById(R.id.btnURL);
        btnURL.setOnClickListener(this);
        btnSaveImg = (Button) findViewById(R.id.btnSaveImg);
        btnSaveImg.setOnClickListener(this);

        tvFileName = (TextView) findViewById(R.id.tvFileName);
        imgView = (ImageView) findViewById(R.id.imgView);
    }

    @Override
    public void onClick(View v) {
        if (v == btnCamera) {
            final String[] items = new String[]{"Take from camera", "Select from gallery"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Select Image");
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    //pick from camera
                    if (item == 0) {
                        Log.d("camera", "camera");
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + File.separator + "testCamera",
                                "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

                        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                        try {
                            intent.putExtra("return-data", true);

                            startActivityForResult(intent, PICK_FROM_CAMERA);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //pick from file
                        if (Build.VERSION.SDK_INT < 19) {
                            Intent intent = new Intent();

                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);

                            startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);

                        } else {

                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                    mImageCaptureUri);
                            startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                        }
                    }
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();


        } else if (v == btnCamera2) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + File.separator + "testCamera",
                    "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

            startActivityForResult(intent, CAMERA_REQUEST);
//            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

        } else if (v == btnCamera3) {
            //pick from file
            if (Build.VERSION.SDK_INT < 19) {
                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE2);

            } else {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                        mImageCaptureUri);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE2);
            }
        } else if (v == btnURL) {
            Picasso.with(this).load("https://s-media-cache-ak0.pinimg.com/originals/2b/0c/1b/2b0c1bb266137693764ef2f9a0917f53.jpg").into(imgView);

        } else if (v == btnSaveImg) {
            imgView.buildDrawingCache();
            Bitmap bMap = imgView.getDrawingCache();

            savePic(bMap);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != -1) return;

        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop();
                break;

            case PICK_FROM_FILE:
                mImageCaptureUri = data.getData();
                doCrop();
                break;

            case CROP_FROM_CAMERA:
                Bundle extras = data.getExtras();

                if (extras != null) {
                    bitmapCrop = extras.getParcelable("data");
                    imgView.setImageBitmap(bitmapCrop);

//                    savePic(bitmapCrop);
                }

            case CAMERA_REQUEST:
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imgView.setImageBitmap(photo);

            case PICK_FROM_FILE2:
                mImageCaptureUri = data.getData();
                imgView.setImageURI(mImageCaptureUri);
                break;

        }
    }

    private void doCrop() {
        //	final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

//            return;
        } else {
            intent.setData(mImageCaptureUri);

            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 400);
            intent.putExtra("aspectX", 3);
            intent.putExtra("aspectY", 4);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

            startActivityForResult(i, CROP_FROM_CAMERA);

        }

    }

    public File savePic(Bitmap c) {

        c = Bitmap.createScaledBitmap(c, c.getWidth(), c.getHeight(), true);
        File photo = new File(Environment.getExternalStorageDirectory() + File.separator + "testCamera",
                "NEW" + System.currentTimeMillis() + ".jpg");

        OutputStream os = null;
        try {
            os = new FileOutputStream(photo);
            c.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            //    t1.setText("w="+String.valueOf(width)+" "+"h="+String.valueOf(height)+" File: "+photo.getPath());
            tvFileName.setText(" File: " + photo.getPath());

        } catch (IOException e) {
            Log.e("combineImages", "problem combining images", e);
            tvFileName.setText(e.getMessage());
        }

        return photo;
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgString;
    }
}
