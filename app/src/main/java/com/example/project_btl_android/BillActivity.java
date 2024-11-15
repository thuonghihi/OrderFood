package com.example.project_btl_android;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.util.ArrayList;

/** @noinspection ALL*/
public class BillActivity extends AppCompatActivity {
    SQLiteDatabase database = null;
    ArrayList<Product> myProductListInBill;
    ArrayAdapterInBill myAdapterInBill;
    ListView lvProductInBill;
    Double totalMoney = 0.0;
    String UserName = null;
    Cart cart;
    TextView txtBackFromBillToCart, txtOrder, txtTaxMoney, txtTotalMoneyWithTax, txtTotalMoneyProductInBill;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Mở database và ánh xạ ID
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        txtTaxMoney = findViewById(R.id.txtTaxMoney);
        txtTotalMoneyWithTax = findViewById(R.id.txtTotalMoneyWithTax);
        txtTotalMoneyProductInBill = findViewById(R.id.txtTotalMoneyProductInBill);
        lvProductInBill = findViewById(R.id.lvProductInBill);
        myProductListInBill = new ArrayList<>();
        txtOrder = findViewById(R.id.txtToOrder);
        txtOrder.setText("Đặt hàng");

        //Xử lý Intent đến từ Detail
        if(getIntent().getAction() != null && getIntent().getAction().equals("FromDetail")){
            Product productFromDetail = (Product) getIntent().getSerializableExtra("ProductFromDetail");
            cart = (Cart) getIntent().getSerializableExtra("Cart");
            totalMoney = productFromDetail.toMoney(productFromDetail.quantityInCart(cart,database));
            productFromDetail.setQuantityInCart(productFromDetail.quantityInCart(cart, database));
            UserName = cart.getUserName();
            myProductListInBill.add(productFromDetail);
        }

        //Intent đến từ Activity khác (Cart)
        else{
            myProductListInBill = (ArrayList<Product>) getIntent().getSerializableExtra("selectProductList");
            totalMoney = getIntent().getDoubleExtra("Total", 1);
            cart = (Cart) getIntent().getSerializableExtra("Cart");
            UserName = cart.getUserName();
        }

        //Đưa danh sách sản phẩm lên giao diện
        myAdapterInBill = new ArrayAdapterInBill(BillActivity.this, R.layout.layout_product_inbill, myProductListInBill);
        lvProductInBill.setAdapter(myAdapterInBill);

        //Đưa tổng tiền (cùng thuế) lên giao diện
        txtTotalMoneyProductInBill.setText(totalMoney+"");
        double tax = totalMoney*10.0/100;
        txtTaxMoney.setText(tax + "");
        txtTotalMoneyWithTax.setText((totalMoney+tax)+"");

        //Tạo dialog xác nhận đặt hàng
        AlertDialog.Builder dialog = new AlertDialog.Builder(BillActivity.this);
        dialog.setTitle("XÁC NHẬN ĐẶT HÀNG");

        //Cancel => huỷ dialog
        dialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        //OK => xử lý
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(BillActivity.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();

                //Đổi định dạng ngày
                LocalDate currentDate = LocalDate.now();
                int year = currentDate.getYear();
                int month = currentDate.getMonthValue();
                int dayOfMonth = currentDate.getDayOfMonth();
                String formattedDate = String.format("%04d-%02d-%02d", year, month, dayOfMonth);// Định dạng thành "YYYY-mm-dd"

                //Tạo bill mới với username, ngày, total
                Bill bill = new Bill(UserName, formattedDate, Double.parseDouble(txtTotalMoneyWithTax.getText().toString()));

                //Truyền dữ liệu bill mới vào CSDL
                ContentValues value = new ContentValues();
                value.put("UserName", bill.getUserName());
                value.put("CreateDay", formattedDate); // Lưu trữ ngày giờ
                value.put("Total", bill.getToTal());

                //insert trả về
                int id = (int) database.insert("Bill", null, value);
                if(id != -1) {
                    // ID bill tự tăng
                    bill.setIdBill(String.valueOf(id));
                }

                //Xử lý:
                for (Product product : myProductListInBill) {
                    //Thêm detail bill
                    ContentValues value_detail = new ContentValues();
                    value_detail.put("BillID", bill.getIdBill());
                    value_detail.put("ProductID", product.getIdProduct());
                    value_detail.put("Quantity", product.getQuantityInCart());
                    database.insert("Detail_ProductBill", null, value_detail);

                    //Giarm số lượng sản phẩm có trong hệ thống
                    product.setQuantity(product.quantity(database) - product.quantityInCart(cart, database));
                    ContentValues value_update = new ContentValues();
                    value_update.put("Quantity", product.getQuantity());
                    database.update("Product", value_update, "ID = ?", new String[]{String.valueOf(product.getIdProduct())});

                    //Xóa sản phẩm ra khỏi giỏ hàng
                    product.setQuantityInCart(0);
                    product.deleteProductFromCart(cart, database);
                    product.setQuantityInCart(0);
                    showMessage();
                }
//                return;
                txtOrder.setText("Đặt hàng thành công");
                txtOrder.setEnabled(false);
//                myAdapterInBill.notifyDataSetChanged();
            }
        });

        txtOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.create().show();
            }
        });

        txtBackFromBillToCart = findViewById(R.id.txtBackFromBillToCart);
        txtBackFromBillToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //Thông báo đặt hàng thành công
    public void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(BillActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_success, null);
        builder.setView(dialogView);
        AlertDialog dialog2 = builder.create();
        dialog2.show();
        TextView txtDone = dialogView.findViewById(R.id.txtDone);
        txtDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}