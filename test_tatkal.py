import requests

s = requests.Session()
# Login as admin
admin_data = {'username': 'admin', 'password': 'admin123'}
r = s.post('http://localhost:9090/auth/do-login', data=admin_data)

# Test Tatkal Enabling
r_tatkal = s.post('http://localhost:9090/admin/trains/tatkal/enable/12301')
print("TATKAL RESULT:", r_tatkal.status_code)
print("TATKAL HTML:", r_tatkal.text[:500])

# Just scrape the trains table to see
manage = s.get('http://localhost:9090/admin/trains')
print("MANAGE TRAINS HTML length:", len(manage.text))
import re
print("Has 12301 enabled?", "12301" in manage.text)

