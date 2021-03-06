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
package org.gots.weather.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.gots.R;
import org.gots.weather.WeatherConditionInterface;
import org.gots.weather.provider.MoonCalculation;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherView extends LinearLayout {
	Context mContext;
	WeatherConditionInterface mWeather = null;
	LayoutInflater inflater;
	Layout layout;
	public static final int IMAGE = 0;
	public static final int TEXT = 1;
	public static final int FULL = 2;
	private int mType = FULL;
	protected boolean bolToggle = true;

	// private AnimationDrawable animation;

	public WeatherView(Context context) {
		super(context);
		this.mContext = context;
		initView();

	}

	public WeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initView();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// animation.start();

		super.onWindowFocusChanged(hasWindowFocus);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

	}

	@Override
	protected void onDetachedFromWindow() {
		// animation.stop();
		super.onDetachedFromWindow();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

	private void initView() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.weather_widget, this);
		setupView();

	}

	private void setupView() {
		LinearLayout boxTemp = (LinearLayout) findViewById(R.id.idWeatherTemp);
		LinearLayout weatherImageContainer = (LinearLayout) findViewById(R.id.idWeatherImageContainer);

		ImageView weatherWidget = (ImageView) findViewById(R.id.idWeatherImage);
		ImageView moonWidget = (ImageView) findViewById(R.id.idMoonImage);
		TextView weatherDay = (TextView) findViewById(R.id.idWeatherDay);
		TextView tempMax = (TextView) findViewById(R.id.idWeatherMax);
		TextView tempMin = (TextView) findViewById(R.id.idWeatherMin);

		if (mWeather == null) {
			return;
		}
		switch (mType) {
		case TEXT:
			weatherWidget.setVisibility(View.GONE);

			if (mWeather.getIconURL() == null) {
				boxTemp.setVisibility(View.GONE);
				weatherDay.setVisibility(View.GONE);
				
			}
			weatherImageContainer.setVisibility(View.GONE);

			break;
		case IMAGE:
			boxTemp.setVisibility(View.GONE);
			weatherDay.setVisibility(View.GONE);

			break;

		default:
			if (mWeather.getIconURL() == null) {
				boxTemp.setVisibility(View.INVISIBLE);

			}
			break;
		}
		try {
			Calendar t = new GregorianCalendar();
			t.setTime(mWeather.getDate());

			tempMin.setText("" + mWeather.getTempCelciusMin());

			tempMax.setText("" + mWeather.getTempCelciusMax());

			SimpleDateFormat sdf = new SimpleDateFormat("E dd/MM", Locale.getDefault());
			if (mWeather.getDate() != null)
				weatherDay.setText("" + sdf.format(mWeather.getDate()));

			weatherWidget.setImageResource(getWeatherResource(mWeather));

			MoonCalculation moon = new MoonCalculation();
			moonWidget.setImageDrawable(getResources().getDrawable(
					getMoonResource(moon.phaseName(moon.moonPhase(t.get(Calendar.YEAR), t.get(Calendar.MONTH),
							t.get(Calendar.DAY_OF_MONTH))))));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// final Handler mHandler = new Handler();
		// final Runnable mUpdateUITimerTask = new Runnable() {
		// public void run() {
		//
		// // Simulating blinking for capture button
		// if(bolToggle) {
		// bolToggle = false;
		// //
		// captureButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_record_blink));
		// weatherWidget.setImageResource(getWeatherResource(mWeather));
		//
		// } else {
		// bolToggle = true;
		// // weatherWidget.setImageResource(getWeatherResource(mWeather));
		// weatherWidget.setImageResource(getMoonResource(moon.phaseName(moon.moonPhase(t.get(Calendar.YEAR),
		// t.get(Calendar.MONTH), t.get(Calendar.DAY_OF_MONTH)))));
		//
		//
		// }
		//
		// mHandler.postDelayed(this, 1000);
		// }
		// };
		// mUpdateUITimerTask.run();
		// invalidate();

		// animation = new AnimationDrawable();
		// animation.addFrame(getResources().getDrawable(getWeatherResource(mWeather)),
		// 4000);
		// animation.addFrame(
		// getResources().getDrawable(
		// getMoonResource(moon.phaseName(moon.moonPhase(t.get(Calendar.YEAR),
		// t.get(Calendar.MONTH),
		// t.get(Calendar.DAY_OF_MONTH))))), 2000);
		// // animation.addFrame(getResources().getDrawable(R.drawable.image3),
		// // 300);
		// animation.setOneShot(false);
		//
		// // ImageView imageAnim = (ImageView) findViewById(R.id.img);
		// weatherWidget.setImageDrawable(animation);
		// weatherWidget.setBackgroundDrawable(animation);
		// start the animation!
		// invalidate();
		// animation.start();

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
	}

	public void setWeather(WeatherConditionInterface weather) {
		this.mWeather = weather;
//		invalidate();
		setupView();
	}

	public static int getWeatherResource(WeatherConditionInterface weatherCondition) {
		if (weatherCondition.getIconURL() == null)
			return R.drawable.weather_nonet;

		if (weatherCondition.getIconURL().contains("rain"))
			return R.drawable.weather_rain;
		else if (weatherCondition.getIconURL().contains("mostly_sunny"))
			return R.drawable.weather_mostlysunny;
		else if (weatherCondition.getIconURL().contains("cloud") || weatherCondition.getIconURL().contains("mist"))
			return R.drawable.weather_cloud;
		else if (weatherCondition.getIconURL().contains("snow"))
			return R.drawable.weather_snow;
		else if (weatherCondition.getIconURL().contains("sunny"))
			return R.drawable.weather_mostlysunny;
		else if (weatherCondition.getIconURL().contains("storm") || weatherCondition.getIconURL().contains("thunder"))
			return R.drawable.weather_thunder;
		return R.drawable.weather_sun;
	}

	public static int getMoonResource(String moon) {
		if ("First quarter".equals(moon))
			return R.drawable.moon_first_quarter;
		else if ("New".equals(moon))
			return R.drawable.moon_new_moon;
		else if ("Waxing crescent".equals(moon))
			return R.drawable.moon_waxing_crescent;
		else if ("Waxing gibbous".equals(moon))
			return R.drawable.moon_waxing_gibbous;
		else if ("Waning gibbous".equals(moon))
			return R.drawable.moon_waning_gibbous;
		else if ("Third quarter".equals(moon))
			return R.drawable.moon_third_quarter;
		else if ("Waning crescent".equals(moon))
			return R.drawable.moon_waning_crescent;
		else if ("Full".equals(moon))
			return R.drawable.moon_full_moon;

		return R.drawable.weather_nonet;
	}

	public void setType(int mType) {
		this.mType = mType;
	}
}
