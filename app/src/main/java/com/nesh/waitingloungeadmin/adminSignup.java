package com.nesh.waitingloungeadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class adminSignup extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore fs;
    Spinner sp;
    List<String> categ;
    ArrayAdapter<String> adapter;
    EditText adminName,adminEmail,adminPassword,adminAddress,adminNumber,adminPropertyName,timePerCustomer;
    String name,email,pass,address,number,propertyName,timepercust,type;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    ProgressBar pbAdminSign;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_signup);
        setTitle("Admin Sign Up");
        sp=(Spinner)findViewById(R.id.categoriesDropDown);
        categ=new ArrayList<String>();
        adminName=(EditText)findViewById(R.id.adminName);
        adminEmail=(EditText)findViewById(R.id.adminEmail);
        adminPassword=(EditText)findViewById(R.id.adminPassword);
        adminAddress=(EditText)findViewById(R.id.adminAddress);
        adminNumber=(EditText)findViewById(R.id.adminNumber);
        adminPropertyName=(EditText)findViewById(R.id.adminPropertyName);
        timePerCustomer=(EditText)findViewById(R.id.timePerCustomer);
        pbAdminSign=(ProgressBar)findViewById(R.id.adminSignupProgessbar);
        pbAdminSign.setVisibility(View.GONE);
        fs=FirebaseFirestore.getInstance();
        categ.clear();
        categ.add("Hotel");
        categ.add("Barber");
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,categ);
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    public void adminSign(View view){
        pbAdminSign.setVisibility(View.VISIBLE);
        number=adminNumber.getText().toString().trim();
        mCallBacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getApplicationContext(),"Verifing Number", Toast.LENGTH_SHORT).show();
                signUp();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+number,
                60,
                TimeUnit.SECONDS,
                adminSignup.this,
                mCallBacks
        );

    }
    public void signUp(){
        name=adminName.getText().toString().trim();
        email=adminEmail.getText().toString().trim();
        pass=adminPassword.getText().toString().trim();
        address=adminAddress.getText().toString().trim();
        propertyName=adminPropertyName.getText().toString().trim();
        timepercust=timePerCustomer.getText().toString().trim();
        if(name.isEmpty() || email.isEmpty() || pass.isEmpty() || pass.length()<6 ||address.isEmpty() || propertyName.isEmpty()|| timepercust.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Enter Details and Password with at least 6 characters",Toast.LENGTH_SHORT).show();
            pbAdminSign.setVisibility(View.GONE);
        }
        else{
            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Toast.makeText(getApplicationContext(), "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            updateData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void updateData(){
        Map<String, Object> data=new HashMap<>();
        fs=FirebaseFirestore.getInstance();
        //String key=Long.toString(System.currentTimeMillis());
        data.put(email,propertyName);
        fs=FirebaseFirestore.getInstance();
        data.clear();
        data.put("Property_Name",propertyName);
        data.put("Name",name);
        data.put("Email",email);
        data.put("Number","+91"+number);
        data.put("Address",address);
        data.put("Time",timepercust);
        fs.collection("Users_Cust")
                .document(email)
                .set(data,SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"Redirecting",Toast.LENGTH_SHORT).show();
                pbAdminSign.setVisibility(View.GONE);
                finish();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
        data.clear();
        data.put("Email",email);
        fs.collection(type).document(propertyName).set(data,SetOptions.merge());
    }
}
