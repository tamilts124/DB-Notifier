package com.tamil.dbnotifier;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp =getSharedPreferences("mydb", MODE_PRIVATE);
        String adminurl =sp.getString("adminurl", "");
        String tablename =sp.getString("tablename", "");
        if (!adminurl.isEmpty() && !tablename.isEmpty()){
            Toast.makeText(getApplicationContext(), "Loading..", Toast.LENGTH_LONG).show();
            launchNotificationPage(adminurl, tablename);}
        EditText dbadminUrl =(EditText) findViewById(R.id.adminUrl);
        EditText tableName =(EditText) findViewById(R.id.tableName);
        Button checkButton =(Button) findViewById(R.id.checkButton);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String adminurl =dbadminUrl.getText().toString().trim();
                String tablename =tableName.getText().toString().trim();
                dbadminUrl.setText(adminurl);
                if (adminurl.isEmpty()){Toast.makeText(MainActivity.this, "Give Admin Url..", Toast.LENGTH_SHORT).show();}
                else if(!adminurl.startsWith("http")){Toast.makeText(MainActivity.this, "Give Currect Url..", Toast.LENGTH_SHORT).show();}
                else{
                    try {
                        Infinitydatabase infdb =new Infinitydatabase(dbadminUrl.getText().toString());
                        if (tablename.isEmpty()){tableName.setText("Notifier");}
                        tablename =tableName.getText().toString().trim();
                        tableName.setText(tablename);
                        HashMap result =getVerificationData(infdb, tablename);
                        if (!(Boolean) result.get("success")) {
                            HashMap responseCode = createTable(infdb, tablename);
                            if ((Boolean) responseCode.get("success")) {
                                savePreferences(sp, adminurl, tablename);
                                launchNotificationPage(adminurl, tablename);}}
                        else {
                            savePreferences(sp, adminurl, tablename);
                            launchNotificationPage(adminurl, tablename);}}
                    catch (Exception e) {Toast.makeText(MainActivity.this, String.valueOf(e), Toast.LENGTH_SHORT).show();}
                }}
        });
    }

    private HashMap getVerificationData(Infinitydatabase infdb, String tablename) throws Exception {
        return infdb.query("select * from "+tablename+" limit 1");}

    private HashMap createTable(Infinitydatabase infdb, String tablename) throws Exception {
        HashMap responseCode =(HashMap) infdb.query("create table "+tablename+" ( " +
                    "Id int auto_increment not null, " +
                    "Place varchar(100) not null, " +
                    "Level varchar(100) not null, " +
                    "NewDate varchar(100) not null, " +
                    "OldDate varchar(100) default null, " +
                    "NewTime varchar(100) not null, " +
                    "OldTime varchar(100) default null, " +
                    "Info text not null, " +
                    "Notify boolean not null default true, " +
                    "Times int not null default 1, " +
                    "primary key(Id) )");
        if ((Boolean) responseCode.get("success")){Toast.makeText(MainActivity.this, tablename+" Table was Created..", Toast.LENGTH_LONG).show();}
        else {Toast.makeText(MainActivity.this, tablename+" Table Can't Create..", Toast.LENGTH_LONG).show();}
        return responseCode;}

    private static void savePreferences(SharedPreferences sp, String adminurl, String tablename){
        SharedPreferences.Editor speditor =sp.edit();
        speditor.putString("adminurl", adminurl);
        speditor.putString("tablename", tablename);
        speditor.apply();}

    private void launchNotificationPage(String adminurl, String tablename){
        Intent notificationPage =new Intent(MainActivity.this, NotificationPage.class);
        notificationPage.putExtra("adminurl", adminurl);
        notificationPage.putExtra("tablename", tablename);
        startActivity(notificationPage);
        finish();
    }
}