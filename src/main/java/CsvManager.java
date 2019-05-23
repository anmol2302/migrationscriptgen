import org.apache.commons.lang3.StringUtils;
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
    FileWriter fwCsv;

    public CsvManager(RequestParams params) throws IOException {
        this.requestParams = params;
        client = new HttpClient(requestParams.getAuthToken(), requestParams.getApiKey());
        searchUtil = new SearchUtil(requestParams, client);
        queryBuilder = new QueryBuilder(requestParams);
        fwCsv=new FileWriter("userExtId.csv");
        fwCsv.write("userName,userId,treasuryId,channel\n");
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
                            if (StringUtils.isNotBlank(values[1]) && StringUtils.isNotBlank(values[0])) {
                                if(values.length==3){
                                processQueryWriteToFile(values[0], values[1], values[2]);}
                                else{
                                    processQueryWriteToFile(values[0], values[1], "");
                                }
                            } else {
                                logger.info("No treasuryId found for this " + values[0]);
                                continue;
                            }

                        } else {
                            logger.error("CsvManager:processCsv: invalid row of records");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(" CsvManager : processCsv:  " + e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                        fw.close();
                        fwCsv.flush();
                        fwCsv.close();
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
            logger.error("file does not exists " + pathToInputFile);
            return null;

        } catch (IOException e) {
            logger.error("Exception occured " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getUserDetails(String userName) throws IOException {
        logger.info("CsvManager: getuserdetails: username is: " + userName);
        Map<String, Object> userMap = searchUtil.userSearch(userName);
        if (userMap.size() != 0) {
            Map<String, Object> userLimitedDetails = new HashMap<>();
            userLimitedDetails.put("id", userMap.get("id"));
            userLimitedDetails.put("organisations", userMap.get("organisations"));
            logger.info(Collections.singletonList(userMap.toString()));
            logger.info(Collections.singletonList(userLimitedDetails.toString()));
            return userLimitedDetails;
        } else {
            return Collections.emptyMap();
        }


    }

    public Map<String, Object> getOrgDetails(String schoolId) throws IOException {
        logger.info("CsvManager: getorgdetails: schoolId is: " + schoolId);
        Map<String, Object> orgMap = searchUtil.orgSearch(schoolId, requestParams.getChannel());
        if (orgMap.size() != 0) {
            logger.info(Collections.singletonList(orgMap.toString()));
            return orgMap;
        } else {
            return Collections.emptyMap();
        }
    }


    public void processQueryWriteToFile(String username, String treasuryId, String schoolId) throws IOException {
        Map<String, Object> userOrgMap = validateUserNameAndSchoolId(username, schoolId);
        if ((Boolean)userOrgMap.get("queryToBeProcessed")) {
            Map<String, Object> userMap = (Map<String, Object>) userOrgMap.get("userMap");
            Map<String, Object> orgMap = (Map<String, Object>) userOrgMap.get("orgMap");
            writeQueryToFile(queryBuilder.createUserUpdateQuery(userMap));
            List<String> deleteQuery = queryBuilder.deleteUserOrgQuery(userMap);
            for (String str : deleteQuery) {
                writeQueryToFile(str);
            }
            prepareUserCsvFile(username,(String) userMap.get("id"),treasuryId,requestParams.getChannel());   // preparing userExtId csv file
            List<Map<String, Object>> userOrgMapList = createUserOrgRequest(schoolId, (String) userMap.get("id"),orgMap);
            if (!userOrgMap.isEmpty()) {
                List<String> insertQuery = queryBuilder.createQueryForUserOrg(userOrgMapList);
                for (String str : insertQuery) {
                    writeQueryToFile(str);
                }
            }

        } else {
            logger.error("Record not processed with this " + username + " and schoolID " + schoolId);
        }
    }

    public List<Map<String, Object>> createUserOrgRequest(String schoolId, String userId,Map<String,Object> orgMap) throws IOException {

            List<Map<String, Object>> userOrgList = new ArrayList<>();
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
            if (orgMap.size()!=0 && !orgMap.get("id").toString().equalsIgnoreCase(requestParams.getRootOrgId())) {
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

            } else {
            logger.error("CsvManager: createUserOrgRequest: No organisations found for this schooId : " + schoolId);
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

    public Map<String, Object> validateUserNameAndSchoolId(String userName, String schoolId) throws IOException {

        Map<String, Object> orgMap = new HashMap<>();
        boolean flag = false;
        Map<String, Object> userMap = getUserDetails(userName);
        if (StringUtils.isNotBlank(schoolId)) {
            orgMap = getOrgDetails(schoolId);
        }
        Map<String, Object> userOrgMap = new HashMap<>();
        userOrgMap.put("userMap", userMap);
        userOrgMap.put("orgMap", orgMap);
        if (userMap.size() != 0) {
            if (StringUtils.isNotBlank(schoolId)) {
                if (orgMap.size() != 0) {
                    flag = true;           // schoolId is valid
                } else {
                    flag = false;         //schoolId is invalid
                }
            } else {
                flag = true;         //schoolId is not provided associating userTo root org.

            }

        } else {
            flag = false;        //userName is invalid

        }
        userOrgMap.put("queryToBeProcessed",flag);
        return userOrgMap;
    }


    public  void prepareUserCsvFile(String userName,String userId,String treasuryId,String channel) throws IOException {
        String query=userName+","+userId+","+treasuryId+","+channel;
        writeUserDataToCsv(query);
    }

    public void writeUserDataToCsv(String query) throws IOException {
        try {
            fwCsv.write(query + "\n");
        }
        catch (Exception e){
            logger.error("Exception occured while writing to write in  userExtId.csv");
        }
    }

}
