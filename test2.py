import requests
import re

s = requests.Session()
r = s.post('http://localhost:9090/auth/register', data={
    'username': 'canceltester2', 'password': 'pass', 'fullName': 'Tester',
    'email': 'test@test.com', 'phone': '1234567890', 'age': '22', 'gender': 'MALE'
})
s.post('http://localhost:9090/auth/do-login', data={'username': 'canceltester2', 'password': 'pass'})
book_data = {
    'trainNo': '12301', 'journeyDate': '2026-04-22', 'classType': 'SLEEPER',
    'quotaType': 'GENERAL', 'paymentMethod': 'upi',
    'passengers[0].name': 'TestPax', 'passengers[0].age': '30', 'passengers[0].gender': 'MALE'
}
r1 = s.post('http://localhost:9090/passenger/book', data=book_data)

history = s.get('http://localhost:9090/passenger/booking-history').text
matches = re.findall(r'/passenger/cancel/(\d+)', history)
if matches:
    pnr = matches[-1] # LATEST
    print('Found PNR:', pnr)
    r2 = s.get('http://localhost:9090/passenger/testcancel/' + pnr)
    print("TEST CANCEL RAW RESULT:", r2.text)
else:
    print('No PNR found. Booking failed!')
