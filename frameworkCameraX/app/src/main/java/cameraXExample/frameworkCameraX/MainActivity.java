package cameraXExample.frameworkCameraX;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
// import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements Runnable {

    private static final String LOG_TAG = MainActivity.class.getSimpleName(); //FOR LOG

    private int mImageIndex = 0; //is a class variable to count data
    private String[] mTestImages = {"test1.png"};
    // ------------------
    // UI   elements
    private ImageView mImageView;
    private ResultView mResultView;
    private Button mButtonImageProcessing;
    private ProgressBar mProgressBar;
    private Bitmap mBitmap = null;
    // ------------------

    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // The first methods to load an activity, the logic begins here
        super.onCreate(savedInstanceState);
        // checking/ read permissions enabled in AndroidManifest.xml
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        try {
            // load an image in UI from files
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex])); // get images in assets
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading assets", e);
            finish();
        }

        // -------------------------------------------------------
        // read and link data from UI to this class
        setContentView(R.layout.activity_main); // LOAD LAYOUT
        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap); //load by default image
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        final Button buttonSelect = findViewById(R.id.selectButton);
        mButtonImageProcessing = findViewById(R.id.imageProcessingButton);
        final Button buttonRealTime = findViewById(R.id.realTimeButton);

        // -------------------------------------------------------
        // assign to UI actions
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                // load from String resources, this is to make app multi language
                final CharSequence[] dialogBoxOptions = { getString(R.string.choose_photos_dlg_label), getString(R.string.take_picture_dlg_label), getString(R.string.cancel_dlg_label) };
                // create dialog box
                AlertDialog.Builder dialogBoxSelect = new AlertDialog.Builder(MainActivity.this);
                dialogBoxSelect.setTitle(getString(R.string.title_dlg_label)); //assign title

                // assign dialog box optins and clicks options
                dialogBoxSelect.setItems(dialogBoxOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        Log.i(LOG_TAG, "DialogInterface --->>> "+item);
                        if (item == 0) {
                            // Choose from Photos
                            Log.i(LOG_TAG, "DialogInterface -> Choose from Photos --->>> "+item);
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 0);
                        }
                        else if (item == 1) {
                            // Capture from camera
                            Log.i(LOG_TAG, "DialogInterface -> Capture from camera --->>> "+item);
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 1);
                        }
                        else if (item == 2) {
                            // Cancel actions
                            Log.i(LOG_TAG, "DialogInterface -> Cancel actions --->>> "+item);
                            dialog.dismiss();
                        }
                    }
                });
                dialogBoxSelect.show();
            }
        });

        // -------------------------------------------------------
        // here onClick event for real time process
        buttonRealTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              // CALL TO OBJECT DETECTION CLASS, IN THIS CLASS WE PROCESS IMAGES.
              final Intent intent = new Intent(MainActivity.this, CameraXActivity.class);
              startActivity(intent); // TODO: ????
            }
        });
        // -------------------------------------------------------

        // -------------------------------------------------------
        // here onClick event for image processing process
        mButtonImageProcessing.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonImageProcessing.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);

                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mImageView.getWidth() / mBitmap.getWidth() : (float)mImageView.getHeight() / mBitmap.getHeight());
                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mImageView.getHeight() / mBitmap.getHeight() : (float)mImageView.getWidth() / mBitmap.getWidth());

                // todo: SCALE????
                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                mStartY = (mImageView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                // TODO: call a new activity
                Thread thread = new Thread(MainActivity.this);
                thread.start(); //call to run() method
            }
        });
        // -------------------------------------------------------
        // end onCreate method
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(LOG_TAG, "ON ACTIVITY RESULT --->>> ");
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    // from dialog box "Choose from Photos"
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Log.i(LOG_TAG, "ON ACTIVITY RESULT --->>> CALLED FROM DIALOG BOX");
                                Log.i(LOG_TAG, "ON ACTIVITY RESULT --->>> "+picturePath);

                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                if(mBitmap!=null){
                                    Matrix matrix = new Matrix();
                                    // ------------------------------
                                    // make something WITH IMAGE
                                    //matrix.postRotate(90.0f);
                                    // ------------------------------
                                    // set image in view
                                    mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                    mImageView.setImageBitmap(mBitmap);  // here update data in UI to show image
                                    // ------------------------------
                                }
                                cursor.close(); // TODO: ????
                            }
                        }
                    }
                    break;
                case 1:
                    // from dialog box "Take Picture"
                    if (resultCode == RESULT_OK && data != null) {
                        Log.i(LOG_TAG, "ON ACTIVITY RESULT --->>> FROM CAMERA DATA");
                        mBitmap = (Bitmap) data.getExtras().get("data");
                        Matrix matrix = new Matrix();
                        // ------------------------------
                        // make something
                        //matrix.postRotate(90.0f);
                        // ------------------------------
                        // set image in view
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        mImageView.setImageBitmap(mBitmap); // here update data in UI to show image
                        // ------------------------------
                    }
                    break;
            }
        }
    }


    @Override
    public void run() {
        Log.i(LOG_TAG, "RUUUUUUUUUNNNNN");
        // -------------------
        // manage image
        // Para que usa PrepostProcessor?
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);

        runOnUiThread(() -> {
            // manage UI elements here
            mButtonImageProcessing.setEnabled(true);
            mButtonImageProcessing.setText(getString(R.string.image_processing_btn_label));
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
        });
    }


    // ------------------------------------------------
    // Menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* define options menu */
        switch (item.getItemId()) {
            case R.id.info: {
                Log.i(LOG_TAG, "onOptionsItemSelected --->>> ");
                // SOME STUFF HERE
                break;
            }
            case R.id.action_settings: {
                Log.i(LOG_TAG, "(id == R.id.action_settings) -->");
                // SOME STUFF HERE
                break;
            }
            case R.id.action_menu_1: {
                Log.i(LOG_TAG, "(MENU 1) -->");
                // SOME STUFF HERE
                break;
            }
            case R.id.action_menu_2: {
                Log.i(LOG_TAG, "(MENU 2) -->");
                // SOME STUFF HERE
                break;
            }
        } // --------

        return super.onOptionsItemSelected(item);
    }
    // ------------------------------------------------
}
