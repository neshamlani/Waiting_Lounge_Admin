package com.nesh.waitingloungeadmin;

import androidx.annotation.NonNull;
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
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    SwipeRefreshLayout swipe;
    FirebaseFirestore fs;
    String pname;
    ListView lv;
    List<String> waitingTime=new ArrayList<>();
    ArrayAdapter<String> adapter;
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
                fs.collection(pname).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    waitingTime.clear();
                                    for(QueryDocumentSnapshot post:task.getResult()){
                                        waitingTime.add(post.getId());
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                swipe.setRefreshing(false);
            }
        });
        lv=(ListView)findViewById(R.id.listDisplay);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,waitingTime);
        lv.setAdapter(adapter);
       Timer t= new Timer();
       t.scheduleAtFixedRate(new TimerTask() {
           @RequiresApi(api = Build.VERSION_CODES.O)
           @Override
           public void run() {
               dataQuery();
           }
       },0,60000);
     }
     public void dataQuery(){
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
                             Toast.makeText(getApplicationContext(),pname,Toast.LENGTH_LONG).show();
                             fs.collection(pname).get()
                                     .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                         @Override
                                         public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                             if(task.isSuccessful()){
                                                 waitingTime.clear();
                                                 for(QueryDocumentSnapshot post:task.getResult()){
                                                     waitingTime.add(post.getId());
                                                 }
                                                 adapter.notifyDataSetChanged();
                                             }
                                         }
                                     });
                         }catch(Exception e){}
                     }
                 });
     }
}
