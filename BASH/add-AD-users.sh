#!/bin/bash
# Add a user to the TALON cluster


# User list on file
talon_users=/etc/talon-users
echo "Reading list of Talon users from $talon_users..."
readarray current_users < $talon_users


# User list from AD
echo "Querying AD for users in the talon-users group..."
ad_users=(`net ads search "(SAMAccountName=talon-users)" -P |& grep "member" | cut -d: -f2 | cut -d= -f2 | cut -d, -f1 | sort | sed 's/ /./g'`)

# Slurm accounts
echo "Querying SLURM for the current department names..."
slurm_accts=(`sacctmgr show account --noheader | awk '{print $3}'`)


# For every user listed in AD, search the local list
for i in ${ad_users[@]}; do
        found=0
        name=" "
        for j in ${current_users[@]}; do
                if [[ $i == $j ]]; then
                        found=1
                        echo "Found $i"
                        break
                fi
        done

        if [ $found -eq 1 ] ; then
                continue
        else
                echo "Did not find $i. Adding...."

                # Take out the periods
                name=`echo $i | sed 's/\./ /g'`

                # Search AD for the name
                username=`net ads search "(&(objectCategory=user)(name=$name))" -P |& grep sAMAccountName | cut -d: -f2`

                # Take out any spaces
                username=`echo $username | sed 's/ //g'`

                # Append @georgiasouthern.edu to the username
                emailaddress=$username"@georgiasouthern.edu"

                # This query doesn't work for all users (i.e. Talon User) but works for faculty & students
                #emailaddress=`net ads search "(&(objectCategory=user)(name=$name))" -P |& grep mailAlias`

                # Get their department from the first OU listed
                dept=`net ads search "(SAMAccountName=talon-users)" -P |& grep "$name" | cut -d: -f2 | cut -d= -f3 | cut -d, -f1`

                # Output results
                echo "$name has username $username, email address is $emailaddress under the $dept department."

                # Print out SLURM departments and ask which one to assign the user to
                # FUTURE: Auto assign based on $dept variable
                echo "Here are the SLURM accounts/departments: "
                echo "${slurm_accts[@]}"
                echo " "
                echo "Which account do you want to assign to $username? "
                read slurm_account

                # Testing vars
                #slurm_account="ceit"
                #read -rsp $'Press any key to continue...\n' -n1 key

                # Add accounts to part1 and train
                echo -e "Adding user account for $username to Slurm group $slurm_account in talon-part1 and talon-train\n"
                sacctmgr -i add user $username account=$slurm_account part=talon-train
                sacctmgr -i modify user $username where part=talon-train set grpcpus=10

                sacctmgr -i add user $username account=$slurm_account part=talon-part1
                sacctmgr -i modify user $username where part=talon-part1 set grpcpus=100

                # Make directories and copy default BASH configs
                echo -e "Making scratch, work and home directories for $username\n"
                #mkdir /scratch2/$username
                mkdir /work/$username
                mkdir /home/$username
                cp -rT /etc/skel /home/$username

                # Create symlinks
                echo "Linking /work/$username to /home/$username/work"
                ln -s /work/$username /home/$username/work

                # Give them a sample script
                echo -e "Putting a sample script in /work/$username"
                cp /usr/local/bin/sample.sh /work/$username

                # Ensure permissions are correct
                echo "Changing UGO permissions for scratch, work and home for $username"
                #chmod 700 /scratch2/$username
                chmod 700 /work/$username
                chmod 700 /home/$username

                # Ensure permissions are correct
                echo -e "Changing UID/GUID permissions for /scratch and /work for $username\n"
                #chown -R $username:talon-users /scratch2/$username
                chown -R $username:talon-users /work/$username
                chown -R $username:talon-users /home/$username

                # Set quotas
                echo -e "Setting quota on home for $username\n"
                ssh storage-2-1 "edquota -p cecombs -u $username -f /home"

                # This needs to be updated once we fix quotas on /work
                #echo -e "Setting quota on work for $username\n"
                #ssh storage-2-1 "edquota -p cecombs -u $username -f /work"

                # Update email aliases
                echo -e "Setting users' email account in aliases\n"
                echo "$username: $emailaddress" >> /etc/aliases
                newaliases

                # Update local user list
                echo "Adding $i to $talon_users..."
                echo $i >> $talon_users

                # Send out notification email
                echo -e "Added AD user $name to the Talon Cluster. Info:\nUsername: $username\nSLURM Acct: $slurm_account\nEmail: $emailaddress" | mail -s "User Added: $name" root@georgiasouthern.edu



                # How to change department - must delete association and create new one
                # sacctmgr delete user name=$username part=talon-train account=$slurm_account
                # sacctmgr delete user name=$username part=talon-part1 account=$slurm_account

                # sacctmgr -i add user $username account=$slurm_account part=talon-train
                # sacctmgr -i modify user $username where part=talon-train set grpcpus=10

                # sacctmgr -i add user $username account=$slurm_account part=talon-part1
                # sacctmgr -i modify user $username where part=talon-part1 set grpcpus=100

        fi
done