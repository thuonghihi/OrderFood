package com.example.project_btl_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ArrayAdapterInBill extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;

    public ArrayAdapterInBill(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy 1 phần tử
        Product myProduct = myList.get(position);
        //Ánh xạ id
        ImageView img = convertView.findViewById(R.id.imgProductInBill);
        byte[] imageData = myProduct.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        img.setImageBitmap(bitmap);
        int n = myProduct.getQuantityInCart();
        TextView txtProductWithQuantityInBill = convertView.findViewById(R.id.txtProductWithQuantityInBill);
        txtProductWithQuantityInBill.setText(n + "x " + myProduct.getName());
        TextView txtProductToMoneyInBill = convertView.findViewById(R.id.txtProductToMoneyInBill);
        txtProductToMoneyInBill.setText(myProduct.toMoney(n)+"");
        return convertView;
    }
}
