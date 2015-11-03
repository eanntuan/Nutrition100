nuttask
=======

AMT task for nutrition recordings

To run as production:
$> NODE_ENV=production node bin/www

To run with debug logs for third-party modules:
$ DEBUG=* NODE_ENV=production node bin/www

To run with debug logs for socket.io:
$ DEBUG=socket.*,engine.* NODE_ENV=production node bin/www

To save debug logs from console
$ DEBUG=socket.*,engine.* DEBUG_FD=3 node bin/www 3> logs/debug.log


Ractive error:
Ractive.js: Failed to compute "${isRecording(0)}"
log.js:73TypeError: _0 is not a function
    at eval (eval at Fn (https://cdnjs.cloudflare.com/ajax/libs/ractive/0.7.2/ractive.min.js:3:5885), <anonymous>:1:8)
    at $d.createEvaluator.n.getter (https://cdnjs.cloudflare.com/ajax/libs/ractive/0.7.2/ractive.min.js:6:24476)
    
log.js:69Ractive.js: Failed to compute "${and(recording,!isRecording(0))}"
log.js:73TypeError: _2 is not a function
    at eval (eval at Fn (https://cdnjs.cloudflare.com/ajax/libs/ractive/0.7.2/ractive.min.js:3:5885), <anonymous>:1:15)
...
log.js:69Ractive.js: Failed to compute "${acceptedStateClass(0)}"

--failed to compute usually means that the function is not part of the data you passed in


Different requests for mturk preview HIT and accept HIT:
===================
Set path prefix: nuttask/
Sending uttData
Requested url: /?dataIndex=9&assignmentId=ASSIGNMENT_ID_NOT_AVAILABLE&hitId=3P0I4CQYVYCYL69E5VFJUBBIVCSWOJ
Request referer: https://workersandbox.mturk.com/mturk/preview?groupId=3V48FT8911185AXMF8N7YXKY3E0MJ5
GET /?dataIndex=9&assignmentId=ASSIGNMENT_ID_NOT_AVAILABLE&hitId=3P0I4CQYVYCYL69E5VFJUBBIVCSWOJ 200 39.471 ms - 12667
Connected to client socket
Recording to dir: /data/sls/scratch/psaylor/nutrecordings
Created new recordings dir at /data/sls/scratch/psaylor/nutrecordings/r_602FCt7pi71A4Ub

Set path prefix: nuttask/
Sending uttData
Requested url: /?dataIndex=9&assignmentId=3YDTZAI2WYL43VHNFPHQC7A1WU941Z&hitId=3P0I4CQYVYCYL69E5VFJUBBIVCSWOJ&workerId=AN8Z1AU9OHR56&turkSubmitTo=https%3A%2F%2Fworkersandbox.mturk.com
Request referer: https://workersandbox.mturk.com/mturk/accept?hitId=3P0I4CQYVYCYL69E5VFJUBBIVCSWOJ&prevHitSubmitted=false&prevRequester=SLS+8&requesterId=A18F23JJPMOONU&prevReward=USD0.10&hitAutoAppDelayInSeconds=604800&groupId=3V48FT8911185AXMF8N7YXKY3E0MJ5&signature=erqbOb49IvLoDHIytXGYB7rHEuk%3D&%2Faccept.x=30&%2Faccept.y=16
GET /?dataIndex=9&assignmentId=3YDTZAI2WYL43VHNFPHQC7A1WU941Z&hitId=3P0I4CQYVYCYL69E5VFJUBBIVCSWOJ&workerId=AN8Z1AU9OHR56&turkSubmitTo=https%3A%2F%2Fworkersandbox.mturk.com 200 56.292 ms - 12667
Connected to client socket
Recording to dir: /data/sls/scratch/psaylor/nutrecordings
Created new recordings dir at /data/sls/scratch/psaylor/nutrecordings/r_6024ujeQU6bMAk9


---------------------


for dev/prod modes, need to change some code in the client side js,
paritcularly for clientSocket the path it looks for socketio at
could have a main-dev and main-prod with almost the same configuration and then in index.html have ractive variable that chooses between those two main files
