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
use strict;
use Dancer;
use LWP::UserAgent;
use HTTP::Request::Common qw(POST);
use Crypt::CBC;

my $reshead  = "Content-Type: text/plain\n\n";
my $ctype    = "application/x-www-form-urlencoded;charset=UTF-8";
my $apikey   = "**API_KEY**";

get '/' => sub {
    return "";
};

get '/push01' => sub {
    return "??";
};

post '/push01' => sub {
    my $id = params->{id};
    my $pass = params->{pass};

    if (length($id) <= 0 || length($pass) <= 0) {
        return "???";
    }

    my $passlen = length($pass);
    if ($passlen < 16) {
        for (my $i = 0; $i + $passlen < 16; $i++) {
            my $num = $i;
            if ($num >= 10) {
                $num -= 10;
            }
            $pass .= $num;
        }
    } elsif ($passlen > 16) {
        $pass = substr($pass, 0, 16);
    }
    my $iv = reverse($pass);

    # decrypt Registration ID of the device
    my $cipher = Crypt::CBC->new(
        -key         => $pass,
        -keysize     => 16,
        -literal_key => 1,
        -cipher      => "Crypt::Rijndael",
        -iv          => $iv,
        -header      => 'none',
    );
    $id = pack("H*", $id);
    my $regid = $cipher->decrypt($id);

    my $ua = LWP::UserAgent->new;

    # sending GCM message
    my $url = "https://android.googleapis.com/gcm/send";
    my $body = "registration_id=$regid&collapse_key=remotewand_msg&data.action=camera&time_to_live=3600&delay_while_idle=0";
    my $req = HTTP::Request->new(POST => $url,
            [Authorization => "key=" . $apikey,
            "Content-Type" => $ctype,
            "Content-Length" => length($body)],
            $body);

    my $res = $ua->request($req);
    if ($res->code == 200) {
        if ($res->content =~ /^id=/) {
            return $res->code . " SUCCESS";
        } else {
            return $res->code . " " . $res->content;
        }
    }
    return $res->status_line;
};

start;
