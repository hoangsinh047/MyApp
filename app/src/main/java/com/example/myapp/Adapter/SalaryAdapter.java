package com.example.myapp.Adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.Activity.SalaryActivity;
import com.example.myapp.R;
import com.example.myapp.item.SalaryItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    private List<SalaryItem> salaryList;
    private Context context;

    public SalaryAdapter(List<SalaryItem> salaryList, Context context) {
        this.salaryList = salaryList;
        this.context = context;
    }

    @NonNull
    @Override
    public SalaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_salary, parent, false);
        return new SalaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryViewHolder holder, int position) {
        SalaryItem item = salaryList.get(position);
        holder.tvID.setText(item.getId()); // Gán giá trị ID
        holder.tvName.setText(item.getName()); // Gán giá trị Name
        holder.tvSalary.setText(String.valueOf(item.getSalary()));

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Chọn hành động");
            String[] options = {"Cập nhật", "Xóa"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Cập nhật
                    if (context instanceof SalaryActivity) {
                        ((SalaryActivity) context).openEditSalaryDialog(item, position);
                    }
                } else if (which == 1) {
                    // Xóa
                    deleteSalary(item, position);
                }
            });
            builder.show();
        });
    }


    @Override
    public int getItemCount() {
        return salaryList.size();
    }

    public static class SalaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvID, tvName, tvSalary;

        public SalaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvID = itemView.findViewById(R.id.tvIDSlr);
            tvName = itemView.findViewById(R.id.tvNameSlr);
            tvSalary = itemView.findViewById(R.id.tvSalary);
        }
    }

    private void addNewSalaryItem(SalaryItem salaryItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        // Tạo ID tự động
        String id = ref.push().getKey(); // Firebase tự sinh ID mới
        salaryItem.setId(id); // Gán ID vào đối tượng

        ref.child(id).setValue(salaryItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Lương đã được thêm", Toast.LENGTH_SHORT).show();
                salaryList.add(salaryItem);
                notifyDataSetChanged(); //cap nhat lai danh sach
            } else {
                Toast.makeText(context, "Lỗi khi thêm lương: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSalary(SalaryItem salaryItem, int position) {
        DatabaseReference salaryRef = FirebaseDatabase.getInstance().getReference("salaries");
        salaryRef.child(salaryItem.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Lương đã được xóa", Toast.LENGTH_SHORT).show();
                salaryList.remove(position);
                notifyItemRemoved(position);
            } else {
                Toast.makeText(context, "Xóa lương thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(SalaryItem salaryItem) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_salary_calculation, null);
        dialogBuilder.setView(dialogView);

        EditText edtBasicSalary = dialogView.findViewById(R.id.edtBasicSalary);
        EditText edtAllowance = dialogView.findViewById(R.id.edtAllowance);
        EditText edtTax = dialogView.findViewById(R.id.edtTax);
        EditText edtInsurance = dialogView.findViewById(R.id.edtInsurance);
        TextView txtResult = dialogView.findViewById(R.id.txtResult);
        Button btnCalculate = dialogView.findViewById(R.id.btnCalculate);

        // Điền các giá trị hiện tại vào dialog
        edtBasicSalary.setText(String.valueOf(salaryItem.getBasicSalary()));
        edtAllowance.setText(String.valueOf(salaryItem.getAllowance()));
        edtTax.setText(String.valueOf(salaryItem.getTax()));
        edtInsurance.setText(String.valueOf(salaryItem.getInsurance()));

        // Xử lý khi người dùng nhấn nút "Tính lương"
        btnCalculate.setOnClickListener(v -> {
            try {
                double basicSalary = Double.parseDouble(edtBasicSalary.getText().toString());
                double allowance = Double.parseDouble(edtAllowance.getText().toString());
                double tax = Double.parseDouble(edtTax.getText().toString());
                double insurance = Double.parseDouble(edtInsurance.getText().toString());

                double finalSalary = basicSalary + allowance - tax - insurance;
                txtResult.setText("Lương thực nhận: " + finalSalary);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Vui lòng nhập các giá trị hợp lệ", Toast.LENGTH_SHORT).show();
            }

        });

        dialogBuilder.setPositiveButton("Cập nhật", (dialog, which) -> {
            try {
                salaryItem.setBasicSalary(Double.parseDouble(edtBasicSalary.getText().toString()));
                salaryItem.setAllowance(Double.parseDouble(edtAllowance.getText().toString()));
                salaryItem.setTax(Double.parseDouble(edtTax.getText().toString()));
                salaryItem.setInsurance(Double.parseDouble(edtInsurance.getText().toString()));

                updateSalaryInFirebase(salaryItem); // Gọi hàm cập nhật Firebase
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Vui lòng nhập các giá trị hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });


        dialogBuilder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateSalaryInFirebase(SalaryItem salaryItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        if (salaryItem.getId() != null && !salaryItem.getId().isEmpty()) {
            ref.child(salaryItem.getId()).setValue(salaryItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Lương đã được cập nhật", Toast.LENGTH_SHORT).show();
                    // Cập nhật danh sách và giao diện
                    int position = findPositionById(salaryItem.getId());
                    if (position != -1) {
                        salaryList.set(position, salaryItem);
                        notifyItemChanged(position); // Chỉ cập nhật item tại vị trí đó thay vì toàn bộ danh sách
                    }
                } else {
                    Toast.makeText(context, "Lỗi khi cập nhật lương: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "ID không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }


    private int findPositionById(String id) {
        for (int i = 0; i < salaryList.size(); i++) {
            if (salaryList.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1; // Nếu không tìm thấy, trả về -1
    }



}

