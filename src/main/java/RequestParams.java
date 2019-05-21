public class RequestParams {
    private String csvFileInput;
    private String csvFileOutput;
    private String channel;
    private String rootOrgId;
    private String orgName;
    private String apiKey;
    private String authToken;
    private String baseUrl;
    private RequestParams() {
    }

    public RequestParams(String csvFileInput, String csvFileOutput, String channel, String rootOrgId, String orgName, String apiKey, String authToken,String baseUrl) {
        this.csvFileInput = csvFileInput;
        this.csvFileOutput = csvFileOutput;
        this.channel = channel;
        this.rootOrgId = rootOrgId;
        this.orgName = orgName;
        this.apiKey = apiKey;
        this.authToken = authToken;
        this.baseUrl=baseUrl;
    }

    public String getCsvFileInput() {
        return csvFileInput;
    }

    public void setCsvFileInput(String csvFileInput) {
        this.csvFileInput = csvFileInput;
    }

    public String getCsvFileOutput() {
        return csvFileOutput;
    }

    public void setCsvFileOutput(String csvFileOutput) {
        this.csvFileOutput = csvFileOutput;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRootOrgId() {
        return rootOrgId;
    }

    public void setRootOrgId(String rootOrgId) {
        this.rootOrgId = rootOrgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
