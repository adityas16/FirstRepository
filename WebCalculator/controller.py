#Web API code for HTTP request/response processing
from flask import Flask
from flask import Flask, render_template
from flask import request, redirect
from flask import jsonify
from service import *
from flask_basicauth import BasicAuth

import time


app = Flask(__name__)

#Authorization code
app.config['BASIC_AUTH_USERNAME'] = 'user'
app.config['BASIC_AUTH_PASSWORD'] = 'password'
basic_auth = BasicAuth(app)
# protect all pages
app.config['BASIC_AUTH_FORCE'] = True



@app.route('/')
def index():
    return render_template('index.html')


@app.route('/model1', methods = ['POST'])
def postmodel1():
	input_map = request.form.to_dict()
	print(input_map)
	#Convert all input values to float from string
	input_map = {k : float(v) for k,v in input_map.items()}
	resp = jsonify(model1(input_map))
	resp.status_code = 200
	return resp

@app.route('/model1', methods = ['GET'])
def getmodel1():
    return render_template('model1.html')


if __name__ == '__main__':
    app.run()
