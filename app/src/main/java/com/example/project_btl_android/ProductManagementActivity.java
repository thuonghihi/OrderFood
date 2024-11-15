package com.example.project_btl_android;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

/** @noinspection ALL*/
public class ProductManagementActivity extends AppCompatActivity {
    ListView lvProduct;
    TextView txtBackToManagementHomepageFromProduct, txtVAddProduct;
    ArrayAdapterProductManagement myAdapterProductManagement;
    ArrayList<Product> myListProduct;
    ArrayList<String> myListProductName;
    SQLiteDatabase database = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtVAddProduct = findViewById(R.id.txtVAddProduct);
        txtBackToManagementHomepageFromProduct = findViewById(R.id.txtBackToManagementHomepageFromProduct);
        lvProduct = findViewById(R.id.lvProduct);
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        myListProduct = new ArrayList<>();
        myListProductName = new ArrayList<>();

        Cursor cursor = database.query("Product", null, "Deleted = ?", new String[]{"0"}, null, null, "ID DESC");
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false){
            Product product = new Product(cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                    cursor.getDouble(3), cursor.getString(4), cursor.getInt(5), cursor.getBlob(6));
            myListProductName.add(product.getName());
            myListProduct.add(product);
            cursor.moveToNext();
        }
        cursor.close();
        myAdapterProductManagement = new ArrayAdapterProductManagement(ProductManagementActivity.this, R.layout.layout_product, myListProduct);
        myAdapterProductManagement.setDatabase(database);
        lvProduct.setAdapter(myAdapterProductManagement);

        txtBackToManagementHomepageFromProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductManagementActivity.this, ManagementHomepageActivity.class);
                startActivity(intent);
            }
        });

        txtVAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProductManagementActivity.this, AddProductManagementActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProductManagementActivity.this, ManagementHomepageActivity.class);
        startActivity(intent);
    }
}