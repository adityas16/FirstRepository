package com.aditya.research.pso.common;

public class BinomialOutcome {
	private float AWin;

	public BinomialOutcome(float aWin) {
		super();
		AWin = aWin;
	}

	public float getAWin() {
		return AWin;
	}
	
	public float getBWin() {
		return 1 - AWin;
	}

	@Override
	public String toString() {
		return "BinomialOutcome [AWin=" + AWin + ", getBWin()=" + getBWin() + "]";
	}
	
}
