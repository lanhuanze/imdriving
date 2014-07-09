package com.irefire.android.imdriving.engine;

public class ResultText implements Comparable<ResultText> {
	private int score;
	private String text;

	public ResultText() {

	}

	public ResultText(int score, String text) {
		this.score = score;
		this.text = text;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int compareTo(ResultText t) {
		if (t == null) {
			return 1;
		} else {
			int c = this.score - t.score;
			if (c != 0) {
				return c;
			}
			return this.text.compareTo(t.text);
		}
	}

}
