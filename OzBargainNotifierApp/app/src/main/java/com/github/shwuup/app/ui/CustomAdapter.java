package com.github.shwuup.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import com.github.shwuup.R;
import com.github.shwuup.app.keyword.KeywordFileManager;
import com.github.shwuup.app.models.Event;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.List;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
  private SortedList<String> keywords;
  private PublishSubject<Event> deleteClickSubject = PublishSubject.create();

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;
    private final View view;

    public ViewHolder(View v) {
      super(v);
      view = v;
      textView = (TextView) v.findViewById(R.id.keyword);
    }

    public View getView() {
      return view;
    }

    public TextView getTextView() {
      return textView;
    }
  }

  public void add(String keyword) {
    if (keywords.indexOf(keyword) == -1) {
      keywords.add(keyword);
    }
  }

  public CustomAdapter(List<String> keywordData) {
    keywords =
        new SortedList<String>(
            String.class,
            new SortedListAdapterCallback<String>(this) {
              @Override
              public int compare(String o1, String o2) {
                return o1.compareTo(o2);
              }

              @Override
              public boolean areContentsTheSame(String oldItem, String newItem) {
                return oldItem.equals(newItem);
              }

              @Override
              public boolean areItemsTheSame(String item1, String item2) {
                return item1.equals(item2);
              }
            });
    for (String keyword : keywordData) {
      keywords.add(keyword);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
    View view =
        LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.list_item_view, viewGroup, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, final int position) {
    KeywordFileManager keywordFileManager =
        new KeywordFileManager(viewHolder.getView().getContext());
    String keyword = keywords.get(position);
    viewHolder.getTextView().setText(keyword);
    Button delete = viewHolder.getView().findViewById(R.id.deleteButton);
    RxView.clicks(delete).map(x -> new Event("delete", keyword)).subscribe(deleteClickSubject);
  }

  @Override
  public int getItemCount() {
    return keywords.size();
  }

  public PublishSubject<Event> getSubject() {
    return deleteClickSubject;
  }

  public SortedList<String> getKeywords() {
    return keywords;
  }
}
