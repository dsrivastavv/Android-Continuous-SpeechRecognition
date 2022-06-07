package com.example.divyansh.googleapivoice;

public class PredictionModel {
    // string predicted_word for storing predicted_word
    // and imgid for storing image id.
    private String predicted_word;
    private int imgid;

    public PredictionModel(String predicted_word, int imgid) {
        this.predicted_word = predicted_word;
        this.imgid = imgid;
    }

    public String get_word() {
        return predicted_word;
    }

    public void set_word(String predicted_word) {
        this.predicted_word = predicted_word;
    }

    public int getImgid() {
        return imgid;
    }

    public void setImgid(int imgid) {
        this.imgid = imgid;
    }
}
