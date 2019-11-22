package com.example.innovacceems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.model.value.ServerTimestampValue;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class CheckIn extends AppCompatActivity {

    EditText visName;
    TextView visID;
    EditText visPN;
    EditText HostName;
    EditText HostID;
    EditText HostPN;

    Button CheckIn;

    String userEmail;
    String userName;

    public Context context;
    boolean validVisID ;
    boolean validVisPN ;
    boolean validHostID ;
    boolean validHostPN ;

    final String username = "innov302iiitd@gmail.com";
    final String password = "Innov302@iiitd";

    Visitor visitor;

    CollectionReference visitorDB = FirebaseFirestore.getInstance().collection("dbVisitors");

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        if(email.isEmpty()){return false;}
        else {
            try {
                InternetAddress emailAddr = new InternetAddress(email);
                emailAddr.validate();
            } catch (AddressException ex) {
                result = false;
            }
            return result;
        }
    }
    public static boolean isValidPhoneNumber(String PN) {
       if(PN.isEmpty()){return false;}
       else{
           return PN.matches("(0/91)?[7-9][0-9]{9}");
       }

    }
    public void sendMail(String from, String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);

            System.out.println("Done");
            Log.v("Mailer","Mailed");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        getSupportActionBar().hide();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = this;

        visitor = MainActivity.visitor;

        visName = findViewById(R.id.VisName);
        visID = findViewById(R.id.VisID);
        visPN = findViewById(R.id.VisPN);
        HostName = findViewById(R.id.HostName);
        HostID = findViewById(R.id.HostID);
        HostPN = findViewById(R.id.HostPN);

        CheckIn = findViewById(R.id.CheckIn);

        userEmail = MainActivity.userEmail;
        userName = MainActivity.userName;
        validHostID = false;
        validHostPN = false;
        validVisID = false;
        validVisPN = false;
        visID.setText(userEmail);
        visName.setText(userName);

        visID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    visitor.visID = visID.getText().toString();
                    visitor.visName = visName.getText().toString();
                    validVisID=isValidEmailAddress(visitor.visID);
                    if(validVisID==false){
                        visID.setBackgroundColor(Color.RED);
                        visID.setTextColor(Color.WHITE);
                    }else{
                        visID.setBackgroundColor(Color.TRANSPARENT);
                        visID.setTextColor(Color.argb(100,233,30,99));
                    }
                }
            }
        });

        visPN.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    visitor.visPN = visPN.getText().toString();
                    validVisPN=isValidPhoneNumber(visitor.visPN);
                    if(validVisPN==false){
                        visPN.setBackgroundColor(Color.RED);
//                        visPN.setHintTextColor(Color.WHITE);
                        visPN.setTextColor(Color.WHITE);
                    }else{
                        visPN.setBackgroundColor(Color.TRANSPARENT);
                        visPN.setTextColor(Color.argb(100,233,30,99));

                    }
                }
            }
        });

        HostID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    visitor.hostID = HostID.getText().toString();
                    visitor.hostName = HostName.getText().toString();
                    validHostID=isValidEmailAddress(visitor.hostID);
                    if(validHostID==false){
                        HostID.setBackgroundColor(Color.RED);
//                        HostID.setHintTextColor(Color.WHITE);
                        HostID.setTextColor(Color.WHITE);
                    }else{
                        HostID.setBackgroundColor(Color.TRANSPARENT);
                        HostID.setTextColor(Color.argb(100,233,30,99));
                    }
                }
            }
        });

        HostPN.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    visitor.hostPN = HostPN.getText().toString();
                    validHostPN=isValidPhoneNumber(visitor.hostPN);

                    if(validHostPN==false){
                        HostPN.setBackgroundColor(Color.RED);

                        HostPN.setTextColor(Color.WHITE);
                    }else{
                        HostPN.setBackgroundColor(Color.TRANSPARENT);
                        HostPN.setTextColor(Color.argb(100,216,27,96));

                    }
                }
//                if(validHostID && validHostPN && validVisID && validVisPN ) {
//                    CheckIn.setActivated(true);
//
//                } else {
//                    CheckIn.setActivated(false);
//                }
            }


        });
        CheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitor.isCheckedIn = true;
                visitor.timeCheckIn = System.currentTimeMillis();

                visitorDB.document(visitor.visID).set(visitor).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String subject="Visitor "+visitor.visName+" Arrived to Meet You";
                        String message="Visitor Name: "+visitor.visName+"\nVisitor Email ID: "+visitor.visID+"\nVisitor Phone Number: "+visitor.visPN;


                        if(isOnline()) {
                            sendMail(visitor.visID, visitor.hostID, subject, message);
                        }
                        try{
                            SmsManager smgr = SmsManager.getDefault();
                            smgr.sendTextMessage(visitor.hostPN.toString(),null,message,null,null);

                        }
                        catch (Exception e){}


                        if(validHostID && validHostPN && validVisID && validVisPN ) {
                            Log.d("debug", "valid");

                            Intent intent = new Intent(context, CheckOut.class);
                            startActivity(intent);
                        }else{
                            Log.d("debug", "something not valid");
                        }

                    }
                });
            }
        });
    }
}
