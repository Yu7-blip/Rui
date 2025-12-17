package com.auth.dto;

public class RecommendationRequest {
    private String userPreference;
    private String weather;
    private String mood;

    // 默认构造函数
    public RecommendationRequest() {}

    // 带参数构造函数
    public RecommendationRequest(String userPreference, String weather, String mood) {
        this.userPreference = userPreference;
        this.weather = weather;
        this.mood = mood;
    }

    // Getter和Setter
    public String getUserPreference() {
        return userPreference;
    }

    public void setUserPreference(String userPreference) {
        this.userPreference = userPreference;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
}