package locidnet.com.marvarid.resources.adapterAnim;

/**
 * Created by myfunnylove on 10.10.17.
 */
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;

public interface AnimateViewHolder {

    void preAnimateAddImpl(final RecyclerView.ViewHolder holder);

    void preAnimateRemoveImpl(final RecyclerView.ViewHolder holder);

    void animateAddImpl(final RecyclerView.ViewHolder holder, ViewPropertyAnimatorListener listener);

    void animateRemoveImpl(final RecyclerView.ViewHolder holder,
                           ViewPropertyAnimatorListener listener);
}
