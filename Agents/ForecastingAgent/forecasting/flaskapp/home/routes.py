# The purpose of this module is to print available HTTP requests (i.e. routes)
# at the application root
from flask import Blueprint, jsonify


# Blueprint Configuration
home_bp = Blueprint(
    'home_bp', __name__
)

# Show an instructional message at the app root
@home_bp.route('/', methods=['GET'])
def default():
    msg = 'Forecasting agent: \n'
    msg += 'The forecasting agent can be used to forecast a time series. \n'
    msg += 'The following parameters are required: \n'
    msg += 'dataIRI: the IRI of the time series to be forecasted \n'
    msg += 'horizon: the number of steps to forecast \n'
    msg += 'Checkout the README.md for more information.'


    res = {'status': '200', 'msg': msg}
    return jsonify(res)