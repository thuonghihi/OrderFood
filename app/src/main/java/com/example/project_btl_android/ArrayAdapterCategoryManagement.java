package com.example.project_btl_android;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class ArrayAdapterCategoryManagement extends ArrayAdapter<Category> {
    Activity context;
    int idLayout;
    ArrayList<Category> myList;
    SQLiteDatabase database = null;
    CategoryManagementActivity mActivity;
    Boolean check = true;

    public ArrayAdapterCategoryManagement(Activity context, int idLayout, ArrayList<Category> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (CategoryManagementActivity) context;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
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
        Category category = myList.get(position);
        //Ánh xạ id
        EditText txtCategoryName = convertView.findViewById(R.id.txtCategoryName);
        txtCategoryName.setText(category.getName());
        ImageView imgVDeleteCategory = convertView.findViewById(R.id.imgVDeleteCategory);
        String categoryNameBeforeUpdate = txtCategoryName.getText().toString();

        //Sửa trực tiếp trên Edittext
        txtCategoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newName = s.toString();
                ContentValues values = new ContentValues();
                if (!newName.equals(category.getName()) && !newName.isEmpty()) {
                    values.put("Name", newName);
                }
                else{
                    Toast.makeText(context, "Tên thể loại không được để trống", Toast.LENGTH_SHORT).show();
                    values.put("Name", categoryNameBeforeUpdate);
                }
                database.update("Category", values, "ID = ?", new String[]{String.valueOf(category.getId())});
                category.setName(newName); // Cập nhật dữ liệu trong ArrayList
            }
        });

        //Khi xóa 1 item -> xác nhận
        imgVDeleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteConfirm(category);
            }
        });
        return convertView;
    }


    //Xác nhận trước khi xóa
    public void DeleteConfirm(Category category){
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setTitle("XÁC NHẬN XÓA");
        dialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

                Cursor c = database.rawQuery("SELECT C.ID, C.Name " +
                                "FROM Category C LEFT JOIN Product P ON C.ID = P.CategoryID " +
                                "WHERE P.CategoryID IS NULL AND C.ID = ?",
                        new String[]{String.valueOf(category.getId())});
                if(c.moveToFirst()) {
                    database.delete("Category", "ID = ?", new String[]{String.valueOf(category.getId())});
                    Toast.makeText(mActivity, "Xóa thể loại thành công", Toast.LENGTH_SHORT).show();
                    mActivity.myListCategory.remove(category);
                    mActivity.myListCategoryName.remove(category.getName());
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(mActivity, "Có sản phẩm thuộc thể loại này, không thể xóa!", Toast.LENGTH_SHORT).show();
                }
                c.close();
            }
        });
        dialog.create().show();
    }
}
