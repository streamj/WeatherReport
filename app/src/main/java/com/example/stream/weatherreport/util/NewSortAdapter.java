package com.example.stream.weatherreport.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.stream.weatherreport.R;
import com.example.stream.weatherreport.model.SortModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by StReaM on 3/3/2017.
 */

public class NewSortAdapter extends RecyclerView.Adapter<NewSortAdapter.ChooserViewHolder>
        implements SectionIndexer {
    private Context mContext;
    private List<SortModel> mList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onChooserItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public NewSortAdapter(Context context, List<SortModel> list) {
        mContext = context;
        mList = list;
    }

    /**
     * 当数据发生变化时,调用此方法来更新ListView
     * @param list
     */
    public void updateListView(List<SortModel> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public ChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.index_list_view_item, parent, false);
        final ChooserViewHolder viewHolder = new ChooserViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ChooserViewHolder holder, int position) {
        final SortModel mContent = mList.get(position);
        Log.d("DEBUG", "CONTENT is " + mContent.getName());
        //根据position获取分类的首字母的char ascii值
        int section = getSectionForPosition(position);
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            holder.tvLetter.setVisibility(View.VISIBLE);
            holder.tvLetter.setText(mContent.getSortLetters());
        } else {
            holder.tvLetter.setVisibility(View.GONE);
        }
        holder.tvTitle.setTag(position);
        holder.tvTitle.setText(mList.get(position).getName());
        holder.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onChooserItemClick((Integer) view.getTag());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public Object[] getSections() {
        return null;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mList.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int position) {
        return mList.get(position).getSortLetters().charAt(0);
    }

    static class ChooserViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView tvTitle;

        @BindView(R.id.catalog)
        TextView tvLetter;

        public ChooserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
