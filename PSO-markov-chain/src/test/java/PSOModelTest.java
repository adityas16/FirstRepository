import org.junit.Assert;
import org.junit.Test;

import com.aditya.research.pso.markovchain.PSOModel;
import com.aditya.research.pso.markovchain.transitions.ConstandDifferenceTransitionMatrix;
import com.aditya.research.pso.markovchain.transitions.TransitionMatrix;

public class PSOModelTest {
	PSOModel psoModel = new PSOModel();
	
	@Test
	//Score probability SHOUL BE SET TO 0.74 for this to work
	public void testFromNilsSheet(){
		Assert.assertEquals(0.5f, psoModel.getProbAWin(0, 0, 0),0.00001f);
		Assert.assertEquals(0.5716759f, psoModel.getProbAWin(1, 1, 0),0.00001f);
		Assert.assertEquals(0.8447059522f, psoModel.getProbAWin(3, 2, 0),0.00001f);
		Assert.assertEquals(0.0540959536f, psoModel.getProbAWin(5, 0, 1),0.00001f);
	}
	
	@Test
	public void testSensitivityToScoreProportion(){
		for(float diff = 0.01f;diff<=0.2f;diff = diff + 0.01f){
			TransitionMatrix tm = new ConstandDifferenceTransitionMatrix(diff, 0.74f);
			psoModel = PSOModel.customModel(tm,tm);
//			System.out.println(psoModel.getRecursiveRapidFire());
//			markovChain.transitionMatrix = new ConstandDifferenceTransitionMatrix(diff,0.74f);
//			System.out.println(probAwin(markovChain.run()));
			System.out.println(psoModel.getProbAWin(0, 0, 0));
		}
	}
}
