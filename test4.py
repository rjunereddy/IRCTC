import requests

s = requests.Session()
# Enable tatkal for 12301
admin_data = {'username': 'admin', 'password': 'admin123'}
s.post('http://localhost:9090/auth/do-login', data=admin_data)
s.post('http://localhost:9090/admin/trains/tatkal/enable/12301')

# Now switch to passenger
s = requests.Session()
s.post('http://localhost:9090/auth/do-login', data={'username': 'admin', 'password': 'admin123'}) # Admin has PASSENGER Role? No. Admin is redirected to Admin.
s.post('http://localhost:9090/auth/register', data={
    'username': 'canceltester3', 'password': 'pass', 'fullName': 'Tester',
    'email': 'test@test.com', 'phone': '1234567890', 'age': '22', 'gender': 'MALE'
})
s.post('http://localhost:9090/auth/do-login', data={'username': 'canceltester3', 'password': 'pass'})

import datetime
tomorrow = (datetime.datetime.now() + datetime.timedelta(days=1)).strftime('%Y-%m-%d')
print("Tomorrow is:", tomorrow)

# Let's search!
r = s.post('http://localhost:9090/passenger/search', data={
    'source': 'SBC', 'destination': 'MAS', 'date': tomorrow, 'classType': 'SLEEPER', 'quotaType': 'TATKAL'
})
print("SEARCH RESULT HTML LENGTH:", len(r.text))

# Let's book!
book_data = {
    'trainNo': '12301', 'journeyDate': tomorrow, 'classType': 'SLEEPER',
    'quotaType': 'TATKAL', 'paymentMethod': 'upi',
    'passengers[0].name': 'TestPax', 'passengers[0].age': '30', 'passengers[0].gender': 'MALE'
}
r1 = s.post('http://localhost:9090/passenger/book', data=book_data)
print("BOOK RESULT:", r1.status_code)

import re
history = s.get('http://localhost:9090/passenger/booking-history').text
matches = re.findall(r'/passenger/cancel/(\d+)', history)
if matches:
    pnr = matches[-1] # LATEST
    print('Found PNR:', pnr)
    # Cancel it
    r2 = s.post('http://localhost:9090/passenger/cancel/' + pnr)
    print("CANCEL STATUS:", r2.status_code)
    err = re.findall(r'alert-danger[^>]*>.*?<strong[^>]*>(.*?)</strong>', r2.text, re.DOTALL)
    if err:
        print('ERROR SEEN DURING CANCEL:', err[0].strip())
    else:
        print('CANCEL SUCCESS! No alert-danger seen.')
else:
    print('No PNR found. Booking failed!')
    err = re.findall(r'alert-danger[^>]*>.*?<strong[^>]*>(.*?)</strong>', r1.text, re.DOTALL)
    if err:
        print('ERROR DURING BOOKING:', err[0].strip())
