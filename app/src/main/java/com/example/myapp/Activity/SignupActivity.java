package com.example.myapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapp.R;
import com.example.myapp.item.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText edtUsername, edtPhone, edtPassword, edtConfirmPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Đảm bảo rằng layout file tên là activity_signup.xml

        edtUsername = findViewById(R.id.edtUsername);
        edtPhone = findViewById(R.id.edtphone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPass = findViewById(R.id.edtConfirmPass);

        // Ánh xạ Button từ layout
        Button btnSignup = findViewById(R.id.btnSignup); // Thay đổi ID nếu cần
        ImageButton btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Nut quay lai
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Nut dang ky
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String confirmPass = edtConfirmPass.getText().toString().trim();

                if(password.equals(confirmPass)) {
                    createUser(username, phone, password);
                } else {
                    Toast.makeText(SignupActivity.this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void createUser(final String email, final String phone, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Đăng ký thành công
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        User userInfo = new User(email, phone);
                        mDatabase.child("users").child(userId).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignupActivity.this, "Lưu thông tin người dùng thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Xử lý lỗi đăng ký
                    String errorMessage = "Đăng ký thất bại";
                    if (task.getException() != null) {
                        errorMessage = task.getException().getMessage();
                    }
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
