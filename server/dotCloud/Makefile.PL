use strict;
use warnings;
use ExtUtils::MakeMaker;

WriteMakefile(
    ($ExtUtils::MakeMaker::VERSION >= 6.3002
      ? ('LICENSE'=> 'perl')
      : ()),
    PL_FILES            => {},
    PREREQ_PM => {
        'Plack'      => 0,
        'Dancer'     => 0,
        'Crypt::CBC' => 0,
        'Crypt::Rijndael' => 0,
        'LWP::Protocol::https' => 0,
    },
);
