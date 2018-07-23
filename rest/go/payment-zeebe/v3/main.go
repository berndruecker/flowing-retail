package main


import (
    "log"
    "fmt"
    "net/http"
    "strings"
    "github.com/zeebe-io/zbc-go/zbc"
    "github.com/zeebe-io/zbc-go/zbc/common"
    "github.com/zeebe-io/zbc-go/zbc/models/zbsubscriptions"
    "github.com/zeebe-io/zbc-go/zbc/services/zbsubscribe"
    "github.com/vmihailenco/msgpack"
    "os"
    "os/signal"
    "encoding/json"
)

const BrokerAddr = "0.0.0.0:51015"
const port = "9001"
var zbClient *zbc.Client;

func init() {
    initRestApi()
    initZeebe()
}

func main() {    
    // Start HTTP server
    log.Fatal(
        http.ListenAndServe(":"+port, nil))
}

func initRestApi() {
    // setup REST endpoint (yay - this is not really REST - I know - but sufficient for this example)
    http.HandleFunc("/payment", handleHttpRequest)
}

func initZeebe() {
	// connect to Zeebe
    newClient, err := zbc.NewClient(BrokerAddr)    
    if err != nil {
        panic(err)
    } else {
        zbClient = newClient
    }

    // deploy workflow model (if not yet on broker)
    _, err = zbClient.CreateWorkflowFromFile("default-topic", zbcommon.BpmnXml, "paymentV3.bpmn")
    if err != nil {
        panic(err)
    }

    // register job handler for 'charge-credit-card' and subscribe
    subscription, err := zbClient.JobSubscription("default-topic", "SomeWorker", "charge-credit-card",  1000, 32, hadleChargeCreditCardJob)
    if err != nil {
        panic(err)
    }
    subscription.StartAsync()    

    // disconnect nicely
    osCh := make(chan os.Signal, 1)
    signal.Notify(osCh, os.Interrupt)
    go func() {
        <-osCh
        err := subscription.Close()
        if err != nil {
            panic(err)
        }
        fmt.Println("Subscription closed.")
        os.Exit(0)
    }()
}

func handleHttpRequest(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "Retrieving payment by creating a charge on the credit card")	

    // fake payload
    jsonStr := `{"amount":"4990"}`

    chargeCreditCard(jsonStr, w)
    w.WriteHeader(http.StatusOK)
}

func chargeCreditCard(someDataAsJson string, w http.ResponseWriter) error {
    payload := make(map[string]interface{})
	json.Unmarshal([]byte(someDataAsJson), &payload)

    instance := zbc.NewWorkflowInstance("payment", -1, payload)
    msg, err := zbClient.CreateWorkflowInstance("default-topic", instance)

    if (err != nil) { 
        fmt.Fprintf(w, "Bam, error: " + err.Error())
        return err;
    } else {
        fmt.Fprintf(w, "Yeah, started: " + msg.String())
        return nil;
    }
}

func hadleChargeCreditCardJob(client zbsubscribe.ZeebeAPI, event *zbsubscriptions.SubscriptionEvent) {
    job, err := event.GetJob()
    if err != nil {
        panic(err)
    }	

    payloadJson := ""
    msgpack.Unmarshal(job.Payload, &payloadJson)

    _, err = doHttpCall(payloadJson)
    if err != nil {
        // couldn't do http call, fail job to trigger retry        
        _, err2 := client.FailJob(event)
        if err2 != nil {
        }
        //fmt.Println(response)
    } else {
        // complete job after processing
        _, err2 := client.CompleteJob(event)
        if err2 != nil {
        }
        //fmt.Println(response)
    }	
}

func doHttpCall(someDataAsJson string) (resp *http.Response, err error) {
    return http.Post("http://localhost:8099/charge", "application/json", strings.NewReader(someDataAsJson))
}