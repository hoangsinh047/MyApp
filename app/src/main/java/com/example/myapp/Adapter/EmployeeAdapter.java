package com.example.myapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapp.item.Employee;
import com.example.myapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EmployeeAdapter extends ArrayAdapter<Employee> {

    private Context context;
    private List<Employee> employees;

    public EmployeeAdapter(Context context, ArrayList<Employee> employees) {
        super(context, 0, employees);
        this.context = context;
        this.employees = employees;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Employee employee = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_employee, parent, false);
        }

        TextView tvEmployeeID = convertView.findViewById(R.id.tvEmployeeID);
        TextView tvEmployeeName = convertView.findViewById(R.id.tvEmployeeName);
        TextView tvEmployeePosition = convertView.findViewById(R.id.tvEmployeePosition);

        if (employee != null) {
            tvEmployeeID.setText(employee.getId());
            tvEmployeeName.setText(employee.getName());
            tvEmployeePosition.setText(employee.getPosition());
        }

        convertView.setOnClickListener(v -> showEmployeeOptionsDialog(employee));

        return convertView;
    }

    private void showEmployeeOptionsDialog(Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn hành động");

        String[] options = {"Chỉnh sửa", "Xóa"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    openEditEmployeeDialog(employee);
                    break;
                case 1:
                    deleteEmployee(employee);
                    break;
            }
        });

        builder.create().show();
    }

    private void openEditEmployeeDialog(Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_employee, null);
        builder.setView(dialogView);

        EditText etID = dialogView.findViewById(R.id.etID);
        EditText etEmployeeName = dialogView.findViewById(R.id.etEmployeeName);
        EditText etEmployeeEmail = dialogView.findViewById(R.id.etEmployeeEmail);
        EditText etEmployeePhone = dialogView.findViewById(R.id.etEmployeePhone);
        EditText etEmployeePosition = dialogView.findViewById(R.id.etEmployeePosition);

        Button btnSave = dialogView.findViewById(R.id.btnSave);

        etID.setText(employee.getId());
        etEmployeeName.setText(employee.getName());
        etEmployeeEmail.setText(employee.getEmail());
        etEmployeePhone.setText(employee.getPhone()); // Sử dụng String cho số điện thoại
        etEmployeePosition.setText(employee.getPosition());

        builder.setTitle("Chỉnh sửa nhân viên");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        btnSave.setOnClickListener(v -> {
            String updatedName = etEmployeeName.getText().toString();
            String updatedEmail = etEmployeeEmail.getText().toString();
            String updatedPhone = etEmployeePhone.getText().toString(); // Giữ số điện thoại dưới dạng String
            String updatedPosition = etEmployeePosition.getText().toString();

            if (!updatedName.isEmpty() && !updatedPosition.isEmpty() && !updatedPhone.isEmpty()) {
                employee.setName(updatedName);
                employee.setEmail(updatedEmail); // Cập nhật email
                employee.setPhone(updatedPhone); // Cập nhật số điện thoại
                employee.setPosition(updatedPosition);

                DatabaseReference employeeRef = FirebaseDatabase.getInstance().getReference("employees");
                employeeRef.child(employee.getId()).setValue(employee)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged(); // Refresh the list
                                alertDialog.dismiss();
                            } else {
                                Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteEmployee(Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Xóa nhân viên");
        builder.setMessage("Bạn có chắc chắn muốn xóa nhân viên này không?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            DatabaseReference employeeRef = FirebaseDatabase.getInstance().getReference("employees");
            employeeRef.child(employee.getId()).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            remove(employee);
                            notifyDataSetChanged(); // Refresh the list
                        } else {
                            Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
