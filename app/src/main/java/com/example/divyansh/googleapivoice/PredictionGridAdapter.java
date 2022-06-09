package com.example.divyansh.googleapivoice;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class PredictionGridAdapter extends ArrayAdapter<PredictionModel> {
    Typeface typeface;
    Boolean store_img;
    String store_fontsize;
    String store_font;
    public static final String KEY_THEME = "Theme options";
    public static final String KEY_IMG = "Show images under word suggestions";
    public static final String KEY_FONT = "Font";
    public static final String KEY_FONTSIZE ="Font size";
    public static final String KEY_NOSUGG ="Number of word suggestions";

    public PredictionGridAdapter(@NonNull Context context, ArrayList<PredictionModel> predictedWordArrayList){
        super(context, 0, predictedWordArrayList);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        store_font = sharedPreferences.getString(KEY_FONT, "Montserrat");
        switch (store_font) {
            case "Montserrat":
                typeface = ResourcesCompat.getFont(context, R.font.montserratmed);
                break;
            case "Calibri":
                typeface = ResourcesCompat.getFont(context, R.font.calibri);
                break;
            case "Arial":
                typeface = ResourcesCompat.getFont(context, R.font.arial);
                break;
            case "Helvetica":
                typeface = ResourcesCompat.getFont(context, R.font.helvetica);
                break;
        }

        store_img = sharedPreferences.getBoolean(KEY_IMG, true);
        store_fontsize = sharedPreferences.getString(KEY_FONTSIZE, "Medium");

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View listitemView = convertView;

        if (listitemView == null){
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.card_item, parent, false);
        }

        PredictionModel predictionModel = getItem(position);
        TextView predicted_word = listitemView.findViewById(R.id.idPredWord);
        ImageView predicted_image = listitemView.findViewById(R.id.idPredImage);

        //font of text
        predicted_word.setText(predictionModel.get_word());
        predicted_word.setTypeface(typeface);

        //font size of text
        switch (store_fontsize) {
            case "Small":
                if(store_font.equals("Montserrat")){
                    predicted_word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                }
                else{
                    predicted_word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);//speech rec text
                }
                break;
            case "Medium":
                if(store_font.equals("Montserrat")){
                    predicted_word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                }
                else{
                    predicted_word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                }
                break;
            case "Large":
                if(store_font.equals("Montserrat")){
                    predicted_word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                }
                else{
                    predicted_word.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                }
                break;
        }

        //get and check whether image should be shown
        predicted_image.setImageResource(predictionModel.getImgid());
        if (store_img){
            predicted_image.setVisibility(View.VISIBLE);
        }
        else{
            predicted_image.setVisibility(View.INVISIBLE);
        }

        return listitemView;

    }

}
