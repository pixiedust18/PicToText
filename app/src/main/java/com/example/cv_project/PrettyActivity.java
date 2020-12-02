package com.example.cv_project;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class PrettyActivity extends AppCompatActivity {
    TextToSpeech t1;
    TextView result_text;
    String sign;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_pretty );
        Intent intent = getIntent();
        String res = intent.getExtras().getString("Results");
        sign = intent.getExtras().getString("Sign");
        result_text = (TextView) findViewById(R.id.result_display_f);
        result_text.setText(res);
        result_text.setGravity( Gravity.CENTER_VERTICAL| Gravity.CENTER_HORIZONTAL);
        result_text.setMovementMethod(new ScrollingMovementMethod());
        auth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById( R.id.toolbar );
        toolbar.setTitle( "Text Editor" );

        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        final CharSequence[] options = { "Save as pdf", "Save as txt file", "Read doc", "Add signature ", "Cancel" };
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(PrettyActivity.this);

        FloatingActionButton fab = findViewById( R.id.fab1 );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Menu:");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    AlertDialog dialog2;
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                dialog.dismiss();
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(PrettyActivity.this);
                                builder2.setTitle("File Name");

                                ;
                                // set the custom layout
                                final View customLayout = getLayoutInflater().inflate(R.layout.custom_layout, null);
                                builder2.setView(customLayout);
                                // add a button
                                builder2.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // send data from the AlertDialog to the Activity
                                        EditText editText = customLayout.findViewById(R.id.editText);
                                        savePdf(editText.getText().toString());
                                        dialog2.dismiss();
                                    }
                                });
                                dialog2 = builder2.create();
                                dialog2.show();
                                // create and show the alert dialog
                            break;
                            case 1:
                                dialog.dismiss();
                                AlertDialog.Builder builder3 = new AlertDialog.Builder(PrettyActivity.this);
                                builder3.setTitle("File Name");

                                ;
                                // set the custom layout
                                final View customLayout2 = getLayoutInflater().inflate(R.layout.custom_layout, null);
                                builder3.setView(customLayout2);
                                // add a button
                                builder3.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // send data from the AlertDialog to the Activity
                                        EditText editText = customLayout2.findViewById(R.id.editText);
                                        saveTxt(editText.getText().toString());
                                        dialog2.dismiss();
                                    }
                                });
                                dialog2 = builder3.create();
                                dialog2.show();
                                // create and show the alert dialog
                            case 2:
                                String toSpeak = result_text.getText().toString();
                                Toast.makeText(getApplicationContext(), "Content is now being read",Toast.LENGTH_SHORT).show();
                                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                dialog.dismiss();
                            break;
                            case 3:
                                add_sign();
                                dialog.dismiss();
                                break;
                            case 4:
                                dialog.dismiss();
                                break;

                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        } );
    }
    private void add_sign()
    {
        String main_str = result_text.getText().toString();
        String f = main_str+"\n"+sign;
        result_text.setText( f );
    }
    private void saveTxt(String mFileName) {
        //create object of Document class
        Document mDoc = new Document();
        //pdf file name
        //String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss",
        //        Locale.getDefault()).format(System.currentTimeMillis());
        //pdf file path
        String mFilePath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".pdf";
        String mtxtPath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".txt";

        try {

            TextView result_text = (TextView) findViewById( R.id.result_display_f );
            String mText = result_text.getText().toString();

            //create instance of PdfWriter class
            /*PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //open the document for writing
            mDoc.open();
            //get text from EditText i.e. mTextEt


            //add author of the document (optional)
            mDoc.addAuthor("CV_app");

            //add paragraph to the document
            mDoc.add(new Paragraph(mText));

            //close the document
            mDoc.close();*/

            FileOutputStream fos = new FileOutputStream( mtxtPath );
            fos.write( mText.toString().getBytes() );
            fos.close();

            Log.d( "Txt saved:", mFileName + ".pdf\nis saved to\n" + mtxtPath );
            //show message that file is saved, it will show file name and file path too
            Toast.makeText( this, mFileName + ".txt\nis saved to\n" + mtxtPath, Toast.LENGTH_LONG ).show();
        } catch (Exception e) {
            //if any thing goes wrong causing exception, get and show exception message
            Toast.makeText( this, e.getMessage(), Toast.LENGTH_SHORT ).show();
            Log.e( "save pdf:", e.getMessage() );
            e.printStackTrace();
        }
    }
    private void savePdf(String mFileName) {
        //create object of Document class
        Document mDoc = new Document();
        //pdf file name
        //String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss",
        //        Locale.getDefault()).format(System.currentTimeMillis());
        //pdf file path
        String mFilePath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".pdf";
        String mtxtPath = Environment.getExternalStorageDirectory() + "/" + mFileName + ".txt";

        try {

            TextView result_text = (TextView) findViewById(R.id.result_display_f);
            String mText = result_text.getText().toString();

            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, new FileOutputStream(mFilePath));
            //open the document for writing
            mDoc.open();
            //get text from EditText i.e. mTextEt


            //add author of the document (optional)
            FirebaseUser currentUser = auth.getCurrentUser();

            mDoc.addAuthor(currentUser.getDisplayName());

            //add paragraph to the document
            mDoc.add(new Paragraph(mText));

            //close the document
            mDoc.close();

            /*FileOutputStream fos = new FileOutputStream(mtxtPath);
            fos.write(mText.toString().getBytes());
            fos.close();*/

            Log.d("Txt saved:", mFileName +".txt\nis saved to\n"+ mFilePath);
            //show message that file is saved, it will show file name and file path too
            Toast.makeText(this, mFileName +".pdf\nis saved to\n"+ mFilePath, Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            //if any thing goes wrong causing exception, get and show exception message
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("save txt:", e.getMessage());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
