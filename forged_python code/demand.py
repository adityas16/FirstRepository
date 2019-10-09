
import constants
import file_handling

from scipy.stats import norm

from utils import random_variable


def create_normal_forecast(mu,sigma):
        forecast = dict()
        r = norm(loc=mu, scale=sigma)
        d = constants.DEMAND_LOWER_BOUND
        while d<constants.DEMAND_UPPER_BOUND + 1:
            forecast[d] = r.cdf(d+constants.BIN_WIDTH/2) - r.cdf(d-constants.BIN_WIDTH/2)
            d = d + constants.BIN_WIDTH

        return random_variable(forecast)

class demand_forecast():
    forecast_by_period = dict()
    def __init__(self):
        self.forecast_by_period[1]=create_normal_forecast(91.08564545,2.4)
        self.forecast_by_period[2] = create_normal_forecast(91.07111881,2.4)



    def load_forecast_for_period(self,t):
        all_forecasts = file_handling.read_forecasts_from_csv()
        for forecast in all_forecasts:
            if int(forecast['month']) == t:
                self.forecast_by_period[t] = create_normal_forecast(forecast['prediction_curr_mu'],forecast['prediction_curr_sigma'])
                self.forecast_by_period[t+1] = create_normal_forecast(forecast['prediction_next_mu'],forecast['prediction_next_sigma'])

    def get(self,t):
        return self.forecast_by_period[t]