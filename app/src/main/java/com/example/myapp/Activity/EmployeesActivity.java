package com.example.myapp.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapp.item.Employee;
import com.example.myapp.Adapter.EmployeeAdapter;
import com.example.myapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmployeesActivity extends AppCompatActivity {

    private ImageButton btnBackEmp;
    private Button btnAddEmployee;
    private ArrayList<Employee> employeeList;
    private EmployeeAdapter adapter;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_employee);

        // Khởi tạo danh sách nhân viên
        employeeList = new ArrayList<>();
        adapter = new EmployeeAdapter(this, employeeList);

        ListView lvEmployee = findViewById(R.id.lvEmployee);
        lvEmployee.setAdapter(adapter);

        // Nút Back và Add
        btnBackEmp = findViewById(R.id.btnBackEmp);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);

        btnAddEmployee.setOnClickListener(v -> showAddEmployeeDialog());

        btnBackEmp.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeesActivity.this, PlaceActivity.class);
            startActivity(intent);
            finish();
        });

        loadEmployeesFromFirebase();
    }

    private void showAddEmployeeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_employee, null);
        builder.setView(dialogView);

        // Khởi tạo thông tin trong dialog
        EditText etID = dialogView.findViewById(R.id.etID);
        EditText etEmployeeName = dialogView.findViewById(R.id.etEmployeeName);
        EditText etEmployeeEmail = dialogView.findViewById(R.id.etEmployeeEmail);
        EditText etEmployeePhone = dialogView.findViewById(R.id.etEmployeePhone);
        EditText etEmployeePosition = dialogView.findViewById(R.id.etEmployeePosition);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        // Tạo dialog
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            hideKeyboard(); // Ẩn bàn phím khi nhấn nút lưu

            String id = etID.getText().toString();
            String name = etEmployeeName.getText().toString();
            String email = etEmployeeEmail.getText().toString();
            String phone = etEmployeePhone.getText().toString();
            String position = etEmployeePosition.getText().toString();

            // Thêm nhân viên mới
            Employee newEmployee = new Employee(id, name, email, phone, position);

            // Lưu vào Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("employees");

            ref.child(id).setValue(newEmployee).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Nhân viên đã được thêm mới", Toast.LENGTH_SHORT).show();
                    loadEmployeesFromFirebase(); // Làm mới danh sách nhân viên
                } else {
                    Toast.makeText(this, "Thêm nhân viên thất bại", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss(); // Đóng dialog
            });
        });

        dialog.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadEmployeesFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("employees");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                employeeList.clear(); // Xóa danh sách hiện tại
                for (DataSnapshot employeeSnapshot : snapshot.getChildren()) {
                    Employee employee = employeeSnapshot.getValue(Employee.class);
                    if (employee != null) {
                        employeeList.add(employee); // Thêm nhân viên vào danh sách
                    }
                }
                adapter.notifyDataSetChanged(); // Cập nhật adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmployeesActivity.this, "Lỗi khi tải danh sách nhân viên", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
