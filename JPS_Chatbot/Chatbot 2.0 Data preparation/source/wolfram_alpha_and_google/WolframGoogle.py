# Our wolfram alpha user id is G5WRTA-882W6ATLGU
import json
from pprint import pprint
import wolframalpha
from googleapiclient.discovery import build
from serpwow.google_search_results import GoogleSearchResults
import json
import requests
from bs4 import BeautifulSoup
import time
import re
import urllib.parse
from urllib.parse import urlparse


# For the google search api, we followed the https://linuxhint.com/google_search_api_python/
# Google customized search ID: 3499ed51db04db22e
# Google customized search API key: AIzaSyB4J9XxLeCeKdVm9Vs0fcihWhaQBV7jpjI
# ========================== comment ========================
# The Google official api does a decent job, however, it does not return the KG result
# As a result, we tried to directly scrape the google result page ...

# To conclude, the direct scraping work fine.




class WolframGoogle:
    def __init__(self):
        # ================= setup wolfram alpha ==============
        self.wolfram_app_id = 'G5WRTA-882W6ATLGU'
        self.wolframalpha_client = wolframalpha.Client(self.wolfram_app_id)
        # ================= setup google api =================
        self.google_api_key = "AIzaSyB4J9XxLeCeKdVm9Vs0fcihWhaQBV7jpjI"
        self.google_cse_id = "3499ed51db04db22e"
        self.google_service = build("customsearch", "v1", developerKey=self.google_api_key)
        self.serpwow = GoogleSearchResults(self.google_api_key)
        self.object_template = {"head": {"vars": ["v"]}, "results": {"bindings": []}}

    # res = client.query('stability of FMOC amide + zinc')

    def get_result_from_google_directly(self, question):
        # query = question.replace(' ', '+')
        URL = f"https://google.com/search?q={question}"
        USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:65.0) Gecko/20100101 Firefox/65.0"
        MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, " \
                            "like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36 "
        headers = {"user-agent": USER_AGENT}
        resp = requests.get(URL, headers=headers)
        print(resp.status_code)
        if resp.status_code == 200:
            soup = BeautifulSoup(resp.content, "html.parser")
            div_result = soup.find_all('div', id='rso')[0]
            children_divs = div_result.findChildren()
            first_child = children_divs[0]
            first_result = first_child.findChildren()[0]
            headings = first_result.find_all('div', role='heading')
            valid_results = []
            for head in headings:
                if 'People also search for' in str(head) or ('View 10+' in str(head)):
                    pass
                else:
                    valid_results.append(head.text)
            result_div = '<br/>'.join(valid_results)
            print('====== result div ==========')
            print(result_div)

            return result_div

        else:
            # This indicates that Google does not give you a direct answer...
            return None

    def get_result_from_serpwow(self, question):
        params = {
            "q": question
        }
        result = self.serpwow.get_json(params)
        pprint(result)

    def get_result_from_google_official(self, question):
        print('----- question received for google --------', question)
        res = self.google_service.cse().list(q=question.replace('add_sign', '+'), cx=self.google_cse_id).execute()
        pprint(res)

        # for item in res['items']:
        #     pprint(item)

    def get_result_from_wolfram(self, question):
        print('----- question received --------', question)
        question = question.replace('add_sign', ' + ')
        self.object_template = {"head": {"vars": ["v"]}, "results": {"bindings": []}}
        res = self.wolframalpha_client.query(question)
        bindings = []
        counter = 0
        for pod in res.pods:
            counter = counter + 1
            for sub in pod.subpods:
                text = sub.plaintext
                if counter <= 2:
                    bindings.append({'v': {'value': text}})

        self.object_template['results']['bindings'] = bindings
        return self.object_template


#    def get_result_from_google(self):

# gs = WolframGoogle()
# gs.get_result_from_google_official('what is the boiling point of water')
# gs.get_result_from_serpwow('what is the boiling point of water')

# test_question_set = ['what is the boiling point of water', 'chemical structure of benzene', 'capital of saudi arabia']
# for q in test_question_set:
#     get_result_from_google_directly(q)
#     time.sleep(5)
#     print('\n ================== \n')


# wf = WolframGoogle()
# r = wf.get_result_from_wolfram('stability of FMOC amide + zinc')
# print(r)
