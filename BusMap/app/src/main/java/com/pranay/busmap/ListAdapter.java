package com.pranay.busmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends
        RecyclerView.Adapter<ListAdapter.MyViewHolder> {

    private List<String> busidlist;
    private List<Boolean> busChecklist;
    private OnItemClickListener listener;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_id;
        public ImageView iv_right;

        public MyViewHolder(View view) {
            super(view);
            iv_right = (ImageView) view.findViewById(R.id.iv_right);
            tv_id = (TextView) view.findViewById(R.id.tv_id);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public ListAdapter(Context mcContext, List<String> busidlist, List<Boolean> busChecklist,OnItemClickListener listener) {
        this.context = mcContext;
        this.busidlist = busidlist;
        this.busChecklist = busChecklist;
        this.listener = listener;
    }

    public void setListerner(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (busChecklist.get(position) == false) {

            holder.iv_right.setVisibility(View.INVISIBLE);
        } else {
            holder.iv_right.setVisibility(View.VISIBLE);
        }
        holder.tv_id.setText("Bus id :- " + busidlist.get(position));
    }

    @Override
    public int getItemCount() {
        return busidlist.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new MyViewHolder(v);
    }
}
