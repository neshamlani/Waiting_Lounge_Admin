package com.nesh.waitingloungeadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    SwipeRefreshLayout swipe;
    FirebaseFirestore fs;
    RecyclerView rv;
    String pname,removed="";
    List<String> name;
    List<String> time;
    card c;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater=new MenuInflater(this);
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.logout:
                mAuth= FirebaseAuth.getInstance();
                mAuth.signOut();
                finish();
                return true;
            case R.id.profile:
                Intent in=new Intent(MainActivity.this,profile.class);
                startActivity(in);
                return true;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipe=(SwipeRefreshLayout)findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //swipe refresh code
                swipe.setRefreshing(false);
            }
        });
        name=new ArrayList<>();
        time=new ArrayList<>();
        rv=findViewById(R.id.recyclerView);
        LinearLayoutManager lm=new LinearLayoutManager(this);
        rv.setLayoutManager(lm);
        c=new card(name,time);
        rv.setAdapter(c);
        mAuth=FirebaseAuth.getInstance();
        String user=mAuth.getCurrentUser().getEmail();
        fs=FirebaseFirestore.getInstance();
        fs.collection("Users_Cust").document(user).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot post=task.getResult();
                        try{
                            JSONObject js=new JSONObject(post.getData());
                            pname=js.getString("Property_Name");
                            name.clear();
                            time.clear();
                            fs.collection(pname)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                            @Nullable FirebaseFirestoreException e) {
                                            for(DocumentChange dc:queryDocumentSnapshots.getDocumentChanges()){
                                                switch (dc.getType()){
                                                    case ADDED:
                                                        time.add(dc.getDocument().getId());
                                                        JSONObject js=new JSONObject(dc.getDocument().getData());
                                                        try {
                                                            name.add(js.getString("Email"));
                                                        } catch (JSONException ex) {
                                                            ex.printStackTrace();
                                                        }
                                                        NotificationChannel notificationChannel=new NotificationChannel("2","Order",NotificationManager.IMPORTANCE_DEFAULT);
                                                        notificationChannel.enableLights(true);
                                                        notificationChannel.enableVibration(true);
                                                        notificationChannel.setLightColor(Color.GREEN);
                                                        Intent in=new Intent(MainActivity.this,profile.class);
                                                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),1970,in,PendingIntent.FLAG_ONE_SHOT);
                                                        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                        Notification.Builder notifyBuilder=new Notification.Builder(getApplicationContext(),"1")
                                                                .setSmallIcon(R.color.colorAccent)
                                                                .setContentTitle("New Order")
                                                                .setContentText(name.get(name.size()-1))
                                                                .setContentIntent(pendingIntent)
                                                                .setAutoCancel(false)
                                                                .setChannelId("2")
                                                                .setSound(soundUri);
                                                        NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                                        nm.createNotificationChannel(notificationChannel);
                                                        nm.notify(1970,notifyBuilder.build());
                                                        break;
                                                    case REMOVED:
                                                        removed=dc.getDocument().getData().toString();
                                                        notificationChannel=new NotificationChannel("1","Order",NotificationManager.IMPORTANCE_DEFAULT);
                                                        notificationChannel.enableLights(true);
                                                        notificationChannel.enableVibration(true);
                                                        notificationChannel.setLightColor(Color.GREEN);
                                                        in=new Intent(MainActivity.this,profile.class);
                                                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        pendingIntent=PendingIntent.getActivity(getApplicationContext(),1971,in,PendingIntent.FLAG_ONE_SHOT);
                                                        soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                        notifyBuilder=new Notification.Builder(getApplicationContext(),"1")
                                                                .setSmallIcon(R.color.colorAccent)
                                                                .setContentTitle("Order Removed")
                                                                .setContentText(removed)
                                                                .setContentIntent(pendingIntent)
                                                                .setAutoCancel(false)
                                                                .setChannelId("1")
                                                                .setSound(soundUri);
                                                        nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                                        nm.createNotificationChannel(notificationChannel);
                                                        nm.notify(1971,notifyBuilder.build());
                                                        c.notifyDataSetChanged();
                                                        break;
                                                }
                                            }
                                            c.notifyDataSetChanged();
                                        }
                                    });

                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

     }
}
