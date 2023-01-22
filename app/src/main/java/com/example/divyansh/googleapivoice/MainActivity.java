package com.example.divyansh.googleapivoice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import com.androidnetworking.AndroidNetworking;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.body.MultipartBody;
import com.mashape.unirest.request.body.RequestBodyEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


import okhttp3.OkHttpClient;
import okhttp3.Request;

import android.os.AsyncTask;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {
    //Speech recognition
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView returnedText;
    private ImageView bigPause;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    //UI
    private FloatingActionButton pauseButton;
    private FloatingActionButton playButton;
    private FloatingActionButton settingsButton;
    //WordGrid
    private GridView wordGrid;
    ArrayList<String> predictions = new ArrayList<>();
    //final int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4};
    //Preferences
    String store_theme;
    String store_font;
    String store_fontsize;
    int store_nosugg = 3;
    public static final String KEY_THEME = "Theme options";
    public static final String KEY_IMG = "Show images under word suggestions";
    public static final String KEY_FONT = "Font";
    public static final String KEY_FONTSIZE = "Font size";
    public static final String KEY_NOSUGG = "Number of word suggestions";

    ArrayList<PredictionModel> predictionModelArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout1);

        // UI initialisation
        returnedText = findViewById(R.id.textView1);
        bigPause = findViewById(R.id.indicator_pause);
        pauseButton = findViewById(R.id.pauseButton);
        playButton = findViewById(R.id.playButton);
        playButton.hide();
        bigPause.setVisibility(View.INVISIBLE);
        settingsButton = findViewById(R.id.plusButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();

            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onStop();
                pauseButton.hide();
                playButton.show();
                bigPause.setVisibility(View.VISIBLE);
            }

        });

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onResume();
                playButton.hide();
                bigPause.setVisibility(View.INVISIBLE);
                pauseButton.show();
            }
        });

        bigPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onResume();
                playButton.hide();
                bigPause.setVisibility(View.INVISIBLE);
                pauseButton.show();
            }
        });


        //checking initial preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        store_theme = sharedPreferences.getString(KEY_THEME, "Light");
        if (store_theme.equals("Light")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (store_theme.equals("Dark")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        //change font of speech recognition text
        store_font = sharedPreferences.getString(KEY_FONT, "Montserrat");
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratmed);

        switch (store_font) {
            case "Montserrat":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratmed);
                break;
            case "Calibri":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.calibri);
                break;
            case "Arial":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.arial);
                break;
            case "Helvetica":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.helvetica);
                break;
        }
        returnedText.setTypeface(typeface);

        //change font size of speech recognition text and word grid
        wordGrid = findViewById(R.id.wordGrid);

        store_fontsize = sharedPreferences.getString(KEY_FONTSIZE, "Medium");

        switch (store_fontsize) {
            case "Small":
                returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);//speech rec text
                wordGrid.setVerticalSpacing(20);
                break;
            case "Medium":
                returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
                wordGrid.setVerticalSpacing(30);
                break;
            case "Large":
                returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                wordGrid.setVerticalSpacing(50);
                break;
        }

        //initial word grid set up (not sure if i should leave it empty)
