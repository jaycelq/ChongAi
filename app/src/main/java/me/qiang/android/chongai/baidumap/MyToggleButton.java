package me.qiang.android.chongai.baidumap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import me.qiang.android.chongai.R;

/**MyToggleButton extends View implements View.OnClickListener
 * 自定义ToggleButton（效果类似Switch），注意此例中空间大小已经定死，若需修改大小请修改onMeasure()；
 * 
 * 使用自定义属性示例：
 * 		在layout文件中声明命名空间格式（http://schemas.android.com/apk/res/ + 应用程序包名）：
 * 			xmlns:androidstudy="http://schemas.android.com/apk/res/com.androidstudy"
 * 在布局文件中使用自定义ToggleButton的格式
 * 		（一定要指定layout_width与layout_height，即使此例中MyToggleButton大小已经定死）：
 * 		（my_on_background my_off_background my_slide_btn三个属性也应指定图片资源）
 * 		<com.androidstudy.ui.MyToggleButton android:id="@+id/togglebtn"
	        android:layout_width="wrap_content"
	        android:layout_height="wrap_content"
	        androidstudy:my_on_background="@drawable/switch_on_background"
	        androidstudy:my_off_background="@drawable/switch_off_background"
	        androidstudy:my_slide_btn="@drawable/toggle_button"
	        androidstudy:my_toggle_state="false" />
 *       
 * @author Eugene
 * @data 2015-1-25
 */
public class MyToggleButton extends View implements View.OnClickListener{
	
	private Bitmap backgroundOnBitmap;//做为On背景的图片
	private Bitmap backgroundOffBitmap;//做为Off背景的图片
	private Bitmap toggleBtn;//可以滑动的按钮图片
	
	private Paint paint;
	
	private int backgroundOnId;//On背景图的资源ID
	private int backgroundOffId;//Off背景图的资源ID
	private int toggleBtnId;//滑动图片的资源ID
	
	private float toggleBtn_left;//滑动按钮的左边界
	private boolean toggleState = false;//当前开关的状态，true为开
	private boolean isDrag = false;//判断是否发生拖动，如果拖动了就不再响应 onClick事件
	private int firstX;//down事件的x值
	private int lastX;//touch事件的最后一个x值
	
	private Handler handler;
	public static final int TOGGLE_CHANGE = 100;
	
	/**在代码里面创建对象的时候，使用此构造方法
	 * @param context
	 */
	public MyToggleButton(Context context) {
		super(context);
		//TODO:
	}
	
