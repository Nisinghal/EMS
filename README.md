
<h1>An EMS Android application for Innovaccer</h1>

Given the visitors that there are in an office and outside, there is a need to for an entry management
software. Such an application would capture the Name, email address, phone no of the visitor and
same information also needs to be captured for the host on the front end.

Elements captured
1. Name
2. Phone
3. Check-in time,
4. Check-out time,
5. Host name
6. Address visited

InnovaccerEMS asks the user to register through their Google Sign In. I chose to use Google Sign In to maintain some privacy and validity of the person, avoiding prank or duplicate entries.
Once logged in successfully, the EMS takes the user's name and mail ID automatically and asks them to enter their phone number. They also need to enter the name, mail ID and phone number of the person tehy are meeting.
On submitting, the input data along with the timestamp is recorded on Firebase Database and a mail will be sent to the person they are visiting with the visitor details. 

The next time there is a login through the same mail ID they will be shown the check out page which contains a check out button. Once clicked, the timestamp gets recorded as the check out time and a mail is sent to the visitor giving them their visit details mentioned above.

I use Firebase to incorporate the Google Sign in and to store data. 
