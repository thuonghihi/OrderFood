package com.example.project_btl_android;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;

public class Product implements Serializable {
    private int idProduct;
    private int idCategory;
    private String name;
    private double price;
    private String description;
    private int quantity;
    private byte[] image;
    private boolean ischeck = false;

    private int quantityInCart = 0;

    public int getIdProduct() {
        return idProduct;
    }

    public int getIdCategory() {
        return idCategory;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public byte[] getImage() {
        return image;
    }

    public boolean isIscheck() {
        return ischeck;
    }

    public void setIdProduct(int idProduct) {
        this.idProduct = idProduct;
    }

    public void setIdCategory(int idCategory) {
        this.idCategory = idCategory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setImage(byte[] Image) {
        this.image = Image;
    }

    public void setIscheck(boolean ischeck) {
        this.ischeck = ischeck;
    }

    public Product(int idProduct, int idCategory, String name, double price, String description, int quantity, byte[] image) {
        this.idProduct = idProduct;
        this.idCategory = idCategory;
        this.name = name;
        this.price = price;
        this.description = description;
        this.quantity = quantity;
        this.image = image;
    }

    public void updateProductQuantityToCart(Cart cart, TextView txtSoLuong, SQLiteDatabase database, Activity context){
        int n = Integer.parseInt(txtSoLuong.getText().toString());
        if(n>checkQuantityVisible(database)){
            Toast.makeText(context, "Không đủ số lượng sản phẩm", Toast.LENGTH_SHORT).show();
            txtSoLuong.setText(this.quantityInCart + "");
            return;
        }
        if(n != 0) {
            this.setQuantityInCart(n);
            //Kieerm tra danh sách sản phẩm trong giỏ hàng
            Cursor c_productQuantityInCart = database.query("Detail_ProductCart", null, "ProductID = ? AND CartID = ?",
                    new String[]{String.valueOf(this.getIdProduct()), cart.getIdCart()}, null, null, null);
            //Nếu chưa có -> thêm mới
            if (!c_productQuantityInCart.moveToFirst()) {
                ContentValues contentValue = new ContentValues();
                contentValue.put("CartID", cart.getIdCart());
                contentValue.put("ProductID", this.getIdProduct());
                contentValue.put("Quantity", n);
                database.insert("Detail_ProductCart", null, contentValue);
            }
            //Đã có -> update số lượng
            else {
                ContentValues value = new ContentValues();
                value.put("Quantity", n);
                database.update("Detail_ProductCart", value, "ProductID = ? AND CartID = ?", new String[]{String.valueOf(this.getIdProduct()), cart.getIdCart()});
            }
            c_productQuantityInCart.close();
        }
    }

    public void deleteProductFromCart(Cart cart, SQLiteDatabase database){
        database.delete("Detail_ProductCart", "ProductID = ? AND CartID = ?", new String[]{String.valueOf(this.getIdProduct()), cart.getIdCart()});
    }

    public int quantityInCart(Cart cart, SQLiteDatabase database){
        Cursor query = database.query("Detail_ProductCart", new String[]{"Quantity"}, "ProductID = ? AND CartID = ?", new String[]{String.valueOf(this.getIdProduct()), cart.getIdCart()}, null, null, null, String.valueOf(1));
        if(query.moveToFirst()){
            return query.getInt(0);
        }
        query.close();
        return 0;
    }

    public int quantity(SQLiteDatabase database){
        Cursor query = database.query("Product", new String[]{"Quantity"}, "ID = ?", new String[]{String.valueOf(this.getIdProduct())}, null, null, null, String.valueOf(1));
        if(query.moveToFirst()){
            return query.getInt(0);
        }
        query.close();
        return 0;
    }

    public double toMoney(int quantity){
        return this.price*quantity;
    }

    public void setQuantityInCart(int quantity){
        this.quantityInCart = quantity;
    }

    public int getQuantityInCart() {
        return quantityInCart;
    }

    public int checkQuantityVisible(SQLiteDatabase database){
        Cursor cursor = database.query("Product", new String[]{"Quantity"}, "ID = ?", new String[]{String.valueOf(this.getIdProduct())}, null, null, null, String.valueOf(1));
        if(cursor.moveToFirst()){
            return cursor.getInt(0);
        }
        return 0;
    }
}
