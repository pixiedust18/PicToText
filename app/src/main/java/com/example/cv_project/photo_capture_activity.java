package com.example.cv_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;


import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Arrays;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

public class photo_capture_activity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    @BindView(R.id.layout_preview)
    LinearLayout cameraPreview;
    @BindView(R.id.txt_result)
    TextView txtResult;

    private static final int REQUEST_STORAGE_PERMISSION = 1;
    String res="Results not able to be processed. Check connectivity.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_photo_capture );
        FirebaseApp.initializeApp(this);
        ButterKnife.bind(this);
//keep screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        checkPermission();
    }

    private void checkPermission() {
        //Set up the permission listener
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                setupPreview();
            }
            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText( photo_capture_activity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        //Check camera permission
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA)
                .check();

        /*if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            setupPreview();
        }*/
    }
    //Here we set up the camera preview
    private void setupPreview() {
        //mCamera = getCameraInstance();
        Camera c = null;
        try {
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            Log.e(getString(R.string.app_name), "Camera in use");
        } finally {
            if (c != null) c.release();
        }
        mPreview = new CameraPreview(getBaseContext(), mCamera);
        try {
            //Set camera autofocus
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

        }catch (Exception e){
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        cameraPreview.addView(mPreview);
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
        mPicture = getPictureCallback();
        mPreview.refreshCamera(mCamera);
    }
    // take photo when the users tap the button
    @OnClick(R.id.btn_take_picture)
    public void takePhoto() {
        mCamera.takePicture(null, null, mPicture);
        /*Intent i = new Intent(this, Result_display.class);
        i.putExtra("Results", res);
        startActivity(i);*/
    }
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }
    private void releaseCamera() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
    //Here we get the photo from the camera and pass it to mlkit processor
    private Camera.PictureCallback getPictureCallback() {
        return new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mlinit(BitmapFactory.decodeByteArray(data, 0, data.length));
                mPreview.refreshCamera(mCamera);

            }
        };
    }


    //the main method that processes the image from the camera and gives labeling result
    private void mlinit(Bitmap bitmap) {
        //By default, the on-device image labeler returns at most 10 labels for an image.
        //But it is too much for us and we wants to get less
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionLabelDetectorOptions options =
                new FirebaseVisionLabelDetectorOptions.Builder()
                        .setConfidenceThreshold(0.4f)
                        .build();
        //To label objects in an image, create a FirebaseVisionImage object from a bitmap
        //Get an instance of FirebaseVisionCloudLabelDetector
        //FirebaseApp.initializeApp(this);
        FirebaseVisionLabelDetector detector = FirebaseVision.getInstance()
                .getVisionLabelDetector(options);
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List>() {
                    @Override
                    public void onSuccess(List labels) {
                        StringBuilder builder = new StringBuilder();
                        // Get information about labeled objects

                        for(int i=0; i<labels.size(); i++)
                        {
                            FirebaseVisionLabel label = (FirebaseVisionLabel)labels.get(i);
                            builder.append(label.getLabel())
                                    .append(" ")
                                    .append(label.getConfidence()).append("\n");

                        }
                        Log.d("label", builder.toString());
                        txtResult.setText("Processing image...");
                        res = ("Results: \n" + builder.toString());
                        Log.d("label", res);
                    }
                }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txtResult.setText(e.getMessage());
                        res = (e.getMessage());
                    }
                });
        Log.d("label", res);
        Log.d("label", "fecf");


        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                Log.d("label", res);
                Intent i = new Intent( photo_capture_activity.this, Result_display.class);
                i.putExtra("Results", res);
                startActivity(i);
            }
        }, 5000);
    }

    private void text_read_img(Bitmap bitmap) {
        //By default, the on-device image labeler returns at most 10 labels for an image.
        //But it is too much for us and we wants to get less
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        textRecognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {
                        String final_text = "";
                        String resultText = result.getText();
                        Log.d("text", resultText);

                        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
                            String blockText = block.getText();
                            final_text+=blockText+"\n";
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionText.Line line: block.getLines()) {
                                String lineText = line.getText();
                                Float lineConfidence = line.getConfidence();
                                List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (FirebaseVisionText.Element element: line.getElements()) {
                                    String elementText = element.getText();
                                    Float elementConfidence = element.getConfidence();
                                    List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }
                        Log.d("text", final_text);
                        txtResult.setText(final_text);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                txtResult.setText(e.getMessage());
                            }
                        });
    }

    private void text_read_doc(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionCloudDocumentRecognizerOptions options =
                new FirebaseVisionCloudDocumentRecognizerOptions.Builder()
                        .setLanguageHints( Arrays.asList("en", "hi"))
                        .build();
        FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer(options);

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        String resultText = result.getText();
                        for (FirebaseVisionDocumentText.Block block: result.getBlocks()) {
                            String blockText = block.getText();
                            Float blockConfidence = block.getConfidence();
                            List<RecognizedLanguage> blockRecognizedLanguages = block.getRecognizedLanguages();
                            Rect blockFrame = block.getBoundingBox();
                            for (FirebaseVisionDocumentText.Paragraph paragraph: block.getParagraphs()) {
                                String paragraphText = paragraph.getText();
                                Float paragraphConfidence = paragraph.getConfidence();
                                List<RecognizedLanguage> paragraphRecognizedLanguages = paragraph.getRecognizedLanguages();
                                Rect paragraphFrame = paragraph.getBoundingBox();
                                for (FirebaseVisionDocumentText.Word word: paragraph.getWords()) {
                                    String wordText = word.getText();
                                    Float wordConfidence = word.getConfidence();
                                    List<RecognizedLanguage> wordRecognizedLanguages = word.getRecognizedLanguages();
                                    Rect wordFrame = word.getBoundingBox();
                                    for (FirebaseVisionDocumentText.Symbol symbol: word.getSymbols()) {
                                        String symbolText = symbol.getText();
                                        Float symbolConfidence = symbol.getConfidence();
                                        List<RecognizedLanguage> symbolRecognizedLanguages = symbol.getRecognizedLanguages();
                                        Rect symbolFrame = symbol.getBoundingBox();
                                    }
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        txtResult.setText(e.getMessage());
                    }
                });
    }
}


