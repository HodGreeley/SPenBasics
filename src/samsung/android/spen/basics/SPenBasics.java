/*
 * Copyright 2012 Samsung Telecommunications America
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *    
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package samsung.android.spen.basics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/*
 * Import a few classes from the SPen SDK
 */
import com.samsung.sdraw.AbstractSettingView;
import com.samsung.sdraw.AbstractSettingView.SettingChangeListener;
import com.samsung.sdraw.CanvasView;
import com.samsung.sdraw.PenSettingInfo;
import com.samsung.sdraw.SettingView;

public class SPenBasics extends Activity {
	private static final String TAG = "SPenBasics";

	/*
	 * Two special SPen view classes, a canvas for drawing
	 *     and a dialog for setting pen properties
	 */
	private CanvasView	mCanvasView;
	private SettingView	mSettingView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initButtons();

		mCanvasView = (CanvasView) findViewById(R.id.canvas_view);
		mSettingView = (SettingView) findViewById(R.id.setting_view);

		/*
		 * The interesting part of the preparation work
		 */
		mCanvasView.setSettingView(mSettingView);        			

		mCanvasView.setOnHistoryChangeListener(historyChangeListener);
		mSettingView.setOnSettingChangeListener(settingChangeListener);
	}

	/*
	 * Click listener for showing the settings dialogs
	 */
	private OnClickListener penMode = new OnClickListener() {
		public void onClick(View v) {
			int mode, viewOn, viewOff;
			Button bOn, bOff;
			
			if (v.getId() == mPenBtn.getId()) {
				mode = CanvasView.PEN_MODE; bOn = mPenBtn; bOff = mEraserBtn;
				viewOn = AbstractSettingView.PEN_SETTING_VIEW; viewOff = AbstractSettingView.ERASER_SETTING_VIEW; 
			}
			else {
				mode = CanvasView.ERASER_MODE; bOn = mEraserBtn; bOff = mPenBtn;
				viewOn = AbstractSettingView.ERASER_SETTING_VIEW; viewOff = AbstractSettingView.PEN_SETTING_VIEW; 					
			}
			
			mCanvasView.changeModeTo(mode);

			if (bOn.isSelected()) {
				if (mSettingView.isShown(viewOn))  mSettingView.closeView();
				else                               mSettingView.showView(viewOn);
			}
			else {
				bOn.setSelected(true);
				bOff.setSelected(false);

				if (mSettingView.isShown(viewOff)) mSettingView.closeView();
			}				
		}
	};

	/*
	 * Pen settings callbacks
	 */
	private AbstractSettingView.SettingChangeListener settingChangeListener = new SettingChangeListener() {
		public void onPenTypeChanged(int type) {
			if (type == PenSettingInfo.PEN_TYPE_SOLID)			Log.d(TAG, "Pen Type = Brush");
			else if (type == PenSettingInfo.PEN_TYPE_PENCIL)	Log.d(TAG, "Pen Type = Pencil");
			else if (type == PenSettingInfo.PEN_TYPE_SOLID)		Log.d(TAG, "Pen Type = Solid");
			else if (type == PenSettingInfo.PEN_TYPE_MARKER)	Log.d(TAG, "Pen Type = Marker");
		}

		public void onColorChanged(int rgb)			{ Log.d(TAG, "Pen RGB Color = (" + Color.red(rgb) + ", " + Color.green(rgb) + ", " + Color.blue(rgb) + ')'); }
		public void onClearAll()					{ Log.d(TAG, "Canvas Clear All!"); }
		public void onEraserWidthChanged(int width)	{ Log.d(TAG, "Eraser Width = " + width); }
		public void onPenAlphaChanged(int alpha)	{ Log.d(TAG, "Pen Alpha Color = " + alpha); }
		public void onPenWidthChanged(int width)	{ Log.d(TAG, "Pen Width = " + width); }
	};

    /*
     * Changing backgrounds.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
       	super.onOptionsItemSelected(item);
       	
       	bgId = (bgId + 1) % 3;
       	
		Bitmap topBg    = BitmapFactory.decodeResource(getResources(), bgsCanvasTop[bgId]);
		Bitmap middleBg = BitmapFactory.decodeResource(getResources(), bgsCanvasMiddle[bgId]);
		Bitmap bottomBg = BitmapFactory.decodeResource(getResources(), bgsCanvasBottom[bgId]);

		mCanvasView.setBackgroundTemplete(topBg, middleBg, bottomBg);

		return true;
    }

	/*
	 * Click listener for panning
	 */
	private OnClickListener arrowBtnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == mUpBtn.getId())    { mCanvasView.panBy(  0,  10); }
			if (v.getId() == mDownBtn.getId())  { mCanvasView.panBy(  0, -10); }
			if (v.getId() == mLeftBtn.getId())  { mCanvasView.panBy( 10,   0); }
			if (v.getId() == mRightBtn.getId()) { mCanvasView.panBy(-10,   0); }
		}
	};

	/*
	 * Click listener for zooming
	 */
	private OnClickListener zoomBtnClickListener = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == mZoomInBtn.getId()) {
				mCanvasView.zoomTo(mZoomValue += 0.2);
				if (mZoomValue > 50) mZoomValue = 50;
			}
			else if (v.getId() == mZoomOutBtn.getId()) {
				mCanvasView.zoomTo(mZoomValue -= 0.2);
				if (mZoomValue < 1) mZoomValue = 1;
			}
		}
	};

    /*
     * Undo and redo are easy
     */
	private OnClickListener undoRedo = new OnClickListener() {
		public void onClick(View v) {
			if (v.getId() == mUndoBtn.getId()) { mCanvasView.undo(); }
			else                               { mCanvasView.redo(); }					

			mUndoBtn.setEnabled(mCanvasView.isUndoable());
			mRedoBtn.setEnabled(mCanvasView.isRedoable());
		}
	};

	/*
	 * Callbacks to go with undo and redo
	 */
	private CanvasView.OnHistoryChangeListener historyChangeListener = new CanvasView.OnHistoryChangeListener() {
		public void onHistoryChanged(boolean isUndoable, boolean isRedoable) {
			mUndoBtn.setEnabled(isUndoable);
			mRedoBtn.setEnabled(isRedoable);
		}
	};

    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
    }
    
	private void initButtons() {
		mPenBtn     = (Button) findViewById(R.id.penBtn);
		mEraserBtn  = (Button) findViewById(R.id.eraseBtn);
		mUndoBtn    = (Button) findViewById(R.id.undoBtn);
		mRedoBtn    = (Button) findViewById(R.id.redoBtn);
		
		mUpBtn      = (Button) findViewById(R.id.panUp);
		mDownBtn    = (Button) findViewById(R.id.panDown);
		mLeftBtn    = (Button) findViewById(R.id.panLeft);
		mRightBtn   = (Button) findViewById(R.id.panRight);

		mZoomInBtn  = (Button) findViewById(R.id.zoomIn);
		mZoomOutBtn = (Button) findViewById(R.id.zoomOut);

		mPenBtn.setSelected(true);
		mUndoBtn.setEnabled(false);
		mRedoBtn.setEnabled(false);

		setButtonListeners();
	}

	private void setButtonListeners() {
		// Handle pen properties and eraser properties
		mPenBtn.setOnClickListener(penMode);
		mEraserBtn.setOnClickListener(penMode);

		// Handle undo and redo
		mUndoBtn.setOnClickListener(undoRedo);
		mRedoBtn.setOnClickListener(undoRedo);

		mUpBtn.setOnClickListener(arrowBtnClickListener);
		mDownBtn.setOnClickListener(arrowBtnClickListener);
		mLeftBtn.setOnClickListener(arrowBtnClickListener);
		mRightBtn.setOnClickListener(arrowBtnClickListener);

		mZoomInBtn.setOnClickListener(zoomBtnClickListener);
		mZoomOutBtn.setOnClickListener(zoomBtnClickListener);
	}

	private Button mPenBtn;
	private Button mEraserBtn;
	private Button mUndoBtn;
	private Button mRedoBtn;
	
	private Button mUpBtn;
	private Button mDownBtn;
	private Button mLeftBtn;
	private Button mRightBtn;

	private Button mZoomInBtn;
	private Button mZoomOutBtn;

	private float mZoomValue = 1f;

	private int bgId = 0;

	private static final int bgsCanvasTop[] = new int[] {
		R.drawable.moon,
		R.drawable.bb_top,
		0
	};

	private static final int bgsCanvasMiddle[] = new int[] {
		0,
		R.drawable.bb_mid,
		R.drawable.dna_repeat
	};

	public static final int bgsCanvasBottom[] = new int[] {
		0,
		R.drawable.bb_base,
		0
	};
}
