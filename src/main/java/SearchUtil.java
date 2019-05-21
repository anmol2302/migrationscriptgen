import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUtil {


    static  HttpClient client;
    static RequestParams params;

    public SearchUtil(RequestParams params, HttpClient client) {
        this.params = params;
        this.client = client;
    }


    public Map<String,Object>  userSearch(String userName) throws IOException {

        Map<String, Object> userRequest = new HashMap<String, Object>();
        Map<String, Object> request = new HashMap<String, Object>();
        Map<String, String> filters = new HashMap<String, String>();
        filters.put("userName",userName);
        request.put("filters", filters);
        userRequest.put("request", request);
        String searchUrl = params.getBaseUrl()+"/user/v1/search";
        Map<String,Object>resMap= client.post(userRequest, searchUrl);
        Map<String, Object> result = null;
        Map<String, Object> responseMap = null;
        List<Map<String, Object>> content = null;
        if (null != resMap) {
            result = (Map<String, Object>) resMap.get("result");
        }
        if (null != result) {
            responseMap = (Map<String, Object>) result.get("response");
        }
        if (null != responseMap) {
            content = (List<Map<String, Object>>) (responseMap).get("content");
        }
        if (null != content) {
            return  content.get(0);
            }
        else{
            Collections.emptyMap();
        }
        return Collections.emptyMap();
    }

    public Map<String,Object> orgSearch(String orgExtId,String channel) throws IOException {

        Map<String, Object> userRequest = new HashMap<String, Object>();
        Map<String, Object> request = new HashMap<String, Object>();
        Map<String, String> filters = new HashMap<String, String>();
        filters.put("channel",channel);
        filters.put("externalId",orgExtId);
        request.put("filters", filters);
        userRequest.put("request", request);
        String searchUrl = params.getBaseUrl()+"/org/v1/search";
        Map<String,Object>resMap= client.post(userRequest, searchUrl);
        Map<String, Object> result = null;
        Map<String, Object> responseMap = null;
        List<Map<String, Object>> content = null;
        if (null != resMap) {
            result = (Map<String, Object>) resMap.get("result");
        }
        if (null != result) {
            responseMap = (Map<String, Object>) result.get("response");
        }
        if (null != responseMap) {
            content = (List<Map<String, Object>>) (responseMap).get("content");
        }
        if (null != content) {
            return  content.get(0);
        }
        else{
            Collections.emptyMap();
        }
        return Collections.emptyMap();
    }


}
