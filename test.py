import requests

s = requests.Session()
# Register a throwaway user
r = s.post('http://localhost:9090/auth/register', data={
    'username': 'canceltester', 'password': 'pass', 'fullName': 'Tester',
    'email': 'test@test.com', 'phone': '1234567890', 'age': '22', 'gender': 'MALE'
})
# Login
s.post('http://localhost:9090/auth/do-login', data={'username': 'canceltester', 'password': 'pass'})
# Book a ticket
book_data = {
    'trainNo': '12301', 'journeyDate': '2026-04-22', 'classType': 'SLEEPER',
    'quotaType': 'GENERAL', 'paymentMethod': 'upi',
    'passengers[0].name': 'TestPax', 'passengers[0].age': '30', 'passengers[0].gender': 'MALE'
}
r1 = s.post('http://localhost:9090/passenger/book', data=book_data)

# If booked, find the PNR
import re
history = s.get('http://localhost:9090/passenger/booking-history').text
matches = re.findall(r'/passenger/cancel/(\d+)', history)
if matches:
    pnr = matches[0]
    print('Found PNR:', pnr)
    # Cancel it!
    r2 = s.post('http://localhost:9090/passenger/cancel/' + pnr)
    print("CANCEL STATUS:", r2.status_code)
    # Print error alert if present
    err = re.findall(r'alert-danger[^>]*>.*?<strong[^>]*>(.*?)</strong>', r2.text, re.DOTALL)
    if err:
        print('ERROR SEEN:', err[0].strip())
    else:
        print('No error seen! Here is the page excerpt:')
        print(r2.text[:500])
else:
    print('No PNR found. Booking may have failed.')
    print('Booking output extract:', r1.text[:500])
