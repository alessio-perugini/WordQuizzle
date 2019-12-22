package server.MyMemoryAPI;

import java.util.*;
import com.fasterxml.jackson.annotation.*;

public class MyMemoryResponse {
    private ResponseData responseData;
    private boolean quotaFinished;
    private Object mtLangSupported;
    private String responseDetails;
    private long responseStatus;
    private String responderID;
    private Object exceptionCode;
    private Match[] matches;

    @JsonProperty("responseData")
    public ResponseData getResponseData() { return responseData; }
    @JsonProperty("responseData")
    public void setResponseData(ResponseData value) { this.responseData = value; }

    @JsonProperty("quotaFinished")
    public boolean getQuotaFinished() { return quotaFinished; }
    @JsonProperty("quotaFinished")
    public void setQuotaFinished(boolean value) { this.quotaFinished = value; }

    @JsonProperty("mtLangSupported")
    public Object getMTLangSupported() { return mtLangSupported; }
    @JsonProperty("mtLangSupported")
    public void setMTLangSupported(Object value) { this.mtLangSupported = value; }

    @JsonProperty("responseDetails")
    public String getResponseDetails() { return responseDetails; }
    @JsonProperty("responseDetails")
    public void setResponseDetails(String value) { this.responseDetails = value; }

    @JsonProperty("responseStatus")
    public long getResponseStatus() { return responseStatus; }
    @JsonProperty("responseStatus")
    public void setResponseStatus(long value) { this.responseStatus = value; }

    @JsonProperty("responderId")
    public String getResponderID() { return responderID; }
    @JsonProperty("responderId")
    public void setResponderID(String value) { this.responderID = value; }

    @JsonProperty("exception_code")
    public Object getExceptionCode() { return exceptionCode; }
    @JsonProperty("exception_code")
    public void setExceptionCode(Object value) { this.exceptionCode = value; }

    @JsonProperty("matches")
    public Match[] getMatches() { return matches; }
    @JsonProperty("matches")
    public void setMatches(Match[] value) { this.matches = value; }
}
