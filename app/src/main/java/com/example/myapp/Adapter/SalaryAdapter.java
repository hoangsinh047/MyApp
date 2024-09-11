package com.example.myapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.item.SalaryItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    private List<SalaryItem> salaryList;
    private OnItemClickListener listener;
    private Context context;

    // Constructor nhận context từ Activity hoặc Fragment
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
        holder.tvID.setText("" + item.getId());
        holder.tvName.setText("" + item.getName());
        holder.tvSalary.setText("  " + item.getSalary());

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Chọn hành động");
            String[] options = {"Cập nhật", "Xóa"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Cập nhật
                    showUpdateDialog(item);
                } else if (which == 1) {
                    // Xóa
                    deleteSalary(item);
                    salaryList.remove(item);
                    notifyDataSetChanged();
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(SalaryItem salaryItem);
    }

    private void deleteSalary(SalaryItem salaryItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        ref.child(salaryItem.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Lương đã được xóa", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Xóa lương thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(SalaryItem salaryItem) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_salary_calculation, null);
        dialogBuilder.setView(dialogView);

        Spinner spinnerEmployee = dialogView.findViewById(R.id.spinnerEmployee);
        EditText edtBasicSalary = dialogView.findViewById(R.id.edtBasicSalary);
        EditText edtAllowance = dialogView.findViewById(R.id.edtAllowance);
        EditText edtTax = dialogView.findViewById(R.id.edtTax);
        EditText edtInsurance = dialogView.findViewById(R.id.edtInsurance);
        TextView txtResult = dialogView.findViewById(R.id.txtResult);
        Button btnCalculate = dialogView.findViewById(R.id.btnCalculate);

        edtBasicSalary.setText(String.valueOf(salaryItem.getBasicSalary()));
        edtAllowance.setText(String.valueOf(salaryItem.getAllowance()));
        edtTax.setText(String.valueOf(salaryItem.getTax()));
        edtInsurance.setText(String.valueOf(salaryItem.getInsurance()));

        btnCalculate.setOnClickListener(v -> {
            double basicSalary = Double.parseDouble(edtBasicSalary.getText().toString());
            double allowance = Double.parseDouble(edtAllowance.getText().toString());
            double tax = Double.parseDouble(edtTax.getText().toString());
            double insurance = Double.parseDouble(edtInsurance.getText().toString());

            double finalSalary = basicSalary + allowance - tax - insurance;
            txtResult.setText("Lương thực nhận: " + finalSalary);
        });

        dialogBuilder.setPositiveButton("Cập nhật", (dialog, which) -> {
            salaryItem.setBasicSalary(Double.parseDouble(edtBasicSalary.getText().toString()));
            salaryItem.setAllowance(Double.parseDouble(edtAllowance.getText().toString()));
            salaryItem.setTax(Double.parseDouble(edtTax.getText().toString()));
            salaryItem.setInsurance(Double.parseDouble(edtInsurance.getText().toString()));

            updateSalaryInFirebase(salaryItem);
        });

        dialogBuilder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }



    private void updateSalaryInFirebase(SalaryItem salaryItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("salaries");

        ref.child(salaryItem.getId()).setValue(salaryItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Lương đã được cập nhật", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Lỗi khi cập nhật lương", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
