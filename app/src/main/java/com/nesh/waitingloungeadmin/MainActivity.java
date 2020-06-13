package com.nesh.waitingloungeadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    SwipeRefreshLayout swipe;
    FirebaseFirestore fs;
    RecyclerView rv;
    String pname;
    List<String> name;
    List<String> time;
    List<String> removeCust;
    card c;
    ListenerRegistration registration;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.i("Permission","Granted");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
        }
        swipe=(SwipeRefreshLayout)findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fs=FirebaseFirestore.getInstance();
                fs.collection(pname).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                time.clear();
                                name.clear();
                                for(QueryDocumentSnapshot post:task.getResult()){
                                    JSONObject js=new JSONObject(post.getData());
                                    try {
                                        time.add("Time:"+js.getString("Time")+"  Token:"+js.getString("Token")+"  Sr.No."+js.getString("Token"));
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                    try {
                                        name.add(js.getString("Email"));
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                c.notifyDataSetChanged();
                            }
                        });
                swipe.setRefreshing(false);
            }
        });
        name=new ArrayList<>();
        time=new ArrayList<>();
        removeCust=new ArrayList<>();
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
                            Query q=fs.collection(pname);
                            registration=q.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                    @Nullable FirebaseFirestoreException e) {
                                    for(DocumentChange dc: queryDocumentSnapshots.getDocumentChanges()){
                                        switch (dc.getType()){
                                            case ADDED:
                                                JSONObject js=new JSONObject(dc.getDocument().getData());
                                                try {
                                                    removeCust.add(js.getString("Time"));
                                                    time.add("Time:"+js.getString("Time")+"  Token:"+js.getString("Token")+"  Sr.No."+js.getString("Token"));
                                                } catch (JSONException ex) {
                                                    ex.printStackTrace();
                                                }
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
                                                        .setContentText("Email")
                                                        .setContentIntent(pendingIntent)
                                                        .setAutoCancel(false)
                                                        .setChannelId("1")
                                                        .setSound(soundUri);
                                                NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                                nm.createNotificationChannel(notificationChannel);
                                                nm.notify(1970,notifyBuilder.build());
                                                c.notifyDataSetChanged();
                                                break;

                                            case REMOVED:
                                                String remove=dc.getDocument().getId();
                                                notificationChannel=new NotificationChannel("2","Order",NotificationManager.IMPORTANCE_DEFAULT);
                                                notificationChannel.enableLights(true);
                                                notificationChannel.enableVibration(true);
                                                notificationChannel.setLightColor(Color.GREEN);
                                                in=new Intent(MainActivity.this,profile.class);
                                                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                pendingIntent=PendingIntent.getActivity(getApplicationContext(),1971,in,PendingIntent.FLAG_ONE_SHOT);
                                                soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                notifyBuilder=new Notification.Builder(getApplicationContext(),"2")
                                                        .setSmallIcon(R.color.colorAccent)
                                                        .setContentTitle("Order Removed")
                                                        .setContentText(remove)
                                                        .setContentIntent(pendingIntent)
                                                        .setAutoCancel(false)
                                                        .setChannelId("2")
                                                        .setSound(soundUri);
                                                nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                                                nm.createNotificationChannel(notificationChannel);
                                                nm.notify(1971,notifyBuilder.build());
                                                fs=FirebaseFirestore.getInstance();
                                                fs.collection(pname).get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                time.clear();
                                                                name.clear();
                                                                for(QueryDocumentSnapshot post:task.getResult()){
                                                                    JSONObject js=new JSONObject(post.getData());
                                                                    try {
                                                                        time.add("Time:"+js.getString("Time")+"  Token:"+js.getString("Token")+"  Sr.No."+js.getString("Token"));
                                                                    } catch (JSONException ex) {
                                                                        ex.printStackTrace();
                                                                    }
                                                                    try {
                                                                        name.add(js.getString("Email"));
                                                                    } catch (JSONException ex) {
                                                                        ex.printStackTrace();
                                                                    }
                                                                }
                                                                c.notifyDataSetChanged();
                                                            }
                                                        });
                                                break;
                                        }
                                        //registration.remove();
                                        c.notifyDataSetChanged();
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

     public void nextCustomer(View view){
         fs=FirebaseFirestore.getInstance();
         fs.collection(pname).get()
                 .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                     @Override
                     public void onComplete(@NonNull Task<QuerySnapshot> task) {
                         removeCust.clear();
                        for(QueryDocumentSnapshot post:task.getResult()){
                            removeCust.add(post.getId());
                        }
                         fs=FirebaseFirestore.getInstance();
                         String remove=removeCust.get(0);
                         if(!remove.isEmpty()) {
                             fs.collection(pname).document(remove).delete()
                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                         @Override
                                         public void onComplete(@NonNull Task<Void> task) {
                                             Toast.makeText(getApplicationContext(), "Current Customer Is Removed", Toast.LENGTH_LONG).show();
                                             runOnUiThread(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     fs = FirebaseFirestore.getInstance();
                                                     fs.collection(pname).get()
                                                             .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                     time.clear();
                                                                     name.clear();
                                                                     int i = 0;
                                                                     for (QueryDocumentSnapshot post : task.getResult()) {
                                                                         i++;
                                                                         JSONObject js = new JSONObject(post.getData());
                                                                         Map<String, Object> data = new HashMap<>();
                                                                         data.put("Token", i);
                                                                         fs.collection(pname).document(post.getId()).set(data, SetOptions.merge());
                                                                         try {
                                                                             time.add("Time:" + js.getString("Time") + "  Token:" + js.getString("Token") + "  Sr.No." + js.getString("Token"));
                                                                         } catch (JSONException ex) {
                                                                             ex.printStackTrace();
                                                                         }
                                                                         try {
                                                                             name.add(js.getString("Email"));
                                                                         } catch (JSONException ex) {
                                                                             ex.printStackTrace();
                                                                         }

                                                                     }
                                                                     c.notifyDataSetChanged();
                                                                 }
                                                             });
                                                 }
                                             });

                                         }
                                     });
                         }
                     }
                 });
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
    }
}
