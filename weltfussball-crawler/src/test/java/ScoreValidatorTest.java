import org.junit.Assert;
import org.junit.Test;

import pso.ScoreValidator;

public class ScoreValidatorTest {
	@Test
	public void testMinGD(){
		Assert.assertEquals(true, ScoreValidator.getMinGDofStrongerTeam(1, 0, 1)<0);
		Assert.assertEquals(true, ScoreValidator.getMinGDofStrongerTeam(1, 2, 4)<0);
	}
}
