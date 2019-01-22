package io.github.mimerme.whereu.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.mimerme.whereu.R;
import io.github.mimerme.whereu.ui.model.WhitelistAdapater;

/*
*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WhitelistFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WhitelistFragment#newInstance} factory method to
 * create an instance of this fragment.
*/
public class WhitelistFragment extends Fragment {
    private RecyclerView rv;
    private WhitelistAdapater mAdapter;

    public WhitelistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_whitelist, container, false);

        //Setup the recycler view
        rv = view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                RecyclerView.VERTICAL);
        rv.addItemDecoration(dividerItemDecoration);

        //Don't reinitilize if we alreayd have an adapter
        if(mAdapter == null)
            mAdapter = new WhitelistAdapater();

        rv.setAdapter(mAdapter);

        new ItemTouchHelper(new WhitelistAdapater.WhitelistTouchCallback(0, ItemTouchHelper.LEFT, mAdapter, getContext())).attachToRecyclerView(rv);


        return view;
    }

    public WhitelistAdapater getWhitelistAdapater(){
        return mAdapter;
    }
}
