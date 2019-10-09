import utils
import demand
import matplotlib.pylab as plt
import constants

import unittest

import model
import numpy as np


class UtilsTest(unittest.TestCase):
    def setUp(self):
        self.X = utils.random_variable({0: 0.5, 1: 0.5})

    def test_expectation(self):
        self.assertTrue(self.X.expect(lambda x:x*2) == 1)

    def test_expectation(self):
        self.assertTrue(self.X.subtract_from(0.5).expect(lambda x:x*2) == 0)

    # def test_plot_demand(self):
    #     f = demand.create_normal_forecast(91.08564545,4.073239579).dist
    #     lists = sorted(f.items())  # sorted by key, return a list of tuples
    #     x, y = zip(*lists)  # unpack a list of pairs into two tuples
    #
    #     plt.plot(x, y)
    #     plt.show()
    #     plt.close()
    #
    # def test_L(self):
    #     l = []
    #     for x in range(constants.INVENTORY_LEVEL_LOWER_BOUND, constants.INVENTORY_LEVEL_UPPER_BOUND + 1):
    #         l = l + [model.L(1,x)]
    #     l=np.array(l)
    #     print("Cost minimized at : ", l.argmin() + constants.INVENTORY_LEVEL_LOWER_BOUND)
    #     plt.plot(l)

    # def test_C(self):
    #     c = []
    #     for y in range(constants.INVENTORY_POSITION_LOWER_BOUND, constants.INVENTORY_POSITION_UPPER_BOUND + 1):
    #         c = c + [model.C(1, y)]
    #     c = np.array(c)
    #     print("Cost minimized at : ", c.argmin() + constants.INVENTORY_POSITION_LOWER_BOUND)
    #     plt.plot(c)
    #     plt.show()

    def test_myopic_policy(self):
        model.myopic_model()

if __name__ == '__main__':
    unittest.main(argv=['first-arg-is-ignored'], exit=False)