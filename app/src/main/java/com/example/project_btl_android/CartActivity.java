package com.example.project_btl_android;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    TextView txtBackFromCartToHomepage, txtToOrder, txtSelectAllInCart, txtTotalMoneyProductSelect;
    ListView lvProductInCart;
    ArrayList<Product> myListProductInCart, mySelectProductList;
    ArrayAdapterInCart myArrayAdapterInCart;
    SQLiteDatabase database = null;
    Cart cart;
    float x1 = 0, x2 = 0;
    Boolean check = false;
    private GestureDetector gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Hiển thị danh sách trong giỏ hàng và biến check selectAll = fasle
        showCart();
        check = false;

        //Chuyển về lại homepage
        txtBackFromCartToHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Thanh toán
        // Nếu danh sách các sản phẩm được chọn không rỗng -> chuyển dữ liệu sang trang thanh toán
        txtToOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mySelectProductList != null && !mySelectProductList.isEmpty()) {
                    Intent intent = new Intent(CartActivity.this, BillActivity.class);
                    intent.putExtra("Total", Double.parseDouble(txtTotalMoneyProductSelect.getText().toString()));
                    intent.putExtra("selectProductList", mySelectProductList);
                    intent.putExtra("Cart", cart);
                    startActivity(intent);
                } else {
                    Toast.makeText(CartActivity.this, "Bạn chưa chọn sản phẩm nào", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Chỉ hủy chọn tất cả khi tất cả các sản phaarmd đang được chọn
        //Nếu 1 hoặc nhiều sản phẩm đang được chọn -> chọn tất
        txtSelectAllInCart = findViewById(R.id.txtSelectAllInCart);
        txtSelectAllInCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    ArrayList<Product> tmpListProductInCart = new ArrayList<>();
                    tmpListProductInCart.addAll(myListProductInCart);
                    for (Product product:myListProductInCart) {
                        if(product.quantityInCart(cart, database) > product.checkQuantityVisible(database)){
                            tmpListProductInCart.remove(product);
                        }
                    }
                    if(mySelectProductList.size() == tmpListProductInCart.size()){
                        for (int i = 0; i<myListProductInCart.size(); i++){
                            //Các biến lưu trữ trạng thái check box được cập nhật
                            myArrayAdapterInCart.checkboxStates.set(i, false);
                        }
                        mySelectProductList.removeAll(myListProductInCart);
                    }
                    else {
                        mySelectProductList.removeAll(myListProductInCart);
                        for (int i = 0; i<myListProductInCart.size(); i++){
                            //Các biến lưu trữ trạng thái check box được cập nhật
                            myArrayAdapterInCart.checkboxStates.set(i, true);
                            mySelectProductList.add(myListProductInCart.get(i));
                            myArrayAdapterInCart.checkEnable(myListProductInCart.get(i), i);
                        }
                    }
                }
                catch (Exception e){
                    Toast.makeText(CartActivity.this, "buggg: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                //Cập nhật lại giao diện
                myArrayAdapterInCart.toStringTotal(cart);
                myArrayAdapterInCart.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCart();
        myArrayAdapterInCart.toStringTotal(cart);
    }

    //Thêm sản phẩm vào giỏ hàng -> đưa vào danh sách sản phẩm được chọn
    public void addSelectProduct(Product product){
        mySelectProductList.add(product);
    }

    //Xóa ra khỏi giỏ hàng
    public void removeSelectProduct(Product product){
        mySelectProductList.remove(product);
    }

    public void showCart(){
        myListProductInCart = new ArrayList<>();
        mySelectProductList = new ArrayList<>();
        txtTotalMoneyProductSelect = findViewById(R.id.txtTotalMoneyProductSelect);
        lvProductInCart = findViewById(R.id.lvProductInCart);
        txtBackFromCartToHomepage = findViewById(R.id.txtBackFromCartToHomepage);
        txtToOrder = findViewById(R.id.txtToOrder);
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        //Lấy cart được gửi từ homepage -> xác định cart theo người dùng
        cart = (Cart) getIntent().getSerializableExtra("Cart");

        // Lấy danh sách các sản phẩm có trong giỏ hàng
        String table = "Detail_ProductCart inner join Product on Detail_ProductCart.ProductID = Product.ID";
        Cursor c_ProductInCart = database.query(table, new String[]{"Product.*", "Detail_ProductCart.Quantity"},
                "CartID = ? AND Product.Deleted = ?", new String[]{cart.getIdCart(), "0"}, null, null, null);
        if(!c_ProductInCart.moveToFirst()){
            LinearLayout layout = findViewById(R.id.main);
            layout.setBackgroundResource(R.drawable.rong);
        }
        else{
            while (c_ProductInCart.isAfterLast() == false){
                Product product = new Product(c_ProductInCart.getInt(0), c_ProductInCart.getInt(1), c_ProductInCart.getString(2), c_ProductInCart.getDouble(3),
                        c_ProductInCart.getString(4), c_ProductInCart.getInt(5), c_ProductInCart.getBlob(6));
                product.setQuantityInCart(Integer.parseInt(c_ProductInCart.getString(8)));
                myListProductInCart.add(product);
                c_ProductInCart.moveToNext();
            }
        }
        c_ProductInCart.close();

        //Đưa trạng thái các sản phẩm trong giỏ hàng về chưa được chọn
        //Xóa các sản phẩm trong danh sách sản phẩm được chọn
        for (Product product:myListProductInCart) {
            product.setIscheck(false);
            mySelectProductList.clear();
        }

        // Hiển thị các sản phẩm lên giao diện
        myArrayAdapterInCart = new ArrayAdapterInCart(CartActivity.this, R.layout.layout_product_incart, myListProductInCart);
        //set cart để đưa cart vào các phương thức cần thực hiện trong adapter
        myArrayAdapterInCart.setCart(cart);
        myArrayAdapterInCart.setDatabase(database);
        lvProductInCart.setAdapter(myArrayAdapterInCart);
    }

}