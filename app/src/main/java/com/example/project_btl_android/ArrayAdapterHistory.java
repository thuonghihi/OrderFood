package com.example.project_btl_android;

import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class ArrayAdapterHistory extends ArrayAdapter<Bill> {
    Activity context;
    int idLayout;
    ArrayList<Bill> myList;
    SQLiteDatabase database = null;

    public ArrayAdapterHistory(Activity context, int idLayout, ArrayList<Bill> myList) {
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
        Bill bill = myList.get(position);
        //Ánh xạ id
        TextView txtBillID = convertView.findViewById(R.id.txtBillID);
        TextView txtBillDay = convertView.findViewById(R.id.txtBillDay);
        TextView txtTotal = convertView.findViewById(R.id.txtTotal);
        txtBillID.setText("#HD"+bill.getIdBill());
        txtBillDay.setText(bill.getCreateDay());
        txtTotal.setText("Tổng tiền: " + bill.getToTal() + "VND");
        database = context.openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        ArrayList<Product> myListProductInBill = new ArrayList<>();
        ArrayList<String> listProductQuantityInHistory = new ArrayList<>();
        Cursor c = database.query(
                "Bill B " +
                        "INNER JOIN Detail_ProductBill D ON B.ID = D.BillID " +
                        "INNER JOIN Product P ON P.ID = D.ProductID",
                new String[]{"P.*, D.Quantity"},
                "B.ID = ?",
                new String[]{bill.getIdBill()},
                null,
                null,
                null
        );
        c.moveToFirst();
        while (c.isAfterLast() == false){
            Product product = new Product(c.getInt(0), c.getInt(1), c.getString(2),
                    c.getDouble(3), c.getString(4), c.getInt(5), c.getBlob(6));
            myListProductInBill.add(product);
            //Lấy số lượng bán ra của 1 sản phẩm trong bill
            listProductQuantityInHistory.add(c.getString(8));
            c.moveToNext();
        }
        c.close();

        //Lấy danh sách teen sản phẩm trong 1 bill
        ArrayList<String> listProductName = new ArrayList<>();
        for (Product product:myListProductInBill) {
            listProductName.add(product.getName());
        }

        //Tạo layout mới chứa tên các sản phẩm trong bill
        LinearLayout productinBill = convertView.findViewById(R.id.productinBill);
        int i = 0;
        for (String name:listProductName) {
            TextView txt = new TextView(context);
            txt.setText(listProductQuantityInHistory.get(i) + "x " + name);
            txt.setPadding(5, 0, 0 ,0);
            txt.setTypeface(null, Typeface.BOLD);
            txt.setTextSize(16);
            productinBill.addView(txt);
            i++;
        }
        return convertView;
    }
}
