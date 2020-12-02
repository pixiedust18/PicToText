package com.example.cv_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import android.os.FileUtils;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    private static final int STORAGE_CODE = 1000;
    String res = "bleh";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        auth = FirebaseAuth.getInstance();
        setContentView( R.layout.activity_main );

        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawerLayout );
        mToggle = new ActionBarDrawerToggle( this, mDrawerLayout, R.string.open, R.string.close );

        ImageButton ib = (ImageButton) findViewById( R.id.profilepic );
        FirebaseUser currentUser = auth.getCurrentUser();
        Button save = (Button) findViewById( R.id.save_change_n );
        EditText et1 = (EditText) findViewById( R.id.sign_custom ) ;

        mDrawerLayout.addDrawerListener( mToggle );
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        NavigationView navigationView = (NavigationView) findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );

        set_user_info();

        if (currentUser == null) {
            Intent activityChangeIntent = new Intent( MainActivity.this, login_activity.class );

            MainActivity.this.startActivity( activityChangeIntent );
        }
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s =  et1.getText().toString();
                et1.setText( s );
            }
        });
        ib.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    //system OS >= Marshmallow(6.0), check if permission is enabled or not
                    if (checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE ) ==
                            PackageManager.PERMISSION_DENIED) {
                        //permission was not granted, request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions( permissions, STORAGE_CODE );
                    } else {
                        Intent intent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                        startActivityForResult( intent, 1 );
                    }
                } else {
                    //system OS < Marshmallow, call save pdf method
                    Intent intent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                    startActivityForResult( intent, 1 );
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected( item ))
        {
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    protected String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private String getFileName(Uri uri) throws IllegalArgumentException {
        // Obtain a cursor with information regarding this uri
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            throw new IllegalArgumentException("Can't obtain file name, cursor is empty");
        }

        cursor.moveToFirst();

        String fileName = cursor.getString(cursor.getColumnIndexOrThrow( OpenableColumns.DISPLAY_NAME));

        cursor.close();

        return fileName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                text_read_img(thumbnail);
                //new MyAsyncTask().execute(thumbnail);

            } else if(requestCode==1)
            {
                Uri selectedImage = data.getData();
                /*String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));*/
                ImageButton ib = (ImageButton) findViewById( R.id.profilepic );
                ib.setImageURI(null);
                ib.setImageURI(selectedImage);
                FirebaseUser user = auth.getInstance().getCurrentUser();
                uploadImgFirebase(selectedImage);
                /*StorageReference sr =
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(selectedImage))
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("UpdTe", "User profile updated.");
                                }
                            }
                        });*/
            } else if(requestCode==0)
            {
                Uri uri = data.getData();
                Log.d("File_url", "File Uri: " + uri.toString());
                // Get the path
                String path =getFileName( uri );
                Log.d("File_url", "File Path: " + path);

                String content;
                //Open( path );
                try {
                    InputStream in = getContentResolver().openInputStream(uri);
                    if ( in != null) {
                        InputStreamReader tmp = new InputStreamReader( in );
                        BufferedReader reader = new BufferedReader(tmp);
                        String str;
                        StringBuilder buf = new StringBuilder();
                        while ((str = reader.readLine()) != null) {
                            buf.append(str + "\n");
                        } in .close();

                        content = buf.toString();
                        Log.d("Open_file", content);
                        Intent activityChangeIntent3 = new Intent(MainActivity.this, PrettyActivity.class);
                        activityChangeIntent3.putExtra("Results", content);
                        EditText et1 = (EditText) findViewById( R.id.sign_custom ) ;
                        String s =  et1.getText().toString();
                        activityChangeIntent3.putExtra("Sign", s);
                        MainActivity.this.startActivity(activityChangeIntent3);
                    }
                } catch (java.io.FileNotFoundException e) {} catch (Throwable t) {
                    Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void uploadImgFirebase(Uri selectedImage) {
        StorageReference fileRef = storageRef.child( "/users/"+auth.getCurrentUser().getUid()+"/profile.png" );
        fileRef.putFile( selectedImage ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText( MainActivity.this, "Profile picture uploaded", Toast.LENGTH_SHORT ).show();
                fileRef.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseUser user = auth.getInstance().getCurrentUser();
                        //Uri x = Uri.parse( "gs://cvproject-d5912.appspot.com"+"/users/"+auth.getCurrentUser().getUid()+"/profile.png" );
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("UpdTe", "User profile updated.");
                                        }
                                    }
                                });
                    }
                } );


            }
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText( MainActivity.this, "Failed", Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    private void set_user_info()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView tv1 = (TextView) findViewById( R.id.userid );
        TextView tv2 = (TextView) findViewById( R.id.name_g );
        TextView tv3 = (TextView) findViewById( R.id.emailid );
        EditText et1 = (EditText) findViewById( R.id.sign_custom ) ;
        ImageView dp = (ImageView) findViewById( R.id.profilepic );

        if (user != null) {
            // Name, email address, and profile photo Url
            String uid = user.getUid();
            tv1.setText( uid );
            String name = user.getDisplayName();
            tv2.setText( name );

            String email = user.getEmail();
            tv3.setText( email );

            Uri photoUrl = user.getPhotoUrl();
            dp.setImageURI(null);
            dp.setImageURI(photoUrl);
            Picasso.get().load( photoUrl ).into(dp);
            String phone = user.getPhoneNumber();

            if(et1.getText().toString().equals( "" )) {
                et1.setText( "Sign: " + name + "\n" + "Contact details: " + email );
            }
            Log.d("User info", name+" "+photoUrl+" "+email);
            // Check if user's email is verified
            //boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.



        }
    }
    public String Open(String fileName) {
        String err="";
        try {
            FileInputStream fis = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            String myData="";
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine;
            }
            in.close();
            Log.d("Read", "SampleFile.txt data retrieved from Internal Storage...");
            return myData;
        } catch (IOException e) {
            err = e.toString();
            e.printStackTrace();
        }
        return err;
        /*String content = "";
        try {
            InputStream in = openFileInput(fileName);
            if ( in != null) {
                InputStreamReader tmp = new InputStreamReader( in );
                BufferedReader reader = new BufferedReader(tmp);
                String str;
                StringBuilder buf = new StringBuilder();
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");
                } in .close();

                content = buf.toString();
            }
        } catch (java.io.FileNotFoundException e) {} catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

        return content;*/
    }
    private void mlinit(Bitmap bitmap) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionLabelDetectorOptions options =
                new FirebaseVisionLabelDetectorOptions.Builder()
                        .setConfidenceThreshold(0.4f)
                        .build();

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
                        res = ("Results: \n" + builder.toString());
                        Log.d("label", res);
                    }
                }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        res = (e.getMessage());
                    }
                });
        Log.d("label", res);
        Log.d("label", "fecf");


        /*new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                Log.d("label", res);
                Intent i = new Intent(MainActivity.this, Result_display.class);
                i.putExtra("Results", res);
                startActivity(i);
            }
        }, 5000);*/
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
                        res = final_text;
                        //txtResult.setText(final_text);
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("text  im doc", e.getMessage());

                            }
                        });
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                Log.d("label", res);
                //while(res.equals( "bleh" ))
                //{

                //}
                Intent i = new Intent(MainActivity.this, PrettyActivity.class);
                i.putExtra("Results", res);
                EditText et1 = (EditText) findViewById( R.id.sign_custom ) ;
                String s =  et1.getText().toString();
                i.putExtra("Sign", s);
                startActivity(i);
            }
        }, 10000);
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
                       // txtResult.setText(e.getMessage());
                    }
                });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission was granted from popup, call savepdf method
                    Intent intent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                    startActivityForResult( intent, 2 );
                }
                else {
                    //permission was denied from popup, show error message
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_signup:
                Intent activityChangeIntent = new Intent(MainActivity.this, SignupActivity.class);
                MainActivity.this.startActivity(activityChangeIntent);
                Log.d("nav", "signup");
                break;
            case R.id.nav_upload:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    //system OS >= Marshmallow(6.0), check if permission is enabled or not
                    if (checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE ) ==
                            PackageManager.PERMISSION_DENIED) {
                        //permission was not granted, request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions( permissions, STORAGE_CODE );
                    } else {
                        Intent intent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                        startActivityForResult( intent, 2 );
                    }
                } else {
                    //system OS < Marshmallow, call save pdf method
                    Intent intent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                    startActivityForResult( intent, 2 );
                }
                break;
            case R.id.nav_click:
                Intent activityChangeIntent2 = new Intent(MainActivity.this, photo_capture_activity.class);

                // currentContext.startActivity(activityChangeIntent);

                MainActivity.this.startActivity(activityChangeIntent2);
                break;
            case R.id.nav_tedit:
                Intent activityChangeIntent3 = new Intent(MainActivity.this, PrettyActivity.class);
                activityChangeIntent3.putExtra("Results", "");
                EditText et1 = (EditText) findViewById( R.id.sign_custom ) ;
                String s =  et1.getText().toString();
                activityChangeIntent3.putExtra("Sign", s);
                MainActivity.this.startActivity(activityChangeIntent3);
                break;
            case R.id.nav_openf:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    //system OS >= Marshmallow(6.0), check if permission is enabled or not
                    if (checkSelfPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE ) ==
                            PackageManager.PERMISSION_DENIED) {
                        //permission was not granted, request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions( permissions, STORAGE_CODE );
                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("text/plain");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        try {
                            startActivityForResult(
                                    Intent.createChooser(intent, "Select a File to Upload"),
                                    0);
                        } catch (android.content.ActivityNotFoundException ex) {
                            // Potentially direct the user to the Market with a Dialog
                            Toast.makeText(this, "Please install a File Manager.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf|text/plain|application/msword");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try {
                        startActivityForResult(
                                Intent.createChooser(intent, "Select a File to Upload"),
                                0);
                    } catch (android.content.ActivityNotFoundException ex) {
                        // Potentially direct the user to the Market with a Dialog
                        Toast.makeText(this, "Please install a File Manager.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();

                Intent activityChangeIntent4 = new Intent(MainActivity.this, login_activity.class);

                MainActivity.this.startActivity(activityChangeIntent4);
                break;
        }
        //close navigation drawer
        mDrawerLayout.closeDrawer( GravityCompat.START);
        return true;
    }
    /*class MyAsyncTask extends AsyncTask<Bitmap, ProgressBar, Bitmap> {
        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator
        }

        protected Bitmap doInBackground(Bitmap... strings) {
            // Some long-running task like downloading an image.
            text_read_img(strings[0]);
            return strings[0];
        }

        protected void onProgressUpdate(ProgressBar... values) {
            // Executes whenever publishProgress is called from doInBackground
            // Used to update the progress indicator
            //progressBar.setProgress(values[0]);
        }

        protected void onPostExecute(Bitmap result) {

            Log.d("label", res);

            Intent i = new Intent(MainActivity.this, PrettyActivity.class);
            i.putExtra("Results", res);
            startActivity(i);
        }
    }*/
}

