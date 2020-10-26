package com.runtimeoverflow.SchulNetzClient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.runtimeoverflow.SchulNetzClient.Data.Lesson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimetableView extends View {
	private ArrayList<Lesson.ScheduleLesson> lessons = new ArrayList<>();
	private Paint fill;
	private Paint stroke;
	private Paint text;
	
	public TimetableView(Context context) {
		super(context);
		
		init();
	}
	
	public TimetableView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}
	
	public TimetableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init();
	}
	
	public TimetableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		
		init();
	}
	
	private void init(){
		fill = new Paint(Paint.ANTI_ALIAS_FLAG);
		fill.setStyle(Paint.Style.FILL);
		fill.setTextSize(50);
		
		stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
		stroke.setStyle(Paint.Style.STROKE);
		stroke.setStrokeWidth(getResources().getDisplayMetrics().densityDpi / 60.0f);
		
		text = new Paint(Paint.ANTI_ALIAS_FLAG);
		text.setStyle(Paint.Style.FILL_AND_STROKE);
		text.setTextSize(getResources().getDisplayMetrics().densityDpi / 12.0f);
	}
	
	public void setLessons(ArrayList<Lesson.ScheduleLesson> lessons) {
		this.lessons = lessons;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int minHour = 0;
		int maxHour = 24;
		
		Rect timeBounds = new Rect();
		text.getTextBounds("00:00", 0, 5, timeBounds);
		
		double topOffset = timeBounds.height() / 2.0;
		double bottomOffset = timeBounds.height() / 2.0;
		double leftOffset = timeBounds.width() + 12 * getResources().getDisplayMetrics().density;
		
		ArrayList<Lesson.ScheduleLesson> startOrdered = Lesson.ScheduleLesson.orderByStartTime(lessons);
		if(startOrdered.size() > 0) minHour = startOrdered.get(0).start.get(Calendar.HOUR_OF_DAY);
		
		ArrayList<Lesson.ScheduleLesson> endOrdered = Lesson.ScheduleLesson.orderByEndTime(lessons);
		if(endOrdered.size() > 0) maxHour = Math.min(endOrdered.get(endOrdered.size() - 1).end.get(Calendar.HOUR_OF_DAY) + 1, 24);
		
		double heightPerMinute = (getHeight() - topOffset - bottomOffset) / (double)(maxHour - minHour) / 60.0;
		
		Calendar today = Calendar.getInstance();
		if(lessons.size() > 0) today.setTimeInMillis(lessons.get(0).start.getTimeInMillis());
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.AM_PM, Calendar.AM);
		today.set(Calendar.HOUR_OF_DAY, 0);
		
		text.setColor(Color.RED);
		if((Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && Calendar.getInstance().get(Calendar.YEAR) == today.get(Calendar.YEAR)) && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= minHour && (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < maxHour || (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == maxHour && Calendar.getInstance().get(Calendar.MINUTE) == 0))){
			canvas.drawLine(0, (float)(((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - minHour) * 60 + Calendar.getInstance().get(Calendar.MINUTE)) * heightPerMinute + topOffset - 1), (float)getWidth(), (float)(((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - minHour) * 60 + Calendar.getInstance().get(Calendar.MINUTE)) * heightPerMinute + topOffset), text);
		}
		
		text.setColor(Color.LTGRAY);
		for(int i = minHour; i <= maxHour; i++){
			canvas.drawText(String.format(Locale.ENGLISH, "%02d", i) + ":00", 0, (float)((i - minHour) * 60 * heightPerMinute + timeBounds.height() / 2 + topOffset), text);
			canvas.drawLine((float)leftOffset - 8 * getResources().getDisplayMetrics().density, (float)((i - minHour) * 60 * heightPerMinute + topOffset - 1), (float)getWidth(), (float)((i - minHour) * 60 * heightPerMinute + topOffset - 1), text);
		}
		
		Calendar tomorrow = Calendar.getInstance();
		if(lessons.size() > 0) tomorrow.setTimeInMillis(lessons.get(0).start.getTimeInMillis());
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		tomorrow.set(Calendar.MINUTE, 0);
		tomorrow.set(Calendar.HOUR, 0);
		tomorrow.set(Calendar.AM_PM, Calendar.AM);
		tomorrow.set(Calendar.HOUR_OF_DAY, 0);
		
		Rect stringBounds = new Rect();
		for(Lesson.ScheduleLesson l : lessons){
			if(l.start.after(tomorrow)) continue;
			
			fill.setColor(l.lesson.color);
			stroke.setColor(ColorUtils.blendARGB(l.lesson.color, Color.BLACK, 0.2f));
			text.setColor(ColorUtils.blendARGB(l.lesson.color, Color.BLACK, 0.2f));
			
			double y = (double)l.start.get(Calendar.MINUTE) + (l.start.get(Calendar.HOUR_OF_DAY) - minHour) * 60.0;
			double height = (double)l.end.get(Calendar.MINUTE) + (l.end.get(Calendar.HOUR_OF_DAY) - minHour) * 60.0;
			if(l.end.compareTo(tomorrow) == 0) height = (maxHour - minHour) * 60 * 60;
			double width = (getWidth() - leftOffset) / (float)l.total;
			double x = l.index * width;
			
			String title = "";
			if(l.lesson.subject != null){
				title = (l.lesson.subject.name != null ? l.lesson.subject.name : l.lesson.subject.shortName) + " [" + l.lesson.room + "]";
				text.getTextBounds(title, 0, title.length(), stringBounds);
				if(width < stringBounds.width() + getResources().getDisplayMetrics().density * 24 + timeBounds.width()) title = l.lesson.subject.shortName + " [" + l.lesson.room + "]";
				text.getTextBounds(title, 0, title.length(), stringBounds);
				if(width < stringBounds.width() + getResources().getDisplayMetrics().density * 24 + timeBounds.width()) title = (l.lesson.subject.name != null ? l.lesson.subject.name : l.lesson.subject.shortName);
				text.getTextBounds(title, 0, title.length(), stringBounds);
				if(width < stringBounds.width() + getResources().getDisplayMetrics().density * 24 + timeBounds.width()) title = l.lesson.subject.shortName;
			} else {
				title = l.lesson.lessonIdentifier + " [" + l.lesson.room + "]";
				text.getTextBounds(title, 0, title.length(), stringBounds);
				if(width < stringBounds.width() + getResources().getDisplayMetrics().density * 24 + timeBounds.width()) title = l.lesson.lessonIdentifier;
			}
			
			text.getTextBounds(title, 0, title.length(), stringBounds);
			
			canvas.drawRect((float)(leftOffset + x), (float)(topOffset + y * heightPerMinute) - 1, (float)(x + leftOffset + width), (float)(topOffset + height * heightPerMinute), fill);
			canvas.drawRect((float)(leftOffset + x) + stroke.getStrokeWidth() / 2, (float)(topOffset + y * heightPerMinute) + stroke.getStrokeWidth() / 2 - 1, (float)(x + leftOffset + width) - stroke.getStrokeWidth() / 2, (float)(topOffset + height * heightPerMinute) - stroke.getStrokeWidth() / 2, stroke);
			if((height - y) * heightPerMinute / 1.5 >= text.getTextSize() && width >= stringBounds.width() + getResources().getDisplayMetrics().density * 24 + timeBounds.width()) canvas.drawText(title, (float)(leftOffset + x + (getResources().getDisplayMetrics().density * 8)), (float)(topOffset + y * heightPerMinute + (height - y) * heightPerMinute / 2 + stringBounds.height() / 3), text);
			
			if((height - y) * heightPerMinute >= timeBounds.height() * 2 + 2 * stroke.getStrokeWidth() + getResources().getDisplayMetrics().density * 6 && width >= timeBounds.width() + 2 * getResources().getDisplayMetrics().density * 8){
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				
				canvas.drawText(sdf.format(l.start.getTime()), (float)(x + leftOffset + width - timeBounds.width() - getResources().getDisplayMetrics().density * 8), (float)(topOffset + y * heightPerMinute + stroke.getStrokeWidth() + getResources().getDisplayMetrics().density * 2 + timeBounds.height()), text);
				canvas.drawText(sdf.format(l.end.getTime()), (float)(x + leftOffset + width - timeBounds.width() - getResources().getDisplayMetrics().density * 8), (float)(topOffset + height * heightPerMinute - stroke.getStrokeWidth() - getResources().getDisplayMetrics().density * 2), text);
			}
		}
	}
}
