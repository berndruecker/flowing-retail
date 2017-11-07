var request = require('request');

var baseUrl = process.env.ENGINE_URL || 'http://localhost:8092/rest/engine/default/';
var workerId = "customerCreditWorker"

poll();

function poll() {
  request.post({
    headers: {'content-type' : 'application/json'},
    url: baseUrl + 'external-task/fetchAndLock',
    body: `{ 
        "workerId":"`+workerId+`",
        "maxTasks":100,
        "usePriority":true,
        "topics":
            [{
              "topicName": "customer-credit",
              "lockDuration": 10000,
              "variables": ["payload"]
            }]
        }`
    }, function (error, response, body) {
      if (!error) {
          var tasks = JSON.parse(body);
          if (tasks.length==0) {
            process.stdout.write(".");            
          }
          for (index = 0; index < tasks.length; ++index) {
            execute(tasks[index]);
          }
          setTimeout(function() {
               poll();
          }, 50);
       }     
  });
}

function execute(externalTask) {
  console.log('[Customer Credit] succeeded for #%s', externalTask.processInstanceId);
  complete(externalTask);
}

function complete(externalTask) {
  var remainingAmount = 0;
  if (Math.random() > 0.5) {
    remainingAmount = 15;   
  }

  request.post({
    headers: {'content-type' : 'application/json'},
    url: baseUrl + 'external-task/' + externalTask.id + "/complete",
    body: `{ 
        "workerId":"`+workerId+`",
        "variables":
            {
              "remainingAmount": {"value": `+remainingAmount+`, "type": "integer"}
            }
        }`
    }, function (error, response, body) {
      if (error) {
        console.log(response);
      }     
  });
}