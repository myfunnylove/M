package locidnet.com.marvarid.resources.searchFilter;

import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michaelan on 9/27/2016.
 */
public abstract class AbstractFilter<E> implements Filterable {

    public ArrayList<E> abcList,abcOriginalList;
    public AbstractFilter(ArrayList<E> list) {
        this.abcList = list;
        this.abcOriginalList = list;

    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                abcList = (ArrayList<E>) results.values;

                refresh(abcList);

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<E> filteredResults = null;
                if (constraint.length() == 0) {
                    filteredResults = abcOriginalList;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;


                return results;
            }
        };
    }

    public abstract ArrayList<E> getFilteredResults(String constraint);

    public abstract void refresh(ArrayList<E>abcList);
}
