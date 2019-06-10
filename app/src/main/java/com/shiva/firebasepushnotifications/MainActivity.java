package com.shiva.firebasepushnotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.firebase.iid.FirebaseInstanceId;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    EditText email_value;
    Spinner spinner;
    String name,token,getnotified_thru;
    static int flag=0;
    Integer contact_id = null;
    ArrayList<String> emails;
    //Receiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.bell_ic_launcher);

        Button btnShowToken = (Button)findViewById(R.id.button_show_token);

        email_value=(EditText) findViewById(R.id.edit_email);
        spinner=(Spinner)findViewById(R.id.spinner);

        ArrayAdapter<String> myAdapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        name= String.valueOf(email_value.getText());

            btnShowToken.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Get the token
                String k= String.valueOf(email_value.getText());
                if(!email_value.getText().toString().equals("")) {
                    token = FirebaseInstanceId.getInstance().getToken();

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("email",email_value.getText().toString());
                    editor.apply();
                    Log.d(TAG, "Token: " + token);
                    new CheckEmail().execute();
                   // receiver.CancelAlarm(MainActivity.this);
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Please enter your emailid registered with us!!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void tempfunc() {
        new CheckEmail().execute();
    }

    public class InsertOrUpdate extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            insertOrUpdateRecord();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if(flag==1) {
                //super.onPostExecute(o);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Thanks!!");
                builder.setMessage("You are now signed up with us!!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // You don't have to do anything here if you just want it dismissed when clicked
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setCancelable(true);
                builder.create().show();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sorry!!");
                builder.setMessage("This email is not present in our database!! Please enter the email address registered with us");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // You don't have to do anything here if you just want it dismissed when clicked
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setCancelable(true);
                builder.create().show();
            }
        }
        private void insertOrUpdateRecord(){
            Connection dbConnection = null;
            PreparedStatement preparedStatement = null;

            System.out.println("FLAG"+flag);
            System.out.println("Contact_id"+contact_id);
            if(flag==1)
            {
                String updateTableSQL = "UPDATE public.contacts SET registrationid =(?),notify_through=(?) WHERE id=(?)";
             //   String updateTableSQL = "UPDATE public.contacts SET registrationid ='"+ FirebaseInstanceId.getInstance().getToken()+"',notify_through='"+spinner.getSelectedItem().toString().trim()+"'WHERE email='"+email_value.getText().toString().replace(" ","").toLowerCase().trim()+"'";

                try {
                    Log.d("entering", "insert");
                    dbConnection = getDBConnection();
                    preparedStatement = dbConnection.prepareStatement(updateTableSQL);
                    //st.executeQuery(fetch_emails);
                    token = FirebaseInstanceId.getInstance().getToken();
                    name = email_value.getText().toString().replace(" ","").toLowerCase().trim();
                    getnotified_thru= spinner.getSelectedItem().toString().trim();
                    preparedStatement.setString(1, token);
                    preparedStatement.setString(2, getnotified_thru);
                    preparedStatement.setInt(3, contact_id);
                    preparedStatement.executeUpdate();
                    System.out.println("Record is updated into contacts!");
                  //  System.out.println(updateTableSQL);
                } catch (SQLException e) {
                    Log.d("exception", "reached");
                    Log.d("exception", e.getMessage());
                } finally {

                    if (preparedStatement != null) {
                        try {
                            preparedStatement.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    if (dbConnection != null) {
                        try {
                            dbConnection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else {
                return;
            }
        }
    }


    public class CheckEmail extends AsyncTask{
        @Override
        protected void onPostExecute(Object o) {
            new InsertOrUpdate().execute();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            checkUserExists();
            return null;
        }
        public void checkUserExists(){
            Connection dbConnection = null;
            try {
                Log.d("entering","insert");
                dbConnection = getDBConnection();
                Statement st = dbConnection.createStatement();

               //String fetch_emails="select 1 from contacts where '"+email_value.getText().toString().replace(" ","").toLowerCase().trim()+"' in ( select replace(lower(email),' ','') from contacts) limit 1";
                String fetch_emails = "select id from (select replace(lower(email),' ','') as email, id from contacts) temp_table where email = '"+email_value.getText().toString().replace(" ","").toLowerCase().trim()+"';" ;
                Log.d("new email", fetch_emails);

                ResultSet rs = st.executeQuery(fetch_emails);

                if(rs.next()) {
                    Log.d("Email Checking","Email already exists");
                    flag=1;
                    contact_id = rs.getInt("id");
                }
                else
                {
                    Log.d("Email Checking","Email not exists");
                    flag=0;
                }
                rs.close();
                st.close();
            } catch (SQLException e) {
                Log.d("exception","reached");
                Log.d("exception",e.getMessage());
                System.out.println(e.getMessage());
            } finally {
                if (dbConnection != null) {

                    try {
                        dbConnection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

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
    @Override
    public void onBackPressed() {

        return;
    }

}
