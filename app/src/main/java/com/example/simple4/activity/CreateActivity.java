package com.example.simple4.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.simple4.database.entity.NoteEntity;
import com.example.simple4.livedata.NoteViewModel;
import com.example.simple4.R;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateActivity extends AppCompatActivity {



    private EditText etNoteTitle, etNoteSubtitle, etNote;
    private ImageView ivDone, ivAddImage, ivNote;
    private TextView tvDateTime;
    private NoteViewModel noteViewModel;

    private String imagePath;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private static final int REQUEST_CODE_STORAGE_PERMISION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        //Memanggil fungsi
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteSubtitle = findViewById(R.id.etNoteSubtitle);
        etNote = findViewById(R.id.etNote);
        ivDone = findViewById(R.id.ivDone);
        ivAddImage = findViewById(R.id.ivAddImage);
        ivNote = findViewById(R.id.ivNote);
        tvDateTime = findViewById(R.id.tvDateTime);

        //Menampilkan waktu dari getDateTime
        tvDateTime.setText(getDateTime());

        //Memanggil ViewModel
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        //Terusan dari pictImage -> handleImagePictResult
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImagePickResult(result.getData().getData());
                    }
                });

        //Fitur button / OnClick
        //button kembali
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //button menambah, mengedit, dan menghapus gambar.
        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Meminta izin untuk mengakses penyimpanan
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(
                            CreateActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISION
                    );
                pickImage();
            }
        });
        ivNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        ivNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteImageDialog();
                return true;
            }
        });
        //button simpan
        ivDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

    }

    //menyimpan data
    private void saveNote() {
        String title = etNoteTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String subtitle = etNoteSubtitle.getText().toString().trim();
        String noteText = etNote.getText().toString().trim();
        String dateTime = getDateTime();

        NoteEntity note = new NoteEntity(title, subtitle, dateTime, imagePath, noteText);

        noteViewModel.insert(note);

        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();

        finish();
    }

    //mengambil file dari galery
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            pickImageLauncher.launch(intent);
        }
    }

    //menampilkan gambar uri pada Bitmap
    private void handleImagePickResult(Uri selectImageUri) {
        if (selectImageUri != null ) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivNote.setImageBitmap(bitmap);
                ivNote.setVisibility(View.VISIBLE);

                imagePath = getPathFromUri(selectImageUri);

            } catch (Exception exception) {
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    //mengambil data waktu
    private String getDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    //Fitur untuk menghapus gambar pada Bitmap
    private void showDeleteImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus");
        builder.setMessage("kamu ingin menghapusnya?");
        builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage();
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    private void deleteImage() {
        ivNote.setImageDrawable(null);
        Toast.makeText(CreateActivity.this, "Gambar dihapus", Toast.LENGTH_SHORT).show();
    }
}
