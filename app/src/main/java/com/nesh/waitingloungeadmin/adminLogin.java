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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class adminLogin extends AppCompatActivity {
    FirebaseAuth mAuth;
    ProgressBar pbAdminLogin;
    EditText adminEmailLogin,adminPasswordLogin;
    int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            Intent in=new Intent(adminLogin.this,MainActivity.class);
            startActivity(in);
            finish();
        }
        setContentView(R.layout.activity_admin_login);
        setTitle("Admin Login");
        adminEmailLogin=(EditText)findViewById(R.id.adminEmailLogin);
        adminPasswordLogin=(EditText)findViewById(R.id.adminPasswordLogin);
        pbAdminLogin=(ProgressBar)findViewById(R.id.adminLoginProgessbar);
        pbAdminLogin.setVisibility(View.GONE);
        mAuth=FirebaseAuth.getInstance();
    }
    public void loginAdmin(View view){
        pbAdminLogin.setVisibility(View.VISIBLE);
        final String email=adminEmailLogin.getText().toString().trim();
        String password=adminPasswordLogin.getText().toString().trim();
        if(email.isEmpty() || password.isEmpty() || password.length()<6){
            Toast.makeText(getApplicationContext(),"Enter EmailID and Password with at least 6 characters",Toast.LENGTH_SHORT).show();
            pbAdminLogin.setVisibility(View.GONE);
        }
        else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                                    Intent in = new Intent(adminLogin.this, MainActivity.class);
                                    startActivity(in);
                                    finish();
                                    pbAdminLogin.setVisibility(View.GONE);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                pbAdminLogin.setVisibility(View.GONE);
                            }
                        });
        }

    }
    public void adminSignUpFtn(View view){
        Intent in=new Intent(adminLogin.this,adminSignup.class);
        startActivity(in);
    }
    public void resetPwd(View view){
        Intent in=new Intent(adminLogin.this,resetPassword.class);
        startActivity(in);
    }
}
