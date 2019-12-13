package com.example.randomizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//My imports
//Notifications
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;

//Random
import java.util.Random;

//Async for parsing JSON
import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    //ClASS VARIABLES

    //Declaring Notifications
    public static final int NOTIFICATION_ID = 222;
    public static final String CHANNEL_ID = "Using only one channel that shows up when email fields match";
    public static final String CHANNEL_NAME = "Channel name";
    NotificationManagerCompat notificationManagerCompat;
    Notification notification;

    //Declaring generated myRandom that we can call by generatedString
    private String generatedString;
    //Declaring EditText
    private EditText confirmEField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Connecting to the server
        DownloadTask task = new DownloadTask();
        //Using local host
        task.execute("http://10.0.2.2:3000");

        //Initializing the views
        //Notifications
        createNotificationChannel();
        notification = createNotification();

        notificationManagerCompat = NotificationManagerCompat.from(this);
        //Send email part. Reading confirmEField
        confirmEField = (EditText) findViewById(R.id.confirmEField);
    }

    //Connect to the server and extend. Read and decode
    public class DownloadTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }

                //Handle errors if stream data fails. Also do logging
            } catch (Exception err) {
                Log.i("ERROR DOWNLOADING DATA", err.getMessage());
            }
            return result;
        }


        //Parsing
        @Override
        protected void onPostExecute(String myS) {
            super.onPostExecute(myS);
            try {
                //Read TV field
                TextView read = findViewById(R.id.confirmBonusPrize);

                JSONObject jsonObject = new JSONObject(myS);
                //Parse JSON object to string. Fetch data from server with quotes
                String jsonText = jsonObject.getString("bonusPrize");

                //Write TV field. Forward parsed json string to confirmBonusPrize field
                read.setText(jsonText);

                //Catch error and do logging
            } catch (Exception err) {
                Log.i("ERROR PARSING JSON: ", err.getMessage());
            }
        }
    }


    //Creating channel for notifications
    private void createNotificationChannel() {
        //Check the SDK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Let's register our notification channel and set the priority level that is HIGH at the moment
            NotificationChannel notificationChannel = new NotificationChannel
                    (CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            //Enable lights & vibration
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            //Define the lights color
            notificationChannel.setLightColor(Color.BLUE);
            //Define vibration pattern
            notificationChannel.setVibrationPattern(new long[] { 500, 250, 250, 500 });
            //Define does our text appear on locked screen. At the moment it does
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }


    //Declaring what does our notification display
    private Notification createNotification() {

        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        //Show notification title / Using "Big text" because without
        // "big text" our body section would disappear after certain point / also we do set small icon on display
        // and use priority level that is HIGH
        myBuilder.setContentTitle("Randomizer")
        .setStyle(new NotificationCompat.BigTextStyle().bigText
                ("Your raffle code has been sent to your email from Randomizer app "))
        .setSmallIcon(R.drawable.bell)
        .setPriority(NotificationCompat.PRIORITY_HIGH);
        return myBuilder.build();
    }

    //Listening gRandom button and setting up our "random"
    public void gRandom(View view) {
        TextView fieldsOfDigits = findViewById(R.id.cAppearance);
        //Declare random. Using three (3) characters only to match json data from the server (AB)
        //String listOfChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String listOfChars = "ABC";
        StringBuilder list = new StringBuilder();
        Random myRandom = new Random();
        //was six (6)
        while (list.length() < 2) { // Maximum length
            int indexOfChar = (int) (myRandom.nextFloat() * listOfChars.length());
            list.append(listOfChars.charAt(indexOfChar));
        }
        //Using
        generatedString = list.toString();

        //Do logging and read clics on button
        Log.i("Randomizer", "Click recorded from generate code button " + generatedString);
        //Read invisible TV field where json string is forwarded
        TextView read = findViewById(R.id.confirmBonusPrize);
        //Get that string
        String write = read.getText().toString();

        //Handle logic. You can peek from the console if your generatedstring equals json string. Aka won
        if (generatedString.equals(write)) {
            Log.i("Randomizer", "Cheat sheet: YOU WON " + generatedString + " equals " + write);
        }
        fieldsOfDigits.setText(generatedString);
    }

    public void confirmAndProceed(View view) {
        //Let's read users input field one and two
        EditText rFieldOne = findViewById(R.id.eField);
        EditText rFieldTwo = findViewById(R.id.confirmEField);

        //Declaring our strings
        String cEmailFieldOne = rFieldOne.getText().toString();
        String cEmailFieldTwo = rFieldTwo.getText().toString();
        String rEmail = confirmEField.getText().toString();

        TextView read = findViewById(R.id.confirmBonusPrize);
        String write = read.getText().toString();
        TextView readCodeField = findViewById(R.id.cAppearance);
        String confirmCodeField = readCodeField.getText().toString();

        //Handle toasts
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast;


        //Handle logic. Both email inputs should match and generated random should match servers json string
        // This block returns WON
        if (cEmailFieldOne.equals(cEmailFieldTwo) && confirmCodeField.equals (write)) {
            CharSequence text = "Sending raffle code to: " + rEmail;
            toast = Toast.makeText(context, text, duration);
            toast.show();

            //Declaring the part when notification shows up
            notificationManagerCompat.notify(NOTIFICATION_ID, notification);
            SendMail sm = new SendMail(this, rEmail, "Randomizer app: raffle code",
                    "You requested raffle code from Randomizer app." +
                            " \n Your raffle code is " + "'' " + generatedString + " ''" + "AND YOU WON!"
                            + "\n Redeem your code at: http://localhost:3000/redeem");

            //Executing sendmail to send email
            sm.execute();
            //Forward user to the 2nd page
            goToSecondActivity();

            Log.i("Randomizer", "Emails and generatedString do match and message was sent!");
        }

        //User doesn't win
        else if (cEmailFieldOne.equals(cEmailFieldTwo)) {
            CharSequence text = "Sending raffle code to: " + rEmail;
            toast = Toast.makeText(context, text, duration);
            toast.show();
            //Declaring the part when notification shows up
            notificationManagerCompat.notify(NOTIFICATION_ID, notification);


            //Declaring subject message and our authentication code.
            SendMail sm = new SendMail(this, rEmail, "Randomizer app: raffle code",
                    "You requested raffle code from Randomizer app." +
                            " \n Your raffle code is " + "'' " + generatedString + " ''" + " but you didn't win this time.");
            Log.i("Randomizer", "Cheat sheet: Code did not match JSON");

            //Executing sendmail to send email
            sm.execute();
            //Forward user to the 2nd page
            goToSecondActivity();

            //If emails fo not match, toast and do not forward to the 2nd page
        } else {
        toast = Toast.makeText(context, "Emails do not match", duration);
        toast.show();
    }
    }

    public void goToSecondActivity() {
        Intent myIntent = new Intent(this, SecondActivity.class);
        startActivity(myIntent);
    }
}