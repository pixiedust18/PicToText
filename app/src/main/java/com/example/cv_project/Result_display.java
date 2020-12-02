package com.example.cv_project;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import android.speech.tts.TextToSpeech;

import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Result_display extends AppCompatActivity {
    private static final int STORAGE_CODE = 1000;
    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_result_display );
        Intent intent = getIntent();
        String res = intent.getExtras().getString("Results");
        final TextView result_text = (TextView) findViewById(R.id.txt_result);
        result_text.setText(res);
        result_text.setGravity( Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL);
        result_text.setMovementMethod(new ScrollingMovementMethod());
        /*ScrollView scrollView = (ScrollView)  findViewById(R.id.scrollView);
        if(result_text.getParent() != null) {
            ((ViewGroup)result_text.getParent()).removeView(result_text); // <- fix
        }
        //scrollView.setText(res);
        LinearLayout ll = (LinearLayout) findViewById(R.id.layoutinside);
        ll.addView(result_text);*/

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        Button back = (Button) findViewById( R.id.button );
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent activityChangeIntent = new Intent(Result_display.this, MainActivity.class);

                // currentContext.startActivity(activityChangeIntent);

                Result_display.this.startActivity(activityChangeIntent);
            }
        });

        Button save_pdf = (Button) findViewById( R.id.button2 );
        save_pdf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                    //system OS >= Marshmallow(6.0), check if permission is enabled or not
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED){
                        //permission was not granted, request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, STORAGE_CODE);
                    }
                    else {
                        //permission already granted, call save pdf method
                        savePdf();
                    }
                }
                else {
                    //system OS < Marshmallow, call save pdf method
                    savePdf();
                }
            }
        });
        Button text_speech = (Button) findViewById( R.id.button3 );
        text_speech.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String toSpeak = result_text.getText().toString();
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    private void savePdf() {
        //create object of Document class
        Document mDoc = new Document();
        //pdf file name
        String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(System.currentTimeMillis());
        //pdf file path
        String mFilePath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".pdf";
        String mtxtPath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".txt";

        try {

            TextView result_text = (TextView) findViewById(R.id.txt_result);
            String mText = result_text.getText().toString();

            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //open the document for writing
            mDoc.open();
            //get text from EditText i.e. mTextEt


            //add author of the document (optional)
            mDoc.addAuthor("CV_app");

            //add paragraph to the document
            mDoc.add(new Paragraph(mText));

            //close the document
            mDoc.close();

            /*FileOutputStream fos = new FileOutputStream(mtxtPath);
            fos.write(mText.toString().getBytes());
            fos.close();*/

            Log.d("Pdf saved:", mFileName +".pdf\nis saved to\n"+ mFilePath);
            //show message that file is saved, it will show file name and file path too
            Toast.makeText(this, mFileName +".pdf\nis saved to\n"+ mFilePath, Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            //if any thing goes wrong causing exception, get and show exception message
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("save pdf:", e.getMessage());
            e.printStackTrace();
        }

        /*try {
            FileInputStream fis = new FileInputStream(mtxtPath);
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

        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }


    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission was granted from popup, call savepdf method
                    savePdf();
                }
                else {
                    //permission was denied from popup, show error message
                    Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
}