//        int nosugg = sharedPreferences.getInt("no_sugg_key", 6);
//        Log.d("no_sugg ", Integer.toString(nosugg));
//        makeWordGrid(predictions, nosugg);

        // initialise package that simplifies API calls
        AndroidNetworking.initialize(getApplicationContext());

        // start speech recogniser
        resetSpeechRecognizer();

        // check for permission
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        setRecogniserIntent();
        speech.startListening(recognizerIntent);
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, ArrayList<String>> {
        private ArrayList<String> asyncPredictions = new ArrayList<>();
        private Integer no_sugg;
        @Override
        protected ArrayList<String> doInBackground(String[] params) {
            no_sugg = Integer.parseInt(params[1]);
            HttpResponse<JsonNode> httpResponse = null;
            try {
                httpResponse = Unirest.post("https://api.openai.com/v1/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer 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")
//                        .field("model", "text-davinci-002")
//                        .field("prompt", params[0])
//                        .field("temperature", 0.29)
//                        .field("top_p", 1)
//                        .field("frequency_penalty", 0)
//                        .field("presence_penalty", 0)
//                        .field("max_tokens", 5)
//                        .field("logprobs", 10)
                    .body("{\"model\":\"text-davinci-002\",\"prompt\":\""+params[0]+"\",\"temperature\":0.29,\"top_p\":1,\"frequency_penalty\":0,\"presence_penalty\":0," +
                            "\"max_tokens\":5, \"logprobs\":10}")
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            Log.d("httpResponse", String.valueOf(httpResponse));
            assert httpResponse != null;

            //extract JSONArray called "top_logprobs" containing the top predicted words
            JSONObject responseObject = httpResponse.getBody().getObject();
            Log.i("response", String.valueOf(responseObject));
            JSONArray choices = new JSONArray();
            try {
                choices = responseObject.getJSONArray("choices");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject choicesObj = new JSONObject();
            try {
                 choicesObj = choices.getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject logprobs = new JSONObject();
            try {
                logprobs = choicesObj.getJSONObject("logprobs");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONArray top_logprobs = new JSONArray();
            try {
                top_logprobs = logprobs.getJSONArray("top_logprobs");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject top_predictions = new JSONObject();
            JSONObject top_predictions_secondword = new JSONObject();

            try {
                top_predictions = top_logprobs.getJSONObject(0);
                top_predictions_secondword = top_logprobs.getJSONObject(1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //convert JSONArray to ArrayList<String>
            assert (top_predictions != null);
            assert (top_predictions_secondword != null);

            Iterator<String> keys = top_predictions.keys();
            Iterator<String> keys_second = top_predictions_secondword.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                if (!Pattern.matches("[\\p{Punct}\\p{IsPunctuation}]|\n+", key)  && !key.equals("<|endoftext|>") && !key.equals("\")")) {
                    asyncPredictions.add(key);
                    }

            }

            while(keys_second.hasNext()) {
                String key = keys_second.next();
                if (!Pattern.matches("[\\p{Punct}\\p{IsPunctuation}]|\n+", key)  && !key.equals("<|endoftext|>") && !key.equals("\")")) {
                        asyncPredictions.add(key);
                }
            }

            Log.i("predictions", String.valueOf(asyncPredictions));
            return asyncPredictions;
        }

        @Override
        protected void onPostExecute(ArrayList<String> asyncPredictions) {
            super.onPostExecute(asyncPredictions);
            makeWordGrid(asyncPredictions, no_sugg);
            predictions = asyncPredictions;
        }
    }

    public void makeWordGrid(ArrayList<String> predictedWords, int no_suggestions){
        //cut down predicted words array based on the settings preference
        if (predictedWords.size()>no_suggestions){
            predictedWords.subList(0, no_suggestions);
        }

        //initialise and populate prediction array list
        predictionModelArrayList = new ArrayList<PredictionModel>();
        for (String predictedWord : predictedWords) {
            predictionModelArrayList.add((new PredictionModel(predictedWord, R.drawable.wordsmith_logo_xml)));
        }
        //make new adapter and apply to grid
        PredictionGridAdapter adapter = new PredictionGridAdapter(this, predictionModelArrayList);
        wordGrid.setAdapter(adapter);
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onError(int errorCode) {
        CharSequence errorMessage = (CharSequence) getErrorText(errorCode);
        int duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();

        Log.i(LOG_TAG, "FAILED " + errorMessage);
        if (!errorMessage.equals("No match")) {
            Toast toast = Toast.makeText(context, errorMessage, duration);
            toast.setGravity(Gravity.CENTER, 0, 500);
            toast.show();
        }

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

        //Resume after returning from settings - this checks new preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        //change theme colour of main activity
        store_theme = sharedPreferences.getString(KEY_THEME, "");

        if (store_theme.equals("Light")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (store_theme.equals("Dark")){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        //change font of speech recognition text
        store_font = sharedPreferences.getString(KEY_FONT, "Montserrat");
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratmed);

        switch (store_font) {
            case "Montserrat":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.montserratmed);
                break;
            case "Calibri":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.calibri);
                break;
            case "Arial":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.arial);
                break;
            case "Helvetica":
                typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.helvetica);
                break;
        }
        returnedText.setTypeface(typeface);

        //change font size of speech recognition text
        store_fontsize = sharedPreferences.getString(KEY_FONTSIZE, "Medium");

        switch (store_fontsize) {
            case "Small":
                returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);//speech rec text
                wordGrid.setVerticalSpacing(20);
                break;
            case "Medium":
                returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
                wordGrid.setVerticalSpacing(30);
                break;
            case "Large":
                returnedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                wordGrid.setVerticalSpacing(50);
                break;
        }

        store_nosugg = sharedPreferences.getInt("no_sugg", 6);
//        Log.d("no_sugg ", String.valueOf(store_nosugg));

        //reset Predicted word grid (update font, fontsize, showing images, and number of sugg)
        makeWordGrid(predictions, store_nosugg);

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

    public static String last10Words(String input) {
        String[] words = input.split("\\s+");
        if (words.length>10){
            String output = "";
            for(int i = words.length-1; i>words.length-11;i--){
                output += words[i] + " ";
            }
            return output.toString();
        }
        else{
            return input;
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String bestMatch = matches.get(0);
        returnedText.setText(last10Words(bestMatch));

//      Getting predicted words
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Integer nosugg = sharedPreferences.getInt("no_sugg", 6);
        Log.d("no_sugg ", Integer.toString(nosugg));

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(bestMatch, String.valueOf(2));

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

        return (String) text;
    }

}




