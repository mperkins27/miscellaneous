#!/bin/bash
# Add a user to the TALON cluster via AD

module load system/openldap-2.4.44

tmpfile=/tmp/useradd.$$

echo "what is the user's username?"
read username

echo "what is the user's email address?"
read emailaddress

echo "what slurm group should this user be in?"
read slurm_account

echo "what is the user's first name?"
read firstname

echo "what is the user's last name?"
read lastname

echo "what is the user's password?"
read password

next_uid=`getent passwd | awk -F: '($3>600) && ($3<20000) && ($3>maxuid) { maxuid=$3; } END { print maxuid+1; }'`
encpass=`/openldap/openldap-2.4.44/sbin/slappasswd -h {MD5} -s $password`

cat <<EOF>$tmpfile
dn: cn=$username,ou=Users,dc=talon-hn,dc=georgiasouthern,dc=edu
objectClass: top
objectClass: posixAccount
objectClass: inetOrgPerson
givenName: $firstname
sn: $lastname
cn: $username
userPassword: $encpass
loginShell: /bin/bash
homeDirectory: /home/$username
gidNumber: 10000
uid: $username
uidNumber: $next_uid
EOF

ldapadd -x -W -D "cn=root,dc=talon-hn,dc=georgiasouthern,dc=edu" -f $tmpfile

echo -e "Adding user account for $username to Slurm group $slurm_account in talon-part1 and talon-train\n"
sacctmgr -i add user $username account=$slurm_account part=talon-train
sacctmgr -i modify user $username where part=talon-train set grpcpus=10

sacctmgr -i add user $username account=$slurm_account part=talon-part1
sacctmgr -i modify user $username where part=talon-part1 set grpcpus=100

echo -e "Making scratch, work and home directories for $username\n"
#mkdir /scratch2/$username
mkdir /work/$username
mkdir /home/$username
cp -rT /etc/skel /home/$username

#chmod 700 /scratch2/$username
chmod 700 /work/$username
chmod 700 /home/$username

echo -e "Putting the Sample in their Work Directory"
cat <<EOF >/work/$username/sample.sh
#!/bin/bash
##SBATCH --mail-user=$email
#SBATCH --mail-type=END
#SBATCH --nodes=2
#SBATCH -p talon-train

echo "This sample completed successfully!"
sleep 1m
EOF

echo -e "Changing permissions for /scratch and /work for $username\n"
#chown -R $username:talon-users /scratch2/$username
chown -R $username:talon-users /work/$username
chown -R $username:talon-users /home/$username

echo -e "Setting quota on home for $username\n"
ssh storage-2-1 "edquota -p cecombs -u $username -f /home"

# This needs to be updated once we fix quotas on /work
#
#echo -e "Setting quota on work for $username\n"
#ssh storage-2-1 "edquota -p cecombs -u $username -f /work"

echo -e "Setting users' email account in aliases\n"
echo "$username: $emailaddress" >> /etc/aliases

newaliases

rm -rf $tmpfile

echo "Added $emailaddress to the Talon Cluster" | mail -s "User Added: $emailaddress" crts@georgiasouthern.edu
