package com.tamil.dbnotifier;

import android.os.StrictMode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Infinitydatabase {

    private final String dbadminUrl;
    private final String host, db;
    private final String[] display_response;
    private final String server, user, token;
    private URL url;
    private HttpURLConnection urlConnec;
    private BufferedReader br;
    private StringBuilder sb;
    private StringBuilder data = new StringBuilder();
    private String redirect_url;
    private StringBuilder cookies = new StringBuilder();
    private JSONObject jsonResult;
    private Map<String, ArrayList> table;
    private ArrayList<String> column;
    private ArrayList<ArrayList<String>> row;

    Infinitydatabase(String dbadminUrl) throws Exception {
        StrictMode.ThreadPolicy policy =new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        this.dbadminUrl = dbadminUrl;
        this.host = this.dbadminUrl.split("login")[0];
        this.db = this.dbadminUrl.split("db=")[1];
        this.display_response = new String[]{"select ", "show ", "desc "};
        url = new URL(this.dbadminUrl);
        urlConnec = (HttpURLConnection) (url.openConnection());
        urlConnec.setInstanceFollowRedirects(false);
        urlConnec.setRequestMethod("GET");
        urlConnec.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0");
        String headerName, cookie;
        for (int i = 1; (headerName = urlConnec.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                cookie = urlConnec.getHeaderField(i);
                cookie = cookie.substring(0, cookie.indexOf(";") + 2);
                if (!cookies.toString().contains(cookie)) {cookies.append(cookie);}
            } else if (headerName.equals("Location")) {this.redirect_url = urlConnec.getHeaderField(i);}}
        url = new URL(this.redirect_url);
        urlConnec = (HttpURLConnection) (url.openConnection());
        urlConnec.setInstanceFollowRedirects(false);
        urlConnec.setRequestProperty("Cookie", this.cookies.toString());
        urlConnec.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0");
        for (int i = 1; (headerName = urlConnec.getHeaderFieldKey(i)) != null; i++) {
            if (headerName.equals("Set-Cookie")) {
                cookie = urlConnec.getHeaderField(i);
                cookie = cookie.substring(0, cookie.indexOf(";") + 2);
                if (!cookies.toString().contains(cookie)) {cookies.append(cookie);}}}
        sb =new StringBuilder();
        br = new BufferedReader(new InputStreamReader((urlConnec.getInputStream())));
        String data;
        while ((data = br.readLine()) != null) {sb.append(data);}
        String result = sb.toString();
        String commonparams = result.split("PMA_commonParams.setAll\\(")[1].split("\\);")[0];
        this.server = commonparams.split("server:\"")[1].split("\"")[0];
        this.token = commonparams.split("token:\"")[1].split("\"")[0];
        this.user = commonparams.split("user:\"")[1].split("\"")[0];
        this.data.append("ajax_request=true&ajax_page_request=true&session_max_rows=10000&pftext=F&server=" + this.server + "&db=" + this.db + "&token=" + this.token + "&sql_query=");
    }

    public HashMap query(String query) throws Exception {
        HashMap hashMap =new HashMap<>();
        query = query.trim();
        url = new URL(this.host + "sql.php");
        urlConnec = (HttpURLConnection) (url.openConnection());
        urlConnec.setInstanceFollowRedirects(false);
        urlConnec.setRequestProperty("Cookie", this.cookies.toString());
        urlConnec.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0");
        urlConnec.setRequestMethod("POST");
        urlConnec.setDoOutput(true);
        OutputStream os=urlConnec.getOutputStream();
        os.write((this.data+ URLEncoder.encode(query, StandardCharsets.UTF_8.toString())).getBytes());
        os.flush();os.close();
        sb =new StringBuilder();
        br = new BufferedReader(new InputStreamReader((urlConnec.getInputStream())));
        String data;
        while ((data = br.readLine()) != null) {sb.append(data);}
        jsonResult =new JSONObject(sb.toString());
        for (String element: this.display_response){
            if (jsonResult.getBoolean("success") && query.toLowerCase().startsWith(element)){
                hashMap = (HashMap) this.displayQueryResponse(jsonResult.getString("message"));}}
        hashMap.put("success", jsonResult.getBoolean("success"));
        return hashMap;
    }

    public Map<String, ArrayList> displayQueryResponse(String result) {
        Map<String, ArrayList> table = new HashMap<>();
        table.put("column", new ArrayList<>());
        table.put("row", new ArrayList<List<String>>());
        Document html = Jsoup.parse(result);
        for (Element tag : html.getAllElements()) {
            if (tag.hasAttr("data-column")) {table.get("column").add(tag.text().trim());}}
        for (Element row : html.getElementsByTag("tr")) {
            List<String> rowData = new ArrayList<String>();
            for (Element tag : row.getAllElements()) {
                if (tag.hasAttr("data-decimals")) {rowData.add(tag.text().trim());}}
            if (!rowData.isEmpty()) {table.get("row").add(rowData);}}
        return table;
    }

}
