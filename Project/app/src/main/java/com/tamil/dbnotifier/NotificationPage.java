package com.tamil.dbnotifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NotificationPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_page);
        SimpleAdapter simpleAdapter = null;
        try {
            String adminurl = getIntent().getStringExtra("adminurl");
            String tablename = getIntent().getStringExtra("tablename");
            ListView notifyView = findViewById(R.id.listView);
            Infinitydatabase infdb = new Infinitydatabase(adminurl);
            HashMap result = getNotificationDatas(infdb, tablename);
            if (!(Boolean) result.get("success") || ((ArrayList) result.get("row")).isEmpty()) {
                emptyList(notifyView, simpleAdapter);
                return;}
            else {result.remove("success");}
            HashMap<String, ArrayList> dataList = result;
            ArrayList<HashMap<String, String>> datalist = columnRowToArrayHashmaps(dataList.get("column"), dataList.get("row"));
            simpleAdapter = addNotifications(datalist);
            notifyView.setAdapter(simpleAdapter);
            SimpleAdapter finalSimpleAdapter = simpleAdapter;
            notifyView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        if ((Boolean) removeNotification(infdb, tablename, datalist.get(i).get("Id")).get("success")) {
                            datalist.remove(i);
                            finalSimpleAdapter.notifyDataSetChanged();
                            if (datalist.isEmpty()) {emptyList(notifyView, finalSimpleAdapter);}
                            Toast.makeText(NotificationPage.this, "Done", Toast.LENGTH_SHORT).show();}}
                    catch (Exception e) {e.printStackTrace();}
                    return false;}
            });
            markNotified(infdb, tablename);
            Intent service =new Intent(this, NotificationService.class);
            startService(service);
        }
        catch (Exception e) {Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show();}
    }

    private void emptyList(ListView notifyView, SimpleAdapter simpleAdapter){
        ArrayList<HashMap<String, String>> ar =new ArrayList<>();
        HashMap<String, String> map =new HashMap<>();
        map.put("nothing", "Nothing..");ar.add(map);
        simpleAdapter = new SimpleAdapter(this, ar, R.layout.no_notification, new String[] {"nothing"}, new int[] {R.id.nothingView});
        notifyView.setAdapter(simpleAdapter);
        Toast.makeText(this, "Nothing", Toast.LENGTH_SHORT).show();
    }

    private HashMap markNotified(Infinitydatabase infdb, String tablename) throws Exception {
        return infdb.query("update "+tablename+" set notify=false");
    }

    private HashMap getNotificationDatas(Infinitydatabase infdb, String tablename) throws Exception {
        return infdb.query("select * from "+tablename+" order by notify desc, newdate desc, newtime desc, olddate desc, oldtime desc, times desc");
    }

    private HashMap removeNotification(Infinitydatabase infdb, String tablename, String id) throws Exception {
        return infdb.query("delete from "+tablename+" where id="+id);
    }

    private SimpleAdapter addNotifications(ArrayList<HashMap<String, String>> datalist){
        // id, place, type, time, date, info
        return new SimpleAdapter(this, datalist,
                R.layout.notification_cell, new String[] {"Place", "Level", "Date", "Time", "Info"},
                new int[] {R.id.place, R.id.level, R.id.date, R.id.time, R.id.info});
    }

    private ArrayList<HashMap<String, String>> columnRowToArrayHashmaps(ArrayList<String> headers, ArrayList<ArrayList<String>> datas) {
        ArrayList<HashMap<String, String>> arrayList =new ArrayList<>();
        ArrayList<Integer> ignore =new ArrayList<Integer>();
        ignore.add(4);ignore.add(6);ignore.add(8);ignore.add(9);
        for (int loop=0; loop < datas.size(); loop++) {
            HashMap<String, String> map = new HashMap<>();
            int times =Integer.parseInt(datas.get(loop).get(9));
            String olddate =datas.get(loop).get(4);
            String oldtime =datas.get(loop).get(6);
            String notify =datas.get(loop).get(8);
            for (int index = 0; index < headers.size(); index++) {
                if (ignore.contains(index)){continue;}
                String header =headers.get(index);
                String data =datas.get(loop).get(index);
                switch (header){
                    case "Place":
                        if (notify.equals("1")){data =data+" (New)";}
                        if (times>1){data =data+" ("+String.valueOf(times)+")";}
                        break;
                    case "Level":
                        data ="Level: "+data;
                        break;
                    case "NewDate":
                        header ="Date";
                        if (times>1){data ="Date: "+olddate+" to "+data;}
                        else {data ="Date: "+data;}
                        break;
                    case "NewTime":
                        header ="Time";
                        if (times>1){data ="Time: "+oldtime+" to "+data;}
                        else {data ="Time: "+data;}
                        break;
                    case "Info":
                        data ="Message: "+data;}
                map.put(header, data);}
            arrayList.add(map);}
        return arrayList;
    }
}