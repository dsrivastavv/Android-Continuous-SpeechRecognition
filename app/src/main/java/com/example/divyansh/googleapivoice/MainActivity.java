package com.example.divyansh.googleapivoice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import com.androidnetworking.AndroidNetworking;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {
    //Speech recognition
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView returnedText;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    //UI
    private ImageButton pauseButton;
    private ImageButton playButton;
    private ImageButton settingsButton;
    private TextView showImagesText;
    //WordGrid
    private GridView wordGrid;
    String[] predictions = {};
    final int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4};
    //Preferences
    String store_theme;
    boolean store_showimg;
    String store_font;
    String store_fontsize;
    public static final String KEY_THEME = "Theme options";
    public static final String KEY_IMG = "Show images under word suggestions";
    public static final String KEY_FONT = "Font";
    public static final String KEY_FONTSIZE ="Font size";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout1);

        // UI initialisation
        returnedText = findViewById(R.id.textView1);
        progressBar = findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);
        pauseButton = findViewById(R.id.pauseButton);
        playButton = findViewById(R.id.playButton);
        playButton.setVisibility(View.INVISIBLE);
        settingsButton = findViewById(R.id.plusButton);
        showImagesText = findViewById(R.id.textView2);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();

            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStop();
                pauseButton.setVisibility(View.INVISIBLE);
                playButton.setVisibility(View.VISIBLE);
            }

        });

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onResume();
                playButton.setVisibility(View.INVISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
            }
        });

        //checking initial preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        store_showimg = sharedPreferences.getBoolean(KEY_IMG, true);
        //Log.d("showimg on start up", Boolean.toString(store_showimg));
        if (store_showimg){
            showImagesText.setVisibility(View.VISIBLE);
        }
        else{
            showImagesText.setVisibility((View.INVISIBLE));
        }

        store_theme = sharedPreferences.getString(KEY_THEME, "Light");
        Log.d("Bg start main", store_theme);
        if (store_theme.equals("Light")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (store_theme.equals("Dark")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        store_font = sharedPreferences.getString(KEY_FONT, "Montserrat");

        store_fontsize = sharedPreferences.getString(KEY_FONTSIZE, "Medium");

        //word grid set up
        wordGrid = findViewById(R.id.wordGrid);

        GridAdapter gridAdapter = new GridAdapter(this, predictions, images);

        wordGrid.setAdapter(gridAdapter);

        // initialise package that simplifies API calls
        AndroidNetworking.initialize(getApplicationContext());

        // start speech recogniser
        resetSpeechRecognizer();

        // start progress bar
        progressBar.setIndeterminate(true);

        // check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setRecogniserIntent();
        speech.startListening(recognizerIntent);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        progressBar.setProgress((int) rmsdB);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.i(LOG_TAG, "FAILED " + errorMessage);
//        if (!errorMessage.equals("No match")) {
//            returnedText.setText(errorMessage);
//        }

        // rest voice recogniser
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    private void resetSpeechRecognizer() {
        if (speech != null)
            speech.destroy();
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        if (SpeechRecognizer.isRecognitionAvailable(this))
            speech.setRecognitionListener(this);
        else
            finish();
    }

    private void setRecogniserIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                speech.startListening(recognizerIntent);
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                        .LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "resume");
        super.onResume();

        //Resume after returning from settings - check new preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        //preference 1: show or don't show images
        store_showimg = sharedPreferences.getBoolean(KEY_IMG, true);
        //Log.d("showimg resume main", Boolean.toString(store_showimg));
        if (store_showimg) {
            showImagesText.setVisibility(View.VISIBLE);
        } else {
            showImagesText.setVisibility((View.INVISIBLE));
        }

        //preference 2: change theme colour of main activity
        store_theme = sharedPreferences.getString(KEY_THEME, "");

        if (store_theme.equals("Light")){
            Log.d("resume main bg", store_theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (store_theme.equals("Dark")){
            Log.d("resume main bg", store_theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        //preference 3: change font
        store_font = sharedPreferences.getString(KEY_FONT, "Montserrat");
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratmed);

        if (store_font.equals("Montserrat")) {
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratmed);
        }
        else if (store_font.equals("Calibri")){
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.calibri);
        }
        else if (store_font.equals("Arial")){
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.arial);
        }
        else if (store_font.equals("Helvetica")){
            typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.helvetica);
        }
        returnedText.setTypeface(typeface);

        store_fontsize = sharedPreferences.getString(KEY_FONTSIZE, "Medium");
        if (store_fontsize.equals("Small")){
            returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP,25);
        }
        else if (store_fontsize.equals("Medium")){
            returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP,35);

        }
        else if (store_fontsize.equals("Large")){
            returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP,45);

        }

        //Resume speech recognition
        resetSpeechRecognizer();
        speech.startListening(recognizerIntent);
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG, "pause");
        super.onPause();
        speech.stopListening();
    }


    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "stop");
        super.onStop();
        if (speech != null) {
            speech.destroy();
        }
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        speech.stopListening();
    }

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";
        }

        returnedText.setText(matches.get(0));

        // call back end here to get predicted words
//        try {
//            run("www.google.com");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        String[] predictions = {
                "Dog", "Cat", "Glass", "Sloth", "Washing", "Name", "Like", "Run", "Know"
        };

        // API calls to get an image for each word
//        final String[] image_files = {"image1.xml", "image2.xml", "image3.xml", "image4.xml", "image5.xml", "image6.xml", "image7.xml", "image8.xml", "image9.xml"};
//        final int[] image_drawables = this.images;
//        for (int i = 0; i<predictions.length - 1 ; i++){
//            final int finalI = i;
//            AndroidNetworking.get("https://www.opensymbols.org/api/v2/symbols")
//                    .addQueryParameter("q", predictions[i])
//                    .build()
//                    .getAsJSONArray(new JSONArrayRequestListener() {
//                        @Override
//                        public void onResponse(JSONArray response) {
//                            String image_url = null;
//                            try {
//                                // use the first image provided by API
//                                image_url = response.getJSONObject(0).getString("image_url");
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                saveImage(image_url, image_files[finalI]);
//                                Drawable.createFromXml(image_drawables[finalI], image_files[finalI]);
//
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        @Override
//                        public void onError(ANError error) {
//                            // handle error
//                        }
//                    });
//        }
        speech.startListening(recognizerIntent);
    }

    public String getErrorText(int errorCode) {
        Context context = getApplicationContext();
        CharSequence text;
        int duration = Toast.LENGTH_SHORT;
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                text = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                text = "Client side error.";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                text = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                text = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                text = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                text = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                text = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                text = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                text = "No speech input";
                break;
            default:
                text = "Didn't understand, please try again.";
                break;
        }

        Toast.makeText(context, text, duration).show();
        return (String) text;
    }



}
