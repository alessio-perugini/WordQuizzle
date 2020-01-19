package server.MyMemoryAPI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseData {
    private String translatedText;
    private double match;

    @JsonProperty("translatedText")
    public String getTranslatedText() {
        return translatedText;
    }

    @JsonProperty("translatedText")
    public void setTranslatedText(String value) {
        this.translatedText = value;
    }

    @JsonProperty("match")
    public double getMatch() {
        return match;
    }

    @JsonProperty("match")
    public void setMatch(double value) {
        this.match = value;
    }
}
