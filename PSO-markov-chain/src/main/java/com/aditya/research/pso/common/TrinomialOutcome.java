package com.aditya.research.pso.common;

public class TrinomialOutcome {
	private float AWin,tie;

	public TrinomialOutcome(float aWin, float tie) {
		super();
		AWin = aWin;
		this.tie = tie;
	}

	public float getAWin() {
		return AWin;
	}

	public float getTie() {
		return tie;
	}
	
	public float getBwin() {
		return 1 - tie - AWin;
	}
	
	public BinomialOutcome asBinomial(float pAWinIfTie){
		float pA = AWin;
		pA += tie * pAWinIfTie;
		return new BinomialOutcome(pA);
	}

	@Override
	public String toString() {
		return "TrinomialOutcome [AWin=" + AWin + ", tie=" + tie + ", getBwin()=" + getBwin() + "]";
	}
	
	
}
