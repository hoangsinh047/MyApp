package com.example.myapp.item;

public class SalaryItem {
    private String id;
    private String name;
    private String salary;

    public SalaryItem() {
    }

    public SalaryItem(String id, String name, String salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    // Getter và Setter cho id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter và Setter cho name
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

    @Override
    public String toString() {
        return name + " - " + salary;
    }
}
