/*******************************************************************************
 * Copyright (c) 2012 sfleury.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     sfleury - initial API and implementation
 ******************************************************************************/
package org.gots.action.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

import org.gots.R;
import org.gots.action.BaseActionInterface;
import org.gots.action.SeedActionInterface;
import org.gots.action.bean.PhotoAction;
import org.gots.action.sql.ActionSeedDBHelper;
import org.gots.action.util.ActionState;
import org.gots.action.view.ActionWidget;
import org.gots.seed.GrowingSeedInterface;
import org.gots.seed.providers.local.sql.GrowingSeedDBHelper;
import org.gots.seed.view.SeedWidget;
import org.gots.weather.WeatherManager;
import org.gots.weather.view.WeatherView;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAllActionAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<BaseActionInterface> actions = new ArrayList<BaseActionInterface>();

    // private ArrayList<GrowingSeedInterface> seeds = new
    // ArrayList<GrowingSeedInterface>();
    // private ArrayList<WeatherConditionInterface> weathers = new
    // ArrayList<WeatherConditionInterface>();

    private int current_status = STATUS_DONE;

    public static final int STATUS_TODO = 0;

    public static final int STATUS_DONE = 1;

    private WeatherManager manager;

    public static final int THUMBNAIL_HEIGHT = 48;

    public static final int THUMBNAIL_WIDTH = 66;

    private static final String TAG = "ListAllActionAdapter";

    public ListAllActionAdapter(Context context, ArrayList<GrowingSeedInterface> allSeeds, int status) {
        this.mContext = context;
        current_status = status;
        ActionSeedDBHelper helper = new ActionSeedDBHelper(context);

        for (Iterator<GrowingSeedInterface> iterator = allSeeds.iterator(); iterator.hasNext();) {
            GrowingSeedInterface seed = iterator.next();
            ArrayList<BaseActionInterface> seedActions;

            if (current_status == STATUS_TODO) {
                seedActions = helper.getActionsToDoBySeed(seed);

            } else {
                seedActions = helper.getActionsDoneBySeed(seed);
            }

            actions.addAll(seedActions);
        }
        if (current_status == STATUS_TODO)
            Collections.sort(actions, new IActionAscendantComparator());
        else
            Collections.sort(actions, new IActionDescendantComparator());
        manager = new WeatherManager(mContext);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

    }

    @Override
    public int getCount() {
        return actions.size();
    }

    @Override
    public BaseActionInterface getItem(int position) {
        return actions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View ll = convertView;

        // if (convertView == null) {
        // ll = new LinearLayout(mContext);
        if (convertView == null)
            ll = LayoutInflater.from(mContext).inflate(R.layout.list_action, parent, false);

        final BaseActionInterface currentAction = getItem(position);

        GrowingSeedDBHelper helper = new GrowingSeedDBHelper(mContext);
        final GrowingSeedInterface seed = helper.getSeedById(currentAction.getGrowingSeedId());

        if (seed != null && BaseActionInterface.class.isInstance(currentAction)) {
            ActionWidget actionWidget = (ActionWidget) ll.findViewById(R.id.idActionView);

            SeedWidget seedView = (SeedWidget) ll.findViewById(R.id.idSeedView);
            seedView.setSeed(seed);

            TextView textviewActionStatus = (TextView) ll.findViewById(R.id.IdSeedActionStatus);
            TextView textviewActionDate = (TextView) ll.findViewById(R.id.IdSeedActionDate);

            SimpleDateFormat dateFormat = new SimpleDateFormat(" dd/MM/yyyy", Locale.FRANCE);

            if (current_status == STATUS_TODO) {
                textviewActionStatus.setText(mContext.getResources().getString(R.string.seed_action_todo));

                Calendar rightNow = Calendar.getInstance();
                rightNow.setTime(seed.getDateSowing());
                rightNow.add(Calendar.DAY_OF_YEAR, currentAction.getDuration());
                textviewActionDate.setText(dateFormat.format(rightNow.getTime()));

                actionWidget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((SeedActionInterface) currentAction).execute(seed);
                        actions.remove(position);
                        // seeds.remove(position);
                        notifyDataSetChanged();
                    }
                });

                WeatherView weatherView = (WeatherView) ll.findViewById(R.id.idWeatherView);
                weatherView.setVisibility(View.GONE);

            } else {
                textviewActionStatus.setText(mContext.getResources().getString(R.string.seed_action_done));

                Calendar rightNow = Calendar.getInstance();
                rightNow.setTime(currentAction.getDateActionDone());
                // rightNow.add(Calendar.DAY_OF_YEAR,
                // currentAction.getDuration());
                textviewActionDate.setText(dateFormat.format(currentAction.getDateActionDone()));

                WeatherView weatherView = (WeatherView) ll.findViewById(R.id.idWeatherView);
                weatherView.setWeather(manager.getCondition(rightNow.getTime()));

                currentAction.setState(ActionState.NORMAL);

                if (PhotoAction.class.isInstance(currentAction) && currentAction.getData() != null) {
                    final File imgFile = new File(currentAction.getData().toString());

                    try {
                        Bitmap imageBitmap = getThumbnail(imgFile);
                        // Bitmap imageBitmap = getThumbnail(
                        // mContext.getContentResolver(),
                        // imgFile.getAbsolutePath());
                        ImageView seedImage = (ImageView) ll.findViewById(R.id.imageviewPhoto);
                        int padding = (THUMBNAIL_WIDTH - imageBitmap.getWidth()) / 2;
                        seedImage.setPadding(padding, 0, padding, 0);
                        seedImage.setImageBitmap(imageBitmap);

                        seedImage.setVisibility(View.VISIBLE);
                        weatherView.setVisibility(View.GONE);

                        seedImage.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse("file://" + imgFile.getAbsolutePath()), "image/*");
                                mContext.startActivity(intent);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(getClass().getName(), "imgFile.getPath()=" + imgFile.getPath() + " - " + e.getMessage());
                        // ll.setVisibility(View.GONE);
                    }

                }
            }
            actionWidget.setAction(currentAction);
            // ll.invalidate();

        }
        // }
        return ll;
    }

    private Bitmap getThumbnail(File imgFile) throws Exception {
        Log.d("getThumbnail", imgFile.getPath());
        Bitmap imageBitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            imageBitmap = BitmapFactory.decodeFile(imgFile.getPath(), options);
            // imageBitmap = BitmapFactory.decodeByteArray(mImageData, 0,
            // mImageData.length);
            Float width = Float.valueOf(imageBitmap.getWidth());
            Float height = Float.valueOf(imageBitmap.getHeight());
            Float ratio = width / height;
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (THUMBNAIL_HEIGHT * ratio), THUMBNAIL_HEIGHT,
                    false);
        } catch (Exception e) {
            throw new Exception("Image file cannot be decoded :" + imgFile.getAbsolutePath());
        }
        return imageBitmap;
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {
        Uri selectedImageUri = Uri.fromFile(new File(path));
        // Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        // new String[] { MediaStore.MediaColumns._ID },
        // MediaStore.MediaColumns.DATA + "=?", new String[] { path },
        // null);
        Cursor ca = cr.query(selectedImageUri, null, MediaStore.Images.Media.DATA + " like ? ",
                new String[] { selectedImageUri.getPath() }, null);

        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            Log.d(TAG, ca.getString(ca.getColumnIndex(MediaStore.MediaColumns.DATA)));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
        }

        ca.close();
        return null;

    }

    class IActionAscendantComparator implements Comparator<BaseActionInterface> {
        @Override
        public int compare(BaseActionInterface obj1, BaseActionInterface obj2) {
            int result = 0;
            if (obj1.getDuration() >= 0 && obj2.getDuration() >= 0) {
                result = obj1.getDuration() < obj2.getDuration() ? -1 : 0;
            }

            return result;
        }
    }

    class IActionDescendantComparator implements Comparator<BaseActionInterface> {
        @Override
        public int compare(BaseActionInterface obj1, BaseActionInterface obj2) {
            int result = 0;
            if (obj1.getDateActionDone() != null && obj2.getDateActionDone() != null) {
                result = obj1.getDateActionDone().getTime() > obj2.getDateActionDone().getTime() ? -1 : 0;
            }

            return result;
        }
    }

}
