package com.example.project_btl_android;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class ArrayAdapterInCart extends ArrayAdapter<Product> {
    Activity context;
    int idLayout;
    ArrayList<Product> myList;
    Cart cart;
    double total;
    CartActivity mActivity;
    SQLiteDatabase database=null;
    ArrayList<Boolean> checkboxStates;
    private boolean isPositiveButtonClicked = false;
    private double tax = 0.0;

    //Khi tạo 1 adapter -> tạo 1 mảng chứa trạng thái của các checkbox (sản phẩm)
    public ArrayAdapterInCart(Activity context, int idLayout, ArrayList<Product> myList) {
        super(context, idLayout, myList);
        this.context = context;
        this.idLayout = idLayout;
        this.myList = myList;
        mActivity = (CartActivity) context;
        checkboxStates = new ArrayList<>();
        for (int i = 0; i < myList.size(); i++) {
            checkboxStates.add(false); // Khởi tạo trạng thái mặc định là false cho mỗi checkbox
        }
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Tạo đế
        LayoutInflater myFlater = context.getLayoutInflater();
        //Đặt layout lên flater
        convertView = myFlater.inflate(idLayout, null);
        //Lấy phần tử tại ví trí position
        Product myProduct = myList.get(position);
        //Ánh xạ id
        TextView txtProductToMoneyInCart = convertView.findViewById(R.id.txtProductToMoneyInCart);
        CheckBox ckProductInCart = convertView.findViewById(R.id.ckProductInCart);
        //Đưa thoong tin sản phẩm lên convertView
        ImageView img = convertView.findViewById(R.id.imgProductInCart);
        byte[] imageData = myProduct.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        img.setImageBitmap(bitmap);
        TextView txtProductNameInCart = convertView.findViewById(R.id.txtProductNameInCart);
        txtProductNameInCart.setText(myProduct.getName());
        TextView txtProductQuantityInCart = convertView.findViewById(R.id.txtPoductQuantityInCart);
        txtProductQuantityInCart.setText(myProduct.getQuantityInCart()+"");
        int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
        TextView txtProductPriceInCart = convertView.findViewById(R.id.txtProductToMoneyInCart);
        txtProductPriceInCart.setText(myProduct.toMoney(n) + "");
        ImageView imgVAddProductToCart = convertView.findViewById(R.id.imgVAddProductToCart);
        ImageView imgVDeleteProductFromCart = convertView.findViewById(R.id.imgVDeleteProductFromCart);


        if(Integer.parseInt(txtProductQuantityInCart.getText().toString()) > myProduct.checkQuantityVisible(database)){
            txtProductNameInCart.setEnabled(false);
            txtProductQuantityInCart.setEnabled(false);
            txtProductPriceInCart.setEnabled(false);
            ckProductInCart.setEnabled(false);
            imgVAddProductToCart.setEnabled(false);
            imgVDeleteProductFromCart.setEnabled(false);
            TextView txtTB = new TextView(context);
            txtTB.setPadding(30, 0, 0, 0);
            txtTB.setTextColor(Color.parseColor("#DB5860"));
            txtTB.setText("Không đủ số lượng " + myProduct.getName());
            ((ViewGroup)convertView).addView(txtTB);
        }
        //Khi chọn 1 sản phẩm trong giỏ hàng
        //Gán giá trị checked cho checkbox theo giá trị trong mảng check ở vị trí position
        ckProductInCart.setChecked(checkboxStates.get(position));
        ckProductInCart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tax = 0.0;
                checkEnable(myProduct, position);
                //Khi click vào checkbox, đảo ngược giá trị checked của checkbox product
                myProduct.setIscheck(!myProduct.isIscheck());
                // Thay đổi giá trị checkbox trong mảng theo isCheck của sự kiện
                checkboxStates.set(position, isChecked);
                //Nếu được chọn -> thêm sản phẩm vào selectList và ngược lại
                if(checkboxStates.get(position) == true){
                    mActivity.addSelectProduct(myProduct);
                }
                else {
                    mActivity.removeSelectProduct(myProduct);
                }
                //Hiển thị tổng tiền của các sản phẩm được chọn trong giỏ hàng
                toStringTotal(cart);
            }
        });

        ImageView imgVTrashFromCart = convertView.findViewById(R.id.imgVTrashFromCart);
        imgVTrashFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteConfirm(myProduct, position);
//                //Cập nhật giao diện
                notifyDataSetChanged();
            }
        });

        //Tăng sản phẩm từ giỏ hàng
        imgVAddProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Thêm sản phẩm
                int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
                txtProductQuantityInCart.setText(n+1+"");
                toStringTotal(cart);
                txtProductToMoneyInCart.setText(myProduct.toMoney(myProduct.getQuantityInCart())+"");
            }
        });

        //Gỉam
        imgVDeleteProductFromCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Nếu số lượng chỉ còn 1 và tiếp tục giảm -> xóa
                if (myProduct.getQuantityInCart() == 1) {
                    DeleteConfirm(myProduct, position);
                } else {
                    int n = Integer.parseInt(txtProductQuantityInCart.getText().toString());
                    txtProductQuantityInCart.setText(n-1+"");
                    txtProductToMoneyInCart.setText(myProduct.toMoney(myProduct.getQuantityInCart())+"");
                    toStringTotal(cart);
                }
                // Thông báo cho ArrayAdapterInCart về sự thay đổi và cập nhật lại giao diện
                notifyDataSetChanged();
            }
        });

        txtProductQuantityInCart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myProduct.updateProductQuantityToCart(cart, txtProductQuantityInCart, database, mActivity);
            }
        });

        //Khi click vào tên của sản phẩm trong giỏ hàng, chuyển đến trang chi tiết của sản phẩm đó
        txtProductNameInCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, DetailProductActivity.class);
                intent.putExtra("product", myProduct);
                intent.putExtra("Cart", mActivity.cart);
                mActivity.startActivity(intent);
            }
        });
        return convertView;
    }


    //cập nhật tổng tiền các sản phẩm được chọn trong giỏ hàng
    public void toStringTotal(Cart cart){
        cart.updateTotalInCart(mActivity.mySelectProductList);
        mActivity.txtTotalMoneyProductSelect.setText(cart.getTotal()+"");
    }

    //Xác nhận xóa
    public void DeleteConfirm(Product myProduct, int position){
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
                //Xóa sản phẩm -> xóa trạng thái checkbox của sản phẩm
                checkboxStates.remove(position);
                myProduct.setIscheck(false);
                if(mActivity.mySelectProductList.contains(myProduct)){
                    mActivity.removeSelectProduct(myProduct);
                }
                mActivity.myListProductInCart.remove(myProduct);
                myProduct.deleteProductFromCart(cart, database);
                toStringTotal(cart);
                notifyDataSetChanged();
            }
        });
        dialog.create().show();
    }

    public void checkEnable(Product myProduct, int position){
        if(myProduct.quantityInCart(cart, database) > myProduct.checkQuantityVisible(database)){
            checkboxStates.set(position, false);
            mActivity.mySelectProductList.remove(myProduct);
        }
    }
}
