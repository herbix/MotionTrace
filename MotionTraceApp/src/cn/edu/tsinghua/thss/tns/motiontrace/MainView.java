package cn.edu.tsinghua.thss.tns.motiontrace;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainView extends SurfaceView implements SurfaceHolder.Callback {

	private DrawThread thread = new DrawThread();
	private boolean updateView;
	private MainActivity activity;
	
	private Paint black = new Paint();
	private Paint red = new Paint();

	public MainView(Context context) {
		super(context);
		activity = (MainActivity)context;
		black.setColor(0xFF000000);
		red.setColor(0xFFFF0000);
		black.setTextSize(80);
		red.setTextSize(80);
		getHolder().addCallback(this);
	}

	public MainView(Context context, AttributeSet attr) {
		this(context);
	}

	public void refresh() {
		updateView = true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		updateView = true;
		if(thread.getState() == Thread.State.NEW)
			thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread.isRunning = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		updateView = true;
	}

	public void doDraw(Canvas canvas) {
		int w = canvas.getWidth() / 2;
		int h = canvas.getHeight() / 2;
		
		canvas.drawColor(0xFFFFFFFF);

		double[] value = activity.values;
		canvas.drawLine(w, h, (float)(w+value[0]*20), (float)(h-value[1]*20), black);
		canvas.drawText("Z", (float)(w+value[0]*20), (float)(h-value[1]*20), black);
		
		value = activity.values2;
		canvas.drawLine(w, h, (float)(w+value[0]*20), (float)(h-value[1]*20), black);
		canvas.drawText("X", (float)(w+value[0]*20), (float)(h-value[1]*20), black);
		canvas.drawText("Direction: " + String.valueOf(Math.atan2(value[1], value[0]) / Math.PI * 180), 20, 300, black);

		value = activity.values3;
		canvas.drawLine(w, h, (float)(w+value[0]*20), (float)(h-value[1]*20), black);
		canvas.drawText("Y", (float)(w+value[0]*20), (float)(h-value[1]*20), black);

		value = activity.values4;
		canvas.drawLine(w, h, (float)(w+value[0]*200), (float)(h-value[1]*200), red);
		
		//double r = Math.sqrt(value[0] * value[0] + value[1] * value[1] + value[2] * value[2]);

		canvas.drawText("Steps: " + String.valueOf(activity.step), 20, 100, black);
		//canvas.drawText(String.valueOf(activity.stepTime), 20, 300, black);

		//canvas.drawText(activity.dpc.getStateString(), 20, 400, black);
		
		//canvas.drawText(String.valueOf(activity.se.getLastStepHalfTime() / 1e9), 20, 500, black);
		canvas.drawText("Stride Length: " + String.valueOf(activity.se.getEstimatedStrideLength(1.8)), 20, 200, black);

	}

	class DrawThread extends Thread {

		boolean isRunning = true;

		@Override
		public void run() {

			while(isRunning) {

				if(updateView) {
					SurfaceHolder holder = getHolder();
					Canvas canvas = holder.lockCanvas();
					if(canvas != null) {
						doDraw(canvas);
						holder.unlockCanvasAndPost(canvas);
					}
				}

				updateView = false;

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}
			}
		}
	}

}
