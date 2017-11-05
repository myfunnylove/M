package locidnet.com.marvarid.adapter.optimize;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import locidnet.com.marvarid.R;

/**
 * Created by myfunnylove on 04.11.2017.
 */

public class OuterRecyclerAdapter extends RecyclerView.Adapter<OuterRecyclerAdapter.Holder> {



    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.res_feed_block_image,null, false);


        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class Holder extends RecyclerView.ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }
}
