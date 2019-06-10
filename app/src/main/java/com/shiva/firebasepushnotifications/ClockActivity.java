package com.shiva.firebasepushnotifications;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.firebase.iid.FirebaseInstanceId;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ClockActivity extends AppCompatActivity {

    static int hour, min;

    TextView txtdate, notice_txt;
    ImageView calenderimage;
    Button btndatepicker,btnsubmit,btnemail;
    String jobid;
    java.sql.Time timeValue;
    SimpleDateFormat format;
    Calendar c;
    int year, month, day;
    String token = FirebaseInstanceId.getInstance().getToken();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.bell_ic_launcher);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        min = c.get(Calendar.MINUTE);

        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        txtdate = (TextView) findViewById(R.id.txtdate);
       // txttime = (TextView) findViewById(R.id.txttime);
        notice_txt=(TextView) findViewById(R.id.txt_text);


        btndatepicker = (Button) findViewById(R.id.btndatepicker);
       // btntimepicker = (Button) findViewById(R.id.btntimepicker);
        btnsubmit = (Button) findViewById(R.id.submit);
        btnemail = (Button) findViewById(R.id.emailbutton);
        calenderimage = (ImageView) findViewById(R.id.calimage);

        notice_txt.setText("You may tell us when you will be ready!!");

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            jobid = extras.getString("jobid");
        }
        final String current_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        c.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = c.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String tomorrowAsString = dateFormat.format(tomorrow);

        btnemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtdate.getText().toString().equals("Date")) {
                    Toast.makeText(ClockActivity.this, "Select the date from the Calender", Toast.LENGTH_LONG).show();
                }
                else if(txtdate.getText().toString().equals(current_date))
                {
                    Toast.makeText(ClockActivity.this, "Do not select the current date", Toast.LENGTH_LONG).show();
                }
                else if(txtdate.getText().toString().equals(tomorrowAsString))
                {
                    Toast.makeText(ClockActivity.this, "So, Are you ready for tomorrow?", Toast.LENGTH_LONG).show();
                }
                else
                {
                    new ClockActivity.FetchDatabase().execute();
                }
            }
        });
        btndatepicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date

                DatePickerDialog dd = new DatePickerDialog(ClockActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    String dateInString = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                    Date date = formatter.parse(dateInString);
                                    formatter = new SimpleDateFormat("yyyy-MM-dd");

                                    calenderimage.setVisibility(View.INVISIBLE);
                                    txtdate.setVisibility(View.VISIBLE);
                                    txtdate.setText(formatter.format(date).toString());

                                } catch (Exception ex) {
                                    Log.d("checking", ex.getMessage());
                                }
                            }
                        }, year, month, day);
                dd.getDatePicker().setMinDate(System.currentTimeMillis() + 172800000 - 1000);
                dd.show();

            }
        });
    }

    private void sendEmail() {

        Log.i("Send email", "");
        String[] TO = {"vandana.shiva1993@gmail.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ClockActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchDatabase extends AsyncTask<Void,Void,String> {
        @Override
        protected String doInBackground(Void... params) {
            String retval = "";
            try {
                insertRecordIntoTable();
            } catch (SQLException e) {
                Log.d("checking", e.getMessage());

            }

            return retval;
        }

        private void insertRecordIntoTable() throws SQLException {
            Connection dbConnection = null;
            PreparedStatement preparedStatement = null;

            String insertTableSQL ="UPDATE jobs SET schedule_date=(?) WHERE job_id=(?)";
          //  +"and contact_name in(select id from contacts where registrationid=(?)) and schedule_date in(select schedule_date from jobs where schedule_date<= TIMESTAMP 'tomorrow' and schedule_date> TIMESTAMP 'today')";

            try {
                Log.d("entering","insert");
                dbConnection = getDBConnection();
                preparedStatement = dbConnection.prepareStatement(insertTableSQL);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date parsed = null;
                try {
                    parsed = format.parse(txtdate.getText().toString());
                } catch (ParseException e) {
                    Log.d("exception",e.getMessage());
                }
                java.sql.Date sqlDate = new java.sql.Date(parsed.getTime());
               // Toast.makeText(ClockActivity.this,txtdate.getText(),Toast.LENGTH_SHORT);
                preparedStatement.setDate(1, sqlDate);
               // preparedStatement.setString(2, (String) txttime.getText());
                Log.d("checking integer job id", jobid);
                preparedStatement.setInt(2, Integer.parseInt(jobid));
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                Log.d("exception","reached");
                Log.d("exception",e.getMessage());

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
            //Toast.makeText(ClockActivity.this, value, Toast.LENGTH_SHORT).show();
           Log.d("checking", "error: " + value);
            notice_txt.setText("Thanks!! You have updated the task to be completed on "+txtdate.getText().toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(ClockActivity.this);
            builder.setTitle("Thanks");
            builder.setMessage("The date you have chosen is updated to our database!!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // You don't have to do anything here if you just want it dismissed when clicked
                    Intent intent = new Intent(ClockActivity.this, MainActivity.class);
                    intent.putExtra("from","ClockActivity");
                    startActivity(intent);
                    finish();
                }
            });
            // Create the AlertDialog object and return it
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




