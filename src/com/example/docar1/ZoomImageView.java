package com.example.docar1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * got from a chinese blog, I will modify it later
 * 
 * @author guolin
 */
public class ZoomImageView extends View {

	/**
	 *
	 */
	public static final int STATUS_INIT = 1;

	/**
	 * å›¾ç‰‡æ”¾å¤§çŠ¶æ€�å¸¸é‡�
	 */
	public static final int STATUS_ZOOM_OUT = 2;

	/**
	 * å›¾ç‰‡ç¼©å°�çŠ¶æ€�å¸¸é‡�
	 */
	public static final int STATUS_ZOOM_IN = 3;

	/**
	 * å›¾ç‰‡æ‹–åŠ¨çŠ¶æ€�å¸¸é‡�
	 */
	public static final int STATUS_MOVE = 4;

	/**
	 * ç”¨äºŽå¯¹å›¾ç‰‡è¿›è¡Œç§»åŠ¨å’Œç¼©æ”¾å�˜æ�¢çš„çŸ©é˜µ
	 */
	private Matrix matrix = new Matrix();

	/**
	 * å¾…å±•ç¤ºçš„Bitmapå¯¹è±¡
	 */
	private Bitmap sourceBitmap;

	/**
	 * è®°å½•å½“å‰�æ“�ä½œçš„çŠ¶æ€�ï¼Œå�¯é€‰å€¼ä¸ºSTATUS_INITã€�STATUS_ZOOM_OUTã€�STATUS_ZOOM_INå’ŒSTATUS_MOVE
	 */
	private int currentStatus;

	/**
	 * ZoomImageViewæŽ§ä»¶çš„å®½åº¦
	 */
	private int width;

	/**
	 * ZoomImageViewæŽ§ä»¶çš„é«˜åº¦
	 */
	private int height;

	/**
	 * è®°å½•ä¸¤æŒ‡å�Œæ—¶æ”¾åœ¨å±�å¹•ä¸Šæ—¶ï¼Œä¸­å¿ƒç‚¹çš„æ¨ªå��æ ‡å€¼
	 */
	private float centerPointX;

	/**
	 * è®°å½•ä¸¤æŒ‡å�Œæ—¶æ”¾åœ¨å±�å¹•ä¸Šæ—¶ï¼Œä¸­å¿ƒç‚¹çš„çºµå��æ ‡å€¼
	 */
	private float centerPointY;

	/**
	 * è®°å½•å½“å‰�å›¾ç‰‡çš„å®½åº¦ï¼Œå›¾ç‰‡è¢«ç¼©æ”¾æ—¶ï¼Œè¿™ä¸ªå€¼ä¼šä¸€èµ·å�˜åŠ¨
	 */
	private float currentBitmapWidth;

	/**
	 * è®°å½•å½“å‰�å›¾ç‰‡çš„é«˜åº¦ï¼Œå›¾ç‰‡è¢«ç¼©æ”¾æ—¶ï¼Œè¿™ä¸ªå€¼ä¼šä¸€èµ·å�˜åŠ¨
	 */
	private float currentBitmapHeight;

	/**
	 * è®°å½•ä¸Šæ¬¡æ‰‹æŒ‡ç§»åŠ¨æ—¶çš„æ¨ªå��æ ‡
	 */
	private float lastXMove = -1;

	/**
	 * è®°å½•ä¸Šæ¬¡æ‰‹æŒ‡ç§»åŠ¨æ—¶çš„çºµå��æ ‡
	 */
	private float lastYMove = -1;

	/**
	 * è®°å½•æ‰‹æŒ‡åœ¨æ¨ªå��æ ‡æ–¹å�‘ä¸Šçš„ç§»åŠ¨è·�ç¦»
	 */
	private float movedDistanceX;

	/**
	 * è®°å½•æ‰‹æŒ‡åœ¨çºµå��æ ‡æ–¹å�‘ä¸Šçš„ç§»åŠ¨è·�ç¦»
	 */
	private float movedDistanceY;

