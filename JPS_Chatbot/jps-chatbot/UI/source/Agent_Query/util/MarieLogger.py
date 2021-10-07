import json
import logging
import sys
from functools import wraps
import os
import warnings
import tensorflow as tf
import numpy as np

np.warnings.filterwarnings("ignore")
# suppress other warnings
warnings.filterwarnings("ignore")
# suppress tensorflow warnings
tf.get_logger().setLevel(logging.ERROR)
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'


def MarieLog(func):
    # @wraps(func)
    def wrapper(*args, **kwargs):
        logger = logging.getLogger('Function called')
        logger.setLevel(logging.INFO)
        logger.info('{} is called with input {}'.format(func.__name__, args[1:]))
        # logger.warning('Test our own warnings')
        return func(*args)

    return wrapper


def MarieQuestionLog(func):
    def wrapper(*args, **kwargs):
        logger = logging.getLogger('Question')
        logger.setLevel(logging.INFO)
        logger.info('\n======================== starting a question =========================')
        logger.info('Processing question {} \n'.format(args[1:]))
        return func(*args)

    return wrapper


def MarieIOLog(func):
    def wrapper(*args, **kwargs):
        logger = logging.getLogger('Function I/O')
        logger.setLevel(logging.INFO)
        rst = func(*args)
        if rst is None:
            logger.warning('{} is called with input {} but returned None'.format(func.__name__, args[1:]))
        else:
            try:
                rst_string = json.dumps(rst, indent=4)
            except:
                rst_string = rst
            logger.info('{} is called with input {} and output {}'.format(func.__name__, args[1:], rst_string))
        return rst

    return wrapper
