package io.github.mimerme.whereu.ui.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.mimerme.whereu.R;

public class WhitelistAdapater extends RecyclerView.Adapter{

    private ArrayList<String[]> data;
    public WhitelistAdapater(ArrayList<String[]> data){
        this.data = data;
    }

    public WhitelistAdapater(){
        this.data = generateDummyData();
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.whitelist_item, parent, false);
        EntryViewHolder holder = new EntryViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        EntryViewHolder holder = (EntryViewHolder) viewHolder;

        holder.name.setText(data.get(position)[0]);
        holder.number.setText(data.get(position)[1]);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;

        EntryViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.whitelist_name);
            number = itemView.findViewById(R.id.whitelist_number);
        }
    }

    public ArrayList<String[]> generateDummyData(){
        ArrayList<String[]> test = new ArrayList<>();
        test.add(new String[]{"Name", "911"});
        test.add(new String[]{"Name", "911"});
        test.add(new String[]{"Name", "911"});
        test.add(new String[]{"Name", "911"});
        test.add(new String[]{"Name", "911"});
        test.add(new String[]{"Name", "911"});
        return test;
    }

    //Allows swipe to delete
    public static class WhitelistTouchCallback extends ItemTouchHelper.SimpleCallback {
        public WhitelistTouchCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    }
}
