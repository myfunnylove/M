package locidnet.com.marvarid.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import locidnet.com.marvarid.model.Quote

/**
 * Created by myfunnylove on 01.10.17.
 */
class PublishViewmodel : ViewModel() {


    var quote = MutableLiveData<Quote>()





}