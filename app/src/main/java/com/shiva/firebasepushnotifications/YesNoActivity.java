package com.shiva.firebasepushnotifications;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class YesNoActivity extends AppCompatActivity {
    Button yes,no;
    TextView notice_txt;
    String jobId, jobtype,lot,subdivision;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yes_no);

        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.bell_ic_launcher);

        yes = (Button) findViewById(R.id.btn_yespicker);
        no=(Button)findViewById(R.id.btn_nopicker);
       // jobid_textview=(TextView) findViewById(R.id.txt_jobid);
        notice_txt=(TextView) findViewById(R.id.txt_text);
        Intent intent = getIntent();
        if(intent.getStringExtra("from").equals("activity1")) {
         jobId=intent.getStringExtra("jobid");
         jobtype=intent.getStringExtra("jobtype");
         subdivision=intent.getStringExtra("subdivision");
         lot=intent.getStringExtra("lot");
        }
        else
        {
         final Bundle extras = intent.getExtras();
         if (extras != null) {

            final Set<String> keySet = extras.keySet();
            final Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {

                final String key = iterator.next();
                final Object o = extras.get(key);
                if (key.equals("jobId"))
                    jobId = o.toString();
                if (key.equals("jobtype"))
                    jobtype = o.toString();
                if (key.equals("subdivision"))
                    subdivision = o.toString();
                if (key.equals("lot"))
                    lot = o.toString();
            }
         }
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new YesNoActivity.FetchSQL().execute();

            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YesNoActivity.this,ClockActivity.class);
                intent.putExtra("jobid",jobId);
                startActivity(intent);

            }
        });

        notice_txt.setText("Hi Builder!\n\nYou have a job of job type "+jobtype+" scheduled tomorrow at lot "+lot+" in subdivision "+subdivision+"\n\n\nPress 'Yes', if you are ready.\nIf not ready, press 'No'");
    }

    public class FetchSQL extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params) {
            String retval = "";
            try {
                insertRecordIntoTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return retval;
        }

        private void insertRecordIntoTable() throws SQLException {
            Connection dbConnection = null;
            PreparedStatement preparedStatement = null;

                String updateTableSQL = "UPDATE public.jobs SET confirmed_from =(?) WHERE job_id=(?)";

                try {
                    Log.d("entering", "insert");
                    dbConnection = getDBConnection();
                    preparedStatement = dbConnection.prepareStatement(updateTableSQL);
                    preparedStatement.setString(1, "App".toString());
                    preparedStatement.setInt(2, Integer.parseInt(jobId));
                    preparedStatement.executeUpdate();
                    System.out.println("Record is updated into contacts!");
                } catch (SQLException e) {
                    Log.d("exception", "reached");
                    Log.d("exception", e.getMessage());
                } finally {

                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                    if (dbConnection != null) {
                        dbConnection.close();
                    }
                }
        }
        @Override
        protected void onPostExecute(String value) {
            AlertDialog.Builder builder = new AlertDialog.Builder(YesNoActivity.this);
            builder.setTitle("Thanks!!");
            builder.setMessage("Your status is updated in the database!!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // You don't have to do anything here if you just want it dismissed when clicked
                    Intent intent = new Intent(YesNoActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            builder.setCancelable(true);
            builder.create().show();
        }
    }

    private Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());

        }
        try {
            // dbConnection = DriverManager.getConnection("jdbc:postgresql://10.0.2.2/dbname", "postgres","Sai!1993");
            dbConnection = DriverManager.getConnection("jdbc:postgresql://184.172.105.22/RHv2_dev","masterRH238","Mgdsp_11_13");
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbConnection;
    }


}




