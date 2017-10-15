package com.appsbyrishavagarwal.translate;

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

import java.util.Calendar;
import java.util.List;

import static com.appsbyrishavagarwal.translate.Translate.showToast;

public class HistoryAdapter extends ArrayAdapter<Translation> {

    public HistoryAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Translation> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_history, parent, false);
        }

        final TextView tvFromText = convertView.findViewById(R.id.tv_from_text);
        final TextView tvToText = convertView.findViewById(R.id.tv_to_text);
        final TextView tvFromLang = convertView.findViewById(R.id.tv_from_lang);
        final TextView tvToLang = convertView.findViewById(R.id.tv_to_lang);
        TextView tvDate = convertView.findViewById(R.id.tv_date);
        ImageButton ibDelete = convertView.findViewById(R.id.ib_delete);
        ImageButton ibBookmark = convertView.findViewById(R.id.ib_bookmark);

        Translation translation = getItem(position);

        tvFromText.setText(translation.getFrom_text());
        tvToText.setText(translation.getTo_text());
        tvFromLang.setText(translation.getFrom_lang());
        tvToLang.setText(translation.getTo_lang());
        tvDate.setText(translation.getDate());

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryDatabaseHandler databaseHandler = new HistoryDatabaseHandler(getContext());
                databaseHandler.deleteTranslation(HistoryFragment.historyAdapter.getItem(position));
                HistoryFragment.historyAdapter.clear();
                HistoryFragment.historyAdapter.addAll(databaseHandler.getAllTranslations());
                showToast("Translation deleted!", getContext());
            }
        });

        ibBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BookmarkDatabaseHandler bookmarkDatabaseHandler = new BookmarkDatabaseHandler(getContext());
                Translation translation = new Translation(tvFromLang.getText().toString(), tvFromText.getText().toString(), tvToLang.getText().toString(), tvToText.getText().toString(), Calendar.getInstance().getTime().toString());

                List<Translation> translationList = bookmarkDatabaseHandler.getAllBookmarks();

                for (int i = 0; i < translationList.size(); ++i) {
                    if (translation.getFrom_lang().equals(translationList.get(i).getFrom_lang()) && translation.getTo_lang().equals(translationList.get(i).getTo_lang()) && translation.getFrom_text().equals(translationList.get(i).getFrom_text()) && translation.getTo_text().equals(translationList.get(i).getTo_text())) {
                        bookmarkDatabaseHandler.deleteBookmark(translationList.get(i));
                    }
                }

                bookmarkDatabaseHandler.addBookmark(translation);
                BookmarkFragment.bookmarkAdapter.clear();
                BookmarkFragment.bookmarkAdapter.addAll(bookmarkDatabaseHandler.getAllBookmarks());
                showToast(getContext().getString(R.string.bookmark_added), getContext());
            }
        });

        return convertView;
    }
}