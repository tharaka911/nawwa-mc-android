package lk.macna.nawwa_mc.ui.my_orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MyOrdersViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MyOrdersViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is my orders fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}