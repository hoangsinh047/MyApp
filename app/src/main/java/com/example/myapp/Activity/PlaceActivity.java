package com.example.myapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapp.R;

public class PlaceActivity extends AppCompatActivity {
    private TextView txtInfo;
    private ConstraintLayout btn1;
    private ConstraintLayout btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        // Cài đặt padding cho ViewCompat
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo btn1 sau khi setContentView
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);

        // Đặt sự kiện click cho btn1
        btn1.setOnClickListener(v -> {
            Intent intent = new Intent(PlaceActivity.this, EmployeesActivity.class);
            startActivity(intent);
        });

        btn2.setOnClickListener(v ->{
            Intent intent = new Intent(PlaceActivity.this, SalaryActivity.class);
            startActivity(intent);
        });
    }
}
