package main


import (
    "strings"
    "encoding/json"
    "net/http"
    "io/ioutil"
    "log"
    "fmt"
    "os"
    "os/signal"
    "flag"
    "math/rand"

    "github.com/zeebe-io/zbc-go/zbc"
    "github.com/zeebe-io/zbc-go/zbc/common"
    "github.com/zeebe-io/zbc-go/zbc/models/zbsubscriptions"
    "github.com/zeebe-io/zbc-go/zbc/services/zbsubscribe"
)

const (
    zeebeBrokerAddr = "0.0.0.0:51015"
    port = "8100"
)
var (
    zbClient *zbc.Client
    doDeployWorkflowModel = false
) 

func init() {
    initParameters()
    initZeebe()
}

func main() {
    startHttpServer()
}

func initParameters() {
    doDeployWorkflowModelPtr := flag.Bool("deploy", false, "-deploy")
    flag.Parse()
    doDeployWorkflowModel = *doDeployWorkflowModelPtr
}

func startHttpServer() {
    // setup REST endpoint (yay - this is not really REST - I know - but sufficient for this example)
    http.HandleFunc("/payment", handleHttpRequest)
    log.Fatal(http.ListenAndServe(":" + port, nil))
}

func initZeebe() {
	// connect to Zeebe Broker
    newClient, err := zbc.NewClient(zeebeBrokerAddr)
    if err != nil { log.Fatal( err ) }
    zbClient = newClient

    // register job handler for 'charge-credit-card' and subscribe
    subscription1, err := zbClient.JobSubscription("default-topic", "SomeWorker", "charge-credit-card",  1000, 32, hadleChargeCreditCardJob)
    if err != nil { log.Fatal(err) }
    subscription1.StartAsync()    

    // register job handler for 'deduct-customer-credit' and subscribe
    subscription2, err := zbClient.JobSubscription("default-topic", "SomeWorker", "deduct-customer-credit",  1000, 32, handleDeductCustomerCredit)
    if err != nil { log.Fatal(err) }
    subscription2.StartAsync()    

    handleInterrupt(subscription1, subscription2)

    // deploy workflow model if requested
    if (doDeployWorkflowModel) {
        deployment, err := zbClient.CreateWorkflowFromFile("default-topic", zbcommon.BpmnXml, "payment.bpmn")
        if err != nil { log.Fatal(err) }
        fmt.Println("deployed workflow model: ", deployment)
    }
}

func handleHttpRequest(w http.ResponseWriter, r *http.Request) {    
    bodyBytes, _ := ioutil.ReadAll(r.Body)
    jsonStr := string(bodyBytes)
    fmt.Println("Retrieving payment request" + jsonStr)

    err := chargeCreditCard(jsonStr)
    if (err != nil) {
        w.WriteHeader(500)     
    } else {
        w.WriteHeader(http.StatusOK)
    }
}

func chargeCreditCard(someDataAsJson string) error {
    payload := make(map[string]interface{})
	json.Unmarshal([]byte(someDataAsJson), &payload)

    instance := zbc.NewWorkflowInstance("paymentV5", -1, payload)
    workflowInstance, err := zbClient.CreateWorkflowInstance("default-topic", instance)

    if (err != nil) { 
        fmt.Println("Error: " + err.Error())
        return err;
    } else {
        fmt.Println("Started: " + workflowInstance.String())
        return nil;
    }
}

func hadleChargeCreditCardJob(client zbsubscribe.ZeebeAPI, event *zbsubscriptions.SubscriptionEvent) {
    job, err := event.GetJob()
    if err != nil { log.Fatal(err) }	
    payload, err := job.GetPayload();
    if err != nil { log.Fatal(err) }	
    jsonPayload, err := json.Marshal( payload );
    if err != nil { log.Fatal(err) }	
 
    _, err = doHttpCall(string(jsonPayload))
    if err != nil {
        // couldn't do http call, fail job to trigger retry        
        client.FailJob(event)
    } else {
        // complete job after processing
        client.CompleteJob(event)
    }	
}

func doHttpCall(someDataAsJson string) (resp *http.Response, err error) {
    fmt.Println("Doing http call", someDataAsJson)
    return http.Post("http://localhost:8099/charge", "application/json", strings.NewReader(someDataAsJson))
}

func handleDeductCustomerCredit(client zbsubscribe.ZeebeAPI, event *zbsubscriptions.SubscriptionEvent) {
    job, err := event.GetJob()
    if err != nil { log.Fatal(err) }	
    payload, err := job.GetPayload()
    if err != nil { log.Fatal(err) }

    // Hardcoded remaining amount, TODO: replace with randomized value
    if (rand.Intn(10) > 3) {
        (*payload)["remainingAmount"] = 5
        log.Println("[worker::deduct-customer-credit] Substracting from customer account - with remaining amount") 
        } else {
        (*payload)["remainingAmount"] = 0
        log.Println("[worker::deduct-customer-credit] Substracting from customer account - no remaining amount")
    }
    event.UpdatePayload(payload)

    client.CompleteJob(event)
}

/*
	Helpers
*/
func handleInterrupt(subscriptions ...*zbsubscribe.JobSubscription) {
	osCh := make(chan os.Signal, 1)
	signal.Notify(osCh, os.Interrupt)
	go func() {
		<-osCh
		for _, sub := range subscriptions {
			sub.Close()
		}

		fmt.Println("subscription closed")
		os.Exit(0)
	}()
}