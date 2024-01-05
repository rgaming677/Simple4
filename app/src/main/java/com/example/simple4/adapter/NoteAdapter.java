package com.example.simple4.adapter;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simple4.database.entity.NoteEntity;
import com.example.simple4.R;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> implements Filterable {

    private List<NoteEntity> noteList;
    private List<NoteEntity> noteListFull;

    public NoteAdapter() {
        this.noteList = new ArrayList<>();
        this.noteListFull = new ArrayList<>();
    }

    public void setNotes(List<NoteEntity> notes) {
        this.noteList = notes;
        this.noteListFull = new ArrayList<>(notes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity currentNote = noteList.get(position);

        holder.titleTextView.setText(currentNote.getTitle());
        holder.subtitleTextView.setText(currentNote.getSubtitle());
        holder.datetimeTextView.setText(currentNote.getDatetime());

        //mengambil text dari database. Jika kosong diangap tidak ada.
        if (TextUtils.isEmpty(currentNote.getSubtitle())) {
            holder.subtitleTextView.setVisibility(View.GONE);
        } else {
            holder.subtitleTextView.setVisibility(View.VISIBLE);
        }

        //mengambil file gambar dari database. jika kosong diangap tidak ada.
        if (currentNote.getImagePath() != null) {
            holder.imageView.setImageBitmap(BitmapFactory.decodeFile(currentNote.getImagePath()));
            holder.imageView.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setVisibility(View.GONE);
        }

        //Fitur click pada menu recyclerview
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(currentNote);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(currentNote);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView subtitleTextView;
        public TextView datetimeTextView;
        public ImageView imageView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvCardTitle);
            subtitleTextView = itemView.findViewById(R.id.tvCardSubtitle);
            datetimeTextView = itemView.findViewById(R.id.tvCardDatetime);
            imageView = itemView.findViewById(R.id.ivCardImage);

            //Mengatur ukuran dan banyaknya text yang akan ditampilkan
            titleTextView.setMaxLines(2);
            subtitleTextView.setTextSize(12);
            subtitleTextView.setMaxLines(1);
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            subtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    @Override
    public Filter getFilter() {
        return noteFilter;
    }

    //Fitur untuk melakukan filter pada list recylerview
    private Filter noteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<NoteEntity> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(noteListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (NoteEntity note : noteListFull) {
                    if (note.getTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(note);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            noteList.clear();
            noteList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


    //Fitur click pada menu recyclerview
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onItemClick(NoteEntity note);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(NoteEntity note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

}
