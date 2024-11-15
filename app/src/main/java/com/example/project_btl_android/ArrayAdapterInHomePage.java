package com.example.project_btl_android;

import android.app.Activity;
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

public class ArrayAdapterInHomePage extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;

    public ArrayAdapterInHomePage(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
    }
    //gọi hàm getView

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Tạo đế
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy 1 phần tử
        Product myProduct = myList.get(position);
        //Ánh xạ id
        ImageView img = convertView.findViewById(R.id.imgProductInHomePage);
        byte[] imageData = myProduct.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        img.setImageBitmap(bitmap);
        TextView txtProductNameInHomePage = convertView.findViewById(R.id.txtProductNameInHomePage);
        txtProductNameInHomePage.setText(myProduct.getName());
        TextView txtProductPriceInHomePage = convertView.findViewById(R.id.txtProductPriceInHomePage);
        txtProductPriceInHomePage.setText(myProduct.getPrice()+" VND");
        return convertView;
    }
}
