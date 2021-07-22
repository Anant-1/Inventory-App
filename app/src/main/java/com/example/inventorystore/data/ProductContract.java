package com.example.inventorystore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {
    private ProductContract(){}

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

    public static class ProductEntry implements BaseColumns{
//        Content uri = content://com.example.android.inventory/products
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_PRODUCT_NAME = "prod_name";
        public static final String COLUMN_PRODUCT_COM_NAME = "prod_com_name";
        public static final String COLUMN_SUPPLIER_NAME = "supp_name";
        public static final String COLUMN_SUPPLIER_EMAIL = "supp_email";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_STOCK = "stock";

    }
}
