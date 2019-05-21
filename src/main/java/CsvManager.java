import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CsvManager {

    RequestParams requestParams;
    HttpClient client;
    FileWriter fw;
    BufferedReader br = null;
    Logger logger = LoggerFactory.getLoggerInstance(MigrationScriptGen.class.getName());
    SearchUtil searchUtil;
    QueryBuilder queryBuilder;

    public CsvManager(RequestParams params) {
        this.requestParams = params;
        client = new HttpClient(requestParams.getAuthToken(), requestParams.getApiKey());
        searchUtil = new SearchUtil(requestParams, client);
        queryBuilder = new QueryBuilder(requestParams);
    }

    public void processCsv() throws IOException {
        fw = new FileWriter(requestParams.getCsvFileOutput());
        String line = "";
        String cvsSplitBy = ",";
        if (FileValidator.isValidFile(requestParams.getCsvFileInput(), requestParams.getCsvFileOutput())) {
            try {
                br = getReaderObject(requestParams.getCsvFileInput());
                if (br != null) {
                    while ((line = br.readLine()) != null) {
                        if (line.length() != 0) {
                            String[] values = line.split(cvsSplitBy);
                            logger.info("value got " + values[0]);
                            processQueryWriteToFile(values[0], values[1], values[2]);
                        } else {
                            logger.error("Cant write query to file");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("error " + e.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                        fw.close();
                        logger.info("Connection Closed");
                    } catch (Exception e) {
                        logger.error("error in closing connection " + e.getMessage());
                    }
                }
            }
        } else {
            logger.info("please provide valid file paths");
        }
    }

    public void writeQueryToFile(String query) {
        try {
            fw.write(query + "\n");
            logger.info("Query written to file " + query);
        } catch (IOException e) {
            logger.error("Error occured while writing query to file " + e.getMessage());
        }
    }


    public BufferedReader getReaderObject(String pathToInputFile) {
        try {
            br = new BufferedReader(new FileReader(pathToInputFile));
            br.readLine();        // escaping headers in file headers
            return br;

        } catch (FileNotFoundException e) {
            logger.error("file doesnt exists " + pathToInputFile);
            return null;

        } catch (IOException e) {
            logger.error("Excetion occured " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getUserDetails(String userName) throws IOException {
        logger.info("in getuserdetails username is" + userName);
        Map<String, Object> userMap = searchUtil.userSearch(userName);
        Map<String, Object> userLimitedDetails = new HashMap<>();
        userLimitedDetails.put("id", userMap.get("id"));
        userLimitedDetails.put("organisations", userMap.get("organisations"));
        logger.info(Collections.singletonList(userMap.toString()));
        logger.info(Collections.singletonList(userLimitedDetails.toString()));
        return userLimitedDetails;
    }

    public Map<String, Object> getOrgDetails(String schoolId) throws IOException {
        logger.info("in getorgdetails username is" + schoolId);
        Map<String, Object> orgMap = searchUtil.orgSearch(schoolId, requestParams.getChannel());
        logger.info(Collections.singletonList(orgMap.toString()));
        return orgMap;
    }


    public void processQueryWriteToFile(String username, String treasuryId, String schoolId) throws IOException {
        Map<String, Object> userMap = getUserDetails(username);
        writeQueryToFile(queryBuilder.createUserUpdateQuery(userMap));
        List<String> deleteQuery = queryBuilder.deleteUserOrgQuery(userMap);
        for (String str : deleteQuery) {
            writeQueryToFile(str);
        }
        List<Map<String,Object>>userOrgMap=createUserOrgRequest(schoolId, (String) userMap.get("id"));
        List<String> insertQuery = queryBuilder.createQueryForUserOrg(userOrgMap);
        for (String str : insertQuery) {
            writeQueryToFile(str);
        }
    }

    public List<Map<String,Object>> createUserOrgRequest(String schoolId, String userId) throws IOException {
        Map<String, Object> orgMap = getOrgDetails(schoolId);
        List<Map<String,Object>>userOrgList=new ArrayList<>();
            Map<String, Object> userOrgReques = new HashMap<>();

            userOrgReques.put("id", getUniqueIdFromTimestamp(1));
            userOrgReques.put("hashtagid", requestParams.getRootOrgId());
            userOrgReques.put("isdeleted", false);
            userOrgReques.put("organisationid", requestParams.getRootOrgId());
            List<String> roles = new ArrayList<>();
            roles.add("PUBLIC");
            userOrgReques.put("roles", roles);
            userOrgReques.put("orgjoindate", getDateFormatter().format(new Date()));
            userOrgReques.put("userid", userId);
            userOrgList.add(userOrgReques);
        if(!orgMap.get("id").toString().equalsIgnoreCase(requestParams.getRootOrgId())) {
            Map<String, Object> userSubOrgReques = new HashMap<>();
            userSubOrgReques.put("id", getUniqueIdFromTimestamp(2));
            userSubOrgReques.put("hashtagid", orgMap.get("hashTagId"));
            userSubOrgReques.put("isdeleted", false);
            userSubOrgReques.put("organisationid", orgMap.get("id"));
            List<String> subRoles = new ArrayList<>();
            subRoles.add("PUBLIC");
            userSubOrgReques.put("roles", subRoles);
            userSubOrgReques.put("orgjoindate", getDateFormatter().format(new Date()));
            userSubOrgReques.put("userid", userId);
            userOrgList.add(userSubOrgReques);
        }

    return userOrgList;
    }

    public static String getUniqueIdFromTimestamp(int environmentId) {
        Random random = new Random();
        AtomicInteger atomicInteger = new AtomicInteger();
        long env = (environmentId + random.nextInt(99999)) / 10000000;
        long uid = System.currentTimeMillis() + random.nextInt(999999);
        uid = uid << 13;
        return env + "" + uid + "" + atomicInteger.getAndIncrement();
    }

    public static SimpleDateFormat getDateFormatter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
        simpleDateFormat.setLenient(false);
        return simpleDateFormat;
    }

}
