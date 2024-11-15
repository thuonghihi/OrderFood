package com.example.project_btl_android;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ArrayAdapterProductInStatistic extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;
    SQLiteDatabase database = null;
    StatisticManagementActivity mActivity;
    double revenue = 0.0;

    public ArrayAdapterProductInStatistic(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (StatisticManagementActivity) context;
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
        Product product = myList.get(position);
        //Ánh xạ id
        TextView txtProductNameInStatistic = convertView.findViewById(R.id.txtProductNameInStatistic);
        TextView txtProductRevenueInStatistic = convertView.findViewById(R.id.txtProductRevenueInStatistic);
        txtProductNameInStatistic.setText(product.getName());
        database = context.openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        revenue = 0.0;
        Cursor cursor = database.rawQuery(
                "SELECT P.Price, D.Quantity " +
                        "FROM Detail_ProductBill D " +
                        "INNER JOIN Product P ON D.ProductID = P.ID " +
                        "INNER JOIN Bill B ON D.BillID = B.ID " +
                        "WHERE D.ProductID = ? AND B.CreateDay >= ? AND B.CreateDay <= ?",
                new String[]{
                        String.valueOf(product.getIdProduct()),
                        mActivity.edtTimeFrom.getText().toString(),
                        mActivity.edtTimeTo.getText().toString()
                });
        int quantity = 0;
        double price = 0.0;
        if(cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                price = cursor.getDouble(0);
                quantity += cursor.getInt(1);
                cursor.moveToNext();
            }
        }
        cursor.close();
        revenue = price * quantity + price * quantity * 10 / 100;
        txtProductRevenueInStatistic.setText(revenue + " VND");
        return convertView;
    }
}
