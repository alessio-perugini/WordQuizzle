package server.MyMemoryAPI;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Match {
    private ID id;
    private String segment;
    private String translation;
    private ID quality;
    private String reference;
    private long usageCount;
    private Subject subject;
    private String createdBy;
    private String lastUpdatedBy;
    private String createDate;
    private String lastUpdateDate;
    private double match;
    private String model;

    @JsonProperty("id")
    public ID getID() {
        return id;
    }

    @JsonProperty("id")
    public void setID(ID value) {
        this.id = value;
    }

    @JsonProperty("segment")
    public String getSegment() {
        return segment;
    }

    @JsonProperty("segment")
    public void setSegment(String value) {
        this.segment = value;
    }

    @JsonProperty("translation")
    public String getTranslation() {
        return translation;
    }

    @JsonProperty("translation")
    public void setTranslation(String value) {
        this.translation = value;
    }

    @JsonProperty("quality")
    public ID getQuality() {
        return quality;
    }

    @JsonProperty("quality")
    public void setQuality(ID value) {
        this.quality = value;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("reference")
    public void setReference(String value) {
        this.reference = value;
    }

    @JsonProperty("usage-count")
    public long getUsageCount() {
        return usageCount;
    }

    @JsonProperty("usage-count")
    public void setUsageCount(long value) {
        this.usageCount = value;
    }

    @JsonProperty("subject")
    public Subject getSubject() {
        return subject;
    }

    @JsonProperty("subject")
    public void setSubject(Subject value) {
        this.subject = value;
    }

    @JsonProperty("created-by")
    public String getCreatedBy() {
        return createdBy;
    }

    @JsonProperty("created-by")
    public void setCreatedBy(String value) {
        this.createdBy = value;
    }

    @JsonProperty("last-updated-by")
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    @JsonProperty("last-updated-by")
    public void setLastUpdatedBy(String value) {
        this.lastUpdatedBy = value;
    }

    @JsonProperty("create-date")
    public String getCreateDate() {
        return createDate;
    }

    @JsonProperty("create-date")
    public void setCreateDate(String value) {
        this.createDate = value;
    }

    @JsonProperty("last-update-date")
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    @JsonProperty("last-update-date")
    public void setLastUpdateDate(String value) {
        this.lastUpdateDate = value;
    }

    @JsonProperty("match")
    public double getMatch() {
        return match;
    }

    @JsonProperty("match")
    public void setMatch(double value) {
        this.match = value;
    }

    @JsonProperty("model")
    public String getModel() {
        return model;
    }

    @JsonProperty("model")
    public void setModel(String value) {
        this.model = value;
    }
}
