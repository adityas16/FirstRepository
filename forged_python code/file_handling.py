from pathlib import Path
import csv
import constants


def read_forecasts_from_csv():
    base_path = Path(__file__).parent
    file_path = (base_path / "./forecast/two_period.csv").resolve()
    with open(file_path) as f:
        a = [{k: float(v) for k, v in row.items()} for row in csv.DictReader(f, skipinitialspace=True)]
    return a

def read_observed_demands():
    base_path = Path(__file__).parent
    file_path = (base_path / "./forecast/two_period.csv").resolve()
    observed_demands = dict()
    with open(file_path) as f:
        for row in csv.DictReader(f, skipinitialspace=True):
            observed_demands[int(row['month'])] = float(row['observed'])
    return observed_demands

def write_results(results):
    with open(constants.OUTPUT_FILE, mode='w', newline='') as f:
        writer = csv.writer(f, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        writer.writerow(['inventory_level', 'order_placed', 'backorder cost', 'holding_cost', 'total_cost'])
        for result in results:
            writer.writerow(result)