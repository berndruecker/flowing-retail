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
    "github.com/zeebe-io/zbc-go/zbc"
    "github.com/zeebe-io/zbc-go/zbc/common"
    "github.com/zeebe-io/zbc-go/zbc/models/zbsubscriptions"
    "github.com/zeebe-io/zbc-go/zbc/services/zbsubscribe"    
)

const ZeebeBrokerAddr = "0.0.0.0:51015"
const port = "9001"
var zbClient *zbc.Client;

func init() {
    initRestApi()
    initZeebe()
}

func main() {    
    // Start HTTP server
    log.Fatal(
        http.ListenAndServe(":" + port, nil))
}

func initRestApi() {
    // setup REST endpoint (yay - this is not really REST - I know - but sufficient for this example)
    http.HandleFunc("/payment", handleHttpRequest)
}

func initZeebe() {
	// connect to Zeebe Broker
    newClient, err := zbc.NewClient(ZeebeBrokerAddr)
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
        
    // deploy workflow model if requested
    if (contains(os.Args, "deploy")) {
        deployment, err := zbClient.CreateWorkflowFromFile("default-topic", zbcommon.BpmnXml, "payment.bpmn")
        if err != nil { log.Fatal(err) }
        fmt.Println("deployed workflow model: ", deployment)
    }

    // disconnect nicely 
    osCh := make(chan os.Signal, 1)
    signal.Notify(osCh, os.Interrupt)
    go func() {
        <-osCh
        err := subscription1.Close()
        if err != nil { log.Fatal(err) }

        err2 := subscription2.Close()
        if err2 != nil { log.Fatal(err2) }

        fmt.Println("Subscriptions closed.")
        os.Exit(0)
    }()
}

func handleHttpRequest(w http.ResponseWriter, r *http.Request) {    
    bodyBytes, _ := ioutil.ReadAll(r.Body)
    jsonStr := string(bodyBytes)
    fmt.Println("Retrieving payment request" + jsonStr)

    chargeCreditCard(jsonStr, w)

    w.WriteHeader(http.StatusOK)
}

func chargeCreditCard(someDataAsJson string, w http.ResponseWriter) error {
    payload := make(map[string]interface{})
	json.Unmarshal([]byte(someDataAsJson), &payload)

    instance := zbc.NewWorkflowInstance("paymentV5", -1, payload)
    workflowInstance, err := zbClient.CreateWorkflowInstance("default-topic", instance)

    if (err != nil) { 
        fmt.Fprintf(w, "Bam, error: " + err.Error())
        return err;
    } else {
        fmt.Fprintf(w, "Yeah, started: " + workflowInstance.String())
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

    log.Println(" Substracting  from customer account") // " + strconv.Itoa( (*payload)["amount"].(int) ) + "
    
    // Hardcoded remaining amount, TODO: replace with randomized value
    (*payload)["remainingAmount"] = 5
    event.UpdatePayload(payload)

    client.CompleteJob(event)
}

/* Helper to check if "deploy" was given as argument */
func contains(arr []string, e string) bool {
    for _, a := range arr {
        if a == e {
            return true
        }
    }
    return false
}