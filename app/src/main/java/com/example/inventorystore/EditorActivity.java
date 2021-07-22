package com.example.inventorystore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventorystore.data.ProductContract;
import com.github.drjacky.imagepicker.ImagePicker;

import org.jetbrains.annotations.NotNull;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView stockView;
    private ImageView mImageView;
    private EditText mProdNameEditText;
    private EditText mProdComNameEditText;
    private EditText mSuppNameEditText;
    private EditText mSuppEmailEditText;
    private EditText mPriceEditText;
    private Uri currentProductUri;
    private Uri mImageUri = null;
    private int stock = 1;
    
    private boolean mProductHasChnaged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChnaged= true;
            return false;
        }
    };

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        
        Intent intent = getIntent();
        currentProductUri = intent.getData();
        if(currentProductUri != null) {
            getSupportLoaderManager().initLoader(InventoryActivity.PRODUCT_LOADER, null, EditorActivity.this);
            setTitle("Edit Product");
        }else {
            setTitle("Add a Product");
            invalidateOptionsMenu();
        }

        Button plusButton = findViewById(R.id.plus_btn);
        Button negButton = findViewById(R.id.neg_btn);
        stockView = findViewById(R.id.zero_stock);

        mImageView = findViewById(R.id.add_image);
        mProdNameEditText = findViewById(R.id.product_name);
        mProdComNameEditText = findViewById(R.id.product_com_name);
        mSuppNameEditText = findViewById(R.id.supp_name);
        mSuppEmailEditText = findViewById(R.id.supp_email);
        mPriceEditText = findViewById(R.id.edit_price);

        mProdNameEditText.setOnTouchListener(mTouchListener);
        mProdComNameEditText.setOnTouchListener(mTouchListener);
        mSuppNameEditText.setOnTouchListener(mTouchListener);
        mSuppEmailEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        stockView.setOnTouchListener(mTouchListener);

        
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.Companion.with(EditorActivity.this)
                        .galleryOnly()
                        .crop()
                        .compress(2048)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });
        
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stock >= 10) {
                    Toast.makeText(EditorActivity.this, "Max stock reached", Toast.LENGTH_SHORT).show();
                    System.out.println("stock" + stock);
                    return;
                }
                stock++;
                stockView.setText(stock+"");
            }
        });
        negButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stock <= 1) {
                    Toast.makeText(EditorActivity.this, "Min stock should be 1", Toast.LENGTH_SHORT).show();
                    System.out.println("stock" + stock);
                    return;
                }
                stock--;
                stockView.setText(stock+"");
            }
        });
    }

    private int saveProduct() {
        String imageString;
        if(mImageUri == null) {
            imageString = "";
        }else {
            imageString = mImageUri.toString();
        }
        String prodNameString = mProdNameEditText.getText().toString().trim();
        String prodComNameString = mProdComNameEditText.getText().toString().trim();
        String suppNameString = mSuppNameEditText.getText().toString().trim();
        String suppEmailString = mSuppEmailEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();


        if(prodNameString.isEmpty()) {
            Toast.makeText(this, "Please enter product name", Toast.LENGTH_SHORT).show();
            return -1;
        }

        if(currentProductUri == null && TextUtils.isEmpty(prodNameString) && TextUtils.isEmpty(prodComNameString)
            && TextUtils.isEmpty(suppNameString) && TextUtils.isEmpty(suppEmailString) && TextUtils.isEmpty(priceString)) return 0;


        int price = 0;
        price = Integer.parseInt(priceString);

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_IMAGE, imageString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, prodNameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME, prodComNameString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, suppNameString);
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, suppEmailString);
        values.put(ProductContract.ProductEntry.COLUMN_PRICE, price);
        values.put(ProductContract.ProductEntry.COLUMN_STOCK, stock);

        if(currentProductUri == null) {
            Uri newProductUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if(newProductUri != null) {
                Toast.makeText(this, "Product Saved Successfully", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Error in saving product", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected != 0) {
                Toast.makeText(this, "Product Updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error in updating product", Toast.LENGTH_SHORT).show();
            }
        }
        return 0;
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this Product Permanently?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePet();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        if(currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if(rowsDeleted != 0) {
                Toast.makeText(this, "Product Deleted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Error in deleting product", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(currentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImageUri = data.getData();

        if(mImageUri != null) {
            mImageView.setImageURI(mImageUri);
        }
    }

    @Override
    public void onBackPressed() {
        if(!mProductHasChnaged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                int code = saveProduct();
                if(code != -1) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home: // for up back button
                if(!mProductHasChnaged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @NotNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable @org.jetbrains.annotations.Nullable Bundle args) {
        String columns[] = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_STOCK
        };
        return new CursorLoader(this, currentProductUri, columns, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull @NotNull Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE);
            int prodNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int prodComNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_COM_NAME);
            int suppNameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            int suppEmailColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRICE);
            int stockColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_STOCK);

            String prodImage = cursor.getString(imageColumnIndex);
            String prodName = cursor.getString(prodNameColumnIndex);
            String prodComName = cursor.getString(prodComNameColumnIndex);
            String suppName = cursor.getString(suppNameColumnIndex);
            String suppEmail = cursor.getString(suppEmailColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int stock = cursor.getInt(stockColumnIndex);

            if(prodImage.equals("")) {
                mImageView.setImageResource(R.drawable.no_image);
            }else {
                mImageUri = Uri.parse(prodImage);
                mImageView.setImageURI(Uri.parse(prodImage));
            }
            mProdNameEditText.setText(prodName);
            mProdComNameEditText.setText(prodComName);
            mSuppNameEditText.setText(suppName);
            mSuppEmailEditText.setText(suppEmail);
            mPriceEditText.setText(String.valueOf(price));
            stockView.setText(String.valueOf(stock));

        }
    }

    @Override
    public void onLoaderReset(@NonNull @NotNull Loader<Cursor> loader) {

    }
}