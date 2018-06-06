package me.rishavagarwal.translate;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class BookmarkAdapter extends ArrayAdapter<Translation> {

    public BookmarkAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Translation> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_history, parent, false);
        }

        TextView tvFromText = convertView.findViewById(R.id.tv_from_text);
        TextView tvToText = convertView.findViewById(R.id.tv_to_text);
        TextView tvFromLang = convertView.findViewById(R.id.tv_from_lang);
        TextView tvToLang = convertView.findViewById(R.id.tv_to_lang);
        TextView tvDate = convertView.findViewById(R.id.tv_date);
        ImageButton ibDelete = convertView.findViewById(R.id.ib_delete);
        ImageButton ibBookmark = convertView.findViewById(R.id.ib_bookmark);

        Translation translation = getItem(position);

        tvFromText.setText(translation.getFrom_text());
        tvToText.setText(translation.getTo_text());
        tvFromLang.setText(translation.getFrom_lang());
        tvToLang.setText(translation.getTo_lang());
        tvDate.setText(translation.getDate());

        ibBookmark.setVisibility(View.INVISIBLE);

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookmarkDatabaseHandler databaseHandler = new BookmarkDatabaseHandler(getContext());
                databaseHandler.deleteBookmark(BookmarkFragment.bookmarkAdapter.getItem(position));
                BookmarkFragment.bookmarkAdapter.clear();
                BookmarkFragment.bookmarkAdapter.addAll(databaseHandler.getAllBookmarks());
                Translate.showToast(getContext().getString(R.string.bookmak_deleted), getContext());
            }
        });

        return convertView;
    }
}