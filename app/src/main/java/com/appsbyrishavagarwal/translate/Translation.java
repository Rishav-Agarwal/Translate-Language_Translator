package com.appsbyrishavagarwal.translate;

public class Translation {

    private int id;
    private String from_lang;
    private String from_text;
    private String to_lang;
    private String to_text;
    private String date;

    public Translation() {
    }

    public Translation(int id, String from_lang, String from_text, String to_lang, String to_text, String date) {
        this.id = id;
        this.from_lang = from_lang;
        this.from_text = from_text;
        this.to_lang = to_lang;
        this.to_text = to_text;
        this.date = date;
    }

    public Translation(String from_lang, String from_text, String to_lang, String to_text, String date) {
        this.from_lang = from_lang;
        this.from_text = from_text;
        this.to_lang = to_lang;
        this.to_text = to_text;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getFrom_lang() {
        return from_lang;
    }

    public String getTo_lang() {
        return to_lang;
    }

    public String getFrom_text() {
        return from_text;
    }

    public String getTo_text() {
        return to_text;
    }

    public String getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFrom_lang(String from_lang) {
        this.from_lang = from_lang;
    }

    public void setTo_lang(String to_lang) {
        this.to_lang = to_lang;
    }

    public void setFrom_text(String from_text) {
        this.from_text = from_text;
    }

    public void setTo_text(String to_text) {
        this.to_text = to_text;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
