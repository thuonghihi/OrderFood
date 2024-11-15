package com.example.project_btl_android;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailProductActivity extends AppCompatActivity {
    SQLiteDatabase database=null;
    TextView txtProductPriceInDetail, txtProductNameInDetail, txtProductDescriptionInDetail,
            txtBackFromDetailToHomepage, txtToMoneyInDetail, txtOrderFromDetail;
    EditText txtProductQuantityInCart;
    Product productToDetail;
    Cart cart;
    ImageView imgProductInDetail, imgVAddProductToCartInDetail, imgVDeleteProductFromCartInDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Ánh xạ ID
        txtProductNameInDetail = findViewById(R.id.txtProductNameInDetail);
        txtProductPriceInDetail = findViewById(R.id.txtProductPriceInDetail);
        txtProductDescriptionInDetail = findViewById(R.id.txtDescriptionProductInDetail);
        txtProductQuantityInCart = findViewById(R.id.txtPoductQuantityInCart);
        txtBackFromDetailToHomepage = findViewById(R.id.txtBackFromDetailToHomepage);
        txtOrderFromDetail = findViewById(R.id.txtOrderFromDetail);
        imgProductInDetail = findViewById(R.id.imgProductInDetail);
        txtToMoneyInDetail = findViewById(R.id.txtToMoneyInDetail);
        imgVAddProductToCartInDetail = findViewById(R.id.imgVAddProductToCartInDetail);
        imgVDeleteProductFromCartInDetail = findViewById(R.id.imgVDeleteProductFromCartInDetail);
        cart = (Cart) getIntent().getSerializableExtra("Cart");
        productToDetail = (Product) getIntent().getSerializableExtra("product");
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        productToDetail.setQuantity(productToDetail.quantity(database));

        //chuyển từ detailActivity đến HomePageActivity
        txtBackFromDetailToHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Đưa thông tin sản phẩm lên giao diện
        byte[] imageData = productToDetail.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        imgProductInDetail.setImageBitmap(bitmap);
        txtProductNameInDetail.setText(productToDetail.getName());
        txtProductPriceInDetail.setText(productToDetail.getPrice()+"");
        txtProductDescriptionInDetail.setText(productToDetail.getDescription());
        txtProductQuantityInCart.setText(productToDetail.quantityInCart(cart, database)+"");
        txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.toMoney(productToDetail.quantityInCart(cart, database)));
        checkQuantity(productToDetail.quantityInCart(cart, database));
        if(productToDetail.quantityInCart(cart, database) > productToDetail.checkQuantityVisible(database)){
            Toast.makeText(this, "Không đủ số lượng sản phẩm", Toast.LENGTH_SHORT).show();
            txtOrderFromDetail.setEnabled(false);
            imgVAddProductToCartInDetail.setEnabled(false);
            imgVDeleteProductFromCartInDetail.setEnabled(false);
            txtProductQuantityInCart.setEnabled(false);
        }

        //Thêm số lượng sản phẩm vào giỏ hàng từ detail
        imgVAddProductToCartInDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = productToDetail.quantityInCart(cart, database);
                txtProductQuantityInCart.setText(n+1+"");
            }
        });

        //Giarm số lượng sản phẩm trong giỏ hàng từ detail
        imgVDeleteProductFromCartInDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = productToDetail.quantityInCart(cart, database);
                txtProductQuantityInCart.setText(n-1+"");
                checkQuantity(n-1);
            }
        });

        txtProductQuantityInCart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            //Sau khi nhập xong
            @Override
            public void afterTextChanged(Editable s) {;
                if (String.valueOf(s).equals("") || Integer.parseInt(String.valueOf(s)) == 0) {
                    productToDetail.deleteProductFromCart(cart, database);
                    txtToMoneyInDetail.setText("Thành tiền: 0.0");
                }
                else {
                    txtProductQuantityInCart.setSelection(txtProductQuantityInCart.getText().length());
                    productToDetail.updateProductQuantityToCart(cart, txtProductQuantityInCart, database, DetailProductActivity.this);
                    checkQuantity(productToDetail.quantityInCart(cart, database));
                }
                txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.toMoney(productToDetail.quantityInCart(cart, database)));
            }
        });

        //Thanh toán từ detail
        txtOrderFromDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productToDetail.quantityInCart(cart, database) > 0){
                    Intent intent = new Intent(DetailProductActivity.this, BillActivity.class);
                    intent.putExtra("ProductFromDetail", productToDetail);
                    intent.putExtra("Cart", cart);
                    intent.setAction("FromDetail");
                    startActivity(intent);
                }
                else {
                    Toast.makeText(DetailProductActivity.this, "Vui lòng nhập số lượng sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Kiểm tra số lượng trong chi tiết
    //Nếu = 0, ẩn trừ và số lượng
    public void checkQuantity(int q){
        if(q == 0){
            imgVDeleteProductFromCartInDetail.setVisibility(View.INVISIBLE);
            txtProductQuantityInCart.setVisibility(View.INVISIBLE);
        }
        else {
            imgVDeleteProductFromCartInDetail.setVisibility(View.VISIBLE);
            txtProductQuantityInCart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkQuantity(productToDetail.quantityInCart(cart,database));
        txtToMoneyInDetail.setText("Thành tiền: " + productToDetail.toMoney(productToDetail.quantityInCart(cart, database)));
    }
}