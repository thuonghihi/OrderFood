package com.example.project_btl_android;

public class Bill {
    private String idBill;
    private String userName;
    private Double toTal = 0.0;
    private String createDay = null;

    public String getIdBill() {
        return idBill;
    }

    public String getUserName() {
        return userName;
    }

    public void setIdBill(String idBill) {
        this.idBill = idBill;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Bill(String idBill, String userName, String createDay, Double total) {
        this.idBill = idBill;
        this.userName = userName;
        this.createDay = createDay;
        this.toTal = total;
    }

    public Double getToTal() {
        return toTal;
    }

    public void setToTal(Double toTal) {
        this.toTal = toTal;
    }

    public String getCreateDay() {
        return createDay;
    }

    public void setCreateDay(String createDay) {
        this.createDay = createDay;
    }

    public Bill(String userName, String createDay, Double total) {
        this.userName = userName;
        this.createDay = createDay;
        this.toTal = total;
    }
}
