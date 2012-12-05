#!/usr/bin/env python
#
# Copyright (C) 2012 KLab Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -*- coding: utf-8 -*-

import cgi
import webapp2
import binascii
from Crypto.Cipher import AES
from google.appengine.api import urlfetch

HCTYPE    = 'Content-Type'
TYPEPLAIN = 'text/plain'
TYPEHTML  = 'text/html; charset=utf-8'
APIKEY    = '**API_KEY**'

class MyMain(webapp2.RequestHandler):
	def get(self):
		self.response.headers[HCTYPE] = TYPEPLAIN

class MySend(webapp2.RequestHandler):
	def get(self):
		self.response.headers[HCTYPE] = TYPEPLAIN
		self.response.out.write('???');

	def post(self):
		id = cgi.escape(self.request.get('id'))
		pw = cgi.escape(self.request.get('pass'))
		if len(id) <= 0 or len(pw) <= 0:
			self.get()
			return
		key = fix_key(pw)
		iv = key[::-1]
		id_plain = decrypt_id(key, iv, id)
		if len(id_plain) <= 0:
			self.get()
			return
		self.response.headers[HCTYPE] = TYPEHTML
		self.response.out.write('<html><head><title>remotewand</title></head><body>')
		self.response.out.write(send_msg(id_plain))
		self.response.out.write('</body></html>')

app = webapp2.WSGIApplication([('/', MyMain), ('/push01', MySend)], debug=True)

def fix_key(keyraw):
	key = keyraw;
	keylen = len(key)
	if keylen < 16:
		num = 0;
		for var in range(keylen, 16):
			key += str(num);
			num += 1;
			if num >= 10:
				num = 0
	else:
		key = key[0:16]
	return key

def decrypt_id(key, iv, id):
	dec = AES.new(key, AES.MODE_CBC, iv)
	try:
		id_bin = binascii.unhexlify(id);
	except TypeError:
		return ""
	id_plain = dec.decrypt(id_bin)
	p = len(id_plain)
	while p > 0:
		c = ord(id_plain[p-1])
		if c >= 0x20 and c <= 0x7e:
			break;
		p -= 1
	return id_plain[0:p]

def send_msg(regid):
	url = 'https://android.googleapis.com/gcm/send'
	body = 'registration_id=' + regid + \
			'&collapse_key=remotewand_msg&data.action=camera&time_to_live=3600&delay_while_idle=0'
	try:
		result = urlfetch.fetch(url=url,
				payload=body,
				method=urlfetch.POST,
				headers={'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
						'Authorization': 'key=' + APIKEY})
	except:
		return 'failed to fetch [' + url + ']'
	if result.content.startswith('id='):
		return str(result.status_code) + ' SUCCESS'
	else:
		return str(result.status_code) + ' ' + result.content