	/**
	 * è®°å½•å›¾ç‰‡åœ¨çŸ©é˜µä¸Šçš„æ¨ªå�‘å��ç§»å€¼
	 */
	private float totalTranslateX;

	/**
	 * è®°å½•å›¾ç‰‡åœ¨çŸ©é˜µä¸Šçš„çºµå�‘å��ç§»å€¼
	 */
	private float totalTranslateY;

	/**
	 * è®°å½•å›¾ç‰‡åœ¨çŸ©é˜µä¸Šçš„æ€»ç¼©æ”¾æ¯”ä¾‹
	 */
	private float totalRatio;

	/**
	 * è®°å½•æ‰‹æŒ‡ç§»åŠ¨çš„è·�ç¦»æ‰€é€ æˆ�çš„ç¼©æ”¾æ¯”ä¾‹
	 */
	private float scaledRatio;

	/**
	 * è®°å½•å›¾ç‰‡åˆ�å§‹åŒ–æ—¶çš„ç¼©æ”¾æ¯”ä¾‹
	 */
	private float initRatio;

	/**
	 * è®°å½•ä¸Šæ¬¡ä¸¤æŒ‡ä¹‹é—´çš„è·�ç¦»
	 */
	private double lastFingerDis;

	/**
	 * ZoomImageViewæž„é€ å‡½æ•°ï¼Œå°†å½“å‰�æ“�ä½œçŠ¶æ€�è®¾ä¸ºSTATUS_INITã€‚
	 * 
	 * @param context
	 * @param attrs
	 */
	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		currentStatus = STATUS_INIT;
	}

	/**
	 * å°†å¾…å±•ç¤ºçš„å›¾ç‰‡è®¾ç½®è¿›æ�¥ã€‚
	 * 
	 * @param bitmap
	 *            å¾…å±•ç¤ºçš„Bitmapå¯¹è±¡
	 */
	public void setImageBitmap(Bitmap bitmap) {
		sourceBitmap = bitmap;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			// åˆ†åˆ«èŽ·å�–åˆ°ZoomImageViewçš„å®½åº¦å’Œé«˜åº¦
			width = getWidth();
			height = getHeight();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
				// å½“æœ‰ä¸¤ä¸ªæ‰‹æŒ‡æŒ‰åœ¨å±�å¹•ä¸Šæ—¶ï¼Œè®¡ç®—ä¸¤æŒ‡ä¹‹é—´çš„è·�ç¦»
				lastFingerDis = distanceBetweenFingers(event);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 1) {
				// å�ªæœ‰å�•æŒ‡æŒ‰åœ¨å±�å¹•ä¸Šç§»åŠ¨æ—¶ï¼Œä¸ºæ‹–åŠ¨çŠ¶æ€�
				float xMove = event.getX();
				float yMove = event.getY();
				if (lastXMove == -1 && lastYMove == -1) {
					lastXMove = xMove;
					lastYMove = yMove;
				}
				currentStatus = STATUS_MOVE;
				movedDistanceX = xMove - lastXMove;
				movedDistanceY = yMove - lastYMove;
				// è¿›è¡Œè¾¹ç•Œæ£€æŸ¥ï¼Œä¸�å…�è®¸å°†å›¾ç‰‡æ‹–å‡ºè¾¹ç•Œ
				if (totalTranslateX + movedDistanceX > 0) {
					movedDistanceX = 0;
				} else if (width - (totalTranslateX + movedDistanceX) > currentBitmapWidth) {
					movedDistanceX = 0;
				}
				if (totalTranslateY + movedDistanceY > 0) {
					movedDistanceY = 0;
				} else if (height - (totalTranslateY + movedDistanceY) > currentBitmapHeight) {
					movedDistanceY = 0;
				}
				// è°ƒç”¨onDraw()æ–¹æ³•ç»˜åˆ¶å›¾ç‰‡
				invalidate();
				lastXMove = xMove;
				lastYMove = yMove;
			} else if (event.getPointerCount() == 2) {
				// æœ‰ä¸¤ä¸ªæ‰‹æŒ‡æŒ‰åœ¨å±�å¹•ä¸Šç§»åŠ¨æ—¶ï¼Œä¸ºç¼©æ”¾çŠ¶æ€�
				centerPointBetweenFingers(event);
				double fingerDis = distanceBetweenFingers(event);
				if (fingerDis > lastFingerDis) {
					currentStatus = STATUS_ZOOM_OUT;
				} else {
					currentStatus = STATUS_ZOOM_IN;
				}
				// è¿›è¡Œç¼©æ”¾å€�æ•°æ£€æŸ¥ï¼Œæœ€å¤§å�ªå…�è®¸å°†å›¾ç‰‡æ”¾å¤§4å€�ï¼Œæœ€å°�å�¯ä»¥ç¼©å°�åˆ°åˆ�å§‹åŒ–æ¯”ä¾‹
				if ((currentStatus == STATUS_ZOOM_OUT && totalRatio < 4 * initRatio)
						|| (currentStatus == STATUS_ZOOM_IN && totalRatio > initRatio)) {
					scaledRatio = (float) (fingerDis / lastFingerDis);
					totalRatio = totalRatio * scaledRatio;
					if (totalRatio > 4 * initRatio) {
						totalRatio = 4 * initRatio;
					} else if (totalRatio < initRatio) {
						totalRatio = initRatio;
					}
					// è°ƒç”¨onDraw()æ–¹æ³•ç»˜åˆ¶å›¾ç‰‡
					invalidate();
					lastFingerDis = fingerDis;
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			if (event.getPointerCount() == 2) {
				// æ‰‹æŒ‡ç¦»å¼€å±�å¹•æ—¶å°†ä¸´æ—¶å€¼è¿˜åŽŸ
				lastXMove = -1;
				lastYMove = -1;
			}
			break;
		case MotionEvent.ACTION_UP:
			// æ‰‹æŒ‡ç¦»å¼€å±�å¹•æ—¶å°†ä¸´æ—¶å€¼è¿˜åŽŸ
			lastXMove = -1;
			lastYMove = -1;
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * æ ¹æ�®currentStatusçš„å€¼æ�¥å†³å®šå¯¹å›¾ç‰‡è¿›è¡Œä»€ä¹ˆæ ·çš„ç»˜åˆ¶æ“�ä½œã€‚
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		switch (currentStatus) {
		case STATUS_ZOOM_OUT:
		case STATUS_ZOOM_IN:
			zoom(canvas);
			break;
		case STATUS_MOVE:
			move(canvas);
			break;
		case STATUS_INIT:
			initBitmap(canvas);
		default:
			canvas.drawBitmap(sourceBitmap, matrix, null);
			break;
		}
	}

	/**
	 * å¯¹å›¾ç‰‡è¿›è¡Œç¼©æ”¾å¤„ç�†ã€‚
	 * 
	 * @param canvas
	 */
	private void zoom(Canvas canvas) {
		matrix.reset();
		// å°†å›¾ç‰‡æŒ‰æ€»ç¼©æ”¾æ¯”ä¾‹è¿›è¡Œç¼©æ”¾
		matrix.postScale(totalRatio, totalRatio);
		float scaledWidth = sourceBitmap.getWidth() * totalRatio;
		float scaledHeight = sourceBitmap.getHeight() * totalRatio;
		float translateX = 0f;
		float translateY = 0f;
		// å¦‚æžœå½“å‰�å›¾ç‰‡å®½åº¦å°�äºŽå±�å¹•å®½åº¦ï¼Œåˆ™æŒ‰å±�å¹•ä¸­å¿ƒçš„æ¨ªå��æ ‡è¿›è¡Œæ°´å¹³ç¼©æ”¾ã€‚å�¦åˆ™æŒ‰ä¸¤æŒ‡çš„ä¸­å¿ƒç‚¹çš„æ¨ªå��æ ‡è¿›è¡Œæ°´å¹³ç¼©æ”¾
		if (currentBitmapWidth < width) {
			translateX = (width - scaledWidth) / 2f;
		} else {
			translateX = totalTranslateX * scaledRatio + centerPointX * (1 - scaledRatio);
			// è¿›è¡Œè¾¹ç•Œæ£€æŸ¥ï¼Œä¿�è¯�å›¾ç‰‡ç¼©æ”¾å�Žåœ¨æ°´å¹³æ–¹å�‘ä¸Šä¸�ä¼šå��ç§»å‡ºå±�å¹•
			if (translateX > 0) {
				translateX = 0;
			} else if (width - translateX > scaledWidth) {
				translateX = width - scaledWidth;
			}
		}
		// å¦‚æžœå½“å‰�å›¾ç‰‡é«˜åº¦å°�äºŽå±�å¹•é«˜åº¦ï¼Œåˆ™æŒ‰å±�å¹•ä¸­å¿ƒçš„çºµå��æ ‡è¿›è¡Œåž‚ç›´ç¼©æ”¾ã€‚å�¦åˆ™æŒ‰ä¸¤æŒ‡çš„ä¸­å¿ƒç‚¹çš„çºµå��æ ‡è¿›è¡Œåž‚ç›´ç¼©æ”¾
		if (currentBitmapHeight < height) {
			translateY = (height - scaledHeight) / 2f;
		} else {
			translateY = totalTranslateY * scaledRatio + centerPointY * (1 - scaledRatio);
			// è¿›è¡Œè¾¹ç•Œæ£€æŸ¥ï¼Œä¿�è¯�å›¾ç‰‡ç¼©æ”¾å�Žåœ¨åž‚ç›´æ–¹å�‘ä¸Šä¸�ä¼šå��ç§»å‡ºå±�å¹•
			if (translateY > 0) {
				translateY = 0;
			} else if (height - translateY > scaledHeight) {
				translateY = height - scaledHeight;
			}
		}
		// ç¼©æ”¾å�Žå¯¹å›¾ç‰‡è¿›è¡Œå��ç§»ï¼Œä»¥ä¿�è¯�ç¼©æ”¾å�Žä¸­å¿ƒç‚¹ä½�ç½®ä¸�å�˜
		matrix.postTranslate(translateX, translateY);
		totalTranslateX = translateX;
		totalTranslateY = translateY;
		currentBitmapWidth = scaledWidth;
		currentBitmapHeight = scaledHeight;
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	/**
	 * å¯¹å›¾ç‰‡è¿›è¡Œå¹³ç§»å¤„ç�†
	 * 
	 * @param canvas
	 */
	private void move(Canvas canvas) {
		matrix.reset();
		// æ ¹æ�®æ‰‹æŒ‡ç§»åŠ¨çš„è·�ç¦»è®¡ç®—å‡ºæ€»å��ç§»å€¼
		float translateX = totalTranslateX + movedDistanceX;
		float translateY = totalTranslateY + movedDistanceY;
		// å…ˆæŒ‰ç…§å·²æœ‰çš„ç¼©æ”¾æ¯”ä¾‹å¯¹å›¾ç‰‡è¿›è¡Œç¼©æ”¾
		matrix.postScale(totalRatio, totalRatio);
		// å†�æ ¹æ�®ç§»åŠ¨è·�ç¦»è¿›è¡Œå��ç§»
		matrix.postTranslate(translateX, translateY);
		totalTranslateX = translateX;
		totalTranslateY = translateY;
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	/**
	 * å¯¹å›¾ç‰‡è¿›è¡Œåˆ�å§‹åŒ–æ“�ä½œï¼ŒåŒ…æ‹¬è®©å›¾ç‰‡å±…ä¸­ï¼Œä»¥å�Šå½“å›¾ç‰‡å¤§äºŽå±�å¹•å®½é«˜æ—¶å¯¹å›¾ç‰‡è¿›è¡ŒåŽ‹ç¼©ã€‚
	 * 
	 * @param canvas
	 */
	private void initBitmap(Canvas canvas) {
		if (sourceBitmap != null) {
			matrix.reset();
			int bitmapWidth = sourceBitmap.getWidth();
			int bitmapHeight = sourceBitmap.getHeight();
			if (bitmapWidth > width || bitmapHeight > height) {
				if (bitmapWidth - width > bitmapHeight - height) {
					// å½“å›¾ç‰‡å®½åº¦å¤§äºŽå±�å¹•å®½åº¦æ—¶ï¼Œå°†å›¾ç‰‡ç­‰æ¯”ä¾‹åŽ‹ç¼©ï¼Œä½¿å®ƒå�¯ä»¥å®Œå…¨æ˜¾ç¤ºå‡ºæ�¥
					float ratio = width / (bitmapWidth * 1.0f);
					matrix.postScale(ratio, ratio);
					float translateY = (height - (bitmapHeight * ratio)) / 2f;
					// åœ¨çºµå��æ ‡æ–¹å�‘ä¸Šè¿›è¡Œå��ç§»ï¼Œä»¥ä¿�è¯�å›¾ç‰‡å±…ä¸­æ˜¾ç¤º
					matrix.postTranslate(0, translateY);
					totalTranslateY = translateY;
					totalRatio = initRatio = ratio;
				} else {
					// å½“å›¾ç‰‡é«˜åº¦å¤§äºŽå±�å¹•é«˜åº¦æ—¶ï¼Œå°†å›¾ç‰‡ç­‰æ¯”ä¾‹åŽ‹ç¼©ï¼Œä½¿å®ƒå�¯ä»¥å®Œå…¨æ˜¾ç¤ºå‡ºæ�¥
					float ratio = height / (bitmapHeight * 1.0f);
					matrix.postScale(ratio, ratio);
					float translateX = (width - (bitmapWidth * ratio)) / 2f;
					// åœ¨æ¨ªå��æ ‡æ–¹å�‘ä¸Šè¿›è¡Œå��ç§»ï¼Œä»¥ä¿�è¯�å›¾ç‰‡å±…ä¸­æ˜¾ç¤º
					matrix.postTranslate(translateX, 0);
					totalTranslateX = translateX;
					totalRatio = initRatio = ratio;
				}
				currentBitmapWidth = bitmapWidth * initRatio;
				currentBitmapHeight = bitmapHeight * initRatio;
			} else {
				// å½“å›¾ç‰‡çš„å®½é«˜éƒ½å°�äºŽå±�å¹•å®½é«˜æ—¶ï¼Œç›´æŽ¥è®©å›¾ç‰‡å±…ä¸­æ˜¾ç¤º
				float translateX = (width - sourceBitmap.getWidth()) / 2f;
				float translateY = (height - sourceBitmap.getHeight()) / 2f;
				matrix.postTranslate(translateX, translateY);
				totalTranslateX = translateX;
				totalTranslateY = translateY;
				totalRatio = initRatio = 1f;
				currentBitmapWidth = bitmapWidth;
				currentBitmapHeight = bitmapHeight;
			}
			canvas.drawBitmap(sourceBitmap, matrix, null);
		}
	}

	/**
	 * è®¡ç®—ä¸¤ä¸ªæ‰‹æŒ‡ä¹‹é—´çš„è·�ç¦»ã€‚
	 * 
	 * @param event
	 * @return ä¸¤ä¸ªæ‰‹æŒ‡ä¹‹é—´çš„è·�ç¦»
	 */
	private double distanceBetweenFingers(MotionEvent event) {
		float disX = Math.abs(event.getX(0) - event.getX(1));
		float disY = Math.abs(event.getY(0) - event.getY(1));
		return Math.sqrt(disX * disX + disY * disY);
	}

	/**
	 * è®¡ç®—ä¸¤ä¸ªæ‰‹æŒ‡ä¹‹é—´ä¸­å¿ƒç‚¹çš„å��æ ‡ã€‚
	 * 
	 * @param event
	 */
	private void centerPointBetweenFingers(MotionEvent event) {
		float xPoint0 = event.getX(0);
		float yPoint0 = event.getY(0);
		float xPoint1 = event.getX(1);
		float yPoint1 = event.getY(1);
		centerPointX = (xPoint0 + xPoint1) / 2;
		centerPointY = (yPoint0 + yPoint1) / 2;
	}

}
