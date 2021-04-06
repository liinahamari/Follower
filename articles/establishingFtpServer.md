Base on article [How To Set Up vsftpd for a User's Directory on Ubuntu 18.04](https://www.digitalocean.com/community/tutorials/how-to-set-up-vsftpd-for-a-user-s-directory-on-ubuntu-18-04)

1. Create new droplet with Ubuntu 18.04 on board;
2.
```
sudo apt update && sudo apt upgrade
sudo apt install vsftpd
sudo cp /etc/vsftpd.conf /etc/vsftpd.conf.orig
sudo ufw enable
sudo ufw allow 20/tcp
sudo ufw allow 21/tcp
sudo ufw allow 990/tcp
sudo ufw allow 40000:50000/tcp
sudo ufw status
sudo adduser ftp_user
sudo mkdir /home/ftp_user/ftp
sudo chown nobody:nogroup /home/ftp_user/ftp
sudo chmod a-w /home/ftp_user/ftp

// optional -- for testing\verification purposes
// sudo ls -la /home/ftp_user/ftp

sudo mkdir /home/ftp_user/ftp/files
sudo chown ftp_user:ftp_user /home/ftp_user/ftp/files

// optional -- for testing purposes
// echo "vsftpd test file" | sudo tee /home/ftp_user/ftp/files/test.txt

```

3. in /etc/vsftpd.conf:
```
anonymous_enable=NO
local_enable=YES
write_enable=YES
chroot_local_user=YES
user_sub_token=$USER
local_root=/home/$USER/ftp
pasv_min_port=40000
pasv_max_port=50000
userlist_enable=YES
userlist_file=/etc/vsftpd.userlist
userlist_deny=NO

```

4.
```echo "ftp_user" | sudo tee -a /etc/vsftpd.userlist
sudo systemctl restart vsftpd
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/ssl/private/vsftpd.pem -out /etc/ssl/private/vsftpd.pem 
// enter credentials for certificate
```

5. in /etc/vsftpd.conf comment lines:
``` 
 # rsa_cert_file=/etc/ssl/certs/ssl-cert-snakeoil.pem
 # rsa_private_key_file=/etc/ssl/private/ssl-cert-snakeoil.key
```

and add new ones:
```
rsa_cert_file=/etc/ssl/private/vsftpd.pem
rsa_private_key_file=/etc/ssl/private/vsftpd.pem
```

then change:
```
ssl_enable=YES
```
and add below:
```
allow_anon_ssl=NO
force_local_data_ssl=YES
force_local_logins_ssl=YES
ssl_tlsv1=YES
ssl_sslv2=NO
ssl_sslv3=NO
require_ssl_reuse=NO
ssl_ciphers=HIGH
```

6. ```sudo systemctl restart vsftpd```
