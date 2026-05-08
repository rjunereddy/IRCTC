import requests

s = requests.Session()
r2 = s.get('http://localhost:9090/passenger/testcancel/2604208647')
print("TEST CANCEL RAW RESULT:")
print(r2.text)
