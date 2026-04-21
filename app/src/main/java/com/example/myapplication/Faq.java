package com.example.myapplication;

import java.io.Serializable;

public class Faq implements Serializable {
    private String id;
    private String question;
    private String answer;
    private String imageUrl;

    public Faq() {}

    public Faq(String id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.imageUrl = "";
    }

    public Faq(String id, String question, String answer, String imageUrl) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
