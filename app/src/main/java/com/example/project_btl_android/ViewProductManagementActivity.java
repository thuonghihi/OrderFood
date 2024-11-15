package com.example.project_btl_android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ViewProductManagementActivity extends AppCompatActivity {
    ImageView imgVProductImageInViewProduct;
    TextView txtCategoryInViewProduct, txtProductNameInViewProduct, txtProductPriceInViewProduct,
            txtProductDescriptionInViewProduct, txtProductQuantityInViewProduct, txtBackToManagementHomepageFromView;
    SQLiteDatabase database = null;
    ArrayList<Category> myListCategory;
    Product product;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_product_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgVProductImageInViewProduct = findViewById(R.id.imgVProductImageInViewProduct);
        txtCategoryInViewProduct = findViewById(R.id.txtCategoryInViewProduct);
        txtProductNameInViewProduct = findViewById(R.id.txtProductNameInViewProduct);
        txtProductPriceInViewProduct = findViewById(R.id.txtProductPriceInViewProduct);
        txtProductDescriptionInViewProduct = findViewById(R.id.txtProductDescriptionInViewProduct);
        txtProductQuantityInViewProduct = findViewById(R.id.txtProductQuantityInViewProduct);
        txtBackToManagementHomepageFromView = findViewById(R.id.txtBackToManagementHomepageFromView);
        myListCategory = new ArrayList<>();
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        Cursor cursor = database.query("Category", null, null, null, null, null, null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Category category = new Category(cursor.getInt(0), cursor.getString(1));
            myListCategory.add(category);
            cursor.moveToNext();
        }
        txtBackToManagementHomepageFromView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        product = (Product) getIntent().getSerializableExtra("product");
        byte[] imageData = product.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        imgVProductImageInViewProduct.setImageBitmap(bitmap);
        int CategoryID = product.getIdCategory();
        String CategoryName = null;
        for (Category category : myListCategory) {
            if (category.getId() == CategoryID) {
                CategoryName = category.getName();
                break;
            }
        }
        txtCategoryInViewProduct.setText(CategoryName);
        txtProductNameInViewProduct.setText(product.getName());
        txtProductPriceInViewProduct.setText(product.getPrice()+ " VND");
        txtProductDescriptionInViewProduct.setText(product.getDescription());
        txtProductQuantityInViewProduct.setText(product.getQuantity() +"");
    }
}