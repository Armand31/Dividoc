Zipped files are encrypted using AES algorithm. The key and the initialization vector that are used for AES encryption and decryption are encrypted with RSA. Here are the commands to decrypt the zipped files, using the OpenSSL library.

First, you need the OpenSSL library that you can download at [https://www.openssl.org/source/](https://www.openssl.org/source/).

## Decrypt the AES key using RSA

Encrypted RSA key is located in the <zipFileName>.key file

In order to decrypt the key, you can use the following command :

```console
openssl rsautl -decrypt -in <zipFileName>.key -inkey <RSAPrivateKey>.pem
```

You will be prompted for a password. You can find this password in the RSA_passphrase.txt file.

The same command is needed for the initialization vector, which is named \<zipFileName\>_IV.txt :

```console
openssl rsautl -decrypt -in <zipFileName>_IV.txt -inkey <RSAPrivateKey>.pem
```

Note : You can save the ouput using the -out flag.

## Decrypt zip file using AES

You can now decrypt the zipped file with the AES key and initialization vector :

```console
openssl aes-256-cbc -d -K <AESKey> -iv <IV> -in <zipFileName>.enc -out <decryptedZipFileName>.zip
```
