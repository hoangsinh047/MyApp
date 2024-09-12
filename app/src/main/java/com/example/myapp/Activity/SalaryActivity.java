package com.example.myapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        salaryAdapter = new SalaryAdapter(salaryList, this);
        recyclerView.setAdapter(salaryAdapter);

        // danh sach nhan vien
        loadEmployeesFromFirebase();

        // danh sach luong
        loadSalariesFromFirebase();

        Button btnAddEmployeeSlr = findViewById(R.id.btnAddEmployeeSlr);
        btnAddEmployeeSlr.setOnClickListener(v -> openCalculateSalaryDialog());

        ImageButton btnBackSalary = findViewById(R.id.btnBackSalary);
        btnBackSalary.setOnClickListener(view -> {
            Intent intent = new Intent(SalaryActivity.this, PlaceActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void onSalaryUpdated() {
        loadSalariesFromFirebase();
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
            Employee selectedEmployee = (Employee) spinnerEmployee.getSelectedItem();
            if (selectedEmployee == null) {
                Toast.makeText(SalaryActivity.this, "Vui lòng chọn nhân viên", Toast.LENGTH_SHORT).show();
                return;
            }

            if(edtBasicSalary.getText().toString().isEmpty() || edtAllowance.getText().toString().isEmpty()
                    || edtTax.getText().toString().isEmpty() || edtInsurance.getText().toString().isEmpty()) {
                Toast.makeText(SalaryActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            double basicSalary = Double.parseDouble(edtBasicSalary.getText().toString());
            double allowance = Double.parseDouble(edtAllowance.getText().toString());
            double tax = Double.parseDouble(edtTax.getText().toString());
            double insurance = Double.parseDouble(edtInsurance.getText().toString());

            double netSalary = basicSalary + allowance - tax - insurance;

            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedSalary = decimalFormat.format(netSalary);

            txtResult.setText("Lương thực nhận: " + formattedSalary);

            DatabaseReference counterRef = FirebaseDatabase.getInstance().getReference("counters/salaryID");
            counterRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Kiểm tra nếu giá trị từ snapshot là null
                    Long currentID = snapshot.getValue(Long.class);
                    if (currentID == null) {
                        // Nếu currentID là null, khởi tạo giá trị mặc định là 0
                        currentID = 0L;
                    }

                    long newID = currentID + 1;
                    String newIdString = String.valueOf(newID);

                    SalaryItem newItem = new SalaryItem(newIdString, selectedEmployee.getName(), formattedSalary, basicSalary, allowance, tax, insurance);

                    DatabaseReference salaryRef = FirebaseDatabase.getInstance().getReference("salaries");
                    salaryRef.child(newIdString).setValue(newItem).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SalaryActivity.this, "Lương đã được lưu", Toast.LENGTH_SHORT).show();
                            salaryList.add(newItem);
                            salaryAdapter.notifyDataSetChanged();
                            counterRef.setValue(newID); // Cập nhật số thứ tự mới
                        } else {
                            Toast.makeText(SalaryActivity.this, "Lưu lương thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SalaryActivity.this, "Lỗi khi lấy số thứ tự", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", error.getMessage());
                }
            });

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    private void saveSalaryToFirebase(SalaryItem salaryItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        String salaryId = ref.push().getKey();
        salaryItem.setId(salaryId);  // Set ID vào SalaryItem

        if (salaryId != null) {
            ref.child(salaryId).setValue(salaryItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SalaryActivity.this, "Lương đã được lưu", Toast.LENGTH_SHORT).show();
                    salaryList.add(salaryItem);
                    salaryAdapter.notifyItemInserted(salaryList.size() - 1);  // Cập nhật danh sách
                } else {
                    Toast.makeText(SalaryActivity.this, "Lưu lương thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                employeeList.clear(); //Xoa danh sach cu
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
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    public void loadSalariesFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                salaryList.clear(); //Xoa danh sach cu
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SalaryItem salaryItem = dataSnapshot.getValue(SalaryItem.class);
                    salaryList.add(salaryItem);
                }
                salaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
                Toast.makeText(SalaryActivity.this, "Lỗi khi tải danh sách lương", Toast.LENGTH_SHORT).show();
            }

            private int findPositionById(String id) {
                for (int i = 0; i < salaryList.size(); i++) {
                    if (salaryList.get(i).getId().equals(id)) {
                        return i;
                    }
                }
                return -1; // Nếu không tìm thấy, trả về -1
            }

        });
    }
    public void openEditSalaryDialog(SalaryItem salaryItem, int position) {
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

        // Set initial values
        edtBasicSalary.setText(String.valueOf(salaryItem.getBasicSalary()));
        edtAllowance.setText(String.valueOf(salaryItem.getAllowance()));
        edtTax.setText(String.valueOf(salaryItem.getTax()));
        edtInsurance.setText(String.valueOf(salaryItem.getInsurance()));

        btnCalculate.setText("Cập nhật");

        btnCalculate.setOnClickListener(v -> {
            double basicSalary = Double.parseDouble(edtBasicSalary.getText().toString());
            double allowance = Double.parseDouble(edtAllowance.getText().toString());
            double tax = Double.parseDouble(edtTax.getText().toString());
            double insurance = Double.parseDouble(edtInsurance.getText().toString());

            double netSalary = basicSalary + allowance - tax - insurance;
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            String formattedSalary = decimalFormat.format(netSalary);

            salaryItem.setBasicSalary(basicSalary);
            salaryItem.setAllowance(allowance);
            salaryItem.setTax(tax);
            salaryItem.setInsurance(insurance);
            salaryItem.setSalary(formattedSalary);

            DatabaseReference salaryRef = FirebaseDatabase.getInstance().getReference("salaries");
            salaryRef.child(salaryItem.getId()).setValue(salaryItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SalaryActivity.this, "Lương đã được cập nhật", Toast.LENGTH_SHORT).show();
                    salaryList.set(position, salaryItem);
                    salaryAdapter.notifyItemChanged(position);
                } else {
                    Toast.makeText(SalaryActivity.this, "Cập nhật lương thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
