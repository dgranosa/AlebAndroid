package com.example.aleb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class MyScoreAdapter extends RecyclerView.Adapter<MyScoreAdapter.MyScoreViewHolder> {

    List <String> score;

    MyScoreAdapter(List <String> score) {
        this.score = score;
    }

    MyScoreAdapter(String[] score) {
        this(Arrays.asList(score));
    }

    @NonNull
    @Override
    public MyScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_score, parent, false);

        return new MyScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyScoreViewHolder holder, int position) {
        String[] data = score.get(position).split(",");

        holder.team0View.setText(data[0]);
        holder.team1View.setText(data[1]);
        holder.trumpView.setImageResource(Game.adutId[Game.getIdPosition(Game.adut, data[2])]);

        if (data[3].equals("true")) {
            if (Integer.parseInt(data[0]) > Integer.parseInt(data[1]))
                holder.team1View.setText("-");
            else
                holder.team0View.setText("-");
        }
    }

    @Override
    public int getItemCount() {
        return score.size();
    }

    public class MyScoreViewHolder extends RecyclerView.ViewHolder {

        TextView team0View, team1View;
        ImageView trumpView;

        public MyScoreViewHolder(@NonNull View itemView) {
            super(itemView);

            team0View = itemView.findViewById(R.id.score_l_team0);
            team1View = itemView.findViewById(R.id.score_l_team1);
            trumpView = itemView.findViewById(R.id.score_i_trump);
        }
    }
}
