package com.example.divyansh.googleapivoice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PredictionGridAdapter extends ArrayAdapter<PredictionModel> {
    public PredictionGridAdapter(@NonNull Context context, ArrayList<PredictionModel> predictedWordArrayList){
        super(context, 0, predictedWordArrayList);
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
        predicted_word.setText(predictionModel.get_word());
        predicted_image.setImageResource(predictionModel.getImgid());
        return listitemView;

    }

}
