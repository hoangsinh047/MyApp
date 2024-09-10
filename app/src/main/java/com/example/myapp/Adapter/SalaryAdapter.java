package com.example.myapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.item.SalaryItem;

import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    private List<SalaryItem> salaryList;

    public SalaryAdapter(List<SalaryItem> salaryList) {
        this.salaryList = salaryList;
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
}
