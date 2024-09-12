package com.example.myapp.item;

public class SalaryItem {
    private String id;
    private String name;
    private String salary;
    private double basicSalary;
    private double allowance;
    private double tax;
    private double insurance;

    public SalaryItem() {
    }

    public SalaryItem(String id, String name, String salary, double basicSalary, double allowance, double tax, double insurance) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.basicSalary = basicSalary;
        this.allowance = allowance;
        this.tax = tax;
        this.insurance = insurance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter và Setter cho salary
    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    // Getter và Setter cho basicSalary
    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    // Getter và Setter cho allowance
    public double getAllowance() {
        return allowance;
    }

    public void setAllowance(double allowance) {
        this.allowance = allowance;
    }

    // Getter và Setter cho tax
    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    // Getter và Setter cho insurance
    public double getInsurance() {
        return insurance;
    }

    public void setInsurance(double insurance) {
        this.insurance = insurance;
    }

    @Override
    public String toString() {
        return name + " - " + salary;
    }
}
