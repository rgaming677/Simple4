package com.example.simple4.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.simple4.adapter.NoteAdapter;
import com.example.simple4.database.entity.NoteEntity;
import com.example.simple4.livedata.NoteViewModel;
import com.example.simple4.R;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView ivAddNoteMain;
    private RecyclerView rcHome;
    private NoteViewModel noteViewModel;

    private NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Memanggil fitur
        etSearch = findViewById(R.id.etSearch);
        ivAddNoteMain = findViewById(R.id.ivAddNoteMain);
        rcHome = findViewById(R.id.rcHome);

        //Memanggil ViewModel
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        //Memanggil RecyclerView
        noteAdapter = new NoteAdapter();

        //model recyclerview
        rcHome.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        rcHome.setAdapter(noteAdapter);
        noteViewModel.getAllNotes().observe(this, new Observer<List<NoteEntity>>() {
            @Override
            public void onChanged(List<NoteEntity> notes) {
                noteAdapter.setNotes(notes);
            }
        });

        setupSearch();

        //button : masuk ke activity CreateActivity
        ivAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateActivity.class));
            }
        });

        //RecyclerView content click
        // perintah klik singkat untuk masuk menu edit
        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NoteEntity note) {
                startEditActivity(note);
            }
        });
        //perintah klik tahan untuk melakukan perintah hapus
        noteAdapter.setOnItemLongClickListener(new NoteAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(NoteEntity note) {
                showDeleteConfirmationDialog(note);
            }
        });
    }

    //Search Menu
    //Fitur filter. menjalankan perintah untuk menfilter text pada recyclerview
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                noteAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //Click menu : memindahkan dari card content ke EditActivity
    private void startEditActivity(NoteEntity note) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    //Click menu long : menampilkan pop-up peringatan hapus konten
    private void showDeleteConfirmationDialog(final NoteEntity note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus");
        builder.setMessage("kamu ingin menghapusnya?");
        builder.setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteFromDatabase(note);
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
    //Menjalankan perintah hapus data dari database
    private void deleteNoteFromDatabase(NoteEntity note) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            noteViewModel.delete(note);
        });
    }
}
