Identify
========

Goal
----

Generate vCards and SSL certificates for LDAP accounts.

SSL certificates are based on a standard CA cert and its key, and a public SSH key (RSA-only) for each account.

Configuration
-------------

See `src/main/resources`.

Note that the private key for the CA should not be password-protected.
If it is, you can use `openssl rsa -in protected.pem -out raw.pem`.

One can use `java -Didentify.configpath=` to use external configuration files instead of the built-in ones.

Usage
-----

    $ gradle build
    $ java -jar build/libs/identify.jar /var/lib/identify/out

Result

    $ tree
    out
    ├── 2012
    │   ├── 05
    │   │   └── 31
    │   │       ├── 143651
    │   │       │   ├── pierre.pem
    │   │       │   └── wouter.pem
    │   │       ├── 143708
    │   │       │   ├── pierre.pem
    │   │       │   └── wouter.pem
    │   └── 06
    │       └── 03
    │           └── 154039
    │               ├── pierre.pem
    │               └── wouter.pem
    ├── everybody.vcf
    ├── pierre.vcf
    └── wouter.vcf
    $ cat pierre.vcf
    BEGIN:VCARD
    FN:Pierre Carrier
    EMAIL:pierre@spotify.com
    TEL:+XXXXXXXXXXX
    TITLE:Service Reliability Engineer
    ADR;TYPE=work:;;;Stockholm;;;;
    URL:https://wiki/wiki/User:pierre
    END:VCARD
