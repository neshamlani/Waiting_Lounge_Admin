package com.nesh.waitingloungeadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class card extends RecyclerView.Adapter<card.cardViewHolder>{
    List<String> name=new ArrayList<>();
    List<String> timedisp=new ArrayList<>();
    List<String> tok=new ArrayList<>();
    List<String> srno=new ArrayList<>();
    List<String> num=new ArrayList<>();

    public card(List<String> name, List<String> timedisp, List<String> tok, List<String> srno, List<String> num) {
        this.name = name;
        this.timedisp = timedisp;
        this.tok = tok;
        this.srno = srno;
        this.num = num;
    }

    @NonNull
    @Override
    public cardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.carddisplay,parent,false);
        cardViewHolder cvh=new cardViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(@NonNull cardViewHolder holder, int position) {
        holder.listName.setText(name.get(position));
        holder.listTime.setText(timedisp.get(position));
        holder.listsrno.setText(srno.get(position));
        holder.listNum.setText(num.get(position));
        holder.listTok.setText(tok.get(position));
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class cardViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView listName,listTime,listNum,listsrno,listTok;
        public cardViewHolder(@NonNull View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cardDisplay);
            listName=(TextView)itemView.findViewById(R.id.listName);
            listTime=(TextView)itemView.findViewById(R.id.listTime);
            listNum=(TextView)itemView.findViewById(R.id.listnum);
            listsrno=(TextView)itemView.findViewById(R.id.sr);
            listTok=(TextView)itemView.findViewById(R.id.listtoken);
        }
    }
}
