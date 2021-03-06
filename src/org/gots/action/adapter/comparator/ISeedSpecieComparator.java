package org.gots.action.adapter.comparator;

import java.util.Comparator;

import org.gots.seed.BaseSeedInterface;
import org.gots.seed.SeedUtil;

import android.content.Context;
import android.util.Log;

public class ISeedSpecieComparator implements Comparator<BaseSeedInterface> {

    private static final String TAG = "Compare";

    private Context mcontext;

    public ISeedSpecieComparator(Context context) {
        this.mcontext = context;
    }

    @Override
    public int compare(BaseSeedInterface obj1, BaseSeedInterface obj2) {
        int result = 0;
        if (obj1.getSpecie() != null && obj2.getSpecie() != null) {
            Log.i(TAG,
                    SeedUtil.translateSpecie(mcontext, obj1) + " | "
                            + SeedUtil.translateSpecie(mcontext, obj2));
            result = SeedUtil.translateSpecie(mcontext, obj1).compareTo(
                    SeedUtil.translateSpecie(mcontext, obj2));
        }
        return result;
    }
}