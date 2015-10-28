package com.skyfin.baidumapdome.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.skyfin.baidumapdome.R;
import com.skyfin.baidumapdome.bean.LocationBean;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> implements Filterable, View.OnClickListener {

    // 回调监听的接口 实现
    OnRecyclerViewItemClickListener myItemClickListener = null;
    //Filter 筛选之后的数据集合
    private List<LocationBean> orig;
    //默认的数据集合
    List<LocationBean> mdata;
    //主页面的Context
    Context mContext;
    //LayoutInflater
    LayoutInflater mLayoutInflater;

    public ItemAdapter(Context mContext, List<LocationBean> mdata) {
        this.mContext = mContext;
        this.mdata = mdata;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item,
                parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.titleimage.setImageResource(R.drawable.icon_geo);
        holder.title.setText(mdata.get(position).title);
        holder.itemView.setTag(mdata.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final List<LocationBean> results = new ArrayList<>();
                if (orig == null)
                    orig = mdata;
                if (constraint != null) {
                    if (orig != null & orig.size() > 0) {
                        for (final LocationBean g : orig) {
                            if (g.title.toLowerCase().contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mdata = (ArrayList<LocationBean>) results.values;
                notifyDataSetChanged();
            }

        };
    }

    @Override
    public void onClick(View v) {
        if (myItemClickListener != null) {
            myItemClickListener.onItemClick(v, (String) v.getTag());
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView titleimage;
        TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            titleimage = (ImageView) itemView.findViewById(R.id.title_image);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, String data);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.myItemClickListener = listener;
    }
}
