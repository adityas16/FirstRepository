def model1(input_map):
	n1 = input_map["n1"]
	n2 = input_map["n2"]

	data = {
    'out1'  : n1 * 2,
    'out2' : n1 + n2
    }
	return data