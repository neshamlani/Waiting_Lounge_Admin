package com.nesh.waitingloungeadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class changeProfile extends AppCompatActivity {
    EditText editname,editBusiness,editNumber,editAddress;
    String name="",number="",address="",business="",user;
    FirebaseFirestore fs;
    FirebaseAuth mAuth;
    ProgressBar pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);
        editname=(EditText)findViewById(R.id.editName);
        editBusiness=(EditText)findViewById(R.id.editBusiness);
        editNumber=(EditText)findViewById(R.id.editNumber);
        editAddress=(EditText)findViewById(R.id.editAddress);
        pb=findViewById(R.id.updateProgess);
        pb.setVisibility(View.GONE);
        mAuth=FirebaseAuth.getInstance();
        fs=FirebaseFirestore.getInstance();
        user=mAuth.getCurrentUser().getEmail();
        fs.collection("Users_Cust").document(user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot post=task.getResult();
                            if(post.exists()){
                                JSONObject js=new JSONObject(post.getData());
                                try {
                                    name = js.getString("Name");
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                try {
                                    business = js.getString("Property_Name");
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                try{
                                    number=js.getString("Number");
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                try{
                                    address=js.getString("Address");
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                            editname.setText(name);
                            editBusiness.setText(business);
                            editNumber.setText(number);
                            editAddress.setText(address);
                        }
                        else{
                            Toast.makeText(getApplicationContext(),task.getException().toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void updateProfile(View view){
        pb.setVisibility(View.VISIBLE);
        name=editname.getText().toString().trim();
        business=editBusiness.getText().toString().trim();
        number=editNumber.getText().toString().trim();
        address=editAddress.getText().toString().trim();
        if(name.isEmpty() || business.isEmpty() || number.isEmpty() || address.isEmpty()){
            Toast.makeText(getApplicationContext(),"Enter ALl The Values",Toast.LENGTH_LONG).show();
            pb.setVisibility(View.GONE);
        }
        else {
            fs=FirebaseFirestore.getInstance();
            final Map<String, Object> data=new HashMap<>();
            data.clear();
            data.put("Property_Name",business);
            data.put("Name",name);
            data.put("Number",number);
            data.put("Address",address);
            PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
            mCallback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    fs=FirebaseFirestore.getInstance();
                    user=mAuth.getCurrentUser().getEmail();
                    fs.collection("Users_Clients").document(user).set(data, SetOptions.merge())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(),"Data Update",Toast.LENGTH_LONG).show();
                                        pb.setVisibility(View.GONE);
                                        Intent in=new Intent(changeProfile.this,profile.class);
                                        startActivity(in);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                        pb.setVisibility(View.GONE);
                                    }
                                }
                            });

                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            };
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91"+number,
                    60,
                    TimeUnit.SECONDS,
                    changeProfile.this,
                    mCallback
            );
        }
    }
}
