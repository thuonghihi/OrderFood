package com.example.project_btl_android;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class CategoryManagementActivity extends AppCompatActivity {
    ListView lvCategory;
    TextView txtBackToManagementHomepage, txtVAddCategory;
    ArrayAdapterCategoryManagement myAdapterCategoryManagement;
    ArrayList<Category> myListCategory;
    ArrayList<String> myListCategoryName;
    SQLiteDatabase database = null;
    boolean checkCategoryName = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtVAddCategory = findViewById(R.id.txtVAddCategory);
        txtBackToManagementHomepage = findViewById(R.id.txtBackToManagementHomepage);
        lvCategory = findViewById(R.id.lvCategory);
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
        myListCategory = new ArrayList<>();
        myListCategoryName = new ArrayList<>();
        Cursor cursor = database.query("Category", null, null, null, null, null, "ID DESC");
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false){
            Category category = new Category(cursor.getInt(0), cursor.getString(1));
            myListCategoryName.add(category.getName());
            myListCategory.add(category);
            cursor.moveToNext();
        }
        cursor.close();
        myAdapterCategoryManagement = new ArrayAdapterCategoryManagement(CategoryManagementActivity.this, R.layout.layout_category, myListCategory);
        myAdapterCategoryManagement.setDatabase(database);
        lvCategory.setAdapter(myAdapterCategoryManagement);

        txtBackToManagementHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Show Dialog nhập tên thể lọại khi thêm
        txtVAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });
    }

    //Dialog nhập tên
    public void showInputDialog() {
        // Tạo một AlertDialog.Builder
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CategoryManagementActivity.this);

        // Thiết lập tiêu đề của Dialog
        dialogBuilder.setTitle("Nhập tên thể loại mới");

        // Tạo một EditText để người dùng nhập giá trị
        final EditText input = new EditText(CategoryManagementActivity.this);

        // Đặt EditText vào Dialog
        dialogBuilder.setView(input);

        // Thiết lập nút Positive Button (OK)
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkCategoryName = true;
                // Lấy giá trị từ EditText
                String categoryName = input.getText().toString().trim();
                if (categoryName.isEmpty()) {
                    Toast.makeText(CategoryManagementActivity.this, "Không để trống tên thể loại", Toast.LENGTH_SHORT).show();
                }
                //Nếu trùng tên -> xác nhận trước khi thêm
                else if (myListCategoryName.contains(categoryName)) {
                    submitInsert(categoryName);
                } else {
                    insertCategory(categoryName);
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Không có hành động cụ thể nếu người dùng chọn Cancel
                dialog.cancel();
            }
        });

        // Thiết lập nút Negative Button (Cancel)

        // Tạo và hiển thị Dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    //Xác nhận khi trùng tên
    public void submitInsert(String categoryName){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CategoryManagementActivity.this);

        // Thiết lập tiêu đề của Dialog
        dialogBuilder.setTitle("Tên thể loại đã tồn tại, bạn chắc chắn muốn thêm?");
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                insertCategory(categoryName);
            }
        });
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    //Thêm
    public void insertCategory(String categoryName){
        Category category = new Category(categoryName);
        ContentValues values = new ContentValues();
        values.put("Name", categoryName);
        long CategoryID = database.insert("Category", null, values);
        category.setId((int) CategoryID);
        Toast.makeText(CategoryManagementActivity.this, "Thêm thể loại mới thành công", Toast.LENGTH_SHORT).show();
        myListCategory.add(category);
        myAdapterCategoryManagement.notifyDataSetChanged();
    }
}