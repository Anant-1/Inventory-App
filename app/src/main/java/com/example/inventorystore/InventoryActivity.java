package com.example.inventorystore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.inventorystore.data.ProductContract.ProductEntry;
import com.example.inventorystore.data.ProductDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    ProductCursorAdapter mProductCursorAdapter;
    public static final int PRODUCT_LOADER = 1;
    private Cursor tmpCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);

            }
        });


        ListView prodListView = findViewById(R.id.list_view);
        View emptyView = (RelativeLayout)findViewById(R.id.empty_view);
        prodListView.setEmptyView(emptyView);
        mProductCursorAdapter = new ProductCursorAdapter(this, null);
        prodListView.setAdapter(mProductCursorAdapter);

        prodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }

    private void insertProduct() {
        ProductDbHelper dbHelper = new ProductDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_IMAGE, "");
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Maggi");
        values.put(ProductEntry.COLUMN_PRODUCT_COM_NAME, "Nestle");
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Ahmad Ansari");
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, "ahmad123@gmail.com");
        values.put(ProductEntry.COLUMN_PRICE, 100);
        values.put(ProductEntry.COLUMN_STOCK, 9);
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        if(newUri != null)
            Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show();
        else Toast.makeText(this, "Error in saving product", Toast.LENGTH_SHORT).show();

    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete all products?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllProducts();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllProducts() {
        getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @NotNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable @org.jetbrains.annotations.Nullable Bundle args) {
        String[] columns = {
                ProductEntry._ID,
                ProductEntry.COLUMN_IMAGE,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_COM_NAME,
                ProductEntry.COLUMN_STOCK
        };
        return new CursorLoader(this, ProductEntry.CONTENT_URI, columns, null, null, null);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_delete_all_entries);
        if(tmpCursor.getCount() == 0) {
            menuItem.setVisible(false);
        }else menuItem.setVisible(true);
        return true;
    }

    @Override
    public void onLoadFinished(@NonNull @NotNull Loader<Cursor> loader, Cursor data) {
        tmpCursor = data;
        mProductCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull @NotNull Loader<Cursor> loader) {
        mProductCursorAdapter.swapCursor(null);
    }
}