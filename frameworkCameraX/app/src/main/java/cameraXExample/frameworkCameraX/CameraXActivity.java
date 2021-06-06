package cameraXExample.frameworkCameraX;

// declare in AndroidManifest.xml
//         <activity
//            android:name=".ObjectDetectionActivity"
//            android:configChanges="orientation"
//            android:screenOrientation="portrait">
//        </activity>

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewStub;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.camera.core.ImageProxy;


import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CameraXActivity extends AbstractCameraXActivity<CameraXActivity.AnalysisResult> {

    private static final String LOG_TAG = CameraXActivity.class.getSimpleName(); //FOR LOG

    private ResultView mResultView;

    static class AnalysisResult {
        private final ArrayList<Result> mResults;

        public AnalysisResult(ArrayList<Result> results) {
            mResults = results;
        }
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_image_processing;
    }

    @Override
    protected TextureView getCameraPreviewTextureView() {
        Log.i(LOG_TAG, "getCameraPreviewTextureView() --->>>");
        mResultView = findViewById(R.id.resultView);
        return ((ViewStub) findViewById(R.id.image_processing_texture_view_stub))
                .inflate()
                .findViewById(R.id.image_processing_texture_view);
    }

    @Override
    protected void applyToUiAnalyzeImageResult(AnalysisResult result) {
        Log.i(LOG_TAG, "applyToUiAnalyzeImageResult --->>>");
        // call to mResultView to update results
        mResultView.setResults(result.mResults);
        mResultView.invalidate();
    }

    private Bitmap imgToBitmap(Image image) {
        Log.i(LOG_TAG, "IMG TO BITMAP --->>>");
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    @Override
    @WorkerThread
    @Nullable
    protected AnalysisResult analyzeImage(ImageProxy image, int rotationDegrees) {
        // TODO: to review this method
        /*reimplement image analyze to something... */
        Log.i(LOG_TAG, "ANALYZE IMAGE --->>>");
        Bitmap bitmap = imgToBitmap(image.getImage());
        // ----------------------------------
        // make somthing here with an image
        Matrix matrix = new Matrix();
        matrix.postRotate(90.0f);

        // -------------------
        // manage image
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        // -------------------

        //float imgScaleX = (float)bitmap.getWidth() / PrePostProcessor.mInputWidth;
        //float imgScaleY = (float)bitmap.getHeight() / PrePostProcessor.mInputHeight;
        //float ivScaleX = (float)mResultView.getWidth() / bitmap.getWidth();
        //float ivScaleY = (float)mResultView.getHeight() / bitmap.getHeight();

        // TODO: return an object analysis result
        ArrayList<Result> results = new ArrayList<>();
        return new AnalysisResult(results);
    }
}
