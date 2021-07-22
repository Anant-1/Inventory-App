package com.example.inventorystore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProductProvider extends ContentProvider {
    private final String LOG_TAG = ProductProvider.class.getSimpleName();

    ProductDbHelper mDbHelper;

    private static final int PRODUCTS = 100;
    private static final int PRODUCTS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Attach uri to Provider
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                break;
            case PRODUCTS_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {
        String prodName = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if(prodName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String prodComName = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME);
        if(prodComName == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer price = contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_PRICE);
        if(price != null  && price < 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, contentValues);
        if(rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsChanged;
        switch (match) {
            case PRODUCTS:
                rowsChanged = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCTS_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsChanged = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(rowsChanged != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsChanged;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCTS_ID:
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
        }

        return 0;
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if(contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String prodName = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if(prodName == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if(contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME)) {
            String prodComName = contentValues.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME);
            if(prodComName == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if(contentValues.containsKey(ProductContract.ProductEntry.COLUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(ProductContract.ProductEntry.COLUMN_PRICE);
            if(price != null  && price < 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }

        if(contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(ProductContract.ProductEntry.TABLE_NAME, contentValues, selection,selectionArgs);

        if(rowsUpdated != 0 ){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
