package com.example.project_btl_android;

import com.example.project_btl_android.Product;

import java.io.Serializable;
import java.util.ArrayList;

public class Cart implements Serializable {
    private String idCart;
    private String userName;
    int ProductQuantity;
    private double total = 0.0;
    private double tax = 0.0;

    public String getIdCart() {
        return idCart;
    }

    public String getUserName() {
        return userName;
    }

    public void setIdCart(String idCart) {
        this.idCart = idCart;
    }

    public void setIdUser(String userName) {
        this.userName = userName;
    }

    public int getProductQuantity() {
        return ProductQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        ProductQuantity = productQuantity;
    }

    public Cart(String idCart, String userName) {
        this.idCart = idCart;
        this.userName = userName;
    }

    public void updateTotalInCart(ArrayList<Product> listProductSelect){
        this.total = 0.0;
        for (Product product: listProductSelect) {
            this.total += product.toMoney(product.getQuantityInCart());
        }
    }

    public double getTotal() {
        return total;
    }
}
