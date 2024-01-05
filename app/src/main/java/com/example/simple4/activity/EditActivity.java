package com.example.simple4.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.simple4.database.entity.NoteEntity;
import com.example.simple4.livedata.NoteViewModel;
import com.example.simple4.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class EditActivity extends AppCompatActivity {
    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private ActivityResultLauncher<Intent> pickEditImageLauncher;

    private EditText etEditTitle, etEditSubtitle, etEditText;
    private ImageView ivEditImage, ivDone, ivEditAddImage;
    private TextView tvEditDateTime;
    private NoteViewModel noteViewModel;
    private String imagePath;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        //Memanggil funsi
        etEditTitle = findViewById(R.id.etEditTitle);
        etEditSubtitle = findViewById(R.id.etEditSubtitle);
        etEditText = findViewById(R.id.etEditText);
        ivEditImage = findViewById(R.id.ivEditImage);
        tvEditDateTime = findViewById(R.id.tvEditDateTime);
        ivDone = findViewById(R.id.ivDone);
        ivEditAddImage = findViewById(R.id.ivEditAddImage);

        //Memanggil ViewModel
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        //terusan dari pickImage -> handleImagePickResult
        pickEditImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImagePickResult(result.getData().getData());
                    }
                });

        //Fungsi button / OnClick
        //Button kembali.
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //button menambah, mengedit, dan menghapus gambar.
        ivEditAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        ivEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        ivEditImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteImageDialog();
                return true;
            }
        });
        //button untuk menyimpan.
        ivDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        //Menampilkan data dari room database
        if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
            int noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, 1);
            if (noteId != 1) {
                noteViewModel.getNoteById(noteId).observe(this, new Observer<NoteEntity>() {
                    @Override
                    public void onChanged(NoteEntity note) {
                        if (note != null) {
                            etEditTitle.setText(note.getTitle());
                            etEditSubtitle.setText(note.getSubtitle());
                            etEditText.setText(note.getText());
                            tvEditDateTime.setText(note.getDatetime());

                            if (note.getImagePath() != null){
                                ivEditImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                                ivEditImage.setVisibility(View.VISIBLE);
                            } else {
                                ivEditImage.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
        }
    }

    //Menyimpan data
    private void saveNote() {
        String title = etEditTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        String subtitle = etEditSubtitle.getText().toString().trim();
        String noteText = etEditText.getText().toString().trim();
        String dateTime = getDateTime();
        String imagePath = getImageView(ivEditImage);

        NoteEntity updatedNote = new NoteEntity(title, subtitle, dateTime, imagePath, noteText);

        //meperbaharui data yang berada pada database
        if (getIntent().hasExtra(EXTRA_NOTE_ID)) {
            int noteId = getIntent().getIntExtra(EXTRA_NOTE_ID, 1);
            if (noteId != 1) {
                updatedNote.setId(noteId);
                noteViewModel.update(updatedNote);
                Toast.makeText(this, "Data tersimpan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Menyimpan gambar pada Bitmap. Agar gambar sebelumnya tidak hilang dari database.
    private String getImageView(ImageView imageView) {
        String imagePath = null;
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            imagePath = simpanBitmap(bitmap);
        }
        return imagePath;
    }
    private String simpanBitmap(Bitmap bitmap) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File file = new File(storageDir, imageFileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //mengambil file dari galery
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            pickEditImageLauncher.launch(intent);
        }
    }

    //Menampilkan gambar uri pada Bitmap
    private void handleImagePickResult(Uri selectImageUri) {
        if (selectImageUri != null ) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivEditImage.setImageBitmap(bitmap);
                ivEditImage.setVisibility(View.VISIBLE);

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

    //Mengambil data waktu
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
        ivEditImage.setImageDrawable(null);
        Toast.makeText(EditActivity.this, "Gambar dihapus", Toast.LENGTH_SHORT).show();
    }

}

