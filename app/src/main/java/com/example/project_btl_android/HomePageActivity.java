package com.example.project_btl_android;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.Normalizer;
import java.util.ArrayList;

/** @noinspection ALL*/
public class HomePageActivity extends AppCompatActivity {
    GridView gvProductInHomePage;
    TabHost tabHost;
    SearchView svProductInHomePage;
    TextView txtNumberProductOfCart;
    ImageView imgBtnCartInHomePage;
    Cart cart;
    ListView lvBillHistory;
    ArrayList<Product> myListProduct, displayedProducts, myListProductToCategory;
    ArrayAdapterInHomePage myAdapterInHomePage;
    ArrayAdapterHistory myArrayAdapterInHistory;
    SQLiteDatabase database=null;
    ArrayList<Bill> myListBill;
    ArrayList<Category> myListCategory;
    String userName = null;
    int currentTab = 1, currentItemInSpn = 0;
    long backPressTime;
    Spinner spnCategoryInHomePage;
    /** @noinspection deprecation*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Khi load giao diện, thực hiện thêm các control và khởi tạo tab 1
        addControl();
        tab1_action();
        spnCategoryInHomePage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                myListProductToCategory.clear();
                currentItemInSpn = position;
                Category category = (Category) parentView.getItemAtPosition(position);
                if(category.getId() == 0){
                    myListProductToCategory.addAll(myListProduct);
                }
                else{
                    Cursor cs = database.rawQuery("SELECT * FROM Product WHERE Deleted = 0 AND CategoryID = ?", new String[]{String.valueOf(category.getId())});
                    cs.moveToFirst();
                    while (!cs.isAfterLast()){
                        Product product = new Product(cs.getInt(0), cs.getInt(1), cs.getString(2), cs.getDouble(3),
                                cs.getString(4), cs.getInt(5), cs.getBlob(6));
                        myListProductToCategory.add(product);
                        cs.moveToNext();
                    }
                    cs.close();
                }
                if(svProductInHomePage.getQuery().toString().isEmpty()){
                    myAdapterInHomePage = new ArrayAdapterInHomePage(HomePageActivity.this, R.layout.layout_product_homepage, myListProductToCategory);
                    gvProductInHomePage.setAdapter(myAdapterInHomePage);
                }
                else{
                    filter(svProductInHomePage.getQuery().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Xử lý khi không có mục nào được chọn trong Spinner
            }
        });
    }

    private void addControl() {
        //Định nghĩa tabHost
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec1, spec2;
        //Tab1
        spec1 = tabHost.newTabSpec("t1");
        spec1.setContent(R.id.tab_homepage);
        spec1.setIndicator("", getResources().getDrawable(R.drawable.home));
        tabHost.addTab(spec1);
        //Tab2
        spec2 = tabHost.newTabSpec("t2");
        spec2.setContent(R.id.tab_history);
        spec2.setIndicator("", getResources().getDrawable(R.drawable.history));
        tabHost.addTab(spec2);

        //Khi chuyển tab
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("t2")) {
                    tab2_action();
                }
                else {
                    tab1_action();
                }
            }
        });

        //Ánh xạ ID
        svProductInHomePage = findViewById(R.id.svProductInHomePage);
        gvProductInHomePage = findViewById(R.id.gvProductInHomePage);
        txtNumberProductOfCart = findViewById(R.id.txtNumberProductOfCart);
        imgBtnCartInHomePage = findViewById(R.id.imgBtnCartInHomePage);
        lvBillHistory = findViewById(R.id.lvBillHistory);
        spnCategoryInHomePage = findViewById(R.id.spnCategoryInHomePage);
        myListProduct = new ArrayList<>();
        displayedProducts = new ArrayList<>();
        myListCategory = new ArrayList<>();
        myListProductToCategory = new ArrayList<>();
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);

        //Lấy ra userName để lấy đúng giỏ hàng của người dùng
        userName = getIntent().getStringExtra("userName");
        Cursor c_Cart = database.query("Cart", null, "UserName = ?", new String[]{userName}, null, null, null, String.valueOf(1));
        String CartID = null;
        if (c_Cart.moveToFirst()) {
            CartID = c_Cart.getString(0);
        }
        c_Cart.close();
        cart = new Cart(CartID, userName);
    }

    //Khi load lại activity, load lại số sản phẩm trong giỏ hàng lên giao diện
    @Override
    protected void onResume() {
        super.onResume();
        setNumberProductInCart();
    }

    public void tab1_action(){
        setNumberProductInCart();
        currentTab = 1;
        //Tránh trùng lặp khi load giao diện nhiều lần
        myListProduct.clear();
        myListCategory.clear();
        //Lấy danh sách sản phẩm có trong CSDL
        Cursor c = database.query("Product", null, "Deleted = ?", new String[]{String.valueOf(0)}, null, null, null);
        c.moveToFirst();
        while (c.isAfterLast() == false){
            Product product = new Product(c.getInt(0), c.getInt(1), c.getString(2), c.getDouble(3),
                    c.getString(4), c.getInt(5), c.getBlob(6));
            myListProduct.add(product);
            c.moveToNext();
        }
        c.close();

        Category defaultCategory = new Category(0, "Tất cả");
        myListCategory.add(0, defaultCategory);
        Cursor cursor = database.rawQuery("SELECT DISTINCT C.* FROM Category C INNER JOIN Product P ON C.ID = P.CategoryID", null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Category category = new Category(cursor.getInt(0), cursor.getString(1));
            myListCategory.add(category);
            cursor.moveToNext();
        }
        cursor.close();
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, myListCategory) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                // Hiển thị tên của category ở vị trí position
                textView.setText(myListCategory.get(position).getName());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                // Hiển thị tên của category ở vị trí position trong danh sách drop-down
                textView.setText(myListCategory.get(position).getName());
                return textView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategoryInHomePage.setAdapter(adapter);


        //Load dữ liệu lên giao diện
        myAdapterInHomePage = new ArrayAdapterInHomePage(HomePageActivity.this, R.layout.layout_product_homepage, myListProduct);
        gvProductInHomePage.setAdapter(myAdapterInHomePage);

        //Khi click vào 1 sản phẩm -> chuyển đến trang chi tiết sản phẩm
        gvProductInHomePage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentToDetailActivity = new Intent(HomePageActivity.this, DetailProductActivity.class);
                Product SelectProduct = null;
                if(svProductInHomePage.getQuery().toString().isEmpty()){
                    SelectProduct = myListProductToCategory.get(position);
                }
                else{
                    SelectProduct = filter(svProductInHomePage.getQuery().toString()).get(position);
                }
                intentToDetailActivity.putExtra("product", SelectProduct);
                intentToDetailActivity.putExtra("Cart", cart);
                startActivity(intentToDetailActivity);
            }
        });

        //Khi click vào biểu tượng giỏ hàng -> chuyển đến trang giỏ hàng
        imgBtnCartInHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToCartActivity = new Intent(HomePageActivity.this, CartActivity.class);
                intentToCartActivity.putExtra("Cart", cart);
                startActivity(intentToCartActivity);
            }
        });

        //Xử lý sự kiện tìm kiếm
        svProductInHomePage.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                tab1_action();
                filter(newText);
                return false;
            }
        });
    }

    //Tab lịch sử đơn hàng
    public void tab2_action(){
        currentTab = 2;
        lvBillHistory = findViewById(R.id.lvBillHistory);
        myListBill = new ArrayList<>();

        //Lấy danh sách các đơn hàng của người dùng từ CSDL
        Cursor c = database.query("Bill", null, "UserName = ?", new String[]{userName}, null, null, "Bill.ID DESC");
        c.moveToFirst();
        while (c.isAfterLast() == false){
            Bill bill = new Bill(c.getString(0), c.getString(1), c.getString(2), c.getDouble(3));
            myListBill.add(bill);
            c.moveToNext();
        }
        c.close();

        //Load lên giao diện
        if(myListBill.isEmpty()){
            Toast.makeText(this, "Bạn chưa có đơn hàng nào", Toast.LENGTH_SHORT).show();
        }
        else{
            myArrayAdapterInHistory = new ArrayAdapterHistory(HomePageActivity.this, R.layout.layout_item_bill_history, myListBill);
            lvBillHistory.setAdapter(myArrayAdapterInHistory);
        }
    }

    //Đếm số sản phẩm trong giỏ hàng
    public void setNumberProductInCart(){
        Cursor c_QuantityInCart = database.query("Detail_ProductCart D inner join Product P on D.ProductID = P.ID",
                new String[]{"count(*)"}, "CartID = ? AND P.Deleted = ?", new String[]{cart.getIdCart(), "0"}, null, null, null);
        int quantityInCart = 0;
        if (c_QuantityInCart.moveToFirst()) {
            quantityInCart = c_QuantityInCart.getInt(0);
        }
        c_QuantityInCart.close();
        cart.setProductQuantity(quantityInCart);
        txtNumberProductOfCart.setText(String.valueOf(cart.getProductQuantity()));
    }

    //Tìm kiếm
    private ArrayList<Product> filter(String searchText) {
        ArrayList<Product> filteredProducts = new ArrayList<>();
        ArrayList<Product> tmpProducts = new ArrayList<>();
        tmpProducts.addAll(myListProductToCategory);
        if (!searchText.isEmpty()) {
            // Lặp qua danh sách sản phẩm đã lọc theo danh mục và thêm các sản phẩm phù hợp vào danh sách lọc
            // Xóa dấu và đưa về chữ in thường
            String searchTextWithoutAccents = removeAccents(searchText.toLowerCase());
            for (Product item : myListProductToCategory) {
                String productName = removeAccents(item.getName().toLowerCase());
                if (productName.contains(searchTextWithoutAccents)) {
                    filteredProducts.add(item);
                }
            }
        } else {
            // Nếu searchText rỗng, hiển thị toàn bộ danh sách sản phẩm mặc định hoặc theo danh mục
            filteredProducts.addAll(myListProductToCategory);
        }

        // Cập nhật lại danh sách hiển thị với danh sách đã lọc
        myAdapterInHomePage = new ArrayAdapterInHomePage(HomePageActivity.this, R.layout.layout_product_homepage, filteredProducts);
        gvProductInHomePage.setAdapter(myAdapterInHomePage);
        return filteredProducts;
    }


    //Xóa dấu thanh
    public static String removeAccents(String input) {
        String regex = "\\p{InCombiningDiacriticalMarks}+";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return temp.replaceAll(regex, "");
    }

    //Khi onBack: tab2->tab1, tab1->nhấn lần 2->login
    @Override
    public void onBackPressed() {
        if (!svProductInHomePage.getQuery().toString().isEmpty()) {
            // Clear the text in searchView
            svProductInHomePage.setQuery("", false);
            svProductInHomePage.clearFocus();
            return; // Do not proceed further
        }
        if(spnCategoryInHomePage.getSelectedItemPosition() != 0){
            spnCategoryInHomePage.setSelection(0);
            return;
        }

        if (currentTab == 2) { // Nếu đang ở tab 2
            // Chuyển tab về tab 1
            tabHost.setCurrentTab(0);
            currentTab = 1; // Cập nhật lại trạng thái hiện tại của tab
            tab1_action();
        } else {
            if(backPressTime + 3000 > System.currentTimeMillis()){
                super.onBackPressed();
                return;
            }
            else{
                Toast.makeText(this, "Nhấn lần nữa để thoát", Toast.LENGTH_SHORT).show();
            }
            backPressTime = System.currentTimeMillis();
        }
    }
}