	/**在布局文件中声明的view，创建时由系统自动调用
	 * @param context
	 * @param attrs
	 */
	public MyToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//设置自定义的属性
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyToggleButton);
		int count = typedArray.getIndexCount();
		for (int i = 0; i < count; i++) {
			int itemId = typedArray.getIndex(i);
			switch (itemId) {
			case R.styleable.MyToggleButton_my_toggle_state:
				toggleState = typedArray.getBoolean(itemId, false);
				break;
			case R.styleable.MyToggleButton_my_on_background:
				backgroundOnId = typedArray.getResourceId(itemId, -1);
				if(backgroundOnId == -1){
					throw new RuntimeException("请设置On背景图片");
				}
				backgroundOnBitmap = BitmapFactory.decodeResource(getResources(), backgroundOnId);
				break;
			case R.styleable.MyToggleButton_my_off_background:
				backgroundOffId = typedArray.getResourceId(itemId, -1);
				if(backgroundOffId == -1){
					throw new RuntimeException("请设置Off背景图片");
				}
				backgroundOffBitmap = BitmapFactory.decodeResource(getResources(), backgroundOffId);
				break;
			case R.styleable.MyToggleButton_my_slide_btn:
				toggleBtnId = typedArray.getResourceId(itemId, -1);
				if(toggleBtnId == -1){
					throw new RuntimeException("请设置可以滑动的按钮图片");
				}
				toggleBtn = BitmapFactory.decodeResource(getResources(), toggleBtnId);
				break;
			default:
				break;
			}
			
		}
		typedArray.recycle();
		
		init();
	}

	/**初始化
	 */
	private void init() {
		//初始化图片（自定义的属性处设置）
//		backgroundOnBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.switch_background);
//		toggleBtn = BitmapFactory.decodeResource(getResources(), R.drawable.slide_button);
		//初始化画笔
		paint = new Paint();
		paint.setAntiAlias(true); //打开抗矩齿
		//添加单击事件监听
		setOnClickListener(this);
		//由于toggleState可由自定义属性设置，应在设置后调用更新相关状态
		refreshToggleBtnState();
		refreshToggleBtnView();
	}
	
	@Override//测量尺寸的回调方法 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		//设置当前view的大小，width为view的宽度，height为view的高度（单位：像素）
		setMeasuredDimension(backgroundOnBitmap.getWidth(), backgroundOnBitmap.getHeight());
	}

	@Override//确定位置的时候调用此方法（自定义view的时候，作用不大，可省略）
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override//绘制当前view的内容
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
		
		//drawBitmap(Bitmap bitmap, float left, float top, Paint paint)中left为图片的左边界，top为图片的上边界
		//绘制背景
		if (toggleState) {//开
			canvas.drawBitmap(backgroundOnBitmap, 0, 0, paint);
		} else {
			canvas.drawBitmap(backgroundOffBitmap, 0, 0, paint);
		}
		
		//绘制可滑动的按钮
		canvas.drawBitmap(toggleBtn, toggleBtn_left, 0, paint);
	}
	
	public boolean getToggleState(){
		return toggleState;
	}
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	/**onClick事件在View.onTouchEvent中被解析；
	 * 系统对onClick事件的解析过于简陋，只要有down或up事件，系统即认为 发生了onClick事件；
	 */
	@Override
	public void onClick(View v) {
		if(!isDrag){//如果没有拖动，才执行改变状态的动作
			toggleState = !toggleState;
			//刷新当前开关状态
			refreshToggleBtnState();
			//刷新当前开关视图
			refreshToggleBtnView();
			//发送消息
			if (handler != null) {
				handler.sendEmptyMessage(TOGGLE_CHANGE);
			}
			
		}
	}

    public void turnoff(){
        toggleState = false;
        //刷新当前开关状态
        refreshToggleBtnState();
        //刷新当前开关视图
        refreshToggleBtnView();
    }

	/**根据当前开关状态刷新ToggleBtn位置值
	 */
	private void refreshToggleBtnState() {
		if(toggleState){
			toggleBtn_left = backgroundOnBitmap.getWidth() - toggleBtn.getWidth();
		}else{
			toggleBtn_left = 0;
		}
	}

	/**刷新当前开关视图
	 */
	private void refreshToggleBtnView() {
		//对slideBtn_left的值进行判断，确保其在合理的位置，即 0 <= toggleBtn_left <= maxLeft
		int maxLeft = backgroundOnBitmap.getWidth() - toggleBtn.getWidth();//slideBtn左边界最大值
		toggleBtn_left = (toggleBtn_left > 0) ? toggleBtn_left : 0;//确保slideBtn_left >= 0
		toggleBtn_left = (toggleBtn_left < maxLeft) ? toggleBtn_left : maxLeft;//确保slideBtn_left <= maxLeft
		
		invalidate();//刷新当前视图，触发执行onDraw
	}

	/**onClick事件在View.onTouchEvent中被解析；
	 * 系统对onClick事件的解析过于简陋，只要有down或up事件，系统即认为 发生了onClick事件；
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			firstX = lastX = (int) event.getX();
			isDrag = false;
			break;
		case MotionEvent.ACTION_MOVE:
			if(Math.abs(event.getX() - firstX) > 5){//判断是否发生拖动
				isDrag = true;
			}
			int dis = (int) (event.getX() - lastX);//计算手指在屏幕上移动的距离
			lastX = (int) event.getX();//更新lastX
			toggleBtn_left = toggleBtn_left + dis;//根据手指移动的距离，改变slideBtn_left的值
			break;
		case MotionEvent.ACTION_UP:
			if (isDrag) {//在发生拖动的情况下，根据最后的位置，判断当前开关的状态
				int maxLeft = backgroundOnBitmap.getWidth() - toggleBtn.getWidth();//slideBtn左边界最大值
				//根据 slideBtn_left判断当前是什么状态
				if (toggleBtn_left > maxLeft / 2) {//此时为打开的状态
					toggleState = true;
				} else {
					toggleState = false;
				}
				refreshToggleBtnState();
				refreshToggleBtnView();
			}
			break;
		}
		refreshToggleBtnView();
		return true; 
	}

}
