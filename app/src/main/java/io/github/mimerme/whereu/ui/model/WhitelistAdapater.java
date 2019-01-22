package io.github.mimerme.whereu.ui.model;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import io.github.mimerme.whereu.R;
import io.github.mimerme.whereu.ui.MainActivity;
import io.github.mimerme.whereu.utility.Utility;

public class WhitelistAdapater extends RecyclerView.Adapter{

    private ArrayList<String[]> mData;
    private HashSet<String> mNumbers = new HashSet<>();
    private String[] mLastDeleted;
    private int mLastDeletedPosition;

    public WhitelistAdapater(){
        this.mData = loadData();
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

        holder.name.setText(mData.get(position)[0]);
        holder.number.setText(mData.get(position)[1]);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void deleteItem(int position){
        Log.i("WhitelistAdapater", "" + position);

        mLastDeletedPosition = position;
        mLastDeleted = mData.get(position);

        mData.remove(position);
        mNumbers.remove(mLastDeleted[1]);

        saveData();
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size());
    }

    public void undo(){
        mData.add(mLastDeletedPosition, mLastDeleted);
        mNumbers.add(mLastDeleted[1]);
        saveData();
        notifyItemInserted(mLastDeletedPosition);
    }

    public void add(String newLine[]){
        mData.add(newLine);
        saveData();
        notifyItemInserted(mData.size() - 1);
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;
        LinearLayout viewFg;

        EntryViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.whitelist_name);
            number = itemView.findViewById(R.id.whitelist_number);
            viewFg = itemView.findViewById(R.id.view_fg);
        }

        public LinearLayout getFg(){
            return viewFg;
        }

        public String getName(){
            return (String) name.getText();
        }

        public String getNumber(){
            return (String) number.getText();
        }
    }

    public ArrayList<String[]> generateDummyData(){
        ArrayList<String[]> test = new ArrayList<>();
        test.add(new String[]{"Name 0", "911"});
        test.add(new String[]{"Name 1", "911"});
        test.add(new String[]{"Name 2", "911"});
        test.add(new String[]{"Name 3", "911"});
        test.add(new String[]{"Name 4", "911"});
        test.add(new String[]{"Name 5", "911"});
        return test;
    }

    public ArrayList<String[]> loadData(){
        ArrayList<String[]> data = new ArrayList<>();

        for(String line : MainActivity.WHITELIST_STORAGE){
            String[] splits = line.split(":");
            String formatedNumber = Utility.formatNumber(splits[1]);
            data.add(new String[]{
                    splits[0],
                    formatedNumber
            });
            mNumbers.add(formatedNumber);
        }

        return data;
    }

    public void saveData(){
        StringBuffer buffer = new StringBuffer();

        for(String[] data : mData){
            buffer.append(data[0] + ":" + data[1] + "\n");
        }

        MainActivity.WHITELIST_STORAGE.writeTo(buffer.toString());
    }

    //Checks to see if the number is already on the whitelist. True for yes, false for no.
    public boolean checkDuplicate(String phoneNumber){
        return mNumbers.contains(phoneNumber);
    }

    //Allows swipe to delete
    public static class WhitelistTouchCallback extends ItemTouchHelper.SimpleCallback {

        private WhitelistAdapater mAdapter;
        private Context mContext;

        public WhitelistTouchCallback(int dragDirs, int swipeDirs, WhitelistAdapater adapter, Context context) {
            super(dragDirs, swipeDirs);
            this.mAdapter = adapter;
            this.mContext = context;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (viewHolder != null) {
                final View foregroundView = ((EntryViewHolder) viewHolder).viewFg;

                getDefaultUIUtil().onSelected(foregroundView);
            }
        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((EntryViewHolder) viewHolder).viewFg;
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final View foregroundView = ((EntryViewHolder) viewHolder).viewFg;
            getDefaultUIUtil().clearView(foregroundView);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            final View foregroundView = ((EntryViewHolder) viewHolder).viewFg;

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            //Toast.makeText(mContext, "Deleted \'" + ((EntryViewHolder) viewHolder).getName() + "\' from the whitelist", Toast.LENGTH_LONG).show();
            mAdapter.deleteItem(viewHolder.getAdapterPosition());
            View view = ((android.app.Activity) mContext).findViewById(R.id.drawer_layout);
            Snackbar snackbar = Snackbar.make(view, "Deleted \'" + ((EntryViewHolder) viewHolder).getName() + "\' from the whitelist",
                    Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", v -> mAdapter.undo());
            snackbar.show();
        }
    }
}
