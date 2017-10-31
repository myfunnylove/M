package locidnet.com.marvarid.resources.searchFilter;

/**
 * Created by Michaelan on 9/27/2016.
 */
public interface IFilter<E> {

    AbstractFilter<E> getFilteredResults();
}
