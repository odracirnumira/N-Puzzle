package es.odracirnumira.npuzzle.view;

import es.odracirnumira.npuzzle.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

public class PuzzleSolutionControlsView extends FrameLayout {

	public PuzzleSolutionControlsView(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.initView();
	}

	private void initView() {
		this.addView(((LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.puzzle_solution_controls_view,
				this, false));
	}

}
