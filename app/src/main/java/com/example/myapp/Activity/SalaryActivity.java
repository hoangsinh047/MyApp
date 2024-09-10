package com.example.myapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.item.Employee;
import com.example.myapp.R;
import com.example.myapp.Adapter.SalaryAdapter;
import com.example.myapp.item.SalaryItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SalaryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SalaryAdapter salaryAdapter;
    private List<SalaryItem> salaryList = new ArrayList<>();
    private List<Employee> employeeList = new ArrayList<>();
    private ArrayAdapter<Employee> employeeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        salaryAdapter = new SalaryAdapter(salaryList);
        recyclerView.setAdapter(salaryAdapter);

        // Load employees from Firebase
        loadEmployeesFromFirebase();

        // Load salaries from Firebase
        loadSalariesFromFirebase(); // Sửa tên phương thức từ LoadSalariesFromFirebase thành loadSalariesFromFirebase

        Button btnAddEmployeeSlr = findViewById(R.id.btnAddEmployeeSlr);
        btnAddEmployeeSlr.setOnClickListener(v -> openCalculateSalaryDialog());

        ImageButton btnBackSalary = findViewById(R.id.btnBackSalary);
        btnBackSalary.setOnClickListener(view -> {
            Intent intent = new Intent(SalaryActivity.this, PlaceActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void openCalculateSalaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_salary_calculation, null);
        builder.setView(dialogView);

        Spinner spinnerEmployee = dialogView.findViewById(R.id.spinnerEmployee);
        EditText edtBasicSalary = dialogView.findViewById(R.id.edtBasicSalary);
        EditText edtAllowance = dialogView.findViewById(R.id.edtAllowance);
        EditText edtTax = dialogView.findViewById(R.id.edtTax);
        EditText edtInsurance = dialogView.findViewById(R.id.edtInsurance);
        Button btnCalculate = dialogView.findViewById(R.id.btnCalculate);
        TextView txtResult = dialogView.findViewById(R.id.txtResult);

        employeeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, employeeList);
        employeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmployee.setAdapter(employeeAdapter);

        btnCalculate.setOnClickListener(v -> {
            // Lay thong tin nhan vien
            Employee selectedEmployee = (Employee) spinnerEmployee.getSelectedItem();
            if (selectedEmployee == null) {
                Toast.makeText(SalaryActivity.this, "Vui lòng chọn nhân viên", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lay gia tri tu EditText
            double basicSalary = Double.parseDouble(edtBasicSalary.getText().toString());
            double allowance = Double.parseDouble(edtAllowance.getText().toString());
            double tax = Double.parseDouble(edtTax.getText().toString());
            double insurance = Double.parseDouble(edtInsurance.getText().toString());

            // Tinh luong
            double netSalary = basicSalary + allowance - tax - insurance;

            // Format result
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedSalary = decimalFormat.format(netSalary);

            // Ket qua
            txtResult.setText("Lương thực nhận: " + formattedSalary);

            // Add new item to RecyclerView
            String id = selectedEmployee.getId();
            String name = selectedEmployee.getName();
            SalaryItem newItem = new SalaryItem(id, name, formattedSalary);
            salaryList.add(newItem);
            salaryAdapter.notifyDataSetChanged();

            // Save salary to Firebase
            saveSalaryToFirebase(newItem);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveSalaryToFirebase(SalaryItem salaryItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        String salaryId = ref.push().getKey();

        if (salaryId != null) {
            ref.child(salaryId).setValue(salaryItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SalaryActivity.this, "Lương đã được lưu", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SalaryActivity.this, "Lưu lương thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadEmployeesFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("employees");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                employeeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Employee employee = dataSnapshot.getValue(Employee.class);
                    if (employee != null) {
                        employeeList.add(employee);
                    }
                }
                if (employeeAdapter != null) {
                    employeeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalaryActivity.this, "Lỗi khi tải danh sách nhân viên", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSalariesFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                salaryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SalaryItem salaryItem = dataSnapshot.getValue(SalaryItem.class);
                    if (salaryItem != null) {
                        salaryList.add(salaryItem);
                    }
                }
                salaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalaryActivity.this, "Lỗi khi tải danh sách lương", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
