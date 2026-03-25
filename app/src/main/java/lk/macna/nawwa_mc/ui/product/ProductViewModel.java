package lk.macna.nawwa_mc.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProductViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public ProductViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Product fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
