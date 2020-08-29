package com.example.aleb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    List<Room> rooms;
    private RecyclerViewClickInterface recyclerViewClickInterface;

    MyAdapter(List<Room> rooms, RecyclerViewClickInterface recyclerViewClickInterface) {
        this.rooms = rooms;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_room, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Room room = rooms.get(position);

        holder.nameView.setText(room.getName());
        holder.playerNumView.setText(room.getNumOfPlayersS());
        holder.typeView.setText(room.getType());
        holder.goalView.setText(String.valueOf(room.getGoal()));

        if (room.isHasPassword())
            holder.lockView.setImageResource(R.drawable.ic_locked);
        else
            holder.lockView.setImageResource(R.drawable.ic_unlocked);

        if (room.isShowScore())
            holder.scoreView.setImageAlpha(255);
        else
            holder.scoreView.setImageAlpha(0);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameView, playerNumView, typeView, goalView;
        ImageView lockView, scoreView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.room_l_name);
            playerNumView = itemView.findViewById(R.id.room_l_playerNum);
            typeView = itemView.findViewById(R.id.room_l_type);
            goalView = itemView.findViewById(R.id.room_l_goal);
            lockView = itemView.findViewById(R.id.room_i_lock);
            scoreView = itemView.findViewById(R.id.room_i_score);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewClickInterface.onItemClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    recyclerViewClickInterface.onLongItemClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }
}
