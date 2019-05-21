import org.apache.log4j.Logger;

import java.io.*;


public class MigrationScriptGen {
    static Logger logger = LoggerFactory.getLoggerInstance(MigrationScriptGen.class.getName());

    static RequestParams inputParams;
    static HttpClient client;


    public static void main(String[] args) throws IOException {
        String csvFileInput = args[0];
        String csvFileOutput = args[1];
        String channel = args[2];
        String rootOrgId = args[3];
        String orgName = args[4];
        String apiKey = args[5];
        String authToken = args[6];
        String baseUrl = args[7];
        inputParams = new RequestParams(csvFileInput, csvFileOutput, channel, rootOrgId, orgName, apiKey, authToken, baseUrl);
        client = new HttpClient(authToken, apiKey);
        CsvManager manager=new CsvManager(inputParams);
        manager.processCsv();


    }
}