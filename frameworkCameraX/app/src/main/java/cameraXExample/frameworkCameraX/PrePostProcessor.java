package cameraXExample.frameworkCameraX;
import android.graphics.Rect;

class Result {
    int classIndex;
    Float score;
    Rect rect;

    public Result(int cls, Float output, Rect rect) {
        this.classIndex = cls;
        this.score = output;
        this.rect = rect;
    }
};

public class PrePostProcessor {

    // model input image size
    static int mInputWidth = 640;
    static int mInputHeight = 640;

}
