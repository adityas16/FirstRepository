import demand
import constants
import numpy as np
from file_handling import *
import functools



demand_forecast = demand.demand_forecast()
observed_demands = read_observed_demands()

def backorder_cost(x):
    return max(0,3 * x * -1)

def holding_cost(x):
    if x<=0:
        return 0
    elif x <= 90:
        return x
    else:
        return 90 + (x-90) * 2

def inventory_cost(x):
    return backorder_cost(x) + holding_cost(x)

@functools.lru_cache()
def L(t,x):
    ''' Expected cost incirred in period t if inventory at the beginning of the period is x '''
    current_period_demand = demand_forecast.get(t)
    period_end_inventory = current_period_demand.subtract_from(x)
    return period_end_inventory.expect(inventory_cost)

def C(t,y):
    ''' Expected cost from period t+1 to the last period if the inventory position at the
     beginning of period n is y'''
    current_period_demand = demand_forecast.get(t)

    if t == constants.LAST_PERIOD:
        return 0
    else:
        x_t_plus_1 = current_period_demand.subtract_from(y)
        return x_t_plus_1.expect(lambda x: L(t+1, x))

def myopic_model():
    x_t = constants.STARTING_INVENTORY
    total_cost = 0
    results = []
    for t in range(constants.START_PERIOD,constants.LAST_PERIOD+1):
        demand_forecast.load_forecast_for_period(t)
        c = []
        y = constants.INVENTORY_POSITION_LOWER_BOUND
        while y < constants.INVENTORY_POSITION_UPPER_BOUND + 1:
            c = c + [C(t, y)]
            y = y +constants.BIN_WIDTH
        c = np.array(c)
        y_t_star = c.argmin() * constants.BIN_WIDTH + constants.INVENTORY_POSITION_LOWER_BOUND


        order = max(0,y_t_star - x_t)

        x_t_end = x_t - observed_demands[t]
        holding_cost_t = holding_cost(x_t_end)
        backorder_cost_t = backorder_cost(x_t_end)
        cost_t = holding_cost_t + backorder_cost_t
        print(x_t, order, backorder_cost_t, holding_cost_t , cost_t)
        results = results + [[x_t, order, backorder_cost_t, holding_cost_t , cost_t,y_t_star]]
        x_t = x_t_end + order
        #Not counting the cost incurred in first period because that is not controlled by the policy
        if(t == constants.START_PERIOD):
            continue
        total_cost = total_cost + cost_t
    print("Mean cost per month (excluding first month) = " , total_cost/(constants.LAST_PERIOD+1 - constants.START_PERIOD))
    write_results(results)