import requests
import re
import datetime

s = requests.Session()
s.post('http://localhost:9090/auth/register', data={'username': 'canceluser', 'password': 'p', 'fullName': 'T', 'email': 't@t.com', 'phone': '1231231231', 'age': '22', 'gender': 'MALE'})
s.post('http://localhost:9090/auth/do-login', data={'username': 'canceluser', 'password': 'p'})

s_admin = requests.Session()
s_admin.post('http://localhost:9090/auth/do-login', data={'username': 'admin', 'password': 'admin123'})
s_admin.post('http://localhost:9090/admin/trains/tatkal/enable/16231')

tomorrow = (datetime.datetime.now() + datetime.timedelta(days=1)).strftime('%Y-%m-%d')
print('DATE:', tomorrow)

r = s.post('http://localhost:9090/passenger/search', data={'source': 'SBC', 'destination': 'MYS', 'date': tomorrow, 'classType': 'SLEEPER', 'quotaType': 'TATKAL'})

book_data = {'trainNo': '16231', 'journeyDate': tomorrow, 'classType': 'SLEEPER', 'quotaType': 'TATKAL', 'paymentMethod': 'upi', 'passengers[0].name': 'TestPax', 'passengers[0].age': '30', 'passengers[0].gender': 'MALE'}
r1 = s.post('http://localhost:9090/passenger/book', data=book_data)

history = s.get('http://localhost:9090/passenger/booking-history').text
matches = re.findall(r'/passenger/cancel/(\d+)', history)
if matches:
    pnr = matches[-1]
    print('Found PNR:', pnr)
    r2 = s.post('http://localhost:9090/passenger/cancel/' + pnr)
    err = re.findall(r'alert-danger[^>]*>.*?<span[^>]*>(.*?)</span>', r2.text, re.DOTALL)
    if err:
        print('ERROR DURING CANCEL:', err[0].strip())
    else:
        print('CANCEL SUCCESS!')
else:
    print('No PNR found. Booking failed!')
    err = re.findall(r'alert-danger[^>]*>.*?<span[^>]*>(.*?)</span>', r1.text, re.DOTALL)
    if err:
        print('ERROR BOOKING:', err[0].strip())
