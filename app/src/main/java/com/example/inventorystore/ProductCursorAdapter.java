package com.example.inventorystore;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.example.inventorystore.data.ProductContract;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = view.findViewById(R.id.image_item);
        TextView prodNameView = view.findViewById(R.id.product_name_item);
        TextView prodComNameView = view.findViewById(R.id.product_com_name_item);
        TextView stockView = view.findViewById(R.id.prod_stock);

        int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE);
        int prodNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int prodComNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME);
        int prodStockColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_STOCK);

        String prodImage = cursor.getString(imageColumnIndex);
        String prodName = cursor.getString(prodNameColumnIndex);
        String prodComName = cursor.getString(prodComNameColumnIndex);
        int stock = cursor.getInt(prodStockColumnIndex);

        if(prodImage.equals("")) {
            imageView.setImageResource(R.drawable.no_image);

        }else {
            Uri imageUri = Uri.parse(prodImage);
            imageView.setImageURI(imageUri);
        }

        prodNameView.setText(prodName);

        if(prodComName.isEmpty()) {
            prodComNameView.setText("Unknown");
        }else {
            prodComNameView.setText(prodComName);
        }
        stockView.setText(String.valueOf(stock));
    }
}
