package com.example.innovacceems;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class CheckOut extends AppCompatActivity {
    public Context context;
    Button CheckOut;

    EditText visName;
    TextView visID;
    EditText visPN;
    EditText HostName;
    EditText HostID;
    EditText HostPN;

    Visitor visitor;

    final String username = "innov302iiitd@gmail.com";
    final String password = "Innov302@iiitd";

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
        setContentView(R.layout.activity_check_out);
        getSupportActionBar().hide();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        visName = findViewById(R.id.VisName);
        visID = findViewById(R.id.VisID);
        visPN = findViewById(R.id.VisPN);
        HostName = findViewById(R.id.HostName);
        HostID = findViewById(R.id.HostID);
        HostPN = findViewById(R.id.HostPN);

        CheckOut = findViewById(R.id.CheckOut);
        context = this;
        CheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visitor = MainActivity.visitor;

                visitor.isCheckedIn = false;
                visitor.timeCheckOut = System.currentTimeMillis();

                visitorDB.document(visitor.visID).set(visitor).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {



                        String subject="Thank you for visiting Innovaccer: Visit Details";
                        String message="Hello " + visitor.visName + ",\nPlease find below the details of your visit to Innovaccer today.\n" +
                                "Visitor Name: "+visitor.visName+"\nVisitor Phone Number: "+visitor.visPN+"\nTime of Check In: "+DateFormat.getInstance().format(visitor.timeCheckIn)+
                                "\nTime of Check Out: "+DateFormat.getInstance().format(visitor.timeCheckOut)+
                                "\nHost Name: "+visitor.hostName+"\nAddress Visited: 2nd and 9th Floor, Tower 3,\n" +
                                "Candor Techspace\n" +
                                "Sector 62, 201309\n" +
                                "Noida, UP\n" +
                                "India";


                        if(isOnline()) {
                            sendMail(username, visitor.visID, subject, message);
                        }

                        try{
                            SmsManager smgr = SmsManager.getDefault();
                            smgr.sendTextMessage(visitor.visPN.toString(),null,message,null,null);

                        }
                        catch (Exception e){}
                        Intent intent = new Intent(context, ThankYou.class);
                        startActivity(intent);
                    }

                });



            }
        });
    }
}
